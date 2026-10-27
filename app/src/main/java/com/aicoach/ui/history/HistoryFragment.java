package com.aicoach.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.NavRootDirections;

import com.aicoach.common.BaseFragment;
import com.aicoach.databinding.FragmentHistoryBinding;

/**
 * Figma 37 Lịch sử & Thống kê (tab "Tiến độ").
 *
 * ĐÃ CÓ: giao diện + điều hướng giữa các màn.
 * B TỰ VIẾT (xem TODO): HistoryViewModel + HistoryRepository (GET lịch sử), đổi Tuần/Tháng/Năm, vẽ BarChart chartReps, HistorySessionAdapter cho rvSessions (item_history_session), ẩn/hiện emptyState.
 */
public class HistoryFragment extends BaseFragment<FragmentHistoryBinding> {

    @Override
    protected FragmentHistoryBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentHistoryBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController nav = NavHostFragment.findNavController(this);
        binding.btnCalendar.setOnClickListener(v -> nav.navigate(HistoryFragmentDirections.actionHistoryToCalendar()));
        binding.btnSeeAll.setOnClickListener(v -> nav.navigate(HistoryFragmentDirections.actionHistoryToCalendar()));
        // Chưa có buổi nào: "Tập buổi đầu tiên" -> màn Sẵn sàng của Trưởng nhóm
        binding.btnFirstWorkout.setOnClickListener(v ->
                nav.navigate(NavRootDirections.actionGlobalToReadiness(DEFAULT_EXERCISE)));

        // TODO(B): ViewModel quan sát dữ liệu; nút Tuần/Tháng/Năm đổi khoảng thời gian.
        // TODO(B): adapter rvSessions — bấm 1 dòng:
        //   nav.navigate(HistoryFragmentDirections.actionHistoryToSessionDetail().setSessionId(id).setExerciseId(exerciseId));
    }

    private static final String DEFAULT_EXERCISE = "squat";
}
