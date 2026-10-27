package com.aicoach.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.NavRootDirections;

import com.aicoach.common.BaseFragment;
import com.aicoach.databinding.FragmentSessionDetailBinding;

/**
 * Figma 39 Chi tiết buổi tập. Nhận sessionId + exerciseId qua Safe Args.
 *
 * ĐÃ CÓ: giao diện + điều hướng giữa các màn.
 * B TỰ VIẾT (xem TODO): tải buổi tập theo sessionId, bảng hiệp rvSets (item_session_set), LineChart chartAngles, thêm chip lỗi (view_session_error_chip) vào groupErrors, chia sẻ bằng Intent.ACTION_SEND.
 */
public class SessionDetailFragment extends BaseFragment<FragmentSessionDetailBinding> {

    @Override
    protected FragmentSessionDetailBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentSessionDetailBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController nav = NavHostFragment.findNavController(this);
        SessionDetailFragmentArgs args = SessionDetailFragmentArgs.fromBundle(requireArguments());

        binding.btnBack.setOnClickListener(v -> nav.popBackStack());
        // "Tập lại bài này" -> màn Sẵn sàng của Trưởng nhóm với đúng bài tập
        binding.btnRedo.setOnClickListener(v ->
                nav.navigate(NavRootDirections.actionGlobalToReadiness(args.getExerciseId())));

        // TODO(B): viewModel.load(args.getSessionId()); btnShare -> Intent chia sẻ kết quả.
    }
}
