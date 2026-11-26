package com.aicoach.ui.challenge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.aicoach.data.model.Challenge;
import com.aicoach.data.model.Social;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

/** Quy tắc hiển thị màn Cộng đồng — chạy trên JVM, không cần máy ảo. */
public class SocialFormatTest {

    @Test
    public void inviteCodeIsNormalizedBeforeCallingServer() {
        assertEquals("7Q2KX9", SocialFormat.normalizeCode(" 7q2kx9 "));
        assertEquals("7Q2KX9", SocialFormat.normalizeCode("7q2 KX9"));
        assertEquals("7Q2KX9", SocialFormat.normalizeCode("7Q2-KX9"));
        assertNull(SocialFormat.normalizeCode(""));
        assertNull(SocialFormat.normalizeCode(null));
        assertNull(SocialFormat.normalizeCode("7Q2K"));
        assertNull(SocialFormat.normalizeCode("7Q2KX9A"));
        assertNull(SocialFormat.normalizeCode("đường"));
    }

    @Test
    public void initialsAndProgress() {
        assertEquals("Đ", SocialFormat.initial("  đức huy"));
        assertEquals("L", SocialFormat.initial("Lan Anh"));
        assertEquals("?", SocialFormat.initial(" "));
        assertEquals("?", SocialFormat.initial(null));

        assertEquals(0, SocialFormat.percent(0, 100));
        assertEquals(72, SocialFormat.percent(72, 100));
        assertEquals(100, SocialFormat.percent(130, 100));
        assertEquals(0, SocialFormat.percent(50, 0));
    }

    @Test
    public void leaderboardAddsMeWhenOutsideTop() {
        Social.LeaderboardEntry a = new Social.LeaderboardEntry(1, "a", "An", 900, 3, false);
        Social.LeaderboardEntry b = new Social.LeaderboardEntry(2, "b", "Bình", 800, 1, false);
        Social.LeaderboardEntry me = new Social.LeaderboardEntry(57, "me", "Minh", 10, 0, true);

        List<Social.LeaderboardEntry> national = SocialFormat.rows(
                new Social.Leaderboard("national", Arrays.asList(a, b), me));
        assertEquals(3, national.size());
        assertSame(me, national.get(2));

        Social.LeaderboardEntry meInTop = new Social.LeaderboardEntry(2, "me", "Minh", 800, 0, true);
        List<Social.LeaderboardEntry> friends = SocialFormat.rows(
                new Social.Leaderboard("friends", Arrays.asList(a, meInTop), meInTop));
        assertEquals(2, friends.size());

        assertTrue(SocialFormat.rows(null).isEmpty());
    }

    @Test
    public void shownChallengeAndAvatars() {
        Challenge.Member m1 = new Challenge.Member("1", "Hoàng Nam", 100, 300, 3, false);
        Challenge.Member m2 = new Challenge.Member("2", "minh", 60, 120, 1, true);
        Challenge.Member m3 = new Challenge.Member("3", "Lan Anh", 0, 0, 0, false);
        Challenge.Member m4 = new Challenge.Member("4", "Đức Huy", 0, 0, 0, false);
        Challenge squat = new Challenge("c1", "100 Squat", "squat", 100, 7, Challenge.ACTIVE, false,
                "7Q2KX9", 60, Arrays.asList(m1, m2, m3, m4));
        Challenge plank = new Challenge("c2", "Plank", "plank", 60, 7, Challenge.ACTIVE, true,
                null, 0, Collections.singletonList(m2));
        List<Challenge> list = Arrays.asList(squat, plank);

        assertSame(squat, SocialFormat.pick(list, null));
        assertSame(plank, SocialFormat.pick(list, "c2"));
        assertSame(squat, SocialFormat.pick(list, "da-bi-xoa"));
        assertNull(SocialFormat.pick(Collections.emptyList(), "c1"));

        assertEquals(Arrays.asList("H", "M", "L"), SocialFormat.avatars(squat, 3));
        assertEquals(1, SocialFormat.moreMembers(squat, 3));
        assertEquals(0, SocialFormat.moreMembers(plank, 1));
        assertTrue(squat.hasInviteCode());
        assertTrue(!plank.hasInviteCode());
    }

    @Test
    public void noticeDayUsesVietnamTime() {
        // 18:30 UTC ngày 25 = 01:30 sáng ngày 26 giờ Việt Nam
        assertEquals(LocalDate.of(2026, 11, 26), SocialFormat.dateOf("2026-11-25T18:30:00Z"));
        assertEquals(LocalDate.of(2026, 11, 25), SocialFormat.dateOf("2026-11-25T16:59:59Z"));
        assertNull(SocialFormat.dateOf("hôm qua"));
        assertNull(SocialFormat.dateOf(null));
        assertEquals(-1, SocialFormat.epochMillis(""));
    }
}
