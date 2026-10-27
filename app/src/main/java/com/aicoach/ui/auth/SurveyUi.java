package com.aicoach.ui.auth;

import android.content.res.ColorStateList;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.R;
import com.aicoach.common.Resource;
import com.aicoach.databinding.ViewSurveyFooterBinding;
import com.aicoach.databinding.ViewSurveyHeaderBinding;

/** Phần lặp lại của 3 màn khảo sát: đầu trang "Bước x/3", nút Tiếp tục có vòng xoay, ViewModel dùng chung. */
final class SurveyUi {

    private SurveyUi() {
    }

    /** ViewModel gắn với cả graph nav_auth → 13, 14, 15, 16, 17 dùng chung một bản. */
    @NonNull
    static SurveyViewModel viewModel(@NonNull Fragment f) {
        NavController nav = NavHostFragment.findNavController(f);
        return new ViewModelProvider(nav.getViewModelStoreOwner(R.id.nav_auth)).get(SurveyViewModel.class);
    }

    /**
     * Bước 1 không có nút quay lại khi đây là màn đầu của back stack (vừa đăng nhập xong).
     */
    static void bindHeader(@NonNull Fragment f, @NonNull ViewSurveyHeaderBinding h, int step) {
        NavController nav = NavHostFragment.findNavController(f);
        h.tvStep.setText(f.getString(R.string.survey_step, step));
        boolean canGoBack = nav.getPreviousBackStackEntry() != null
                && nav.getPreviousBackStackEntry().getDestination().getId() != R.id.nav_auth;
        h.btnBack.setVisibility(canGoBack ? View.VISIBLE : View.INVISIBLE);
        h.btnBack.setOnClickListener(v -> nav.navigateUp());
        View[] bars = {h.bar1, h.bar2, h.bar3};
        int on = ContextCompat.getColor(f.requireContext(), R.color.brand_primary);
        int off = ContextCompat.getColor(f.requireContext(), R.color.divider);
        for (int i = 0; i < bars.length; i++) {
            bars[i].setBackgroundTintList(ColorStateList.valueOf(i < step ? on : off));
        }
    }

    static void renderFooter(@NonNull ViewSurveyFooterBinding footer, Resource<?> r) {
        boolean loading = r != null && r.status == Resource.Status.LOADING;
        footer.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        footer.btnContinue.setEnabled(!loading);
        footer.btnContinue.setTextColor(ContextCompat.getColor(footer.getRoot().getContext(),
                loading ? R.color.transparent : R.color.white));
    }
}
