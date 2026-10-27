package com.aicoach.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.aicoach.common.Resource;
import com.aicoach.data.model.HomeData;
import com.aicoach.data.remote.ApiClient;
import com.aicoach.data.remote.api.HomeApi;

/** Trang chủ (Figma 21) — một API GET home gộp Chuỗi 7 ngày + Bài tập hôm nay (Backend-for-Frontend). */
public class HomeRepository extends BaseRepository {

    private final HomeApi api;

    public HomeRepository(@NonNull Context context) {
        api = ApiClient.create(context.getApplicationContext(), HomeApi.class);
    }

    public LiveData<Resource<HomeData>> getHome() {
        return execute(api.getHome());
    }
}
