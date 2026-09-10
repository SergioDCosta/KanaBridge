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
    public enum RomanizationStyle { SIMPLE, MACRON }

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
        public final boolean partial;

        Result(String original, String romaji, List<Token> tokens,
               String translationPt, String translationEn) {
            this.original = original;
            this.romaji = romaji;
            this.tokens = Collections.unmodifiableList(tokens);
            this.translationPt = translationPt;
            this.translationEn = translationEn;
            this.partial = containsJapanese(romaji);
        }
    }

    public static final class DictionaryEntry {
        public final String word;
        public final String reading;
        public final String translationPt;
        public final String translationEn;

        DictionaryEntry(String word, String reading, String translationPt, String translationEn) {
            this.word = word;
            this.reading = reading;
            this.translationPt = translationPt;
            this.translationEn = translationEn;
        }
    }

    private static final Map<String, DictionaryEntry> DICTIONARY = new HashMap<>(128);
    private static final List<DictionaryEntry> DICTIONARY_ENTRIES = new ArrayList<>(96);

    static {
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
        addWord("いらっしゃいませ", "bem-vindo/a (saudação de atendimento em lojas e restaurantes)", "welcome (staff greeting in shops and restaurants)");
        addWord("きって", "selo postal", "postage stamp");
        addWord("まっちゃ", "matcha / chá verde em pó", "matcha / powdered green tea");
        addWord("ざっし", "revista", "magazine");
        addWord("きっぷ", "bilhete", "ticket");
        addWord("すうがく", "matemática", "mathematics");
        addWord("とうきょう", "Tóquio", "Tokyo");

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
        return transliterate(source, RomanizationStyle.SIMPLE);
    }

    public static Result transliterate(String source, RomanizationStyle style) {
        RomanizationStyle selectedStyle = style == null ? RomanizationStyle.SIMPLE : style;
        // NFKC also converts half-width katakana (e.g. ｶﾀｶﾅ) to standard katakana.
        String input = normalizeKana(source);
        List<Token> tokens = new ArrayList<>();
        StringBuilder output = new StringBuilder();
        String previousKana = "";
        String core = input.trim().replaceAll("[。！？!?.,、]+$", "");
        DictionaryEntry known = DICTIONARY.get(core);
        boolean greeting = "こんにちは".equals(core) || "こんばんは".equals(core);
        int coreStart = input.indexOf(core);

        for (int i = 0; i < input.length();) {
            int cp = input.codePointAt(i);
            String current = new String(Character.toChars(cp));
            int currentLength = current.length();

            if (isSmallTsu(current)) {
                int nextIndex = i + currentLength;
                String nextRomaji = romajiAt(input, nextIndex);
                String doubled = doubledConsonant(nextRomaji);
                output.append(doubled.isEmpty() ? current : doubled);
                tokens.add(makeToken(current, doubled.isEmpty() ? current : doubled,
                    doubled.isEmpty() ? "っ pequeno sem consoante seguinte: mantido; a leitura depende do contexto."
                        : "っ pequeno (sokuon): faz uma breve pausa antes da consoante seguinte. Aqui acrescenta “" + doubled + "” antes de “" + nextRomaji + "”. Exemplo: っしゃ → ssha, como em irasshaimase. Não se lê tsu.",
                    doubled.isEmpty() ? "Small tsu without a following consonant: preserved."
                        : "Small tsu (sokuon) adds a short closure before the next consonant. Here it adds “" + doubled + "”. It is not pronounced tsu."));
                previousKana = "";
                i = nextIndex;
                continue;
            }

            if ("ー".equals(current)) {
                String vowel = previousKana.isEmpty() ? "" : finalVowel(output);
                String displayedVowel = vowel;
                if (selectedStyle == RomanizationStyle.MACRON && replaceFinalVowelWithMacron(output)) {
                    displayedVowel = macronFor(vowel);
                } else {
                    output.append(vowel.isEmpty() ? current : vowel);
                }
                tokens.add(makeToken(current, displayedVowel, "Prolonga a vogal anterior", "Lengthens the previous vowel"));
                previousKana = "";
                i += currentLength;
                continue;
            }

            if (isIterationMark(current)) {
                boolean voiced = "ゞ".equals(current) || "ヾ".equals(current);
                String repeatedKana = repeatKana(previousKana, voiced);
                String repeated = repeatedKana.isEmpty() ? current : KanaData.readingFor(repeatedKana);
                if (repeated == null) repeated = current;
                output.append(repeated);
                tokens.add(makeToken(current, repeated,
                    voiced ? "Repete o kana anterior com voz" : "Repete o kana anterior",
                    voiced ? "Repeats the previous kana with voicing" : "Repeats the previous kana"));
                previousKana = repeatedKana;
                i += currentLength;
                continue;
            }

            String pair = null;
            int pairLength = currentLength;
            if (i + currentLength < input.length()) {
                int nextCp = input.codePointAt(i + currentLength);
                String next = new String(Character.toChars(nextCp));
                String candidate = current + next;
                if (KanaData.comboReading(candidate) != null) {
                    pair = candidate;
                    pairLength += next.length();
                }
            }

            String unit = pair != null ? pair : current;
            String romaji = pair != null ? KanaData.comboReading(pair)
                : isKana(current) ? KanaData.readingFor(current) : null;
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

            boolean greetingHa = greeting && i == coreStart + core.length() - 1 && "は".equals(current);
            if (greetingHa) romaji = "wa";
            output.append(romaji);
            tokens.add(makeToken(unit, romaji,
                greetingHa ? "は costuma ler-se ha. Nesta saudação lê-se wa." : null,
                greetingHa ? "は is usually ha; in this greeting it is pronounced wa." : null));
            previousKana = isKana(current) && !romaji.equals(current) ? unit : "";
            i += pairLength;
        }

        DictionaryEntry word = known;
        String romaji = output.toString();
        if (word != null && !word.reading.isEmpty()) {
            romaji = input.substring(0, coreStart) + word.reading + input.substring(coreStart + core.length());
            tokens.clear();
            tokens.add(new Token(core, word.reading, "Palavra conhecida", "Known word",
                "Leitura desta palavra no dicionário. Os kanji podem ter outras leituras noutras palavras.",
                "Dictionary reading of this word; kanji can have other readings in other words."));
        }
        if (selectedStyle == RomanizationStyle.MACRON && word != null) {
            String formatted = knownMacron(core);
            if (formatted != null) romaji = input.substring(0, coreStart) + formatted + input.substring(coreStart + core.length());
        }
        String pt = word == null ? "" : word.translationPt;
        String en = word == null ? "" : word.translationEn;
        return new Result(source == null ? "" : source, romaji, tokens, pt, en);
    }

    public static String toKatakana(String source) {
        return KanaData.toKatakana(source);
    }

    public static String toHiragana(String source) {
        return KanaData.toHiragana(source);
    }

    public static List<DictionaryEntry> dictionaryEntries() {
        return Collections.unmodifiableList(DICTIONARY_ENTRIES);
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
            String combo = KanaData.comboReading(pair);
            if (combo != null) return combo;
        }
        String value = KanaData.readingFor(first);
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

    private static boolean replaceFinalVowelWithMacron(StringBuilder output) {
        if (output.length() == 0) return false;
        int index = output.length() - 1;
        char vowel = Character.toLowerCase(output.charAt(index));
        String macron = macronFor(String.valueOf(vowel));
        if (macron.equals(String.valueOf(vowel))) return false;
        output.replace(index, index + 1, macron);
        return true;
    }

    private static String macronFor(String vowel) {
        if ("a".equals(vowel)) return "ā";
        if ("i".equals(vowel)) return "ī";
        if ("u".equals(vowel)) return "ū";
        if ("e".equals(vowel)) return "ē";
        if ("o".equals(vowel)) return "ō";
        return vowel;
    }

    private static String knownMacron(String word) {
        String[][] entries = {{"がっこう", "gakkō"}, {"学校", "gakkō"}, {"すうがく", "sūgaku"},
            {"とうきょう", "tōkyō"}, {"ありがとう", "arigatō"}, {"おはよう", "ohayō"},
            {"さようなら", "sayōnara"}, {"きょう", "kyō"}, {"だいじょうぶ", "daijōbu"}};
        for (String[] entry : entries) if (entry[0].equals(word)) return entry[1];
        return null;
    }

    private static String repeatKana(String previous, boolean voiced) {
        if (previous.isEmpty()) return "";
        String base = Normalizer.normalize(previous, Normalizer.Form.NFD).replace("\u3099", "").replace("\u309A", "");
        String transformed = Normalizer.normalize(base + (voiced ? "\u3099" : ""), Normalizer.Form.NFC);
        return KanaData.readingFor(transformed) == null ? "" : transformed;
    }

    public static String normalizeKana(String source) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("[\\uFF61-\\uFF9F]+").matcher(source == null ? "" : source);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) matcher.appendReplacement(result,
            java.util.regex.Matcher.quoteReplacement(Normalizer.normalize(matcher.group(), Normalizer.Form.NFKC)));
        matcher.appendTail(result);
        return Normalizer.normalize(result, Normalizer.Form.NFC);
    }

    public static boolean containsJapanese(String value) {
        for (int i = 0; i < value.length();) {
            int cp = value.codePointAt(i);
            // Explicit ranges keep detection available on Android 6 / API 23.
            if (isHiragana(cp) || isKatakana(cp) || (cp >= 0x3400 && cp <= 0x9FFF)
                || (cp >= 0xF900 && cp <= 0xFAFF) || (cp >= 0x20000 && cp <= 0x323AF)) return true;
            i += Character.charCount(cp);
        }
        return false;
    }

    public static String searchKey(String value) {
        String key = Normalizer.normalize(value == null ? "" : value.trim(), Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
        key = key.replace("ō", "oo").replace("ū", "uu").replace("ā", "aa").replace("ī", "ii").replace("ē", "ee");
        key = Normalizer.normalize(key, Normalizer.Form.NFD).replaceAll("[\\u0300-\\u036f]", "");
        return KanaData.toHiragana(Normalizer.normalize(key, Normalizer.Form.NFC)).replace("ou", "oo");
    }

    public static List<DictionaryEntry> searchDictionary(String query) {
        List<DictionaryEntry> matches = new ArrayList<>();
        String key = searchKey(query);
        for (DictionaryEntry entry : DICTIONARY_ENTRIES) {
            String reading = entry.reading.isEmpty() ? transliterate(entry.word).romaji : entry.reading;
            if (searchKey(entry.word + " " + reading + " " + entry.translationPt + " " + entry.translationEn).contains(key)) matches.add(entry);
        }
        return matches;
    }

    private static boolean isSmallTsu(String s) { return "っ".equals(s) || "ッ".equals(s); }
    private static boolean isIterationMark(String s) { return "ゝ".equals(s) || "ゞ".equals(s) || "ヽ".equals(s) || "ヾ".equals(s); }
    private static boolean isKana(String s) { return s != null && !s.isEmpty() && (isHiragana(s.codePointAt(0)) || isKatakana(s.codePointAt(0))); }
    private static boolean isHiragana(int cp) { return KanaData.isHiragana(cp); }
    private static boolean isKatakana(int cp) { return KanaData.isKatakana(cp); }

    private static void addWord(String kana, String pt, String en) {
        addDictionaryEntry(new DictionaryEntry(kana, "", pt, en));
    }

    private static void addKanjiWord(String kanji, String reading, String pt, String en) {
        addDictionaryEntry(new DictionaryEntry(kanji, reading, pt, en));
    }

    private static void addDictionaryEntry(DictionaryEntry entry) {
        DICTIONARY.put(entry.word, entry);
        DICTIONARY_ENTRIES.add(entry);
    }
}
