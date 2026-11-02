package com.aicoach.ui.auth;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.common.Resource;
import com.aicoach.databinding.FragmentVerifyCodeBinding;
import com.aicoach.util.ResetCodePolicy;
import com.aicoach.util.SimpleTextWatcher;
import java.util.Locale;

/** Figma 09: mã thật, thời gian chờ từ server, không tự gửi lại khi khôi phục màn. */
public class VerifyCodeFragment extends BaseFragment<FragmentVerifyCodeBinding> {
    private VerifyCodeViewModel vm;
    private String email;
    private boolean showingError;
    private boolean restoringView;

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (binding == null) return;
            renderControls();
            binding.tvResend.postDelayed(this, 1000);
        }
    };

    @Override protected FragmentVerifyCodeBinding inflate(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentVerifyCodeBinding.inflate(inflater, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        restoringView = true;
        vm = new ViewModelProvider(this).get(VerifyCodeViewModel.class);
        email = VerifyCodeFragmentArgs.fromBundle(requireArguments()).getEmail();
        NavController nav = NavHostFragment.findNavController(this);
        binding.icon.imgIcon.setImageResource(R.drawable.ic_mail_24);
        binding.tvEmail.setText(VerifyCodeViewModel.maskEmail(email));
        binding.btnBack.setOnClickListener(v -> nav.navigateUp());
        binding.rowBoxes.setOnClickListener(v -> focusCode());
        binding.etCode.addTextChangedListener(SimpleTextWatcher.after(() -> {
            if (!restoringView) vm.clearError();
            renderBoxes();
            renderControls();
        }));
        binding.btnConfirm.setOnClickListener(v -> vm.verify(email, code()));
        binding.tvResend.setOnClickListener(v -> vm.resend(email));

        vm.getState().observe(getViewLifecycleOwner(), result -> {
            showingError = result != null && result.status == Resource.Status.ERROR;
            binding.tvError.setVisibility(showingError ? View.VISIBLE : View.GONE);
            if (showingError) binding.tvError.setText(result.message);
            renderBoxes();
            renderControls();
            // Response<Void>: SUCCESS vẫn có data = null.
            if (result != null && result.status == Resource.Status.SUCCESS) {
                String verifiedCode = vm.getSubmittedCode();
                vm.consume();
                nav.navigate(VerifyCodeFragmentDirections.actionVerifyToNewPassword(email, verifiedCode));
            }
        });
        vm.getResendState().observe(getViewLifecycleOwner(), result -> {
            if (result != null && result.status == Resource.Status.SUCCESS) {
                if (result.data != null) {
                    vm.applyCodeSent(result.data);
                    binding.etCode.setText("");
                    showError(getString(R.string.auth_otp_resent));
                } else {
                    showError(getString(R.string.common_error_generic));
                }
                vm.consumeResend();
            } else if (result != null && result.status == Resource.Status.ERROR) {
                showError(result.message);
                vm.consumeResend();
            }
            renderBoxes();
            renderControls();
        });
        renderBoxes();
        renderControls();
    }

    @Override public void onViewStateRestored(@Nullable Bundle state) {
        super.onViewStateRestored(state);
        restoringView = false;
        renderBoxes();
        renderControls();
        focusCode();
    }
    @Override public void onResume() {
        super.onResume();
        binding.tvResend.removeCallbacks(tick);
        tick.run();
    }
    @Override public void onPause() {
        if (binding != null) binding.tvResend.removeCallbacks(tick);
        super.onPause();
    }
    @Override public void onDestroyView() {
        if (binding != null) binding.tvResend.removeCallbacks(tick);
        super.onDestroyView();
    }
    @NonNull private String code() {
        return LoginFragment.text(binding.etCode.getText());
    }
    private void renderBoxes() {
        TextView[] boxes = {binding.box1, binding.box2, binding.box3, binding.box4, binding.box5, binding.box6};
        String code = code();
        for (int i = 0; i < boxes.length; i++) {
            boxes[i].setText(i < code.length() ? String.valueOf(code.charAt(i)) : "");
            boolean active = i == code.length() || (i == boxes.length - 1 && code.length() == boxes.length);
            boxes[i].setBackgroundResource(showingError ? R.drawable.bg_auth_otp_box_error
                    : active ? R.drawable.bg_auth_otp_box_active : R.drawable.bg_auth_otp_box);
        }
    }
    private void renderControls() {
        boolean busy = vm.isBusy();
        int left = vm.secondsUntilResend();
        binding.etCode.setEnabled(!busy);
        binding.btnConfirm.setEnabled(!busy && ResetCodePolicy.isValidCode(code()));
        binding.btnConfirm.setText(busy ? R.string.auth_otp_processing : R.string.auth_otp_confirm);
        binding.tvResend.setEnabled(!busy && left == 0);
        if (busy) {
            binding.tvResend.setText(R.string.auth_otp_processing);
        } else if (left > 0) {
            String mmss = String.format(Locale.ROOT, "%02d:%02d", left / 60, left % 60);
            binding.tvResend.setText(getString(R.string.auth_otp_resend_in, mmss));
        } else {
            binding.tvResend.setText(R.string.auth_otp_resend);
        }
        binding.tvResend.setTextColor(requireContext().getColor(!busy && left == 0
                ? R.color.brand_primary : R.color.text_secondary));
        int validSeconds = vm.getExpiresInSeconds();
        binding.tvCodeValid.setVisibility(validSeconds > 0 ? View.VISIBLE : View.GONE);
        if (validSeconds > 0) binding.tvCodeValid.setText(getString(R.string.auth_otp_valid_seconds, validSeconds));
    }
    private void focusCode() {
        if (vm.isBusy()) return;
        binding.etCode.requestFocus();
        binding.etCode.setSelection(code().length());
        InputMethodManager keyboard = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        View input = binding.etCode;
        if (keyboard != null) input.post(() -> {
            if (binding != null && input.isAttachedToWindow() && !vm.isBusy()) {
                keyboard.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }
}
