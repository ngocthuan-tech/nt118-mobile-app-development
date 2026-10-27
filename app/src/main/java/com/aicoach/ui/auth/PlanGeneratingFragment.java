package com.aicoach.ui.auth;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.common.Resource;
import com.aicoach.data.model.Plan;
import com.aicoach.databinding.FragmentPlanGeneratingBinding;
import com.aicoach.databinding.ViewGeneratingStepBinding;
import com.aicoach.ui.plan.PlanUi;

import java.util.ArrayList;
import java.util.List;

/**
 * Figma 16 — hiệu ứng ~2,6 giây trong lúc tải GET plans, rồi "AI" chọn giáo án khớp khảo sát → 17.
 * Tiến độ tính theo thời gian lưu trong ViewModel nên xoay màn hình vẫn chạy tiếp, không làm lại từ đầu.
 */
public class PlanGeneratingFragment extends BaseFragment<FragmentPlanGeneratingBinding> {

    private static final long TICK_MS = 40;

    private SurveyViewModel vm;
    private boolean navigated;

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (binding == null) return;
            render();
            if (!navigated) binding.ring.postDelayed(this, TICK_MS);
        }
    };

    @Override
    protected FragmentPlanGeneratingBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentPlanGeneratingBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vm = SurveyUi.viewModel(this);

        List<String> focus = new ArrayList<>();
        focus.add(getString(goalLabel()));
        binding.stepBody.tvTitle.setText(R.string.plan_generating_step_body);
        binding.stepBody.tvDesc.setText(getString(R.string.plan_generating_step_body_desc,
                vm.getHeightCm(), vm.getWeightKg(), vm.bmiText()));
        binding.stepGoal.tvTitle.setText(R.string.plan_generating_step_goal);
        binding.stepGoal.tvDesc.setText(TextUtils.join(" – ", focus));
        binding.stepExercises.tvTitle.setText(R.string.plan_generating_step_exercises);
        binding.stepExercises.tvDesc.setText(getString(R.string.plan_generating_step_exercises_desc,
                getString(PlanUi.levelLabel(vm.getLevel())).toLowerCase(java.util.Locale.ROOT)));
        binding.stepSchedule.tvTitle.setText(R.string.plan_generating_step_schedule);
        binding.stepSchedule.tvDesc.setText(getString(R.string.plan_generating_step_schedule_desc,
                vm.sessionsPerWeek(), vm.getDurationMinutes()));

        // Đang tạo thì không cho Back nửa chừng
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        Resource<List<Plan>> r = vm.getPlansState().getValue();
                        if (r != null && r.status == Resource.Status.ERROR) {
                            setEnabled(false);
                            requireActivity().getOnBackPressedDispatcher().onBackPressed();
                        }
                    }
                });

        binding.btnRetry.setOnClickListener(v -> vm.retryPlans());
        vm.getPlansState().observe(getViewLifecycleOwner(), r -> render());
        vm.startGenerating();
    }

    @Override
    public void onResume() {
        super.onResume();
        tick.run();
    }

    @Override
    public void onPause() {
        if (binding != null) binding.ring.removeCallbacks(tick);
        super.onPause();
    }

    private void render() {
        Resource<List<Plan>> r = vm.getPlansState().getValue();
        boolean failed = r != null && r.status == Resource.Status.ERROR;
        boolean loaded = r != null && r.status == Resource.Status.SUCCESS && r.data != null;

        int progress = vm.generatingProgress();
        if (!loaded) progress = Math.min(progress, 90);   // chưa có dữ liệu thì dừng ở 90%
        binding.ring.setProgressCompat(progress, true);
        binding.tvPercent.setText(getString(R.string.plan_percent_value, progress));

        bindStep(binding.stepBody, progress, 25);
        bindStep(binding.stepGoal, progress, 50);
        bindStep(binding.stepExercises, progress, 75);
        bindStep(binding.stepSchedule, progress, 100);

        binding.btnRetry.setVisibility(failed ? View.VISIBLE : View.GONE);
        if (failed && r.message != null && binding.btnRetry.getTag() != r) {
            binding.btnRetry.setTag(r);
            showError(r.message);
        }

        if (loaded && progress >= 100 && !navigated && isResumed()) {
            navigated = true;
            vm.pickPlan(r.data);
            NavHostFragment.findNavController(this).navigate(R.id.action_generating_to_ready);
        }
    }

    /** Xong (≥ mốc) = tích xanh · đang làm (trong 25% trước mốc) = vòng xoay · chưa tới = vòng trống. */
    private void bindStep(@NonNull ViewGeneratingStepBinding step, int progress, int doneAt) {
        boolean done = progress >= doneAt;
        boolean running = !done && progress >= doneAt - 25;
        step.spinner.setVisibility(running ? View.VISIBLE : View.GONE);
        step.imgState.setVisibility(running ? View.INVISIBLE : View.VISIBLE);
        if (done) {
            step.imgState.setImageResource(R.drawable.ic_check_circle_24);
            step.imgState.setBackground(null);
            step.imgState.setImageTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.state_success)));
        } else {
            step.imgState.setImageDrawable(null);
            step.imgState.setBackgroundResource(R.drawable.bg_circle_outline);
        }
        float alpha = done || running ? 1f : 0.5f;
        step.tvTitle.setAlpha(alpha);
        step.tvDesc.setAlpha(alpha);
        step.tvDesc.setTextColor(ContextCompat.getColor(requireContext(),
                running ? R.color.accent_cyan : R.color.text_on_dark_secondary));
    }

    @StringRes
    private int goalLabel() {
        switch (vm.getGoal()) {
            case SurveyViewModel.GOAL_MUSCLE: return R.string.survey_goal_muscle;
            case SurveyViewModel.GOAL_FIT: return R.string.survey_goal_fit;
            case SurveyViewModel.GOAL_ENDURANCE: return R.string.survey_goal_endurance;
            default: return R.string.survey_goal_lose;
        }
    }
}
