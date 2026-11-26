package com.aicoach.api.social;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Thử thách giữa bạn bè: mỗi ngày tập đủ dailyTarget lần đúng của một bài, trong `days` ngày. */
@Entity
@Table(name = "challenges")
public class Challenge {

    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();
    @Column(nullable = false, length = 50)
    private String title;
    @Column(name = "exercise_id", nullable = false, length = 40)
    private String exerciseId;
    @Column(name = "daily_target", nullable = false)
    private int dailyTarget;
    @Column(nullable = false)
    private int days;
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    @Column(name = "private_group", nullable = false)
    private boolean privateGroup = true;
    @Column(name = "invite_code", nullable = false, length = 12)
    private String inviteCode;
    @Column(name = "creator_id", nullable = false, length = 36)
    private String creatorId;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public String getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getExerciseId() { return exerciseId; }
    public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }
    public int getDailyTarget() { return dailyTarget; }
    public void setDailyTarget(int dailyTarget) { this.dailyTarget = dailyTarget; }
    public int getDays() { return days; }
    public void setDays(int days) { this.days = days; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public boolean isPrivateGroup() { return privateGroup; }
    public void setPrivateGroup(boolean privateGroup) { this.privateGroup = privateGroup; }
    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }
    public Instant getCreatedAt() { return createdAt; }
}
