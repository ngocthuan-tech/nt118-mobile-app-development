package com.aicoach.ui.profile;

import android.app.Application;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.aicoach.common.Resource;
import com.aicoach.data.model.User;
import com.google.gson.Gson;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ProfileViewModelTest {
    @Test public void profileScreenRendersServerFieldsAndRefreshError() {
        User user = new Gson().fromJson("{\"displayName\":\"Profile Test\",\"email\":\"profile@example.com\","
                + "\"heightCm\":170,\"weightKg\":65,\"streak\":7,\"totalReps\":123,\"totalXp\":456}", User.class);
        MutableLiveData<Resource<User>> response = new MutableLiveData<>(Resource.success(user));
        try (androidx.test.core.app.ActivityScenario<com.aicoach.MainActivity> scenario =
                     androidx.test.core.app.ActivityScenario.launch(com.aicoach.MainActivity.class)) {
            scenario.onActivity(activity -> {
                androidx.navigation.fragment.NavHostFragment host = (androidx.navigation.fragment.NavHostFragment)
                        activity.getSupportFragmentManager().findFragmentById(com.aicoach.R.id.navHostFragment);
                host.getChildFragmentManager().registerFragmentLifecycleCallbacks(
                        new androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks() {
                            @Override public void onFragmentAttached(androidx.fragment.app.FragmentManager fm,
                                    androidx.fragment.app.Fragment fragment, android.content.Context context) {
                                if (fragment instanceof ProfileFragment) {
                                    new androidx.lifecycle.ViewModelProvider(fragment,
                                            new androidx.lifecycle.ViewModelProvider.Factory() {
                                                @Override public <T extends androidx.lifecycle.ViewModel> T create(Class<T> type) {
                                                    return type.cast(new ProfileViewModel(activity.getApplication(), () -> response));
                                                }
                                            }).get(ProfileViewModel.class);
                                }
                            }
                        }, false);
                host.getNavController().navigate(com.aicoach.R.id.action_global_to_profile);
                host.getChildFragmentManager().executePendingTransactions();
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(activity -> {
                android.widget.TextView name = activity.findViewById(com.aicoach.R.id.tvName);
                android.widget.TextView email = activity.findViewById(com.aicoach.R.id.tvEmail);
                android.widget.TextView reps = activity.findViewById(com.aicoach.R.id.tvTotalReps);
                android.widget.TextView bmi = activity.findViewById(com.aicoach.R.id.tvBmi);
                assertEquals("Profile Test", name.getText().toString());
                assertEquals("profile@example.com", email.getText().toString());
                assertEquals("123", reps.getText().toString());
                assertEquals(String.format(java.util.Locale.getDefault(), "%.1f", 65 / (1.7 * 1.7)), bmi.getText().toString());
                response.setValue(Resource.error("offline"));
                activity.findViewById(com.aicoach.R.id.btnRefreshProfile).performClick();
                android.widget.TextView error = activity.findViewById(com.aicoach.R.id.profileError);
                assertEquals("offline", error.getText().toString());
                assertEquals(android.view.View.VISIBLE, error.getVisibility());
                assertEquals("Profile Test", name.getText().toString());
                response.setValue(Resource.success(user));
                activity.findViewById(com.aicoach.R.id.btnRefreshProfile).performClick();
                assertEquals(android.view.View.GONE, error.getVisibility());
            });
        }
    }

    @Test public void refreshRetainsLastProfileAndObserverRecreationDoesNotRefetch() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Application app = (Application) InstrumentationRegistry.getInstrumentation()
                    .getTargetContext().getApplicationContext();
            AtomicInteger calls = new AtomicInteger();
            MutableLiveData<Resource<User>> reply = new MutableLiveData<>(Resource.loading());
            ProfileViewModel vm = new ProfileViewModel(app, () -> { calls.incrementAndGet(); return reply; });
            vm.refresh();
            assertEquals(1, calls.get());
            Observer<Resource<User>> observer = ignored -> {};
            vm.getState().observeForever(observer);
            User user = new Gson().fromJson("{\"displayName\":\"Test User\",\"heightCm\":170,\"weightKg\":65}", User.class);
            reply.setValue(Resource.success(user));
            assertSame(user, vm.getLast());
            vm.getState().removeObserver(observer);
            vm.getState().observeForever(observer);
            assertEquals(1, calls.get());
            reply.setValue(Resource.loading());
            vm.refresh();
            vm.refresh();
            assertEquals(2, calls.get());
            reply.setValue(Resource.error("offline"));
            assertSame(user, vm.getLast());
            assertEquals(Resource.Status.ERROR, vm.getState().getValue().status);
            reply.setValue(Resource.loading());
            vm.refresh();
            assertEquals(3, calls.get());
            reply.setValue(Resource.success(user));
            assertEquals(Resource.Status.SUCCESS, vm.getState().getValue().status);
            vm.getState().removeObserver(observer);
        });
    }

    @Test public void emptySuccessBecomesRetryableError() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Application app = (Application) InstrumentationRegistry.getInstrumentation()
                    .getTargetContext().getApplicationContext();
            ProfileViewModel vm = new ProfileViewModel(app, () -> new MutableLiveData<>(Resource.success(null)));
            Observer<Resource<User>> observer = ignored -> {};
            vm.getState().observeForever(observer);
            assertEquals(Resource.Status.ERROR, vm.getState().getValue().status);
            assertNull(vm.getLast());
            vm.refresh();
            assertEquals(Resource.Status.ERROR, vm.getState().getValue().status);
            vm.getState().removeObserver(observer);
        });
    }
}
