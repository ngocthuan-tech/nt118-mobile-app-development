package com.aicoach.pose;

/**
 * Chỉ số 33 điểm khớp — trùng với hằng số của ML Kit (PoseLandmark.LEFT_SHOULDER = 11, ...).
 * Lõi AI dùng lớp này thay cho PoseLandmark để không phụ thuộc Android → chạy được unit test thuần Java.
 */
public final class Landmarks {

    public static final int COUNT = 33;

    public static final int NOSE = 0;
    public static final int LEFT_EAR = 7;
    public static final int RIGHT_EAR = 8;
    public static final int LEFT_SHOULDER = 11;
    public static final int RIGHT_SHOULDER = 12;
    public static final int LEFT_ELBOW = 13;
    public static final int RIGHT_ELBOW = 14;
    public static final int LEFT_WRIST = 15;
    public static final int RIGHT_WRIST = 16;
    public static final int LEFT_HIP = 23;
    public static final int RIGHT_HIP = 24;
    public static final int LEFT_KNEE = 25;
    public static final int RIGHT_KNEE = 26;
    public static final int LEFT_ANKLE = 27;
    public static final int RIGHT_ANKLE = 28;
    public static final int LEFT_HEEL = 29;
    public static final int RIGHT_HEEL = 30;
    public static final int LEFT_FOOT_INDEX = 31;
    public static final int RIGHT_FOOT_INDEX = 32;

    /** Các đoạn xương để vẽ khung (cặp điểm đầu–cuối). */
    public static final int[][] BONES = {
            {LEFT_SHOULDER, RIGHT_SHOULDER},
            {LEFT_SHOULDER, LEFT_ELBOW}, {LEFT_ELBOW, LEFT_WRIST},
            {RIGHT_SHOULDER, RIGHT_ELBOW}, {RIGHT_ELBOW, RIGHT_WRIST},
            {LEFT_SHOULDER, LEFT_HIP}, {RIGHT_SHOULDER, RIGHT_HIP},
            {LEFT_HIP, RIGHT_HIP},
            {LEFT_HIP, LEFT_KNEE}, {LEFT_KNEE, LEFT_ANKLE},
            {RIGHT_HIP, RIGHT_KNEE}, {RIGHT_KNEE, RIGHT_ANKLE},
            {LEFT_ANKLE, LEFT_FOOT_INDEX}, {RIGHT_ANKLE, RIGHT_FOOT_INDEX},
    };

    /** Các điểm chấm khớp được vẽ (bỏ mắt, miệng, ngón tay cho đỡ rối). */
    public static final int[] DRAWN_JOINTS = {
            NOSE, LEFT_SHOULDER, RIGHT_SHOULDER, LEFT_ELBOW, RIGHT_ELBOW, LEFT_WRIST, RIGHT_WRIST,
            LEFT_HIP, RIGHT_HIP, LEFT_KNEE, RIGHT_KNEE, LEFT_ANKLE, RIGHT_ANKLE,
            LEFT_FOOT_INDEX, RIGHT_FOOT_INDEX,
    };

    private Landmarks() {
    }
}
