package com.aicoach.api;

import com.aicoach.api.auth.reset.ResetCodeSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Quên mật khẩu (Figma 08–11) trên PostgreSQL thật. Email được thay bằng mock để bắt mã. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PasswordResetFlowTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ResetCodeSender sender;

    private static String randomEmail() {
        return "reset-" + UUID.randomUUID().toString().substring(0, 8) + "@gmail.com";
    }

    private void register(String email, String password) throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"displayName":"Người Quên","email":"%s","password":"%s"}""".formatted(email, password)))
                .andExpect(status().isCreated());
    }

    private String login(String email, String password, int expectStatus) throws Exception {
        MvcResult res = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}""".formatted(email, password)))
                .andExpect(status().is(expectStatus))
                .andReturn();
        return "Bearer " + mapper.readTree(res.getResponse().getContentAsString()).path("data").path("accessToken").asText();
    }

    private void forgot(String email) throws Exception {
        mvc.perform(post("/api/v1/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s"}""".formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.resendAfterSeconds").value(60))
                .andExpect(jsonPath("$.data.expiresInSeconds").value(600));
    }

    private String capturedCode(String email) {
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(sender).send(eq(email), any(), code.capture(), eq(10));
        return code.getValue();
    }

    private static String wrongCode(String real) {
        return real.equals("000000") ? "111111" : "000000";
    }

    @Test
    void fullResetFlow() throws Exception {
        String email = randomEmail();
        register(email, "abc12345");
        String oldToken = login(email, "abc12345", 200);

        // 08: gửi mã — email viết hoa vẫn nhận ra
        forgot(email.toUpperCase());
        String code = capturedCode(email);

        // Bấm gửi lại ngay: không gửi thêm email
        forgot(email);
        verify(sender, times(1)).send(eq(email), any(), anyString(), anyInt());

        // 09: sai mã → báo còn 4 lần
        mvc.perform(post("/api/v1/auth/verify-code").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","code":"%s"}""".formatted(email, wrongCode(code))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("còn 4 lần")));

        // 09: đúng mã
        mvc.perform(post("/api/v1/auth/verify-code").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","code":"%s"}""".formatted(email, code)))
                .andExpect(status().isOk());

        // 10: mật khẩu yếu bị từ chối
        mvc.perform(post("/api/v1/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","code":"%s","newPassword":"12345678"}""".formatted(email, code)))
                .andExpect(status().isBadRequest());

        // 10 → 11: đặt mật khẩu mới
        mvc.perform(post("/api/v1/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","code":"%s","newPassword":"moi98765"}""".formatted(email, code)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Đã đặt lại mật khẩu"));

        // Token cũ bị huỷ (đăng xuất khỏi mọi thiết bị)
        mvc.perform(get("/api/v1/users/me").header("Authorization", oldToken))
                .andExpect(status().isUnauthorized());

        // Mã đã dùng không dùng lại được
        mvc.perform(post("/api/v1/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","code":"%s","newPassword":"khac12345"}""".formatted(email, code)))
                .andExpect(status().isBadRequest());

        // Mật khẩu cũ hết tác dụng, mật khẩu mới đăng nhập được
        login(email, "abc12345", 401);
        // A fresh token must work immediately, including within the reset's second.
        String newToken = login(email, "moi98765", 200);
        mvc.perform(get("/api/v1/users/me").header("Authorization", newToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(email));
    }

    @Test
    void fiveWrongCodesLockTheCode() throws Exception {
        String email = randomEmail();
        register(email, "abc12345");
        forgot(email);
        String code = capturedCode(email);

        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/v1/auth/verify-code").contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email":"%s","code":"%s"}""".formatted(email, wrongCode(code))))
                    .andExpect(status().isBadRequest());
        }
        // Đúng mã nhưng đã bị khoá
        mvc.perform(post("/api/v1/auth/verify-code").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","code":"%s"}""".formatted(email, code)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("quá 5 lần")));
    }

    @Test
    void unknownEmailGetsSameAnswerButNoEmail() throws Exception {
        String email = randomEmail();
        forgot(email);
        verify(sender, never()).send(anyString(), any(), anyString(), anyInt());

        mvc.perform(post("/api/v1/auth/verify-code").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","code":"123456"}""".formatted(email)))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"khong-phai-email"}"""))
                .andExpect(status().isBadRequest());
    }
}
