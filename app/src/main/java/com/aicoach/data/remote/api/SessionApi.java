package com.aicoach.data.remote.api;

import com.aicoach.data.model.WorkoutSession;
import com.aicoach.data.remote.ApiResponse;
import com.aicoach.data.remote.dto.CreateSessionRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Buổi tập: Trưởng nhóm lưu ở màn Kết quả; Thành viên B đọc cho Lịch sử / Chi tiết buổi tập. */
public interface SessionApi {

    @POST("sessions")
    Call<ApiResponse<WorkoutSession>> createSession(@Body CreateSessionRequest body);

    /** Mới nhất trước, tối đa 100. */
    @GET("sessions")
    Call<ApiResponse<List<WorkoutSession>>> getSessions(@Query("limit") int limit);

    @GET("sessions/{id}")
    Call<ApiResponse<WorkoutSession>> getSession(@Path("id") String id);
}
