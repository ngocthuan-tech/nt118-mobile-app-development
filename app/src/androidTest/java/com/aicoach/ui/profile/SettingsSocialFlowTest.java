package com.aicoach.ui.profile;

import android.app.Application;
import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.aicoach.MainActivity;
import com.aicoach.R;
import com.aicoach.common.Resource;
import com.aicoach.data.local.SettingsPrefs;
import com.aicoach.data.model.User;
import com.aicoach.ui.challenge.SocialViewModel;
import com.google.android.material.slider.Slider;
import com.google.gson.Gson;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class SettingsSocialFlowTest {
    private static Application app() {
        return (Application) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
    }
    private static void main(Runnable action) { InstrumentationRegistry.getInstrumentation().runOnMainSync(action); }
    private static void idle() { InstrumentationRegistry.getInstrumentation().waitForIdleSync(); }
    private static NavHostFragment host(MainActivity a) {
        return (NavHostFragment) a.getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
    }
    private static String text(MainActivity a, int id) { return ((TextView) a.findViewById(id)).getText().toString(); }
    private static void await(ActivityScenario<MainActivity> scenario, Predicate<MainActivity> condition) throws Exception {
        long deadline = System.currentTimeMillis() + 7000;
        AtomicBoolean ready = new AtomicBoolean();
        do {
            idle(); scenario.onActivity(a -> ready.set(condition.test(a)));
            if (ready.get()) return;
            Thread.sleep(50);
        } while (System.currentTimeMillis() < deadline);
        fail("UI did not reach expected state");
    }
    private static int count(MainActivity a, int id) { return ((RecyclerView) a.findViewById(id)).getAdapter().getItemCount(); }
    private static View row(MainActivity a, int id, int index) {
        return ((RecyclerView) a.findViewById(id)).findViewHolderForAdapterPosition(index).itemView;
    }
    private static void injectProfile(MainActivity a, MutableLiveData<Resource<User>> response) {
        host(a).getChildFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override public void onFragmentAttached(FragmentManager fm, Fragment f, Context context) {
                if (f instanceof BadgesFragment) {
                    new ViewModelProvider(f, new ViewModelProvider.Factory() {
                        @Override public <T extends ViewModel> T create(Class<T> type) {
                            return type.cast(new AchievementsViewModel(a.getApplication(),
                                    () -> new MutableLiveData<>(Resource.error("Not deployed"))));
                        }
                    }).get(AchievementsViewModel.class);
                }
                if (f instanceof ProfileFragment || f instanceof BadgesFragment) {
                    new ViewModelProvider(f, new ViewModelProvider.Factory() {
                        @Override public <T extends ViewModel> T create(Class<T> type) {
                            return type.cast(new ProfileViewModel(a.getApplication(), () -> response));
                        }
                    }).get(ProfileViewModel.class);
                }
            }
        }, false);
    }

    @Test public void preferencesAndSavedFiltersSurviveNewInstances() {
        main(() -> {
            VoicePreferences p = new VoicePreferences(app());
            float oldSpeed = p.speed(), oldVolume = p.volume();
            try {
                p.speed(Float.NaN); p.volume(Float.POSITIVE_INFINITY);
                assertEquals(1f, p.speed(), .001f); assertEquals(70f, p.volume(), .001f);
                p.speed(5); p.volume(-10);
                assertEquals(2f, new VoicePreferences(app()).speed(), .001f);
                assertEquals(0f, new VoicePreferences(app()).volume(), .001f);
                p.speed(1.34f); p.volume(33);
                assertEquals(1.3f, p.speed(), .001f); assertEquals(35f, p.volume(), .001f);
                SavedStateHandle saved = new SavedStateHandle();
                SocialViewModel vm = new SocialViewModel(app(), saved);
                vm.target(1001); vm.days(14); vm.exercise("lunge"); vm.filter(1); vm.select("an", true);
                SocialViewModel restored = new SocialViewModel(app(), saved);
                assertEquals(1000, restored.target()); assertEquals(14, restored.days());
                assertEquals("lunge", restored.exercise()); assertEquals(1, restored.filter());
                assertTrue(restored.selected.contains("an"));
                assertEquals("national", restored.scope());
                assertFalse(restored.join("sai ma"));       // sai định dạng → không gọi server
                assertFalse(restored.create("ab", false));  // tên < 3 ký tự → không gọi server
            } finally { p.speed(oldSpeed); p.volume(oldVolume); }
        });
    }

    @Test public void badgesHandleLoadingRetryEmptyFiltersAndRotation() throws Exception {
        User user = new Gson().fromJson("{\"displayName\":\"Test B\",\"totalXp\":5000,\"totalReps\":500,\"streak\":7}", User.class);
        MutableLiveData<Resource<User>> response = new MutableLiveData<>(Resource.loading());
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(a -> {
                injectProfile(a, response);
                host(a).getNavController().navigate(R.id.action_global_to_profile);
            }); idle();
            scenario.onActivity(a -> a.findViewById(R.id.btnAllBadges).performClick()); idle();
            scenario.onActivity(a -> {
                assertEquals(View.VISIBLE, a.findViewById(R.id.loadingBadges).getVisibility());
                response.setValue(Resource.error("offline"));
                assertEquals("offline", text(a, R.id.tvBadgeError));
                response.setValue(Resource.success(user)); a.findViewById(R.id.btnRetryBadges).performClick();
            });
            await(scenario, a -> count(a, R.id.rvBadges) == 6);
            screenshot("badges");
            scenario.onActivity(a -> {
                assertEquals("Master", text(a, R.id.tvSummary));
                a.findViewById(R.id.btnLocked).performClick();
            }); await(scenario, a -> count(a, R.id.rvBadges) == 2);
            scenario.recreate(); idle();
            await(scenario, a -> count(a, R.id.rvBadges) == 2);
            scenario.onActivity(a -> {
                response.setValue(Resource.success(new Gson().fromJson("{}", User.class)));
                a.findViewById(R.id.btnRetryBadges).performClick();
                a.findViewById(R.id.btnEarned).performClick();
            }); await(scenario, a -> count(a, R.id.rvBadges) == 0);
            scenario.onActivity(a -> assertEquals(View.VISIBLE, a.findViewById(R.id.tvBadgeEmpty).getVisibility()));
        }
    }

    @Test public void voicePreferencesPersistAcrossRotationAndMissingEngineIsExplained() throws Exception {
        SettingsPrefs settings = new SettingsPrefs(app());
        VoicePreferences prefs = new VoicePreferences(app());
        boolean oldEnabled = settings.isVoiceEnabled(), oldCount = prefs.enabled("count");
        float oldSpeed = prefs.speed(), oldVolume = prefs.volume();
        MutableLiveData<Resource<User>> response = new MutableLiveData<>(Resource.success(new Gson().fromJson("{}", User.class)));
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(a -> { injectProfile(a, response); host(a).getNavController().navigate(R.id.action_global_to_profile); }); idle();
            scenario.onActivity(a -> a.findViewById(R.id.rowVoice).performClick()); idle();
            scenario.onActivity(a -> {
                ((CompoundButton) a.findViewById(R.id.switchVoice)).setChecked(false);
                ((Slider) a.findViewById(R.id.sliderSpeed)).setValue(1.5f);
                ((Slider) a.findViewById(R.id.sliderVolume)).setValue(35f);
                ((CompoundButton) a.findViewById(R.id.cbCount)).setChecked(false);
            }); scenario.recreate(); idle();
            scenario.onActivity(a -> {
                assertFalse(((CompoundButton) a.findViewById(R.id.switchVoice)).isChecked());
                assertEquals(1.5f, ((Slider) a.findViewById(R.id.sliderSpeed)).getValue(), .001f);
                assertEquals(35f, ((Slider) a.findViewById(R.id.sliderVolume)).getValue(), .001f);
                assertFalse(((CompoundButton) a.findViewById(R.id.cbCount)).isChecked());
            });
            await(scenario, a -> !text(a, R.id.tvVoiceStatus).equals(a.getString(R.string.profile_voice_loading)));
            screenshot("voice");
            scenario.onActivity(a -> {
                String status = text(a, R.id.tvVoiceStatus);
                assertTrue(status.contains("chưa có") || status.contains("không cung cấp"));
                a.findViewById(R.id.btnBack).performClick();
            }); idle();
        } finally { settings.setVoiceEnabled(oldEnabled); prefs.enabled("count", oldCount); prefs.speed(oldSpeed); prefs.volume(oldVolume); }
    }

    private static void screenshot(String name) throws Exception {
        if (!"true".equals(InstrumentationRegistry.getArguments().getString("captureScreenshots"))) return;
        Thread.sleep(500); // Let RecyclerView diff animations and the next rendered frame finish.
        idle();
        android.graphics.Bitmap bitmap = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        java.io.File dir = app().getExternalFilesDir("b-completion-test");
        try (java.io.FileOutputStream out = new java.io.FileOutputStream(new java.io.File(dir, name + ".png"))) {
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out);
        } finally { bitmap.recycle(); }
    }
}
