package com.aicoach.ui.history;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
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
import com.aicoach.data.model.WorkoutSession;
import com.google.gson.Gson;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class HistoryFlowTest {
    private static WorkoutSession session(String id, LocalDate date, int reps) {
        String time = date.atTime(12,0).atZone(HistoryStats.ZONE).toInstant().toString();
        return new Gson().fromJson("{\"id\":\""+id+"\",\"exerciseId\":\"pushup\",\"exerciseName\":\"Push-up\",\"startedAt\":\""+time
                +"\",\"totalReps\":"+reps+",\"correctReps\":8,\"targetReps\":15,\"durationSec\":90,\"accuracy\":0.8,\"calories\":12.5,\"xpEarned\":24,\"formErrors\":{\"back_not_straight\":2}}",WorkoutSession.class);
    }
    private static Application app() {
        return (Application) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
    }
    private static void idle() { InstrumentationRegistry.getInstrumentation().waitForIdleSync(); }
    private static NavHostFragment host(MainActivity a) { return (NavHostFragment)a.getSupportFragmentManager().findFragmentById(R.id.navHostFragment); }
    private static String text(MainActivity a,int id) { return ((TextView)a.findViewById(id)).getText().toString(); }
    @Test public void loadingRetryKeepsDataAndBlocksDuplicateRequests() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            AtomicInteger calls=new AtomicInteger();
            MutableLiveData<Resource<List<WorkoutSession>>> response=new MutableLiveData<>(Resource.loading());
            SavedStateHandle saved=new SavedStateHandle();
            HistoryViewModel vm=new HistoryViewModel(app(),saved,()->{calls.incrementAndGet();return response;});
            vm.refresh(); assertEquals(1,calls.get());
            Observer<Resource<List<WorkoutSession>>> observer=ignored->{};
            vm.state().observeForever(observer);
            response.setValue(Resource.success(Collections.singletonList(session("one",LocalDate.now(HistoryStats.ZONE),10))));
            assertTrue(vm.hasData());
            vm.range(2); vm.moveMonth(-1); vm.day(vm.month().atDay(2));
            response.setValue(Resource.loading()); vm.refresh(); vm.refresh(); assertEquals(2,calls.get());
            vm.state().removeObserver(observer);
            response.setValue(Resource.error("offline")); vm.state().observeForever(observer);
            assertEquals(Resource.Status.ERROR,vm.state().getValue().status); assertEquals(1,vm.sessions().size());
            assertEquals(2,vm.range()); assertEquals(2,vm.day().getDayOfMonth());
            response.setValue(Resource.success(Collections.emptyList())); vm.refresh();
            assertTrue(vm.hasData()); assertTrue(vm.sessions().isEmpty());
            response.setValue(Resource.success(null)); vm.refresh(); assertEquals(Resource.Status.ERROR,vm.state().getValue().status);
            vm.state().removeObserver(observer);
        });
    }
    @Test public void detailRejectsMissingIdAndAllowsRetryAfterNotFound() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            AtomicInteger calls=new AtomicInteger();
            MutableLiveData<Resource<WorkoutSession>> response=new MutableLiveData<>(Resource.error("not found"));
            SessionDetailViewModel missing=new SessionDetailViewModel(app(),"",id->{calls.incrementAndGet();return response;});
            assertEquals(0,calls.get()); assertEquals(Resource.Status.ERROR,missing.state().getValue().status);
            SessionDetailViewModel vm=new SessionDetailViewModel(app(),"session-id",id->{assertEquals("session-id",id);calls.incrementAndGet();return response;});
            Observer<Resource<WorkoutSession>> observer=ignored->{};
            vm.state().observeForever(observer); assertNull(vm.session());
            response.setValue(Resource.success(session("session-id",LocalDate.now(HistoryStats.ZONE),10))); vm.refresh();
            assertEquals("session-id",vm.session().getId()); assertEquals(2,calls.get());
            vm.state().removeObserver(observer);
        });
    }
    @Test public void historyCalendarDetailAndRotationUseRealIds() throws Exception {
        LocalDate today=LocalDate.now(HistoryStats.ZONE);
        List<WorkoutSession> fixtures=Arrays.asList(session("one",today,10),session("two",today,20),session("old",today.minusDays(40),30));
        MutableLiveData<Resource<List<WorkoutSession>>> response=new MutableLiveData<>(Resource.success(fixtures));
        MutableLiveData<Resource<WorkoutSession>> detail=new MutableLiveData<>(Resource.success(fixtures.get(0)));
        AtomicInteger loads=new AtomicInteger();
        try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(a->{
                NavHostFragment host=host(a);
                host.getChildFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks(){
                    @Override public void onFragmentAttached(FragmentManager fm,Fragment fragment,Context context){
                        if(fragment instanceof HistoryFragment) {
                            new ViewModelProvider(host.getNavController().getViewModelStoreOwner(R.id.nav_social),new ViewModelProvider.Factory(){
                                @Override public <T extends ViewModel> T create(Class<T> type){return type.cast(new HistoryViewModel(a.getApplication(),new SavedStateHandle(),()->{loads.incrementAndGet();return response;}));}
                            }).get(HistoryViewModel.class);
                        }
                        if(fragment instanceof SessionDetailFragment){
                            new ViewModelProvider(fragment,new ViewModelProvider.Factory(){
                                @Override public <T extends ViewModel> T create(Class<T> type){return type.cast(new SessionDetailViewModel(a.getApplication(),fragment.requireArguments().getString("sessionId"),id->{assertEquals("one",id);return detail;}));}
                            }).get(SessionDetailViewModel.class);
                        }
                    }
                },false);
                host.getNavController().navigate(R.id.nav_social);
                host.getChildFragmentManager().executePendingTransactions();
            }); idle();
            scenario.onActivity(a->{
                assertEquals("2",text(a,R.id.tvSessions)); assertEquals("30",text(a,R.id.tvReps));
                com.github.mikephil.charting.charts.BarChart chart=a.findViewById(R.id.chartReps);
                assertEquals(7,chart.getData().getEntryCount());
                assertEquals(30f,chart.getData().getDataSetByIndex(0).getEntryForIndex(6).getY(),0.01f);
                a.findViewById(R.id.btnMonth).performClick();
            });
            screenshot("history");
            scenario.recreate(); idle();
            scenario.onActivity(a->{
                assertTrue(((com.google.android.material.button.MaterialButton)a.findViewById(R.id.btnMonth)).isChecked());
                assertEquals(1,loads.get());
                response.setValue(Resource.error("offline")); a.findViewById(R.id.btnRefresh).performClick();
                assertEquals("offline",text(a,R.id.error)); assertEquals("2",text(a,R.id.tvSessions));
                response.setValue(Resource.success(fixtures)); a.findViewById(R.id.btnRefresh).performClick();
                a.findViewById(R.id.btnCalendar).performClick();
            }); idle();
            scenario.onActivity(a->{
                RecyclerView days=a.findViewById(R.id.rvDays);
                int pos=HistoryStats.calendar(java.time.YearMonth.now(HistoryStats.ZONE)).indexOf(today);
                days.findViewHolderForAdapterPosition(pos).itemView.performClick();
                assertEquals(2,((RecyclerView)a.findViewById(R.id.rvSessions)).getAdapter().getItemCount());
            }); idle(); screenshot("calendar");
            scenario.recreate(); idle();
            // FragmentLifecycleCallbacks belong to the old FragmentManager after recreation.
            scenario.onActivity(a -> host(a).getChildFragmentManager().registerFragmentLifecycleCallbacks(
                    new FragmentManager.FragmentLifecycleCallbacks() {
                        @Override public void onFragmentAttached(FragmentManager fm, Fragment fragment, Context context) {
                            if (fragment instanceof SessionDetailFragment) {
                                new ViewModelProvider(fragment, new ViewModelProvider.Factory() {
                                    @Override public <T extends ViewModel> T create(Class<T> type) {
                                        return type.cast(new SessionDetailViewModel(a.getApplication(),
                                                fragment.requireArguments().getString("sessionId"), id -> {
                                                    assertEquals("one", id); return detail;
                                                }));
                                    }
                                }).get(SessionDetailViewModel.class);
                            }
                        }
                    }, false));
            scenario.onActivity(a->{
                assertEquals(View.VISIBLE,a.findViewById(R.id.btnClearDay).getVisibility());
                RecyclerView list=a.findViewById(R.id.rvSessions);
                list.findViewHolderForAdapterPosition(0).itemView.performClick();
            }); idle(); screenshot("detail");
            scenario.onActivity(a->{
                assertEquals(R.id.sessionDetailFragment,host(a).getNavController().getCurrentDestination().getId());
                assertEquals("Push-up",text(a,R.id.tvExercise)); assertEquals("10",text(a,R.id.tvReps));
                assertTrue(text(a,R.id.tvTotals).contains("24")); assertTrue(a.findViewById(R.id.btnRedo).isEnabled());
                a.findViewById(R.id.btnRedo).performClick();
            }); idle();
            scenario.onActivity(a->{
                assertEquals("pushup",host(a).getNavController().getCurrentBackStackEntry().getArguments().getString("exerciseId"));
                host(a).getNavController().popBackStack();
            }); idle();
            scenario.onActivity(a->a.findViewById(R.id.btnBack).performClick()); idle();
            scenario.onActivity(a->{
                a.findViewById(R.id.btnNextMonth).performClick();
                assertEquals("0",text(a,R.id.tvMonthSessions));
                assertEquals(View.VISIBLE,a.findViewById(R.id.tvEmpty).getVisibility());
                a.findViewById(R.id.btnBack).performClick();
            }); idle();
            scenario.onActivity(a->{
                response.setValue(Resource.success(Collections.emptyList()));
                a.findViewById(R.id.btnRefresh).performClick();
                assertEquals(View.VISIBLE,a.findViewById(R.id.emptyState).getVisibility());
                assertEquals("0",text(a,R.id.tvReps));
            });
        }
    }
    private static void screenshot(String name) throws Exception {
        if(!"true".equals(InstrumentationRegistry.getArguments().getString("captureScreenshots"))) return;
        android.graphics.Bitmap bitmap=InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        java.io.File dir=app().getExternalFilesDir("history-test");
        try(java.io.FileOutputStream out=new java.io.FileOutputStream(new java.io.File(dir,name+".png"))) {
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out);
        } finally {bitmap.recycle();}
    }
}
