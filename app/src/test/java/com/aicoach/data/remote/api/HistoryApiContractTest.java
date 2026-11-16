package com.aicoach.data.remote.api;

import com.aicoach.data.repository.HistoryRepository;
import okhttp3.Request;
import org.junit.Test;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static org.junit.Assert.*;

public class HistoryApiContractTest {
    private final SessionApi api = new Retrofit.Builder().baseUrl("https://example.invalid/api/v1/")
            .addConverterFactory(GsonConverterFactory.create()).build().create(SessionApi.class);
    @Test public void listIsReadOnlyAndUsesDocumentedLimit() {
        Request request = api.getSessions(HistoryRepository.LIMIT).request();
        assertEquals("GET",request.method()); assertEquals("/api/v1/sessions",request.url().encodedPath());
        assertEquals("50",request.url().queryParameter("limit")); assertNull(request.body());
    }
    @Test public void detailUsesSessionIdNotExerciseId() {
        Request request = api.getSession("session-123").request();
        assertEquals("GET",request.method()); assertEquals("/api/v1/sessions/session-123",request.url().encodedPath());
    }
}
