package com.kanabridge;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Deterministic IME-style spelling conversion. Kanji selection is a separate lexical operation. */
public final class RomajiConverter {
    private static final Map<String, String> MAP = new HashMap<>();
    static {
        for (String category : new String[]{KanaData.GOJUON, KanaData.DAKUTEN, KanaData.YOON, KanaData.MODERN}) {
            for (KanaEntry entry : KanaData.entries(category)) {
                String kana = KanaData.toHiragana(entry.symbol(false));
                if (!MAP.containsKey(entry.romaji)) MAP.put(entry.romaji, kana);
            }
        }
        aliases("si:し ti:ち tu:つ hu:ふ zi:じ di:ぢ du:づ sya:しゃ syu:しゅ syo:しょ tya:ちゃ tyu:ちゅ tyo:ちょ cya:ちゃ cyu:ちゅ cyo:ちょ jya:じゃ jyu:じゅ jyo:じょ zya:じゃ zyu:じゅ zyo:じょ dya:ぢゃ dyu:ぢゅ dyo:ぢょ she:しぇ che:ちぇ je:じぇ wi:うぃ we:うぇ ye:いぇ kwa:くぁ gwa:ぐぁ tsa:つぁ tsi:つぃ tse:つぇ tso:つぉ");
        aliases("sye:しぇ tye:ちぇ cye:ちぇ jye:じぇ zye:じぇ");
        aliases("xa:ぁ xi:ぃ xu:ぅ xe:ぇ xo:ぉ xya:ゃ xyu:ゅ xyo:ょ xtu:っ xtsu:っ xwa:ゎ xka:ゕ xke:ゖ la:ぁ li:ぃ lu:ぅ le:ぇ lo:ぉ lya:ゃ lyu:ゅ lyo:ょ ltu:っ ltsu:っ lwa:ゎ lka:ゕ lke:ゖ");
    }
    private static void aliases(String values) {
        for (String value : values.split(" ")) { String[] pair = value.split(":"); MAP.put(pair[0], pair[1]); }
    }
    public static final class Result {
        public final String hiragana, katakana;
        public final boolean partial;
        Result(String value, boolean partial) { hiragana = value; katakana = KanaData.toKatakana(value); this.partial = partial; }
    }
    public static Result convert(String source) {
        if (source == null) source = "";
        String input = KanaTransliterator.normalizeKana(Normalizer.normalize(source, Normalizer.Form.NFKC))
            .toLowerCase(Locale.ROOT).replace('’', '\'').replace("ā", "aa").replace("ī", "ii")
            .replace("ū", "uu").replace("ē", "ee").replace("ō", "ou");
        // Conventional greetings have lexical spellings; do not rewrite particles globally.
        input = input.replaceAll("(?<![a-z])konnichiwa(?![a-z])", "konnichiha")
            .replaceAll("(?<![a-z])konbanwa(?![a-z])", "konbanha");
        StringBuilder out = new StringBuilder();
        boolean partial = false;
        for (int i = 0; i < input.length();) {
            char ch = input.charAt(i);
            if (ch == '-') { out.append('ー'); i++; continue; }
            if (ch == 'n') {
                if (i + 1 == input.length() || !isLetter(input.charAt(i + 1))) {
                    out.append('ん'); i++;
                    if (i < input.length() && input.charAt(i) == '\'') i++;
                    continue;
                }
                char next = input.charAt(i + 1);
                if (next == 'n') {
                    out.append('ん');
                    // nn at a boundary is one nasal; nna/nnya keep the second n for the next mora.
                    i += i + 2 < input.length() && "aiueoy".indexOf(input.charAt(i + 2)) >= 0 ? 1 : 2;
                    continue;
                }
                if ("aiueoy".indexOf(next) < 0) { out.append('ん'); i++; continue; }
            }
            if (i + 1 < input.length() && ch == input.charAt(i + 1) && "bcdfghjkpqrstvwxyz".indexOf(ch) >= 0
                && hasSyllable(input, i + 1)) { out.append('っ'); i++; continue; }
            if (input.startsWith("tch", i) && hasSyllable(input, i + 1)) { out.append('っ'); i++; continue; }
            String kana = null; int size = 0;
            for (int length = Math.min(4, input.length() - i); length > 0; length--) {
                kana = MAP.get(input.substring(i, i + length));
                if (kana != null) { size = length; break; }
            }
            if (kana != null) { out.append(kana); i += size; }
            else {
                int cp = input.codePointAt(i);
                // Preserve unrecognised input verbatim, including emoji and Latin case.
                out.appendCodePoint(cp <= 127 && isLetter(ch) && source.length() == input.length() ? source.codePointAt(i) : cp);
                if (isLetter(ch)) partial = true;
                i += Character.charCount(cp);
            }
        }
        return new Result(KanaData.toHiragana(out.toString()), partial);
    }
    private static boolean isLetter(char c) { return c >= 'a' && c <= 'z'; }
    private static boolean hasSyllable(String input, int offset) {
        for (int n = Math.min(4, input.length() - offset); n > 0; n--) if (MAP.containsKey(input.substring(offset, offset + n))) return true;
        return false;
    }
}
