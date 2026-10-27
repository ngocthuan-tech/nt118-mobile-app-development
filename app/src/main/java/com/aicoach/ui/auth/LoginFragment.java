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
import com.aicoach.data.remote.dto.AuthDtos;
import com.aicoach.databinding.FragmentLoginBinding;
import com.aicoach.util.SimpleTextWatcher;

/**
 * Figma 06 Đăng nhập + 07 báo lỗi.
 * Thành công: hồ sơ đã xong → Trang chủ (không Back lại được); chưa xong → Khảo sát 13.
 */
public class LoginFragment extends BaseFragment<FragmentLoginBinding> {

    private LoginViewModel vm;

    @Override
    protected FragmentLoginBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentLoginBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vm = new ViewModelProvider(this).get(LoginViewModel.class);
        NavController nav = NavHostFragment.findNavController(this);

        binding.btnBack.setVisibility(nav.getPreviousBackStackEntry() != null ? View.VISIBLE : View.GONE);
        binding.btnBack.setOnClickListener(v -> nav.navigateUp());

        binding.btnLogin.setOnClickListener(v -> submit());
        binding.etPassword.setOnEditorActionListener((tv, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit();
                return true;
            }
            return false;
        });
        binding.etEmail.addTextChangedListener(SimpleTextWatcher.after(vm::clearErrors));
        binding.etPassword.addTextChangedListener(SimpleTextWatcher.after(vm::clearErrors));

        binding.btnForgotPassword.setOnClickListener(v -> nav.navigate(R.id.action_login_to_forgot));
        binding.btnRegister.setOnClickListener(v -> nav.navigate(R.id.action_login_to_register));
        binding.btnGoogle.setOnClickListener(v -> showError(getString(R.string.auth_google_unavailable)));

        vm.getEmailError().observe(getViewLifecycleOwner(), err ->
                binding.tilEmail.setError(err == null ? null : getString(err)));
        vm.getPasswordError().observe(getViewLifecycleOwner(), err ->
                binding.tilPassword.setError(err == null ? null : getString(err)));
        vm.getState().observe(getViewLifecycleOwner(), this::render);
    }

    private void submit() {
        vm.login(text(binding.etEmail.getText()), text(binding.etPassword.getText()));
    }

    private void render(@Nullable Resource<AuthDtos.AuthResponse> r) {
        boolean loading = r != null && r.status == Resource.Status.LOADING;
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!loading);
        binding.btnLogin.setText(loading ? "" : getString(R.string.auth_login_button));

        boolean error = r != null && r.status == Resource.Status.ERROR;
        binding.errorBanner.setVisibility(error ? View.VISIBLE : View.GONE);
        if (error) binding.tvErrorMessage.setText(r.message);

        if (r != null && r.status == Resource.Status.SUCCESS && r.data != null) {
            vm.consume();
            AuthNavigation.afterSignIn(NavHostFragment.findNavController(this),
                    r.data.getUser() != null && r.data.getUser().isProfileCompleted());
        }
    }

    @NonNull
    static String text(@Nullable CharSequence cs) {
        return cs == null ? "" : cs.toString();
    }
}
