package com.aicoach.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.aicoach.NavRootDirections;
import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.data.model.WorkoutSession;
import com.aicoach.databinding.FragmentHistoryBinding;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class HistoryFragment extends BaseFragment<FragmentHistoryBinding> {
    private HistoryViewModel vm;
    private HistorySessionAdapter adapter;
    @Override protected FragmentHistoryBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentHistoryBinding.inflate(inflater, container, false);
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        NavController nav = NavHostFragment.findNavController(this);
        vm = new ViewModelProvider(nav.getViewModelStoreOwner(R.id.nav_social)).get(HistoryViewModel.class);
        adapter = new HistorySessionAdapter(s -> nav.navigate(HistoryFragmentDirections.actionHistoryToSessionDetail()
                .setSessionId(s.getId()).setExerciseId(s.getExerciseId() == null ? "" : s.getExerciseId())));
        binding.rvSessions.setAdapter(adapter);
        binding.btnCalendar.setOnClickListener(v -> nav.navigate(HistoryFragmentDirections.actionHistoryToCalendar()));
        binding.btnSeeAll.setOnClickListener(v -> nav.navigate(HistoryFragmentDirections.actionHistoryToCalendar()));
        binding.btnFirstWorkout.setOnClickListener(v -> nav.navigate(NavRootDirections.actionGlobalToReadiness("squat")));
        binding.requestState.btnRefresh.setOnClickListener(v -> vm.refresh());
        binding.tvTrend.setVisibility(View.GONE);
        SegmentGroup.bind(vm.range(), index -> { vm.range(index); render(); }, binding.btnWeek, binding.btnMonth, binding.btnYear);
        vm.state().observe(getViewLifecycleOwner(), result -> { HistoryUi.request(binding.requestState, result); render(); });
    }
    private void render() {
        LocalDate today = LocalDate.now();
        List<WorkoutSession> sessions = HistoryStats.between(vm.sessions(), HistoryStats.rangeStart(vm.range(), today), today, ZoneId.systemDefault());
        String missing = getString(R.string.progress_missing);
        binding.tvSessions.setText(vm.hasData() ? String.valueOf(sessions.size()) : missing);
        binding.tvReps.setText(vm.hasData() ? String.valueOf(HistoryStats.reps(sessions)) : missing);
        binding.tvAvgScore.setText(vm.hasData() && !sessions.isEmpty() ? String.valueOf(HistoryStats.averageScore(sessions)) : missing);
        adapter.submit(sessions);
        binding.emptyState.setVisibility(vm.hasData() && sessions.isEmpty() ? View.VISIBLE : View.GONE);
        int title = vm.range() == 2 ? R.string.progress_chart_year : vm.range() == 1 ? R.string.progress_chart_month : R.string.progress_chart_week;
        binding.tvChartTitle.setText(title);
        binding.chartReps.setVisibility(vm.hasData() ? View.VISIBLE : View.GONE);
        if (vm.hasData()) HistoryChart.render(binding.chartReps, sessions, vm.range(), today, getString(title));
    }
    @Override public void onDestroyView() {
        binding.rvSessions.setAdapter(null);
        super.onDestroyView();
    }
}
