package com.aicoach.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.common.BaseFragment;
import com.aicoach.databinding.FragmentMonthCalendarBinding;

/**
 * Figma 38 Lịch tháng.
 *
 * ĐÃ CÓ: giao diện + điều hướng giữa các màn.
 * B TỰ VIẾT (xem TODO): tạo lưới ngày cho rvDays (item_calendar_day, nền bg_progress_day_done/planned/today), đổi tháng bằng btnPrevMonth/btnNextMonth, tính tổng buổi/thời gian/kcal và chuỗi ngày.
 */
public class MonthCalendarFragment extends BaseFragment<FragmentMonthCalendarBinding> {

    @Override
    protected FragmentMonthCalendarBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentMonthCalendarBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController nav = NavHostFragment.findNavController(this);
        binding.btnBack.setOnClickListener(v -> nav.popBackStack());
        // Thẻ "Buổi gần nhất" -> 39. TODO(B): truyền sessionId thật của buổi gần nhất.
        binding.cardLatest.setOnClickListener(v ->
                nav.navigate(MonthCalendarFragmentDirections.actionCalendarToSessionDetail()));

        // TODO(B): CalendarDayAdapter cho rvDays + xử lý btnPrevMonth / btnNextMonth.
    }
}
