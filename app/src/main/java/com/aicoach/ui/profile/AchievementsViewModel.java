package com.aicoach.ui.profile;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.aicoach.R;
import com.aicoach.common.Resource;
import com.aicoach.data.model.AchievementSummary;
import com.aicoach.data.repository.UserRepository;

/** One request per screen instance; rotation reuses state. No synthetic or partial XP fallback. */
public final class AchievementsViewModel extends AndroidViewModel {
    interface Loader { LiveData<Resource<AchievementSummary>> load(); }
    private final Loader loader;
    private final MediatorLiveData<Resource<AchievementSummary>> state = new MediatorLiveData<>();
    private LiveData<Resource<AchievementSummary>> source;
    private AchievementSummary last;
    private boolean loading;
    public AchievementsViewModel(@NonNull Application app) { this(app, new UserRepository(app)::getAchievements); }
    AchievementsViewModel(Application app, Loader loader) { super(app); this.loader = loader; refresh(); }
    public LiveData<Resource<AchievementSummary>> getState() { return state; }
    public AchievementSummary getLast() { return last; }
    public void refresh() {
        if (loading) return;
        loading = true;
        state.setValue(Resource.loading());
        source = loader.load();
        LiveData<Resource<AchievementSummary>> request = source;
        state.addSource(request, result -> {
            if (result == null) return;
            if (result.status != Resource.Status.LOADING) {
                loading = false; state.removeSource(request); source = null;
            }
            if (result.status == Resource.Status.SUCCESS) {
                if (result.data == null || !result.data.isValid()) {
                    state.setValue(Resource.error(getApplication().getString(R.string.achievement_invalid)));
                    return;
                }
                last = result.data;
            }
            state.setValue(result);
        });
    }
    @Override protected void onCleared() { if (source != null) state.removeSource(source); }
}
