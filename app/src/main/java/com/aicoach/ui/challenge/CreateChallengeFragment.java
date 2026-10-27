package com.aicoach.ui.challenge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.common.BaseFragment;
import com.aicoach.databinding.FragmentCreateChallengeBinding;

/**
 * Figma 41 Tạo thử thách.
 *
 * ĐÃ CÓ: giao diện + điều hướng giữa các màn.
 * B TỰ VIẾT (xem TODO): adapter chọn bài tập cho rvExercises (item_challenge_exercise), tăng/giảm mục tiêu (btnMinus/btnPlus), chọn số ngày, kiểm tra tên (tilName), gọi API tạo rồi truyền challengeId sang màn 42.
 */
public class CreateChallengeFragment extends BaseFragment<FragmentCreateChallengeBinding> {

    @Override
    protected FragmentCreateChallengeBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentCreateChallengeBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController nav = NavHostFragment.findNavController(this);
        binding.btnBack.setOnClickListener(v -> nav.popBackStack());
        // TODO(B): kiểm tra dữ liệu + gọi API tạo, thành công mới điều hướng kèm challengeId:
        //   CreateChallengeFragmentDirections.actionCreateToInvite().setChallengeId(id)
        binding.btnContinue.setOnClickListener(v -> nav.navigate(CreateChallengeFragmentDirections.actionCreateToInvite()));
    }
}
