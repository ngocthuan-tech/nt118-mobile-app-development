package com.aicoach.api.user;

import com.aicoach.api.common.ApiException;
import com.aicoach.api.common.VnTime;
import com.aicoach.api.plan.PlanRepository;
import com.aicoach.api.progress.ProgressService;
import com.aicoach.api.user.UserDtos.ChangePasswordRequest;
import com.aicoach.api.user.UserDtos.UpdateProfileRequest;
import com.aicoach.api.user.UserDtos.UserDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class UserService {

    private final UserRepository users;
    private final PlanRepository plans;
    private final ProgressService progress;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public UserService(UserRepository users, PlanRepository plans, ProgressService progress,
                       PasswordEncoder passwordEncoder, Clock clock) {
        this.users = users;
        this.plans = plans;
        this.progress = progress;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    /** Token hợp lệ nhưng tài khoản đã bị xoá thì coi như phiên hết hạn. */
    public UserAccount require(String userId) {
        return users.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("Phiên đăng nhập không hợp lệ hoặc đã hết hạn"));
    }

    @Transactional(readOnly = true)
    public UserDto getMe(String userId) {
        return toDto(require(userId));
    }

    @Transactional
    public UserDto updateMe(String userId, UpdateProfileRequest req) {
        UserAccount u = require(userId);

        if (req.displayName() != null) {
            String name = req.displayName().trim();
            if (name.length() < 2) {
                throw ApiException.badRequest("Tên hiển thị cần 2–50 ký tự");
            }
            u.setDisplayName(name);
        }
        if (req.avatarUrl() != null) u.setAvatarUrl(req.avatarUrl().isBlank() ? null : req.avatarUrl().trim());
        if (req.heightCm() != null) u.setHeightCm(req.heightCm());
        if (req.weightKg() != null) u.setWeightKg(req.weightKg());
        if (req.goal() != null) u.setGoal(req.goal());
        if (req.level() != null) u.setLevel(req.level());
        if (req.weeklyGoalSessions() != null) u.setWeeklyGoalSessions(req.weeklyGoalSessions());
        if (req.profileCompleted() != null) u.setProfileCompleted(req.profileCompleted());

        if (req.activePlanId() != null) {
            String planId = req.activePlanId().trim();
            if (planId.isEmpty()) {
                u.setActivePlanId(null);
                u.setPlanStartDate(null);
            } else if (!planId.equals(u.getActivePlanId())) {
                if (!plans.existsById(planId)) {
                    throw ApiException.notFound("Không tìm thấy giáo án");
                }
                u.setActivePlanId(planId);
                u.setPlanStartDate(VnTime.today(clock));
            }
        }
        return toDto(u);
    }

    @Transactional
    public void changePassword(String userId, ChangePasswordRequest req) {
        UserAccount u = require(userId);
        if (!passwordEncoder.matches(req.currentPassword(), u.getPasswordHash())) {
            throw ApiException.badRequest("Mật khẩu hiện tại không đúng");
        }
        if (req.currentPassword().equals(req.newPassword())) {
            throw ApiException.badRequest("Mật khẩu mới phải khác mật khẩu hiện tại");
        }
        u.setPasswordHash(passwordEncoder.encode(req.newPassword()));
    }

    public UserDto toDto(UserAccount u) {
        return new UserDto(
                u.getId(),
                u.getDisplayName(),
                u.getEmail(),
                u.getAvatarUrl(),
                u.getHeightCm(),
                u.getWeightKg(),
                u.getGoal(),
                u.getLevel(),
                u.getWeeklyGoalSessions(),
                u.isProfileCompleted(),
                u.getCreatedAt(),
                u.getActivePlanId(),
                u.getPlanStartDate(),
                progress.effectiveStreak(u),
                u.getLastWorkoutDate(),
                progress.effectiveWeeklyXp(u),
                u.getTotalXp(),
                u.getTotalReps());
    }
}
