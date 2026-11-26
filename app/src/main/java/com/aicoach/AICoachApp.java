package com.aicoach;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.aicoach.reminder.ReminderScheduler;

/**
 * Application của app. Hiện chỉ lo một việc: giữ lịch nhắc tập luôn khớp với cài đặt.
 *  - Mở app → canh lại lịch (đổi múi giờ, đổi giờ máy, cập nhật app… đều được sửa).
 *  - Bất kỳ màn nào đổi giờ / ngày / bật-tắt nhắc (Khảo sát 15, Bật thông báo 20, Hồ sơ) → tự đặt lại lịch,
 *    các màn đó không cần gọi gì thêm.
 */
public class AICoachApp extends Application {

    /** Tên file và khoá giống SettingsPrefs. */
    private static final String SETTINGS_FILE = "aicoach_settings";

    // Giữ tham chiếu mạnh: SharedPreferences chỉ giữ listener dạng weak, mất là không nghe nữa.
    private final SharedPreferences.OnSharedPreferenceChangeListener reminderListener = (prefs, key) -> {
        if (key != null && key.startsWith("reminder_")) ReminderScheduler.sync(this);
    };

    @Override
    public void onCreate() {
        super.onCreate();
        getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(reminderListener);
        ReminderScheduler.sync(this);
    }
}
