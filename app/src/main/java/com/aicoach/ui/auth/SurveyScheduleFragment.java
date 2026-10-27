package com.aicoach.ui.auth;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.common.Resource;
import com.aicoach.databinding.FragmentSurveyScheduleBinding;
import com.aicoach.databinding.ViewSurveyLevelBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Locale;

/**
 * Figma 15 Khảo sát 3/3 — trình độ, ngày tập, thời lượng, giờ nhắc.
 * "Tạo giáo án với AI" → PUT users/me {level, weeklyGoalSessions} → 16.
 */
public class SurveyScheduleFragment extends BaseFragment<FragmentSurveyScheduleBinding> {

    private static final String TIME_PICKER_TAG = "reminder_time";

    private SurveyViewModel vm;

    @Override
    protected FragmentSurveyScheduleBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentSurveyScheduleBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vm = SurveyUi.viewModel(this);
        SurveyUi.bindHeader(this, binding.header, 3);

        setupLevel(binding.levelBeginner, SurveyViewModel.LEVEL_BEGINNER, R.drawable.ic_level_1_24,
                R.string.survey_level_beginner, R.string.survey_level_beginner_desc);
        setupLevel(binding.levelIntermediate, SurveyViewModel.LEVEL_INTERMEDIATE, R.drawable.ic_level_2_24,
                R.string.survey_level_intermediate, R.string.survey_level_intermediate_desc);
        setupLevel(binding.levelAdvanced, SurveyViewModel.LEVEL_ADVANCED, R.drawable.ic_level_3_24,
                R.string.survey_level_advanced, R.string.survey_level_advanced_desc);

        TextView[] dayViews = days();
        for (int i = 0; i < dayViews.length; i++) {
            final int index = i;
            dayViews[i].setOnClickListener(v -> {
                if (!vm.toggleDay(index)) showError(getString(R.string.survey_days_min));
                render();
            });
        }

        MaterialButton[] durations = durationButtons();
        for (int i = 0; i < durations.length; i++) {
            final int minutes = SurveyViewModel.DURATIONS[i];
            durations[i].setText(getString(R.string.survey_duration_value, minutes));
            durations[i].setOnClickListener(v -> {
                vm.setDurationMinutes(minutes);
                render();
            });
        }

        binding.switchReminder.setChecked(vm.isReminderOn());
        binding.switchReminder.setOnCheckedChangeListener((b, checked) -> vm.setReminderOn(checked));
        binding.rowReminderTime.setOnClickListener(v -> pickTime());
        restoreTimePickerListener();

        binding.footer.btnContinue.setText(R.string.survey_create_plan);
        binding.footer.btnContinue.setIconResource(R.drawable.ic_sparkle_24);
        binding.footer.btnContinue.setOnClickListener(v -> vm.saveSchedule());
        vm.getSaveState().observe(getViewLifecycleOwner(), r -> {
            SurveyUi.renderFooter(binding.footer, r);
            if (r == null) return;
            if (r.status == Resource.Status.ERROR) {
                showError(r.message);
                vm.consumeSave();
            } else if (r.status == Resource.Status.SUCCESS) {
                vm.consumeSave();
                NavHostFragment.findNavController(this).navigate(R.id.action_survey_schedule_to_generating);
            }
        });
        render();
    }

    private TextView[] days() {
        return new TextView[]{binding.dayMon, binding.dayTue, binding.dayWed, binding.dayThu,
                binding.dayFri, binding.daySat, binding.daySun};
    }

    private MaterialButton[] durationButtons() {
        return new MaterialButton[]{binding.btnDuration15, binding.btnDuration25, binding.btnDuration40};
    }

    private void setupLevel(@NonNull ViewSurveyLevelBinding card, @NonNull String level, @DrawableRes int icon,
                            @StringRes int title, @StringRes int desc) {
        card.imgIcon.setImageResource(icon);
        card.tvTitle.setText(title);
        card.tvDesc.setText(desc);
        card.getRoot().setTag(level);
        card.getRoot().setOnClickListener(v -> {
            vm.setLevel(level);
            render();
        });
    }

    private void render() {
        int blue = ContextCompat.getColor(requireContext(), R.color.brand_primary);
        int grey = ContextCompat.getColor(requireContext(), R.color.text_secondary);
        for (ViewSurveyLevelBinding card : new ViewSurveyLevelBinding[]{
                binding.levelBeginner, binding.levelIntermediate, binding.levelAdvanced}) {
            boolean selected = vm.getLevel().equals(card.getRoot().getTag());
            card.getRoot().setSelected(selected);
            card.imgIcon.setImageTintList(ColorStateList.valueOf(selected ? blue : grey));
        }

        TextView[] dayViews = days();
        for (int i = 0; i < dayViews.length; i++) dayViews[i].setSelected(vm.isDaySelected(i));
        binding.tvDaysCount.setText(getString(R.string.survey_days_count, vm.sessionsPerWeek()));

        MaterialButton[] durations = durationButtons();
        for (int i = 0; i < durations.length; i++) {
            durations[i].setChecked(SurveyViewModel.DURATIONS[i] == vm.getDurationMinutes());
        }

        binding.tvReminderTime.setText(vm.getReminderTime());
        binding.tvEstSessions.setText(String.valueOf(vm.sessionsPerWeek()));
        binding.tvEstMinutes.setText(String.valueOf(vm.weeklyMinutes()));
        binding.tvEstKcal.setText(getString(R.string.survey_estimate_kcal_value, vm.weeklyKcal()));
    }

    private void pickTime() {
        String[] hm = vm.getReminderTime().split(":");
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(Integer.parseInt(hm[0]))
                .setMinute(Integer.parseInt(hm[1]))
                .setTitleText(R.string.survey_reminder)
                .build();
        listenTo(picker);
        picker.show(getChildFragmentManager(), TIME_PICKER_TAG);
    }

    /** Xoay màn hình khi đang mở đồng hồ: gắn lại listener cho hộp thoại được khôi phục. */
    private void restoreTimePickerListener() {
        MaterialTimePicker existing = (MaterialTimePicker) getChildFragmentManager().findFragmentByTag(TIME_PICKER_TAG);
        if (existing != null) listenTo(existing);
    }

    private void listenTo(@NonNull MaterialTimePicker picker) {
        picker.addOnPositiveButtonClickListener(v -> {
            vm.setReminderTime(String.format(Locale.ROOT, "%02d:%02d", picker.getHour(), picker.getMinute()));
            if (binding != null) render();
        });
    }
}
