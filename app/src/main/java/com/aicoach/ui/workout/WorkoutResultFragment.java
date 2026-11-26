package com.aicoach.ui.workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.R;
import com.aicoach.common.Resource;
import com.aicoach.common.BaseFragment;
import com.aicoach.data.model.WorkoutSession;
import com.aicoach.databinding.FragmentWorkoutResultBinding;
import com.aicoach.pose.FormIssue;
import com.aicoach.pose.WorkoutSummary;
import com.google.android.material.snackbar.Snackbar;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Figma 35 "Kết quả buổi tập" (Trưởng nhóm) — mọi số liệu là của lượt vừa tập:
 *  - Tập lẻ 1 bài: chi tiết từng lỗi + so điểm với buổi trước cùng bài (GET /sessions).
 *  - Buổi giáo án nhiều bài: tổng hợp cả buổi, mỗi bài 1 dòng phân tích.
 * Việc lưu lên server đã chạy ngay khi xong từng bài (WorkoutSessionViewModel); màn này chỉ hiện
 * trạng thái lưu, kcal/XP server trả về, và nút Thử lại nếu mất mạng.
 */
public class WorkoutResultFragment extends BaseFragment<FragmentWorkoutResultBinding> {

    private static final ZoneId VN = ZoneId.of("Asia/Ho_Chi_Minh");

    private final AiFeedbackAdapter feedbackAdapter = new AiFeedbackAdapter();
    private WorkoutSessionViewModel vm;
    private Snackbar saveError;

    @Override
    protected FragmentWorkoutResultBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentWorkoutResultBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController nav = NavHostFragment.findNavController(this);
        vm = WorkoutSessionViewModel.of(this);
        binding.rvAiFeedback.setAdapter(feedbackAdapter);

        View.OnClickListener backHome = v -> nav.popBackStack(R.id.nav_workout, true);
        binding.btnClose.setOnClickListener(backHome);
        binding.btnBackHome.setOnClickListener(backHome);
        binding.btnShare.setOnClickListener(v -> nav.navigate(WorkoutResultFragmentDirections.actionResultToShare()));

        // Huy hiệu mới do module Thành tích tính riêng, POST /sessions không trả về → không hiện thẻ giả
        binding.cardNewBadge.setVisibility(View.GONE);

        if (!vm.hasResults()) {
            // App bị hệ điều hành đóng giữa chừng, mất dữ liệu lượt tập → không hiện số giả
            nav.popBackStack(R.id.nav_workout, true);
            return;
        }
        bindResults(vm.entries());

        vm.entriesLive().observe(getViewLifecycleOwner(), e -> renderSave());
        vm.previousScore().observe(getViewLifecycleOwner(), p -> renderSave());
    }

    // ------------------------------------------------------------------ số liệu (không đổi khi đang lưu)

    private void bindResults(List<WorkoutSessionViewModel.Entry> entries) {
        boolean single = entries.size() == 1;
        WorkoutSummary first = entries.get(0).summary;
        String time = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
                .format(Instant.ofEpochMilli(first.startedAtMs).atZone(VN));

        int attempts = 0, correct = 0, seconds = 0, completionSum = 0;
        for (WorkoutSessionViewModel.Entry e : entries) {
            attempts += e.summary.totalReps;
            correct += e.summary.correctReps;
            seconds += e.summary.durationSec;
            completionSum += e.summary.completionPercent();
        }
        int score = attempts == 0 ? 0 : Math.round(correct * 100f / attempts);
        int completion = completionSum / entries.size();

        binding.tvSessionMeta.setText(single
                ? getString(R.string.ai_result_meta, first.exerciseName, first.setsDone, time)
                : getString(R.string.ai_result_meta_plan, entries.size(), time));
        binding.progressScore.setProgress(score);
        binding.tvScore.setText(String.valueOf(score));

        if (single) {
            setStat(binding.statReps.tvValue, binding.statReps.tvLabel, String.valueOf(first.correctReps),
                    first.timed ? R.string.ai_stat_seconds : R.string.result_stat_reps);
        } else {
            setStat(binding.statReps.tvValue, binding.statReps.tvLabel,
                    getString(R.string.ai_progress_count, entries.size(), vm.size()), R.string.ai_stat_exercises);
        }
        setStat(binding.statTime.tvValue, binding.statTime.tvLabel, formatTime(seconds), R.string.result_stat_time);
        setStat(binding.statKcal.tvValue, binding.statKcal.tvLabel, "…", R.string.result_stat_kcal);
        setStat(binding.statAccuracy.tvValue, binding.statAccuracy.tvLabel, completion + "%",
                R.string.ai_stat_completion);
        binding.statAccuracy.tvValue.setTextColor(ContextCompat.getColor(requireContext(),
                completion >= 50 ? R.color.state_success : R.color.state_warning));

        feedbackAdapter.submitList(single ? detailFeedback(first) : perExerciseFeedback(entries));
    }

    /** 1 bài: mỗi lỗi 1 dòng (nhiều nhất lên đầu) + 1 dòng tổng số lần đúng. */
    private List<AiFeedbackItem> detailFeedback(WorkoutSummary s) {
        List<Map.Entry<String, Long>> errors = sortedErrors(s);
        List<AiFeedbackItem> items = new ArrayList<>();
        for (Map.Entry<String, Long> e : errors) {
            long count = e.getValue();
            AiFeedbackItem.Type type = count >= 3 ? AiFeedbackItem.Type.ERROR : AiFeedbackItem.Type.WARNING;
            items.add(new AiFeedbackItem(type, issueLabel(e.getKey()), getString(R.string.ai_feedback_count, (int) count)));
        }
        String goodTitle = s.timed
                ? getString(R.string.ai_feedback_good_seconds, s.correctReps, s.totalReps)
                : getString(R.string.ai_feedback_good_reps, s.correctReps, s.totalReps);
        items.add(new AiFeedbackItem(AiFeedbackItem.Type.GOOD, goodTitle,
                errors.isEmpty() ? getString(R.string.ai_feedback_none) : getString(R.string.ai_feedback_good_detail)));
        return items;
    }

    /** Buổi giáo án: mỗi bài 1 dòng — số lần đúng + lỗi gặp nhiều nhất. */
    private List<AiFeedbackItem> perExerciseFeedback(List<WorkoutSessionViewModel.Entry> entries) {
        List<AiFeedbackItem> items = new ArrayList<>();
        for (WorkoutSessionViewModel.Entry entry : entries) {
            WorkoutSummary s = entry.summary;
            String title = s.timed
                    ? getString(R.string.ai_feedback_exercise_seconds, s.exerciseName, s.correctReps, s.targetReps)
                    : getString(R.string.ai_feedback_exercise_reps, s.exerciseName, s.correctReps, s.targetReps);
            List<Map.Entry<String, Long>> errors = sortedErrors(s);
            if (errors.isEmpty()) {
                items.add(new AiFeedbackItem(AiFeedbackItem.Type.GOOD, title, getString(R.string.ai_feedback_none)));
            } else {
                Map.Entry<String, Long> top = errors.get(0);
                AiFeedbackItem.Type type = s.postureScore() < 70 ? AiFeedbackItem.Type.ERROR : AiFeedbackItem.Type.WARNING;
                items.add(new AiFeedbackItem(type, title, getString(R.string.ai_feedback_exercise_errors,
                        issueLabel(top.getKey()), top.getValue().intValue())));
            }
        }
        return items;
    }

    // ------------------------------------------------------------------ trạng thái lưu (thay đổi theo mạng)

    private void renderSave() {
        if (binding == null || !vm.hasResults()) return;
        List<WorkoutSessionViewModel.Entry> entries = vm.entries();

        double kcal = 0;
        long xp = 0;
        for (WorkoutSessionViewModel.Entry e : entries) {
            Resource<WorkoutSession> r = e.save();
            if (e.saved() && r != null && r.data != null) {
                kcal += r.data.getCalories();
                xp += r.data.getXpEarned();
            }
        }

        if (vm.pendingCount() > 0) {
            binding.tvScoreNote.setText(R.string.ai_saving);
            binding.statKcal.tvValue.setText("…");
            if (saveError != null) saveError.dismiss();
            return;
        }
        binding.statKcal.tvValue.setText(String.valueOf(Math.round(kcal)));

        if (vm.failedCount() > 0) {
            Resource<WorkoutSession> failed = null;
            for (WorkoutSessionViewModel.Entry e : entries) if (e.failed()) failed = e.save();
            String reason = failed != null && failed.message != null ? failed.message : "";
            String msg = getString(R.string.ai_save_failed, reason);
            binding.tvScoreNote.setText(msg);
            if (saveError == null || !saveError.isShown()) {
                saveError = Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.common_retry, v -> vm.retryFailed());
                saveError.show();
            }
            return;
        }
        if (saveError != null) saveError.dismiss();

        String note = compareNote(entries);
        if (note == null) return;   // đang tải điểm buổi trước
        binding.tvScoreNote.setText(xp > 0 ? getString(R.string.ai_note_xp, note, (int) xp) : note);
    }

    /** Dòng dưới vòng điểm: 1 bài → so với buổi trước cùng bài; buổi giáo án → đã lưu bao nhiêu bài. */
    @Nullable
    private String compareNote(List<WorkoutSessionViewModel.Entry> entries) {
        if (entries.size() > 1 || vm.isPlanRun()) {
            return entries.size() >= vm.size()
                    ? getString(R.string.ai_note_plan, entries.size())
                    : getString(R.string.ai_note_plan_partial, entries.size());
        }
        Integer previous = vm.previousScore().getValue();
        if (previous == null) return null;
        WorkoutSummary s = entries.get(0).summary;
        if (previous == WorkoutSessionViewModel.NO_PREVIOUS) {
            return getString(R.string.ai_note_first, s.exerciseName);
        }
        int diff = s.postureScore() - previous;
        return diff > 0 ? getString(R.string.ai_note_better, diff)
                : diff < 0 ? getString(R.string.ai_note_worse, -diff)
                : getString(R.string.ai_note_same);
    }

    // ------------------------------------------------------------------ tiện ích

    private static List<Map.Entry<String, Long>> sortedErrors(WorkoutSummary s) {
        List<Map.Entry<String, Long>> errors = new ArrayList<>(s.formErrors.entrySet());
        errors.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
        return errors;
    }

    private static String issueLabel(String key) {
        FormIssue issue = FormIssue.fromKey(key);
        String label = issue != null ? issue.label : key;
        return label.isEmpty() ? label : Character.toUpperCase(label.charAt(0)) + label.substring(1);
    }

    private static void setStat(TextView value, TextView label, String v, @StringRes int labelRes) {
        value.setText(v);
        label.setText(labelRes);
    }

    private static String formatTime(int seconds) {
        return String.format(Locale.US, "%02d:%02d", seconds / 60, seconds % 60);
    }

    @Override
    public void onDestroyView() {
        if (saveError != null) saveError.dismiss();
        super.onDestroyView();
    }
}
