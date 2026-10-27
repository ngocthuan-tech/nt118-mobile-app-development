package com.aicoach.ui.plan;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.aicoach.common.Resource;
import com.aicoach.data.model.Plan;
import com.aicoach.data.model.User;
import com.aicoach.data.repository.PlanRepository;
import com.aicoach.util.RequestTracker;

/** Figma 26 — GET plans/{id}, tuần đang xem, "Theo giáo án này" (PUT users/me {activePlanId}). */
public class PlanDetailViewModel extends AndroidViewModel {

    private final PlanRepository repo;
    private final MediatorLiveData<Resource<Plan>> plan = new MediatorLiveData<>();
    private final MutableLiveData<Integer> selectedWeek = new MutableLiveData<>();
    private final RequestTracker<User> follow = new RequestTracker<>();
    private String loadedId;
    private boolean following;
    private boolean initialized;

    public PlanDetailViewModel(@NonNull Application app) {
        super(app);
        repo = new PlanRepository(app);
    }

    public LiveData<Resource<Plan>> getPlan() { return plan; }

    public LiveData<Integer> getSelectedWeek() { return selectedWeek; }

    public LiveData<Resource<User>> getFollowState() { return follow.getState(); }

    public boolean isFollowing() { return following; }

    /** Tuần đang xem để trống tới khi tải xong giáo án — lúc đó Fragment chọn tuần hiện tại. */
    public void init(@NonNull String id, boolean alreadyFollowing) {
        if (!initialized) {
            initialized = true;
            following = alreadyFollowing;
        }
        load(id);
    }

    public void load(@NonNull String id) {
        Resource<Plan> r = plan.getValue();
        if (id.equals(loadedId) && r != null && r.status != Resource.Status.ERROR) return;
        loadedId = id;
        LiveData<Resource<Plan>> src = repo.getPlan(id);
        plan.addSource(src, res -> {
            plan.setValue(res);
            if (res.status != Resource.Status.LOADING) plan.removeSource(src);
        });
    }

    public void selectWeek(int week) {
        Integer cur = selectedWeek.getValue();
        if (cur == null || cur != week) selectedWeek.setValue(week);
    }

    public void follow() {
        if (loadedId != null) follow.track(repo.followPlan(loadedId));
    }

    public void onFollowed() {
        following = true;
        follow.consume();
    }

    public void consumeFollow() { follow.consume(); }
}
