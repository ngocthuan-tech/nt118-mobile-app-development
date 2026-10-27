package com.aicoach.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aicoach.R;
import com.aicoach.common.Resource;
import com.aicoach.data.repository.AuthRepository;
import com.aicoach.util.RequestTracker;
import com.aicoach.util.ValidationUtils;

/** Figma 08 — kiểm tra email rồi xin mã xác thực (dữ liệu mẫu giữa kỳ). */
public class ForgotPasswordViewModel extends AndroidViewModel {

    private final AuthRepository repo;
    private final RequestTracker<Boolean> request = new RequestTracker<>();
    private final MutableLiveData<Integer> emailError = new MutableLiveData<>();
    private String email = "";

    public ForgotPasswordViewModel(@NonNull Application app) {
        super(app);
        repo = new AuthRepository(app);
    }

    public LiveData<Resource<Boolean>> getState() { return request.getState(); }

    public LiveData<Integer> getEmailError() { return emailError; }

    @NonNull
    public String getEmail() { return email; }

    public void send(@Nullable String raw) {
        email = ValidationUtils.normalizeEmail(raw);
        Integer err = email.isEmpty() ? Integer.valueOf(R.string.auth_error_email_empty)
                : !ValidationUtils.isValidEmail(email) ? Integer.valueOf(R.string.auth_error_email) : null;
        emailError.setValue(err);
        if (err == null) request.track(repo.requestResetCode(email));
    }

    public void clearError() {
        if (emailError.getValue() != null) emailError.setValue(null);
    }

    public void consume() { request.consume(); }
}
