package com.aicoach.data.model;

import java.util.ArrayList;
import java.util.List;

/** Giáo án, khớp JSON của GET /api/v1/plans và /plans/{id}. */
public class Plan {

    private String id;
    private String title;
    private String description;
    private String level;           // beginner | intermediate | advanced
    private int durationDays;
    private int sessionsPerWeek;
    private int minutesPerSession;
    private String coverImageName;
    private int totalSessions;
    private List<PlanDay> days = new ArrayList<>();

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLevel() { return level; }
    public int getDurationDays() { return durationDays; }
    public int getSessionsPerWeek() { return sessionsPerWeek; }
    public int getMinutesPerSession() { return minutesPerSession; }
    public String getCoverImageName() { return coverImageName; }
    public int getTotalSessions() { return totalSessions; }
    public List<PlanDay> getDays() { return days; }

    /** Một buổi trong giáo án. */
    public static class PlanDay {
        private int dayIndex;
        private List<PlanItem> items = new ArrayList<>();

        public int getDayIndex() { return dayIndex; }
        public List<PlanItem> getItems() { return items; }
    }

    /** Một bài trong buổi: bài nào, mấy hiệp, mỗi hiệp mấy rep (hoặc mấy giây nếu measureType = seconds). */
    public static class PlanItem {
        private String exerciseId;
        private String exerciseName;
        private String imageName;
        private String measureType;
        private int sets;
        private int reps;

        public String getExerciseId() { return exerciseId; }
        public String getExerciseName() { return exerciseName; }
        public String getImageName() { return imageName; }
        public String getMeasureType() { return measureType; }
        public int getSets() { return sets; }
        public int getReps() { return reps; }

        public boolean isTimeBased() { return "seconds".equals(measureType); }
    }
}
