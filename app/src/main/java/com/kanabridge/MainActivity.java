package com.kanabridge;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MainActivity extends Activity {
    private static final int INK = Color.rgb(25, 32, 42);
    private static final int MUTED = Color.rgb(95, 107, 122);
    private static final int CORAL = Color.rgb(230, 95, 77);
    private static final int CREAM = Color.rgb(250, 247, 241);
    private static final int WHITE = Color.WHITE;
    private static final int LINE = Color.rgb(230, 228, 222);
    private static final int MINT = Color.rgb(230, 244, 238);
    private static final int SOFT = Color.rgb(244, 239, 229);

    private static final String SECTION_CONVERTER = "Converter";
    private static final String SECTION_TABLE = "Tabela";
    private static final String SECTION_DICTIONARY = "Dicionário";
    private static final String SECTION_ABOUT = "Sobre";

    private ScrollView pageScroll;
    private LinearLayout root;
    private LinearLayout content;
    private LinearLayout results;
    private EditText input;
    private final List<Button> navigationButtons = new ArrayList<>();

    private String converterText = "";
    private boolean useMacrons;

    private boolean tableKatakana;
    private String tableCategory = KanaData.GOJUON;
    private EditText tableSearch;
    private LinearLayout tableBody;
    private Button hiraganaToggle;
    private Button katakanaToggle;
    private final List<Button> categoryButtons = new ArrayList<>();

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureSystemBars();

        pageScroll = new ScrollView(this);
        pageScroll.setFillViewport(true);
        pageScroll.setBackgroundColor(CREAM);

        root = column();
        root.setPadding(dp(20), dp(18), dp(20), dp(36));
        pageScroll.addView(root, matchWrap());
        pageScroll.setOnApplyWindowInsetsListener((view, insets) -> {
            root.setPadding(
                dp(20), dp(18) + insets.getSystemWindowInsetTop(),
                dp(20), dp(36) + insets.getSystemWindowInsetBottom()
            );
            return insets;
        });

        setContentView(pageScroll);
        pageScroll.requestApplyInsets();
        buildChrome();
        selectSection(SECTION_CONVERTER);
    }

    private void configureSystemBars() {
        Window window = getWindow();
        window.setStatusBarColor(CREAM);
        window.setNavigationBarColor(CREAM);
        int systemUi = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            systemUi |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        window.getDecorView().setSystemUiVisibility(systemUi);
    }

    private void buildChrome() {
        LinearLayout brand = row();
        TextView mark = text("あ", 24, WHITE, Typeface.BOLD);
        mark.setGravity(Gravity.CENTER);
        mark.setBackground(round(CORAL, 14, CORAL));
        brand.addView(mark, new LinearLayout.LayoutParams(dp(48), dp(48)));

        LinearLayout titles = column();
        titles.setPadding(dp(12), 0, 0, 0);
        titles.addView(text("KanaBridge", 22, INK, Typeface.BOLD));
        titles.addView(text("Japonês sem adivinhações", 13, MUTED, Typeface.NORMAL));
        brand.addView(titles, new LinearLayout.LayoutParams(0, dp(52), 1));
        root.addView(brand, matchWrap());

        HorizontalScrollView navigationScroll = new HorizontalScrollView(this);
        navigationScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout navigation = row();
        addNavigationButton(navigation, SECTION_CONVERTER);
        addNavigationButton(navigation, SECTION_TABLE);
        addNavigationButton(navigation, SECTION_DICTIONARY);
        addNavigationButton(navigation, SECTION_ABOUT);
        navigationScroll.addView(navigation, wrapWrap());
        root.addView(navigationScroll, spaced(0, 20, 0, 18));

        content = column();
        root.addView(content, matchWrap());
    }

    private void addNavigationButton(LinearLayout navigation, String section) {
        Button button = button(section, true);
        button.setTag(section);
        button.setOnClickListener(view -> selectSection(section));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(98), dp(42));
        params.setMarginEnd(dp(8));
        navigation.addView(button, params);
        navigationButtons.add(button);
    }

    private void selectSection(String section) {
        content.removeAllViews();
        for (Button button : navigationButtons) {
            boolean selected = section.equals(button.getTag());
            applyButtonState(button, selected, INK);
        }

        if (SECTION_TABLE.equals(section)) {
            showKanaTable();
        } else if (SECTION_DICTIONARY.equals(section)) {
            showDictionary();
        } else if (SECTION_ABOUT.equals(section)) {
            showAbout();
        } else {
            showConverter();
        }
        pageScroll.post(() -> pageScroll.smoothScrollTo(0, 0));
    }

    private void showConverter() {
        TextView headline = text("Lê hiragana e\nkatakana com confiança.", 31, INK, Typeface.BOLD);
        headline.setLineSpacing(0, 0.96f);
        content.addView(headline, spaced(0, 4, 0, 8));
        content.addView(text(
            "Cola kana abaixo. A leitura em rōmaji aparece imediatamente — tudo offline.",
            15, MUTED, Typeface.NORMAL), spaced(0, 0, 0, 22));

        LinearLayout inputCard = card(WHITE);
        inputCard.addView(label("TEXTO JAPONÊS"));
        input = new EditText(this);
        input.setTextSize(27);
        input.setTextColor(INK);
        input.setHintTextColor(Color.rgb(166, 171, 177));
        input.setHint("Ex.: がっこう ou キャット");
        input.setGravity(Gravity.TOP);
        input.setMinHeight(dp(108));
        input.setPadding(0, dp(10), 0, dp(8));
        input.setBackgroundColor(Color.TRANSPARENT);
        input.setSingleLine(false);
        inputCard.addView(input, matchWrap());

        LinearLayout actions = row();
        Button example = button("Experimentar exemplo", false);
        example.setOnClickListener(view -> setConverterInput("こんにちは"));
        Button clear = button("Limpar", true);
        clear.setOnClickListener(view -> setConverterInput(""));
        actions.addView(example, weightedButton());
        LinearLayout.LayoutParams clearParams = new LinearLayout.LayoutParams(dp(88), dp(46));
        clearParams.setMarginStart(dp(10));
        actions.addView(clear, clearParams);
        inputCard.addView(actions, matchWrap());

        LinearLayout conversionActions = row();
        Button toKatakana = button("Hiragana → Katakana", true);
        toKatakana.setOnClickListener(view -> setConverterInput(
            KanaTransliterator.toKatakana(input.getText().toString())));
        Button toHiragana = button("Katakana → Hiragana", true);
        toHiragana.setOnClickListener(view -> setConverterInput(
            KanaTransliterator.toHiragana(input.getText().toString())));
        conversionActions.addView(toKatakana, weightedButton());
        LinearLayout.LayoutParams secondConversion = weightedButton();
        secondConversion.setMarginStart(dp(8));
        conversionActions.addView(toHiragana, secondConversion);
        inputCard.addView(conversionActions, spaced(0, 10, 0, 0));
        content.addView(inputCard, matchWrap());

        LinearLayout styleCard = card(SOFT);
        styleCard.addView(label("ESTILO DE RŌMAJI"));
        LinearLayout styles = row();
        Button simple = button("Simples · gakkou", true);
        Button macron = button("Macron · gakkō", true);
        simple.setOnClickListener(view -> {
            useMacrons = false;
            applyButtonState(simple, true, CORAL);
            applyButtonState(macron, false, CORAL);
            refreshConverterResult();
        });
        macron.setOnClickListener(view -> {
            useMacrons = true;
            applyButtonState(simple, false, CORAL);
            applyButtonState(macron, true, CORAL);
            refreshConverterResult();
        });
        applyButtonState(simple, !useMacrons, CORAL);
        applyButtonState(macron, useMacrons, CORAL);
        styles.addView(simple, weightedButton());
        LinearLayout.LayoutParams macronParams = weightedButton();
        macronParams.setMarginStart(dp(8));
        styles.addView(macron, macronParams);
        styleCard.addView(styles, spaced(0, 10, 0, 0));
        content.addView(styleCard, spaced(0, 12, 0, 0));

        results = column();
        content.addView(results, spaced(0, 18, 0, 0));
        input.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                converterText = s.toString();
                refreshConverterResult();
            }
            public void afterTextChanged(Editable s) {}
        });
        input.setText(converterText);
        input.setSelection(input.length());
        refreshConverterResult();
    }

    private void setConverterInput(String value) {
        input.setText(value);
        input.setSelection(input.length());
    }

    private void refreshConverterResult() {
        if (results == null) return;
        if (converterText.trim().isEmpty()) {
            showEmptyResult();
        } else {
            showResult(converterText);
        }
    }

    private void showEmptyResult() {
        results.removeAllViews();
        LinearLayout info = card(SOFT);
        info.addView(text("Uma nota rápida", 16, INK, Typeface.BOLD));
        TextView body = text(
            "Kana representa sons. Por isso, あ lê-se “a”, mas não significa uma palavra sozinho. " +
            "Quando reconhecemos uma palavra completa, mostramos também o significado em português e inglês.",
            14, MUTED, Typeface.NORMAL);
        body.setLineSpacing(dp(3), 1f);
        info.addView(body, spaced(0, 8, 0, 0));
        results.addView(info, matchWrap());
    }

    private void showResult(String raw) {
        KanaTransliterator.RomanizationStyle style = useMacrons
            ? KanaTransliterator.RomanizationStyle.MACRON
            : KanaTransliterator.RomanizationStyle.SIMPLE;
        KanaTransliterator.Result parsed = KanaTransliterator.transliterate(raw, style);
        results.removeAllViews();

        LinearLayout reading = card(INK);
        LinearLayout readingHeader = row();
        TextView readingLabel = text("RŌMAJI", 12, Color.rgb(194, 202, 211), Typeface.BOLD);
        readingHeader.addView(readingLabel, new LinearLayout.LayoutParams(0, dp(30), 1));
        Button copy = button("Copiar", false);
        copy.setTextColor(WHITE);
        copy.setBackground(round(Color.rgb(54, 64, 77), 12, Color.rgb(54, 64, 77)));
        copy.setOnClickListener(view -> copyText("Rōmaji", parsed.romaji, "Rōmaji copiado"));
        readingHeader.addView(copy, new LinearLayout.LayoutParams(dp(88), dp(38)));
        reading.addView(readingHeader, matchWrap());
        TextView roma = text(parsed.romaji.isEmpty() ? "—" : parsed.romaji, 30, WHITE, Typeface.BOLD);
        roma.setTextIsSelectable(true);
        reading.addView(roma, spaced(0, 6, 0, 2));
        results.addView(reading, matchWrap());

        if (!parsed.translationPt.isEmpty()) {
            LinearLayout meaning = card(MINT);
            meaning.addView(label("PALAVRA RECONHECIDA · OFFLINE"));
            meaning.addView(languageLine("Português", parsed.translationPt), spaced(0, 12, 0, 6));
            meaning.addView(languageLine("English", parsed.translationEn), matchWrap());
            results.addView(meaning, spaced(0, 12, 0, 0));
        } else {
            LinearLayout noMeaning = card(WHITE);
            noMeaning.addView(text("Tradução da palavra", 16, INK, Typeface.BOLD));
            noMeaning.addView(text(
                "Não está no mini-dicionário offline. A leitura em rōmaji continua correta; " +
                "kana isolado indica som, não significado.", 14, MUTED, Typeface.NORMAL),
                spaced(0, 7, 0, 0));
            results.addView(noMeaning, spaced(0, 12, 0, 0));
        }

        results.addView(label("CARÁCTER A CARÁCTER"), spaced(2, 24, 0, 10));
        for (KanaTransliterator.Token token : parsed.tokens) {
            if (token.kana.trim().isEmpty()) continue;
            results.addView(tokenCard(token), spaced(0, 0, 0, 9));
        }
    }

    private View tokenCard(KanaTransliterator.Token token) {
        LinearLayout resultCard = card(WHITE);
        LinearLayout top = row();
        TextView kana = text(token.kana, 30, INK, Typeface.BOLD);
        kana.setGravity(Gravity.CENTER);
        kana.setBackground(round(Color.rgb(246, 243, 237), 12, Color.rgb(246, 243, 237)));
        top.addView(kana, new LinearLayout.LayoutParams(dp(62), dp(62)));
        LinearLayout details = column();
        details.setPadding(dp(14), 0, 0, 0);
        details.addView(text(token.romaji.isEmpty() ? "—" : token.romaji, 21, CORAL, Typeface.BOLD));
        details.addView(text(token.scriptPt, 13, MUTED, Typeface.NORMAL));
        top.addView(details, new LinearLayout.LayoutParams(0, dp(62), 1));
        resultCard.addView(top, matchWrap());
        resultCard.addView(divider(), spacedHeight(1, 13, 11));
        resultCard.addView(languageLine("PT", token.notePt), matchWrap());
        resultCard.addView(languageLine("EN", token.noteEn), spaced(0, 6, 0, 0));
        return resultCard;
    }

    private void showKanaTable() {
        content.addView(text("Tabela Kana", 31, INK, Typeface.BOLD), spaced(0, 4, 0, 8));
        content.addView(text(
            "Explora os silabários, pesquisa por símbolo ou rōmaji e toca num kana para ver detalhes.",
            15, MUTED, Typeface.NORMAL), spaced(0, 0, 0, 18));

        LinearLayout searchCard = card(WHITE);
        searchCard.addView(label("PESQUISA OFFLINE"));
        tableSearch = new EditText(this);
        tableSearch.setSingleLine(true);
        tableSearch.setTextSize(17);
        tableSearch.setTextColor(INK);
        tableSearch.setHintTextColor(Color.rgb(150, 158, 168));
        tableSearch.setHint("Pesquisar kana ou rōmaji...");
        tableSearch.setBackgroundColor(Color.TRANSPARENT);
        tableSearch.setPadding(0, dp(8), 0, 0);
        searchCard.addView(tableSearch, matchWrap());
        content.addView(searchCard, matchWrap());

        LinearLayout scriptToggle = row();
        hiraganaToggle = button("Hiragana", true);
        katakanaToggle = button("Katakana", true);
        hiraganaToggle.setOnClickListener(view -> {
            tableKatakana = false;
            renderTable();
        });
        katakanaToggle.setOnClickListener(view -> {
            tableKatakana = true;
            renderTable();
        });
        scriptToggle.addView(hiraganaToggle, weightedButton());
        LinearLayout.LayoutParams katakanaParams = weightedButton();
        katakanaParams.setMarginStart(dp(8));
        scriptToggle.addView(katakanaToggle, katakanaParams);
        content.addView(scriptToggle, spaced(0, 12, 0, 0));

        categoryButtons.clear();
        HorizontalScrollView categoryScroll = new HorizontalScrollView(this);
        categoryScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout categories = row();
        addCategoryButton(categories, KanaData.GOJUON);
        addCategoryButton(categories, KanaData.DAKUTEN);
        addCategoryButton(categories, KanaData.YOON);
        addCategoryButton(categories, KanaData.SMALL);
        addCategoryButton(categories, KanaData.MODERN);
        addCategoryButton(categories, KanaData.AINU);
        categoryScroll.addView(categories, wrapWrap());
        content.addView(categoryScroll, spaced(0, 10, 0, 10));

        tableBody = column();
        content.addView(tableBody, matchWrap());
        tableSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { renderTable(); }
            public void afterTextChanged(Editable s) {}
        });
        renderTable();
    }

    private void addCategoryButton(LinearLayout parent, String category) {
        Button button = button(category, true);
        button.setTag(category);
        button.setOnClickListener(view -> {
            tableCategory = category;
            if (KanaData.MODERN.equals(category) || KanaData.AINU.equals(category)) {
                tableKatakana = true;
            }
            renderTable();
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, dp(42));
        params.setMarginEnd(dp(8));
        parent.addView(button, params);
        categoryButtons.add(button);
    }

    private void renderTable() {
        if (tableBody == null) return;
        tableBody.removeAllViews();
        applyButtonState(hiraganaToggle, !tableKatakana, CORAL);
        applyButtonState(katakanaToggle, tableKatakana, CORAL);
        for (Button button : categoryButtons) {
            applyButtonState(button, tableCategory.equals(button.getTag()), INK);
        }

        String query = tableSearch == null ? "" : tableSearch.getText().toString().trim();
        if (!query.isEmpty()) {
            renderSearchResults(query);
            return;
        }

        tableBody.addView(text(tableCategory, 22, INK, Typeface.BOLD), spaced(0, 8, 0, 8));
        addCategoryExplanation(tableCategory);
        if (KanaData.GOJUON.equals(tableCategory)) {
            renderGojuon();
        } else {
            boolean forceKatakana = KanaData.AINU.equals(tableCategory);
            renderEntryGrid(KanaData.entries(tableCategory), forceKatakana || tableKatakana, false);
        }
    }

    private void addCategoryExplanation(String category) {
        String pt;
        String en;
        if (KanaData.DAKUTEN.equals(category)) {
            pt = "Dakuten ゛ torna a consoante sonora; handakuten ゜ transforma a linha H em sons P.";
            en = "Dakuten ゛ voices a consonant; handakuten ゜ changes the H row into P sounds.";
        } else if (KanaData.YOON.equals(category)) {
            pt = "Os caracteres ゃ, ゅ e ょ são versões pequenas de や, ゆ e よ e formam sons como kya, shu ou cho.";
            en = "Small ゃ, ゅ and ょ combine with selected kana to form sounds such as kya, shu or cho.";
        } else if (KanaData.SMALL.equals(category)) {
            pt = "Os kana pequenos modificam outros sons. っ e ッ indicam normalmente a duplicação da consoante seguinte: かった → katta.";
            en = "Small kana modify other sounds. っ and ッ usually double the next consonant: かった → katta.";
        } else if (KanaData.MODERN.equals(category)) {
            pt = "Combinações katakana usadas sobretudo para representar sons de palavras estrangeiras.";
            en = "Katakana combinations mainly used to represent sounds in foreign words.";
        } else if (KanaData.AINU.equals(category)) {
            pt = "Extensões usadas principalmente na representação fonética da língua Ainu; não são katakana japonês quotidiano comum.";
            en = "Extensions mainly used for Ainu phonetics; they are not common everyday Japanese katakana.";
        } else {
            pt = "A tabela gojūon organiza os sons básicos por linha consonantal e vogal.";
            en = "The gojūon table organizes basic sounds by consonant row and vowel.";
        }
        LinearLayout info = card(SOFT);
        info.addView(languageLine("PT", pt), matchWrap());
        info.addView(languageLine("EN", en), spaced(0, 6, 0, 0));
        tableBody.addView(info, spaced(0, 0, 0, 14));
    }

    private void renderSearchResults(String query) {
        List<KanaEntry> matches = KanaData.search(query);
        tableBody.addView(text("Resultados", 22, INK, Typeface.BOLD), spaced(0, 8, 0, 4));
        tableBody.addView(text(matches.size() + " entradas encontradas", 13, MUTED, Typeface.NORMAL),
            spaced(0, 0, 0, 12));
        if (matches.isEmpty()) {
            LinearLayout empty = card(SOFT);
            empty.addView(text("Nenhum kana corresponde à pesquisa.", 14, MUTED, Typeface.NORMAL));
            tableBody.addView(empty, matchWrap());
            return;
        }

        GridLayout grid = createGrid();
        for (KanaEntry entry : matches) {
            if (!entry.hiragana.isEmpty()) addGridCell(grid, kanaCell(entry, false, false));
            if (!entry.katakana.isEmpty()) addGridCell(grid, kanaCell(entry, true, false));
        }
        tableBody.addView(grid, matchWrap());
    }

    private void renderEntryGrid(List<KanaEntry> entries, boolean katakana, boolean searchMode) {
        GridLayout grid = createGrid();
        for (KanaEntry entry : entries) {
            addGridCell(grid, kanaCell(entry, katakana, searchMode));
        }
        tableBody.addView(grid, matchWrap());
    }

    private GridLayout createGrid() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(gridColumns());
        grid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        return grid;
    }

    private void addGridCell(GridLayout grid, View cell) {
        int columns = grid.getColumnCount();
        int gap = dp(8);
        int available = getResources().getDisplayMetrics().widthPixels - dp(40);
        int width = Math.max(dp(84), (available - gap * (columns - 1)) / columns);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = width;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.setMargins(0, 0, gap, gap);
        grid.addView(cell, params);
    }

    private int gridColumns() {
        float widthDp = getResources().getDisplayMetrics().widthPixels
            / getResources().getDisplayMetrics().density;
        if (widthDp >= 720) return 6;
        if (widthDp >= 500) return 5;
        if (widthDp >= 390) return 4;
        return 3;
    }

    private View kanaCell(KanaEntry entry, boolean katakana, boolean searchMode) {
        LinearLayout cell = column();
        cell.setGravity(Gravity.CENTER);
        cell.setPadding(dp(8), dp(11), dp(8), dp(10));
        cell.setMinimumHeight(dp(82));
        cell.setBackground(round(WHITE, 14, LINE));

        String symbol = entry.symbol(katakana);
        String base = entry.baseSymbol(katakana);
        String display = !base.isEmpty() && !searchMode ? base + " → " + symbol : symbol;
        cell.addView(text(display, base.isEmpty() || searchMode ? 27 : 20, INK, Typeface.BOLD));
        cell.addView(text(entry.romaji.isEmpty() ? "—" : entry.romaji, 12, CORAL, Typeface.BOLD),
            spaced(0, 4, 0, 0));
        cell.setClickable(true);
        cell.setFocusable(true);
        cell.setOnClickListener(view -> showKanaDetails(entry, katakana));
        return cell;
    }

    private void renderGojuon() {
        String[][] grid = KanaData.gojuonGrid(tableKatakana);
        String[] labels = KanaData.gojuonRowLabels();
        TableLayout table = new TableLayout(this);

        TableRow header = new TableRow(this);
        header.addView(tableHeader("", 40));
        for (String vowel : new String[]{"a", "i", "u", "e", "o"}) {
            header.addView(tableHeader(vowel, 64));
        }
        table.addView(header);

        for (int rowIndex = 0; rowIndex < grid.length; rowIndex++) {
            TableRow row = new TableRow(this);
            row.addView(tableHeader(labels[rowIndex], 40));
            for (String symbol : grid[rowIndex]) {
                if (symbol.isEmpty()) {
                    row.addView(new View(this), new TableRow.LayoutParams(dp(64), dp(72)));
                } else {
                    KanaEntry entry = KanaData.find(symbol);
                    View cell = kanaCell(entry, tableKatakana, false);
                    TableRow.LayoutParams params = new TableRow.LayoutParams(dp(64), dp(72));
                    params.setMargins(dp(2), dp(2), dp(2), dp(2));
                    row.addView(cell, params);
                }
            }
            table.addView(row);
        }

        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.addView(table, wrapWrap());
        tableBody.addView(scroll, matchWrap());
    }

    private TextView tableHeader(String value, int widthDp) {
        TextView header = text(value, 13, MUTED, Typeface.BOLD);
        header.setGravity(Gravity.CENTER);
        header.setLayoutParams(new TableRow.LayoutParams(dp(widthDp), dp(38)));
        return header;
    }

    private void showKanaDetails(KanaEntry entry, boolean katakana) {
        String symbol = entry.symbol(katakana);
        String counterpart = entry.counterpart(katakana);
        String type = entry.variant.isEmpty() ? entry.category : entry.variant;
        StringBuilder details = new StringBuilder();
        details.append("Tipo: ").append(type);
        details.append("\nRōmaji: ").append(entry.romaji.isEmpty() ? "—" : entry.romaji);
        details.append("\nLinha: ").append(entry.line);
        if (!counterpart.isEmpty()) {
            details.append(katakana ? "\nHiragana correspondente: " : "\nKatakana correspondente: ")
                .append(counterpart);
        }
        String base = entry.baseSymbol(katakana);
        if (!base.isEmpty()) details.append("\nTransformação: ").append(base).append(" → ").append(symbol);
        if (!entry.notePt.isEmpty()) details.append("\n\nPT · ").append(entry.notePt);
        if (!entry.noteEn.isEmpty()) details.append("\nEN · ").append(entry.noteEn);

        new AlertDialog.Builder(this)
            .setTitle(symbol)
            .setMessage(details.toString())
            .setPositiveButton("Copiar", (dialog, which) -> copyText("Kana", symbol, "Kana copiado"))
            .setNegativeButton("Fechar", null)
            .show();
    }

    private void showDictionary() {
        content.addView(text("Mini-dicionário", 31, INK, Typeface.BOLD), spaced(0, 4, 0, 8));
        content.addView(text(
            "Palavras frequentes reconhecidas pelo conversor, guardadas inteiramente no dispositivo.",
            15, MUTED, Typeface.NORMAL), spaced(0, 0, 0, 18));

        LinearLayout searchCard = card(WHITE);
        searchCard.addView(label("PROCURAR PALAVRA"));
        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setTextSize(17);
        search.setTextColor(INK);
        search.setHintTextColor(Color.rgb(150, 158, 168));
        search.setHint("Kana, kanji, rōmaji ou significado...");
        search.setBackgroundColor(Color.TRANSPARENT);
        search.setPadding(0, dp(8), 0, 0);
        searchCard.addView(search, matchWrap());
        content.addView(searchCard, matchWrap());

        LinearLayout dictionaryBody = column();
        content.addView(dictionaryBody, spaced(0, 14, 0, 0));
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderDictionary(dictionaryBody, s.toString());
            }
            public void afterTextChanged(Editable s) {}
        });
        renderDictionary(dictionaryBody, "");
    }

    private void renderDictionary(LinearLayout body, String query) {
        body.removeAllViews();
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        int shown = 0;
        int limit = normalized.isEmpty() ? 24 : 80;
        for (KanaTransliterator.DictionaryEntry entry : KanaTransliterator.dictionaryEntries()) {
            String reading = entry.reading.isEmpty()
                ? KanaTransliterator.transliterate(entry.word).romaji : entry.reading;
            String searchable = (entry.word + " " + reading + " " + entry.translationPt + " "
                + entry.translationEn).toLowerCase(Locale.ROOT);
            if (!normalized.isEmpty() && !searchable.contains(normalized)) continue;
            if (shown >= limit) break;
            body.addView(dictionaryCard(entry, reading), spaced(0, 0, 0, 9));
            shown++;
        }
        if (shown == 0) {
            LinearLayout empty = card(SOFT);
            empty.addView(text("Nenhuma palavra encontrada.", 14, MUTED, Typeface.NORMAL));
            body.addView(empty, matchWrap());
        } else if (normalized.isEmpty()) {
            body.addView(text("Pesquisa para consultar as restantes entradas.", 13, MUTED, Typeface.NORMAL),
                spaced(2, 4, 0, 0));
        }
    }

    private View dictionaryCard(KanaTransliterator.DictionaryEntry entry, String reading) {
        LinearLayout item = card(WHITE);
        LinearLayout firstLine = row();
        firstLine.addView(text(entry.word, 25, INK, Typeface.BOLD),
            new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        firstLine.addView(text(reading, 15, CORAL, Typeface.BOLD));
        item.addView(firstLine, matchWrap());
        item.addView(languageLine("PT", entry.translationPt), spaced(0, 10, 0, 4));
        item.addView(languageLine("EN", entry.translationEn), matchWrap());
        return item;
    }

    private void showAbout() {
        content.addView(text("Sobre", 31, INK, Typeface.BOLD), spaced(0, 4, 0, 8));
        content.addView(text("Uma referência de kana simples, rápida e completamente offline.",
            15, MUTED, Typeface.NORMAL), spaced(0, 0, 0, 18));

        LinearLayout about = card(WHITE);
        about.addView(text("KanaBridge", 21, INK, Typeface.BOLD));
        about.addView(text(
            "Converte hiragana e katakana em rōmaji, explica cada som e inclui uma tabela de referência interativa.",
            14, MUTED, Typeface.NORMAL), spaced(0, 8, 0, 0));
        content.addView(about, matchWrap());

        LinearLayout privacy = card(MINT);
        privacy.addView(text("Privacidade por natureza", 17, INK, Typeface.BOLD));
        privacy.addView(text(
            "Sem Internet, contas, anúncios, permissões ou recolha de dados. Todo o conteúdo permanece no telemóvel.",
            14, MUTED, Typeface.NORMAL), spaced(0, 8, 0, 0));
        content.addView(privacy, spaced(0, 12, 0, 0));

        LinearLayout compatibility = card(SOFT);
        compatibility.addView(label("COMPATIBILIDADE"));
        compatibility.addView(text("Android 6.0 (API 23) ou superior", 15, INK, Typeface.BOLD),
            spaced(0, 9, 0, 0));
        content.addView(compatibility, spaced(0, 12, 0, 0));
    }

    private TextView languageLine(String language, String value) {
        TextView view = text(language + "  ·  " + value, 14, INK, Typeface.NORMAL);
        view.setLineSpacing(dp(2), 1f);
        return view;
    }

    private void copyText(String label, String value, String message) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText(label, value));
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private LinearLayout card(int color) {
        LinearLayout card = column();
        card.setPadding(dp(17), dp(16), dp(17), dp(16));
        card.setBackground(round(color, 18, color == WHITE ? LINE : color));
        return card;
    }

    private Button button(String value, boolean quiet) {
        Button button = new Button(this);
        button.setText(value);
        button.setTextSize(13);
        button.setAllCaps(false);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setTextColor(quiet ? MUTED : INK);
        button.setPadding(dp(10), 0, dp(10), 0);
        int background = quiet ? Color.rgb(246, 244, 239) : Color.rgb(239, 233, 224);
        button.setBackground(round(background, 13, background));
        return button;
    }

    private void applyButtonState(Button button, boolean selected, int selectedColor) {
        int background = selected ? selectedColor : Color.rgb(246, 244, 239);
        button.setTextColor(selected ? WHITE : MUTED);
        button.setBackground(round(background, 13, background));
    }

    private TextView label(String value) {
        TextView label = text(value, 12, MUTED, Typeface.BOLD);
        label.setLetterSpacing(0.08f);
        return label;
    }

    private TextView text(String value, int sp, int color, int style) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(sp);
        text.setTextColor(color);
        text.setTypeface(Typeface.create("sans-serif", style));
        return text;
    }

    private View divider() {
        View divider = new View(this);
        divider.setBackgroundColor(LINE);
        return divider;
    }

    private LinearLayout row() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        return row;
    }

    private LinearLayout column() {
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        return column;
    }

    private GradientDrawable round(int fill, int radius, int stroke) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radius));
        if (stroke != fill) drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams wrapWrap() {
        return new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams weightedButton() {
        return new LinearLayout.LayoutParams(0, dp(46), 1);
    }

    private LinearLayout.LayoutParams spaced(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = matchWrap();
        params.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return params;
    }

    private LinearLayout.LayoutParams spacedHeight(int height, int top, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(height));
        params.setMargins(0, dp(top), 0, dp(bottom));
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
