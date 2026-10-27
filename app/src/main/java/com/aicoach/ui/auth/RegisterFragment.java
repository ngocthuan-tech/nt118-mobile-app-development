package com.aicoach.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.common.Resource;
import com.aicoach.databinding.FragmentRegisterBinding;
import com.aicoach.util.SimpleTextWatcher;

import java.util.Locale;

/** Figma 12 Tạo tài khoản → thành công vào Khảo sát 13 (tài khoản mới luôn chưa hoàn tất hồ sơ). */
public class RegisterFragment extends BaseFragment<FragmentRegisterBinding> {

    private RegisterViewModel vm;

    @Override
    protected FragmentRegisterBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentRegisterBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vm = new ViewModelProvider(this).get(RegisterViewModel.class);
        NavController nav = NavHostFragment.findNavController(this);

        binding.btnBack.setOnClickListener(v -> nav.navigateUp());
        binding.btnLogin.setOnClickListener(v -> {
            // Đã đi qua màn Đăng nhập thì quay lại đúng màn đó, tránh chồng 2 màn Đăng nhập.
            if (!nav.popBackStack(R.id.loginFragment, false)) nav.navigate(R.id.action_register_to_login);
        });
        binding.btnRegister.setOnClickListener(v -> submit());
        binding.etConfirm.setOnEditorActionListener((tv, actionId, e) -> {
            if (actionId != EditorInfo.IME_ACTION_DONE) return false;
            submit();
            return true;
        });
        binding.etPassword.addTextChangedListener(SimpleTextWatcher.after(() -> {
            PasswordUi.bindStrength(binding.strength, LoginFragment.text(binding.etPassword.getText()), true);
            vm.clearErrors();
        }));
        binding.etName.addTextChangedListener(SimpleTextWatcher.after(vm::clearErrors));
        binding.etEmail.addTextChangedListener(SimpleTextWatcher.after(vm::clearErrors));
        binding.etConfirm.addTextChangedListener(SimpleTextWatcher.after(vm::clearErrors));
        binding.cbTerms.setOnCheckedChangeListener((b, checked) -> vm.clearErrors());
        PasswordUi.bindStrength(binding.strength, LoginFragment.text(binding.etPassword.getText()), true);

        vm.getErrors().observe(getViewLifecycleOwner(), fe -> {
            binding.tilName.setError(fe == null || fe.name == null ? null : getString(fe.name));
            binding.tilEmail.setError(fe == null || fe.email == null ? null : getString(fe.email));
            binding.tilPassword.setError(fe == null || fe.password == null ? null : getString(fe.password));
            binding.tilConfirm.setError(fe == null || fe.confirm == null ? null : getString(fe.confirm));
            binding.tvTermsError.setVisibility(fe != null && fe.terms ? View.VISIBLE : View.GONE);
        });
        vm.getState().observe(getViewLifecycleOwner(), r -> {
            boolean loading = r != null && r.status == Resource.Status.LOADING;
            binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnRegister.setEnabled(!loading);
            binding.btnRegister.setText(loading ? "" : getString(R.string.auth_register_button));
            if (r == null) return;
            if (r.status == Resource.Status.ERROR) {
                // 409 "email đã đăng ký" → hiện ngay dưới ô Email; lỗi khác (mạng, server) → Snackbar.
                if (r.message != null && r.message.toLowerCase(Locale.ROOT).contains("email")) {
                    binding.tilEmail.setError(r.message);
                } else {
                    showError(r.message);
                }
                vm.consume();
            } else if (r.status == Resource.Status.SUCCESS && r.data != null) {
                vm.consume();
                AuthNavigation.afterSignIn(nav, r.data.getUser() != null && r.data.getUser().isProfileCompleted());
            }
        });
    }

    private void submit() {
        vm.register(LoginFragment.text(binding.etName.getText()), LoginFragment.text(binding.etEmail.getText()),
                LoginFragment.text(binding.etPassword.getText()), LoginFragment.text(binding.etConfirm.getText()),
                binding.cbTerms.isChecked());
    }
}
