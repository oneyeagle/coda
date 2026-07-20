package opc.ma.model;

import lombok.Builder;

import java.util.List;

/**
 * Mirrors pc_ppi_classic.jrxml's top-level parameters and fields — the
 * "Conditions Particulières" (PPI classic) report, a single-record data
 * source (whenNoDataType = One Empty Record) carrying both scalar
 * $P{...} parameters and the five $F{...} collection fields
 * (customers, cautionBeneficiaries, properties, warranties, reserves)
 * plus the standalone hypothec fields (warrantyAmount, warrantyRang,
 * warrantyLandCertificate) used by the single "5.1 GARANTIE HYPOTHECAIRE"
 * block in Article 5.
 * <p>
 * The jrxml also declares "Emprunteur" and "Caution" parameters of type
 * JRBeanCollectionDataSource — both unused by the template body (dead
 * parameters in the source), so they are not modeled here.
 */
@Builder
public record PcPpiClassicData(
        List<PcCustomerItem> customers,
        List<PcCautionItem> cautionBeneficiaries,
        List<PcPropertyItem> properties,
        List<String> warranties,
        List<String> reserves,
        String warrantyAmount,
        Integer warrantyRang,
        String warrantyLandCertificate,

        String loanObject,
        String loanAmountAmplitude,
        String loanDuration,
        String globalEffectiveRate,
        String rateType,
        String rate,
        String loanApplicationFee,
        String freeHandFee,
        String insuranceCost,
        String customerFullName,
        String bonusRate,
        String dueAmount,
        String dueAmountTVA,
        String periodicity,
        String firstInstallmentDate,
        String lastInstallmentDate,
        String deferredDuration,
        String deferredType,
        String totalCreditCost,
        String customerAgencyCode,
        String customerRib,
        String signedIn,
        String signedAt,
        String decisionDate,
        String differe,
        String capital
) {
}