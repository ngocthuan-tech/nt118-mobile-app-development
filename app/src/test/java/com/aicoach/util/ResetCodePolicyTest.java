package com.aicoach.util;
import org.junit.Test;
import static org.junit.Assert.*;
public class ResetCodePolicyTest {
    @Test public void acceptsSixDigitsIncludingLeadingZero() {
        assertTrue(ResetCodePolicy.isValidCode("012345"));
        assertFalse(ResetCodePolicy.isValidCode("12345"));
        assertFalse(ResetCodePolicy.isValidCode("1234567"));
        assertFalse(ResetCodePolicy.isValidCode("12a456"));
        assertFalse(ResetCodePolicy.isValidCode(null));
    }
    @Test public void usesServerDelayAndRoundsRemainingSecondsUp() {
        long deadline = ResetCodePolicy.deadline(1000, 75);
        assertEquals(75, ResetCodePolicy.secondsLeft(deadline, 1000));
        assertEquals(1, ResetCodePolicy.secondsLeft(deadline, 75999));
        assertEquals(0, ResetCodePolicy.secondsLeft(deadline, 76000));
        assertEquals(0, ResetCodePolicy.secondsLeft(deadline, 90000));
    }
    @Test public void invalidNegativeDelayDoesNotLockResend() {
        assertEquals(1000, ResetCodePolicy.deadline(1000, -5));
    }
    @Test public void existingDeadlineContinuesWhenReturningToScreen() {
        long deadline = ResetCodePolicy.deadline(1000, 60);
        assertEquals(30, ResetCodePolicy.secondsLeft(deadline, 31000));
    }
}
