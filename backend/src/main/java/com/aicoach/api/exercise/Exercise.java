package com.aicoach.api.exercise;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/** 9 bài tập có AI theo dõi. id là chuỗi cố định (squat, pushup...) để app map sang ảnh img_&lt;id&gt;. */
@Entity
@Table(name = "exercises")
public class Exercise {

    @Id
    @Column(length = 40)
    private String id;
    @Column(nullable = false, length = 60)
    private String name;
    @Column(length = 20)
    private String muscleGroup;     // legs | chest_arms | core | cardio
    @Column(length = 10)
    private String difficulty;      // easy | medium | hard
    @Column(length = 60)
    private String imageName;
    @Column(length = 1000)
    private String description;
    @Column(length = 10)
    private String measureType;     // reps | seconds (plank tính bằng giây)
    private int defaultSets;
    private int defaultReps;
    private double caloriesPerRep;  // với bài tính giây: kcal mỗi giây
    private int sortOrder;

    @ElementCollection
    @CollectionTable(name = "exercise_steps", joinColumns = @JoinColumn(name = "exercise_id"))
    @OrderColumn(name = "idx")
    @Column(name = "content", length = 500)
    private List<String> steps = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "exercise_tips", joinColumns = @JoinColumn(name = "exercise_id"))
    @OrderColumn(name = "idx")
    @Column(name = "content", length = 500)
    private List<String> tips = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "exercise_mistakes", joinColumns = @JoinColumn(name = "exercise_id"))
    @OrderColumn(name = "idx")
    @Column(name = "content", length = 500)
    private List<String> commonMistakes = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMuscleGroup() { return muscleGroup; }
    public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMeasureType() { return measureType; }
    public void setMeasureType(String measureType) { this.measureType = measureType; }

    public int getDefaultSets() { return defaultSets; }
    public void setDefaultSets(int defaultSets) { this.defaultSets = defaultSets; }

    public int getDefaultReps() { return defaultReps; }
    public void setDefaultReps(int defaultReps) { this.defaultReps = defaultReps; }

    public double getCaloriesPerRep() { return caloriesPerRep; }
    public void setCaloriesPerRep(double caloriesPerRep) { this.caloriesPerRep = caloriesPerRep; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public List<String> getSteps() { return steps; }
    public void setSteps(List<String> steps) { this.steps = steps; }

    public List<String> getTips() { return tips; }
    public void setTips(List<String> tips) { this.tips = tips; }

    public List<String> getCommonMistakes() { return commonMistakes; }
    public void setCommonMistakes(List<String> commonMistakes) { this.commonMistakes = commonMistakes; }
}
