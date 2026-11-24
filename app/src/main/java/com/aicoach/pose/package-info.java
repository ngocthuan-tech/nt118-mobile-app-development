/**
 * Trưởng nhóm — lõi AI nhận diện tư thế.
 *
 * Phần thuần Java (unit test được, không cần điện thoại):
 *   PoseFrame, Landmarks, PoseMath     — dữ liệu khung xương + hình học (góc khớp, độ võng)
 *   ExerciseRule, ExerciseRules        — luật chấm cho 9 bài (Strategy pattern)
 *   RepCounter                         — máy trạng thái đếm rep có vùng trễ
 *   WorkoutEngine, WorkoutUpdate, WorkoutSummary — quản lý buổi tập, hiệp, lỗi, tổng kết
 *
 * Phần Android:
 *   PoseCamera      — CameraX + ML Kit Pose Detection, trả PoseFrame về main thread
 *   PoseOverlayView — vẽ khung xương đè lên hình camera
 */
package com.aicoach.pose;
