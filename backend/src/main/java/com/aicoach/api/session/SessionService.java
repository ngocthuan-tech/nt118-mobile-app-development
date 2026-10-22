package com.aicoach.api.session;

import com.aicoach.api.common.ApiException;
import com.aicoach.api.exercise.Exercise;
import com.aicoach.api.exercise.ExerciseRepository;
import com.aicoach.api.plan.PlanRepository;
import com.aicoach.api.progress.ProgressService;
import com.aicoach.api.user.UserAccount;
import com.aicoach.api.user.UserService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SessionService {

    /** Dữ liệu module tập AI gửi lên khi kết thúc buổi tập (màn Kết quả). */
    public record CreateSessionRequest(
            @NotBlank(message = "Thiếu mã bài tập")
            String exerciseId,
            Instant startedAt,
            @Min(value = 0, message = "Thời lượng không hợp lệ")
            @Max(value = 14400, message = "Thời lượng không hợp lệ")
            int durationSec,
            @Min(value = 0, message = "Mục tiêu không hợp lệ")
            int targetReps,
            @Min(value = 0, message = "Số rep không hợp lệ")
            @Max(value = 10000, message = "Số rep không hợp lệ")
            int totalReps,
            @Min(value = 0, message = "Số rep đúng không hợp lệ")
            int correctReps,
            Double calories,
            @Size(max = 20, message = "Quá nhiều loại lỗi tư thế")
            Map<String, Long> formErrors,
            String planId) {
    }

    public record SessionDto(
            String id,
            String exerciseId,
            String exerciseName,
            Instant startedAt,
            int durationSec,
            int targetReps,
            int totalReps,
            int correctReps,
            double accuracy,
            double calories,
            Map<String, Long> formErrors,
            String planId,
            long xpEarned) {
    }

    private final WorkoutSessionRepository sessions;
    private final ExerciseRepository exercises;
    private final PlanRepository plans;
    private final UserService userService;
    private final ProgressService progress;
    private final Clock clock;

    public SessionService(WorkoutSessionRepository sessions, ExerciseRepository exercises, PlanRepository plans,
                          UserService userService, ProgressService progress, Clock clock) {
        this.sessions = sessions;
        this.exercises = exercises;
        this.plans = plans;
        this.userService = userService;
        this.progress = progress;
        this.clock = clock;
    }

    @Transactional
    public SessionDto create(String userId, CreateSessionRequest req) {
        UserAccount user = userService.require(userId);
        Exercise ex = exercises.findById(req.exerciseId())
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy bài tập"));
        if (req.correctReps() > req.totalReps()) {
            throw ApiException.badRequest("Số rep đúng không được lớn hơn tổng số rep");
        }
        Instant now = clock.instant();
        Instant startedAt = req.startedAt() != null ? req.startedAt() : now;
        if (startedAt.isAfter(now.plus(Duration.ofMinutes(5)))) {
            throw ApiException.badRequest("Thời điểm bắt đầu không được ở tương lai");
        }
        String planId = req.planId() == null || req.planId().isBlank() ? null : req.planId().trim();
        if (planId != null && !plans.existsById(planId)) {
            throw ApiException.notFound("Không tìm thấy giáo án");
        }

        WorkoutSession s = new WorkoutSession();
        s.setUserId(userId);
        s.setExerciseId(ex.getId());
        s.setExerciseName(ex.getName());
        s.setStartedAt(startedAt);
        s.setDurationSec(req.durationSec());
        s.setTargetReps(req.targetReps());
        s.setTotalReps(req.totalReps());
        s.setCorrectReps(req.correctReps());
        s.setAccuracy(req.totalReps() == 0 ? 0 : round((double) req.correctReps() / req.totalReps(), 3));
        double kcal = req.calories() != null ? req.calories() : ex.getCaloriesPerRep() * req.totalReps();
        s.setCalories(round(Math.max(0, kcal), 1));
        Map<String, Long> errors = new HashMap<>();
        if (req.formErrors() != null) {
            req.formErrors().forEach((code, count) -> {
                if (code != null && !code.isBlank() && code.length() <= 40 && count != null && count > 0) {
                    errors.put(code.trim(), count);
                }
            });
        }
        s.setFormErrors(errors);
        s.setPlanId(planId);
        sessions.save(s);

        progress.applySession(user, startedAt, req.totalReps(), req.correctReps());
        return toDto(s);
    }

    @Transactional(readOnly = true)
    public List<SessionDto> list(String userId, int limit) {
        int size = Math.max(1, Math.min(limit, 100));
        return sessions.findByUserIdOrderByStartedAtDesc(userId, PageRequest.of(0, size))
                .stream().map(SessionService::toDto).toList();
    }

    @Transactional(readOnly = true)
    public SessionDto get(String userId, String id) {
        return sessions.findByIdAndUserId(id, userId).map(SessionService::toDto)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy buổi tập"));
    }

    public static SessionDto toDto(WorkoutSession s) {
        return new SessionDto(s.getId(), s.getExerciseId(), s.getExerciseName(), s.getStartedAt(),
                s.getDurationSec(), s.getTargetReps(), s.getTotalReps(), s.getCorrectReps(), s.getAccuracy(),
                s.getCalories(), Map.copyOf(s.getFormErrors()), s.getPlanId(),
                ProgressService.xpFor(s.getTotalReps(), s.getCorrectReps()));
    }

    private static double round(double value, int digits) {
        double f = Math.pow(10, digits);
        return Math.round(value * f) / f;
    }
}
