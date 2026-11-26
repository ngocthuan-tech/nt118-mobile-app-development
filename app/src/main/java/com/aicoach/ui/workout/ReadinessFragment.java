package com.aicoach.ui.workout;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.databinding.FragmentReadinessBinding;
import com.aicoach.databinding.ViewCheckRowBinding;
import com.aicoach.pose.ExerciseRule;
import com.aicoach.pose.ExerciseRules;
import com.aicoach.pose.PoseCamera;
import com.aicoach.pose.PoseFrame;
import com.aicoach.pose.ReadinessEvaluator;

/**
 * Figma 27 + 28 "Kiểm tra sẵn sàng" (Trưởng nhóm) — dùng camera + ML Kit thật.
 * Mỗi khung hình chấm 3 mục (ReadinessEvaluator): khung hình, ánh sáng, hướng đứng.
 * Đạt liên tục 2 giây thì TỰ sang đếm ngược — người tập đang đứng xa máy không cần quay lại bấm nút.
 */
public class ReadinessFragment extends BaseFragment<FragmentReadinessBinding> {

    private static final long AUTO_START_MS = 2000;
    private static final long VOICE_HINT_GAP_MS = 6000;

    private String exerciseId;
    private ExerciseRule rule;
    private WorkoutSessionViewModel sessionVm;
    private PoseCamera camera;
    private VoiceCoach voice;
    private long readySinceMs = -1;
    private long lastHintMs;
    private boolean leaving;

    @Override
    protected FragmentReadinessBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentReadinessBinding.inflate(inflater, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        voice = new VoiceCoach(requireContext(), null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        exerciseId = ReadinessFragmentArgs.fromBundle(requireArguments()).getExerciseId();
        sessionVm = WorkoutSessionViewModel.of(this);
        WorkoutQueue.Step step = sessionVm.stepFor(exerciseId);
        rule = step != null ? ExerciseRules.create(exerciseId, step.sets, step.target) : ExerciseRules.create(exerciseId);
        leaving = false;
        readySinceMs = -1;
        if (sessionVm.isMultiExercise()) {
            // Buổi giáo án: báo đang ở bài mấy + cách đứng của bài này (mỗi bài có thể cần hướng khác)
            voice.say("Bài " + sessionVm.position() + " trên " + sessionVm.size() + ": " + rule.name
                    + ". " + rule.setupHint, true);
            lastHintMs = SystemClock.elapsedRealtime();
        }

        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.btnRefresh.setOnClickListener(v -> {
            readySinceMs = -1;
            lastHintMs = 0;
        });
        binding.btnStart.setOnClickListener(v -> startWorkout());

        // Khung nét đứt: bỏ backgroundTint trong XML (tint làm cả khung bị tô kín màu xanh),
        // đổi màu viền trực tiếp trên drawable.
        binding.bodyFrame.setBackgroundTintList(null);
        render(CheckStatus.ERROR, CheckStatus.PENDING, CheckStatus.PENDING, R.string.workout_check_orientation_wait, false);

        camera = new PoseCamera(requireContext());
        camera.start(getViewLifecycleOwner(), binding.previewView, this::onPoseFrame);
    }

    private void onPoseFrame(@NonNull PoseFrame frame) {
        if (binding == null || leaving) return;
        ReadinessEvaluator.Result r = ReadinessEvaluator.evaluate(frame, rule);

        CheckStatus frameStatus = map(r.frame);
        CheckStatus light = map(r.light);
        CheckStatus orientation = map(r.orientation);
        render(frameStatus, light, orientation, orientationText(r.orientation), r.canStart());

        long now = SystemClock.elapsedRealtime();
        if (r.canStart()) {
            if (readySinceMs < 0) {
                readySinceMs = now;
                voice.say("Tốt lắm, giữ nguyên", true);
            } else if (now - readySinceMs >= AUTO_START_MS) {
                startWorkout();
            }
        } else {
            readySinceMs = -1;
            speakHint(r, now);
        }
    }

    /** Nhắc bằng giọng nói, cách nhau vài giây cho đỡ phiền. */
    private void speakHint(ReadinessEvaluator.Result r, long now) {
        if (now - lastHintMs < VOICE_HINT_GAP_MS) return;
        String hint;
        if (r.frame != ReadinessEvaluator.Level.OK) {
            hint = "Lùi ra xa để camera thấy toàn thân";
        } else if (r.light == ReadinessEvaluator.Level.ERROR) {
            hint = "Phòng hơi tối, hãy bật thêm đèn";
        } else {
            return;
        }
        lastHintMs = now;
        voice.say(hint, true);
    }

    private void startWorkout() {
        if (leaving || binding == null) return;
        NavDestination current = NavHostFragment.findNavController(this).getCurrentDestination();
        if (current == null || current.getId() != R.id.readinessFragment) return;
        leaving = true;
        NavHostFragment.findNavController(this)
                .navigate(ReadinessFragmentDirections.actionReadinessToCountdown(exerciseId));
    }

    // ------------------------------------------------------------------ vẽ

    /** Vẽ lại toàn màn theo 3 mục kiểm tra. */
    private void render(CheckStatus frame, CheckStatus light, CheckStatus orientation,
                        @StringRes int orientationText, boolean ready) {
        bindRow(binding.rowFrame, R.drawable.ic_frame_24, R.string.workout_check_frame, frame,
                frame == CheckStatus.OK ? R.string.workout_check_frame_ok : R.string.workout_check_frame_bad);
        bindRow(binding.rowLight, R.drawable.ic_sun_24, R.string.workout_check_light, light,
                light == CheckStatus.OK || light == CheckStatus.PENDING
                        ? R.string.workout_check_light_ok : R.string.workout_check_light_low);
        bindRow(binding.rowOrientation, R.drawable.ic_standing_24, R.string.workout_check_orientation,
                orientation, orientationText);

        boolean personVisible = frame == CheckStatus.OK;
        binding.cardStepBack.setVisibility(personVisible ? View.GONE : View.VISIBLE);
        // Thấy người thì làm mờ hình minh hoạ để người tập thấy rõ chính mình trên camera
        binding.imgBodyGuide.setAlpha(personVisible ? 0.12f : 0.5f);
        strokeFrame(personVisible ? R.color.accent_cyan : R.color.state_warning);

        String status = getString(ready ? R.string.ai_readiness_auto : personVisible
                ? R.string.workout_readiness_in_frame : R.string.workout_readiness_no_person);
        binding.chipStatus.setText(sessionVm != null && sessionVm.isMultiExercise()
                ? getString(R.string.ai_exercise_position, sessionVm.position(), sessionVm.size(), rule.name)
                        + " · " + status
                : status);
        binding.chipStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
                personVisible ? R.drawable.ic_frame_24 : R.drawable.ic_alert_24, 0, 0, 0);
        TextViewCompat.setCompoundDrawableTintList(binding.chipStatus,
                ColorStateList.valueOf(color(R.color.white)));
        binding.chipStatus.setBackgroundTintList(ColorStateList.valueOf(color(
                ready ? R.color.state_success : personVisible ? R.color.white_10 : R.color.state_warning)));

        boolean canStart = frame.isPassing() && light.isPassing() && orientation.isPassing();
        binding.btnStart.setEnabled(canStart);
        binding.btnStart.setAlpha(canStart ? 1f : 0.35f);
    }

    @StringRes
    private int orientationText(ReadinessEvaluator.Level level) {
        if (level == ReadinessEvaluator.Level.PENDING) return R.string.workout_check_orientation_wait;
        boolean ok = level == ReadinessEvaluator.Level.OK;
        switch (rule.preferredView()) {
            case FRONT:
                return ok ? R.string.ai_orient_front_ok : R.string.ai_orient_front_warn;
            case SIDE:
                return ok ? R.string.ai_orient_side_ok : R.string.ai_orient_side_warn;
            case ANY:
            default:
                return R.string.ai_orient_any_ok;
        }
    }

    private static CheckStatus map(ReadinessEvaluator.Level level) {
        switch (level) {
            case OK:
                return CheckStatus.OK;
            case WARNING:
                return CheckStatus.WARNING;
            case ERROR:
                return CheckStatus.ERROR;
            case PENDING:
            default:
                return CheckStatus.PENDING;
        }
    }

    private void bindRow(ViewCheckRowBinding row, @DrawableRes int icon, @StringRes int title,
                         CheckStatus status, @StringRes int subtitle) {
        row.imgIcon.setImageResource(icon);
        row.tvTitle.setText(title);
        row.tvSubtitle.setText(subtitle);

        @ColorRes int accent = status == CheckStatus.PENDING ? R.color.text_on_dark_secondary : status.color;
        row.imgIcon.setImageTintList(ColorStateList.valueOf(color(accent)));
        row.tvSubtitle.setTextColor(color(status == CheckStatus.OK || status == CheckStatus.PENDING
                ? R.color.text_on_dark_secondary : status.color));

        if (status == CheckStatus.PENDING) {
            row.imgStatus.setImageDrawable(null);
            row.imgStatus.setBackgroundResource(R.drawable.bg_circle_outline);
            row.imgStatus.setBackgroundTintList(null);
        } else {
            row.imgStatus.setImageResource(status.icon);
            row.imgStatus.setBackgroundResource(R.drawable.bg_circle);
            row.imgStatus.setBackgroundTintList(ColorStateList.valueOf(color(status.color)));
        }
    }

    /** Đổi màu viền nét đứt, giữ nền trong suốt để nhìn xuyên thấy camera. */
    private void strokeFrame(@ColorRes int c) {
        Drawable bg = binding.bodyFrame.getBackground();
        if (bg instanceof GradientDrawable) {
            float d = getResources().getDisplayMetrics().density;
            GradientDrawable frame = (GradientDrawable) bg.mutate();
            frame.setColor(Color.TRANSPARENT);
            frame.setStroke(Math.round(2 * d), color(c), 10 * d, 6 * d);
        }
        binding.imgBodyGuide.setImageTintList(ColorStateList.valueOf(color(c)));
    }

    private int color(@ColorRes int res) {
        return ContextCompat.getColor(requireContext(), res);
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
