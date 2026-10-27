package com.aicoach.ui.auth;

import android.Manifest;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.data.local.SettingsPrefs;
import com.aicoach.databinding.FragmentNotificationPermissionBinding;
import com.aicoach.databinding.ViewNotifyRowBinding;

import java.util.Arrays;

/**
 * Figma 20 Bật thông báo nhắc tập — bước cuối của luồng đăng ký, xong vào Trang chủ.
 * Android 13+ cần xin quyền POST_NOTIFICATIONS lúc chạy; lựa chọn từng loại lưu trong SettingsPrefs.
 */
public class NotificationPermissionFragment extends BaseFragment<FragmentNotificationPermissionBinding> {

    private SettingsPrefs prefs;

    private final ActivityResultLauncher<String> requestPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> finish(granted));

    @Override
    protected FragmentNotificationPermissionBinding inflate(@NonNull LayoutInflater inflater,
                                                            @Nullable ViewGroup container) {
        return FragmentNotificationPermissionBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = new SettingsPrefs(requireContext());

        String days = TextUtils.join(", ", Arrays.asList(prefs.getReminderDays().split(",")));
        bindRow(binding.rowReminder, R.drawable.ic_schedule_24, R.color.brand_primary, R.color.brand_primary_light,
                getString(R.string.notify_reminder),
                getString(R.string.notify_reminder_desc, prefs.getReminderTime(), days), true);
        bindRow(binding.rowSocial, R.drawable.ic_trophy_24, R.color.navy_700, R.color.brand_primary_light,
                getString(R.string.notify_social), getString(R.string.notify_social_desc),
                prefs.isSocialNotifyEnabled());
        bindRow(binding.rowWeekly, R.drawable.ic_chart_24, R.color.state_success, R.color.state_success_light,
                getString(R.string.notify_weekly), getString(R.string.notify_weekly_desc),
                prefs.isWeeklyReportEnabled());

        binding.btnEnable.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                finish(true);
            }
        });
        binding.btnSkip.setOnClickListener(v -> finish(false));
    }

    /** Lưu lựa chọn rồi vào Trang chủ (global action xoá luồng đăng nhập khỏi back stack). */
    private void finish(boolean enabled) {
        if (binding != null) {
            prefs.setReminderEnabled(enabled && binding.rowReminder.switchOn.isChecked());
            prefs.setSocialNotifyEnabled(enabled && binding.rowSocial.switchOn.isChecked());
            prefs.setWeeklyReportEnabled(enabled && binding.rowWeekly.switchOn.isChecked());
        }
        NavHostFragment.findNavController(this).navigate(R.id.action_global_to_main);
    }

    private void bindRow(@NonNull ViewNotifyRowBinding row, @DrawableRes int icon, @ColorRes int tint,
                         @ColorRes int bg, @NonNull String title, @NonNull String desc, boolean on) {
        row.imgIcon.setImageResource(icon);
        row.imgIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), tint)));
        row.iconBox.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), bg)));
        row.tvTitle.setText(title);
        row.tvDesc.setText(desc);
        row.switchOn.setChecked(on);
    }
}
