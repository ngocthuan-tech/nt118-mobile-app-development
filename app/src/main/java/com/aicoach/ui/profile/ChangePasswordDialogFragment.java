package com.aicoach.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aicoach.databinding.DialogChangePasswordBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Đổi mật khẩu — bottom sheet mở từ dòng "Đổi mật khẩu" ở Hồ sơ (43).
 *
 * ĐÃ CÓ: giao diện.
 * B TỰ VIẾT (xem TODO): kiểm tra bằng ValidationUtils của A, gọi API đổi mật khẩu, hiện lỗi trên từng ô.
 */
public class ChangePasswordDialogFragment extends BottomSheetDialogFragment {

    private DialogChangePasswordBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogChangePasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // TODO(B): kiểm tra 3 ô (tilCurrent / tilNew / tilConfirm) + gọi API, thành công mới dismiss().
        binding.btnSubmit.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
