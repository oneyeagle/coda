package opc.ma.model;

/**
 * One repetition of attach_caut_solidaire.jrxml's detail band — mirrors the
 * fields JRBeanCollectionDataSource read off each element of the
 * ATTCH_SOLIDAIRE_DATA list (nom_complet_garant, adresse_caution, cin_garant).
 */
public record AttachCautionSolidaireItem(
        String beneficiaryFullName,
        String beneficiaryAddress,
        String beneficiaryCardID
) {
}