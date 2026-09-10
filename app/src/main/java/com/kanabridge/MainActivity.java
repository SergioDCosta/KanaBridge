package com.kanabridge;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public final class MainActivity extends Activity {
    private Ui ui;
    private SharedPreferences prefs;
    private LocalProgress progress;
    private JapaneseSpeech speech;
    private LinearLayout shell, page, navigation;
    private ScrollView scroll;
    private String section = "discover", converter = "", tableQuery = "", dictionaryQuery = "";
    private String category = KanaData.GOJUON, group = "Vogais";
    private boolean katakana, macrons, english, hideRomaji;
    private int collection;
    private long quizSeed;
    private StudySession quiz;
    private ArrayList<String> reviewWords = new ArrayList<>();
    private ArrayList<String> quizWords = new ArrayList<>();
    private ArrayList<String> quizReadings = new ArrayList<>();
    private ArrayList<String> history = new ArrayList<>();
    private boolean dark;
    private android.window.OnBackInvokedCallback backCallback;
    private boolean backRegistered;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pending;

    @Override public void onCreate(Bundle saved) {
        prefs = getSharedPreferences("kanabridge", MODE_PRIVATE);
        String theme = prefs.getString("theme", "system");
        dark = "dark".equals(theme) || ("system".equals(theme)
            && (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES);
        setTheme(dark ? android.R.style.Theme_Material_NoActionBar : android.R.style.Theme_Material_Light_NoActionBar);
        super.onCreate(saved);
        ui = new Ui(this, dark);
        progress = new LocalProgress(prefs);
        speech = new JapaneseSpeech(this);
        english = prefs.getBoolean("english", false);
        macrons = prefs.getBoolean("macrons", false);
        hideRomaji = prefs.getBoolean("hideRomaji", false);
        converter = prefs.getString("draft", "");
        section = prefs.getString("section", "discover");
        if ("session".equals(section)) section = "practice";
        if (saved != null) restore(saved);
        configureWindow();
        buildShell();
        render();
        if (saved != null) scroll.post(() -> scroll.scrollTo(0, saved.getInt("scroll")));
    }

    private void configureWindow() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(ui.background);
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
        } else {
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            if (!dark) flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (!dark && android.os.Build.VERSION.SDK_INT >= 26) flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
        if (android.os.Build.VERSION.SDK_INT >= 33) backCallback = () -> back();
    }

    private void buildShell() {
        shell = ui.column(); shell.setBackgroundColor(ui.background);
        shell.setOnApplyWindowInsetsListener((view, insets) -> {
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                android.graphics.Insets keyboard = insets.getInsets(WindowInsets.Type.ime());
                shell.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, keyboard.bottom));
                navigation.setVisibility(insets.isVisible(WindowInsets.Type.ime()) ? View.GONE : View.VISIBLE);
            } else {
                shell.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            }
            return insets;
        });
        scroll = new ScrollView(this); scroll.setFillViewport(true);
        page = ui.column(); page.setPadding(ui.dp(20), ui.dp(12), ui.dp(20), ui.dp(24));
        page.setFocusableInTouchMode(true);
        scroll.addView(page, new ScrollView.LayoutParams(-1, -2));
        shell.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        navigation = ui.column(); navigation.setPadding(ui.dp(8), ui.dp(6), ui.dp(8), ui.dp(6));
        navigation.setBackgroundColor(ui.surface);
        shell.addView(navigation, ui.wrap());
        setContentView(shell); shell.requestApplyInsets();
        shell.post(() -> {
            if (android.os.Build.VERSION.SDK_INT >= 30 && getWindow().getInsetsController() != null) {
                int mask = android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
                getWindow().getInsetsController().setSystemBarsAppearance(dark ? 0 : mask, mask);
            }
        });
    }

    private void navigate(String target) {
        if (!section.equals(target)) {
            history.add(section);
            if (history.size() > 30) history.remove(0);
        }
        section = target;
        ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(shell.getWindowToken(), 0);
        render(); scroll.scrollTo(0, 0);
    }

    private void render() {
        if (pending != null) handler.removeCallbacks(pending);
        page.removeAllViews();
        LinearLayout brand = ui.row();
        TextView title = ui.text("あ  KanaBridge", 19, ui.accent, true);
        brand.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        Button settings = ui.button("Opções", false, () -> navigate("settings"));
        brand.addView(settings, new LinearLayout.LayoutParams(-2, -2));
        page.addView(brand, ui.wrap());
        if (!history.isEmpty()) ui.add(page, ui.button("← Voltar", false, () -> back()));
        switch (section) {
            case "kana": showKana(); break;
            case "practice": showPractice(); break;
            case "tools": showTools(); break;
            case "converter": showConverter(); break;
            case "dictionary": showDictionary(); break;
            case "lesson": showLesson(); break;
            case "collection": showCollection(); break;
            case "session": showSession(); break;
            case "favorites": showFavorites(); break;
            case "settings": showSettings(); break;
            default: section = "discover"; showDiscover();
        }
        renderNavigation();
        page.requestFocus();
        updateBack();
    }

    private void renderNavigation() {
        navigation.removeAllViews();
        String[] labels = {"Descobrir", "Kana", "Praticar", "Ferramentas"};
        String[] targets = {"discover", "kana", "practice", "tools"};
        int cols = getResources().getConfiguration().fontScale > 1.3f || getResources().getConfiguration().screenWidthDp < 360 ? 2 : 4;
        String active = section;
        if ("lesson".equals(section) || "collection".equals(section)) active = "discover";
        if ("session".equals(section) || "favorites".equals(section)) active = "practice";
        if ("converter".equals(section) || "dictionary".equals(section) || "settings".equals(section)) active = "tools";
        LinearLayout row = null;
        for (int i = 0; i < targets.length; i++) {
            if (i % cols == 0) { row = ui.row(); navigation.addView(row, ui.wrap()); }
            final String target = targets[i];
            Button button = ui.button(labels[i], active.equals(target), () -> navigate(target));
            ui.paint(button, active.equals(target));
            button.setTextSize(12);
            button.setPadding(ui.dp(3), ui.dp(10), ui.dp(3), ui.dp(10));
            button.setId(110 + i);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1);
            lp.setMargins(ui.dp(2), ui.dp(2), ui.dp(2), ui.dp(2)); row.addView(button, lp);
        }
    }

    private void heading(String title, String subtitle) {
        ui.add(page, ui.heading(title));
        ui.add(page, ui.text(subtitle, 15, ui.muted, false));
    }
    private LinearLayout card(String title, String body) {
        LinearLayout card = ui.card();
        card.addView(ui.text(title, 20, ui.ink, true));
        if (!body.isEmpty()) ui.add(card, ui.text(body, 15, ui.ink, false));
        ui.add(page, card); return card;
    }
    private void action(LinearLayout parent, String label, boolean primary, Runnable runnable) {
        ui.add(parent, ui.button(label, primary, runnable));
    }

    private void showDiscover() {
        heading("Um pequeno passo\npara ler japonês.", "Explora ao teu ritmo. As tuas descobertas ficam neste telemóvel.");
        LinearLayout start = card("Começa com cinco sons", "あ · い · う · え · お\nConhece as vogais, ouve e experimenta um primeiro desafio.");
        action(start, "Começar pelas vogais", true, () -> navigate("lesson"));
        action(page, "Ler um texto japonês", false, () -> navigate("converter"));
        if (quiz != null && !quiz.finished()) action(page, "Continuar a prática", false, () -> navigate("session"));
        int due = progress.due().size();
        if (due > 0) action(page, "Rever " + due + " itens disponíveis", false, () -> startQuiz("Revisão"));
        int day = (int)java.util.concurrent.TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis());
        int count = LearningContent.CURIOSITIES.length;
        String[] curiosity = LearningContent.CURIOSITIES[((day % count) + count) % count];
        LinearLayout fact = card(curiosity[0], curiosity[1]);
        action(fact, "Explorar “" + curiosity[2] + "”", false, () -> showWord(curiosity[2]));
        ui.add(page, ui.text("Descobre por interesse", 22, ui.ink, true));
        for (int i = 0; i < LearningContent.COLLECTIONS.length; i++) {
            final int index = i;
            String[] item = LearningContent.COLLECTIONS[i];
            action(page, item[0] + "\n" + item[1], false, () -> { collection = index; navigate("collection"); });
        }
    }

    private void showLesson() {
        heading("As tuas primeiras vogais", "Kana representa sons. Hiragana e katakana têm formas diferentes para os mesmos sons básicos.");
        card("Três sistemas de escrita", "Hiragana: あ · Katakana: ア · Kanji: 山\nO kanji pode representar significado e ter várias leituras. Começamos pelos sons do hiragana.");
        for (String[] vowel : LearningContent.VOWELS) {
            LinearLayout item = card(vowel[0] + "   " + vowel[1], "Katakana correspondente: " + KanaData.toKatakana(vowel[0]));
            action(item, "Ouvir " + vowel[0], false, () -> speech.speak(vowel[0], false));
            favoriteButton(item, vowel[0]);
        }
        card("Já consegues juntar sons", "あ + い → あい (ai)\nUma palavra que pode significar amor.");
        action(page, "Experimentar cinco perguntas", true, () -> startQuiz("Vogais"));
    }

    private void showCollection() {
        String[] items = LearningContent.COLLECTIONS[Math.max(0, Math.min(collection, LearningContent.COLLECTIONS.length - 1))];
        heading(items[0], items[1]);
        for (int i = 2; i < items.length; i++) {
            String word = items[i];
            KanaTransliterator.Result value = KanaTransliterator.transliterate(word);
            LinearLayout item = card(word + " · " + value.romaji, value.translationPt);
            action(item, "Explorar palavra", false, () -> showWord(word));
        }
        action(page, "Praticar esta coleção", true, () -> startQuiz("Coleção"));
    }

    private void showTools() {
        heading("Ferramentas", "Consulta uma leitura ou descobre o significado de uma palavra.");
        action(page, "Conversor\nKana → rōmaji e troca de silabário", true, () -> navigate("converter"));
        action(page, "Dicionário\nPesquisa por japonês, leitura ou significado", false, () -> navigate("dictionary"));
        action(page, "Palavras guardadas", false, () -> navigate("favorites"));
    }

    private EditText field(LinearLayout parent, String label, String value, boolean multiline, int id) {
        TextView caption = ui.text(label, 13, ui.muted, true); caption.setLabelFor(id); ui.add(parent, caption);
        EditText input = new EditText(this); input.setId(id);
        input.setTextSize(multiline ? 23 : 17); input.setTextColor(ui.ink); input.setHintTextColor(ui.muted);
        input.setSingleLine(!multiline); input.setMinHeight(ui.dp(multiline ? 112 : 56));
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | (multiline ? android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE : 0));
        input.setImeOptions(android.view.inputmethod.EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        input.setGravity(Gravity.TOP | Gravity.START);
        input.setPadding(ui.dp(12), ui.dp(12), ui.dp(12), ui.dp(12));
        input.setBackground(ui.shape(ui.surface, ui.line, 14));
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(multiline ? 4000 : 120)});
        input.setText(value); input.setSelection(input.length()); ui.add(parent, input); return input;
    }
    private interface Changed { void change(String value); }
    private void watch(EditText input, Changed changed) {
        input.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { changed.change(s.toString()); }
            public void afterTextChanged(Editable s) {}
        });
    }

    private void showConverter() {
        heading("Lê e compreende", "Cola até 4 000 caracteres. Toca num segmento para perceber como se lê.");
        EditText input = field(page, "TEXTO JAPONÊS", converter, true, 201);
        input.setHint("Ex.: いらっしゃいませ");
        LinearLayout output = ui.column(); ui.add(page, output);
        renderResult(output);
        watch(input, value -> {
            converter = value;
            if (pending != null) handler.removeCallbacks(pending);
            pending = () -> renderResult(output); handler.postDelayed(pending, 140);
        });
        action(page, "Experimentar いらっしゃいませ", false, () -> input.setText("いらっしゃいませ"));
        action(page, "Opções de conversão", false, () -> new AlertDialog.Builder(this)
            .setTitle("Opções de conversão").setItems(new String[]{"Hiragana → Katakana", "Katakana → Hiragana", macrons ? "Usar rōmaji simples" : "Usar macrons", "Limpar texto"}, (d, which) -> {
                if (which == 0) input.setText(KanaTransliterator.toKatakana(converter));
                else if (which == 1) input.setText(KanaTransliterator.toHiragana(converter));
                else if (which == 2) { macrons = !macrons; prefs.edit().putBoolean("macrons", macrons).apply(); renderResult(output); }
                else input.setText("");
            }).setNegativeButton("Fechar", null).show());
    }

    private void renderResult(LinearLayout output) {
        output.removeAllViews();
        if (converter.trim().isEmpty()) {
            ui.add(output, ui.text("Experimenta o exemplo abaixo. O pequeno っ ajuda a formar a consoante dupla em irasshaimase.", 15, ui.muted, false)); return;
        }
        KanaTransliterator.Result result = KanaTransliterator.transliterate(converter,
            macrons ? KanaTransliterator.RomanizationStyle.MACRON : KanaTransliterator.RomanizationStyle.SIMPLE);
        LinearLayout reading = ui.card();
        reading.addView(ui.text(macrons ? "LEITURA · MACRONS" : "LEITURA · SIMPLES", 12, ui.muted, true));
        TextView romaji = ui.text(result.romaji, 27, ui.ink, true); romaji.setTextIsSelectable(true); ui.add(reading, romaji);
        action(reading, "Copiar leitura", false, () -> copy(result.romaji));
        if (!result.translationPt.isEmpty()) {
            ui.add(reading, ui.text(result.translationPt, 17, ui.ink, false));
            if (english) ui.add(reading, ui.text(result.translationEn, 15, ui.muted, false));
        }
        ui.add(output, reading);
        if (result.partial) ui.add(output, ui.text("Leitura parcial: alguns sinais ou kanji foram preservados. Não foi determinada a sua leitura neste contexto.", 15, ui.accent, false));
        else if (result.translationPt.isEmpty()) ui.add(output, ui.text("Conversão dos kana, sem análise gramatical da frase. Partículas e nomes podem ter leituras contextuais. Não há significado para esta entrada no mini-dicionário.", 14, ui.muted, false));
        if (macrons) ui.add(output, ui.text("Macrons nas palavras com leitura longa registada e em ー. Outras sequências de vogais são preservadas para evitar adivinhar a pronúncia.", 14, ui.muted, false));
        ui.add(output, ui.text("Como se lê", 20, ui.ink, true));
        GridLayout segments = grid();
        int shown = 0;
        for (KanaTransliterator.Token token : result.tokens) {
            if (token.kana.trim().isEmpty()) continue;
            if (shown++ >= 60) break;
            Button button = ui.button(token.kana + "\n" + token.romaji, false, () -> new AlertDialog.Builder(this)
                .setTitle(token.kana + " · " + token.romaji)
                .setMessage(token.notePt + (english ? "\n\n" + token.noteEn : ""))
                .setPositiveButton("Percebi", null).show());
            gridAdd(segments, button);
        }
        ui.add(output, segments);
        if (shown > 60) ui.add(output, ui.text("Mostrados os primeiros 60 segmentos. A leitura acima inclui todo o texto; usa um excerto para explorar os restantes.", 14, ui.muted, false));
    }

    private GridLayout grid() {
        GridLayout grid = new GridLayout(this);
        float scale = getResources().getConfiguration().fontScale;
        int width = getResources().getConfiguration().screenWidthDp - 40;
        grid.setColumnCount(Math.max(1, Math.min(5, width / (scale > 1.3f ? 148 : 96))));
        grid.setUseDefaultMargins(false); return grid;
    }
    private void gridAdd(GridLayout grid, View child) {
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0; params.height = -2;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(ui.dp(3), ui.dp(3), ui.dp(3), ui.dp(3));
        grid.addView(child, params);
    }

    private void showKana() {
        heading("Explora os kana", "Começa pelos sons básicos. Toca para ouvir, ver um exemplo e guardar.");
        EditText search = field(page, "PESQUISAR KANA OU RŌMAJI", tableQuery, false, 202);
        action(page, katakana ? "Silabário: Katakana · mudar" : "Silabário: Hiragana · mudar", false, () -> { katakana = !katakana; render(); });
        String[] categories = {KanaData.GOJUON, KanaData.DAKUTEN, KanaData.YOON, KanaData.SMALL, KanaData.MODERN, KanaData.AINU, KanaData.REFERENCE};
        action(page, "Grupo: " + category, false, () -> new AlertDialog.Builder(this).setTitle("Escolhe um grupo")
            .setItems(categories, (d, which) -> { category = categories[which]; if (which == 4 || which == 5) katakana = true; render(); })
            .setNegativeButton("Fechar", null).show());
        LinearLayout body = ui.column(); ui.add(page, body); renderKana(body);
        watch(search, value -> { tableQuery = value; renderKana(body); });
    }
    private void renderKana(LinearLayout body) {
        body.removeAllViews();
        if (KanaData.AINU.equals(category) && tableQuery.isEmpty()) ui.add(body, ui.text("Ainu é uma língua distinta. Estas extensões são uma referência complementar, não uma etapa do japonês inicial. As etiquetas mostram a relação gráfica com os kana de base, não uma transcrição completa do Ainu.", 15, ui.ink, false));
        if (KanaData.SMALL.equals(category) && tableQuery.isEmpty()) ui.add(body, ui.text("っ / ッ prepara a consoante seguinte. ゃ / ャ, ゅ / ュ e ょ / ョ combinam-se com outros kana: きゃ → kya.", 15, ui.ink, false));
        List<KanaEntry> entries = tableQuery.trim().isEmpty() ? KanaData.entries(category) : KanaData.search(tableQuery);
        ui.add(body, ui.text(entries.size() + " entradas", 14, ui.muted, false));
        if (tableQuery.trim().isEmpty() && KanaData.GOJUON.equals(category)) {
            String[][] rows = KanaData.gojuonGrid(katakana);
            String[] labels = KanaData.gojuonRowLabels();
            for (int index = 0; index < rows.length; index++) {
                ui.add(body, ui.text(index == 0 ? "Vogais · a  i  u  e  o" : "Linha " + labels[index], 14, ui.muted, true));
                GridLayout row = grid();
                if (getResources().getConfiguration().fontScale <= 1.3f && getResources().getConfiguration().screenWidthDp >= 320) row.setColumnCount(5);
                for (String symbol : rows[index]) {
                    if (symbol.isEmpty()) { if (row.getColumnCount() == 5) gridAdd(row, new View(this)); continue; }
                    KanaEntry entry = KanaData.find(symbol);
                    Button cell = ui.button(symbol + (hideRomaji ? "" : "\n" + entry.romaji), false, () -> showKanaDetails(entry, symbol));
                    cell.setTextSize(15); cell.setPadding(ui.dp(2), ui.dp(12), ui.dp(2), ui.dp(12));
                    gridAdd(row, cell);
                }
                ui.add(body, row);
            }
            return;
        }
        GridLayout grid = grid();
        for (KanaEntry entry : entries) {
            String symbol = entry.symbol(katakana);
            String reading = entry.romaji.isEmpty() ? "marca" : entry.romaji;
            gridAdd(grid, ui.button(symbol + (hideRomaji ? "" : "\n" + reading), false, () -> showKanaDetails(entry, symbol)));
        }
        ui.add(body, grid);
        if (entries.isEmpty()) ui.add(body, ui.text("Sem resultados. Experimenta shi, し ou っ.", 15, ui.ink, false));
    }

    private void showKanaDetails(KanaEntry entry, String symbol) {
        LinearLayout body = ui.column(); body.setPadding(ui.dp(20), ui.dp(12), ui.dp(20), ui.dp(16));
        ui.add(body, ui.text(entry.romaji.isEmpty() ? "Marca que modifica a leitura" : "Leitura: " + entry.romaji, 22, ui.ink, true));
        String counterpart = symbol.equals(entry.katakana) ? entry.hiragana : entry.katakana;
        if (!counterpart.isEmpty()) ui.add(body, ui.text("Outro silabário: " + counterpart, 16, ui.ink, false));
        if (!entry.notePt.isEmpty()) ui.add(body, ui.text(entry.notePt, 15, ui.ink, false));
        if (english && !entry.noteEn.isEmpty()) ui.add(body, ui.text(entry.noteEn, 14, ui.muted, false));
        String example = "っ".equals(symbol) || "ッ".equals(symbol) ? "いらっしゃいませ" : exampleFor(symbol);
        if (!example.isEmpty()) action(body, "Exemplo: " + example, false, () -> showWord(example));
        if (!entry.romaji.isEmpty() && !KanaData.AINU.equals(entry.category)) action(body, "Ouvir", false, () -> speech.speak(symbol, false));
        if (!entry.romaji.isEmpty() && !KanaData.AINU.equals(entry.category)) favoriteButton(body, symbol);
        action(body, "Copiar kana", false, () -> copy(symbol));
        dialog(symbol, body);
    }
    private String exampleFor(String symbol) {
        String hira = KanaData.toHiragana(symbol);
        for (KanaTransliterator.DictionaryEntry entry : KanaTransliterator.dictionaryEntries())
            if (KanaData.toHiragana(entry.word).contains(hira)) return entry.word;
        return "";
    }

    private void showDictionary() {
        heading("Palavras e significados", "Pesquisa em japonês, rōmaji ou português. Também podes escrever cafe ou gakkō.");
        EditText search = field(page, "PESQUISAR PALAVRA", dictionaryQuery, false, 203);
        LinearLayout body = ui.column(); ui.add(page, body);
        renderDictionary(body, 30);
        watch(search, value -> { dictionaryQuery = value; renderDictionary(body, 30); });
    }
    private void renderDictionary(LinearLayout body, int limit) {
        body.removeAllViews();
        List<KanaTransliterator.DictionaryEntry> entries = KanaTransliterator.searchDictionary(dictionaryQuery);
        ui.add(body, ui.text(entries.size() + " entradas encontradas", 14, ui.muted, false));
        for (int i = 0; i < Math.min(limit, entries.size()); i++) {
            KanaTransliterator.DictionaryEntry entry = entries.get(i);
            String reading = KanaTransliterator.transliterate(entry.word).romaji;
            action(body, entry.word + " · " + reading + "\n" + entry.translationPt, false, () -> showWord(entry.word));
        }
        if (entries.size() > limit) action(body, "Ver mais palavras", false, () -> renderDictionary(body, limit + 30));
        if (entries.isEmpty()) ui.add(body, ui.text("Ainda não temos essa palavra. Experimenta uma parte da leitura ou explora uma coleção.", 15, ui.ink, false));
    }
    private void showWord(String word) {
        KanaTransliterator.Result parsed = KanaTransliterator.transliterate(word);
        LinearLayout body = ui.column(); body.setPadding(ui.dp(20), ui.dp(12), ui.dp(20), ui.dp(16));
        ui.add(body, ui.text(parsed.romaji, 25, ui.accent, true));
        if (!parsed.translationPt.isEmpty()) ui.add(body, ui.text(parsed.translationPt, 18, ui.ink, false));
        if (english && !parsed.translationEn.isEmpty()) ui.add(body, ui.text(parsed.translationEn, 15, ui.muted, false));
        ui.add(body, ui.text(LearningContent.context(word), 15, ui.ink, false));
        action(body, "Ouvir", false, () -> speech.speak(word, false));
        action(body, "Ouvir devagar", false, () -> speech.speak(word, true));
        favoriteButton(body, word);
        AlertDialog[] handle = new AlertDialog[1];
        action(body, "Perceber a leitura", true, () -> { handle[0].dismiss(); converter = word; navigate("converter"); });
        handle[0] = dialog(word, body);
    }
    private AlertDialog dialog(String title, LinearLayout body) {
        ScrollView scroll = new ScrollView(this); scroll.addView(body);
        return new AlertDialog.Builder(this).setTitle(title).setView(scroll).setNegativeButton("Fechar", null).show();
    }
    private void favoriteButton(LinearLayout body, String word) {
        Button button = ui.button(progress.isFavorite(word) ? "Guardado · remover" : "Guardar para rever", false, () -> {});
        button.setOnClickListener(v -> {
            boolean added = progress.toggle(word); button.setText(added ? "Guardado · remover" : "Guardar para rever");
            button.announceForAccessibility(added ? "Guardado nos favoritos" : "Removido dos favoritos");
        }); ui.add(body, button);
    }
    private void showFavorites() {
        heading("As tuas descobertas", "Guarda palavras e kana para os reveres mais tarde.");
        List<String> words = progress.favorites();
        if (words.isEmpty()) card("O teu caderno começa aqui", "Toca num kana ou numa palavra e escolhe Guardar para rever.");
        else {
            action(page, "Praticar favoritos", true, () -> startQuiz("Favoritos"));
            for (String word : words) {
                LinearLayout item = card(word, KanaTransliterator.transliterate(word).romaji);
                action(item, "Explorar", false, () -> showWord(word));
                action(item, "Remover dos favoritos", false, () -> { progress.toggle(word); render(); });
            }
        }
    }

    private void showPractice() {
        heading("Um pouco de prática", "Sessões curtas, sem pressa. Os erros ajudam a escolher o que rever.");
        ui.add(page, ui.text(progress.answers() + " respostas dadas · " + progress.due().size() + " itens para rever", 14, ui.muted, false));
        if (quiz != null && !quiz.finished()) action(page, "Continuar sessão", true, () -> navigate("session"));
        for (String name : new String[]{"Vogais", "Hiragana", "Katakana", "Pequeno っ", "Parecidos", "Favoritos", "Revisão"})
            action(page, name, "Vogais".equals(name), () -> startQuiz(name));
        card("Antes de comparar シ e ツ", "Observa a orientação dos dois traços curtos e do traço longo. Em シ a composição tende a ser mais horizontal; em ツ, mais vertical. Compara também ソ e ン.");
        action(page, "Abrir palavras guardadas", false, () -> navigate("favorites"));
    }

    private List<String[]> quizPool() {
        List<String[]> items = new ArrayList<>();
        if ("Favoritos".equals(group) || "Revisão".equals(group)) {
            for (String word : reviewWords) items.add(new String[]{word, KanaTransliterator.transliterate(word).romaji});
        } else if ("Coleção".equals(group)) {
            String[] source = LearningContent.COLLECTIONS[collection];
            for (int i = 2; i < source.length; i++) items.add(new String[]{source[i], KanaTransliterator.transliterate(source[i]).romaji});
        } else items = LearningContent.group(group);
        return items;
    }
    private void startQuiz(String name) {
        String previousGroup = group;
        group = name;
        if ("Favoritos".equals(name)) reviewWords = new ArrayList<>(progress.favorites());
        if ("Revisão".equals(name)) reviewWords = new ArrayList<>(progress.due());
        List<String[]> pool = quizPool();
        if (pool.isEmpty()) {
            group = previousGroup;
            Toast.makeText(this, "Favoritos".equals(name) ? "Guarda primeiro um kana ou uma palavra." : "Tudo revisto por agora. Experimenta outro grupo.", Toast.LENGTH_LONG).show(); return;
        }
        quizWords.clear(); quizReadings.clear();
        for (String[] item : pool) { quizWords.add(item[0]); quizReadings.add(item[1]); }
        quizSeed = System.currentTimeMillis(); quiz = new StudySession(pool, quizSeed);
        navigate("session");
    }
    private void showSession() {
        if (quiz == null) { section = "practice"; showPractice(); return; }
        if (quiz.finished()) {
            heading("Sessão concluída", "Fizeste mais uma descoberta.");
            card(quiz.score + " de " + quiz.questions.size() + " respostas certas", "Os itens praticados ficam agendados para revisão local. Os erros voltam mais cedo; podes repetir o grupo agora.");
            action(page, "Repetir grupo", true, () -> startQuiz(group));
            action(page, "Escolher outra prática", false, () -> navigate("practice")); return;
        }
        heading(group, "Pergunta " + (quiz.index + 1) + " de " + quiz.questions.size());
        String[] question = quiz.current();
        TextView glyph = ui.text(question[0], question[0].length() > 4 ? 32 : 64, ui.ink, true);
        glyph.setGravity(Gravity.CENTER); ui.add(page, glyph);
        ui.add(page, ui.text("Como se lê?", 18, ui.ink, true));
        List<String> options = quiz.choices();
        if (options.size() == 1 && quiz.selected.isEmpty()) {
            ui.add(page, ui.text("Pensa na leitura antes de revelar.", 15, ui.muted, false));
            action(page, "Revelar: " + "toca para conferir", false, () -> revealSingle(question[1]));
        } else {
            for (String choice : options) {
                Button button = ui.button(choice, false, () -> submitAnswer(choice));
                button.setEnabled(quiz.selected.isEmpty()); ui.add(page, button);
            }
        }
        if (!quiz.selected.isEmpty()) {
            boolean correct = question[1].equals(quiz.selected);
            LinearLayout feedback = card(correct ? "Certo!" : "Vamos fixar esta leitura", question[0] + " → " + question[1]);
            feedback.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
            if (question[0].contains("っ")) ui.add(feedback, ui.text("O pequeno っ prepara a consoante seguinte: っし → sshi, っしゃ → ssha e っち → tchi.", 15, ui.ink, false));
            action(feedback, "Ouvir resposta", false, () -> speech.speak(question[0], false));
            action(page, quiz.index + 1 == quiz.questions.size() ? "Ver resultado" : "Próxima pergunta", true, () -> { quiz.next(); render(); scroll.scrollTo(0, 0); });
        }
    }
    private void revealSingle(String answer) {
        new AlertDialog.Builder(this).setTitle(answer).setMessage("Conseguiste recordar a leitura?")
            .setPositiveButton("Sim", (d, w) -> submitAnswer(answer))
            .setNegativeButton("Ainda não", (d, w) -> {
                if (quiz.selected.isEmpty()) { quiz.selected = "Ainda não"; progress.record(quiz.current()[0], false); render(); }
            }).show();
    }
    private void submitAnswer(String choice) {
        if (quiz.answer(choice)) {
            progress.record(quiz.current()[0], quiz.current()[1].equals(choice));
            render(); page.announceForAccessibility(quiz.current()[1].equals(choice) ? "Certo" : "A leitura é " + quiz.current()[1]);
        }
    }

    private void showSettings() {
        heading("À tua maneira", "Leitura, aparência e informação da aplicação.");
        action(page, "Aparência: " + prefs.getString("theme", "system").replace("system", "sistema").replace("dark", "escuro").replace("light", "claro"), false,
            () -> new AlertDialog.Builder(this).setTitle("Aparência").setItems(new String[]{"Seguir sistema", "Claro", "Escuro"}, (d, which) -> {
                prefs.edit().putString("theme", new String[]{"system", "light", "dark"}[which]).apply(); recreate();
            }).show());
        action(page, english ? "Inglês nas explicações: ligado" : "Inglês nas explicações: desligado", false, () -> { english = !english; prefs.edit().putBoolean("english", english).apply(); render(); });
        action(page, hideRomaji ? "Rōmaji na tabela: oculto" : "Rōmaji na tabela: visível", false, () -> { hideRomaji = !hideRomaji; prefs.edit().putBoolean("hideRomaji", hideRomaji).apply(); render(); });
        action(page, "Testar voz japonesa offline", false, () -> speech.speak("こんにちは", false));
        card("KanaBridge 3.0", "Preparada para Android 16 (API 36), com compatibilidade desde Android 6.\nSem conta, anúncios ou permissão de Internet. Texto, favoritos e revisão são guardados neste telemóvel. A desinstalação elimina estes dados.");
        card("Sobre as leituras", "O conversor combina transliteração dos kana com leituras de palavras conhecidas. Não traduz frases nem determina todas as leituras dos kanji. O áudio usa a síntese de voz japonesa offline instalada no telemóvel, não gravações humanas.");
        card("Referências de aprendizagem", "Japan Foundation · Marugoto\nmarugoto.jpf.go.jp\n\nConvenções de romanização: tabela japonesa ALA-LC (2022).\nloc.gov/catdir/cpso/romanization/japanese.pdf\n\nExplicações e exercícios incluídos na KanaBridge. As extensões Ainu são referência gráfica complementar.");
    }
    private void copy(String value) {
        ((ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("KanaBridge", value));
        if (android.os.Build.VERSION.SDK_INT < 33) Toast.makeText(this, "Copiado", Toast.LENGTH_SHORT).show();
    }
    private void updateBack() {
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (backRegistered) { getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(backCallback); backRegistered = false; }
            if (!history.isEmpty()) {
                getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT, backCallback);
                backRegistered = true;
            }
        }
    }
    private void back() {
        if (!history.isEmpty()) { section = history.remove(history.size() - 1); render(); scroll.scrollTo(0, 0); }
        else finish();
    }
    @Override public void onBackPressed() { back(); }
    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putString("section", section); out.putString("converter", converter);
        out.putString("tableQuery", tableQuery); out.putString("dictionaryQuery", dictionaryQuery);
        out.putString("category", category); out.putBoolean("katakana", katakana);
        out.putInt("collection", collection); out.putInt("scroll", scroll.getScrollY());
        out.putStringArrayList("history", history); out.putStringArrayList("reviewWords", reviewWords);
        out.putStringArrayList("quizWords", quizWords); out.putStringArrayList("quizReadings", quizReadings);
        if (quiz != null) {
            out.putString("group", group); out.putLong("seed", quizSeed);
            out.putInt("index", quiz.index); out.putInt("score", quiz.score); out.putString("selected", quiz.selected);
        }
    }
    private void restore(Bundle in) {
        section = in.getString("section", "discover"); converter = in.getString("converter", "");
        tableQuery = in.getString("tableQuery", ""); dictionaryQuery = in.getString("dictionaryQuery", "");
        category = in.getString("category", KanaData.GOJUON); katakana = in.getBoolean("katakana");
        collection = in.getInt("collection");
        if (in.getStringArrayList("history") != null) history = in.getStringArrayList("history");
        if (in.getStringArrayList("reviewWords") != null) reviewWords = in.getStringArrayList("reviewWords");
        if (in.containsKey("seed")) {
            group = in.getString("group", "Vogais"); quizSeed = in.getLong("seed");
            List<String[]> originalPool = new ArrayList<>();
            if (in.getStringArrayList("quizWords") != null && in.getStringArrayList("quizReadings") != null) {
                quizWords = in.getStringArrayList("quizWords"); quizReadings = in.getStringArrayList("quizReadings");
                for (int i = 0; i < Math.min(quizWords.size(), quizReadings.size()); i++) originalPool.add(new String[]{quizWords.get(i), quizReadings.get(i)});
            }
            quiz = new StudySession(originalPool, quizSeed);
            quiz.index = Math.min(in.getInt("index"), quiz.questions.size()); quiz.score = in.getInt("score"); quiz.selected = in.getString("selected", "");
        }
    }
    @Override protected void onStop() {
        super.onStop(); speech.stop();
        prefs.edit().putString("draft", converter).putString("section", section).apply();
    }
    @Override protected void onDestroy() {
        handler.removeCallbacksAndMessages(null); speech.close();
        if (android.os.Build.VERSION.SDK_INT >= 33 && backRegistered) getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(backCallback);
        super.onDestroy();
    }
}
