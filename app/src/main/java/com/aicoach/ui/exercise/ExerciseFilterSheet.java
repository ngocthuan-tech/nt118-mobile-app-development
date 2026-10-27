package com.aicoach.ui.exercise;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.R;
import com.aicoach.databinding.FragmentExerciseFilterBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Set;

/**
 * Figma 24 Bộ lọc — bottom sheet (destination dạng &lt;dialog&gt; trong nav_main).
 * Sửa trên bản nháp; nút "Áp dụng • N bài tập" đếm trực tiếp trên danh sách đang có rồi mới áp vào Thư viện.
 */
public class ExerciseFilterSheet extends BottomSheetDialogFragment {

    private static final String KEY_DRAFT_GROUPS = "draft_groups";
    private static final String KEY_DRAFT_DIFFS = "draft_diffs";
    private static final String KEY_DRAFT_MEASURE = "draft_measure";
    private static final String KEY_DRAFT_NO_EQUIP = "draft_no_equip";

    private FragmentExerciseFilterBinding binding;
    private ExerciseLibraryViewModel vm;
    private ExerciseFilter draft;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentExerciseFilterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vm = new ViewModelProvider(NavHostFragment.findNavController(this).getViewModelStoreOwner(R.id.nav_main))
                .get(ExerciseLibraryViewModel.class);
        draft = savedInstanceState != null ? restore(savedInstanceState) : vm.currentFilter().copy();

        for (String g : ExerciseUi.GROUPS) {
            addChip(binding.groupMuscle, g, ExerciseUi.groupLabel(g), draft.groups);
        }
        addChip(binding.groupDifficulty, ExerciseUi.DIFF_EASY, R.string.exercise_difficulty_easy, draft.difficulties);
        addChip(binding.groupDifficulty, ExerciseUi.DIFF_MEDIUM, R.string.exercise_difficulty_medium, draft.difficulties);
        addChip(binding.groupDifficulty, ExerciseUi.DIFF_HARD, R.string.exercise_difficulty_hard, draft.difficulties);

        bindMode(binding.btnModeAll, null);
        bindMode(binding.btnModeReps, ExerciseUi.MEASURE_REPS);
        bindMode(binding.btnModeSeconds, ExerciseUi.MEASURE_SECONDS);

        binding.switchNoEquipment.setChecked(draft.noEquipment);
        binding.switchNoEquipment.setOnCheckedChangeListener((b, checked) -> {
            draft.noEquipment = checked;
            render();
        });

        binding.btnReset.setOnClickListener(v -> {
            draft = new ExerciseFilter();
            uncheckAll(binding.groupMuscle);
            uncheckAll(binding.groupDifficulty);
            binding.switchNoEquipment.setChecked(false);
            render();
        });
        binding.btnApply.setOnClickListener(v -> {
            vm.applyFilter(draft);
            dismiss();
        });
        render();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        if (draft == null) return;
        out.putStringArray(KEY_DRAFT_GROUPS, draft.groups.toArray(new String[0]));
        out.putStringArray(KEY_DRAFT_DIFFS, draft.difficulties.toArray(new String[0]));
        out.putString(KEY_DRAFT_MEASURE, draft.measure);
        out.putBoolean(KEY_DRAFT_NO_EQUIP, draft.noEquipment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @NonNull
    private static ExerciseFilter restore(@NonNull Bundle in) {
        ExerciseFilter f = new ExerciseFilter();
        String[] groups = in.getStringArray(KEY_DRAFT_GROUPS);
        String[] diffs = in.getStringArray(KEY_DRAFT_DIFFS);
        if (groups != null) java.util.Collections.addAll(f.groups, groups);
        if (diffs != null) java.util.Collections.addAll(f.difficulties, diffs);
        f.measure = in.getString(KEY_DRAFT_MEASURE);
        f.noEquipment = in.getBoolean(KEY_DRAFT_NO_EQUIP);
        return f;
    }

    private void addChip(@NonNull ChipGroup group, @NonNull String key, @StringRes int label,
                         @NonNull Set<String> target) {
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.view_exercise_filter_chip, group, false);
        chip.setId(View.generateViewId());
        chip.setText(label);
        chip.setChecked(target.contains(key));
        chip.setOnCheckedChangeListener((c, checked) -> {
            Set<String> set = group == binding.groupMuscle ? draft.groups : draft.difficulties;
            if (checked) set.add(key);
            else set.remove(key);
            render();
        });
        group.addView(chip);
    }

    private static void uncheckAll(@NonNull ChipGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) ((Chip) group.getChildAt(i)).setChecked(false);
    }

    private void bindMode(@NonNull MaterialButton button, @Nullable String measure) {
        button.setOnClickListener(v -> {
            draft.measure = measure;
            render();
        });
    }

    private void render() {
        if (binding == null) return;
        binding.btnModeAll.setChecked(draft.measure == null);
        binding.btnModeReps.setChecked(ExerciseUi.MEASURE_REPS.equals(draft.measure));
        binding.btnModeSeconds.setChecked(ExerciseUi.MEASURE_SECONDS.equals(draft.measure));
        int count = vm.countFor(draft);
        binding.btnApply.setText(getString(R.string.exercise_filter_apply, count));
        binding.btnApply.setEnabled(count > 0);
    }
}
