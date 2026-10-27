package com.aicoach.ui.plan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.NavRootDirections;
import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.common.Resource;
import com.aicoach.data.local.SettingsPrefs;
import com.aicoach.data.model.Plan;
import com.aicoach.databinding.FragmentPlanDetailBinding;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Figma 26 Chi tiết giáo án.
 * Tham số: planId · sessionsDone (-1 = chưa theo giáo án này) · preview (true khi mở từ 17, ẩn nút tập).
 * Buổi đã tập = tích xanh, buổi kế tiếp = "Hôm nay" (bung danh sách bài + nút Bắt đầu), còn lại = khoá.
 */
public class PlanDetailFragment extends BaseFragment<FragmentPlanDetailBinding> {

    static final String ARG_PLAN_ID = "planId";
    static final String ARG_SESSIONS_DONE = "sessionsDone";
    static final String ARG_PREVIEW = "preview";

    private PlanDetailViewModel vm;
    private PlanSessionAdapter adapter;
    private String planId;
    private int sessionsDone;
    private boolean preview;

    @Override
    protected FragmentPlanDetailBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentPlanDetailBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = requireArguments();
        planId = args.getString(ARG_PLAN_ID, PlanUi.DEFAULT_PLAN_ID);
        sessionsDone = args.getInt(ARG_SESSIONS_DONE, -1);
        preview = args.getBoolean(ARG_PREVIEW, false);
        NavController nav = NavHostFragment.findNavController(this);

        vm = new ViewModelProvider(this).get(PlanDetailViewModel.class);
        adapter = new PlanSessionAdapter(new PlanSessionAdapter.Listener() {
            @Override
            public void onStartSession(@NonNull PlanSessionItem session) {
                if (preview || session.items.isEmpty()) return;
                nav.navigate(NavRootDirections.actionGlobalToReadiness(session.items.get(0).getExerciseId()));
            }

            @Override
            public void onExercise(@NonNull Plan.PlanItem item) {
                if (preview || item.getExerciseId() == null) return;
                nav.navigate(PlanDetailFragmentDirections.actionPlanDetailToExerciseDetail(item.getExerciseId()));
            }
        });
        binding.rvSessions.setAdapter(adapter);
        binding.aiNote.tvNote.setText(R.string.plan_detail_ai_note);

        binding.btnBack.setOnClickListener(v -> nav.navigateUp());
        binding.btnRetry.setOnClickListener(v -> vm.load(planId));
        binding.btnFollow.setOnClickListener(v -> vm.follow());

        vm.getPlan().observe(getViewLifecycleOwner(), r -> render());
        vm.getSelectedWeek().observe(getViewLifecycleOwner(), w -> render());
        vm.getFollowState().observe(getViewLifecycleOwner(), r -> {
            boolean loading = r != null && r.status == Resource.Status.LOADING;
            binding.progressFollow.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnFollow.setEnabled(!loading);
            if (r == null) return;
            if (r.status == Resource.Status.ERROR) {
                showError(r.message);
                vm.consumeFollow();
            } else if (r.status == Resource.Status.SUCCESS) {
                vm.onFollowed();
                showError(getString(R.string.plan_follow_done));
                renderBottom();
            }
        });

        vm.init(planId, sessionsDone >= 0);
    }

    private void render() {
        Resource<Plan> r = vm.getPlan().getValue();
        boolean loading = r == null || r.status == Resource.Status.LOADING;
        boolean error = r != null && r.status == Resource.Status.ERROR;
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.errorState.setVisibility(error ? View.VISIBLE : View.GONE);
        binding.content.setVisibility(r != null && r.status == Resource.Status.SUCCESS ? View.VISIBLE : View.INVISIBLE);
        if (error) binding.tvError.setText(r.message);
        renderBottom();
        if (r == null || r.status != Resource.Status.SUCCESS || r.data == null) return;

        Plan plan = r.data;
        int weeks = PlanUi.totalWeeks(plan);
        int perWeek = PlanUi.sessionsPerWeek(plan);
        int total = plan.getDays() != null ? plan.getDays().size() : plan.getTotalSessions();
        boolean active = sessionsDone >= 0;
        int currentWeek = active ? Math.min(weeks, sessionsDone / perWeek + 1) : 1;
        Integer selected = vm.getSelectedWeek().getValue();
        if (selected == null) {
            vm.selectWeek(currentWeek);        // observer gọi lại render() với tuần hiện tại
            return;
        }

        binding.tvTitle.setText(plan.getTitle());
        binding.tvMeta.setText(getString(R.string.plan_detail_meta, weeks, total,
                getString(PlanUi.levelLabel(plan.getLevel()))));

        binding.rowProgress.setVisibility(active ? View.VISIBLE : View.GONE);
        if (active) {
            int done = Math.min(sessionsDone, total);
            int percent = total == 0 ? 0 : done * 100 / total;
            binding.tvProgress.setText(getString(R.string.plan_detail_progress, Math.min(total, done + 1), total));
            binding.tvPercent.setText(getString(R.string.plan_detail_percent, percent));
            binding.progressPlan.setProgressCompat(percent, false);
        }

        buildWeekChips(weeks, currentWeek, selected, active);
        adapter.submitList(sessionsOf(plan, selected, perWeek, active));
    }

    /** Chip tuần: tuần đã qua có tích, tuần chưa tới có khoá (chỉ khi đang theo giáo án). */
    private void buildWeekChips(int weeks, int currentWeek, int selected, boolean active) {
        if (binding.groupWeeks.getChildCount() != weeks) {
            binding.groupWeeks.removeAllViews();
            for (int w = 1; w <= weeks; w++) {
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.view_plan_week_chip, binding.groupWeeks, false);
                chip.setId(View.generateViewId());
                chip.setText(getString(R.string.plan_week_tab, w));
                chip.setTag(w);
                binding.groupWeeks.addView(chip);
            }
            binding.groupWeeks.setOnCheckedStateChangeListener((group, ids) -> {
                if (ids.isEmpty()) return;
                Chip c = group.findViewById(ids.get(0));
                if (c != null) vm.selectWeek((Integer) c.getTag());
            });
        }
        for (int i = 0; i < binding.groupWeeks.getChildCount(); i++) {
            Chip chip = (Chip) binding.groupWeeks.getChildAt(i);
            int w = (Integer) chip.getTag();
            int icon = !active ? 0 : w < currentWeek ? R.drawable.ic_check_24 : w > currentWeek ? R.drawable.ic_lock_24 : 0;
            chip.setChipIconVisible(icon != 0);
            if (icon != 0) chip.setChipIconResource(icon);
            if (chip.isChecked() != (w == selected)) chip.setChecked(w == selected);
        }
    }

    @NonNull
    private List<PlanSessionItem> sessionsOf(@NonNull Plan plan, int week, int perWeek, boolean active) {
        List<String> days = userDays(perWeek);
        List<Plan.PlanDay> list = PlanUi.daysOfWeek(plan, week);
        List<PlanSessionItem> out = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Plan.PlanDay d = list.get(i);
            int global = (week - 1) * perWeek + i;      // 0-based trong cả giáo án
            PlanSessionItem.State state;
            if (!active) state = PlanSessionItem.State.UPCOMING;
            else if (global < sessionsDone) state = PlanSessionItem.State.DONE;
            else if (global == sessionsDone && !preview) state = PlanSessionItem.State.TODAY;
            else state = PlanSessionItem.State.LOCKED;
            out.add(new PlanSessionItem(d.getDayIndex(), global + 1, PlanUi.dayLabel(days, perWeek, i),
                    PlanUi.exerciseNames(d), PlanUi.dayMinutes(d),
                    d.getItems() != null ? d.getItems() : new ArrayList<>(), state, false, false));
        }
        return out;
    }

    /** Ngày tập người dùng chọn ở khảo sát 15 (nếu khớp số buổi/tuần của giáo án). */
    @Nullable
    private List<String> userDays(int perWeek) {
        String saved = new SettingsPrefs(requireContext()).getReminderDays();
        List<String> days = Arrays.asList(saved.split(","));
        return days.size() == perWeek ? days : null;
    }

    private void renderBottom() {
        Resource<Plan> r = vm.getPlan().getValue();
        boolean loaded = r != null && r.status == Resource.Status.SUCCESS;
        boolean show = loaded && !preview;
        binding.bottomBar.setVisibility(show ? View.VISIBLE : View.GONE);
        boolean following = vm.isFollowing();
        binding.btnFollow.setText(following ? R.string.plan_following : R.string.plan_follow);
        binding.btnFollow.setEnabled(!following);
    }
}
