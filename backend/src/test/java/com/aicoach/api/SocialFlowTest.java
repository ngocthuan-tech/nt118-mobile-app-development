package com.aicoach.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Module Cộng đồng trên PostgreSQL thật: tạo thử thách → mời → nhận / từ chối → tham gia bằng mã →
 * tiến độ tính từ buổi tập → bạn bè, bảng xếp hạng, thông báo; và các chặn quyền / dữ liệu sai.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SocialFlowTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    private record Account(String token, String email, String id) {
    }

    // ------------------------------------------------------------------ tạo + kiểm tra dữ liệu vào

    @Test
    void createChallengeMakesCreatorAMemberWithInviteCode() throws Exception {
        Account a = account("An");
        JsonNode c = createChallenge(a, "100 Squat mỗi ngày", "squat", 100, 7);

        assertEquals("ACTIVE", c.path("status").asText());
        assertEquals(7, c.path("daysLeft").asInt());
        assertEquals(1, c.path("memberCount").asInt());
        assertTrue(c.path("creator").asBoolean());
        assertTrue(c.path("inviteCode").asText().matches("[2-9A-HJ-NP-Z]{6}"));
        assertEquals("Squat", c.path("exerciseName").asText());

        mvc.perform(get("/api/v1/challenges").header("Authorization", a.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(c.path("id").asText()));
    }

    @Test
    void invalidChallengesAreRejected() throws Exception {
        Account a = account("An");
        postJson(a, "/api/v1/challenges", challengeJson("ab", "squat", 100, 7)).andExpect(status().isBadRequest());
        postJson(a, "/api/v1/challenges", challengeJson("Hợp lệ", "squat", 5, 7)).andExpect(status().isBadRequest());
        postJson(a, "/api/v1/challenges", challengeJson("Hợp lệ", "squat", 100, 5)).andExpect(status().isBadRequest());
        postJson(a, "/api/v1/challenges", challengeJson("Hợp lệ", "khong-co", 100, 7))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/challenges")).andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------------ mời → chấp nhận

    @Test
    void inviteByEmailAcceptMakesFriendsAndNotifiesInviter() throws Exception {
        Account a = account("An");
        Account b = account("Bình");
        String id = createChallenge(a, "Squat 7 ngày", "squat", 50, 7).path("id").asText();

        // Chưa là bạn: tìm đúng email mới thấy
        mvc.perform(get("/api/v1/challenges/" + id + "/candidates").param("q", b.email())
                        .header("Authorization", a.token()))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].userId").value(b.id()))
                .andExpect(jsonPath("$.data[0].friend").value(false))
                .andExpect(jsonPath("$.data[0].status").value("NONE"));

        postJson(a, "/api/v1/challenges/" + id + "/invites", "{\"userIds\":[\"" + b.id() + "\"]}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.invited").value(1));
        // Mời lần 2 khi đang chờ → bỏ qua
        postJson(a, "/api/v1/challenges/" + id + "/invites", "{\"userIds\":[\"" + b.id() + "\"]}")
                .andExpect(jsonPath("$.data.invited").value(0))
                .andExpect(jsonPath("$.data.skipped").value(1));

        String noticeId = inviteNotice(b, id);
        postJson(b, "/api/v1/notifications/" + noticeId + "/respond", "{\"accept\":true}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.inviteStatus").value("ACCEPTED"))
                .andExpect(jsonPath("$.data.read").value(true));
        // Bấm lại lần 2 không lỗi, không đổi
        postJson(b, "/api/v1/notifications/" + noticeId + "/respond", "{\"accept\":false}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.inviteStatus").value("ACCEPTED"));

        mvc.perform(get("/api/v1/challenges/" + id).header("Authorization", b.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memberCount").value(2));
        mvc.perform(get("/api/v1/social/friends").header("Authorization", a.token()))
                .andExpect(jsonPath("$.data[*].userId", hasItem(b.id())));
        mvc.perform(get("/api/v1/notifications").param("type", "challenge").header("Authorization", a.token()))
                .andExpect(jsonPath("$.data.items[*].type", hasItem("CHALLENGE_JOINED")));
        // Đã là thành viên → không mời lại được
        postJson(a, "/api/v1/challenges/" + id + "/invites", "{\"userIds\":[\"" + b.id() + "\"]}")
                .andExpect(jsonPath("$.data.skipped").value(1));
        mvc.perform(get("/api/v1/challenges/" + id + "/candidates").header("Authorization", a.token()))
                .andExpect(jsonPath("$.data[0].status").value("MEMBER"));
    }

    // ------------------------------------------------------------------ từ chối → mời lại

    @Test
    void declineKeepsUserOutAndCanBeReinvited() throws Exception {
        Account a = account("An");
        Account c = account("Chi");
        String id = createChallenge(a, "Hít đất 3 ngày", "pushup", 30, 3).path("id").asText();
        postJson(a, "/api/v1/challenges/" + id + "/invites", "{\"userIds\":[\"" + c.id() + "\"]}");

        postJson(c, "/api/v1/notifications/" + inviteNotice(c, id) + "/respond", "{\"accept\":false}")
                .andExpect(jsonPath("$.data.inviteStatus").value("DECLINED"));
        mvc.perform(get("/api/v1/challenges/" + id).header("Authorization", c.token()))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/notifications").header("Authorization", a.token()))
                .andExpect(jsonPath("$.data.items[*].type", hasItem("CHALLENGE_DECLINED")));

        postJson(a, "/api/v1/challenges/" + id + "/invites", "{\"userIds\":[\"" + c.id() + "\"]}")
                .andExpect(jsonPath("$.data.invited").value(1));
    }

    // ------------------------------------------------------------------ mã mời + quyền

    @Test
    void joinByCodeAndOutsidersCannotSeeOrInvite() throws Exception {
        Account a = account("An");
        Account d = account("Dũng");
        Account stranger = account("Người lạ");
        JsonNode created = createChallenge(a, "Lunge 14 ngày", "lunge", 20, 14);
        String id = created.path("id").asText();
        String code = created.path("inviteCode").asText();
        String typed = code.substring(0, 3).toLowerCase() + " " + code.substring(3);   // "7q2 KX9"

        mvc.perform(get("/api/v1/challenges/" + id).header("Authorization", stranger.token()))
                .andExpect(status().isNotFound());
        postJson(stranger, "/api/v1/challenges/" + id + "/invites", "{\"userIds\":[\"" + d.id() + "\"]}")
                .andExpect(status().isNotFound());
        postJson(d, "/api/v1/challenges/join", "{\"code\":\"000000\"}").andExpect(status().isNotFound());
        postJson(d, "/api/v1/challenges/join", "{\"code\":\"" + code.substring(0, 4) + "\"}")
                .andExpect(status().isNotFound());

        postJson(d, "/api/v1/challenges/join", "{\"code\":\"" + typed + "\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.memberCount").value(2));
        mvc.perform(get("/api/v1/social/friends").header("Authorization", d.token()))
                .andExpect(jsonPath("$.data[*].userId", hasItem(a.id())));
        mvc.perform(get("/api/v1/social/friends").header("Authorization", stranger.token()))
                .andExpect(jsonPath("$.data[*].userId", not(hasItem(a.id()))));
    }

    @Test
    void privateChallengeHidesCodeAndOnlyAcceptsInvites() throws Exception {
        Account a = account("An");
        Account b = account("Bình");
        MvcResult res = postJson(a, "/api/v1/challenges", challengeJson("Nhóm kín", "squat", 30, 7, true))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.privateGroup").value(true))
                .andExpect(jsonPath("$.data.inviteCode").doesNotExist())
                .andReturn();
        String id = data(res).path("id").asText();

        postJson(a, "/api/v1/challenges/" + id + "/invites", "{\"userIds\":[\"" + b.id() + "\"]}")
                .andExpect(jsonPath("$.data.invited").value(1));
        postJson(b, "/api/v1/notifications/" + inviteNotice(b, id) + "/respond", "{\"accept\":true}")
                .andExpect(jsonPath("$.data.inviteStatus").value("ACCEPTED"));
        mvc.perform(get("/api/v1/challenges/" + id).header("Authorization", b.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memberCount").value(2))
                .andExpect(jsonPath("$.data.inviteCode").doesNotExist());
    }

    // ------------------------------------------------------------------ tiến độ từ buổi tập

    @Test
    void progressComesFromWorkoutSessionsOfTheSameExercise() throws Exception {
        Account a = account("An");
        Account b = account("Bình");
        JsonNode created = createChallenge(a, "100 Squat", "squat", 100, 7);
        String id = created.path("id").asText();
        postJson(b, "/api/v1/challenges/join", "{\"code\":\"" + created.path("inviteCode").asText() + "\"}");

        saveSession(b, "squat", 70, 60);
        saveSession(b, "squat", 55, 50);
        saveSession(b, "pushup", 40, 40);      // khác bài → không tính
        saveSession(a, "squat", 30, 20);

        MvcResult res = mvc.perform(get("/api/v1/challenges/" + id).header("Authorization", b.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.myTodayReps").value(110))
                .andExpect(jsonPath("$.data.myCompletedDays").value(1))
                .andExpect(jsonPath("$.data.members[0].userId").value(b.id()))
                .andExpect(jsonPath("$.data.members[0].totalReps").value(110))
                .andExpect(jsonPath("$.data.members[1].todayReps").value(20))
                .andExpect(jsonPath("$.data.members[1].completedDays").value(0))
                .andReturn();
        assertTrue(data(res).path("members").get(0).path("me").asBoolean());
    }

    // ------------------------------------------------------------------ bảng xếp hạng + thông báo

    @Test
    void leaderboardAndNotifications() throws Exception {
        Account a = account("An");
        Account b = account("Bình");
        JsonNode created = createChallenge(a, "Squat", "squat", 10, 3);
        postJson(b, "/api/v1/challenges/join", "{\"code\":\"" + created.path("inviteCode").asText() + "\"}");
        saveSession(b, "squat", 20, 20);    // b có XP, a chưa có

        MvcResult friends = mvc.perform(get("/api/v1/social/leaderboard").header("Authorization", a.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scope").value("friends"))
                .andExpect(jsonPath("$.data.items", hasSize(2)))
                .andExpect(jsonPath("$.data.items[0].userId").value(b.id()))
                .andReturn();
        JsonNode me = data(friends).path("me");
        assertEquals(2, me.path("rank").asInt());
        assertTrue(me.path("me").asBoolean());

        mvc.perform(get("/api/v1/social/leaderboard").param("scope", "national").header("Authorization", a.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scope").value("national"))
                .andExpect(jsonPath("$.data.me.userId").value(a.id()));

        // Tài khoản mới có sẵn thông báo chào mừng (hệ thống)
        mvc.perform(get("/api/v1/notifications").param("type", "system").header("Authorization", a.token()))
                .andExpect(jsonPath("$.data.items[*].type", hasItem("SYSTEM")));
        MvcResult list = mvc.perform(get("/api/v1/notifications").header("Authorization", a.token()))
                .andReturn();
        assertTrue(data(list).path("unreadCount").asLong() >= 2);   // chào mừng + "Bình vừa tham gia"

        mvc.perform(post("/api/v1/notifications/read-all").header("Authorization", a.token()))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/notifications").header("Authorization", a.token()))
                .andExpect(jsonPath("$.data.unreadCount").value(0));

        // Không đọc / trả lời được thông báo của người khác
        String bNotice = data(mvc.perform(get("/api/v1/notifications").header("Authorization", b.token()))
                .andReturn()).path("items").get(0).path("id").asText();
        mvc.perform(post("/api/v1/notifications/" + bNotice + "/read").header("Authorization", a.token()))
                .andExpect(status().isNotFound());
        postJson(a, "/api/v1/notifications/" + bNotice + "/respond", "{\"accept\":true}")
                .andExpect(status().isNotFound());
        // Thông báo không phải lời mời thì không "Tham gia" được
        postJson(b, "/api/v1/notifications/" + bNotice + "/respond", "{\"accept\":true}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void demoAccountHasFriendsAndChallenges() throws Exception {
        String token = login("demo@aicoach.vn", "Demo1234");
        MvcResult res = mvc.perform(get("/api/v1/challenges").header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();
        boolean hasOpen = false;
        for (JsonNode c : data(res)) hasOpen |= !"ENDED".equals(c.path("status").asText());
        assertTrue(hasOpen);
        mvc.perform(get("/api/v1/social/friends").header("Authorization", token))
                .andExpect(jsonPath("$.data", hasSize(5)));
        MvcResult notices = mvc.perform(get("/api/v1/notifications").param("type", "challenge")
                        .header("Authorization", token))
                .andReturn();
        boolean pendingInvite = false;
        for (JsonNode n : data(notices).path("items")) {
            pendingInvite |= "PENDING".equals(n.path("inviteStatus").asText());
        }
        assertTrue(pendingInvite);
        assertFalse(data(res).isEmpty());
    }

    // ------------------------------------------------------------------ helpers

    private Account account(String name) throws Exception {
        String email = "social-" + UUID.randomUUID().toString().substring(0, 8) + "@gmail.com";
        String body = """
                {"displayName":"%s","email":"%s","password":"abc12345"}""".formatted(name, email);
        MvcResult res = mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        String token = "Bearer " + data(res).path("accessToken").asText();
        MvcResult me = mvc.perform(get("/api/v1/users/me").header("Authorization", token)).andReturn();
        return new Account(token, email, data(me).path("id").asText());
    }

    private String login(String email, String password) throws Exception {
        String body = """
                {"email":"%s","password":"%s"}""".formatted(email, password);
        MvcResult res = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn();
        return "Bearer " + data(res).path("accessToken").asText();
    }

    private JsonNode createChallenge(Account a, String title, String exercise, int target, int days) throws Exception {
        MvcResult res = postJson(a, "/api/v1/challenges", challengeJson(title, exercise, target, days))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.inviteCode", matchesPattern("[2-9A-HJ-NP-Z]{6}")))
                .andReturn();
        return data(res);
    }

    private static String challengeJson(String title, String exercise, int target, int days) {
        return challengeJson(title, exercise, target, days, false);
    }

    private static String challengeJson(String title, String exercise, int target, int days, boolean privateGroup) {
        return """
                {"title":"%s","exerciseId":"%s","dailyTarget":%d,"days":%d,"privateGroup":%s}"""
                .formatted(title, exercise, target, days, privateGroup);
    }

    /** Id thông báo lời mời (PENDING) của thử thách này trong hộp thư của người được mời. */
    private String inviteNotice(Account invitee, String challengeId) throws Exception {
        MvcResult res = mvc.perform(get("/api/v1/notifications").param("type", "challenge")
                        .header("Authorization", invitee.token()))
                .andExpect(status().isOk())
                .andReturn();
        for (JsonNode n : data(res).path("items")) {
            if ("CHALLENGE_INVITE".equals(n.path("type").asText()) && challengeId.equals(n.path("challengeId").asText())
                    && "PENDING".equals(n.path("inviteStatus").asText())) {
                return n.path("id").asText();
            }
        }
        throw new AssertionError("Không thấy lời mời cho thử thách " + challengeId);
    }

    private void saveSession(Account a, String exercise, int total, int correct) throws Exception {
        String body = """
                {"exerciseId":"%s","durationSec":120,"targetReps":%d,"totalReps":%d,"correctReps":%d,"formErrors":{}}"""
                .formatted(exercise, total, total, correct);
        postJson(a, "/api/v1/sessions", body).andExpect(status().isCreated());
    }

    private ResultActions postJson(Account a, String url, String body) throws Exception {
        return mvc.perform(post(url).header("Authorization", a.token())
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private JsonNode data(MvcResult res) throws Exception {
        return mapper.readTree(res.getResponse().getContentAsString()).path("data");
    }
}
