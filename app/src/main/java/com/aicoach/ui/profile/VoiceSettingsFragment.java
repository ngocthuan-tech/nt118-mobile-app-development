package com.aicoach.ui.profile;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;
import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.data.local.SettingsPrefs;
import com.aicoach.databinding.FragmentVoiceSettingsBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Uses installed voices. Android's Voice metadata cannot certify gender or southern accent. */
public class VoiceSettingsFragment extends BaseFragment<FragmentVoiceSettingsBinding> {
    private final Handler main = new Handler(Looper.getMainLooper());
    private final List<Voice> voices = new ArrayList<>();
    private TextToSpeech tts;
    private VoicePreferences prefs;
    private SettingsPrefs settings;
    private int generation;
    private androidx.appcompat.app.AlertDialog voiceDialog;
    private final ActivityResultLauncher<Intent> installVoice = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (binding != null) initializeEngine();
            });

    @Override protected FragmentVoiceSettingsBinding inflate(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container) {
        return FragmentVoiceSettingsBinding.inflate(inflater, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        prefs = new VoicePreferences(requireContext());
        settings = new SettingsPrefs(requireContext());
        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.switchVoice.setChecked(settings.isVoiceEnabled());
        binding.switchVoice.setOnCheckedChangeListener((button, on) -> {
            settings.setVoiceEnabled(on);
            if (!on && tts != null) tts.stop();
        });
        binding.btnChooseVoice.setOnClickListener(v -> chooseVoice());
        binding.btnVoiceSystemSettings.setOnClickListener(v -> {
            try { installVoice.launch(new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)); }
            catch (ActivityNotFoundException e) { showError(getString(R.string.profile_voice_unavailable)); }
        });
        binding.sliderSpeed.setValue(prefs.speed());
        binding.sliderVolume.setValue(prefs.volume());
        labels();
        binding.sliderSpeed.addOnChangeListener((slider, value, user) -> {
            prefs.speed(value); labels();
        });
        binding.sliderVolume.addOnChangeListener((slider, value, user) -> {
            prefs.volume(value); labels();
        });
        CompoundButton[] boxes = {binding.cbCount, binding.cbForm, binding.cbPraise, binding.cbRest};
        String[] keys = {"count", "form", "praise", "rest"};
        for (int i = 0; i < boxes.length; i++) {
            String key = keys[i];
            boxes[i].setChecked(prefs.enabled(key));
            boxes[i].setOnCheckedChangeListener((button, on) -> prefs.enabled(key, on));
        }
        initializeEngine();
    }

    private void initializeEngine() {
        releaseEngine();
        final int request = generation;
        binding.btnChooseVoice.setEnabled(false);
        binding.tvVoiceStatus.setText(R.string.profile_voice_loading);
        // Post even an immediate failure: the constructor must finish assigning tts first.
        tts = new TextToSpeech(requireContext().getApplicationContext(), result -> main.post(() -> {
            if (binding == null || request != generation || tts == null) return;
            if (result != TextToSpeech.SUCCESS) { unavailable(); return; }
            if (tts.getVoices() != null) {
                for (Voice voice : tts.getVoices()) {
                    if ("vi".equals(voice.getLocale().getLanguage())) voices.add(voice);
                }
            }
            voices.sort(Comparator.comparing(Voice::isNetworkConnectionRequired).thenComparing(Voice::getName));
            if (voices.isEmpty()) { unavailable(); return; }
            Voice selected = voices.get(0);
            for (Voice voice : voices) if (voice.getName().equals(prefs.voice())) selected = voice;
            if (tts.setVoice(selected) == TextToSpeech.ERROR) { unavailable(); return; }
            binding.btnChooseVoice.setEnabled(true);
            updateStatus(selected);
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override public void onStart(String id) { }
                @Override public void onDone(String id) { }
                @Override public void onError(String id) {
                    main.post(() -> {
                        if (binding != null && request == generation) unavailable();
                    });
                }
            });
        }));
    }

    private void labels() {
        binding.tvSpeed.setText(String.format(Locale.getDefault(), "%.1f×", binding.sliderSpeed.getValue()));
        binding.tvVolume.setText(String.format(Locale.getDefault(), "%.0f%%", binding.sliderVolume.getValue()));
    }

    private void updateStatus(Voice voice) {
        binding.tvVoiceStatus.setText(getString(R.string.profile_voice_actual,
                voice.getName() + " · " + getString(voice.isNetworkConnectionRequired()
                        ? R.string.profile_voice_network : R.string.profile_voice_local)));
    }

    private void unavailable() { binding.tvVoiceStatus.setText(R.string.profile_voice_unavailable); }

    private void chooseVoice() {
        if (tts == null || voices.isEmpty()) return;
        String[] names = new String[voices.size()];
        for (int i = 0; i < names.length; i++) names[i] = voices.get(i).getName();
        voiceDialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.profile_voice_select_actual).setItems(names, (dialog, index) -> {
                    if (binding == null || tts == null) return;
                    Voice voice = voices.get(index);
                    if (tts.setVoice(voice) == TextToSpeech.ERROR) { unavailable(); return; }
                    prefs.voice(voice.getName());
                    updateStatus(voice);
                    if (!settings.isVoiceEnabled()) {
                        showError(getString(R.string.profile_voice_enable_first)); return;
                    }
                    tts.setSpeechRate(prefs.speed());
                    Bundle params = new Bundle();
                    params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, prefs.volume() / 100f);
                    if (tts.speak(getString(R.string.profile_voice_sample_actual),
                            TextToSpeech.QUEUE_FLUSH, params, "preview") == TextToSpeech.ERROR) unavailable();
                }).show();
    }

    private void releaseEngine() {
        generation++;
        if (tts != null) { tts.stop(); tts.shutdown(); tts = null; }
        voices.clear();
    }

    @Override public void onStop() {
        if (tts != null) tts.stop();
        super.onStop();
    }

    @Override public void onDestroyView() {
        if (voiceDialog != null) { voiceDialog.dismiss(); voiceDialog = null; }
        releaseEngine();
        super.onDestroyView();
    }
}
