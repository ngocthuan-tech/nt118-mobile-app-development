package com.aicoach.api.user;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;

/** Các DTO của module người dùng. Tên field khớp model User bên app Android. */
public final class UserDtos {

    private UserDtos() {
    }

    public static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$";
    public static final String PASSWORD_MESSAGE = "Mật khẩu cần 8–64 ký tự, có cả chữ và số";

    /** Hồ sơ + tiến độ trả về cho app. Không bao giờ chứa mật khẩu. */
    public record UserDto(
            String id,
            String displayName,
            String email,
            String avatarUrl,
            Double heightCm,
            Double weightKg,
            String goal,
            String level,
            int weeklyGoalSessions,
            boolean profileCompleted,
            Instant createdAt,
            String activePlanId,
            LocalDate planStartDate,
            int streak,
            LocalDate lastWorkoutDate,
            long weeklyXp,
            long totalXp,
            long totalReps) {
    }

    /**
     * Sửa hồ sơ — chỉ field nào gửi lên (khác null) mới được cập nhật.
     * activePlanId = "" nghĩa là bỏ giáo án đang theo.
     */
    public record UpdateProfileRequest(
            @Size(min = 2, max = 50, message = "Tên hiển thị cần 2–50 ký tự")
            String displayName,

            @Size(max = 500, message = "Đường dẫn ảnh đại diện quá dài")
            String avatarUrl,

            @DecimalMin(value = "100", message = "Chiều cao phải từ 100 đến 250 cm")
            @DecimalMax(value = "250", message = "Chiều cao phải từ 100 đến 250 cm")
            Double heightCm,

            @DecimalMin(value = "30", message = "Cân nặng phải từ 30 đến 250 kg")
            @DecimalMax(value = "250", message = "Cân nặng phải từ 30 đến 250 kg")
            Double weightKg,

            @Pattern(regexp = "lose_weight|build_muscle|stay_fit",
                    message = "Mục tiêu chỉ nhận lose_weight, build_muscle hoặc stay_fit")
            String goal,

            @Pattern(regexp = "beginner|intermediate|advanced",
                    message = "Trình độ chỉ nhận beginner, intermediate hoặc advanced")
            String level,

            @Min(value = 1, message = "Số buổi mỗi tuần phải từ 1 đến 7")
            @Max(value = 7, message = "Số buổi mỗi tuần phải từ 1 đến 7")
            Integer weeklyGoalSessions,

            Boolean profileCompleted,

            @Size(max = 64, message = "Mã giáo án không hợp lệ")
            String activePlanId) {
    }

    public record ChangePasswordRequest(
            @NotBlank(message = "Vui lòng nhập mật khẩu hiện tại")
            String currentPassword,

            @NotBlank(message = "Vui lòng nhập mật khẩu mới")
            @Pattern(regexp = PASSWORD_REGEX, message = PASSWORD_MESSAGE)
            String newPassword) {
    }
}
