package com.aicoach.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.ui.history.SegmentGroup;
import com.aicoach.common.BaseFragment;
import com.aicoach.databinding.FragmentNotificationsBinding;

/**
 * Figma 22 Thông báo (mở từ chuông ở Trang chủ).
 *
 * ĐÃ CÓ: giao diện + điều hướng giữa các màn.
 * B TỰ VIẾT (xem TODO): NotificationAdapter cho rvNotifications (item_notification, nhóm theo ngày bằng tvSection), lọc Tất cả/Thử thách/Hệ thống, "Đọc tất cả", ẩn/hiện emptyState.
 */
public class NotificationsFragment extends BaseFragment<FragmentNotificationsBinding> {

    @Override
    protected FragmentNotificationsBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentNotificationsBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController nav = NavHostFragment.findNavController(this);
        binding.btnBack.setOnClickListener(v -> nav.popBackStack());
        // TODO(B): trong adapter, nút "Tham gia" của lời mời thử thách:
        //   nav.navigate(NotificationsFragmentDirections.actionNotificationsToChallenges());
        // Tất cả / Thử thách / Hệ thống. TODO(B): onSelect -> lọc danh sách.
        SegmentGroup.bind(0, null, binding.btnFilterAll, binding.btnFilterChallenges, binding.btnFilterSystem);
        // TODO(B): btnReadAll đánh dấu đã đọc tất cả.
    }
}
