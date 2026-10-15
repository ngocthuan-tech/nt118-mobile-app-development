package com.aicoach.ui.workout;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.databinding.FragmentLiveWorkoutBinding;

/**
 * Figma 30 "Tập với AI Camera" + 31 "Phát hiện sai tư thế" + 33 "Tạm dừng" + 34 "Xác nhận kết thúc".
 * Trưởng nhóm. Hiện mới có giao diện + dữ liệu mẫu. Các bước tiếp theo:
 *  - Tuần 3: CameraX hiển thị camera trước vào previewView
 *  - Tuần 4: ML Kit Pose Detection + vẽ poseOverlay
 *  - Tuần 5–6: đếm rep, chấm điểm tư thế, nhắc giọng nói → gọi showRepGood()/showFormError()
 *
 * Mẹo xem thử giao diện khi chưa có AI: chạm vào chip "Tư thế 92%" để đổi qua lại
 * trạng thái đúng/sai tư thế; chạm vào số rep lớn để sang màn Nghỉ giữa hiệp.
 */
public class LiveWorkoutFragment extends BaseFragment<FragmentLiveWorkoutBinding> {

    private String exerciseId;
    private boolean demoError = false;

    @Override
    protected FragmentLiveWorkoutBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentLiveWorkoutBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        exerciseId = LiveWorkoutFragmentArgs.fromBundle(requireArguments()).getExerciseId();
        NavController nav = NavHostFragment.findNavController(this);

        // Thanh điều khiển
        binding.btnPause.setOnClickListener(v -> showPause(true));
        binding.btnFinish.setOnClickListener(v -> showFinishConfirm(true));
        binding.btnClose.setOnClickListener(v -> showFinishConfirm(true));

        // Lớp phủ Tạm dừng (Figma 33)
        binding.pauseOverlay.btnResume.setOnClickListener(v -> showPause(false));
        binding.pauseOverlay.btnRedoSet.setOnClickListener(v -> showPause(false));
        binding.pauseOverlay.btnEndSession.setOnClickListener(v -> {
            showPause(false);
            showFinishConfirm(true);
        });

        // Lớp phủ Xác nhận kết thúc (Figma 34)
        binding.finishConfirm.btnKeepGoing.setOnClickListener(v -> showFinishConfirm(false));
        binding.finishConfirm.btnFinishSave.setOnClickListener(v ->
                nav.navigate(LiveWorkoutFragmentDirections.actionLiveToResult()));

        // Nút Back của hệ thống: hỏi xác nhận thay vì thoát luôn
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (binding.pauseOverlay.getRoot().getVisibility() == View.VISIBLE) {
                            showPause(false);
                        } else if (binding.finishConfirm.getRoot().getVisibility() == View.VISIBLE) {
                            showFinishConfirm(false);
                        } else {
                            showFinishConfirm(true);
                        }
                    }
                });

        // Chế độ xem thử
        binding.chipPostureScore.setOnClickListener(v -> {
            demoError = !demoError;
            if (demoError) {
                showFormError("gối chụm vào trong", "Gối chụm vào 14°", "“Đẩy hai gối ra ngoài!”");
            } else {
                showRepGood(12, "Góc gối 94°", "“Hạ hông thấp hơn một chút”");
            }
        });
        binding.tvRepCount.setOnClickListener(v ->
                nav.navigate(LiveWorkoutFragmentDirections.actionLiveToRest(exerciseId)));

        bindDemoData();
    }

    /** Dữ liệu mẫu giống Figma 30. Xoá khi đã có bộ đếm rep thật. */
    private void bindDemoData() {
        binding.chipExerciseSet.setText(getString(R.string.workout_exercise_set, "Squat", 2, 3));
        binding.tvTimer.setText("02:14");
        binding.tvRepCount.setText("12");
        binding.tvRepTarget.setText(getString(R.string.workout_rep_target, 15));
        binding.progressReps.setProgress(80);
        binding.tvPostureScore.setText(getString(R.string.workout_posture_score, 92));
        showRepGood(12, "Góc gối 94°", "“Hạ hông thấp hơn một chút”");

        binding.pauseOverlay.tvPauseMeta.setText(getString(R.string.workout_exercise_set, "Squat", 2, 3));
        binding.pauseOverlay.tvPauseReps.setText("12/15");
        binding.pauseOverlay.tvPauseTime.setText("02:14");
        binding.pauseOverlay.tvPauseScore.setText("92%");

        binding.finishConfirm.tvConfirmBody.setText(getString(R.string.workout_finish_confirm_body, 27, 45, 60));
        binding.finishConfirm.progressSession.setProgress(60);
        binding.finishConfirm.tvProgressCount.setText("27/45");
        binding.finishConfirm.tvProgressPercent.setText("60%");
    }

    /** Figma 30: rep vừa rồi đúng tư thế. */
    public void showRepGood(int repNumber, String jointAngle, String voiceText) {
        binding.viewErrorVignette.setVisibility(View.GONE);
        binding.chipNotCounted.setVisibility(View.GONE);

        binding.chipJointAngle.setText(jointAngle);
        tint(binding.chipJointAngle, R.color.state_success);

        binding.chipRepFeedback.setText(getString(R.string.workout_rep_feedback_good, repNumber));
        binding.chipRepFeedback.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check_24, 0, 0, 0);
        tint(binding.chipRepFeedback, R.color.state_success);

        VoicePrompt.show(binding.voicePrompt, VoicePrompt.Tone.COACH, voiceText);
    }

    /** Figma 31: phát hiện sai tư thế, rep không được tính. */
    public void showFormError(String errorName, String jointAngle, String voiceText) {
        binding.viewErrorVignette.setVisibility(View.VISIBLE);
        binding.chipNotCounted.setVisibility(View.VISIBLE);

        binding.chipJointAngle.setText(jointAngle);
        tint(binding.chipJointAngle, R.color.state_error);

        binding.chipRepFeedback.setText(getString(R.string.workout_form_error, errorName));
        binding.chipRepFeedback.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_alert_24, 0, 0, 0);
        tint(binding.chipRepFeedback, R.color.state_error);

        VoicePrompt.show(binding.voicePrompt, VoicePrompt.Tone.ERROR, voiceText);
    }

    private void showPause(boolean show) {
        binding.pauseOverlay.getRoot().setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showFinishConfirm(boolean show) {
        binding.finishConfirm.getRoot().setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void tint(android.widget.TextView chip, @ColorRes int color) {
        int c = ContextCompat.getColor(requireContext(), color);
        chip.setBackgroundTintList(ColorStateList.valueOf(c));
        TextViewCompat.setCompoundDrawableTintList(chip,
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white)));
    }
}
