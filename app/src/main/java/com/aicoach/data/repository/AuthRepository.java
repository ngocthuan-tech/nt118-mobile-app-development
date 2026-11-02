package com.aicoach.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.aicoach.common.Resource;
import com.aicoach.data.local.TokenManager;
import com.aicoach.data.remote.ApiClient;
import com.aicoach.data.remote.api.AuthApi;
import com.aicoach.data.remote.dto.AuthDtos;

/**
 * Authentication (Thành viên A) — Figma 06, 07, 08–11, 12.
 * Đăng nhập / đăng ký gọi API thật và lưu token ngay khi thành công.
 * Quên mật khẩu: gửi mã, xác thực và đặt mật khẩu mới bằng API thật.
 */
public class AuthRepository extends BaseRepository {

    private final AuthApi api;
    private final TokenManager tokenManager;

    public AuthRepository(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        api = ApiClient.create(appContext, AuthApi.class);
        tokenManager = TokenManager.getInstance(appContext);
    }

    public LiveData<Resource<AuthDtos.AuthResponse>> login(@NonNull String email, @NonNull String password) {
        return execute(api.login(new AuthDtos.LoginRequest(email, password)),
                res -> tokenManager.saveToken(res.getAccessToken()));
    }

    public LiveData<Resource<AuthDtos.AuthResponse>> register(@NonNull String name, @NonNull String email,
                                                              @NonNull String password) {
        return execute(api.register(new AuthDtos.RegisterRequest(name, email, password)),
                res -> tokenManager.saveToken(res.getAccessToken()));
    }

    // ---------------------------------------------------------------- Quên mật khẩu (API thật)

    /** Figma 08 → 09: gọi API thật để yêu cầu gửi mã xác thực. */
    public LiveData<Resource<AuthDtos.CodeSent>> requestResetCode(
            @NonNull String email
    ) {
        return execute(
                api.forgotPassword(new AuthDtos.ForgotPasswordRequest(email))
        );
    }

    /** Figma 09 → 10: xác thực mã với server. */
    public LiveData<Resource<Void>> verifyResetCode(
            @NonNull String email,
            @NonNull String code
    ) {
        return execute(
                api.verifyCode(new AuthDtos.VerifyCodeRequest(email, code))
        );
    }

    /** Figma 10 → 11: thành công chỉ khi server đã đặt lại mật khẩu. */
    public LiveData<Resource<Void>> resetPassword(@NonNull String email, @NonNull String code,
                                                  @NonNull String newPassword) {
        return execute(api.resetPassword(new AuthDtos.ResetPasswordRequest(email, code, newPassword)));
    }
}
