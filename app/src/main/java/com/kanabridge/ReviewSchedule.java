package com.kanabridge;

/** Pure scheduling rules; local progress does not require a service or account. */
public final class ReviewSchedule {
    public static int nextLevel(int previous, boolean correct) {
        return correct ? Math.min(6, Math.max(0, previous) + 1) : 0;
    }
    public static long nextDue(long now, int level) {
        long[] delays = {10 * 60_000L, 86_400_000L, 3 * 86_400_000L, 7 * 86_400_000L,
            14 * 86_400_000L, 30 * 86_400_000L, 60 * 86_400_000L};
        return now + delays[Math.max(0, Math.min(level, delays.length - 1))];
    }
    private ReviewSchedule() {}
}
