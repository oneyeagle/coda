package ma.gov.acaps.common; // TODO: adjust to your package

/**
 * Model for the dynamic "Conditions Particulières" extract (PoC).
 * Values arrive pre-formatted (locale formatting is the caller's concern,
 * e.g. "4 850,00"); the template only places them.
 *
 * Record components are resolved by SpringEL as ${cp.dureeGlobale} etc.
 * (Spring Framework 6+ / Boot 3+).
 */
public record CpData(
        String dureeGlobale,   // e.g. "240 mois"
        String teg,            // e.g. "9,90 %"
        String taux,           // e.g. "5,10" — injected on BOTH FR and AR sides
        String echeanceTtc,    // e.g. "4 850,00"
        String fraisDossier    // e.g. "2 500,00 DH HT"
) {}
