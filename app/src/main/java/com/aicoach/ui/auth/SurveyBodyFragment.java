package com.aicoach.ui.auth;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.common.Resource;
import com.aicoach.databinding.FragmentSurveyBodyBinding;
import com.aicoach.databinding.ViewSurveyGenderBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** Figma 13 Khảo sát 1/3 — giới tính, năm sinh, chiều cao, cân nặng, BMI tự tính → PUT users/me. */
public class SurveyBodyFragment extends BaseFragment<FragmentSurveyBodyBinding> {

    private static final int MIN_AGE = 12;
    private static final int MAX_AGE = 80;

    private SurveyViewModel vm;

    @Override
    protected FragmentSurveyBodyBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentSurveyBodyBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vm = SurveyUi.viewModel(this);
        SurveyUi.bindHeader(this, binding.header, 1);

        binding.cardMale.tvLabel.setText(R.string.survey_gender_male);
        binding.cardFemale.tvLabel.setText(R.string.survey_gender_female);
        binding.cardMale.getRoot().setOnClickListener(v -> { vm.setMale(true); renderGender(); });
        binding.cardFemale.getRoot().setOnClickListener(v -> { vm.setMale(false); renderGender(); });

        binding.rowBirthYear.setOnClickListener(v -> pickBirthYear());

        binding.sliderHeight.setValue(vm.getHeightCm());
        binding.sliderWeight.setValue(vm.getWeightKg());
        binding.sliderHeight.addOnChangeListener((s, value, fromUser) -> {
            vm.setHeightCm(Math.round(value));
            renderNumbers();
        });
        binding.sliderWeight.addOnChangeListener((s, value, fromUser) -> {
            vm.setWeightKg(Math.round(value));
            renderNumbers();
        });

        binding.footer.btnContinue.setOnClickListener(v -> vm.saveBody());
        vm.getSaveState().observe(getViewLifecycleOwner(), r -> {
            SurveyUi.renderFooter(binding.footer, r);
            if (r == null) return;
            if (r.status == Resource.Status.ERROR) {
                showError(r.message);
                vm.consumeSave();
            } else if (r.status == Resource.Status.SUCCESS) {
                vm.consumeSave();
                NavHostFragment.findNavController(this).navigate(R.id.action_survey_body_to_goal);
            }
        });

        renderGender();
        renderNumbers();
    }

    private void renderGender() {
        bindGender(binding.cardMale, vm.isMale());
        bindGender(binding.cardFemale, !vm.isMale());
    }

    private void bindGender(@NonNull ViewSurveyGenderBinding card, boolean selected) {
        card.getRoot().setSelected(selected);
        card.radio.setChecked(selected);
        if (selected) {
            card.avatar.setBackgroundResource(R.drawable.bg_auth_gradient_circle);
            card.avatar.setBackgroundTintList(null);
            card.imgIcon.setImageTintList(ColorStateList.valueOf(color(R.color.white)));
        } else {
            card.avatar.setBackgroundResource(R.drawable.bg_circle);
            card.avatar.setBackgroundTintList(ColorStateList.valueOf(color(R.color.brand_primary_light)));
            card.imgIcon.setImageTintList(ColorStateList.valueOf(color(R.color.brand_primary)));
        }
    }

    private void renderNumbers() {
        binding.tvBirthYear.setText(String.valueOf(vm.getBirthYear()));
        binding.tvAge.setText(getString(R.string.survey_age, vm.getAge()));
        binding.tvHeight.setText(getString(R.string.survey_height_value, vm.getHeightCm()));
        binding.tvWeight.setText(getString(R.string.survey_weight_value, vm.getWeightKg()));

        double bmi = vm.bmi();
        @StringRes int label;
        @ColorRes int fg;
        @ColorRes int bg;
        if (bmi < 18.5) {
            label = R.string.survey_bmi_under; fg = R.color.state_warning; bg = R.color.state_warning_light;
        } else if (bmi < 25) {
            label = R.string.survey_bmi_normal; fg = R.color.state_success; bg = R.color.state_success_light;
        } else if (bmi < 30) {
            label = R.string.survey_bmi_over; fg = R.color.state_warning; bg = R.color.state_warning_light;
        } else {
            label = R.string.survey_bmi_obese; fg = R.color.state_error; bg = R.color.state_error_light;
        }
        binding.tvBmi.setText(getString(R.string.survey_bmi, vm.bmiText(), getString(label)));
        binding.tvBmi.setTextColor(color(fg));
        binding.imgBmi.setImageTintList(ColorStateList.valueOf(color(fg)));
        binding.boxBmi.setBackgroundTintList(ColorStateList.valueOf(color(bg)));
    }

    private void pickBirthYear() {
        int year = SurveyViewModel.currentYear();
        NumberPicker picker = new NumberPicker(requireContext());
        picker.setMinValue(year - MAX_AGE);
        picker.setMaxValue(year - MIN_AGE);
        picker.setValue(Math.max(picker.getMinValue(), Math.min(picker.getMaxValue(), vm.getBirthYear())));
        picker.setWrapSelectorWheel(false);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.survey_pick_year)
                .setView(picker)
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.common_confirm, (d, w) -> {
                    vm.setBirthYear(picker.getValue());
                    if (binding != null) renderNumbers();
                })
                .show();
    }

    private int color(@ColorRes int res) {
        return ContextCompat.getColor(requireContext(), res);
    }
}
