package opc.ma.model;

import java.util.List;

/**
 * Per-repetition view fed to attach_caut_hypothecaire's Thymeleaf fragment
 * as "cah" — merges the loan-level scalars (shared across every repetition
 * via $P{REPORT_PARAMETERS_MAP} in attach_caut_hypothecaire.jrxml) with one
 * element's fields from ATTCH_HYPOTHECAIRE_DATA.
 */
public record AttachCautionHypothecaireView(
        String customerFullName,
        String decisionDate,
        String signedIn,
        String loanAmount,
        String rateNature,
        String rate,
        String beneficiaryFullName,
        String beneficiaryAddress,
        String beneficiaryCardID,
        String propertyName,
        String propertyLandCertificate,
        String propertyCity,
        String propertyArea,
        String enrichment,
        List<TitreFoncierRang> propertyRanks
) {
}