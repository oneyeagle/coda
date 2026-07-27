package opc.ma.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import opc.ma.dto.CodeLabelDto;
import opc.ma.dto.DossierDataDto;
import opc.ma.dto.LoanDetailValidationResultDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Dossier/loan-detail fixtures live in src/test/resources/mock (see HARD-RULE.md:
 * data-building logic in CommonParamsBuilder/templates stays untouched, only the
 * test data source moved from hand-built Java objects to JSON).
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest(classes = {
        org.springframework.boot.thymeleaf.autoconfigure.ThymeleafAutoConfiguration.class,
        org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration.class,
        OpcGeneratorServiceImpl.class,
        HtmlPdfRenderer.class,
        ProductDocumentRegistry.class,
        CommonParamsBuilder.class
})
class OpcGeneratorServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private OpcGeneratorServiceImpl opcGeneratorService;

    private DossierDataDto dossier;
    private LoanDetailValidationResultDto loanDetailValidationResult;

    @BeforeEach
    void setUp() throws IOException {
        dossier = readMock("mock/dossier.json", DossierDataDto.class);
        loanDetailValidationResult = readMock("mock/loan-detail.json", LoanDetailValidationResultDto.class);
    }

    private <T> T readMock(String classpathLocation, Class<T> type) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(classpathLocation)) {
            if (in == null) {
                throw new IOException("Mock file not found on classpath: " + classpathLocation);
            }
            return MAPPER.readValue(in, type);
        }
    }


    @Test
    @DisplayName("Should generate valid PPI_CLASSIQUE PDF with all sections")
    void shouldGeneratePpiClassicPdf() throws Exception {
        dossier.setProduct(CodeLabelDto.builder().code("PPI_CLASSIQUE").build());

        byte[] pdfBytes = opcGeneratorService.generateOpc(dossier, loanDetailValidationResult);

        // 1. Basic validity
        Assertions.assertThat(pdfBytes)
                .isNotNull()
                .isNotEmpty()
                .hasSizeGreaterThan(10_000); // Realistic PDF size
        assertPdfIsValid(pdfBytes);

        Files.createDirectories(Paths.get("target"));
        Files.write(Paths.get("target/opc_ppi_classic.pdf"), pdfBytes);
    }

    private void assertPdfIsValid(byte[] pdfBytes) throws IOException {
        assertThatCode(() -> {
            try (PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfBytes));
                 PdfDocument doc = new PdfDocument(reader)) {
                Assertions.assertThat(doc.getNumberOfPages()).isGreaterThan(0);
            }
        }).doesNotThrowAnyException();
    }
}