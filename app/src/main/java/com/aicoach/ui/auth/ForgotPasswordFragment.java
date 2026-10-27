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
import com.aicoach.data.mock.MockAuthData;
import com.aicoach.databinding.FragmentForgotPasswordBinding;
import com.aicoach.util.SimpleTextWatcher;

/** Figma 08 Quên mật khẩu → 09 Nhập mã xác thực. */
public class ForgotPasswordFragment extends BaseFragment<FragmentForgotPasswordBinding> {

    private ForgotPasswordViewModel vm;

    @Override
    protected FragmentForgotPasswordBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentForgotPasswordBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vm = new ViewModelProvider(this).get(ForgotPasswordViewModel.class);
        NavController nav = NavHostFragment.findNavController(this);

        binding.icon.imgIcon.setImageResource(R.drawable.ic_key_24);
        binding.tvCodeValid.setText(getString(R.string.auth_forgot_code_valid, MockAuthData.CODE_VALID_MINUTES));

        binding.btnBack.setOnClickListener(v -> nav.navigateUp());
        binding.btnBackToLogin.setOnClickListener(v -> nav.navigateUp());
        binding.btnSend.setOnClickListener(v -> vm.send(LoginFragment.text(binding.etEmail.getText())));
        binding.etEmail.setOnEditorActionListener((tv, actionId, e) -> {
            if (actionId != EditorInfo.IME_ACTION_SEND) return false;
            vm.send(LoginFragment.text(binding.etEmail.getText()));
            return true;
        });
        binding.etEmail.addTextChangedListener(SimpleTextWatcher.after(vm::clearError));

        vm.getEmailError().observe(getViewLifecycleOwner(), err ->
                binding.tilEmail.setError(err == null ? null : getString(err)));
        vm.getState().observe(getViewLifecycleOwner(), r -> {
            boolean loading = r != null && r.status == Resource.Status.LOADING;
            binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnSend.setEnabled(!loading);
            if (r == null) return;
            if (r.status == Resource.Status.ERROR) showError(r.message);
            if (r.status == Resource.Status.SUCCESS) {
                vm.consume();
                nav.navigate(ForgotPasswordFragmentDirections.actionForgotToVerify(vm.getEmail()));
            }
        });
    }
}
