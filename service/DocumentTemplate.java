package ma.sg.its.octroicreditapi.service.impl.opc;

import ma.sg.its.octroicreditapi.dto.DossierDataDto;
import ma.sg.its.octroicreditapi.dto.LoanDetailValidationResult;

/**
 * Represents a renderable section of the OPC document.
 * Each template is self-contained and declares its applicability.
 */
public interface DocumentTemplate {

    String getFragmentName();

    String getDisplayName();

    boolean isApplicable(DossierDataDto dossier, LoanDetailValidationResult loanDetail);


    Object prepareData(DossierDataDto dossier, LoanDetailValidationResult loanDetail);
}