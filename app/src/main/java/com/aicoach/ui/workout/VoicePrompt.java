package com.aicoach.ui.workout;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.aicoach.R;
import com.aicoach.databinding.ViewVoicePromptBinding;

/** Tô màu + đặt nội dung cho thẻ "Đang nhắc bằng giọng nói" (view_voice_prompt.xml). */
public final class VoicePrompt {

    /** Màu thẻ theo ngữ cảnh trong Figma. */
    public enum Tone {
        INFO(R.color.accent_cyan),        // Figma 29: đếm ngược
        COACH(R.color.state_warning),     // Figma 30: nhắc chỉnh nhẹ
        ERROR(R.color.state_error);       // Figma 31: sai tư thế

        @ColorRes final int color;

        Tone(@ColorRes int color) {
            this.color = color;
        }
    }

    private VoicePrompt() {
    }

    public static void show(@NonNull ViewVoicePromptBinding b, @NonNull Tone tone, @NonNull CharSequence text) {
        Context ctx = b.getRoot().getContext();
        int c = ContextCompat.getColor(ctx, tone.color);

        b.tvVoicePrompt.setText(text);
        b.tvVoiceLabel.setTextColor(c);
        b.voiceIconBg.setBackgroundTintList(ColorStateList.valueOf(c));
        for (int i = 0; i < b.voiceWave.getChildCount(); i++) {
            b.voiceWave.getChildAt(i).setBackgroundTintList(ColorStateList.valueOf(c));
        }

        Drawable bg = b.cardVoice.getBackground();
        if (bg instanceof GradientDrawable) {
            GradientDrawable card = (GradientDrawable) bg.mutate();
            int strokePx = Math.round(1.5f * ctx.getResources().getDisplayMetrics().density);
            card.setStroke(strokePx, c);
        }
        b.getRoot().setVisibility(View.VISIBLE);
    }
}
