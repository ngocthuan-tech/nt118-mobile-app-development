package com.aicoach.ui.home;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.NavRootDirections;
import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.common.ExerciseImages;
import com.aicoach.databinding.FragmentHomeBinding;

import java.util.Arrays;

/**
 * Trang chủ — giao diện dựng theo Figma 08.
 * TODO (Thành viên A): thay bindDemoData() bằng HomeViewModel → HomeRepository → HomeApi (GET /home).
 */
public class HomeFragment extends BaseFragment<FragmentHomeBinding> {

    private ExerciseCardAdapter exerciseAdapter;

    @Override
    protected FragmentHomeBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentHomeBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        exerciseAdapter = new ExerciseCardAdapter(item -> openWorkout(item.exerciseId));
        binding.rvTodayExercises.setAdapter(exerciseAdapter);

        binding.btnStartWorkout.setOnClickListener(v -> openWorkout("squat"));
        binding.tvAvatar.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_global_to_profile));
        binding.btnNotifications.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_global_to_notifications));

        bindDemoData();
    }

    /** HỢP ĐỒNG điều hướng: mọi nút "Bắt đầu tập" đều đi qua action_global_to_readiness. */
    private void openWorkout(@NonNull String exerciseId) {
        NavHostFragment.findNavController(this)
                .navigate(NavRootDirections.actionGlobalToReadiness(exerciseId));
    }

    /** Dữ liệu mẫu giống Figma để xem giao diện. Xoá khi đã nối API. */
    private void bindDemoData() {
        binding.tvGreeting.setText(getString(R.string.home_greeting, "Minh"));
        binding.tvAvatar.setText("M");
        binding.tvStreakTitle.setText(getString(R.string.home_streak_title, 7));
        binding.tvStreakSub.setText(getString(R.string.home_streak_sub, 1));
        markDaysDone(5);

        binding.tvMinutesValue.setText("18");
        binding.tvKcalValue.setText("320");

        binding.tvPlanTitle.setText("Đốt mỡ toàn thân – 4 tuần");
        binding.tvPlanMeta.setText("Tuần 2 • Ngày 5/28 • 25 phút/buổi");
        binding.progressPlan.setProgress(18);
        binding.tvPlanPercent.setText("18%");
        ExerciseImages.bind(binding.imgPlan, "jumping_jack");

        binding.tvTodaySummary.setText(getString(R.string.home_today_summary, 3, 18));
        exerciseAdapter.submitList(Arrays.asList(
                new ExerciseCardItem("squat", "Squat", "3 × 15 lần"),
                new ExerciseCardItem("pushup", "Hít đất", "3 × 12 lần"),
                new ExerciseCardItem("plank", "Plank", "3 × 45 giây")));
    }

    /** Chip ngày trong tuần (T2 → CN): đã tập = chữ cyan, hôm nay = viền cyan, chưa tới = mờ. */
    private void markDaysDone(int doneCount) {
        TextView[] days = {binding.dayMon, binding.dayTue, binding.dayWed, binding.dayThu,
                binding.dayFri, binding.daySat, binding.daySun};
        int cyan = ContextCompat.getColor(requireContext(), R.color.accent_cyan);
        int doneBg = ContextCompat.getColor(requireContext(), R.color.day_done_bg);
        int dimBg = ContextCompat.getColor(requireContext(), R.color.white_10);
        int dimText = ContextCompat.getColor(requireContext(), R.color.text_on_dark_secondary);
        for (int i = 0; i < days.length; i++) {
            if (i < doneCount) {
                days[i].setBackgroundResource(R.drawable.bg_circle);
                days[i].setBackgroundTintList(ColorStateList.valueOf(doneBg));
                days[i].setTextColor(cyan);
            } else if (i == doneCount) {
                days[i].setBackgroundResource(R.drawable.bg_day_today);
                days[i].setBackgroundTintList(null);
                days[i].setTextColor(cyan);
            } else {
                days[i].setBackgroundResource(R.drawable.bg_circle);
                days[i].setBackgroundTintList(ColorStateList.valueOf(dimBg));
                days[i].setTextColor(dimText);
            }
        }
    }
}
