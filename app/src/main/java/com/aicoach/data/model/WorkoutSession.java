package com.aicoach.data.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Một buổi tập đã lưu, khớp JSON của GET /api/v1/sessions.
 * Trưởng nhóm gửi lên ở màn Kết quả (POST /sessions); Thành viên B đọc để vẽ Lịch sử, Chi tiết buổi tập.
 */
public class WorkoutSession {

    private String id;
    private String exerciseId;
    private String exerciseName;
    private String startedAt;                           // ISO, ví dụ "2026-10-22T12:00:00Z"
    private int durationSec;
    private int targetReps;
    private int totalReps;
    private int correctReps;
    private double accuracy;                            // 0..1
    private double calories;
    private Map<String, Long> formErrors = new HashMap<>();  // mã lỗi -> số lần, ví dụ "knee_over_toe" -> 3
    private String planId;
    private long xpEarned;

    public String getId() { return id; }
    public String getExerciseId() { return exerciseId; }
    public String getExerciseName() { return exerciseName; }
    public String getStartedAt() { return startedAt; }
    public int getDurationSec() { return durationSec; }
    public int getTargetReps() { return targetReps; }
    public int getTotalReps() { return totalReps; }
    public int getCorrectReps() { return correctReps; }
    public double getAccuracy() { return accuracy; }
    public double getCalories() { return calories; }
    public Map<String, Long> getFormErrors() { return formErrors; }
    public String getPlanId() { return planId; }
    public long getXpEarned() { return xpEarned; }
}
