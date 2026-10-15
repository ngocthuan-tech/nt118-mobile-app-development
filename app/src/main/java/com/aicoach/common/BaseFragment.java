package com.aicoach.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.snackbar.Snackbar;

/**
 * Fragment gốc của cả app: lo ViewBinding (tạo/huỷ đúng vòng đời để tránh rò bộ nhớ)
 * và cách hiển thị lỗi thống nhất. Mọi màn hình đều kế thừa lớp này.
 */
public abstract class BaseFragment<VB extends ViewBinding> extends Fragment {

    protected VB binding;

    /** Mỗi màn trả về binding của mình, ví dụ: FragmentHomeBinding.inflate(inflater, container, false). */
    protected abstract VB inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = inflate(inflater, container);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /** Màn nào có vòng xoay thì override để bật/tắt nó. */
    protected void showLoading(boolean show) {
    }

    protected void showError(String message) {
        if (binding == null || message == null) return;
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }
}
