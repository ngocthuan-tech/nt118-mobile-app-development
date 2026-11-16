package com.aicoach.ui.challenge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.common.ExerciseImages;
import com.aicoach.databinding.FragmentCreateChallengeBinding;
import com.aicoach.databinding.ItemChallengeExerciseBinding;
import com.aicoach.ui.history.SegmentGroup;
import java.util.Arrays;
import java.util.List;

public class CreateChallengeFragment extends BaseFragment<FragmentCreateChallengeBinding> {
    private SocialViewModel vm;
    private static final List<String> EXERCISES = Arrays.asList("squat", "pushup", "lunge", "jumping_jack");
    @Override protected FragmentCreateChallengeBinding inflate(@NonNull LayoutInflater i, @Nullable ViewGroup c) {
        return FragmentCreateChallengeBinding.inflate(i, c, false);
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        vm = new ViewModelProvider(this).get(SocialViewModel.class);
        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        SocialRows<String> adapter = new SocialRows<>(R.layout.item_challenge_exercise, id -> id, (row, id, pos) -> {
            ItemChallengeExerciseBinding b = ItemChallengeExerciseBinding.bind(row);
            b.tvName.setText(exerciseLabel(id));
            ExerciseImages.bind(b.img, id);
            b.imgCheck.setVisibility(id.equals(vm.exercise()) ? View.VISIBLE : View.GONE);
            b.getRoot().setOnClickListener(v -> {
                int old = EXERCISES.indexOf(vm.exercise());
                vm.exercise(id);
                binding.rvExercises.getAdapter().notifyItemChanged(old);
                binding.rvExercises.getAdapter().notifyItemChanged(pos);
            });
        });
        binding.rvExercises.setAdapter(adapter);
        adapter.submit(EXERCISES);
        int[] days = {3, 7, 14, 30};
        SegmentGroup.bind(Arrays.asList(3, 7, 14, 30).indexOf(vm.days()), index -> vm.days(days[index]),
                binding.btnDays3, binding.btnDays7, binding.btnDays14, binding.btnDays30);
        binding.tvTarget.setText(String.valueOf(vm.target()));
        binding.tvReward.setText(R.string.social_demo_no_reward);
        binding.btnContinue.setText(R.string.social_create);
        binding.etName.addTextChangedListener(com.aicoach.util.SimpleTextWatcher.after(() -> {
            if (binding != null && binding.tilName.getError() != null) {
                int length = binding.etName.getText().toString().trim().length();
                if (length >= 3 && length <= 50) binding.tilName.setError(null);
            }
        }));
        binding.btnMinus.setOnClickListener(v -> adjustTarget(-10));
        binding.btnPlus.setOnClickListener(v -> adjustTarget(10));
        binding.btnContinue.setOnClickListener(v -> {
            String name = binding.etName.getText().toString();
            try {
                vm.create(name, binding.switchPrivate.isChecked());
                binding.btnContinue.setEnabled(false);
                NavHostFragment.findNavController(this).popBackStack(R.id.challengeListFragment, false);
            } catch (IllegalArgumentException e) {
                binding.tilName.setError(getString(R.string.social_name_validation));
            }
        });
    }
    private void adjustTarget(int delta) {
        vm.target(vm.target() + delta);
        binding.tvTarget.setText(String.valueOf(vm.target()));
    }
    public static int exerciseLabel(String id) {
        switch (id) {
            case "pushup": return R.string.social_exercise_pushup;
            case "lunge": return R.string.social_exercise_lunge;
            case "jumping_jack": return R.string.social_exercise_jumping_jack;
            default: return R.string.social_exercise_squat;
        }
    }
    @Override public void onDestroyView() {
        binding.rvExercises.setAdapter(null);
        super.onDestroyView();
    }
}
