package com.aicoach.ui.auth;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aicoach.common.BaseFragment;
import com.aicoach.databinding.FragmentLoginBinding;

public class LoginFragment extends BaseFragment<FragmentLoginBinding> {

    @Override
    protected FragmentLoginBinding inflate(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container
    ) {
        return FragmentLoginBinding.inflate(inflater, container, false);
    }
}