package com.aicoach.api;

import com.aicoach.api.home.PlanProgress;
import com.aicoach.api.plan.PlanService.PlanDayDto;
import com.aicoach.api.plan.PlanService.PlanItemDto;
import com.aicoach.api.session.WorkoutSession;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Buổi giáo án chỉ xong khi tập đủ mọi bài; bài đã tập trong buổi đang làm được trả về để app bỏ qua. */
class PlanProgressTest {

    private static final Instant T = Instant.parse("2026-11-24T10:00:00Z");

    private final List<PlanDayDto> days = List.of(
            day(0, "jumping_jack", "squat"),
            day(1, "jumping_jack", "lunge", "chair_dip", "plank"),
            day(2, "pushup", "toe_touch"));

    @Test
    void oneExerciseDoesNotFinishTheDay() {
        List<WorkoutSession> s = List.of(session("jumping_jack", 0, 90, 90));

        PlanProgress.Result r = PlanProgress.compute(days, s);

        assertEquals(0, r.sessionsDone());
        assertEquals(Integer.valueOf(0), r.currentDayIndex());
        assertEquals(List.of("jumping_jack"), r.doneExerciseIds());
    }

    @Test
    void allExercisesFinishTheDayAndOpenTheNextOne() {
        List<WorkoutSession> s = List.of(
                session("jumping_jack", 0, 90, 80),
                session("squat", 0, 45, 40),
                session("lunge", 1, 30, 30));

        PlanProgress.Result r = PlanProgress.compute(days, s);

        assertEquals(1, r.sessionsDone());
        assertEquals(Integer.valueOf(1), r.currentDayIndex());
        assertEquals(List.of("lunge"), r.doneExerciseIds());
    }

    @Test
    void exerciseUnderHalfTargetDoesNotCount() {
        List<WorkoutSession> s = List.of(
                session("jumping_jack", 0, 90, 20),
                session("squat", 0, 45, 45));

        PlanProgress.Result r = PlanProgress.compute(days, s);

        assertEquals(0, r.sessionsDone());
        assertEquals(List.of("squat"), r.doneExerciseIds());
    }

    @Test
    void legacySessionsStillCountOnePerDate() {
        List<WorkoutSession> s = new ArrayList<>();
        s.add(session("squat", null, 45, 40));                              // hôm nay
        WorkoutSession older = session("pushup", null, 36, 30);
        older.setStartedAt(T.minusSeconds(3 * 86_400));                       // 3 ngày trước
        s.add(older);

        PlanProgress.Result r = PlanProgress.compute(days, s);

        assertEquals(2, r.sessionsDone());
        assertEquals(Integer.valueOf(2), r.currentDayIndex());
    }

    @Test
    void finishedPlanHasNoCurrentDay() {
        List<WorkoutSession> s = List.of(
                session("jumping_jack", 0, 1, 1), session("squat", 0, 1, 1),
                session("jumping_jack", 1, 1, 1), session("lunge", 1, 1, 1),
                session("chair_dip", 1, 1, 1), session("plank", 1, 1, 1),
                session("pushup", 2, 1, 1), session("toe_touch", 2, 1, 1));

        PlanProgress.Result r = PlanProgress.compute(days, s);

        assertEquals(3, r.sessionsDone());
        assertNull(r.currentDayIndex());
    }

    private static PlanDayDto day(int index, String... exerciseIds) {
        List<PlanItemDto> items = new ArrayList<>();
        for (String id : exerciseIds) items.add(new PlanItemDto(id, id, id, "reps", 3, 10));
        return new PlanDayDto(index, items);
    }

    private static WorkoutSession session(String exerciseId, Integer planDay, int target, int correct) {
        WorkoutSession s = new WorkoutSession();
        s.setExerciseId(exerciseId);
        s.setPlanId("fat-burn-4w");
        s.setPlanDay(planDay);
        s.setStartedAt(T);
        s.setTargetReps(target);
        s.setTotalReps(target);
        s.setCorrectReps(correct);
        return s;
    }
}
