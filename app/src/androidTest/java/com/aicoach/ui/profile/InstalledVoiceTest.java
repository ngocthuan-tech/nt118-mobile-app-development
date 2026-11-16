package com.aicoach.ui.profile;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.util.Comparator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/** Device integration test; skips when the emulator has no installed Vietnamese voice. */
@RunWith(AndroidJUnit4.class)
public class InstalledVoiceTest {
    @Test public void installedVietnameseVoiceCanSynthesizeAudio() throws Exception {
        AtomicReference<TextToSpeech> engine = new AtomicReference<>();
        AtomicBoolean initialized = new AtomicBoolean(), synthesized = new AtomicBoolean();
        CountDownLatch ready = new CountDownLatch(1), done = new CountDownLatch(1);
        Handler main = new Handler(Looper.getMainLooper());
        android.content.Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File audio = new File(context.getExternalFilesDir("b-completion-test"), "voice-preview.wav");
        try {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> engine.set(new TextToSpeech(context, status -> {
                initialized.set(status == TextToSpeech.SUCCESS); ready.countDown();
            })));
            assertTrue("TTS initialization timed out", ready.await(20, TimeUnit.SECONDS));
            assumeTrue("No usable TTS engine on this device", initialized.get());
            TextToSpeech tts = engine.get();
            Voice voice = tts.getVoices().stream().filter(v -> "vi".equals(v.getLocale().getLanguage())
                    && !v.isNetworkConnectionRequired()).sorted(Comparator.comparing(Voice::getName)).findFirst().orElse(null);
            assumeTrue("No Vietnamese device voice installed", voice != null);
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override public void onStart(String id) { }
                @Override public void onDone(String id) { synthesized.set(true); done.countDown(); }
                @Override public void onError(String id) { done.countDown(); }
            });
            assertEquals(TextToSpeech.SUCCESS, tts.setVoice(voice));
            assertEquals(TextToSpeech.SUCCESS, tts.synthesizeToFile("Mình cùng tập nhé! Giữ lưng thẳng, bạn làm tốt lắm!",
                    new Bundle(), audio, "voice-test"));
            assertTrue("Voice generation timed out", done.await(30, TimeUnit.SECONDS));
            assertTrue("TTS engine could not generate speech", synthesized.get());
            assertTrue("Generated audio is empty", audio.length() > 44);
            Bundle evidence = new Bundle(); evidence.putString("voice", voice.getName());
            evidence.putLong("audioBytes", audio.length());
            InstrumentationRegistry.getInstrumentation().sendStatus(0, evidence);
        } finally {
            main.post(() -> { if (engine.get() != null) { engine.get().stop(); engine.get().shutdown(); } });
        }
    }
}
