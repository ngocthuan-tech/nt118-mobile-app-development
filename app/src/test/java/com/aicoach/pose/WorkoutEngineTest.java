package com.aicoach.pose;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/** Kiểm thử lõi AI bằng khung xương giả: đếm rep, bắt lỗi, hiệp, plank, mất dấu người. */
public class WorkoutEngineTest {

    private long t = 0;

    // ------------------------------------------------------------------ hình học

    @Test
    public void angle_rightAngleIs90() {
        PoseFrame f = new PoseFrame.Builder(100, 100)
                .point(0, 0, 0, 1).point(1, 0, 10, 1).point(2, 10, 10, 1).build();
        assertEquals(90.0, PoseMath.angle(f, 0, 1, 2), 0.01);
    }

    @Test
    public void angle_straightLineIs180() {
        PoseFrame f = new PoseFrame.Builder(100, 100)
                .point(0, 0, 0, 1).point(1, 0, 10, 1).point(2, 0, 20, 1).build();
        assertEquals(180.0, PoseMath.angle(f, 0, 1, 2), 0.01);
    }

    // ------------------------------------------------------------------ squat

    @Test
    public void squat_fullRepsAreCounted() {
        WorkoutEngine e = new WorkoutEngine(ExerciseRules.create("squat"));
        List<WorkoutUpdate.Event> events = new ArrayList<>();
        for (int i = 0; i < 3; i++) events.addAll(squatRep(e, 0.1, 60));

        assertEquals(3, count(events, WorkoutUpdate.Event.REP_GOOD));
        assertEquals(3, e.correctReps());
        assertEquals(3, e.totalReps());
        assertEquals(100, e.postureScore());
    }

    @Test
    public void squat_shallowRepIsNotCounted() {
        WorkoutEngine e = new WorkoutEngine(ExerciseRules.create("squat"));
        List<WorkoutUpdate> updates = squatRepUpdates(e, 0.5, 60);

        WorkoutUpdate bad = find(updates, WorkoutUpdate.Event.REP_BAD);
        assertEquals(FormIssue.SQUAT_DEPTH, bad.issue);
        assertEquals(0, e.correctReps());
        assertEquals(1, e.totalReps());
        assertEquals(Long.valueOf(1), e.summary().formErrors.get("squat_depth"));
    }

    @Test
    public void squat_kneesCavingInIsFlagged() {
        WorkoutEngine e = new WorkoutEngine(ExerciseRules.create("squat"));
        List<WorkoutUpdate> updates = squatRepUpdates(e, 0.1, 20);

        assertEquals(FormIssue.KNEES_IN, find(updates, WorkoutUpdate.Event.REP_BAD).issue);
        assertEquals(0, e.correctReps());
    }

    @Test
    public void squat_startingInSquatDoesNotCountUntilStanding() {
        WorkoutEngine e = new WorkoutEngine(ExerciseRules.create("squat"));
        // Vào màn khi đang ngồi xổm rồi đứng lên → chưa phải 1 rep
        for (int i = 0; i < 10; i++) e.onFrame(FakePose.squatFront(0.1, 60, next()), 0);
        for (int i = 0; i < 10; i++) e.onFrame(FakePose.squatFront(1.0, 60, next()), 0);
        assertEquals(0, e.totalReps());
        // Sau đó squat đủ 1 lần → được tính
        squatRep(e, 0.1, 60);
        assertEquals(1, e.correctReps());
    }

    @Test
    public void squat_jitterWhileStandingIsIgnored() {
        WorkoutEngine e = new WorkoutEngine(ExerciseRules.create("squat"));
        for (int i = 0; i < 60; i++) {
            double noise = (i % 2 == 0) ? 0.06 : -0.06;
            e.onFrame(FakePose.squatFront(0.95 + noise, 60, next()), 0);
        }
        assertEquals(0, e.totalReps());
    }

    @Test
    public void squat_setsAndWorkoutComplete() {
        ExerciseRule rule = ExerciseRules.create("squat");
        WorkoutEngine e = new WorkoutEngine(rule);
        List<WorkoutUpdate.Event> events = new ArrayList<>();

        for (int set = 1; set <= rule.sets; set++) {
            for (int i = 0; i < rule.target; i++) events.addAll(squatRep(e, 0.1, 60));
            assertTrue(e.isSetFinished());
            // Đang nghỉ: có squat thêm cũng không tính
            squatRep(e, 0.1, 60);
            assertEquals(set * rule.target, e.correctReps());
            e.startNextSet();
        }

        assertEquals(rule.sets - 1, count(events, WorkoutUpdate.Event.SET_DONE));
        assertEquals(1, count(events, WorkoutUpdate.Event.WORKOUT_DONE));
        assertTrue(e.isWorkoutFinished());

        WorkoutSummary s = e.summary();
        assertEquals("squat", s.exerciseId);
        assertEquals(45, s.targetReps);
        assertEquals(45, s.correctReps);
        assertEquals(3, s.setsDone);
        assertEquals(100, s.completionPercent());
        assertTrue(s.durationSec > 0);
    }

    @Test
    public void redoSet_dropsProgressOfCurrentSet() {
        WorkoutEngine e = new WorkoutEngine(ExerciseRules.create("squat"));
        for (int i = 0; i < 4; i++) squatRep(e, 0.1, 60);
        e.redoSet();
        assertEquals(0, e.setProgress());
        assertEquals(0, e.correctReps());
        assertEquals(1, e.currentSet());
    }

    // ------------------------------------------------------------------ hít đất

    @Test
    public void pushup_countsAndFlagsSaggingHips() {
        WorkoutEngine e = new WorkoutEngine(ExerciseRules.create("pushup"));
        pushupRep(e, 0);
        assertEquals(1, e.correctReps());

        List<WorkoutUpdate> updates = pushupRep(e, 40); // hông võng 40px
        assertEquals(FormIssue.HIP_SAG, find(updates, WorkoutUpdate.Event.REP_BAD).issue);
        assertEquals(1, e.correctReps());
        assertEquals(2, e.totalReps());
    }

    // ------------------------------------------------------------------ plank

    @Test
    public void plank_countsOnlyGoodSeconds() {
        WorkoutEngine e = new WorkoutEngine(ExerciseRules.create("plank"));
        hold(e, 0, 10_000);
        assertEquals(10, e.setProgress(), 1);

        List<WorkoutUpdate> updates = hold(e, 45, 5_000); // võng hông 5 giây
        assertEquals(FormIssue.HIP_SAG, find(updates, WorkoutUpdate.Event.LIVE_ISSUE).issue);
        assertEquals(10, e.setProgress(), 1);
        assertTrue(e.totalReps() >= 14);
    }

    @Test
    public void plank_setFinishesAtTarget() {
        WorkoutEngine e = new WorkoutEngine(ExerciseRules.create("plank"));
        List<WorkoutUpdate> updates = hold(e, 0, 50_000);
        assertTrue(contains(updates, WorkoutUpdate.Event.SET_DONE));
        assertEquals(45, e.setProgress());
    }

    // ------------------------------------------------------------------ nâng gối

    @Test
    public void highKnees_countsBothLegs() {
        WorkoutEngine e = new WorkoutEngine(ExerciseRules.create("high_knees"));
        for (int i = 0; i < 8; i++) e.onFrame(FakePose.highKnees(0, 0, next()), 0);
        for (int rep = 0; rep < 3; rep++) {
            liftKnee(e, true);
            liftKnee(e, false);
        }
        assertEquals(6, e.correctReps());
    }

    // ------------------------------------------------------------------ mất dấu người

    @Test
    public void bodyLost_isReportedOnceAndRecovered() {
        WorkoutEngine e = new WorkoutEngine(ExerciseRules.create("squat"));
        for (int i = 0; i < 5; i++) e.onFrame(FakePose.squatFront(1, 60, next()), 0);
        List<WorkoutUpdate.Event> events = new ArrayList<>();
        for (int i = 0; i < 40; i++) events.add(e.onFrame(FakePose.nobody(next()), 0).event);
        assertEquals(1, count(events, WorkoutUpdate.Event.BODY_LOST));

        WorkoutUpdate back = e.onFrame(FakePose.squatFront(1, 60, next()), 0);
        assertEquals(WorkoutUpdate.Event.BODY_FOUND, back.event);
        assertFalse(e.isSetFinished());
    }

    // ------------------------------------------------------------------ helpers

    /** Dùng chung cho các test khác: làm n lần squat xuống tới độ hạ {@code bottom}. */
    void doSquats(WorkoutEngine e, int n, double bottom) {
        for (int i = 0; i < n; i++) squatRep(e, bottom, 60);
    }

    private long next() {
        t += FakePose.FRAME_MS;
        return t;
    }

    private List<WorkoutUpdate.Event> squatRep(WorkoutEngine e, double bottom, float kneeWidth) {
        List<WorkoutUpdate.Event> out = new ArrayList<>();
        for (WorkoutUpdate u : squatRepUpdates(e, bottom, kneeWidth)) out.add(u.event);
        return out;
    }

    /** Đứng 6 khung → hạ dần xuống {@code bottom} trong 10 khung → đứng lên trong 10 khung. */
    private List<WorkoutUpdate> squatRepUpdates(WorkoutEngine e, double bottom, float kneeWidth) {
        List<WorkoutUpdate> out = new ArrayList<>();
        for (int i = 0; i < 6; i++) out.add(e.onFrame(FakePose.squatFront(1.0, 60, next()), 0));
        for (int i = 1; i <= 10; i++) {
            double d = 1.0 - (1.0 - bottom) * i / 10.0;
            float kw = d < 0.5 ? kneeWidth : 60;
            out.add(e.onFrame(FakePose.squatFront(d, kw, next()), 0));
        }
        for (int i = 9; i >= 0; i--) {
            double d = 1.0 - (1.0 - bottom) * i / 10.0;
            float kw = d < 0.5 ? kneeWidth : 60;
            out.add(e.onFrame(FakePose.squatFront(d, kw, next()), 0));
        }
        for (int i = 0; i < 4; i++) out.add(e.onFrame(FakePose.squatFront(1.0, 60, next()), 0));
        return out;
    }

    private List<WorkoutUpdate> pushupRep(WorkoutEngine e, float hipSag) {
        List<WorkoutUpdate> out = new ArrayList<>();
        for (int i = 0; i < 5; i++) out.add(e.onFrame(FakePose.horizontal(0, 170, next()), 0));
        for (int a = 170; a >= 80; a -= 10) out.add(e.onFrame(FakePose.horizontal(hipSag, a, next()), 0));
        for (int a = 80; a <= 170; a += 10) out.add(e.onFrame(FakePose.horizontal(hipSag, a, next()), 0));
        for (int i = 0; i < 4; i++) out.add(e.onFrame(FakePose.horizontal(0, 170, next()), 0));
        return out;
    }

    private List<WorkoutUpdate> hold(WorkoutEngine e, float hipSag, long durationMs) {
        List<WorkoutUpdate> out = new ArrayList<>();
        long end = t + durationMs;
        while (t < end) out.add(e.onFrame(FakePose.horizontal(hipSag, 90, next()), 0));
        return out;
    }

    private void liftKnee(WorkoutEngine e, boolean left) {
        for (double l = 0; l <= 1.0; l += 0.2) {
            e.onFrame(FakePose.highKnees(left ? l : 0, left ? 0 : l, next()), 0);
        }
        for (double l = 1.0; l >= 0; l -= 0.2) {
            e.onFrame(FakePose.highKnees(left ? l : 0, left ? 0 : l, next()), 0);
        }
        for (int i = 0; i < 3; i++) e.onFrame(FakePose.highKnees(0, 0, next()), 0);
    }

    private static int count(List<WorkoutUpdate.Event> events, WorkoutUpdate.Event type) {
        int n = 0;
        for (WorkoutUpdate.Event ev : events) if (ev == type) n++;
        return n;
    }

    private static boolean contains(List<WorkoutUpdate> updates, WorkoutUpdate.Event type) {
        for (WorkoutUpdate u : updates) if (u.event == type) return true;
        return false;
    }

    private static WorkoutUpdate find(List<WorkoutUpdate> updates, WorkoutUpdate.Event type) {
        for (WorkoutUpdate u : updates) if (u.event == type) return u;
        throw new AssertionError("Không thấy sự kiện " + type);
    }
}
