package com.aicoach.ui.auth;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.data.local.SettingsPrefs;
import com.aicoach.databinding.FragmentOnboardingBinding;

/**
 * Figma 02–04 Onboarding — màn đầu tiên của nav_auth.
 * Đã xem Onboarding rồi (SettingsPrefs) thì chuyển thẳng sang 05 Chào mừng, không hiện lại.
 */
public class OnboardingFragment extends BaseFragment<FragmentOnboardingBinding> {

    private final ViewPager2.OnPageChangeCallback pageCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            renderPage(position);
        }
    };

    @Override
    protected FragmentOnboardingBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentOnboardingBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (new SettingsPrefs(requireContext()).isOnboardingDone()) {
            leaveTo(R.id.action_onboarding_to_welcome);
            return;
        }

        binding.pager.setAdapter(new OnboardingPagerAdapter());
        binding.pager.registerOnPageChangeCallback(pageCallback);

        binding.btnNext.setOnClickListener(v -> {
            int next = binding.pager.getCurrentItem() + 1;
            if (next < OnboardingPagerAdapter.PAGE_COUNT) binding.pager.setCurrentItem(next, true);
        });
        binding.btnSkip.setOnClickListener(v -> leaveTo(R.id.action_onboarding_to_welcome));
        binding.btnStart.setOnClickListener(v -> leaveTo(R.id.action_onboarding_to_welcome));
        binding.btnLogin.setOnClickListener(v -> leaveTo(R.id.action_onboarding_to_login));
        renderPage(binding.pager.getCurrentItem());
    }

    @Override
    public void onDestroyView() {
        if (binding != null) binding.pager.unregisterOnPageChangeCallback(pageCallback);
        super.onDestroyView();
    }

    /** Trang cuối: ẩn "Bỏ qua" + nút tròn, hiện "Bắt đầu ngay". Chấm đang chọn dài hơn, màu cyan. */
    private void renderPage(int position) {
        boolean last = position == OnboardingPagerAdapter.PAGE_COUNT - 1;
        binding.btnSkip.setVisibility(last ? View.INVISIBLE : View.VISIBLE);
        binding.btnNext.setVisibility(last ? View.GONE : View.VISIBLE);
        binding.btnStart.setVisibility(last ? View.VISIBLE : View.GONE);
        binding.rowLogin.setVisibility(last ? View.VISIBLE : View.GONE);

        View[] dots = {binding.dot1, binding.dot2, binding.dot3};
        int active = ContextCompat.getColor(requireContext(), R.color.accent_cyan);
        int inactive = ContextCompat.getColor(requireContext(), R.color.white_40);
        int small = getResources().getDimensionPixelSize(R.dimen.onboarding_dot_size);
        int wide = getResources().getDimensionPixelSize(R.dimen.onboarding_dot_active_width);
        for (int i = 0; i < dots.length; i++) {
            ViewGroup.LayoutParams lp = dots[i].getLayoutParams();
            lp.width = i == position ? wide : small;
            dots[i].setLayoutParams(lp);
            dots[i].setBackgroundTintList(ColorStateList.valueOf(i == position ? active : inactive));
        }
    }

    /** Rời Onboarding (đánh dấu đã xem) và bỏ màn này khỏi back stack. */
    private void leaveTo(int actionId) {
        new SettingsPrefs(requireContext()).setOnboardingDone(true);
        NavController nav = NavHostFragment.findNavController(this);
        nav.navigate(actionId, null, new NavOptions.Builder()
                .setPopUpTo(R.id.onboardingFragment, true)
                .build());
    }
}
