package com.aicoach.data.local;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Cài đặt lưu trên máy (không gửi lên server).
 * Thành viên A ghi (màn Cài đặt S14, Splash S01); module AI của Trưởng nhóm đọc isVoiceEnabled().
 */
public class SettingsPrefs {

    private static final String FILE = "aicoach_settings";
    private static final String KEY_ONBOARDING_DONE = "onboarding_done";
    private static final String KEY_VOICE_ENABLED = "voice_enabled";
    private static final String KEY_REMINDER_ENABLED = "reminder_enabled";
    private static final String KEY_REMINDER_TIME = "reminder_time";   // "HH:mm"
    private static final String KEY_WEIGHT_UNIT = "weight_unit";       // "kg" | "lb"

    private final SharedPreferences prefs;

    public SettingsPrefs(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public boolean isOnboardingDone() {
        return prefs.getBoolean(KEY_ONBOARDING_DONE, false);
    }

    public void setOnboardingDone(boolean done) {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, done).apply();
    }

    public boolean isVoiceEnabled() {
        return prefs.getBoolean(KEY_VOICE_ENABLED, true);
    }

    public void setVoiceEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_VOICE_ENABLED, enabled).apply();
    }

    public boolean isReminderEnabled() {
        return prefs.getBoolean(KEY_REMINDER_ENABLED, false);
    }

    public void setReminderEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply();
    }

    public String getReminderTime() {
        return prefs.getString(KEY_REMINDER_TIME, "19:00");
    }

    public void setReminderTime(String hhmm) {
        prefs.edit().putString(KEY_REMINDER_TIME, hhmm).apply();
    }

    public String getWeightUnit() {
        return prefs.getString(KEY_WEIGHT_UNIT, "kg");
    }

    public void setWeightUnit(String unit) {
        prefs.edit().putString(KEY_WEIGHT_UNIT, unit).apply();
    }
}
