/**
 * Repository — nơi DUY NHẤT gọi API (qua các interface trong data/remote/api) hoặc trả dữ liệu mẫu.
 * Mọi Repository kế thừa BaseRepository. Mỗi người một file, không sửa file của người khác:
 * <ul>
 *   <li>Thành viên A: AuthRepository, OnboardingRepository, HomeRepository, ExerciseRepository, PlanRepository</li>
 *   <li>Thành viên B: UserRepository (hồ sơ), HistoryRepository, SocialRepository, BadgeRepository</li>
 *   <li>Trưởng nhóm: WorkoutRepository (lưu buổi tập POST /sessions)</li>
 * </ul>
 */
package com.aicoach.data.repository;
