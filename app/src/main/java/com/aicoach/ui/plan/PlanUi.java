package com.aicoach.ui.plan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.aicoach.R;
import com.aicoach.data.model.Plan;

import java.util.ArrayList;
import java.util.List;

/** Tính toán hiển thị cho giáo án (Figma 17, 21, 26): nhãn ngày, số phút mỗi buổi, tên các bài. */
public final class PlanUi {

    public static final String[] WEEK_DAYS = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};

    /** Giáo án gợi ý khi người dùng chưa chọn (khớp id trong backend DataSeeder). */
    public static final String DEFAULT_PLAN_ID = "fat-burn-4w";

    private static final int SECONDS_PER_REP = 3;
    private static final int REST_BETWEEN_SETS_SEC = 45;
    private static final int WARMUP_SEC = 120;

    private PlanUi() {
    }

    /** Lịch mặc định theo số buổi/tuần: 3 buổi → T2, T4, T6 (giống Figma 17). */
    @NonNull
    public static List<String> defaultDays(int sessionsPerWeek) {
        int[][] patterns = {
                {0}, {1, 4}, {0, 2, 4}, {0, 1, 3, 4}, {0, 1, 2, 3, 4}, {0, 1, 2, 3, 4, 5}, {0, 1, 2, 3, 4, 5, 6}};
        int n = Math.max(1, Math.min(7, sessionsPerWeek));
        List<String> out = new ArrayList<>();
        for (int i : patterns[n - 1]) out.add(WEEK_DAYS[i]);
        return out;
    }

    /** Nhãn ngày cho buổi thứ index (0-based) trong tuần; days lấy từ khảo sát nếu có. */
    @NonNull
    public static String dayLabel(@Nullable List<String> days, int sessionsPerWeek, int indexInWeek) {
        List<String> source = (days != null && !days.isEmpty()) ? days : defaultDays(sessionsPerWeek);
        return source.get(indexInWeek % source.size());
    }

    /** "Squat • Hít đất • Plank". */
    @NonNull
    public static String exerciseNames(@NonNull Plan.PlanDay day) {
        StringBuilder sb = new StringBuilder();
        if (day.getItems() == null) return "";
        for (Plan.PlanItem item : day.getItems()) {
            if (sb.length() > 0) sb.append(" • ");
            sb.append(item.getExerciseName());
        }
        return sb.toString();
    }

    /** Phút ước tính 1 buổi: thời gian tập + nghỉ giữa hiệp của mọi bài + 1 lần khởi động. */
    public static int dayMinutes(@NonNull Plan.PlanDay day) {
        int seconds = WARMUP_SEC;
        if (day.getItems() == null) return 0;
        for (Plan.PlanItem item : day.getItems()) {
            seconds += item.isTimeBased() ? item.getSets() * item.getReps()
                    : item.getSets() * item.getReps() * SECONDS_PER_REP;
            seconds += Math.max(0, item.getSets() - 1) * REST_BETWEEN_SETS_SEC;
        }
        return (int) Math.ceil(seconds / 60.0);
    }

    public static int totalWeeks(@NonNull Plan plan) {
        return Math.max(1, (int) Math.ceil(plan.getDurationDays() / 7.0));
    }

    public static int sessionsPerWeek(@NonNull Plan plan) {
        return Math.max(1, plan.getSessionsPerWeek());
    }

    /** Các buổi của tuần week (1-based). */
    @NonNull
    public static List<Plan.PlanDay> daysOfWeek(@NonNull Plan plan, int week) {
        int per = sessionsPerWeek(plan);
        int from = (week - 1) * per;
        List<Plan.PlanDay> all = plan.getDays() != null ? plan.getDays() : new ArrayList<>();
        List<Plan.PlanDay> out = new ArrayList<>();
        for (int i = from; i < Math.min(all.size(), from + per); i++) out.add(all.get(i));
        return out;
    }

    @StringRes
    public static int levelLabel(@Nullable String level) {
        if ("intermediate".equals(level)) return R.string.plan_level_intermediate;
        if ("advanced".equals(level)) return R.string.plan_level_advanced;
        return R.string.plan_level_beginner;
    }
}
