package com.aicoach.pose;

import static com.aicoach.pose.Landmarks.*;

/** Dựng khung xương giả (ảnh 480×640) để kiểm thử lõi AI mà không cần camera. */
final class FakePose {

    static final int W = 480;
    static final int H = 640;
    static final long FRAME_MS = 66; // ~15 khung/giây

    private FakePose() {
    }

    /**
     * Người đứng CHÍNH DIỆN đang squat.
     * @param drop      độ hạ hông: 1 = đứng thẳng, 0 = đùi song song sàn
     * @param kneeWidth khoảng cách 2 gối (px); 2 cổ chân luôn cách nhau 60 px
     */
    static PoseFrame squatFront(double drop, float kneeWidth, long t) {
        float ankleY = 600, kneeY = 470, shin = ankleY - kneeY;
        float hipY = (float) (kneeY - drop * shin);
        float shoulderY = hipY - 130;
        float cx = 240;
        return new PoseFrame.Builder(W, H)
                .point(NOSE, cx, shoulderY - 50, 0.99f)
                .point(LEFT_SHOULDER, cx - 40, shoulderY, 0.99f)
                .point(RIGHT_SHOULDER, cx + 40, shoulderY, 0.99f)
                .point(LEFT_ELBOW, cx - 50, shoulderY + 60, 0.95f)
                .point(RIGHT_ELBOW, cx + 50, shoulderY + 60, 0.95f)
                .point(LEFT_WRIST, cx - 50, shoulderY + 110, 0.9f)
                .point(RIGHT_WRIST, cx + 50, shoulderY + 110, 0.9f)
                .point(LEFT_HIP, cx - 30, hipY, 0.99f)
                .point(RIGHT_HIP, cx + 30, hipY, 0.99f)
                .point(LEFT_KNEE, cx - kneeWidth / 2, kneeY, 0.99f)
                .point(RIGHT_KNEE, cx + kneeWidth / 2, kneeY, 0.99f)
                .point(LEFT_ANKLE, cx - 30, ankleY, 0.99f)
                .point(RIGHT_ANKLE, cx + 30, ankleY, 0.99f)
                .time(t)
                .build();
    }

    /** Người nằm ngang (nhìn nghiêng, quay mặt sang phải) — dùng cho Plank / Hít đất. */
    static PoseFrame horizontal(float hipOffsetY, double elbowAngleDeg, long t) {
        float y = 400;
        float shoulderX = 360, hipX = 240, ankleX = 100;
        // Cánh tay: vai → khuỷu thẳng xuống 60px, cẳng tay tạo góc elbowAngle với cánh tay trên
        float elbowX = shoulderX, elbowY = y + 60;
        double rad = Math.toRadians(180 - elbowAngleDeg);
        float wristX = (float) (elbowX + 60 * Math.sin(rad));
        float wristY = (float) (elbowY + 60 * Math.cos(rad));
        return new PoseFrame.Builder(W, H)
                .point(RIGHT_SHOULDER, shoulderX, y, 0.99f)
                .point(RIGHT_ELBOW, elbowX, elbowY, 0.99f)
                .point(RIGHT_WRIST, wristX, wristY, 0.99f)
                .point(RIGHT_HIP, hipX, y + hipOffsetY, 0.99f)
                .point(RIGHT_KNEE, 170, y + hipOffsetY / 2, 0.99f)
                .point(RIGHT_ANKLE, ankleX, y, 0.99f)
                .point(LEFT_SHOULDER, shoulderX, y, 0.3f)
                .point(LEFT_HIP, hipX, y, 0.3f)
                .time(t)
                .build();
    }

    /** Đứng chính diện, nâng gối: lift = 0 đứng, 1 = gối ngang hông. */
    static PoseFrame highKnees(double leftLift, double rightLift, long t) {
        float hipY = 330, shoulderY = 200, torso = hipY - shoulderY;
        float standKnee = hipY + 0.9f * torso;
        float lk = (float) (standKnee - leftLift * 0.9f * torso);
        float rk = (float) (standKnee - rightLift * 0.9f * torso);
        return new PoseFrame.Builder(W, H)
                .point(LEFT_SHOULDER, 200, shoulderY, 0.99f)
                .point(RIGHT_SHOULDER, 280, shoulderY, 0.99f)
                .point(LEFT_HIP, 210, hipY, 0.99f)
                .point(RIGHT_HIP, 270, hipY, 0.99f)
                .point(LEFT_KNEE, 210, lk, 0.99f)
                .point(RIGHT_KNEE, 270, rk, 0.99f)
                .point(LEFT_ANKLE, 210, lk + 120, 0.99f)
                .point(RIGHT_ANKLE, 270, rk + 120, 0.99f)
                .time(t)
                .build();
    }

    static PoseFrame nobody(long t) {
        return PoseFrame.empty(W, H, 120, t, 20);
    }
}
