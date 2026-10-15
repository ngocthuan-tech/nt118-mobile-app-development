package com.aicoach.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * Document users/{uid}.
 * Hồ sơ + kế hoạch: Thành viên A ghi. Tiến độ (streak, XP, reps): Thành viên B ghi, A chỉ đọc.
 */
public class User {

    @DocumentId
    private String id;

    // ---- Hồ sơ (Thành viên A) ----
    private String displayName;
    private String email;
    private String emailLower;      // dùng để tìm bạn theo email
    private String avatarUrl;
    private double heightCm;
    private double weightKg;
    private String goal;            // lose_weight | build_muscle | stay_fit
    private String level;           // beginner | intermediate | advanced
    private boolean profileCompleted;
    @ServerTimestamp
    private Timestamp createdAt;
    private String activePlanId;
    private String planStartDate;   // yyyy-MM-dd

    // ---- Tiến độ (Thành viên B) ----
    private int streak;
    private String lastWorkoutDate; // yyyy-MM-dd, theo giờ Việt Nam
    private long weeklyXp;
    private String weekKey;         // ví dụ 2026-W43
    private long totalXp;
    private long totalReps;

    /** Firestore cần constructor rỗng. */
    public User() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEmailLower() { return emailLower; }
    public void setEmailLower(String emailLower) { this.emailLower = emailLower; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public double getHeightCm() { return heightCm; }
    public void setHeightCm(double heightCm) { this.heightCm = heightCm; }

    public double getWeightKg() { return weightKg; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public boolean isProfileCompleted() { return profileCompleted; }
    public void setProfileCompleted(boolean profileCompleted) { this.profileCompleted = profileCompleted; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getActivePlanId() { return activePlanId; }
    public void setActivePlanId(String activePlanId) { this.activePlanId = activePlanId; }

    public String getPlanStartDate() { return planStartDate; }
    public void setPlanStartDate(String planStartDate) { this.planStartDate = planStartDate; }

    public int getStreak() { return streak; }
    public void setStreak(int streak) { this.streak = streak; }

    public String getLastWorkoutDate() { return lastWorkoutDate; }
    public void setLastWorkoutDate(String lastWorkoutDate) { this.lastWorkoutDate = lastWorkoutDate; }

    public long getWeeklyXp() { return weeklyXp; }
    public void setWeeklyXp(long weeklyXp) { this.weeklyXp = weeklyXp; }

    public String getWeekKey() { return weekKey; }
    public void setWeekKey(String weekKey) { this.weekKey = weekKey; }

    public long getTotalXp() { return totalXp; }
    public void setTotalXp(long totalXp) { this.totalXp = totalXp; }

    public long getTotalReps() { return totalReps; }
    public void setTotalReps(long totalReps) { this.totalReps = totalReps; }
}
