package com.kanabridge;

public final class KanaTransliteratorTest {
    private static int tests = 0;

    public static void main(String[] args) {
        expect("あ", "a");
        expect("こんにちは", "konnichiha");
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
        search("shi", "し", "シ");
        search("きゃ", "きゃ", "キャ");
        search("va", "ゔぁ", "ヴァ");
        category(KanaData.DAKUTEN, 25);
        category(KanaData.YOON, 30);
        category(KanaData.AINU, 16);
        System.out.println("OK: " + tests + " testes");
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
