package ma.sg.its.octroicreditapi.service.impl.opc;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ma.sg.its.octroicredit.common.exception.TechnicalException;
import ma.sg.its.octroicreditapi.dto.DossierDataDto;
import ma.sg.its.octroicreditapi.dto.LoanDetailValidationResult;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Slf4j
@Component
//@RequiredArgsConstructor
public class HtmlPdfRenderer {

    private final ProductDocumentRegistry registry;
    private final CommonParamsBuilder paramsBuilder;
    private final TemplateEngine templateEngine;

    private final byte[] fontRegular;
    private final byte[] fontBold;
    private final byte[] fontArabic;
    private final byte[] fontArabicBold;

    public HtmlPdfRenderer(
            ProductDocumentRegistry registry,
            CommonParamsBuilder paramsBuilder,
            TemplateEngine templateEngine) {
        this.registry = registry;
        this.paramsBuilder = paramsBuilder;
        this.templateEngine = templateEngine;

        // Load all fonts at startup
        this.fontRegular = loadFontResource("fonts/NotoSans-Regular.ttf");
        this.fontBold = loadFontResource("fonts/NotoSans-Bold.ttf");
        this.fontArabic = loadFontResource("fonts/NotoNaskhArabic-Regular.ttf");
        this.fontArabicBold = loadFontResource("fonts/NotoNaskhArabic-Bold.ttf");

        log.info("✓ Fonts loaded:");
        log.info("  Noto Sans Regular: {} KB", fontRegular.length / 1024);
        log.info("  Noto Sans Bold: {} KB", fontBold.length / 1024);
        log.info("  Noto Naskh Arabic Regular: {} KB", fontArabic.length / 1024);
        log.info("  Noto Naskh Arabic Bold: {} KB", fontArabicBold.length / 1024);
    }

    /**
     * Generate OPC PDF from dossier data
     */
    @SneakyThrows
    public byte[] generatePdf(String productCode,
                              DossierDataDto dossier,
                              LoanDetailValidationResult loanDetail) {

        String dossierCode = dossier.getCodeDossier();
        log.info("Generating OPC PDF - Product: {}, Dossier: {}", productCode, dossierCode);

        try {
            // 1. Get applicable document templates
            List<DocumentTemplate> templates =
                    registry.getApplicableTemplates(productCode, dossier, loanDetail);

            if (templates.isEmpty()) {
                throw new TechnicalException(
                        "No applicable templates found for product: " + productCode);
            }

            // 2. Build rendering context
            Map<String, Object> context = buildRenderContext(templates, dossier, loanDetail);

            // 3. Render HTML
            String html = renderTemplate("opc/main_report", context);

            // 4. Convert to PDF
            byte[] pdfBytes = convertHtmlToPdf(html, dossierCode);

            log.info("✓ Successfully generated OPC PDF - Dossier: {}, Size: {} bytes",
                    dossierCode, pdfBytes.length);
            return pdfBytes;

        } catch (TechnicalException e) {
            log.error("Technical error generating OPC PDF - Dossier: {}", dossierCode, e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error generating OPC PDF - Dossier: {}", dossierCode, e);
            throw new TechnicalException("PDF generation failed: " + dossierCode);
        }
    }

    /**
     * Build Thymeleaf context
     */
    private Map<String, Object> buildRenderContext(
            List<DocumentTemplate> templates,
            DossierDataDto dossier,
            LoanDetailValidationResult loanDetail) {

        Map<String, Object> context = new HashMap<>();

        Map<String, Object> commonParams = paramsBuilder.buildCommonParams(dossier, loanDetail);
        context.put("commonParams", commonParams);

        List<Map<String, Object>> sections = templates.stream()
                .map(template -> {
                    Map<String, Object> section = new HashMap<>();
                    section.put("fragmentName", template.getFragmentName());
                    section.put("displayName", template.getDisplayName());
                    section.put("data", template.prepareData(dossier, loanDetail));
                    return section;
                })
                .toList();

        context.put("sections", sections);
        context.put("productCode", dossier.getProduct().getCode());
        context.put("dossierCode", dossier.getCodeDossier());
        context.put("generatedAt", new Date());

        return context;
    }

    /**
     * Render HTML using Thymeleaf
     */
    private String renderTemplate(String templateName, Map<String, Object> context) {
        try {
            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(context);
            return templateEngine.process(templateName, thymeleafContext);
        } catch (Exception e) {
            log.error("Template rendering failed: {}", templateName, e);
            throw new TechnicalException("Failed to render template: " + templateName);
        }
    }

    /**
     * Convert HTML to PDF with FULL RTL/Arabic support
     */
    @SneakyThrows
    public byte[] convertHtmlToPdf(String html, String dossierCode) {

        try (ByteArrayOutputStream pdfStream = new ByteArrayOutputStream(256 * 1024)) {

            PdfRendererBuilder builder = new PdfRendererBuilder();

            // === PERFORMANCE ===
            builder.useFastMode();

            // === FONT REGISTRATION (MUST come before SVG/RTL setup) ===
            registerFont(builder, fontRegular, "Noto Sans", 400);
            registerFont(builder, fontBold, "Noto Sans", 700);
            registerFont(builder, fontArabic, "Noto Naskh Arabic", 400);
            registerFont(builder, fontArabicBold, "Noto Naskh Arabic", 700);

            // === SVG SUPPORT ===
//            builder.useSVGDrawer(new BatikSVGDrawer());

            // === RTL/ARABIC SUPPORT (openhtmltopdf-rtl-support) ===
            // This enables automatic RTL detection and bidi text reordering
//            builder.useDefaultReplacedElementFactory();

            // === HTML CONTENT ===
            builder.withHtmlContent(html, null);
            builder.toStream(pdfStream);

            // === RENDER ===
            builder.run();

            log.debug("✓ PDF rendered successfully");
            return pdfStream.toByteArray();

        } catch (Exception e) {
            log.error("PDF conversion failed for dossier: {}", dossierCode, e);
            throw new TechnicalException("PDF generation failed: " + e.getMessage());
        }
    }

    /**
     * Register a single font
     */
    private void registerFont(
            PdfRendererBuilder builder,
            byte[] fontBytes,
            String fontName,
            int weight) {

        try {
            builder.useFont(
                    () -> new ByteArrayInputStream(fontBytes),
                    fontName
                    // embed font
            );
            log.debug("✓ Registered font: {} (weight: {})", fontName, weight);
        } catch (Exception e) {
            log.warn("Failed to register font {} (weight {}): {}", fontName, weight, e.getMessage());
        }
    }

    /**
     * Load font from classpath
     */
    private static byte[] loadFontResource(String resourcePath) {
        try (InputStream in = new ClassPathResource(resourcePath).getInputStream()) {
            byte[] fontBytes = in.readAllBytes();
            if (fontBytes.length == 0) {
                throw new IOException("Font file is empty: " + resourcePath);
            }
            return fontBytes;
        } catch (IOException e) {
            throw new TechnicalException("Failed to load font: " + resourcePath);
        }
    }
}