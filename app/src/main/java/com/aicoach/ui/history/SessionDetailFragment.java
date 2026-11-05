package com.aicoach.ui.history;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.aicoach.NavRootDirections;
import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.data.model.WorkoutSession;
import com.aicoach.databinding.FragmentSessionDetailBinding;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class SessionDetailFragment extends BaseFragment<FragmentSessionDetailBinding> {
    private SessionDetailViewModel vm;
    @Override protected FragmentSessionDetailBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentSessionDetailBinding.inflate(inflater, container, false);
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        vm = new ViewModelProvider(this).get(SessionDetailViewModel.class);
        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.requestState.btnRefresh.setOnClickListener(v -> vm.refresh());
        binding.btnRedo.setOnClickListener(v -> {
            WorkoutSession s = vm.session();
            if (s != null && s.getExerciseId() != null && !s.getExerciseId().trim().isEmpty())
                NavHostFragment.findNavController(this).navigate(NavRootDirections.actionGlobalToReadiness(s.getExerciseId()));
        });
        binding.btnShare.setOnClickListener(v -> {
            WorkoutSession s = vm.session();
            if (s == null) return;
            String text = getString(R.string.progress_share_text, HistoryUi.name(requireContext(), s),
                    HistoryUi.time(requireContext(), s), s.getTotalReps(), HistoryStats.score(s),
                    HistoryUi.duration(s.getDurationSec()), s.getCalories(), s.getXpEarned());
            startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND).setType("text/plain")
                    .putExtra(Intent.EXTRA_TEXT, text), getString(R.string.progress_share)));
        });
        vm.state().observe(getViewLifecycleOwner(), result -> {
            HistoryUi.request(binding.requestState, result);
            render(vm.session());
        });
    }
    private void render(WorkoutSession s) {
        binding.detailContent.setVisibility(s == null ? View.GONE : View.VISIBLE);
        binding.btnRedo.setEnabled(s != null && s.getExerciseId() != null && !s.getExerciseId().trim().isEmpty());
        binding.btnShare.setEnabled(s != null);
        if (s == null) return;
        binding.tvExercise.setText(HistoryUi.name(requireContext(), s));
        binding.tvDate.setText(HistoryUi.time(requireContext(), s));
        binding.tvScore.setText(String.valueOf(HistoryStats.score(s)));
        binding.ringScore.setProgress(HistoryStats.score(s));
        int color = HistoryStats.score(s) >= 85 ? R.color.state_success
                : HistoryStats.score(s) >= 70 ? R.color.state_warning : R.color.state_error;
        binding.ringScore.setIndicatorColor(androidx.core.content.ContextCompat.getColor(requireContext(), color));
        int track = HistoryStats.score(s) >= 85 ? R.color.state_success_light
                : HistoryStats.score(s) >= 70 ? R.color.state_warning_light : R.color.state_error_light;
        binding.ringScore.setTrackColor(androidx.core.content.ContextCompat.getColor(requireContext(), track));
        binding.tvReps.setText(String.valueOf(s.getTotalReps()));
        binding.tvDuration.setText(HistoryUi.duration(s.getDurationSec()));
        binding.tvKcal.setText(String.format(Locale.getDefault(), "%.1f", s.getCalories()));
        binding.tvTotals.setText(getString(R.string.progress_totals, s.getCorrectReps(),
                Math.max(0, s.getTotalReps() - s.getCorrectReps()), s.getTargetReps(), s.getXpEarned()));
        binding.groupErrors.removeAllViews();
        Map<String, Long> errors = new TreeMap<>();
        if (s.getFormErrors() != null) for (Map.Entry<String, Long> e : s.getFormErrors().entrySet())
            if (e.getKey() != null && e.getValue() != null && e.getValue() > 0) errors.put(e.getKey(), e.getValue());
        binding.tvErrorsTitle.setText(errors.isEmpty() ? R.string.progress_no_errors : R.string.progress_errors_title);
        for (Map.Entry<String, Long> error : errors.entrySet()) {
            TextView chip = (TextView) getLayoutInflater().inflate(R.layout.view_session_error_chip, binding.groupErrors, false);
            String label;
            switch (error.getKey()) {
                case "knee_over_toe": label = getString(R.string.progress_error_knee); break;
                case "back_not_straight": label = getString(R.string.progress_error_back); break;
                default: label = error.getKey().replace('_', ' ');
            }
            chip.setText(getString(R.string.progress_error_count, label, error.getValue()));
            binding.groupErrors.addView(chip);
        }
    }
}
