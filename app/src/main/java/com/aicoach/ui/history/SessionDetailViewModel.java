package com.aicoach.ui.history;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import com.aicoach.R;
import com.aicoach.common.Resource;
import com.aicoach.data.model.WorkoutSession;
import com.aicoach.data.repository.HistoryRepository;
import java.util.function.Function;

public class SessionDetailViewModel extends AndroidViewModel {
    private final HistoryLoad<WorkoutSession> load = new HistoryLoad<>();
    private final Function<String, LiveData<Resource<WorkoutSession>>> loader;
    private final String id;

    public SessionDetailViewModel(Application app, SavedStateHandle saved) {
        this(app, saved.get("sessionId"), new HistoryRepository(app)::getSession);
    }
    SessionDetailViewModel(Application app, String id,
            Function<String, LiveData<Resource<WorkoutSession>>> loader) {
        super(app); this.id = id; this.loader = loader; refresh();
    }
    public LiveData<Resource<WorkoutSession>> state() { return load.state; }
    public WorkoutSession session() { return load.data; }
    public void refresh() {
        if (id == null || id.trim().isEmpty()) {
            load.state.setValue(Resource.error(getApplication().getString(R.string.progress_missing_id))); return;
        }
        load.run(() -> loader.apply(id), getApplication().getString(R.string.progress_response_empty));
    }
    @Override protected void onCleared() { load.clear(); }
}
