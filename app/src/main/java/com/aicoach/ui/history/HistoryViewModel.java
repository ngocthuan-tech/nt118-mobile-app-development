package com.aicoach.ui.history;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import com.aicoach.R;
import com.aicoach.common.Resource;
import com.aicoach.data.model.WorkoutSession;
import com.aicoach.data.repository.HistoryRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.function.Supplier;

public class HistoryViewModel extends AndroidViewModel {
    private final HistoryLoad<List<WorkoutSession>> load = new HistoryLoad<>();
    private final Supplier<LiveData<Resource<List<WorkoutSession>>>> loader;
    private final SavedStateHandle saved;

    public HistoryViewModel(Application app, SavedStateHandle saved) {
        this(app, saved, new HistoryRepository(app)::getSessions);
    }
    HistoryViewModel(Application app, SavedStateHandle saved,
            Supplier<LiveData<Resource<List<WorkoutSession>>>> loader) {
        super(app); this.saved = saved; this.loader = loader; refresh();
    }
    public LiveData<Resource<List<WorkoutSession>>> state() { return load.state; }
    public List<WorkoutSession> sessions() { return HistoryStats.sorted(load.data); }
    public boolean hasData() { return load.data != null; }
    public void refresh() { load.run(loader, getApplication().getString(R.string.progress_response_empty)); }
    public int range() { Integer value = saved.get("range"); return value == null ? 0 : value; }
    public void range(int value) { saved.set("range", value); }
    public YearMonth month() {
        String value = saved.get("month"); return value == null ? YearMonth.now() : YearMonth.parse(value);
    }
    public void moveMonth(int delta) { saved.set("month", month().plusMonths(delta).toString()); saved.set("day", null); }
    public LocalDate day() { String value = saved.get("day"); return value == null ? null : LocalDate.parse(value); }
    public void day(LocalDate value) { saved.set("day", value == null ? null : value.toString()); }
    @Override protected void onCleared() { load.clear(); }
}
