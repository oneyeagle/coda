package opc.ma.util;

public final class StringUtils {

    private StringUtils() {
    }

    public static String defaultString(String value) {
        return value == null ? "" : value;
    }
}