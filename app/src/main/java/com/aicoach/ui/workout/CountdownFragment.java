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
import com.aicoach.pose.ExerciseRule;
import com.aicoach.pose.ExerciseRules;

/** Figma 29 "Đếm ngược bắt đầu" (Trưởng nhóm): 3 giây rồi tự sang màn tập. */
public class CountdownFragment extends BaseFragment<FragmentCountdownBinding> {

    private static final long COUNTDOWN_MS = 3000;
    private static final long TICK_MS = 50;

    private CountDownTimer timer;
    private String exerciseId;
    private VoiceCoach voice;
    private int lastSpoken = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        voice = new VoiceCoach(requireContext(), null);
    }

    @Override
    protected FragmentCountdownBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentCountdownBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        exerciseId = CountdownFragmentArgs.fromBundle(requireArguments()).getExerciseId();

        WorkoutQueue.Step step = WorkoutSessionViewModel.of(this).stepFor(exerciseId);
        ExerciseRule rule = step != null ? ExerciseRules.create(exerciseId, step.sets, step.target)
                : ExerciseRules.create(exerciseId);
        binding.chipReady.setText(getString(R.string.workout_ready_count, 3, 3));
        binding.tvMeta.setText(getString(rule.timed ? R.string.ai_countdown_timed : R.string.workout_countdown_meta,
                rule.name, 1, rule.sets, rule.target));
        VoicePrompt.show(binding.voicePrompt, VoicePrompt.Tone.INFO, "“Bắt đầu sau 3 giây!”");

        binding.btnCancel.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        startCountdown();
    }

    private void startCountdown() {
        timer = new CountDownTimer(COUNTDOWN_MS, TICK_MS) {
            @Override
            public void onTick(long msLeft) {
                if (binding == null) return;
                int sec = (int) (msLeft / 1000) + 1;
                binding.tvCountdown.setText(String.valueOf(sec));
                if (sec != lastSpoken) {
                    lastSpoken = sec;
                    voice.say(String.valueOf(sec), true, VoiceCoach.Kind.REST);
                }
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

    @Override
    public void onDestroy() {
        voice.shutdown();
        super.onDestroy();
    }
}
