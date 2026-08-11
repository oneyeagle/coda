package opc.ma.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class MoneyToWords {

    private static final DecimalFormatSymbols FR_SYMBOLS = new DecimalFormatSymbols(Locale.FRENCH);

    private static final String[] UNITS = {
            "", "un", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf",
            "dix", "onze", "douze", "treize", "quatorze", "quinze", "seize",
            "dix-sept", "dix-huit", "dix-neuf"
    };

    private static final String[] TENS = {
            "", "", "vingt", "trente", "quarante", "cinquante", "soixante", "soixante-dix", "quatre-vingt", "quatre-vingt-dix"
    };

    private MoneyToWords() {
    }

    /**
     * Plain grouped-thousands amount, e.g. "45 673,00"
     */
    public static String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "";
        }
        DecimalFormat format = new DecimalFormat("#,##0.00", FR_SYMBOLS);
        return format.format(amount.setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * Same as formatAmount, without trailing zero decimals when the amount is a whole number.
     */
    public static String freeAmount(BigDecimal amount) {
        if (amount == null) {
            return "";
        }
        BigDecimal scaled = amount.setScale(2, RoundingMode.HALF_UP);
        DecimalFormat format = new DecimalFormat(
                scaled.stripTrailingZeros().scale() <= 0 ? "#,##0" : "#,##0.00", FR_SYMBOLS);
        return format.format(scaled);
    }

    /**
     * Formatted amount followed by its French spelled-out value, e.g.
     * "45 673,00 DH (quarante-cinq mille six cent soixante-treize dirhams)"
     */
    public static String formatConvertAmount(BigDecimal amount) {
        if (amount == null) {
            return "";
        }
        BigDecimal scaled = amount.setScale(2, RoundingMode.HALF_UP);
        long integerPart = scaled.longValue();
        return formatAmount(amount) + " DH (" + toWords(integerPart) + " dirhams)";
    }

    public static String toWords(long number) {
        if (number == 0) {
            return "zéro";
        }
        if (number < 0) {
            return "moins " + toWords(-number);
        }

        StringBuilder result = new StringBuilder();
        long billions = number / 1_000_000_000L;
        long millions = (number / 1_000_000L) % 1_000L;
        long thousands = (number / 1_000L) % 1_000L;
        long remainder = number % 1_000L;

        if (billions > 0) {
            result.append(threeDigitsToWords(billions)).append(" milliard").append(billions > 1 ? "s" : "").append(" ");
        }
        if (millions > 0) {
            result.append(threeDigitsToWords(millions)).append(" million").append(millions > 1 ? "s" : "").append(" ");
        }
        if (thousands > 0) {
            if (thousands == 1) {
                result.append("mille ");
            } else {
                result.append(threeDigitsToWords(thousands)).append(" mille ");
            }
        }
        if (remainder > 0) {
            result.append(threeDigitsToWords(remainder));
        }

        return result.toString().trim();
    }

    private static String threeDigitsToWords(long number) {
        StringBuilder result = new StringBuilder();
        long hundreds = number / 100;
        long rest = number % 100;

        if (hundreds > 0) {
            if (hundreds == 1) {
                result.append("cent");
            } else {
                result.append(UNITS[(int) hundreds]).append(" cent");
                if (rest == 0) {
                    result.append("s");
                }
            }
            if (rest > 0) {
                result.append(" ");
            }
        }

        if (rest > 0) {
            result.append(twoDigitsToWords(rest));
        }

        return result.toString();
    }

    private static String twoDigitsToWords(long number) {
        if (number < 20) {
            return UNITS[(int) number];
        }

        int tensDigit = (int) (number / 10);
        int unitDigit = (int) (number % 10);

        if (tensDigit == 7 || tensDigit == 9) {
            int base = tensDigit - 1;
            int remainder = (int) (number - base * 10);
            return TENS[base] + "-" + UNITS[remainder];
        }

        StringBuilder result = new StringBuilder(TENS[tensDigit]);
        if (unitDigit == 1 && tensDigit != 8) {
            result.append(" et un");
        } else if (unitDigit > 0) {
            result.append("-").append(UNITS[unitDigit]);
        } else if (tensDigit == 8) {
            result.append("s");
        }

        return result.toString();
    }
}