package com.aicoach.ui.history;

import com.aicoach.data.model.WorkoutSession;
import com.google.gson.Gson;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class HistoryStatsTest {
    private final ZoneId zone = HistoryStats.ZONE;
    private WorkoutSession session(String time, int reps) {
        return new Gson().fromJson("{\"startedAt\":\"" + time + "\",\"totalReps\":" + reps
                + ",\"durationSec\":60,\"calories\":2.5,\"accuracy\":0.8}", WorkoutSession.class);
    }
    @Test public void vietnamGroupingDoesNotFollowDeviceTimeZone() {
        java.util.TimeZone previous = java.util.TimeZone.getDefault();
        try {
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("America/Los_Angeles"));
            assertEquals(LocalDate.of(2026, 11, 1), HistoryStats.date(session("2026-10-31T18:00:00Z", 10), HistoryStats.ZONE));
        } finally { java.util.TimeZone.setDefault(previous); }
    }
    @Test public void groupsUtcAcrossLocalMidnight() {
        WorkoutSession s = session("2026-10-31T18:00:00Z", 10);
        assertEquals(LocalDate.of(2026, 11, 1), HistoryStats.date(s, zone));
        assertEquals(1, HistoryStats.between(Collections.singletonList(s), LocalDate.of(2026,11,1), LocalDate.of(2026,11,1), zone).size());
    }
    @Test public void weekCrossesYearAndIncludesExactlySevenDates() {
        LocalDate today = LocalDate.of(2027,1,2);
        LocalDate start = HistoryStats.rangeStart(0,today);
        assertEquals(LocalDate.of(2026,12,27), start);
        List<WorkoutSession> source = Arrays.asList(session("2026-12-26T05:00:00Z",1),
                session("2026-12-27T05:00:00Z",2), session("2027-01-02T05:00:00Z",3),session("2027-01-03T05:00:00Z",4));
        assertEquals(5, HistoryStats.reps(HistoryStats.between(source,start,today,zone)));
    }
    @Test public void monthAndYearUseCalendarBoundaries() {
        LocalDate today = LocalDate.of(2026,11,4);
        assertEquals(LocalDate.of(2026,11,1),HistoryStats.rangeStart(1,today));
        assertEquals(LocalDate.of(2026,1,1),HistoryStats.rangeStart(2,today));
    }
    @Test public void leapCalendarIsMondayFirstAndIncludes29th() {
        List<LocalDate> grid=HistoryStats.calendar(YearMonth.of(2024,2));
        assertEquals(35,grid.size()); assertNull(grid.get(0)); assertNull(grid.get(2));
        assertEquals(LocalDate.of(2024,2,1),grid.get(3));
        assertEquals(LocalDate.of(2024,2,29),grid.get(31)); assertNull(grid.get(34));
    }
    @Test public void invalidDatesAreNotAssignedToTodayAndSortLast() {
        WorkoutSession invalid=session("bad-date",10), older=session("2026-10-01T00:00:00Z",20), newer=session("2026-11-01T00:00:00Z",30);
        assertNull(HistoryStats.date(invalid,zone));
        assertEquals(Arrays.asList(newer,older,invalid),HistoryStats.sorted(Arrays.asList(invalid,null,older,newer)));
    }
    @Test public void streakDeduplicatesMultipleSessionsPerDayAndResetsAtGaps() {
        List<WorkoutSession> source=Arrays.asList(session("2026-11-01T01:00:00Z",1),session("2026-11-01T02:00:00Z",1),
                session("2026-11-02T01:00:00Z",1),session("2026-11-04T01:00:00Z",1));
        assertEquals(2,HistoryStats.longestStreak(source,zone));
        assertEquals(0,HistoryStats.longestStreak(Collections.emptyList(),zone));
    }
    @Test public void totalsAndEmptyDataHaveStableValues() {
        List<WorkoutSession> source=Arrays.asList(session("2026-11-01T01:00:00Z",10),session("2026-11-02T01:00:00Z",20));
        assertEquals(30,HistoryStats.reps(source)); assertEquals(120,HistoryStats.duration(source));
        assertEquals(5.0,HistoryStats.calories(source),0.001); assertEquals(80,HistoryStats.averageScore(source));
        assertEquals(0,HistoryStats.averageScore(Collections.emptyList()));
    }
}
