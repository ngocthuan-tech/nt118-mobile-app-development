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

    /** GET /home/streak — thẻ "Chuỗi 7 ngày": số ngày liên tiếp + 7 chấm T2–CN của tuần này. */
    public record StreakDto(int streakDays, LocalDate lastWorkoutDate, int goalSessions, int doneSessions,
                            int remainingSessions, List<WeekDay> days) {
    }

    public record TodaySummary(int sessions, int minutes, double calories) {
    }

    public record ActivePlan(String planId, String title, String coverImageName, int currentWeek, int totalWeeks,
                             int sessionsDone, int totalSessions, int minutesPerSession, int progressPercent,
                             boolean completed) {
    }

    /** GET /home/today-workout — dải "Bài tập hôm nay". planId = null khi chưa chọn giáo án (gợi ý mặc định). */
    public record TodayWorkoutDto(String planId, String planTitle, Integer planDayIndex, int estimatedMinutes,
                                  List<PlanItemDto> exercises) {
    }

    /** GET /home — gộp mọi thứ cho Trang chủ (giữ lại để tương thích). */
    public record HomeDto(String displayName, String avatarUrl, int streak, long weeklyXp, StreakDto week,
                          TodaySummary today, double weekCalories, ActivePlan activePlan,
                          TodayWorkoutDto todayWorkout) {
    }

    /** Kết quả tính giáo án đang theo, dùng chung cho /home và /home/today-workout. */
    private record PlanState(ActivePlan activePlan, TodayWorkoutDto todayWorkout) {
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
    public StreakDto getStreak(String userId) {
        UserAccount user = userService.require(userId);
        return buildStreak(user, weekSessions(userId), VnTime.today(clock));
    }

    @Transactional(readOnly = true)
    public TodayWorkoutDto getTodayWorkout(String userId) {
        UserAccount user = userService.require(userId);
        return planState(user, VnTime.today(clock), planService.exerciseMap()).todayWorkout();
    }

    @Transactional(readOnly = true)
    public HomeDto getHome(String userId) {
        UserAccount user = userService.require(userId);
        LocalDate today = VnTime.today(clock);
        List<WorkoutSession> week = weekSessions(userId);

        List<WorkoutSession> todaySessions = week.stream()
                .filter(s -> VnTime.dateOf(s.getStartedAt()).equals(today))
                .toList();
        int todaySeconds = todaySessions.stream().mapToInt(WorkoutSession::getDurationSec).sum();
        double todayKcal = todaySessions.stream().mapToDouble(WorkoutSession::getCalories).sum();
        double weekKcal = week.stream().mapToDouble(WorkoutSession::getCalories).sum();
        TodaySummary todaySummary = new TodaySummary(todaySessions.size(), Math.round(todaySeconds / 60f),
                round1(todayKcal));

        PlanState plan = planState(user, today, planService.exerciseMap());
        return new HomeDto(user.getDisplayName(), user.getAvatarUrl(), progress.effectiveStreak(user),
                progress.effectiveWeeklyXp(user), buildStreak(user, week, today), todaySummary, round1(weekKcal),
                plan.activePlan(), plan.todayWorkout());
    }

    /** Các buổi tập từ thứ Hai đến hết Chủ nhật tuần này (giờ VN) — một truy vấn, có index (user_id, started_at). */
    private List<WorkoutSession> weekSessions(String userId) {
        LocalDate monday = VnTime.mondayOf(VnTime.today(clock));
        return sessions.findByUserIdAndStartedAtGreaterThanEqualAndStartedAtLessThanOrderByStartedAtAsc(
                userId, VnTime.startOf(monday), VnTime.startOf(monday.plusDays(7)));
    }

    private StreakDto buildStreak(UserAccount user, List<WorkoutSession> week, LocalDate today) {
        LocalDate monday = VnTime.mondayOf(today);
        Set<LocalDate> activeDays = week.stream()
                .map(s -> VnTime.dateOf(s.getStartedAt()))
                .collect(Collectors.toSet());
        List<WeekDay> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = monday.plusDays(i);
            days.add(new WeekDay(DAY_LABELS[i], d, activeDays.contains(d), d.equals(today)));
        }
        int goal = Math.max(1, user.getWeeklyGoalSessions());
        return new StreakDto(progress.effectiveStreak(user), user.getLastWorkoutDate(), goal, activeDays.size(),
                Math.max(0, goal - activeDays.size()), days);
    }

    private PlanState planState(UserAccount user, LocalDate today, Map<String, Exercise> exerciseById) {
        Plan plan = user.getActivePlanId() == null ? null : plans.findById(user.getActivePlanId()).orElse(null);
        if (plan == null) {
            return new PlanState(null, defaultWorkout(exerciseById));
        }
        LocalDate start = user.getPlanStartDate() != null ? user.getPlanStartDate() : today;
        // Một buổi của giáo án = một ngày có tập theo giáo án (một ngày có thể lưu nhiều bài).
        int done = (int) sessions.findByUserIdAndPlanIdAndStartedAtGreaterThanEqual(
                        user.getId(), plan.getId(), VnTime.startOf(start)).stream()
                .map(s -> VnTime.dateOf(s.getStartedAt()))
                .distinct()
                .count();
        List<PlanDayDto> planDays = PlanService.days(plan, exerciseById);
        int total = planDays.size();
        int totalWeeks = Math.max(1, (int) Math.ceil(plan.getDurationDays() / 7.0));
        int currentWeek = Math.min(totalWeeks, done / Math.max(1, plan.getSessionsPerWeek()) + 1);
        boolean completed = total > 0 && done >= total;
        int percent = total == 0 ? 0 : Math.min(100, Math.round(done * 100f / total));
        ActivePlan active = new ActivePlan(plan.getId(), plan.getTitle(), plan.getCoverImageName(), currentWeek,
                totalWeeks, Math.min(done, total), total, plan.getMinutesPerSession(), percent, completed);
        if (total == 0) {
            return new PlanState(active, defaultWorkout(exerciseById));
        }
        PlanDayDto next = planDays.get(Math.min(done, total - 1));
        return new PlanState(active, new TodayWorkoutDto(plan.getId(), plan.getTitle(), next.dayIndex(),
                plan.getMinutesPerSession(), next.items()));
    }

    /** Chưa chọn giáo án: gợi ý 3 bài cơ bản với số hiệp/rep mặc định. */
    private TodayWorkoutDto defaultWorkout(Map<String, Exercise> byId) {
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
        return new TodayWorkoutDto(null, null, null, (int) Math.ceil(seconds / 60.0), items);
    }

    private static double round1(double v) {
        return Math.round(v * 10) / 10.0;
    }
}
