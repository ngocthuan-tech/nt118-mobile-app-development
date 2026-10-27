package com.aicoach.ui.exercise;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.common.Resource;
import com.aicoach.data.model.Exercise;
import com.aicoach.databinding.FragmentExerciseLibraryBinding;
import com.aicoach.util.SimpleTextWatcher;
import com.google.android.material.chip.Chip;

import java.util.List;

/**
 * Figma 23 Thư viện bài tập (tab "Bài tập") — GET exercises, tìm không dấu, chip nhóm cơ, nút Bộ lọc (24).
 * Bấm ô bài tập → 25 Chi tiết.
 */
public class ExerciseLibraryFragment extends BaseFragment<FragmentExerciseLibraryBinding> {

    private static final long SEARCH_DEBOUNCE_MS = 350;

    private ExerciseLibraryViewModel vm;
    private ExerciseGridAdapter adapter;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearch;
    private boolean bindingChips;

    @Override
    protected FragmentExerciseLibraryBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentExerciseLibraryBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController nav = NavHostFragment.findNavController(this);
        vm = new ViewModelProvider(nav.getViewModelStoreOwner(R.id.nav_main)).get(ExerciseLibraryViewModel.class);

        adapter = new ExerciseGridAdapter(e -> nav.navigate(
                ExerciseLibraryFragmentDirections.actionLibraryToExerciseDetail(e.getId())
                        .setPosition(vm.positionOf(e.getId()))));
        binding.rvExercises.setAdapter(adapter);

        buildGroupChips();
        binding.btnFilter.setOnClickListener(v -> nav.navigate(R.id.action_library_to_filter));
        binding.btnEmptyAction.setOnClickListener(v -> {
            Resource<List<Exercise>> r = vm.getState().getValue();
            if (r != null && r.status == Resource.Status.ERROR) {
                vm.retry();
            } else {
                binding.etSearch.setText(null);
                vm.clearFilter();
            }
        });

        binding.etSearch.addTextChangedListener(SimpleTextWatcher.after(() -> {
            if (pendingSearch != null) handler.removeCallbacks(pendingSearch);
            String text = binding.etSearch.getText() == null ? null : binding.etSearch.getText().toString();
            pendingSearch = () -> vm.search(text);
            handler.postDelayed(pendingSearch, SEARCH_DEBOUNCE_MS);
        }));
        binding.etSearch.setOnEditorActionListener((tv, actionId, e) -> {
            if (actionId != EditorInfo.IME_ACTION_SEARCH) return false;
            if (pendingSearch != null) handler.removeCallbacks(pendingSearch);
            vm.search(tv.getText() == null ? null : tv.getText().toString());
            return true;
        });

        vm.getState().observe(getViewLifecycleOwner(), r -> renderState());
        vm.getVisible().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            renderState();
        });
        vm.getFilter().observe(getViewLifecycleOwner(), f -> {
            syncChips(f);
            binding.dotFilter.setVisibility(f.difficulties.isEmpty() && f.measure == null && !f.noEquipment
                    && f.groups.size() <= 1 ? View.GONE : View.VISIBLE);
            renderState();
        });
    }

    @Override
    public void onDestroyView() {
        if (pendingSearch != null) handler.removeCallbacks(pendingSearch);
        super.onDestroyView();
    }

    /** Chip "Tất cả (9)" + 4 nhóm cơ; tag của chip = mã nhóm (null = tất cả). */
    private void buildGroupChips() {
        binding.groupChips.removeAllViews();
        addChip(null, getString(R.string.exercise_group_all_count, vm.getTotalCount()));
        for (String g : ExerciseUi.GROUPS) addChip(g, getString(ExerciseUi.groupLabel(g)));
        binding.groupChips.setOnCheckedStateChangeListener((group, ids) -> {
            if (bindingChips || ids.isEmpty()) return;
            Chip chip = group.findViewById(ids.get(0));
            vm.selectGroup(chip != null ? (String) chip.getTag() : null);
        });
    }

    private void addChip(@Nullable String group, @NonNull String text) {
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.view_exercise_group_chip, binding.groupChips, false);
        chip.setId(View.generateViewId());
        chip.setText(text);
        chip.setTag(group);
        binding.groupChips.addView(chip);
    }

    /** Bộ lọc đổi (từ màn 24) → chọn lại đúng chip; chọn nhiều nhóm thì không chip nào sáng ngoài "Tất cả". */
    private void syncChips(@NonNull ExerciseFilter f) {
        bindingChips = true;
        String selected = f.groups.size() == 1 ? f.groups.iterator().next() : null;
        for (int i = 0; i < binding.groupChips.getChildCount(); i++) {
            Chip chip = (Chip) binding.groupChips.getChildAt(i);
            Object tag = chip.getTag();
            boolean on = selected == null ? (tag == null && f.groups.isEmpty()) : selected.equals(tag);
            chip.setChecked(on);
        }
        bindingChips = false;
    }

    private void renderState() {
        Resource<List<Exercise>> r = vm.getState().getValue();
        List<Exercise> list = vm.getVisible().getValue();
        boolean loading = r == null || r.status == Resource.Status.LOADING;
        boolean error = r != null && r.status == Resource.Status.ERROR;
        boolean empty = !loading && !error && (list == null || list.isEmpty());

        binding.progress.setVisibility(loading && (list == null || list.isEmpty()) ? View.VISIBLE : View.GONE);
        binding.emptyState.setVisibility(error || empty ? View.VISIBLE : View.GONE);
        if (error) {
            binding.imgEmpty.setImageResource(R.drawable.ic_alert_24);
            binding.tvEmptyTitle.setText(r.message);
            binding.tvEmptyDesc.setVisibility(View.GONE);
            binding.btnEmptyAction.setText(R.string.common_retry);
        } else if (empty) {
            binding.imgEmpty.setImageResource(R.drawable.ic_search_24);
            binding.tvEmptyTitle.setText(R.string.exercise_empty);
            binding.tvEmptyDesc.setVisibility(View.VISIBLE);
            binding.btnEmptyAction.setText(R.string.exercise_clear_filters);
        }

        int total = vm.getTotalCount();
        binding.tvSubtitle.setText(getString(R.string.exercise_library_subtitle, total));
        if (binding.groupChips.getChildCount() > 0) {
            ((Chip) binding.groupChips.getChildAt(0)).setText(getString(R.string.exercise_group_all_count, total));
        }
    }
}
