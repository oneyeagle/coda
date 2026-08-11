package opc.ma.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class ObjectUtils {

    private static final DecimalFormatSymbols FR_SYMBOLS = new DecimalFormatSymbols(Locale.FRENCH);

    private ObjectUtils() {
    }

    public static String formatArea(String rawArea) {
        if (rawArea == null || rawArea.isBlank()) {
            return "";
        }
        try {
            double area = Double.parseDouble(rawArea.trim());
            DecimalFormat format = new DecimalFormat("#,##0.##", FR_SYMBOLS);
            return format.format(area) + " m²";
        } catch (NumberFormatException e) {
            return rawArea;
        }
    }

    public static String extractBranchCode(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            return "";
        }
        String trimmed = accountNumber.trim();
        return trimmed.length() > 3 ? trimmed.substring(0, 3) : trimmed;
    }

    public static String formatPercentage(BigDecimal value) {
        if (value == null) {
            return "";
        }
        DecimalFormat format = new DecimalFormat("#,##0.##", FR_SYMBOLS);
        return format.format(value.setScale(2, RoundingMode.HALF_UP)) + " %";
    }
}