package com.kanabridge;

public final class CurrencyConverterTest {
    private static int count;

    private static void check(boolean value, String label) { if (!value) throw new AssertionError(label); count++; }
    private static void same(String expected, String actual, String label) {
        if (!expected.equals(actual)) throw new AssertionError(label + ": expected [" + expected + "], got [" + actual + "]");
        count++;
    }
    private static void amount(String text, double expected) {
        double actual = CurrencyConverter.parseAmount(text);
        if (Math.abs(actual - expected) > 1e-9) throw new AssertionError("parseAmount(" + text + "): expected " + expected + ", got " + actual);
        count++;
    }

    public static void main(String[] args) {
        // Service response
        CurrencyConverter.Rate rate = CurrencyConverter.parse("{\"date\":\"2026-10-14\",\"base\":\"EUR\",\"quote\":\"JPY\",\"rate\":177.46}");
        check(rate != null && rate.yenPerEuro == 177.46 && rate.date.equals("2026-10-14"), "v2 response parsed");
        check(CurrencyConverter.parse("{\"rate\": 180, \"date\": \"2026-01-02\"}").yenPerEuro == 180.0, "integer rate and key order");
        check(CurrencyConverter.parse(null) == null, "null response");
        check(CurrencyConverter.parse("") == null, "empty response");
        check(CurrencyConverter.parse("<html>502 Bad Gateway</html>") == null, "html error page");
        check(CurrencyConverter.parse("{\"date\":\"2026-10-14\"}") == null, "missing rate");
        check(CurrencyConverter.parse("{\"rate\":177.46}") == null, "missing date");
        check(CurrencyConverter.parse("{\"date\":\"2026-10-14\",\"rate\":0}") == null, "zero rate rejected");
        check(CurrencyConverter.parse("{\"date\":\"2026-10-14\",\"rate\":-3.2}") == null, "negative rate rejected");
        check(CurrencyConverter.parse("{\"date\":\"2026-10-14\",\"rate\":9999999}") == null, "absurd rate rejected");

        // Amounts typed by a person
        amount("12", 12); amount("12,5", 12.5); amount("12.5", 12.5); amount(" 12,50 ", 12.5);
        amount("1 234,50", 1234.5); amount("1.234,50", 1234.5); amount("1,234.50", 1234.5);
        amount("1,234,567", 1234567); amount("0,5", 0.5); amount(",5", 0.5); amount("100.", 100);
        amount("0", 0);
        amount("", -1); amount("   ", -1); amount(null, -1); amount("abc", -1); amount("12e3", -1);
        amount("-5", -1); amount("1,2,3x", -1); amount(".", -1); amount("12..5", -1); amount("99999999999999", -1);

        // Arithmetic and rounding
        CurrencyConverter.Rate fixed = new CurrencyConverter.Rate(177.46, "2026-10-14");
        same("17 746", CurrencyConverter.formatYen(CurrencyConverter.toYen(100, fixed)), "100 EUR in yen");
        same("1 774 600", CurrencyConverter.formatYen(CurrencyConverter.toYen(10000, fixed)), "10 000 EUR in yen");
        same("0", CurrencyConverter.formatYen(CurrencyConverter.toYen(0, fixed)), "zero");
        same("89", CurrencyConverter.formatYen(CurrencyConverter.toYen(0.5, fixed)), "half euro rounds to whole yen");
        same("100,00", CurrencyConverter.formatEuro(CurrencyConverter.toEuro(17746, fixed)), "17 746 yen back to euro");
        same("5,64", CurrencyConverter.formatEuro(CurrencyConverter.toEuro(1000, fixed)), "1 000 yen in euro");
        same("1 234,57", CurrencyConverter.formatEuro(1234.567), "euro grouping and decimals");
        same("0,05", CurrencyConverter.formatEuro(0.05), "cents padded");
        same("0,00", CurrencyConverter.formatEuro(0), "zero euro");
        same("177,46", CurrencyConverter.formatRate(177.46), "rate display");
        same("160,00", CurrencyConverter.formatRate(160), "rate keeps two decimals");
        same("12 345 678", CurrencyConverter.formatYen(12345678), "large yen grouping");
        same("999", CurrencyConverter.formatYen(999), "no grouping under a thousand");

        // Round trip stays within a cent
        double back = CurrencyConverter.toEuro(CurrencyConverter.toYen(250, fixed), fixed);
        check(Math.abs(back - 250) < 1e-9, "round trip");

        same("14/10/2026", CurrencyConverter.formatDate("2026-10-14"), "date for display");
        same("ontem", CurrencyConverter.formatDate("ontem"), "non-iso date untouched");
        same("null", CurrencyConverter.formatDate(null), "null date safe");

        // Endpoint must stay HTTPS and point at the documented route
        check(CurrencyConverter.ENDPOINT.startsWith("https://"), "endpoint uses https");
        check(CurrencyConverter.ENDPOINT.endsWith("/EUR/JPY"), "endpoint asks for EUR/JPY only");

        System.out.println("OK: " + count + " currency assertions");
    }
}
