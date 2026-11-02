package com.aicoach.ui.auth;
import org.junit.Test;
import static org.junit.Assert.*;
public class NewPasswordViewModelTest {
    @Test public void requiresValidPasswordAndMatchingConfirmation() {
        assertTrue(NewPasswordViewModel.canSubmit("NewPass123", "NewPass123"));
        assertFalse(NewPasswordViewModel.canSubmit("NewPass123", "NewPass124"));
        assertFalse(NewPasswordViewModel.canSubmit("12345678", "12345678"));
        assertFalse(NewPasswordViewModel.canSubmit("abcdefgh", "abcdefgh"));
        assertFalse(NewPasswordViewModel.canSubmit("a1", "a1"));
    }
}
