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

/** Figma 12 — kiểm tra đủ 5 điều kiện rồi mới gọi POST auth/register. */
public class RegisterViewModel extends AndroidViewModel {

    /** Lỗi theo từng ô; giá trị là id chuỗi, null = hợp lệ. */
    public static class FieldErrors {
        public final Integer name;
        public final Integer email;
        public final Integer password;
        public final Integer confirm;
        public final boolean terms;

        FieldErrors(Integer name, Integer email, Integer password, Integer confirm, boolean terms) {
            this.name = name;
            this.email = email;
            this.password = password;
            this.confirm = confirm;
            this.terms = terms;
        }

        boolean any() {
            return name != null || email != null || password != null || confirm != null || terms;
        }
    }

    private final AuthRepository repo;
    private final RequestTracker<AuthDtos.AuthResponse> request = new RequestTracker<>();
    private final MutableLiveData<FieldErrors> errors = new MutableLiveData<>();

    public RegisterViewModel(@NonNull Application app) {
        super(app);
        repo = new AuthRepository(app);
    }

    public LiveData<Resource<AuthDtos.AuthResponse>> getState() { return request.getState(); }

    public LiveData<FieldErrors> getErrors() { return errors; }

    public void register(@Nullable String rawName, @Nullable String rawEmail, @NonNull String password,
                         @NonNull String confirm, boolean termsAccepted) {
        String name = rawName == null ? "" : rawName.trim().replaceAll("\\s+", " ");
        String email = ValidationUtils.normalizeEmail(rawEmail);
        FieldErrors fe = new FieldErrors(
                name.isEmpty() ? Integer.valueOf(R.string.auth_error_name_empty) : null,
                email.isEmpty() ? Integer.valueOf(R.string.auth_error_email_empty)
                        : !ValidationUtils.isValidEmail(email) ? Integer.valueOf(R.string.auth_error_email) : null,
                !ValidationUtils.isValidPassword(password) ? Integer.valueOf(R.string.auth_error_password_rule) : null,
                !password.equals(confirm) ? Integer.valueOf(R.string.auth_error_password_mismatch) : null,
                !termsAccepted);
        errors.setValue(fe);
        if (fe.any()) return;
        request.track(repo.register(name, email, password));
    }

    public void clearErrors() {
        if (errors.getValue() != null) errors.setValue(null);
    }

    public void consume() { request.consume(); }
}
