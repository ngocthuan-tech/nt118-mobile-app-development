package com.aicoach.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.common.Resource;
import com.aicoach.databinding.FragmentEditProfileBinding;
import com.aicoach.util.SimpleTextWatcher;

/** Returns to the profile only after the server accepts the changes. */
public class EditProfileFragment extends BaseFragment<FragmentEditProfileBinding> {
    static final String UPDATED = "profile_updated";
    private EditProfileViewModel vm;
    private androidx.activity.OnBackPressedCallback savingBack;
    private boolean filled;
    @Override protected FragmentEditProfileBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentEditProfileBinding.inflate(inflater, container, false);
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        filled = false;
        vm = new ViewModelProvider(this).get(EditProfileViewModel.class);
        savingBack = new androidx.activity.OnBackPressedCallback(false) {
            @Override public void handleOnBackPressed() { /* Wait for the pending save. */ }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), savingBack);
        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.btnSave.setOnClickListener(v -> save());
        binding.btnSaveTop.setOnClickListener(v -> save());
        binding.btnRetryEdit.setOnClickListener(v -> vm.load());
        binding.btnDeleteAccount.setOnClickListener(v -> showError(getString(R.string.profile_delete_unavailable)));
        dropdown(binding.ddGoal, new String[]{getString(R.string.profile_goal_lose), getString(R.string.profile_goal_build), getString(R.string.profile_goal_fit)});
        dropdown(binding.ddLevel, new String[]{getString(R.string.profile_level_beginner), getString(R.string.profile_level_intermediate), getString(R.string.profile_level_advanced)});
        dropdown(binding.ddSessions, new String[]{"1", "2", "3", "4", "5", "6", "7"});
        binding.ddGoal.setOnItemClickListener((p, v, pos, id) -> { vm.form.goal = EditProfileForm.GOALS[pos]; buttons(); });
        binding.ddLevel.setOnItemClickListener((p, v, pos, id) -> { vm.form.level = EditProfileForm.LEVELS[pos]; buttons(); });
        binding.ddSessions.setOnItemClickListener((p, v, pos, id) -> { vm.form.sessions = pos + 1; buttons(); });
        binding.etName.addTextChangedListener(SimpleTextWatcher.after(this::changed));
        binding.etHeight.addTextChangedListener(SimpleTextWatcher.after(this::changed));
        binding.etWeight.addTextChangedListener(SimpleTextWatcher.after(this::changed));
        vm.state().observe(getViewLifecycleOwner(), r -> {
            if (vm.form != null && !filled) fill();
            binding.editProgress.setVisibility(vm.busy ? View.VISIBLE : View.GONE);
            boolean error = r != null && r.status == Resource.Status.ERROR;
            binding.editError.setVisibility(error ? View.VISIBLE : View.GONE);
            binding.editError.setText(error ? r.message : null);
            binding.btnRetryEdit.setVisibility(error && vm.form == null ? View.VISIBLE : View.GONE);
            buttons(); finishSave();
        });
    }
    private void dropdown(AutoCompleteTextView field, String[] labels) {
        field.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, labels));
    }
    private void fill() {
        EditProfileForm f = vm.form;
        binding.etName.setText(f.name); binding.etHeight.setText(f.height); binding.etWeight.setText(f.weight);
        binding.etEmail.setText(f.original.getEmail()); binding.tvAvatar.setText(f.original.getInitial());
        select(binding.ddGoal, EditProfileForm.GOALS, f.goal);
        select(binding.ddLevel, EditProfileForm.LEVELS, f.level);
        binding.ddSessions.setText(String.valueOf(f.sessions), false);
        filled = true;
    }
    private void select(AutoCompleteTextView field, String[] keys, String selected) {
        for (int i = 0; i < keys.length; i++) if (keys[i].equals(selected)) {
            field.setText(String.valueOf(field.getAdapter().getItem(i)), false); return;
        }
    }
    private void changed() {
        if (!filled || vm.busy || vm.form == null) return;
        vm.form.name = binding.etName.getText().toString();
        vm.form.height = binding.etHeight.getText().toString();
        vm.form.weight = binding.etWeight.getText().toString();
        binding.tilName.setError(null); binding.tilHeight.setError(null); binding.tilWeight.setError(null);
        buttons();
    }
    private void buttons() {
        boolean enabled = vm.form != null && !vm.busy && !vm.saved;
        binding.tilName.setEnabled(enabled); binding.tilHeight.setEnabled(enabled); binding.tilWeight.setEnabled(enabled);
        binding.tilGoal.setEnabled(enabled); binding.tilLevel.setEnabled(enabled); binding.tilSessions.setEnabled(enabled);
        binding.btnSave.setEnabled(enabled && vm.form.dirty()); binding.btnSaveTop.setEnabled(enabled && vm.form.dirty());
        binding.btnBack.setEnabled(!vm.busy);
        savingBack.setEnabled(vm.busy && vm.form != null);
    }
    private void save() {
        if (vm.form == null || vm.busy) return;
        EditProfileForm f = vm.form;
        binding.tilName.setError(f.validName() ? null : getString(R.string.profile_edit_name_error));
        binding.tilHeight.setError(f.validHeight() ? null : getString(R.string.profile_edit_height_error));
        binding.tilWeight.setError(f.validWeight() ? null : getString(R.string.profile_edit_weight_error));
        vm.save();
    }
    private void finishSave() {
        if (vm.saved && isResumed() && !getParentFragmentManager().isStateSaved()) {
            vm.saved = false;
            getParentFragmentManager().setFragmentResult(UPDATED, new Bundle());
            NavHostFragment.findNavController(this).popBackStack();
        }
    }
    @Override public void onResume() { super.onResume(); if (vm != null) finishSave(); }
}
