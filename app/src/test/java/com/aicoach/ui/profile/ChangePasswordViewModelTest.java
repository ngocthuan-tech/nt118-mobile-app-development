package com.aicoach.ui.profile;

import com.aicoach.R;
import org.junit.Test;
import static org.junit.Assert.*;

public class ChangePasswordViewModelTest {
    @Test public void requiresCurrentPasswordAndConfirmation() {
        assertEquals(R.string.profile_password_current_required,
                ChangePasswordViewModel.validate("  ", "NewPass123", "NewPass123").current);
        assertEquals(R.string.profile_password_confirm_required,
                ChangePasswordViewModel.validate("OldPass123", "NewPass123", "").confirm);
    }

    @Test public void rejectsWeakLongSameOrMismatchedPasswords() {
        for (String password : new String[]{"", "a1", "abcdefgh", "12345678", "a1" + repeat('x', 63)}) {
            assertEquals(R.string.profile_password_rule,
                    ChangePasswordViewModel.validate("OldPass123", password, password).password);
        }
        assertEquals(R.string.profile_password_same,
                ChangePasswordViewModel.validate("OldPass123", "OldPass123", "OldPass123").password);
        assertEquals(R.string.profile_password_mismatch,
                ChangePasswordViewModel.validate("OldPass123", "NewPass123", "NewPass124").confirm);
    }

    @Test public void acceptsBoundaryLengthsAndDoesNotTrimPasswords() {
        for (String password : new String[]{"Abcdef12", "a1" + repeat('x', 62), " NewPass123 "}) {
            assertTrue(ChangePasswordViewModel.validate("OldPass123", password, password).isValid());
        }
        assertFalse(ChangePasswordViewModel.validate("OldPass123", " NewPass123 ", "NewPass123").isValid());
    }

    private static String repeat(char value, int count) {
        char[] chars = new char[count];
        java.util.Arrays.fill(chars, value);
        return new String(chars);
    }
}
