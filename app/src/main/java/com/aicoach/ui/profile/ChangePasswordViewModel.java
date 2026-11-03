package com.aicoach.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.aicoach.R;
import com.aicoach.common.Resource;
import com.aicoach.data.repository.UserRepository;
import com.aicoach.util.ValidationUtils;

/** Keeps a password-change request across view recreation, without retaining input passwords. */
public class ChangePasswordViewModel extends AndroidViewModel {
    interface PasswordChanger {
        LiveData<Resource<Void>> change(String current, String password);
    }

    public static final class Errors {
        public final int current;
        public final int password;
        public final int confirm;

        Errors(int current, int password, int confirm) {
            this.current = current;
            this.password = password;
            this.confirm = confirm;
        }

        public boolean isValid() { return current == 0 && password == 0 && confirm == 0; }
    }

    private final PasswordChanger changer;
    private final MediatorLiveData<Resource<Void>> state = new MediatorLiveData<>();
    private LiveData<Resource<Void>> source;
    private boolean inFlight;

    public ChangePasswordViewModel(@NonNull Application app) {
        this(app, new UserRepository(app)::changePassword);
    }

    ChangePasswordViewModel(Application app, PasswordChanger changer) {
        super(app);
        this.changer = changer;
    }

    public LiveData<Resource<Void>> getState() { return state; }
    public boolean isLoading() { return inFlight; }

    public static Errors validate(String current, String password, String confirm) {
        int currentError = current.trim().isEmpty() ? R.string.profile_password_current_required : 0;
        int passwordError = !ValidationUtils.isValidPassword(password)
                ? R.string.profile_password_rule
                : password.equals(current) ? R.string.profile_password_same : 0;
        int confirmError = confirm.isEmpty() ? R.string.profile_password_confirm_required
                : !password.equals(confirm) ? R.string.profile_password_mismatch : 0;
        return new Errors(currentError, passwordError, confirmError);
    }

    public Errors submit(String current, String password, String confirm) {
        Errors errors = validate(current, password, confirm);
        if (!errors.isValid() || inFlight) return errors;
        inFlight = true;
        state.setValue(Resource.loading());
        // Pass passwords unchanged: trimming would change the user's actual password.
        source = changer.change(current, password);
        LiveData<Resource<Void>> request = source;
        state.addSource(request, result -> {
            if (result == null) return;
            if (result.status != Resource.Status.LOADING) {
                inFlight = false;
                state.removeSource(request);
                source = null;
            }
            state.setValue(result);
        });
        return errors;
    }

    public void consume() {
        if (!inFlight) state.setValue(null);
    }

    @Override protected void onCleared() {
        if (source != null) state.removeSource(source);
    }
}
