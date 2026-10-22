package com.aicoach.api.progress;

import com.aicoach.api.common.VnTime;
import com.aicoach.api.user.UserAccount;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Luật tiến độ — nguồn sự thật duy nhất nằm ở server, app chỉ hiển thị:
 * <ul>
 *   <li>Streak: số ngày tập liên tiếp (theo giờ VN). Tập lại trong cùng ngày không tăng thêm; bỏ 1 ngày thì về 1.</li>
 *   <li>XP mỗi buổi = 2 × số rep đúng + 1 × số rep sai.</li>
 *   <li>weeklyXp reset khi sang tuần ISO mới (thứ Hai).</li>
 * </ul>
 */
@Service
public class ProgressService {

    private final Clock clock;

    public ProgressService(Clock clock) {
        this.clock = clock;
    }

    public static long xpFor(int totalReps, int correctReps) {
        return 2L * correctReps + Math.max(0, totalReps - correctReps);
    }

    /** Cập nhật tiến độ của người dùng sau khi lưu một buổi tập. */
    public void applySession(UserAccount user, Instant startedAt, int totalReps, int correctReps) {
        LocalDate day = VnTime.dateOf(startedAt);
        LocalDate last = user.getLastWorkoutDate();

        if (last == null) {
            user.setStreak(1);
            user.setLastWorkoutDate(day);
        } else if (day.isAfter(last)) {
            user.setStreak(day.equals(last.plusDays(1)) ? user.getStreak() + 1 : 1);
            user.setLastWorkoutDate(day);
        }
        // Buổi tập có ngày cũ hơn lastWorkoutDate (đồng bộ muộn) không làm đổi streak.

        long xp = xpFor(totalReps, correctReps);
        String sessionWeek = VnTime.weekKey(day);
        String currentWeek = VnTime.weekKey(VnTime.today(clock));
        if (sessionWeek.equals(currentWeek)) {
            if (!currentWeek.equals(user.getWeekKey())) {
                user.setWeekKey(currentWeek);
                user.setWeeklyXp(0);
            }
            user.setWeeklyXp(user.getWeeklyXp() + xp);
        }
        user.setTotalXp(user.getTotalXp() + xp);
        user.setTotalReps(user.getTotalReps() + totalReps);
    }

    /** Streak hiển thị: nếu lần tập cuối trước hôm qua thì chuỗi đã đứt, trả 0. */
    public int effectiveStreak(UserAccount user) {
        LocalDate last = user.getLastWorkoutDate();
        if (last == null) {
            return 0;
        }
        LocalDate today = VnTime.today(clock);
        return last.isBefore(today.minusDays(1)) ? 0 : user.getStreak();
    }

    /** XP tuần hiển thị: dữ liệu của tuần cũ thì coi như 0. */
    public long effectiveWeeklyXp(UserAccount user) {
        String currentWeek = VnTime.weekKey(VnTime.today(clock));
        return currentWeek.equals(user.getWeekKey()) ? user.getWeeklyXp() : 0;
    }
}
