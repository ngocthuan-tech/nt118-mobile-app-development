package com.aicoach.data.mock;

import com.aicoach.data.repository.SocialRepository.ChallengeItem;
import com.aicoach.data.repository.SocialRepository.Friend;
import com.aicoach.data.repository.SocialRepository.Notice;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Explicit fixtures for the midterm community screens; never server records. */
public final class MockSocialData {
    private MockSocialData() { }
    public static List<ChallengeItem> challenges() {
        return Collections.singletonList(new ChallengeItem("demo-squat", "Thử thách Squat 7 ngày",
                "squat", 50, 7, false, Collections.emptySet()));
    }
    public static List<Friend> friends() {
        return Arrays.asList(new Friend("an", "Minh An", 820), new Friend("binh", "Thanh Bình", 740),
                new Friend("chi", "Lan Chi", 680), new Friend("dung", "Anh Dũng", 520));
    }
    public static List<Friend> nationalLeaders() {
        return Collections.singletonList(new Friend("national", "Ngọc Minh", 1200));
    }
    public static List<Notice> notices() {
        return Arrays.asList(new Notice("invite", "Lời mời thử thách mẫu", "Cùng tham gia Squat 7 ngày.",
                        true, false, false, false),
                new Notice("welcome", "Chào mừng bạn", "Dữ liệu cộng đồng trong bản giữa kỳ là dữ liệu mẫu trên máy.",
                        false, false, false, false));
    }
}
