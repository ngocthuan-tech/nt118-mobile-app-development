package com.aicoach.api.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Bảng users. Tên field khớp với model User bên app (displayName, heightCm, streak, weeklyXp...).
 * Phần hồ sơ do người dùng sửa; phần tiến độ (streak, XP, reps) chỉ server cập nhật khi lưu buổi tập.
 */
@Entity
@Table(name = "users")
public class UserAccount {

    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, length = 100)
    private String passwordHash;

    // ---- Hồ sơ ----
    @Column(nullable = false, length = 50)
    private String displayName;
    @Column(length = 500)
    private String avatarUrl;
    private Double heightCm;
    private Double weightKg;
    @Column(length = 20)
    private String goal;            // lose_weight | build_muscle | stay_fit
    @Column(length = 20)
    private String level;           // beginner | intermediate | advanced
    private int weeklyGoalSessions = 3;
    private boolean profileCompleted;
    @Column(length = 64)
    private String activePlanId;
    private LocalDate planStartDate;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    // ---- Tiến độ ----
    private int streak;
    private LocalDate lastWorkoutDate;
    private long weeklyXp;
    @Column(length = 10)
    private String weekKey;
    private long totalXp;
    private long totalReps;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public Double getHeightCm() { return heightCm; }
    public void setHeightCm(Double heightCm) { this.heightCm = heightCm; }

    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public int getWeeklyGoalSessions() { return weeklyGoalSessions; }
    public void setWeeklyGoalSessions(int weeklyGoalSessions) { this.weeklyGoalSessions = weeklyGoalSessions; }

    public boolean isProfileCompleted() { return profileCompleted; }
    public void setProfileCompleted(boolean profileCompleted) { this.profileCompleted = profileCompleted; }

    public String getActivePlanId() { return activePlanId; }
    public void setActivePlanId(String activePlanId) { this.activePlanId = activePlanId; }

    public LocalDate getPlanStartDate() { return planStartDate; }
    public void setPlanStartDate(LocalDate planStartDate) { this.planStartDate = planStartDate; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public int getStreak() { return streak; }
    public void setStreak(int streak) { this.streak = streak; }

    public LocalDate getLastWorkoutDate() { return lastWorkoutDate; }
    public void setLastWorkoutDate(LocalDate lastWorkoutDate) { this.lastWorkoutDate = lastWorkoutDate; }

    public long getWeeklyXp() { return weeklyXp; }
    public void setWeeklyXp(long weeklyXp) { this.weeklyXp = weeklyXp; }

    public String getWeekKey() { return weekKey; }
    public void setWeekKey(String weekKey) { this.weekKey = weekKey; }

    public long getTotalXp() { return totalXp; }
    public void setTotalXp(long totalXp) { this.totalXp = totalXp; }

    public long getTotalReps() { return totalReps; }
    public void setTotalReps(long totalReps) { this.totalReps = totalReps; }
}
