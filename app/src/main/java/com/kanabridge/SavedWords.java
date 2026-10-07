package com.kanabridge;

import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class SavedWords {
    private final SharedPreferences prefs;
    SavedWords(SharedPreferences prefs) { this.prefs = prefs; }
    boolean contains(String word) { return prefs.getStringSet("favorites", Collections.emptySet()).contains(word); }
    boolean toggle(String word) {
        Set<String> words = new HashSet<>(prefs.getStringSet("favorites", Collections.emptySet()));
        boolean added = words.add(word); if (!added) words.remove(word);
        prefs.edit().putStringSet("favorites", words).apply(); return added;
    }
    List<String> all() {
        List<String> words = new ArrayList<>(prefs.getStringSet("favorites", Collections.emptySet()));
        Collections.sort(words); return words;
    }
}
