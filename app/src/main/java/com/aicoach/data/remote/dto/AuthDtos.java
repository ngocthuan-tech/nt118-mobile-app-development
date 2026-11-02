package com.aicoach.data.remote.dto;

import com.aicoach.data.model.User;

/** Request/response của auth/register, auth/login và quên mật khẩu (auth/forgot-password, verify-code, reset-password). */
public final class AuthDtos {

    private AuthDtos() {
    }

    public static class RegisterRequest {
        final String displayName;
        final String email;
        final String password;

        public RegisterRequest(String displayName, String email, String password) {
            this.displayName = displayName;
            this.email = email;
            this.password = password;
        }
    }

    public static class LoginRequest {
        final String email;
        final String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    /** Figma 08 — POST auth/forgot-password. */
    public static class ForgotPasswordRequest {
        final String email;

        public ForgotPasswordRequest(String email) {
            this.email = email;
        }
    }

    /** Trả về sau khi gửi mã: hiện "Gửi lại mã sau 00:60", "Mã có hiệu lực trong 10 phút". */
    public static class CodeSent {
        private int resendAfterSeconds;
        private int expiresInSeconds;

        public int getResendAfterSeconds() { return resendAfterSeconds; }
        public int getExpiresInSeconds() { return expiresInSeconds; }
    }

    /** Figma 09 — POST auth/verify-code. */
    public static class VerifyCodeRequest {
        final String email;
        final String code;

        public VerifyCodeRequest(String email, String code) {
            this.email = email;
            this.code = code;
        }
    }

    /** Figma 10 — POST auth/reset-password: gửi lại email + mã đã xác thực ở bước 09 + mật khẩu mới. */
    public static class ResetPasswordRequest {
        final String email;
        final String code;
        final String newPassword;

        public ResetPasswordRequest(String email, String code, String newPassword) {
            this.email = email;
            this.code = code;
            this.newPassword = newPassword;
        }
    }

    /** Đăng ký và đăng nhập đều trả token + hồ sơ để vào thẳng Trang chủ. */
    public static class AuthResponse {
        private String accessToken;
        private String tokenType;
        private long expiresIn;
        private User user;

        public String getAccessToken() { return accessToken; }
        public String getTokenType() { return tokenType; }
        public long getExpiresIn() { return expiresIn; }
        public User getUser() { return user; }
    }
}
