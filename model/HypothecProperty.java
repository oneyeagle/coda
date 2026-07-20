package opc.ma.model;

/** Mirrors attach_hypothec.jrxml's Properties sub-dataset. */
public record HypothecProperty(
        String nomBien,
        String titreFoncierBien,
        String villeBien,
        String areaBien,
        String descriptionBien
) {
}