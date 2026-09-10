package com.kanabridge;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.widget.Toast;
import java.util.Locale;

/** Only uses an installed Japanese voice which explicitly works without network. */
final class JapaneseSpeech {
    private TextToSpeech tts;
    private boolean ready;
    private boolean initialized;
    private boolean closed;
    private final Activity activity;
    JapaneseSpeech(Activity activity) {
        this.activity = activity;
        tts = new TextToSpeech(activity, status -> activity.runOnUiThread(() -> {
            if (closed || tts == null) return;
            initialized = true;
            if (status != TextToSpeech.SUCCESS) return;
            try {
                if (tts.getVoices() != null) for (Voice voice : tts.getVoices()) {
                    if ("ja".equals(voice.getLocale().getLanguage()) && !voice.isNetworkConnectionRequired()
                        && (voice.getFeatures() == null || !voice.getFeatures().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED))) {
                        ready = tts.setVoice(voice) == TextToSpeech.SUCCESS;
                        if (ready) break;
                    }
                }
            } catch (RuntimeException ignored) { ready = false; }
        }));
    }
    void speak(String value, boolean slow) {
        if (!initialized) { Toast.makeText(activity, "A preparar a voz… tenta novamente num instante.", Toast.LENGTH_SHORT).show(); return; }
        if (!ready) {
            new AlertDialog.Builder(activity).setTitle("Voz japonesa offline")
                .setMessage("Não foi encontrada uma voz japonesa instalada que funcione offline. Nas definições de síntese de voz do telemóvel, instala japonês e volta a abrir a aplicação. O download da voz pode precisar de Internet.")
                .setPositiveButton("Definições de voz", (d, w) -> {
                    try { activity.startActivity(new Intent("com.android.settings.TTS_SETTINGS")); }
                    catch (android.content.ActivityNotFoundException e) { activity.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS)); }
                }).setNegativeButton("Agora não", null).show();
            return;
        }
        tts.setSpeechRate(slow ? 0.65f : 0.9f);
        if (tts.speak(value, TextToSpeech.QUEUE_FLUSH, null, "kana") == TextToSpeech.ERROR)
            Toast.makeText(activity, "Não foi possível reproduzir. Verifica a voz nas definições.", Toast.LENGTH_LONG).show();
    }
    void stop() { if (tts != null) tts.stop(); }
    void close() { closed = true; if (tts != null) { tts.stop(); tts.shutdown(); } }
}
