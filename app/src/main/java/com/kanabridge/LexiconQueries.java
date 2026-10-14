package com.kanabridge;

import java.util.Locale;

/** Parameterized dictionary queries; converter lookup remains independent. */
public final class LexiconQueries {
    public static final String JAPANESE = "SELECT word,reading,meaning,entry,original_reading FROM words WHERE (reading>=? AND reading<?) OR (word>=? AND word<?) ORDER BY CASE WHEN reading=? OR word=? THEN 0 ELSE 1 END,rank,length(reading),word,reading,entry,meaning LIMIT ? OFFSET ?";
    public static final String MEANING = "SELECT word,reading,meaning,entry,original_reading FROM definitions CROSS JOIN spellings ON spellings.definition=definitions.id WHERE meaning LIKE ? ESCAPE '\\' COLLATE NOCASE ORDER BY CASE WHEN meaning=? COLLATE NOCASE THEN 0 ELSE 1 END,rank,length(meaning),word,reading,entry,meaning LIMIT ? OFFSET ?";
    public final String sql;
    public final String[] args;
    public LexiconQueries(String query, boolean meaning, int limit, int offset) {
        this(query, meaning ? 1 : 0, limit, offset);
    }
    public LexiconQueries(String query, int mode, int limit, int offset) {
        String value = query.trim();
        String size = String.valueOf(Math.max(1, limit)), start = String.valueOf(Math.max(0, offset));
        if (mode != 0) {
            String escaped = value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
            sql = MEANING; args = new String[]{"%" + escaped + "%", value.toLowerCase(Locale.ROOT), size, start};
        } else {
            String kana = KanaData.toHiragana(RomajiConverter.convert(value).hiragana);
            sql = JAPANESE; args = new String[]{kana, kana + '\uffff', value, value + '\uffff', kana, value, size, start};
        }
    }
}
