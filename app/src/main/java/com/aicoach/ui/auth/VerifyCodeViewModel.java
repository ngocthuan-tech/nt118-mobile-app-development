package com.aicoach.ui.auth;

import android.app.Application;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.aicoach.common.Resource;
import com.aicoach.data.mock.MockAuthData;
import com.aicoach.data.repository.AuthRepository;
import com.aicoach.util.RequestTracker;

/** Figma 09 — giữ mốc "gửi lại mã" trong ViewModel nên xoay màn hình không reset đồng hồ. */
public class VerifyCodeViewModel extends AndroidViewModel {

    public static final int CODE_LENGTH = 6;

    private final AuthRepository repo;
    private final RequestTracker<Boolean> verify = new RequestTracker<>();
    private long resendAvailableAt;

    public VerifyCodeViewModel(@NonNull Application app) {
        super(app);
        repo = new AuthRepository(app);
        restartTimer();
    }

    public LiveData<Resource<Boolean>> getState() { return verify.getState(); }

    /** Số giây còn lại trước khi được gửi lại mã (0 = được gửi). */
    public int secondsUntilResend() {
        long left = resendAvailableAt - SystemClock.elapsedRealtime();
        return left <= 0 ? 0 : (int) Math.ceil(left / 1000.0);
    }

    public void resend(@NonNull String email) {
        if (secondsUntilResend() > 0) return;
        repo.requestResetCode(email);
        restartTimer();
    }

    public void verify(@NonNull String email, @NonNull String code) {
        if (code.length() != CODE_LENGTH) return;
        verify.track(repo.verifyResetCode(email, code));
    }

    public void clearError() {
        Resource<Boolean> r = verify.getState().getValue();
        if (r != null && r.status == Resource.Status.ERROR) verify.consume();
    }

    public void consume() { verify.consume(); }

    private void restartTimer() {
        resendAvailableAt = SystemClock.elapsedRealtime() + MockAuthData.RESEND_SECONDS * 1000L;
    }

    /** "minh.nguyen@gmail.com" → "m••••••n@gmail.com". */
    @NonNull
    static String maskEmail(@NonNull String email) {
        int at = email.indexOf('@');
        if (at <= 1) return email;
        String local = email.substring(0, at);
        StringBuilder sb = new StringBuilder().append(local.charAt(0));
        for (int i = 1; i < Math.min(local.length() - 1, 7); i++) sb.append('•');
        if (local.length() > 1) sb.append(local.charAt(local.length() - 1));
        return sb.append(email.substring(at)).toString();
    }
}
