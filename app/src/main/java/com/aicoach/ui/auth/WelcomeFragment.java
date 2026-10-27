package com.aicoach.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.databinding.FragmentWelcomeBinding;
import com.aicoach.databinding.ViewWelcomeFeatureBinding;

/** Figma 05 Chào mừng: "Đăng nhập" → 06, "Tạo tài khoản mới" → 12. */
public class WelcomeFragment extends BaseFragment<FragmentWelcomeBinding> {

    @Override
    protected FragmentWelcomeBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentWelcomeBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindFeature(binding.rowCount, R.drawable.ic_camera_24, R.string.welcome_feature_count);
        bindFeature(binding.rowForm, R.drawable.ic_alert_24, R.string.welcome_feature_form);
        bindFeature(binding.rowVoice, R.drawable.ic_volume_24, R.string.welcome_feature_voice);

        binding.btnLogin.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_welcome_to_login));
        binding.btnRegister.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_welcome_to_register));
    }

    private static void bindFeature(@NonNull ViewWelcomeFeatureBinding row, @DrawableRes int icon,
                                    @StringRes int text) {
        row.imgIcon.setImageResource(icon);
        row.tvText.setText(text);
    }
}
