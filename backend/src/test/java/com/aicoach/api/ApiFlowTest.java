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

import java.util.UUID;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Kiểm thử toàn luồng mà thầy chấm: Authentication → User Profile → Home, cộng bài tập và lưu buổi tập. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiFlowTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    private String register(String email, String password) throws Exception {
        String body = """
                {"displayName":"Nguyễn Văn A","email":"%s","password":"%s"}""".formatted(email, password);
        MvcResult res = mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andReturn();
        return token(res);
    }

    private String login(String email, String password) throws Exception {
        String body = """
                {"email":"%s","password":"%s"}""".formatted(email, password);
        MvcResult res = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn();
        return token(res);
    }

    private String token(MvcResult res) throws Exception {
        JsonNode node = mapper.readTree(res.getResponse().getContentAsString());
        return "Bearer " + node.path("data").path("accessToken").asText();
    }

    private static String randomEmail() {
        return "user-" + UUID.randomUUID().toString().substring(0, 8) + "@gmail.com";
    }

    @Test
    void healthIsPublic() throws Exception {
        mvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.database").value("UP"));
    }

    @Test
    void protectedApiRejectsMissingOrBadToken() throws Exception {
        mvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
        mvc.perform(get("/api/v1/home").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerValidationAndDuplicate() throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"displayName":"A","email":"khong-phai-email","password":"123"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        String email = randomEmail();
        register(email, "abc12345");
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"displayName":"Người khác","email":"%s","password":"abc12345"}"""
                                .formatted(email.toUpperCase())))
                .andExpect(status().isConflict());
    }

    @Test
    void loginWrongPasswordIs401() throws Exception {
        String email = randomEmail();
        register(email, "abc12345");
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"sai12345"}""".formatted(email)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Email hoặc mật khẩu không đúng"));
    }

    @Test
    void fullUserFlow() throws Exception {
        String email = randomEmail();
        register(email, "abc12345");
        String auth = login("  " + email.toUpperCase() + " ", "abc12345");

        // User Profile
        mvc.perform(get("/api/v1/users/me").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.profileCompleted").value(false))
                .andExpect(jsonPath("$.data.streak").value(0));

        mvc.perform(put("/api/v1/users/me").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"goal":"bay_len_troi"}"""))
                .andExpect(status().isBadRequest());

        mvc.perform(put("/api/v1/users/me").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"heightCm":170,"weightKg":65.5,"goal":"build_muscle","level":"beginner",
                                 "weeklyGoalSessions":4,"profileCompleted":true,"activePlanId":"core-2w"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.heightCm").value(170.0))
                .andExpect(jsonPath("$.data.profileCompleted").value(true))
                .andExpect(jsonPath("$.data.activePlanId").value("core-2w"));

        // Exercises
        mvc.perform(get("/api/v1/exercises").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(9)));
        mvc.perform(get("/api/v1/exercises").param("q", "hit dat").header("Authorization", auth))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value("pushup"));
        mvc.perform(get("/api/v1/exercises/squat").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.steps", hasSize(3)));
        mvc.perform(get("/api/v1/exercises/khong-co").header("Authorization", auth))
                .andExpect(status().isNotFound());

        // Lưu buổi tập → streak, XP cập nhật
        mvc.perform(post("/api/v1/sessions").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"exerciseId":"plank","durationSec":120,"targetReps":90,"totalReps":90,
                                 "correctReps":80,"formErrors":{"hip_sag":2},"planId":"core-2w"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.exerciseName").value("Plank"))
                .andExpect(jsonPath("$.data.xpEarned").value(170));

        mvc.perform(get("/api/v1/users/me").header("Authorization", auth))
                .andExpect(jsonPath("$.data.streak").value(1))
                .andExpect(jsonPath("$.data.totalReps").value(90));

        // Home
        mvc.perform(get("/api/v1/home").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.streak").value(1))
                .andExpect(jsonPath("$.data.week.days", hasSize(7)))
                .andExpect(jsonPath("$.data.today.sessions").value(1))
                .andExpect(jsonPath("$.data.activePlan.planId").value("core-2w"))
                .andExpect(jsonPath("$.data.activePlan.sessionsDone").value(1))
                .andExpect(jsonPath("$.data.todayWorkout.exercises", hasSize(3)));

        mvc.perform(get("/api/v1/sessions").header("Authorization", auth))
                .andExpect(jsonPath("$.data", hasSize(1)));

        // Đổi mật khẩu
        mvc.perform(put("/api/v1/users/me/password").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"abc12345","newPassword":"xyz98765"}"""))
                .andExpect(status().isOk());
        login(email, "xyz98765");
    }

    @Test
    void demoAccountHasHistoryForHome() throws Exception {
        String auth = login("demo@aicoach.vn", "Demo1234");
        mvc.perform(get("/api/v1/home").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.displayName").value("Minh"))
                .andExpect(jsonPath("$.data.streak").value(5))
                .andExpect(jsonPath("$.data.activePlan.planId").value("fat-burn-4w"))
                .andExpect(jsonPath("$.data.activePlan.sessionsDone").value(8))
                .andExpect(jsonPath("$.data.todayWorkout.exercises", hasSize(greaterThanOrEqualTo(1))));
        mvc.perform(get("/api/v1/plans").header("Authorization", auth))
                .andExpect(jsonPath("$.data", hasSize(3)));
    }
}
