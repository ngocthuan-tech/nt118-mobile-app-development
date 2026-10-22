package com.aicoach.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aicoach.common.ErrorMapper;
import com.aicoach.common.Resource;
import com.aicoach.data.remote.ApiResponse;

import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Lớp cha của mọi Repository gọi API. Gọi Retrofit bất đồng bộ và trả về LiveData&lt;Resource&lt;T&gt;&gt;:
 * LOADING ngay lập tức → SUCCESS(data) hoặc ERROR(câu tiếng Việt).
 * <pre>
 * public LiveData&lt;Resource&lt;User&gt;&gt; getMe() { return execute(api.getMe()); }
 * </pre>
 */
public abstract class BaseRepository {

    protected <T> LiveData<Resource<T>> execute(@NonNull Call<ApiResponse<T>> call) {
        return execute(call, null);
    }

    /** onSuccess chạy trước khi phát SUCCESS — dùng để lưu token sau khi đăng nhập. */
    protected <T> LiveData<Resource<T>> execute(@NonNull Call<ApiResponse<T>> call,
                                                @Nullable Consumer<T> onSuccess) {
        MutableLiveData<Resource<T>> result = new MutableLiveData<>(Resource.loading());
        call.enqueue(new Callback<ApiResponse<T>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<T>> c, @NonNull Response<ApiResponse<T>> response) {
                ApiResponse<T> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess()) {
                    if (onSuccess != null && body.getData() != null) onSuccess.accept(body.getData());
                    result.setValue(Resource.success(body.getData()));
                } else {
                    result.setValue(Resource.error(ErrorMapper.fromResponse(response)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<T>> c, @NonNull Throwable t) {
                result.setValue(Resource.error(ErrorMapper.fromThrowable(t)));
            }
        });
        return result;
    }

    /** Màn chưa có API (giữa kỳ dùng dữ liệu mẫu) — cùng kiểu trả về để sau này chỉ đổi 1 dòng. */
    protected <T> LiveData<Resource<T>> mock(T data) {
        return new MutableLiveData<>(Resource.success(data));
    }
}
