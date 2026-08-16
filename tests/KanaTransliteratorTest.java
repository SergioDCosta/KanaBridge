package com.kanabridge;

public final class KanaTransliteratorTest {
    private static int tests = 0;

    public static void main(String[] args) {
        expect("あ", "a");
        expect("こんにちは", "konnichiha");
        expect("がっこう", "gakkou");
        expect("きゃく", "kyaku");
        expect("キャット", "kyatto");
        expect("スーパー", "suupaa");
        expect("まっちゃ", "matcha");
        expect("しんよう", "shin'you");
        expect("ヴァイオリン", "vaiorin");
        expect("ティッシュ", "tisshu");
        expect("ﾊﾟｰﾃｨｰ", "paatii");
        expect("ㇰ", "ku");
        expect("日本語 あ", "日本語 a");
        expect("日本", "nihon");
        expect("学校", "gakkou");
        meaning("ねこ", "gato", "cat");
        meaning("コンピューター", "computador", "computer");
        meaning("日本語", "língua japonesa", "Japanese language");
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
}
