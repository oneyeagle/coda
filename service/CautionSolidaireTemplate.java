package ma.sg.its.octroicreditapi.service.impl.opc;

import lombok.extern.slf4j.Slf4j;
import ma.sg.its.octroicredit.common.util.MoneyToWords;
import ma.sg.its.octroicredit.common.util.StringUtils;
import ma.sg.its.octroicreditapi.dto.DossierDataDto;
import ma.sg.its.octroicreditapi.dto.LoanDetailValidationResult;
import ma.sg.its.octroicreditapi.dto.RangDto;
import ma.sg.its.octroicreditapi.dto.opc.CautionHypothecDto;
import ma.sg.its.octroicreditapi.dto.opc.DAJCautionSolidaryDto;
import ma.sg.its.octroicreditapi.dto.opc.OpcRangDto;
import ma.sg.its.octroicreditapi.tools.ObjectUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class CautionSolidaireTemplate implements DocumentTemplate {

    @Override
    public String getFragmentName() {
        return "attach_caut_solidaire";
    }

    @Override
    public String getDisplayName() {
        return "Caution Solidaire";
    }

    @Override
    public boolean isApplicable(DossierDataDto dossier, LoanDetailValidationResult loanDetail) {
        List<?> data = (List<?>) prepareData(dossier, loanDetail);
        return data != null && !data.isEmpty();
    }

    @Override
    public Object prepareData(DossierDataDto dossier, LoanDetailValidationResult loanDetail) {
        if (dossier == null || dossier.getGuarantors() == null) {
            return List.of();
        }

        return dossier.getGuarantors().stream()
                .filter(g -> Boolean.FALSE.equals(g.getIsBorrower()))
                .map(g -> DAJCautionSolidaryDto.builder()
                        .nom_complet_garant(constructFullName(g.getLastName(), g.getFirstName()))
                        .adresse_caution(StringUtils.defaultString(g.getAddress()))
                        .cin_garant(StringUtils.defaultString(g.getIdCardNumber()))
                        .build())
                .toList();
    }

    private String constructFullName(String lastName, String firstName) {
        return String.format("MME/MR %s %s",
                StringUtils.defaultString(lastName),
                StringUtils.defaultString(firstName)).trim();
    }
}