package com.aicoach.api.social;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tính tiến độ thử thách từ các buổi tập (thuần Java, không cần DB → unit test được).
 *
 *  - Chỉ tính buổi tập CÙNG bài, có ngày (giờ Việt Nam) nằm trong [startDate, endDate].
 *  - Một ngày "hoàn thành" khi tổng số lần ĐÚNG trong ngày ≥ dailyTarget.
 *  - todayReps: tổng lần đúng của hôm nay (0 nếu hôm nay ngoài khoảng thử thách).
 */
public final class ChallengeProgress {

    /** Một buổi tập đã quy về ngày Việt Nam. */
    public record Point(String userId, LocalDate date, int correctReps) {
    }

    public record Result(int todayReps, int totalReps, int completedDays) {
        static final Result EMPTY = new Result(0, 0, 0);
    }

    private ChallengeProgress() {
    }

    public static Map<String, Result> compute(LocalDate startDate, LocalDate endDate, int dailyTarget,
                                              LocalDate today, List<Point> points) {
        Map<String, Map<LocalDate, Integer>> perUserDay = new HashMap<>();
        for (Point p : points) {
            if (p.date().isBefore(startDate) || p.date().isAfter(endDate) || p.correctReps() <= 0) continue;
            perUserDay.computeIfAbsent(p.userId(), k -> new HashMap<>())
                    .merge(p.date(), p.correctReps(), Integer::sum);
        }
        Map<String, Result> out = new HashMap<>();
        for (Map.Entry<String, Map<LocalDate, Integer>> e : perUserDay.entrySet()) {
            int total = 0;
            int completed = 0;
            for (Map.Entry<LocalDate, Integer> d : e.getValue().entrySet()) {
                total += d.getValue();
                if (d.getValue() >= dailyTarget) completed++;
            }
            int todayReps = e.getValue().getOrDefault(today, 0);
            out.put(e.getKey(), new Result(todayReps, total, completed));
        }
        return out;
    }

    public static Result of(Map<String, Result> results, String userId) {
        return results.getOrDefault(userId, Result.EMPTY);
    }

    /** UPCOMING | ACTIVE | ENDED theo ngày hôm nay. */
    public static String status(LocalDate startDate, LocalDate endDate, LocalDate today) {
        if (today.isBefore(startDate)) return "UPCOMING";
        if (today.isAfter(endDate)) return "ENDED";
        return "ACTIVE";
    }

    /** Số ngày còn lại tính cả hôm nay; 0 khi đã kết thúc. */
    public static int daysLeft(LocalDate startDate, LocalDate endDate, LocalDate today) {
        if (today.isAfter(endDate)) return 0;
        LocalDate from = today.isBefore(startDate) ? startDate : today;
        return (int) (endDate.toEpochDay() - from.toEpochDay()) + 1;
    }
}
