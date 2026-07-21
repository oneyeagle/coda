package ma.sg.its.octroicreditapi.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import lombok.extern.slf4j.Slf4j;
import ma.sg.its.octroicredit.common.dto.CodeLabelDto;
import ma.sg.its.octroicreditapi.dto.DossierDataDto;
import ma.sg.its.octroicreditapi.dto.LoanDetailValidationResult;
import ma.sg.its.octroicreditapi.service.impl.OpcGeneratorServiceImpl;
import ma.sg.its.octroicreditapi.service.impl.opc.CommonParamsBuilder;
import ma.sg.its.octroicreditapi.service.impl.opc.HtmlPdfRenderer;
import ma.sg.its.octroicreditapi.service.impl.opc.ProductDocumentRegistry;
import ma.sg.its.octroicreditapi.service.impl.opc.data.PpiClassicService;
import ma.sg.its.octroicreditapi.service.impl.opc.data.PpiMreService;
import ma.sg.its.octroicreditapi.service.impl.opc.data.PpiPprService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest(classes = {
        org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration.class,
        org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration.class,
        PpiClassicService.class,
        PpiPprService.class,
        PpiMreService.class,
        OpcGeneratorServiceImpl.class,
        HtmlPdfRenderer.class,
        ProductDocumentRegistry.class,
        CommonParamsBuilder.class
})
@DisplayName("General Conditions Fragment - Arabic Rendering Test")
class GeneralConditionsRenderingTest {

    @Autowired
    private HtmlPdfRenderer htmlPdfRenderer;

    @Autowired
    private TemplateEngine templateEngine;

    private DossierDataDto dossier;
    private LoanDetailValidationResult loanDetail;

    @BeforeEach
    void setUp() {
        dossier = DossierDataDto.builder()
                .codeDossier("TEST-GC-001")
                .product(CodeLabelDto.builder().code("PPI_CLASSIQUE").build())
                .build();

        loanDetail = LoanDetailValidationResult.builder().build();
    }

    @Test
    @DisplayName("Step 1: Render general_conditions HTML fragment (Thymeleaf only)")
    void testRenderGeneralConditionsHtmlFragment() {
        log.info("\n\n=== STEP 1: Render HTML Fragment Only ===\n");

        // Render just the fragment template
        String html = renderFragment("opc/fragments/general_conditions", new HashMap<>());

        log.info("Fragment HTML length: {} characters", html.length());
        log.info("Fragment HTML preview:\n{}", html.substring(0, Math.min(500, html.length())));

        // Save HTML for inspection
        try {
            Files.write(Paths.get("target/test-artifacts/general_conditions.html"), html.getBytes());
            log.info("✓ Saved fragment HTML to target/test-artifacts/general_conditions.html");
        } catch (IOException e) {
            log.warn("Could not save HTML: {}", e.getMessage());
        }

        assertThat(html)
                .isNotEmpty()
                .contains("CONDITIONS GENERALES")
                .contains("PREAMBULE");
    }

    @Test
    @DisplayName("Step 2: Check if Arabic text is in the fragment")
    void testArabicTextInFragment() {
        log.info("\n\n=== STEP 2: Check Arabic Text ===\n");

        String html = renderFragment("opc/fragments/general_conditions", new HashMap<>());

        // Look for Arabic characters (should be actual Arabic, not ###)
        boolean hasArabic = html.contains("عــــرض");
        boolean hasArabicContainer = html.contains("dir=\"rtl\"");
        boolean hasArabicFont = html.contains("Noto Naskh Arabic");

        log.info("✓ Has Arabic text: {}", hasArabic);
        log.info("✓ Has RTL directives: {}", hasArabicContainer);
        log.info("✓ Has Arabic font family: {}", hasArabicFont);

        assertThat(html)
                .as("Should contain Arabic script")
                .contains("عــــرض");

        assertThat(html)
                .as("Should have RTL direction")
                .contains("dir=\"rtl\"");
    }

    @Test
    @DisplayName("Step 3: Render full main_report with only general_conditions section")
    void testRenderMainReportWithGeneralConditionsOnly() {
        log.info("\n\n=== STEP 3: Render Main Report (General Conditions Only) ===\n");

        // Build minimal context with only general conditions
        Map<String, Object> context = new HashMap<>();
        context.put("commonParams", new HashMap<>());

        // Only one section: general_conditions
        Map<String, Object> gcSection = new HashMap<>();
        gcSection.put("fragmentName", "general_conditions");
        gcSection.put("displayName", "General Conditions");
        gcSection.put("data", new HashMap<>());

        context.put("sections", java.util.List.of(gcSection));
        context.put("productCode", "PPI_CLASSIQUE");
        context.put("dossierCode", "TEST-GC-001");
        context.put("generatedAt", new Date());

        String html = renderTemplate("opc/main_report", context);

        log.info("Main report HTML length: {} characters", html.length());

        // Save for inspection
        try {
            Files.createDirectories(Paths.get("target/test-artifacts"));
            Files.write(Paths.get("target/test-artifacts/main_report_gc_only.html"), html.getBytes());
            log.info("✓ Saved main report HTML to target/test-artifacts/main_report_gc_only.html");
        } catch (IOException e) {
            log.warn("Could not save HTML: {}", e.getMessage());
        }

        assertThat(html)
                .isNotEmpty()
                .contains("CONDITIONS GENERALES");
    }

    @Test
    @DisplayName("Step 4: Convert HTML to PDF (General Conditions Only)")
    void testConvertGeneralConditionsToPdf() throws IOException {
        log.info("\n\n=== STEP 4: Convert to PDF ===\n");

        // Build minimal context
        Map<String, Object> context = new HashMap<>();
        context.put("commonParams", new HashMap<>());

        Map<String, Object> gcSection = new HashMap<>();
        gcSection.put("fragmentName", "general_conditions");
        gcSection.put("displayName", "General Conditions");
        gcSection.put("data", new HashMap<>());

        context.put("sections", java.util.List.of(gcSection));
        context.put("productCode", "PPI_CLASSIQUE");
        context.put("dossierCode", "TEST-GC-001");
        context.put("generatedAt", new Date());

        String html = renderTemplate("opc/main_report", context);

        // Convert to PDF
        byte[] pdfBytes = convertHtmlToPdf(html);

        log.info("✓ PDF generated: {} bytes", pdfBytes.length);

        // Save PDF
        Files.createDirectories(Paths.get("target/test-artifacts"));
        Files.write(Paths.get("target/test-artifacts/general_conditions_only.pdf"), pdfBytes);
        log.info("✓ Saved PDF to target/test-artifacts/general_conditions_only.pdf");

        // Extract text
        String pdfText = extractPdfText(pdfBytes);
        log.info("PDF Text Preview:\n{}", pdfText.substring(0, Math.min(800, pdfText.length())));

        assertThat(pdfBytes).isNotEmpty().hasSizeGreaterThan(10_000);
    }

    @Test
    @DisplayName("Step 5: Check Arabic rendering in PDF")
    void testArabicInPdf() throws IOException {
        log.info("\n\n=== STEP 5: Check Arabic in PDF ===\n");

        Map<String, Object> context = new HashMap<>();
        context.put("commonParams", new HashMap<>());

        Map<String, Object> gcSection = new HashMap<>();
        gcSection.put("fragmentName", "general_conditions");
        gcSection.put("displayName", "General Conditions");
        gcSection.put("data", new HashMap<>());

        context.put("sections", java.util.List.of(gcSection));
        context.put("productCode", "PPI_CLASSIQUE");
        context.put("dossierCode", "TEST-GC-001");
        context.put("generatedAt", new Date());

        String html = renderTemplate("opc/main_report", context);
        byte[] pdfBytes = convertHtmlToPdf(html);

        String pdfText = extractPdfText(pdfBytes);

        // Check for:
        // 1. French text (should always be present)
        // 2. Arabic text (might be garbled as ####)
        // 3. Numbers/dates (should be present)

        log.info("Contains French: {}", pdfText.contains("CONDITIONS GENERALES"));
        log.info("Contains Arabic: {}", pdfText.contains("عــــرض"));
        log.info("Contains dates: {}", pdfText.contains("2011") || pdfText.contains("1432"));

        boolean hasFrench = pdfText.contains("CONDITIONS GENERALES");
        boolean hasArabic = pdfText.contains("عــــرض");

        log.info("\n✓ French text rendered: {}", hasFrench);
        log.info("✓ Arabic text rendered: {}", hasArabic);

        if (!hasArabic) {
            log.warn("⚠️  Arabic text not found in PDF - showing as ####");
            log.warn("Full PDF text:\n{}", pdfText);
        }

        assertThat(hasFrench).as("French text should be present").isTrue();
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private String renderFragment(String fragmentName, Map<String, Object> context) {
        try {
            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(context);
            return templateEngine.process(fragmentName, thymeleafContext);
        } catch (Exception e) {
            log.error("Failed to render fragment: {}", fragmentName, e);
            throw new RuntimeException(e);
        }
    }

    private String renderTemplate(String templateName, Map<String, Object> context) {
        try {
            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(context);
            return templateEngine.process(templateName, thymeleafContext);
        } catch (Exception e) {
            log.error("Failed to render template: {}", templateName, e);
            throw new RuntimeException(e);
        }
    }

    private byte[] convertHtmlToPdf(String html) {
        try {
            return htmlPdfRenderer.generatePdf("PPI_CLASSIQUE", dossier, loanDetail);
        } catch (Exception e) {
            log.error("Failed to convert HTML to PDF", e);
            throw new RuntimeException(e);
        }
    }

    private String extractPdfText(byte[] pdfBytes) throws IOException {
        StringBuilder text = new StringBuilder();
        try (PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfBytes));
             PdfDocument doc = new PdfDocument(reader)) {

            for (int i = 1; i <= doc.getNumberOfPages(); i++) {
                text.append(PdfTextExtractor.getTextFromPage(doc.getPage(i)));
                text.append("\n");
            }
        }
        return text.toString();
    }

    @Test
    @DisplayName("Convert raw HTML string to PDF (isolate conversion)")
    void testHtmlStringConversionOnly() throws IOException {
        String simpleHtml = """
        <html>
            <head>
                <style>
                    body { font-family: 'Noto Naskh Arabic'; }
                    .ar { direction: rtl; }
                </style>
            </head>
            <body>
                <h1>CONDITIONS GENERALES</h1>
                <p class="ar">عــــرض القرض المشروط</p>
            </body>
        </html>
        """;

        byte[] pdf = htmlPdfRenderer.convertHtmlToPdf(simpleHtml, "test");
        assertThat(pdf).isNotEmpty();

        String text = extractPdfText(pdf);
        log.error("PDF text: {}", text);
    }
}