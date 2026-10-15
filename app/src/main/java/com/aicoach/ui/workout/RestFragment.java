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

import java.util.Locale;

/** Figma 32 "Nghỉ giữa hiệp" (Trưởng nhóm): đếm ngược 30 giây, hết giờ tự quay lại màn tập. */
public class RestFragment extends BaseFragment<FragmentRestBinding> {

    private static final int REST_SECONDS = 30;
    private static final int ADD_SECONDS = 10;

    private CountDownTimer timer;
    private long remainingMs = REST_SECONDS * 1000L;
    private int totalSeconds = REST_SECONDS;

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

        bindDemoData();
        startTimer();
    }

    /** Dữ liệu mẫu giống Figma 32. */
    private void bindDemoData() {
        binding.chipSetDone.setText(getString(R.string.workout_set_done, "Squat", 2, 3));
        binding.tvLastReps.setText("15/15");
        binding.tvLastScore.setText("90");
        binding.tvLastErrors.setText("1");
        binding.tvNextTitle.setText("Hiệp 3/3 – Squat");
        binding.tvNextMeta.setText("15 lần • Giữ nhịp 2 giây/lần");
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
            }

            @Override
            public void onFinish() {
                if (binding != null) backToWorkout();
            }
        }.start();
    }

    private void backToWorkout() {
        NavHostFragment.findNavController(this).navigateUp();
    }

    @Override
    public void onDestroyView() {
        if (timer != null) timer.cancel();
        super.onDestroyView();
    }
}
