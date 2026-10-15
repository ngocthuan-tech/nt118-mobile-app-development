package com.aicoach.ui.workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.databinding.FragmentLiveWorkoutBinding;

/**
 * Tập với AI Camera (Figma 12) — Trưởng nhóm.
 * Hiện mới có giao diện + dữ liệu mẫu. Các bước tiếp theo:
 *  Tuần 3: CameraX hiển thị camera trước vào previewView
 *  Tuần 4: ML Kit Pose Detection + vẽ poseOverlay
 *  Tuần 5–6: đếm rep, chấm điểm tư thế, nhắc giọng nói
 */
public class LiveWorkoutFragment extends BaseFragment<FragmentLiveWorkoutBinding> {

    @Override
    protected FragmentLiveWorkoutBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentLiveWorkoutBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController nav = NavHostFragment.findNavController(this);

        binding.btnClose.setOnClickListener(v -> nav.popBackStack(R.id.nav_workout, true));
        binding.btnFinish.setOnClickListener(v -> nav.navigate(R.id.action_live_to_result));

        bindDemoData();
    }

    /** Dữ liệu mẫu giống Figma. Xoá khi đã có bộ đếm rep thật. */
    private void bindDemoData() {
        binding.chipExerciseSet.setText(getString(R.string.workout_exercise_set, "Squat", 2, 3));
        binding.tvTimer.setText("02:14");
        binding.tvRepCount.setText("12");
        binding.tvRepTarget.setText(getString(R.string.workout_rep_target, 15));
        binding.progressReps.setProgress(80);
        binding.tvPostureScore.setText(getString(R.string.workout_posture_score, 92));
        binding.chipJointAngle.setText(getString(R.string.workout_joint_angle, 94));
        binding.tvRepFeedback.setText(getString(R.string.workout_rep_feedback_good, 12));
        binding.tvVoicePrompt.setText("“Hạ hông thấp hơn một chút”");
    }
}
