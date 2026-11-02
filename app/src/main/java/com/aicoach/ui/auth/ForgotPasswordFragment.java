package com.aicoach.ui.auth;

import android.os.Bundle;
import android.os.SystemClock;
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
import com.aicoach.databinding.FragmentForgotPasswordBinding;
import com.aicoach.util.SimpleTextWatcher;

/** Figma 08: yêu cầu gửi mã thật, thành công mới chuyển sang màn 09. */
public class ForgotPasswordFragment
        extends BaseFragment<FragmentForgotPasswordBinding> {

    private ForgotPasswordViewModel vm;

    @Override
    protected FragmentForgotPasswordBinding inflate(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container
    ) {
        return FragmentForgotPasswordBinding.inflate(
                inflater, container, false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        vm = new ViewModelProvider(this)
                .get(ForgotPasswordViewModel.class);

        NavController nav = NavHostFragment.findNavController(this);

        binding.icon.imgIcon.setImageResource(R.drawable.ic_key_24);

        // Hiện hướng dẫn chung; thời hạn thật lấy từ server ở màn nhập mã.
        // Nội dung được khai báo trong layout qua strings_auth.xml.
        binding.tvCodeValid.setVisibility(View.VISIBLE);

        binding.btnBack.setOnClickListener(v -> nav.navigateUp());
        binding.btnBackToLogin.setOnClickListener(v -> nav.navigateUp());

        binding.btnSend.setOnClickListener(v ->
                vm.send(LoginFragment.text(binding.etEmail.getText()))
        );

        binding.etEmail.setOnEditorActionListener((tv, actionId, event) -> {
            if (actionId != EditorInfo.IME_ACTION_SEND) {
                return false;
            }

            vm.send(LoginFragment.text(binding.etEmail.getText()));
            return true;
        });

        binding.etEmail.addTextChangedListener(
                SimpleTextWatcher.after(vm::clearError)
        );

        vm.getEmailError().observe(getViewLifecycleOwner(), error ->
                binding.tilEmail.setError(
                        error == null ? null : getString(error)
                )
        );

        vm.getState().observe(getViewLifecycleOwner(), result -> {
            boolean loading = result != null
                    && result.status == Resource.Status.LOADING;

            binding.progress.setVisibility(
                    loading ? View.VISIBLE : View.GONE
            );
            binding.btnSend.setEnabled(!loading);
            binding.etEmail.setEnabled(!loading);

            if (result == null) {
                return;
            }

            if (result.status == Resource.Status.ERROR) {
                showError(result.message);
                vm.consume();
                return;
            }

            if (result.status != Resource.Status.SUCCESS) {
                return;
            }

            // API gửi mã phải có dữ liệu thời gian chờ.
            if (result.data == null) {
                vm.consume();
                showError(getString(R.string.common_error_generic));
                return;
            }

            long resendAvailableAt = SystemClock.elapsedRealtime()
                    + Math.max(
                    0,
                    result.data.getResendAfterSeconds()
            ) * 1000L;

            int expiresInSeconds = Math.max(
                    0,
                    result.data.getExpiresInSeconds()
            );

            ForgotPasswordFragmentDirections.ActionForgotToVerify action =
                    ForgotPasswordFragmentDirections.actionForgotToVerify(
                            vm.getEmail()
                    );

            action.setResendAvailableAt(resendAvailableAt);
            action.setExpiresInSeconds(expiresInSeconds);

            // Xóa kết quả đã xử lý để tránh chuyển màn lặp lại.
            vm.consume();
            nav.navigate(action);
        });
    }
}