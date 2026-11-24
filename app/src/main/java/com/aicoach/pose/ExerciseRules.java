package com.aicoach.pose;

import static com.aicoach.pose.Landmarks.*;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * Luật chấm cho 9 bài trong thư viện (id trùng với bảng exercises trên server).
 * Số hiệp / mục tiêu khớp dữ liệu seed của backend.
 *
 * Nguyên tắc chọn số đo — ưu tiên đại lượng KHÔNG đổi khi người xoay so với camera:
 *  - Squat, Lunge: độ hạ hông = (y gối − y hông) / (y cổ chân − y gối). Đứng thẳng ≈ 1,
 *    đùi song song sàn ≈ 0. Trục dọc không bị co ngắn khi xoay người nên đo được cả chính diện lẫn nghiêng,
 *    khác với góc gối 2D bị sai lệch khi đứng chính diện.
 *  - Hít đất, Plank, Cầu mông, Chống ghế: bắt buộc quay NGHIÊNG → đo góc khớp ở bên nhìn rõ hơn.
 *  - Bật nhảy, Nâng gối, Chạm chân: đứng CHÍNH DIỆN → đo theo tỉ lệ chiều dài cơ thể.
 */
public final class ExerciseRules {

    private ExerciseRules() {
    }

    /**
     * Luật với khối lượng lấy từ giáo án (ví dụ Plank 3 × 40 giây thay vì mặc định 3 × 45).
     * sets/target ≤ 0 → dùng mặc định của bài.
     */
    public static ExerciseRule create(String exerciseId, int sets, int target) {
        ExerciseRule def = create(exerciseId);
        if (sets <= 0 || target <= 0) return def;
        switch (def.id) {
            case "pushup":
                return new PushUp(sets, target);
            case "plank":
                return new Plank(sets, target);
            case "lunge":
                return new Lunge(sets, target);
            case "glute_bridge":
                return new GluteBridge(sets, target);
            case "chair_dip":
                return new ChairDip(sets, target);
            case "jumping_jack":
                return new JumpingJack(sets, target);
            case "high_knees":
                return new HighKnees(sets, target);
            case "toe_touch":
                return new ToeTouch(sets, target);
            case "squat":
            default:
                return new Squat(sets, target);
        }
    }

    /** Tạo luật mới (mỗi buổi tập một đối tượng riêng vì luật có trạng thái). Id lạ → Squat. */
    public static ExerciseRule create(String exerciseId) {
        if (exerciseId == null) return new Squat();
        switch (exerciseId) {
            case "pushup":
                return new PushUp();
            case "plank":
                return new Plank();
            case "lunge":
                return new Lunge();
            case "glute_bridge":
                return new GluteBridge();
            case "chair_dip":
                return new ChairDip();
            case "jumping_jack":
                return new JumpingJack();
            case "high_knees":
                return new HighKnees();
            case "toe_touch":
                return new ToeTouch();
            case "squat":
            default:
                return new Squat();
        }
    }

    // ------------------------------------------------------------------ tiện ích chung

    private static String fmt(String pattern, double v) {
        return String.format(Locale.US, pattern, Math.round(v));
    }

    /** Tỉ lệ độ hạ hông của 1 chân; NaN nếu thiếu khớp hoặc cẳng chân quá ngắn trên ảnh. */
    private static double hipDrop(PoseFrame f, int hip, int knee, int ankle) {
        if (!f.allVisible(hip, knee, ankle)) return Double.NaN;
        double shin = f.y(ankle) - f.y(knee);
        if (shin < f.height() * 0.03) return Double.NaN;
        return (f.y(knee) - f.y(hip)) / shin;
    }

    private static float midX(PoseFrame f, int a, int b) {
        return (f.x(a) + f.x(b)) / 2f;
    }

    private static float midY(PoseFrame f, int a, int b) {
        return (f.y(a) + f.y(b)) / 2f;
    }

    /** Chiều dài thân (giữa vai → giữa hông). */
    private static double torsoLength(PoseFrame f) {
        return Math.hypot(midX(f, LEFT_SHOULDER, RIGHT_SHOULDER) - midX(f, LEFT_HIP, RIGHT_HIP),
                midY(f, LEFT_SHOULDER, RIGHT_SHOULDER) - midY(f, LEFT_HIP, RIGHT_HIP));
    }

    /** Độ ngả của thân (giữa vai so với giữa hông) khỏi phương thẳng đứng, độ. */
    private static double torsoTilt(PoseFrame f) {
        double dx = Math.abs(midX(f, LEFT_SHOULDER, RIGHT_SHOULDER) - midX(f, LEFT_HIP, RIGHT_HIP));
        double dy = Math.abs(midY(f, LEFT_SHOULDER, RIGHT_SHOULDER) - midY(f, LEFT_HIP, RIGHT_HIP));
        return Math.toDegrees(Math.atan2(dx, dy));
    }

    /** Đứng chính diện: vai rộng so với thân. Nghiêng hẳn thì vai gần như trùng nhau. */
    private static boolean facingCamera(PoseFrame f) {
        return PoseMath.facingRatio(f) > PoseMath.FRONT_RATIO;
    }

    private static final int[] LEFT_SIDE = {LEFT_SHOULDER, LEFT_ELBOW, LEFT_WRIST, LEFT_HIP, LEFT_KNEE, LEFT_ANKLE};
    private static final int[] RIGHT_SIDE = {RIGHT_SHOULDER, RIGHT_ELBOW, RIGHT_WRIST, RIGHT_HIP, RIGHT_KNEE, RIGHT_ANKLE};

    /** Bộ khớp một bên: vai, khuỷu, cổ tay, hông, gối, cổ chân — chọn bên camera thấy rõ hơn. */
    private static int[] clearerSide(PoseFrame f) {
        return PoseMath.leftIsClearer(f, LEFT_SIDE, RIGHT_SIDE) ? LEFT_SIDE : RIGHT_SIDE;
    }

    private static final int S = 0, E = 1, W = 2, H = 3, K = 4, A = 5;

    // ------------------------------------------------------------------ 1. Squat

    static final class Squat extends ExerciseRule {
        private double maxTilt;
        private double minKneeRatio = Double.MAX_VALUE; // khoảng cách 2 gối / 2 cổ chân, lúc xuống sâu

        Squat() {
            this(3, 15);
        }

        Squat(int sets, int target) {
            super("squat", "Squat", sets, target, false, "Đứng cách máy 2–3 m, nghiêng khoảng 45°, thấy toàn thân");
        }

        @Override
        public FrameAnalysis analyze(PoseFrame f) {
            double l = hipDrop(f, LEFT_HIP, LEFT_KNEE, LEFT_ANKLE);
            double r = hipDrop(f, RIGHT_HIP, RIGHT_KNEE, RIGHT_ANKLE);
            double drop = PoseMath.meanIgnoringNaN(l, r);
            if (Double.isNaN(drop) || !f.hasPerson()) return FrameAnalysis.notVisible();

            maxTilt = Math.max(maxTilt, torsoTilt(f));
            boolean front = facingCamera(f);
            if (front && drop < 0.5 && f.allVisible(LEFT_KNEE, RIGHT_KNEE, LEFT_ANKLE, RIGHT_ANKLE)) {
                double ankles = PoseMath.dist(f, LEFT_ANKLE, RIGHT_ANKLE);
                if (ankles > 1) {
                    minKneeRatio = Math.min(minKneeRatio, PoseMath.dist(f, LEFT_KNEE, RIGHT_KNEE) / ankles);
                }
            }

            String text;
            if (front) {
                double depth = Math.max(0, Math.min(100, (1 - drop) / 0.8 * 100));
                text = fmt("Độ sâu %d%%", depth);
            } else {
                int[] s = clearerSide(f);
                text = fmt("Góc gối %d°", PoseMath.angle(f, s[H], s[K], s[A]));
            }
            return FrameAnalysis.of(drop, text);
        }

        @Override
        public RepCounter.Config counter() {
            return new RepCounter.Config(true, 0.80, 0.65, 0.30);
        }

        @Override
        protected void onRepStarted() {
            maxTilt = 0;
            minKneeRatio = Double.MAX_VALUE;
        }

        @Override
        protected FormIssue repIssue() {
            if (minKneeRatio < 0.75) return FormIssue.KNEES_IN;
            if (maxTilt > 60) return FormIssue.TORSO_LEAN;
            return null;
        }

        @Override
        public FormIssue partialIssue() {
            return FormIssue.SQUAT_DEPTH;
        }
    }

    // ------------------------------------------------------------------ 2. Hít đất

    static final class PushUp extends ExerciseRule {
        private double maxSag;
        private double minSag;

        PushUp() {
            this(3, 12);
        }

        PushUp(int sets, int target) {
            super("pushup", "Hít đất", sets, target, false, "Đặt máy thấp ngang sàn, quay NGHIÊNG người về phía camera");
        }

        @Override
        public CameraView preferredView() {
            return CameraView.SIDE;
        }

        @Override
        public FrameAnalysis analyze(PoseFrame f) {
            int[] s = clearerSide(f);
            if (!f.allVisible(s[S], s[E], s[W], s[H])) return FrameAnalysis.notVisible();
            // Chỉ đếm khi thân gần nằm ngang — tránh đếm lúc đứng vung tay
            if (PoseMath.tiltFromVertical(f, s[S], s[H]) < 50) {
                return FrameAnalysis.of(Double.NaN, "Vào tư thế chống đẩy");
            }
            int foot = f.visible(s[A]) ? s[A] : s[K];
            if (f.visible(foot)) {
                double sag = PoseMath.sagRatio(f, s[S], s[H], foot);
                maxSag = Math.max(maxSag, sag);
                minSag = Math.min(minSag, sag);
            }
            double elbow = PoseMath.angle(f, s[S], s[E], s[W]);
            return FrameAnalysis.of(elbow, fmt("Góc khuỷu %d°", elbow));
        }

        @Override
        public RepCounter.Config counter() {
            return new RepCounter.Config(true, 150, 135, 100);
        }

        @Override
        protected void onRepStarted() {
            maxSag = 0;
            minSag = 0;
        }

        @Override
        protected FormIssue repIssue() {
            if (maxSag > 0.10) return FormIssue.HIP_SAG;
            if (minSag < -0.12) return FormIssue.HIP_PIKE;
            return null;
        }

        @Override
        public FormIssue partialIssue() {
            return FormIssue.PUSHUP_DEPTH;
        }
    }

    // ------------------------------------------------------------------ 3. Plank (tính giây)

    static final class Plank extends ExerciseRule {
        Plank() {
            this(3, 45);
        }

        Plank(int sets, int target) {
            super("plank", "Plank", sets, target, true, "Đặt máy thấp ngang sàn, quay NGHIÊNG người về phía camera");
        }

        @Override
        public CameraView preferredView() {
            return CameraView.SIDE;
        }

        @Override
        public FrameAnalysis analyze(PoseFrame f) {
            int[] s = clearerSide(f);
            if (!f.allVisible(s[S], s[H])) return FrameAnalysis.notVisible();
            int foot = f.visible(s[A]) ? s[A] : s[K];
            if (!f.visible(foot)) return FrameAnalysis.notVisible();

            boolean lying = PoseMath.tiltFromVertical(f, s[S], s[H]) > 55;
            double line = PoseMath.angle(f, s[S], s[H], foot);
            Set<FormIssue> issues = EnumSet.noneOf(FormIssue.class);
            if (lying) {
                double sag = PoseMath.sagRatio(f, s[S], s[H], foot);
                if (sag > 0.08) issues.add(FormIssue.HIP_SAG);
                else if (sag < -0.10) issues.add(FormIssue.HIP_PIKE);
            }
            return FrameAnalysis.hold(lying, fmt("Thân thẳng %d°", line), issues);
        }

        @Override
        public RepCounter.Config counter() {
            return null;
        }

        @Override
        public FormIssue partialIssue() {
            return FormIssue.HIP_SAG;
        }
    }

    // ------------------------------------------------------------------ 4. Lunge

    static final class Lunge extends ExerciseRule {
        private double maxTilt;

        Lunge() {
            this(3, 12);
        }

        Lunge(int sets, int target) {
            super("lunge", "Lunge", sets, target, false, "Đứng NGHIÊNG với camera, cách 2–3 m, thấy toàn thân");
        }

        @Override
        public FrameAnalysis analyze(PoseFrame f) {
            double l = hipDrop(f, LEFT_HIP, LEFT_KNEE, LEFT_ANKLE);
            double r = hipDrop(f, RIGHT_HIP, RIGHT_KNEE, RIGHT_ANKLE);
            // Chân trước là chân có hông hạ gần ngang gối nhất → lấy giá trị nhỏ hơn
            double drop = Double.isNaN(l) ? r : Double.isNaN(r) ? l : Math.min(l, r);
            if (Double.isNaN(drop) || !f.hasPerson()) return FrameAnalysis.notVisible();

            maxTilt = Math.max(maxTilt, torsoTilt(f));
            boolean leftFront = !Double.isNaN(l) && (Double.isNaN(r) || l <= r);
            double knee = leftFront
                    ? PoseMath.angle(f, LEFT_HIP, LEFT_KNEE, LEFT_ANKLE)
                    : PoseMath.angle(f, RIGHT_HIP, RIGHT_KNEE, RIGHT_ANKLE);
            return FrameAnalysis.of(drop, fmt("Góc gối %d°", knee));
        }

        @Override
        public RepCounter.Config counter() {
            return new RepCounter.Config(true, 0.80, 0.65, 0.35);
        }

        @Override
        protected void onRepStarted() {
            maxTilt = 0;
        }

        @Override
        protected FormIssue repIssue() {
            return maxTilt > 40 ? FormIssue.TORSO_LEAN : null;
        }

        @Override
        public FormIssue partialIssue() {
            return FormIssue.LUNGE_DEPTH;
        }
    }

    // ------------------------------------------------------------------ 5. Cầu mông

    static final class GluteBridge extends ExerciseRule {
        GluteBridge() {
            this(3, 15);
        }

        GluteBridge(int sets, int target) {
            super("glute_bridge", "Cầu mông", sets, target, false, "Nằm ngửa, quay NGHIÊNG người về phía camera, máy đặt thấp");
        }

        @Override
        public CameraView preferredView() {
            return CameraView.SIDE;
        }

        @Override
        public FrameAnalysis analyze(PoseFrame f) {
            int[] s = clearerSide(f);
            if (!f.allVisible(s[S], s[H], s[K])) return FrameAnalysis.notVisible();
            if (PoseMath.tiltFromVertical(f, s[S], s[H]) < 50) {
                return FrameAnalysis.of(Double.NaN, "Nằm ngửa để bắt đầu");
            }
            double hip = PoseMath.angle(f, s[S], s[H], s[K]);
            return FrameAnalysis.of(hip, fmt("Góc hông %d°", hip));
        }

        @Override
        public RepCounter.Config counter() {
            return new RepCounter.Config(false, 145, 152, 165);
        }

        @Override
        public FormIssue partialIssue() {
            return FormIssue.BRIDGE_HEIGHT;
        }
    }

    // ------------------------------------------------------------------ 6. Chống ghế

    static final class ChairDip extends ExerciseRule {
        ChairDip() {
            this(3, 10);
        }

        ChairDip(int sets, int target) {
            super("chair_dip", "Chống ghế", sets, target, false, "Đặt ghế NGHIÊNG với camera, thấy rõ vai – khuỷu – cổ tay");
        }

        @Override
        public CameraView preferredView() {
            return CameraView.SIDE;
        }

        @Override
        public FrameAnalysis analyze(PoseFrame f) {
            int[] s = clearerSide(f);
            if (!f.allVisible(s[S], s[E], s[W], s[H])) return FrameAnalysis.notVisible();
            if (PoseMath.tiltFromVertical(f, s[S], s[H]) > 45) {
                return FrameAnalysis.of(Double.NaN, "Giữ thân thẳng đứng");
            }
            double elbow = PoseMath.angle(f, s[S], s[E], s[W]);
            return FrameAnalysis.of(elbow, fmt("Góc khuỷu %d°", elbow));
        }

        @Override
        public RepCounter.Config counter() {
            return new RepCounter.Config(true, 150, 135, 100);
        }

        @Override
        public FormIssue partialIssue() {
            return FormIssue.DIP_DEPTH;
        }
    }

    // ------------------------------------------------------------------ 7. Bật nhảy

    static final class JumpingJack extends ExerciseRule {
        private double maxLegSpread;

        JumpingJack() {
            this(3, 30);
        }

        JumpingJack(int sets, int target) {
            super("jumping_jack", "Bật nhảy", sets, target, false, "Đứng CHÍNH DIỆN, cách 2–3 m, thấy cả tay giơ cao lẫn bàn chân");
        }

        @Override
        public CameraView preferredView() {
            return CameraView.FRONT;
        }

        @Override
        public FrameAnalysis analyze(PoseFrame f) {
            if (!f.allVisible(LEFT_SHOULDER, RIGHT_SHOULDER, LEFT_HIP, RIGHT_HIP, LEFT_ELBOW, RIGHT_ELBOW)) {
                return FrameAnalysis.notVisible();
            }
            double arms = PoseMath.meanIgnoringNaN(
                    PoseMath.angle(f, LEFT_HIP, LEFT_SHOULDER, LEFT_ELBOW),
                    PoseMath.angle(f, RIGHT_HIP, RIGHT_SHOULDER, RIGHT_ELBOW));
            double shoulders = PoseMath.dist(f, LEFT_SHOULDER, RIGHT_SHOULDER);
            if (shoulders > 1 && f.allVisible(LEFT_ANKLE, RIGHT_ANKLE)) {
                maxLegSpread = Math.max(maxLegSpread, PoseMath.dist(f, LEFT_ANKLE, RIGHT_ANKLE) / shoulders);
            }
            return FrameAnalysis.of(arms, fmt("Tay %d°", arms));
        }

        @Override
        public RepCounter.Config counter() {
            return new RepCounter.Config(false, 50, 80, 140);
        }

        @Override
        protected void onRepStarted() {
            maxLegSpread = 0;
        }

        @Override
        protected FormIssue repIssue() {
            return maxLegSpread < 1.2 ? FormIssue.LEGS_CLOSED : null;
        }

        @Override
        public FormIssue partialIssue() {
            return FormIssue.ARMS_LOW;
        }
    }

    // ------------------------------------------------------------------ 8. Nâng gối (2 bộ đếm)

    static final class HighKnees extends ExerciseRule {
        HighKnees() {
            this(3, 30);
        }

        HighKnees(int sets, int target) {
            super("high_knees", "Nâng gối", sets, target, false, "Đứng CHÍNH DIỆN, cách 2–3 m, thấy toàn thân");
        }

        @Override
        public CameraView preferredView() {
            return CameraView.FRONT;
        }

        @Override
        public FrameAnalysis analyze(PoseFrame f) {
            if (!f.allVisible(LEFT_SHOULDER, RIGHT_SHOULDER, LEFT_HIP, RIGHT_HIP)) return FrameAnalysis.notVisible();
            double torso = torsoLength(f);
            if (torso < 1) return FrameAnalysis.notVisible();
            double l = f.visible(LEFT_KNEE) ? (f.y(LEFT_KNEE) - f.y(LEFT_HIP)) / torso : Double.NaN;
            double r = f.visible(RIGHT_KNEE) ? (f.y(RIGHT_KNEE) - f.y(RIGHT_HIP)) / torso : Double.NaN;
            if (Double.isNaN(l) && Double.isNaN(r)) return FrameAnalysis.notVisible();
            double best = Double.isNaN(l) ? r : Double.isNaN(r) ? l : Math.min(l, r);
            // 0.9 (đứng) → 100%, 0 (gối ngang hông) → 100% độ cao
            double pct = Math.max(0, Math.min(100, (0.9 - best) / 0.9 * 100));
            return FrameAnalysis.dual(l, r, fmt("Gối cao %d%%", pct));
        }

        @Override
        public RepCounter.Config counter() {
            return new RepCounter.Config(true, 0.70, 0.55, 0.30);
        }

        @Override
        public boolean dualCounter() {
            return true;
        }

        @Override
        public FormIssue partialIssue() {
            return FormIssue.KNEE_LOW;
        }
    }

    // ------------------------------------------------------------------ 9. Chạm chân

    static final class ToeTouch extends ExerciseRule {
        ToeTouch() {
            this(3, 15);
        }

        ToeTouch(int sets, int target) {
            super("toe_touch", "Chạm chân", sets, target, false, "Đứng NGHIÊNG hoặc chính diện, thấy từ đầu tới bàn chân");
        }

        @Override
        public FrameAnalysis analyze(PoseFrame f) {
            int[] s = clearerSide(f);
            if (!f.allVisible(s[H], s[A]) || !(f.visible(LEFT_WRIST) || f.visible(RIGHT_WRIST))) {
                return FrameAnalysis.notVisible();
            }
            double legLen = f.y(s[A]) - f.y(s[H]);
            if (legLen < f.height() * 0.05) return FrameAnalysis.notVisible();
            // Lấy cổ tay xuống thấp nhất trong 2 tay
            float wristY = Math.max(f.visible(LEFT_WRIST) ? f.y(LEFT_WRIST) : -1e9f,
                    f.visible(RIGHT_WRIST) ? f.y(RIGHT_WRIST) : -1e9f);
            double reach = (wristY - f.y(s[H])) / legLen;
            double pct = Math.max(0, Math.min(100, reach * 100));
            return FrameAnalysis.of(reach, fmt("Với tay %d%%", pct));
        }

        @Override
        public RepCounter.Config counter() {
            return new RepCounter.Config(false, 0.45, 0.55, 0.85);
        }

        @Override
        public FormIssue partialIssue() {
            return FormIssue.SHORT_REACH;
        }
    }
}
