package com.kanabridge;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Offline Hepburn-style kana transliteration. No Android dependencies. */
public final class KanaTransliterator {
    public static final class Token {
        public final String kana;
        public final String romaji;
        public final String scriptPt;
        public final String scriptEn;
        public final String notePt;
        public final String noteEn;

        Token(String kana, String romaji, String scriptPt, String scriptEn,
              String notePt, String noteEn) {
            this.kana = kana;
            this.romaji = romaji;
            this.scriptPt = scriptPt;
            this.scriptEn = scriptEn;
            this.notePt = notePt;
            this.noteEn = noteEn;
        }
    }

    public static final class Result {
        public final String original;
        public final String romaji;
        public final List<Token> tokens;
        public final String translationPt;
        public final String translationEn;

        Result(String original, String romaji, List<Token> tokens,
               String translationPt, String translationEn) {
            this.original = original;
            this.romaji = romaji;
            this.tokens = Collections.unmodifiableList(tokens);
            this.translationPt = translationPt;
            this.translationEn = translationEn;
        }
    }

    private static final Map<String, String> KANA = new HashMap<>(192);
    private static final Map<String, String> COMBOS = new HashMap<>(128);
    private static final Map<String, Word> DICTIONARY = new HashMap<>(128);

    private static final class Word {
        final String reading;
        final String translationPt;
        final String translationEn;

        Word(String reading, String translationPt, String translationEn) {
            this.reading = reading;
            this.translationPt = translationPt;
            this.translationEn = translationEn;
        }
    }

    static {
        addRows(
            "あ:a い:i う:u え:e お:o か:ka き:ki く:ku け:ke こ:ko " +
            "さ:sa し:shi す:su せ:se そ:so た:ta ち:chi つ:tsu て:te と:to " +
            "な:na に:ni ぬ:nu ね:ne の:no は:ha ひ:hi ふ:fu へ:he ほ:ho " +
            "ま:ma み:mi む:mu め:me も:mo や:ya ゆ:yu よ:yo " +
            "ら:ra り:ri る:ru れ:re ろ:ro わ:wa ゐ:wi ゑ:we を:wo ん:n " +
            "が:ga ぎ:gi ぐ:gu げ:ge ご:go ざ:za じ:ji ず:zu ぜ:ze ぞ:zo " +
            "だ:da ぢ:ji づ:zu で:de ど:do ば:ba び:bi ぶ:bu べ:be ぼ:bo " +
            "ぱ:pa ぴ:pi ぷ:pu ぺ:pe ぽ:po ゔ:vu " +
            "ぁ:a ぃ:i ぅ:u ぇ:e ぉ:o ゃ:ya ゅ:yu ょ:yo ゎ:wa ゕ:ka ゖ:ke " +
            "っ: ー: ゝ: ゞ: "
        );

        // Katakana is algorithmically parallel to hiragana, including rare kana.
        Map<String, String> snapshot = new HashMap<>(KANA);
        for (Map.Entry<String, String> e : snapshot.entrySet()) {
            int cp = e.getKey().codePointAt(0);
            if (cp >= 0x3041 && cp <= 0x3096) {
                KANA.put(new String(Character.toChars(cp + 0x60)), e.getValue());
            }
        }
        addRows("ヷ:va ヸ:vi ヹ:ve ヺ:vo ヵ:ka ヶ:ke ヮ:wa ヽ: ヾ: ヿ:koto " +
            "ㇰ:ku ㇱ:shi ㇲ:su ㇳ:to ㇴ:nu ㇵ:ha ㇶ:hi ㇷ:fu ㇸ:he ㇹ:ho " +
            "ㇺ:mu ㇻ:ra ㇼ:ri ㇽ:ru ㇾ:re ㇿ:ro " +
            "・:· 。:. 、:, 「:\u201c 」:\u201d 『:\u201c 』:\u201d ！:! ？:?　: ");

        addCombos(
            "きゃ:kya きゅ:kyu きょ:kyo ぎゃ:gya ぎゅ:gyu ぎょ:gyo " +
            "しゃ:sha しゅ:shu しょ:sho じゃ:ja じゅ:ju じょ:jo " +
            "ちゃ:cha ちゅ:chu ちょ:cho ぢゃ:ja ぢゅ:ju ぢょ:jo " +
            "にゃ:nya にゅ:nyu にょ:nyo ひゃ:hya ひゅ:hyu ひょ:hyo " +
            "びゃ:bya びゅ:byu びょ:byo ぴゃ:pya ぴゅ:pyu ぴょ:pyo " +
            "みゃ:mya みゅ:myu みょ:myo りゃ:rya りゅ:ryu りょ:ryo " +
            "いぇ:ye うぃ:wi うぇ:we うぉ:wo " +
            "ゔぁ:va ゔぃ:vi ゔぇ:ve ゔぉ:vo ゔゅ:vyu " +
            "しぇ:she じぇ:je ちぇ:che " +
            "てぃ:ti でぃ:di とぅ:tu どぅ:du てゅ:tyu でゅ:dyu " +
            "ふぁ:fa ふぃ:fi ふぇ:fe ふぉ:fo ふゅ:fyu " +
            "つぁ:tsa つぃ:tsi つぇ:tse つぉ:tso " +
            "くぁ:kwa くぃ:kwi くぇ:kwe くぉ:kwo くゎ:kwa " +
            "ぐぁ:gwa ぐぃ:gwi ぐぇ:gwe ぐぉ:gwo ぐゎ:gwa " +
            "すぃ:si ずぃ:zi "
        );
        Map<String, String> comboSnapshot = new HashMap<>(COMBOS);
        for (Map.Entry<String, String> e : comboSnapshot.entrySet()) {
            COMBOS.put(toKatakana(e.getKey()), e.getValue());
        }

        addWord("あい", "amor", "love");
        addWord("あお", "azul", "blue");
        addWord("あか", "vermelho", "red");
        addWord("ありがとう", "obrigado/a", "thank you");
        addWord("おはよう", "bom dia", "good morning");
        addWord("こんにちは", "olá / boa tarde", "hello / good afternoon");
        addWord("こんばんは", "boa noite (saudação)", "good evening");
        addWord("おやすみ", "boa noite (ao despedir-se)", "good night");
        addWord("さようなら", "adeus", "goodbye");
        addWord("はい", "sim", "yes");
        addWord("いいえ", "não", "no");
        addWord("すみません", "desculpe / com licença", "sorry / excuse me");
        addWord("ごめんなさい", "desculpe", "I am sorry");
        addWord("おねがい", "pedido / por favor", "request / please");
        addWord("ください", "por favor, dê-me…", "please give me…");
        addWord("なまえ", "nome", "name");
        addWord("わたし", "eu", "I / me");
        addWord("あなた", "tu / você", "you");
        addWord("ともだち", "amigo/a", "friend");
        addWord("かぞく", "família", "family");
        addWord("ひと", "pessoa", "person");
        addWord("おとこ", "homem", "man");
        addWord("おんな", "mulher", "woman");
        addWord("こども", "criança", "child");
        addWord("ねこ", "gato", "cat");
        addWord("いぬ", "cão", "dog");
        addWord("とり", "ave / pássaro", "bird");
        addWord("さかな", "peixe", "fish");
        addWord("みず", "água", "water");
        addWord("おちゃ", "chá", "tea");
        addWord("ごはん", "arroz / refeição", "rice / meal");
        addWord("たべもの", "comida", "food");
        addWord("のみもの", "bebida", "drink");
        addWord("がっこう", "escola", "school");
        addWord("せんせい", "professor/a", "teacher");
        addWord("がくせい", "estudante", "student");
        addWord("にほん", "Japão", "Japan");
        addWord("にほんご", "língua japonesa", "Japanese language");
        addWord("えいご", "língua inglesa", "English language");
        addWord("いえ", "casa", "house / home");
        addWord("へや", "quarto / divisão", "room");
        addWord("くるま", "carro", "car");
        addWord("でんしゃ", "comboio", "train");
        addWord("じかん", "tempo / hora", "time");
        addWord("きょう", "hoje", "today");
        addWord("あした", "amanhã", "tomorrow");
        addWord("きのう", "ontem", "yesterday");
        addWord("いま", "agora", "now");
        addWord("すき", "gosto / favorito", "liked / favourite");
        addWord("だいすき", "adoro / gosto muito", "love / like very much");
        addWord("かわいい", "fofo/a", "cute");
        addWord("きれい", "bonito/a / limpo/a", "beautiful / clean");
        addWord("おいしい", "delicioso/a", "delicious");
        addWord("たのしい", "divertido/a", "fun / enjoyable");
        addWord("うれしい", "feliz / contente", "happy / glad");
        addWord("かなしい", "triste", "sad");
        addWord("だいじょうぶ", "está tudo bem", "all right / okay");
        addWord("コンピューター", "computador", "computer");
        addWord("コーヒー", "café", "coffee");
        addWord("テレビ", "televisão", "television");
        addWord("アニメ", "anime / animação japonesa", "anime / Japanese animation");
        addWord("マンガ", "manga / banda desenhada japonesa", "manga / Japanese comic");
        addWord("スマホ", "telemóvel inteligente", "smartphone");
        addWord("ホテル", "hotel", "hotel");
        addWord("レストラン", "restaurante", "restaurant");
        addWord("バイク", "mota", "motorbike");

        // Leituras de palavras completas em kanji de nível inicial. Kanji isolado pode
        // ter mais do que uma leitura; por isso apenas reconhecemos entradas exatas.
        addKanjiWord("一", "ichi", "um", "one");
        addKanjiWord("二", "ni", "dois", "two");
        addKanjiWord("三", "san", "três", "three");
        addKanjiWord("人", "hito", "pessoa", "person");
        addKanjiWord("山", "yama", "montanha", "mountain");
        addKanjiWord("川", "kawa", "rio", "river");
        addKanjiWord("口", "kuchi", "boca", "mouth");
        addKanjiWord("目", "me", "olho", "eye");
        addKanjiWord("手", "te", "mão", "hand");
        addKanjiWord("日", "hi", "dia / sol", "day / sun");
        addKanjiWord("月", "tsuki", "lua / mês", "moon / month");
        addKanjiWord("火", "hi", "fogo", "fire");
        addKanjiWord("水", "mizu", "água", "water");
        addKanjiWord("木", "ki", "árvore / madeira", "tree / wood");
        addKanjiWord("金", "kane", "dinheiro / ouro", "money / gold");
        addKanjiWord("土", "tsuchi", "terra", "soil / earth");
        addKanjiWord("日本", "nihon", "Japão", "Japan");
        addKanjiWord("日本語", "nihongo", "língua japonesa", "Japanese language");
        addKanjiWord("学校", "gakkou", "escola", "school");
        addKanjiWord("先生", "sensei", "professor/a", "teacher");
        addKanjiWord("学生", "gakusei", "estudante", "student");
    }

    private KanaTransliterator() {}

    public static Result transliterate(String source) {
        // NFKC also converts half-width katakana (e.g. ｶﾀｶﾅ) to standard katakana.
        String input = Normalizer.normalize(source == null ? "" : source, Normalizer.Form.NFKC);
        List<Token> tokens = new ArrayList<>();
        StringBuilder output = new StringBuilder();
        String previousRomaji = "";

        for (int i = 0; i < input.length();) {
            int cp = input.codePointAt(i);
            String current = new String(Character.toChars(cp));
            int currentLength = current.length();

            if (isSmallTsu(current)) {
                int nextIndex = i + currentLength;
                String nextRomaji = romajiAt(input, nextIndex);
                String doubled = doubledConsonant(nextRomaji);
                output.append(doubled);
                tokens.add(makeToken(current, doubled, "Marca consoante dupla", "Double-consonant marker"));
                previousRomaji = doubled;
                i = nextIndex;
                continue;
            }

            if ("ー".equals(current)) {
                String vowel = finalVowel(output);
                output.append(vowel);
                tokens.add(makeToken(current, vowel, "Prolonga a vogal anterior", "Lengthens the previous vowel"));
                previousRomaji = vowel;
                i += currentLength;
                continue;
            }

            if (isIterationMark(current)) {
                boolean voiced = "ゞ".equals(current) || "ヾ".equals(current);
                String repeated = voiced ? voice(previousRomaji) : previousRomaji;
                output.append(repeated);
                tokens.add(makeToken(current, repeated,
                    voiced ? "Repete o kana anterior com voz" : "Repete o kana anterior",
                    voiced ? "Repeats the previous kana with voicing" : "Repeats the previous kana"));
                previousRomaji = repeated;
                i += currentLength;
                continue;
            }

            String pair = null;
            int pairLength = currentLength;
            if (i + currentLength < input.length()) {
                int nextCp = input.codePointAt(i + currentLength);
                String next = new String(Character.toChars(nextCp));
                String candidate = current + next;
                if (COMBOS.containsKey(candidate)) {
                    pair = candidate;
                    pairLength += next.length();
                }
            }

            String unit = pair != null ? pair : current;
            String romaji = pair != null ? COMBOS.get(pair) : KANA.get(current);
            if (romaji == null) {
                romaji = current;
            }

            // ん before a vowel/y receives an apostrophe to avoid ambiguity.
            if (("ん".equals(current) || "ン".equals(current)) && i + currentLength < input.length()) {
                String next = romajiAt(input, i + currentLength);
                if (!next.isEmpty() && "aiueoy".indexOf(Character.toLowerCase(next.charAt(0))) >= 0) {
                    romaji = "n'";
                }
            }

            output.append(romaji);
            tokens.add(makeToken(unit, romaji, null, null));
            if (isKana(current) && !romaji.trim().isEmpty()) previousRomaji = romaji;
            i += pairLength;
        }

        Word word = DICTIONARY.get(input.trim());
        String romaji = word != null && !word.reading.isEmpty() ? word.reading : output.toString();
        String pt = word == null ? "" : word.translationPt;
        String en = word == null ? "" : word.translationEn;
        return new Result(input, romaji, tokens, pt, en);
    }

    private static Token makeToken(String kana, String romaji, String overridePt, String overrideEn) {
        String scriptPt;
        String scriptEn;
        int cp = kana.codePointAt(0);
        if (isHiragana(cp)) {
            scriptPt = "Hiragana";
            scriptEn = "Hiragana";
        } else if (isKatakana(cp)) {
            scriptPt = "Katakana";
            scriptEn = "Katakana";
        } else {
            scriptPt = "Símbolo / texto";
            scriptEn = "Symbol / text";
        }

        String notePt = overridePt;
        String noteEn = overrideEn;
        if (notePt == null) {
            if (isKana(kana.substring(0, Character.charCount(cp)))) {
                boolean combo = kana.codePointCount(0, kana.length()) > 1;
                String typePt = combo ? "Combinação; som “" + romaji + "”" : "Som / leitura “" + romaji + "”";
                String typeEn = combo ? "Combination; “" + romaji + "” sound" : "Sound / reading “" + romaji + "”";
                notePt = typePt + ". Kana representa som, não uma tradução isolada.";
                noteEn = typeEn + ". Kana represents sound, not an isolated translation.";
            } else {
                notePt = "Mantido no resultado.";
                noteEn = "Preserved in the result.";
            }
        }
        return new Token(kana, romaji, scriptPt, scriptEn, notePt, noteEn);
    }

    private static String romajiAt(String input, int index) {
        if (index >= input.length()) return "";
        int cp = input.codePointAt(index);
        String first = new String(Character.toChars(cp));
        int nextIndex = index + first.length();
        if (nextIndex < input.length()) {
            int cp2 = input.codePointAt(nextIndex);
            String pair = first + new String(Character.toChars(cp2));
            if (COMBOS.containsKey(pair)) return COMBOS.get(pair);
        }
        String value = KANA.get(first);
        return value == null ? "" : value;
    }

    private static String doubledConsonant(String next) {
        if (next == null || next.isEmpty()) return "";
        String lower = next.toLowerCase(Locale.ROOT);
        if (lower.startsWith("ch")) return "t";
        if (lower.startsWith("sh")) return "s";
        if (lower.startsWith("ts")) return "t";
        char first = lower.charAt(0);
        return "aeioun".indexOf(first) >= 0 ? "" : String.valueOf(first);
    }

    private static String finalVowel(StringBuilder text) {
        for (int i = text.length() - 1; i >= 0; i--) {
            char c = Character.toLowerCase(text.charAt(i));
            if ("aeiou".indexOf(c) >= 0) return String.valueOf(c);
            if (Character.isLetter(c)) break;
        }
        return "";
    }

    private static String voice(String roma) {
        if (roma == null || roma.isEmpty()) return "";
        if (roma.startsWith("k")) return "g" + roma.substring(1);
        if (roma.startsWith("s")) return "z" + roma.substring(1);
        if (roma.startsWith("t")) return "d" + roma.substring(1);
        if (roma.startsWith("h")) return "b" + roma.substring(1);
        return roma;
    }

    private static boolean isSmallTsu(String s) { return "っ".equals(s) || "ッ".equals(s); }
    private static boolean isIterationMark(String s) { return "ゝ".equals(s) || "ゞ".equals(s) || "ヽ".equals(s) || "ヾ".equals(s); }
    private static boolean isKana(String s) { return s != null && !s.isEmpty() && (isHiragana(s.codePointAt(0)) || isKatakana(s.codePointAt(0))); }
    private static boolean isHiragana(int cp) { return cp >= 0x3040 && cp <= 0x309F; }
    private static boolean isKatakana(int cp) { return (cp >= 0x30A0 && cp <= 0x30FF) || (cp >= 0x31F0 && cp <= 0x31FF); }

    private static String toKatakana(String hiragana) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < hiragana.length();) {
            int cp = hiragana.codePointAt(i);
            if (cp >= 0x3041 && cp <= 0x3096) cp += 0x60;
            result.appendCodePoint(cp);
            i += Character.charCount(cp);
        }
        return result.toString();
    }

    private static void addRows(String encoded) {
        for (String item : encoded.split(" ")) {
            int colon = item.indexOf(':');
            if (colon >= 0) KANA.put(item.substring(0, colon), item.substring(colon + 1));
        }
    }

    private static void addCombos(String encoded) {
        for (String item : encoded.split(" ")) {
            int colon = item.indexOf(':');
            if (colon >= 0) COMBOS.put(item.substring(0, colon), item.substring(colon + 1));
        }
    }

    private static void addWord(String kana, String pt, String en) {
        DICTIONARY.put(kana, new Word("", pt, en));
    }

    private static void addKanjiWord(String kanji, String reading, String pt, String en) {
        DICTIONARY.put(kanji, new Word(reading, pt, en));
    }
}
