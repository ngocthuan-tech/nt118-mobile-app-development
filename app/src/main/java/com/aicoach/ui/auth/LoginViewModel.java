package com.aicoach.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aicoach.R;
import com.aicoach.common.Resource;
import com.aicoach.data.remote.dto.AuthDtos;
import com.aicoach.data.repository.AuthRepository;
import com.aicoach.util.RequestTracker;
import com.aicoach.util.ValidationUtils;

/** Figma 06/07 — kiểm tra dữ liệu nhập trước, hợp lệ mới gọi POST auth/login. */
public class LoginViewModel extends AndroidViewModel {

    private final AuthRepository repo;
    private final RequestTracker<AuthDtos.AuthResponse> request = new RequestTracker<>();
    private final MutableLiveData<Integer> emailError = new MutableLiveData<>();
    private final MutableLiveData<Integer> passwordError = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application app) {
        super(app);
        repo = new AuthRepository(app);
    }

    public LiveData<Resource<AuthDtos.AuthResponse>> getState() { return request.getState(); }

    /** id chuỗi lỗi dưới ô Email, null = hợp lệ. */
    public LiveData<Integer> getEmailError() { return emailError; }

    public LiveData<Integer> getPasswordError() { return passwordError; }

    public void login(@Nullable String rawEmail, @Nullable String password) {
        String email = ValidationUtils.normalizeEmail(rawEmail);
        Integer eErr = email.isEmpty() ? Integer.valueOf(R.string.auth_error_email_empty)
                : !ValidationUtils.isValidEmail(email) ? Integer.valueOf(R.string.auth_error_email) : null;
        Integer pErr = (password == null || password.isEmpty())
                ? Integer.valueOf(R.string.auth_error_password_empty) : null;
        emailError.setValue(eErr);
        passwordError.setValue(pErr);
        if (eErr != null || pErr != null) return;
        request.track(repo.login(email, password));
    }

    public void clearErrors() {
        if (emailError.getValue() != null) emailError.setValue(null);
        if (passwordError.getValue() != null) passwordError.setValue(null);
        Resource<AuthDtos.AuthResponse> r = request.getState().getValue();
        if (r != null && r.status == Resource.Status.ERROR) request.consume();
    }

    public void consume() {
        request.consume();
    }
}
