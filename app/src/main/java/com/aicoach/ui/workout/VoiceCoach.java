package com.aicoach.ui.workout;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aicoach.data.local.SettingsPrefs;
import com.aicoach.ui.profile.VoicePreferences;

import java.util.Locale;
import java.util.Set;

/**
 * Huấn luyện viên giọng nói tiếng Việt dùng TextToSpeech có sẵn của Android (chạy offline, miễn phí).
 *  - Làm theo Cài đặt giọng nói (Hồ sơ → Giọng nói & âm thanh): bật/tắt chung, giọng đọc, tốc độ, âm lượng,
 *    và bật/tắt từng loại nhắc (đếm lần lặp, sai tư thế, khen ngợi, đếm ngược & giờ nghỉ). Đọc lại cài đặt
 *    ở mỗi câu nên đổi xong là có hiệu lực ngay.
 *  - Lời nhắc lỗi (urgent) cắt ngang câu đang đọc để người tập nghe ngay.
 *  - Đếm số thì không cắt ngang lời nhắc lỗi đang đọc.
 *  - Máy không có giọng tiếng Việt → báo 1 lần qua onUnavailable, app vẫn chạy bình thường (chỉ hiện chữ).
 */
public final class VoiceCoach implements TextToSpeech.OnInitListener {

    /** Loại câu nói — khớp các ô "AI sẽ nhắc khi" ở Cài đặt giọng nói. INFO (bắt đầu, xong hiệp, hướng dẫn) luôn đọc. */
    public enum Kind {
        INFO(null), COUNT("count"), FORM("form"), PRAISE("praise"), REST("rest");

        @Nullable final String key;

        Kind(@Nullable String key) {
            this.key = key;
        }
    }

    private static final String TAG = "VoiceCoach";
    private static final Locale VIETNAMESE = new Locale("vi", "VN");

    private final TextToSpeech tts;
    private final VoicePreferences prefs;
    @Nullable private final Runnable onUnavailable;
    private boolean ready;
    private boolean muted;
    private boolean lastWasUrgent;
    private long lastUrgentEndGuessMs;
    private int utteranceSeq;
    @Nullable private String pending;
    @Nullable private Kind pendingKind;

    public VoiceCoach(@NonNull Context context, @Nullable Runnable onUnavailable) {
        this.onUnavailable = onUnavailable;
        this.prefs = new VoicePreferences(context);
        // Tắt "Nhắc bằng giọng nói" trong Cài đặt → vào màn tập là im lặng (vẫn bật lại được bằng nút loa)
        this.muted = !new SettingsPrefs(context).isVoiceEnabled();
        this.tts = new TextToSpeech(context.getApplicationContext(), this);
    }

    @Override
    public void onInit(int status) {
        if (status != TextToSpeech.SUCCESS) {
            Log.w(TAG, "Không khởi tạo được TextToSpeech: " + status);
            if (onUnavailable != null) onUnavailable.run();
            return;
        }
        int result = tts.setLanguage(VIETNAMESE);
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w(TAG, "Máy chưa có giọng đọc tiếng Việt");
            if (onUnavailable != null) onUnavailable.run();
            return;
        }
        applyChosenVoice();
        ready = true;
        if (pending != null) {
            say(pending, true, pendingKind == null ? Kind.INFO : pendingKind);
            pending = null;
            pendingKind = null;
        }
    }

    /** Giọng người dùng đã chọn ở Cài đặt (nếu máy còn giọng đó); không thì giữ giọng tiếng Việt mặc định. */
    private void applyChosenVoice() {
        String name = prefs.voice();
        if (name == null || name.isEmpty()) return;
        try {
            Set<Voice> voices = tts.getVoices();
            if (voices == null) return;
            for (Voice v : voices) {
                if (name.equals(v.getName())) {
                    tts.setVoice(v);
                    return;
                }
            }
        } catch (RuntimeException e) {
            Log.w(TAG, "Không đổi được giọng đã chọn, dùng giọng mặc định", e);
        }
    }

    /** Câu thông tin (luôn đọc nếu không tắt tiếng). */
    public boolean say(@NonNull String text, boolean urgent) {
        return say(text, urgent, Kind.INFO);
    }

    /**
     * Đọc một câu.
     * @param urgent true = lời nhắc lỗi / thông báo quan trọng (cắt ngang câu đang đọc)
     * @param kind   loại câu — người dùng tắt loại này trong Cài đặt thì không đọc
     * @return false nếu câu bị bỏ vì tắt tiếng / tắt loại nhắc / đang đọc lời nhắc lỗi
     */
    public boolean say(@NonNull String text, boolean urgent, @NonNull Kind kind) {
        String clean = text.replace("“", "").replace("”", "").replace("\"", "").trim();
        if (clean.isEmpty() || muted || !allowed(kind)) return false;
        if (!ready) {
            pending = clean;
            pendingKind = kind;
            return true;
        }
        long now = System.currentTimeMillis();
        // Đừng để tiếng đếm số cắt mất lời nhắc lỗi vừa đọc
        if (!urgent && lastWasUrgent && now < lastUrgentEndGuessMs) return false;

        tts.setSpeechRate(prefs.speed());
        Bundle params = new Bundle();
        params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, prefs.volume() / 100f);
        // Luôn FLUSH: số đếm mới thay số cũ, không để hàng đợi dồn khiến giọng nói trễ so với động tác
        tts.speak(clean, TextToSpeech.QUEUE_FLUSH, params, "aicoach-" + (utteranceSeq++));
        lastWasUrgent = urgent;
        if (urgent) lastUrgentEndGuessMs = now + 250L + (long) (clean.length() * 70L / Math.max(0.5f, prefs.speed()));
        return true;
    }

    /** Loại câu này có được bật trong Cài đặt giọng nói không. */
    public boolean allowed(@NonNull Kind kind) {
        return kind.key == null || prefs.enabled(kind.key);
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        if (muted && ready) tts.stop();
    }

    public boolean isMuted() {
        return muted;
    }

    public void stop() {
        if (ready) tts.stop();
    }

    /** Gọi khi rời màn hình để giải phóng bộ đọc. */
    public void shutdown() {
        tts.stop();
        tts.shutdown();
    }
}
