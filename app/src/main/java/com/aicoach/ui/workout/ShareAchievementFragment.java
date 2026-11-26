package com.aicoach.ui.workout;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.databinding.FragmentShareAchievementBinding;
import com.aicoach.databinding.ViewShareActionBinding;
import com.aicoach.pose.WorkoutSummary;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Figma 36 "Chia sẻ thành tích" (Trưởng nhóm).
 * Hiện chia sẻ bằng chữ qua bảng chia sẻ của Android. Về sau: chụp cardShare thành ảnh
 * (View → Bitmap → FileProvider) để chia sẻ/lưu ảnh.
 */
public class ShareAchievementFragment extends BaseFragment<FragmentShareAchievementBinding> {

    @Override
    protected FragmentShareAchievementBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentShareAchievementBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnClose.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        binding.switchShowStats.setOnCheckedChangeListener((b, checked) ->
                binding.rowShareStats.setVisibility(checked ? View.VISIBLE : View.GONE));

        bindAction(binding.actionSend, R.drawable.ic_send_24, R.string.share_send_friends, true);
        bindAction(binding.actionSave, R.drawable.ic_download_24, R.string.share_save_image, false);
        bindAction(binding.actionCopyLink, R.drawable.ic_link_24, R.string.share_copy_link, false);
        bindAction(binding.actionMore, R.drawable.ic_share_24, R.string.share_more, false);

        View.OnClickListener shareText = v -> shareAsText();
        binding.actionSend.getRoot().setOnClickListener(shareText);
        binding.actionMore.getRoot().setOnClickListener(shareText);

        bindSession();
    }

    /** Số liệu lượt vừa tập (dùng chung WorkoutSessionViewModel với màn Kết quả). */
    private void bindSession() {
        WorkoutSessionViewModel vm = WorkoutSessionViewModel.of(this);
        List<WorkoutSessionViewModel.Entry> entries = vm.entries();

        binding.tvShareDate.setText(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.US)));
        if (entries.isEmpty()) {
            binding.tvShareHeadline.setText(R.string.share_footer);
            binding.tvShareReps.setText("—");
            binding.tvShareScore.setText("—");
            binding.tvShareTime.setText("—");
        } else {
            int attempts = 0, correct = 0, seconds = 0;
            for (WorkoutSessionViewModel.Entry e : entries) {
                attempts += e.summary.totalReps;
                correct += e.summary.correctReps;
                seconds += e.summary.durationSec;
            }
            WorkoutSummary s = entries.get(0).summary;
            if (entries.size() == 1) {
                binding.tvShareHeadline.setText(getString(R.string.share_today_did,
                        getString(R.string.ai_share_did, s.exerciseName.toLowerCase(new Locale("vi", "VN")))));
                binding.tvShareReps.setText(s.timed ? getString(R.string.ai_share_seconds, s.correctReps)
                        : getString(R.string.share_reps, s.correctReps));
            } else {
                String count = getString(R.string.ai_share_exercises, entries.size());
                binding.tvShareHeadline.setText(getString(R.string.share_today_did,
                        getString(R.string.ai_share_did, count)));
                binding.tvShareReps.setText(count);
            }
            binding.tvShareScore.setText(String.valueOf(attempts == 0 ? 0 : Math.round(correct * 100f / attempts)));
            binding.tvShareTime.setText(String.format(Locale.US, "%02d:%02d", seconds / 60, seconds % 60));
        }
        binding.tvShareStreak.setText("—");
        vm.streak().observe(getViewLifecycleOwner(), streak -> {
            if (streak != null && binding != null) {
                binding.tvShareStreak.setText(getString(R.string.share_streak_days, streak));
            }
        });
    }

    private void bindAction(ViewShareActionBinding action, @DrawableRes int icon, @StringRes int label,
                            boolean highlighted) {
        action.imgAction.setImageResource(icon);
        action.tvAction.setText(label);
        if (highlighted) {
            action.actionCircle.setBackgroundResource(R.drawable.bg_fab_train);
            action.actionCircle.setBackgroundTintList(null);
        } else {
            action.actionCircle.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white_10)));
        }
    }

    private void shareAsText() {
        String text = binding.tvShareHeadline.getText() + " " + binding.tvShareReps.getText()
                + " — " + getString(R.string.share_footer);
        Intent send = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(send, getString(R.string.share_title)));
    }
}
