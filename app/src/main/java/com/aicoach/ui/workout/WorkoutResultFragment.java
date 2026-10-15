package com.aicoach.ui.workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.databinding.FragmentWorkoutResultBinding;

import java.util.Arrays;

/**
 * Kết quả buổi tập (Figma 13) — Trưởng nhóm.
 * Về sau: nhận WorkoutSession từ màn tập, gọi SessionRepository.saveCompletedSession() của
 * Thành viên B, rồi hiển thị XP / streak / huy hiệu mới trả về.
 */
public class WorkoutResultFragment extends BaseFragment<FragmentWorkoutResultBinding> {

    private final AiFeedbackAdapter feedbackAdapter = new AiFeedbackAdapter();

    @Override
    protected FragmentWorkoutResultBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentWorkoutResultBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.rvAiFeedback.setAdapter(feedbackAdapter);

        View.OnClickListener backHome = v ->
                NavHostFragment.findNavController(this).popBackStack(R.id.nav_workout, true);
        binding.btnClose.setOnClickListener(backHome);
        binding.btnBackHome.setOnClickListener(backHome);
        binding.btnShare.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(WorkoutResultFragmentDirections.actionResultToShare()));

        bindDemoData();
    }

    /** Dữ liệu mẫu giống Figma. Xoá khi đã có WorkoutSession thật. */
    private void bindDemoData() {
        binding.tvSessionMeta.setText("Squat • 3 hiệp • Hôm nay 19:30");
        binding.progressScore.setProgress(92);
        binding.tvScore.setText("92");
        binding.tvScoreNote.setText("Xuất sắc! Tốt hơn 12% so với lần trước");

        setStat(binding.statReps.tvValue, binding.statReps.tvLabel, "45", R.string.result_stat_reps);
        setStat(binding.statTime.tvValue, binding.statTime.tvLabel, "08:24", R.string.result_stat_time);
        setStat(binding.statKcal.tvValue, binding.statKcal.tvLabel, "62", R.string.result_stat_kcal);
        setStat(binding.statAccuracy.tvValue, binding.statAccuracy.tvLabel, "89%", R.string.result_stat_accuracy);
        binding.statAccuracy.tvValue.setTextColor(
                androidx.core.content.ContextCompat.getColor(requireContext(), R.color.state_success));

        feedbackAdapter.submitList(Arrays.asList(
                new AiFeedbackItem(AiFeedbackItem.Type.WARNING,
                        "Gối hơi chụm vào trong", "Xuất hiện 4 lần • chủ yếu ở hiệp 3"),
                new AiFeedbackItem(AiFeedbackItem.Type.ERROR,
                        "Lưng cong khi xuống sâu", "2 lần • hiệp 2"),
                new AiFeedbackItem(AiFeedbackItem.Type.GOOD,
                        "Độ sâu squat đạt chuẩn", "39/45 lần đạt góc gối ≤ 90°")));

        binding.tvBadgeName.setText("Chiến binh 7 ngày");
    }

    private static void setStat(android.widget.TextView value, android.widget.TextView label,
                                String v, int labelRes) {
        value.setText(v);
        label.setText(labelRes);
    }
}
