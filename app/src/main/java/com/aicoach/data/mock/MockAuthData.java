package com.aicoach.data.mock;

/**
 * Dữ liệu mẫu cho luồng Quên mật khẩu (Figma 08–11) — giữa kỳ backend chưa có API gửi mã qua email.
 * Giai đoạn 2: AuthRepository đổi sang POST auth/forgot-password, auth/verify-code, auth/reset-password.
 */
public final class MockAuthData {

    /** Mã xác thực đúng khi demo. */
    public static final String RESET_CODE = "123456";

    /** Mã có hiệu lực trong 10 phút; được gửi lại sau 60 giây. */
    public static final int CODE_VALID_MINUTES = 10;
    public static final int RESEND_SECONDS = 60;

    private MockAuthData() {
    }
}
