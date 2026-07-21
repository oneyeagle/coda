package ma.sg.its.octroicreditapi.service.impl.opc;

import lombok.extern.slf4j.Slf4j;
import ma.sg.its.octroicreditapi.dto.*;
import ma.sg.its.octroicreditapi.dto.opc.*;
import ma.sg.its.octroicredit.common.util.MoneyToWords;
import ma.sg.its.octroicreditapi.tools.ObjectUtils;
import java.util.*;

@Slf4j
public class HypothecTemplate implements DocumentTemplate {

    @Override
    public String getFragmentName() {
        return "attach_hypothec";
    }

    @Override
    public String getDisplayName() {
        return "Hypothec Attachment";
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
                .filter(b -> b != null && Boolean.TRUE.equals(b.getIsBorrower()))
                .map(b -> HypothecDto.builder()
                        .properties(Optional.ofNullable(b.getProperties()).orElse(List.of()).stream()
                                .filter(Objects::nonNull)
                                .map(p -> OpcPropertyDto.builder()
                                        .titreFoncierBien(p.getLandCertificateNumber())
                                        .nomBien(p.getDenomination())
                                        .descriptionBien(p.getDescriptionBien())
                                        .villeBien(p.getCodePropertyCity())
                                        .areaBien(p.getPropertyArea() != null ? ObjectUtils.formatArea(p.getPropertyArea()) : "")
                                        .build())
                                .toList())
                        .rangs(Optional.ofNullable(b.getRangs()).orElse(List.of()).stream()
                                .filter(Objects::nonNull)
                                .map(r -> OpcRangDto.builder()
                                        .rang(Objects.toString(r.getRang(), ""))
                                        .amount(MoneyToWords.formatConvertAmount(r.getWarrantyAmount()))
                                        .build())
                                .toList())
                        .build())
                .toList();
    }
}

