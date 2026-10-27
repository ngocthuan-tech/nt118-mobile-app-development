package com.aicoach.data.model;

/**
 * Toàn bộ Trang chủ gộp trong GET /api/v1/home (tuỳ chọn).
 * Giữa kỳ Trang chủ dùng 2 API riêng: HomeApi.getStreak() → HomeStreak, HomeApi.getTodayWorkout() → TodayWorkout.
 */
public class HomeData {

    private String displayName;
    private String avatarUrl;
    private int streak;
    private long weeklyXp;
    private HomeStreak week;
    private Today today;
    private double weekCalories;
    private ActivePlan activePlan;
    private TodayWorkout todayWorkout;

    public String getDisplayName() { return displayName; }
    public String getAvatarUrl() { return avatarUrl; }
    public int getStreak() { return streak; }
    public long getWeeklyXp() { return weeklyXp; }
    public HomeStreak getWeek() { return week; }
    public Today getToday() { return today; }
    public double getWeekCalories() { return weekCalories; }
    public ActivePlan getActivePlan() { return activePlan; }
    public TodayWorkout getTodayWorkout() { return todayWorkout; }

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

}
