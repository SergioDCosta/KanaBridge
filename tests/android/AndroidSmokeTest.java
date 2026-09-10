package com.kanabridge.tests;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;

/** Runs only against an emulator. No external test library or production test hooks. */
public final class AndroidSmokeTest extends Instrumentation {
    private volatile Activity activity;
    private int assertions;
    @Override public void onCreate(Bundle arguments) { super.onCreate(arguments); start(); }
    @Override public void callActivityOnCreate(Activity value, Bundle state) {
        super.callActivityOnCreate(value, state); activity = value;
    }
    @Override public void onStart() {
        Bundle result = new Bundle();
        try {
            if (!android.os.Build.FINGERPRINT.contains("generic") && !android.os.Build.MODEL.contains("sdk")) throw new AssertionError("Emulator required");
            getTargetContext().getSharedPreferences("kanabridge", 0).edit().clear().commit();
            Intent launch = getTargetContext().getPackageManager().getLaunchIntentForPackage("com.kanabridge");
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity = startActivitySync(launch); idle();
            check(text().contains("Começa com cinco sons"), "discovery loaded"); screenshot("01-discover");
            click("Começar pelas vogais");
            check(text().contains("As tuas primeiras vogais"), "lesson opened");
            click("Experimentar cinco perguntas");
            check(text().contains("Pergunta 1 de 5"), "quiz starts");
            clickFirstAnswer();
            check(text().contains("Próxima pergunta"), "answer feedback");
            screenshot("02-practice");
            recreate(); check(text().contains("Próxima pergunta"), "answered question restored");
            click("Próxima pergunta"); check(text().contains("Pergunta 2 de 5"), "quiz advances once");
            byId(113); click("Conversor");
            input(201, "いらっしゃいませ");
            check(text().contains("irasshaimase"), "welcome reading");
            check(text().contains("bem-vindo/a"), "welcome meaning");
            screenshot("03-converter");
            click("っ\ns");
            // Dialog is in a separate window; dismiss with a system back event.
            screenshot("04-sokuon");
            getUiAutomation().performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK); idle();
            recreate();
            check(value(201).equals("いらっしゃいませ"), "draft survives recreation");
            input(201, "日本語 あ"); check(text().contains("Leitura parcial"), "partial reading disclosed");
            input(201, "こんにちは"); check(text().contains("konnichiwa"), "contextual greeting");
            byId(111); check(text().contains("Explora os kana"), "kana navigation");
            screenshot("05-kana");
            input(202, "shi"); check(text().contains("し\nshi"), "kana search");
            recreate(); check(value(202).equals("shi"), "kana query restored");
            byId(113); click("Dicionário");
            input(203, "cafe"); check(text().contains("コーヒー"), "accent tolerant dictionary");
            input(203, "gakkō"); check(text().contains("がっこう"), "macron dictionary alias");
            input(203, "ｺｰﾋｰ"); check(text().contains("コーヒー"), "halfwidth dictionary alias");
            screenshot("06-dictionary");
            getTargetContext().getSharedPreferences("kanabridge", 0).edit().putString("theme", "dark").commit();
            recreate(); byId(110); screenshot("07-dark");
            check(text().contains("Começa com cinco sons"), "dark recreation");
            result.putString("stream", "OK: " + assertions + " Android UI assertions; screenshots in app external files.\n");
            finish(Activity.RESULT_OK, result);
        } catch (Throwable error) {
            result.putString("stream", "FAILED: " + android.util.Log.getStackTraceString(error));
            finish(Activity.RESULT_CANCELED, result);
        }
    }
    private void idle() { SystemClock.sleep(350); waitForIdleSync(); }
    private View root() { return activity.getWindow().getDecorView(); }
    private void input(int id, String value) { runOnMainSync(() -> ((EditText)activity.findViewById(id)).setText(value)); idle(); }
    private String value(int id) {
        String[] value = new String[1]; runOnMainSync(() -> value[0] = ((EditText)activity.findViewById(id)).getText().toString()); return value[0];
    }
    private void byId(int id) { runOnMainSync(() -> activity.findViewById(id).performClick()); idle(); }
    private void click(String prefix) {
        runOnMainSync(() -> {
            Button button = button(root(), prefix);
            if (button == null) throw new AssertionError("Button not found: " + prefix);
            button.performClick();
        }); idle();
    }
    private Button button(View view, String prefix) {
        if (view instanceof Button && ((Button)view).getText().toString().startsWith(prefix)) return (Button)view;
        if (view instanceof ViewGroup) for (int i = 0; i < ((ViewGroup)view).getChildCount(); i++) {
            Button match = button(((ViewGroup)view).getChildAt(i), prefix); if (match != null) return match;
        }
        return null;
    }
    private void clickFirstAnswer() {
        for (String candidate : new String[]{"a", "i", "u", "e", "o"}) {
            boolean[] exists = new boolean[1]; runOnMainSync(() -> exists[0] = exactAnswer(root(), candidate));
            if (exists[0]) { idle(); return; }
        }
        throw new AssertionError("No answer found");
    }
    private boolean exactAnswer(View view, String text) {
        if (view instanceof Button && ((Button)view).getText().toString().equals(text)) { view.performClick(); return true; }
        if (view instanceof ViewGroup) for (int i = 0; i < ((ViewGroup)view).getChildCount(); i++) if (exactAnswer(((ViewGroup)view).getChildAt(i), text)) return true;
        return false;
    }
    private String text() {
        StringBuilder value = new StringBuilder(); runOnMainSync(() -> collect(root(), value)); return value.toString();
    }
    private void collect(View view, StringBuilder value) {
        if (view instanceof TextView) value.append(((TextView)view).getText()).append('\n');
        if (view instanceof ViewGroup) for (int i = 0; i < ((ViewGroup)view).getChildCount(); i++) collect(((ViewGroup)view).getChildAt(i), value);
    }
    private void recreate() {
        Activity old = activity; runOnMainSync(old::recreate);
        long deadline = SystemClock.uptimeMillis() + 10000;
        while (activity == old && SystemClock.uptimeMillis() < deadline) SystemClock.sleep(100);
        check(activity != old, "activity recreated"); idle();
    }
    private void screenshot(String name) throws Exception {
        Bitmap bitmap = getUiAutomation().takeScreenshot();
        if (bitmap == null) throw new AssertionError("Screenshot unavailable");
        File directory = new File(getTargetContext().getExternalFilesDir(null), "screenshots"); directory.mkdirs();
        try (FileOutputStream stream = new FileOutputStream(new File(directory, name + ".png"))) { bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream); }
        bitmap.recycle();
    }
    private void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); assertions++; }
}
