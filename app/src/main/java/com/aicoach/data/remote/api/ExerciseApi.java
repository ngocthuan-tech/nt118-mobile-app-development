package com.aicoach.data.remote.api;

import com.aicoach.data.model.Exercise;
import com.aicoach.data.remote.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Thư viện 9 bài tập. */
public interface ExerciseApi {

    /** Tham số có thể null: muscleGroup (legs|chest_arms|core|cardio), difficulty (easy|medium|hard), q (tìm không dấu). */
    @GET("exercises")
    Call<ApiResponse<List<Exercise>>> getExercises(@Query("muscleGroup") String muscleGroup,
                                                   @Query("difficulty") String difficulty,
                                                   @Query("q") String query);

    @GET("exercises/{id}")
    Call<ApiResponse<Exercise>> getExercise(@Path("id") String id);
}
