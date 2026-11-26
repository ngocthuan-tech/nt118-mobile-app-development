package com.aicoach.api.admin;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Request / response của web admin (/api/v1/admin/**). Ngày giờ trả về theo ISO; ngày thống kê theo giờ Việt Nam. */
public final class AdminDtos {

    private AdminDtos() {
    }

    // ---------------------------------------------------------------- Tài khoản admin đang đăng nhập

    public record AdminMe(String id, String email, String displayName, String role) {
    }

    // ---------------------------------------------------------------- Tổng quan

    public record DayCount(LocalDate date, long count) {
    }

    public record ExerciseCount(String exerciseId, String name, long sessions) {
    }

    /** key = mã lỗi AI (knees_in, hip_sag…) — web tự đổi sang tên tiếng Việt. */
    public record FormErrorCount(String key, long count) {
    }

    public record Stats(
            int days,
            long totalUsers,            // người dùng (không tính admin)
            long newUsers,              // đăng ký trong khoảng ngày
            long activeUsers,           // có ít nhất 1 buổi tập trong khoảng ngày
            long totalSessions,         // tổng buổi tập từ trước tới nay
            long sessionsInRange,       // buổi tập trong khoảng ngày
            double correctRate,         // % lần làm đúng / tổng lần trong khoảng ngày (AI chấm)
            long activeChallenges,      // thử thách còn hạn
            List<DayCount> sessionsPerDay,
            List<DayCount> signupsPerDay,
            List<ExerciseCount> topExercises,
            List<FormErrorCount> topFormErrors) {
    }

    // ---------------------------------------------------------------- Người dùng

    public record UserRow(
            String id,
            String email,
            String displayName,
            String role,
            boolean locked,
            int streak,
            long totalXp,
            long sessionCount,
            Instant createdAt,
            LocalDate lastWorkoutDate) {
    }

    public record PageResult<T>(List<T> items, long total, int page, int size) {
    }

    public record SessionRow(
            String id,
            String exerciseId,
            String exerciseName,
            Instant startedAt,
            int durationSec,
            int totalReps,
            int correctReps,
            double accuracy,
            String planId) {
    }

    public record UserDetail(
            UserRow user,
            String goal,
            String level,
            Double heightCm,
            Double weightKg,
            int weeklyGoalSessions,
            String activePlanId,
            String activePlanTitle,
            long totalReps,
            List<SessionRow> recentSessions) {
    }

    public record LockRequest(@NotNull(message = "Thiếu trạng thái khoá") Boolean locked) {
    }

    // ---------------------------------------------------------------- Bài tập

    public record ExerciseRow(
            String id,
            String name,
            String muscleGroup,
            String difficulty,
            String measureType,
            String description,
            int defaultSets,
            int defaultReps,
            double caloriesPerRep,
            boolean adminEdited,
            long sessionCount) {
    }

    public record ExerciseUpdate(
            @NotBlank(message = "Tên bài tập không được để trống")
            @Size(min = 2, max = 50, message = "Tên bài tập cần 2–50 ký tự") String name,
            @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự") String description,
            @Min(value = 1, message = "Số hiệp từ 1 đến 10") @Max(value = 10, message = "Số hiệp từ 1 đến 10")
            int defaultSets,
            @Min(value = 1, message = "Số lần / giây từ 1 đến 200")
            @Max(value = 200, message = "Số lần / giây từ 1 đến 200") int defaultReps,
            @DecimalMin(value = "0.0", message = "Kcal mỗi lần từ 0 đến 5")
            @DecimalMax(value = "5.0", message = "Kcal mỗi lần từ 0 đến 5") double caloriesPerRep) {
    }

    // ---------------------------------------------------------------- Giáo án

    public record PlanItemRow(Long id, String exerciseId, String exerciseName, String measureType, int sets, int reps) {
    }

    public record PlanDayRow(int dayIndex, List<PlanItemRow> items) {
    }

    public record PlanRow(
            String id,
            String title,
            String level,
            int durationDays,
            int sessionsPerWeek,
            int minutesPerSession,
            boolean adminEdited,
            long followers,
            List<PlanDayRow> days) {
    }

    public record PlanItemUpdate(
            @Min(value = 1, message = "Số hiệp từ 1 đến 10") @Max(value = 10, message = "Số hiệp từ 1 đến 10") int sets,
            @Min(value = 1, message = "Số lần / giây từ 1 đến 200")
            @Max(value = 200, message = "Số lần / giây từ 1 đến 200") int reps) {
    }
}
