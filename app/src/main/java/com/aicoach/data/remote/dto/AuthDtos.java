package com.aicoach.data.remote.dto;

import com.aicoach.data.model.User;

/** Request/response của /auth/register và /auth/login. */
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
