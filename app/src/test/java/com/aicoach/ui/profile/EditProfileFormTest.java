package com.aicoach.ui.profile;

import com.aicoach.data.model.User;
import com.aicoach.data.remote.api.UserApi;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.Request;
import okio.Buffer;
import org.junit.Test;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static org.junit.Assert.*;

public class EditProfileFormTest {
    private EditProfileForm form() {
        return new EditProfileForm(new Gson().fromJson("{\"displayName\":\"Test User\",\"heightCm\":170,\"weightKg\":65,\"weeklyGoalSessions\":3}", User.class));
    }
    @Test public void unchangedFormattingDoesNotSave() {
        EditProfileForm f = form();
        f.name = " Test User "; f.height = "170"; f.weight = "65,0";
        assertTrue(f.valid()); assertFalse(f.dirty());
    }
    @Test public void validatesNamesAndNumericBoundaries() {
        EditProfileForm f = form();
        for (String bad : new String[]{"", "99", "251", "NaN", "Infinity", "abc"}) {
            f.height = bad; assertFalse(bad, f.validHeight());
        }
        f.height = "100"; assertTrue(f.validHeight());
        f.height = "250"; assertTrue(f.validHeight());
        f.weight = "29.9"; assertFalse(f.validWeight());
        f.weight = "30"; assertTrue(f.validWeight());
        f.weight = "250"; assertTrue(f.validWeight());
        f.weight = "250.1"; assertFalse(f.validWeight());
        f.name = " A "; assertFalse(f.validName());
        f.name = new String(new char[51]).replace('\0', 'A'); assertFalse(f.validName());
    }
    @Test public void absentMeasurementsCanRemainAbsent() {
        EditProfileForm f = new EditProfileForm(new Gson().fromJson("{\"displayName\":\"Test User\",\"weeklyGoalSessions\":3}", User.class));
        assertTrue(f.valid());
        f.name = "Updated User";
        JsonObject data = new Gson().toJsonTree(f.request()).getAsJsonObject();
        assertEquals(1, data.size()); assertFalse(data.has("heightCm"));
    }
    @Test public void putContainsOnlyChangesAndCorrectEndpoint() throws Exception {
        EditProfileForm f = form(); f.name = " Updated User "; f.weight = "66,5";
        UserApi api = new Retrofit.Builder().baseUrl("https://example.invalid/api/v1/")
                .addConverterFactory(GsonConverterFactory.create()).build().create(UserApi.class);
        Request request = api.updateMe(f.request()).request();
        assertEquals("PUT", request.method()); assertEquals("/api/v1/users/me", request.url().encodedPath());
        Buffer buffer = new Buffer(); request.body().writeTo(buffer);
        JsonObject data = new Gson().fromJson(buffer.readUtf8(), JsonObject.class);
        assertEquals(2, data.size()); assertEquals("Updated User", data.get("displayName").getAsString());
        assertEquals(66.5, data.get("weightKg").getAsDouble(), 0.001);
    }
}
