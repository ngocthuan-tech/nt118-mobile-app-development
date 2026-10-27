package com.aicoach.ui.auth;

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
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.common.Resource;
import com.aicoach.databinding.FragmentSurveyGoalBinding;
import com.aicoach.databinding.ViewSurveyOptionBinding;
import com.google.android.material.chip.Chip;

/** Figma 14 Khảo sát 2/3 — 1 mục tiêu + nhiều vùng cơ muốn cải thiện → PUT users/me {goal}. */
public class SurveyGoalFragment extends BaseFragment<FragmentSurveyGoalBinding> {

    private SurveyViewModel vm;

    @Override
    protected FragmentSurveyGoalBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentSurveyGoalBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vm = SurveyUi.viewModel(this);
        SurveyUi.bindHeader(this, binding.header, 2);

        setupOption(binding.optionLose, SurveyViewModel.GOAL_LOSE, R.drawable.ic_fire_24,
                R.color.medal_orange, R.color.state_warning_light, R.string.survey_goal_lose, R.string.survey_goal_lose_desc);
        setupOption(binding.optionMuscle, SurveyViewModel.GOAL_MUSCLE, R.drawable.ic_dumbbell_24,
                R.color.brand_primary, R.color.brand_primary_light, R.string.survey_goal_muscle, R.string.survey_goal_muscle_desc);
        setupOption(binding.optionFit, SurveyViewModel.GOAL_FIT, R.drawable.ic_target_24,
                R.color.state_success, R.color.state_success_light, R.string.survey_goal_fit, R.string.survey_goal_fit_desc);
        setupOption(binding.optionEndurance, SurveyViewModel.GOAL_ENDURANCE, R.drawable.ic_favorite_border_24,
                R.color.navy_700, R.color.brand_primary_light, R.string.survey_goal_endurance, R.string.survey_goal_endurance_desc);

        Chip[] chips = {binding.chipAbs, binding.chipLegs, binding.chipArms, binding.chipChest,
                binding.chipBack, binding.chipFull};
        for (Chip chip : chips) {
            String key = chip.getText().toString();
            chip.setChecked(vm.getFocusAreas().contains(key));
            chip.setOnCheckedChangeListener((c, checked) -> {
                if (checked) vm.getFocusAreas().add(key);
                else vm.getFocusAreas().remove(key);
            });
        }
        if (savedInstanceState == null && vm.getFocusAreas().isEmpty()) {
            binding.chipAbs.setChecked(true);
            binding.chipLegs.setChecked(true);
        }

        binding.footer.btnContinue.setOnClickListener(v -> vm.saveGoal());
        vm.getSaveState().observe(getViewLifecycleOwner(), r -> {
            SurveyUi.renderFooter(binding.footer, r);
            if (r == null) return;
            if (r.status == Resource.Status.ERROR) {
                showError(r.message);
                vm.consumeSave();
            } else if (r.status == Resource.Status.SUCCESS) {
                vm.consumeSave();
                NavHostFragment.findNavController(this).navigate(R.id.action_survey_goal_to_schedule);
            }
        });
        render();
    }

    private void setupOption(@NonNull ViewSurveyOptionBinding o, @NonNull String goal, @DrawableRes int icon,
                             @ColorRes int tint, @ColorRes int bg, @StringRes int title, @StringRes int desc) {
        o.imgIcon.setImageResource(icon);
        o.imgIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), tint)));
        o.iconBox.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), bg)));
        o.tvTitle.setText(title);
        o.tvDesc.setText(desc);
        o.getRoot().setTag(goal);
        o.getRoot().setOnClickListener(v -> {
            vm.setGoal(goal);
            render();
        });
    }

    private void render() {
        ViewSurveyOptionBinding[] options = {binding.optionLose, binding.optionMuscle, binding.optionFit,
                binding.optionEndurance};
        for (ViewSurveyOptionBinding o : options) {
            boolean selected = vm.getGoal().equals(o.getRoot().getTag());
            o.getRoot().setSelected(selected);
            o.radio.setChecked(selected);
        }
        @StringRes int note;
        switch (vm.getGoal()) {
            case SurveyViewModel.GOAL_MUSCLE: note = R.string.survey_ai_muscle; break;
            case SurveyViewModel.GOAL_FIT: note = R.string.survey_ai_fit; break;
            case SurveyViewModel.GOAL_ENDURANCE: note = R.string.survey_ai_endurance; break;
            default: note = R.string.survey_ai_lose; break;
        }
        binding.aiNote.tvNote.setText(note);
    }
}
