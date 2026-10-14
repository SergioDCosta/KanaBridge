package com.kanabridge;

import java.util.ArrayList;
import java.util.List;

/** At most 30 deliberately selected searches; query text is never logged on each keystroke. */
public final class RecentQueries {
    public static final class Item {
        public final String query; public final int mode;
        Item(String query, int mode) { this.query = query; this.mode = mode; }
    }
    public static List<Item> read(String stored) {
        List<Item> result = new ArrayList<>();
        for (String line : stored.split("\n")) try {
            int separator = line.indexOf('\t'); if (separator < 0) continue;
            int mode = Integer.parseInt(line.substring(0, separator)); String query = SavedEntry.decode(line.substring(separator + 1));
            if (mode >= 0 && mode <= 1 && !query.trim().isEmpty() && query.length() <= 300) result.add(new Item(query, mode));
            if (result.size() == 30) break;
        } catch (IllegalArgumentException ignored) { }
        return result;
    }
    public static String add(String stored, String query, int mode) {
        String value = query.trim(); if (value.isEmpty() || value.length() > 300 || mode < 0 || mode > 1) return stored;
        List<Item> values = read(stored); for (java.util.Iterator<Item> iterator = values.iterator(); iterator.hasNext();) { Item item = iterator.next(); if (item.mode == mode && item.query.equals(value)) iterator.remove(); } values.add(0, new Item(value, mode));
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < Math.min(30, values.size()); i++) { Item item = values.get(i); if (i > 0) result.append('\n'); result.append(item.mode).append('\t').append(SavedEntry.encode(item.query)); }
        return result.toString();
    }
}
