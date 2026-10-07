package com.kanabridge;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Two primary tools. Additional consultation is optional and accessed from the menu. */
public final class MainActivity extends Activity {
    private Ui ui;
    private SharedPreferences prefs;
    private SavedWords savedWords;
    private JapaneseSpeech speech;
    private OfflineLexicon lexicon;
    private LinearLayout shell, page, navigation, results, table;
    private ScrollView scroll;
    private String section = "converter", draft = "", query = "", category = KanaData.GOJUON;
    private String referenceCategory = "Frases", referenceQuery = "", dictionaryQuery = "";
    private String selectedKanji = "", chosenReading = "";
    private boolean katakana, dark, macrons;
    private int generation;
    private int outputMode;
    private int navigationLeft, navigationRight, navigationBottom;
    private Button[] outputButtons;
    private final Map<Integer, String> composition = new HashMap<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pending;
    private android.window.OnBackInvokedCallback backCallback;
    private boolean backRegistered;

    @Override public void onCreate(Bundle state) {
        prefs = getSharedPreferences("kanabridge", MODE_PRIVATE);
        String theme = prefs.getString("theme", "system");
        dark = "dark".equals(theme) || ("system".equals(theme) && (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES);
        setTheme(getResources().getIdentifier(dark ? "KanaBridgeDark" : "KanaBridgeLight", "style", getPackageName()));
        super.onCreate(state);
        ui = new Ui(this, dark); savedWords = new SavedWords(prefs); speech = new JapaneseSpeech(this); lexicon = new OfflineLexicon(this);
        draft = prefs.getString("draft", ""); macrons = prefs.getBoolean("macrons", false);
        section = prefs.getString("section", "converter");
        if (state != null) {
            section = state.getString("section", "converter"); draft = state.getString("draft", draft);
            query = state.getString("query", ""); category = state.getString("category", KanaData.GOJUON); katakana = state.getBoolean("katakana");
            referenceCategory = state.getString("referenceCategory", "Frases"); referenceQuery = state.getString("referenceQuery", ""); dictionaryQuery = state.getString("dictionaryQuery", "");
            chosenReading = state.getString("chosenReading", ""); selectedKanji = state.getString("selectedKanji", "");
            outputMode = state.getInt("outputMode");
            ArrayList<String> choices = state.getStringArrayList("composition");
            if (choices != null) for (int i = 0; i < choices.size(); i++) if (!choices.get(i).isEmpty()) composition.put(i, choices.get(i));
        }
        if (!java.util.Arrays.asList("converter", "kana", "reference", "dictionary", "saved", "settings", "sources").contains(section)) section = "converter";
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        getWindow().setStatusBarColor(Color.TRANSPARENT); getWindow().setNavigationBarColor(Color.BLACK);
        if (android.os.Build.VERSION.SDK_INT >= 30) getWindow().setDecorFitsSystemWindows(false);
        else getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | (dark ? 0 : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR));
        buildShell(); render();
        if (state != null) scroll.post(() -> scroll.scrollTo(0, state.getInt("scroll")));
    }
    private void buildShell() {
        shell = ui.column(); shell.setBackgroundColor(ui.background);
        // Android 16 draws navigation bars over the app. Provide an opaque, high-contrast
        // surface even when a system IME has changed the navigation icon appearance.
        shell.setBackground(new android.graphics.drawable.Drawable() {
            private final android.graphics.Paint paint = new android.graphics.Paint();
            @Override public void draw(android.graphics.Canvas canvas) {
                canvas.drawColor(ui.background); paint.setColor(Color.BLACK);
                android.graphics.Rect bounds = getBounds();
                if (navigationBottom > 0) canvas.drawRect(0, bounds.height() - navigationBottom, bounds.width(), bounds.height(), paint);
                if (navigationLeft > 0) canvas.drawRect(0, 0, navigationLeft, bounds.height(), paint);
                if (navigationRight > 0) canvas.drawRect(bounds.width() - navigationRight, 0, bounds.width(), bounds.height(), paint);
            }
            @Override public void setAlpha(int alpha) {}
            @Override public void setColorFilter(android.graphics.ColorFilter filter) {}
            @Override public int getOpacity() { return android.graphics.PixelFormat.OPAQUE; }
        });
        scroll = new ScrollView(this); scroll.setFillViewport(false);
        page = ui.column(); page.setPadding(ui.dp(16), ui.dp(10), ui.dp(16), ui.dp(18)); page.setFocusableInTouchMode(true);
        scroll.addView(page, new ScrollView.LayoutParams(-1, -2)); shell.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        navigation = ui.row(); navigation.setPadding(ui.dp(12), ui.dp(6), ui.dp(12), ui.dp(6)); navigation.setBackgroundColor(ui.surface); shell.addView(navigation, ui.wrap());
        shell.setOnApplyWindowInsetsListener((view, insets) -> {
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                applyInsets(insets); navigation.setVisibility(insets.isVisible(WindowInsets.Type.ime()) ? View.GONE : View.VISIBLE);
                if (!insets.isVisible(WindowInsets.Type.ime())) shell.post(this::systemBars);
            } else shell.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            scroll.post(this::revealCursor); return insets;
        });
        if (android.os.Build.VERSION.SDK_INT >= 30) shell.setWindowInsetsAnimationCallback(new android.view.WindowInsetsAnimation.Callback(android.view.WindowInsetsAnimation.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE) {
            @Override public WindowInsets onProgress(WindowInsets insets, List<android.view.WindowInsetsAnimation> animations) {
                applyInsets(insets); scroll.post(MainActivity.this::revealCursor); return insets;
            }
        });
        shell.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob) -> { if (b != ob) scroll.post(this::revealCursor); });
        setContentView(shell); shell.requestApplyInsets();
        shell.post(this::systemBars);
        if (android.os.Build.VERSION.SDK_INT >= 33) backCallback = this::back;
    }
    private void systemBars() {
        if (android.os.Build.VERSION.SDK_INT >= 30 && getWindow().getInsetsController() != null) {
            int mask = android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
            getWindow().getInsetsController().setSystemBarsAppearance(dark ? 0 : android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS, mask);
        }
    }
    private void applyInsets(WindowInsets insets) {
        android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
        android.graphics.Insets nav = insets.getInsets(WindowInsets.Type.navigationBars());
        navigationLeft = nav.left; navigationRight = nav.right; navigationBottom = nav.bottom; shell.invalidate();
        shell.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, insets.getInsets(WindowInsets.Type.ime()).bottom));
    }
    private void revealCursor() {
        View focused = getCurrentFocus(); if (!(focused instanceof EditText)) return;
        EditText field = (EditText)focused; if (field.getLayout() == null) return;
        int position = Math.min(Math.max(0, field.getSelectionEnd()), field.length());
        int line = field.getLayout().getLineForOffset(position);
        int y = field.getLayout().getLineTop(line) + field.getTotalPaddingTop();
        int x = (int)field.getLayout().getPrimaryHorizontal(position) + field.getTotalPaddingLeft();
        field.requestRectangleOnScreen(new Rect(Math.max(0, x - ui.dp(8)), Math.max(0, y - ui.dp(12)), x + ui.dp(16), y + field.getLineHeight() + ui.dp(24)), false);
    }
    private void go(String target) {
        ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(shell.getWindowToken(), 0);
        section = target; render(); scroll.scrollTo(0, 0);
    }
    private void back() { go("converter"); }
    @Override public void onBackPressed() { if (!section.equals("converter")) back(); else super.onBackPressed(); }
    private void render() {
        generation++; if (pending != null) handler.removeCallbacks(pending); page.removeAllViews();
        LinearLayout bar = ui.row(); bar.addView(ui.text("あ  KanaBridge", 20, ui.accent, true), new LinearLayout.LayoutParams(0, -2, 1));
        bar.addView(ui.button("Menu", false, this::menu), new LinearLayout.LayoutParams(-2, -2)); page.addView(bar, ui.wrap());
        if (!section.equals("converter") && !section.equals("kana")) ui.add(page, ui.button("← Converter", false, () -> go("converter")));
        switch (section) {
            case "kana": kana(); break;
            case "reference": reference(); break;
            case "dictionary": dictionary(); break;
            case "saved": saved(); break;
            case "settings": settings(); break;
            case "sources": sources(); break;
            default: converter();
        }
        navigation.removeAllViews();
        Button convert = ui.button("Converter", section.equals("converter"), () -> go("converter")); convert.setId(110);
        Button kana = ui.button("Kana", section.equals("kana"), () -> go("kana")); kana.setId(111);
        ui.paint(convert, section.equals("converter")); ui.paint(kana, section.equals("kana")); weighted(navigation, convert); weighted(navigation, kana); page.requestFocus();
        if (android.os.Build.VERSION.SDK_INT >= 33 && backCallback != null) {
            if (backRegistered) { getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(backCallback); backRegistered = false; }
            if (!section.equals("converter")) { getOnBackInvokedDispatcher().registerOnBackInvokedCallback(0, backCallback); backRegistered = true; }
        }
    }
    private void menu() {
        String[] labels = {"Dicionário offline", "Guardados", "Referência de japonês (opcional)", "Definições", "Fontes e licenças"};
        String[] routes = {"dictionary", "saved", "reference", "settings", "sources"};
        new AlertDialog.Builder(this).setTitle("Consulta e opções").setItems(labels, (d,w) -> go(routes[w])).setNegativeButton("Fechar", null).show();
    }
    private void title(String value) { ui.add(page, ui.heading(value)); }
    private TextView label(String value) { return ui.text(value, 15, ui.muted, false); }
    private void weighted(LinearLayout row, View view) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1); lp.setMargins(ui.dp(2), ui.dp(2), ui.dp(2), ui.dp(2)); row.addView(view, lp);
    }
    private EditText field(String text, String hint, int id, boolean multi) {
        EditText field = new EditText(this); field.setId(id); field.setTextColor(ui.ink); field.setHintTextColor(ui.muted); field.setTextSize(20);
        field.setPadding(ui.dp(14), ui.dp(12), ui.dp(14), ui.dp(12)); field.setBackground(ui.shape(ui.surface, ui.line, 14));
        field.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | (multi ? android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE : 0));
        field.setImeOptions(android.view.inputmethod.EditorInfo.IME_FLAG_NO_EXTRACT_UI | (multi ? android.view.inputmethod.EditorInfo.IME_ACTION_NONE : android.view.inputmethod.EditorInfo.IME_ACTION_DONE));
        field.setGravity(Gravity.TOP | Gravity.START); field.setSingleLine(!multi); field.setMinLines(multi ? 2 : 1); field.setMaxLines(multi ? 5 : 1);
        field.setFilters(new InputFilter[]{new InputFilter.LengthFilter(multi ? 4000 : 120)}); field.setHint(hint); field.setContentDescription(hint); field.setText(text); return field;
    }
    private interface Changed { void changed(String value); }
    private void watch(EditText field, Changed changed) {
        field.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int start,int count,int after) {}
            public void onTextChanged(CharSequence s,int start,int before,int count) { changed.changed(s.toString()); }
            public void afterTextChanged(Editable e) {}
        });
    }
    private void later(Runnable action) { if (pending != null) handler.removeCallbacks(pending); pending = action; handler.postDelayed(action, 160); }
    private void converter() {
        title("Converter"); ui.add(page, label("Escreve rōmaji ou cola japonês. Copia o resultado que precisas."));
        EditText input = field(draft, "Rōmaji ou japonês", 201, true); ui.add(page, input);
        LinearLayout tools = ui.row(); weighted(tools, ui.button("Colar", false, () -> {
            ClipboardManager clipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
            if (clipboard.hasPrimaryClip() && clipboard.getPrimaryClip().getItemCount() > 0) {
                CharSequence value = clipboard.getPrimaryClip().getItemAt(0).coerceToText(this); if (value != null) { input.setText(value); input.setSelection(input.length()); }
            }
        })); weighted(tools, ui.button("Limpar", false, () -> input.setText(""))); ui.add(page, tools);
        String[] modes = {"Hiragana", "Katakana", "Kanji", "Rōmaji"}; outputButtons = new Button[4];
        for (int row = 0; row < 2; row++) {
            LinearLayout choices = ui.row();
            for (int col = 0; col < 2; col++) {
                final int mode = row * 2 + col;
                Button button = ui.button(modes[mode], mode == outputMode, () -> {
                    outputMode = mode; for (int i = 0; i < 4; i++) ui.paint(outputButtons[i], i == mode); updateResults();
                }); button.setId(220 + mode); ui.paint(button, mode == outputMode); outputButtons[mode] = button; weighted(choices, button);
            }
            page.addView(choices, ui.space(4));
        }
        results = ui.column(); ui.add(page, results); updateResults();
        watch(input, value -> {
            // Android restores EditText after onCreate; an identical value must not clear lexical choices.
            if (value.equals(draft)) return;
            draft = value; selectedKanji = ""; chosenReading = ""; composition.clear(); generation++;
            later(() -> { updateResults(); revealCursor(); });
        });
    }
    private void output(LinearLayout parent, String name, String value, boolean speak) {
        LinearLayout card = ui.card(); card.addView(label(name), ui.wrap());
        TextView text = ui.text(value, 24, ui.ink, false); text.setTextIsSelectable(true); card.addView(text, ui.space(6));
        LinearLayout actions = ui.row(); weighted(actions, ui.button("Copiar", false, () -> copy(value)));
        if (speak) weighted(actions, ui.button("Ouvir", false, () -> speech.speak(value, false)));
        card.addView(actions, ui.space(6)); ui.add(parent, card);
    }
    private void updateResults() {
        final int request = ++generation; results.removeAllViews();
        if (draft.trim().isEmpty()) {
            ui.add(results, label("Ex.: irasshaimase → いらっしゃいませ\ngakkou → がっこう · nihongo → にほんご → 日本語"));
            ui.add(results, ui.button("Como escrever sons especiais", false, this::spellingHelp)); return;
        }
        RomajiConverter.Result converted = RomajiConverter.convert(draft);
        String hira = chosenReading.isEmpty() ? converted.hiragana : chosenReading;
        KanaTransliterator.Result reading = KanaTransliterator.transliterate(hira, macrons ? KanaTransliterator.RomanizationStyle.MACRON : KanaTransliterator.RomanizationStyle.SIMPLE);
        if (outputMode == 0) output(results, "Hiragana", hira, true);
        if (outputMode == 1) output(results, "Katakana", KanaData.toKatakana(hira), true);
        if (outputMode == 3) output(results, "Rōmaji", reading.romaji, false);
        if (converted.partial || reading.partial || hasKanji(hira)) ui.add(results, label("Conversão parcial: texto sem leitura reconhecida foi mantido. Para um kanji, abre Kanji e escolhe uma leitura. Frases com kanji não têm leitura automática completa."));
        if (outputMode != 2) {
            ui.add(results, ui.button("Como escrever sons especiais", false, this::spellingHelp)); return;
        }
        if (!selectedKanji.isEmpty()) output(results, "Kanji escolhido", selectedKanji, true);
        String[] parts = converted.hiragana.trim().split("\\s+");
        if (parts.length > 1) {
            StringBuilder built = new StringBuilder(); java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\S+").matcher(converted.hiragana);
            int index = 0, end = 0;
            while (matcher.find()) { built.append(converted.hiragana, end, matcher.start()).append(composition.containsKey(index) ? composition.get(index) : matcher.group()); end = matcher.end(); index++; }
            built.append(converted.hiragana.substring(end)); output(results, "Composição com kanji", built.toString(), true);
            ui.add(results, label("Escolhe kanji por palavra. Separa as palavras por espaços; partículas e flexões exigem a grafia correta. Isto não traduz nem analisa a gramática da frase."));
            for (int i = 0; i < Math.min(parts.length, 40); i++) {
                final int position = i; final String part = parts[i]; ui.add(results, ui.button(part + " · escolher grafia", false, () -> choosePart(part, position)));
            }
            if (parts.length > 40) ui.add(results, label("Mostradas as primeiras 40 palavras. Divide o texto para escolher as restantes."));
        } else {
            LinearLayout candidates = ui.column(); ui.add(results, candidates);
            loadCandidates(candidates, converted.hiragana.trim(), false, 30, request, word -> { selectedKanji = word.word; chosenReading = word.reading; updateResults(); });
        }
        ui.add(results, ui.button("Como escrever sons especiais", false, this::spellingHelp));
    }
    private interface Picked { void pick(OfflineLexicon.Word word); }
    private boolean hasKanji(String value) {
        for (int i = 0; i < value.length();) {
            int cp = value.codePointAt(i); if ((cp >= 0x3400 && cp <= 0x9fff) || (cp >= 0x20000 && cp <= 0x323af)) return true;
            i += Character.charCount(cp);
        }
        return false;
    }
    private void loadCandidates(LinearLayout into, String value, boolean prefix, int limit, int request, Picked picked) {
        into.removeAllViews(); ui.add(into, label("A procurar no dicionário offline…"));
        lexicon.search(value, prefix, limit, (words, error) -> {
            if (request != generation || isFinishing() || isDestroyed()) return;
            into.removeAllViews();
            if (error != null) { ui.add(into, label(error)); ui.add(into, ui.button("Tentar novamente", false, () -> loadCandidates(into, value, prefix, limit, request, picked))); return; }
            ui.add(into, label(words.isEmpty() ? "Sem palavras correspondentes. Confirma a grafia e as vogais longas; nem todas as palavras usam kanji." : "Grafias e leituras · significados em inglês\nMantém premido para ver os detalhes completos."));
            for (OfflineLexicon.Word word : words) {
                Button button = ui.button(word.word + "  ·  " + word.reading + "\n" + word.meaning, false, () -> picked.pick(word));
                button.setMaxLines(4); button.setEllipsize(android.text.TextUtils.TruncateAt.END);
                button.setOnLongClickListener(v -> { wordDetail(word); return true; });
                button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL); ui.add(into, button);
            }
            if (words.size() == limit) ui.add(into, ui.button("Mostrar mais opções", false, () -> loadCandidates(into, value, prefix, limit + 30, request, picked)));
        });
    }
    private void choosePart(String value, int position) {
        LinearLayout content = ui.column(); content.setPadding(ui.dp(16), 0, ui.dp(16), ui.dp(12)); ScrollView scroller = new ScrollView(this); scroller.addView(content);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Grafia para " + value).setView(scroller).setNegativeButton("Fechar", null).create(); dialog.show();
        loadCandidates(content, value, false, 30, generation, word -> { composition.put(position, word.word); dialog.dismiss(); updateResults(); });
    }
    private void spellingHelp() {
        message("Rōmaji → japonês", "Consoante dupla: gakkou → がっこう; irasshaimase → いらっしゃいませ; matcha → まっちゃ. O っ representa uma breve pausa antes da consoante seguinte.\n\nDakuten e handakuten são automáticos: ga → が, za → ざ, pa → ぱ. Combinações: kya → きゃ, sha → しゃ, cha → ちゃ.\n\nN separado: kan'i → かんい; shin'you → しんよう. nna → んな; nn no fim → ん.\n\nKana pequenos: xa → ぁ; xya → ゃ; xtsu → っ (também la, lya, ltsu).\n\nVogais longas: escreve a grafia japonesa: ou, oo, uu, ei… ō usa ou por convenção, mas não distingue おう de おお. Para nomes estrangeiros usa hífen: ko-hi- → コーヒー.\n\nRōmaji indica sons, não o significado. Para kanji escolhe uma palavra pelo significado. Partículas mantêm a grafia japonesa: wa (tema) escreve-se ha → は; e (direção) escreve-se he → へ; o (objeto) escreve-se wo → を.");
    }

    private void kana() {
        LinearLayout scripts = ui.row();
        Button hira = ui.button("Hiragana", !katakana, () -> { katakana = false; kanaRefresh(); });
        Button kata = ui.button("Katakana", katakana, () -> { katakana = true; kanaRefresh(); });
        ui.paint(hira, !katakana); ui.paint(kata, katakana); hira.setId(210); kata.setId(211); weighted(scripts, hira); weighted(scripts, kata);
        Button categories = ui.button(categoryLabel(category) + " ▾", false, () -> {
            String[] keys = {KanaData.GOJUON, KanaData.DAKUTEN, KanaData.YOON, KanaData.SMALL, KanaData.MODERN, KanaData.REFERENCE, KanaData.AINU};
            String[] names = new String[keys.length]; for (int i = 0; i < keys.length; i++) names[i] = categoryLabel(keys[i]);
            new AlertDialog.Builder(this).setTitle("Tabela").setSingleChoiceItems(names, java.util.Arrays.asList(keys).indexOf(category), (d,w) -> { category = keys[w]; query = ""; d.dismiss(); kanaRefresh(); }).setNegativeButton("Fechar", null).show();
        }); categories.setId(212);
        boolean wide = getResources().getConfiguration().screenWidthDp >= 600 && getResources().getConfiguration().fontScale <= 1.3f;
        if (wide) weighted(scripts, categories);
        ui.add(page, scripts); if (!wide) ui.add(page, categories);
        EditText search = field(query, "Procurar som ou kana: shi, が, kya…", 202, false); ui.add(page, search);
        table = ui.column(); table.setId(213); page.addView(table, ui.space(8)); renderTable(); watch(search, value -> { query = value; later(this::renderTable); });
    }
    private void kanaRefresh() { int y = scroll.getScrollY(); render(); scroll.post(() -> scroll.scrollTo(0, y)); }
    private String categoryLabel(String key) {
        if (key.equals(KanaData.GOJUON)) return "Básicos · gojūon";
        if (key.equals(KanaData.DAKUTEN)) return "Sons com ゛ e ゜ · dakuten";
        if (key.equals(KanaData.YOON)) return "Combinações · kya, sha, cha…";
        return key;
    }
    private void renderTable() {
        table.removeAllViews(); boolean compact = getResources().getConfiguration().fontScale <= 1.3f && getResources().getConfiguration().screenWidthDp >= 340;
        if (query.trim().isEmpty() && category.equals(KanaData.GOJUON) && compact) {
            LinearLayout header = ui.row();
            for (String v : new String[]{"a", "i", "u", "e", "o"}) { TextView t = label(v); t.setGravity(Gravity.CENTER); weighted(header, t); } table.addView(header, ui.wrap());
            for (String[] values : KanaData.gojuonGrid(katakana)) {
                LinearLayout row = ui.row(); row.setGravity(Gravity.TOP);
                for (String symbol : values) {
                    if (symbol.isEmpty()) { View empty = new View(this); empty.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO); weighted(row, empty); }
                    else weighted(row, kanaButton(KanaData.find(symbol), true));
                }
                table.addView(row, ui.space(4));
            }
        } else {
            List<KanaEntry> entries = query.trim().isEmpty() ? KanaData.entries(category) : KanaData.search(query);
            if (entries.isEmpty()) { ui.add(table, label("Sem resultados. Tenta um som como shi ou um símbolo como し.")); return; }
            int columns = compact ? 3 : getResources().getConfiguration().fontScale > 1.6f ? 1 : 2;
            LinearLayout row = null;
            for (int i = 0; i < entries.size(); i++) {
                if (i % columns == 0) { row = ui.row(); row.setGravity(Gravity.TOP); table.addView(row, ui.space(4)); }
                weighted(row, kanaButton(entries.get(i), false));
            }
            if (entries.size() % columns != 0) for (int i = entries.size() % columns; i < columns; i++) weighted(row, new View(this));
        }
        ui.add(table, label("Toca num kana para copiar, ouvir ou ver o som. ゛ e ゜ já fazem parte dos símbolos da tabela Dakuten."));
    }
    private Button kanaButton(KanaEntry entry, boolean compact) {
        String symbol = entry.symbol(katakana); Button cell = ui.button(symbol + "\n" + entry.romaji, false, () -> kanaDetail(entry));
        cell.setTextSize(compact ? 18 : 20); cell.setPadding(ui.dp(3), ui.dp(10), ui.dp(3), ui.dp(10)); cell.setMinHeight(ui.dp(72));
        cell.setContentDescription(symbol + ", " + entry.romaji + ", " + entry.category + ". Toca para opções."); return cell;
    }
    private void kanaDetail(KanaEntry entry) {
        String symbol = entry.symbol(katakana); LinearLayout body = ui.card(); body.addView(ui.text(symbol + "  ·  " + entry.romaji, 30, ui.ink, true));
        ui.add(body, label("Hiragana: " + (entry.hiragana.isEmpty() ? "sem equivalente" : entry.hiragana) + "\nKatakana: " + entry.katakana + (entry.notePt.isEmpty() ? "" : "\n\n" + entry.notePt)));
        if (symbol.equals("っ") || symbol.equals("ッ")) ui.add(body, label("Pausa curta antes da consoante: がっこう → gakkou; いらっしゃいませ → irasshaimase. Não se lê tsu neste uso."));
        ui.add(body, ui.button("Ouvir", false, () -> speech.speak(symbol, false))); ui.add(body, ui.button("Copiar " + symbol, false, () -> copy(symbol)));
        ScrollView view = new ScrollView(this); view.addView(body); AlertDialog dialog = new AlertDialog.Builder(this).setView(view).setNegativeButton("Fechar", null).create();
        ui.add(body, ui.button("Usar no conversor", true, () -> { draft = symbol; chosenReading = ""; selectedKanji = ""; composition.clear(); dialog.dismiss(); go("converter"); })); dialog.show();
    }

    private void dictionary() {
        title("Dicionário offline"); ui.add(page, label("Procura uma palavra em rōmaji, kana ou kanji. Significados do JMdict em inglês; consulta portuguesa na Referência."));
        EditText input = field(dictionaryQuery, "Palavra: hashi, にほんご, 日本語…", 203, false); ui.add(page, input); LinearLayout list = ui.column(); ui.add(page, list);
        Runnable refresh = () -> {
            int request = ++generation;
            if (dictionaryQuery.trim().isEmpty()) { list.removeAllViews(); ui.add(list, label("Escreve uma palavra para ver grafias, leituras e significados.")); }
            else loadCandidates(list, RomajiConverter.convert(dictionaryQuery).hiragana, true, 30, request, this::wordDetail);
        };
        watch(input, value -> { dictionaryQuery = value; generation++; later(refresh); }); refresh.run();
    }
    private void wordDetail(OfflineLexicon.Word word) {
        LinearLayout body = ui.card(); body.addView(ui.text(word.word, 30, ui.ink, true));
        ui.add(body, label(word.reading + " · " + KanaTransliterator.transliterate(word.reading).romaji + "\n\n" + word.meaning));
        ui.add(body, ui.button("Ouvir leitura", false, () -> speech.speak(word.reading, false))); ui.add(body, ui.button("Copiar", false, () -> copy(word.word)));
        Button save = ui.button(savedWords.contains(word.word) ? "Remover dos guardados" : "Guardar", false, () -> {});
        save.setOnClickListener(v -> { savedWords.toggle(word.word); save.setText(savedWords.contains(word.word) ? "Remover dos guardados" : "Guardar"); }); ui.add(body, save);
        ScrollView view = new ScrollView(this); view.addView(body); AlertDialog dialog = new AlertDialog.Builder(this).setView(view).setNegativeButton("Fechar", null).create();
        ui.add(body, ui.button("Usar no conversor", true, () -> { draft = word.reading; selectedKanji = word.word; chosenReading = word.reading; composition.clear(); dialog.dismiss(); go("converter"); })); dialog.show();
    }
    private void saved() {
        title("Guardados"); List<String> words = savedWords.all(); if (words.isEmpty()) ui.add(page, label("Guarda palavras nas opções do dicionário para voltar a consultá-las aqui."));
        for (String word : words) ui.add(page, ui.button(word, false, () -> { dictionaryQuery = word; go("dictionary"); }));
    }
    private void reference() {
        title("Referência de japonês"); ui.add(page, label("Consulta opcional: " + JapaneseReference.all().size() + " entradas em português. Frases, partículas, verbos e termos; sem exercícios."));
        ui.add(page, ui.button(referenceCategory + " ▾", false, () -> {
            String[] categories = {"Frases", "Verbos", "Termos", "Expressões", "Tudo"};
            new AlertDialog.Builder(this).setTitle("Consultar").setItems(categories, (d,w) -> { referenceCategory = categories[w]; go("reference"); }).setNegativeButton("Fechar", null).show();
        }));
        EditText input = field(referenceQuery, "Procurar em português, rōmaji ou japonês", 204, false); ui.add(page, input); LinearLayout list = ui.column(); ui.add(page, list);
        watch(input, value -> { referenceQuery = value; later(() -> referenceList(list, 30)); }); referenceList(list, 30);
    }
    private void referenceList(LinearLayout list, int limit) {
        list.removeAllViews(); List<JapaneseReference.Entry> entries = JapaneseReference.search(referenceCategory, referenceQuery); ui.add(list, label(entries.size() + " resultados · " + referenceCategory));
        for (int i = 0; i < Math.min(entries.size(), limit); i++) {
            JapaneseReference.Entry entry = entries.get(i);
            Button button = ui.button(entry.meaning + "\n" + entry.japanese + " · " + entry.romaji(), false, () -> {
                LinearLayout body = ui.card(); body.addView(ui.text(entry.japanese, 25, ui.ink, true));
                ui.add(body, label(entry.reading + "\n" + entry.romaji() + "\n\n" + entry.meaning + (entry.detail.isEmpty() ? "" : "\n\n" + entry.detail)));
                ui.add(body, ui.button("Ouvir", false, () -> speech.speak(entry.japanese, false))); ui.add(body, ui.button("Ouvir devagar", false, () -> speech.speak(entry.japanese, true)));
                ui.add(body, ui.button("Copiar japonês", false, () -> copy(entry.japanese))); ScrollView view = new ScrollView(this); view.addView(body);
                new AlertDialog.Builder(this).setView(view).setNegativeButton("Fechar", null).show();
            }); button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL); ui.add(list, button);
        }
        if (entries.size() > limit) ui.add(list, ui.button("Mostrar mais (" + (entries.size() - limit) + ")", false, () -> referenceList(list, limit + 30)));
    }
    private void settings() {
        title("Definições");
        ui.add(page, ui.button("Aspeto: " + prefs.getString("theme", "system"), false, () -> {
            String[] values = {"system", "light", "dark"}; new AlertDialog.Builder(this).setTitle("Aspeto").setItems(new String[]{"Seguir o sistema", "Claro", "Escuro"}, (d,w) -> { prefs.edit().putString("theme", values[w]).apply(); recreate(); }).show();
        }));
        ui.add(page, ui.button("Rōmaji: " + (macrons ? "com mácrones (ō, ū)" : "vogais por extenso (ou, uu)"), false, () -> { macrons = !macrons; prefs.edit().putBoolean("macrons", macrons).apply(); render(); }));
        ui.add(page, ui.button("Testar voz japonesa", false, () -> speech.speak("こんにちは", false)));
        ui.add(page, label("O tamanho do texto acompanha as definições de acessibilidade do Android. A tabela adapta-se a texto grande.\n\nKanaBridge 4 · Android 6–16 · sem conta e sem Internet. Voz japonesa requer um pacote offline instalado no telemóvel."));
    }
    private void sources() {
        title("Fontes e licenças");
        TextView text = label("Dicionário: JMdict © James William Breen e Electronic Dictionary Research and Development Group (EDRDG). Derivado SQLite sob CC BY-SA 4.0, com leituras normalizadas, restrições de grafia e ordenação por frequência. Significados em inglês.\n\nhttps://www.edrdg.org/\nhttps://www.edrdg.org/edrdg/licence.html\nhttps://creativecommons.org/licenses/by-sa/4.0/\n\nA referência em português foi escrita para esta app. Não constitui um tradutor automático de frases. Nomes próprios, flexões e ambiguidades podem não ter correspondência direta.");
        android.text.util.Linkify.addLinks(text, android.text.util.Linkify.WEB_URLS); text.setLinkTextColor(ui.accent); ui.add(page, text);
        try {
            org.json.JSONObject metadata = new org.json.JSONObject(assetText("lexicon-source.json"));
            ui.add(page, label("Dicionário incluído: " + metadata.getInt("entries") + " entradas. Preparado em " + metadata.getString("generated").substring(0, 10) + "."));
        } catch (Exception ignored) {}
        ui.add(page, ui.button("Licença CC BY-SA 4.0", false, () -> { try { message("CC BY-SA 4.0", assetText("CC-BY-SA-4.0.txt")); } catch (Exception e) { message("Licença", "https://creativecommons.org/licenses/by-sa/4.0/"); } }));
        ui.add(page, ui.button("Condições EDRDG", false, () -> { try { message("Condições EDRDG", android.text.Html.fromHtml(assetText("EDRDG-LICENCE.html")).toString()); } catch (Exception e) { message("EDRDG", "https://www.edrdg.org/edrdg/licence.html"); } }));
    }
    private String assetText(String name) throws Exception {
        try (java.io.InputStream input = getAssets().open(name); java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096]; int count; while ((count = input.read(buffer)) != -1) out.write(buffer, 0, count); return out.toString("UTF-8");
        }
    }
    private void message(String title, String text) { new AlertDialog.Builder(this).setTitle(title).setMessage(text).setPositiveButton("Fechar", null).show(); }
    private void copy(String value) { ((ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("KanaBridge", value)); if (android.os.Build.VERSION.SDK_INT < 33) android.widget.Toast.makeText(this, "Copiado", android.widget.Toast.LENGTH_SHORT).show(); }
    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state); state.putString("section", section); state.putString("draft", draft); state.putString("query", query); state.putString("category", category); state.putBoolean("katakana", katakana);
        state.putString("referenceCategory", referenceCategory); state.putString("referenceQuery", referenceQuery); state.putString("dictionaryQuery", dictionaryQuery); state.putString("chosenReading", chosenReading); state.putString("selectedKanji", selectedKanji); state.putInt("scroll", scroll.getScrollY()); state.putInt("outputMode", outputMode);
        ArrayList<String> choices = new ArrayList<>(); int count = RomajiConverter.convert(draft).hiragana.trim().split("\\s+").length;
        for (int i = 0; i < count; i++) choices.add(composition.containsKey(i) ? composition.get(i) : ""); state.putStringArrayList("composition", choices);
    }
    @Override protected void onStop() { prefs.edit().putString("draft", draft).putString("section", section).apply(); speech.stop(); super.onStop(); }
    @Override protected void onDestroy() { if (pending != null) handler.removeCallbacks(pending); speech.close(); lexicon.close(); super.onDestroy(); }
}
