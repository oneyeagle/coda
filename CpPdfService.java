package ma.gov.acaps.common; // TODO: adjust to your package

import com.openhtmltopdf.bidi.support.ICUBidiReorderer;
import com.openhtmltopdf.bidi.support.ICUBidiSplitter;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.TextDirection;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;

/**
 * DYNAMIC counterpart of OpcPdfService — proof of the Thymeleaf pattern.
 *
 * Differences vs the static OPC service:
 * - Template processed per request via Thymeleaf (values change) — so NO
 *   result cache. Fonts and template are still loaded once.
 * - The engine MUST be Boot's auto-configured SpringTemplateEngine
 *   (SpringEL). A plain TemplateEngine throws
 *   ClassNotFoundException: ognl.PropertyAccessor in a Boot app
 *   (HANDOFF §3 lesson).
 *
 * The dot leaders in the template are pure CSS (dotted rule + white-masked
 * labels): the visible dot run self-fits to any value length. The Jasper
 * dot-count computation does not exist in this stack.
 */
@Service
public class CpPdfService {

    private final ITemplateEngine templateEngine; // inject SpringTemplateEngine
    private final byte[] fontRegular;
    private final byte[] fontBold;
    private final byte[] fontArabic;
    private final byte[] fontArabicBold;

    public CpPdfService(ITemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
        this.fontRegular = loadResource("fonts/NotoSans-Regular.ttf");
        this.fontBold = loadResource("fonts/NotoSans-Bold.ttf");
        this.fontArabic = loadResource("fonts/NotoNaskhArabic-Regular.ttf");
        this.fontArabicBold = loadResource("fonts/NotoNaskhArabic-Bold.ttf");
    }

    /** Renders the Conditions Particulières extract for the given data. */
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

    private static InputStream streamOf(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }

    private static byte[] loadResource(String path) {
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Missing classpath resource: " + path, e);
        }
    }
}
