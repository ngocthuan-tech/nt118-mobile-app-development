package com.aicoach.ui.profile;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.aicoach.R;
import com.aicoach.common.Resource;
import com.aicoach.data.model.User;
import com.aicoach.data.repository.UserRepository;

/** Loads once per screen instance; rotation reuses the result and refresh is explicit. */
public class ProfileViewModel extends AndroidViewModel {
    interface ProfileLoader { LiveData<Resource<User>> load(); }
    private final ProfileLoader loader;
    private final MediatorLiveData<Resource<User>> state = new MediatorLiveData<>();
    private LiveData<Resource<User>> source;
    private User last;
    private boolean loading;

    public ProfileViewModel(@NonNull Application app) {
        this(app, new UserRepository(app)::getMe);
    }

    ProfileViewModel(Application app, ProfileLoader loader) {
        super(app);
        this.loader = loader;
        refresh();
    }

    public LiveData<Resource<User>> getState() { return state; }
    public User getLast() { return last; }

    public void refresh() {
        if (loading) return;
        loading = true;
        state.setValue(Resource.loading());
        source = loader.load();
        LiveData<Resource<User>> request = source;
        state.addSource(request, result -> {
            if (result == null) return;
            if (result.status != Resource.Status.LOADING) {
                loading = false;
                state.removeSource(request);
                source = null;
            }
            if (result.status == Resource.Status.SUCCESS) {
                if (result.data == null) {
                    state.setValue(Resource.error(getApplication().getString(R.string.profile_empty_response)));
                    return;
                }
                last = result.data;
            }
            state.setValue(result);
        });
    }

    @Override protected void onCleared() {
        if (source != null) state.removeSource(source);
    }
}
