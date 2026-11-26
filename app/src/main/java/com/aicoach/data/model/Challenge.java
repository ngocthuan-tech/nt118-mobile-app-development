package com.aicoach.data.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Thử thách giữa bạn bè (Figma 40–42) — khớp JSON của GET /challenges, /challenges/{id}.
 * Tiến độ (myTodayReps, members[].todayReps…) do server tính từ lịch sử buổi tập AI, app chỉ hiển thị.
 */
public class Challenge {

    public static final String ACTIVE = "ACTIVE";
    public static final String UPCOMING = "UPCOMING";
    public static final String ENDED = "ENDED";

    private String id;
    private String title;
    private String exerciseId;
    private String exerciseName;
    private String measureType;     // reps | seconds
    private int dailyTarget;
    private int days;
    private String startDate;       // "2026-11-26" (giờ Việt Nam)
    private String endDate;
    private int daysLeft;
    private String status;          // ACTIVE | UPCOMING | ENDED
    private boolean privateGroup;
    private String inviteCode;      // null khi thử thách riêng tư (chỉ vào qua lời mời)
    private String creatorName;
    private boolean creator;
    private int memberCount;
    private int myTodayReps;
    private int myCompletedDays;
    private List<Member> members = new ArrayList<>();

    public Challenge() {
    }

    /** Dùng cho kiểm thử. */
    public Challenge(String id, String title, String exerciseId, int dailyTarget, int days, String status,
                     boolean privateGroup, String inviteCode, int myTodayReps, List<Member> members) {
        this.id = id;
        this.title = title;
        this.exerciseId = exerciseId;
        this.dailyTarget = dailyTarget;
        this.days = days;
        this.status = status;
        this.privateGroup = privateGroup;
        this.inviteCode = inviteCode;
        this.myTodayReps = myTodayReps;
        this.members = members;
        this.memberCount = members == null ? 0 : members.size();
    }

    public String getId() { return id; }
    public String getTitle() { return title == null ? "" : title; }
    public String getExerciseId() { return exerciseId == null ? "" : exerciseId; }
    public String getExerciseName() { return exerciseName; }
    public boolean isTimed() { return "seconds".equals(measureType); }
    public int getDailyTarget() { return dailyTarget; }
    public int getDays() { return days; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public int getDaysLeft() { return daysLeft; }
    public String getStatus() { return status == null ? ACTIVE : status; }
    public boolean isEnded() { return ENDED.equals(status); }
    public boolean isPrivateGroup() { return privateGroup; }
    public String getInviteCode() { return inviteCode; }
    public boolean hasInviteCode() { return inviteCode != null && !inviteCode.isEmpty(); }
    public String getCreatorName() { return creatorName; }
    public boolean isCreator() { return creator; }
    public int getMemberCount() { return memberCount; }
    public int getMyTodayReps() { return myTodayReps; }
    public int getMyCompletedDays() { return myCompletedDays; }

    /** Thành viên, nhiều lần đúng nhất trước (server đã sắp xếp). */
    public List<Member> getMembers() {
        return members == null ? Collections.emptyList() : Collections.unmodifiableList(members);
    }

    public static class Member {
        private String userId;
        private String displayName;
        private int todayReps;
        private int totalReps;
        private int completedDays;
        private boolean me;

        public Member() {
        }

        public Member(String userId, String displayName, int todayReps, int totalReps, int completedDays, boolean me) {
            this.userId = userId;
            this.displayName = displayName;
            this.todayReps = todayReps;
            this.totalReps = totalReps;
            this.completedDays = completedDays;
            this.me = me;
        }

        public String getUserId() { return userId; }
        public String getDisplayName() { return displayName == null ? "" : displayName; }
        public int getTodayReps() { return todayReps; }
        public int getTotalReps() { return totalReps; }
        public int getCompletedDays() { return completedDays; }
        public boolean isMe() { return me; }
    }
}
