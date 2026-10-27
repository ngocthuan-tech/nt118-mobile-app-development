package com.aicoach.ui.home;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.NavRootDirections;
import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.common.ExerciseImages;
import com.aicoach.common.Resource;
import com.aicoach.data.model.HomeData;
import com.aicoach.data.model.Plan;
import com.aicoach.databinding.FragmentHomeBinding;
import com.aicoach.ui.exercise.ExerciseUi;
import com.aicoach.ui.plan.PlanUi;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

/**
 * Figma 21 Trang chủ — Fragment → HomeViewModel → HomeRepository → HomeApi (GET home).
 * Khối thầy chấm giữa kỳ: "Chuỗi 7 ngày" (streak + 7 chấm T2–CN) và "Bài tập hôm nay", đều lấy từ database.
 * Tài khoản mới: streak 0, 7 chấm trống, 3 bài gợi ý — vẫn hiển thị đầy đủ.
 */
public class HomeFragment extends BaseFragment<FragmentHomeBinding> {

    private static final String DEFAULT_EXERCISE = "squat";

    private HomeViewModel vm;
    private ExerciseCardAdapter exerciseAdapter;
    private String firstExerciseId = DEFAULT_EXERCISE;
    private HomeData.ActivePlan activePlan;

    @Override
    protected FragmentHomeBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentHomeBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vm = new ViewModelProvider(this).get(HomeViewModel.class);
        NavController nav = NavHostFragment.findNavController(this);

        exerciseAdapter = new ExerciseCardAdapter(item ->
                nav.navigate(HomeFragmentDirections.actionHomeToExerciseDetail(item.exerciseId)));
        binding.rvTodayExercises.setAdapter(exerciseAdapter);

        binding.btnStartWorkout.setOnClickListener(v -> openWorkout(firstExerciseId));
        binding.tvAvatar.setOnClickListener(v -> nav.navigate(R.id.action_global_to_profile));
        binding.btnNotifications.setOnClickListener(v -> nav.navigate(R.id.action_global_to_notifications));
        binding.cardPlan.setOnClickListener(v -> openPlan());
        binding.btnSeeAllPlans.setOnClickListener(v -> openPlan());

        vm.getHome().observe(getViewLifecycleOwner(), this::render);
    }

    @Override
    public void onResume() {
        super.onResume();
        vm.refreshIfStale();
    }

    private void render(@Nullable Resource<HomeData> r) {
        if (r == null) return;
        binding.progressHome.setVisibility(r.status == Resource.Status.LOADING ? View.VISIBLE : View.GONE);
        if (r.status == Resource.Status.SUCCESS && r.data != null) {
            bind(r.data);
        } else if (r.status == Resource.Status.LOADING && vm.getLast() != null) {
            bind(vm.getLast());
        } else if (r.status == Resource.Status.ERROR) {
            if (vm.getLast() != null) bind(vm.getLast());
            Snackbar.make(binding.getRoot(), r.message != null ? r.message : getString(R.string.common_error_generic),
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.common_retry, v -> vm.load()).show();
        }
    }

    private void bind(@NonNull HomeData d) {
        // Lời chào + avatar chữ cái đầu của tên cuối ("Nguyễn Minh" → "M")
        String name = d.getDisplayName() != null && !d.getDisplayName().trim().isEmpty()
                ? d.getDisplayName().trim() : getString(R.string.home_greeting_fallback);
        String[] parts = name.split("\\s+");
        binding.tvGreeting.setText(getString(R.string.home_greeting, parts[parts.length - 1]));
        binding.tvAvatar.setText(parts[parts.length - 1].substring(0, 1).toUpperCase());

        // Chuỗi 7 ngày
        binding.tvStreakTitle.setText(getString(R.string.home_streak_title, d.getStreak()));
        HomeData.Week week = d.getWeek();
        if (week != null && week.getRemainingSessions() > 0) {
            binding.tvStreakSub.setText(getString(R.string.home_streak_sub, week.getRemainingSessions()));
        } else {
            binding.tvStreakSub.setText(R.string.home_week_goal_done);
        }
        bindWeekDays(week != null && week.getDays() != null ? week.getDays() : new ArrayList<>());

        // Phút / kcal hôm nay
        HomeData.Today today = d.getToday();
        binding.tvMinutesValue.setText(String.valueOf(today != null ? today.getMinutes() : 0));
        binding.tvKcalValue.setText(String.valueOf(today != null ? Math.round(today.getCalories()) : 0));

        bindPlan(d.getActivePlan());
        bindTodayWorkout(d.getTodayWorkout());
    }

    /** 7 chấm T2 → CN: đã tập = nền cyan mờ, hôm nay = viền cyan, còn lại = mờ. */
    private void bindWeekDays(@NonNull List<HomeData.WeekDay> days) {
        TextView[] chips = {binding.dayMon, binding.dayTue, binding.dayWed, binding.dayThu,
                binding.dayFri, binding.daySat, binding.daySun};
        int cyan = ContextCompat.getColor(requireContext(), R.color.accent_cyan);
        int doneBg = ContextCompat.getColor(requireContext(), R.color.day_done_bg);
        int dimBg = ContextCompat.getColor(requireContext(), R.color.white_10);
        int dimText = ContextCompat.getColor(requireContext(), R.color.text_on_dark_secondary);
        for (int i = 0; i < chips.length; i++) {
            HomeData.WeekDay day = i < days.size() ? days.get(i) : null;
            if (day != null && day.getLabel() != null) chips[i].setText(day.getLabel());
            boolean active = day != null && day.isActive();
            boolean isToday = day != null && day.isToday();
            if (isToday) {
                chips[i].setBackgroundResource(R.drawable.bg_day_today);
                chips[i].setBackgroundTintList(active ? ColorStateList.valueOf(doneBg) : null);
                chips[i].setTextColor(cyan);
            } else {
                chips[i].setBackgroundResource(R.drawable.bg_circle);
                chips[i].setBackgroundTintList(ColorStateList.valueOf(active ? doneBg : dimBg));
                chips[i].setTextColor(active ? cyan : dimText);
            }
        }
    }

    private void bindPlan(@Nullable HomeData.ActivePlan plan) {
        activePlan = plan;
        if (plan == null) {
            binding.tvPlanTitle.setText(R.string.home_choose_plan);
            binding.tvPlanMeta.setText(R.string.home_choose_plan_desc);
            binding.progressPlan.setVisibility(View.GONE);
            binding.tvPlanPercent.setVisibility(View.GONE);
            ExerciseImages.bind(binding.imgPlan, "jumping_jack");
            return;
        }
        binding.tvPlanTitle.setText(plan.getTitle());
        binding.tvPlanMeta.setText(plan.isCompleted() ? getString(R.string.home_plan_completed)
                : getString(R.string.home_plan_meta, Math.max(1, plan.getCurrentWeek()),
                Math.min(plan.getTotalSessions(), plan.getSessionsDone() + 1), plan.getTotalSessions(),
                plan.getMinutesPerSession()));
        binding.progressPlan.setVisibility(View.VISIBLE);
        binding.tvPlanPercent.setVisibility(View.VISIBLE);
        binding.progressPlan.setProgressCompat(plan.getProgressPercent(), true);
        binding.tvPlanPercent.setText(getString(R.string.home_plan_percent, plan.getProgressPercent()));
        ExerciseImages.bind(binding.imgPlan, plan.getCoverImageName() != null ? plan.getCoverImageName() : "jumping_jack");
    }

    private void bindTodayWorkout(@Nullable HomeData.TodayWorkout workout) {
        List<ExerciseCardItem> cards = new ArrayList<>();
        int minutes = 0;
        if (workout != null && workout.getExercises() != null) {
            for (Plan.PlanItem item : workout.getExercises()) {
                if (item.getExerciseId() == null) continue;
                cards.add(new ExerciseCardItem(item.getExerciseId(),
                        item.getExerciseName() != null ? item.getExerciseName() : item.getExerciseId(),
                        ExerciseUi.volume(requireContext(), item.getSets(), item.getReps(), item.isTimeBased())));
            }
            minutes = workout.getEstimatedMinutes();
        }
        firstExerciseId = cards.isEmpty() ? DEFAULT_EXERCISE : cards.get(0).exerciseId;
        binding.tvTodaySummary.setText(getString(R.string.home_today_summary, cards.size(), minutes));
        exerciseAdapter.submitList(cards);
    }

    /** HỢP ĐỒNG điều hướng: mọi nút "Bắt đầu tập" đều đi qua action_global_to_readiness. */
    private void openWorkout(@NonNull String exerciseId) {
        NavHostFragment.findNavController(this).navigate(NavRootDirections.actionGlobalToReadiness(exerciseId));
    }

    /** Thẻ giáo án / "Xem tất cả" → 26 Chi tiết giáo án (chưa chọn thì mở giáo án gợi ý mặc định). */
    private void openPlan() {
        HomeFragmentDirections.ActionHomeToPlanDetail action = HomeFragmentDirections.actionHomeToPlanDetail(
                activePlan != null ? activePlan.getPlanId() : PlanUi.DEFAULT_PLAN_ID);
        if (activePlan != null) action.setSessionsDone(activePlan.getSessionsDone());
        NavHostFragment.findNavController(this).navigate(action);
    }
}
