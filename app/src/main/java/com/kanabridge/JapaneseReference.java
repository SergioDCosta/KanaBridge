package com.kanabridge;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Authored reference asset, loaded once; search keys are computed outside the typing path. */
public final class JapaneseReference {
    public static final class Entry {
        public final String id, category, topic, japanese, reading, meaning, detail, example;
        private final String romaji, searchKey;
        private final List<String> forms;
        Entry(Element node) {
            id = node.getAttribute("id"); category = text(node, "category"); topic = text(node, "topic");
            japanese = text(node, "japanese"); reading = text(node, "reading"); meaning = text(node, "meaning"); detail = text(node, "detail");
            NodeList examples = node.getElementsByTagName("example");
            if (examples.getLength() == 0) example = "";
            else { Element item = (Element)examples.item(0); example = text(item,"japanese") + "\n" + text(item,"reading") + "\n" + text(item,"romaji") + "\n" + text(item,"meaning"); }
            String authored = text(node, "romaji"); romaji = authored.isEmpty() ? KanaTransliterator.transliterate(reading).romaji : authored;
            forms = new ArrayList<>(); NodeList formNodes = node.getElementsByTagName("form");
            for (int i = 0; i < formNodes.getLength(); i++) { Element form = (Element)formNodes.item(i); forms.add(form.getAttribute("written")); forms.add(form.getAttribute("reading")); }
            searchKey = KanaTransliterator.searchKey(topic + " " + japanese + " " + reading + " " + romaji + " " + meaning + " " + detail + " " + example);
        }
        public String romaji() { return romaji; }
    }
    private static volatile java.util.Map<String,List<Entry>> inflections = Collections.emptyMap();
    private static volatile List<Entry> entries = Collections.emptyList();
    public static boolean loaded() { return !entries.isEmpty(); }
    public static synchronized void load(InputStream input) throws Exception {
        if (loaded()) return;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setExpandEntityReferences(false);
        javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver((publicId, systemId) -> { throw new org.xml.sax.SAXException("External entities are not allowed"); });
        Element root = builder.parse(input).getDocumentElement();
        if (!root.getTagName().equals("reference") || !root.getAttribute("schema_version").equals("1")) throw new java.io.IOException("Invalid reference schema");
        List<Entry> result = new ArrayList<>(); HashSet<String> ids = new HashSet<>();
        NodeList nodes = root.getElementsByTagName("entry");
        for (int i = 0; i < nodes.getLength(); i++) {
            Entry entry = new Entry((Element)nodes.item(i));
            if (entry.id.isEmpty() || !ids.add(entry.id) || entry.japanese.isEmpty() || entry.reading.isEmpty() || entry.meaning.isEmpty()) throw new java.io.IOException("Invalid reference entry");
            result.add(entry);
        }
        if (result.isEmpty()) throw new java.io.IOException("Empty reference");
        java.util.Map<String,List<Entry>> index = new java.util.HashMap<>();
        for (Entry entry : result) for (String form : entry.forms) {
            String key = KanaData.toHiragana(form); List<Entry> matches = index.get(key);
            if (matches == null) { matches = new ArrayList<>(); index.put(key, matches); }
            if (!matches.contains(entry)) matches.add(entry);
        }
        inflections = index; entries = Collections.unmodifiableList(result);
    }
    private static String text(Element node, String key) { NodeList list = node.getElementsByTagName(key); return list.getLength() == 0 ? "" : list.item(0).getTextContent(); }
    public static List<Entry> all() { return entries; }
    public static List<Entry> search(String category, String query) {
        String key = KanaTransliterator.searchKey(query); List<Entry> result = new ArrayList<>();
        for (Entry entry : entries) if ((category.equals("Tudo") || category.equals(entry.category) || category.equals(entry.topic)) && (key.isEmpty() || entry.searchKey.contains(key))) result.add(entry);
        return result;
    }
    public static List<Entry> inflected(String query) {
        String key = KanaData.toHiragana(RomajiConverter.convert(query.trim()).hiragana);
        List<Entry> matches = inflections.get(key); return matches == null ? Collections.emptyList() : Collections.unmodifiableList(matches);
    }
}
