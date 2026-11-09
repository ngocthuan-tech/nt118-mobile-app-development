package com.aicoach.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.aicoach.common.Resource;
import com.aicoach.data.model.WorkoutSession;
import com.aicoach.data.remote.ApiClient;
import com.aicoach.data.remote.api.SessionApi;
import java.util.List;

/** Read-only access to the signed-in user's workout sessions. */
public class HistoryRepository extends BaseRepository {
    public static final int LIMIT = 50;
    private final SessionApi api;

    public HistoryRepository(Context context) {
        api = ApiClient.create(context.getApplicationContext(), SessionApi.class);
    }

    public LiveData<Resource<List<WorkoutSession>>> getSessions() {
        return execute(api.getSessions(LIMIT));
    }

    public LiveData<Resource<WorkoutSession>> getSession(String id) {
        return execute(api.getSession(id));
    }
}
