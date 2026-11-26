package com.aicoach.ui.challenge;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.common.Resource;
import com.aicoach.data.model.Challenge;
import com.aicoach.data.model.Social;
import com.aicoach.databinding.FragmentInviteFriendsBinding;
import com.aicoach.databinding.ItemFriendInviteBinding;
import com.aicoach.util.SimpleTextWatcher;
import com.google.android.material.snackbar.Snackbar;
import java.util.List;

/**
 * 42 Mời bạn bè: mã mời (thử thách công khai) + danh sách người mời được — bạn bè lọc theo tên,
 * hoặc gõ đúng email để mời người chưa là bạn. Gửi xong người được mời nhận thông báo Tham gia / Từ chối.
 */
public class InviteFriendsFragment extends BaseFragment<FragmentInviteFriendsBinding> {
    private static final long SEARCH_DELAY_MS = 350;

    private SocialViewModel vm;
    private String challengeId;
    private SocialRows<Social.Candidate> adapter;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable search = () -> {
        if (binding != null && vm != null) {
            vm.searchCandidates(challengeId, query());
        }
    };

    @Override protected FragmentInviteFriendsBinding inflate(@NonNull LayoutInflater i, @Nullable ViewGroup c) {
        return FragmentInviteFriendsBinding.inflate(i, c, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        vm = new ViewModelProvider(this).get(SocialViewModel.class);
        challengeId = InviteFriendsFragmentArgs.fromBundle(requireArguments()).getChallengeId();
        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        // Chưa có link mời / mã QR phía server → ẩn, chỉ giữ Sao chép mã và Gửi qua tin nhắn.
        binding.btnQr.setVisibility(View.GONE);
        binding.btnQrCode.setVisibility(View.GONE);
        binding.btnCopyLink.setVisibility(View.GONE);
        if (challengeId == null || challengeId.isEmpty()) {
            showMissing();
            return;
        }
        showCode(false);
        binding.btnCopyCode.setOnClickListener(v -> copyCode());
        binding.btnMessage.setOnClickListener(v -> shareCode());

        adapter = new SocialRows<>(R.layout.item_friend_invite, Social.Candidate::getUserId, (row, c, pos) -> {
            ItemFriendInviteBinding b = ItemFriendInviteBinding.bind(row);
            b.tvName.setText(c.getDisplayName());
            b.tvAvatar.setText(SocialFormat.initial(c.getDisplayName()));
            b.tvSubtitle.setText(subtitle(row.getContext(), c));
            b.cbInvite.setOnCheckedChangeListener(null);
            b.cbInvite.setChecked(!c.canInvite() || vm.selected.contains(c.getUserId()));
            b.cbInvite.setEnabled(c.canInvite() && canInvite() && !vm.inviting());
            b.cbInvite.setOnCheckedChangeListener((button, on) -> {
                vm.select(c.getUserId(), on);
                counts();
            });
            b.getRoot().setOnClickListener(v -> {
                if (b.cbInvite.isEnabled()) b.cbInvite.toggle();
            });
        });
        binding.rvFriends.setAdapter(adapter);
        binding.etSearch.addTextChangedListener(SimpleTextWatcher.after(() -> {
            handler.removeCallbacks(search);
            handler.postDelayed(search, SEARCH_DELAY_MS);
        }));
        binding.btnSend.setOnClickListener(v -> {
            if (vm.selected.size() > SocialFormat.MAX_INVITES_PER_SEND) {
                showError(getString(R.string.social_invite_limit));
            } else if (vm.invite(challengeId)) {
                counts();
            }
        });

        vm.challengeState().observe(getViewLifecycleOwner(), r -> {
            if (r == null || r.status == Resource.Status.LOADING) return;
            if (r.status == Resource.Status.ERROR) {
                if (vm.challengeData() == null) showMissing();
                showError(r.message);
                return;
            }
            renderChallenge();
        });
        vm.candidatesState().observe(getViewLifecycleOwner(), r -> {
            if (r == null) return;
            if (r.status == Resource.Status.ERROR) showError(r.message);
            renderCandidates(r.status == Resource.Status.LOADING);
        });
        vm.inviteState().observe(getViewLifecycleOwner(), this::onInvited);
        vm.loadChallenge(challengeId);
        vm.searchCandidates(challengeId, query());
        counts();
    }

    // ---------------------------------------------------------------- Thử thách + mã mời

    private void renderChallenge() {
        Challenge c = vm.challengeData();
        if (binding == null || c == null) return;
        String unit = getString(c.isTimed() ? R.string.social_unit_seconds : R.string.social_unit_reps);
        String name = c.getExerciseName() != null ? c.getExerciseName()
                : getString(CreateChallengeFragment.exerciseLabel(c.getExerciseId()));
        binding.tvChallengeTitle.setText(c.getTitle());
        if (c.isEnded()) {
            binding.tvChallengeMeta.setText(R.string.social_challenge_ended);
        } else {
            String meta = getString(R.string.social_invite_meta, name, c.getDailyTarget(), unit, c.getMemberCount());
            binding.tvChallengeMeta.setText(c.hasInviteCode() ? meta
                    : meta + "\n" + getString(R.string.social_private_no_code));
        }
        boolean code = c.hasInviteCode() && !c.isEnded();
        showCode(code);
        if (code) binding.tvCode.setText(c.getInviteCode());
        binding.etSearch.setEnabled(!c.isEnded());
        counts();
        if (adapter != null) adapter.notifyItemRangeChanged(0, adapter.getItemCount());
    }

    private void showCode(boolean visible) {
        int v = visible ? View.VISIBLE : View.GONE;
        binding.tvCodeLabel.setVisibility(v);
        binding.boxCode.setVisibility(v);
        binding.rowShare.setVisibility(v);
    }

    private void showMissing() {
        binding.btnSend.setEnabled(false);
        binding.etSearch.setEnabled(false);
        showCode(false);
        binding.tvChallengeTitle.setText(R.string.social_challenge_missing);
        binding.tvChallengeMeta.setText("");
    }

    private String query() {
        CharSequence text = binding.etSearch.getText();
        return text == null ? "" : text.toString();
    }

    private boolean canInvite() {
        Challenge c = vm.challengeData();
        return c != null && !c.isEnded();
    }

    private void copyCode() {
        Challenge c = vm.challengeData();
        if (c == null || !c.hasInviteCode()) return;
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.social_invite_code), c.getInviteCode()));
        }
        Snackbar.make(binding.getRoot(), R.string.social_code_copied, Snackbar.LENGTH_SHORT).show();
    }

    private void shareCode() {
        Challenge c = vm.challengeData();
        if (c == null || !c.hasInviteCode()) return;
        String unit = getString(c.isTimed() ? R.string.social_unit_seconds : R.string.social_unit_reps);
        Intent send = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, getString(R.string.social_share_text, c.getTitle(),
                        c.getDailyTarget(), unit, c.getDays(), c.getInviteCode()));
        try {
            startActivity(Intent.createChooser(send, getString(R.string.social_share_chooser)));
        } catch (ActivityNotFoundException e) {
            copyCode();
        }
    }

    // ---------------------------------------------------------------- Danh sách người mời được

    private static String subtitle(Context context, Social.Candidate c) {
        switch (c.getStatus()) {
            case Social.Candidate.MEMBER:
                return context.getString(R.string.social_candidate_member);
            case Social.Candidate.INVITED:
                return context.getString(R.string.social_candidate_invited);
            default:
                return c.isFriend() ? context.getString(R.string.social_candidate_friend, c.getStreak())
                        : context.getString(R.string.social_candidate_email);
        }
    }

    private void renderCandidates(boolean loading) {
        if (binding == null || adapter == null) return;
        List<Social.Candidate> list = vm.candidateList();
        adapter.submit(list);
        binding.tvFriendsCount.setText(getString(R.string.social_found_count, list.size()));
        binding.tvFriendsEmpty.setVisibility(list.isEmpty() && !loading ? View.VISIBLE : View.GONE);
        counts();
    }

    private void counts() {
        if (binding == null) return;
        boolean busy = vm.inviting();
        binding.tvSelected.setText(getString(R.string.social_selected_count, vm.selected.size()));
        binding.btnSend.setEnabled(!vm.selected.isEmpty() && canInvite() && !busy);
        binding.btnSend.setText(busy ? R.string.social_sending : R.string.social_send_invites);
    }

    private void onInvited(@Nullable Resource<Social.InviteResult> r) {
        if (r == null) return;
        if (r.status == Resource.Status.LOADING) {
            counts();
            if (adapter != null) adapter.notifyItemRangeChanged(0, adapter.getItemCount());
            return;
        }
        vm.consumeInvite();
        if (r.status == Resource.Status.SUCCESS && r.data != null) {
            int sent = r.data.getInvited();
            Snackbar.make(binding.getRoot(), sent > 0 ? getString(R.string.social_invites_sent, sent)
                    : getString(R.string.social_invites_none), Snackbar.LENGTH_LONG).show();
        } else {
            showError(r.message);
        }
        counts();
        if (adapter != null) adapter.notifyItemRangeChanged(0, adapter.getItemCount());
    }

    @Override public void onDestroyView() {
        handler.removeCallbacks(search);
        binding.rvFriends.setAdapter(null);
        adapter = null;
        super.onDestroyView();
    }
}
