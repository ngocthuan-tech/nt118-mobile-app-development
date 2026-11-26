package com.aicoach.ui.challenge;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.common.Resource;
import com.aicoach.data.model.Challenge;
import com.aicoach.data.model.Social;
import com.aicoach.databinding.FragmentChallengeListBinding;
import com.aicoach.databinding.ItemLeaderboardRowBinding;
import com.aicoach.ui.history.SegmentGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import java.util.List;

/** 40 Thử thách & Xếp hạng: thẻ thử thách đang theo (tiến độ hôm nay từ buổi tập AI) + bảng XP bạn bè / toàn quốc. */
public class ChallengeListFragment extends BaseFragment<FragmentChallengeListBinding> {
    private SocialViewModel vm;
    private SocialRows<Social.LeaderboardEntry> adapter;
    private AlertDialog dialog;

    @Override protected FragmentChallengeListBinding inflate(@NonNull LayoutInflater i, @Nullable ViewGroup c) {
        return FragmentChallengeListBinding.inflate(i, c, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        vm = new ViewModelProvider(this).get(SocialViewModel.class);
        binding.btnCreate.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(ChallengeListFragmentDirections.actionChallengesToCreate()));
        binding.btnInviteFriends.setOnClickListener(v -> {
            Challenge c = vm.shown();
            if (c != null) openInvite(c.getId());
        });
        binding.cardActive.setOnClickListener(v -> chooseChallenge());
        adapter = new SocialRows<>(R.layout.item_leaderboard_row, e -> String.valueOf(e.getUserId()), (row, e, pos) -> {
            ItemLeaderboardRowBinding b = ItemLeaderboardRowBinding.bind(row);
            b.tvRank.setText(String.valueOf(e.getRank()));
            b.tvName.setText(e.isMe() ? row.getContext().getString(R.string.social_me, e.getDisplayName())
                    : e.getDisplayName());
            b.tvAvatar.setText(SocialFormat.initial(e.getDisplayName()));
            b.tvPoints.setText(String.valueOf(e.getPoints()));
            b.chipChange.setVisibility(View.GONE);
        });
        binding.rvLeaderboard.setAdapter(adapter);
        SegmentGroup.bind(vm.filter(), index -> {
            vm.filter(index);
            renderBoard();
            vm.loadLeaderboard();
        }, binding.btnFriends, binding.btnNational);

        vm.challengesState().observe(getViewLifecycleOwner(), r -> {
            if (r == null) return;
            if (r.status == Resource.Status.ERROR) showError(r.message);
            renderChallenge(r.status == Resource.Status.LOADING);
        });
        vm.leaderboardState().observe(getViewLifecycleOwner(), r -> {
            if (r == null) return;
            if (r.status == Resource.Status.ERROR) showError(r.message);
            renderBoard();
        });
        vm.joinState().observe(getViewLifecycleOwner(), this::onJoined);
        // Mỗi lần mở tab đều tải lại: vừa tập xong, tạo hoặc mời xong quay về là thấy số mới.
        vm.loadChallenges();
        vm.loadLeaderboard();
    }

    private void openInvite(String id) {
        NavHostFragment.findNavController(this).navigate(
                ChallengeListFragmentDirections.actionChallengesToInvite().setChallengeId(id));
    }

    // ---------------------------------------------------------------- Thẻ thử thách

    private void renderChallenge(boolean loading) {
        if (binding == null) return;
        Challenge c = vm.shown();
        binding.btnInviteFriends.setEnabled(c != null && !c.isEnded());
        if (c == null) {
            binding.tvChallengeTitle.setText(loading ? R.string.social_loading : R.string.social_no_challenge_title);
            binding.tvChallengeMeta.setText(loading ? "" : getString(R.string.social_no_challenge_meta));
            binding.tvTodayProgress.setVisibility(View.GONE);
            binding.progressToday.setVisibility(View.GONE);
            binding.rowParticipants.setVisibility(View.GONE);
            return;
        }
        String unit = getString(c.isTimed() ? R.string.social_unit_seconds : R.string.social_unit_reps);
        String remain = c.isEnded() ? getString(R.string.social_ended)
                : getString(R.string.social_days_left, c.getDaysLeft());
        binding.tvChallengeTitle.setText(c.getTitle());
        binding.tvChallengeMeta.setText(getString(R.string.social_challenge_meta, exerciseName(c),
                c.getDailyTarget(), unit, c.getMemberCount(), remain));
        binding.tvTodayProgress.setVisibility(View.VISIBLE);
        binding.tvTodayProgress.setText(getString(R.string.social_today_progress, c.getMyTodayReps(),
                c.getDailyTarget(), unit, c.getMyCompletedDays(), c.getDays()));
        binding.progressToday.setVisibility(View.VISIBLE);
        binding.progressToday.setMax(100);
        binding.progressToday.setProgressCompat(SocialFormat.percent(c.getMyTodayReps(), c.getDailyTarget()), false);

        List<String> letters = SocialFormat.avatars(c, 3);
        TextView[] avatars = {binding.avatar1, binding.avatar2, binding.avatar3};
        for (int i = 0; i < avatars.length; i++) {
            avatars[i].setVisibility(i < letters.size() ? View.VISIBLE : View.GONE);
            if (i < letters.size()) avatars[i].setText(letters.get(i));
        }
        int more = SocialFormat.moreMembers(c, letters.size());
        binding.avatarMore.setVisibility(more > 0 ? View.VISIBLE : View.GONE);
        binding.avatarMore.setText(getString(R.string.social_more_members, more));
        binding.rowParticipants.setVisibility(letters.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private String exerciseName(Challenge c) {
        String name = c.getExerciseName();
        return name != null && !name.isEmpty() ? name : getString(CreateChallengeFragment.exerciseLabel(c.getExerciseId()));
    }

    /** Chạm thẻ: chọn thử thách khác để xem, hoặc nhập mã mời của bạn bè. */
    private void chooseChallenge() {
        List<Challenge> list = vm.challengeList();
        if (list.isEmpty()) {
            askCode();
            return;
        }
        String[] names = new String[list.size()];
        for (int i = 0; i < names.length; i++) {
            Challenge c = list.get(i);
            names[i] = c.isEnded() ? c.getTitle() + " (" + getString(R.string.social_ended) + ")" : c.getTitle();
        }
        dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.social_choose_challenge)
                .setItems(names, (d, index) -> {
                    vm.show(list.get(index).getId());
                    renderChallenge(false);
                })
                .setNeutralButton(R.string.social_enter_code, (d, which) -> askCode())
                .show();
    }

    private void askCode() {
        if (binding == null) return;
        EditText input = new EditText(requireContext());
        input.setHint(R.string.social_join_hint);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        FrameLayout box = new FrameLayout(requireContext());
        int pad = getResources().getDimensionPixelSize(R.dimen.space_16);
        box.setPadding(pad, pad / 2, pad, 0);
        box.addView(input);
        AlertDialog codeDialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.social_join_title)
                .setView(box)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.social_join, null)
                .create();
        // Tự gắn sự kiện nút để mã sai thì báo lỗi ngay trong hộp thoại thay vì đóng hộp thoại.
        codeDialog.setOnShowListener(d -> codeDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (vm.join(input.getText().toString())) codeDialog.dismiss();
            else input.setError(getString(R.string.social_code_invalid));
        }));
        dialog = codeDialog;
        codeDialog.show();
    }

    private void onJoined(@Nullable Resource<Challenge> r) {
        if (r == null || r.status == Resource.Status.LOADING) return;
        if (r.status == Resource.Status.SUCCESS && r.data != null) {
            Snackbar.make(binding.getRoot(), getString(R.string.social_joined, r.data.getTitle()),
                    Snackbar.LENGTH_LONG).show();
        } else {
            showError(r.message);
        }
        vm.consumeJoin();
    }

    // ---------------------------------------------------------------- Bảng xếp hạng

    private void renderBoard() {
        if (binding == null) return;
        List<Social.LeaderboardEntry> rows = vm.leaderboardRows();
        adapter.submit(rows);
        TextView[] names = {binding.tvFirstName, binding.tvSecondName, binding.tvThirdName};
        TextView[] avatars = {binding.tvFirstAvatar, binding.tvSecondAvatar, binding.tvThirdAvatar};
        TextView[] points = {binding.tvFirstPoints, binding.tvSecondPoints, binding.tvThirdPoints};
        for (int i = 0; i < 3; i++) {
            Social.LeaderboardEntry e = i < rows.size() ? rows.get(i) : null;
            names[i].setText(e != null ? e.getDisplayName() : "—");
            avatars[i].setText(e != null ? SocialFormat.initial(e.getDisplayName()) : "—");
            points[i].setText(e != null ? String.valueOf(e.getPoints()) : "—");
        }
    }

    @Override public void onDestroyView() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        binding.rvLeaderboard.setAdapter(null);
        super.onDestroyView();
    }
}
