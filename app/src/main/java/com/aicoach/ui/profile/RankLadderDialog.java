package com.aicoach.ui.profile;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import com.aicoach.R;
import com.aicoach.databinding.ItemExerciseRankBinding;
import com.aicoach.domain.AchievementRules;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class RankLadderDialog extends DialogFragment {
    static RankLadderDialog create(long xp) {
        RankLadderDialog dialog = new RankLadderDialog();
        Bundle args = new Bundle(); args.putLong("xp", xp);
        dialog.setArguments(args); return dialog;
    }
    @NonNull @Override public Dialog onCreateDialog(Bundle state) {
        Bundle args = requireArguments();
        long xp = args.getLong("xp");
        ScrollView scroll = new ScrollView(requireContext());
        LinearLayout list = new LinearLayout(requireContext()); list.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        list.setPadding(pad, pad, pad, pad); scroll.addView(list);
        for (AchievementRules.Tier tier : AchievementRules.RANKS) {
            ItemExerciseRankBinding b = ItemExerciseRankBinding.inflate(getLayoutInflater(), list, false);
            AchievementArt.bind(b.imgRankFrame, tier);
            b.tvExerciseName.setText(tier.name);
            b.tvExerciseRank.setText(getString(R.string.achievement_threshold, AchievementArt.number(tier.minimumXp)));
            b.progressRank.setVisibility(View.GONE);
            b.tvRankNext.setVisibility(View.VISIBLE);
            b.tvRankNext.setText(AchievementRules.rank(xp).current == tier ? R.string.achievement_current
                    : xp >= tier.minimumXp ? R.string.achievement_unlocked : R.string.achievement_locked);
            b.imgRankExercise.setVisibility(View.GONE);
            list.addView(b.getRoot());
        }
        android.widget.TextView note = new android.widget.TextView(requireContext());
        note.setText(R.string.achievement_rules_body); note.setPadding(pad, pad, pad, pad); list.addView(note);
        return new MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.achievement_rank_rules)
                .setView(scroll).setPositiveButton(R.string.achievement_close, null).create();
    }
}
