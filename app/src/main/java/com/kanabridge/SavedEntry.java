package com.kanabridge;

/** Portable, delimiter-safe identity for a selected JMdict spelling and reading. */
public final class SavedEntry {
    public final long entry;
    public final String word, reading, meaning, originalReading;
    public SavedEntry(long entry, String word, String reading, String meaning, String originalReading) {
        this.entry = entry; this.word = word; this.reading = reading; this.meaning = meaning; this.originalReading = originalReading;
    }
    public String key() { return entry + "\t" + encode(word) + "\t" + encode(originalReading); }
    public String serialize() { return entry + "\t" + encode(word) + "\t" + encode(reading) + "\t" + encode(meaning) + "\t" + encode(originalReading); }
    public static SavedEntry parse(String value) {
        try {
            String[] fields = value.split("\t", -1); if (fields.length != 5) return null;
            long id = Long.parseLong(fields[0]); if (id <= 0) return null;
            return new SavedEntry(id, decode(fields[1]), decode(fields[2]), decode(fields[3]), decode(fields[4]));
        } catch (Exception error) { return null; }
    }
    static String encode(String value) { try { return java.net.URLEncoder.encode(value, "UTF-8"); } catch (java.io.UnsupportedEncodingException impossible) { throw new AssertionError(impossible); } }
    static String decode(String value) { try { return java.net.URLDecoder.decode(value, "UTF-8"); } catch (java.io.UnsupportedEncodingException impossible) { throw new AssertionError(impossible); } }
}
