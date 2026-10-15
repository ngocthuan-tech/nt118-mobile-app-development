package com.aicoach.data.model;

import com.google.firebase.firestore.DocumentId;

import java.util.ArrayList;
import java.util.List;

/** Document plans/{planId}: một kế hoạch gồm nhiều ngày, mỗi ngày nhiều bài. */
public class Plan {

    @DocumentId
    private String id;
    private String title;
    private String level;           // beginner | intermediate | advanced
    private int durationDays;
    private String coverImageName;
    private List<PlanDay> days = new ArrayList<>();

    public Plan() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }

    public String getCoverImageName() { return coverImageName; }
    public void setCoverImageName(String coverImageName) { this.coverImageName = coverImageName; }

    public List<PlanDay> getDays() { return days; }
    public void setDays(List<PlanDay> days) { this.days = days; }

    /** Một ngày trong kế hoạch. */
    public static class PlanDay {
        private int dayIndex;
        private List<PlanItem> items = new ArrayList<>();

        public PlanDay() {
        }

        public int getDayIndex() { return dayIndex; }
        public void setDayIndex(int dayIndex) { this.dayIndex = dayIndex; }

        public List<PlanItem> getItems() { return items; }
        public void setItems(List<PlanItem> items) { this.items = items; }
    }

    /** Một bài trong ngày: bài nào, mấy hiệp, mỗi hiệp mấy rep. */
    public static class PlanItem {
        private String exerciseId;
        private int sets;
        private int reps;

        public PlanItem() {
        }

        public String getExerciseId() { return exerciseId; }
        public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }

        public int getSets() { return sets; }
        public void setSets(int sets) { this.sets = sets; }

        public int getReps() { return reps; }
        public void setReps(int reps) { this.reps = reps; }
    }
}
