package com.aicoach.data.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Thẻ "Chuỗi 7 ngày" ở Trang chủ (Figma 21), khớp JSON của GET /api/v1/home/streak.
 * streakDays = số ngày tập liên tiếp; days = 7 chấm T2 → CN của tuần này.
 */
public class HomeStreak {

    private int streakDays;
    private String lastWorkoutDate;     // "2026-10-27", null nếu chưa tập bao giờ
    private int goalSessions;           // mục tiêu số buổi/tuần
    private int doneSessions;           // số ngày đã tập trong tuần
    private int remainingSessions;      // "Còn 1 buổi để đạt mục tiêu tuần"
    private List<Day> days = new ArrayList<>();

    public int getStreakDays() { return streakDays; }
    public String getLastWorkoutDate() { return lastWorkoutDate; }
    public int getGoalSessions() { return goalSessions; }
    public int getDoneSessions() { return doneSessions; }
    public int getRemainingSessions() { return remainingSessions; }
    public List<Day> getDays() { return days; }

    /** Một chấm trong dãy T2 … CN. */
    public static class Day {
        private String label;           // T2 … CN
        private String date;            // "2026-10-27"
        private boolean active;         // ngày đó có tập → tô màu
        private boolean today;          // hôm nay → viền

        public String getLabel() { return label; }
        public String getDate() { return date; }
        public boolean isActive() { return active; }
        public boolean isToday() { return today; }
    }
}
