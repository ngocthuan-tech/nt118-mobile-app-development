package com.aicoach.ui.history;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.aicoach.common.Resource;
import java.util.function.Supplier;

/** One in-flight request; successful data stays visible during refresh/errors. */
final class HistoryLoad<T> {
    final MediatorLiveData<Resource<T>> state = new MediatorLiveData<>();
    T data;
    boolean busy;
    private LiveData<Resource<T>> source;

    void run(Supplier<LiveData<Resource<T>>> loader, String emptyResponse) {
        if (busy) return;
        busy = true;
        state.setValue(Resource.loading());
        source = loader.get();
        LiveData<Resource<T>> request = source;
        state.addSource(request, value -> {
            if (value == null || value.status == Resource.Status.LOADING) return;
            state.removeSource(request); source = null; busy = false;
            if (value.status == Resource.Status.SUCCESS) {
                if (value.data == null) { state.setValue(Resource.error(emptyResponse)); return; }
                data = value.data;
            }
            state.setValue(value);
        });
    }

    void clear() { if (source != null) state.removeSource(source); }
}
