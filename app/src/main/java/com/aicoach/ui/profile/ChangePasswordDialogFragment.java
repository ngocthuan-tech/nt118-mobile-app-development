package com.aicoach.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.aicoach.R;
import com.aicoach.common.Resource;
import com.aicoach.databinding.DialogChangePasswordBinding;
import com.aicoach.util.SimpleTextWatcher;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Đổi mật khẩu — bottom sheet mở từ dòng "Đổi mật khẩu" ở Hồ sơ (43).
 *
 * Kiểm tra dữ liệu qua ViewModel, giữ hộp thoại khi có lỗi và chỉ đóng khi API thành công.
 */
public class ChangePasswordDialogFragment extends BottomSheetDialogFragment {

    private DialogChangePasswordBinding binding;
    private ChangePasswordViewModel vm;

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
        vm = new ViewModelProvider(this).get(ChangePasswordViewModel.class);
        binding.etCurrent.addTextChangedListener(SimpleTextWatcher.after(() -> {
            binding.tilCurrent.setError(null);
            clearServerError();
        }));
        binding.etNew.addTextChangedListener(SimpleTextWatcher.after(() -> {
            binding.tilNew.setError(null);
            binding.tilConfirm.setError(null);
            clearServerError();
        }));
        binding.etConfirm.addTextChangedListener(SimpleTextWatcher.after(() -> {
            binding.tilConfirm.setError(null);
            clearServerError();
        }));
        binding.btnSubmit.setOnClickListener(v -> {
            ChangePasswordViewModel.Errors errors = vm.submit(
                    text(binding.etCurrent.getText()), text(binding.etNew.getText()),
                    text(binding.etConfirm.getText()));
            binding.tilCurrent.setError(errors.current == 0 ? null : getString(errors.current));
            binding.tilNew.setError(errors.password == 0 ? null : getString(errors.password));
            binding.tilConfirm.setError(errors.confirm == 0 ? null : getString(errors.confirm));
        });
        vm.getState().observe(getViewLifecycleOwner(), this::render);
    }

    private static String text(@Nullable CharSequence value) {
        return value == null ? "" : value.toString();
    }

    private void clearServerError() {
        Resource<Void> result = vm.getState().getValue();
        if (result != null && result.status == Resource.Status.ERROR) vm.consume();
    }

    private void render(@Nullable Resource<Void> result) {
        if (binding == null) return;
        boolean loading = vm.isLoading();
        binding.btnSubmit.setEnabled(!loading);
        binding.etCurrent.setEnabled(!loading);
        binding.etNew.setEnabled(!loading);
        binding.etConfirm.setEnabled(!loading);
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSubmit.setText(loading ? R.string.profile_password_processing : R.string.profile_password_submit);
        setCancelable(!loading);
        if (getDialog() instanceof BottomSheetDialog) {
            ((BottomSheetDialog) getDialog()).getBehavior().setDraggable(!loading);
        }
        boolean error = result != null && result.status == Resource.Status.ERROR;
        binding.tvError.setVisibility(error ? View.VISIBLE : View.GONE);
        binding.tvError.setText(error ? result.message : null);
        if (result != null && result.status == Resource.Status.SUCCESS && isResumed()
                && !getParentFragmentManager().isStateSaved()) {
            vm.consume();
            Snackbar.make(requireActivity().findViewById(android.R.id.content),
                    R.string.profile_password_success, Snackbar.LENGTH_LONG).show();
            binding.etCurrent.setText(null);
            binding.etNew.setText(null);
            binding.etConfirm.setText(null);
            dismiss();
        }
    }

    @Override public void onStart() {
        super.onStart();
        if (getDialog() instanceof BottomSheetDialog) {
            BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
            dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
            if (dialog.getWindow() != null) {
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            }
        }
    }

    @Override public void onResume() {
        super.onResume();
        render(vm.getState().getValue());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
