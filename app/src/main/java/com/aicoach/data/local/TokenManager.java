package com.aicoach.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

/**
 * Giữ JWT sau khi đăng nhập. File SharedPreferences chế độ PRIVATE: chỉ app này đọc được.
 * Chỉ AuthRepository được gọi saveToken(); chỉ AuthInterceptor đọc token để gắn header.
 */
public final class TokenManager {

    private static final String FILE = "aicoach_auth";
    private static final String KEY_TOKEN = "access_token";

    private static volatile TokenManager instance;

    private final SharedPreferences prefs;

    private TokenManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static TokenManager getInstance(Context context) {
        if (instance == null) {
            synchronized (TokenManager.class) {
                if (instance == null) instance = new TokenManager(context);
            }
        }
        return instance;
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    @Nullable
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    /** Đăng xuất hoặc token hết hạn. */
    public void clear() {
        prefs.edit().remove(KEY_TOKEN).apply();
    }
}
