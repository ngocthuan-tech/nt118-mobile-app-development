package com.aicoach.ui.workout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Danh sách bài của MỘT lượt tập (Trưởng nhóm). Tập lẻ = 1 bài; tập theo giáo án = các bài CÒN LẠI của buổi.
 * Truyền qua tham số điều hướng dạng chuỗi "jumping_jack:3:30:0;plank:3:40:1"
 * (mã bài : số hiệp : mục tiêu mỗi hiệp : 1 nếu tính giây) — bền khi app bị hệ điều hành đóng rồi mở lại.
 * Thuần Java → unit test được.
 */
public final class WorkoutQueue {

    /** Một bài trong lượt tập, kèm khối lượng (hiệp × lần/giây) lấy từ giáo án. */
    public static final class Step {
        public final String exerciseId;
        public final int sets;
        public final int target;
        public final boolean timed;

        public Step(String exerciseId, int sets, int target, boolean timed) {
            this.exerciseId = exerciseId;
            this.sets = sets;
            this.target = target;
            this.timed = timed;
        }
    }

    private final List<Step> steps;
    private int index;

    public WorkoutQueue(List<Step> steps) {
        this.steps = Collections.unmodifiableList(new ArrayList<>(steps));
    }

    public static String encode(List<Step> steps) {
        StringBuilder sb = new StringBuilder();
        for (Step s : steps) {
            if (sb.length() > 0) sb.append(';');
            sb.append(String.format(Locale.US, "%s:%d:%d:%d", s.exerciseId, s.sets, s.target, s.timed ? 1 : 0));
        }
        return sb.toString();
    }

    /** Chuỗi hỏng / rỗng → lượt tập rỗng (các màn sẽ dùng khối lượng mặc định của bài). */
    public static WorkoutQueue decode(String text) {
        List<Step> out = new ArrayList<>();
        if (text != null) {
            for (String part : text.split(";")) {
                String[] f = part.trim().split(":");
                if (f.length != 4 || f[0].isEmpty()) continue;
                try {
                    int sets = Integer.parseInt(f[1]);
                    int target = Integer.parseInt(f[2]);
                    if (sets <= 0 || target <= 0) continue;
                    out.add(new Step(f[0], sets, target, "1".equals(f[3])));
                } catch (NumberFormatException ignored) {
                    // bỏ qua phần hỏng
                }
            }
        }
        return new WorkoutQueue(out);
    }

    public boolean isEmpty() {
        return steps.isEmpty();
    }

    public int size() {
        return steps.size();
    }

    /** Vị trí bài hiện tại, đếm từ 1 (để hiện "Bài 2/4"). */
    public int position() {
        return Math.min(index + 1, steps.size());
    }

    public Step current() {
        return index < steps.size() ? steps.get(index) : null;
    }

    /** Sang bài kế; trả về bài đó, hoặc null nếu đã hết. */
    public Step advance() {
        if (index < steps.size()) index++;
        return current();
    }

    public boolean hasNext() {
        return index + 1 < steps.size();
    }

    /** Bài kế tiếp (không chuyển), null nếu đang ở bài cuối. */
    public Step peekNext() {
        return hasNext() ? steps.get(index + 1) : null;
    }

    /** Khối lượng của một bài trong lượt tập (null nếu bài không có trong danh sách). */
    public Step find(String exerciseId) {
        for (Step s : steps) {
            if (s.exerciseId.equals(exerciseId)) return s;
        }
        return null;
    }
}
