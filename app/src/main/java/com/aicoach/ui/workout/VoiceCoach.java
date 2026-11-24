package com.aicoach.ui.workout;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

/**
 * Huấn luyện viên giọng nói tiếng Việt dùng TextToSpeech có sẵn của Android (chạy offline, miễn phí).
 *  - Lời nhắc lỗi (urgent) cắt ngang câu đang đọc để người tập nghe ngay.
 *  - Đếm số thì không cắt ngang lời nhắc lỗi đang đọc.
 *  - Máy không có giọng tiếng Việt → báo 1 lần qua onUnavailable, app vẫn chạy bình thường (chỉ hiện chữ).
 */
public final class VoiceCoach implements TextToSpeech.OnInitListener {

    private static final String TAG = "VoiceCoach";
    private static final Locale VIETNAMESE = new Locale("vi", "VN");

    private final TextToSpeech tts;
    @Nullable private final Runnable onUnavailable;
    private boolean ready;
    private boolean muted;
    private boolean lastWasUrgent;
    private long lastUrgentEndGuessMs;
    private int utteranceSeq;
    @Nullable private String pending;

    public VoiceCoach(@NonNull Context context, @Nullable Runnable onUnavailable) {
        this.onUnavailable = onUnavailable;
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
        tts.setSpeechRate(1.1f);
        ready = true;
        if (pending != null) {
            say(pending, true);
            pending = null;
        }
    }

    /**
     * Đọc một câu.
     * @param urgent true = lời nhắc lỗi / thông báo quan trọng (cắt ngang câu đang đọc)
     */
    public void say(@NonNull String text, boolean urgent) {
        String clean = text.replace("“", "").replace("”", "").replace("\"", "").trim();
        if (clean.isEmpty() || muted) return;
        if (!ready) {
            pending = clean;
            return;
        }
        long now = System.currentTimeMillis();
        // Đừng để tiếng đếm số cắt mất lời nhắc lỗi vừa đọc
        if (!urgent && lastWasUrgent && now < lastUrgentEndGuessMs) return;

        // Luôn FLUSH: số đếm mới thay số cũ, không để hàng đợi dồn khiến giọng nói trễ so với động tác
        tts.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "aicoach-" + (utteranceSeq++));
        lastWasUrgent = urgent;
        if (urgent) lastUrgentEndGuessMs = now + 250L + clean.length() * 70L;
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
