package com.aicoach.ui.auth;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.aicoach.R;
import com.aicoach.databinding.ViewPasswordRuleBinding;
import com.aicoach.databinding.ViewPasswordStrengthBinding;
import com.aicoach.util.ValidationUtils;

/** Vẽ thanh độ mạnh và các dòng điều kiện mật khẩu (Figma 10, 12). */
final class PasswordUi {

    private PasswordUi() {
    }

    static void bindStrength(@NonNull ViewPasswordStrengthBinding b, @Nullable String password,
                             boolean withPrefix) {
        Context c = b.getRoot().getContext();
        int score = ValidationUtils.strength(password);
        @ColorRes int color = score <= 1 ? R.color.state_error
                : score == 2 ? R.color.state_warning : R.color.state_success;
        View[] bars = {b.bar1, b.bar2, b.bar3, b.bar4};
        for (int i = 0; i < bars.length; i++) {
            int tint = ContextCompat.getColor(c, i < score ? color : R.color.divider);
            bars[i].setBackgroundTintList(ColorStateList.valueOf(tint));
        }
        if (score == 0) {
            b.tvStrength.setText("");
            return;
        }
        @StringRes int label = score <= 1 ? R.string.auth_strength_weak
                : score == 2 ? R.string.auth_strength_medium
                : score == 3 ? R.string.auth_strength_strong : R.string.auth_strength_very_strong;
        String text = c.getString(label);
        b.tvStrength.setText(withPrefix
                ? c.getString(R.string.auth_strength_label, text.toLowerCase(java.util.Locale.ROOT)) : text);
        b.tvStrength.setTextColor(ContextCompat.getColor(c, color));
    }

    static void bindRule(@NonNull ViewPasswordRuleBinding row, @StringRes int text, boolean passed) {
        Context c = row.getRoot().getContext();
        row.tvRule.setText(text);
        row.tvRule.setTextColor(ContextCompat.getColor(c, passed ? R.color.text_primary : R.color.text_secondary));
        if (passed) {
            row.imgCheck.setImageResource(R.drawable.ic_check_circle_24);
            row.imgCheck.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(c, R.color.state_success)));
            row.imgCheck.setBackground(null);
        } else {
            row.imgCheck.setImageDrawable(null);
            row.imgCheck.setBackgroundResource(R.drawable.bg_circle_outline);
            row.imgCheck.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(c, R.color.divider)));
        }
    }
}
