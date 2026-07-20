package opc.ma.model;

/**
 * One repetition of pc_ppi_classic.jrxml's "Customer" subDataset (list
 * ${F}{customers}) — the borrower(s) identification card in Article 1.
 */
public record PcCustomerItem(
        String customerName,
        String customerAddress,
        String customerBirthDate,
        String customerBirthPlace,
        String customerCardId,
        String customerCardIssuedDate,
        String intitule,
        String mandataire
) {
}