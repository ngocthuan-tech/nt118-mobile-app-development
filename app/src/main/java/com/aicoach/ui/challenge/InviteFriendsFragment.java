package com.aicoach.ui.challenge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.R;

import com.aicoach.common.BaseFragment;
import com.aicoach.databinding.FragmentInviteFriendsBinding;

/**
 * Figma 42 Mời bạn bè. Nhận challengeId qua Safe Args.
 *
 * ĐÃ CÓ: giao diện + điều hướng giữa các màn.
 * B TỰ VIẾT (xem TODO): hiện mã mời, sao chép mã/link (ClipboardManager), tìm bạn (etSearch), FriendInviteAdapter cho rvFriends (item_friend_invite), đếm số bạn đã chọn và gọi API gửi lời mời.
 */
public class InviteFriendsFragment extends BaseFragment<FragmentInviteFriendsBinding> {

    @Override
    protected FragmentInviteFriendsBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentInviteFriendsBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController nav = NavHostFragment.findNavController(this);
        binding.btnBack.setOnClickListener(v -> nav.popBackStack());
        // Gửi lời mời xong quay về 40, bỏ màn 41 khỏi back stack.
        // TODO(B): gọi API gửi lời mời trước, thành công mới quay về.
        binding.btnSend.setOnClickListener(v -> nav.popBackStack(R.id.challengeListFragment, false));
    }
}
