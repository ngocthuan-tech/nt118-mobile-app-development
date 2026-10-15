package com.aicoach.ui.workout;

import android.content.res.ColorStateList;
import android.os.Bundle;
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
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.databinding.FragmentReadinessBinding;
import com.aicoach.databinding.ViewCheckRowBinding;

/**
 * Figma 27 + 28 "Kiểm tra sẵn sàng" (Trưởng nhóm).
 * Hiện dùng dữ liệu mẫu: bấm nút làm mới để xem 2 trạng thái.
 * Tuần 3–4: camera + ML Kit sẽ tự gọi render(...) theo kết quả thật.
 */
public class ReadinessFragment extends BaseFragment<FragmentReadinessBinding> {

    private String exerciseId;
    private boolean demoReady = true;

    @Override
    protected FragmentReadinessBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentReadinessBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        exerciseId = ReadinessFragmentArgs.fromBundle(requireArguments()).getExerciseId();

        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.btnRefresh.setOnClickListener(v -> {
            demoReady = !demoReady;
            renderDemo();
        });
        binding.btnStart.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(ReadinessFragmentDirections.actionReadinessToCountdown(exerciseId)));

        renderDemo();
    }

    private void renderDemo() {
        if (demoReady) {
            render(CheckStatus.OK, CheckStatus.WARNING, CheckStatus.OK);     // Figma 28
        } else {
            render(CheckStatus.ERROR, CheckStatus.OK, CheckStatus.PENDING);  // Figma 27
        }
    }

    /** Vẽ lại toàn màn theo 3 mục kiểm tra. */
    public void render(CheckStatus frame, CheckStatus light, CheckStatus orientation) {
        bindRow(binding.rowFrame, R.drawable.ic_frame_24, R.string.workout_check_frame, frame,
                frame == CheckStatus.OK ? R.string.workout_check_frame_ok : R.string.workout_check_frame_bad);
        bindRow(binding.rowLight, R.drawable.ic_sun_24, R.string.workout_check_light, light,
                light == CheckStatus.OK ? R.string.workout_check_light_ok : R.string.workout_check_light_low);
        bindRow(binding.rowOrientation, R.drawable.ic_standing_24, R.string.workout_check_orientation, orientation,
                orientation == CheckStatus.OK ? R.string.workout_check_orientation_ok
                        : R.string.workout_check_orientation_wait);

        boolean personVisible = frame != CheckStatus.ERROR;
        binding.cardStepBack.setVisibility(personVisible ? View.GONE : View.VISIBLE);
        binding.imgBodyGuide.setAlpha(personVisible ? 0.85f : 0.25f);
        tintFrame(personVisible ? R.color.accent_cyan : R.color.state_warning);

        binding.chipStatus.setText(personVisible
                ? R.string.workout_readiness_in_frame : R.string.workout_readiness_no_person);
        binding.chipStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
                personVisible ? R.drawable.ic_frame_24 : R.drawable.ic_alert_24, 0, 0, 0);
        TextViewCompat.setCompoundDrawableTintList(binding.chipStatus,
                ColorStateList.valueOf(color(R.color.white)));
        binding.chipStatus.setBackgroundTintList(personVisible ? null
                : ColorStateList.valueOf(color(R.color.state_warning)));

        boolean canStart = frame.isPassing() && light.isPassing() && orientation.isPassing();
        binding.btnStart.setEnabled(canStart);
        binding.btnStart.setAlpha(canStart ? 1f : 0.35f);
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

    private void tintFrame(@ColorRes int c) {
        binding.bodyFrame.setBackgroundTintList(ColorStateList.valueOf(color(c)));
    }

    private int color(@ColorRes int res) {
        return ContextCompat.getColor(requireContext(), res);
    }
}
