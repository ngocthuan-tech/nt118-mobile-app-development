package com.aicoach.ui.workout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;

import com.aicoach.NavRootDirections;
import com.aicoach.data.model.Plan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Điểm vào DUY NHẤT của luồng tập AI (Trưởng nhóm).
 *  - Tập lẻ 1 bài:            startSingle(nav, "squat")
 *  - Tập 1 buổi giáo án:      startPlanDay(nav, planId, dayIndex, items, doneIds)
 *    → chỉ đưa vào các bài CHƯA tập; tập xong bài này tự sang bài kế; mỗi bài lưu kèm planId + planDay
 *      để server biết buổi nào xong.
 */
public final class WorkoutLauncher {

    private WorkoutLauncher() {
    }

    public static void startSingle(@NonNull NavController nav, @NonNull String exerciseId) {
        nav.navigate(NavRootDirections.actionGlobalToReadiness(exerciseId));
    }

    /**
     * Bắt đầu (hoặc tập tiếp) một buổi giáo án.
     * @return false nếu mọi bài của buổi đã tập xong (không có gì để mở)
     */
    public static boolean startPlanDay(@NonNull NavController nav, @NonNull String planId, int planDay,
                                       @NonNull List<Plan.PlanItem> items, @Nullable Collection<String> doneIds) {
        List<WorkoutQueue.Step> steps = remainingSteps(items, doneIds);
        if (steps.isEmpty()) return false;
        NavRootDirections.ActionGlobalToReadiness action =
                NavRootDirections.actionGlobalToReadiness(steps.get(0).exerciseId);
        action.setQueue(WorkoutQueue.encode(steps));
        action.setPlanId(planId);
        action.setPlanDay(planDay);
        nav.navigate(action);
        return true;
    }

    /** Các bài chưa tập của buổi, giữ đúng thứ tự trong giáo án. */
    @NonNull
    static List<WorkoutQueue.Step> remainingSteps(@NonNull List<Plan.PlanItem> items,
                                                  @Nullable Collection<String> doneIds) {
        List<WorkoutQueue.Step> steps = new ArrayList<>();
        for (Plan.PlanItem item : items) {
            String id = item.getExerciseId();
            if (id == null || (doneIds != null && doneIds.contains(id))) continue;
            steps.add(new WorkoutQueue.Step(id, Math.max(1, item.getSets()), Math.max(1, item.getReps()),
                    item.isTimeBased()));
        }
        return steps;
    }
}
