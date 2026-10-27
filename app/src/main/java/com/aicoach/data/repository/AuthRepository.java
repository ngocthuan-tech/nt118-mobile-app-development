package com.aicoach.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aicoach.R;
import com.aicoach.common.Resource;
import com.aicoach.data.local.TokenManager;
import com.aicoach.data.mock.MockAuthData;
import com.aicoach.data.remote.ApiClient;
import com.aicoach.data.remote.api.AuthApi;
import com.aicoach.data.remote.dto.AuthDtos;

/**
 * Authentication (Thành viên A) — Figma 06, 07, 08–11, 12.
 * Đăng nhập / đăng ký gọi API thật và lưu token ngay khi thành công.
 * Quên mật khẩu: giữa kỳ dùng dữ liệu mẫu (MockAuthData), cùng kiểu trả về để sau chỉ đổi 1 dòng.
 */
public class AuthRepository extends BaseRepository {

    private final AuthApi api;
    private final TokenManager tokenManager;
    private final Context appContext;

    public AuthRepository(@NonNull Context context) {
        appContext = context.getApplicationContext();
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

    // ---------------------------------------------------------------- Quên mật khẩu (dữ liệu mẫu)

    /** Figma 08 → 09. TODO(giai-doan-2): return execute(api.forgotPassword(...)); */
    public LiveData<Resource<Boolean>> requestResetCode(@NonNull String email) {
        return mock(Boolean.TRUE);
    }

    /** Figma 09 → 10. Mã đúng khi demo: 123456. */
    public LiveData<Resource<Boolean>> verifyResetCode(@NonNull String email, @NonNull String code) {
        if (MockAuthData.RESET_CODE.equals(code)) return mock(Boolean.TRUE);
        return new MutableLiveData<>(Resource.error(appContext.getString(R.string.auth_otp_wrong)));
    }

    /** Figma 10 → 11. TODO(giai-doan-2): return execute(api.resetPassword(...)); */
    public LiveData<Resource<Boolean>> resetPassword(@NonNull String email, @NonNull String code,
                                                     @NonNull String newPassword) {
        return mock(Boolean.TRUE);
    }
}
