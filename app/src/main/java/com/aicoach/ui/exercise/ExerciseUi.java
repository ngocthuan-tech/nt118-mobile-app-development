package com.aicoach.ui.exercise;

import android.content.Context;

import androidx.annotation.ArrayRes;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.aicoach.R;
import com.aicoach.data.model.Exercise;

/**
 * Đổi mã từ API (muscleGroup, difficulty, measureType) sang chữ / màu hiển thị.
 * Dùng chung cho Thư viện (23), Bộ lọc (24), Chi tiết (25), Trang chủ (21), Giáo án (17, 26).
 */
public final class ExerciseUi {

    public static final String GROUP_LEGS = "legs";
    public static final String GROUP_CHEST_ARMS = "chest_arms";
    public static final String GROUP_CORE = "core";
    public static final String GROUP_CARDIO = "cardio";

    /** Thứ tự chip nhóm cơ trên màn Thư viện (sau chip "Tất cả"). */
    public static final String[] GROUPS = {GROUP_LEGS, GROUP_CHEST_ARMS, GROUP_CORE, GROUP_CARDIO};

    public static final String DIFF_EASY = "easy";
    public static final String DIFF_MEDIUM = "medium";
    public static final String DIFF_HARD = "hard";

    public static final String MEASURE_REPS = "reps";
    public static final String MEASURE_SECONDS = "seconds";

    /** Bài cần dụng cụ (ghế) — bị ẩn khi bật "Không cần dụng cụ" ở Bộ lọc. */
    public static final String NEEDS_EQUIPMENT = "chair_dip";

    private static final int SECONDS_PER_REP = 3;
    private static final int REST_BETWEEN_SETS_SEC = 60;
    private static final int WARMUP_SEC = 120;

    private ExerciseUi() {
    }

    @StringRes
    public static int groupLabel(@Nullable String group) {
        if (group == null) return R.string.exercise_group_all;
        switch (group) {
            case GROUP_LEGS: return R.string.exercise_group_legs;
            case GROUP_CHEST_ARMS: return R.string.exercise_group_chest_arms;
            case GROUP_CORE: return R.string.exercise_group_core;
            case GROUP_CARDIO: return R.string.exercise_group_cardio;
            default: return R.string.exercise_group_all;
        }
    }

    @StringRes
    public static int difficultyLabel(@Nullable String difficulty) {
        if (DIFF_MEDIUM.equals(difficulty)) return R.string.exercise_difficulty_medium;
        if (DIFF_HARD.equals(difficulty)) return R.string.exercise_difficulty_hard;
        return R.string.exercise_difficulty_easy;
    }

    @ColorRes
    public static int difficultyColor(@Nullable String difficulty) {
        if (DIFF_MEDIUM.equals(difficulty)) return R.color.state_warning;
        if (DIFF_HARD.equals(difficulty)) return R.color.state_error;
        return R.color.state_success;
    }

    @ColorRes
    public static int difficultyBg(@Nullable String difficulty) {
        if (DIFF_MEDIUM.equals(difficulty)) return R.color.state_warning_light;
        if (DIFF_HARD.equals(difficulty)) return R.color.state_error_light;
        return R.color.state_success_light;
    }

    @StringRes
    public static int aiModeLabel(boolean timeBased) {
        return timeBased ? R.string.exercise_ai_timer : R.string.exercise_ai_counter;
    }

    /** "3 × 15 lần" hoặc "3 × 45 giây". */
    @NonNull
    public static String volume(@NonNull Context c, int sets, int reps, boolean timeBased) {
        return c.getString(timeBased ? R.string.exercise_volume_seconds : R.string.exercise_volume_reps, sets, reps);
    }

    /** Ước lượng thời gian 1 bài (phút): thời gian tập + nghỉ giữa hiệp + khởi động. */
    public static int estimatedMinutes(int sets, int reps, boolean timeBased) {
        int work = timeBased ? sets * reps : sets * reps * SECONDS_PER_REP;
        int rest = Math.max(0, sets - 1) * REST_BETWEEN_SETS_SEC;
        return (int) Math.ceil((work + rest + WARMUP_SEC) / 60.0);
    }

    /** kcal ước tính theo hệ số của server (kcal mỗi lần, hoặc mỗi giây với bài tính giờ). */
    public static int estimatedKcal(@NonNull Exercise e) {
        return (int) Math.round(e.getCaloriesPerRep() * e.getDefaultSets() * e.getDefaultReps());
    }

    /** Gợi ý đặt camera hiện trên ảnh ở Chi tiết bài tập (Figma 25). */
    @StringRes
    public static int cameraHint(@Nullable String exerciseId) {
        if (exerciseId == null) return R.string.exercise_camera_front;
        switch (exerciseId) {
            case "squat":
            case "lunge":
            case "toe_touch":
                return R.string.exercise_camera_angled;
            case "pushup":
            case "plank":
            case "glute_bridge":
            case "chair_dip":
                return R.string.exercise_camera_side;
            default:
                return R.string.exercise_camera_front;
        }
    }

    /** "AI sẽ theo dõi" — các điểm AI kiểm tra cho từng bài (theo trang Đặc tả AI trong Figma). */
    @ArrayRes
    public static int aiChecks(@Nullable String exerciseId) {
        if (exerciseId == null) return R.array.exercise_ai_checks_squat;
        switch (exerciseId) {
            case "pushup": return R.array.exercise_ai_checks_pushup;
            case "plank": return R.array.exercise_ai_checks_plank;
            case "lunge": return R.array.exercise_ai_checks_lunge;
            case "glute_bridge": return R.array.exercise_ai_checks_glute_bridge;
            case "chair_dip": return R.array.exercise_ai_checks_chair_dip;
            case "jumping_jack": return R.array.exercise_ai_checks_jumping_jack;
            case "high_knees": return R.array.exercise_ai_checks_high_knees;
            case "toe_touch": return R.array.exercise_ai_checks_toe_touch;
            default: return R.array.exercise_ai_checks_squat;
        }
    }
}
