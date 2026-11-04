package com.aicoach.ui.history;

import com.aicoach.data.model.WorkoutSession;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** Pure date/aggregation rules. All grouping uses the device's time zone. */
public final class HistoryStats {
    private HistoryStats() {}

    public static Instant instant(WorkoutSession session) {
        try { return Instant.parse(session.getStartedAt()); }
        catch (DateTimeParseException | NullPointerException e) { return null; }
    }

    public static LocalDate date(WorkoutSession session, ZoneId zone) {
        Instant time = instant(session);
        return time == null ? null : time.atZone(zone).toLocalDate();
    }

    public static List<WorkoutSession> sorted(List<WorkoutSession> source) {
        List<WorkoutSession> result = new ArrayList<>();
        if (source != null) for (WorkoutSession s : source) if (s != null) result.add(s);
        result.sort(Comparator.comparing(HistoryStats::instant,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return result;
    }

    public static LocalDate rangeStart(int range, LocalDate today) {
        if (range == 1) return today.withDayOfMonth(1);
        if (range == 2) return today.withDayOfYear(1);
        return today.minusDays(6);
    }

    public static List<WorkoutSession> between(List<WorkoutSession> source,
            LocalDate start, LocalDate end, ZoneId zone) {
        List<WorkoutSession> result = new ArrayList<>();
        for (WorkoutSession s : source) {
            LocalDate date = date(s, zone);
            if (date != null && !date.isBefore(start) && !date.isAfter(end)) result.add(s);
        }
        return result;
    }

    public static long reps(List<WorkoutSession> list) {
        long total = 0;
        for (WorkoutSession s : list) total += Math.max(0, s.getTotalReps());
        return total;
    }

    public static long duration(List<WorkoutSession> list) {
        long total = 0;
        for (WorkoutSession s : list) total += Math.max(0, s.getDurationSec());
        return total;
    }

    public static double calories(List<WorkoutSession> list) {
        double total = 0;
        for (WorkoutSession s : list) if (Double.isFinite(s.getCalories())) total += Math.max(0, s.getCalories());
        return total;
    }

    public static int score(WorkoutSession session) {
        double accuracy = session.getAccuracy();
        return Double.isFinite(accuracy) ? (int) Math.round(Math.max(0, Math.min(1, accuracy)) * 100) : 0;
    }

    public static int averageScore(List<WorkoutSession> list) {
        if (list.isEmpty()) return 0;
        long total = 0;
        for (WorkoutSession s : list) total += score(s);
        return (int) Math.round((double) total / list.size());
    }

    public static int longestStreak(List<WorkoutSession> list, ZoneId zone) {
        Set<LocalDate> days = new TreeSet<>();
        for (WorkoutSession s : list) { LocalDate d = date(s, zone); if (d != null) days.add(d); }
        int best = 0, run = 0;
        LocalDate previous = null;
        for (LocalDate day : days) {
            run = previous != null && previous.plusDays(1).equals(day) ? run + 1 : 1;
            best = Math.max(best, run); previous = day;
        }
        return best;
    }

    /** Monday-first grid, with null cells before/after this month's actual dates. */
    public static List<LocalDate> calendar(YearMonth month) {
        List<LocalDate> cells = new ArrayList<>();
        int padding = month.atDay(1).getDayOfWeek().getValue() - 1;
        for (int i = 0; i < padding; i++) cells.add(null);
        for (int day = 1; day <= month.lengthOfMonth(); day++) cells.add(month.atDay(day));
        while (cells.size() % 7 != 0) cells.add(null);
        return cells;
    }
}
