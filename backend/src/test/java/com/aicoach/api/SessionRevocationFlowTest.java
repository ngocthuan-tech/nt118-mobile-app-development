package com.aicoach.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Same frozen second for registration, every device login and repeated password changes. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SessionRevocationFlowTest.FrozenTime.class)
class SessionRevocationFlowTest {
    @TestConfiguration static class FrozenTime {
        @Bean @Primary Clock sessionTestClock() { return Clock.fixed(Instant.parse("2026-11-09T10:00:00Z"), ZoneOffset.UTC); }
    }
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired Clock clock;
    @Autowired com.aicoach.api.user.UserRepository users;
    @Autowired org.springframework.transaction.PlatformTransactionManager transactions;
    private record Account(String email, String token, String id) { }
    private Account register() throws Exception {
        String email="session-"+UUID.randomUUID()+"@example.com";
        JsonNode data=mapper.readTree(mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"displayName\":\"Session Test\",\"email\":\""+email+"\",\"password\":\"OldPass123\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).path("data");
        return new Account(email,"Bearer "+data.path("accessToken").asText(),data.path("user").path("id").asText());
    }
    private String login(String email,String password,int expected) throws Exception {
        JsonNode data=mapper.readTree(mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\""+email+"\",\"password\":\""+password+"\"}"))
                .andExpect(status().is(expected)).andReturn().getResponse().getContentAsString()).path("data");
        return "Bearer "+data.path("accessToken").asText();
    }
    private void change(String token,String current,String password,int expected) throws Exception {
        mvc.perform(put("/api/v1/users/me/password").header("Authorization",token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\""+current+"\",\"newPassword\":\""+password+"\"}"))
                .andExpect(status().is(expected));
    }
    private void valid(String token) throws Exception {
        mvc.perform(get("/api/v1/users/me/session").header("Authorization",token)).andExpect(status().isOk());
    }
    private void revoked(String token) throws Exception {
        for(String path:new String[]{"/api/v1/users/me/session","/api/v1/users/me","/api/v1/sessions"})
            mvc.perform(get(path).header("Authorization",token)).andExpect(status().isUnauthorized())
                    .andExpect(header().string("X-Session-Reason","PASSWORD_CHANGED"))
                    .andExpect(jsonPath("$.message").value("Mật khẩu đã thay đổi. Vui lòng đăng nhập lại."));
    }
    @Test void changeRevokesAllOldDevicesAndNewLoginWorksInSameSecond() throws Exception {
        Account account=register(), other=register();
        String deviceB=login(account.email(),"OldPass123",200);
        assertNotEquals(account.token(),deviceB); valid(account.token()); valid(deviceB);
        change(account.token(),"OldPass123","NewPass456",200);
        revoked(account.token()); revoked(deviceB); valid(other.token());
        login(account.email(),"OldPass123",401);
        String fresh=login(account.email(),"NewPass456",200); valid(fresh);
        change(fresh,"NewPass456","Another789",200);
        revoked(fresh); valid(login(account.email(),"Another789",200));
    }
    @Test void failedChangesDoNotLogOutAnyDevice() throws Exception {
        Account a=register(); String b=login(a.email(),"OldPass123",200);
        change(a.token(),"WrongPass123","NewPass456",400);
        change(a.token(),"OldPass123","OldPass123",400);
        change(a.token(),"OldPass123","weak",400);
        valid(a.token()); valid(b);
    }
    @Test void legacyTokenIsAcceptedUntilFirstChangeThenAlwaysRevoked() throws Exception {
        Account a=register();
        String legacy="Bearer "+Jwts.builder().subject(a.id()).issuedAt(Date.from(clock.instant()))
                .expiration(Date.from(clock.instant().plusSeconds(3600)))
                .signWith(Keys.hmacShaKeyFor("test-secret-for-ci-only-0123456789-abcdefghijklmnop".getBytes(StandardCharsets.UTF_8)))
                .compact();
        valid(legacy); change(legacy,"OldPass123","NewPass456",200); revoked(legacy);
        valid(login(a.email(),"NewPass456",200));
    }
    @Test void profileAndWorkoutWritesCannotRestoreRevokedSessions() throws Exception {
        Account a=register(); change(a.token(),"OldPass123","NewPass456",200);
        String fresh=login(a.email(),"NewPass456",200);
        mvc.perform(put("/api/v1/users/me").header("Authorization",fresh).contentType(MediaType.APPLICATION_JSON)
                .content("{\"displayName\":\"Updated Profile\"}")).andExpect(status().isOk());
        mvc.perform(post("/api/v1/sessions").header("Authorization",fresh).contentType(MediaType.APPLICATION_JSON)
                .content("{\"exerciseId\":\"pushup\",\"totalReps\":10,\"correctReps\":9,\"durationSec\":60}"))
                .andExpect(status().isCreated());
        revoked(a.token()); valid(fresh);
    }
    @Test void missingOrForgedTokenIsGenericUnauthorized() throws Exception {
        mvc.perform(get("/api/v1/users/me/session")).andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("X-Session-Reason"));
        mvc.perform(get("/api/v1/users/me/session").header("Authorization","Bearer bad.token.signature"))
                .andExpect(status().isUnauthorized()).andExpect(header().doesNotExist("X-Session-Reason"));
    }

    @Test void staleInFlightProfileWriteCannotRestoreOldPasswordOrSessionVersion() throws Exception {
        Account a=register();
        java.util.concurrent.ExecutorService secondDevice=java.util.concurrent.Executors.newSingleThreadExecutor();
        try {
            new org.springframework.transaction.support.TransactionTemplate(transactions).executeWithoutResult(tx -> {
                var stale=users.findById(a.id()).orElseThrow();
                try {
                    secondDevice.submit(() -> { change(a.token(),"OldPass123","NewPass456",200); return null; })
                            .get(15,java.util.concurrent.TimeUnit.SECONDS);
                } catch(Exception e) { throw new RuntimeException(e); }
                stale.setDisplayName("Slow profile request");
                users.saveAndFlush(stale);
            });
            login(a.email(),"OldPass123",401);
            valid(login(a.email(),"NewPass456",200)); revoked(a.token());
        } finally { secondDevice.shutdownNow(); }
    }
}
