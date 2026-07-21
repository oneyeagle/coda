package ma.sg.its.octroicreditapi.service.impl.opc;

import lombok.extern.slf4j.Slf4j;
import ma.sg.its.octroicredit.common.dto.CodeLabelDto;
import ma.sg.its.octroicredit.common.dto.PersonalInfoDTO;
import ma.sg.its.octroicreditapi.dto.*;
import ma.sg.its.octroicreditapi.dto.opc.*;
import ma.sg.its.octroicredit.common.util.DateUtils;
import ma.sg.its.octroicredit.common.util.MoneyToWords;
import ma.sg.its.octroicredit.common.util.StringUtils;
import ma.sg.its.octroicreditapi.service.impl.opc.DocumentTemplate;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
public class ParticularConditionTemplate implements DocumentTemplate {

    private final String templateVariant; // "pc_ppi_classic", "pc_ppi_ppr", "pc_ppi_mre"

    public ParticularConditionTemplate(String templateVariant) {
        this.templateVariant = templateVariant;
    }

    @Override
    public String getFragmentName() {
        return templateVariant;
    }

    @Override
    public String getDisplayName() {
        return "Particular Conditions (" + templateVariant + ")";
    }

    @Override
    public boolean isApplicable(DossierDataDto dossier, LoanDetailValidationResult loanDetail) {
        return dossier.getCustomerData() != null;
    }

    @Override
    public Object prepareData(DossierDataDto dossier, LoanDetailValidationResult loanDetail) {
        return buildParticularConditionData(dossier);
    }

    private ParticularConditionDto buildParticularConditionData(DossierDataDto dossier) {
        if (dossier.getCustomerData() == null) return null;

        var personalInfo = dossier.getCustomerData().getPersonalInfo();
        var representative = dossier.getRepresentatives().stream()
                .filter(rep -> rep.getCustomer() != null)
                .findFirst()
                .orElse(null);
        var beneficiaries = dossier.getBeneficiaries();
        var guarantors = dossier.getGuarantors();
        var properties = dossier.getPropertyData().getProperties();

        // Warranty extraction (first borrower's first rang)
        var opcWarrantyDto = properties.stream()
                .filter(p -> Boolean.TRUE.equals(p.getForAcquisition()))
                .findFirst()
                .map(p -> {
                    var firstRan = beneficiaries.stream()
                            .filter(b -> b != null && Boolean.TRUE.equals(b.getIsBorrower()))
                            .findFirst()
                            .flatMap(b -> Optional.ofNullable(b.getRangs())
                                    .orElseGet(Collections::emptyList).stream()
                                    .findFirst())
                            .orElse(null);

                    return OpcWarrantyDto.builder()
                            .rang(firstRan != null ? firstRan.getRang() : null)
                            .amount(firstRan != null && firstRan.getWarrantyAmount() != null
                                    ? MoneyToWords.formatConvertAmount(firstRan.getWarrantyAmount())
                                    : "")
                            .landCertificate(p.getLandCertificateNumber() != null ? p.getLandCertificateNumber() : "")
                            .build();
                })
                .orElse(null);

        var builder = ParticularConditionDto.builder()
                .customers(List.of(
                        OpcCustomerDto.builder()
                                .customerName(constructFullName(personalInfo.getLastName(), personalInfo.getFirstName()))
                                .customerAddress(streamAddress(personalInfo))
                                .customerBirthDate(formatDate(personalInfo.getBirthDate()))
                                .customerCardIssuedDate(formatDate(personalInfo.getCardIDEmissionDate()))
                                .customerBirthPlace(personalInfo.getBirthCity())
                                .customerCardId(personalInfo.getCardID())
                                .intitule(constructLabel(guarantors))
                                .mandataire(constructRepresentative(representative))
                                .build()
                ))
                .cautionBeneficiaries(buildCautionList(guarantors, beneficiaries, dossier))
                .properties(buildPropertiesList(properties, dossier))
                .warranties(buildWarrantiesList(dossier))
                .reserves(getRestrictionsList(dossier));

        if (opcWarrantyDto != null) {
            builder.warrantyRang(opcWarrantyDto.getRang())
                    .warrantyAmount(opcWarrantyDto.getAmount())
                    .warrantyLandCertificate(opcWarrantyDto.getLandCertificate());
        }

        return builder.build();
    }

    private String streamAddress(PersonalInfoDTO info) {
        return Arrays.stream(new String[]{info.getAddress1(), info.getCity(), info.getCountry()})
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(", "));
    }

    private List<OpcCautionDto> buildCautionList(
            List<GuarantorDto> guarantors,
            List<BeneficiaryDto> beneficiaries,
            DossierDataDto dossier) {

        var repMap = memoizeRepresentatives(dossier);

        var guarantorCautions = Optional.ofNullable(guarantors).orElseGet(Collections::emptyList).stream()
                .filter(g -> g != null && Boolean.FALSE.equals(g.getIsBorrower()))
                .map(g -> OpcCautionDto.builder()
                        .nameCaution(constructFullName(g.getLastName(), g.getFirstName()))
                        .addressCaution(g.getAddress())
                        .dateBirthCaution(formatDate(g.getBirthDate()))
                        .placeBirthCaution(g.getCodeBirthPlace())
                        .nationalIdentityCaution(g.getIdCardNumber())
                        .issuedCaution(formatDate(g.getIssuedAt()))
                        .natureCaution(constructCautionLabel(Boolean.TRUE.equals(g.getIsBeneficiary())))
                        .representative(repMap.getOrDefault("GUARANTOR", ""))
                        .build());

        var beneficiaryCautions = Optional.ofNullable(beneficiaries).orElseGet(Collections::emptyList).stream()
                .filter(b -> b != null && Boolean.FALSE.equals(b.getIsBorrower()))
                .map(b -> OpcCautionDto.builder()
                        .nameCaution(constructFullName(b.getLastname(), b.getFirstname()))
                        .addressCaution(b.getAddress())
                        .dateBirthCaution(formatDate(b.getBirthDate()))
                        .placeBirthCaution(b.getCodeBirthPlace())
                        .nationalIdentityCaution(b.getIdCardNumber())
                        .issuedCaution(formatDate(b.getIssuedAt()))
                        .natureCaution(constructBeneficiaryLabel(Boolean.TRUE.equals(b.getIsGuarantor())))
                        .representative(repMap.getOrDefault("BENEFICIARY", ""))
                        .build());

        return Stream.concat(guarantorCautions, beneficiaryCautions).toList();
    }

    private Map<String, String> memoizeRepresentatives(DossierDataDto dossier) {
        Map<String, String> repMap = new HashMap<>();

        dossier.getRepresentatives().stream().findFirst().ifPresent(rep -> {
            if (rep.getGuarantors() != null && !rep.getGuarantors().isEmpty()) {
                repMap.put("GUARANTOR", constructRepresentative(rep));
            }
        });

        dossier.getRepresentatives().stream()
                .filter(rep -> rep.getBeneficiaries() != null && !rep.getBeneficiaries().isEmpty())
                .findFirst()
                .ifPresent(rep -> repMap.put("BENEFICIARY", constructRepresentative(rep)));

        return repMap;
    }

    private List<OpcPropertyDto> buildPropertiesList(List<PropertyDto> properties, DossierDataDto dossier) {
        return Optional.ofNullable(properties).orElseGet(Collections::emptyList).stream()
                .filter(p -> p != null && Boolean.TRUE.equals(p.getForAcquisition()))
                .map(p -> OpcPropertyDto.builder()
                        .titreFoncierBien(p.getLandCertificateNumber())
                        .typeBien(Optional.ofNullable(dossier.getLoanData())
                                .map(LoanDataDto::getPropertyType)
                                .map(CodeLabelDto::getDesignation)
                                .orElse(null))
                        .villeBien(p.getCodePropertyCity())
                        .descriptionBien(p.getDescriptionBien())
                        .build())
                .toList();
    }

    private List<String> buildWarrantiesList(DossierDataDto dossier) {
        return Optional.ofNullable(dossier.getWarranties()).orElseGet(Collections::emptyList).stream()
                .filter(w -> w != null && (w.getType() == WarrantyType.TO_COLLECT || w.getType() == WarrantyType.AUTO))
                .map(WarrantyDto::getContent)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<String> getRestrictionsList(DossierDataDto dossier) {
        return Optional.ofNullable(dossier.getRestrictions()).orElseGet(Collections::emptyList).stream()
                .filter(r -> r.getAttachment() != null &&
                        (r.getType() == RestrictionType.DSC ||
                                r.getType() == RestrictionType.PROPOSED_DSC ||
                                r.getType() == RestrictionType.FRONT))
                .map(RestrictionDto::getContent)
                .filter(Objects::nonNull)
                .toList();
    }

    private String constructRepresentative(RepresentativeDto rep) {
        if (rep == null) return "";
        return String.format("Représenté par %s Titulaire de la CIN %s selon la procuration en date du %s",
                constructFullName(rep.getLastname(), rep.getFirstname()),
                rep.getCin(),
                formatDate(rep.getCinIssuedAt()));
    }

    private String constructCautionLabel(boolean isBeneficiary) {
        return isBeneficiary ? "Caution solidaire et hypothécaire" : "Caution solidaire";
    }

    private String constructBeneficiaryLabel(boolean isCaution) {
        return isCaution ? "Caution solidaire et hypothécaire" : "Caution hypothécaire";
    }

    private String constructLabel(List<GuarantorDto> guarantors) {
        return Optional.ofNullable(guarantors).orElseGet(Collections::emptyList).stream()
                .anyMatch(g -> Boolean.TRUE.equals(g.getIsBorrower()))
                ? "Emprunteur caution solidaire"
                : "Emprunteur";
    }

    private String formatDate(java.time.LocalDate date) {
        if (date == null) return null;
        return DateUtils.convertLocalDateToString(date, "dd/MM/yyyy");
    }

    private String constructFullName(String lastName, String firstName) {
        return String.format("MME/MR %s %s",
                StringUtils.defaultString(lastName),
                StringUtils.defaultString(firstName)).trim();
    }
}