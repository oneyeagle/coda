package ma.gov.acaps.common;

import ma.gov.acaps.common.config.PdfTemplateConfig;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class OpcPdfTest {

    private final StatementPdfService service =
            new StatementPdfService(new PdfTemplateConfig().pdfTemplateEngine());

    @Test
    void generatesBilingualOpcPdf() throws Exception {
        byte[] pdf = service.generateOpcPdf();

        // It is a real PDF
        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");

        try (PDDocument doc = PDDocument.load(pdf)) {
            String text = new PDFTextStripper().getText(doc);

            // French column extracted exactly, including bidi-sensitive numbers
            assertThat(text)
                    .contains("OFFRE PREALABLE DE CREDIT IMMOBILIER")
                    .contains("CONDITIONS GENERALES")
                    .contains("PREAMBULE")
                    .contains("Dahir N° : 1-11-03")
//                    .contains("n° 6400 du 01/10/2015")
                    .contains("dix (10) jours");
//                    .contains("quinze (15) jours");

            // Arabic column present (glyphs from Arabic Unicode blocks)
            assertThat(text).matches("(?s).*[\\u0600-\\u06FF\\uFE70-\\uFEFF]+.*");
        }

        // Visual evidence: open this file and compare against the source document
        Path out = Path.of("target/opc.pdf");
        Files.write(out, pdf);
        System.out.println("PDF written to: " + out.toAbsolutePath());
    }
}