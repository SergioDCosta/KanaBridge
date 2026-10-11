package com.kanabridge;

import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** User favourites stay separate from the replaceable dictionary asset. */
final class SavedWords {
    private final SharedPreferences prefs;
    SavedWords(SharedPreferences prefs) { this.prefs = prefs; }
    boolean contains(SavedEntry entry) { for (SavedEntry saved : entries()) if (saved.key().equals(entry.key())) return true; return false; }
    boolean toggle(SavedEntry entry) {
        Set<String> values = records(); boolean added = !contains(entry);
        for (java.util.Iterator<String> iterator = values.iterator(); iterator.hasNext();) { SavedEntry saved = SavedEntry.parse(iterator.next()); if (saved != null && saved.key().equals(entry.key())) iterator.remove(); }
        if (added) values.add(entry.serialize());
        prefs.edit().putStringSet("saved_entries_v2", values).apply(); return added;
    }
    void migrate(String legacy, SavedEntry entry) {
        Set<String> values = records(), old = new HashSet<>(legacy());
        for (java.util.Iterator<String> iterator = values.iterator(); iterator.hasNext();) { SavedEntry saved = SavedEntry.parse(iterator.next()); if (saved != null && saved.key().equals(entry.key())) iterator.remove(); }
        values.add(entry.serialize()); old.remove(legacy);
        prefs.edit().putStringSet("saved_entries_v2", values).putStringSet("favorites", old).apply();
    }
    private Set<String> records() { return new HashSet<>(prefs.getStringSet("saved_entries_v2", Collections.emptySet())); }
    List<SavedEntry> entries() {
        List<SavedEntry> result = new ArrayList<>();
        for (String value : records()) { SavedEntry entry = SavedEntry.parse(value); if (entry != null) result.add(entry); }
        Collections.sort(result, (a,b) -> { int word = a.word.compareTo(b.word); return word != 0 ? word : a.key().compareTo(b.key()); }); return result;
    }
    List<String> legacy() { List<String> result = new ArrayList<>(prefs.getStringSet("favorites", Collections.emptySet())); Collections.sort(result); return result; }
    int size() { return entries().size() + legacy().size(); }
}
