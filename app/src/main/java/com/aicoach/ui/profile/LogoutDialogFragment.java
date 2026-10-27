package com.aicoach.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.NavRootDirections;
import com.aicoach.databinding.DialogLogoutBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Figma 47 Xác nhận đăng xuất — bottom sheet (destination dạng &lt;dialog&gt; trong nav_profile).
 *
 * ĐÃ CÓ: giao diện + điều hướng về luồng đăng nhập.
 * B TỰ VIẾT (xem TODO): xoá token trước khi rời đi.
 */
public class LogoutDialogFragment extends BottomSheetDialogFragment {

    private DialogLogoutBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogLogoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnCancel.setOnClickListener(v -> dismiss());
        binding.btnConfirm.setOnClickListener(v -> {
            // TODO(B): TokenManager.clear() (và xoá dữ liệu người dùng đang giữ) trước khi điều hướng.
            NavHostFragment.findNavController(this).navigate(NavRootDirections.actionGlobalToAuth());
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
