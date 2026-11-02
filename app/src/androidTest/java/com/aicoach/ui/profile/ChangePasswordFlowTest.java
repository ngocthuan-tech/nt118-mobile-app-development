package com.aicoach.ui.profile;

import android.app.Application;
import android.graphics.Bitmap;
import android.widget.EditText;
import android.widget.TextView;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.aicoach.BuildConfig;
import com.aicoach.MainActivity;
import com.aicoach.R;
import com.aicoach.common.Resource;
import com.aicoach.data.local.TokenManager;
import com.aicoach.data.remote.api.AuthApi;
import com.aicoach.data.remote.dto.AuthDtos;
import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.junit.runner.RunWith;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import com.google.android.material.textfield.TextInputLayout;

@RunWith(AndroidJUnit4.class)
public class ChangePasswordFlowTest {
    private Application app() {
        return (Application) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
    }

    @Test public void validationNeverSendsRequest() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            AtomicInteger calls = new AtomicInteger();
            ChangePasswordViewModel vm = new ChangePasswordViewModel(app(), (current, password) -> {
                calls.incrementAndGet();
                return new MutableLiveData<>(Resource.success(null));
            });
            vm.submit("", "NewPass123", "NewPass123");
            vm.submit("OldPass123", "OldPass123", "OldPass123");
            vm.submit("OldPass123", "NewPass123", "Mismatch123");
            assertEquals(0, calls.get());
            assertFalse(vm.isLoading());
        });
    }

    @Test public void blocksDuplicatesWithoutObserverAndAllowsRetryAfterError() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            AtomicInteger calls = new AtomicInteger();
            MutableLiveData<Resource<Void>> response = new MutableLiveData<>(Resource.loading());
            ChangePasswordViewModel vm = new ChangePasswordViewModel(app(), (current, password) -> {
                assertEquals(" OldPass123 ", current);
                assertEquals(" NewPass123 ", password);
                calls.incrementAndGet();
                return response;
            });
            vm.submit(" OldPass123 ", " NewPass123 ", " NewPass123 ");
            vm.submit(" OldPass123 ", " NewPass123 ", " NewPass123 ");
            assertEquals(1, calls.get());
            assertTrue(vm.isLoading());
            Observer<Resource<Void>> observer = ignored -> {};
            vm.getState().observeForever(observer);
            vm.getState().removeObserver(observer); // UI temporarily stopped/recreated.
            response.setValue(Resource.error("Không có kết nối mạng"));
            vm.getState().observeForever(observer);
            assertFalse(vm.isLoading());
            assertEquals(Resource.Status.ERROR, vm.getState().getValue().status);
            response.setValue(Resource.loading());
            vm.submit(" OldPass123 ", " NewPass123 ", " NewPass123 ");
            assertEquals(2, calls.get());
            response.setValue(Resource.success(null));
            assertEquals(Resource.Status.SUCCESS, vm.getState().getValue().status);
            vm.consume();
            assertNull(vm.getState().getValue());
            vm.getState().removeObserver(observer);
        });
    }

    /** Opt-in: creates ONE disposable account on the configured backend, never changes a demo account. */
    @Test public void liveDialogChangesPasswordAndNewPasswordCanLogIn() throws Exception {
        assumeTrue("true".equals(InstrumentationRegistry.getArguments().getString("livePasswordTest")));
        AuthApi auth = new Retrofit.Builder().baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build().create(AuthApi.class);
        String email = "password-test-" + UUID.randomUUID() + "@example.com";
        String oldPassword = "Old-" + UUID.randomUUID() + "9";
        String newPassword = "New-" + UUID.randomUUID() + "8";
        String displayName = InstrumentationRegistry.getArguments().getString("testDisplayName", "Password Test");
        var registered = auth.register(new AuthDtos.RegisterRequest(displayName, email, oldPassword)).execute();
        assertTrue("Disposable registration failed: " + registered.code(), registered.isSuccessful());
        assertNotNull(registered.body());
        TokenManager tokens = TokenManager.getInstance(app());
        String previousToken = tokens.getToken();
        tokens.saveToken(registered.body().getData().getAccessToken());
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> new ChangePasswordDialogFragment()
                    .showNow(activity.getSupportFragmentManager(), "password-test"));
            pressSubmit(scenario);
            scenario.onActivity(activity -> {
                TextInputLayout field = dialog(activity).requireView().findViewById(R.id.tilCurrent);
                assertEquals(activity.getString(R.string.profile_password_current_required), field.getError());
            });
            edit(scenario, R.id.etCurrent, "WrongPass123");
            edit(scenario, R.id.etNew, newPassword);
            edit(scenario, R.id.etConfirm, newPassword);
            scenario.recreate(); // Keep entered fields across rotation/recreation.
            scenario.onActivity(activity -> {
                EditText field = dialog(activity).requireView().findViewById(R.id.etNew);
                assertEquals(newPassword, field.getText().toString());
            });
            pressSubmit(scenario);
            waitUntil(() -> {
                AtomicBoolean done = new AtomicBoolean();
                scenario.onActivity(activity -> {
                    var dialog = activity.getSupportFragmentManager().findFragmentByTag("password-test");
                    if (dialog != null) {
                        var vm = new androidx.lifecycle.ViewModelProvider(dialog).get(ChangePasswordViewModel.class);
                        done.set(vm.getState().getValue() != null
                                && vm.getState().getValue().status == Resource.Status.ERROR);
                    }
                });
                return done.get();
            });
            scenario.onActivity(activity -> {
                var view = dialog(activity).requireView();
                TextView error = view.findViewById(R.id.tvError);
                assertEquals("Mật khẩu hiện tại không đúng", error.getText().toString());
                assertTrue(view.findViewById(R.id.btnSubmit).isEnabled());
            });
            Bitmap screenshot = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
            if (screenshot != null) {
                try (FileOutputStream stream = new FileOutputStream(new File(app().getExternalFilesDir(null), "password-change.png"))) {
                    screenshot.compress(Bitmap.CompressFormat.PNG, 100, stream);
                } finally { screenshot.recycle(); }
            }
            edit(scenario, R.id.etCurrent, oldPassword);
            pressSubmit(scenario);
            waitUntil(() -> {
                AtomicBoolean closed = new AtomicBoolean();
                scenario.onActivity(activity -> closed.set(activity.getSupportFragmentManager()
                        .findFragmentByTag("password-test") == null));
                return closed.get();
            });
            assertTrue(auth.login(new AuthDtos.LoginRequest(email, newPassword)).execute().isSuccessful());
            assertEquals(401, auth.login(new AuthDtos.LoginRequest(email, oldPassword)).execute().code());
        } finally {
            if (previousToken == null) tokens.clear(); else tokens.saveToken(previousToken);
        }
    }

    private interface Condition { boolean met(); }

    private ChangePasswordDialogFragment dialog(MainActivity activity) {
        return (ChangePasswordDialogFragment) activity.getSupportFragmentManager().findFragmentByTag("password-test");
    }

    // Operate real widgets on the UI thread; Espresso's legacy input injector is incompatible with API 37.
    private void edit(ActivityScenario<MainActivity> scenario, int id, String value) {
        scenario.onActivity(activity -> {
            EditText field = dialog(activity).requireView().findViewById(id);
            field.setText(value);
        });
    }

    private void pressSubmit(ActivityScenario<MainActivity> scenario) {
        scenario.onActivity(activity -> dialog(activity).requireView().findViewById(R.id.btnSubmit).performClick());
    }

    private void waitUntil(Condition condition) throws Exception {
        long deadline = System.currentTimeMillis() + 30000;
        while (System.currentTimeMillis() < deadline) {
            if (condition.met()) return;
            Thread.sleep(100);
        }
        fail("Timed out waiting for password request");
    }
}
