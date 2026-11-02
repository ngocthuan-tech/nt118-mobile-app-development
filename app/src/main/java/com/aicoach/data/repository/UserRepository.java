package com.aicoach.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.aicoach.common.Resource;
import com.aicoach.data.model.User;
import com.aicoach.data.remote.ApiClient;
import com.aicoach.data.remote.api.UserApi;

/** Lấy hồ sơ của người dùng đang đăng nhập. */
public class UserRepository extends BaseRepository {

    private final UserApi api;

    public UserRepository(@NonNull Context context) {
        api = ApiClient.create(
                context.getApplicationContext(),
                UserApi.class
        );
    }

    public LiveData<Resource<User>> getMe() {
        return execute(api.getMe());
    }
}