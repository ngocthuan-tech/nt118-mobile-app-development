package com.aicoach.data.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Một trong 9 bài tập, khớp JSON của GET /api/v1/exercises.
 * id: squat | pushup | plank | lunge | glute_bridge | chair_dip | jumping_jack | high_knees | toe_touch.
 * imageName là tên ảnh trong res/drawable-nodpi (vd. "img_squat").
 */
public class Exercise {

    private String id;
    private String name;
    private String muscleGroup;     // legs | chest_arms | core | cardio
    private String difficulty;      // easy | medium | hard
    private String imageName;
    private String description;
    private String measureType;     // reps | seconds (plank tính bằng giây)
    private int defaultSets;
    private int defaultReps;
    private double caloriesPerRep;
    private List<String> steps = new ArrayList<>();
    private List<String> tips = new ArrayList<>();
    private List<String> commonMistakes = new ArrayList<>();

    public String getId() { return id; }
    public String getName() { return name; }
    public String getMuscleGroup() { return muscleGroup; }
    public String getDifficulty() { return difficulty; }
    public String getImageName() { return imageName; }
    public String getDescription() { return description; }
    public String getMeasureType() { return measureType; }
    public int getDefaultSets() { return defaultSets; }
    public int getDefaultReps() { return defaultReps; }
    public double getCaloriesPerRep() { return caloriesPerRep; }
    public List<String> getSteps() { return steps; }
    public List<String> getTips() { return tips; }
    public List<String> getCommonMistakes() { return commonMistakes; }

    public boolean isTimeBased() { return "seconds".equals(measureType); }
}
