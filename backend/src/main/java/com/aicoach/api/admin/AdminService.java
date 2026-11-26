package com.aicoach.api.admin;

import com.aicoach.api.admin.AdminDtos.AdminMe;
import com.aicoach.api.admin.AdminDtos.DayCount;
import com.aicoach.api.admin.AdminDtos.ExerciseCount;
import com.aicoach.api.admin.AdminDtos.ExerciseRow;
import com.aicoach.api.admin.AdminDtos.ExerciseUpdate;
import com.aicoach.api.admin.AdminDtos.FormErrorCount;
import com.aicoach.api.admin.AdminDtos.PageResult;
import com.aicoach.api.admin.AdminDtos.PlanDayRow;
import com.aicoach.api.admin.AdminDtos.PlanItemRow;
import com.aicoach.api.admin.AdminDtos.PlanItemUpdate;
import com.aicoach.api.admin.AdminDtos.PlanRow;
import com.aicoach.api.admin.AdminDtos.SessionRow;
import com.aicoach.api.admin.AdminDtos.Stats;
import com.aicoach.api.admin.AdminDtos.UserDetail;
import com.aicoach.api.admin.AdminDtos.UserRow;
import com.aicoach.api.common.ApiException;
import com.aicoach.api.common.VnTime;
import com.aicoach.api.exercise.Exercise;
import com.aicoach.api.exercise.ExerciseRepository;
import com.aicoach.api.plan.Plan;
import com.aicoach.api.plan.PlanItem;
import com.aicoach.api.plan.PlanRepository;
import com.aicoach.api.progress.ProgressService;
import com.aicoach.api.session.WorkoutSession;
import com.aicoach.api.session.WorkoutSessionRepository;
import com.aicoach.api.social.ChallengeRepository;
import com.aicoach.api.user.UserAccount;
import com.aicoach.api.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Nghiệp vụ web admin. Mọi API ở đây đã được Spring Security chặn theo vai trò ADMIN (SecurityConfig),
 * service vẫn tự kiểm các luật riêng: không tự khoá mình, không khoá admin khác, dữ liệu sửa phải hợp lệ.
 */
@Service
public class AdminService {

    static final int MIN_DAYS = 7;
    static final int MAX_DAYS = 90;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int RECENT_SESSIONS = 10;

    private final UserRepository users;
    private final WorkoutSessionRepository sessions;
    private final ExerciseRepository exercises;
    private final PlanRepository plans;
    private final ChallengeRepository challenges;
    private final ProgressService progress;
    private final Clock clock;

    public AdminService(UserRepository users, WorkoutSessionRepository sessions, ExerciseRepository exercises,
                        PlanRepository plans, ChallengeRepository challenges, ProgressService progress, Clock clock) {
        this.users = users;
        this.sessions = sessions;
        this.exercises = exercises;
        this.plans = plans;
        this.challenges = challenges;
        this.progress = progress;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public AdminMe me(String adminId) {
        UserAccount u = requireUser(adminId);
        return new AdminMe(u.getId(), u.getEmail(), u.getDisplayName(), u.getRole());
    }

    // ================================================================ Tổng quan

    @Transactional(readOnly = true)
    public Stats stats(int days) {
        if (days < MIN_DAYS || days > MAX_DAYS) {
            throw ApiException.badRequest("Khoảng thống kê từ " + MIN_DAYS + " đến " + MAX_DAYS + " ngày");
        }
        LocalDate today = VnTime.today(clock);
        LocalDate firstDay = today.minusDays(days - 1L);
        Instant from = VnTime.startOf(firstDay);

        List<WorkoutSession> inRange = sessions.findByStartedAtGreaterThanEqual(from);
        Map<LocalDate, Long> perDay = emptyDays(firstDay, today);
        Set<String> activeUserIds = new HashSet<>();
        Map<String, Long> perExercise = new HashMap<>();
        long totalReps = 0;
        long correctReps = 0;
        for (WorkoutSession s : inRange) {
            perDay.computeIfPresent(VnTime.dateOf(s.getStartedAt()), (d, n) -> n + 1);
            activeUserIds.add(s.getUserId());
            perExercise.merge(s.getExerciseId(), 1L, Long::sum);
            totalReps += Math.max(0, s.getTotalReps());
            correctReps += Math.max(0, s.getCorrectReps());
        }

        Map<LocalDate, Long> signups = emptyDays(firstDay, today);
        List<UserAccount> newcomers = users.findByRoleAndCreatedAtGreaterThanEqual(UserAccount.ROLE_USER, from);
        for (UserAccount u : newcomers) signups.computeIfPresent(VnTime.dateOf(u.getCreatedAt()), (d, n) -> n + 1);

        Map<String, String> names = exerciseNames();
        List<ExerciseCount> topExercises = perExercise.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry::getKey))
                .map(e -> new ExerciseCount(e.getKey(), names.getOrDefault(e.getKey(), e.getKey()), e.getValue()))
                .toList();
        List<FormErrorCount> topErrors = sessions.topFormErrors(from).stream()
                .map(f -> new FormErrorCount(f.getCode(), f.getTotal()))
                .toList();

        double correctRate = totalReps == 0 ? 0 : Math.round(correctReps * 1000.0 / totalReps) / 10.0;
        return new Stats(days,
                users.countByRole(UserAccount.ROLE_USER),
                newcomers.size(),
                activeUserIds.size(),
                sessions.count(),
                inRange.size(),
                correctRate,
                challenges.countByEndDateGreaterThanEqual(today),
                toDayCounts(perDay),
                toDayCounts(signups),
                topExercises,
                topErrors);
    }

    private static Map<LocalDate, Long> emptyDays(LocalDate first, LocalDate last) {
        Map<LocalDate, Long> out = new LinkedHashMap<>();
        for (LocalDate d = first; !d.isAfter(last); d = d.plusDays(1)) out.put(d, 0L);
        return out;
    }

    private static List<DayCount> toDayCounts(Map<LocalDate, Long> map) {
        return map.entrySet().stream().map(e -> new DayCount(e.getKey(), e.getValue())).toList();
    }

    // ================================================================ Người dùng

    /** Danh sách người dùng, mới đăng ký trước. q tìm theo email hoặc tên, không phân biệt dấu ("hoang" ra "Hoàng"). */
    @Transactional(readOnly = true)
    public PageResult<UserRow> users(String q, int page, int size) {
        int p = Math.max(0, page);
        int s = Math.min(MAX_PAGE_SIZE, Math.max(1, size));
        String keyword = fold(q);
        if (keyword.isEmpty()) {
            Page<UserAccount> result = users.findAll(PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "createdAt")));
            return new PageResult<>(result.getContent().stream().map(this::toRow).toList(),
                    result.getTotalElements(), p, s);
        }
        // Lọc không dấu trong Java: số tài khoản của đồ án nhỏ, đổi lại tìm tiếng Việt chính xác hơn LIKE của SQL
        List<UserAccount> matched = users.findAllByOrderByCreatedAtDesc().stream()
                .filter(u -> fold(u.getEmail()).contains(keyword) || fold(u.getDisplayName()).contains(keyword))
                .toList();
        int fromIndex = Math.min(matched.size(), p * s);
        int toIndex = Math.min(matched.size(), fromIndex + s);
        return new PageResult<>(matched.subList(fromIndex, toIndex).stream().map(this::toRow).toList(),
                matched.size(), p, s);
    }

    @Transactional(readOnly = true)
    public UserDetail user(String id) {
        UserAccount u = requireUser(id);
        Map<String, String> names = exerciseNames();
        List<SessionRow> recent = sessions.findByUserIdOrderByStartedAtDesc(id, PageRequest.of(0, RECENT_SESSIONS))
                .stream().map(s -> toSessionRow(s, names)).toList();
        String planTitle = u.getActivePlanId() == null ? null
                : plans.findById(u.getActivePlanId()).map(Plan::getTitle).orElse(null);
        return new UserDetail(toRow(u), u.getGoal(), u.getLevel(), u.getHeightCm(), u.getWeightKg(),
                u.getWeeklyGoalSessions(), u.getActivePlanId(), planTitle, u.getTotalReps(), recent);
    }

    /**
     * Khoá / mở khoá. Khoá thì tăng sessionVersion → mọi token đang dùng của người đó bị từ chối ngay ở request kế tiếp
     * (app tự về màn Đăng nhập), đăng nhập lại bị báo tài khoản đã khoá.
     */
    @Transactional
    public UserRow setLocked(String adminId, String userId, boolean locked) {
        UserAccount u = requireUser(userId);
        if (u.getId().equals(adminId)) throw ApiException.badRequest("Không thể tự khoá tài khoản của chính mình");
        if (locked && u.isAdmin()) throw ApiException.badRequest("Không khoá được tài khoản quản trị viên");
        if (u.isLocked() == locked) return toRow(u);
        u.setLocked(locked);
        users.save(u);
        if (locked) users.revokeSessions(u.getId());      // flushAutomatically: ghi locked trước rồi mới tăng phiên
        return toRow(u);
    }

    private UserRow toRow(UserAccount u) {
        return new UserRow(u.getId(), u.getEmail(), u.getDisplayName(), u.getRole(), u.isLocked(),
                progress.effectiveStreak(u), u.getTotalXp(), sessions.countByUserId(u.getId()), u.getCreatedAt(),
                u.getLastWorkoutDate());
    }

    private static SessionRow toSessionRow(WorkoutSession s, Map<String, String> names) {
        String name = s.getExerciseName() != null ? s.getExerciseName()
                : names.getOrDefault(s.getExerciseId(), s.getExerciseId());
        return new SessionRow(s.getId(), s.getExerciseId(), name, s.getStartedAt(), s.getDurationSec(),
                s.getTotalReps(), s.getCorrectReps(), s.getAccuracy(), s.getPlanId());
    }

    // ================================================================ Bài tập

    @Transactional(readOnly = true)
    public List<ExerciseRow> exercises() {
        Map<String, Long> counts = sessions.countByExercise().stream()
                .collect(Collectors.toMap(WorkoutSessionRepository.ExerciseCount::getExerciseId,
                        WorkoutSessionRepository.ExerciseCount::getSessions));
        return exercises.findAllByOrderBySortOrderAsc().stream()
                .map(e -> toExerciseRow(e, counts.getOrDefault(e.getId(), 0L)))
                .toList();
    }

    /** Sửa bài tập: app đọc ngay số mới qua GET /exercises; DataSeeder không ghi đè bài này nữa (adminEdited). */
    @Transactional
    public ExerciseRow updateExercise(String id, ExerciseUpdate req) {
        Exercise e = exercises.findById(id).orElseThrow(() -> ApiException.notFound("Không tìm thấy bài tập"));
        String name = req.name().trim();
        if (name.length() < 2) throw ApiException.badRequest("Tên bài tập cần 2–50 ký tự");
        e.setName(name);
        e.setDescription(req.description() == null ? "" : req.description().trim());
        e.setDefaultSets(req.defaultSets());
        e.setDefaultReps(req.defaultReps());
        e.setCaloriesPerRep(req.caloriesPerRep());
        e.setAdminEdited(true);
        exercises.save(e);
        return toExerciseRow(e, sessions.countByExercise().stream()
                .filter(c -> id.equals(c.getExerciseId())).mapToLong(WorkoutSessionRepository.ExerciseCount::getSessions)
                .findFirst().orElse(0L));
    }

    private static ExerciseRow toExerciseRow(Exercise e, long sessionCount) {
        return new ExerciseRow(e.getId(), e.getName(), e.getMuscleGroup(), e.getDifficulty(), e.getMeasureType(),
                e.getDescription(), e.getDefaultSets(), e.getDefaultReps(), e.getCaloriesPerRep(), e.isAdminEdited(),
                sessionCount);
    }

    // ================================================================ Giáo án

    @Transactional(readOnly = true)
    public List<PlanRow> plans() {
        Map<String, Exercise> byId = exercises.findAll().stream()
                .collect(Collectors.toMap(Exercise::getId, Function.identity()));
        return plans.findAllByOrderBySortOrderAsc().stream().map(p -> toPlanRow(p, byId)).toList();
    }

    /** Sửa số hiệp × lần một bài trong giáo án: app dùng ngay số mới (GET /plans, /home); DataSeeder không ghi đè nữa. */
    @Transactional
    public PlanRow updatePlanItem(String planId, long itemId, PlanItemUpdate req) {
        Plan plan = plans.findById(planId).orElseThrow(() -> ApiException.notFound("Không tìm thấy giáo án"));
        PlanItem item = plan.getItems().stream()
                .filter(i -> i.getId() != null && i.getId() == itemId)
                .findFirst()
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy bài này trong giáo án (hãy tải lại trang)"));
        item.setSets(req.sets());
        item.setReps(req.reps());
        plan.setAdminEdited(true);
        plans.save(plan);
        Map<String, Exercise> byId = exercises.findAll().stream()
                .collect(Collectors.toMap(Exercise::getId, Function.identity()));
        return toPlanRow(plan, byId);
    }

    private PlanRow toPlanRow(Plan p, Map<String, Exercise> exercisesById) {
        Map<Integer, List<PlanItemRow>> byDay = new LinkedHashMap<>();
        p.getItems().stream()
                .sorted(Comparator.comparingInt(PlanItem::getDayIndex).thenComparingInt(PlanItem::getSortOrder))
                .forEach(i -> {
                    Exercise e = exercisesById.get(i.getExerciseId());
                    byDay.computeIfAbsent(i.getDayIndex(), d -> new ArrayList<>()).add(new PlanItemRow(i.getId(),
                            i.getExerciseId(), e == null ? i.getExerciseId() : e.getName(),
                            e == null ? "reps" : e.getMeasureType(), i.getSets(), i.getReps()));
                });
        List<PlanDayRow> days = byDay.entrySet().stream().map(d -> new PlanDayRow(d.getKey(), d.getValue())).toList();
        return new PlanRow(p.getId(), p.getTitle(), p.getLevel(), p.getDurationDays(), p.getSessionsPerWeek(),
                p.getMinutesPerSession(), p.isAdminEdited(), users.countByActivePlanId(p.getId()), days);
    }

    // ================================================================ Tiện ích

    private UserAccount requireUser(String id) {
        return users.findById(id).orElseThrow(() -> ApiException.notFound("Không tìm thấy người dùng"));
    }

    private Map<String, String> exerciseNames() {
        return exercises.findAll().stream().collect(Collectors.toMap(Exercise::getId, Exercise::getName));
    }

    /** Bỏ dấu tiếng Việt + chữ thường để tìm kiếm. */
    static String fold(String s) {
        if (s == null) return "";
        String noAccent = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return noAccent.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT).trim();
    }
}
