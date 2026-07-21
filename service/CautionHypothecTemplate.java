package ma.sg.its.octroicreditapi.service.impl.opc;

import lombok.extern.slf4j.Slf4j;
import ma.sg.its.octroicredit.common.util.MoneyToWords;
import ma.sg.its.octroicredit.common.util.StringUtils;
import ma.sg.its.octroicreditapi.dto.DossierDataDto;
import ma.sg.its.octroicreditapi.dto.LoanDetailValidationResult;
import ma.sg.its.octroicreditapi.dto.RangDto;
import ma.sg.its.octroicreditapi.dto.opc.CautionHypothecDto;
import ma.sg.its.octroicreditapi.dto.opc.HypothecDto;
import ma.sg.its.octroicreditapi.dto.opc.OpcPropertyDto;
import ma.sg.its.octroicreditapi.dto.opc.OpcRangDto;
import ma.sg.its.octroicreditapi.tools.ObjectUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class CautionHypothecTemplate implements DocumentTemplate {

    @Override
    public String getFragmentName() {
        return "attach_caut_hypothecaire";
    }

    @Override
    public String getDisplayName() {
        return "Caution Hypothecaire";
    }

    @Override
    public boolean isApplicable(DossierDataDto dossier, LoanDetailValidationResult loanDetail) {
        List<?> data = (List<?>) prepareData(dossier, loanDetail);
        return data != null && !data.isEmpty();
    }

    @Override
    public Object prepareData(DossierDataDto dossier, LoanDetailValidationResult loanDetail) {
        if (dossier == null || dossier.getBeneficiaries() == null) {
            return List.of();
        }

        return dossier.getBeneficiaries().stream()
                .filter(b -> !Boolean.TRUE.equals(b.getIsBorrower()))
                .map(b -> {
                    var beneficiaryRangs = Optional.ofNullable(b.getRangs()).orElse(List.of());
                    var rankPropertyIds = beneficiaryRangs.stream()
                            .map(RangDto::getPropertyId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());

                    var linkedAcqProperties = Optional.ofNullable(b.getProperties()).orElse(List.of()).stream()
                            .filter(p -> rankPropertyIds.contains(p.getId()) && Boolean.TRUE.equals(p.getForAcquisition()))
                            .toList();

                    return CautionHypothecDto.builder()
                            .beneficiaryFullName(constructFullName(b.getLastname(), b.getFirstname()))
                            .beneficiaryAddress(StringUtils.defaultString(b.getAddress()))
                            .beneficiaryCardID(StringUtils.defaultString(b.getIdCardNumber()))
                            .propertyName(linkedAcqProperties.stream()
                                    .map(p -> StringUtils.defaultString(p.getDenomination()))
                                    .collect(Collectors.joining(", ")))
                            .propertyLandCertificate(linkedAcqProperties.stream()
                                    .map(p -> StringUtils.defaultString(p.getLandCertificateNumber()))
                                    .collect(Collectors.joining(", ")))
                            .propertyArea(linkedAcqProperties.stream()
                                    .map(p -> ObjectUtils.formatArea(p.getPropertyArea()))
                                    .collect(Collectors.joining(", ")))
                            .propertyCity(linkedAcqProperties.stream()
                                    .map(p -> StringUtils.defaultString(p.getCodePropertyCity()))
                                    .collect(Collectors.joining(", ")))
                            .propertyRanks(beneficiaryRangs.stream()
                                    .map(r -> OpcRangDto.builder()
                                            .rang(Objects.toString(r.getRang(), ""))
                                            .amount(MoneyToWords.formatConvertAmount(r.getWarrantyAmount()))
                                            .build())
                                    .toList())
                            .build();
                })
                .toList();
    }

    private String constructFullName(String lastName, String firstName) {
        return String.format("MME/MR %s %s",
                StringUtils.defaultString(lastName),
                StringUtils.defaultString(firstName)).trim();
    }
}
