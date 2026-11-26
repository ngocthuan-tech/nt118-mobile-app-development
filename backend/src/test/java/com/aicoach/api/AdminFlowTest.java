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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web admin trên PostgreSQL thật: phân quyền chặn ở server (401 / 403), khoá tài khoản làm token cũ chết ngay,
 * admin sửa bài tập / giáo án thì API của app thấy số mới. Tài khoản admin lấy từ application-test.yml.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminFlowTest {

    private static final String ADMIN_EMAIL = "admin@aicoach.vn";
    private static final String ADMIN_PASSWORD = "Admin1234";

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    private record Account(String token, String email, String password, String id) {
    }

    // ------------------------------------------------------------------ 1–3. Phân quyền

    @Test
    void adminApisAreGuardedByRoleOnTheServer() throws Exception {
        Account user = register("Người dùng thường");
        String admin = adminToken();

        // 1. Không có token → 401
        mvc.perform(get("/api/v1/admin/stats")).andExpect(status().isUnauthorized());
        // 2. Token người dùng thường → 403 (đăng nhập hợp lệ nhưng không có quyền)
        mvc.perform(get("/api/v1/admin/stats").header("Authorization", user.token()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
        mvc.perform(get("/api/v1/admin/users").header("Authorization", user.token()))
                .andExpect(status().isForbidden());
        // 3. Token admin → 200
        MvcResult res = mvc.perform(get("/api/v1/admin/stats").param("days", "7").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.days").value(7))
                .andExpect(jsonPath("$.data.sessionsPerDay", hasSize(7)))
                .andExpect(jsonPath("$.data.signupsPerDay", hasSize(7)))
                .andReturn();
        assertTrue(data(res).path("totalUsers").asLong() >= 2);

        mvc.perform(get("/api/v1/admin/me").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("ADMIN"))
                .andExpect(jsonPath("$.data.email").value(ADMIN_EMAIL));
        mvc.perform(get("/api/v1/admin/stats").param("days", "5").header("Authorization", admin))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------ 4–5. Khoá tài khoản

    @Test
    void lockingRevokesTokensImmediatelyAndBlocksLogin() throws Exception {
        Account user = register("Sẽ bị khoá");
        String admin = adminToken();

        putJson(admin, "/api/v1/admin/users/" + user.id() + "/lock", "{\"locked\":true}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.locked").value(true));

        // 4. Token cũ của người bị khoá → 401 ngay, kèm lý do để app báo đúng
        mvc.perform(get("/api/v1/users/me").header("Authorization", user.token()))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("X-Session-Reason", "ACCOUNT_LOCKED"))
                .andExpect(jsonPath("$.message").value("Tài khoản đã bị khoá. Liên hệ quản trị viên."));
        // 5. Đăng nhập lại → 403
        loginStatus(user.email(), user.password()).andExpect(status().isForbidden());
        // Sai mật khẩu vẫn chỉ báo sai mật khẩu (không lộ là tài khoản đang bị khoá)
        loginStatus(user.email(), "sai-mat-khau-1").andExpect(status().isUnauthorized());

        mvc.perform(get("/api/v1/admin/users").param("q", user.email()).header("Authorization", admin))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].locked").value(true));

        // Mở khoá → đăng nhập được, dùng app bình thường
        putJson(admin, "/api/v1/admin/users/" + user.id() + "/lock", "{\"locked\":false}")
                .andExpect(jsonPath("$.data.locked").value(false));
        MvcResult again = loginStatus(user.email(), user.password()).andExpect(status().isOk()).andReturn();
        mvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + data(again).path("accessToken").asText()))
                .andExpect(status().isOk());
    }

    @Test
    void adminCannotLockThemselvesOrAnotherAdmin() throws Exception {
        String admin = adminToken();
        String adminId = data(mvc.perform(get("/api/v1/admin/me").header("Authorization", admin)).andReturn())
                .path("id").asText();

        putJson(admin, "/api/v1/admin/users/" + adminId + "/lock", "{\"locked\":true}")
                .andExpect(status().isBadRequest());
        putJson(admin, "/api/v1/admin/users/khong-ton-tai/lock", "{\"locked\":true}")
                .andExpect(status().isNotFound());
        putJson(admin, "/api/v1/admin/users/" + adminId + "/lock", "{}")
                .andExpect(status().isBadRequest());
        // Admin vẫn dùng được sau các lần thử trên
        mvc.perform(get("/api/v1/admin/me").header("Authorization", admin)).andExpect(status().isOk());
    }

    // ------------------------------------------------------------------ 6. Sửa nội dung → app thấy ngay

    @Test
    void editingAnExerciseIsVisibleToTheApp() throws Exception {
        Account user = register("Người tập");
        String admin = adminToken();
        JsonNode original = null;
        for (JsonNode e : data(mvc.perform(get("/api/v1/admin/exercises").header("Authorization", admin))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data", hasSize(9))).andReturn())) {
            if ("toe_touch".equals(e.path("id").asText())) original = e;
        }
        assertTrue(original != null);
        try {
            putJson(admin, "/api/v1/admin/exercises/toe_touch", exerciseJson("Chạm mũi chân", "Mô tả do admin sửa", 4, 21, 0.2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.adminEdited").value(true));
            mvc.perform(get("/api/v1/exercises/toe_touch").header("Authorization", user.token()))
                    .andExpect(jsonPath("$.data.name").value("Chạm mũi chân"))
                    .andExpect(jsonPath("$.data.defaultSets").value(4))
                    .andExpect(jsonPath("$.data.defaultReps").value(21));

            putJson(admin, "/api/v1/admin/exercises/toe_touch", exerciseJson("Chạm mũi chân", "", 0, 21, 0.2))
                    .andExpect(status().isBadRequest());
            putJson(admin, "/api/v1/admin/exercises/toe_touch", exerciseJson("A", "", 3, 21, 0.2))
                    .andExpect(status().isBadRequest());
            putJson(admin, "/api/v1/admin/exercises/toe_touch", exerciseJson("Chạm mũi chân", "", 3, 21, 9))
                    .andExpect(status().isBadRequest());
            putJson(admin, "/api/v1/admin/exercises/khong-co", exerciseJson("Bài lạ", "", 3, 10, 0.2))
                    .andExpect(status().isNotFound());
        } finally {
            // Trả lại nội dung cũ để các test khác (cùng database) không bị ảnh hưởng
            putJson(admin, "/api/v1/admin/exercises/toe_touch", exerciseJson(original.path("name").asText(),
                    original.path("description").asText(), original.path("defaultSets").asInt(), original.path("defaultReps").asInt(),
                    original.path("caloriesPerRep").asDouble()))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void editingAPlanItemIsVisibleToTheApp() throws Exception {
        Account user = register("Theo giáo án");
        String admin = adminToken();
        JsonNode plan = null;
        for (JsonNode p : data(mvc.perform(get("/api/v1/admin/plans").header("Authorization", admin))
                .andExpect(status().isOk()).andReturn())) {
            if ("core-2w".equals(p.path("id").asText())) plan = p;
        }
        assertTrue(plan != null);
        JsonNode item = plan.path("days").get(0).path("items").get(0);
        long itemId = item.path("id").asLong();
        int sets = item.path("sets").asInt();
        int reps = item.path("reps").asInt();
        try {
            putJson(admin, "/api/v1/admin/plans/core-2w/items/" + itemId, "{\"sets\":4,\"reps\":33}")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.adminEdited").value(true));
            mvc.perform(get("/api/v1/plans/core-2w").header("Authorization", user.token()))
                    .andExpect(jsonPath("$.data.days[0].items[0].sets").value(4))
                    .andExpect(jsonPath("$.data.days[0].items[0].reps").value(33));

            putJson(admin, "/api/v1/admin/plans/core-2w/items/" + itemId, "{\"sets\":0,\"reps\":33}")
                    .andExpect(status().isBadRequest());
            putJson(admin, "/api/v1/admin/plans/strength-4w/items/" + itemId, "{\"sets\":3,\"reps\":10}")
                    .andExpect(status().isNotFound());      // bài không thuộc giáo án này
        } finally {
            putJson(admin, "/api/v1/admin/plans/core-2w/items/" + itemId,
                    "{\"sets\":" + sets + ",\"reps\":" + reps + "}").andExpect(status().isOk());
        }
    }

    // ------------------------------------------------------------------ Tìm kiếm, chi tiết, thống kê

    @Test
    void searchDetailAndStatsReflectRealWorkouts() throws Exception {
        String tag = UUID.randomUUID().toString().substring(0, 6);
        Account user = register("Hoàng Tuấn Kiệt " + tag);
        String admin = adminToken();
        String body = """
                {"exerciseId":"squat","durationSec":90,"targetReps":15,"totalReps":12,"correctReps":9,
                 "formErrors":{"knees_in":2,"squat_depth":1}}""";
        mvc.perform(post("/api/v1/sessions").header("Authorization", user.token())
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated());

        // Tìm không dấu
        mvc.perform(get("/api/v1/admin/users").param("q", "hoang tuan kiet " + tag).header("Authorization", admin))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value(user.id()))
                .andExpect(jsonPath("$.data.items[0].sessionCount").value(1));
        mvc.perform(get("/api/v1/admin/users").param("size", "5").header("Authorization", admin))
                .andExpect(jsonPath("$.data.items", hasSize(5)))
                .andExpect(jsonPath("$.data.size").value(5));

        mvc.perform(get("/api/v1/admin/users/" + user.id()).header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.email").value(user.email()))
                .andExpect(jsonPath("$.data.recentSessions", hasSize(1)))
                .andExpect(jsonPath("$.data.recentSessions[0].exerciseId").value("squat"))
                .andExpect(jsonPath("$.data.recentSessions[0].correctReps").value(9));
        mvc.perform(get("/api/v1/admin/users/khong-ton-tai").header("Authorization", admin))
                .andExpect(status().isNotFound());

        MvcResult stats = mvc.perform(get("/api/v1/admin/stats").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionsPerDay", hasSize(30)))
                .andExpect(jsonPath("$.data.topExercises[*].exerciseId", hasItem("squat")))
                .andExpect(jsonPath("$.data.topFormErrors[*].key", hasItem("knees_in")))
                .andReturn();
        JsonNode s = data(stats);
        assertTrue(s.path("sessionsInRange").asLong() >= 1);
        assertTrue(s.path("activeUsers").asLong() >= 1);
        long today = s.path("sessionsPerDay").get(29).path("count").asLong();
        assertTrue(today >= 1, "buổi tập vừa lưu phải nằm ở ngày cuối (hôm nay, giờ Việt Nam)");
        assertEquals(30, s.path("days").asInt());
    }

    // ------------------------------------------------------------------ helpers

    private Account register(String name) throws Exception {
        String email = "admin-test-" + UUID.randomUUID().toString().substring(0, 8) + "@gmail.com";
        String password = "abc12345";
        String body = """
                {"displayName":"%s","email":"%s","password":"%s"}""".formatted(name, email, password);
        MvcResult res = mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        String token = "Bearer " + data(res).path("accessToken").asText();
        String id = data(res).path("user").path("id").asText();
        return new Account(token, email, password, id);
    }

    private String adminToken() throws Exception {
        MvcResult res = loginStatus(ADMIN_EMAIL, ADMIN_PASSWORD).andExpect(status().isOk()).andReturn();
        return "Bearer " + data(res).path("accessToken").asText();
    }

    private ResultActions loginStatus(String email, String password) throws Exception {
        String body = """
                {"email":"%s","password":"%s"}""".formatted(email, password);
        return mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private ResultActions putJson(String token, String url, String body) throws Exception {
        return mvc.perform(put(url).header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private String exerciseJson(String name, String description, int sets, int reps, double kcal) throws Exception {
        var body = mapper.createObjectNode();
        body.put("name", name);
        body.put("description", description);
        body.put("defaultSets", sets);
        body.put("defaultReps", reps);
        body.put("caloriesPerRep", kcal);
        return mapper.writeValueAsString(body);
    }

    private JsonNode data(MvcResult res) throws Exception {
        return mapper.readTree(res.getResponse().getContentAsString()).path("data");
    }
}
