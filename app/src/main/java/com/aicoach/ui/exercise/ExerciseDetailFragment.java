package com.aicoach.ui.exercise;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.NavRootDirections;
import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.common.ExerciseImages;
import com.aicoach.common.Resource;
import com.aicoach.data.model.Exercise;
import com.aicoach.databinding.FragmentExerciseDetailBinding;
import com.aicoach.databinding.ItemExerciseCheckBinding;
import com.aicoach.databinding.ItemExerciseStepBinding;
import com.aicoach.databinding.ViewExerciseStatBinding;

import java.util.Arrays;
import java.util.List;

/**
 * Figma 25.1–25.9 Chi tiết bài tập — 1 layout dùng chung cho 9 bài, nội dung lấy từ GET exercises/{id}.
 * "Bắt đầu tập" → action_global_to_readiness(exerciseId) sang luồng tập AI của Trưởng nhóm.
 */
public class ExerciseDetailFragment extends BaseFragment<FragmentExerciseDetailBinding> {

    private static final int LIBRARY_SIZE = 9;

    private String exerciseId;
    private ExerciseDetailViewModel vm;

    @Override
    protected FragmentExerciseDetailBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentExerciseDetailBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ExerciseDetailFragmentArgs args = ExerciseDetailFragmentArgs.fromBundle(requireArguments());
        exerciseId = args.getExerciseId();
        vm = new ViewModelProvider(this).get(ExerciseDetailViewModel.class);

        // Ảnh hiện ngay từ id, không chờ API
        ExerciseImages.bind(binding.imgExercise, exerciseId);
        binding.tvCameraHint.setText(ExerciseUi.cameraHint(exerciseId));
        binding.tvPosition.setVisibility(args.getPosition() > 0 ? View.VISIBLE : View.GONE);
        binding.tvPosition.setText(getString(R.string.exercise_position, args.getPosition(), LIBRARY_SIZE));
        fillChecks(binding.listAiChecks, Arrays.asList(getResources().getStringArray(ExerciseUi.aiChecks(exerciseId))),
                R.drawable.ic_check_24, R.color.state_success, R.color.state_success_light);

        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.btnStart.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(NavRootDirections.actionGlobalToReadiness(exerciseId)));
        binding.btnRetry.setOnClickListener(v -> vm.load(exerciseId));
        binding.btnShare.setOnClickListener(v -> share());

        vm.getExercise().observe(getViewLifecycleOwner(), this::render);
        vm.load(exerciseId);
    }

    private void render(@Nullable Resource<Exercise> r) {
        boolean loading = r == null || r.status == Resource.Status.LOADING;
        boolean error = r != null && r.status == Resource.Status.ERROR;
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.errorState.setVisibility(error ? View.VISIBLE : View.GONE);
        binding.content.setVisibility(error ? View.INVISIBLE : View.VISIBLE);
        if (error) binding.tvError.setText(r.message);
        if (r != null && r.status == Resource.Status.SUCCESS && r.data != null) bind(r.data);
    }

    private void bind(@NonNull Exercise e) {
        binding.tvName.setText(e.getName());
        binding.tvDescription.setText(e.getDescription());
        if (e.getImageName() != null) ExerciseImages.bind(binding.imgExercise, e.getImageName());

        binding.tagGroup.setText(ExerciseUi.groupLabel(e.getMuscleGroup()));
        binding.tagDifficulty.setText(ExerciseUi.difficultyLabel(e.getDifficulty()));
        binding.tagDifficulty.setTextColor(color(ExerciseUi.difficultyColor(e.getDifficulty())));
        binding.tagDifficulty.setBackgroundTintList(ColorStateList.valueOf(color(ExerciseUi.difficultyBg(e.getDifficulty()))));
        binding.tagAi.setText(ExerciseUi.aiModeLabel(e.isTimeBased()));

        int sets = e.getDefaultSets();
        int reps = e.getDefaultReps();
        bindStat(binding.statTime, R.drawable.ic_schedule_24, R.color.brand_primary,
                getString(R.string.exercise_stat_time_value, ExerciseUi.estimatedMinutes(sets, reps, e.isTimeBased())),
                getString(R.string.exercise_stat_time));
        bindStat(binding.statKcal, R.drawable.ic_fire_24, R.color.medal_orange,
                getString(R.string.exercise_stat_kcal_value, ExerciseUi.estimatedKcal(e)),
                getString(R.string.exercise_stat_kcal));
        bindStat(binding.statVolume, R.drawable.ic_target_24, R.color.state_success,
                getString(R.string.exercise_stat_volume_value, sets, reps),
                getString(e.isTimeBased() ? R.string.exercise_stat_volume_seconds : R.string.exercise_stat_volume_reps));

        binding.listSteps.removeAllViews();
        List<String> steps = orEmpty(e.getSteps());
        for (int i = 0; i < steps.size(); i++) {
            ItemExerciseStepBinding row = ItemExerciseStepBinding.inflate(getLayoutInflater(), binding.listSteps, true);
            row.tvNumber.setText(String.valueOf(i + 1));
            row.tvText.setText(steps.get(i));
        }
        fillChecks(binding.listTips, orEmpty(e.getTips()), R.drawable.ic_sparkle_24, R.color.brand_primary,
                R.color.brand_primary_light);
        fillChecks(binding.listMistakes, orEmpty(e.getCommonMistakes()), R.drawable.ic_alert_24, R.color.state_error,
                R.color.state_error_light);
    }

    private void bindStat(@NonNull ViewExerciseStatBinding stat, @DrawableRes int icon, @ColorRes int tint,
                          @NonNull String value, @NonNull String label) {
        stat.imgIcon.setImageResource(icon);
        stat.imgIcon.setImageTintList(ColorStateList.valueOf(color(tint)));
        stat.tvValue.setText(value);
        stat.tvLabel.setText(label);
    }

    private void fillChecks(@NonNull LinearLayout list, @NonNull List<String> lines, @DrawableRes int icon,
                            @ColorRes int tint, @ColorRes int bg) {
        list.removeAllViews();
        for (String line : lines) {
            ItemExerciseCheckBinding row = ItemExerciseCheckBinding.inflate(getLayoutInflater(), list, true);
            row.imgIcon.setImageResource(icon);
            row.imgIcon.setImageTintList(ColorStateList.valueOf(color(tint)));
            row.iconBox.setBackgroundTintList(ColorStateList.valueOf(color(bg)));
            row.tvText.setText(line);
        }
    }

    private void share() {
        Resource<Exercise> r = vm.getExercise().getValue();
        String name = r != null && r.data != null ? r.data.getName() : exerciseId;
        Intent send = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, getString(R.string.exercise_share_text, name));
        startActivity(Intent.createChooser(send, getString(R.string.exercise_share)));
    }

    @NonNull
    private static List<String> orEmpty(@Nullable List<String> list) {
        return list != null ? list : java.util.Collections.emptyList();
    }

    private int color(@ColorRes int res) {
        return ContextCompat.getColor(requireContext(), res);
    }
}
