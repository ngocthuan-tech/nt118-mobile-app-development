package com.aicoach.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.common.ExerciseImages;
import com.aicoach.common.Resource;
import com.aicoach.data.model.AchievementSummary;
import com.aicoach.databinding.FragmentBadgesBinding;
import com.aicoach.databinding.ItemExerciseRankBinding;
import com.aicoach.domain.AchievementRules;
import com.aicoach.ui.challenge.SocialRows;
import com.aicoach.ui.history.SegmentGroup;
import java.util.Collections;

public class BadgesFragment extends BaseFragment<FragmentBadgesBinding> {
    private ProfileViewModel profile;
    private AchievementsViewModel achievements;
    private RealmAdapter realms;
    private SocialRows<AchievementSummary.ExerciseProgress> ranks;
    private int filter;
    @Override protected FragmentBadgesBinding inflate(@NonNull LayoutInflater i, @Nullable ViewGroup c) {
        return FragmentBadgesBinding.inflate(i, c, false);
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        filter = state == null ? 0 : state.getInt("filter");
        profile = new ViewModelProvider(this).get(ProfileViewModel.class);
        achievements = new ViewModelProvider(this).get(AchievementsViewModel.class);
        realms = new RealmAdapter(); binding.rvBadges.setAdapter(realms);
        ranks = new SocialRows<>(R.layout.item_exercise_rank, AchievementSummary.ExerciseProgress::getExerciseId, (row, e, index) -> {
            ItemExerciseRankBinding b = ItemExerciseRankBinding.bind(row);
            AchievementRules.Progress p = AchievementRules.rank(e.getTotalXp());
            AchievementArt.bind(b.imgRankFrame, p.current);
            ExerciseImages.bind(b.imgRankExercise, e.getImageName());
            b.imgRankExercise.setImageTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));
            b.tvExerciseName.setText(e.getExerciseName());
            b.tvExerciseRank.setText(getString(R.string.achievement_rank_xp, AchievementArt.number(p.xp), p.current.name));
            b.progressRank.setProgress(p.percent);
            b.tvRankNext.setText(p.xp == 0 ? getString(R.string.achievement_no_workout) : AchievementArt.next(requireContext(), p));
            row.setOnClickListener(v -> openLadder(e.getImageName(), e.getExerciseName(), e.getTotalXp()));
            row.setFocusable(true);
        });
        binding.rvExerciseRanks.setAdapter(ranks);
        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.btnRankRules.setOnClickListener(v -> openLadder(null, getString(R.string.achievement_rank_rules), 0));
        SegmentGroup.bind(filter, index -> { filter = index; render(); }, binding.btnAll, binding.btnEarned, binding.btnLocked);
        binding.btnRetryBadges.setOnClickListener(v -> { profile.refresh(); achievements.refresh(); });
        profile.getState().observe(getViewLifecycleOwner(), result -> render());
        achievements.getState().observe(getViewLifecycleOwner(), result -> render());
    }
    private void openLadder(String id, String name, long xp) {
        if (getChildFragmentManager().findFragmentByTag("rank-ladder") == null)
            RankLadderDialog.create(id, name, xp).show(getChildFragmentManager(), "rank-ladder");
    }
    private void render() {
        Resource<?> profileState = profile.getState().getValue(), rankState = achievements.getState().getValue();
        boolean loading = is(profileState, Resource.Status.LOADING) || is(rankState, Resource.Status.LOADING);
        boolean rankError = is(rankState, Resource.Status.ERROR);
        AchievementSummary summary = achievements.getLast();
        Long xp = null;
        if (summary != null) xp = summary.getTotalXp();
        else if (profile.getLast() != null) xp = profile.getLast().getTotalXp();
        boolean realmError = xp == null && is(profileState, Resource.Status.ERROR);
        binding.loadingBadges.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnRetryBadges.setEnabled(!loading);
        binding.btnRetryBadges.setText(rankError || realmError ? R.string.profile_retry : R.string.profile_refresh);
        binding.tvBadgeError.setVisibility(realmError ? View.VISIBLE : View.GONE);
        binding.tvBadgeError.setText(realmError ? profileState.message : null);
        binding.imgRealm.setVisibility(xp == null ? View.GONE : View.VISIBLE);
        binding.progressBadges.setVisibility(xp == null ? View.GONE : View.VISIBLE);
        if (xp == null) {
            binding.tvSummary.setText(R.string.profile_missing);
            binding.tvTotalAchievementXp.setText("");
            binding.tvNextHint.setText(loading ? R.string.profile_badge_loading : R.string.achievement_realm_caption);
        } else {
            AchievementRules.Progress p = AchievementRules.realm(xp);
            AchievementArt.bind(binding.imgRealm, p.current);
            binding.tvSummary.setText(p.current.name);
            binding.tvTotalAchievementXp.setText(getString(R.string.achievement_total, AchievementArt.number(p.xp)));
            binding.progressBadges.setProgress(p.percent);
            binding.tvNextHint.setText(AchievementArt.next(requireContext(), p));
        }
        realms.submit(xp, filter);
        binding.tvBadgeEmpty.setVisibility(xp != null && realms.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        ranks.submit(summary == null ? Collections.emptyList() : summary.getExercises());
        boolean statusVisible = rankError || summary == null || summary.getExercises().isEmpty();
        binding.tvRankStatus.setVisibility(statusVisible ? View.VISIBLE : View.GONE);
        binding.tvRankStatus.setText(rankError ? (summary == null ? R.string.achievement_rank_unavailable : R.string.achievement_stale)
                : summary == null ? R.string.profile_badge_loading : R.string.achievement_empty);
    }
    private static boolean is(Resource<?> r, Resource.Status s) { return r != null && r.status == s; }
    @Override public void onSaveInstanceState(@NonNull Bundle state) { super.onSaveInstanceState(state); state.putInt("filter", filter); }
    @Override public void onDestroyView() {
        binding.rvBadges.setAdapter(null); binding.rvExerciseRanks.setAdapter(null); super.onDestroyView();
    }
}
