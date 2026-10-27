package com.aicoach.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.databinding.FragmentResetSuccessBinding;

/** Figma 11 Đổi mật khẩu thành công — nút và phím Back đều về màn Đăng nhập. */
public class ResetSuccessFragment extends BaseFragment<FragmentResetSuccessBinding> {

    @Override
    protected FragmentResetSuccessBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentResetSuccessBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnLoginNow.setOnClickListener(v -> backToLogin());
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        backToLogin();
                    }
                });

        if (savedInstanceState == null) {
            binding.successRing.setScaleX(0.6f);
            binding.successRing.setScaleY(0.6f);
            binding.successRing.animate().scaleX(1f).scaleY(1f).setDuration(450)
                    .setInterpolator(new OvershootInterpolator()).start();
        }
    }

    private void backToLogin() {
        NavHostFragment.findNavController(this).navigate(R.id.action_reset_success_to_login);
    }
}
