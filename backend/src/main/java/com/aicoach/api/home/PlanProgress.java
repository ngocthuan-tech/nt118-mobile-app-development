package com.aicoach.api.home;

import com.aicoach.api.common.VnTime;
import com.aicoach.api.plan.PlanService.PlanDayDto;
import com.aicoach.api.plan.PlanService.PlanItemDto;
import com.aicoach.api.session.WorkoutSession;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tính tiến độ giáo án từ các bài đã lưu (thuần Java, không cần DB → unit test được).
 *
 * Quy tắc:
 *  - Mỗi bài lưu kèm planDay (ngày của giáo án). Một buổi giáo án XONG khi MỌI bài của ngày đó
 *    đều đã có ít nhất 1 lần tập đạt ≥ 50% mục tiêu.
 *  - Các buổi đi theo thứ tự: buổi kế tiếp chỉ mở khi buổi trước đã xong.
 *  - Dữ liệu cũ (trước V5, planDay = null) giữ quy tắc cũ: mỗi ngày có tập theo giáo án = 1 buổi.
 */
public final class PlanProgress {

    /** Kết quả: số buổi đã xong + các bài đã tập của buổi đang làm (để app bỏ qua, không bắt tập lại). */
    public record Result(int sessionsDone, Integer currentDayIndex, List<String> doneExerciseIds) {
    }

    private PlanProgress() {
    }

    /** Một bài được tính "đã tập" cho giáo án khi đạt ít nhất một nửa mục tiêu. */
    static boolean countsTowardPlan(WorkoutSession s) {
        return s.getTargetReps() <= 0 || s.getCorrectReps() * 2 >= s.getTargetReps();
    }

    public static Result compute(List<PlanDayDto> days, List<WorkoutSession> planSessions) {
        Set<LocalDate> legacyDates = new HashSet<>();
        Map<Integer, Set<String>> doneByDay = new HashMap<>();
        for (WorkoutSession s : planSessions) {
            if (s.getPlanDay() == null) {
                legacyDates.add(VnTime.dateOf(s.getStartedAt()));
            } else if (countsTowardPlan(s)) {
                doneByDay.computeIfAbsent(s.getPlanDay(), k -> new HashSet<>()).add(s.getExerciseId());
            }
        }

        int done = Math.min(legacyDates.size(), days.size());
        while (done < days.size() && isComplete(days.get(done), doneByDay)) {
            done++;
        }

        if (done >= days.size()) {
            return new Result(done, null, List.of());
        }
        PlanDayDto current = days.get(done);
        Set<String> doneToday = doneByDay.getOrDefault(current.dayIndex(), Set.of());
        List<String> ordered = new ArrayList<>();
        for (PlanItemDto item : current.items()) {
            if (doneToday.contains(item.exerciseId())) ordered.add(item.exerciseId());
        }
        return new Result(done, current.dayIndex(), ordered);
    }

    private static boolean isComplete(PlanDayDto day, Map<Integer, Set<String>> doneByDay) {
        Set<String> done = doneByDay.get(day.dayIndex());
        if (done == null || day.items().isEmpty()) return false;
        for (PlanItemDto item : day.items()) {
            if (!done.contains(item.exerciseId())) return false;
        }
        return true;
    }
}
