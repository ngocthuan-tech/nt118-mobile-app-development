package com.aicoach.ui.challenge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.ui.history.SegmentGroup;
import com.aicoach.common.BaseFragment;
import com.aicoach.databinding.FragmentChallengeListBinding;

/**
 * Figma 40 Thử thách & Xếp hạng (tab "Thử thách").
 *
 * ĐÃ CÓ: giao diện + điều hướng giữa các màn.
 * B TỰ VIẾT (xem TODO): SocialRepository (+ MockSocialData khi chưa có API), đổ thẻ thử thách đang tham gia, bục 3 hạng đầu, LeaderboardAdapter cho rvLeaderboard (item_leaderboard_row), chuyển Bạn bè/Toàn quốc.
 */
public class ChallengeListFragment extends BaseFragment<FragmentChallengeListBinding> {

    @Override
    protected FragmentChallengeListBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentChallengeListBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController nav = NavHostFragment.findNavController(this);
        binding.btnCreate.setOnClickListener(v -> nav.navigate(ChallengeListFragmentDirections.actionChallengesToCreate()));
        // Thẻ thử thách đang tham gia -> mời thêm bạn. TODO(B): truyền challengeId thật.
        binding.cardActive.setOnClickListener(v -> nav.navigate(ChallengeListFragmentDirections.actionChallengesToInvite()));

        // Bạn bè / Toàn quốc. TODO(B): onSelect -> tải bảng xếp hạng tương ứng.
        SegmentGroup.bind(0, null, binding.btnFriends, binding.btnNational);
        // TODO(B): ViewModel đổ thẻ thử thách, bục 3 hạng đầu, rvLeaderboard.
    }
}
