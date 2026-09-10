package com.kanabridge;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public final class StudySessionTest {
    private static int tests;
    public static void main(String[] args) {
        StudySession session = new StudySession(LearningContent.group("Vogais"), 42);
        StudySession restored = new StudySession(LearningContent.group("Vogais"), 42);
        check(session.questions.size() == 5, "five vowel questions");
        for (int i = 0; i < 5; i++) {
            check(Arrays.equals(session.current(), restored.current()), "stable question after recreation");
            List<String> choices = session.choices();
            check(choices.equals(restored.choices()), "stable choices");
            check(choices.contains(session.current()[1]), "correct option present");
            check(new HashSet<>(choices).size() == choices.size(), "no duplicate choices");
            check(!session.answer("invalid"), "invalid answer rejected");
            check(session.answer(session.current()[1]), "accept first answer");
            check(!session.answer(session.current()[1]), "cannot score twice");
            session.next(); restored.answer(restored.current()[1]); restored.next();
        }
        check(session.finished() && session.score == 5, "final score");
        StudySession wrong = new StudySession(LearningContent.group("Pequeno っ"), 17);
        String incorrect = wrong.choices().stream().filter(c -> !c.equals(wrong.current()[1])).findFirst().get();
        wrong.answer(incorrect);
        check(wrong.score == 0 && !wrong.selected.isEmpty(), "wrong answer held for feedback");
        check(ReviewSchedule.nextLevel(3, false) == 0, "error resets interval");
        check(ReviewSchedule.nextLevel(6, true) == 6, "bounded level");
        check(ReviewSchedule.nextDue(0, 0) == 600_000L, "error due in ten minutes");
        check(ReviewSchedule.nextDue(0, 1) == 86_400_000L, "first success due tomorrow");
        check(ReviewSchedule.nextDue(0, 6) == 60L * 86_400_000L, "long interval uses long arithmetic");
        check(new StudySession(LearningContent.group("Hiragana"), 1).questions.size() == 10, "bounded session");
        System.out.println("OK: " + tests + " testes de prática e revisão");
    }
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
        tests++;
    }
}
