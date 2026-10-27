package com.aicoach.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Các ca thầy hay bấm thử ở màn Đăng nhập / Đăng ký (PHAN_CONG_THANH_VIEN_A.md mục A.6). */
public class ValidationUtilsTest {

    @Test
    public void emailIsTrimmedAndLowercased() {
        assertEquals("minh@gmail.com", ValidationUtils.normalizeEmail("  Minh@Gmail.COM "));
        assertEquals("", ValidationUtils.normalizeEmail(null));
    }

    @Test
    public void emailFormat() {
        assertTrue(ValidationUtils.isValidEmail("demo@aicoach.vn"));
        assertFalse(ValidationUtils.isValidEmail("demo@aicoach"));
        assertFalse(ValidationUtils.isValidEmail("demo aicoach.vn"));
        assertFalse(ValidationUtils.isValidEmail(""));
    }

    @Test
    public void passwordNeedsLetterAndDigit() {
        assertFalse("chỉ có số", ValidationUtils.isValidPassword("12345678"));
        assertFalse("chỉ có chữ", ValidationUtils.isValidPassword("abcdefgh"));
        assertFalse("quá ngắn", ValidationUtils.isValidPassword("abc123"));
        assertTrue(ValidationUtils.isValidPassword("Demo1234"));
    }

    @Test
    public void passwordTooLong() {
        StringBuilder sb = new StringBuilder("a1");
        while (sb.length() <= ValidationUtils.PASSWORD_MAX) sb.append('x');
        assertFalse(ValidationUtils.isValidPassword(sb.toString()));
    }

    @Test
    public void strengthScore() {
        assertEquals(0, ValidationUtils.strength(""));
        assertEquals(1, ValidationUtils.strength("abcdefgh"));
        assertEquals(3, ValidationUtils.strength("Abcdefg1"));
        assertEquals(4, ValidationUtils.strength("Abcdef1!"));
    }
}
