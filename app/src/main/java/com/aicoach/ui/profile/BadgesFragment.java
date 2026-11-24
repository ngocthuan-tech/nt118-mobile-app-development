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
import java.util.ArrayList;
import java.util.List;
import com.aicoach.domain.BadgeRules;

public class BadgesFragment extends BaseFragment<FragmentBadgesBinding> {
    private ProfileViewModel profile;
    private AchievementsViewModel achievements;
    private BadgeAdapter badges;
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
        badges = new BadgeAdapter();
        binding.rvBadges.setAdapter(badges);
        ranks = new SocialRows<>(R.layout.item_exercise_rank, AchievementSummary.ExerciseProgress::getExerciseId, (row, e, index) -> {
            ItemExerciseRankBinding b = ItemExerciseRankBinding.bind(row);
            long xp = e.getTotalXp();
            boolean earned = AchievementRules.earnedExerciseBadge(xp);
            b.imgRankFrame.setImageResource(earned ? R.drawable.rank_master : R.drawable.ic_lock_24);
            ExerciseImages.bind(b.imgRankExercise, e.getImageName());
            b.imgRankExercise.setVisibility(earned ? View.VISIBLE : View.GONE);
            b.tvExerciseName.setText(getString(R.string.achievement_master_badge, e.getExerciseName()));
            b.tvExerciseRank.setText(earned ? R.string.achievement_unlocked : R.string.achievement_locked);
            long target = AchievementRules.MASTER_BADGE_XP;
            long progress = Math.min(xp, target);
            b.progressRank.setProgress((int) (100 * progress / target));
            b.tvRankNext.setText(getString(R.string.achievement_badge_progress,
                    AchievementArt.number(progress), AchievementArt.number(target)));
        });
        binding.rvExerciseRanks.setAdapter(ranks);
        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.btnRankRules.setOnClickListener(v -> openLadder());
        SegmentGroup.bind(filter, index -> { filter = index; render(); }, binding.btnAll, binding.btnEarned, binding.btnLocked);
        binding.btnRetryBadges.setOnClickListener(v -> { profile.refresh(); achievements.refresh(); });
        profile.getState().observe(getViewLifecycleOwner(), result -> render());
        achievements.getState().observe(getViewLifecycleOwner(), result -> render());
    }
    private void openLadder() {
        Long xp = accountXp();
        if (xp == null) return;
        if (getChildFragmentManager().findFragmentByTag("rank-ladder") == null)
            RankLadderDialog.create(xp).show(getChildFragmentManager(), "rank-ladder");
    }
    private Long accountXp() {
        if (achievements.getLast() != null) return achievements.getLast().getTotalXp();
        if (profile.getLast() != null) return profile.getLast().getTotalXp();
        return null;
    }
    private boolean matchesFilter(boolean earned) {
        return filter == 0 || (filter == 1) == earned;
    }
    private void render() {
        Resource<?> profileState = profile.getState().getValue(), rankState = achievements.getState().getValue();
        boolean loading = is(profileState, Resource.Status.LOADING) || is(rankState, Resource.Status.LOADING);
        boolean rankError = is(rankState, Resource.Status.ERROR);
        AchievementSummary summary = achievements.getLast();
        Long xp = accountXp();
        binding.btnRankRules.setEnabled(xp != null);
        boolean profileError = is(profileState, Resource.Status.ERROR);
        binding.loadingBadges.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnRetryBadges.setEnabled(!loading);
        binding.btnRetryBadges.setText(rankError || profileError ? R.string.profile_retry : R.string.profile_refresh);
        binding.tvBadgeError.setVisibility(profileError ? View.VISIBLE : View.GONE);
        binding.tvBadgeError.setText(profileError ? profileState.message : null);
        binding.imgAccountRank.setVisibility(xp == null ? View.GONE : View.VISIBLE);
        binding.progressBadges.setVisibility(xp == null ? View.GONE : View.VISIBLE);
        if (xp == null) {
            binding.tvSummary.setText(R.string.profile_missing);
            binding.tvTotalAchievementXp.setText("");
            binding.tvNextHint.setText(loading ? R.string.profile_badge_loading : R.string.achievement_account_rank);
        } else {
            AchievementRules.Progress p = AchievementRules.rank(xp);
            AchievementArt.bind(binding.imgAccountRank, p.current);
            binding.tvSummary.setText(p.current.name);
            binding.tvTotalAchievementXp.setText(getString(R.string.achievement_total, AchievementArt.number(p.xp)));
            binding.progressBadges.setProgress(p.percent);
            binding.tvNextHint.setText(AchievementArt.next(requireContext(), p));
        }
        List<BadgeRules.Badge> shown = new ArrayList<>();
        if (profile.getLast() != null) {
            for (BadgeRules.Badge badge : BadgeRules.evaluate(profile.getLast().getTotalReps(), profile.getLast().getStreak())) {
                if (matchesFilter(badge.earned())) shown.add(badge);
            }
        }
        badges.submitList(shown);
        binding.tvBadgeEmpty.setVisibility(profile.getLast() != null && shown.isEmpty() ? View.VISIBLE : View.GONE);
        List<AchievementSummary.ExerciseProgress> exercises = new ArrayList<>();
        if (summary != null) {
            for (AchievementSummary.ExerciseProgress exercise : summary.getExercises()) {
                if (matchesFilter(AchievementRules.earnedExerciseBadge(exercise.getTotalXp()))) exercises.add(exercise);
            }
        }
        ranks.submit(exercises);
        boolean statusVisible = rankError || exercises.isEmpty();
        binding.tvRankStatus.setVisibility(statusVisible ? View.VISIBLE : View.GONE);
        binding.tvRankStatus.setText(rankError ? (summary == null ? R.string.achievement_rank_unavailable : R.string.achievement_stale)
                : summary == null ? R.string.profile_badge_loading : (summary.getExercises().isEmpty() ? R.string.achievement_empty : R.string.profile_badge_empty));
    }
    private static boolean is(Resource<?> r, Resource.Status s) { return r != null && r.status == s; }
    @Override public void onSaveInstanceState(@NonNull Bundle state) { super.onSaveInstanceState(state); state.putInt("filter", filter); }
    @Override public void onDestroyView() {
        binding.rvBadges.setAdapter(null); binding.rvExerciseRanks.setAdapter(null); super.onDestroyView();
    }
}
