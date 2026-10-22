package com.aicoach.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.databinding.FragmentPlaceholderBinding;

/** Figma 43 Hồ sơ &amp; Cài đặt — MÀN TẠM. Thành viên B thay bằng giao diện + GET /users/me. */
public class ProfileFragment extends BaseFragment<FragmentPlaceholderBinding> {

    @Override
    protected FragmentPlaceholderBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentPlaceholderBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.tvTitle.setText(R.string.placeholder_profile);
        binding.tvOwner.setText(R.string.placeholder_owner_b);
    }
}
