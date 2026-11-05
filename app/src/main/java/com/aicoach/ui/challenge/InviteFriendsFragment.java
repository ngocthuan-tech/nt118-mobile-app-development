package com.aicoach.ui.challenge;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import com.aicoach.databinding.FragmentInviteFriendsBinding;
import com.aicoach.databinding.ItemFriendInviteBinding;
import com.aicoach.data.repository.SocialRepository;
import com.aicoach.util.SimpleTextWatcher;
import java.util.List;

public class InviteFriendsFragment extends BaseFragment<FragmentInviteFriendsBinding> {
    private SocialViewModel vm;
    private String challengeId;
    private SocialRows<SocialRepository.Friend> adapter;
    @Override protected FragmentInviteFriendsBinding inflate(@NonNull LayoutInflater i, @Nullable ViewGroup c) {
        return FragmentInviteFriendsBinding.inflate(i, c, false);
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        vm = new ViewModelProvider(this).get(SocialViewModel.class);
        challengeId = InviteFriendsFragmentArgs.fromBundle(requireArguments()).getChallengeId();
        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        // The midterm has no invite links/QR endpoint. Do not expose non-working share actions.
        binding.btnQr.setVisibility(View.GONE);
        binding.btnQrCode.setVisibility(View.GONE);
        binding.btnCopyLink.setVisibility(View.GONE);
        binding.btnMessage.setVisibility(View.GONE);
        SocialRepository.ChallengeItem challenge = vm.find(challengeId);
        if (challenge == null) {
            binding.btnSend.setEnabled(false);
            binding.btnCopyCode.setEnabled(false);
            binding.etSearch.setEnabled(false);
            binding.tvChallengeTitle.setText(R.string.social_challenge_missing);
            binding.tvChallengeMeta.setText(R.string.social_demo_notice);
            return;
        }
        binding.tvChallengeTitle.setText(challenge.name);
        binding.tvChallengeMeta.setText(R.string.social_demo_notice);
        String code = SocialRepository.invitationCode(challenge);
        binding.tvCode.setText(code);
        binding.btnCopyCode.setOnClickListener(v -> {
            ((ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE))
                    .setPrimaryClip(ClipData.newPlainText(getString(R.string.social_invite_code), code));
            com.google.android.material.snackbar.Snackbar.make(binding.getRoot(),
                    R.string.social_code_copied, com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
        });
        adapter = new SocialRows<>(R.layout.item_friend_invite, f -> f.id, (row, f, pos) -> {
            ItemFriendInviteBinding b = ItemFriendInviteBinding.bind(row);
            b.tvName.setText(f.name);
            b.tvAvatar.setText(f.name.substring(0, 1));
            boolean invited = vm.find(challengeId).invited.contains(f.id);
            b.tvSubtitle.setText(invited ? R.string.social_invited_demo : R.string.social_friend_demo);
            b.cbInvite.setOnCheckedChangeListener(null);
            b.cbInvite.setChecked(invited || vm.selected.contains(f.id));
            b.cbInvite.setEnabled(!invited);
            b.cbInvite.setOnCheckedChangeListener((button, on) -> { vm.select(f.id, on); counts(); });
        });
        binding.rvFriends.setAdapter(adapter);
        binding.etSearch.addTextChangedListener(SimpleTextWatcher.after(this::filter));
        filter();
        binding.btnSend.setOnClickListener(v -> {
            vm.invite(challengeId);
            adapter.notifyItemRangeChanged(0, adapter.getItemCount());
            counts();
            com.google.android.material.snackbar.Snackbar.make(binding.getRoot(), R.string.social_invited_demo,
                    com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
        });
    }
    private void counts() {
        binding.tvSelected.setText(getString(R.string.social_selected_count, vm.selected.size()));
        binding.btnSend.setEnabled(!vm.selected.isEmpty());
    }
    private void filter() {
        if (adapter == null) return;
        List<SocialRepository.Friend> list = vm.friends(binding.etSearch.getText().toString());
        adapter.submit(list);
        binding.tvFriendsCount.setText(getString(R.string.social_found_count, list.size()));
        binding.tvFriendsEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        counts();
    }
    @Override public void onDestroyView() {
        binding.rvFriends.setAdapter(null);
        adapter = null;
        super.onDestroyView();
    }
}
