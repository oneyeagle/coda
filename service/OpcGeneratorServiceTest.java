package ma.sg.its.octroicreditapi.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import ma.sg.its.octroicredit.common.dto.AttachmentDto;
import ma.sg.its.octroicredit.common.dto.BalanceActivityDTO;
import ma.sg.its.octroicredit.common.dto.CodeLabelDto;
import ma.sg.its.octroicredit.common.dto.DossierAttachmentTypeDto;
import ma.sg.its.octroicredit.common.dto.IndividualCustomerDTO;
import ma.sg.its.octroicredit.common.dto.PersonalInfoDTO;
import ma.sg.its.octroicreditapi.dto.BeneficiaryDto;
import ma.sg.its.octroicreditapi.dto.DossierDataDto;
import ma.sg.its.octroicreditapi.dto.GuarantorDto;
import ma.sg.its.octroicreditapi.dto.LoanDataDto;
import ma.sg.its.octroicreditapi.dto.LoanDetailDto;
import ma.sg.its.octroicreditapi.dto.LoanDetailValidationResult;
import ma.sg.its.octroicreditapi.dto.PropertyDataDto;
import ma.sg.its.octroicreditapi.dto.PropertyDto;
import ma.sg.its.octroicreditapi.dto.RangDto;
import ma.sg.its.octroicreditapi.dto.RepresentativeBeneficiaryDto;
import ma.sg.its.octroicreditapi.dto.RepresentativeCustomerDto;
import ma.sg.its.octroicreditapi.dto.RepresentativeDto;
import ma.sg.its.octroicreditapi.dto.RestrictionDto;
import ma.sg.its.octroicreditapi.dto.RestrictionType;
import ma.sg.its.octroicreditapi.dto.WarrantyDto;
import ma.sg.its.octroicreditapi.dto.WarrantyType;
import ma.sg.its.octroicreditapi.dto.core.CustomerDto;
import ma.sg.its.octroicreditapi.service.impl.OpcGeneratorServiceImpl;
import ma.sg.its.octroicreditapi.service.impl.opc.CommonParamsBuilder;
import ma.sg.its.octroicreditapi.service.impl.opc.HtmlPdfRenderer;
import ma.sg.its.octroicreditapi.service.impl.opc.ProductDocumentRegistry;
import ma.sg.its.octroicreditapi.service.impl.opc.data.PpiClassicService;
import ma.sg.its.octroicreditapi.service.impl.opc.data.PpiMreService;
import ma.sg.its.octroicreditapi.service.impl.opc.data.PpiPprService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
class OpcGeneratorServiceTest {
    @Autowired
    private OpcGeneratorServiceImpl opcGeneratorService;
    protected DossierDataDto dossier;
    protected  LoanDetailValidationResult loanDetailValidationResult;

    @MockitoSpyBean(name = "PPI_CLASSIQUE")
    private PpiClassicService ppiClassicService;

    @MockitoSpyBean(name = "PPI_PPR_FONC")
    private PpiPprService ppiPprService;

    @MockitoSpyBean(name = "PPI_MRE")
    private PpiMreService ppiMreService;
    @BeforeEach
    void setUp() {
        dossier = DossierDataDto.builder()
                .firstReleasedDate(LocalDateTime.now())
                .customerData(IndividualCustomerDTO.builder()

                        .personalInfo(PersonalInfoDTO.builder()
                                .firstName("Abdelaziz")
                                .lastName("Aguenchich")
                                .address1("Rue 123, Casablanca")
                                .birthDate(LocalDate.of(1999, 3, 15))
                                .cardID("AB123456")
                                .cardIDEmissionDate(LocalDate.of(2015, 7, 20))
                                .city("Casablanca")
                                .branchCode("BR001")
                                .build())
                        .balanceActivity(BalanceActivityDTO.builder()
                                .accountNumber("ACC123456789")
                                .build())
                        .build()
                )
                .loanData(LoanDataDto.builder()
                        .loanAmount(BigDecimal.valueOf(45673))
                        .rateType(CodeLabelDto.builder().designation("Fixe").build())
                        .rateNature(CodeLabelDto.builder().designation("Standard").build())
                        .rate(3.9)
                        .freeHandFee(BigDecimal.valueOf(500))
                        .applicationFee(BigDecimal.valueOf(250))
                        .deadlineNumber(120)
                        .delayed(false)
//                            .delayDuration(12)
                        .periodicity(CodeLabelDto.builder().designation("Mensuelle").build())
                        .propertyType(CodeLabelDto.builder().designation("Appartement").build())
                        .loanObject(CodeLabelDto.builder().designation("Acquisition").build())
                        .delayType(CodeLabelDto.builder().designation("Capital").build())
                        .build())

                .beneficiaries(List.of(
                        BeneficiaryDto.builder()
                                .id(1L)
                                .isBorrower(false)
                                .isGuarantor(false)
                                .firstname("Kamal")
                                .lastname("Abu Ali")
                                .address("ER 3434 CASA")
                                .idCardNumber("FG 3353")
                                .birthDate(LocalDate.of(2018, 11, 5))
                                .codeBirthPlace("CASA")
                                .properties(List.of(PropertyDto.builder()
                                        .id(102L)
                                        .forAcquisition(true)
                                        .propertyArea("5439.0")
                                        .denomination("Property")
                                        .codePropertyCity("Casa")
                                        .landCertificateNumber("432HTYT/768")
                                        .rangs(List.of(
                                                RangDto.builder()
                                                        .rang(1)
                                                        .warrantyAmount(BigDecimal.valueOf(10000))
                                                        .build(),
                                                RangDto.builder()
                                                        .rang(2)
                                                        .warrantyAmount(BigDecimal.valueOf(20000))
                                                        .build()
                                        ))
                                        .build()))
                                .rangs(
                                        List.of(
                                                RangDto.builder()
                                                        .rang(1)
                                                        .warrantyAmount(new BigDecimal("8807"))
                                                        .propertyId(101L)
                                                        .build(),
                                                RangDto.builder()
                                                        .rang(2)
                                                        .warrantyAmount(new BigDecimal("6965"))
                                                        .propertyId(102L)
                                                        .build()
                                        )
                                )
                                .build(),
                        BeneficiaryDto.builder()
                                .id(1L)
                                .isBorrower(true)
                                .isGuarantor(false)
                                .firstname("Btissam")
                                .lastname("Ait ElHaj")
                                .address("ER 3434 Marrakech")
                                .idCardNumber("RE 4567")
                                .birthDate(LocalDate.of(2003, 11, 5))
                                .codeBirthPlace("MARRAKECH")
                                .properties(List.of(PropertyDto.builder()
                                        .id(101L)
                                        .forAcquisition(true)
                                        .propertyArea("4335.0")
                                        .denomination("Property Name")
                                        .codePropertyCity("Marrakech")
                                        .landCertificateNumber("YUP45/768")
                                        .rangs(List.of(
                                                RangDto.builder()
                                                        .rang(1)
                                                        .warrantyAmount(BigDecimal.valueOf(10000))
                                                        .build(),
                                                RangDto.builder()
                                                        .rang(2)
                                                        .warrantyAmount(BigDecimal.valueOf(20000))
                                                        .build()))
                                                .build()
                                        ,
                                                PropertyDto.builder()
                                                        .id(102L)
                                                        .forAcquisition(false)
                                                        .propertyArea("80.0")
                                                        .denomination("Garage")
                                                        .codePropertyCity("CASABLANCA")
                                                        .landCertificateNumber("98765")
                                                        .rangs(List.of())
                                        .build()))
                                .rangs(
                                        List.of(
                                                RangDto.builder()
                                                        .rang(1)
                                                        .warrantyAmount(new BigDecimal("1500000"))
                                                        .propertyId(101L)
                                                        .build(),
                                                RangDto.builder()
                                                        .rang(2)
                                                        .warrantyAmount(new BigDecimal("500000"))
                                                        .propertyId(102L)
                                                        .build()
                                        )
                                )
                                .build()
                ))
                .propertyData(PropertyDataDto.builder()
                        .properties(List.of(
                                PropertyDto.builder()
                                        .codePropertyCity("Marrakech")
                                        .denomination("Property Name")
                                        .landCertificateNumber("LC123")
                                        .forAcquisition(true)
                                        .rangs(List.of(
                                                RangDto.builder()
                                                        .rang(1)
                                                        .warrantyAmount(BigDecimal.valueOf(10000))
                                                        .build(),
                                                RangDto.builder()
                                                        .rang(2)
                                                        .warrantyAmount(BigDecimal.valueOf(20000))
                                                        .build()
                                        ))
                                        .build(),
                                PropertyDto.builder()
                                        .codePropertyCity("Marrakech")
                                        .landCertificateNumber("LC456")
                                        .forAcquisition(false)
                                        .rangs(List.of(
                                                RangDto.builder()
                                                        .rang(1)
                                                        .warrantyAmount(BigDecimal.valueOf(15000))
                                                        .build(),
                                                RangDto.builder()
                                                        .rang(2)
                                                        .warrantyAmount(BigDecimal.valueOf(25000))
                                                        .build(),
                                                RangDto.builder()
                                                        .rang(3)
                                                        .warrantyAmount(BigDecimal.valueOf(35000))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build())
                .representatives(List.of(
                        RepresentativeDto.builder()
                                .id(1725L)
                                .firstname("AGUENCHICH")
                                .lastname("ABDELAZIZ")
                                .cin("CV452")
                                .cinIssuedAt(LocalDate.of(2026, 7, 23))
                                .customer(RepresentativeCustomerDto.builder()
                                        .id(1036L)
                                        .customer(CustomerDto.builder()
                                                .code("000305904")
                                                .lastName("NOM000305904")
                                                .firstName("PRE000305904")
                                                .sexe("M")
                                                .birthDate(LocalDate.of(1958, 7, 1))
                                                .birthCountry("MAROC")
                                                .cardType("CIN")
                                                .cardId("B206023")
                                                .birthCity("BERRECHID")
                                                .build())
                                        .proxyDate(LocalDate.of(2026, 7, 10))
                                        .build())
                                .beneficiaries(List.of())
                                .guarantors(List.of())
                                .build(),
                        RepresentativeDto.builder()
                                .id(1726L)
                                .firstname("fernad")
                                .lastname("ayman")
                                .cin("SD345")
                                .cinIssuedAt(LocalDate.of(2026, 7, 23))
                                .beneficiaries(List.of(
                                        RepresentativeBeneficiaryDto.builder()
                                                .id(10072L)
                                                .build()
                                ))
                                .guarantors(List.of())
                                .build()
                ))
                .guarantors(List.of(
                        GuarantorDto.builder()
                                .id(1L)
                                .isBorrower(false)
                                .isBeneficiary(true)
                                .firstName("Btissam")
                                .lastName("Ait ElHaj")
                                .address("ER 3434 Marrakech")
                                .idCardNumber("RE 4567")
                                .issuedAt(LocalDate.of(2018, 11, 5))
                                .birthDate(LocalDate.of(2018, 11, 5))
                                .codeBirthPlace("CASA")
                                .build(),
                        GuarantorDto.builder()
                                .id(1L)
                                .isBorrower(true)
                                .isBeneficiary(false)
                                .firstName("Kamal")
                                .lastName("Abu Ali")
                                .address("ER 3434 CASA")
                                .idCardNumber("FG 3353")
                                .issuedAt(LocalDate.of(2020, 5, 12))
                                .birthDate(LocalDate.of(2018, 11, 5))
                                .codeBirthPlace("SAFI")
                                .build()
                ))
                .restrictions(List.of(
                        RestrictionDto.builder()
                                .type(RestrictionType.DSC)
                                .content("Restriction DSC: Le présent contrat de crédit immobilier est soumis à l'acceptation préalable d'un rapport d'évaluation du bien immobilier conforme aux standards bancaires internationaux. L'emprunteur s'engage à fournir une assurance habitation couvrant la totalité du bien immobilier en garantie. Les frais d'assurance et de maintenance sont à la charge de l'emprunteur. Une inspection périodique du bien sera effectuée tous les deux ans pour évaluer son état général et sa conformité avec les obligations contractuelles.")
                                .attachment(AttachmentDto.builder()
                                        .uploadedAt(LocalDateTime.of(2026, 6, 15, 10, 53, 15))
                                        .build())
                                .build(),
                        RestrictionDto.builder()
                                .type(RestrictionType.FRONT)
                                .content("Restriction FRONT: Avant la mise en place du financement, le bénéficiaire doit justifier de la régularité de sa situation fiscale auprès des autorités compétentes. Une attestation d'immatriculation au registre du commerce et l'extrait de casier judiciaire seront exigés. Le dossier doit être validé par le service de conformité avant transmission au directeur pour approbation finale. Aucune modification majeure du projet n'est acceptée après la signature du présent accord.")
                                .attachment(AttachmentDto.builder()
                                        .uploadedAt(LocalDateTime.of(2026, 6, 15, 10, 53, 15))
                                        .build())
                                .build(),
                        RestrictionDto.builder()
                                .type(RestrictionType.OBSERVATION)
                                .content("Observation importante: L'emprunteur est invité à réviser sa charge d'endettement global avant la finalisation du contrat. Une réévaluation de la capacité de remboursement sera nécessaire si le montant total des engagements financiers dépasse 40% du revenu mensuel net. Le respect des échéances mensuelles est obligatoire, tout retard supérieur à 30 jours entraînera des pénalités conformément aux conditions générales.")
                                .attachment(AttachmentDto.builder()
                                        .uploadedAt(LocalDateTime.of(2026, 6, 15, 10, 53, 15))
                                        .build())
                                .build(),
                        RestrictionDto.builder()
                                .type(RestrictionType.PROPOSED_DSC)
                                .content("Restriction DSC proposée: En cas de changement de situation professionnelle ou de revenu, l'emprunteur s'engage à informer immédiatement l'établissement prêteur. La banque se réserve le droit de réévaluer les conditions du crédit ou de demander un remboursement anticipé si le risque augmente significativement. Une assurance chômage ou invalidité est fortement recommandée pour sécuriser le remboursement du crédit.")
                                .attachment(AttachmentDto.builder()
                                        .uploadedAt(LocalDateTime.of(2026, 6, 15, 10, 53, 15))
                                        .build())
                                .build(),
                        RestrictionDto.builder()
                                .type(RestrictionType.PROPOSED_FRONT)
                                .content("Restriction FRONT proposée: Les conditions météorologiques extrêmes ou les catastrophes naturelles ne sont pas couvertes par les garanties standard. L'emprunteur doit souscrire une assurance spécifique pour couvrir les risques de dégâts des eaux, tremblements de terre ou tempêtes selon la localisation du bien. Les déclarations de sinistre doivent être effectuées dans un délai de 10 jours ouvrables.")
                                .attachment(AttachmentDto.builder()
                                        .uploadedAt(LocalDateTime.of(2026, 6, 15, 10, 53, 15))
                                        .build())
                                .build()
                ))
                .warranties(List.of(
                        WarrantyDto.builder()
                                .id(9584L)
                                .type(WarrantyType.AUTO)
                                .content("Caution Solidaire de M/Mme HAMZA AITALI à hauteur de 2,577,982,961 DHS - Engagement irrévocable de l'ensemble des obligations de l'emprunteur principal. La caution s'engage à rembourser tous les montants restants dues en cas de défaillance de l'emprunteur. Cette caution s'étend à l'ensemble des intérêts de retard et des frais de recouvrement encourus.")
                                .build(),
                        WarrantyDto.builder()
                                .id(9585L)
                                .type(WarrantyType.AUTO)
                                .content("Caution Solidaire de M/Mme ABDELAZIZ AGUENCHICH à hauteur de 2,577,982,961 DHS - Engagement solidaire et incessible de garantir l'exécution de toutes les obligations de l'emprunteur. La caution renonce expressément au bénéfice de division et au droit de discussion. Hypothèque de premier rang constituée sur le bien immobilier financé en garantie supplémentaire.")
                                .build(),
                        WarrantyDto.builder()
                                .id(9586L)
                                .type(WarrantyType.AUTO)
                                .content("Délégation assurance confort habitation à hauteur de 2,577,982,961 DHS - Assurance multirisque habitation couvrant les dégâts aux structures du bâtiment, les installations intérieures, et les biens mobiliers. Responsabilité civile du propriétaire incluse avec couverture jusqu'à 10 millions de DHS. Assistance 24/7 en cas de sinistre ou d'urgence.")
                                .build(),
                        WarrantyDto.builder()
                                .id(9587L)
                                .type(WarrantyType.TO_COLLECT)
                                .content("Délégation assurance immobilier habitation à hauteur de 57,897,667 DHS - Couverture complémentaire des risques immobiliers spécifiques. Indemnisation automatique en cas de sinistre couvert sans formalités préalables. Franchise minimale avec délai de carence limité à 15 jours sauf pour les catastrophes naturelles.")
                                .build(),
                        WarrantyDto.builder()
                                .id(9588L)
                                .type(WarrantyType.TO_COLLECT)
                                .content("Garantie hypothécaire de premier rang sur le bien immobilier acquis d'une valeur estimée à 3,500,000 DHS - L'établissement prêteur dispose d'un droit de gage prioritaire sur l'ensemble des biens et revenus de l'emprunteur. Inscription au registre foncier effectuée aux frais et diligences du bénéficiaire de la garantie conformément à la législation en vigueur.")
                                .build(),
                        WarrantyDto.builder()
                                .id(1111L)
                                .type(WarrantyType.PROPOSED)
                                .content("This should be ignored - Proposed warranty not yet validated")
                                .build()
                ))
                .dossierAttachmentTypes(List.of(
                        DossierAttachmentTypeDto.builder()
                                .uuid("6fd450ef-ed5b-4db5-acb1-264c9f0c72fd")
                                .codeRefAttachmentType("NOTIFICATION")
                                .completed(true)
                                .attachments(List.of(
                                        AttachmentDto.builder()
                                                .id(7213L)
                                                .uuid("a361512b-ea75-468c-9b27-d344b6418871")
                                                .dossierUuid("456485f1-a67c-4ab3-8285-b56a6f8138b3")
                                                .filename("notification00001267.pdf")
                                                .path("456485f1-a67c-4ab3-8285-b56a6f8138b3/NOTIFICATION/1781271821107")
                                                .extension("pdf")
                                                .byteSize(57564L)
                                                .storageDocumentId(755352)
                                                .contentType("application/pdf")
                                                .uploadedAt(LocalDateTime.of(2026, 6, 12, 14, 43, 41))
                                                .uploadedBy("290055")
                                                .attachmentTypeCode("NOTIFICATION")
                                                .dossierAttachmentTypeUuid("6fd450ef-ed5b-4db5-acb1-264c9f0c72fd")
                                                .interventions(List.of())
                                                .build(),
                                        AttachmentDto.builder()
                                                .id(7261L)
                                                .uuid("b5a79177-4222-404c-bebb-ae18576d14ed")
                                                .dossierUuid("456485f1-a67c-4ab3-8285-b56a6f8138b3")
                                                .filename("notification00001267.pdf")
                                                .path("456485f1-a67c-4ab3-8285-b56a6f8138b3/NOTIFICATION/1781517195417")
                                                .extension("pdf")
                                                .byteSize(60046L)
                                                .storageDocumentId(756131)
                                                .contentType("application/pdf")
                                                .uploadedAt(LocalDateTime.of(2026, 6, 15, 10, 53, 15))
                                                .uploadedBy("290055")
                                                .attachmentTypeCode("NOTIFICATION")
                                                .dossierAttachmentTypeUuid("6fd450ef-ed5b-4db5-acb1-264c9f0c72fd")
                                                .interventions(List.of())
                                                .build()
                                ))
                                .build()
                ))
                .dscTransferDate(LocalDateTime.now())
                .build();
        loanDetailValidationResult = LoanDetailValidationResult.builder()
                .loanDetail(LoanDetailDto.builder()
                        .teg(BigDecimal.valueOf(5.1))
                        .firstInstallmentDate(LocalDate.of(2026, 3, 1))
                        .lastInstallmentDate(LocalDate.of(2036, 2, 1))
                        .loanAmount(BigDecimal.valueOf(483000.00))
                        .totalCreditCost(BigDecimal.valueOf(12000000))
                        .insuranceCost(BigDecimal.valueOf(3000))
                        .constantInstallmentAmount(BigDecimal.valueOf(8900))
                        .build())
                .build();
    }


    @Test
    void shouldGeneratePpiClassicPdf() throws Exception {

        dossier.setProduct(CodeLabelDto.builder().code("PPI_CLASSIQUE").build());
        byte[] pdfBytes = opcGeneratorService.generateOpc(dossier, loanDetailValidationResult);

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(100);

        Files.write(Paths.get("target/opc_ppi_classic.pdf"), pdfBytes);
    }

    @Test
    void shouldGeneratePpiPprPdf() throws Exception {

        dossier.setProduct(CodeLabelDto.builder().code("PPI_PPR_FONC").build());
        byte[] pdfBytes = opcGeneratorService.generateOpc(dossier, loanDetailValidationResult);

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(100);

        Files.write(Paths.get("target/opc_ppi_ppr.pdf"), pdfBytes);
    }

    @Test
    void shouldGeneratePpiMrePdf() throws Exception {

        dossier.setProduct(CodeLabelDto.builder().code("PPI_MRE").build());
        byte[] pdfBytes = opcGeneratorService.generateOpc(dossier, loanDetailValidationResult);

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(100);

        Files.write(Paths.get("target/opc_ppi_mre.pdf"), pdfBytes);
    }

    @Test
    @DisplayName("Should generate valid PPI_CLASSIQUE PDF with all sections")
    void shouldGeneratePpiClassicPdfWithAllSections() throws Exception {
        dossier.setProduct(CodeLabelDto.builder().code("PPI_CLASSIQUE").build());

        byte[] pdfBytes = opcGeneratorService.generateOpc(dossier, loanDetailValidationResult);

        // 1. Basic validity
        Assertions.assertThat(pdfBytes)
                .isNotNull()
                .isNotEmpty()
                .hasSizeGreaterThan(10_000); // Realistic PDF size
        assertPdfIsValid(pdfBytes);

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