package com.aicoach.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.common.BaseFragment;
import com.aicoach.databinding.FragmentProfileBinding;

/**
 * Figma 43 Hồ sơ & Cài đặt.
 *
 * ĐÃ CÓ: giao diện + điều hướng giữa các màn.
 * B TỰ VIẾT (xem TODO): UserRepository (GET hồ sơ), tính BMI, BadgeRules cho rvBadges (item_badge_small), lưu switchVoice / switchAutoCheck vào SettingsPrefs, chọn giờ nhắc (MaterialTimePicker).
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

        // TODO(B): ViewModel đổ dữ liệu hồ sơ; rowReminder mở chọn giờ.
    }
}
