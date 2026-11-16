package com.aicoach.ui.profile;

import android.app.Application;
import android.content.Context;
import android.view.View;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
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
import com.aicoach.data.model.AchievementSummary;
import com.aicoach.data.model.User;
import com.aicoach.domain.AchievementRules;
import com.google.gson.Gson;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AchievementFlowTest {
    private static Application app() { return (Application) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext(); }
    private static void main(Runnable r) { InstrumentationRegistry.getInstrumentation().runOnMainSync(r); }
    private static void idle() { InstrumentationRegistry.getInstrumentation().waitForIdleSync(); }
    private static NavHostFragment host(MainActivity a) { return (NavHostFragment) a.getSupportFragmentManager().findFragmentById(R.id.navHostFragment); }
    private static String text(MainActivity a, int id) { return ((TextView) a.findViewById(id)).getText().toString(); }
    private static AchievementSummary data(long xp, long pushupXp) {
        return new Gson().fromJson("{\"totalXp\":" + xp + ",\"exercises\":["
                + "{\"exerciseId\":\"pushup\",\"exerciseName\":\"Hít đất\",\"totalXp\":" + pushupXp + "},"
                + "{\"exerciseId\":\"squat\",\"exerciseName\":\"Squat\",\"totalXp\":0}]}", AchievementSummary.class);
    }
    private static void await(ActivityScenario<MainActivity> s, Predicate<MainActivity> condition) throws Exception {
        AtomicBoolean ok = new AtomicBoolean(); long deadline = System.currentTimeMillis() + 7000;
        do { idle(); s.onActivity(a -> ok.set(condition.test(a))); if (ok.get()) return; Thread.sleep(50); }
        while (System.currentTimeMillis() < deadline);
        fail("UI did not settle");
    }
    private static void inject(MainActivity a, MutableLiveData<Resource<AchievementSummary>> replies) {
        host(a).getChildFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override public void onFragmentAttached(FragmentManager fm, Fragment f, Context context) {
                if (f instanceof ProfileFragment || f instanceof BadgesFragment) {
                    new ViewModelProvider(f, new ViewModelProvider.Factory() {
                        @Override public <T extends ViewModel> T create(Class<T> type) {
                            return type.cast(new ProfileViewModel(a.getApplication(), () -> new MutableLiveData<>(
                                    Resource.success(new Gson().fromJson("{\"totalXp\":5000}", User.class)))));
                        }
                    }).get(ProfileViewModel.class);
                }
                if (f instanceof BadgesFragment) {
                    new ViewModelProvider(f, new ViewModelProvider.Factory() {
                        @Override public <T extends ViewModel> T create(Class<T> type) {
                            return type.cast(new AchievementsViewModel(a.getApplication(), () -> replies));
                        }
                    }).get(AchievementsViewModel.class);
                }
            }
        }, false);
        host(a).getNavController().navigate(R.id.action_global_to_profile);
    }
    private static int ranks(MainActivity a) { return ((RecyclerView) a.findViewById(R.id.rvExerciseRanks)).getAdapter().getItemCount(); }
    private static View row(MainActivity a, int index) { return ((RecyclerView)a.findViewById(R.id.rvExerciseRanks)).findViewHolderForAdapterPosition(index).itemView; }

    @Test public void realmAndExerciseRanksRenderIndependentlyAndRefreshAfterWorkout() throws Exception {
        MutableLiveData<Resource<AchievementSummary>> replies = new MutableLiveData<>(Resource.success(data(6000, 2500)));
        try (ActivityScenario<MainActivity> s = ActivityScenario.launch(MainActivity.class)) {
            s.onActivity(a -> inject(a, replies)); idle();
            s.onActivity(a -> {
                assertEquals("Cảnh giới: Kết Đan", text(a, R.id.tvProfileRealm));
                a.findViewById(R.id.btnAllBadges).performClick();
            }); idle();
            await(s, a -> ranks(a) == 2);
            s.onActivity(a -> {
                assertEquals("Kết Đan", text(a, R.id.tvSummary));
                assertEquals("6.000 EXP tổng", text(a, R.id.tvTotalAchievementXp));
                a.findViewById(R.id.achievementScroll).scrollTo(0, 0);
            });
            screenshot("achievement-realms");
            s.onActivity(a -> a.findViewById(R.id.rvExerciseRanks).requestRectangleOnScreen(
                    new android.graphics.Rect(0,0,600,650), true)); idle();
            await(s, a -> ((RecyclerView)a.findViewById(R.id.rvExerciseRanks)).findViewHolderForAdapterPosition(1) != null);
            s.onActivity(a -> {
                assertEquals("2.500 EXP · Expert", ((TextView)row(a,0).findViewById(R.id.tvExerciseRank)).getText().toString());
                assertEquals("0 EXP · Novice", ((TextView)row(a,1).findViewById(R.id.tvExerciseRank)).getText().toString());
            }); screenshot("achievement-ranks");
            s.recreate(); idle();
            await(s, a -> ranks(a) == 2);
            s.onActivity(a -> {
                assertEquals("Kết Đan", text(a,R.id.tvSummary));
                replies.setValue(Resource.success(data(15000, 5000)));
                a.findViewById(R.id.btnRetryBadges).performClick();
                assertEquals("Nguyên Anh", text(a,R.id.tvSummary));
                a.findViewById(R.id.btnRankRules).performClick();
            }); idle();
            s.onActivity(a -> {
                Fragment current = host(a).getChildFragmentManager().getPrimaryNavigationFragment();
                RankLadderDialog dialog = (RankLadderDialog)current.getChildFragmentManager().findFragmentByTag("rank-ladder");
                assertNotNull(dialog); assertTrue(dialog.requireDialog().isShowing());
            }); screenshot("achievement-ladder");
            s.recreate(); idle();
            s.onActivity(a -> {
                Fragment current = host(a).getChildFragmentManager().getPrimaryNavigationFragment();
                RankLadderDialog dialog = (RankLadderDialog)current.getChildFragmentManager().findFragmentByTag("rank-ladder");
                assertNotNull(dialog); dialog.dismiss();
            }); idle();
            s.onActivity(a -> a.findViewById(R.id.btnBack).performClick()); idle();
            s.onActivity(a -> assertEquals(R.id.profileFragment, host(a).getNavController().getCurrentDestination().getId()));
        }
    }

    @Test public void missingEndpointNeverShowsInventedRankAndRetryRetainsLastGoodData() throws Exception {
        MutableLiveData<Resource<AchievementSummary>> replies = new MutableLiveData<>(Resource.error("404"));
        try (ActivityScenario<MainActivity> s = ActivityScenario.launch(MainActivity.class)) {
            s.onActivity(a -> inject(a, replies)); idle();
            s.onActivity(a -> a.findViewById(R.id.btnAllBadges).performClick()); idle();
            s.onActivity(a -> {
                assertEquals("Kết Đan", text(a, R.id.tvSummary)); assertEquals(0, ranks(a));
                assertTrue(text(a,R.id.tvRankStatus).contains("Chưa tải được"));
                replies.setValue(Resource.success(data(6000,2500))); a.findViewById(R.id.btnRetryBadges).performClick();
            }); await(s, a -> ranks(a) == 2);
            s.onActivity(a -> {
                replies.setValue(Resource.error("offline")); a.findViewById(R.id.btnRetryBadges).performClick();
                assertTrue(text(a,R.id.tvRankStatus).contains("lần tải thành công trước"));
                assertEquals(2, ranks(a)); assertEquals("6.000 EXP tổng", text(a,R.id.tvTotalAchievementXp));
            });
        }
    }

    @Test public void viewModelPreventsDuplicateRequestsAndRejectsIncompletePayload() {
        main(() -> {
            AtomicInteger calls = new AtomicInteger();
            MutableLiveData<Resource<AchievementSummary>> response = new MutableLiveData<>(Resource.loading());
            AchievementsViewModel vm = new AchievementsViewModel(app(), () -> { calls.incrementAndGet(); return response; });
            Observer<Resource<AchievementSummary>> observer = ignored -> {};
            vm.getState().observeForever(observer); vm.refresh(); assertEquals(1,calls.get());
            response.setValue(Resource.success(new Gson().fromJson("{}",AchievementSummary.class)));
            assertEquals(Resource.Status.ERROR, vm.getState().getValue().status); assertNull(vm.getLast());
            response.setValue(Resource.success(data(0,0))); vm.refresh();
            assertEquals(2,calls.get()); assertEquals(0,vm.getLast().getTotalXp());
            vm.getState().removeObserver(observer); vm.getState().observeForever(observer);
            assertEquals(2,calls.get()); vm.getState().removeObserver(observer);
        });
    }
    @Test public void everyBadgeAssetExistsAndHasTransparentCorners() {
        for (java.util.List<AchievementRules.Tier> list : java.util.Arrays.asList(AchievementRules.REALMS,AchievementRules.RANKS))
            for (AchievementRules.Tier tier : list) {
                int id=app().getResources().getIdentifier(tier.id,"drawable",app().getPackageName()); assertNotEquals(0,id);
                android.graphics.Bitmap b=android.graphics.BitmapFactory.decodeResource(app().getResources(),id);
                assertNotNull(b); assertTrue(b.hasAlpha()); assertEquals(0,android.graphics.Color.alpha(b.getPixel(0,0))); b.recycle();
            }
    }
    private static void screenshot(String name) throws Exception {
        if (!"true".equals(InstrumentationRegistry.getArguments().getString("captureScreenshots"))) return;
        Thread.sleep(700); idle();
        android.graphics.Bitmap bitmap = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        try (java.io.FileOutputStream out = new java.io.FileOutputStream(new java.io.File(app().getExternalFilesDir("achievement-test"),name+".png"))) {
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out);
        } finally { bitmap.recycle(); }
    }
}
