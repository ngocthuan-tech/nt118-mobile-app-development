package com.aicoach.ui.workout;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.databinding.FragmentLiveWorkoutBinding;
import com.aicoach.pose.ExerciseRules;
import com.aicoach.pose.PoseCamera;
import com.aicoach.pose.PoseFrame;

import java.util.Locale;

/**
 * Figma 30 "Tập với AI Camera" + 31 "Phát hiện sai tư thế" + 33 "Tạm dừng" + 34 "Xác nhận kết thúc".
 * Trưởng nhóm.
 *
 *  PoseCamera (CameraX + ML Kit) ──PoseFrame──▶ LiveWorkoutViewModel (WorkoutEngine)
 *        ▲                                            │ State (vẽ UI) + Command (đọc / chuyển màn)
 *        └─────────────── Fragment ◀──────────────────┘
 *
 * Fragment chỉ lo camera, vẽ và điều hướng; toàn bộ logic đếm/chấm nằm trong ViewModel + WorkoutEngine.
 */
public class LiveWorkoutFragment extends BaseFragment<FragmentLiveWorkoutBinding> {

    /** Chờ một chút trước khi sang màn Nghỉ/Kết quả để người tập kịp thấy số cuối và nghe lời khen. */
    private static final long NAV_DELAY_MS = 1200;

    private String exerciseId;
    private LiveWorkoutViewModel vm;
    /** Dùng chung cả luồng tập: chuyển kết quả hiệp / buổi sang màn Nghỉ, Kết quả. */
    private WorkoutSessionViewModel sessionVm;
    private PoseCamera camera;
    private VoiceCoach voice;
    private boolean voiceWarningShown;

    @Override
    protected FragmentLiveWorkoutBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentLiveWorkoutBinding.inflate(inflater, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Giọng nói sống theo Fragment (không theo View) để câu "Nghỉ ngơi nhé" không bị cắt khi sang màn Nghỉ
        voice = new VoiceCoach(requireContext(), this::onVoiceUnavailable);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        exerciseId = LiveWorkoutFragmentArgs.fromBundle(requireArguments()).getExerciseId();
        vm = new ViewModelProvider(this).get(LiveWorkoutViewModel.class);
        sessionVm = WorkoutSessionViewModel.of(this);

        setupControls();
        vm.state().observe(getViewLifecycleOwner(), this::render);
        vm.command().observe(getViewLifecycleOwner(), this::handleCommand);
        // Khối lượng theo giáo án (VD Plank 3 × 40 giây) + bài kế tiếp để AI báo trước
        WorkoutQueue.Step step = sessionVm.stepFor(exerciseId);
        WorkoutQueue.Step next = sessionVm.peekNext();
        vm.onViewReady(exerciseId, step != null ? step.sets : 0, step != null ? step.target : 0,
                next != null ? ExerciseRules.create(next.exerciseId).name : null);

        camera = new PoseCamera(requireContext());
        camera.start(getViewLifecycleOwner(), binding.previewView, this::onPoseFrame);
    }

    private void onPoseFrame(@NonNull PoseFrame frame) {
        if (binding == null) return;   // khung đến muộn sau khi đã rời màn
        vm.onFrame(frame);
    }

    // ------------------------------------------------------------------ nút bấm

    private void setupControls() {
        NavController nav = NavHostFragment.findNavController(this);

        binding.btnPause.setOnClickListener(v -> showPause(true));
        binding.btnFinish.setOnClickListener(v -> showFinishConfirm(true));
        binding.btnClose.setOnClickListener(v -> showFinishConfirm(true));
        binding.btnSound.setOnClickListener(v -> toggleVoice());

        // Lớp phủ Tạm dừng (Figma 33)
        binding.pauseOverlay.btnResume.setOnClickListener(v -> showPause(false));
        binding.pauseOverlay.btnRedoSet.setOnClickListener(v -> {
            vm.redoSet();
            showPause(false);
        });
        binding.pauseOverlay.btnEndSession.setOnClickListener(v -> {
            binding.pauseOverlay.getRoot().setVisibility(View.GONE);
            showFinishConfirm(true);
        });
        binding.pauseOverlay.chipVoice.setOnClickListener(v -> toggleVoice());
        binding.pauseOverlay.chipSwitchCamera.setOnClickListener(v -> {
            if (camera != null) camera.switchCamera();
        });

        // Lớp phủ Xác nhận kết thúc (Figma 34)
        binding.finishConfirm.btnKeepGoing.setOnClickListener(v -> showFinishConfirm(false));
        binding.finishConfirm.btnFinishSave.setOnClickListener(v -> {
            voice.stop();
            endEarly();
        });

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
        renderVoiceButton();
    }

    private void showPause(boolean show) {
        binding.pauseOverlay.getRoot().setVisibility(show ? View.VISIBLE : View.GONE);
        vm.setPaused(show);
        if (show) voice.stop();
    }

    private void showFinishConfirm(boolean show) {
        binding.finishConfirm.getRoot().setVisibility(show ? View.VISIBLE : View.GONE);
        vm.setPaused(show);
        if (show) voice.stop();
    }

    private void toggleVoice() {
        voice.setMuted(!voice.isMuted());
        renderVoiceButton();
    }

    private void renderVoiceButton() {
        boolean on = !voice.isMuted();
        binding.btnSound.setAlpha(on ? 1f : 0.4f);
        binding.pauseOverlay.chipVoice.setText(on ? R.string.workout_voice_on : R.string.ai_voice_off);
    }

    // ------------------------------------------------------------------ lệnh từ ViewModel

    private void handleCommand(LiveWorkoutViewModel.Command c) {
        if (c == null || !c.consume()) return;
        switch (c.type) {
            case SPEAK:
                if (c.text != null) voice.say(c.text, c.urgent);
                break;
            case GO_REST:
                sessionVm.setLastSet(vm.lastSet());
                navigateLater(() -> NavHostFragment.findNavController(this)
                        .navigate(LiveWorkoutFragmentDirections.actionLiveToRest(exerciseId)));
                break;
            case GO_RESULT:
                navigateLater(this::finishExercise);
                break;
        }
    }

    /** Tập đủ hiệp của bài này: lưu bài → sang bài kế trong buổi giáo án, hoặc sang Kết quả nếu là bài cuối. */
    private void finishExercise() {
        if (!vm.closeExercise()) return;
        NavController nav = NavHostFragment.findNavController(this);
        WorkoutQueue.Step next = sessionVm.onExerciseFinished(vm.summary(), true);
        if (next != null) {
            nav.navigate(LiveWorkoutFragmentDirections.actionLiveToNextExercise(next.exerciseId));
        } else {
            nav.navigate(LiveWorkoutFragmentDirections.actionLiveToResult());
        }
    }

    /**
     * Bấm "Kết thúc": lưu bài đang tập nếu đã làm ít nhất 1 lần, dừng cả lượt.
     * Chưa tập gì (cả các bài trước) → thoát luôn, KHÔNG sang Kết quả, KHÔNG lưu buổi rỗng.
     */
    private void endEarly() {
        if (!vm.closeExercise()) return;
        NavController nav = NavHostFragment.findNavController(this);
        sessionVm.onExerciseFinished(vm.summary(), false);
        if (sessionVm.hasResults()) {
            nav.navigate(LiveWorkoutFragmentDirections.actionLiveToResult());
        } else {
            nav.popBackStack(R.id.nav_workout, true);
        }
    }

    /** Chuyển màn sau một nhịp, và chỉ khi vẫn đang đứng ở màn tập (tránh crash bấm 2 lần). */
    private void navigateLater(Runnable go) {
        binding.getRoot().postDelayed(() -> {
            if (binding == null || !isAdded()) return;
            NavDestination current = NavHostFragment.findNavController(this).getCurrentDestination();
            if (current != null && current.getId() == R.id.liveWorkoutFragment) go.run();
        }, NAV_DELAY_MS);
    }

    // ------------------------------------------------------------------ vẽ giao diện

    private void render(LiveWorkoutViewModel.State s) {
        if (s == null || binding == null) return;

        // Buổi giáo án nhiều bài: "Bài 2/4 · Lunge • Hiệp 1/3"
        String exerciseLabel = sessionVm.isMultiExercise()
                ? getString(R.string.ai_exercise_position, sessionVm.position(), sessionVm.size(), s.exerciseName)
                : s.exerciseName;
        binding.chipExerciseSet.setText(getString(R.string.workout_exercise_set, exerciseLabel, s.currentSet, s.sets));
        String time = formatTime(s.elapsedMs);
        binding.tvTimer.setText(time);
        binding.tvRepCount.setText(String.valueOf(s.progress));
        binding.tvRepTarget.setText(getString(R.string.workout_rep_target, s.target));
        binding.tvRepUnit.setText(s.timed ? R.string.ai_unit_seconds : R.string.workout_rep_unit);
        binding.progressReps.setProgress(percent(s.progress, s.target));
        binding.tvPostureScore.setText(getString(R.string.workout_posture_score, s.postureScore));
        binding.chipJointAngle.setText(s.metricText != null ? s.metricText : getString(R.string.ai_metric_waiting));

        switch (s.feedback) {
            case GOOD:
                showGood(s);
                break;
            case BAD:
                showFormError(s);
                break;
            case HINT:
            case NONE:
            default:
                showHint(s.coachText);
                break;
        }

        // Khung xương: vẽ khi ML Kit thấy người, kể cả khi chưa đủ khớp để chấm
        if (s.frame != null) {
            if (s.frame.hasPerson()) {
                binding.poseOverlay.setPose(s.frame, camera != null && camera.isFrontCamera(), s.warnJoints);
            } else {
                binding.poseOverlay.clear();
            }
        }

        // Lớp phủ Tạm dừng (Figma 33)
        String meta = getString(R.string.workout_exercise_set, s.exerciseName, s.currentSet, s.sets);
        binding.pauseOverlay.tvPauseMeta.setText(meta);
        binding.pauseOverlay.tvPauseReps.setText(getString(R.string.ai_progress_count, s.progress, s.target));
        binding.pauseOverlay.tvPauseTime.setText(time);
        binding.pauseOverlay.tvPauseScore.setText(String.format(Locale.US, "%d%%", s.postureScore));

        // Lớp phủ Xác nhận kết thúc (Figma 34)
        int done = percent(s.doneReps, s.totalTarget);
        boolean empty = s.attemptedReps == 0 && !sessionVm.hasResults();
        binding.finishConfirm.tvConfirmBody.setText(empty ? getString(R.string.ai_finish_empty)
                : getString(R.string.workout_finish_confirm_body, s.doneReps, s.totalTarget, done));
        binding.finishConfirm.btnFinishSave.setText(empty ? R.string.ai_exit : R.string.workout_finish_and_save);
        binding.finishConfirm.progressSession.setProgress(done);
        binding.finishConfirm.tvProgressCount.setText(getString(R.string.ai_progress_count, s.doneReps, s.totalTarget));
        binding.finishConfirm.tvProgressPercent.setText(String.format(Locale.US, "%d%%", done));
    }

    /** Figma 30: lần vừa rồi đúng tư thế. */
    private void showGood(LiveWorkoutViewModel.State s) {
        binding.viewErrorVignette.setVisibility(View.GONE);
        binding.chipNotCounted.setVisibility(View.GONE);
        tint(binding.chipJointAngle, R.color.state_success);

        binding.chipRepFeedback.setVisibility(View.VISIBLE);
        binding.chipRepFeedback.setText(s.timed ? s.coachText
                : getString(R.string.workout_rep_feedback_good, s.feedbackRep));
        binding.chipRepFeedback.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check_24, 0, 0, 0);
        tint(binding.chipRepFeedback, R.color.state_success);

        VoicePrompt.show(binding.voicePrompt, VoicePrompt.Tone.COACH, quote(s.coachText));
    }

    /** Figma 31: phát hiện sai tư thế, lần này không được tính. */
    private void showFormError(LiveWorkoutViewModel.State s) {
        binding.viewErrorVignette.setVisibility(View.VISIBLE);
        binding.chipNotCounted.setVisibility(s.timed ? View.GONE : View.VISIBLE);
        tint(binding.chipJointAngle, R.color.state_error);

        binding.chipRepFeedback.setVisibility(View.VISIBLE);
        String label = s.issue != null ? s.issue.label : "";
        binding.chipRepFeedback.setText(getString(R.string.workout_form_error, label));
        binding.chipRepFeedback.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_alert_24, 0, 0, 0);
        tint(binding.chipRepFeedback, R.color.state_error);

        VoicePrompt.show(binding.voicePrompt, VoicePrompt.Tone.ERROR, quote(s.coachText));
    }

    /** Gợi ý chung: chưa thấy người, bắt đầu hiệp, hướng dẫn đặt máy. */
    private void showHint(String text) {
        binding.viewErrorVignette.setVisibility(View.GONE);
        binding.chipNotCounted.setVisibility(View.GONE);
        binding.chipRepFeedback.setVisibility(View.INVISIBLE);
        tint(binding.chipJointAngle, R.color.accent_cyan);
        VoicePrompt.show(binding.voicePrompt, VoicePrompt.Tone.INFO, quote(text));
    }

    private void tint(TextView chip, @ColorRes int color) {
        int c = ContextCompat.getColor(requireContext(), color);
        chip.setBackgroundTintList(ColorStateList.valueOf(c));
        TextViewCompat.setCompoundDrawableTintList(chip,
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white)));
    }

    private static String quote(String text) {
        return text == null || text.isEmpty() ? "" : "“" + text + "”";
    }

    private static int percent(int part, int whole) {
        return whole <= 0 ? 0 : Math.min(100, part * 100 / whole);
    }

    private static String formatTime(long ms) {
        long sec = ms / 1000;
        return String.format(Locale.US, "%02d:%02d", sec / 60, sec % 60);
    }

    private void onVoiceUnavailable() {
        if (voiceWarningShown) return;
        voiceWarningShown = true;
        if (binding != null) {
            binding.getRoot().post(() -> showError(getString(R.string.ai_voice_unavailable)));
        }
    }

    // ------------------------------------------------------------------ vòng đời

    @Override
    public void onResume() {
        super.onResume();
        // Giữ màn hình sáng trong lúc tập
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onPause() {
        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        if (camera != null) {
            camera.close();
            camera = null;
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        voice.shutdown();
        super.onDestroy();
    }
}
