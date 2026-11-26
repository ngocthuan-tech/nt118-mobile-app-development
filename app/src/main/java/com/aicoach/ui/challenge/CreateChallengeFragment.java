package com.aicoach.ui.challenge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.common.ExerciseImages;
import com.aicoach.common.Resource;
import com.aicoach.data.model.Challenge;
import com.aicoach.databinding.FragmentCreateChallengeBinding;
import com.aicoach.databinding.ItemChallengeExerciseBinding;
import com.aicoach.ui.history.SegmentGroup;
import java.util.Arrays;
import java.util.List;

/** 41 Tạo thử thách → server tạo + sinh mã mời → sang 42 Mời bạn bè (bỏ màn 41 khỏi back stack). */
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
                if (binding == null || binding.rvExercises.getAdapter() == null) return;
                if (old >= 0) binding.rvExercises.getAdapter().notifyItemChanged(old);
                binding.rvExercises.getAdapter().notifyItemChanged(pos);
            });
        });
        binding.rvExercises.setAdapter(adapter);
        adapter.submit(EXERCISES);
        int[] days = {3, 7, 14, 30};
        SegmentGroup.bind(Arrays.asList(3, 7, 14, 30).indexOf(vm.days()), index -> vm.days(days[index]),
                binding.btnDays3, binding.btnDays7, binding.btnDays14, binding.btnDays30);
        binding.tvTarget.setText(String.valueOf(vm.target()));
        binding.tvReward.setText(R.string.social_reward);
        binding.etName.addTextChangedListener(com.aicoach.util.SimpleTextWatcher.after(() -> {
            if (binding != null && binding.tilName.getError() != null) {
                int length = binding.etName.getText().toString().trim().length();
                if (length >= 3 && length <= 50) binding.tilName.setError(null);
            }
        }));
        binding.btnMinus.setOnClickListener(v -> adjustTarget(-10));
        binding.btnPlus.setOnClickListener(v -> adjustTarget(10));
        binding.btnContinue.setOnClickListener(v -> {
            String name = binding.etName.getText() == null ? "" : binding.etName.getText().toString();
            if (!vm.create(name, binding.switchPrivate.isChecked())) {
                binding.tilName.setError(getString(R.string.social_name_validation));
            }
        });
        vm.createState().observe(getViewLifecycleOwner(), this::onCreated);
        renderButton();
    }

    private void onCreated(@Nullable Resource<Challenge> r) {
        renderButton();
        if (r == null || r.status == Resource.Status.LOADING) return;
        vm.consumeCreate();
        if (r.status == Resource.Status.SUCCESS && r.data != null) {
            NavOptions popCreate = new NavOptions.Builder()
                    .setPopUpTo(R.id.createChallengeFragment, true)
                    .build();
            NavHostFragment.findNavController(this).navigate(
                    CreateChallengeFragmentDirections.actionCreateToInvite().setChallengeId(r.data.getId()), popCreate);
        } else {
            showError(r.message);
        }
    }

    private void renderButton() {
        if (binding == null) return;
        boolean busy = vm.creating();
        binding.btnContinue.setEnabled(!busy);
        binding.btnContinue.setText(busy ? R.string.social_creating : R.string.social_continue_invite);
    }

    private void adjustTarget(int delta) {
        vm.target(vm.target() + delta);
        binding.tvTarget.setText(String.valueOf(vm.target()));
    }

    public static int exerciseLabel(String id) {
        switch (id == null ? "" : id) {
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
