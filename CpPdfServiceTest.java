package ma.gov.acaps.common;

import ma.gov.acaps.common.CpData;
import ma.gov.acaps.common.CpPdfService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * No Spring context needed: a SpringTemplateEngine is built manually
 * (it works standalone and keeps SpringEL — never plain TemplateEngine,
 * which crashes on OGNL in a Boot classpath, HANDOFF §3).
 */
class CpPdfServiceTest {

    private static SpringTemplateEngine engine() {
        ClassLoaderTemplateResolver r = new ClassLoaderTemplateResolver();
        r.setPrefix("templates/");
        r.setSuffix(".html");
        r.setTemplateMode(TemplateMode.HTML);
        r.setCharacterEncoding("UTF-8");
        SpringTemplateEngine e = new SpringTemplateEngine();
        e.setTemplateResolver(r);
        return e;
    }

    private final StatementPdfService service = new StatementPdfService(engine());

    private static final CpData SAMPLE = new CpData(
            "240 mois", "9,90 %", "5,10", "4 850,00", "2 500,00 DH HT");

    @Test
    void generatesDynamicBilingualCpPdf() throws Exception {
        byte[] pdf = service.generateCpPdf(SAMPLE);

        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");

        try (PDDocument doc = PDDocument.load(pdf)) {
            String text = new PDFTextStripper().getText(doc);

            // Injected values present (French side: exact asserts)
            assertThat(text)
                .contains("CONDITIONS PARTICULIERES")
                .contains("Durée Globale du Crédit")
                .contains("240 mois")            // ${cp.dureeGlobale}
                .contains("9,90 %")              // ${cp.teg}
                .contains("5,10")                // ${cp.taux} (both sides)
                .contains("4 850,00")            // ${cp.echeanceTtc}
                .contains("2 500,00 DH HT");     // ${cp.fraisDossier}

            // Arabic present (presence only — shaped glyph extraction is
            // unreliable by design, HANDOFF §6)
            assertThat(text).matches("(?s).*[\\u0600-\\u06FF\\uFE70-\\uFEFF]+.*");

            // NOTE: the dot leaders are a dotted CSS border, not text —
            // they are intentionally NOT assertable via text extraction.
            // Their self-fitting behavior is verified visually on target/cp.pdf
            // (short vs long values below).
            assertThat(doc.getNumberOfPages()).isEqualTo(1);
        }

        Files.write(Path.of("target/cp.pdf"), pdf);
    }

    @Test
    void leaderSelfFitsForLongValues() throws Exception {
        // Same template, much longer values: must still render on one page
        // with no exception and both values present — the dot run absorbs
        // the difference (visual check: target/cp-long.pdf).
        CpData longData = new CpData(
                "360 mois dont 24 de différé", "11,4567 %", "6,25",
                "12 480,75", "12 750,00 DH HT + timbres");

        byte[] pdf = service.generateCpPdf(longData);

        try (PDDocument doc = PDDocument.load(pdf)) {
            String text = new PDFTextStripper().getText(doc);
            assertThat(text)
                .contains("360 mois dont 24 de différé")
                .contains("12 750,00 DH HT + timbres");
            assertThat(doc.getNumberOfPages()).isEqualTo(1);
        }

        Files.write(Path.of("target/cp-long.pdf"), pdf);
    }
}
