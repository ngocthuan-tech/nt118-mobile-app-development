package com.aicoach.domain;

import java.util.Arrays;
import java.util.List;

/** Six display achievements derived from server totals; current streak is not a lifetime record. */
public final class BadgeRules {
    public static final class Badge {
        public final String id;
        public final long current, target;
        Badge(String id, long current, long target) {
            this.id = id; this.current = Math.max(0, current); this.target = target;
        }
        public boolean earned() { return current >= target; }
        public int progress() { return (int) Math.min(100, 100.0 * current / target); }
    }
    private BadgeRules() { }
    public static List<Badge> evaluate(long reps, int streak) {
        return Arrays.asList(new Badge("reps100", reps, 100), new Badge("reps500", reps, 500),
                new Badge("reps1000", reps, 1000), new Badge("streak3", streak, 3),
                new Badge("streak7", streak, 7), new Badge("streak30", streak, 30));
    }
}
