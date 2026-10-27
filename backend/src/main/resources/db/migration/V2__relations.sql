-- =====================================================================
-- V2: khoá ngoại giữa các bảng chính.
-- Chạy được trên cả database mới (sau V1) lẫn database đang chạy trên server
-- (bảng do Hibernate tạo trước đây): dọn dòng mồ côi trước rồi mới thêm khoá ngoại.
-- =====================================================================

-- Dọn dữ liệu tham chiếu tới thứ không tồn tại (nếu có) để thêm khoá ngoại không lỗi
UPDATE users SET active_plan_id = NULL, plan_start_date = NULL
    WHERE active_plan_id IS NOT NULL AND active_plan_id NOT IN (SELECT id FROM plans);
UPDATE workout_sessions SET plan_id = NULL
    WHERE plan_id IS NOT NULL AND plan_id NOT IN (SELECT id FROM plans);
DELETE FROM session_form_errors
    WHERE session_id IN (SELECT id FROM workout_sessions
                         WHERE user_id NOT IN (SELECT id FROM users)
                            OR exercise_id NOT IN (SELECT id FROM exercises));
DELETE FROM workout_sessions
    WHERE user_id NOT IN (SELECT id FROM users)
       OR exercise_id NOT IN (SELECT id FROM exercises);
DELETE FROM plan_items WHERE exercise_id NOT IN (SELECT id FROM exercises);

-- users N–1 plans: người dùng đang theo một giáo án (có thể chưa chọn)
ALTER TABLE users
    ADD CONSTRAINT fk_users_active_plan FOREIGN KEY (active_plan_id) REFERENCES plans (id);

-- plan_items N–1 exercises
ALTER TABLE plan_items
    ADD CONSTRAINT fk_plan_items_exercise FOREIGN KEY (exercise_id) REFERENCES exercises (id);

-- workout_sessions N–1 users / exercises / plans
ALTER TABLE workout_sessions
    ADD CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) REFERENCES users (id),
    ADD CONSTRAINT fk_sessions_exercise FOREIGN KEY (exercise_id) REFERENCES exercises (id),
    ADD CONSTRAINT fk_sessions_plan FOREIGN KEY (plan_id) REFERENCES plans (id),
    ADD CONSTRAINT ck_sessions_reps CHECK (correct_reps >= 0 AND correct_reps <= total_reps);

-- Hai truy vấn hay dùng khác: lịch sử theo người dùng, tiến độ giáo án
CREATE INDEX IF NOT EXISTS idx_session_user_plan ON workout_sessions (user_id, plan_id, started_at);
CREATE INDEX IF NOT EXISTS idx_plan_items_plan ON plan_items (plan_id, day_index, sort_order);
