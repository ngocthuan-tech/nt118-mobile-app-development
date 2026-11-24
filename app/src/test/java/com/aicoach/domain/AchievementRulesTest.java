package com.aicoach.domain;

import org.junit.Test;
import static org.junit.Assert.*;

public class AchievementRulesTest {
    @Test public void everyRankBoundaryIsInclusive() {
        for (int i = 1; i < AchievementRules.RANKS.size(); i++) {
            AchievementRules.Tier tier = AchievementRules.RANKS.get(i);
            assertSame(AchievementRules.RANKS.get(i - 1), AchievementRules.rank(tier.minimumXp - 1).current);
            assertSame(tier, AchievementRules.rank(tier.minimumXp).current);
            assertSame(tier, AchievementRules.rank(tier.minimumXp + 1).current);
        }
    }
    @Test public void negativeAndZeroXpUseStartingTiersWithoutPhantomProgress() {
        assertEquals("Novice", AchievementRules.rank(0).current.name);
        assertEquals(0, AchievementRules.rank(Long.MIN_VALUE).percent);
        assertEquals(200, AchievementRules.rank(-50).remaining);
    }
    @Test public void maxTierHandlesLongMaxWithoutOverflow() {
        AchievementRules.Progress p = AchievementRules.rank(Long.MAX_VALUE);
        assertEquals("Grandmaster", p.current.name);
        assertEquals(100, p.percent); assertNull(p.next); assertEquals(0, p.remaining);
    }
    @Test public void progressMeasuresDistanceBetweenCurrentAndNextThreshold() {
        AchievementRules.Progress p = AchievementRules.rank(3500);
        assertEquals(50, p.percent); assertEquals(1500, p.remaining);
        assertEquals(0, AchievementRules.rank(750).percent);
        assertEquals(1250, AchievementRules.rank(750).remaining);
        assertEquals(99, AchievementRules.rank(1999).percent);
    }
    @Test public void masterBadgeRequiresFiveThousandXpInOneExercise() {
        assertFalse(AchievementRules.earnedExerciseBadge(4999));
        assertTrue(AchievementRules.earnedExerciseBadge(5000));
    }
}
