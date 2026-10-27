package com.aicoach.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

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

        // 3 giọng đọc: chỉ chọn 1 (viền xanh + radio). TODO(B): lưu lựa chọn vào SettingsPrefs và đọc lại khi mở màn.
        View[] options = {binding.optionFemaleNorth, binding.optionMaleNorth, binding.optionFemaleSouth};
        RadioButton[] radios = {binding.radioFemaleNorth, binding.radioMaleNorth, binding.radioFemaleSouth};
        for (int i = 0; i < options.length; i++) {
            final int index = i;
            options[i].setOnClickListener(v -> selectVoice(index, options, radios));
        }
        selectVoice(0, options, radios);
        // TODO(B): btnPlay* nghe thử (TextToSpeech), 2 Slider cập nhật tvSpeed / tvVolume, 4 ô cb* lưu lại.
    }

    private static void selectVoice(int index, View[] options, RadioButton[] radios) {
        for (int i = 0; i < options.length; i++) {
            options[i].setSelected(i == index);
            radios[i].setChecked(i == index);
        }
    }
}
