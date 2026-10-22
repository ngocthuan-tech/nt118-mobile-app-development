package com.aicoach.data.remote.api;

import com.aicoach.data.model.User;
import com.aicoach.data.remote.ApiResponse;
import com.aicoach.data.remote.dto.ProfileDtos.ChangePasswordRequest;
import com.aicoach.data.remote.dto.ProfileDtos.UpdateProfileRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

/** User Profile — hồ sơ của người đang đăng nhập. */
public interface UserApi {

    @GET("users/me")
    Call<ApiResponse<User>> getMe();

    @PUT("users/me")
    Call<ApiResponse<User>> updateMe(@Body UpdateProfileRequest body);

    @PUT("users/me/password")
    Call<ApiResponse<Void>> changePassword(@Body ChangePasswordRequest body);
}
