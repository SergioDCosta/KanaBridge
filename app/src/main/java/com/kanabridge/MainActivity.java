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

/** Offline dictionary and Japanese consultation tools. */
public final class MainActivity extends Activity {
    private Ui ui;
    private SharedPreferences prefs;
    private SavedWords savedWords;
    private String referenceError;
    private JapaneseSpeech speech;
    private OfflineLexicon lexicon;
    private LinearLayout shell, page, navigation, results, table;
    private ScrollView scroll;
    private String section = "converter", draft = "", query = "", category = KanaData.GOJUON;
    private String referenceCategory = "Tudo", referenceQuery = "", dictionaryQuery = "";
    private String selectedKanji = "", chosenReading = "";
    private OfflineLexicon.Word detailWord;
    private String detailOrigin = "dictionary";
    private int dictionaryLimit = 30, dictionaryReturnScroll = -1, detailReturnScroll;
    private int dictionarySelection;
    private boolean dictionaryFocused;
    private int dictionaryMode;
    private String savedQuery = "";
    private boolean katakana, dark, macrons;
    private String currencyAmount = "";
    private boolean yenToEuro, rateLoading;
    private CurrencyConverter.Rate rate;
    private long rateFetched;
    private String rateStatus = "";
    private TextView currencyResult, currencyRate, currencyStatus;
    private int generation;
    private int outputMode;
    private int navigationLeft, navigationRight, navigationBottom;
    private Button[] outputButtons;
    private TextView converterTitle, converterDescription;
    private EditText converterInput;
    private View converterTools;
    private boolean keyboardVisible;
    private LinearLayout simpleResult;
    private TextView resultTitle, resultText, partialNotice;
    private Button resultSpeak;
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
        lexicon.prepareReference(error -> {
            referenceError = error;
            if (!isFinishing() && !isDestroyed() && (section.equals("reference") || section.equals("word"))) render();
        });
        if (!prefs.contains("startSection") && prefs.contains("section")) {
            String previous = prefs.getString("section", "converter");
            String initial = java.util.Arrays.asList("dictionary", "converter", "kana", "reference").contains(previous) ? previous : "converter";
            prefs.edit().putString("startSection", initial).apply();
        }
        draft = prefs.getString("draft", ""); macrons = prefs.getBoolean("macrons", false); loadRate();
        section = prefs.getString("section", prefs.getString("startSection", "dictionary"));
        if (state != null) {
            section = state.getString("section", "converter"); draft = state.getString("draft", draft);
            query = state.getString("query", ""); category = state.getString("category", KanaData.GOJUON); katakana = state.getBoolean("katakana");
            referenceCategory = state.getString("referenceCategory", "Tudo"); referenceQuery = state.getString("referenceQuery", ""); dictionaryQuery = state.getString("dictionaryQuery", "");
            chosenReading = state.getString("chosenReading", ""); selectedKanji = state.getString("selectedKanji", "");
            outputMode = state.getInt("outputMode");
            dictionaryLimit = state.getInt("dictionaryLimit", 30); dictionaryMode = state.getInt("dictionaryMode", state.getBoolean("dictionaryMeaning") ? 1 : 0); savedQuery = state.getString("savedQuery", "");
            if (section.equals("dictionary")) dictionaryReturnScroll = state.getInt("scroll");
            detailOrigin = state.getString("detailOrigin", "dictionary");
            detailReturnScroll = state.getInt("detailReturnScroll");
            dictionarySelection = state.getInt("dictionarySelection");
            dictionaryFocused = state.getBoolean("dictionaryFocused");
            if (state.containsKey("detailWord")) detailWord = new OfflineLexicon.Word(state.getString("detailWord"), state.getString("detailReading"), state.getString("detailMeaning"), state.getLong("detailEntry"), state.getString("detailOriginalReading", state.getString("detailReading")));
            ArrayList<String> choices = state.getStringArrayList("composition");
            if (choices != null) for (int i = 0; i < choices.size(); i++) if (!choices.get(i).isEmpty()) composition.put(i, choices.get(i));
        }
        if (!java.util.Arrays.asList("converter", "kana", "reference", "dictionary", "saved", "settings", "sources", "word", "currency").contains(section)) section = "converter";
        if (dictionaryMode < 0 || dictionaryMode > 1) { dictionaryMode = 0; dictionaryQuery = ""; }
        if (section.equals("word") && detailWord == null) section = "dictionary";
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        getWindow().setStatusBarColor(Color.TRANSPARENT); getWindow().setNavigationBarColor(Color.BLACK);
        if (android.os.Build.VERSION.SDK_INT >= 30) getWindow().setDecorFitsSystemWindows(false);
        else getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | (dark ? 0 : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR));
        buildShell(); render();

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
        navigation = ui.column(); navigation.setPadding(ui.dp(12), ui.dp(6), ui.dp(12), ui.dp(6)); navigation.setBackgroundColor(ui.surface); shell.addView(navigation, ui.wrap());
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
        boolean visible = insets.isVisible(WindowInsets.Type.ime());
        if (visible != keyboardVisible) { keyboardVisible = visible; fitConverterToKeyboard(); }
        android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
        android.graphics.Insets nav = insets.getInsets(WindowInsets.Type.navigationBars());
        navigationLeft = nav.left; navigationRight = nav.right; navigationBottom = nav.bottom; shell.invalidate();
        shell.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, insets.getInsets(WindowInsets.Type.ime()).bottom));
    }
    private void fitConverterToKeyboard() {
        if (converterInput == null) return;
        converterTitle.setVisibility(keyboardVisible ? View.GONE : View.VISIBLE);
        converterDescription.setVisibility(keyboardVisible ? View.GONE : View.VISIBLE);
        converterInput.setMinLines(keyboardVisible ? 1 : 2);
        for (View view : new View[]{converterInput, converterTools, results}) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)view.getLayoutParams();
            params.topMargin = ui.dp(keyboardVisible ? Ui.SPACE_SMALL : Ui.SPACE);
            view.setLayoutParams(params);
        }
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
    private void back() {
        if (section.equals("word")) {
            if (detailOrigin.equals("dictionary")) dictionaryReturnScroll = detailReturnScroll;
            go(detailOrigin);
            if (!detailOrigin.equals("dictionary")) scroll.post(() -> scroll.scrollTo(0, detailReturnScroll));
        } else if (section.equals("saved")) go("dictionary");
        else go("converter");
    }
    @Override public void onBackPressed() { if (!section.equals("converter")) back(); else super.onBackPressed(); }
    private void render() {
        converterInput = null; simpleResult = null; currencyResult = null; currencyRate = null; currencyStatus = null;
        page.setPadding(ui.dp(16), ui.dp(Ui.SPACE), ui.dp(16), ui.dp(Ui.SPACE_LARGE));
        generation++; if (pending != null) handler.removeCallbacks(pending); page.removeAllViews();
        if (!compactKanaHeader()) {
            LinearLayout bar = ui.row(); bar.addView(ui.text("あ  KanaBridge", 20, ui.accent, true), new LinearLayout.LayoutParams(0, -2, 1));
            bar.addView(ui.button("Menu", false, this::menu), new LinearLayout.LayoutParams(-2, -2)); page.addView(bar, ui.wrap());
        }
        if (section.equals("word")) ui.add(page, ui.button("← " + (detailOrigin.equals("dictionary") ? "Dicionário" : "Guardados"), false, this::back));
        else if (section.equals("saved")) ui.add(page, ui.button("← Dicionário", false, this::back));
        else if (section.equals("settings") || section.equals("sources")) ui.add(page, ui.button("← Dicionário", false, () -> go("dictionary")));
        switch (section) {
            case "kana": kana(); break;
            case "reference": reference(); break;
            case "dictionary": dictionary(); break;
            case "saved": saved(); break;
            case "word": dictionaryDetail(); break;
            case "settings": settings(); break;
            case "sources": sources(); break;
            case "currency": currency(); break;
            default: converter();
        }
        navigation.removeAllViews();
        String[] destinations = {"dictionary", "converter", "kana", "reference", "currency"};
        String[] names = {"Dicionário", "Converter", "Kana", "Referência", "Iene"};
        int[] ids = {112, 110, 111, 113, 114};
        boolean stacked = getResources().getConfiguration().screenWidthDp < 520 || getResources().getConfiguration().fontScale > 1.3f;
        LinearLayout row = null;
        for (int i = 0; i < destinations.length; i++) {
            if (i == 0 || (stacked && i == 3)) { row = ui.row(); navigation.addView(row, ui.wrap()); }
            String target = destinations[i];
            boolean selected = section.equals(target) || (target.equals("dictionary") && (section.equals("saved") || section.equals("word")));
            Button button = ui.button(names[i], selected, () -> go(target)); button.setId(ids[i]);
            ui.paint(button, selected); weighted(row, button);
        }
        page.requestFocus();
        if (android.os.Build.VERSION.SDK_INT >= 33 && backCallback != null) {
            if (backRegistered) { getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(backCallback); backRegistered = false; }
            if (!section.equals("converter")) { getOnBackInvokedDispatcher().registerOnBackInvokedCallback(0, backCallback); backRegistered = true; }
        }
    }
    private void menu() {
        String[] labels = {"Dicionário offline", "Guardados", "Referência de japonês (opcional)", "Euro → Iene", "Definições", "Fontes e licenças"};
        String[] routes = {"dictionary", "saved", "reference", "currency", "settings", "sources"};
        new AlertDialog.Builder(this).setTitle("Consulta e opções").setItems(labels, (d,w) -> go(routes[w])).setNegativeButton("Fechar", null).show();
    }
    private void title(String value) { ui.add(page, ui.heading(value)); }
    private TextView label(String value) { return ui.text(value, Ui.CAPTION_SIZE, ui.muted, false); }
    private void weighted(LinearLayout row, View view) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1); lp.setMargins(ui.dp(2), ui.dp(2), ui.dp(2), ui.dp(2)); row.addView(view, lp);
    }
    private EditText field(String text, String hint, int id, boolean multi) {
        EditText field = new EditText(this); field.setId(id); field.setTextColor(ui.ink); field.setHintTextColor(ui.muted); field.setTextSize(20);
        field.setPadding(ui.dp(Ui.SPACE_LARGE), ui.dp(12), ui.dp(Ui.SPACE_LARGE), ui.dp(12));
        field.setBackground(ui.controlBackground(ui.surface, ui.line, false));
        field.setMinimumHeight(ui.dp(Ui.CONTROL_HEIGHT));
        if (android.os.Build.VERSION.SDK_INT >= 29 && field.getTextCursorDrawable() != null) field.getTextCursorDrawable().setTint(ui.accent);
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
        converterTitle = ui.heading("Converter"); ui.add(page, converterTitle);
        converterDescription = label("Escreve rōmaji ou cola japonês. Copia o resultado que precisas."); ui.add(page, converterDescription);
        EditText input = field(draft, "Rōmaji ou japonês", 201, true); converterInput = input; ui.add(page, input);
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
                    if (outputMode == mode) return;
                    outputMode = mode; for (int i = 0; i < 4; i++) ui.paint(outputButtons[i], i == mode); updateResults();
                }); button.setId(220 + mode); ui.paint(button, mode == outputMode); outputButtons[mode] = button; weighted(choices, button);
            }
            page.addView(choices, ui.space(4));
        }
        converterTools = tools;
        results = ui.column(); ui.add(page, results); updateResults(); fitConverterToKeyboard();
        watch(input, value -> {
            // Android restores EditText after onCreate; an identical value must not clear lexical choices.
            if (value.equals(draft)) return;
            draft = value; selectedKanji = ""; chosenReading = ""; composition.clear(); generation++;
            later(() -> { updateResults(); revealCursor(); });
        });
    }
    private void output(LinearLayout parent, String name, String value, boolean speak) {
        LinearLayout card = ui.card(); card.addView(label(name), ui.wrap());
        TextView text = ui.text(value, Ui.JAPANESE_SIZE, ui.ink, false); text.setTextIsSelectable(true); card.addView(text, ui.space(Ui.SPACE));
        LinearLayout actions = ui.row(); weighted(actions, ui.button("Copiar", false, () -> copy(value)));
        if (speak) weighted(actions, ui.button("Ouvir", false, () -> speech.speak(value, false)));
        card.addView(actions, ui.space(Ui.SPACE)); ui.add(parent, card);
    }
    private void updateResults() {
        final int request = ++generation;
        if (draft.trim().isEmpty()) {
            if (simpleResult == null && results.getTag() == Boolean.TRUE) return;
            simpleResult = null; results.removeAllViews(); results.setTag(Boolean.TRUE);
            ui.add(results, label("Ex.: irasshaimase → いらっしゃいませ\ngakkou → がっこう · nihongo → にほんご → 日本語"));
            ui.add(results, ui.button("Como escrever sons especiais", false, this::spellingHelp)); return;
        }
        RomajiConverter.Result converted = RomajiConverter.convert(draft);
        String hira = chosenReading.isEmpty() ? converted.hiragana : chosenReading;
        KanaTransliterator.Result reading = KanaTransliterator.transliterate(hira, macrons ? KanaTransliterator.RomanizationStyle.MACRON : KanaTransliterator.RomanizationStyle.SIMPLE);
        boolean partial = converted.partial || reading.partial || hasKanji(hira);
        if (outputMode != 2) {
            updateSimpleResult(outputMode == 0 ? "Hiragana" : outputMode == 1 ? "Katakana" : "Rōmaji",
                outputMode == 0 ? hira : outputMode == 1 ? KanaData.toKatakana(hira) : reading.romaji, outputMode != 3, partial);
            return;
        }
        simpleResult = null; results.setTag(null); results.removeAllViews();
        if (partial) ui.add(results, label(partialMessage()));
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
    private String partialMessage() {
        return "Conversão parcial: o texto sem leitura reconhecida foi mantido. Para um kanji, abre Kanji e escolhe uma leitura. Frases com kanji não têm leitura automática completa.";
    }
    private void updateSimpleResult(String name, String value, boolean speak, boolean partial) {
        if (simpleResult == null) {
            results.removeAllViews(); results.setTag(null);
            simpleResult = ui.card(); resultTitle = label(name); simpleResult.addView(resultTitle, ui.wrap());
            resultText = ui.text(value, Ui.JAPANESE_SIZE, ui.ink, false); resultText.setId(205);
            resultText.setTextIsSelectable(true); simpleResult.addView(resultText, ui.space(Ui.SPACE));
            LinearLayout actions = ui.row();
            weighted(actions, ui.button("Copiar", false, () -> copy(resultText.getText().toString())));
            resultSpeak = ui.button("Ouvir", false, () -> speech.speak(resultText.getText().toString(), false)); weighted(actions, resultSpeak);
            simpleResult.addView(actions, ui.space(Ui.SPACE)); ui.add(results, simpleResult);
            partialNotice = label(partialMessage());
            partialNotice.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE); ui.add(results, partialNotice);
            ui.add(results, ui.button("Como escrever sons especiais", false, this::spellingHelp));
        }
        resultTitle.setText(name + (partial ? " · Conversão parcial" : ""));
        // Keep the selectable view and its selection when a conversion has the same output.
        if (!value.contentEquals(resultText.getText())) resultText.setText(value);
        resultSpeak.setVisibility(speak ? View.VISIBLE : View.GONE);
        partialNotice.setVisibility(partial ? View.VISIBLE : View.GONE);
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
            if (error != null) {
                TextView notice = label(error); notice.setTextColor(ui.error);
                notice.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE); ui.add(into, notice); ui.add(into, ui.button("Tentar novamente", false, () -> loadCandidates(into, value, prefix, limit, request, picked))); return; }
            ui.add(into, label(words.isEmpty() ? "Sem palavras correspondentes. Confirma a grafia e as vogais longas; nem todas as palavras usam kanji." : (section.equals("dictionary") ? "Resultados · toca numa palavra para ver os detalhes" : "Grafias e leituras · significados em inglês\nMantém premido para ver os detalhes completos.")));
            for (OfflineLexicon.Word word : words) {
                Button button = ui.button(word.word + "  ·  " + word.originalReading + "\n" + word.meaning, false, () -> picked.pick(word));
                button.setMaxLines(4); button.setEllipsize(android.text.TextUtils.TruncateAt.END);
                button.setContentDescription(word.word + " · " + word.reading + ". " + word.meaning + (section.equals("dictionary") ? ". Abrir detalhes." : ". Mantém premido para ver detalhes completos."));
                if (!section.equals("dictionary")) button.setOnLongClickListener(v -> { wordDetail(word); return true; });
                button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL); ui.add(into, button);
            }
            if (words.size() == limit) ui.add(into, ui.button("Mostrar mais opções", false, () -> { if (section.equals("dictionary")) dictionaryLimit = limit + 30; loadCandidates(into, value, prefix, limit + 30, request, picked); }));
            if (section.equals("dictionary") && dictionaryReturnScroll >= 0) {
                int restore = dictionaryReturnScroll; dictionaryReturnScroll = -1;
                View input = findViewById(203);
                if (input instanceof EditText) {
                    EditText field = (EditText)input; field.setSelection(Math.max(0, Math.min(field.length(), dictionarySelection)));
                    if (dictionaryFocused) field.requestFocus();
                }
                scroll.post(() -> scroll.scrollTo(0, restore));
            }
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
        if (compactKanaHeader()) weighted(scripts, ui.button("Menu", false, this::menu));
        ui.add(page, scripts); if (!wide) ui.add(page, categories);
        EditText search = field(query, "Som ou kana", 202, false);
        search.setContentDescription("Procurar som ou kana: shi, が, kya"); ui.add(page, search);
        table = ui.column(); table.setId(213); page.addView(table, ui.space(8)); renderTable(); watch(search, value -> { query = value; later(this::renderTable); });
    }
    private boolean compactKanaHeader() {
        Configuration config = getResources().getConfiguration();
        return section.equals("kana") && config.orientation == Configuration.ORIENTATION_LANDSCAPE
            && config.screenWidthDp >= 600 && config.fontScale <= 1.3f;
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
        title("Dicionário offline"); ui.add(page, label(dictionaryMode == 1 ? "Pesquisa nas definições em inglês." : "Pesquisa em rōmaji, kana ou kanji. Definições em inglês."));
        String[] names = {"Japonês / rōmaji", "Significado (EN)"};
        LinearLayout languageRow = ui.row();
        for (int i = 0; i < names.length; i++) {
            final int mode = i;
            Button button = ui.button(names[i], dictionaryMode == mode, () -> { if (dictionaryMode == mode) return; dictionaryMode = mode; dictionaryLimit = 30; dictionaryReturnScroll = -1; render(); });
            ui.paint(button, dictionaryMode == mode);
            weighted(languageRow, button);
        } ui.add(page, languageRow);
        ui.add(page, ui.button("Guardados · " + savedWords.size(), false, () -> go("saved")));
        EditText input = field(dictionaryQuery, dictionaryMode == 1 ? "Significado em inglês" : "Pesquisar palavra", 203, false); input.setContentDescription(dictionaryMode == 1 ? "Pesquisar significado em inglês: bridge, school" : "Pesquisar palavra em rōmaji, kana ou kanji: hashi, にほんご, 日本語"); ui.add(page, input); LinearLayout list = ui.column(); ui.add(page, list);
        Runnable refresh = () -> {
            int request = ++generation;
            if (dictionaryQuery.trim().isEmpty()) { list.removeAllViews(); ui.add(list, label("Escreve ou cola uma palavra. Experimenta uma destas pesquisas:"));
                for (String example : (dictionaryMode == 1 ? new String[]{"bridge", "school", "water"} : new String[]{"nihongo", "hashi", "gakkou"})) ui.add(list, ui.button(example, false, () -> { input.setText(example); input.setSelection(input.length()); }));
                List<RecentQueries.Item> recent = RecentQueries.read(prefs.getString("recent_queries", ""));
                if (!recent.isEmpty()) { ui.add(list, label("Consultas recentes · escolhidas por ti")); for (RecentQueries.Item item : recent) ui.add(list, ui.button(item.query + " · " + (item.mode == 1 ? "EN" : "JP"), false, () -> { dictionaryQuery = item.query; dictionaryMode = item.mode; dictionaryLimit = 30; render(); })); }
            }
            else if (dictionaryMode != 0 && dictionaryQuery.trim().length() < 2) { list.removeAllViews(); ui.add(list, label("Escreve pelo menos duas letras da pesquisa.")); }
            else { list.removeAllViews(); dictionaryPage(list, dictionaryQuery, dictionaryMode, 0, dictionaryLimit, request); }
        };
        LinearLayout actions = ui.row();
        weighted(actions, ui.button("Colar", false, () -> {
            ClipboardManager clipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
            if (clipboard.hasPrimaryClip() && clipboard.getPrimaryClip().getItemCount() > 0) {
                CharSequence value = clipboard.getPrimaryClip().getItemAt(0).coerceToText(this);
                if (value != null) { input.setText(value); input.setSelection(input.length()); }
            }
        }));
        weighted(actions, ui.button("Limpar", false, () -> input.setText(""))); page.addView(actions, page.indexOfChild(list));
        watch(input, value -> { dictionaryQuery = value; dictionaryLimit = 30; dictionaryReturnScroll = -1; generation++; later(refresh); }); refresh.run();
    }
    private void dictionaryPage(LinearLayout list, String query, int mode, int offset, int size, int request) {
        TextView status = label("A pesquisar offline…"); ui.add(list, status);
        lexicon.dictionary(query, mode, size + 1, offset, (words, error) -> {
            if (request != generation || !section.equals("dictionary") || isFinishing() || isDestroyed()) return;
            list.removeView(status);
            if (error != null) {
                TextView failure = label(error); failure.setTextColor(ui.error); ui.add(list, failure);
                ui.add(list, ui.button("Tentar novamente", false, () -> { list.removeAllViews(); dictionaryPage(list, query, mode, 0, dictionaryLimit, request); })); return;
            }
            int count = Math.min(size, words.size());
            if (offset == 0 && count == 0 && mode == 0 && !JapaneseReference.inflected(query).isEmpty()) {
                TextView checking = label("A verificar possíveis formas verbais…"); ui.add(list, checking);
                lexicon.inflected(JapaneseReference.inflected(query), (candidates, failure) -> {
                    if (request != generation || !section.equals("dictionary")) return;
                    list.removeView(checking);
                    ui.add(list, label(failure != null ? failure : candidates.isEmpty() ? "Sem resultados para estas formas. Experimenta a forma de dicionário." : "Possíveis formas de · verbos da Referência. Confirma o sentido e o contexto."));
                    if (failure != null) ui.add(list, ui.button("Tentar novamente", false, () -> { list.removeAllViews(); dictionaryPage(list, query, mode, 0, size, request); }));
                    for (OfflineLexicon.Word candidate : candidates) ui.add(list, ui.button("Possível forma de " + candidate.word + " · " + candidate.originalReading + "\n" + candidate.meaning, false, () -> openDictionaryDetail(candidate)));
                }); return;
            }
            if (offset == 0) ui.add(list, label(count == 0 ? "Sem resultados. Confirma a grafia ou experimenta outro modo de pesquisa." : "Resultados · toca numa palavra para ver os detalhes"));
            String kana = KanaData.toHiragana(RomajiConverter.convert(query.trim()).hiragana);
            for (int i = 0; i < count; i++) {
                OfflineLexicon.Word word = words.get(i);
                boolean exact = word.word.equals(query.trim()) || word.reading.equals(kana);
                Button card = ui.button(word.word + "  ·  " + word.originalReading + "\n" + word.meaning + (mode == 0 && !exact ? "\nComeça por esta pesquisa" : ""), false, () -> openDictionaryDetail(word));
                card.setGravity(Gravity.START | Gravity.CENTER_VERTICAL); card.setMaxLines(5); card.setEllipsize(android.text.TextUtils.TruncateAt.END);
                card.setContentDescription(word.word + " · " + word.originalReading + ". " + word.meaning + ". Abrir detalhes."); ui.add(list, card);
            }
            dictionaryLimit = Math.max(30, offset + count);
            if (words.size() > size) {
                Button more = ui.button("Mostrar mais resultados", false, () -> {});
                more.setOnClickListener(v -> { list.removeView(more); dictionaryPage(list, query, mode, offset + count, 30, request); }); ui.add(list, more);
            }
            if (dictionaryReturnScroll >= 0) {
                int restore = dictionaryReturnScroll; dictionaryReturnScroll = -1;
                View input = findViewById(203);
                if (input instanceof EditText) {
                    EditText field = (EditText)input; field.setSelection(Math.max(0, Math.min(field.length(), dictionarySelection)));
                    if (dictionaryFocused) field.requestFocus();
                }
                scroll.post(() -> scroll.scrollTo(0, restore));
            }
        });
    }
    private void openDictionaryDetail(OfflineLexicon.Word word) {
        if (section.equals("dictionary")) prefs.edit().putString("recent_queries", RecentQueries.add(prefs.getString("recent_queries", ""), dictionaryQuery, dictionaryMode)).apply();
        detailWord = word; detailOrigin = section; detailReturnScroll = scroll.getScrollY();
        View input = findViewById(203);
        if (input instanceof EditText) { dictionarySelection = ((EditText)input).getSelectionEnd(); dictionaryFocused = input.hasFocus(); }
        go("word");
    }
    private void dictionaryDetail() {
        title(detailWord.word);
        TextView reading = ui.text(detailWord.originalReading + " · " + KanaTransliterator.transliterate(detailWord.reading).romaji, 20, ui.ink, false);
        reading.setTextIsSelectable(true); ui.add(page, reading);
        ui.add(page, label("Definição em inglês · JMdict"));
        ui.add(page, saveButton(detailWord));
        LinearLayout senses = ui.column(); ui.add(page, senses); loadWordSenses(senses, detailWord, generation);
        ui.add(page, ui.button("Copiar palavra", false, () -> copy(detailWord.word)));
        ui.add(page, ui.button("Copiar leitura", false, () -> copy(detailWord.originalReading)));
        ui.add(page, ui.button("Ouvir leitura", false, () -> speech.speak(detailWord.reading, false)));
        ui.add(page, ui.button("Usar no conversor", true, () -> {
            draft = detailWord.reading; selectedKanji = detailWord.word; chosenReading = detailWord.reading; composition.clear(); go("converter");
        }));
    }
    private void loadWordSenses(LinearLayout target, OfflineLexicon.Word word, int request) {
        target.removeAllViews(); ui.add(target, label("A carregar sentidos…"));
        lexicon.details(word, (senses, error) -> {
            if (request != generation || !section.equals("word") || isFinishing() || isDestroyed()) return;
            target.removeAllViews();
            if (error != null) {
                TextView notice = label(error); notice.setTextColor(ui.error); ui.add(target, notice);
                ui.add(target, ui.button("Tentar novamente", false, () -> loadWordSenses(target, word, request))); return;
            }
            if (senses.isEmpty()) { if (detailOrigin.equals("saved")) ui.add(target, label("Esta entrada já não consta da base atual. O resumo guardado foi conservado.")); TextView text = ui.text(word.meaning, Ui.BODY_SIZE, ui.ink, false); text.setTextIsSelectable(true); ui.add(target, text); }
            for (int i = 0; i < senses.size(); i++) {
                org.json.JSONObject sense = senses.get(i); LinearLayout card = ui.card();
                ui.add(card, ui.text("Sentido " + (i + 1), 18, ui.ink, true));
                for (String tag : new String[]{"pos", "field", "misc", "dial"}) {
                    String value = tag.equals("pos") ? grammarLabels(sense.optJSONArray(tag)) : jsonList(sense.optJSONArray(tag));
                    if (!value.isEmpty()) ui.add(card, label((tag.equals("pos") ? "Classe: " : tag.equals("field") ? "Área: " : tag.equals("dial") ? "Dialeto: " : "Uso: ") + value));
                }
                TextView definition = ui.text(jsonList(sense.optJSONArray("glosses")), Ui.BODY_SIZE, ui.ink, false); definition.setTextIsSelectable(true); ui.add(card, definition);
                for (String tag : new String[]{"s_inf", "xref", "ant"}) {
                    String value = jsonList(sense.optJSONArray(tag)); if (!value.isEmpty()) ui.add(card, label((tag.equals("xref") ? "Ver também: " : tag.equals("ant") ? "Contraste: " : "Nota: ") + value));
                }
                ui.add(target, card);
            }
        });
    }
    private String grammarLabels(org.json.JSONArray values) {
        StringBuilder text = new StringBuilder();
        if (values != null) for (int i = 0; i < values.length(); i++) {
            String value = values.optString(i);
            if (value.equals("Ichidan verb")) value = "Verbo ichidan";
            else if (value.startsWith("Godan verb")) value = "Verbo godan";
            else if (value.equals("transitive verb")) value = "Transitivo";
            else if (value.equals("intransitive verb")) value = "Intransitivo";
            else if (value.equals("noun (common) (futsuumeishi)")) value = "Nome";
            else if (value.equals("adjective (keiyoushi)")) value = "Adjetivo em い";
            else if (value.startsWith("adjectival nouns")) value = "Adjetivo em な";
            else if (value.equals("adverb (fukushi)")) value = "Advérbio";
            else if (value.equals("particle")) value = "Partícula";
            if (i > 0) text.append("; "); text.append(value);
        }
        return text.toString();
    }
    private String jsonList(org.json.JSONArray values) {
        StringBuilder text = new StringBuilder(); if (values != null) for (int i = 0; i < values.length(); i++) { if (i > 0) text.append("; "); text.append(values.optString(i)); } return text.toString();
    }
    private void wordDetail(OfflineLexicon.Word word) {
        LinearLayout body = ui.card(); body.addView(ui.text(word.word, 30, ui.ink, true));
        ui.add(body, label(word.reading + " · " + KanaTransliterator.transliterate(word.reading).romaji + "\n\n" + word.meaning));
        ui.add(body, ui.button("Ouvir leitura", false, () -> speech.speak(word.reading, false))); ui.add(body, ui.button("Copiar", false, () -> copy(word.word)));
        ui.add(body, saveButton(word));
        ScrollView view = new ScrollView(this); view.addView(body); AlertDialog dialog = new AlertDialog.Builder(this).setView(view).setNegativeButton("Fechar", null).create();
        ui.add(body, ui.button("Usar no conversor", true, () -> { draft = word.reading; selectedKanji = word.word; chosenReading = word.reading; composition.clear(); dialog.dismiss(); go("converter"); })); dialog.show();
    }
    private SavedEntry savedEntry(OfflineLexicon.Word word) { return new SavedEntry(word.entry, word.word, word.reading, word.meaning, word.originalReading); }
    private Button saveButton(OfflineLexicon.Word word) {
        SavedEntry entry = savedEntry(word); boolean legacy = savedWords.legacy().contains(word.word);
        Button button = ui.button(savedWords.contains(entry) ? "Remover dos guardados" : legacy ? "Guardar esta entrada (guardado antigo)" : "Guardar palavra", false, () -> {});
        button.setEnabled(word.entry > 0);
        button.setOnClickListener(view -> {
            if (savedWords.legacy().contains(word.word) && !savedWords.contains(entry)) savedWords.migrate(word.word, entry); else savedWords.toggle(entry);
            button.setText(savedWords.contains(entry) ? "Remover dos guardados" : "Guardar palavra");
        }); return button;
    }
    private void saved() {
        title("Guardados");
        EditText filter = field(savedQuery, "Pesquisar guardados", 205, false); ui.add(page, filter);
        LinearLayout list = ui.column(); ui.add(page, list); watch(filter, value -> { savedQuery = value; later(() -> savedList(list)); }); savedList(list);
    }
    private void savedList(LinearLayout list) {
        list.removeAllViews(); String key = KanaTransliterator.searchKey(savedQuery);
        if (savedWords.size() == 0) { ui.add(list, label("Ainda não tens palavras guardadas. Abre uma palavra no dicionário e toca em Guardar.")); ui.add(list, ui.button("Abrir dicionário", false, () -> go("dictionary"))); return; }
        int shown = 0;
        for (SavedEntry entry : savedWords.entries()) if (KanaTransliterator.searchKey(entry.word + " " + entry.originalReading + " " + entry.meaning).contains(key)) {
            ui.add(list, ui.button(entry.word + " · " + entry.originalReading + "\n" + entry.meaning, false, () -> { detailWord = new OfflineLexicon.Word(entry.word, entry.reading, entry.meaning, entry.entry, entry.originalReading); detailOrigin = "saved"; detailReturnScroll = scroll.getScrollY(); go("word"); })); shown++;
        }
        for (String legacy : savedWords.legacy()) if (KanaTransliterator.searchKey(legacy).contains(key)) {
            ui.add(list, ui.button(legacy + " · guardado antigo: escolher entrada", false, () -> {
                int request = generation;
                lexicon.resolveLegacy(legacy, (words, error) -> {
                    if (request != generation || !section.equals("saved")) return;
                    if (error != null) { message("Guardados", error); return; }
                    if (words.size() == 1) { OfflineLexicon.Word word = words.get(0); savedWords.migrate(legacy, savedEntry(word)); detailWord = word; detailOrigin = "saved"; detailReturnScroll = scroll.getScrollY(); go("word"); }
                    else { dictionaryQuery = legacy; dictionaryMode = 0; dictionaryLimit = 30; go("dictionary"); message("Guardado antigo", "A grafia antiga não identifica uma única entrada. Escolhe a palavra e a leitura certas, e toca em Guardar esta entrada. O guardado antigo será conservado até essa escolha."); }
                });
            })); shown++;
        }
        if (shown == 0) ui.add(list, label("Sem guardados para esta pesquisa."));
    }
    private void reference() {
        title("Referência de japonês"); if (!JapaneseReference.loaded() && referenceError == null) { ui.add(page, label("A carregar Referência…")); return; } if (referenceError != null) { ui.add(page, label(referenceError)); ui.add(page, ui.button("Tentar novamente", false, () -> recreate())); return; } ui.add(page, label("Consulta opcional: " + JapaneseReference.all().size() + " entradas em português. Frases, partículas, verbos e termos; sem exercícios."));
        ui.add(page, ui.button(referenceCategory + " ▾", false, () -> {
            String[] categories = {"Tudo", "Fundamentos", "Partículas", "Verbos e formas", "Adjetivos", "Números e tempo", "Pedidos e situações", "Viagem e dia a dia", "Compras e refeições", "Comunicação e registo", "Frases", "Verbos", "Termos", "Expressões"};
            new AlertDialog.Builder(this).setTitle("Consultar").setItems(categories, (d,w) -> { referenceCategory = categories[w]; go("reference"); }).setNegativeButton("Fechar", null).show();
        }));
        EditText input = field(referenceQuery, "Pesquisar", 204, false);
        input.setContentDescription("Procurar em português, rōmaji ou japonês"); ui.add(page, input); LinearLayout list = ui.column(); ui.add(page, list);
        watch(input, value -> { referenceQuery = value; later(() -> referenceList(list, 30)); }); referenceList(list, 30);
    }
    private void referenceList(LinearLayout list, int limit) {
        list.removeAllViews(); List<JapaneseReference.Entry> entries = JapaneseReference.search(referenceCategory, referenceQuery); ui.add(list, label((entries.size() == 0 ? "Sem resultados. Experimenta outra palavra ou categoria." : entries.size() + " resultados · " + referenceCategory)));
        if (!referenceQuery.trim().isEmpty() && !referenceCategory.equals("Tudo") && JapaneseReference.search("Tudo", referenceQuery).size() > entries.size()) {
            ui.add(list, ui.button("Pesquisar em todos os temas", false, () -> { referenceCategory = "Tudo"; render(); }));
        }
        for (int i = 0; i < Math.min(entries.size(), limit); i++) {
            JapaneseReference.Entry entry = entries.get(i);
            Button button = ui.button(entry.meaning + "\n" + entry.japanese + " · " + entry.romaji(), false, () -> {
                LinearLayout body = ui.card(); body.addView(ui.text(entry.japanese, 25, ui.ink, true));
                ui.add(body, label(entry.topic + "\n\n" + entry.reading + "\n" + entry.romaji() + "\n\n" + entry.meaning + (entry.detail.isEmpty() ? "" : "\n\n" + entry.detail) + (entry.example.isEmpty() ? "" : "\n\nOutro exemplo\n" + entry.example)));
                ui.add(body, ui.button("Ouvir", false, () -> speech.speak(entry.japanese, false))); ui.add(body, ui.button("Ouvir devagar", false, () -> speech.speak(entry.japanese, true)));
                ui.add(body, ui.button("Copiar japonês", false, () -> copy(entry.japanese))); ScrollView view = new ScrollView(this); view.addView(body);
                new AlertDialog.Builder(this).setView(view).setNegativeButton("Fechar", null).show();
            }); button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL); ui.add(list, button);
        }
        if (entries.size() > limit) ui.add(list, ui.button("Mostrar mais (" + (entries.size() - limit) + ")", false, () -> referenceList(list, limit + 30)));
    }
    private void settings() {
        title("Definições");
        String start = prefs.getString("startSection", "dictionary");
        String[] startRoutes = {"dictionary", "converter", "kana", "reference", "currency"};
        String[] startNames = {"Dicionário", "Converter", "Kana", "Referência", "Iene"};
        int startIndex = java.util.Arrays.asList(startRoutes).indexOf(start);
        ui.add(page, ui.button("Ecrã inicial: " + startNames[Math.max(0, startIndex)], false, () -> {
            new AlertDialog.Builder(this).setTitle("Ecrã inicial").setItems(startNames, (d,w) -> {
                prefs.edit().putString("startSection", startRoutes[w]).putString("section", startRoutes[w]).apply(); render();
            }).show();
        }));
        String theme = prefs.getString("theme", "system");
        String themeName = "dark".equals(theme) ? "Escuro" : "light".equals(theme) ? "Claro" : "Sistema";
        ui.add(page, ui.button("Aspeto: " + themeName, false, () -> {
            String[] values = {"system", "light", "dark"}; new AlertDialog.Builder(this).setTitle("Aspeto").setItems(new String[]{"Seguir o sistema", "Claro", "Escuro"}, (d,w) -> { prefs.edit().putString("theme", values[w]).apply(); recreate(); }).show();
        }));
        ui.add(page, ui.button("Rōmaji: " + (macrons ? "com mácrones (ō, ū)" : "vogais por extenso (ou, uu)"), false, () -> { macrons = !macrons; prefs.edit().putBoolean("macrons", macrons).apply(); render(); }));
        ui.add(page, ui.button("Limpar consultas recentes", false, () -> { prefs.edit().remove("recent_queries").apply(); message("Consultas recentes", "Histórico local limpo."); }));
        ui.add(page, ui.button("Testar voz japonesa", false, () -> speech.speak("こんにちは", false)));
        ui.add(page, label("O tamanho do texto acompanha as definições de acessibilidade do Android. A tabela adapta-se a texto grande.\n\nKanaBridge 5.2 · Android 6–16 · sem conta. A única ligação à Internet é a do conversor de euros, que pede a taxa do dia (sem enviar dados pessoais); o resto funciona offline. Voz japonesa requer um pacote offline instalado no telemóvel."));
    }
    private void sources() {
        title("Fontes e licenças");
        TextView text = label("Dicionário: JMdict © James William Breen e Electronic Dictionary Research and Development Group (EDRDG). Derivado SQLite sob CC BY-SA 4.0, com leituras normalizadas, restrições de grafia e ordenação pela prioridade da fonte. Significados em inglês.\n\nhttps://www.edrdg.org/\nhttps://www.edrdg.org/edrdg/licence.html\nhttps://creativecommons.org/licenses/by-sa/4.0/\n\nA referência em português foi escrita para esta app. Não constitui um tradutor automático de frases. Nomes próprios, flexões e ambiguidades podem não ter correspondência direta.");
        android.text.util.Linkify.addLinks(text, android.text.util.Linkify.WEB_URLS); text.setLinkTextColor(ui.accent); ui.add(page, text);
        TextView rates = label("Câmbio: taxa de referência do Banco Central Europeu, obtida através do serviço Frankfurter (https://frankfurter.dev). O pedido contém apenas o par EUR/JPY. A última taxa fica guardada no telemóvel para uso sem ligação.");
        android.text.util.Linkify.addLinks(rates, android.text.util.Linkify.WEB_URLS); rates.setLinkTextColor(ui.accent); ui.add(page, rates);
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
    // ---- Euro → iene -------------------------------------------------------------------------
    private void loadRate() {
        String stored = prefs.getString("rate_value", ""); String date = prefs.getString("rate_date", "");
        try { if (prefs.getString("rate_source", "").equals(CurrencyConverter.ENDPOINT) && !stored.isEmpty()) {
            CurrencyConverter.Rate cached = new CurrencyConverter.Rate(Double.parseDouble(stored), date);
            rate = CurrencyConverter.usable(cached) ? cached : null;
        } } catch (NumberFormatException ignored) { rate = null; }
        rateFetched = prefs.getLong("rate_fetched", 0);
        currencyAmount = prefs.getString("currency_amount", ""); yenToEuro = prefs.getBoolean("currency_reverse", false);
    }
    private void currency() {
        title(yenToEuro ? "Iene → Euro" : "Euro → Iene");
        ui.add(page, label("Taxa de referência do Banco Central Europeu, atualizada nos dias úteis. O resultado muda enquanto escreves."));
        LinearLayout directions = ui.row();
        Button forward = ui.button("€ → ¥", !yenToEuro, () -> setCurrencyDirection(false)); ui.paint(forward, !yenToEuro); forward.setId(230); weighted(directions, forward);
        Button reverse = ui.button("¥ → €", yenToEuro, () -> setCurrencyDirection(true)); ui.paint(reverse, yenToEuro); reverse.setId(231); weighted(directions, reverse);
        ui.add(page, directions);
        EditText input = field(currencyAmount, yenToEuro ? "Montante em ienes (¥)" : "Montante em euros (€)", 205, false);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
        ui.add(page, input);
        LinearLayout card = ui.card();
        currencyResult = ui.text("", 32, ui.accent, true); currencyResult.setId(232); currencyResult.setTextIsSelectable(true); card.addView(currencyResult);
        currencyRate = label(""); ui.add(card, currencyRate);
        currencyStatus = label(""); ui.add(card, currencyStatus);
        ui.add(page, card);
        ui.add(page, ui.button("Copiar resultado", false, () -> { String value = currencyOutput(); if (value.isEmpty()) message("Câmbio", "Escreve primeiro um montante."); else copy(value); }));
        ui.add(page, ui.button("Atualizar taxa", false, () -> refreshRate(true)));
        watch(input, value -> { currencyAmount = value; prefs.edit().putString("currency_amount", value).apply(); updateCurrency(); });
        updateCurrency(); refreshRate(false);
    }
    private void setCurrencyDirection(boolean reverse) {
        if (yenToEuro == reverse) return;
        yenToEuro = reverse; prefs.edit().putBoolean("currency_reverse", reverse).apply(); render();
    }
    /** The converted figure alone, e.g. "17 746 ¥", or an empty string when it cannot be computed. */
    private String currencyOutput() {
        double amount = CurrencyConverter.parseAmount(currencyAmount);
        if (rate == null || amount < 0) return "";
        return yenToEuro ? CurrencyConverter.formatEuro(CurrencyConverter.toEuro(amount, rate)) + " €" : CurrencyConverter.formatYen(CurrencyConverter.toYen(amount, rate)) + " ¥";
    }
    private void updateCurrency() {
        if (currencyResult == null) return;
        String output = currencyOutput();
        currencyResult.setText(output.isEmpty() ? "—" : output);
        currencyRate.setText(rate == null ? "Ainda sem taxa." : "1 € = " + CurrencyConverter.formatRate(rate.yenPerEuro) + " ¥ · taxa de " + CurrencyConverter.formatDate(rate.date));
        double amount = CurrencyConverter.parseAmount(currencyAmount);
        String status = rateStatus;
        if (status.isEmpty() && rate != null && amount < 0 && !currencyAmount.trim().isEmpty()) status = "Montante não reconhecido. Usa só números, por exemplo 1250 ou 12,50.";
        else if (status.isEmpty() && rate != null && currencyAmount.trim().isEmpty()) status = "Escreve um montante para converter.";
        currencyStatus.setText(status);
        currencyResult.setContentDescription(output.isEmpty() ? "Sem resultado" : "Resultado: " + output);
    }
    /** Downloads at most one small JSON document; a failure keeps whatever rate was stored before. */
    private void refreshRate(boolean force) {
        if (rateLoading) return;
        if (!force && CurrencyConverter.fresh(rate, rateFetched, System.currentTimeMillis())) { rateStatus = ""; updateCurrency(); return; }
        rateLoading = true; rateStatus = "A atualizar a taxa…"; updateCurrency();
        new Thread(() -> {
            CurrencyConverter.Rate fresh = null;
            try { fresh = CurrencyConverter.fetch(); } catch (Exception ignored) { }
            final CurrencyConverter.Rate result = fresh;
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                rateLoading = false;
                if (result != null) {
                    rate = result; rateFetched = System.currentTimeMillis();
                    prefs.edit().putString("rate_source", CurrencyConverter.ENDPOINT).putString("rate_value", Double.toString(result.yenPerEuro)).putString("rate_date", result.date).putLong("rate_fetched", rateFetched).apply();
                    rateStatus = "Atualizada agora.";
                } else if (rate != null) rateStatus = "Sem ligação. A usar a taxa guardada de " + CurrencyConverter.formatDate(rate.date) + ".";
                else rateStatus = "Sem taxa guardada. Liga-te à Internet uma vez para obter a primeira.";
                updateCurrency();
            });
        }, "kanabridge-rate").start();
    }
    private void message(String title, String text) { new AlertDialog.Builder(this).setTitle(title).setMessage(text).setPositiveButton("Fechar", null).show(); }
    private void copy(String value) { ((ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("KanaBridge", value)); if (android.os.Build.VERSION.SDK_INT < 33) android.widget.Toast.makeText(this, "Copiado", android.widget.Toast.LENGTH_SHORT).show(); }
    @Override protected void onSaveInstanceState(Bundle state) {
        if (converterInput != null) {
            state.putBoolean("inputFocused", converterInput.hasFocus());
            state.putInt("inputStart", converterInput.getSelectionStart()); state.putInt("inputEnd", converterInput.getSelectionEnd());
            state.putInt("inputScroll", converterInput.getScrollY());
        }
        View dictionaryInput = findViewById(203);
        if (dictionaryInput instanceof EditText) { dictionarySelection = ((EditText)dictionaryInput).getSelectionEnd(); dictionaryFocused = dictionaryInput.hasFocus(); }
        state.putInt("dictionaryMode", dictionaryMode); state.putString("savedQuery", savedQuery); state.putInt("dictionaryLimit", dictionaryLimit); state.putString("detailOrigin", detailOrigin);
        state.putInt("detailReturnScroll", detailReturnScroll); state.putInt("dictionarySelection", dictionarySelection); state.putBoolean("dictionaryFocused", dictionaryFocused);
        if (detailWord != null) { state.putString("detailWord", detailWord.word); state.putString("detailReading", detailWord.reading); state.putString("detailMeaning", detailWord.meaning); state.putLong("detailEntry", detailWord.entry); state.putString("detailOriginalReading", detailWord.originalReading); }
        super.onSaveInstanceState(state); state.putString("section", section); state.putString("draft", draft); state.putString("query", query); state.putString("category", category); state.putBoolean("katakana", katakana);
        state.putString("referenceCategory", referenceCategory); state.putString("referenceQuery", referenceQuery); state.putString("dictionaryQuery", dictionaryQuery); state.putString("chosenReading", chosenReading); state.putString("selectedKanji", selectedKanji); state.putInt("scroll", scroll.getScrollY()); state.putInt("outputMode", outputMode);
        ArrayList<String> choices = new ArrayList<>(); int count = RomajiConverter.convert(draft).hiragana.trim().split("\\s+").length;
        for (int i = 0; i < count; i++) choices.add(composition.containsKey(i) ? composition.get(i) : ""); state.putStringArrayList("composition", choices);
    }
    @Override protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        if (converterInput != null && state.containsKey("inputStart")) {
            int length = converterInput.length();
            converterInput.setSelection(Math.max(0, Math.min(length, state.getInt("inputStart"))), Math.max(0, Math.min(length, state.getInt("inputEnd"))));
            if (state.getBoolean("inputFocused")) converterInput.requestFocus();
            EditText restoredInput = converterInput;
            restoredInput.post(() -> restoredInput.scrollTo(0, state.getInt("inputScroll")));
        }
        scroll.post(() -> scroll.scrollTo(0, state.getInt("scroll")));
    }
    @Override protected void onStop() { prefs.edit().putString("draft", draft).putString("section", prefs.getString("startSection", "dictionary")).apply(); speech.stop(); super.onStop(); }
    @Override protected void onDestroy() { if (pending != null) handler.removeCallbacks(pending); speech.close(); lexicon.close(); super.onDestroy(); }
}
