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
import com.aicoach.databinding.FragmentBadgesBinding;

/**
 * Figma 46 Bộ sưu tập huy hiệu.
 *
 * ĐÃ CÓ: giao diện + điều hướng giữa các màn.
 * B TỰ VIẾT (xem TODO): BadgeRules (tính từ lịch sử tập, kèm unit test), BadgeAdapter cho rvBadges (item_badge), lọc Tất cả/Đã đạt/Chưa đạt, tvSummary + progressBadges + tvNextHint.
 */
public class BadgesFragment extends BaseFragment<FragmentBadgesBinding> {

    @Override
    protected FragmentBadgesBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentBadgesBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController nav = NavHostFragment.findNavController(this);
        binding.btnBack.setOnClickListener(v -> nav.popBackStack());
    }
}
