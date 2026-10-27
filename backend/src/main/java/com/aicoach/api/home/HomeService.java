package com.aicoach.api.home;

import com.aicoach.api.common.VnTime;
import com.aicoach.api.exercise.Exercise;
import com.aicoach.api.plan.Plan;
import com.aicoach.api.plan.PlanRepository;
import com.aicoach.api.plan.PlanService;
import com.aicoach.api.plan.PlanService.PlanDayDto;
import com.aicoach.api.plan.PlanService.PlanItemDto;
import com.aicoach.api.progress.ProgressService;
import com.aicoach.api.session.WorkoutSession;
import com.aicoach.api.session.WorkoutSessionRepository;
import com.aicoach.api.user.UserAccount;
import com.aicoach.api.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Gộp mọi số liệu Trang chủ (Figma 21) vào MỘT API để app chỉ gọi một lần:
 * lời chào, chuỗi ngày, tiến độ tuần T2–CN, phút/kcal hôm nay, giáo án đang theo và bài tập hôm nay.
 */
@Service
public class HomeService {

    private static final String[] DAY_LABELS = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
    private static final List<String> DEFAULT_WORKOUT = List.of("squat", "pushup", "plank");

    public record WeekDay(String label, LocalDate date, boolean active, boolean today) {
    }

    public record WeekProgress(int goalSessions, int doneSessions, int remainingSessions, List<WeekDay> days) {
    }

    public record TodaySummary(int sessions, int minutes, double calories) {
    }

    public record ActivePlan(String planId, String title, String coverImageName, int currentWeek, int totalWeeks,
                             int sessionsDone, int totalSessions, int minutesPerSession, int progressPercent,
                             boolean completed) {
    }

    public record TodayWorkout(Integer planDayIndex, int estimatedMinutes, List<PlanItemDto> exercises) {
    }

    public record HomeDto(String displayName, String avatarUrl, int streak, long weeklyXp, WeekProgress week,
                          TodaySummary today, double weekCalories, ActivePlan activePlan,
                          TodayWorkout todayWorkout) {
    }

    private final UserService userService;
    private final ProgressService progress;
    private final WorkoutSessionRepository sessions;
    private final PlanRepository plans;
    private final PlanService planService;
    private final Clock clock;

    public HomeService(UserService userService, ProgressService progress, WorkoutSessionRepository sessions,
                       PlanRepository plans, PlanService planService, Clock clock) {
        this.userService = userService;
        this.progress = progress;
        this.sessions = sessions;
        this.plans = plans;
        this.planService = planService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public HomeDto getHome(String userId) {
        UserAccount user = userService.require(userId);
        LocalDate today = VnTime.today(clock);
        LocalDate monday = VnTime.mondayOf(today);

        List<WorkoutSession> weekSessions = sessions
                .findByUserIdAndStartedAtGreaterThanEqualAndStartedAtLessThanOrderByStartedAtAsc(
                        userId, VnTime.startOf(monday), VnTime.startOf(monday.plusDays(7)));

        Set<LocalDate> activeDays = weekSessions.stream()
                .map(s -> VnTime.dateOf(s.getStartedAt()))
                .collect(Collectors.toSet());
        List<WeekDay> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = monday.plusDays(i);
            days.add(new WeekDay(DAY_LABELS[i], d, activeDays.contains(d), d.equals(today)));
        }
        int goal = Math.max(1, user.getWeeklyGoalSessions());
        WeekProgress week = new WeekProgress(goal, activeDays.size(), Math.max(0, goal - activeDays.size()), days);

        List<WorkoutSession> todaySessions = weekSessions.stream()
                .filter(s -> VnTime.dateOf(s.getStartedAt()).equals(today))
                .toList();
        int todaySeconds = todaySessions.stream().mapToInt(WorkoutSession::getDurationSec).sum();
        double todayKcal = todaySessions.stream().mapToDouble(WorkoutSession::getCalories).sum();
        double weekKcal = weekSessions.stream().mapToDouble(WorkoutSession::getCalories).sum();
        TodaySummary todaySummary = new TodaySummary(todaySessions.size(), Math.round(todaySeconds / 60f),
                round1(todayKcal));

        Map<String, Exercise> exerciseById = planService.exerciseMap();
        ActivePlan activePlan = null;
        TodayWorkout todayWorkout = null;

        Plan plan = user.getActivePlanId() == null ? null : plans.findById(user.getActivePlanId()).orElse(null);
        if (plan != null) {
            LocalDate start = user.getPlanStartDate() != null ? user.getPlanStartDate() : today;
            // Một buổi của giáo án = một ngày có tập theo giáo án (một ngày có thể lưu nhiều bài).
            int done = (int) sessions.findByUserIdAndPlanIdAndStartedAtGreaterThanEqual(
                            userId, plan.getId(), VnTime.startOf(start)).stream()
                    .map(s -> VnTime.dateOf(s.getStartedAt()))
                    .distinct()
                    .count();
            List<PlanDayDto> planDays = PlanService.days(plan, exerciseById);
            int total = planDays.size();
            int totalWeeks = Math.max(1, (int) Math.ceil(plan.getDurationDays() / 7.0));
            int currentWeek = Math.min(totalWeeks, done / Math.max(1, plan.getSessionsPerWeek()) + 1);
            boolean completed = total > 0 && done >= total;
            int percent = total == 0 ? 0 : Math.min(100, Math.round(done * 100f / total));
            activePlan = new ActivePlan(plan.getId(), plan.getTitle(), plan.getCoverImageName(), currentWeek,
                    totalWeeks, Math.min(done, total), total, plan.getMinutesPerSession(), percent, completed);
            if (total > 0) {
                PlanDayDto next = planDays.get(Math.min(done, total - 1));
                todayWorkout = new TodayWorkout(next.dayIndex(), plan.getMinutesPerSession(), next.items());
            }
        }
        if (todayWorkout == null) {
            todayWorkout = defaultWorkout(exerciseById);
        }

        return new HomeDto(user.getDisplayName(), user.getAvatarUrl(), progress.effectiveStreak(user),
                progress.effectiveWeeklyXp(user), week, todaySummary, round1(weekKcal), activePlan, todayWorkout);
    }

    /** Chưa chọn giáo án: gợi ý 3 bài cơ bản với số hiệp/rep mặc định. */
    private TodayWorkout defaultWorkout(Map<String, Exercise> byId) {
        List<PlanItemDto> items = new ArrayList<>();
        int seconds = 0;
        for (String id : DEFAULT_WORKOUT) {
            Exercise e = byId.get(id);
            if (e == null) {
                continue;
            }
            items.add(new PlanItemDto(e.getId(), e.getName(), e.getImageName(), e.getMeasureType(),
                    e.getDefaultSets(), e.getDefaultReps()));
            int perSet = "seconds".equals(e.getMeasureType()) ? e.getDefaultReps() : e.getDefaultReps() * 3;
            seconds += e.getDefaultSets() * (perSet + 30);
        }
        return new TodayWorkout(null, (int) Math.ceil(seconds / 60.0), items);
    }

    private static double round1(double v) {
        return Math.round(v * 10) / 10.0;
    }
}
