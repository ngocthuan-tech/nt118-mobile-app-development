-- Buổi tập theo giáo án: ghi thêm bài này thuộc NGÀY nào của giáo án (plan_items.day_index).
-- Một buổi giáo án chỉ được tính là xong khi MỌI bài của ngày đó đều đã tập (≥ 50% mục tiêu).
-- NULL = tập lẻ, hoặc dữ liệu cũ trước V5 (vẫn tính theo quy tắc cũ: mỗi ngày có tập = 1 buổi).
-- Không cần index mới: idx_session_user_plan (user_id, plan_id, started_at) ở V2 đã phục vụ truy vấn này.
ALTER TABLE workout_sessions ADD COLUMN plan_day INTEGER;
