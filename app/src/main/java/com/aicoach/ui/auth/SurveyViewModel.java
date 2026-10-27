package com.aicoach.ui.auth;

import android.app.Application;
import android.os.SystemClock;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.aicoach.common.Resource;
import com.aicoach.data.local.SettingsPrefs;
import com.aicoach.data.model.Plan;
import com.aicoach.data.model.User;
import com.aicoach.data.repository.OnboardingRepository;
import com.aicoach.data.repository.PlanRepository;
import com.aicoach.ui.plan.PlanUi;
import com.aicoach.util.RequestTracker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Dữ liệu khảo sát dùng chung cho 13 → 14 → 15 → 16 → 17 (Thành viên A).
 * Lấy bằng ViewModelProvider(navController.getViewModelStoreOwner(R.id.nav_auth)) nên sống suốt luồng đăng nhập
 * và xoay màn hình không mất. Mỗi bước bấm "Tiếp tục" thì lưu ngay phần của bước đó lên server (PUT users/me).
 */
public class SurveyViewModel extends AndroidViewModel {

    // Mục tiêu trên giao diện (4 lựa chọn Figma 14) → 3 giá trị server chấp nhận
    public static final String GOAL_LOSE = "lose_weight";
    public static final String GOAL_MUSCLE = "build_muscle";
    public static final String GOAL_FIT = "stay_fit";
    public static final String GOAL_ENDURANCE = "endurance";

    public static final String LEVEL_BEGINNER = "beginner";
    public static final String LEVEL_INTERMEDIATE = "intermediate";
    public static final String LEVEL_ADVANCED = "advanced";

    public static final int[] DURATIONS = {15, 25, 40};
    /** Thời gian hiệu ứng "AI đang tạo giáo án" (Figma 16). */
    public static final long GENERATING_MS = 2600;

    private static final double KCAL_PER_MINUTE = 12;

    private final OnboardingRepository onboardingRepo;
    private final PlanRepository planRepo;
    private final SettingsPrefs prefs;
    private final RequestTracker<User> save = new RequestTracker<>();
    private final RequestTracker<List<Plan>> plans = new RequestTracker<>();

    // 13
    private boolean male = true;
    private int birthYear;
    private int heightCm = 170;
    private int weightKg = 62;
    // 14
    private String goal = GOAL_LOSE;
    private final Set<String> focusAreas = new LinkedHashSet<>();
    // 15
    private String level = LEVEL_BEGINNER;
    private final boolean[] days = {true, false, true, false, true, false, false};
    private int durationMinutes = 25;
    private String reminderTime;
    private boolean reminderOn = true;
    // 16, 17
    private long generatingStartedAt;
    @Nullable
    private Plan chosenPlan;

    public SurveyViewModel(@NonNull Application app) {
        super(app);
        onboardingRepo = new OnboardingRepository(app);
        planRepo = new PlanRepository(app);
        prefs = new SettingsPrefs(app);
        birthYear = currentYear() - 23;
        reminderTime = prefs.getReminderTime();
    }

    // ------------------------------------------------------------------ Lưu từng bước

    public LiveData<Resource<User>> getSaveState() { return save.getState(); }

    public void saveBody() { save.track(onboardingRepo.saveBody(heightCm, weightKg)); }

    public void saveGoal() { save.track(onboardingRepo.saveGoal(serverGoal())); }

    /** Bước 3: lưu trình độ + số buổi lên server, giờ nhắc + ngày tập lưu trên máy. */
    public void saveSchedule() {
        prefs.setReminderEnabled(reminderOn);
        prefs.setReminderTime(reminderTime);
        prefs.setReminderDays(TextUtils.join(",", selectedDayLabels()));
        save.track(onboardingRepo.saveSchedule(level, sessionsPerWeek()));
    }

    public void consumeSave() { save.consume(); }

    // ------------------------------------------------------------------ 16 AI tạo giáo án

    public LiveData<Resource<List<Plan>>> getPlansState() { return plans.getState(); }

    /** Bắt đầu hiệu ứng + tải danh sách giáo án (chỉ lần đầu, xoay màn hình không tải lại). */
    public void startGenerating() {
        if (generatingStartedAt == 0) generatingStartedAt = SystemClock.elapsedRealtime();
        Resource<List<Plan>> r = plans.getState().getValue();
        if (r == null || r.status == Resource.Status.ERROR) plans.track(planRepo.getPlans());
    }

    public void retryPlans() {
        plans.track(planRepo.getPlans());
    }

    /** 0–100 theo thời gian đã chạy. */
    public int generatingProgress() {
        if (generatingStartedAt == 0) return 0;
        long elapsed = SystemClock.elapsedRealtime() - generatingStartedAt;
        return (int) Math.min(100, elapsed * 100 / GENERATING_MS);
    }

    /**
     * "AI" chọn giáo án khớp mục tiêu + trình độ trong 3 giáo án của server:
     * tăng cơ hoặc đã có nền tảng → Sức mạnh; giữ dáng → Bụng săn chắc; còn lại → Đốt mỡ.
     */
    public void pickPlan(@NonNull List<Plan> all) {
        if (all.isEmpty()) return;
        String wanted;
        if (GOAL_MUSCLE.equals(goal) || !LEVEL_BEGINNER.equals(level)) wanted = "strength-4w";
        else if (GOAL_FIT.equals(goal)) wanted = "core-2w";
        else wanted = "fat-burn-4w";
        Plan byLevel = null;
        for (Plan p : all) {
            if (wanted.equals(p.getId())) {
                chosenPlan = p;
                return;
            }
            if (byLevel == null && level.equals(p.getLevel())) byLevel = p;
        }
        chosenPlan = byLevel != null ? byLevel : all.get(0);
    }

    @Nullable
    public Plan getChosenPlan() { return chosenPlan; }

    /** 17 "Bắt đầu hành trình": theo giáo án + profileCompleted = true. */
    public void confirmPlan() {
        if (chosenPlan != null) save.track(onboardingRepo.choosePlan(chosenPlan.getId()));
    }

    // ------------------------------------------------------------------ Getter / setter cho UI

    public boolean isMale() { return male; }
    public void setMale(boolean v) { male = v; }

    public int getBirthYear() { return birthYear; }
    public void setBirthYear(int v) { birthYear = v; }
    public int getAge() { return currentYear() - birthYear; }
    public static int currentYear() { return Calendar.getInstance().get(Calendar.YEAR); }

    public int getHeightCm() { return heightCm; }
    public void setHeightCm(int v) { heightCm = v; }
    public int getWeightKg() { return weightKg; }
    public void setWeightKg(int v) { weightKg = v; }

    public double bmi() {
        double m = heightCm / 100.0;
        return m <= 0 ? 0 : weightKg / (m * m);
    }

    @NonNull
    public String bmiText() { return String.format(Locale.US, "%.1f", bmi()); }

    public String getGoal() { return goal; }
    public void setGoal(@NonNull String v) { goal = v; }

    /** Server chỉ có 3 mục tiêu: "Tăng sức bền" lưu là stay_fit. */
    @NonNull
    public String serverGoal() { return GOAL_ENDURANCE.equals(goal) ? GOAL_FIT : goal; }

    public Set<String> getFocusAreas() { return focusAreas; }

    public String getLevel() { return level; }
    public void setLevel(@NonNull String v) { level = v; }

    public boolean isDaySelected(int index) { return days[index]; }

    /** Bỏ chọn ngày cuối cùng còn lại → không cho (cần ≥ 1 buổi/tuần). Trả về false nếu bị từ chối. */
    public boolean toggleDay(int index) {
        if (days[index] && sessionsPerWeek() == 1) return false;
        days[index] = !days[index];
        return true;
    }

    public int sessionsPerWeek() {
        int n = 0;
        for (boolean d : days) if (d) n++;
        return n;
    }

    @NonNull
    public List<String> selectedDayLabels() {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < days.length; i++) if (days[i]) out.add(PlanUi.WEEK_DAYS[i]);
        return out;
    }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int v) { durationMinutes = v; }

    public int weeklyMinutes() { return sessionsPerWeek() * durationMinutes; }
    public int weeklyKcal() { return (int) (Math.round(weeklyMinutes() * KCAL_PER_MINUTE / 50.0) * 50); }

    public String getReminderTime() { return reminderTime; }
    public void setReminderTime(@NonNull String v) { reminderTime = v; }
    public boolean isReminderOn() { return reminderOn; }
    public void setReminderOn(boolean v) { reminderOn = v; }
}
