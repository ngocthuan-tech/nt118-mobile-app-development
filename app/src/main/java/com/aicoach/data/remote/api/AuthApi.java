package com.aicoach.data.remote.api;

import com.aicoach.data.remote.ApiResponse;
import com.aicoach.data.remote.dto.AuthDtos.AuthResponse;
import com.aicoach.data.remote.dto.AuthDtos.LoginRequest;
import com.aicoach.data.remote.dto.AuthDtos.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/** Authentication — không cần token. */
public interface AuthApi {

    @POST("auth/register")
    Call<ApiResponse<AuthResponse>> register(@Body RegisterRequest body);

    @POST("auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest body);
}
