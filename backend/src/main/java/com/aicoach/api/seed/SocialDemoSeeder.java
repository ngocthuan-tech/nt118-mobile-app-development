package com.aicoach.api.seed;

import com.aicoach.api.common.VnTime;
import com.aicoach.api.social.ChallengeService;
import com.aicoach.api.social.FriendService;
import com.aicoach.api.social.NotificationRepository;
import com.aicoach.api.social.NotificationService;
import com.aicoach.api.social.SocialDtos.ChallengeDto;
import com.aicoach.api.social.SocialDtos.CreateChallengeRequest;
import com.aicoach.api.social.SocialDtos.InviteRequest;
import com.aicoach.api.user.UserAccount;
import com.aicoach.api.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Dữ liệu cộng đồng cho tài khoản demo@aicoach.vn — chỉ chạy khi bật app.seed.demo.
 * Đi qua đúng các service thật (tạo thử thách, tham gia bằng mã, mời) nên dữ liệu demo giống hệt dữ liệu người dùng tạo.
 *
 *  - 5 bạn bè mẫu (đăng nhập được bằng mật khẩu Demo1234 để thử nhận lời mời).
 *  - Nếu demo chưa có thử thách nào đang diễn ra: Hoàng Nam tạo "100 Squat mỗi ngày" (demo + 2 bạn tham gia),
 *    Thu Trang tạo "Plank mỗi ngày" và mời demo (thông báo có nút Tham gia / Từ chối).
 *  - Chạy lại mỗi lần khởi động nhưng không tạo trùng; thử thách cũ hết hạn thì tạo bộ mới để lúc nào demo cũng có dữ liệu.
 */
@Component
public class SocialDemoSeeder {

    private static final Logger log = LoggerFactory.getLogger(SocialDemoSeeder.class);

    /** email, tên, XP tích luỹ, chuỗi ngày. */
    private static final Object[][] FRIENDS = {
            {"lananh.demo@aicoach.vn", "Lan Anh", 1410L, 12},
            {"duchuy.demo@aicoach.vn", "Đức Huy", 1300L, 5},
            {"hoangnam.demo@aicoach.vn", "Hoàng Nam", 1520L, 7},
            {"thutrang.demo@aicoach.vn", "Thu Trang", 1280L, 3},
            {"quocbao.demo@aicoach.vn", "Quốc Bảo", 1120L, 1},
    };

    private final UserRepository users;
    private final FriendService friends;
    private final ChallengeService challenges;
    private final NotificationService notificationService;
    private final NotificationRepository notifications;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public SocialDemoSeeder(UserRepository users, FriendService friends, ChallengeService challenges,
                            NotificationService notificationService, NotificationRepository notifications,
                            PasswordEncoder passwordEncoder, Clock clock) {
        this.users = users;
        this.friends = friends;
        this.challenges = challenges;
        this.notificationService = notificationService;
        this.notifications = notifications;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    public void seed(UserAccount demo) {
        LocalDate today = VnTime.today(clock);
        List<UserAccount> pals = new ArrayList<>();
        for (Object[] f : FRIENDS) {
            pals.add(users.findByEmail((String) f[0]).orElseGet(() -> createFriend(f, today)));
        }
        for (UserAccount p : pals) friends.befriend(demo.getId(), p.getId());

        if (notifications.findByUserIdOrderByCreatedAtDesc(demo.getId(), PageRequest.of(0, 1)).isEmpty()) {
            notificationService.welcome(demo.getId());
        }

        boolean hasOpenChallenge = challenges.list(demo.getId()).stream()
                .anyMatch(c -> !"ENDED".equals(c.status()));
        if (hasOpenChallenge) return;

        UserAccount lanAnh = pals.get(0);
        UserAccount ducHuy = pals.get(1);
        UserAccount hoangNam = pals.get(2);
        UserAccount thuTrang = pals.get(3);

        ChallengeDto squat = challenges.create(hoangNam.getId(),
                new CreateChallengeRequest("100 Squat mỗi ngày", "squat", 100, 7, false));
        challenges.joinByCode(demo.getId(), squat.inviteCode());
        challenges.joinByCode(lanAnh.getId(), squat.inviteCode());
        challenges.joinByCode(ducHuy.getId(), squat.inviteCode());

        ChallengeDto plank = challenges.create(thuTrang.getId(),
                new CreateChallengeRequest("Plank mỗi ngày", "plank", 60, 7, true));
        challenges.invite(thuTrang.getId(), plank.id(), new InviteRequest(List.of(demo.getId())));

        log.info("Đã tạo dữ liệu cộng đồng mẫu cho {}", demo.getEmail());
    }

    private UserAccount createFriend(Object[] f, LocalDate today) {
        UserAccount u = new UserAccount();
        u.setEmail((String) f[0]);
        u.setDisplayName((String) f[1]);
        u.setPasswordHash(passwordEncoder.encode(DataSeeder.DEMO_PASSWORD));
        u.setGoal("stay_fit");
        u.setLevel("beginner");
        u.setProfileCompleted(true);
        u.setTotalXp((Long) f[2]);
        u.setStreak((Integer) f[3]);
        u.setLastWorkoutDate(today);
        return users.save(u);
    }
}
