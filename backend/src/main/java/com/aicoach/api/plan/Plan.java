package com.aicoach.api.plan;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/** Giáo án: nhiều buổi (dayIndex 1..n), mỗi buổi nhiều bài. */
@Entity
@Table(name = "plans")
public class Plan {

    @Id
    @Column(length = 64)
    private String id;
    @Column(nullable = false, length = 100)
    private String title;
    @Column(length = 500)
    private String description;
    @Column(length = 20)
    private String level;           // beginner | intermediate | advanced
    private int durationDays;
    private int sessionsPerWeek;
    private int minutesPerSession;
    @Column(length = 60)
    private String coverImageName;
    private int sortOrder;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayIndex ASC, sortOrder ASC")
    private List<PlanItem> items = new ArrayList<>();

    public void addItem(int dayIndex, String exerciseId, int sets, int reps) {
        PlanItem item = new PlanItem();
        item.setPlan(this);
        item.setDayIndex(dayIndex);
        item.setSortOrder(items.size());
        item.setExerciseId(exerciseId);
        item.setSets(sets);
        item.setReps(reps);
        items.add(item);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }

    public int getSessionsPerWeek() { return sessionsPerWeek; }
    public void setSessionsPerWeek(int sessionsPerWeek) { this.sessionsPerWeek = sessionsPerWeek; }

    public int getMinutesPerSession() { return minutesPerSession; }
    public void setMinutesPerSession(int minutesPerSession) { this.minutesPerSession = minutesPerSession; }

    public String getCoverImageName() { return coverImageName; }
    public void setCoverImageName(String coverImageName) { this.coverImageName = coverImageName; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public List<PlanItem> getItems() { return items; }
    public void setItems(List<PlanItem> items) { this.items = items; }
}
