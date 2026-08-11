package opc.ma.util;

public final class LocationUtils {

    private static final String DEFAULT_CITY = "Casablanca";

    private LocationUtils() {
    }

    public static String getCityFromTimezone() {
        return DEFAULT_CITY;
    }
}