package com.kanabridge;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

final class Ui {
    final Context context;
    static final int SPACE_SMALL = 4, SPACE = 8, SPACE_MEDIUM = 12, SPACE_LARGE = 16, SPACE_SECTION = 24;
    static final int CONTROL_RADIUS = 12, CARD_RADIUS = 16, CONTROL_HEIGHT = 52;
    static final int TITLE_SIZE = 28, BODY_SIZE = 16, CAPTION_SIZE = 14, JAPANESE_SIZE = 24;
    final int ink, muted, accent, background, surface, soft, line, onAccent, error;
    Ui(Context context, boolean dark) {
        this.context = context;
        ink = Color.parseColor(dark ? "#F5F1E9" : "#19202A");
        muted = Color.parseColor(dark ? "#B7BFC8" : "#586373");
        android.util.TypedValue highlight = new android.util.TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorAccent, highlight, true);
        accent = highlight.data;
        background = Color.parseColor(dark ? "#14191F" : "#FAF7F1");
        surface = Color.parseColor(dark ? "#202831" : "#FFFFFF");
        soft = Color.parseColor(dark ? "#2A343D" : "#F0E9DD");
        line = Color.parseColor(dark ? "#75828F" : "#8B8790");
        error = Color.parseColor(dark ? "#FFB4AB" : "#BA1A1A");
        onAccent = Color.parseColor(dark ? "#2D1710" : "#FFFFFF");
    }
    int dp(int value) { return Math.round(value * context.getResources().getDisplayMetrics().density); }
    LinearLayout column() {
        LinearLayout view = new LinearLayout(context); view.setOrientation(LinearLayout.VERTICAL); return view;
    }
    LinearLayout row() {
        LinearLayout view = new LinearLayout(context); view.setOrientation(LinearLayout.HORIZONTAL);
        view.setGravity(Gravity.CENTER_VERTICAL); return view;
    }
    LinearLayout.LayoutParams wrap() { return new LinearLayout.LayoutParams(-1, -2); }
    LinearLayout.LayoutParams space(int top) {
        LinearLayout.LayoutParams params = wrap(); params.topMargin = dp(top); return params;
    }
    TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(context);
        view.setText(value); view.setTextSize(size); view.setTextColor(color);
        view.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        view.setLineSpacing(dp(2), 1f); return view;
    }
    TextView heading(String value) {
        TextView view = text(value, TITLE_SIZE, ink, true);
        if (android.os.Build.VERSION.SDK_INT >= 28) view.setAccessibilityHeading(true);
        return view;
    }
    LinearLayout card() {
        LinearLayout view = column(); view.setPadding(dp(SPACE_LARGE), dp(SPACE_LARGE), dp(SPACE_LARGE), dp(SPACE_LARGE));
        view.setBackground(shape(surface, line, CARD_RADIUS)); return view;
    }
    GradientDrawable shape(int color, int stroke, int radius) {
        GradientDrawable shape = new GradientDrawable(); shape.setColor(color);
        shape.setCornerRadius(dp(radius)); shape.setStroke(dp(1), stroke); return shape;
    }
    Button button(String label, boolean primary, Runnable action) {
        Button button = new Button(context);
        button.setText(label); button.setAllCaps(false); button.setTextSize(15);
        button.setMinHeight(dp(CONTROL_HEIGHT)); button.setMinimumHeight(dp(CONTROL_HEIGHT));
        button.setMinWidth(0); button.setMinimumWidth(0);
        button.setPadding(dp(SPACE_MEDIUM), dp(SPACE), dp(SPACE_MEDIUM), dp(SPACE));
        button.setStateListAnimator(null); button.setElevation(0);
        button.setOnClickListener(v -> action.run());
        paint(button, primary);
        // Primary action styling does not make an action a selected choice.
        button.setSelected(false);
        if (android.os.Build.VERSION.SDK_INT >= 30) button.setStateDescription(null);
        return button;
    }
    void paint(Button button, boolean selected) {
        button.setTextColor(new ColorStateList(new int[][]{new int[]{-android.R.attr.state_enabled}, new int[]{}},
            new int[]{muted, selected ? onAccent : ink}));
        button.setTypeface(Typeface.create("sans-serif", selected ? Typeface.BOLD : Typeface.NORMAL));
        button.setBackground(new RippleDrawable(ColorStateList.valueOf(Color.argb(32, Color.red(ink), Color.green(ink), Color.blue(ink))),
            controlBackground(selected ? accent : surface, selected ? accent : line, selected), null));
        button.setSelected(selected);
        if (android.os.Build.VERSION.SDK_INT >= 30) button.setStateDescription(selected ? "Selecionado" : null);
    }
    StateListDrawable controlBackground(int fill, int border, boolean selected) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{-android.R.attr.state_enabled}, shape(soft, line, CONTROL_RADIUS));
        GradientDrawable focused = shape(fill, selected ? onAccent : accent, CONTROL_RADIUS);
        focused.setStroke(dp(2), selected ? onAccent : accent);
        states.addState(new int[]{android.R.attr.state_focused}, focused);
        GradientDrawable normal = shape(fill, border, CONTROL_RADIUS);
        if (selected) normal.setStroke(dp(2), border);
        states.addState(new int[]{}, normal);
        return states;
    }
    void add(LinearLayout parent, View view) { parent.addView(view, space(SPACE)); }
}
