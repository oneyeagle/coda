package opc.ma.service;

import opc.ma.config.PdfTemplateConfig;
import opc.ma.model.AttachCautionHypothecaireItem;
import opc.ma.model.AttachCautionSolidaireItem;
import opc.ma.model.AttachHypothecItem;
import opc.ma.model.HypothecProperty;
import opc.ma.model.OpcReportData;
import opc.ma.model.PcCautionItem;
import opc.ma.model.PcCustomerItem;
import opc.ma.model.PcPpiClassicData;
import opc.ma.model.PcPropertyItem;
import opc.ma.model.TitreFoncierRang;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.Loader;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpcPdfServiceTest {

    private final OpcPdfService service =
            new OpcPdfService(new PdfTemplateConfig().pdfTemplateEngine());

    @Test
    void generateOpcPdf() throws Exception {
        byte[] pdf = service.generateOpcPdf();

        // It is a real PDF
        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");

        try (PDDocument doc = Loader.loadPDF(pdf)) {
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

    @Test
    void generateOpcPdf_withAttachHypothec() throws Exception {
        OpcReportData data = OpcReportData.builder()
                .customerFullName("M. EXEMPLE CLIENT")
                .decisionDate("15/06/2026")
                .signedIn("Casablanca")
                .loanAmount("850 000,00 DH")
                .rateType("Fixe")
                .rate("5,10 %")
                .attachHypothecData(List.of(
                        new AttachHypothecItem(
                                List.of(new TitreFoncierRang("850 000,00 DH", "1")),
                                List.of(new HypothecProperty(
                                        "Résidence Exemple",
                                        "TF 12345/C",
                                        "Casablanca",
                                        "120 m²",
                                        "Appartement au 3ème étage"))
                        )
                ))
                .build();

        byte[] pdf = service.generateOpcPdf(data);

        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(doc);

            // General conditions still present ahead of the attachment
            assertThat(text).contains("OFFRE PREALABLE DE CREDIT IMMOBILIER");

            // Attach hypothec fragment rendered with the supplied data
            assertThat(text)
                    .contains("ANNEXE AU CONTRAT DE CREDIT")
                    .contains("ACTE D'HYPOTHEQUE")
                    .contains("M. EXEMPLE CLIENT")
                    .contains("850 000,00 DH")
                    .contains("Résidence Exemple")
                    .contains("TF 12345/C");

            assertThat(text).matches("(?s).*[\\u0600-\\u06FF\\uFE70-\\uFEFF]+.*");
        }

        Path out = Path.of("target/opc-attach-hypothec.pdf");
        Files.write(out, pdf);
        System.out.println("PDF written to: " + out.toAbsolutePath());
    }

    @Test
    void generateOpcPdf_withAttachCautionHypothecaire() throws Exception {
        OpcReportData data = OpcReportData.builder()
                .customerFullName("M. EXEMPLE CLIENT")
                .decisionDate("15/06/2026")
                .signedIn("Casablanca")
                .loanAmount("850 000,00 DH")
                .rateNature("Standard")
                .rate("5,10 %")
                .attachHypothecaireData(List.of(
                        new AttachCautionHypothecaireItem(
                                "M. EXEMPLE CAUTION",
                                "Rabat",
                                "AB123456",
                                "Résidence Exemple",
                                "TF 12345/C",
                                "Casablanca",
                                "120 m²",
                                "Appartement au 3ème étage",
                                List.of(new TitreFoncierRang("850 000,00 DH", "1"))
                        )
                ))
                .build();

        byte[] pdf = service.generateOpcPdf(data);

        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(doc);

            assertThat(text).contains("OFFRE PREALABLE DE CREDIT IMMOBILIER");

            assertThat(text)
                    .contains("ANNEXE AU CONTRAT DE CREDIT")
                    .contains("ACTE DE CAUTIONNEMENT HYPOTHECAIRE")
                    .contains("M. EXEMPLE CAUTION")
                    .contains("AB123456")
                    .contains("Résidence Exemple")
                    .contains("TF 12345/C");

            assertThat(text).matches("(?s).*[\\u0600-\\u06FF\\uFE70-\\uFEFF]+.*");
        }

        Path out = Path.of("target/opc-attach-caut-hypothecaire.pdf");
        Files.write(out, pdf);
        System.out.println("PDF written to: " + out.toAbsolutePath());
    }

    @Test
    void generateOpcPdf_withAttachCautionSolidaire() throws Exception {
        OpcReportData data = OpcReportData.builder()
                .customerFullName("M. EXEMPLE CLIENT")
                .decisionDate("15/06/2026")
                .signedIn("Casablanca")
                .loanAmount("850 000,00 DH")
                .rateNature("Standard")
                .rate("5,10 %")
                .attachSolidaireData(List.of(
                        new AttachCautionSolidaireItem(
                                "M. EXEMPLE GARANT",
                                "Rabat",
                                "AB123456"
                        )
                ))
                .build();

        byte[] pdf = service.generateOpcPdf(data);

        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(doc);

            assertThat(text).contains("OFFRE PREALABLE DE CREDIT IMMOBILIER");

            assertThat(text)
                    .contains("ANNEXE AU CONTRAT DE CREDIT")
                    .contains("ACTE DE CAUTIONNEMENT PERSONNEL ET SOLIDAIRE")
                    .contains("M. EXEMPLE GARANT")
                    .contains("AB123456")
                    .contains("solidairement avec")
                    .contains("M. EXEMPLE CLIENT");

            assertThat(text).matches("(?s).*[\\u0600-\\u06FF\\uFE70-\\uFEFF]+.*");
        }

        Path out = Path.of("target/opc-attach-caut-solidaire.pdf");
        Files.write(out, pdf);
        System.out.println("PDF written to: " + out.toAbsolutePath());
    }

    @Test
    void generateOpcPdf_withPcPpiClassic() throws Exception {
        PcPpiClassicData pcData = PcPpiClassicData.builder()
                .customers(List.of(
                        new PcCustomerItem(
                                "M. EXEMPLE CLIENT",
                                "12, rue Exemple, Casablanca",
                                "15/06/1985",
                                "Casablanca",
                                "AB123456",
                                "10/05/2020",
                                "Emprunteur",
                                null)
                ))
                .cautionBeneficiaries(List.of(
                        new PcCautionItem(
                                "M. EXEMPLE CAUTION",
                                "Rabat",
                                "02/02/1980",
                                "Rabat",
                                "CD654321",
                                "01/01/2019",
                                "Caution Hypothécaire")
                ))
                .properties(List.of(
                        new PcPropertyItem("TF 12345/C", "Casablanca", "Appartement au 3ème étage")
                ))
                .warranties(List.of("Nantissement de fonds de commerce"))
                .reserves(List.of("Production de l'attestation d'assurance habitation"))
                .warrantyAmount("850 000,00 DH")
                .warrantyRang(1)
                .warrantyLandCertificate("TF 12345/C")
                .loanObject("Acquisition d'un bien immobilier")
                .loanAmountAmplitude("850 000,00 DH")
                .loanDuration("240")
                .globalEffectiveRate("6,10 %")
                .rateType("Fixe")
                .rate("5,10")
                .loanApplicationFee("1 000,00 DH")
                .freeHandFee("500,00 DH")
                .insuranceCost("2 400,00 DH")
                .customerFullName("M. EXEMPLE CLIENT")
                .bonusRate("0,35 %")
                .dueAmount("185,00 DH")
                .dueAmountTVA("210,00 DH")
                .periodicity("Mensuelle")
                .firstInstallmentDate("01/08/2026")
                .lastInstallmentDate("01/08/2046")
                .totalCreditCost("85 000,00 DH")
                .customerAgencyCode("Agence Casablanca Centre")
                .customerRib("0123456789012345678901")
                .signedIn("Casablanca")
                .decisionDate("15/06/2026")
                .build();

        OpcReportData data = OpcReportData.builder()
                .pcName("Leaf_Grey")
                .pcData(pcData)
                .build();

        byte[] pdf = service.generateOpcPdf(data);

        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(doc);

            assertThat(text).contains("OFFRE PREALABLE DE CREDIT IMMOBILIER");

            assertThat(text)
                    .contains("CONDITIONS PARTICULIERS")
                    .contains("IDENTIFICATION DES PARTIES")
                    .contains("M. EXEMPLE CLIENT")
                    .contains("M. EXEMPLE CAUTION")
                    .contains("ARTICLE 2 : OBJET")
                    .contains("TF 12345/C")
                    .contains("CARACTERISTIQUES DU")
                    .contains("ARTICLE 4 : DOMICILIATION BANCAIRE")
                    .contains("ARTICLE 5 : GARANTIES")
                    .contains("CONDITIONS SUSPENSIVES")
                    .contains("Nantissement de fonds de commerce")
                    .contains("Production de l'attestation d'assurance habitation");

            assertThat(text).matches("(?s).*[\\u0600-\\u06FF\\uFE70-\\uFEFF]+.*");
        }

        Path out = Path.of("target/opc-pc-ppi-classic.pdf");
        Files.write(out, pdf);
        System.out.println("PDF written to: " + out.toAbsolutePath());
    }

    @Test
    void generateOpcPdf_fullDocument() throws Exception {
        PcPpiClassicData pcData = PcPpiClassicData.builder()
                .customers(List.of(
                        new PcCustomerItem(
                                "M. EXEMPLE CLIENT",
                                "12, rue Exemple, Casablanca",
                                "15/06/1985",
                                "Casablanca",
                                "AB123456",
                                "10/05/2020",
                                "Emprunteur",
                                null)
                ))
                .cautionBeneficiaries(List.of(
                        new PcCautionItem(
                                "M. EXEMPLE CAUTION",
                                "Rabat",
                                "02/02/1980",
                                "Rabat",
                                "CD654321",
                                "01/01/2019",
                                "Caution Hypothécaire")
                ))
                .properties(List.of(
                        new PcPropertyItem("TF 12345/C", "Casablanca", "Appartement au 3ème étage")
                ))
                .warranties(List.of("Nantissement de fonds de commerce"))
                .reserves(List.of("Production de l'attestation d'assurance habitation"))
                .warrantyAmount("850 000,00 DH")
                .warrantyRang(1)
                .warrantyLandCertificate("TF 12345/C")
                .loanObject("Acquisition d'un bien immobilier")
                .loanAmountAmplitude("850 000,00 DH")
                .loanDuration("240")
                .globalEffectiveRate("6,10 %")
                .rateType("Fixe")
                .rate("5,10")
                .loanApplicationFee("1 000,00 DH")
                .freeHandFee("500,00 DH")
                .insuranceCost("2 400,00 DH")
                .customerFullName("M. EXEMPLE CLIENT")
                .bonusRate("0,35 %")
                .dueAmount("185,00 DH")
                .dueAmountTVA("210,00 DH")
                .periodicity("Mensuelle")
                .firstInstallmentDate("01/08/2026")
                .lastInstallmentDate("01/08/2046")
                .totalCreditCost("85 000,00 DH")
                .customerAgencyCode("Agence Casablanca Centre")
                .customerRib("0123456789012345678901")
                .signedIn("Casablanca")
                .decisionDate("15/06/2026")
                .build();

        OpcReportData data = OpcReportData.builder()
                .pcName("Leaf_Grey")
                .pcData(pcData)
                .customerFullName("M. EXEMPLE CLIENT")
                .decisionDate("15/06/2026")
                .signedIn("Casablanca")
                .loanAmount("850 000,00 DH")
                .rateType("Fixe")
                .rateNature("Standard")
                .rate("5,10 %")
                .attachHypothecData(List.of(
                        new AttachHypothecItem(
                                List.of(new TitreFoncierRang("850 000,00 DH", "1")),
                                List.of(new HypothecProperty(
                                        "Résidence Exemple",
                                        "TF 12345/C",
                                        "Casablanca",
                                        "120 m²",
                                        "Appartement au 3ème étage"))
                        )
                ))
                .attachHypothecaireData(List.of(
                        new AttachCautionHypothecaireItem(
                                "M. EXEMPLE CAUTION",
                                "Rabat",
                                "AB123456",
                                "Résidence Exemple",
                                "TF 12345/C",
                                "Casablanca",
                                "120 m²",
                                "Appartement au 3ème étage",
                                List.of(new TitreFoncierRang("850 000,00 DH", "1"))
                        )
                ))
                .attachSolidaireData(List.of(
                        new AttachCautionSolidaireItem(
                                "M. EXEMPLE GARANT",
                                "Rabat",
                                "AB123456"
                        )
                ))
                .build();

        byte[] pdf = service.generateOpcPdf(data);

        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(doc);

            // 1. General conditions (always present)
            assertThat(text)
                    .contains("OFFRE PREALABLE DE CREDIT IMMOBILIER")
                    .contains("CONDITIONS GENERALES")
                    .contains("PREAMBULE");

            // 2. Conditions Particulières (pc_ppi_classic)
            assertThat(text)
                    .contains("CONDITIONS PARTICULIERS")
                    .contains("IDENTIFICATION DES PARTIES")
                    .contains("ARTICLE 5 : GARANTIES");

            // 3. Attach Hypothec
            assertThat(text)
                    .contains("ACTE D'HYPOTHEQUE")
                    .contains("Résidence Exemple");

            // 4. Attach Caution Hypothécaire
            assertThat(text)
                    .contains("ACTE DE CAUTIONNEMENT HYPOTHECAIRE")
                    .contains("M. EXEMPLE CAUTION");

            // 5. Attach Caution Solidaire
            assertThat(text)
                    .contains("ACTE DE CAUTIONNEMENT PERSONNEL ET SOLIDAIRE")
                    .contains("M. EXEMPLE GARANT");

            assertThat(text).matches("(?s).*[\\u0600-\\u06FF\\uFE70-\\uFEFF]+.*");
        }

        Path out = Path.of("target/opc-full.pdf");
        Files.write(out, pdf);
        System.out.println("PDF written to: " + out.toAbsolutePath());
    }
}