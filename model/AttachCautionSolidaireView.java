package opc.ma.model;

/**
 * Per-repetition view fed to attach_caut_solidaire's Thymeleaf fragment as
 * "cs" — merges the loan-level scalars (shared across every repetition via
 * $P{REPORT_PARAMETERS_MAP} in attach_caut_solidaire.jrxml) with one
 * element's fields from ATTCH_SOLIDAIRE_DATA.
 */
public record AttachCautionSolidaireView(
        String customerFullName,
        String decisionDate,
        String signedIn,
        String loanAmount,
        String rateNature,
        String rate,
        String beneficiaryFullName,
        String beneficiaryAddress,
        String beneficiaryCardID
) {
}