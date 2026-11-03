package com.aicoach.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.common.BaseFragment;
import com.aicoach.common.Resource;
import com.aicoach.data.model.User;
import com.aicoach.R;
import com.aicoach.databinding.FragmentProfileBinding;
import java.util.Locale;

/**
 * Figma 43 Hồ sơ & Cài đặt.
 *
 * GET hồ sơ qua ViewModel, dữ liệu thật và trạng thái tải/lỗi/thử lại.
 * TODO(B): BadgeRules, lưu các công tắc và chọn giờ nhắc.
 */
public class ProfileFragment extends BaseFragment<FragmentProfileBinding> {

    @Override
    protected FragmentProfileBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentProfileBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController nav = NavHostFragment.findNavController(this);
        binding.btnBack.setOnClickListener(v -> nav.popBackStack());
        binding.btnEdit.setOnClickListener(v -> nav.navigate(ProfileFragmentDirections.actionProfileToEdit()));
        binding.btnSettings.setOnClickListener(v -> nav.navigate(ProfileFragmentDirections.actionProfileToVoice()));
        binding.rowVoice.setOnClickListener(v -> nav.navigate(ProfileFragmentDirections.actionProfileToVoice()));
        binding.btnAllBadges.setOnClickListener(v -> nav.navigate(ProfileFragmentDirections.actionProfileToBadges()));
        binding.rowPassword.setOnClickListener(v -> nav.navigate(ProfileFragmentDirections.actionProfileToChangePassword()));
        binding.btnLogout.setOnClickListener(v -> nav.navigate(ProfileFragmentDirections.actionProfileToLogout()));

        ProfileViewModel vm = new ViewModelProvider(this).get(ProfileViewModel.class);
        getParentFragmentManager().setFragmentResultListener(EditProfileFragment.UPDATED, getViewLifecycleOwner(), (key, result) -> {
            vm.refresh();
            com.google.android.material.snackbar.Snackbar.make(binding.getRoot(), R.string.profile_edit_saved,
                    com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
        });
        renderUser(vm.getLast());
        binding.btnRefreshProfile.setOnClickListener(v -> vm.refresh());
        binding.btnAllBadges.setText(R.string.profile_view_badges);
        vm.getState().observe(getViewLifecycleOwner(), result -> {
            boolean loading = result != null && result.status == Resource.Status.LOADING;
            boolean error = result != null && result.status == Resource.Status.ERROR;
            binding.profileProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnRefreshProfile.setEnabled(!loading);
            binding.btnRefreshProfile.setText(error ? R.string.profile_retry : R.string.profile_refresh);
            binding.profileError.setVisibility(error ? View.VISIBLE : View.GONE);
            binding.profileError.setText(error ? result.message : null);
            renderUser(vm.getLast());
        });
    }

    private void renderUser(@Nullable User user) {
        String missing = getString(R.string.profile_missing);
        binding.tvAvatar.setText(user == null ? "?" : user.getInitial());
        binding.tvName.setText(user == null || user.getDisplayName() == null ? missing : user.getDisplayName());
        binding.tvEmail.setText(user == null || user.getEmail() == null ? missing : user.getEmail());
        binding.tvMeta.setText(user == null ? missing : getString(R.string.profile_meta,
                getString(goalLabel(user.getGoal())), getString(levelLabel(user.getLevel()))));
        binding.tvStreak.setText(user == null ? missing : String.valueOf(user.getStreak()));
        binding.tvTotalReps.setText(user == null ? missing : String.valueOf(user.getTotalReps()));
        binding.tvTotalXp.setText(user == null ? missing : String.valueOf(user.getTotalXp()));
        Double height = user == null ? null : user.getHeightCm();
        Double weight = user == null ? null : user.getWeightKg();
        binding.tvHeight.setText(ProfileNumbers.validHeight(height) ? getString(R.string.profile_height_value, height) : missing);
        binding.tvWeight.setText(ProfileNumbers.validWeight(weight) ? getString(R.string.profile_weight_value, weight) : missing);
        Double bmi = ProfileNumbers.bmi(height, weight);
        binding.tvBmi.setText(bmi == null ? missing : String.format(Locale.getDefault(), "%.1f", bmi));
        binding.tvBmiNote.setText(bmi == null ? R.string.profile_body_missing : R.string.profile_bmi);
    }

    private int goalLabel(String goal) {
        if ("lose_weight".equals(goal)) return R.string.profile_goal_lose;
        if ("build_muscle".equals(goal)) return R.string.profile_goal_build;
        if ("stay_fit".equals(goal)) return R.string.profile_goal_fit;
        return R.string.profile_unset;
    }

    private int levelLabel(String level) {
        if ("beginner".equals(level)) return R.string.profile_level_beginner;
        if ("intermediate".equals(level)) return R.string.profile_level_intermediate;
        if ("advanced".equals(level)) return R.string.profile_level_advanced;
        return R.string.profile_unset;
    }
}
