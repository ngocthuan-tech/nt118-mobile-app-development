package com.aicoach.ui.profile;

import android.app.Application;
import android.content.Context;
import android.widget.EditText;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.aicoach.MainActivity;
import com.aicoach.R;
import com.aicoach.common.Resource;
import com.aicoach.data.model.User;
import com.aicoach.data.remote.dto.ProfileDtos;
import com.google.gson.Gson;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class EditProfileFlowTest {
    static User user(String name) {
        return new Gson().fromJson("{\"displayName\":\"" + name + "\",\"email\":\"fixture@example.com\",\"heightCm\":170,\"weightKg\":65,\"weeklyGoalSessions\":3}", User.class);
    }
    static class Store implements EditProfileViewModel.Store {
        final MutableLiveData<Resource<User>> load = new MutableLiveData<>(Resource.success(user("Before Edit")));
        final MutableLiveData<Resource<User>> save = new MutableLiveData<>(Resource.loading());
        int loads, saves;
        ProfileDtos.UpdateProfileRequest captured;
        public LiveData<Resource<User>> load() { loads++; return load; }
        public LiveData<Resource<User>> save(ProfileDtos.UpdateProfileRequest r) { saves++; captured = r; return save; }
    }
    @Test public void retryAndDuplicateRequestsRetainDraft() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Application app = (Application) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
            Store store = new Store(); store.load.setValue(Resource.error("offline"));
            EditProfileViewModel vm = new EditProfileViewModel(app, store);
            Observer<Resource<User>> observer = ignored -> {};
            vm.state().observeForever(observer);
            assertNull(vm.form); assertFalse(vm.busy);
            store.load.setValue(Resource.success(user("Before Edit"))); vm.load();
            assertEquals(2, store.loads);
            vm.save(); assertEquals(0, store.saves);
            vm.form.name = "After Edit";
            vm.save(); vm.save(); assertEquals(1, store.saves);
            vm.state().removeObserver(observer);
            store.save.setValue(Resource.error("offline"));
            vm.state().observeForever(observer);
            assertFalse(vm.busy); assertEquals("After Edit", vm.form.name);
            store.save.setValue(Resource.loading()); vm.save(); assertEquals(2, store.saves);
            store.save.setValue(Resource.success(user("After Edit")));
            assertTrue(vm.saved); assertFalse(vm.busy);
            vm.save(); assertEquals(2, store.saves);
            vm.state().removeObserver(observer);
        });
    }
    @Test public void editScreenSurvivesRecreationShowsErrorsAndRefreshesProfile() {
        Store store = new Store();
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                NavHostFragment host = (NavHostFragment) activity.getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
                host.getChildFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
                    @Override public void onFragmentAttached(FragmentManager fm, Fragment fragment, Context context) {
                        if (fragment instanceof EditProfileFragment || fragment instanceof ProfileFragment) {
                            ViewModelProvider provider = new ViewModelProvider(fragment, new ViewModelProvider.Factory() {
                                @Override public <T extends ViewModel> T create(Class<T> type) {
                                    if (type == EditProfileViewModel.class) return type.cast(new EditProfileViewModel(activity.getApplication(), store));
                                    return type.cast(new ProfileViewModel(activity.getApplication(), () -> store.load));
                                }
                            });
                            if (fragment instanceof EditProfileFragment) provider.get(EditProfileViewModel.class);
                            else provider.get(ProfileViewModel.class);
                        }
                    }
                }, false);
                host.getNavController().navigate(R.id.action_global_to_profile);
                host.getChildFragmentManager().executePendingTransactions();
            });
            idle();
            scenario.onActivity(a -> a.findViewById(R.id.btnEdit).performClick()); idle();
            scenario.onActivity(a -> {
                assertFalse(a.findViewById(R.id.btnSave).isEnabled());
                ((EditText)a.findViewById(R.id.etName)).setText("After Edit");
                ((EditText)a.findViewById(R.id.etHeight)).setText("99");
                a.findViewById(R.id.btnSave).performClick();
                assertEquals(0, store.saves);
                assertNotNull(((com.google.android.material.textfield.TextInputLayout)a.findViewById(R.id.tilHeight)).getError());
                ((EditText)a.findViewById(R.id.etHeight)).setText("175");
            });
            scenario.recreate(); idle();
            scenario.onActivity(a -> {
                assertEquals("After Edit", ((EditText)a.findViewById(R.id.etName)).getText().toString());
                assertEquals("175", ((EditText)a.findViewById(R.id.etHeight)).getText().toString());
                a.findViewById(R.id.btnSave).performClick();
                assertFalse(a.findViewById(R.id.btnSaveTop).isEnabled());
                assertEquals(1, store.saves);
                store.save.setValue(Resource.error("offline"));
                assertEquals("offline", ((android.widget.TextView)a.findViewById(R.id.editError)).getText().toString());
                store.save.setValue(Resource.loading());
                a.findViewById(R.id.btnSaveTop).performClick();
                assertEquals(2, store.saves);
                store.load.setValue(Resource.success(user("After Edit")));
                store.save.setValue(Resource.success(user("After Edit")));
            });
            idle();
            scenario.onActivity(a -> {
                assertEquals("After Edit", ((android.widget.TextView)a.findViewById(R.id.tvName)).getText().toString());
                NavHostFragment host = (NavHostFragment)a.getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
                assertEquals(R.id.profileFragment, host.getNavController().getCurrentDestination().getId());
            });
        }
    }
    private static void idle() { InstrumentationRegistry.getInstrumentation().waitForIdleSync(); }
}
