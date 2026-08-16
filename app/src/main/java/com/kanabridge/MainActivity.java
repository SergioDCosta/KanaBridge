package com.kanabridge;

import android.app.Activity;
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
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


public final class MainActivity extends Activity {
    private static final int INK = Color.rgb(25, 32, 42);
    private static final int MUTED = Color.rgb(95, 107, 122);
    private static final int CORAL = Color.rgb(230, 95, 77);
    private static final int CREAM = Color.rgb(250, 247, 241);
    private static final int WHITE = Color.WHITE;
    private static final int LINE = Color.rgb(230, 228, 222);
    private static final int MINT = Color.rgb(230, 244, 238);

    private LinearLayout root;
    private LinearLayout results;
    private EditText input;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setStatusBarColor(CREAM);
        window.setNavigationBarColor(CREAM);
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            int systemUi = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                systemUi |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
            window.getDecorView().setSystemUiVisibility(systemUi);
        }

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(CREAM);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(18), dp(20), dp(36));
        scroll.addView(root, matchWrap());
        scroll.setOnApplyWindowInsetsListener((view, insets) -> {
            // Android 15 draws apps edge-to-edge. Keep the content below the status bar
            // and above the navigation area on every screen shape, including cut-outs.
            root.setPadding(
                dp(20), dp(18) + insets.getSystemWindowInsetTop(),
                dp(20), dp(36) + insets.getSystemWindowInsetBottom()
            );
            return insets;
        });
        setContentView(scroll);
        scroll.requestApplyInsets();
        buildInterface();
    }

    private void buildInterface() {
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

        TextView headline = text("Lê hiragana e\nkatakana com confiança.", 32, INK, Typeface.BOLD);
        headline.setLineSpacing(0, 0.96f);
        root.addView(headline, spaced(0, 30, 0, 8));
        root.addView(text("Cola kana abaixo. A leitura em rōmaji aparece imediatamente — tudo offline.", 15, MUTED, Typeface.NORMAL), spaced(0, 0, 0, 22));

        LinearLayout inputCard = card(WHITE);
        inputCard.addView(label("TEXTO JAPONÊS"));
        input = new EditText(this);
        input.setTextSize(27);
        input.setTextColor(INK);
        input.setHintTextColor(Color.rgb(166, 171, 177));
        input.setHint("Ex.: がっこう ou キャット");
        input.setGravity(Gravity.TOP);
        input.setMinHeight(dp(112));
        input.setPadding(0, dp(10), 0, dp(8));
        input.setBackgroundColor(Color.TRANSPARENT);
        input.setSingleLine(false);
        inputCard.addView(input, matchWrap());

        LinearLayout actions = row();
        Button example = button("Experimentar exemplo", false);
        example.setOnClickListener(v -> input.setText("こんにちは"));
        Button clear = button("Limpar", true);
        clear.setOnClickListener(v -> input.setText(""));
        actions.addView(example, new LinearLayout.LayoutParams(0, dp(46), 1));
        LinearLayout.LayoutParams clearParams = new LinearLayout.LayoutParams(dp(88), dp(46));
        clearParams.setMarginStart(dp(10));
        actions.addView(clear, clearParams);
        inputCard.addView(actions, matchWrap());
        root.addView(inputCard, matchWrap());

        results = column();
        root.addView(results, spaced(0, 18, 0, 0));
        input.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { showResult(s.toString()); }
            public void afterTextChanged(Editable s) {}
        });
        showEmpty();
    }

    private void showEmpty() {
        results.removeAllViews();
        LinearLayout info = card(Color.rgb(244, 239, 229));
        info.addView(text("Uma nota rápida", 16, INK, Typeface.BOLD));
        TextView body = text("Kana representa sons. Por isso, あ lê-se “a”, mas não significa uma palavra sozinho. Quando reconhecemos uma palavra completa, mostramos também o significado em português e inglês.", 14, MUTED, Typeface.NORMAL);
        body.setLineSpacing(dp(3), 1f);
        info.addView(body, spaced(0, 8, 0, 0));
        results.addView(info, matchWrap());
    }

    private void showResult(String raw) {
        if (raw.trim().isEmpty()) { showEmpty(); return; }
        KanaTransliterator.Result parsed = KanaTransliterator.transliterate(raw);
        results.removeAllViews();

        LinearLayout reading = card(INK);
        LinearLayout readingHeader = row();
        TextView readingLabel = text("RŌMAJI", 12, Color.rgb(194, 202, 211), Typeface.BOLD);
        readingHeader.addView(readingLabel, new LinearLayout.LayoutParams(0, dp(30), 1));
        Button copy = button("Copiar", false);
        copy.setTextColor(WHITE);
        copy.setBackground(round(Color.rgb(54, 64, 77), 12, Color.rgb(54, 64, 77)));
        copy.setOnClickListener(v -> copy(parsed.romaji));
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
            noMeaning.addView(text("Não está no mini-dicionário offline. A leitura em rōmaji continua correta; kana isolado indica som, não significado.", 14, MUTED, Typeface.NORMAL), spaced(0, 7, 0, 0));
            results.addView(noMeaning, spaced(0, 12, 0, 0));
        }

        results.addView(text("CARÁCTER A CARÁCTER", 12, MUTED, Typeface.BOLD), spaced(dp(2), 24, 0, 10));
        for (KanaTransliterator.Token token : parsed.tokens) {
            if (token.kana.trim().isEmpty()) continue;
            results.addView(tokenCard(token), spaced(0, 0, 0, 9));
        }
    }

    private View tokenCard(KanaTransliterator.Token token) {
        LinearLayout card = card(WHITE);
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
        card.addView(top, matchWrap());
        View divider = new View(this);
        divider.setBackgroundColor(LINE);
        card.addView(divider, spacedHeight(1, 13, 11));
        card.addView(languageLine("PT", token.notePt), matchWrap());
        card.addView(languageLine("EN", token.noteEn), spaced(0, 6, 0, 0));
        return card;
    }

    private TextView languageLine(String language, String value) {
        TextView view = text(language + "  ·  " + value, 14, INK, Typeface.NORMAL);
        view.setLineSpacing(dp(2), 1f);
        return view;
    }

    private void copy(String value) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("Rōmaji", value));
        Toast.makeText(this, "Rōmaji copiado", Toast.LENGTH_SHORT).show();
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
        int bg = quiet ? Color.rgb(246, 244, 239) : Color.rgb(239, 233, 224);
        button.setBackground(round(bg, 13, bg));
        return button;
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
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams spaced(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams p = matchWrap();
        p.setMargins(left, top, right, bottom);
        return p;
    }

    private LinearLayout.LayoutParams spacedHeight(int height, int top, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(height));
        p.setMargins(0, dp(top), 0, dp(bottom));
        return p;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
