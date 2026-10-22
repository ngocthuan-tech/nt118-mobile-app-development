package com.aicoach.data.model;

/**
 * Hồ sơ + tiến độ người dùng, khớp 1-1 với JSON của GET /api/v1/users/me.
 * Phần hồ sơ: người dùng sửa qua PUT /users/me. Phần tiến độ (streak, XP, reps): CHỈ server tính, app chỉ đọc.
 * Ngày giờ là chuỗi ISO: createdAt "2026-10-22T10:00:00Z", planStartDate/lastWorkoutDate "2026-10-22".
 */
public class User {

    private String id;

    // ---- Hồ sơ ----
    private String displayName;
    private String email;
    private String avatarUrl;
    private Double heightCm;
    private Double weightKg;
    private String goal;            // lose_weight | build_muscle | stay_fit
    private String level;           // beginner | intermediate | advanced
    private int weeklyGoalSessions;
    private boolean profileCompleted;
    private String createdAt;
    private String activePlanId;
    private String planStartDate;

    // ---- Tiến độ (server tính) ----
    private int streak;
    private String lastWorkoutDate;
    private long weeklyXp;
    private long totalXp;
    private long totalReps;

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getEmail() { return email; }
    public String getAvatarUrl() { return avatarUrl; }
    public Double getHeightCm() { return heightCm; }
    public Double getWeightKg() { return weightKg; }
    public String getGoal() { return goal; }
    public String getLevel() { return level; }
    public int getWeeklyGoalSessions() { return weeklyGoalSessions; }
    public boolean isProfileCompleted() { return profileCompleted; }
    public String getCreatedAt() { return createdAt; }
    public String getActivePlanId() { return activePlanId; }
    public String getPlanStartDate() { return planStartDate; }
    public int getStreak() { return streak; }
    public String getLastWorkoutDate() { return lastWorkoutDate; }
    public long getWeeklyXp() { return weeklyXp; }
    public long getTotalXp() { return totalXp; }
    public long getTotalReps() { return totalReps; }

    /** Chữ cái đầu để hiện avatar tròn khi chưa có ảnh (Figma 21: "M"). */
    public String getInitial() {
        if (displayName == null || displayName.trim().isEmpty()) return "?";
        String[] parts = displayName.trim().split("\\s+");
        return parts[parts.length - 1].substring(0, 1).toUpperCase();
    }
}
