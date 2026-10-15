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

        bindDemoData();
    }

    /** Dữ liệu mẫu giống Figma 36. */
    private void bindDemoData() {
        binding.tvShareDate.setText("10/10/2026");
        binding.tvShareHeadline.setText(getString(R.string.share_today_did, "squat"));
        binding.tvShareReps.setText(getString(R.string.share_reps, 45));
        binding.tvShareScore.setText("92");
        binding.tvShareTime.setText("08:24");
        binding.tvShareStreak.setText(getString(R.string.share_streak_days, 7));
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
