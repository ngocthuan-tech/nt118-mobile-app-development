package com.aicoach.api;

import com.aicoach.api.session.WorkoutSession;
import com.aicoach.api.session.WorkoutSessionRepository;
import com.aicoach.api.user.UserAccount;
import com.aicoach.api.user.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import java.util.UUID;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AchievementFlowTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UserRepository users;
    @Autowired WorkoutSessionRepository sessions;

    private record Account(String token, String email) { }
    private Account account() throws Exception {
        String email = "achievement-" + UUID.randomUUID() + "@example.com";
        String response = mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"displayName\":\"Achievement Test\",\"email\":\"" + email + "\",\"password\":\"Test12345\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return new Account("Bearer " + mapper.readTree(response).path("data").path("accessToken").asText(), email);
    }
    private JsonNode summary(Account a) throws Exception {
        return mapper.readTree(mvc.perform(get("/api/v1/users/me/achievements").header("Authorization", a.token()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.exercises", hasSize(9)))
                .andReturn().getResponse().getContentAsString()).path("data");
    }
    private long xp(JsonNode summary, String exercise) {
        for (JsonNode e : summary.path("exercises")) {
            if (exercise.equals(e.path("exerciseId").asText())) return e.path("totalXp").asLong();
        }
        fail("Missing exercise " + exercise);
        return -1;
    }
    @Test void requiresAuthentication() throws Exception {
        mvc.perform(get("/api/v1/users/me/achievements")).andExpect(status().isUnauthorized());
    }
    @Test void newAccountHasZeroXpAndAllExercises() throws Exception {
        JsonNode data = summary(account());
        assertEquals(0, data.path("totalXp").asLong());
        for (JsonNode e : data.path("exercises")) {
            assertEquals(0, e.path("totalXp").asLong());
            assertFalse(e.path("exerciseName").asText().isBlank());
        }
    }
    @Test void completedSessionsAccumulateSeparatelyAndReadsNeverGrantXp() throws Exception {
        Account a = account();
        for (String exercise : new String[]{"pushup", "pushup", "plank"}) {
            mvc.perform(post("/api/v1/sessions").header("Authorization", a.token()).contentType(MediaType.APPLICATION_JSON)
                    .content("{\"exerciseId\":\"" + exercise + "\",\"durationSec\":60,\"totalReps\":20,\"correctReps\":15}"))
                    .andExpect(status().isCreated()).andExpect(jsonPath("$.data.xpEarned").value(35));
        }
        JsonNode data = summary(a);
        assertEquals(105, data.path("totalXp").asLong());
        assertEquals(70, xp(data, "pushup"));
        assertEquals(35, xp(data, "plank"));
        assertEquals(0, xp(data, "squat"));
        assertEquals(data, summary(a));
        assertEquals(0, xp(summary(account()), "pushup"));
    }
    @Test void aggregatesBeyondHistoryLimitAndDoesNotLeakOtherAccounts() throws Exception {
        Account a = account(), other = account();
        UserAccount user = users.findByEmail(a.email()).orElseThrow();
        UserAccount stranger = users.findByEmail(other.email()).orElseThrow();
        for (int i = 0; i < 125; i++) {
            WorkoutSession s = new WorkoutSession();
            s.setUserId(user.getId()); s.setExerciseId("pushup"); s.setStartedAt(Instant.now());
            s.setTotalReps(10); s.setCorrectReps(7); sessions.save(s);
        }
        WorkoutSession alien = new WorkoutSession();
        alien.setUserId(stranger.getId()); alien.setExerciseId("pushup"); alien.setStartedAt(Instant.now());
        alien.setTotalReps(1000); alien.setCorrectReps(1000); sessions.saveAndFlush(alien);
        user.setTotalXp(2125); users.saveAndFlush(user);
        JsonNode data = summary(a);
        assertEquals(2125, xp(data, "pushup"));
        assertEquals(2125, data.path("totalXp").asLong());
        assertEquals(2000, xp(summary(other), "pushup"));
    }
}
