package com.aicoach.ui.profile;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import com.aicoach.R;
import com.aicoach.common.ExerciseImages;
import com.aicoach.databinding.ItemExerciseRankBinding;
import com.aicoach.domain.AchievementRules;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class RankLadderDialog extends DialogFragment {
    static RankLadderDialog create(String exerciseId, String name, long xp) {
        RankLadderDialog dialog = new RankLadderDialog();
        Bundle args = new Bundle(); args.putString("exercise", exerciseId); args.putString("name", name); args.putLong("xp", xp);
        dialog.setArguments(args); return dialog;
    }
    @NonNull @Override public Dialog onCreateDialog(Bundle state) {
        Bundle args = requireArguments();
        String exercise = args.getString("exercise");
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
            b.tvRankNext.setVisibility(exercise == null ? View.GONE : View.VISIBLE);
            b.tvRankNext.setText(AchievementRules.rank(xp).current == tier ? R.string.achievement_current
                    : xp >= tier.minimumXp ? R.string.achievement_unlocked : R.string.achievement_locked);
            if (exercise == null) b.imgRankExercise.setVisibility(View.GONE);
            else {
                ExerciseImages.bind(b.imgRankExercise, exercise);
                b.imgRankExercise.setImageTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));
            }
            list.addView(b.getRoot());
        }
        android.widget.TextView note = new android.widget.TextView(requireContext());
        note.setText(R.string.achievement_rules_body); note.setPadding(pad, pad, pad, pad); list.addView(note);
        return new MaterialAlertDialogBuilder(requireContext()).setTitle(args.getString("name", getString(R.string.achievement_rank_rules)))
                .setView(scroll).setPositiveButton(R.string.achievement_close, null).create();
    }
}
