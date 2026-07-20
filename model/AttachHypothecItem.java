package opc.ma.model;

import java.util.List;

/**
 * One repetition of attach_hypothec.jrxml's detail band — mirrors the two
 * fields (rangs, properties) JRBeanCollectionDataSource read off each
 * element of the ATTCH_HYPOTHEC_DATA list.
 */
public record AttachHypothecItem(
        List<TitreFoncierRang> rangs,
        List<HypothecProperty> properties
) {
}