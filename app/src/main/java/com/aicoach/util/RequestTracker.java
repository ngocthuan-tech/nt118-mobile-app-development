package com.aicoach.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.aicoach.common.Resource;

/**
 * Gói 1 request gửi đi (đăng nhập, lưu khảo sát…) cho ViewModel (Thành viên A).
 * <ul>
 *   <li>Nhận LiveData từ Repository, đẩy LOADING → SUCCESS/ERROR ra UI rồi tự gỡ nguồn.</li>
 *   <li>{@link #consume()} xoá kết quả sau khi đã chuyển màn → quay lại màn này không bị chuyển lần nữa.</li>
 *   <li>Giữ trong ViewModel nên xoay màn hình không gửi lại request.</li>
 * </ul>
 */
public class RequestTracker<T> {

    private final MediatorLiveData<Resource<T>> state = new MediatorLiveData<>();
    @Nullable
    private LiveData<Resource<T>> current;

    @NonNull
    public LiveData<Resource<T>> getState() {
        return state;
    }

    public boolean isLoading() {
        Resource<T> r = state.getValue();
        return r != null && r.status == Resource.Status.LOADING;
    }

    /** Bỏ qua nếu request trước chưa xong (chống bấm 2 lần). */
    public void track(@NonNull LiveData<Resource<T>> source) {
        if (isLoading()) return;
        if (current != null) state.removeSource(current);
        current = source;
        state.addSource(source, r -> {
            state.setValue(r);
            if (r != null && r.status != Resource.Status.LOADING) {
                state.removeSource(source);
                if (current == source) current = null;
            }
        });
    }

    /** Lỗi do app tự kiểm tra (chưa gửi lên server). */
    public void fail(@NonNull String message) {
        state.setValue(Resource.error(message));
    }

    public void consume() {
        state.setValue(null);
    }
}
