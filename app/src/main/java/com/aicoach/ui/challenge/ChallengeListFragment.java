package com.aicoach.ui.challenge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.databinding.FragmentChallengeListBinding;
import com.aicoach.databinding.ItemLeaderboardRowBinding;
import com.aicoach.data.repository.SocialRepository;
import com.aicoach.ui.history.SegmentGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;

public class ChallengeListFragment extends BaseFragment<FragmentChallengeListBinding> {
    private SocialViewModel vm;
    private SocialRows<SocialRepository.Friend> adapter;
    private androidx.appcompat.app.AlertDialog challengesDialog;
    @Override protected FragmentChallengeListBinding inflate(@NonNull LayoutInflater i, @Nullable ViewGroup c) {
        return FragmentChallengeListBinding.inflate(i, c, false);
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        vm = new ViewModelProvider(this).get(SocialViewModel.class);
        binding.btnCreate.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(ChallengeListFragmentDirections.actionChallengesToCreate()));
        binding.btnInviteFriends.setOnClickListener(v -> openInvite(vm.challenges().get(0).id));
        binding.cardActive.setOnClickListener(v -> {
            List<SocialRepository.ChallengeItem> list = vm.challenges();
            String[] names = new String[list.size()];
            for (int i = 0; i < names.length; i++) names[i] = list.get(i).name;
            challengesDialog = new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.social_choose_challenge).setItems(names,
                            (dialog, index) -> openInvite(list.get(index).id)).show();
        });
        adapter = new SocialRows<>(R.layout.item_leaderboard_row, f -> f.id, (row, f, pos) -> {
            ItemLeaderboardRowBinding b = ItemLeaderboardRowBinding.bind(row);
            b.tvRank.setText(String.valueOf(pos + 1));
            b.tvName.setText(f.name);
            b.tvAvatar.setText(f.name.substring(0, 1));
            b.tvPoints.setText(String.valueOf(f.points));
            b.chipChange.setVisibility(View.GONE);
        });
        binding.rvLeaderboard.setAdapter(adapter);
        SegmentGroup.bind(vm.filter(), index -> { vm.filter(index); render(); }, binding.btnFriends, binding.btnNational);
        vm.state().observe(getViewLifecycleOwner(), ignored -> render());
    }
    private void openInvite(String id) {
        NavHostFragment.findNavController(this).navigate(
                ChallengeListFragmentDirections.actionChallengesToInvite().setChallengeId(id));
    }
    private void render() {
        List<SocialRepository.ChallengeItem> challenges = vm.challenges();
        binding.btnInviteFriends.setEnabled(!challenges.isEmpty());
        binding.cardActive.setEnabled(!challenges.isEmpty());
        binding.tvTodayProgress.setText(R.string.social_demo_notice);
        binding.progressToday.setVisibility(View.GONE);
        binding.rowParticipants.setVisibility(View.GONE);
        if (!challenges.isEmpty()) {
            SocialRepository.ChallengeItem c = challenges.get(0);
            binding.tvChallengeTitle.setText(c.name);
            binding.tvChallengeMeta.setText(getString(R.string.social_active_meta,
                    getString(CreateChallengeFragment.exerciseLabel(c.exercise)), c.target, c.days, c.invited.size(),
                    getString(c.privateGroup ? R.string.social_private : R.string.social_public)));
        } else binding.tvChallengeTitle.setText(R.string.social_challenge_missing);
        List<SocialRepository.Friend> rank = vm.leaderboard();
        adapter.submit(rank);
        TextView[] names = {binding.tvFirstName, binding.tvSecondName, binding.tvThirdName};
        TextView[] avatars = {binding.tvFirstAvatar, binding.tvSecondAvatar, binding.tvThirdAvatar};
        TextView[] points = {binding.tvFirstPoints, binding.tvSecondPoints, binding.tvThirdPoints};
        for (int i = 0; i < 3; i++) {
            names[i].setText(i < rank.size() ? rank.get(i).name : "—");
            avatars[i].setText(i < rank.size() ? rank.get(i).name.substring(0, 1) : "—");
            points[i].setText(i < rank.size() ? String.valueOf(rank.get(i).points) : "—");
        }
    }
    @Override public void onDestroyView() {
        if (challengesDialog != null) { challengesDialog.dismiss(); challengesDialog = null; }
        binding.rvLeaderboard.setAdapter(null);
        super.onDestroyView();
    }
}
