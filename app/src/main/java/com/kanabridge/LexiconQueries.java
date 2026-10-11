package com.kanabridge;

import java.util.Locale;

/** Parameterized dictionary queries; converter lookup remains independent. */
public final class LexiconQueries {
    public static final String JAPANESE = "SELECT word,reading,meaning,entry,original_reading FROM words WHERE (reading>=? AND reading<?) OR (word>=? AND word<?) ORDER BY CASE WHEN reading=? OR word=? THEN 0 ELSE 1 END,rank,length(reading),word,reading,entry,meaning LIMIT ? OFFSET ?";
    public static final String MEANING = "SELECT word,reading,meaning,entry,original_reading FROM definitions CROSS JOIN spellings ON spellings.definition=definitions.id WHERE meaning LIKE ? ESCAPE '\\' COLLATE NOCASE ORDER BY CASE WHEN meaning=? COLLATE NOCASE THEN 0 ELSE 1 END,rank,length(meaning),word,reading,entry,meaning LIMIT ? OFFSET ?";
    public static final String PORTUGUESE = "SELECT spellings.word,spellings.reading,definitions.meaning,spellings.entry,original_reading FROM editorial CROSS JOIN spellings ON spellings.entry=editorial.entry AND spellings.word=editorial.word AND spellings.reading=editorial.reading JOIN definitions ON definitions.id=spellings.definition WHERE editorial.search_key LIKE ? ESCAPE '\\' COLLATE NOCASE ORDER BY rank,length(editorial.meaning),spellings.word,spellings.reading,spellings.entry LIMIT ? OFFSET ?";
    public final String sql;
    public final String[] args;
    public LexiconQueries(String query, boolean meaning, int limit, int offset) {
        this(query, meaning ? 1 : 0, limit, offset);
    }
    public LexiconQueries(String query, int mode, int limit, int offset) {
        String value = query.trim();
        String size = String.valueOf(Math.max(1, limit)), start = String.valueOf(Math.max(0, offset));
        if (mode != 0) {
            String literal = mode == 2 ? java.text.Normalizer.normalize(value.toLowerCase(Locale.ROOT), java.text.Normalizer.Form.NFD).replaceAll("[\\u0300-\\u036f]", "") : value;
            String escaped = literal.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
            if (mode == 2) { sql = PORTUGUESE; args = new String[]{"%" + escaped + "%", size, start}; return; }
            sql = MEANING; args = new String[]{"%" + escaped + "%", value.toLowerCase(Locale.ROOT), size, start};
        } else {
            String kana = KanaData.toHiragana(RomajiConverter.convert(value).hiragana);
            sql = JAPANESE; args = new String[]{kana, kana + '\uffff', value, value + '\uffff', kana, value, size, start};
        }
    }
}
