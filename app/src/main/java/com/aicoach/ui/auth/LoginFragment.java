package com.aicoach.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.databinding.FragmentPlaceholderBinding;

/** Đăng nhập — MÀN TẠM. Thành viên A thay bằng giao diện + API đăng nhập thật. */
public class LoginFragment extends BaseFragment<FragmentPlaceholderBinding> {

    @Override
    protected FragmentPlaceholderBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentPlaceholderBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.tvTitle.setText(R.string.placeholder_login);
        binding.tvOwner.setText(R.string.placeholder_owner_a);
        binding.btnAction.setVisibility(View.VISIBLE);
        binding.btnAction.setText(R.string.placeholder_login_skip);
        binding.btnAction.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_global_to_main));
    }
}
