package com.aicoach.data.model;

import com.google.firebase.firestore.DocumentId;

import java.util.ArrayList;
import java.util.List;

/** Document exercises/{exerciseId} — 9 bài, Trưởng nhóm nạp sẵn. id ví dụ: "squat", "pushup". */
public class Exercise {

    @DocumentId
    private String id;
    private String name;
    private String muscleGroup;     // legs | chest | core | ...
    private String difficulty;      // easy | medium | hard
    private String imageName;       // tên ảnh trong res/drawable-nodpi, ví dụ "img_squat"
    private String description;
    private List<String> steps = new ArrayList<>();
    private List<String> tips = new ArrayList<>();
    private List<String> commonMistakes = new ArrayList<>();
    private int defaultReps;
    private double caloriesPerRep;

    public Exercise() {
    }

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

    public List<String> getSteps() { return steps; }
    public void setSteps(List<String> steps) { this.steps = steps; }

    public List<String> getTips() { return tips; }
    public void setTips(List<String> tips) { this.tips = tips; }

    public List<String> getCommonMistakes() { return commonMistakes; }
    public void setCommonMistakes(List<String> commonMistakes) { this.commonMistakes = commonMistakes; }

    public int getDefaultReps() { return defaultReps; }
    public void setDefaultReps(int defaultReps) { this.defaultReps = defaultReps; }

    public double getCaloriesPerRep() { return caloriesPerRep; }
    public void setCaloriesPerRep(double caloriesPerRep) { this.caloriesPerRep = caloriesPerRep; }
}
