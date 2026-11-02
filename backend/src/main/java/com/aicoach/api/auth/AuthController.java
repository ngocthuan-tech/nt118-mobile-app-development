package com.aicoach.api.auth;

import com.aicoach.api.auth.AuthDtos.AuthResponse;
import com.aicoach.api.auth.AuthDtos.ForgotPasswordRequest;
import com.aicoach.api.auth.AuthDtos.ResetPasswordRequest;
import com.aicoach.api.auth.AuthDtos.VerifyCodeRequest;
import com.aicoach.api.auth.reset.PasswordResetService;
import com.aicoach.api.auth.reset.PasswordResetService.CodeSentDto;
import com.aicoach.api.auth.AuthDtos.LoginRequest;
import com.aicoach.api.auth.AuthDtos.RegisterRequest;
import com.aicoach.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "1. Authentication", description = "Đăng ký, đăng nhập, quên mật khẩu — không cần token")
@SecurityRequirements
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService resetService;

    public AuthController(AuthService authService, PasswordResetService resetService) {
        this.authService = authService;
        this.resetService = resetService;
    }

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản mới, trả token để vào thẳng app")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Đăng ký thành công", authService.register(body)));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập bằng email + mật khẩu")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest body) {
        return ApiResponse.ok("Đăng nhập thành công", authService.login(body));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Quên mật khẩu (Figma 08): gửi mã 6 số tới email, hiệu lực 10 phút, 60 giây mới gửi lại được")
    public ApiResponse<CodeSentDto> forgotPassword(@Valid @RequestBody ForgotPasswordRequest body) {
        return ApiResponse.ok("Nếu email đã đăng ký, mã xác thực đã được gửi tới hộp thư",
                resetService.requestCode(body.email()));
    }

    @PostMapping("/verify-code")
    @Operation(summary = "Kiểm tra mã xác thực (Figma 09). Sai 5 lần thì phải gửi lại mã mới")
    public ApiResponse<Void> verifyCode(@Valid @RequestBody VerifyCodeRequest body) {
        resetService.verifyCode(body.email(), body.code());
        return ApiResponse.ok("Mã xác thực hợp lệ", null);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Đặt mật khẩu mới (Figma 10 → 11). Mọi phiên đăng nhập cũ bị đăng xuất")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest body) {
        resetService.resetPassword(body.email(), body.code(), body.newPassword());
        return ApiResponse.ok("Đã đặt lại mật khẩu", null);
    }
}
