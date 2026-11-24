package com.aicoach.ui.workout;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.R;

import com.aicoach.common.Resource;
import com.aicoach.data.model.HomeData;
import com.aicoach.data.model.WorkoutSession;
import com.aicoach.data.remote.dto.CreateSessionRequest;
import com.aicoach.data.repository.HomeRepository;
import com.aicoach.data.repository.SessionRepository;
import com.aicoach.pose.SetResult;
import com.aicoach.pose.WorkoutSummary;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * ViewModel dùng chung cho CẢ lượt tập (scope = graph nav_workout, Trưởng nhóm).
 *
 *  - Lượt tập = 1 bài (tập lẻ) hoặc nhiều bài (1 buổi giáo án, WorkoutQueue).
 *  - Xong MỖI bài là lưu ngay lên server (kèm planId + planDay) → thoát giữa chừng thì
 *    các bài đã xong vẫn được tính, lần sau vào buổi đó chỉ còn các bài chưa tập.
 *  - Màn Nghỉ, Kết quả, Chia sẻ đọc dữ liệu từ đây. Hết lượt (popBackStack nav_workout) thì ViewModel
 *    bị huỷ → lượt tập sau bắt đầu sạch, không bao giờ hiện lại kết quả cũ.
 *
 * Lấy trong Fragment:  new ViewModelProvider(nav.getViewModelStoreOwner(R.id.nav_workout))
 *                          .get(WorkoutSessionViewModel.class)
 */
public class WorkoutSessionViewModel extends AndroidViewModel {

    /** Không có buổi trước cùng bài để so sánh. */
    public static final int NO_PREVIOUS = -1;

    /** Một bài đã tập xong trong lượt + trạng thái lưu lên server. */
    public static final class Entry {
        public final WorkoutSummary summary;
        @Nullable private Resource<WorkoutSession> save;

        Entry(WorkoutSummary summary) {
            this.summary = summary;
        }

        @Nullable
        public Resource<WorkoutSession> save() {
            return save;
        }

        public boolean saved() {
            return save != null && save.status == Resource.Status.SUCCESS;
        }

        public boolean failed() {
            return save != null && save.status == Resource.Status.ERROR;
        }
    }

    private final SessionRepository sessions;
    private final HomeRepository home;

    // ---- lượt tập
    private boolean initialized;
    private WorkoutQueue queue = new WorkoutQueue(Collections.emptyList());
    @Nullable private String planId;
    private int planDay = -1;

    // ---- kết quả
    private final List<Entry> entries = new ArrayList<>();
    private final MutableLiveData<List<Entry>> entriesLive = new MutableLiveData<>(Collections.emptyList());
    @Nullable private SetResult lastSet;
    private final MutableLiveData<Integer> previousScore = new MutableLiveData<>();
    private final MutableLiveData<Integer> streak = new MutableLiveData<>();
    private boolean previousRequested;

    /** Các LiveData đang theo dõi bằng observeForever — gỡ hết khi ViewModel bị huỷ. */
    private final List<Runnable> cleanups = new ArrayList<>();

    public WorkoutSessionViewModel(@NonNull Application app) {
        super(app);
        sessions = new SessionRepository(app);
        home = new HomeRepository(app);
    }

    /** Lấy ViewModel chung của lượt tập (và nạp tham số queue/planId/planDay ở lần đầu). */
    @NonNull
    public static WorkoutSessionViewModel of(@NonNull Fragment fragment) {
        NavController nav = NavHostFragment.findNavController(fragment);
        WorkoutSessionViewModel vm = new ViewModelProvider(nav.getViewModelStoreOwner(R.id.nav_workout))
                .get(WorkoutSessionViewModel.class);
        vm.initFromArgs(nav.getBackStackEntry(R.id.nav_workout).getArguments());
        return vm;
    }

    // ------------------------------------------------------------------ lượt tập

    /** Đọc tham số của graph nav_workout (queue / planId / planDay) — chỉ lần đầu. */
    public void initFromArgs(@Nullable Bundle graphArgs) {
        if (initialized) return;
        initialized = true;
        if (graphArgs == null) return;
        queue = WorkoutQueue.decode(graphArgs.getString("queue"));
        String p = graphArgs.getString("planId");
        planId = p == null || p.trim().isEmpty() ? null : p.trim();
        planDay = graphArgs.getInt("planDay", -1);
    }

    public boolean isPlanRun() {
        return planId != null && planDay >= 0;
    }

    /** Lượt tập có từ 2 bài trở lên (để hiện "Bài 2/4"). */
    public boolean isMultiExercise() {
        return queue.size() > 1;
    }

    public int position() {
        return queue.position();
    }

    public int size() {
        return Math.max(1, queue.size());
    }

    /** Khối lượng (hiệp × mục tiêu) theo giáo án của bài; null = dùng mặc định của bài. */
    @Nullable
    public WorkoutQueue.Step stepFor(@NonNull String exerciseId) {
        return queue.find(exerciseId);
    }

    /** Bài kế tiếp sẽ tập (chưa chuyển), null nếu đây là bài cuối. */
    @Nullable
    public WorkoutQueue.Step peekNext() {
        return queue.peekNext();
    }

    /**
     * Gọi khi XONG một bài (đủ hiệp, hoặc bấm Kết thúc giữa chừng mà đã tập ít nhất 1 lần):
     * ghi kết quả, lưu ngay lên server.
     * @param continueRun true = tập tiếp bài kế (nếu còn); false = dừng lượt tập tại đây
     * @return bài kế tiếp cần tập, hoặc null nếu hết lượt
     */
    @Nullable
    public WorkoutQueue.Step onExerciseFinished(@Nullable WorkoutSummary summary, boolean continueRun) {
        if (summary != null && summary.totalReps > 0) {
            Entry e = new Entry(summary);
            entries.add(e);
            publishEntries();
            save(e);
        }
        if (!continueRun) return null;
        return queue.advance();
    }

    public boolean hasResults() {
        return !entries.isEmpty();
    }

    public List<Entry> entries() {
        return Collections.unmodifiableList(entries);
    }

    public LiveData<List<Entry>> entriesLive() {
        return entriesLive;
    }

    public void setLastSet(@Nullable SetResult set) {
        this.lastSet = set;
    }

    @Nullable
    public SetResult lastSet() {
        return lastSet;
    }

    // ------------------------------------------------------------------ lưu lên server

    private void save(Entry e) {
        WorkoutSummary s = e.summary;
        CreateSessionRequest body = new CreateSessionRequest();
        body.exerciseId = s.exerciseId;
        body.startedAt = Instant.ofEpochMilli(s.startedAtMs).toString();
        body.durationSec = s.durationSec;
        body.targetReps = s.targetReps;
        body.totalReps = s.totalReps;
        body.correctReps = Math.min(s.correctReps, s.totalReps);
        body.calories = null;   // server tự tính theo bài tập
        body.formErrors = new HashMap<>(s.formErrors);
        body.planId = isPlanRun() ? planId : null;
        body.planDay = isPlanRun() ? planDay : null;

        LiveData<Resource<WorkoutSession>> src = sessions.save(body);
        Observer<Resource<WorkoutSession>> obs = new Observer<Resource<WorkoutSession>>() {
            @Override
            public void onChanged(Resource<WorkoutSession> r) {
                e.save = r;
                publishEntries();
                if (r.status == Resource.Status.LOADING) return;
                src.removeObserver(this);
                if (r.status == Resource.Status.SUCCESS) onAnySaved(r.data);
            }
        };
        src.observeForever(obs);
        cleanups.add(() -> src.removeObserver(obs));
    }

    /** Nút "Thử lại" ở màn Kết quả: gửi lại các bài lưu lỗi (mất mạng...). */
    public void retryFailed() {
        for (Entry e : entries) {
            if (e.failed()) save(e);
        }
    }

    public int pendingCount() {
        int n = 0;
        for (Entry e : entries) if (e.save == null || e.save.status == Resource.Status.LOADING) n++;
        return n;
    }

    public int failedCount() {
        int n = 0;
        for (Entry e : entries) if (e.failed()) n++;
        return n;
    }

    private void onAnySaved(@Nullable WorkoutSession saved) {
        loadStreak();
        // So điểm với buổi trước chỉ có nghĩa khi lượt tập có đúng 1 bài
        if (entries.size() == 1 && !previousRequested && saved != null) {
            previousRequested = true;
            loadPreviousScore(entries.get(0).summary.exerciseId, saved.getId());
        }
    }

    /** Điểm (0–100) buổi trước cùng bài; NO_PREVIOUS nếu đây là buổi đầu; null khi đang tải. */
    public LiveData<Integer> previousScore() {
        return previousScore;
    }

    /** Chuỗi ngày mới nhất sau khi lưu (cho màn Chia sẻ); null khi chưa có. */
    public LiveData<Integer> streak() {
        return streak;
    }

    /** Tìm buổi gần nhất CÙNG bài (khác buổi vừa lưu) để so điểm — không bịa "tốt hơn x%". */
    private void loadPreviousScore(@NonNull String exerciseId, @Nullable String savedId) {
        LiveData<Resource<List<WorkoutSession>>> src = sessions.recent(50);
        Observer<Resource<List<WorkoutSession>>> obs = new Observer<Resource<List<WorkoutSession>>>() {
            @Override
            public void onChanged(Resource<List<WorkoutSession>> r) {
                if (r.status == Resource.Status.LOADING) return;
                src.removeObserver(this);
                int result = NO_PREVIOUS;
                if (r.status == Resource.Status.SUCCESS && r.data != null) {
                    for (WorkoutSession s : r.data) {   // server trả mới nhất trước
                        if (exerciseId.equals(s.getExerciseId()) && (savedId == null || !savedId.equals(s.getId()))) {
                            result = (int) Math.round(s.getAccuracy() * 100);
                            break;
                        }
                    }
                }
                previousScore.setValue(result);
            }
        };
        src.observeForever(obs);
        cleanups.add(() -> src.removeObserver(obs));
    }

    private void loadStreak() {
        LiveData<Resource<HomeData>> src = home.getHome();
        Observer<Resource<HomeData>> obs = new Observer<Resource<HomeData>>() {
            @Override
            public void onChanged(Resource<HomeData> r) {
                if (r.status == Resource.Status.LOADING) return;
                src.removeObserver(this);
                if (r.status == Resource.Status.SUCCESS && r.data != null) streak.setValue(r.data.getStreak());
            }
        };
        src.observeForever(obs);
        cleanups.add(() -> src.removeObserver(obs));
    }

    private void publishEntries() {
        entriesLive.setValue(new ArrayList<>(entries));
    }

    @Override
    protected void onCleared() {
        for (Runnable r : cleanups) r.run();
        cleanups.clear();
        super.onCleared();
    }
}
