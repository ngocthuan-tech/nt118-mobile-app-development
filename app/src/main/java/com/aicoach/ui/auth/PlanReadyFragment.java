package com.aicoach.ui.auth;

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
import com.aicoach.common.Resource;
import com.aicoach.data.model.Plan;
import com.aicoach.databinding.FragmentPlanReadyBinding;
import com.aicoach.ui.plan.PlanSessionAdapter;
import com.aicoach.ui.plan.PlanSessionItem;
import com.aicoach.ui.plan.PlanUi;

import java.util.ArrayList;
import java.util.List;

/**
 * Figma 17 — giáo án AI vừa chọn. "Bắt đầu hành trình" → PUT users/me {activePlanId, profileCompleted: true} → 20.
 * "Xem đủ x tuần" → 26 (chế độ xem trước), "Điều chỉnh giáo án" → quay về 15.
 */
public class PlanReadyFragment extends BaseFragment<FragmentPlanReadyBinding> {

    private SurveyViewModel vm;

    @Override
    protected FragmentPlanReadyBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentPlanReadyBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vm = SurveyUi.viewModel(this);
        NavController nav = NavHostFragment.findNavController(this);
        Plan plan = vm.getChosenPlan();
        if (plan == null) {          // app bị hệ thống đóng giữa chừng → làm lại bước 15
            nav.popBackStack(R.id.surveyScheduleFragment, false);
            return;
        }

        int weeks = PlanUi.totalWeeks(plan);
        binding.tvPlanTitle.setText(plan.getTitle());
        binding.tvWeeks.setText(String.valueOf(weeks));
        binding.tvSessions.setText(String.valueOf(plan.getTotalSessions() > 0
                ? plan.getTotalSessions() : plan.getDays().size()));
        binding.tvMinutes.setText(getString(R.string.plan_stat_minutes_value, plan.getMinutesPerSession()));
        binding.btnSeeAll.setText(getString(R.string.plan_see_all_weeks, weeks));
        binding.aiNote.tvNote.setText(R.string.plan_ready_ai_note);

        PlanSessionAdapter adapter = new PlanSessionAdapter(new PlanSessionAdapter.Listener() {
            @Override
            public void onStartSession(@NonNull PlanSessionItem session) {
            }

            @Override
            public void onExercise(@NonNull Plan.PlanItem item) {
            }
        });
        binding.rvSessions.setAdapter(adapter);
        adapter.submitList(firstWeek(plan));

        binding.btnSeeAll.setOnClickListener(v ->
                nav.navigate(PlanReadyFragmentDirections.actionReadyToPlanPreview(plan.getId())));
        binding.btnAdjust.setOnClickListener(v -> nav.popBackStack(R.id.surveyScheduleFragment, false));
        binding.btnStart.setOnClickListener(v -> vm.confirmPlan());

        vm.getSaveState().observe(getViewLifecycleOwner(), r -> {
            boolean loading = r != null && r.status == Resource.Status.LOADING;
            binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnStart.setEnabled(!loading);
            if (r == null) return;
            if (r.status == Resource.Status.ERROR) {
                showError(r.message);
                vm.consumeSave();
            } else if (r.status == Resource.Status.SUCCESS) {
                vm.consumeSave();
                nav.navigate(R.id.action_ready_to_notifications);
            }
        });
    }

    /** Tuần 1 theo đúng các ngày người dùng chọn ở 15 (T2, T4, T6…). */
    @NonNull
    private List<PlanSessionItem> firstWeek(@NonNull Plan plan) {
        List<String> labels = vm.selectedDayLabels();
        List<PlanSessionItem> out = new ArrayList<>();
        List<Plan.PlanDay> days = PlanUi.daysOfWeek(plan, 1);
        for (int i = 0; i < days.size(); i++) {
            Plan.PlanDay d = days.get(i);
            out.add(new PlanSessionItem(d.getDayIndex(), i + 1,
                    PlanUi.dayLabel(labels, PlanUi.sessionsPerWeek(plan), i), PlanUi.exerciseNames(d),
                    PlanUi.dayMinutes(d), d.getItems(), PlanSessionItem.State.UPCOMING, true, i == 0));
        }
        return out;
    }
}
