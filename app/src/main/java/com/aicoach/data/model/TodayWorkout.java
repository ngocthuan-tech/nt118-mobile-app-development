package com.aicoach.data.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Dải "Bài tập hôm nay" ở Trang chủ (Figma 21), khớp JSON của GET /api/v1/home/today-workout.
 * Đang theo giáo án → buổi tiếp theo của giáo án. Chưa chọn giáo án → planId = null, 3 bài gợi ý.
 */
public class TodayWorkout {

    private String planId;
    private String planTitle;
    private Integer planDayIndex;       // buổi số mấy của giáo án
    private int estimatedMinutes;       // "3 bài • ~18 phút"
    private List<Plan.PlanItem> exercises = new ArrayList<>();

    public String getPlanId() { return planId; }
    public String getPlanTitle() { return planTitle; }
    public Integer getPlanDayIndex() { return planDayIndex; }
    public int getEstimatedMinutes() { return estimatedMinutes; }
    public List<Plan.PlanItem> getExercises() { return exercises; }

    public boolean hasPlan() { return planId != null; }
}
