package com.aicoach.pose;

/**
 * Chấm 3 mục của màn "Kiểm tra sẵn sàng" (Figma 27, 28) từ một khung hình:
 *   1. Khung hình  — thấy đủ toàn thân để chấm bài này chưa
 *   2. Ánh sáng    — độ sáng trung bình camera
 *   3. Hướng đứng  — chính diện / nghiêng đúng như bài yêu cầu
 * Thuần Java → unit test được.
 */
public final class ReadinessEvaluator {

    public enum Level { OK, WARNING, ERROR, PENDING }

    /** Độ sáng (0–255) từ mức này trở lên là đủ sáng. */
    static final float LIGHT_OK = 70f;
    /** Dưới mức này là quá tối, AI nhận diện sai nhiều. */
    static final float LIGHT_MIN = 40f;

    public static final class Result {
        public final Level frame;
        public final Level light;
        public final Level orientation;
        public final boolean personSeen;

        Result(Level frame, Level light, Level orientation, boolean personSeen) {
            this.frame = frame;
            this.light = light;
            this.orientation = orientation;
            this.personSeen = personSeen;
        }

        /** Được bắt đầu khi không mục nào lỗi/đang chờ (cảnh báo vẫn cho tập). */
        public boolean canStart() {
            return passing(frame) && passing(light) && passing(orientation);
        }

        private static boolean passing(Level l) {
            return l == Level.OK || l == Level.WARNING;
        }
    }

    private ReadinessEvaluator() {
    }

    public static Result evaluate(PoseFrame f, ExerciseRule rule) {
        Level light = f.brightness() >= LIGHT_OK ? Level.OK
                : f.brightness() >= LIGHT_MIN ? Level.WARNING : Level.ERROR;

        boolean person = f.hasPerson();
        if (!person) return new Result(Level.ERROR, light, Level.PENDING, false);

        boolean feet = f.visible(Landmarks.LEFT_ANKLE) || f.visible(Landmarks.RIGHT_ANKLE);
        boolean head = f.visible(Landmarks.NOSE)
                || f.visible(Landmarks.LEFT_SHOULDER) || f.visible(Landmarks.RIGHT_SHOULDER);
        boolean ruleCanSee = rule.analyze(f).visible;
        Level frame = feet && head && ruleCanSee ? Level.OK : Level.ERROR;

        Level orientation;
        double facing = PoseMath.facingRatio(f);
        if (Double.isNaN(facing)) {
            // Đứng nghiêng hẳn thì 1 bên vai/hông bị che → vẫn tính là nghiêng
            orientation = rule.preferredView() == ExerciseRule.CameraView.FRONT ? Level.WARNING : Level.OK;
        } else {
            switch (rule.preferredView()) {
                case FRONT:
                    orientation = facing > PoseMath.FRONT_RATIO ? Level.OK : Level.WARNING;
                    break;
                case SIDE:
                    orientation = facing < PoseMath.SIDE_RATIO ? Level.OK : Level.WARNING;
                    break;
                case ANY:
                default:
                    orientation = Level.OK;
                    break;
            }
        }
        return new Result(frame, light, orientation, true);
    }
}
