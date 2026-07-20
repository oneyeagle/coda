package opc.ma.service;

import com.openhtmltopdf.bidi.support.ICUBidiReorderer;
import com.openhtmltopdf.bidi.support.ICUBidiSplitter;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.TextDirection;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import opc.ma.model.AttachCautionHypothecaireView;
import opc.ma.model.AttachCautionSolidaireView;
import opc.ma.model.AttachHypothecView;
import opc.ma.model.OpcReportData;
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
import java.util.List;

@Service
public class OpcPdfService {

    private static final String MAIN_TEMPLATE = "main_report";

    private final TemplateEngine templateEngine;
    private final byte[] fontRegular;
    private final byte[] fontBold;
    private final byte[] fontArabic;
    private final byte[] fontArabicBold;

    public OpcPdfService(@Qualifier("pdfTemplateEngine") TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
        this.fontRegular = loadResource("fonts/NotoSans-Regular.ttf");
        this.fontBold = loadResource("fonts/NotoSans-Bold.ttf");
        this.fontArabic = loadResource("fonts/NotoNaskhArabic-Regular.ttf");
        this.fontArabicBold = loadResource("fonts/NotoNaskhArabic-Bold.ttf");
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

    public static final class OpcGenerationException extends RuntimeException {
        public OpcGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public byte[] generateOpcPdf() {
        return generateOpcPdf(OpcReportData.generalConditionsOnly());
    }

    public byte[] generateOpcPdf(OpcReportData data) {
        String html = templateEngine.process(MAIN_TEMPLATE, toContext(data));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream(64 * 1024)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useUnicodeBidiSplitter(new ICUBidiSplitter.ICUBidiSplitterFactory());
            builder.useUnicodeBidiReorderer(new ICUBidiReorderer());
            builder.defaultTextDirection(TextDirection.LTR);
            builder.useFont(() -> streamOf(fontRegular),    "Noto Sans", 500, FontStyle.NORMAL, true);
            builder.useFont(() -> streamOf(fontBold),       "Noto Sans", 800, FontStyle.NORMAL, true);
            builder.useFont(() -> streamOf(fontArabic),     "Noto Naskh Arabic", 400, FontStyle.NORMAL, true);
            builder.useFont(() -> streamOf(fontArabicBold), "Noto Naskh Arabic", 700, FontStyle.NORMAL, true);
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (IOException e) {
            throw new OpcGenerationException("Failed to render OPC PDF", e);
        }
    }

    private static Context toContext(OpcReportData data) {
        Context context = new Context();
        context.setVariable("pcName", data.pcName());
        context.setVariable("pcData", data.pcData());
        context.setVariable("attachHypothecName", data.attachHypothecName());
        context.setVariable("attachHypothecData", toHypothecViews(data));
        context.setVariable("attachHypothecaireName", data.attachHypothecaireName());
        context.setVariable("attachHypothecaireData", toHypothecaireViews(data));
        context.setVariable("attachSolidaireName", data.attachSolidaireName());
        context.setVariable("attachSolidaireData", toSolidaireViews(data));
        return context;
    }

    private static List<AttachHypothecView> toHypothecViews(OpcReportData data) {
        if (data.attachHypothecData() == null) {
            return null;
        }
        return data.attachHypothecData().stream()
                .map(item -> new AttachHypothecView(
                        data.customerFullName(),
                        data.decisionDate(),
                        data.signedIn(),
                        data.loanAmount(),
                        data.rateType(),
                        data.rate(),
                        item.rangs(),
                        item.properties()))
                .toList();
    }

    private static List<AttachCautionHypothecaireView> toHypothecaireViews(OpcReportData data) {
        if (data.attachHypothecaireData() == null) {
            return null;
        }
        return data.attachHypothecaireData().stream()
                .map(item -> new AttachCautionHypothecaireView(
                        data.customerFullName(),
                        data.decisionDate(),
                        data.signedIn(),
                        data.loanAmount(),
                        data.rateNature(),
                        data.rate(),
                        item.beneficiaryFullName(),
                        item.beneficiaryAddress(),
                        item.beneficiaryCardID(),
                        item.propertyName(),
                        item.propertyLandCertificate(),
                        item.propertyCity(),
                        item.propertyArea(),
                        item.enrichment(),
                        item.propertyRanks()))
                .toList();
    }

    private static List<AttachCautionSolidaireView> toSolidaireViews(OpcReportData data) {
        if (data.attachSolidaireData() == null) {
            return null;
        }
        return data.attachSolidaireData().stream()
                .map(item -> new AttachCautionSolidaireView(
                        data.customerFullName(),
                        data.decisionDate(),
                        data.signedIn(),
                        data.loanAmount(),
                        data.rateNature(),
                        data.rate(),
                        item.beneficiaryFullName(),
                        item.beneficiaryAddress(),
                        item.beneficiaryCardID()))
                .toList();
    }
}