package com.aicoach.data.remote.api;

import com.aicoach.data.model.Plan;
import com.aicoach.data.remote.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/** Giáo án. Chọn giáo án = UserApi.updateMe với activePlanId. */
public interface PlanApi {

    @GET("plans")
    Call<ApiResponse<List<Plan>>> getPlans();

    @GET("plans/{id}")
    Call<ApiResponse<Plan>> getPlan(@Path("id") String id);
}
