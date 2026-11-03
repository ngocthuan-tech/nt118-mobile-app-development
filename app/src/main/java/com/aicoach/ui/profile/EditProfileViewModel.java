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
import com.aicoach.data.remote.dto.ProfileDtos;

public class EditProfileViewModel extends AndroidViewModel {
    interface Store {
        LiveData<Resource<User>> load();
        LiveData<Resource<User>> save(ProfileDtos.UpdateProfileRequest request);
    }
    private final Store store;
    private final MediatorLiveData<Resource<User>> state = new MediatorLiveData<>();
    private LiveData<Resource<User>> source;
    EditProfileForm form;
    boolean busy, saved;
    public EditProfileViewModel(@NonNull Application app) { this(app, adapter(new UserRepository(app))); }
    private static Store adapter(UserRepository repo) {
        return new Store() {
            public LiveData<Resource<User>> load() { return repo.getMe(); }
            public LiveData<Resource<User>> save(ProfileDtos.UpdateProfileRequest r) { return repo.updateMe(r); }
        };
    }
    EditProfileViewModel(Application app, Store store) { super(app); this.store = store; load(); }
    LiveData<Resource<User>> state() { return state; }
    void load() { if (!busy && form == null) start(false); }
    void save() { if (!busy && !saved && form != null && form.valid() && form.dirty()) start(true); }
    private void start(boolean saving) {
        busy = true; state.setValue(Resource.loading());
        source = saving ? store.save(form.request()) : store.load();
        LiveData<Resource<User>> request = source;
        state.addSource(request, result -> {
            if (result == null || result.status == Resource.Status.LOADING) return;
            state.removeSource(request); source = null; busy = false;
            if (result.status == Resource.Status.SUCCESS) {
                if (result.data == null) {
                    state.setValue(Resource.error(getApplication().getString(R.string.profile_empty_response))); return;
                }
                if (saving) saved = true; else form = new EditProfileForm(result.data);
            }
            state.setValue(result);
        });
    }
    @Override protected void onCleared() { if (source != null) state.removeSource(source); }
}
