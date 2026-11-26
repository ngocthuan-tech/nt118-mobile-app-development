package com.aicoach.ui.workout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;

/** Lượt tập nhiều bài theo giáo án: mã hoá qua tham số điều hướng và đi lần lượt từng bài. */
public class WorkoutQueueTest {

    @Test
    public void encodeDecode_roundTrip() {
        String text = WorkoutQueue.encode(Arrays.asList(
                new WorkoutQueue.Step("jumping_jack", 3, 30, false),
                new WorkoutQueue.Step("plank", 3, 40, true)));
        assertEquals("jumping_jack:3:30:0;plank:3:40:1", text);

        WorkoutQueue q = WorkoutQueue.decode(text);
        assertEquals(2, q.size());
        assertEquals("plank", q.find("plank").exerciseId);
        assertEquals(40, q.find("plank").target);
        assertTrue(q.find("plank").timed);
    }

    @Test
    public void walksThroughExercisesInOrder() {
        WorkoutQueue q = WorkoutQueue.decode("jumping_jack:3:30:0;lunge:3:10:0;chair_dip:3:10:0");
        assertEquals("jumping_jack", q.current().exerciseId);
        assertEquals(1, q.position());
        assertTrue(q.hasNext());

        assertEquals("lunge", q.advance().exerciseId);
        assertEquals(2, q.position());
        assertEquals("chair_dip", q.advance().exerciseId);
        assertFalse(q.hasNext());
        assertNull(q.advance());
    }

    @Test
    public void brokenTextGivesEmptyQueue() {
        assertTrue(WorkoutQueue.decode(null).isEmpty());
        assertTrue(WorkoutQueue.decode("").isEmpty());
        assertEquals(1, WorkoutQueue.decode("squat:3:15:0;oops;lunge:x:1:0;plank:0:40:1").size());
    }
}
