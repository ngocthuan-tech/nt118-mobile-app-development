package com.aicoach.api.auth;

import com.aicoach.api.user.UserDtos;
import com.aicoach.api.user.UserDtos.UserDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank(message = "Vui lòng nhập tên hiển thị")
            @Size(min = 2, max = 50, message = "Tên hiển thị cần 2–50 ký tự")
            String displayName,

            @NotBlank(message = "Vui lòng nhập email")
            @Email(message = "Email không đúng định dạng")
            @Size(max = 120, message = "Email quá dài")
            String email,

            @NotBlank(message = "Vui lòng nhập mật khẩu")
            @Pattern(regexp = UserDtos.PASSWORD_REGEX, message = UserDtos.PASSWORD_MESSAGE)
            String password) {
    }

    public record LoginRequest(
            @NotBlank(message = "Vui lòng nhập email")
            String email,

            @NotBlank(message = "Vui lòng nhập mật khẩu")
            String password) {
    }

    /** Đăng ký và đăng nhập đều trả về cấu trúc này: token + hồ sơ để app vào thẳng Trang chủ. */
    public record AuthResponse(
            String accessToken,
            String tokenType,
            long expiresIn,
            UserDto user) {
    }
}
