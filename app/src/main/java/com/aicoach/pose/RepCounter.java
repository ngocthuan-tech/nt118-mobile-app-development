package com.aicoach.pose;

/**
 * Bộ đếm rep bằng máy trạng thái có "vùng trễ" (hysteresis) để không đếm nhầm khi số đo rung.
 *
 * Mỗi bài có 1 số đo chính (ví dụ góc gối). Ba ngưỡng:
 *   rest  — tư thế nghỉ (đứng thẳng: góc gối ≥ 160°)
 *   start — bắt đầu động tác (góc gối ≤ 145°)
 *   peak  — đạt điểm sâu nhất hợp lệ (góc gối ≤ 100°)
 *
 *   WAITING ──(về rest)──▶ REST ──(qua start)──▶ MOVING ──(qua peak)──▶ PEAK
 *                           ▲                      │                     │
 *                           └──── về rest: PARTIAL ┘   về rest: FULL ────┘
 *
 * Bắt đầu ở WAITING: người đang ngồi xổm sẵn lúc vào màn thì KHÔNG bị tính rep cho tới khi đứng thẳng.
 * Số đo được làm mượt bằng trung bình trượt hàm mũ (EMA) để lọc nhiễu khung hình.
 */
public final class RepCounter {

    public enum Result { NONE, STARTED, PEAK, FULL_REP, PARTIAL_REP }

    private enum State { WAITING, REST, MOVING, PEAK }

    /** Ngưỡng của một bài. peakIsLow = true khi động tác làm số đo GIẢM (squat), false khi TĂNG (cầu mông). */
    public static final class Config {
        final boolean peakIsLow;
        final double rest;
        final double start;
        final double peak;

        public Config(boolean peakIsLow, double rest, double start, double peak) {
            this.peakIsLow = peakIsLow;
            this.rest = rest;
            this.start = start;
            this.peak = peak;
        }
    }

    /** Rep ngắn hơn mức này coi là rung/nhiễu, bỏ qua. */
    static final long MIN_REP_MS = 350;
    /** Hệ số làm mượt EMA (0–1): càng lớn càng bám sát số đo mới. */
    static final double SMOOTHING = 0.6;

    private final Config cfg;
    private State state = State.WAITING;
    private double smoothed = Double.NaN;
    private long moveStartMs;
    private double extreme = Double.NaN;

    public RepCounter(Config cfg) {
        this.cfg = cfg;
    }

    /** Đưa số đo của khung hình mới vào. value = NaN nghĩa là khung này không đo được → giữ nguyên trạng thái. */
    public Result update(double value, long timeMs) {
        if (Double.isNaN(value)) return Result.NONE;
        smoothed = Double.isNaN(smoothed) ? value : SMOOTHING * value + (1 - SMOOTHING) * smoothed;
        double v = smoothed;

        switch (state) {
            case WAITING:
                if (isRest(v)) state = State.REST;
                return Result.NONE;

            case REST:
                if (isStarted(v)) {
                    state = State.MOVING;
                    moveStartMs = timeMs;
                    extreme = v;
                    return Result.STARTED;
                }
                return Result.NONE;

            case MOVING:
                trackExtreme(v);
                if (isPeak(v)) {
                    state = State.PEAK;
                    return Result.PEAK;
                }
                if (isRest(v)) {
                    state = State.REST;
                    return timeMs - moveStartMs >= MIN_REP_MS ? Result.PARTIAL_REP : Result.NONE;
                }
                return Result.NONE;

            case PEAK:
                trackExtreme(v);
                if (isRest(v)) {
                    state = State.REST;
                    return timeMs - moveStartMs >= MIN_REP_MS ? Result.FULL_REP : Result.NONE;
                }
                return Result.NONE;

            default:
                return Result.NONE;
        }
    }

    /** Đang ở giữa một lần lặp (đã rời tư thế nghỉ). */
    public boolean inMotion() {
        return state == State.MOVING || state == State.PEAK;
    }

    /** Số đo sâu nhất của lần lặp đang/vừa diễn ra (ví dụ góc gối nhỏ nhất). */
    public double extreme() {
        return extreme;
    }

    public double smoothed() {
        return smoothed;
    }

    /** Về trạng thái chờ (dùng khi bắt đầu hiệp mới hoặc mất dấu người). */
    public void reset() {
        state = State.WAITING;
        smoothed = Double.NaN;
        extreme = Double.NaN;
    }

    private void trackExtreme(double v) {
        if (Double.isNaN(extreme)) {
            extreme = v;
        } else {
            extreme = cfg.peakIsLow ? Math.min(extreme, v) : Math.max(extreme, v);
        }
    }

    private boolean isRest(double v) {
        return cfg.peakIsLow ? v >= cfg.rest : v <= cfg.rest;
    }

    private boolean isStarted(double v) {
        return cfg.peakIsLow ? v <= cfg.start : v >= cfg.start;
    }

    private boolean isPeak(double v) {
        return cfg.peakIsLow ? v <= cfg.peak : v >= cfg.peak;
    }
}
