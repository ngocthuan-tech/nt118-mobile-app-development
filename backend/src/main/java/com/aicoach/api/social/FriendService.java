package com.aicoach.api.social;

import com.aicoach.api.common.ApiException;
import com.aicoach.api.progress.ProgressService;
import com.aicoach.api.social.SocialDtos.FriendDto;
import com.aicoach.api.social.SocialDtos.LeaderboardDto;
import com.aicoach.api.social.SocialDtos.LeaderboardEntry;
import com.aicoach.api.user.UserAccount;
import com.aicoach.api.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Bạn bè và bảng xếp hạng (Figma 40, 42).
 * Hai người thành bạn khi một người chấp nhận lời mời thử thách của người kia hoặc tham gia bằng mã mời.
 * Điểm xếp hạng = XP tích luỹ (totalXp) — cùng thước đo với danh hiệu trong Hồ sơ.
 */
@Service
public class FriendService {

    private static final Comparator<UserAccount> BY_XP = Comparator
            .comparingLong(UserAccount::getTotalXp).reversed()
            .thenComparing(UserAccount::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));

    private final FriendshipRepository friendships;
    private final UserRepository users;
    private final ProgressService progress;

    public FriendService(FriendshipRepository friendships, UserRepository users, ProgressService progress) {
        this.friendships = friendships;
        this.users = users;
        this.progress = progress;
    }

    /** Kết bạn 2 chiều; đã là bạn thì bỏ qua. */
    @Transactional
    public void befriend(String a, String b) {
        if (a == null || b == null || a.equals(b)) return;
        if (!friendships.existsByUserIdAndFriendId(a, b)) friendships.save(new Friendship(a, b));
        if (!friendships.existsByUserIdAndFriendId(b, a)) friendships.save(new Friendship(b, a));
    }

    public boolean areFriends(String a, String b) {
        return friendships.existsByUserIdAndFriendId(a, b);
    }

    public List<UserAccount> friendAccounts(String userId) {
        List<String> ids = friendships.findByUserId(userId).stream().map(Friendship::getFriendId).toList();
        return ids.isEmpty() ? List.of() : users.findAllById(ids);
    }

    @Transactional(readOnly = true)
    public List<FriendDto> friends(String userId) {
        return friendAccounts(userId).stream()
                .sorted(Comparator.comparing(u -> u.getDisplayName().toLowerCase(Locale.ROOT)))
                .map(u -> new FriendDto(u.getId(), u.getDisplayName(), progress.effectiveStreak(u), u.getTotalXp()))
                .toList();
    }

    /** scope = friends (mặc định: tôi + bạn bè) | national (top 50 toàn app). */
    @Transactional(readOnly = true)
    public LeaderboardDto leaderboard(String userId, String scope) {
        UserAccount me = users.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("Phiên đăng nhập không hợp lệ"));
        boolean national = "national".equalsIgnoreCase(scope);

        List<UserAccount> ranked;
        if (national) {
            ranked = new ArrayList<>(users.findTop50ByOrderByTotalXpDescCreatedAtAsc().stream()
                    .filter(u -> u.getTotalXp() > 0 || u.getId().equals(userId))
                    .toList());
        } else {
            ranked = new ArrayList<>(friendAccounts(userId));
            ranked.add(me);
            ranked.sort(BY_XP);
        }

        List<LeaderboardEntry> items = new ArrayList<>();
        LeaderboardEntry mine = null;
        for (int i = 0; i < ranked.size(); i++) {
            UserAccount u = ranked.get(i);
            LeaderboardEntry e = entry(i + 1, u, userId);
            items.add(e);
            if (e.me()) mine = e;
        }
        if (mine == null) {
            // Không lọt top 50 toàn quốc: hạng = số người nhiều XP hơn + 1
            int rank = (int) Math.min(Integer.MAX_VALUE, users.countByTotalXpGreaterThan(me.getTotalXp()) + 1);
            mine = entry(rank, me, userId);
        }
        return new LeaderboardDto(national ? "national" : "friends", items, mine);
    }

    private LeaderboardEntry entry(int rank, UserAccount u, String meId) {
        return new LeaderboardEntry(rank, u.getId(), u.getDisplayName(), u.getTotalXp(),
                progress.effectiveStreak(u), u.getId().equals(meId));
    }
}
