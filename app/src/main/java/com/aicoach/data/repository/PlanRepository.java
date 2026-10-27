package com.aicoach.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.aicoach.common.Resource;
import com.aicoach.data.model.Plan;
import com.aicoach.data.model.User;
import com.aicoach.data.remote.ApiClient;
import com.aicoach.data.remote.api.PlanApi;
import com.aicoach.data.remote.api.UserApi;
import com.aicoach.data.remote.dto.ProfileDtos;

import java.util.List;

/** Giáo án (Figma 16, 17, 26). Chọn giáo án = PUT users/me với activePlanId. */
public class PlanRepository extends BaseRepository {

    private final PlanApi api;
    private final UserApi userApi;

    public PlanRepository(@NonNull Context context) {
        Context app = context.getApplicationContext();
        api = ApiClient.create(app, PlanApi.class);
        userApi = ApiClient.create(app, UserApi.class);
    }

    public LiveData<Resource<List<Plan>>> getPlans() {
        return execute(api.getPlans());
    }

    public LiveData<Resource<Plan>> getPlan(@NonNull String id) {
        return execute(api.getPlan(id));
    }

    /** Figma 26 "Theo giáo án này". */
    public LiveData<Resource<User>> followPlan(@NonNull String planId) {
        return execute(userApi.updateMe(new ProfileDtos.UpdateProfileRequest().activePlanId(planId)));
    }
}
