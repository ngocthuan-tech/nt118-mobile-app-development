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

/** Figma 22 Thông báo — MÀN TẠM. Thành viên B thay bằng giao diện + dữ liệu mẫu. */
public class NotificationsFragment extends BaseFragment<FragmentPlaceholderBinding> {

    @Override
    protected FragmentPlaceholderBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentPlaceholderBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.tvTitle.setText(R.string.placeholder_notifications);
        binding.tvOwner.setText(R.string.placeholder_owner_b);
    }
}
