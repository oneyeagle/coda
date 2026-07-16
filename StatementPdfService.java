package ma.gov.acaps.common;

import com.openhtmltopdf.bidi.support.ICUBidiReorderer;
import com.openhtmltopdf.bidi.support.ICUBidiSplitter;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.TextDirection;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.Map;

@Service
public class StatementPdfService {

    private static final String TEMPLATE = "statement";
    private static final String FONT_FAMILY = "Amiri";

    private final TemplateEngine templateEngine;
    private final byte[] fontRegular;
    private final byte[] fontBold;
    private final byte[] fontArabic;
    private final byte[] fontArabicBold;

    public StatementPdfService(@Qualifier("pdfTemplateEngine") TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
        this.fontRegular =loadResource("fonts/NotoSans-Regular.ttf");
        this.fontBold = loadResource("fonts/NotoSans-Bold.ttf");
        this.fontArabic = loadResource("fonts/NotoNaskhArabic-Regular.ttf");
        this.fontArabicBold = loadResource("fonts/NotoNaskhArabic-Bold.ttf");
    }

    public byte[] generateStatementPdf(AccountDto data) {
        Context context = new Context(Locale.ROOT, Map.of("account", data));
        String html = templateEngine.process(TEMPLATE, context);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream(64 * 1024)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();

            // Bidi support from openhtmltopdf-rtl-support: correct run splitting/reordering
            builder.useUnicodeBidiSplitter(new ICUBidiSplitter.ICUBidiSplitterFactory());
            builder.useUnicodeBidiReorderer(new ICUBidiReorderer());
            builder.defaultTextDirection(TextDirection.LTR);

            // Font routing: family name here must match font-family in the CSS.
            // Fresh stream per render; bytes are cached, subsetting keeps the PDF small.
            builder.useFont(() -> streamOf(fontRegular), FONT_FAMILY, 400, FontStyle.NORMAL, true);
            builder.useFont(() -> streamOf(fontBold),    FONT_FAMILY, 700, FontStyle.NORMAL, true);

            builder.withHtmlContent(html, null); // null base URI: no external resource resolution
            builder.toStream(out);
            builder.run();

            return out.toByteArray();
        } catch (IOException e) {
            throw new StatementGenerationException("Failed to render statement PDF", e);
        }
    }

    private static InputStream streamOf(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }

    private static byte[] loadResource(String path) {
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Missing required classpath resource: " + path, e);
        }
    }

    public static final class StatementGenerationException extends RuntimeException {
        public StatementGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private final String opcHtml = new String(
            loadResource("templates/opc.html"), java.nio.charset.StandardCharsets.UTF_8);

    /** Lazily rendered once; the document is fully static. */
    private volatile byte[] opcPdfCache;

    public byte[] generateOpcPdf() {
        byte[] cached = opcPdfCache;
        if (cached != null) return cached.clone();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream(64 * 1024)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useUnicodeBidiSplitter(new ICUBidiSplitter.ICUBidiSplitterFactory());
            builder.useUnicodeBidiReorderer(new ICUBidiReorderer());
            builder.defaultTextDirection(TextDirection.LTR);
            builder.useFont(() -> streamOf(fontRegular),    "Noto Sans", 400, FontStyle.NORMAL, true);
            builder.useFont(() -> streamOf(fontBold),       "Noto Sans", 700, FontStyle.NORMAL, true);
            builder.useFont(() -> streamOf(fontArabic),     "Noto Naskh Arabic", 400, FontStyle.NORMAL, true);
            builder.useFont(() -> streamOf(fontArabicBold), "Noto Naskh Arabic", 700, FontStyle.NORMAL, true);
            builder.withHtmlContent(opcHtml, null);
            builder.toStream(out);
            builder.run();

            byte[] pdf = out.toByteArray();
            opcPdfCache = pdf;
            return pdf.clone();
        } catch (IOException e) {
            throw new StatementGenerationException("Failed to render OPC PDF", e);
        }
    }
    public byte[] generateCpPdf(CpData data) {
        Context ctx = new Context();
        ctx.setVariable("cp", data);
        // Boot default resolver: src/main/resources/templates/ + ".html"
        String html = templateEngine.process("cp", ctx);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream(32 * 1024)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useUnicodeBidiSplitter(new ICUBidiSplitter.ICUBidiSplitterFactory());
            builder.useUnicodeBidiReorderer(new ICUBidiReorderer());
            builder.defaultTextDirection(TextDirection.LTR);
            builder.useFont(() -> streamOf(fontRegular),    "Noto Sans",         400, FontStyle.NORMAL, true);
            builder.useFont(() -> streamOf(fontBold),       "Noto Sans",         700, FontStyle.NORMAL, true);
            builder.useFont(() -> streamOf(fontArabic),     "Noto Naskh Arabic", 400, FontStyle.NORMAL, true);
            builder.useFont(() -> streamOf(fontArabicBold), "Noto Naskh Arabic", 700, FontStyle.NORMAL, true);
            builder.withHtmlContent(html, null); // null base URI: no external resources
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (IOException e) {
            throw new StatementPdfService.StatementGenerationException("Failed to render CP PDF", e);
        }
    }
}