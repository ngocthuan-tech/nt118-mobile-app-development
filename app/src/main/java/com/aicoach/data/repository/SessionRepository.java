package com.aicoach.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.aicoach.common.Resource;
import com.aicoach.common.WorkoutSavedSignal;
import com.aicoach.data.model.WorkoutSession;
import com.aicoach.data.remote.ApiClient;
import com.aicoach.data.remote.api.SessionApi;
import com.aicoach.data.remote.dto.CreateSessionRequest;

import java.util.List;

/** Trưởng nhóm: lưu buổi tập AI lên server (POST /sessions) và đọc các buổi gần nhất để so sánh. */
public class SessionRepository extends BaseRepository {

    private final SessionApi api;

    public SessionRepository(@NonNull Context context) {
        api = ApiClient.create(context.getApplicationContext(), SessionApi.class);
    }

    /** Lưu xong thì báo cho Trang chủ / Tiến độ biết để tải lại. */
    public LiveData<Resource<WorkoutSession>> save(@NonNull CreateSessionRequest body) {
        return execute(api.createSession(body), saved -> WorkoutSavedSignal.markChanged());
    }

    public LiveData<Resource<List<WorkoutSession>>> recent(int limit) {
        return execute(api.getSessions(limit));
    }
}
