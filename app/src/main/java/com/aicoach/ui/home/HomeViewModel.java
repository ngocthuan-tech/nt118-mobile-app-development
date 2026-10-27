package com.aicoach.ui.home;

import android.app.Application;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.aicoach.common.Resource;
import com.aicoach.data.model.HomeData;
import com.aicoach.data.repository.HomeRepository;

/**
 * Figma 21 — gọi GET home một lần trong constructor (xoay màn hình không gọi lại).
 * Quay về từ màn tập / chi tiết thì làm mới nếu dữ liệu đã cũ hơn 30 giây (streak có thể vừa tăng).
 */
public class HomeViewModel extends AndroidViewModel {

    private static final long STALE_MS = 30_000;

    private final HomeRepository repo;
    private final MediatorLiveData<Resource<HomeData>> home = new MediatorLiveData<>();
    private LiveData<Resource<HomeData>> source;
    private long loadedAt;
    private HomeData last;

    public HomeViewModel(@NonNull Application app) {
        super(app);
        repo = new HomeRepository(app);
        load();
    }

    public LiveData<Resource<HomeData>> getHome() { return home; }

    /** Dữ liệu lần tải thành công gần nhất — giữ màn hình không bị trống khi đang tải lại. */
    public HomeData getLast() { return last; }

    public void load() {
        if (source != null) home.removeSource(source);
        source = repo.getHome();
        LiveData<Resource<HomeData>> src = source;
        home.addSource(src, r -> {
            if (r.status == Resource.Status.SUCCESS) {
                last = r.data;
                loadedAt = SystemClock.elapsedRealtime();
            }
            home.setValue(r);
            if (r.status != Resource.Status.LOADING) home.removeSource(src);
        });
    }

    public void refreshIfStale() {
        Resource<HomeData> r = home.getValue();
        boolean loading = r != null && r.status == Resource.Status.LOADING;
        if (!loading && loadedAt > 0 && SystemClock.elapsedRealtime() - loadedAt > STALE_MS) load();
    }
}
