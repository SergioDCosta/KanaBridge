package com.kanabridge;

import java.util.ArrayList;
import java.util.List;

/** Small, bundled learning paths. Content is deliberately separate from the screens. */
public final class LearningContent {
    public static final String[][] VOWELS = {{"あ", "a"}, {"い", "i"}, {"う", "u"}, {"え", "e"}, {"お", "o"}};
    public static final String[][] SIMILAR = {{"シ", "shi"}, {"ツ", "tsu"}, {"ソ", "so"}, {"ン", "n"}, {"ぬ", "nu"}, {"め", "me"}};
    public static final String[][] SOKUON = {{"いらっしゃいませ", "irasshaimase"}, {"きって", "kitte"}, {"まっちゃ", "matcha"}, {"ざっし", "zasshi"}, {"きっぷ", "kippu"}};
    public static final String[][] COLLECTIONS = {
        {"À mesa", "Pequenas descobertas num café ou restaurante.", "コーヒー", "おちゃ", "みず", "ごはん", "まっちゃ", "おいしい"},
        {"Em viagem", "Palavras para reconhecer à tua volta.", "ホテル", "レストラン", "でんしゃ", "きっぷ", "すみません", "ありがとう"},
        {"Saudações", "A expressão muda com a situação.", "こんにちは", "こんばんは", "おはよう", "いらっしゃいませ", "おやすみ"},
        {"Cultura pop", "Palavras familiares, com atenção ao contexto.", "アニメ", "マンガ", "かわいい", "すき", "だいすき", "ともだち"}
    };
    public static final String[][] CURIOSITIES = {
        {"Três sistemas, uma língua", "Hiragana aparece em palavras e elementos gramaticais. Katakana aparece frequentemente em empréstimos e destaques. Kanji liga escrita e significado. Podem coexistir na mesma frase.", "にほんご"},
        {"O pequeno っ faz diferença", "Em いらっしゃいませ, っ prepara a consoante de しゃ. Em rōmaji escrevemos ssha: irasshaimase. É uma breve pausa, não um tsu extra.", "いらっしゃいませ"},
        {"Uma vogal que dura mais", "O sinal ー prolonga a vogal anterior em palavras como コーヒー. Na leitura simples aparece koohii; com macrons, kōhī.", "コーヒー"},
        {"Uma saudação de atendimento", "いらっしゃいませ é uma saudação usada por quem recebe clientes em lojas e restaurantes. Não é a saudação habitual do cliente ao entrar.", "いらっしゃいませ"},
        {"Anime e conversa quotidiana", "O registo e a relação entre pessoas importam. Uma fala de uma personagem pode ser teatral, rude ou muito informal. Aprende a situação juntamente com a palavra.", "アニメ"}
    };
    public static List<String[]> group(String name) {
        List<String[]> result = new ArrayList<>();
        if ("Vogais".equals(name)) add(result, VOWELS);
        else if ("Parecidos".equals(name)) add(result, SIMILAR);
        else if ("Pequeno っ".equals(name)) add(result, SOKUON);
        else {
            boolean katakana = "Katakana".equals(name);
            for (KanaEntry entry : KanaData.entries(KanaData.GOJUON))
                result.add(new String[]{entry.symbol(katakana), entry.romaji});
        }
        return result;
    }
    private static void add(List<String[]> target, String[][] values) {
        for (String[] value : values) target.add(value.clone());
    }
    public static String context(String word) {
        if ("いらっしゃいませ".equals(word)) return "Usado por quem recebe clientes numa loja ou restaurante. O pequeno っ antes de しゃ dá ssha, como em irasshaimase.";
        if ("こんにちは".equals(word) || "こんばんは".equals(word)) return "Nesta saudação, は lê-se wa. A leitura literal ha não representa a pronúncia da expressão.";
        if ("かわいい".equals(word)) return "Descreve algo ou alguém como fofo ou querido. O contexto e a relação entre pessoas importam.";
        if ("すき".equals(word) || "だいすき".equals(word)) return "Expressa gosto ou preferência; a tradução natural depende da frase e do contexto.";
        if ("おやすみ".equals(word)) return "Forma informal usada ao despedir-se para dormir; a forma mais polida é おやすみなさい.";
        if ("おはよう".equals(word)) return "Bom dia, numa forma informal. おはようございます é uma forma mais polida.";
        if ("あなた".equals(word)) return "O uso de “tu/você” não é igual ao português. Em japonês, o nome, o título ou a omissão podem ser mais naturais.";
        return "Explora a leitura e guarda esta palavra para voltar a encontrá-la na prática.";
    }
    private LearningContent() {}
}
