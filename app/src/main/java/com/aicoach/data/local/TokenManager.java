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
    private static final String KEY_END_REASON = "session_end_reason";

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

    public synchronized void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).remove(KEY_END_REASON).apply();
    }

    @Nullable
    public synchronized String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    /** Đăng xuất hoặc token hết hạn. */
    public synchronized void clear() {
        prefs.edit().remove(KEY_TOKEN).remove(KEY_END_REASON).apply();
    }

    /** A late response for an old session must never clear a newer login. */
    public synchronized boolean expireIfMatches(@Nullable String expected, SessionEndReason reason) {
        if (expected == null || !expected.equals(getToken())) return false;
        prefs.edit().remove(KEY_TOKEN).putString(KEY_END_REASON, reason.name()).apply();
        return true;
    }

    /** Persisted until the foreground UI can show it; survives a process restart. */
    @Nullable public synchronized SessionEndReason takeEndReason() {
        if (isLoggedIn()) return null;
        String value = prefs.getString(KEY_END_REASON, null);
        if (value == null) return null;
        prefs.edit().remove(KEY_END_REASON).apply();
        try { return SessionEndReason.valueOf(value); }
        catch (IllegalArgumentException e) { return SessionEndReason.EXPIRED; }
    }
}
