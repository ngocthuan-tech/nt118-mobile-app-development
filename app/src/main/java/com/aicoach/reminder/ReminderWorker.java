package com.aicoach.reminder;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.aicoach.MainActivity;
import com.aicoach.R;
import com.aicoach.data.local.SettingsPrefs;

import java.time.LocalDateTime;

/** Chạy mỗi ngày quanh giờ nhắc: đúng ngày tập thì hiện thông báo "Đến giờ tập rồi", chạm vào mở app. */
public class ReminderWorker extends Worker {

    static final String CHANNEL_ID = "workout_reminder";
    private static final int NOTIFICATION_ID = 1001;

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SettingsPrefs prefs = new SettingsPrefs(context);
        if (prefs.isReminderEnabled() && ReminderSchedule.shouldNotify(LocalDateTime.now(),
                ReminderSchedule.parseTime(prefs.getReminderTime()),
                ReminderSchedule.parseDays(prefs.getReminderDays()))) {
            show(context);
        }
        return Result.success();
    }

    static void show(@NonNull Context context) {
        // Android 13+: người dùng chưa cho phép thông báo thì không hiện (không làm app lỗi)
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        if (!manager.areNotificationsEnabled()) return;
        createChannel(context);

        Intent open = new Intent(context, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent tap = PendingIntent.getActivity(context, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_dumbbell_24)
                .setColor(ContextCompat.getColor(context, R.color.brand_primary))
                .setContentTitle(context.getString(R.string.reminder_title))
                .setContentText(context.getString(R.string.reminder_body))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.reminder_body)))
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(tap)
                .setAutoCancel(true);
        try {
            manager.notify(NOTIFICATION_ID, builder.build());
        } catch (SecurityException ignored) {
            // Quyền bị thu hồi đúng lúc hiện thông báo — bỏ qua lần nhắc này
        }
    }

    private static void createChannel(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm == null || nm.getNotificationChannel(CHANNEL_ID) != null) return;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                context.getString(R.string.reminder_channel), NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(context.getString(R.string.reminder_channel_desc));
        nm.createNotificationChannel(channel);
    }
}
