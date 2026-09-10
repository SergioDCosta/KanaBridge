package com.kanabridge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Reproducible question order and distractors, including after Activity recreation. */
public final class StudySession {
    public final List<String[]> questions = new ArrayList<>();
    private final List<String[]> pool;
    private final long seed;
    public int index;
    public int score;
    public String selected = "";

    public StudySession(List<String[]> source, long seed) {
        this.seed = seed;
        pool = new ArrayList<>(source);
        questions.addAll(source);
        Collections.shuffle(questions, new Random(seed));
        if (questions.size() > 10) questions.subList(10, questions.size()).clear();
    }
    public boolean finished() { return index >= questions.size(); }
    public String[] current() { return questions.get(index); }
    public List<String> choices() {
        List<String> result = new ArrayList<>();
        result.add(current()[1]);
        List<String[]> candidates = new ArrayList<>(pool);
        Collections.shuffle(candidates, new Random(seed + index));
        for (String[] candidate : candidates) {
            if (!result.contains(candidate[1])) result.add(candidate[1]);
            if (result.size() == 4) break;
        }
        Collections.shuffle(result, new Random(seed - index));
        return result;
    }
    public boolean answer(String value) {
        if (finished() || !selected.isEmpty() || !choices().contains(value)) return false;
        selected = value;
        if (current()[1].equals(value)) score++;
        return true;
    }
    public void next() {
        if (!finished() && !selected.isEmpty()) { index++; selected = ""; }
    }
}
