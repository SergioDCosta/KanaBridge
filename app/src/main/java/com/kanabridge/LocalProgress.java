package com.kanabridge;

import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class LocalProgress {
    private final SharedPreferences prefs;
    LocalProgress(SharedPreferences prefs) { this.prefs = prefs; }
    boolean isFavorite(String word) { return prefs.getStringSet("favorites", new HashSet<String>()).contains(word); }
    boolean toggle(String word) {
        Set<String> values = new HashSet<>(prefs.getStringSet("favorites", new HashSet<String>()));
        boolean added = values.add(word);
        if (!added) values.remove(word);
        prefs.edit().putStringSet("favorites", values).apply();
        return added;
    }
    List<String> favorites() {
        List<String> values = new ArrayList<>(prefs.getStringSet("favorites", new HashSet<String>()));
        java.util.Collections.sort(values);
        return values;
    }
    void record(String word, boolean correct) {
        int level = ReviewSchedule.nextLevel(prefs.getInt("level:" + word, 0), correct);
        Set<String> studied = new HashSet<>(prefs.getStringSet("studied", new HashSet<String>()));
        studied.add(word);
        prefs.edit().putStringSet("studied", studied).putInt("level:" + word, level)
            .putLong("due:" + word, ReviewSchedule.nextDue(System.currentTimeMillis(), level))
            .putInt("answers", answers() + 1).apply();
    }
    int answers() { return prefs.getInt("answers", 0); }
    List<String> due() {
        Set<String> words = new HashSet<>(prefs.getStringSet("studied", new HashSet<String>()));
        words.addAll(favorites());
        List<String> result = new ArrayList<>();
        for (String word : words) if (prefs.getLong("due:" + word, 0) <= System.currentTimeMillis()) result.add(word);
        java.util.Collections.sort(result);
        return result;
    }
}
