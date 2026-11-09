package com.aicoach.data.local;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import com.aicoach.data.remote.ApiClient;
import com.aicoach.data.remote.ApiResponse;
import com.aicoach.data.remote.api.UserApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Checks only while the Activity is visible. Offline failures never log the user out. */
public final class SessionMonitor {
    interface Probe { Call<ApiResponse<Void>> create(); }
    private final TokenManager tokens;
    private final Probe probe;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final long interval;
    private boolean running;
    private int generation;
    private Call<ApiResponse<Void>> active;
    private final Runnable tick = this::check;

    public SessionMonitor(Context context) {
        this(TokenManager.getInstance(context), ApiClient.create(context, UserApi.class)::checkSession, 30_000);
    }
    SessionMonitor(TokenManager tokens, Probe probe, long interval) {
        this.tokens = tokens; this.probe = probe; this.interval = interval;
    }
    public void start() { if (!running) { running = true; generation++; check(); } }
    public void stop() {
        running = false; generation++; handler.removeCallbacks(tick);
        if (active != null) active.cancel();
        active = null;
    }
    private void check() {
        if (!running || active != null) return;
        if (!tokens.isLoggedIn()) { handler.postDelayed(tick, interval); return; }
        int requestGeneration = generation;
        active = probe.create();
        active.enqueue(new Callback<ApiResponse<Void>>() {
            private void done() {
                handler.post(() -> {
                    if (!running || generation != requestGeneration) return;
                    active = null; handler.postDelayed(tick, interval);
                });
            }
            @Override public void onResponse(@NonNull Call<ApiResponse<Void>> c, @NonNull Response<ApiResponse<Void>> response) { done(); }
            @Override public void onFailure(@NonNull Call<ApiResponse<Void>> c, @NonNull Throwable error) { done(); }
        });
    }
}
