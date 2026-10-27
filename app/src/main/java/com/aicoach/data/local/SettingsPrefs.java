package com.aicoach.data.local;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Cài đặt lưu trên máy (không gửi lên server).
 * Thành viên A ghi: Onboarding (02–04), Khảo sát 15 (giờ nhắc, ngày tập), Bật thông báo (20).
 * Module AI của Trưởng nhóm đọc isVoiceEnabled().
 */
public class SettingsPrefs {

    private static final String FILE = "aicoach_settings";
    private static final String KEY_ONBOARDING_DONE = "onboarding_done";
    private static final String KEY_VOICE_ENABLED = "voice_enabled";
    private static final String KEY_REMINDER_ENABLED = "reminder_enabled";
    private static final String KEY_REMINDER_TIME = "reminder_time";   // "HH:mm"
    private static final String KEY_WEIGHT_UNIT = "weight_unit";       // "kg" | "lb"
    private static final String KEY_REMINDER_DAYS = "reminder_days";   // "T2,T4,T6"
    private static final String KEY_NOTIFY_SOCIAL = "notify_social";
    private static final String KEY_NOTIFY_WEEKLY = "notify_weekly";

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

    public String getReminderDays() {
        return prefs.getString(KEY_REMINDER_DAYS, "T2,T4,T6");
    }

    public void setReminderDays(String days) {
        prefs.edit().putString(KEY_REMINDER_DAYS, days).apply();
    }

    public boolean isSocialNotifyEnabled() {
        return prefs.getBoolean(KEY_NOTIFY_SOCIAL, true);
    }

    public void setSocialNotifyEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFY_SOCIAL, enabled).apply();
    }

    public boolean isWeeklyReportEnabled() {
        return prefs.getBoolean(KEY_NOTIFY_WEEKLY, false);
    }

    public void setWeeklyReportEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFY_WEEKLY, enabled).apply();
    }
}
