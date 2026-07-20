package opc.ma.model;

/**
 * One repetition of pc_ppi_classic.jrxml's "Caution" subDataset (list
 * ${F}{cautionBeneficiaries}) — reused twice in the source: the guarantor
 * identification card in Article 1, and again as a signature line in the
 * final signature block.
 */
public record PcCautionItem(
        String nameCaution,
        String addressCaution,
        String dateBirthCaution,
        String placeBirthCaution,
        String nationalIdentityCaution,
        String issuedCaution,
        String natureCaution
) {
}