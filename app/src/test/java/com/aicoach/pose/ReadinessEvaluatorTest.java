package com.aicoach.pose;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Kiểm thử màn Kiểm tra sẵn sàng + thống kê từng hiệp. */
public class ReadinessEvaluatorTest {

    @Test
    public void nobody_cannotStart() {
        ReadinessEvaluator.Result r = ReadinessEvaluator.evaluate(FakePose.nobody(0), ExerciseRules.create("squat"));
        assertEquals(ReadinessEvaluator.Level.ERROR, r.frame);
        assertEquals(ReadinessEvaluator.Level.PENDING, r.orientation);
        assertFalse(r.canStart());
    }

    @Test
    public void fullBodyInGoodLight_canStartSquat() {
        ReadinessEvaluator.Result r = ReadinessEvaluator.evaluate(withBrightness(130), ExerciseRules.create("squat"));
        assertEquals(ReadinessEvaluator.Level.OK, r.frame);
        assertEquals(ReadinessEvaluator.Level.OK, r.light);
        assertTrue(r.canStart());
    }

    @Test
    public void tooDark_blocksStart() {
        ReadinessEvaluator.Result r = ReadinessEvaluator.evaluate(withBrightness(20), ExerciseRules.create("squat"));
        assertEquals(ReadinessEvaluator.Level.ERROR, r.light);
        assertFalse(r.canStart());
    }

    @Test
    public void frontFacing_warnsForSideExercise() {
        ReadinessEvaluator.Result r = ReadinessEvaluator.evaluate(withBrightness(130), ExerciseRules.create("pushup"));
        assertEquals(ReadinessEvaluator.Level.WARNING, r.orientation);
        assertTrue(r.canStart()); // cảnh báo vẫn cho tập
    }

    @Test
    public void setResult_reportsRepsErrorsAndTopIssue() {
        WorkoutEngineTest helper = new WorkoutEngineTest();
        WorkoutEngine e = new WorkoutEngine(ExerciseRules.create("squat"));
        helper.doSquats(e, 2, 0.5);           // 2 lần nông
        helper.doSquats(e, 15, 0.1);          // 15 lần đúng → xong hiệp
        SetResult s = e.lastSet();
        assertEquals(1, s.setNumber);
        assertEquals(15, s.done);
        assertEquals(17, s.attempts);
        assertEquals(2, s.errors);
        assertEquals(FormIssue.SQUAT_DEPTH, s.topIssue);
        assertEquals(88, s.score());
    }

    private static PoseFrame withBrightness(float b) {
        PoseFrame base = FakePose.squatFront(1.0, 60, 0);
        PoseFrame.Builder copy = new PoseFrame.Builder(base.width(), base.height()).brightness(b);
        for (int i = 0; i < Landmarks.COUNT; i++) copy.point(i, base.x(i), base.y(i), base.score(i));
        copy.point(Landmarks.NOSE, base.x(Landmarks.NOSE), base.y(Landmarks.NOSE), 0.99f);
        return copy.build();
    }
}
