package com.aicoach.reminder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;
import org.junit.Test;

/** Lịch nhắc tập hằng ngày: đọc cài đặt "19:00" + "T2,T4,T6", tính lần nhắc kế tiếp, chặn nhắc sai giờ. */
public class ReminderScheduleTest {

    private static final LocalTime SEVEN_PM = LocalTime.of(19, 0);
    // 2026-11-23 là thứ Hai
    private static final LocalDateTime MON_10AM = LocalDateTime.of(2026, 11, 23, 10, 0);

    @Test
    public void parsesSettingsWrittenBySurveyAndProfile() {
        assertEquals(LocalTime.of(7, 30), ReminderSchedule.parseTime("07:30"));
        assertEquals(SEVEN_PM, ReminderSchedule.parseTime("25:00"));
        assertEquals(SEVEN_PM, ReminderSchedule.parseTime("abc"));
        assertEquals(SEVEN_PM, ReminderSchedule.parseTime(null));

        assertEquals(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                ReminderSchedule.parseDays("T2,T4,T6"));
        assertEquals(EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY), ReminderSchedule.parseDays(" t7 , CN "));
        assertTrue(ReminderSchedule.parseDays("T1,T8,xx").isEmpty());
        assertTrue(ReminderSchedule.parseDays(null).isEmpty());
    }

    @Test
    public void nextReminderIsOnAChosenDayAtTheChosenTime() {
        Set<DayOfWeek> monWedFri = ReminderSchedule.parseDays("T2,T4,T6");
        // Thứ Hai 10h → 19h cùng ngày
        assertEquals(LocalDateTime.of(2026, 11, 23, 19, 0), ReminderSchedule.next(MON_10AM, SEVEN_PM, monWedFri));
        // Thứ Hai 19h đúng → không nhắc lại ngay, sang thứ Tư
        assertEquals(LocalDateTime.of(2026, 11, 25, 19, 0),
                ReminderSchedule.next(LocalDateTime.of(2026, 11, 23, 19, 0), SEVEN_PM, monWedFri));
        // Thứ Sáu 20h → thứ Hai tuần sau
        assertEquals(LocalDateTime.of(2026, 11, 30, 19, 0),
                ReminderSchedule.next(LocalDateTime.of(2026, 11, 27, 20, 0), SEVEN_PM, monWedFri));
        // Chỉ Chủ nhật, đang là Chủ nhật 21h → Chủ nhật tuần sau
        assertEquals(LocalDateTime.of(2026, 12, 6, 19, 0), ReminderSchedule.next(
                LocalDateTime.of(2026, 11, 29, 21, 0), SEVEN_PM, EnumSet.of(DayOfWeek.SUNDAY)));
        assertNull(ReminderSchedule.next(MON_10AM, SEVEN_PM, EnumSet.noneOf(DayOfWeek.class)));
    }

    @Test
    public void notifiesOnlyOnTrainingDaysAndNotTooLate() {
        Set<DayOfWeek> monWedFri = ReminderSchedule.parseDays("T2,T4,T6");
        assertTrue(ReminderSchedule.shouldNotify(LocalDateTime.of(2026, 11, 23, 19, 5), SEVEN_PM, monWedFri));
        assertTrue(ReminderSchedule.shouldNotify(LocalDateTime.of(2026, 11, 23, 18, 59, 30), SEVEN_PM, monWedFri));
        assertFalse(ReminderSchedule.shouldNotify(LocalDateTime.of(2026, 11, 23, 18, 0), SEVEN_PM, monWedFri));
        // Máy tắt cả tối, sáng hôm sau WorkManager mới chạy → không nhắc
        assertFalse(ReminderSchedule.shouldNotify(LocalDateTime.of(2026, 11, 23, 23, 30), SEVEN_PM, monWedFri));
        // Thứ Ba không phải ngày tập
        assertFalse(ReminderSchedule.shouldNotify(LocalDateTime.of(2026, 11, 24, 19, 0), SEVEN_PM, monWedFri));
    }
}
