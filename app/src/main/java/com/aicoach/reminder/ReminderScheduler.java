package com.aicoach.reminder;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.aicoach.data.local.SettingsPrefs;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Đặt / huỷ lịch nhắc tập hằng ngày bằng WorkManager (vẫn chạy sau khi tắt app hoặc khởi động lại máy,
 * không cần quyền báo thức chính xác).
 *
 * Lịch: 1 việc định kỳ mỗi 24 giờ, lần đầu canh đúng giờ nhắc. Mỗi lần chạy, {@link ReminderWorker} tự kiểm tra
 * hôm nay có phải ngày tập không. Gọi {@link #sync} mỗi khi mở app hoặc đổi cài đặt để canh lại đúng giờ.
 */
public final class ReminderScheduler {

    static final String WORK_NAME = "daily_workout_reminder";

    private ReminderScheduler() {
    }

    public static void sync(@NonNull Context context) {
        Context app = context.getApplicationContext();
        SettingsPrefs prefs = new SettingsPrefs(app);
        WorkManager work = WorkManager.getInstance(app);
        if (!prefs.isReminderEnabled()) {
            work.cancelUniqueWork(WORK_NAME);
            return;
        }
        LocalTime time = ReminderSchedule.parseTime(prefs.getReminderTime());
        Set<java.time.DayOfWeek> days = ReminderSchedule.parseDays(prefs.getReminderDays());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = ReminderSchedule.next(now, time, days);
        if (next == null) {
            work.cancelUniqueWork(WORK_NAME);
            return;
        }
        // Lần đầu: đúng giờ nhắc của ngày gần nhất (hôm nay nếu chưa tới giờ, không thì ngày mai)
        LocalDateTime firstRun = now.toLocalDate().atTime(time);
        if (!firstRun.isAfter(now)) firstRun = firstRun.plusDays(1);
        long delayMs = Duration.between(now, firstRun).toMillis();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(ReminderWorker.class, 1, TimeUnit.DAYS)
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .build();
        // Huỷ lịch cũ rồi đặt lại → giờ mới có hiệu lực ngay, và mỗi lần mở app lịch được canh lại cho khỏi trôi.
        work.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, request);
    }
}
