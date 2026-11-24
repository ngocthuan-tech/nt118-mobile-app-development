package com.aicoach.pose;

/**
 * Một khung hình đã phân tích: toạ độ 33 khớp trong ảnh ĐÃ xoay đứng (trục y hướng xuống),
 * độ tin cậy từng khớp, độ sáng trung bình và thời điểm chụp.
 * Lớp bất biến, không phụ thuộc Android/ML Kit → dùng chung cho app và unit test.
 */
public final class PoseFrame {

    /** Khớp có độ tin cậy từ ngưỡng này trở lên mới được coi là "nhìn thấy". */
    public static final float MIN_SCORE = 0.5f;

    private final float[] x;
    private final float[] y;
    private final float[] score;
    private final int width;
    private final int height;
    private final float brightness;
    private final long timeMs;
    private final long latencyMs;

    private PoseFrame(Builder b) {
        this.x = b.x;
        this.y = b.y;
        this.score = b.score;
        this.width = b.width;
        this.height = b.height;
        this.brightness = b.brightness;
        this.timeMs = b.timeMs;
        this.latencyMs = b.latencyMs;
    }

    public float x(int lm) {
        return x[lm];
    }

    public float y(int lm) {
        return y[lm];
    }

    public float score(int lm) {
        return score[lm];
    }

    public boolean visible(int lm) {
        return score[lm] >= MIN_SCORE;
    }

    public boolean allVisible(int... lms) {
        for (int lm : lms) {
            if (!visible(lm)) return false;
        }
        return true;
    }

    /** Tổng độ tin cậy của một nhóm khớp — dùng để chọn bên trái/phải rõ hơn. */
    public float scoreSum(int... lms) {
        float s = 0f;
        for (int lm : lms) s += score[lm];
        return s;
    }

    /** Có phát hiện người không (ít nhất vai và hông). */
    public boolean hasPerson() {
        return (visible(Landmarks.LEFT_SHOULDER) || visible(Landmarks.RIGHT_SHOULDER))
                && (visible(Landmarks.LEFT_HIP) || visible(Landmarks.RIGHT_HIP));
    }

    /** Chiều rộng ảnh (đã xoay đứng), đơn vị pixel. */
    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    /** Độ sáng trung bình 0–255 (kênh Y của camera). */
    public float brightness() {
        return brightness;
    }

    public long timeMs() {
        return timeMs;
    }

    /** Thời gian ML Kit xử lý khung này (ms) — để đo hiệu năng đưa vào báo cáo. */
    public long latencyMs() {
        return latencyMs;
    }

    /** Khung rỗng: camera có hình nhưng không thấy người. */
    public static PoseFrame empty(int width, int height, float brightness, long timeMs, long latencyMs) {
        return new Builder(width, height).brightness(brightness).time(timeMs).latency(latencyMs).build();
    }

    public static final class Builder {
        private final float[] x = new float[Landmarks.COUNT];
        private final float[] y = new float[Landmarks.COUNT];
        private final float[] score = new float[Landmarks.COUNT];
        private final int width;
        private final int height;
        private float brightness = 128f;
        private long timeMs;
        private long latencyMs;

        public Builder(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public Builder point(int lm, float px, float py, float likelihood) {
            if (lm < 0 || lm >= Landmarks.COUNT) return this;
            x[lm] = px;
            y[lm] = py;
            score[lm] = likelihood;
            return this;
        }

        public Builder brightness(float value) {
            brightness = value;
            return this;
        }

        public Builder time(long ms) {
            timeMs = ms;
            return this;
        }

        public Builder latency(long ms) {
            latencyMs = ms;
            return this;
        }

        public PoseFrame build() {
            return new PoseFrame(this);
        }
    }
}
