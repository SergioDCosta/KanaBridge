package com.kanabridge;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Asset installation and indexed lookup run off the UI thread. */
final class OfflineLexicon {
    static final class Word {
        final String word, reading, meaning;
        Word(String word, String reading, String meaning) { this.word = word; this.reading = reading; this.meaning = meaning; }
    }
    interface Callback { void done(List<Word> words, String error); }
    private final Context context;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private SQLiteDatabase db;
    private volatile boolean closed;
    OfflineLexicon(Context context) { this.context = context.getApplicationContext(); }
    private void open() throws Exception {
        if (db != null) return;
        String fingerprint;
        try (InputStream metadata = context.getAssets().open("lexicon-source.json"); java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096]; int count; while ((count = metadata.read(buffer)) != -1) bytes.write(buffer, 0, count);
            fingerprint = new org.json.JSONObject(bytes.toString("UTF-8")).getString("sha256");
        }
        if (!fingerprint.matches("[a-f0-9]{64}")) throw new java.io.IOException("Invalid dictionary fingerprint");
        File target = new File(context.getFilesDir(), "lexicon-" + fingerprint + ".db");
        if (!target.isFile()) {
            File temp = new File(context.getFilesDir(), "lexicon-install.tmp");
            try (InputStream input = context.getAssets().open("lexicon.db"); FileOutputStream output = new FileOutputStream(temp)) {
                byte[] bytes = new byte[65536]; int count;
                while ((count = input.read(bytes)) != -1) output.write(bytes, 0, count);
            }
            if (!temp.renameTo(target)) throw new java.io.IOException("Não foi possível instalar o dicionário.");
        }
        db = SQLiteDatabase.openDatabase(target.getPath(), null, SQLiteDatabase.OPEN_READONLY);
        File[] oldFiles = context.getFilesDir().listFiles();
        if (oldFiles != null) for (File file : oldFiles) if (file.getName().matches("lexicon-(?:[a-f0-9]{64}|v4)\\.db") && !file.equals(target)) file.delete();
    }
    void search(String query, boolean prefix, int limit, Callback callback) {
        worker.execute(() -> {
            List<Word> results = new ArrayList<>(); String error = null;
            try {
                open();
                String kana = KanaData.toHiragana(query.trim());
                String sql = "SELECT word,reading,meaning FROM words WHERE reading=? OR word=? ORDER BY rank,word LIMIT ?";
                try (Cursor cursor = db.rawQuery(sql, new String[]{kana, query.trim(), String.valueOf(limit)})) { read(cursor, results); }
                if (results.isEmpty() && prefix && !kana.isEmpty()) {
                    // Range on the reading index, avoiding wildcard interpretation of user input.
                    try (Cursor cursor = db.rawQuery("SELECT word,reading,meaning FROM words WHERE reading>=? AND reading<? ORDER BY rank,word LIMIT ?",
                        new String[]{kana, kana + '\uffff', String.valueOf(limit)})) { read(cursor, results); }
                }
            } catch (Exception e) { error = "Não foi possível abrir o dicionário offline. Volta a tentar."; }
            final String failure = error;
            main.post(() -> { if (!closed) callback.done(results, failure); });
        });
    }
    private static void read(Cursor cursor, List<Word> into) {
        while (cursor.moveToNext()) into.add(new Word(cursor.getString(0), cursor.getString(1), cursor.getString(2)));
    }
    void close() { closed = true; worker.execute(() -> { if (db != null) db.close(); }); worker.shutdown(); }
}
