package com.aicoach.ui.profile;

import android.app.Application;
import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
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
import com.aicoach.data.repository.SocialRepository;
import com.aicoach.ui.challenge.SocialViewModel;
import com.google.android.material.slider.Slider;
import com.google.gson.Gson;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class SettingsSocialFlowTest {
    @org.junit.Before public void cleanDemo() { main(() -> SocialRepository.get().resetDemoData()); }
    @org.junit.After public void clearDemo() { main(() -> SocialRepository.get().resetDemoData()); }
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

    @Test public void socialValidationInvitesAndReadStateAreIndependent() {
        main(() -> {
            SocialRepository repo = SocialRepository.get();
            int before = repo.challenges().size();
            for (String name : Arrays.asList("", "ab", String.join("", Collections.nCopies(51, "x")))) {
                assertThrows(IllegalArgumentException.class, () -> repo.create(name, "squat", 50, 7, false));
            }
            assertThrows(IllegalArgumentException.class, () -> repo.create("Valid", "plank", 50, 7, false));
            assertThrows(IllegalArgumentException.class, () -> repo.create("Valid", "squat", 0, 7, false));
            assertThrows(IllegalArgumentException.class, () -> repo.create("Valid", "squat", 1001, 7, false));
            assertThrows(IllegalArgumentException.class, () -> repo.create("Valid", "squat", 50, 2, false));
            assertEquals(before, repo.challenges().size());
            String id = repo.create("  Valid name  ", "jumping_jack", 1000, 30, true);
            assertEquals("Valid name", repo.find(id).getTitle());
            assertTrue(repo.find(id).privateGroup);
            SocialRepository.ChallengeItem previous = repo.find(id);
            repo.invite(id, new HashSet<>(Arrays.asList("an", "unknown")));
            repo.invite(id, Collections.singleton("an"));
            assertEquals(1, repo.find(id).invited.size());
            assertTrue(previous.invited.isEmpty());
            assertThrows(IllegalArgumentException.class, () -> repo.invite("missing", Collections.emptySet()));
            SocialRepository.Notice invitation = repo.notices().get(0);
            repo.markRead(invitation.id);
            SocialRepository.Notice read = repo.notices().get(0);
            assertTrue(read.read); assertEquals(invitation.accepted, read.accepted); assertEquals(invitation.declined, read.declined);
            repo.readAll(); assertTrue(repo.notices().stream().allMatch(n -> n.read));
            repo.resolve("welcome", true); assertFalse(repo.notices().get(1).accepted);
        });
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
                SocialViewModel vm = new SocialViewModel(saved);
                vm.target(1001); vm.days(14); vm.exercise("lunge"); vm.filter(1); vm.select("an", true);
                SocialViewModel restored = new SocialViewModel(saved);
                assertEquals(1000, restored.target()); assertEquals(14, restored.days());
                assertEquals("lunge", restored.exercise()); assertEquals(1, restored.filter());
                assertTrue(restored.selected.contains("an"));
                assertEquals("dung", restored.friends("  anh dung ").get(0).id);
                assertTrue(restored.friends("no such friend").isEmpty());
            } finally { p.speed(oldSpeed); p.volume(oldVolume); }
        });
    }

    @Test public void createInviteSearchRotationAndLeaderboardWork() throws Exception {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(a -> host(a).getNavController().navigate(R.id.nav_challenge)); idle();
            scenario.onActivity(a -> a.findViewById(R.id.btnNational).performClick());
            await(scenario, a -> count(a, R.id.rvLeaderboard) == 5);
            screenshot("leaderboard");
            scenario.onActivity(a -> {
                assertEquals("Ngọc Minh", text(a, R.id.tvFirstName));
                assertEquals("2", ((TextView) row(a, R.id.rvLeaderboard, 1).findViewById(R.id.tvRank)).getText().toString());
                a.findViewById(R.id.btnCreate).performClick();
            }); idle();
            scenario.onActivity(a -> {
                a.findViewById(R.id.btnContinue).performClick();
                assertNotNull(((com.google.android.material.textfield.TextInputLayout) a.findViewById(R.id.tilName)).getError());
                ((EditText) a.findViewById(R.id.etName)).setText("Thử thách kiểm thử B");
                assertNull(((com.google.android.material.textfield.TextInputLayout) a.findViewById(R.id.tilName)).getError());
                a.findViewById(R.id.btnPlus).performClick();
                a.findViewById(R.id.btnDays14).performClick();
            });
            screenshot("create"); scenario.recreate(); idle();
            scenario.onActivity(a -> {
                assertEquals("Thử thách kiểm thử B", text(a, R.id.etName));
                assertEquals("60", text(a, R.id.tvTarget));
                assertTrue(((com.google.android.material.button.MaterialButton) a.findViewById(R.id.btnDays14)).isChecked());
                a.findViewById(R.id.btnContinue).performClick();
            }); idle();
            scenario.onActivity(a -> {
                assertEquals(R.id.challengeListFragment, host(a).getNavController().getCurrentDestination().getId());
                assertEquals("Thử thách kiểm thử B", text(a, R.id.tvChallengeTitle));
                a.findViewById(R.id.btnInviteFriends).performClick();
            }); idle();
            scenario.onActivity(a -> ((EditText) a.findViewById(R.id.etSearch)).setText("khong co ban"));
            await(scenario, a -> count(a, R.id.rvFriends) == 0);
            scenario.onActivity(a -> {
                assertEquals(View.VISIBLE, a.findViewById(R.id.tvFriendsEmpty).getVisibility());
                ((EditText) a.findViewById(R.id.etSearch)).setText("anh dung");
            });
            await(scenario, a -> count(a, R.id.rvFriends) == 1 && ((RecyclerView)a.findViewById(R.id.rvFriends)).findViewHolderForAdapterPosition(0) != null);
            scenario.onActivity(a -> ((CompoundButton) row(a, R.id.rvFriends, 0).findViewById(R.id.cbInvite)).setChecked(true));
            scenario.recreate(); idle();
            await(scenario, a -> count(a, R.id.rvFriends) == 1);
            scenario.onActivity(a -> {
                assertTrue(a.findViewById(R.id.btnSend).isEnabled());
                a.findViewById(R.id.btnSend).performClick();
                assertFalse(a.findViewById(R.id.btnSend).isEnabled());
            }); idle(); screenshot("invite");
            scenario.onActivity(a -> {
                CompoundButton cb = row(a, R.id.rvFriends, 0).findViewById(R.id.cbInvite);
                assertTrue(cb.isChecked()); assertFalse(cb.isEnabled());
                a.findViewById(R.id.btnBack).performClick();
            }); idle();
            scenario.onActivity(a -> assertTrue(text(a, R.id.tvChallengeMeta).contains("1 bạn")));
        }
    }

    @Test public void badgesHandleLoadingRetryEmptyFiltersAndRotation() throws Exception {
        User user = new Gson().fromJson("{\"displayName\":\"Test B\",\"totalReps\":500,\"streak\":7}", User.class);
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
                assertTrue(text(a, R.id.tvSummary).contains("4 / 6"));
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

    @Test public void notificationFiltersReadAndNavigationWork() throws Exception {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(a -> host(a).getNavController().navigate(R.id.action_global_to_notifications)); idle();
            await(scenario, a -> count(a, R.id.rvNotifications) == 2);
            screenshot("notifications");
            scenario.onActivity(a -> a.findViewById(R.id.btnFilterSystem).performClick());
            await(scenario, a -> count(a, R.id.rvNotifications) == 1);
            scenario.recreate(); idle();
            await(scenario, a -> count(a, R.id.rvNotifications) == 1);
            scenario.onActivity(a -> { a.findViewById(R.id.btnReadAll).performClick(); a.findViewById(R.id.btnFilterChallenges).performClick(); });
            await(scenario, a -> count(a, R.id.rvNotifications) == 1);
            scenario.onActivity(a -> row(a, R.id.rvNotifications, 0).findViewById(R.id.content).performClick()); idle();
            scenario.onActivity(a -> assertEquals(R.id.challengeListFragment, host(a).getNavController().getCurrentDestination().getId()));
        }
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
