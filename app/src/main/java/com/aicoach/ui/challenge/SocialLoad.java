package com.aicoach.ui.challenge;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.aicoach.common.Resource;
import java.util.function.Consumer;

/**
 * Một luồng dữ liệu trong ViewModel: yêu cầu mới thay yêu cầu cũ (bấm đổi tab nhanh không bị kết quả cũ đè),
 * dữ liệu tốt gần nhất vẫn giữ trong {@link #data} khi đang tải lại hoặc lỗi mạng.
 */
final class SocialLoad<T> {
    final MediatorLiveData<Resource<T>> state = new MediatorLiveData<>();
    @Nullable T data;
    @Nullable private LiveData<Resource<T>> source;

    void run(LiveData<Resource<T>> request) {
        run(request, null);
    }

    void run(LiveData<Resource<T>> request, @Nullable Consumer<T> onSuccess) {
        if (source != null) state.removeSource(source);
        source = request;
        state.setValue(Resource.loading());
        state.addSource(request, value -> {
            if (value == null || value.status == Resource.Status.LOADING) return;
            state.removeSource(request);
            if (source == request) source = null;
            if (value.status == Resource.Status.SUCCESS) {
                data = value.data;
                if (onSuccess != null) onSuccess.accept(value.data);
            }
            state.setValue(value);
        });
    }

    boolean busy() {
        return source != null;
    }

    /** Kết quả một lần (tạo, mời, tham gia…) đã xử lý xong → xoá để xoay màn / quay lại không chạy lại. */
    void consume() {
        if (source != null) {
            state.removeSource(source);
            source = null;
        }
        state.setValue(null);
    }
}
