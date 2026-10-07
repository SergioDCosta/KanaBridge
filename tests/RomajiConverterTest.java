package com.kanabridge;

public final class RomajiConverterTest {
    private static int assertions;
    private static void check(boolean value, String label) { if (!value) throw new AssertionError(label); assertions++; }
    private static void word(String source, String expected) {
        RomajiConverter.Result result = RomajiConverter.convert(source);
        check(expected.equals(result.hiragana), source + ": expected " + expected + " got " + result.hiragana);
        check(!result.partial, source + " should be complete");
        check(KanaData.toKatakana(expected).equals(result.katakana), source + " katakana");
    }
    public static void main(String[] args) {
        word("irasshaimase", "いらっしゃいませ"); word("IRASSHAIMASE", "いらっしゃいませ");
        word("gakkou", "がっこう"); word("gakkō", "がっこう"); word("matcha", "まっちゃ"); word("maccha", "まっちゃ");
        word("issho", "いっしょ"); word("kitte", "きって"); word("zasshi", "ざっし"); word("tsuppari", "つっぱり");
        word("kan'i", "かんい"); word("shin’you", "しんよう"); word("konnichiwa", "こんにちは");
        word("konbanwa", "こんばんは"); word("wa", "わ");
        word("konnichiha", "こんにちは"); word("annai", "あんない"); word("nnya", "んにゃ");
        word("n", "ん"); word("nn", "ん"); word("ten", "てん"); word("tenn", "てん");
        word("nya nyu nyo", "にゃ にゅ にょ"); word("ga gi gu ge go", "が ぎ ぐ げ ご");
        word("za ji zu ze zo", "ざ じ ず ぜ ぞ"); word("da di du de do", "だ ぢ づ で ど");
        word("pa pi pu pe po", "ぱ ぴ ぷ ぺ ぽ"); word("kya sha cha ja", "きゃ しゃ ちゃ じゃ");
        word("si ti tu hu zi", "し ち つ ふ じ"); word("sya tyu cyo jye", "しゃ ちゅ ちょ じぇ");
        word("xa xi xu xe xo", "ぁ ぃ ぅ ぇ ぉ"); word("xya xyu xyo xtsu", "ゃ ゅ ょ っ");
        word("la li lu le lo ltsu", "ぁ ぃ ぅ ぇ ぉ っ"); word("fa fi fe fo va vi vu ve vo", "ふぁ ふぃ ふぇ ふぉ ゔぁ ゔぃ ゔ ゔぇ ゔぉ");
        word("ko-hi-", "こーひー"); word("ﾊﾟﾝ", "ぱん"); word("か\u3099", "が");
        word("ｇａｋｋｏｕ", "がっこう"); word("nihongo! 123 😊", "にほんご! 123 😊");
        word("nihon\nnihongo", "にほん\nにほんご"); word("日本語", "日本語"); word("", ""); word(null, "");
        check(RomajiConverter.convert("q").partial, "unknown Latin disclosed");
        check(RomajiConverter.convert("q").hiragana.equals("q"), "unknown preserved");
        check(KanaTransliterator.transliterate(RomajiConverter.convert("irasshaimase").hiragana).romaji.equals("irasshaimase"), "sokuon round trip");
        check(JapaneseReference.all().size() >= 280, "substantial reference");
        check(JapaneseReference.search("Verbos", "").size() >= 100, "100 verbs");
        check(JapaneseReference.search("Termos", "").size() >= 120, "120 terms");
        check(JapaneseReference.search("Frases", "").size() >= 25, "25 patterns");
        check(!JapaneseReference.search("Frases", "porque").isEmpty(), "Portuguese accent tolerant search");
        check(!JapaneseReference.search("Verbos", "taberu").isEmpty(), "romaji reference search");
        check(!JapaneseReference.search("Termos", "駅").isEmpty(), "kanji reference search");
        check(JapaneseReference.search("Frases", "Ordem da frase").get(0).romaji().contains("watashi wa"), "spoken topic particle in reference");
        check(JapaneseReference.search("Frases", "Destino").get(0).romaji().equals("nihon e ikimasu."), "spoken directional particle");
        check(JapaneseReference.search("Frases", "O que vais comer").get(0).romaji().contains("nani o tabemasu"), "spoken object particle");
        check(KanaData.search("si").stream().anyMatch(e -> e.hiragana.equals("し")), "table accepts si alias");
        check(KanaData.search("xtsu").stream().anyMatch(e -> e.hiragana.equals("っ")), "table finds small kana by IME alias");
        for (JapaneseReference.Entry entry : JapaneseReference.all()) {
            check(!entry.meaning.isEmpty() && !entry.reading.isEmpty() && !entry.japanese.isEmpty(), "complete reference entry");
            check(!KanaTransliterator.transliterate(entry.reading).partial, "readable kana: " + entry.reading);
        }
        System.out.println("OK: " + assertions + " reverse conversion and reference assertions");
        System.out.println("Reference: " + JapaneseReference.all().size() + " entries; " + JapaneseReference.search("Verbos", "").size() + " verbs; " + JapaneseReference.search("Termos", "").size() + " terms; " + JapaneseReference.search("Frases", "").size() + " patterns; " + JapaneseReference.search("Expressões", "").size() + " expressions");
    }
}
