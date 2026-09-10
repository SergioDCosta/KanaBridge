package com.kanabridge;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

final class Ui {
    final Context context;
    final int ink, muted, accent, background, surface, soft, line, onAccent;
    Ui(Context context, boolean dark) {
        this.context = context;
        ink = Color.parseColor(dark ? "#F5F1E9" : "#19202A");
        muted = Color.parseColor(dark ? "#B7BFC8" : "#586373");
        accent = Color.parseColor(dark ? "#FFA18F" : "#AC392B");
        background = Color.parseColor(dark ? "#14191F" : "#FAF7F1");
        surface = Color.parseColor(dark ? "#202831" : "#FFFFFF");
        soft = Color.parseColor(dark ? "#2A343D" : "#F0E9DD");
        line = Color.parseColor(dark ? "#465361" : "#DAD5CB");
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
        TextView view = text(value, 28, ink, true);
        if (android.os.Build.VERSION.SDK_INT >= 28) view.setAccessibilityHeading(true);
        return view;
    }
    LinearLayout card() {
        LinearLayout view = column(); view.setPadding(dp(16), dp(16), dp(16), dp(16));
        view.setBackground(shape(surface, line, 18)); return view;
    }
    GradientDrawable shape(int color, int stroke, int radius) {
        GradientDrawable shape = new GradientDrawable(); shape.setColor(color);
        shape.setCornerRadius(dp(radius)); shape.setStroke(dp(1), stroke); return shape;
    }
    Button button(String label, boolean primary, Runnable action) {
        Button button = new Button(context);
        button.setText(label); button.setAllCaps(false); button.setTextSize(15);
        button.setMinHeight(dp(52)); button.setMinimumHeight(dp(52));
        button.setMinWidth(0); button.setMinimumWidth(0);
        button.setPadding(dp(12), dp(10), dp(12), dp(10));
        button.setOnClickListener(v -> action.run());
        paint(button, primary);
        button.setSelected(false);
        if (android.os.Build.VERSION.SDK_INT >= 30) button.setStateDescription(null);
        return button;
    }
    void paint(Button button, boolean selected) {
        button.setTextColor(selected ? onAccent : ink);
        button.setBackground(new RippleDrawable(ColorStateList.valueOf(line),
            shape(selected ? accent : soft, selected ? accent : soft, 14), null));
        button.setSelected(selected);
        if (android.os.Build.VERSION.SDK_INT >= 30) button.setStateDescription(selected ? "Selecionado" : null);
    }
    void add(LinearLayout parent, View view) { parent.addView(view, space(10)); }
}
