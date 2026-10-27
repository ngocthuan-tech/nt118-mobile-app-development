package com.aicoach.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.aicoach.common.Resource;
import com.aicoach.data.model.User;
import com.aicoach.data.remote.ApiClient;
import com.aicoach.data.remote.api.UserApi;
import com.aicoach.data.remote.dto.ProfileDtos;

/**
 * Khảo sát 13–15 và chọn giáo án 17 (Thành viên A). Gọi chung UserApi.updateMe với UserRepository của B —
 * mỗi bước chỉ gửi đúng field của bước đó (Gson bỏ field null).
 */
public class OnboardingRepository extends BaseRepository {

    private final UserApi userApi;

    public OnboardingRepository(@NonNull Context context) {
        userApi = ApiClient.create(context.getApplicationContext(), UserApi.class);
    }

    /** Figma 13: chiều cao, cân nặng. */
    public LiveData<Resource<User>> saveBody(double heightCm, double weightKg) {
        return execute(userApi.updateMe(new ProfileDtos.UpdateProfileRequest()
                .heightCm(heightCm).weightKg(weightKg)));
    }

    /** Figma 14: lose_weight | build_muscle | stay_fit. */
    public LiveData<Resource<User>> saveGoal(@NonNull String goal) {
        return execute(userApi.updateMe(new ProfileDtos.UpdateProfileRequest().goal(goal)));
    }

    /** Figma 15: beginner | intermediate | advanced, số buổi/tuần 1–7. */
    public LiveData<Resource<User>> saveSchedule(@NonNull String level, int sessionsPerWeek) {
        return execute(userApi.updateMe(new ProfileDtos.UpdateProfileRequest()
                .level(level).weeklyGoalSessions(sessionsPerWeek)));
    }

    /** Figma 17 "Bắt đầu hành trình": theo giáo án + đánh dấu đã hoàn tất hồ sơ. */
    public LiveData<Resource<User>> choosePlan(@NonNull String planId) {
        return execute(userApi.updateMe(new ProfileDtos.UpdateProfileRequest()
                .activePlanId(planId).profileCompleted(true)));
    }
}
