package com.aicoach.data.remote.api;

import com.aicoach.data.model.HomeData;
import com.aicoach.data.remote.ApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;

/** Trang chủ — toàn bộ dữ liệu trong một lần gọi. */
public interface HomeApi {

    @GET("home")
    Call<ApiResponse<HomeData>> getHome();
}
