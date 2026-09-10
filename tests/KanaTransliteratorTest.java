package com.kanabridge;

public final class KanaTransliteratorTest {
    private static int tests = 0;

    public static void main(String[] args) {
        expect("あ", "a");
        expect("こんにちは", "konnichiwa");
        expect("こんばんは", "konbanwa");
        expect("こんにちは。", "konnichiwa。");
        expect("  学校! ", "  gakkou! ");
        expect("いらっしゃいませ", "irasshaimase");
        expect("イラッシャイマセ", "irasshaimase");
        expect("いらっしゃいませ！", "irasshaimase！");
        expect("ざっし", "zasshi");
        expect("きって", "kitte");
        expect("きっぷ", "kippu");
        expect("しゞ", "shiji");
        expect("ちゞ", "chiji");
        expect("ふゞ", "fubu");
        expect("がゝ", "gaka");
        expect("か ゝ", "ka ゝ");
        expect("っ", "っ");
        expect("っあ", "っa");
        expect("ー", "ー");
        expect("か。ー", "ka。ー");
        expect("ABC １２３。", "ABC １２３。");
        expect("😀かな", "😀kana");
        expect("が", "ga");
        expect("ぱ", "pa");
        expect("がっこう", "gakkou");
        expect("きゃく", "kyaku");
        expect("キャット", "kyatto");
        expect("スーパー", "suupaa");
        expect("まっちゃ", "matcha");
        expect("しんよう", "shin'you");
        expect("ヴァイオリン", "vaiorin");
        expect("ティッシュ", "tisshu");
        expect("かった", "katta");
        expect("ﾊﾟｰﾃｨｰ", "paatii");
        expect("ㇰ", "ku");
        expect("ABC 123!", "ABC 123!");
        expect("日本語 あ", "日本語 a");
        expect("日本", "nihon");
        expect("学校", "gakkou");
        meaning("ねこ", "gato", "cat");
        meaning("コンピューター", "computador", "computer");
        meaning("日本語", "língua japonesa", "Japanese language");
        convertToKatakana("こんにちは", "コンニチハ");
        convertToKatakana("かな ABC 山", "カナ ABC 山");
        convertToKatakana("ゝゞ", "ヽヾ");
        convertToHiragana("カタカナ", "かたかな");
        convertToHiragana("ヴァ ABC 山", "ゔぁ ABC 山");
        convertToHiragana("ヽヾ", "ゝゞ");
        macron("がっこう", "gakkō");
        macron("スーパー", "sūpā");
        macron("コーヒー", "kōhī");
        macron("すうがく", "sūgaku");
        macron("とうきょう", "tōkyō");
        macron("おもう", "omou");
        macron("かわいい", "kawaii");
        macron("学校。", "gakkō。");
        search("shi", "し", "シ");
        search("きゃ", "きゃ", "キャ");
        search("va", "ゔぁ", "ヴァ");
        search("ゐ", "ゐ", "ヰ");
        search("ヶ", "ゖ", "ヶ");
        dictionarySearch("cafe", "コーヒー");
        dictionarySearch("gakkō", "がっこう");
        dictionarySearch("ｺｰﾋｰ", "コーヒー");
        dictionarySearch("irasshaimase", "いらっしゃいませ");
        check(KanaTransliterator.transliterate("日本語 あ").partial, "mixed text is partial");
        check(!KanaTransliterator.transliterate("日本語").partial, "known word is complete");
        check(KanaTransliterator.transliterate("っ").partial, "isolated sokuon preserved");
        KanaTransliterator.Result welcome = KanaTransliterator.transliterate("いらっしゃいませ");
        boolean sokuon = false;
        for (KanaTransliterator.Token token : welcome.tokens) {
            if (token.kana.equals("っ")) sokuon = token.romaji.equals("s") && token.notePt.contains("ssha");
        }
        check(sokuon, "sokuon token explains the s in irasshaimase");
        check(welcome.translationPt.contains("atendimento"), "welcome context");
        category(KanaData.DAKUTEN, 25);
        category(KanaData.YOON, 30);
        category(KanaData.AINU, 16);
        System.out.println("OK: " + tests + " testes");
    }

    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
        tests++;
    }
    private static void dictionarySearch(String query, String word) {
        boolean found = false;
        for (KanaTransliterator.DictionaryEntry entry : KanaTransliterator.searchDictionary(query))
            if (entry.word.equals(word)) found = true;
        check(found, "dictionary search: " + query);
    }

    private static void expect(String input, String expected) {
        String actual = KanaTransliterator.transliterate(input).romaji;
        if (!expected.equals(actual)) throw new AssertionError(input + ": expected " + expected + ", got " + actual);
        tests++;
    }

    private static void meaning(String input, String pt, String en) {
        KanaTransliterator.Result r = KanaTransliterator.transliterate(input);
        if (!pt.equals(r.translationPt) || !en.equals(r.translationEn)) {
            throw new AssertionError(input + ": dictionary mismatch");
        }
        tests++;
    }

    private static void convertToKatakana(String input, String expected) {
        String actual = KanaTransliterator.toKatakana(input);
        if (!expected.equals(actual)) throw new AssertionError("toKatakana: " + actual);
        tests++;
    }

    private static void convertToHiragana(String input, String expected) {
        String actual = KanaTransliterator.toHiragana(input);
        if (!expected.equals(actual)) throw new AssertionError("toHiragana: " + actual);
        tests++;
    }

    private static void macron(String input, String expected) {
        String actual = KanaTransliterator.transliterate(
            input, KanaTransliterator.RomanizationStyle.MACRON).romaji;
        if (!expected.equals(actual)) throw new AssertionError("macron: expected " + expected + ", got " + actual);
        tests++;
    }

    private static void search(String query, String hiragana, String katakana) {
        boolean foundHiragana = false;
        boolean foundKatakana = false;
        for (KanaEntry entry : KanaData.search(query)) {
            if (hiragana.equals(entry.hiragana)) foundHiragana = true;
            if (katakana.equals(entry.katakana)) foundKatakana = true;
        }
        if (!foundHiragana || !foundKatakana) {
            throw new AssertionError("search failed for " + query);
        }
        tests++;
    }

    private static void category(String name, int minimumSize) {
        int actual = KanaData.entries(name).size();
        if (actual < minimumSize) {
            throw new AssertionError(name + ": expected at least " + minimumSize + ", got " + actual);
        }
        tests++;
    }
}
