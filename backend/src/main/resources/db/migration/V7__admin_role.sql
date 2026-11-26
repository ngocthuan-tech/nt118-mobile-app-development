-- Web admin (cuối kỳ): vai trò tài khoản + khoá tài khoản, và đánh dấu dữ liệu admin đã sửa.
--  users.role    USER | ADMIN — server gán quyền ROLE_ADMIN theo cột này (không tin dữ liệu từ app/web gửi lên)
--  users.locked  admin khoá tài khoản: không đăng nhập được, token cũ bị từ chối ngay
--  exercises.admin_edited / plans.admin_edited: admin đã sửa trên web → DataSeeder khởi động lại
--    KHÔNG ghi đè bằng nội dung mặc định trong code (nếu không, mỗi lần deploy là mất chỉnh sửa của admin)
ALTER TABLE users ADD COLUMN role VARCHAR(10) NOT NULL DEFAULT 'USER';
ALTER TABLE users ADD CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN'));
ALTER TABLE users ADD COLUMN locked BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE exercises ADD COLUMN admin_edited BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE plans ADD COLUMN admin_edited BOOLEAN NOT NULL DEFAULT FALSE;

-- Trang "Người dùng" của admin: sắp xếp người mới đăng ký trước
CREATE INDEX idx_users_created_at ON users (created_at DESC);
-- Thống kê theo khoảng ngày trên toàn hệ thống (không lọc theo user)
CREATE INDEX idx_session_started_at ON workout_sessions (started_at);
