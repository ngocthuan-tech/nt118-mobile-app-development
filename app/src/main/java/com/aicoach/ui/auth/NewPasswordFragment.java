package com.aicoach.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.common.Resource;
import com.aicoach.databinding.FragmentNewPasswordBinding;
import com.aicoach.util.SimpleTextWatcher;
import com.aicoach.util.ValidationUtils;

/** Figma 10 Tạo mật khẩu mới → 11 Thành công. "Huỷ" về màn Đăng nhập. */
public class NewPasswordFragment extends BaseFragment<FragmentNewPasswordBinding> {

    private NewPasswordViewModel vm;
    private String email;
    private String code;

    @Override
    protected FragmentNewPasswordBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentNewPasswordBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vm = new ViewModelProvider(this).get(NewPasswordViewModel.class);
        NewPasswordFragmentArgs args = NewPasswordFragmentArgs.fromBundle(requireArguments());
        email = args.getEmail();
        code = args.getCode();
        NavController nav = NavHostFragment.findNavController(this);

        binding.icon.imgIcon.setImageResource(R.drawable.ic_shield_24);
        binding.btnBack.setOnClickListener(v -> nav.navigateUp());
        binding.btnCancel.setOnClickListener(v -> nav.popBackStack(R.id.loginFragment, false));
        binding.etPassword.addTextChangedListener(SimpleTextWatcher.after(this::renderRules));
        binding.etConfirm.addTextChangedListener(SimpleTextWatcher.after(this::renderRules));
        binding.btnSubmit.setOnClickListener(v -> vm.submit(email, code, password(), confirm()));

        vm.getState().observe(getViewLifecycleOwner(), r -> {
            boolean loading = r != null && r.status == Resource.Status.LOADING;
            binding.btnSubmit.setEnabled(!loading && NewPasswordViewModel.canSubmit(password(), confirm()));
            if (r == null) return;
            if (r.status == Resource.Status.ERROR) showError(r.message);
            if (r.status == Resource.Status.SUCCESS) {
                vm.consume();
                nav.navigate(R.id.action_new_password_to_success);
            }
        });
        renderRules();
    }

    @NonNull
    private String password() { return LoginFragment.text(binding.etPassword.getText()); }

    @NonNull
    private String confirm() { return LoginFragment.text(binding.etConfirm.getText()); }

    private void renderRules() {
        String p = password();
        String c = confirm();
        PasswordUi.bindStrength(binding.strength, p, false);
        PasswordUi.bindRule(binding.ruleLength, R.string.auth_rule_length, ValidationUtils.hasMinLength(p));
        PasswordUi.bindRule(binding.ruleCase, R.string.auth_rule_case, ValidationUtils.hasUpperAndLower(p));
        PasswordUi.bindRule(binding.ruleDigit, R.string.auth_rule_digit, ValidationUtils.hasDigit(p));
        PasswordUi.bindRule(binding.ruleSpecial, R.string.auth_rule_special, ValidationUtils.hasSpecial(p));

        boolean typedConfirm = !c.isEmpty();
        boolean match = typedConfirm && c.equals(p);
        binding.tilConfirm.setError(typedConfirm && !match ? getString(R.string.auth_error_password_mismatch) : null);
        binding.tilConfirm.setHelperText(match ? getString(R.string.auth_password_match) : null);
        boolean showRule = !p.isEmpty() && !ValidationUtils.isValidPassword(p);
        binding.tilPassword.setError(showRule ? getString(R.string.auth_error_password_rule) : null);
        binding.btnSubmit.setEnabled(NewPasswordViewModel.canSubmit(p, c));
    }
}
