package com.aicoach.ui.auth;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.aicoach.R;
import com.aicoach.databinding.ItemOnboardingAiBinding;
import com.aicoach.databinding.ItemOnboardingStreakBinding;
import com.aicoach.databinding.ItemOnboardingTextBinding;
import com.aicoach.databinding.ItemOnboardingVoiceBinding;

/** 3 trang Onboarding (Figma 02, 03, 04) — mỗi trang một layout minh hoạ riêng, phần chữ dùng chung. */
class OnboardingPagerAdapter extends RecyclerView.Adapter<OnboardingPagerAdapter.PageHolder> {

    static final int PAGE_COUNT = 3;

    private static final int[] TITLES = {
            R.string.onboarding_1_title, R.string.onboarding_2_title, R.string.onboarding_3_title};
    private static final int[] DESCS = {
            R.string.onboarding_1_desc, R.string.onboarding_2_desc, R.string.onboarding_3_desc};

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public PageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case 0: {
                ItemOnboardingAiBinding b = ItemOnboardingAiBinding.inflate(inf, parent, false);
                return new PageHolder(b, b.text);
            }
            case 1: {
                ItemOnboardingVoiceBinding b = ItemOnboardingVoiceBinding.inflate(inf, parent, false);
                return new PageHolder(b, b.text);
            }
            default: {
                ItemOnboardingStreakBinding b = ItemOnboardingStreakBinding.inflate(inf, parent, false);
                return new PageHolder(b, b.text);
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull PageHolder holder, int position) {
        holder.text.tvTitle.setText(TITLES[position]);
        holder.text.tvDesc.setText(DESCS[position]);
    }

    @Override
    public int getItemCount() {
        return PAGE_COUNT;
    }

    static class PageHolder extends RecyclerView.ViewHolder {
        final ItemOnboardingTextBinding text;

        PageHolder(@NonNull ViewBinding page, @NonNull ItemOnboardingTextBinding text) {
            super(page.getRoot());
            this.text = text;
        }
    }
}
