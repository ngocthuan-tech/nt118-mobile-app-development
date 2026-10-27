package com.aicoach.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.common.BaseFragment;
import com.aicoach.databinding.FragmentVoiceSettingsBinding;

/**
 * Figma 45 Giọng nói & âm thanh.
 *
 * ĐÃ CÓ: giao diện + điều hướng giữa các màn.
 * B TỰ VIẾT (xem TODO): đọc/ghi SettingsPrefs (switchVoice — module AI của Trưởng nhóm đọc cờ này), chỉ cho chọn 1 trong 3 giọng, nghe thử bằng TextToSpeech, cập nhật tvSpeed/tvVolume theo 2 Slider, lưu 4 ô cbCount/cbForm/cbPraise/cbRest.
 */
public class VoiceSettingsFragment extends BaseFragment<FragmentVoiceSettingsBinding> {

    @Override
    protected FragmentVoiceSettingsBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentVoiceSettingsBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController nav = NavHostFragment.findNavController(this);
        binding.btnBack.setOnClickListener(v -> nav.popBackStack());
    }
}
