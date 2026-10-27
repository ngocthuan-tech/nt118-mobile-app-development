package com.aicoach.data.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Toàn bộ dữ liệu Trang chủ (Figma 21), khớp JSON của GET /api/v1/home — chỉ gọi 1 API.
 * activePlan = null khi người dùng chưa chọn giáo án.
 */
public class HomeData {

    private String displayName;
    private String avatarUrl;
    private int streak;
    private long weeklyXp;
    private Week week;
    private Today today;
    private double weekCalories;
    private ActivePlan activePlan;
    private TodayWorkout todayWorkout;

    public String getDisplayName() { return displayName; }
    public String getAvatarUrl() { return avatarUrl; }
    public int getStreak() { return streak; }
    public long getWeeklyXp() { return weeklyXp; }
    public Week getWeek() { return week; }
    public Today getToday() { return today; }
    public double getWeekCalories() { return weekCalories; }
    public ActivePlan getActivePlan() { return activePlan; }
    public TodayWorkout getTodayWorkout() { return todayWorkout; }

    /** Thẻ chuỗi ngày: "Còn {remainingSessions} buổi để đạt mục tiêu tuần" + 7 chấm T2–CN. */
    public static class Week {
        private int goalSessions;
        private int doneSessions;
        private int remainingSessions;
        private List<WeekDay> days = new ArrayList<>();

        public int getGoalSessions() { return goalSessions; }
        public int getDoneSessions() { return doneSessions; }
        public int getRemainingSessions() { return remainingSessions; }
        public List<WeekDay> getDays() { return days; }
    }

    public static class WeekDay {
        private String label;       // T2 … CN
        private String date;        // "2026-10-22"
        private boolean active;     // ngày đó có tập
        private boolean today;

        public String getLabel() { return label; }
        public String getDate() { return date; }
        public boolean isActive() { return active; }
        public boolean isToday() { return today; }
    }

    /** "18 phút hôm nay", "320 kcal". */
    public static class Today {
        private int sessions;
        private int minutes;
        private double calories;

        public int getSessions() { return sessions; }
        public int getMinutes() { return minutes; }
        public double getCalories() { return calories; }
    }

    /** Thẻ "Giáo án của bạn": "Tuần 2 • Buổi 5/12 • 25 phút/buổi", thanh tiến độ %. */
    public static class ActivePlan {
        private String planId;
        private String title;
        private String coverImageName;
        private int currentWeek;
        private int totalWeeks;
        private int sessionsDone;
        private int totalSessions;
        private int minutesPerSession;
        private int progressPercent;
        private boolean completed;

        public String getPlanId() { return planId; }
        public String getTitle() { return title; }
        public String getCoverImageName() { return coverImageName; }
        public int getCurrentWeek() { return currentWeek; }
        public int getTotalWeeks() { return totalWeeks; }
        public int getSessionsDone() { return sessionsDone; }
        public int getTotalSessions() { return totalSessions; }
        public int getMinutesPerSession() { return minutesPerSession; }
        public int getProgressPercent() { return progressPercent; }
        public boolean isCompleted() { return completed; }
    }

    /** Dải "Bài tập hôm nay": "3 bài • ~18 phút". planDayIndex = null nếu là bài gợi ý mặc định. */
    public static class TodayWorkout {
        private Integer planDayIndex;
        private int estimatedMinutes;
        private List<Plan.PlanItem> exercises = new ArrayList<>();

        public Integer getPlanDayIndex() { return planDayIndex; }
        public int getEstimatedMinutes() { return estimatedMinutes; }
        public List<Plan.PlanItem> getExercises() { return exercises; }
    }
}
