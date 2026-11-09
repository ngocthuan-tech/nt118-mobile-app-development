package com.aicoach.data.local;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.aicoach.data.remote.AuthInterceptor;
import com.aicoach.data.remote.AuthenticatedSession;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class SessionSecurityTest {
    private TokenManager tokens;
    private String previous;
    @Before public void before() {
        tokens=TokenManager.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext());
        previous=tokens.getToken(); tokens.clear();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(SessionEvents::consume);
    }
    @After public void after() {
        tokens.clear(); if(previous!=null) tokens.saveToken(previous);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(SessionEvents::consume);
    }
    private Response response(Request request,int status,String reason) {
        Response.Builder b=new Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(status).message("Test")
                .body(ResponseBody.create(MediaType.get("application/json"),"{\"success\":false}"));
        if(reason!=null)b.header("X-Session-Reason",reason); return b.build();
    }
    private void execute(Request request,Interceptor terminal) throws IOException {
        OkHttpClient client=new OkHttpClient.Builder().addInterceptor(new AuthInterceptor(tokens)).addInterceptor(terminal).build();
        try(Response ignored=client.newCall(request).execute()) { }
        finally { client.dispatcher().executorService().shutdown(); client.connectionPool().evictAll(); }
    }
    private Request request(String path) { return new Request.Builder().url("https://example.invalid/api/v1/"+path).build(); }

    @Test public void revokedTokenClearsOnceAndKeepsSpecificReason() throws Exception {
        tokens.saveToken("device-b");
        execute(request("users/me/session"), chain -> response(chain.request(),401,"PASSWORD_CHANGED"));
        assertNull(tokens.getToken());
        assertFalse(tokens.expireIfMatches("device-b",SessionEndReason.EXPIRED));
        assertEquals(SessionEndReason.PASSWORD_CHANGED,tokens.takeEndReason());
        assertNull(tokens.takeEndReason());
    }
    @Test public void lateUnauthorizedResponseCannotLogOutNewLogin() throws Exception {
        tokens.saveToken("old-session");
        execute(request("users/me"),chain -> {
            assertEquals("Bearer old-session",chain.request().header("Authorization"));
            tokens.saveToken("new-session"); return response(chain.request(),401,"PASSWORD_CHANGED");
        });
        assertEquals("new-session",tokens.getToken()); assertNull(tokens.takeEndReason());
    }
    @Test public void sensitiveRequestRemainsBoundToOriginalAccount() throws Exception {
        tokens.saveToken("new-account");
        Request pinned=request("users/me/password").newBuilder().tag(AuthenticatedSession.class,new AuthenticatedSession("original-account")).build();
        execute(pinned,chain -> {
            assertEquals("Bearer original-account",chain.request().header("Authorization"));
            return response(chain.request(),401,"PASSWORD_CHANGED");
        });
        assertEquals("new-account",tokens.getToken());
    }
    @Test public void wrongLoginPasswordAndNetworkErrorsDoNotEndCurrentSession() throws Exception {
        tokens.saveToken("existing-session");
        execute(request("auth/login"),chain -> {
            assertNull(chain.request().header("Authorization")); return response(chain.request(),401,null);
        });
        execute(request("users/me/session"),chain -> response(chain.request(),503,null));
        try { execute(request("users/me/session"),chain -> {throw new IOException("offline");}); fail(); }
        catch(IOException expected) { }
        assertEquals("existing-session",tokens.getToken()); assertNull(tokens.takeEndReason());
    }
    @Test public void genericUnauthorizedHasDifferentMessageAndLoginClearsPendingNotice() throws Exception {
        tokens.saveToken("expired");
        execute(request("users/me/session"),chain -> response(chain.request(),401,null));
        assertEquals(SessionEndReason.EXPIRED,tokens.takeEndReason());
        tokens.saveToken("old"); tokens.expireIfMatches("old",SessionEndReason.PASSWORD_CHANGED);
        tokens.saveToken("new"); assertNull(tokens.takeEndReason());
    }
    @Test public void monitorDoesNotProbeWhileLoggedOutAndStopCancelsInFlightCall() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            AtomicInteger calls=new AtomicInteger();
            HoldingCall pending=new HoldingCall();
            SessionMonitor monitor=new SessionMonitor(tokens,()->{calls.incrementAndGet();return pending;},30_000);
            monitor.start(); monitor.start(); assertEquals(0,calls.get()); monitor.stop();
            tokens.saveToken("session"); monitor.start(); monitor.start(); assertEquals(1,calls.get());
            monitor.stop(); assertTrue(pending.canceled);
            pending.callback.onFailure(pending,new IOException("canceled"));
        });
    }
    private static final class HoldingCall implements retrofit2.Call<com.aicoach.data.remote.ApiResponse<Void>> {
        boolean canceled,executed;
        retrofit2.Callback<com.aicoach.data.remote.ApiResponse<Void>> callback;
        @Override public void enqueue(retrofit2.Callback<com.aicoach.data.remote.ApiResponse<Void>> c){executed=true;callback=c;}
        @Override public retrofit2.Response<com.aicoach.data.remote.ApiResponse<Void>> execute(){throw new UnsupportedOperationException();}
        @Override public boolean isExecuted(){return executed;}
        @Override public void cancel(){canceled=true;}
        @Override public boolean isCanceled(){return canceled;}
        @Override public retrofit2.Call<com.aicoach.data.remote.ApiResponse<Void>> clone(){return new HoldingCall();}
        @Override public Request request(){return new Request.Builder().url("https://example.invalid").build();}
        @Override public okio.Timeout timeout(){return okio.Timeout.NONE;}
    }
}
