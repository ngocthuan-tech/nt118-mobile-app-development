package com.aicoach.data.local;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Báo cho MainActivity biết phiên đăng nhập đã hết (server trả 401) để đưa người dùng về màn Đăng nhập.
 * AuthInterceptor hoặc đổi mật khẩu thành công phát tín hiệu; lý do lưu ở TokenManager.
 * MainActivity chỉ xử lý nếu lý do vẫn thuộc phiên đã kết thúc, không ảnh hưởng lần đăng nhập mới.
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
