package com.kanabridge;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Central, Android-free kana catalogue shared by transliteration, search and UI. */
public final class KanaData {
    public static final String GOJUON = "Gojuon";
    public static final String DAKUTEN = "Dakuten";
    public static final String YOON = "Yōon";
    public static final String SMALL = "Kana pequenos";
    public static final String MODERN = "Katakana moderno";
    public static final String AINU = "Extensões Ainu";
    public static final String REFERENCE = "Sinais e kana históricos";

    private static final List<KanaEntry> ENTRIES = new ArrayList<>(160);
    private static final Map<String, List<KanaEntry>> BY_CATEGORY = new HashMap<>();
    private static final Map<String, KanaEntry> BY_SYMBOL = new HashMap<>(256);
    private static final Map<String, String> SINGLE_READINGS = new HashMap<>(256);
    private static final Map<String, String> COMBO_READINGS = new HashMap<>(160);

    private static final String[] GOJUON_ROW_LABELS = {
        "∅", "k", "s", "t", "n", "h", "m", "y", "r", "w", "n"
    };

    private static final String[][] GOJUON_HIRAGANA = {
        {"あ", "い", "う", "え", "お"},
        {"か", "き", "く", "け", "こ"},
        {"さ", "し", "す", "せ", "そ"},
        {"た", "ち", "つ", "て", "と"},
        {"な", "に", "ぬ", "ね", "の"},
        {"は", "ひ", "ふ", "へ", "ほ"},
        {"ま", "み", "む", "め", "も"},
        {"や", "", "ゆ", "", "よ"},
        {"ら", "り", "る", "れ", "ろ"},
        {"わ", "", "", "", "を"},
        {"ん", "", "", "", ""}
    };

    static {
        addPairs(GOJUON, "Vogais", "あ:ア:a い:イ:i う:ウ:u え:エ:e お:オ:o");
        addPairs(GOJUON, "K", "か:カ:ka き:キ:ki く:ク:ku け:ケ:ke こ:コ:ko");
        addPairs(GOJUON, "S", "さ:サ:sa し:シ:shi す:ス:su せ:セ:se そ:ソ:so");
        addPairs(GOJUON, "T", "た:タ:ta ち:チ:chi つ:ツ:tsu て:テ:te と:ト:to");
        addPairs(GOJUON, "N", "な:ナ:na に:ニ:ni ぬ:ヌ:nu ね:ネ:ne の:ノ:no");
        addPairs(GOJUON, "H", "は:ハ:ha ひ:ヒ:hi ふ:フ:fu へ:ヘ:he ほ:ホ:ho");
        addPairs(GOJUON, "M", "ま:マ:ma み:ミ:mi む:ム:mu め:メ:me も:モ:mo");
        addPairs(GOJUON, "Y", "や:ヤ:ya ゆ:ユ:yu よ:ヨ:yo");
        addPairs(GOJUON, "R", "ら:ラ:ra り:リ:ri る:ル:ru れ:レ:re ろ:ロ:ro");
        addPairs(GOJUON, "W", "わ:ワ:wa を:ヲ:wo");
        addPairs(GOJUON, "N", "ん:ン:n");

        addModified("か", "カ", "が", "ガ", "ga", "K", "Dakuten ゛");
        addModified("き", "キ", "ぎ", "ギ", "gi", "K", "Dakuten ゛");
        addModified("く", "ク", "ぐ", "グ", "gu", "K", "Dakuten ゛");
        addModified("け", "ケ", "げ", "ゲ", "ge", "K", "Dakuten ゛");
        addModified("こ", "コ", "ご", "ゴ", "go", "K", "Dakuten ゛");
        addModified("さ", "サ", "ざ", "ザ", "za", "S", "Dakuten ゛");
        addModified("し", "シ", "じ", "ジ", "ji", "S", "Dakuten ゛");
        addModified("す", "ス", "ず", "ズ", "zu", "S", "Dakuten ゛");
        addModified("せ", "セ", "ぜ", "ゼ", "ze", "S", "Dakuten ゛");
        addModified("そ", "ソ", "ぞ", "ゾ", "zo", "S", "Dakuten ゛");
        addModified("た", "タ", "だ", "ダ", "da", "T", "Dakuten ゛");
        addModified("ち", "チ", "ぢ", "ヂ", "ji", "T", "Dakuten ゛");
        addModified("つ", "ツ", "づ", "ヅ", "zu", "T", "Dakuten ゛");
        addModified("て", "テ", "で", "デ", "de", "T", "Dakuten ゛");
        addModified("と", "ト", "ど", "ド", "do", "T", "Dakuten ゛");
        addModified("は", "ハ", "ば", "バ", "ba", "H", "Dakuten ゛");
        addModified("ひ", "ヒ", "び", "ビ", "bi", "H", "Dakuten ゛");
        addModified("ふ", "フ", "ぶ", "ブ", "bu", "H", "Dakuten ゛");
        addModified("へ", "ヘ", "べ", "ベ", "be", "H", "Dakuten ゛");
        addModified("ほ", "ホ", "ぼ", "ボ", "bo", "H", "Dakuten ゛");
        addModified("は", "ハ", "ぱ", "パ", "pa", "H", "Handakuten ゜");
        addModified("ひ", "ヒ", "ぴ", "ピ", "pi", "H", "Handakuten ゜");
        addModified("ふ", "フ", "ぷ", "プ", "pu", "H", "Handakuten ゜");
        addModified("へ", "ヘ", "ぺ", "ペ", "pe", "H", "Handakuten ゜");
        addModified("ほ", "ホ", "ぽ", "ポ", "po", "H", "Handakuten ゜");

        addPairs(YOON, "K", "きゃ:キャ:kya きゅ:キュ:kyu きょ:キョ:kyo");
        addPairs(YOON, "S", "しゃ:シャ:sha しゅ:シュ:shu しょ:ショ:sho");
        addPairs(YOON, "T", "ちゃ:チャ:cha ちゅ:チュ:chu ちょ:チョ:cho");
        addPairs(YOON, "N", "にゃ:ニャ:nya にゅ:ニュ:nyu にょ:ニョ:nyo");
        addPairs(YOON, "H", "ひゃ:ヒャ:hya ひゅ:ヒュ:hyu ひょ:ヒョ:hyo");
        addPairs(YOON, "M", "みゃ:ミャ:mya みゅ:ミュ:myu みょ:ミョ:myo");
        addPairs(YOON, "R", "りゃ:リャ:rya りゅ:リュ:ryu りょ:リョ:ryo");
        addPairs(YOON, "G", "ぎゃ:ギャ:gya ぎゅ:ギュ:gyu ぎょ:ギョ:gyo");
        addPairs(YOON, "J", "じゃ:ジャ:ja じゅ:ジュ:ju じょ:ジョ:jo");
        addPairs(YOON, "B", "びゃ:ビャ:bya びゅ:ビュ:byu びょ:ビョ:byo");
        addPairs(YOON, "P", "ぴゃ:ピャ:pya ぴゅ:ピュ:pyu ぴょ:ピョ:pyo");
        addPairs(YOON, "J", "ぢゃ:ヂャ:ja ぢゅ:ヂュ:ju ぢょ:ヂョ:jo");

        addPairs(SMALL, "Vogais pequenas", "ぁ:ァ:a ぃ:ィ:i ぅ:ゥ:u ぇ:ェ:e ぉ:ォ:o");
        addPairs(SMALL, "Y/W pequenos", "ゃ:ャ:ya ゅ:ュ:yu ょ:ョ:yo ゎ:ヮ:wa");
        addEntry(new KanaEntry("っ", "ッ", "", "Consoante dupla", SMALL, "", "", "Sokuon",
            "Prepara a consoante seguinte com uma breve pausa. っしゃ → ssha em いらっしゃいませ → irasshaimase; った → tta em かった → katta. Não se lê tsu.",
            "Usually doubles the following consonant, as in かった → katta."));

        addPairs(MODERN, "V", "ゔ:ヴ:vu");
        addPairs(MODERN, "Palavras estrangeiras",
            "いぇ:イェ:ye うぃ:ウィ:wi うぇ:ウェ:we うぉ:ウォ:wo " +
            "ゔぁ:ヴァ:va ゔぃ:ヴィ:vi ゔぇ:ヴェ:ve ゔぉ:ヴォ:vo ゔゅ:ヴュ:vyu " +
            "しぇ:シェ:she じぇ:ジェ:je ちぇ:チェ:che " +
            "てぃ:ティ:ti でぃ:ディ:di とぅ:トゥ:tu どぅ:ドゥ:du てゅ:テュ:tyu でゅ:デュ:dyu " +
            "ふぁ:ファ:fa ふぃ:フィ:fi ふぇ:フェ:fe ふぉ:フォ:fo ふゅ:フュ:fyu " +
            "つぁ:ツァ:tsa つぃ:ツィ:tsi つぇ:ツェ:tse つぉ:ツォ:tso " +
            "くぁ:クァ:kwa くぃ:クィ:kwi くぇ:クェ:kwe くぉ:クォ:kwo くゎ:クヮ:kwa " +
            "ぐぁ:グァ:gwa ぐぃ:グィ:gwi ぐぇ:グェ:gwe ぐぉ:グォ:gwo ぐゎ:グヮ:gwa " +
            "すぃ:スィ:si ずぃ:ズィ:zi");

        addPairs(AINU, "Ainu",
            ":ㇰ:ku :ㇱ:shi :ㇲ:su :ㇳ:to :ㇴ:nu :ㇵ:ha :ㇶ:hi :ㇷ:fu " +
            ":ㇸ:he :ㇹ:ho :ㇺ:mu :ㇻ:ra :ㇼ:ri :ㇽ:ru :ㇾ:re :ㇿ:ro");

        putSingle("ゐ", "wi"); putSingle("ゑ", "we");
        putSingle("ヰ", "wi"); putSingle("ヱ", "we");
        putSingle("ゕ", "ka"); putSingle("ゖ", "ke");
        putSingle("ヵ", "ka"); putSingle("ヶ", "ke"); putSingle("ヿ", "koto");
        putSingle("ヷ", "va"); putSingle("ヸ", "vi"); putSingle("ヹ", "ve"); putSingle("ヺ", "vo");
        putSingle("ー", ""); putSingle("ゝ", ""); putSingle("ゞ", "");
        putSingle("ヽ", ""); putSingle("ヾ", "");
        putSingle("・", "·"); putSingle("。", "."); putSingle("、", ",");
        putSingle("「", "“"); putSingle("」", "”"); putSingle("『", "“"); putSingle("』", "”");
        putSingle("！", "!"); putSingle("？", "?"); putSingle("　", " ");

        addPairs(REFERENCE, "Kana históricos", "ゐ:ヰ:wi ゑ:ヱ:we ゕ:ヵ:ka ゖ:ヶ:ke");
        addEntry(new KanaEntry("", "ー", "", "Vogal longa", REFERENCE, "", "", "Chōonpu",
            "Prolonga a vogal anterior: コーヒー → koohii / kōhī.", "Lengthens the preceding vowel."));
        addEntry(new KanaEntry("ゝ", "ヽ", "", "Repetição", REFERENCE, "", "", "Iteração",
            "Repete o kana anterior sem dakuten.", "Repeats the previous kana without dakuten."));
        addEntry(new KanaEntry("ゞ", "ヾ", "", "Repetição sonora", REFERENCE, "", "", "Iteração sonora",
            "Repete o kana anterior com dakuten: しゞ → shiji.", "Repeats the preceding kana with dakuten."));

        for (KanaEntry entry : ENTRIES) {
            List<KanaEntry> category = BY_CATEGORY.get(entry.category);
            if (category == null) {
                category = new ArrayList<>();
                BY_CATEGORY.put(entry.category, category);
            }
            category.add(entry);
        }
        for (Map.Entry<String, List<KanaEntry>> category : BY_CATEGORY.entrySet()) {
            category.setValue(Collections.unmodifiableList(category.getValue()));
        }
    }

    private KanaData() {}

    public static List<KanaEntry> entries(String category) {
        List<KanaEntry> result = BY_CATEGORY.get(category);
        return result == null ? Collections.<KanaEntry>emptyList() : result;
    }

    public static List<KanaEntry> search(String query) {
        String normalized = Normalizer.normalize(query == null ? "" : query.trim(), Normalizer.Form.NFKC);
        if (normalized.isEmpty()) return Collections.emptyList();
        RomajiConverter.Result converted = RomajiConverter.convert(normalized);
        List<KanaEntry> result = new ArrayList<>();
        for (KanaEntry entry : ENTRIES) {
            if (entry.matches(normalized) || (!converted.partial && entry.matches(converted.hiragana))) result.add(entry);
        }
        return result;
    }

    public static KanaEntry find(String symbol) {
        return BY_SYMBOL.get(symbol);
    }

    public static String toKatakana(String source) {
        return convertScript(source, true);
    }

    public static String toHiragana(String source) {
        return convertScript(source, false);
    }

    static String readingFor(String symbol) {
        return SINGLE_READINGS.get(symbol);
    }

    static String comboReading(String symbol) {
        return COMBO_READINGS.get(symbol);
    }

    static boolean isHiragana(int codePoint) {
        return codePoint >= 0x3040 && codePoint <= 0x309F;
    }

    static boolean isKatakana(int codePoint) {
        return (codePoint >= 0x30A0 && codePoint <= 0x30FF)
            || (codePoint >= 0x31F0 && codePoint <= 0x31FF);
    }

    public static String[] gojuonRowLabels() {
        return GOJUON_ROW_LABELS.clone();
    }

    public static String[][] gojuonGrid(boolean katakana) {
        String[][] copy = new String[GOJUON_HIRAGANA.length][5];
        for (int row = 0; row < GOJUON_HIRAGANA.length; row++) {
            for (int column = 0; column < 5; column++) {
                String symbol = GOJUON_HIRAGANA[row][column];
                copy[row][column] = katakana ? toKatakana(symbol) : symbol;
            }
        }
        return copy;
    }

    private static String convertScript(String source, boolean katakana) {
        String input = KanaTransliterator.normalizeKana(source);
        StringBuilder output = new StringBuilder(input.length());
        for (int i = 0; i < input.length();) {
            int cp = input.codePointAt(i);
            int converted = cp;
            if (katakana && cp >= 0x3041 && cp <= 0x3096) converted = cp + 0x60;
            if (!katakana && cp >= 0x30A1 && cp <= 0x30F6) converted = cp - 0x60;
            if (katakana && (cp == 0x309D || cp == 0x309E)) converted = cp + 0x60;
            if (!katakana && (cp == 0x30FD || cp == 0x30FE)) converted = cp - 0x60;
            output.appendCodePoint(converted);
            i += Character.charCount(cp);
        }
        return output.toString();
    }

    private static void addPairs(String category, String line, String encoded) {
        String notePt = "";
        String noteEn = "";
        if (YOON.equals(category)) {
            notePt = "Combinação com ゃ, ゅ ou ょ pequeno para formar um único som.";
            noteEn = "Combination with small ゃ, ゅ or ょ to form one sound.";
        } else if (SMALL.equals(category)) {
            notePt = "Kana pequeno usado dentro de combinações fonéticas.";
            noteEn = "Small kana used inside phonetic combinations.";
        } else if (MODERN.equals(category)) {
            notePt = "Combinação usada sobretudo para sons de palavras estrangeiras.";
            noteEn = "Combination mainly used for sounds in foreign words.";
        } else if (AINU.equals(category)) {
            notePt = "Extensão fonética usada principalmente para escrever a língua Ainu.";
            noteEn = "Phonetic extension mainly used to write the Ainu language.";
        }
        for (String item : encoded.split(" ")) {
            String[] fields = item.split(":", -1);
            addEntry(new KanaEntry(fields[0], fields[1], fields[2], line, category,
                "", "", "", notePt, noteEn));
        }
    }

    private static void addModified(String baseHiragana, String baseKatakana,
                                    String hiragana, String katakana, String romaji,
                                    String line, String variant) {
        boolean handakuten = variant.startsWith("Handakuten");
        addEntry(new KanaEntry(hiragana, katakana, romaji, line, DAKUTEN,
            baseHiragana, baseKatakana, variant,
            handakuten ? "O handakuten transforma o som H num som P."
                       : "O dakuten torna a consoante sonora.",
            handakuten ? "Handakuten changes the H sound into a P sound."
                       : "Dakuten voices the consonant."));
    }

    private static void addEntry(KanaEntry entry) {
        ENTRIES.add(entry);
        register(entry.hiragana, entry.romaji, entry);
        register(entry.katakana, entry.romaji, entry);
    }

    private static void register(String symbol, String romaji, KanaEntry entry) {
        if (symbol.isEmpty()) return;
        BY_SYMBOL.put(symbol, entry);
        if (symbol.codePointCount(0, symbol.length()) > 1) {
            COMBO_READINGS.put(symbol, romaji);
        } else {
            SINGLE_READINGS.put(symbol, romaji);
        }
    }

    private static void putSingle(String symbol, String romaji) {
        SINGLE_READINGS.put(symbol, romaji);
    }
}
