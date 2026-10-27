package com.aicoach.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.aicoach.common.Resource;
import com.aicoach.data.repository.AuthRepository;
import com.aicoach.util.RequestTracker;
import com.aicoach.util.ValidationUtils;

/** Figma 10 — mật khẩu mới phải đạt luật server và khớp ô nhập lại. */
public class NewPasswordViewModel extends AndroidViewModel {

    private final AuthRepository repo;
    private final RequestTracker<Boolean> request = new RequestTracker<>();

    public NewPasswordViewModel(@NonNull Application app) {
        super(app);
        repo = new AuthRepository(app);
    }

    public LiveData<Resource<Boolean>> getState() { return request.getState(); }

    public static boolean canSubmit(@NonNull String password, @NonNull String confirm) {
        return ValidationUtils.isValidPassword(password) && password.equals(confirm);
    }

    public void submit(@NonNull String email, @NonNull String code, @NonNull String password, @NonNull String confirm) {
        if (!canSubmit(password, confirm)) return;
        request.track(repo.resetPassword(email, code, password));
    }

    public void consume() { request.consume(); }
}
