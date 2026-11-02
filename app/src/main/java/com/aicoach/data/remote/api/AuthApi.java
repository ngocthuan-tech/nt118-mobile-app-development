package com.aicoach.data.remote.api;

import com.aicoach.data.remote.ApiResponse;
import com.aicoach.data.remote.dto.AuthDtos.AuthResponse;
import com.aicoach.data.remote.dto.AuthDtos.CodeSent;
import com.aicoach.data.remote.dto.AuthDtos.ForgotPasswordRequest;
import com.aicoach.data.remote.dto.AuthDtos.LoginRequest;
import com.aicoach.data.remote.dto.AuthDtos.RegisterRequest;
import com.aicoach.data.remote.dto.AuthDtos.ResetPasswordRequest;
import com.aicoach.data.remote.dto.AuthDtos.VerifyCodeRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/** Authentication — không cần token. */
public interface AuthApi {

    @POST("auth/register")
    Call<ApiResponse<AuthResponse>> register(@Body RegisterRequest body);

    @POST("auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest body);

    /** Figma 08: gửi mã 6 số tới email. Luôn trả thành công dù email có tồn tại hay không. */
    @POST("auth/forgot-password")
    Call<ApiResponse<CodeSent>> forgotPassword(@Body ForgotPasswordRequest body);

    /** Figma 09: kiểm tra mã. Sai → lỗi 400 kèm "Bạn còn N lần thử"; sai 5 lần phải gửi lại mã. */
    @POST("auth/verify-code")
    Call<ApiResponse<Void>> verifyCode(@Body VerifyCodeRequest body);

    /** Figma 10 → 11: đặt mật khẩu mới. Thành công thì mọi phiên đăng nhập cũ bị đăng xuất. */
    @POST("auth/reset-password")
    Call<ApiResponse<Void>> resetPassword(@Body ResetPasswordRequest body);
}
