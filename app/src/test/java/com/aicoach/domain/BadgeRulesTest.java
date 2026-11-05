package com.aicoach.domain;

import org.junit.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

public class BadgeRulesTest {
    @Test public void newUserHasSixLockedBadgesAndStableIds() {
        Set<String> ids = new HashSet<>();
        for (BadgeRules.Badge b : BadgeRules.evaluate(0, 0)) {
            assertFalse(b.earned()); assertEquals(0, b.progress()); assertTrue(ids.add(b.id));
        }
        assertEquals(6, ids.size());
    }
    @Test public void everyThresholdUnlocksExactlyAtBoundary() {
        long[] reps = {100, 500, 1000};
        for (int i = 0; i < reps.length; i++) {
            assertFalse(BadgeRules.evaluate(reps[i] - 1, 0).get(i).earned());
            assertTrue(BadgeRules.evaluate(reps[i], 0).get(i).earned());
            assertEquals(100, BadgeRules.evaluate(reps[i], 0).get(i).progress());
        }
        int[] streaks = {3, 7, 30};
        for (int i = 0; i < streaks.length; i++) {
            assertFalse(BadgeRules.evaluate(0, streaks[i] - 1).get(i + 3).earned());
            assertTrue(BadgeRules.evaluate(0, streaks[i]).get(i + 3).earned());
        }
    }
    @Test public void progressClampsInvalidAndExtremeValues() {
        for (BadgeRules.Badge b : BadgeRules.evaluate(-50, -3)) assertEquals(0, b.progress());
        for (BadgeRules.Badge b : BadgeRules.evaluate(Long.MAX_VALUE, Integer.MAX_VALUE)) {
            assertEquals(100, b.progress()); assertTrue(b.earned());
        }
        assertEquals(50, BadgeRules.evaluate(50, 0).get(0).progress());
    }
    @Test public void currentStreakDoesNotPretendToBeLifetimeAchievement() {
        assertTrue(BadgeRules.evaluate(100, 7).get(4).earned());
        assertFalse(BadgeRules.evaluate(100, 1).get(4).earned());
        assertTrue(BadgeRules.evaluate(100, 1).get(0).earned());
    }
}
