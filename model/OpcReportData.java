package opc.ma.model;

import lombok.Builder;

import java.util.List;

/**
 * Mirrors the flat REPORT_PARAMETERS_MAP the old JasperReports pipeline passed
 * into main_report.jrxml (and, through it, into every subreport) — one flat
 * parameter surface per HARD-RULE.md, not a nested object graph.
 */
@Builder
public record OpcReportData(
        // main_report.jrxml parameters
        String pcName,
        PcPpiClassicData pcData,
        String attachHypothecName,
        List<AttachHypothecItem> attachHypothecData,
        String attachHypothecaireName,
        List<AttachCautionHypothecaireItem> attachHypothecaireData,
        String attachSolidaireName,
        List<AttachCautionSolidaireItem> attachSolidaireData,

        // attach_hypothec.jrxml parameters, carried via $P{REPORT_PARAMETERS_MAP}
        // (shared across every ATTCH_HYPOTHEC_DATA repetition, not per-item)
        String customerFullName,
        String decisionDate,
        String signedAt,
        String signedIn,
        String loanAmount,
        String rateType,
        String rate,

        // attach_caut_hypothecaire.jrxml parameter, carried via
        // $P{REPORT_PARAMETERS_MAP} (shared across every ATTCH_HYPOTHECAIRE_DATA
        // repetition, not per-item) — named "rateNature" in that jrxml, distinct
        // from attach_hypothec.jrxml's "rateType" parameter
        String rateNature
) {

    public static OpcReportData generalConditionsOnly() {
        return OpcReportData.builder().build();
    }
}