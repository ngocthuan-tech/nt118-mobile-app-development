package com.aicoach.ui.auth;

import android.app.Application;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;

import com.aicoach.common.Resource;
import com.aicoach.data.remote.dto.AuthDtos;
import com.aicoach.data.repository.AuthRepository;
import com.aicoach.util.RequestTracker;
import com.aicoach.util.ResetCodePolicy;

/** Figma 09: xác thực mã và gửi lại mã bằng API thật. */
public class VerifyCodeViewModel extends AndroidViewModel {

    public static final int CODE_LENGTH = 6;

    private static final String KEY_RESEND_AT = "resendAvailableAt";
    private static final String KEY_EXPIRES = "expiresInSeconds";
    private static final String KEY_SUBMITTED_CODE = "submittedCode";

    private final AuthRepository repo;
    private final SavedStateHandle savedState;

    private final RequestTracker<Void> verify = new RequestTracker<>();
    private final RequestTracker<AuthDtos.CodeSent> resend =
            new RequestTracker<>();

    public VerifyCodeViewModel(
            @NonNull Application app,
            @NonNull SavedStateHandle savedState
    ) {
        super(app);
        this.savedState = savedState;
        repo = new AuthRepository(app);
    }

    public LiveData<Resource<Void>> getState() {
        return verify.getState();
    }

    public LiveData<Resource<AuthDtos.CodeSent>> getResendState() {
        return resend.getState();
    }

    public boolean isBusy() {
        return verify.isLoading() || resend.isLoading();
    }

    /** Mốc ban đầu được lấy từ tham số điều hướng của màn 08. */
    public int secondsUntilResend() {
        Long availableAt = savedState.get(KEY_RESEND_AT);

        if (availableAt == null) {
            return 0;
        }

        return ResetCodePolicy.secondsLeft(availableAt, SystemClock.elapsedRealtime());
    }

    public int getExpiresInSeconds() {
        Integer seconds = savedState.get(KEY_EXPIRES);
        return seconds == null ? 0 : Math.max(0, seconds);
    }

    /** Chỉ gọi sau khi API gửi lại mã đã thành công. */
    public void applyCodeSent(@NonNull AuthDtos.CodeSent data) {
        long availableAt = ResetCodePolicy.deadline(
                SystemClock.elapsedRealtime(), data.getResendAfterSeconds());

        savedState.set(KEY_RESEND_AT, availableAt);
        savedState.set(
                KEY_EXPIRES,
                Math.max(0, data.getExpiresInSeconds())
        );

        clearError();
    }

    public void resend(@NonNull String email) {
        if (isBusy() || secondsUntilResend() > 0) {
            return;
        }

        resend.track(repo.requestResetCode(email));
    }

    public void verify(
            @NonNull String email,
            @NonNull String code
    ) {
        if (isBusy() || !ResetCodePolicy.isValidCode(code)) {
            return;
        }

        // Giữ đúng mã đã gửi, tránh dùng nhầm nội dung ô nhập
        // nếu nội dung đó thay đổi trong lúc chờ server.
        savedState.set(KEY_SUBMITTED_CODE, code);

        verify.track(repo.verifyResetCode(email, code));
    }

    @NonNull
    public String getSubmittedCode() {
        String code = savedState.get(KEY_SUBMITTED_CODE);
        return code == null ? "" : code;
    }

    public void clearError() {
        Resource<Void> result = verify.getState().getValue();

        if (result != null && result.status == Resource.Status.ERROR) {
            verify.consume();
        }
    }

    public void consume() {
        verify.consume();
    }

    public void consumeResend() {
        resend.consume();
    }

    @NonNull
    static String maskEmail(@NonNull String email) {
        int at = email.indexOf('@');

        if (at <= 1) {
            return email;
        }

        String local = email.substring(0, at);
        StringBuilder masked = new StringBuilder()
                .append(local.charAt(0));

        for (int i = 1; i < Math.min(local.length() - 1, 7); i++) {
            masked.append('•');
        }

        masked.append(local.charAt(local.length() - 1));
        return masked.append(email.substring(at)).toString();
    }
}