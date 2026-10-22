package com.aicoach.api.auth;

import com.aicoach.api.auth.AuthDtos.AuthResponse;
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
@Tag(name = "1. Authentication", description = "Đăng ký, đăng nhập — không cần token")
@SecurityRequirements
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
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
}
