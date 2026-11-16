package com.aicoach.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.aicoach.common.Resource;
import com.aicoach.data.local.SessionEndReason;
import com.aicoach.data.local.SessionEvents;
import com.aicoach.data.local.TokenManager;
import com.aicoach.data.remote.AuthInterceptor;
import com.aicoach.data.remote.api.UserApi;
import com.google.gson.JsonParser;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static org.junit.Assert.*;

/** Real Retrofit -> repository -> token lifecycle, with an in-memory HTTP response and no server writes. */
@RunWith(AndroidJUnit4.class)
public class PasswordSessionRepositoryTest {
    private TokenManager tokens;
    private String previous;

    @Before public void before() {
        tokens = TokenManager.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext());
        previous = tokens.getToken();
        tokens.saveToken("initiating-session");
        main(SessionEvents::consume);
    }

    @After public void after() {
        tokens.clear();
        if (previous != null) tokens.saveToken(previous);
        main(SessionEvents::consume);
    }

    private static void main(Runnable action) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(action);
    }

    @Test public void successfulNullDataEndsCurrentSessionBeforePublishingSuccess() throws Exception {
        assertEquals(Resource.Status.SUCCESS, change(200, true, false).status);
        assertNull(tokens.getToken());
        assertEquals(SessionEndReason.PASSWORD_UPDATED_HERE, tokens.takeEndReason());
        assertEquals(Boolean.TRUE, SessionEvents.expired().getValue());
    }

    @Test public void rejectedChangeKeepsSessionAndDoesNotPublishLogout() throws Exception {
        assertEquals(Resource.Status.ERROR, change(400, false, false).status);
        assertEquals("initiating-session", tokens.getToken());
        assertNull(tokens.takeEndReason());
        assertNotEquals(Boolean.TRUE, SessionEvents.expired().getValue());
        assertEquals(Resource.Status.ERROR, change(200, false, false).status);
        assertEquals("initiating-session", tokens.getToken());
    }

    @Test public void lateSuccessfulChangeCannotLogOutANewerLogin() throws Exception {
        assertEquals(Resource.Status.SUCCESS, change(200, true, true).status);
        assertEquals("new-login", tokens.getToken());
        assertNull(tokens.takeEndReason());
        assertNotEquals(Boolean.TRUE, SessionEvents.expired().getValue());
    }

    private Resource<Void> change(int code, boolean success, boolean replaceSession) throws Exception {
        AtomicReference<Throwable> requestFailure = new AtomicReference<>();
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(tokens))
                .addInterceptor(chain -> {
                    try {
                        assertEquals("PUT", chain.request().method());
                        assertEquals("/api/v1/users/me/password", chain.request().url().encodedPath());
                        assertEquals("Bearer initiating-session", chain.request().header("Authorization"));
                        Buffer buffer = new Buffer();
                        chain.request().body().writeTo(buffer);
                        var json = JsonParser.parseString(buffer.readUtf8()).getAsJsonObject();
                        assertEquals("OldPass123", json.get("currentPassword").getAsString());
                        assertEquals("NewPass456", json.get("newPassword").getAsString());
                    } catch (Throwable error) { requestFailure.set(error); }
                    if (replaceSession) tokens.saveToken("new-login");
                    return new Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                            .code(code).message("Test")
                            .body(ResponseBody.create(MediaType.get("application/json"),
                                    "{\"success\":" + success + ",\"message\":\"Test\",\"data\":null}"))
                            .build();
                }).build();
        UserApi api = new Retrofit.Builder().baseUrl("https://example.invalid/api/v1/")
                .client(client).addConverterFactory(GsonConverterFactory.create()).build().create(UserApi.class);
        AtomicReference<LiveData<Resource<Void>>> live = new AtomicReference<>();
        AtomicReference<Resource<Void>> result = new AtomicReference<>();
        AtomicReference<String> tokenAtSuccess = new AtomicReference<>();
        CountDownLatch complete = new CountDownLatch(1);
        Observer<Resource<Void>> observer = state -> {
            if (state.status != Resource.Status.LOADING) {
                tokenAtSuccess.set(tokens.getToken());
                result.set(state);
                complete.countDown();
            }
        };
        try {
            main(() -> {
                live.set(new UserRepository(api, tokens).changePassword("OldPass123", "NewPass456"));
                live.get().observeForever(observer);
            });
            assertTrue("Password response timed out", complete.await(10, TimeUnit.SECONDS));
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            if (requestFailure.get() != null) throw new AssertionError(requestFailure.get());
            if (success && code == 200 && !replaceSession) assertNull(tokenAtSuccess.get());
            return result.get();
        } finally {
            main(() -> { if (live.get() != null) live.get().removeObserver(observer); });
            client.dispatcher().executorService().shutdown();
            client.connectionPool().evictAll();
        }
    }
}
