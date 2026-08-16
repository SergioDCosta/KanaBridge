package com.kanabridge;

import java.util.Locale;

/** Immutable description of one kana sound used by the converter and reference table. */
public final class KanaEntry {
    public final String hiragana;
    public final String katakana;
    public final String romaji;
    public final String line;
    public final String category;
    public final String baseHiragana;
    public final String baseKatakana;
    public final String variant;
    public final String notePt;
    public final String noteEn;

    KanaEntry(String hiragana, String katakana, String romaji, String line,
              String category, String baseHiragana, String baseKatakana,
              String variant, String notePt, String noteEn) {
        this.hiragana = hiragana;
        this.katakana = katakana;
        this.romaji = romaji;
        this.line = line;
        this.category = category;
        this.baseHiragana = baseHiragana;
        this.baseKatakana = baseKatakana;
        this.variant = variant;
        this.notePt = notePt;
        this.noteEn = noteEn;
    }

    public String symbol(boolean useKatakana) {
        if (useKatakana && !katakana.isEmpty()) return katakana;
        if (!hiragana.isEmpty()) return hiragana;
        return katakana;
    }

    public String counterpart(boolean showingKatakana) {
        return showingKatakana ? hiragana : katakana;
    }

    public String baseSymbol(boolean useKatakana) {
        return useKatakana ? baseKatakana : baseHiragana;
    }

    boolean matches(String normalizedQuery) {
        if (normalizedQuery.isEmpty()) return true;
        String query = normalizedQuery.toLowerCase(Locale.ROOT);
        return hiragana.contains(normalizedQuery)
            || katakana.contains(normalizedQuery)
            || baseHiragana.contains(normalizedQuery)
            || baseKatakana.contains(normalizedQuery)
            || romaji.toLowerCase(Locale.ROOT).contains(query)
            || category.toLowerCase(Locale.ROOT).contains(query)
            || variant.toLowerCase(Locale.ROOT).contains(query);
    }
}
