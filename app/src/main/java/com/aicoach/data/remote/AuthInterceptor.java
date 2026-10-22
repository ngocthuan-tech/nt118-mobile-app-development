package com.aicoach.data.remote;

import androidx.annotation.NonNull;

import com.aicoach.data.local.SessionEvents;
import com.aicoach.data.local.TokenManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Tự gắn "Authorization: Bearer &lt;token&gt;" vào mọi request (trừ /auth/).
 * Server trả 401 ở API cần đăng nhập → xoá token và báo SessionEvents để app quay về màn Đăng nhập.
 * Không ai tự thêm header Authorization bằng tay ở chỗ khác.
 */
public class AuthInterceptor implements Interceptor {

    private final TokenManager tokenManager;

    public AuthInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        boolean isAuthCall = request.url().encodedPath().contains("/auth/");
        String token = tokenManager.getToken();

        if (token != null && !isAuthCall) {
            request = request.newBuilder().header("Authorization", "Bearer " + token).build();
        }
        Response response = chain.proceed(request);

        if (response.code() == 401 && !isAuthCall && token != null) {
            tokenManager.clear();
            SessionEvents.notifyExpired();
        }
        return response;
    }
}
