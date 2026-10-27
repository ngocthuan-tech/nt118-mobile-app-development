package com.aicoach.ui.exercise;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.aicoach.common.Resource;
import com.aicoach.data.model.Exercise;
import com.aicoach.data.repository.ExerciseRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Thư viện (23) + Bộ lọc (24) dùng chung ViewModel này, gắn với graph nav_main
 * (ViewModelProvider(navController.getViewModelStoreOwner(R.id.nav_main))).
 * Từ khoá tìm kiếm → server tìm không dấu (GET exercises?q=); bộ lọc → lọc tại app.
 */
public class ExerciseLibraryViewModel extends AndroidViewModel {

    private final ExerciseRepository repo;
    private final MediatorLiveData<Resource<List<Exercise>>> raw = new MediatorLiveData<>();
    private final MutableLiveData<ExerciseFilter> filter = new MutableLiveData<>(new ExerciseFilter());
    private final MediatorLiveData<List<Exercise>> visible = new MediatorLiveData<>();
    private LiveData<Resource<List<Exercise>>> source;
    @Nullable private String query;
    private int totalCount = 9;

    public ExerciseLibraryViewModel(@NonNull Application app) {
        super(app);
        repo = new ExerciseRepository(app);
        visible.addSource(raw, r -> recompute());
        visible.addSource(filter, f -> recompute());
        load(null);
    }

    public LiveData<Resource<List<Exercise>>> getState() { return raw; }

    /** Danh sách sau khi lọc — thứ Thư viện hiển thị. */
    public LiveData<List<Exercise>> getVisible() { return visible; }

    public LiveData<ExerciseFilter> getFilter() { return filter; }

    @NonNull
    public ExerciseFilter currentFilter() {
        ExerciseFilter f = filter.getValue();
        return f != null ? f : new ExerciseFilter();
    }

    /** Tổng số bài của thư viện (không tính từ khoá) — cho chip "Tất cả (9)". */
    public int getTotalCount() { return totalCount; }

    @Nullable
    public String getQuery() { return query; }

    public void search(@Nullable String text) {
        String q = text == null ? null : text.trim();
        if (q != null && q.isEmpty()) q = null;
        if (q == null ? query == null : q.equals(query)) return;
        load(q);
    }

    public void retry() { load(query); }

    private void load(@Nullable String q) {
        query = q;
        if (source != null) raw.removeSource(source);
        LiveData<Resource<List<Exercise>>> src = repo.getExercises(q);
        source = src;
        raw.addSource(src, r -> {
            if (r.status == Resource.Status.SUCCESS && r.data != null && query == null) totalCount = r.data.size();
            raw.setValue(r);
            if (r.status != Resource.Status.LOADING) raw.removeSource(src);
        });
    }

    /** Chip nhóm cơ ở màn 23: chọn 1 nhóm (null = Tất cả). */
    public void selectGroup(@Nullable String group) {
        ExerciseFilter f = currentFilter().copy();
        f.groups.clear();
        if (group != null) f.groups.add(group);
        filter.setValue(f);
    }

    /** Màn 24 bấm "Áp dụng". */
    public void applyFilter(@NonNull ExerciseFilter f) { filter.setValue(f.copy()); }

    public void clearFilter() { filter.setValue(new ExerciseFilter()); }

    /** Số bài khớp bản nháp bộ lọc — nút "Áp dụng • 5 bài tập". */
    public int countFor(@NonNull ExerciseFilter draft) {
        Resource<List<Exercise>> r = raw.getValue();
        return draft.apply(r != null ? r.data : null).size();
    }

    /** Vị trí bài trong thư viện ("Bài 3/9" ở màn 25). */
    public int positionOf(@NonNull String exerciseId) {
        Resource<List<Exercise>> r = raw.getValue();
        if (r == null || r.data == null || query != null) return 0;
        for (int i = 0; i < r.data.size(); i++) if (exerciseId.equals(r.data.get(i).getId())) return i + 1;
        return 0;
    }

    private void recompute() {
        Resource<List<Exercise>> r = raw.getValue();
        visible.setValue(r != null && r.data != null ? currentFilter().apply(r.data) : new ArrayList<>());
    }
}
