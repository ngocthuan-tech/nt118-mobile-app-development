package com.aicoach.ui.workout;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.databinding.FragmentCountdownBinding;

/** Figma 29 "Đếm ngược bắt đầu" (Trưởng nhóm): 3 giây rồi tự sang màn tập. */
public class CountdownFragment extends BaseFragment<FragmentCountdownBinding> {

    private static final long COUNTDOWN_MS = 3000;
    private static final long TICK_MS = 50;

    private CountDownTimer timer;
    private String exerciseId;

    @Override
    protected FragmentCountdownBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentCountdownBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        exerciseId = CountdownFragmentArgs.fromBundle(requireArguments()).getExerciseId();

        binding.chipReady.setText(getString(R.string.workout_ready_count, 3, 3));
        binding.tvMeta.setText(getString(R.string.workout_countdown_meta, "Squat", 1, 3, 15));
        VoicePrompt.show(binding.voicePrompt, VoicePrompt.Tone.INFO, "“Bắt đầu sau 3 giây!”");

        binding.btnCancel.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        startCountdown();
    }

    private void startCountdown() {
        timer = new CountDownTimer(COUNTDOWN_MS, TICK_MS) {
            @Override
            public void onTick(long msLeft) {
                if (binding == null) return;
                binding.tvCountdown.setText(String.valueOf((msLeft / 1000) + 1));
                binding.progressCountdown.setProgress((int) msLeft);
            }

            @Override
            public void onFinish() {
                if (binding == null) return;
                NavHostFragment.findNavController(CountdownFragment.this)
                        .navigate(CountdownFragmentDirections.actionCountdownToLive(exerciseId));
            }
        }.start();
    }

    @Override
    public void onDestroyView() {
        if (timer != null) timer.cancel();
        super.onDestroyView();
    }
}
