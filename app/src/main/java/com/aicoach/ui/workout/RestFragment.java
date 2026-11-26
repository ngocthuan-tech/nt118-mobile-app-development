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
import com.aicoach.databinding.FragmentRestBinding;
import com.aicoach.pose.ExerciseRule;
import com.aicoach.pose.ExerciseRules;
import com.aicoach.pose.SetResult;

import java.util.Locale;

/**
 * Figma 32 "Nghỉ giữa hiệp" (Trưởng nhóm): đếm ngược 30 giây, hết giờ tự quay lại màn tập.
 * Số liệu hiệp vừa rồi lấy từ WorkoutSessionViewModel (màn Tập ghi vào trước khi chuyển sang đây).
 * Giọng nói "Còn 10 giây", "3, 2, 1" chỉ đọc khi bật "Đếm ngược & giờ nghỉ" trong Cài đặt giọng nói.
 */
public class RestFragment extends BaseFragment<FragmentRestBinding> {

    private static final int REST_SECONDS = 30;
    private static final int ADD_SECONDS = 10;

    private CountDownTimer timer;
    private long remainingMs = REST_SECONDS * 1000L;
    private int totalSeconds = REST_SECONDS;
    private VoiceCoach voice;
    private long lastSpokenSec = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        voice = new VoiceCoach(requireContext(), null);
    }

    @Override
    protected FragmentRestBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentRestBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnSkip.setOnClickListener(v -> backToWorkout());
        binding.btnAddTime.setOnClickListener(v -> {
            remainingMs += ADD_SECONDS * 1000L;
            totalSeconds += ADD_SECONDS;
            startTimer();
        });

        bindSetResult();
        startTimer();
    }

    /** Hiệp vừa rồi + hiệp tiếp theo — số liệu thật từ AI. */
    private void bindSetResult() {
        String exerciseId = RestFragmentArgs.fromBundle(requireArguments()).getExerciseId();
        WorkoutSessionViewModel sessionVm = WorkoutSessionViewModel.of(this);
        WorkoutQueue.Step step = sessionVm.stepFor(exerciseId);
        ExerciseRule rule = step != null ? ExerciseRules.create(exerciseId, step.sets, step.target)
                : ExerciseRules.create(exerciseId);
        SetResult set = sessionVm.lastSet();

        int doneSet = set != null ? set.setNumber : 1;
        int sets = set != null ? set.sets : rule.sets;
        binding.chipSetDone.setText(getString(R.string.workout_set_done, rule.name, doneSet, sets));

        if (set != null) {
            binding.tvLastReps.setText(getString(R.string.ai_progress_count, set.done, set.target));
            binding.tvLastScore.setText(String.valueOf(set.score()));
            binding.tvLastErrors.setText(String.valueOf(set.errors));
        } else {
            binding.tvLastReps.setText("—");
            binding.tvLastScore.setText("—");
            binding.tvLastErrors.setText("—");
        }

        int next = Math.min(doneSet + 1, sets);
        binding.tvNextTitle.setText(getString(R.string.ai_next_title, next, sets, rule.name));
        binding.tvNextMeta.setText(rule.timed
                ? getString(R.string.ai_next_seconds, rule.target)
                : getString(R.string.ai_next_reps, rule.target));
    }

    private void startTimer() {
        if (timer != null) timer.cancel();
        binding.progressRest.setMax(totalSeconds * 1000);
        binding.tvRestTotal.setText(getString(R.string.workout_rest_left, totalSeconds));

        timer = new CountDownTimer(remainingMs, 100) {
            @Override
            public void onTick(long msLeft) {
                remainingMs = msLeft;
                if (binding == null) return;
                long sec = (msLeft + 999) / 1000;
                binding.tvRestTime.setText(String.format(Locale.US, "%02d:%02d", sec / 60, sec % 60));
                binding.progressRest.setProgress((int) msLeft);
                speakCountdown(sec);
            }

            @Override
            public void onFinish() {
                if (binding != null) backToWorkout();
            }
        }.start();
    }

    private void speakCountdown(long sec) {
        if (sec == lastSpokenSec) return;
        lastSpokenSec = sec;
        if (sec == 10) {
            voice.say("Còn 10 giây", false, VoiceCoach.Kind.REST);
        } else if (sec >= 1 && sec <= 3) {
            voice.say(String.valueOf(sec), true, VoiceCoach.Kind.REST);
        }
    }

    private void backToWorkout() {
        NavHostFragment.findNavController(this).navigateUp();
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
