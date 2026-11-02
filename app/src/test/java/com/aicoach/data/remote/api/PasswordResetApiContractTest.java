package com.aicoach.data.remote.api;

import com.aicoach.data.remote.ApiResponse;
import com.aicoach.data.remote.dto.AuthDtos;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;
import okhttp3.Request;
import okio.Buffer;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static org.junit.Assert.*;

/** Builds HTTP requests without sending mail or changing any remote account. */
public class PasswordResetApiContractTest {
    private final Gson gson = new Gson();
    private final AuthApi api = new Retrofit.Builder()
            .baseUrl("https://example.invalid/api/v1/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build().create(AuthApi.class);

    private JsonObject body(Request request) throws Exception {
        assertEquals("POST", request.method());
        assertNotNull(request.body());
        Buffer buffer = new Buffer();
        request.body().writeTo(buffer);
        return gson.fromJson(buffer.readUtf8(), JsonObject.class);
    }

    @Test public void sendCodeContainsOnlyEmail() throws Exception {
        Request request = api.forgotPassword(
                new AuthDtos.ForgotPasswordRequest("person@example.com")).request();
        assertEquals("/api/v1/auth/forgot-password", request.url().encodedPath());
        JsonObject data = body(request);
        assertEquals(1, data.size());
        assertEquals("person@example.com", data.get("email").getAsString());
    }

    @Test public void verifyPreservesLeadingZeroInCode() throws Exception {
        Request request = api.verifyCode(
                new AuthDtos.VerifyCodeRequest("person@example.com", "012345")).request();
        assertEquals("/api/v1/auth/verify-code", request.url().encodedPath());
        JsonObject data = body(request);
        assertEquals(2, data.size());
        assertEquals("person@example.com", data.get("email").getAsString());
        assertEquals("012345", data.get("code").getAsString());
    }

    @Test public void resetSendsNewPasswordField() throws Exception {
        Request request = api.resetPassword(new AuthDtos.ResetPasswordRequest(
                "person@example.com", "012345", "NewPass123")).request();
        assertEquals("/api/v1/auth/reset-password", request.url().encodedPath());
        JsonObject data = body(request);
        assertEquals(3, data.size());
        assertEquals("person@example.com", data.get("email").getAsString());
        assertEquals("012345", data.get("code").getAsString());
        assertEquals("NewPass123", data.get("newPassword").getAsString());
    }

    @Test public void readsServerDurationsInsteadOfDemoConstants() {
        ApiResponse<AuthDtos.CodeSent> response = gson.fromJson(
                "{\"success\":true,\"data\":{\"resendAfterSeconds\":75,\"expiresInSeconds\":420}}",
                new TypeToken<ApiResponse<AuthDtos.CodeSent>>() {}.getType());
        assertTrue(response.isSuccess());
        assertEquals(75, response.getData().getResendAfterSeconds());
        assertEquals(420, response.getData().getExpiresInSeconds());
    }

    @Test public void successfulVoidResponseHasNoData() {
        ApiResponse<Void> response = gson.fromJson("{\"success\":true,\"data\":null}",
                new TypeToken<ApiResponse<Void>>() {}.getType());
        assertTrue(response.isSuccess());
        assertNull(response.getData());
    }
}
