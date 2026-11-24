package com.aicoach.pose;

/** Các phép hình học trên khung xương 2D. Toạ độ ảnh: x sang phải, y hướng XUỐNG. */
public final class PoseMath {

    /** facingRatio lớn hơn mức này = đứng chính diện camera. */
    public static final double FRONT_RATIO = 0.55;
    /** facingRatio nhỏ hơn mức này = đứng nghiêng hẳn. */
    public static final double SIDE_RATIO = 0.45;

    private PoseMath() {
    }

    /** Góc tại khớp b (độ, 0–180) tạo bởi a–b–c. Ví dụ góc gối = angle(hông, gối, cổ chân). */
    public static double angle(PoseFrame f, int a, int b, int c) {
        double abx = f.x(a) - f.x(b);
        double aby = f.y(a) - f.y(b);
        double cbx = f.x(c) - f.x(b);
        double cby = f.y(c) - f.y(b);
        double lenAB = Math.hypot(abx, aby);
        double lenCB = Math.hypot(cbx, cby);
        if (lenAB < 1e-6 || lenCB < 1e-6) return Double.NaN;
        double cos = (abx * cbx + aby * cby) / (lenAB * lenCB);
        cos = Math.max(-1.0, Math.min(1.0, cos));
        return Math.toDegrees(Math.acos(cos));
    }

    public static double dist(PoseFrame f, int a, int b) {
        return Math.hypot(f.x(a) - f.x(b), f.y(a) - f.y(b));
    }

    /**
     * Độ nghiêng của đoạn từ {@code bottom} lên {@code top} so với phương thẳng đứng (độ, 0–90).
     * 0° = đứng thẳng, 90° = nằm ngang. Dùng cho độ ngả thân người.
     */
    public static double tiltFromVertical(PoseFrame f, int top, int bottom) {
        double dx = Math.abs(f.x(top) - f.x(bottom));
        double dy = Math.abs(f.y(top) - f.y(bottom));
        if (dx < 1e-6 && dy < 1e-6) return Double.NaN;
        return Math.toDegrees(Math.atan2(dx, dy));
    }

    /**
     * Khoảng cách có dấu từ điểm p tới đường thẳng a→b, chia cho độ dài a–b.
     * Với a = vai, b = cổ chân khi người nằm ngang: giá trị DƯƠNG nghĩa là p nằm THẤP hơn đường thẳng
     * (theo trục y ảnh) — tức là võng hông; ÂM là nhô mông lên.
     */
    public static double sagRatio(PoseFrame f, int a, int p, int b) {
        double ax = f.x(a), ay = f.y(a), bx = f.x(b), by = f.y(b);
        double len = Math.hypot(bx - ax, by - ay);
        if (len < 1e-6) return 0;
        // y của đường thẳng a→b tại hoành độ của p (nội suy tuyến tính)
        double t = Math.abs(bx - ax) < 1e-6 ? 0.5 : (f.x(p) - ax) / (bx - ax);
        double lineY = ay + t * (by - ay);
        return (f.y(p) - lineY) / len;
    }

    /** true nếu bên TRÁI của nhóm khớp rõ hơn bên phải (để chọn bên đo khi đứng nghiêng). */
    public static boolean leftIsClearer(PoseFrame f, int[] left, int[] right) {
        return f.scoreSum(left) >= f.scoreSum(right);
    }

    /**
     * Độ "quay mặt" về camera = bề rộng vai / chiều dài thân (giữa vai → giữa hông).
     * Chính diện ≈ 0.6–0.8, nghiêng 45° ≈ 0.4–0.5, nghiêng hẳn ≈ 0–0.3. NaN nếu thiếu vai/hông.
     */
    public static double facingRatio(PoseFrame f) {
        if (!f.allVisible(Landmarks.LEFT_SHOULDER, Landmarks.RIGHT_SHOULDER, Landmarks.LEFT_HIP, Landmarks.RIGHT_HIP)) {
            return Double.NaN;
        }
        double sx = (f.x(Landmarks.LEFT_SHOULDER) + f.x(Landmarks.RIGHT_SHOULDER)) / 2.0;
        double sy = (f.y(Landmarks.LEFT_SHOULDER) + f.y(Landmarks.RIGHT_SHOULDER)) / 2.0;
        double hx = (f.x(Landmarks.LEFT_HIP) + f.x(Landmarks.RIGHT_HIP)) / 2.0;
        double hy = (f.y(Landmarks.LEFT_HIP) + f.y(Landmarks.RIGHT_HIP)) / 2.0;
        double torso = Math.hypot(sx - hx, sy - hy);
        if (torso < 1) return Double.NaN;
        return dist(f, Landmarks.LEFT_SHOULDER, Landmarks.RIGHT_SHOULDER) / torso;
    }

    /** Trung bình 2 giá trị, bỏ qua giá trị NaN. */
    public static double meanIgnoringNaN(double a, double b) {
        if (Double.isNaN(a)) return b;
        if (Double.isNaN(b)) return a;
        return (a + b) / 2.0;
    }
}
