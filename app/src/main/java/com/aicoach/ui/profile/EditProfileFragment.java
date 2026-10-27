package com.aicoach.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.R;

import com.aicoach.common.BaseFragment;
import com.aicoach.databinding.FragmentEditProfileBinding;

/**
 * Figma 44 Chỉnh sửa hồ sơ.
 *
 * ĐÃ CÓ: giao diện + điều hướng giữa các màn.
 * B TỰ VIẾT (xem TODO): đổ dữ liệu hiện tại, ArrayAdapter cho 5 ô chọn (ddBirthYear, ddGender, ddGoal, ddLevel, ddSessions), kiểm tra chiều cao/cân nặng, PUT hồ sơ rồi mới quay lại.
 */
public class EditProfileFragment extends BaseFragment<FragmentEditProfileBinding> {

    @Override
    protected FragmentEditProfileBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentEditProfileBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController nav = NavHostFragment.findNavController(this);
        binding.btnBack.setOnClickListener(v -> nav.popBackStack());
        // TODO(B): kiểm tra + PUT hồ sơ, thành công mới popBackStack().
        binding.btnSave.setOnClickListener(v -> nav.popBackStack());
        binding.btnSaveTop.setOnClickListener(v -> nav.popBackStack());
        // Backend chưa có API xoá tài khoản
        binding.btnDeleteAccount.setOnClickListener(v -> showError(getString(R.string.profile_delete_unavailable)));
    }
}
