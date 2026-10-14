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
    public static final String ENDPOINT = "https://api.frankfurter.dev/v2/rate/EUR/JPY";

    /** One yen-per-euro rate and the day the bank published it, as yyyy-MM-dd. */
    public static final class Rate {
        public final double yenPerEuro;
        public final String date;
        public Rate(double yenPerEuro, String date) { this.yenPerEuro = yenPerEuro; this.date = date; }
    }

    private static final Pattern RATE = Pattern.compile("\"rate\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)");
    private static final Pattern DATE = Pattern.compile("\"date\"\\s*:\\s*\"(\\d{4}-\\d{2}-\\d{2})\"");

    /** Reads the service response; returns null for anything that is not a plausible rate. */
    public static Rate parse(String json) {
        if (json == null) return null;
        Matcher rate = RATE.matcher(json), date = DATE.matcher(json);
        if (!rate.find() || !date.find()) return null;
        double value = Double.parseDouble(rate.group(1));
        return value > 0 && value < 100000 ? new Rate(value, date.group(1)) : null;
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
            // Both present: the one that comes last is the decimal mark, the other groups thousands.
            value = comma > dot ? value.replace(".", "").replace(',', '.') : value.replace(",", "");
        } else if (comma >= 0) {
            if (value.indexOf(',') != comma) value = value.replace(",", "");
            else value = value.replace(',', '.');
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
        return sign + digits + (decimals > 0 ? "," + String.format("%0" + decimals + "d", fraction) : "");
    }

    private CurrencyConverter() {}
}
