package com.aicoach.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.aicoach.common.Resource;
import com.aicoach.data.local.SessionEndReason;
import com.aicoach.data.local.SessionEvents;
import com.aicoach.data.local.TokenManager;
import com.aicoach.data.model.User;
import com.aicoach.data.remote.ApiClient;
import com.aicoach.data.remote.AuthenticatedSession;
import com.aicoach.data.remote.api.UserApi;
import com.aicoach.data.remote.dto.ProfileDtos;

/** Lấy hồ sơ của người dùng đang đăng nhập. */
public class UserRepository extends BaseRepository {

    private final UserApi api;
    private final TokenManager tokens;

    public UserRepository(@NonNull Context context) {
        this(ApiClient.create(context.getApplicationContext(), UserApi.class), TokenManager.getInstance(context));
    }

    UserRepository(UserApi api, TokenManager tokens) {
        this.api = api;
        this.tokens = tokens;
    }

    public LiveData<Resource<User>> getMe() {
        return execute(api.getMe());
    }

    public LiveData<Resource<com.aicoach.data.model.AchievementSummary>> getAchievements() {
        return execute(api.getAchievements());
    }

    public LiveData<Resource<User>> updateMe(@NonNull ProfileDtos.UpdateProfileRequest request) {
        return execute(api.updateMe(request));
    }

    public LiveData<Resource<Void>> changePassword(
            @NonNull String currentPassword,
            @NonNull String newPassword
    ) {
        ProfileDtos.ChangePasswordRequest request =
                new ProfileDtos.ChangePasswordRequest(
                        currentPassword,
                        newPassword
                );

        String initiatingToken = tokens.getToken();
        return executeCompletion(api.changePassword(request, new AuthenticatedSession(initiatingToken)), () -> {
            if (tokens.expireIfMatches(initiatingToken, SessionEndReason.PASSWORD_UPDATED_HERE))
                SessionEvents.notifyExpired();
        });
    }

}
