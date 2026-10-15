package com.kanabridge;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Euro and yen conversion. Parsing, arithmetic and formatting are plain Java so they run in the
 * host tests; the single network call is isolated in {@link #fetch()}.
 */
public final class CurrencyConverter {
    /** Reference rate published by the European Central Bank, relayed by Frankfurter. */
    public static final String ENDPOINT = "https://api.frankfurter.dev/v2/providers/ecb/rate/EUR/JPY";

    /** One yen-per-euro rate and the day the bank published it, as yyyy-MM-dd. */
    public static final class Rate {
        public final double yenPerEuro;
        public final String date;
        public Rate(double yenPerEuro, String date) { this.yenPerEuro = yenPerEuro; this.date = date; }
    }

    private static final Pattern RATE = Pattern.compile("\"rate\"\\s*:\\s*(-?(?:0|[1-9][0-9]*)(?:\\.[0-9]+)?(?:[eE][+-]?[0-9]+)?)(?=\\s*[,}])");
    private static final Pattern DATE = Pattern.compile("\"date\"\\s*:\\s*\"(\\d{4}-\\d{2}-\\d{2})\"");

    /** Shared validation for downloaded and cached rates; compatible with Android API 23. */
    public static boolean usable(Rate rate) {
        if (rate == null || Double.isNaN(rate.yenPerEuro) || Double.isInfinite(rate.yenPerEuro)
                || rate.yenPerEuro <= 0 || rate.yenPerEuro >= 100000 || rate.date == null
                || !rate.date.matches("\\d{4}-\\d{2}-\\d{2}")) return false;
        try {
            java.util.GregorianCalendar calendar = new java.util.GregorianCalendar(java.util.TimeZone.getTimeZone("UTC"));
            calendar.clear(); calendar.setLenient(false);
            calendar.set(Integer.parseInt(rate.date.substring(0,4)), Integer.parseInt(rate.date.substring(5,7))-1,
                    Integer.parseInt(rate.date.substring(8,10)));
            calendar.getTimeInMillis(); return true;
        } catch (IllegalArgumentException error) { return false; }
    }
    public static boolean fresh(Rate rate, long fetched, long now) {
        return usable(rate) && fetched > 0 && now >= fetched && now - fetched < 3600000L;
    }
    /** Reads the response; a malformed number or impossible date must never become a cached rate. */
    public static Rate parse(String json) {
        if (json == null) return null;
        Matcher rate = RATE.matcher(json), date = DATE.matcher(json);
        if (!rate.find() || !date.find()) return null;
        try {
            Rate parsed = new Rate(Double.parseDouble(rate.group(1)), date.group(1));
            return usable(parsed) ? parsed : null;
        } catch (NumberFormatException error) { return null; }
    }

    /** Downloads the current rate. Blocking: call it off the main thread. */
    public static Rate fetch() throws IOException {
        HttpURLConnection connection = (HttpURLConnection)new URL(ENDPOINT).openConnection();
        connection.setConnectTimeout(6000); connection.setReadTimeout(8000);
        connection.setRequestProperty("Accept", "application/json");
        try {
            if (connection.getResponseCode() != 200) throw new IOException("HTTP " + connection.getResponseCode());
            try (InputStream in = connection.getInputStream()) {
                byte[] buffer = new byte[2048]; int total = 0, read;
                while (total < buffer.length && (read = in.read(buffer, total, buffer.length - total)) > 0) total += read;
                if (total == buffer.length && in.read() != -1) throw new IOException("Resposta demasiado grande");
                Rate rate = parse(new String(buffer, 0, total, StandardCharsets.UTF_8));
                if (rate == null) throw new IOException("Resposta inesperada");
                return rate;
            }
        } finally { connection.disconnect(); }
    }

    /** Accepts "12,5", "12.5" and "1 234,50"; returns -1 when the text is not an amount. */
    public static double parseAmount(String text) {
        if (text == null) return -1;
        String value = text.trim().replace(" ", "").replace(" ", "");
        if (value.isEmpty()) return -1;
        int comma = value.lastIndexOf(','), dot = value.lastIndexOf('.');
        if (comma >= 0 && dot >= 0) {
            if (comma > dot) {
                if (!value.matches("[0-9]{1,3}(?:\\.[0-9]{3})+,[0-9]+")) return -1;
                value = value.replace(".", "").replace(',', '.');
            } else {
                if (!value.matches("[0-9]{1,3}(?:,[0-9]{3})+\\.[0-9]+")) return -1;
                value = value.replace(",", "");
            }
        } else if (comma >= 0) {
            if (value.indexOf(',') != comma) {
                if (!value.matches("[0-9]{1,3}(?:,[0-9]{3})+")) return -1;
                value = value.replace(",", "");
            } else value = value.replace(',', '.');
        } else if (dot >= 0 && value.indexOf('.') != dot) {
            if (!value.matches("[0-9]{1,3}(?:\\.[0-9]{3})+")) return -1;
            value = value.replace(".", "");
        }
        if (!value.matches("[0-9]*\\.?[0-9]*") || value.equals(".")) return -1;
        try {
            double amount = Double.parseDouble(value);
            return amount <= 1e12 ? amount : -1;
        } catch (NumberFormatException e) { return -1; }
    }

    public static double toYen(double euro, Rate rate) { return euro * rate.yenPerEuro; }
    public static double toEuro(double yen, Rate rate) { return yen / rate.yenPerEuro; }

    /** Yen has no minor unit: whole numbers grouped with spaces, e.g. "17 746". */
    public static String formatYen(double yen) { return group(Math.round(yen), 0, 0); }

    /** Euro with two decimals and a decimal comma, e.g. "1 234,57". */
    public static String formatEuro(double euro) {
        long cents = Math.round(euro * 100);
        return group(cents / 100, cents % 100, 2);
    }

    /** The rate itself, e.g. "177,46". */
    public static String formatRate(double yenPerEuro) {
        long hundredths = Math.round(yenPerEuro * 100);
        return group(hundredths / 100, hundredths % 100, 2);
    }

    /** yyyy-MM-dd to dd/MM/yyyy; anything else is returned untouched. */
    public static String formatDate(String iso) {
        return iso != null && iso.matches("\\d{4}-\\d{2}-\\d{2}")
            ? iso.substring(8) + "/" + iso.substring(5, 7) + "/" + iso.substring(0, 4) : String.valueOf(iso);
    }

    private static String group(long whole, long fraction, int decimals) {
        StringBuilder digits = new StringBuilder(Long.toString(Math.abs(whole)));
        for (int i = digits.length() - 3; i > 0; i -= 3) digits.insert(i, ' ');
        String sign = whole < 0 ? "-" : "";
        return sign + digits + (decimals > 0 ? "," + String.format(java.util.Locale.ROOT, "%0" + decimals + "d", fraction) : "");
    }

    private CurrencyConverter() {}
}
