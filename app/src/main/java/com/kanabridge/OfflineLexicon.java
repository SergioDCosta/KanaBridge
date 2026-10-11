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
        final String word, reading, meaning, originalReading;
        final long entry;
        Word(String word, String reading, String meaning) { this(word, reading, meaning, 0, reading); }
        Word(String word, String reading, String meaning, long entry, String originalReading) { this.word = word; this.reading = reading; this.meaning = meaning; this.entry = entry; this.originalReading = originalReading; }
    }
    interface Callback { void done(List<Word> words, String error); }
    private final Context context;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private SQLiteDatabase db;
    private volatile boolean closed;
    OfflineLexicon(Context context) { this.context = context.getApplicationContext(); }
    interface ReferenceCallback { void done(String error); }
    void prepareReference(ReferenceCallback callback) {
        worker.execute(() -> {
            String error = null;
            try (InputStream input = context.getAssets().open("reference.xml")) { JapaneseReference.load(input); }
            catch (Exception failure) { error = "Não foi possível carregar a Referência."; }
            final String failure = error; main.post(() -> { if (!closed) callback.done(failure); });
        });
    }
    // Rotation can create a second repository while an old worker is still copying the asset.
    private static final Object INSTALL_LOCK = new Object();
    private void open() throws Exception {
        if (db != null) return;
        synchronized (INSTALL_LOCK) {
            if (closed) throw new java.io.IOException("Repository closed");
            String fingerprint;
            try (InputStream metadata = context.getAssets().open("lexicon-source.json"); java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096]; int count; while ((count = metadata.read(buffer)) != -1) bytes.write(buffer, 0, count);
                fingerprint = new org.json.JSONObject(bytes.toString("UTF-8")).getString("database_sha256");
            }
            if (!fingerprint.matches("[a-f0-9]{64}")) throw new java.io.IOException("Invalid dictionary fingerprint");
            File target = new File(context.getFilesDir(), "lexicon-" + fingerprint + ".db");
            if (target.isFile()) {
                try {
                    db = SQLiteDatabase.openDatabase(target.getPath(), null, SQLiteDatabase.OPEN_READONLY);
                    if (db.getVersion() != 2) throw new java.io.IOException("Invalid dictionary schema");
                } catch (Exception failure) {
                    if (db != null) db.close(); db = null;
                    if (!target.delete()) throw new java.io.IOException("Não foi possível substituir a base danificada.", failure);
                }
            }
            if (db == null) {
                File temp = new File(context.getFilesDir(), "lexicon-install.tmp");
                try {
                    java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
                    try (InputStream input = context.getAssets().open("lexicon.db"); FileOutputStream output = new FileOutputStream(temp)) {
                        byte[] buffer = new byte[65536]; int count;
                        while ((count = input.read(buffer)) != -1) { output.write(buffer, 0, count); digest.update(buffer, 0, count); }
                        output.getFD().sync();
                    }
                    StringBuilder actual = new StringBuilder(); for (byte value : digest.digest()) actual.append(String.format(java.util.Locale.ROOT, "%02x", value & 255));
                    if (!fingerprint.equals(actual.toString())) throw new java.io.IOException("Invalid dictionary digest");
                    try (SQLiteDatabase candidate = SQLiteDatabase.openDatabase(temp.getPath(), null, SQLiteDatabase.OPEN_READONLY)) {
                        if (candidate.getVersion() != 2) throw new java.io.IOException("Invalid dictionary schema");
                        try (Cursor check = candidate.rawQuery("PRAGMA quick_check", null)) { if (!check.moveToFirst() || !"ok".equals(check.getString(0))) throw new java.io.IOException("Invalid dictionary data"); }
                    }
                    if (!temp.renameTo(target)) throw new java.io.IOException("Não foi possível instalar o dicionário.");
                    db = SQLiteDatabase.openDatabase(target.getPath(), null, SQLiteDatabase.OPEN_READONLY);
                } finally { if (temp.isFile()) temp.delete(); }
            }
            File[] oldFiles = context.getFilesDir().listFiles();
            if (oldFiles != null) for (File file : oldFiles) if (file.getName().matches("lexicon-(?:[a-f0-9]{64}|v4)\\.db") && !file.equals(target)) file.delete();
        }
    }
    void search(String query, boolean prefix, int limit, Callback callback) {
        worker.execute(() -> {
            List<Word> results = new ArrayList<>(); String error = null;
            try {
                open();
                String kana = KanaData.toHiragana(query.trim());
                String sql = "SELECT word,reading,meaning,entry,original_reading FROM words WHERE reading=? OR word=? ORDER BY rank,word LIMIT ?";
                try (Cursor cursor = db.rawQuery(sql, new String[]{kana, query.trim(), String.valueOf(limit)})) { read(cursor, results); }
                if (results.isEmpty() && prefix && !kana.isEmpty()) {
                    // Range on the reading index, avoiding wildcard interpretation of user input.
                    try (Cursor cursor = db.rawQuery("SELECT word,reading,meaning,entry,original_reading FROM words WHERE reading>=? AND reading<? ORDER BY rank,word LIMIT ?",
                        new String[]{kana, kana + '\uffff', String.valueOf(limit)})) { read(cursor, results); }
                }
            } catch (Exception e) { error = "Não foi possível abrir o dicionário offline. Volta a tentar."; }
            final String failure = error;
            main.post(() -> { if (!closed) callback.done(results, failure); });
        });
    }
    private final java.util.concurrent.atomic.AtomicInteger dictionaryRequest = new java.util.concurrent.atomic.AtomicInteger();
    void dictionary(String query, int mode, int limit, int offset, Callback callback) {
        int ticket = dictionaryRequest.incrementAndGet();
        worker.execute(() -> {
            if (closed || ticket != dictionaryRequest.get()) return;
            List<Word> results = new ArrayList<>(); String error = null;
            try {
                open(); LexiconQueries request = new LexiconQueries(query, mode, limit, offset);
                try (Cursor cursor = db.rawQuery(request.sql, request.args)) { read(cursor, results); }
            } catch (Exception e) { error = "Não foi possível consultar o dicionário offline. Volta a tentar."; }
            final String failure = error;
            main.post(() -> { if (!closed && ticket == dictionaryRequest.get()) callback.done(results, failure); });
        });
    }
    void inflected(List<JapaneseReference.Entry> candidates, Callback callback) {
        worker.execute(() -> {
            List<Word> results = new ArrayList<>(); String error = null; java.util.Set<String> identities = new java.util.HashSet<>();
            try { open(); for (JapaneseReference.Entry candidate : candidates) {
                List<Word> words = new ArrayList<>();
                try (Cursor cursor = db.rawQuery("SELECT word,reading,meaning,entry,original_reading FROM words WHERE word=? AND reading=? ORDER BY rank,entry LIMIT 10", new String[]{candidate.japanese, KanaData.toHiragana(candidate.reading)})) { read(cursor, words); }
                for (Word word : words) if (identities.add(word.entry + "|" + word.word + "|" + word.reading)) results.add(word);
            } } catch (Exception failure) { error = "Não foi possível consultar estas formas."; }
            final String failure = error; main.post(() -> { if (!closed) callback.done(results, failure); });
        });
    }
    void resolveLegacy(String word, Callback callback) {
        worker.execute(() -> {
            List<Word> results = new ArrayList<>(); String error = null;
            try { open(); try (Cursor cursor = db.rawQuery("SELECT word,reading,meaning,entry,original_reading FROM words WHERE word=? ORDER BY rank,entry,reading LIMIT 101", new String[]{word})) { read(cursor, results); } }
            catch (Exception failure) { error = "Não foi possível atualizar os guardados antigos."; }
            final String failure = error; main.post(() -> { if (!closed) callback.done(results, failure); });
        });
    }
    interface DetailCallback { void done(List<org.json.JSONObject> senses, String error); }
    void details(Word word, DetailCallback callback) {
        worker.execute(() -> {
            List<org.json.JSONObject> senses = new ArrayList<>(); String error = null;
            try {
                open(); long entry = word.entry;
                if (entry == 0) {
                    try (Cursor cursor = db.rawQuery("SELECT entry FROM words WHERE word=? AND reading=? ORDER BY rank,entry LIMIT 1", new String[]{word.word, word.reading})) { if (cursor.moveToFirst()) entry = cursor.getLong(0); }
                }
                try (Cursor cursor = db.rawQuery("SELECT payload,glosses FROM senses JOIN sense_payloads ON sense_payloads.id=senses.payload_id JOIN definitions ON definitions.id=senses.definition WHERE entry=? ORDER BY ordinal", new String[]{String.valueOf(entry)})) {
                    while (cursor.moveToNext()) {
                        org.json.JSONObject sense = new org.json.JSONObject(cursor.getString(0));
                        sense.put("glosses", new org.json.JSONArray(cursor.getString(1)));
                        if (allows(sense.optJSONArray("stagk"), word.word) && allows(sense.optJSONArray("stagr"), word.originalReading)) senses.add(sense);
                    }
                }
            } catch (Exception e) { error = "Não foi possível carregar os sentidos. Volta a tentar."; }
            final String failure = error;
            main.post(() -> { if (!closed) callback.done(senses, failure); });
        });
    }
    private static boolean allows(org.json.JSONArray restrictions, String value) {
        if (restrictions == null || restrictions.length() == 0) return true;
        for (int i = 0; i < restrictions.length(); i++) if (value.equals(restrictions.optString(i))) return true;
        return false;
    }
    private static void read(Cursor cursor, List<Word> into) {
        while (cursor.moveToNext()) into.add(new Word(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getLong(3), cursor.getString(4)));
    }
    void close() { closed = true; worker.execute(() -> { if (db != null) db.close(); }); worker.shutdown(); }
}
