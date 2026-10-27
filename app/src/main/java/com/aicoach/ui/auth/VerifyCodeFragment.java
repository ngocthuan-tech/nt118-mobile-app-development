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
import com.aicoach.util.SimpleTextWatcher;

import java.util.Locale;

/** Figma 09 Nhập mã xác thực (6 số) → 10 Mật khẩu mới. Mã demo: 123456. */
public class VerifyCodeFragment extends BaseFragment<FragmentVerifyCodeBinding> {

    private VerifyCodeViewModel vm;
    private String email;
    private boolean showingError;

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (binding == null) return;
            renderResend();
            binding.tvResend.postDelayed(this, 1000);
        }
    };

    @Override
    protected FragmentVerifyCodeBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentVerifyCodeBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vm = new ViewModelProvider(this).get(VerifyCodeViewModel.class);
        email = VerifyCodeFragmentArgs.fromBundle(requireArguments()).getEmail();
        NavController nav = NavHostFragment.findNavController(this);

        binding.icon.imgIcon.setImageResource(R.drawable.ic_mail_24);
        binding.tvEmail.setText(VerifyCodeViewModel.maskEmail(email));
        binding.btnBack.setOnClickListener(v -> nav.navigateUp());

        binding.rowBoxes.setOnClickListener(v -> focusCode());
        binding.etCode.addTextChangedListener(SimpleTextWatcher.after(() -> {
            vm.clearError();
            renderBoxes();
            if (code().length() == VerifyCodeViewModel.CODE_LENGTH) vm.verify(email, code());
        }));
        binding.btnConfirm.setOnClickListener(v -> vm.verify(email, code()));
        binding.tvResend.setOnClickListener(v -> {
            if (vm.secondsUntilResend() > 0) return;
            vm.resend(email);
            showError(getString(R.string.auth_otp_resent));
            renderResend();
        });

        vm.getState().observe(getViewLifecycleOwner(), r -> {
            boolean loading = r != null && r.status == Resource.Status.LOADING;
            showingError = r != null && r.status == Resource.Status.ERROR;
            binding.tvError.setVisibility(showingError ? View.VISIBLE : View.GONE);
            if (showingError) binding.tvError.setText(r.message);
            renderBoxes();
            binding.btnConfirm.setEnabled(!loading && code().length() == VerifyCodeViewModel.CODE_LENGTH);
            if (r != null && r.status == Resource.Status.SUCCESS) {
                vm.consume();
                nav.navigate(VerifyCodeFragmentDirections.actionVerifyToNewPassword(email, code()));
            }
        });

        renderBoxes();
        focusCode();
    }

    @Override
    public void onResume() {
        super.onResume();
        tick.run();
    }

    @Override
    public void onPause() {
        if (binding != null) binding.tvResend.removeCallbacks(tick);
        super.onPause();
    }

    @NonNull
    private String code() {
        return LoginFragment.text(binding.etCode.getText());
    }

    /** Ô đã nhập hiện số; ô kế tiếp viền xanh (đang nhập); sai mã thì cả hàng viền đỏ. */
    private void renderBoxes() {
        TextView[] boxes = {binding.box1, binding.box2, binding.box3, binding.box4, binding.box5, binding.box6};
        String c = code();
        for (int i = 0; i < boxes.length; i++) {
            boxes[i].setText(i < c.length() ? String.valueOf(c.charAt(i)) : "");
            int bg = showingError ? R.drawable.bg_auth_otp_box_error
                    : (i == c.length() || (i == boxes.length - 1 && c.length() == boxes.length))
                    ? R.drawable.bg_auth_otp_box_active : R.drawable.bg_auth_otp_box;
            boxes[i].setBackgroundResource(bg);
        }
        binding.btnConfirm.setEnabled(c.length() == VerifyCodeViewModel.CODE_LENGTH);
    }

    private void renderResend() {
        int left = vm.secondsUntilResend();
        if (left > 0) {
            String mmss = String.format(Locale.ROOT, "%02d:%02d", left / 60, left % 60);
            binding.tvResend.setText(getString(R.string.auth_otp_resend_in, mmss));
            binding.tvResend.setTextColor(requireContext().getColor(R.color.text_secondary));
        } else {
            binding.tvResend.setText(R.string.auth_otp_resend);
            binding.tvResend.setTextColor(requireContext().getColor(R.color.brand_primary));
        }
    }

    private void focusCode() {
        binding.etCode.requestFocus();
        binding.etCode.setSelection(code().length());
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        View input = binding.etCode;
        if (imm != null) input.post(() -> imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT));
    }
}
