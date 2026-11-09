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
import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.data.model.WorkoutSession;
import com.aicoach.databinding.FragmentMonthCalendarBinding;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class MonthCalendarFragment extends BaseFragment<FragmentMonthCalendarBinding> {
    private HistoryViewModel vm;
    private CalendarDayAdapter calendar;
    private HistorySessionAdapter sessions;
    @Override protected FragmentMonthCalendarBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentMonthCalendarBinding.inflate(inflater, container, false);
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        NavController nav = NavHostFragment.findNavController(this);
        vm = new ViewModelProvider(nav.getViewModelStoreOwner(R.id.nav_social)).get(HistoryViewModel.class);
        calendar = new CalendarDayAdapter(day -> { vm.day(day.equals(vm.day()) ? null : day); render(); });
        sessions = new HistorySessionAdapter(s -> nav.navigate(MonthCalendarFragmentDirections.actionCalendarToSessionDetail()
                .setSessionId(s.getId()).setExerciseId(s.getExerciseId() == null ? "" : s.getExerciseId())));
        binding.rvDays.setAdapter(calendar);
        binding.rvSessions.setAdapter(sessions);
        binding.btnBack.setOnClickListener(v -> nav.popBackStack());
        binding.btnPrevMonth.setOnClickListener(v -> { vm.moveMonth(-1); render(); });
        binding.btnNextMonth.setOnClickListener(v -> { vm.moveMonth(1); render(); });
        binding.btnClearDay.setOnClickListener(v -> { vm.day(null); render(); });
        binding.requestState.btnRefresh.setOnClickListener(v -> vm.refresh());
        vm.state().observe(getViewLifecycleOwner(), result -> { HistoryUi.request(binding.requestState, result); render(); });
    }
    private void render() {
        YearMonth month = vm.month();
        ZoneId zone = HistoryStats.ZONE;
        List<WorkoutSession> all = HistoryStats.between(vm.sessions(), month.atDay(1), month.atEndOfMonth(), zone);
        LocalDate selected = vm.day();
        List<WorkoutSession> filtered = selected == null ? all : HistoryStats.between(all, selected, selected, zone);
        calendar.submit(month, selected, all);
        sessions.submit(filtered);
        binding.tvMonth.setText(getString(R.string.progress_month_label, month.getMonthValue(), month.getYear()));
        String missing = getString(R.string.progress_missing);
        binding.tvMonthSessions.setText(vm.hasData() ? String.valueOf(all.size()) : missing);
        binding.tvMonthTime.setText(vm.hasData() ? getString(R.string.progress_minutes, HistoryStats.duration(all) / 60) : missing);
        binding.tvMonthKcal.setText(vm.hasData() ? String.format(Locale.getDefault(), "%.1f", HistoryStats.calories(all)) : missing);
        binding.tvStreak.setText(vm.hasData() ? getString(R.string.progress_streak_label, HistoryStats.longestStreak(all, zone)) : missing);
        binding.tvStreakNote.setText(R.string.progress_streak_note);
        binding.tvSelection.setText(selected == null ? getString(R.string.progress_history_title)
                : getString(R.string.progress_day_label, selected.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        binding.btnClearDay.setVisibility(selected == null ? View.GONE : View.VISIBLE);
        binding.tvEmpty.setVisibility(vm.hasData() && filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }
    @Override public void onDestroyView() {
        binding.rvDays.setAdapter(null); binding.rvSessions.setAdapter(null);
        super.onDestroyView();
    }
}
