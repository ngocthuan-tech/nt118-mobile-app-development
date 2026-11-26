package com.aicoach.api;

import com.aicoach.api.social.ChallengeProgress;
import com.aicoach.api.social.ChallengeProgress.Point;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Tiến độ thử thách: cộng lần đúng theo ngày, chỉ trong khoảng thử thách, ngày đủ mục tiêu mới tính hoàn thành. */
class ChallengeProgressTest {

    private static final LocalDate START = LocalDate.of(2026, 11, 20);
    private static final LocalDate END = START.plusDays(6);
    private static final LocalDate TODAY = START.plusDays(3);

    @Test
    void sumsPerDayAndCountsCompletedDays() {
        List<Point> points = List.of(
                new Point("a", START, 60), new Point("a", START, 50),         // 110 ≥ 100 → xong
                new Point("a", START.plusDays(1), 90),                        // 90 < 100
                new Point("a", TODAY, 100),                                    // hôm nay xong
                new Point("b", TODAY, 30));

        Map<String, ChallengeProgress.Result> r = ChallengeProgress.compute(START, END, 100, TODAY, points);

        assertEquals(new ChallengeProgress.Result(100, 300, 2), r.get("a"));
        assertEquals(new ChallengeProgress.Result(30, 30, 0), r.get("b"));
        assertEquals(new ChallengeProgress.Result(0, 0, 0), ChallengeProgress.of(r, "c"));
    }

    @Test
    void ignoresSessionsOutsideTheChallenge() {
        List<Point> points = List.of(
                new Point("a", START.minusDays(1), 500),
                new Point("a", END.plusDays(1), 500),
                new Point("a", END, 100));

        Map<String, ChallengeProgress.Result> r = ChallengeProgress.compute(START, END, 100, END.plusDays(1), points);

        assertEquals(new ChallengeProgress.Result(0, 100, 1), r.get("a"));
    }

    @Test
    void statusAndDaysLeft() {
        assertEquals("UPCOMING", ChallengeProgress.status(START, END, START.minusDays(1)));
        assertEquals("ACTIVE", ChallengeProgress.status(START, END, START));
        assertEquals("ACTIVE", ChallengeProgress.status(START, END, END));
        assertEquals("ENDED", ChallengeProgress.status(START, END, END.plusDays(1)));

        assertEquals(7, ChallengeProgress.daysLeft(START, END, START));
        assertEquals(7, ChallengeProgress.daysLeft(START, END, START.minusDays(2)));
        assertEquals(1, ChallengeProgress.daysLeft(START, END, END));
        assertEquals(0, ChallengeProgress.daysLeft(START, END, END.plusDays(1)));
    }
}
