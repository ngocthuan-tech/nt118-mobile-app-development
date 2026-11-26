package com.aicoach.data.repository;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.aicoach.common.Resource;
import com.aicoach.data.model.Challenge;
import com.aicoach.data.model.Social;
import com.aicoach.data.remote.ApiClient;
import com.aicoach.data.remote.api.SocialApi;
import com.aicoach.data.remote.dto.SocialDtos.CreateChallengeRequest;
import com.aicoach.data.remote.dto.SocialDtos.InviteRequest;
import com.aicoach.data.remote.dto.SocialDtos.JoinRequest;
import com.aicoach.data.remote.dto.SocialDtos.RespondRequest;
import java.util.Collection;
import java.util.List;

/**
 * Cộng đồng — mọi dữ liệu lấy từ server (không giữ bản sao trên máy, nên đăng xuất / đổi tài khoản
 * không bao giờ thấy dữ liệu của người trước). Mỗi hàm trả LiveData: LOADING → SUCCESS / ERROR.
 */
public class SocialRepository extends BaseRepository {

    public static final String SCOPE_FRIENDS = "friends";
    public static final String SCOPE_NATIONAL = "national";

    private final SocialApi api;

    public SocialRepository(Context context) {
        api = ApiClient.create(context.getApplicationContext(), SocialApi.class);
    }

    // ---------------------------------------------------------------- Thử thách

    public LiveData<Resource<List<Challenge>>> challenges() {
        return execute(api.getChallenges());
    }

    public LiveData<Resource<Challenge>> challenge(String id) {
        return execute(api.getChallenge(id));
    }

    public LiveData<Resource<Challenge>> create(String title, String exerciseId, int dailyTarget, int days,
                                                boolean privateGroup) {
        return execute(api.createChallenge(
                new CreateChallengeRequest(title, exerciseId, dailyTarget, days, privateGroup)));
    }

    public LiveData<Resource<Challenge>> join(String code) {
        return execute(api.join(new JoinRequest(code)));
    }

    public LiveData<Resource<List<Social.Candidate>>> candidates(String challengeId, @Nullable String query) {
        String q = query == null || query.trim().isEmpty() ? null : query.trim();
        return execute(api.getCandidates(challengeId, q));
    }

    public LiveData<Resource<Social.InviteResult>> invite(String challengeId, Collection<String> userIds) {
        return execute(api.invite(challengeId, new InviteRequest(userIds)));
    }

    // ---------------------------------------------------------------- Xếp hạng

    public LiveData<Resource<Social.Leaderboard>> leaderboard(String scope) {
        return execute(api.getLeaderboard(scope));
    }

    // ---------------------------------------------------------------- Thông báo

    /** type = all | challenge | system */
    public LiveData<Resource<Social.NoticePage>> notifications(String type) {
        return execute(api.getNotifications(type));
    }

    /** Đánh dấu đã đọc hết rồi tải lại danh sách (một luồng duy nhất để màn hình không nhấp nháy). */
    public LiveData<Resource<Social.NoticePage>> readAllThenList(String type) {
        return then(execute(api.readAll()), () -> notifications(type));
    }

    public LiveData<Resource<Social.NoticePage>> markReadThenList(String id, String type) {
        return then(execute(api.markRead(id)), () -> notifications(type));
    }

    public LiveData<Resource<Social.Notice>> respond(String noticeId, boolean accept) {
        return execute(api.respond(noticeId, new RespondRequest(accept)));
    }

    /** Chạy {@code first}; xong thì chạy {@code next} và trả kết quả của {@code next}. first lỗi → trả lỗi đó. */
    private static <A, B> LiveData<Resource<B>> then(@NonNull LiveData<Resource<A>> first,
                                                     @NonNull java.util.function.Supplier<LiveData<Resource<B>>> next) {
        MediatorLiveData<Resource<B>> out = new MediatorLiveData<>();
        out.setValue(Resource.loading());
        out.addSource(first, a -> {
            if (a == null || a.status == Resource.Status.LOADING) return;
            out.removeSource(first);
            if (a.status == Resource.Status.ERROR) {
                out.setValue(Resource.error(a.message));
                return;
            }
            LiveData<Resource<B>> second = next.get();
            out.addSource(second, b -> {
                if (b == null || b.status == Resource.Status.LOADING) return;
                out.removeSource(second);
                out.setValue(b);
            });
        });
        return out;
    }
}
