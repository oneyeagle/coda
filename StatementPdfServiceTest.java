package ma.gov.acaps.common;

import ma.gov.acaps.common.config.PdfTemplateConfig;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class StatementPdfServiceTest {

    private static StatementPdfService service;

    @BeforeAll
    static void setUp() {
        // Reuse the real production config — no Spring context needed
        service = new StatementPdfService(new PdfTemplateConfig().pdfTemplateEngine());
    }

    @Test
    void generatesValidPdfWithCorrectContent() throws Exception {
        byte[] pdf = service.generateStatementPdf(account(3));

        // Magic bytes: it is actually a PDF
        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");

        try (PDDocument doc = PDDocument.load(pdf)) {
            String text = new PDFTextStripper().getText(doc);
            assertThat(doc.getNumberOfPages()).isEqualTo(1);
            assertThat(text)
                    .contains("Jane Doe")
                    .contains("DE89 3704 0044 0532 0130 00")
                    .contains("Transaction 1")
                    .contains("1,234.56");
        }
    }

    @Test
    void paginatesLargeStatements_andRepeatsTableHeader() throws Exception {
        byte[] pdf = service.generateStatementPdf(account(200));

        try (PDDocument doc = PDDocument.load(pdf)) {
            assertThat(doc.getNumberOfPages()).isGreaterThan(1);

            // Header repetition: "Description" must appear on page 2 as well
            PDFTextStripper page2 = new PDFTextStripper();
            page2.setStartPage(2);
            page2.setEndPage(2);
            assertThat(page2.getText(doc)).contains("Description");
        }
    }

    @Test
    void escapesHostileInput() throws Exception {
        AccountDto hostile = new AccountDto(
                "<script>alert(1)</script>", "DE00", "EUR",
                LocalDate.now(), LocalDate.now(),
                BigDecimal.ZERO, BigDecimal.ZERO, List.of());

        byte[] pdf = service.generateStatementPdf(hostile);

        try (PDDocument doc = PDDocument.load(pdf)) {
            // Rendered as literal text, not parsed as markup
            assertThat(new PDFTextStripper().getText(doc)).contains("<script>");
        }
    }

    private static AccountDto account(int txCount) {
        List<TransactionDto> txs = IntStream.rangeClosed(1, txCount)
                .mapToObj(i -> new TransactionDto(
                        LocalDate.of(2026, 6, 1).plusDays(i % 28),
                        "Transaction " + i,
                        "REF-" + i,
                        i % 2 == 0 ? new BigDecimal("1234.56") : null,
                        i % 2 == 0 ? null : new BigDecimal("500.00"),
                        new BigDecimal("10000.00")))
                .toList();
        return new AccountDto("Jane Doe", "DE89 3704 0044 0532 0130 00", "EUR",
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30),
                new BigDecimal("10000.00"), new BigDecimal("12500.00"), txs);
    }

    @Test
    void writeSampleForVisualReview() throws Exception {
        java.nio.file.Path out = java.nio.file.Path.of("target/statement-sample.pdf");
        java.nio.file.Files.write(out, service.generateStatementPdf(account(80)));
        System.out.println("PDF written to: " + out.toAbsolutePath());
    }
}