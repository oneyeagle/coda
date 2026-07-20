package opc.ma.model;

import java.util.List;

/**
 * One repetition of attach_caut_hypothecaire.jrxml's detail band — mirrors
 * the fields JRBeanCollectionDataSource read off each element of the
 * ATTCH_HYPOTHECAIRE_DATA list, including the nested Rank sub-dataset
 * (propertyRanks, fields amount/rang — same shape as TitreFoncierRang).
 */
public record AttachCautionHypothecaireItem(
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