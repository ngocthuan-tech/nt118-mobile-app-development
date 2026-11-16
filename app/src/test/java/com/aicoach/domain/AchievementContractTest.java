package com.aicoach.domain;

import com.aicoach.data.model.AchievementSummary;
import com.aicoach.data.remote.api.UserApi;
import com.google.gson.Gson;
import org.junit.Test;
import retrofit2.http.GET;
import static org.junit.Assert.*;

public class AchievementContractTest {
    @Test public void endpointIsAuthenticatedUserScope() throws Exception {
        assertEquals("users/me/achievements", UserApi.class.getMethod("getAchievements").getAnnotation(GET.class).value());
        assertEquals(0, UserApi.class.getMethod("getAchievements").getParameterCount());
    }
    @Test public void lifetimeXpPayloadDoesNotMixExercises() {
        AchievementSummary s = new Gson().fromJson("{\"totalXp\":6000,\"exercises\":["
                + "{\"exerciseId\":\"pushup\",\"exerciseName\":\"Hít đất\",\"totalXp\":2500},"
                + "{\"exerciseId\":\"squat\",\"exerciseName\":\"Squat\",\"totalXp\":0}]}", AchievementSummary.class);
        assertTrue(s.isValid());
        assertEquals("Kết Đan", AchievementRules.realm(s.getTotalXp()).current.name);
        assertEquals("Expert", AchievementRules.rank(s.getExercises().get(0).getTotalXp()).current.name);
        assertEquals("Novice", AchievementRules.rank(s.getExercises().get(1).getTotalXp()).current.name);
    }
    @Test public void missingOrMalformedExercisesAreNotTreatedAsZeroXp() {
        assertFalse(new Gson().fromJson("{}", AchievementSummary.class).isValid());
        assertFalse(new Gson().fromJson("{\"exercises\":[null]}", AchievementSummary.class).isValid());
        assertFalse(new Gson().fromJson("{\"exercises\":[{}]}", AchievementSummary.class).isValid());
        assertTrue(new Gson().fromJson("{\"totalXp\":0,\"exercises\":[]}", AchievementSummary.class).isValid());
    }
}
