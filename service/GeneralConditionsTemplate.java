package ma.sg.its.octroicreditapi.service.impl.opc;

import lombok.extern.slf4j.Slf4j;
import ma.sg.its.octroicreditapi.dto.*;

import java.util.*;

@Slf4j
public class GeneralConditionsTemplate implements DocumentTemplate {

    @Override
    public String getFragmentName() {
        return "general_conditions";
    }

    @Override
    public String getDisplayName() {
        return "General Conditions";
    }

    @Override
    public boolean isApplicable(DossierDataDto dossier, LoanDetailValidationResult loanDetail) {
        return true;
    }

    @Override
    public Object prepareData(DossierDataDto dossier, LoanDetailValidationResult loanDetail) {
        return Collections.emptyMap();
    }
}
