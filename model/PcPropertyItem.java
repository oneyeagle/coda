package opc.ma.model;

/**
 * One repetition of pc_ppi_classic.jrxml's "Bien" subDataset (list
 * ${F}{properties}) — the property/ies designated in Article 2 (Objet).
 */
public record PcPropertyItem(
        String titreFoncierBien,
        String villeBien,
        String descriptionBien
) {
}