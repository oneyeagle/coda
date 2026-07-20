package opc.ma.model;

import java.util.List;

/**
 * Per-repetition view fed to attach_hypothec's Thymeleaf fragment as "hy" —
 * merges the loan-level scalars (shared across every repetition via
 * $P{REPORT_PARAMETERS_MAP} in attach_hypothec.jrxml) with one element's
 * rangs/properties collections from ATTCH_HYPOTHEC_DATA.
 */
public record AttachHypothecView(
        String customerFullName,
        String decisionDate,
        String signedIn,
        String loanAmount,
        String rateType,
        String rate,
        List<TitreFoncierRang> rangs,
        List<HypothecProperty> properties
) {
}