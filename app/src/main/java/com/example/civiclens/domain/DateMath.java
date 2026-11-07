package com.example.civiclens.domain;

import java.util.Locale;

/**
 * Day arithmetic for yyyy-MM-dd strings, without java.time, so it works on API 24.
 * Uses the standard days-from-civil algorithm. 1970-01-01 is day 0.
 */
public final class DateMath {

    private DateMath() { }

    public static long toEpochDay(String iso) {
        String[] parts = iso.split("-");
        long y = Long.parseLong(parts[0]);
        long m = Long.parseLong(parts[1]);
        long d = Long.parseLong(parts[2]);

        y -= m <= 2 ? 1 : 0;
        long era = (y >= 0 ? y : y - 399) / 400;
        long yoe = y - era * 400;
        long doy = (153 * (m > 2 ? m - 3 : m + 9) + 2) / 5 + d - 1;
        long doe = yoe * 365 + yoe / 4 - yoe / 100 + doy;
        return era * 146097 + doe - 719468;
    }

    public static String fromEpochDay(long epochDay) {
        long z = epochDay + 719468;
        long era = (z >= 0 ? z : z - 146096) / 146097;
        long doe = z - era * 146097;
        long yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365;
        long y = yoe + era * 400;
        long doy = doe - (365 * yoe + yoe / 4 - yoe / 100);
        long mp = (5 * doy + 2) / 153;
        long d = doy - (153 * mp + 2) / 5 + 1;
        long m = mp < 10 ? mp + 3 : mp - 9;
        y += m <= 2 ? 1 : 0;
        return String.format(Locale.US, "%04d-%02d-%02d", y, m, d);
    }

    public static String addDays(String iso, int days) {
        return fromEpochDay(toEpochDay(iso) + days);
    }

    /** Positive when {@code to} is after {@code from}. */
    public static int daysBetween(String from, String to) {
        return (int) (toEpochDay(to) - toEpochDay(from));
    }
}
