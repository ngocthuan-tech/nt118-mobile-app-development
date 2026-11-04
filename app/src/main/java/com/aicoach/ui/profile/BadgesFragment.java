package com.aicoach.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.common.Resource;
import com.aicoach.databinding.FragmentBadgesBinding;
import com.aicoach.domain.BadgeRules;
import com.aicoach.ui.history.SegmentGroup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BadgesFragment extends BaseFragment<FragmentBadgesBinding> {
    private ProfileViewModel vm;
    private BadgeAdapter adapter;
    private int filter;
    @Override protected FragmentBadgesBinding inflate(@NonNull LayoutInflater i, @Nullable ViewGroup c) {
        return FragmentBadgesBinding.inflate(i, c, false);
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        filter = state == null ? 0 : state.getInt("filter");
        vm = new ViewModelProvider(this).get(ProfileViewModel.class);
        adapter = new BadgeAdapter();
        binding.rvBadges.setAdapter(adapter);
        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        SegmentGroup.bind(filter, index -> { filter = index; render(); },
                binding.btnAll, binding.btnEarned, binding.btnLocked);
        binding.btnRetryBadges.setOnClickListener(v -> vm.refresh());
        vm.getState().observe(getViewLifecycleOwner(), result -> render());
    }
    private void render() {
        Resource<?> result = vm.getState().getValue();
        boolean loading = result != null && result.status == Resource.Status.LOADING;
        boolean error = result != null && result.status == Resource.Status.ERROR;
        binding.loadingBadges.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnRetryBadges.setEnabled(!loading);
        binding.btnRetryBadges.setText(error ? R.string.profile_retry : R.string.profile_refresh);
        binding.tvBadgeError.setVisibility(error ? View.VISIBLE : View.GONE);
        binding.tvBadgeError.setText(error ? result.message : null);
        if (vm.getLast() == null) {
            binding.tvSummary.setText(R.string.profile_missing);
            binding.tvNextHint.setText(loading ? R.string.profile_badge_loading : R.string.profile_badge_hint);
            adapter.submitList(Collections.emptyList());
            binding.tvBadgeEmpty.setVisibility(View.GONE);
            return;
        }
        List<BadgeRules.Badge> badges = BadgeRules.evaluate(vm.getLast().getTotalReps(), vm.getLast().getStreak());
        List<BadgeRules.Badge> shown = new ArrayList<>();
        int earned = 0;
        for (BadgeRules.Badge badge : badges) {
            if (badge.earned()) earned++;
            if (filter == 0 || (filter == 1) == badge.earned()) shown.add(badge);
        }
        binding.tvSummary.setText(getString(R.string.profile_badge_summary, earned, badges.size()));
        binding.progressBadges.setProgress(earned * 100 / badges.size());
        binding.tvNextHint.setText(R.string.profile_badge_hint);
        binding.tvBadgeEmpty.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.submitList(shown);
    }
    @Override public void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state); state.putInt("filter", filter);
    }
    @Override public void onDestroyView() {
        binding.rvBadges.setAdapter(null);
        super.onDestroyView();
    }
}
