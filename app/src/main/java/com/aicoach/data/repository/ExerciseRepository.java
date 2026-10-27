package com.aicoach.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.aicoach.common.Resource;
import com.aicoach.data.model.Exercise;
import com.aicoach.data.remote.ApiClient;
import com.aicoach.data.remote.api.ExerciseApi;

import java.util.List;

/** Thư viện bài tập (Figma 23, 24) và Chi tiết bài tập (25.1–25.9). */
public class ExerciseRepository extends BaseRepository {

    private final ExerciseApi api;

    public ExerciseRepository(@NonNull Context context) {
        api = ApiClient.create(context.getApplicationContext(), ExerciseApi.class);
    }

    /** query tìm không dấu ở server ("hit dat" ra "Hít đất"); null = tất cả 9 bài. */
    public LiveData<Resource<List<Exercise>>> getExercises(@Nullable String query) {
        return execute(api.getExercises(null, null, query));
    }

    public LiveData<Resource<Exercise>> getExercise(@NonNull String id) {
        return execute(api.getExercise(id));
    }
}
