package com.kanabridge.tests;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;

/** Device-level checks use only Android's instrumentation API and run only on emulators. */
public final class AndroidSmokeTest extends Instrumentation {
    private volatile Activity activity;
    private int assertions;
    private boolean landscape;
    @Override public void onCreate(Bundle arguments) { super.onCreate(arguments); landscape = arguments != null && "landscape".equals(arguments.getString("orientation")); start(); }
    @Override public void callActivityOnCreate(Activity value, Bundle state) { super.callActivityOnCreate(value, state); activity = value; }
    @Override public void onStart() {
        Bundle result = new Bundle();
        try {
            if (!android.os.Build.FINGERPRINT.contains("generic") && !android.os.Build.MODEL.contains("sdk")
                && !android.os.Build.HARDWARE.equals("ranchu") && !android.os.Build.HARDWARE.equals("goldfish")) throw new AssertionError("Emulator required");
            getTargetContext().getSharedPreferences("kanabridge", 0).edit().clear().commit();
            Intent launch = getTargetContext().getPackageManager().getLaunchIntentForPackage("com.kanabridge");
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); activity = startActivitySync(launch); idle();
            runOnMainSync(() -> activity.setRequestedOrientation(landscape ? android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
            long orientationDeadline = SystemClock.uptimeMillis() + 10000;
            int expectedOrientation = landscape ? android.content.res.Configuration.ORIENTATION_LANDSCAPE : android.content.res.Configuration.ORIENTATION_PORTRAIT;
            while (activity.getResources().getConfiguration().orientation != expectedOrientation && SystemClock.uptimeMillis() < orientationDeadline) idle();
            idle(); check(activity.getResources().getConfiguration().orientation == expectedOrientation, "requested test orientation");
            check(text().contains("Converter") && activity.findViewById(201) != null, "converter is home");
            check(!text().contains("Descobrir") && !text().contains("Praticar"), "removed learning navigation");
            input(201, "irasshaimase"); check(text().contains("いらっしゃいませ"), "reverse sokuon");
            byId(221); check(text().contains("イラッシャイマセ"), "katakana output");
            byId(223); check(text().contains("irasshaimase"), "romaji round trip");
            input(201, "nihongo"); byId(222); await("日本語  ·"); click("日本語  ·");
            check(text().contains("Kanji escolhido"), "explicit kanji selection");
            recreate(); check(text().contains("Kanji escolhido"), "kanji selection survives recreation");
            byId(220); check(text().contains("にほんご"), "hiragana output after kanji selection"); screenshot("01-converter");
            input(201, "日本語"); byId(222); await("日本語  ·"); click("日本語  ·"); byId(223);
            check(text().contains("nihongo"), "lexical kanji reading");
            input(201, "gakkou"); byId(220); check(text().contains("がっこう"), "double consonant");
            input(201, "kan'i"); check(text().contains("かんい"), "nasal separator");
            input(201, "ga pa kya"); check(text().contains("が ぱ きゃ"), "dakuten handakuten combination");
            input(201, "q"); check(text().contains("Conversão parcial"), "unsupported input disclosed");
            input(201, "watashi ha nihongo"); byId(222); check(text().contains("Composição com kanji"), "multiword composition");
            click("わたし · escolher grafia"); dialogItem("私  ·  わたし");
            click("にほんご · escolher grafia"); dialogItem("日本語  ·  にほんご");
            check(text().contains("私 は 日本語"), "composed kanji preserves particles and spaces");
            recreate(); check(text().contains("私 は 日本語"), "composition survives recreation");
            byId(111); check(text().contains("Básicos · gojūon"), "kana table");
            runOnMainSync(() -> {
                View search = activity.findViewById(202), table = activity.findViewById(213);
                float density = activity.getResources().getDisplayMetrics().density;
                check(table.getTop() - search.getBottom() <= 12 * density, "no empty gap above table");
                Button cell = findButton(activity.getWindow().getDecorView(), "し\nshi");
                check(cell != null && cell.getHeight() >= 48 * density, "accessible kana touch target");
            });
            screenshot("02-kana"); byId(211); check(text().contains("シ\nshi"), "direct script toggle");
            input(202, "shi"); check(text().contains("シ\nshi"), "kana search");
            recreate(); check(value(202).equals("shi"), "table query survives recreation");
            byId(110); input(201, "a\ni\nu\ne\no\nka\nki\nku\nke\nko\nsa\nshi\nsu\nse\nso");
            runOnMainSync(() -> {
                EditText field = activity.findViewById(201); field.requestFocus(); field.setSelection(field.length());
                ((android.view.inputmethod.InputMethodManager)activity.getSystemService(Activity.INPUT_METHOD_SERVICE)).showSoftInput(field, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            });
            long deadline = SystemClock.uptimeMillis() + 10000;
            while (!keyboardVisible() && SystemClock.uptimeMillis() < deadline) idle();
            check(keyboardVisible(), "keyboard actually opens"); idle();
            runOnMainSync(() -> {
                EditText field = activity.findViewById(201); android.graphics.Rect visible = new android.graphics.Rect();
                check(field.getGlobalVisibleRect(visible), "input remains visible");
                int[] xy = new int[2]; field.getLocationOnScreen(xy);
                int line = field.getLayout().getLineForOffset(field.getSelectionEnd());
                int cursorBottom = xy[1] + field.getTotalPaddingTop() + field.getLayout().getLineBottom(line) - field.getScrollY();
                check(cursorBottom <= visible.bottom, "cursor above keyboard");
            }); screenshot("03-keyboard");
            getUiAutomation().performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK); idle();
            click("Menu"); dialogItem("Referência de japonês (opcional)");
            check(text().contains("290 entradas"), "large optional reference");
            input(204, "porque"); check(text().contains("Porque gosto"), "Portuguese reference search"); screenshot("04-reference");
            click("Menu"); dialogItem("Dicionário offline"); input(203, "hashi"); await("bridge");
            check(text().contains("chopsticks"), "ambiguous kanji meanings"); screenshot("05-dictionary");
            getTargetContext().getSharedPreferences("kanabridge", 0).edit().putString("theme", "dark").commit();
            recreate(); byId(111); input(202, ""); screenshot("06-dark");
            result.putString("stream", "OK: " + assertions + " Android UI assertions; screenshots in app external files.\n"); finish(Activity.RESULT_OK, result);
        } catch (Throwable error) {
            try { screenshot("failure"); } catch (Exception ignored) {}
            result.putString("stream", "FAILED: " + android.util.Log.getStackTraceString(error)); finish(Activity.RESULT_CANCELED, result);
        }
    }
    private boolean keyboardVisible() {
        boolean[] visible = new boolean[1]; runOnMainSync(() -> {
            android.view.WindowInsets insets = activity.getWindow().getDecorView().getRootWindowInsets();
            visible[0] = insets.isVisible(android.view.WindowInsets.Type.ime()) && insets.getInsets(android.view.WindowInsets.Type.ime()).bottom > 100 * activity.getResources().getDisplayMetrics().density;
        }); return visible[0];
    }
    private void idle() { SystemClock.sleep(250); waitForIdleSync(); }
    private void await(String value) {
        long deadline = SystemClock.uptimeMillis() + 30000;
        while (!text().contains(value) && SystemClock.uptimeMillis() < deadline) idle();
        check(text().contains(value), "await: " + value);
    }
    private void input(int id, String value) { runOnMainSync(() -> ((EditText)activity.findViewById(id)).setText(value)); idle(); }
    private String value(int id) { String[] value = new String[1]; runOnMainSync(() -> value[0] = ((EditText)activity.findViewById(id)).getText().toString()); return value[0]; }
    private void byId(int id) { runOnMainSync(() -> activity.findViewById(id).performClick()); idle(); }
    private void click(String prefix) {
        runOnMainSync(() -> { Button button = findButton(activity.getWindow().getDecorView(), prefix); if (button == null) throw new AssertionError("Button: " + prefix); button.performClick(); }); idle();
    }
    private Button findButton(View view, String prefix) {
        if (view instanceof Button && ((Button)view).getText().toString().startsWith(prefix)) return (Button)view;
        if (view instanceof ViewGroup) for (int i = 0; i < ((ViewGroup)view).getChildCount(); i++) { Button button = findButton(((ViewGroup)view).getChildAt(i), prefix); if (button != null) return button; }
        return null;
    }
    private void dialogItem(String value) {
        AccessibilityNodeInfo root = getUiAutomation().getRootInActiveWindow(); boolean clicked = false;
        for (AccessibilityNodeInfo node : root.findAccessibilityNodeInfosByText(value)) {
            AccessibilityNodeInfo target = node; while (target != null && !target.isClickable()) target = target.getParent();
            if (target != null) { clicked = target.performAction(AccessibilityNodeInfo.ACTION_CLICK); if (clicked) break; }
        }
        check(clicked, "dialog item " + value); idle();
    }
    private String text() { StringBuilder result = new StringBuilder(); runOnMainSync(() -> collect(activity.getWindow().getDecorView(), result)); return result.toString(); }
    private void collect(View view, StringBuilder result) {
        if (view instanceof TextView) result.append(((TextView)view).getText()).append('\n');
        if (view instanceof ViewGroup) for (int i = 0; i < ((ViewGroup)view).getChildCount(); i++) collect(((ViewGroup)view).getChildAt(i), result);
    }
    private void recreate() {
        Activity old = activity; runOnMainSync(old::recreate); long deadline = SystemClock.uptimeMillis() + 10000;
        while (activity == old && SystemClock.uptimeMillis() < deadline) SystemClock.sleep(100);
        check(activity != old, "activity recreated"); idle();
    }
    private void screenshot(String name) throws Exception {
        Bitmap bitmap = getUiAutomation().takeScreenshot(); if (bitmap == null) throw new AssertionError("Screenshot unavailable");
        File directory = new File(getTargetContext().getExternalFilesDir(null), "screenshots"); directory.mkdirs();
        try (FileOutputStream output = new FileOutputStream(new File(directory, name + ".png"))) { bitmap.compress(Bitmap.CompressFormat.PNG, 100, output); } bitmap.recycle();
    }
    private void check(boolean value, String message) { if (!value) throw new AssertionError(message); assertions++; }
}
