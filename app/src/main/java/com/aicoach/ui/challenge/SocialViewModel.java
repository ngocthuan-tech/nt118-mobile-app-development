package com.aicoach.ui.challenge;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import com.aicoach.common.Resource;
import com.aicoach.data.model.Challenge;
import com.aicoach.data.model.Social;
import com.aicoach.data.repository.SocialRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Dùng chung cho 4 màn Cộng đồng (40, 41, 42, 22) — mỗi màn một instance riêng.
 * Lựa chọn trên form (bài, mục tiêu, số ngày, tab, người được chọn) giữ qua xoay màn bằng SavedStateHandle;
 * dữ liệu thì luôn tải từ server.
 */
public class SocialViewModel extends AndroidViewModel {

    /** Tham số type của GET /notifications theo nút lọc Tất cả / Thử thách / Hệ thống. */
    private static final String[] NOTICE_TYPES = {"all", "challenge", "system"};

    private final SocialRepository repo;
    private final SavedStateHandle saved;
    public final Set<String> selected = new LinkedHashSet<>();

    private final SocialLoad<List<Challenge>> challenges = new SocialLoad<>();
    private final SocialLoad<Challenge> challenge = new SocialLoad<>();
    private final SocialLoad<Social.Leaderboard> board = new SocialLoad<>();
    private final SocialLoad<List<Social.Candidate>> candidates = new SocialLoad<>();
    private final SocialLoad<Social.NoticePage> notices = new SocialLoad<>();
    // Thao tác một lần
    private final SocialLoad<Challenge> created = new SocialLoad<>();
    private final SocialLoad<Challenge> joined = new SocialLoad<>();
    private final SocialLoad<Social.InviteResult> invited = new SocialLoad<>();
    private final SocialLoad<Social.Notice> responded = new SocialLoad<>();
    private String lastQuery = "";

    public SocialViewModel(@NonNull Application app, @NonNull SavedStateHandle saved) {
        this(app, saved, new SocialRepository(app));
    }

    SocialViewModel(@NonNull Application app, @NonNull SavedStateHandle saved, @NonNull SocialRepository repo) {
        super(app);
        this.saved = saved;
        this.repo = repo;
        ArrayList<String> ids = saved.get("selected");
        if (ids != null) selected.addAll(ids);
    }

    // ================================================================ Trạng thái form (giữ qua xoay màn)

    public int filter() { return value("filter", 0); }
    public void filter(int value) { saved.set("filter", value); }
    public int target() { return value("target", 50); }
    public void target(int value) { saved.set("target", Math.max(10, Math.min(1000, value))); }
    public int days() { return value("days", 7); }
    public void days(int value) { saved.set("days", value); }
    public String exercise() { return value("exercise", "squat"); }
    public void exercise(String value) { saved.set("exercise", value); }

    private <T> T value(String key, T fallback) {
        T value = saved.get(key);
        return value == null ? fallback : value;
    }

    public void select(String id, boolean on) {
        if (on) selected.add(id); else selected.remove(id);
        saved.set("selected", new ArrayList<>(selected));
    }

    private void clearSelection() {
        selected.clear();
        saved.set("selected", new ArrayList<String>());
    }

    // ================================================================ 40 Thử thách & Xếp hạng

    public LiveData<Resource<List<Challenge>>> challengesState() { return challenges.state; }

    public void loadChallenges() { challenges.run(repo.challenges()); }

    public List<Challenge> challengeList() {
        return challenges.data == null ? Collections.emptyList() : challenges.data;
    }

    /** Thử thách đang hiện trên thẻ (người dùng chọn qua hộp thoại), mặc định cái đầu tiên. */
    @Nullable
    public Challenge shown() { return SocialFormat.pick(challenges.data, saved.get("shown")); }

    public void show(String challengeId) { saved.set("shown", challengeId); }

    public LiveData<Resource<Social.Leaderboard>> leaderboardState() { return board.state; }

    public String scope() {
        return filter() == 1 ? SocialRepository.SCOPE_NATIONAL : SocialRepository.SCOPE_FRIENDS;
    }

    public void loadLeaderboard() { board.run(repo.leaderboard(scope())); }

    /** Chỉ trả dòng của đúng tab đang chọn (đổi tab nhanh không hiện nhầm bảng cũ). */
    public List<Social.LeaderboardEntry> leaderboardRows() {
        if (board.data == null || !scope().equals(board.data.getScope())) return Collections.emptyList();
        return SocialFormat.rows(board.data);
    }

    public LiveData<Resource<Challenge>> joinState() { return joined.state; }

    /** @return false nếu mã sai định dạng (không gọi server). */
    public boolean join(String rawCode) {
        String code = SocialFormat.normalizeCode(rawCode);
        if (code == null) return false;
        if (joined.busy()) return true;
        joined.run(repo.join(code), c -> {
            show(c.getId());
            loadChallenges();
            loadLeaderboard();
        });
        return true;
    }

    public void consumeJoin() { joined.consume(); }

    // ================================================================ 41 Tạo thử thách

    public LiveData<Resource<Challenge>> createState() { return created.state; }

    /** @return false nếu tên không hợp lệ (3–50 ký tự). */
    public boolean create(String name, boolean privateGroup) {
        String title = name == null ? "" : name.trim();
        if (title.length() < 3 || title.length() > 50) return false;
        if (!created.busy()) created.run(repo.create(title, exercise(), target(), days(), privateGroup));
        return true;
    }

    public boolean creating() { return created.busy(); }

    public void consumeCreate() { created.consume(); }

    // ================================================================ 42 Mời bạn bè

    public LiveData<Resource<Challenge>> challengeState() { return challenge.state; }

    public void loadChallenge(String id) { challenge.run(repo.challenge(id)); }

    @Nullable
    public Challenge challengeData() { return challenge.data; }

    public LiveData<Resource<List<Social.Candidate>>> candidatesState() { return candidates.state; }

    public void searchCandidates(String challengeId, @Nullable String query) {
        lastQuery = query == null ? "" : query.trim();
        candidates.run(repo.candidates(challengeId, lastQuery));
    }

    public List<Social.Candidate> candidateList() {
        return candidates.data == null ? Collections.emptyList() : candidates.data;
    }

    public LiveData<Resource<Social.InviteResult>> inviteState() { return invited.state; }

    /** @return false nếu chưa chọn ai hoặc chọn quá 20 người. */
    public boolean invite(String challengeId) {
        if (selected.isEmpty() || selected.size() > SocialFormat.MAX_INVITES_PER_SEND) return false;
        if (invited.busy()) return true;
        invited.run(repo.invite(challengeId, new ArrayList<>(selected)), result -> {
            clearSelection();
            searchCandidates(challengeId, lastQuery);
        });
        return true;
    }

    public boolean inviting() { return invited.busy(); }

    public void consumeInvite() { invited.consume(); }

    // ================================================================ 22 Thông báo

    public LiveData<Resource<Social.NoticePage>> noticesState() { return notices.state; }

    private String noticeType() {
        int f = filter();
        return NOTICE_TYPES[f >= 0 && f < NOTICE_TYPES.length ? f : 0];
    }

    public void loadNotices() { notices.run(repo.notifications(noticeType())); }

    public List<Social.Notice> noticeList() {
        return notices.data == null ? Collections.emptyList() : notices.data.getItems();
    }

    public long unreadCount() { return notices.data == null ? 0 : notices.data.getUnreadCount(); }

    public void readAll() { notices.run(repo.readAllThenList(noticeType())); }

    public void markRead(String id) { notices.run(repo.markReadThenList(id, noticeType())); }

    public LiveData<Resource<Social.Notice>> respondState() { return responded.state; }

    public void respond(String noticeId, boolean accept) {
        if (responded.busy()) return;
        responded.run(repo.respond(noticeId, accept), n -> loadNotices());
    }

    public boolean responding() { return responded.busy(); }

    public void consumeRespond() { responded.consume(); }
}
