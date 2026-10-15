package com.aicoach.ui.workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.databinding.FragmentPlaceholderBinding;

/**
 * Kiểm tra khung hình (Trưởng nhóm). Hiện là MÀN TẠM: hiển thị exerciseId nhận được để
 * chứng minh hợp đồng điều hướng chạy đúng. Sẽ thay bằng camera + kiểm tra ánh sáng/khoảng cách.
 */
public class ReadinessFragment extends BaseFragment<FragmentPlaceholderBinding> {

    @Override
    protected FragmentPlaceholderBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentPlaceholderBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String exerciseId = ReadinessFragmentArgs.fromBundle(requireArguments()).getExerciseId();

        binding.tvTitle.setText(getString(R.string.placeholder_readiness, exerciseId));
        binding.tvOwner.setText(R.string.placeholder_owner_lead);
        binding.btnAction.setVisibility(View.VISIBLE);
        binding.btnAction.setText(R.string.placeholder_start_workout);
        binding.btnAction.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(
                        ReadinessFragmentDirections.actionReadinessToLive(exerciseId)));
    }
}
