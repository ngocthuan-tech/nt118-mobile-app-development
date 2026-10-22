package com.aicoach.data.local;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Báo cho MainActivity biết phiên đăng nhập đã hết (server trả 401) để đưa người dùng về màn Đăng nhập.
 * AuthInterceptor phát sự kiện, MainActivity lắng nghe — các màn khác không cần tự xử lý 401.
 */
public final class SessionEvents {

    private static final MutableLiveData<Boolean> expired = new MutableLiveData<>(false);

    private SessionEvents() {
    }

    public static LiveData<Boolean> expired() {
        return expired;
    }

    public static void notifyExpired() {
        expired.postValue(true);
    }

    public static void consume() {
        expired.setValue(false);
    }
}
