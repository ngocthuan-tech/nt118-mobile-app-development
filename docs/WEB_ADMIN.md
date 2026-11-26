# AI Coach — Web Admin

Web quản trị bắt buộc của đồ án (React). Địa chỉ sau khi deploy:
**https://aicoach-r15.koreacentral.cloudapp.azure.com/admin/**

| Thành phần | Vị trí |
|---|---|
| Web (React 18 + Vite + TypeScript + Ant Design 5 + Recharts) | `admin-web/` |
| API quản trị (Spring Boot) | `backend/src/main/java/com/aicoach/api/admin/` |
| Phân quyền | `security/JwtAuthFilter.java`, `security/SecurityConfig.java` |
| Database | `V7__admin_role.sql` (`users.role`, `users.locked`, `admin_edited`) |
| Test tự động | `backend/.../AdminFlowTest.java` (6 kịch bản trên PostgreSQL thật), `admin-web/src/utils/format.test.ts` |
| CI | `.github/workflows/admin-web-ci.yml` (test + build web, build Docker, kiểm tra Caddyfile + docker compose) |

## 1. Chức năng

| Màn | Đường dẫn | Nội dung |
|---|---|---|
| Đăng nhập | `/admin/login` | Email + mật khẩu. Tài khoản thường đăng nhập được API nhưng bị báo "không có quyền quản trị" |
| Tổng quan | `/admin/` | 7 thẻ số (tổng người dùng, người mới, đang hoạt động, buổi tập trong kỳ, tổng buổi tập, tỉ lệ làm đúng do AI chấm, thử thách còn hạn); biểu đồ buổi tập + đăng ký theo ngày; bài phổ biến; **lỗi tư thế AI hay gặp** (tên tiếng Việt). Chọn 7 / 30 / 90 ngày |
| Người dùng | `/admin/users` | Tìm theo email / tên (không dấu), phân trang ở server, **khoá / mở khoá có xác nhận**, bấm một dòng mở chi tiết + 10 buổi tập gần nhất |
| Bài tập | `/admin/exercises` | 9 bài: sửa tên, mô tả, số hiệp, số lần (giây với Plank), kcal — app thấy ngay |
| Giáo án | `/admin/plans` | 3 giáo án → từng buổi → sửa số hiệp × lần từng bài; số người đang theo giáo án |

## 2. Bảo mật — điểm hội đồng hay hỏi

1. **Chặn ở server, không chỉ ẩn menu:** `SecurityConfig` có `/api/v1/admin/**` → `hasRole("ADMIN")`. Chưa đăng nhập → 401, người dùng thường → 403 (có test).
2. **Quyền đọc từ database mỗi request:** `JwtAuthFilter` lấy người dùng theo token rồi gán `ROLE_` + cột `users.role`. Quyền không nằm trong token nên thu hồi quyền có hiệu lực ngay, sửa token cũng không tự nâng quyền được.
3. **Khoá tài khoản:** đặt `locked = true` và tăng `session_version` → mọi token đang dùng bị từ chối ngay ở request kế tiếp (header `X-Session-Reason: ACCOUNT_LOCKED`, app hiện hộp thoại "Tài khoản đã bị khoá"); đăng nhập lại trả 403. Kiểm tra khoá **sau** khi đúng mật khẩu nên người ngoài không dò được tài khoản nào bị khoá.
4. **Admin không tự khoá mình, không khoá admin khác** (400).
5. **Admin đầu tiên tạo từ biến môi trường** `ADMIN_EMAIL` / `ADMIN_PASSWORD` trong `.env` trên server — không có mật khẩu trong code. Email đã có người đăng ký bằng app thì **không tự nâng quyền** (chặn kiểu đăng ký trước email admin để chiếm quyền).
6. **Token admin lưu `sessionStorage`** (đóng tab là hết), không lưu `localStorage`, không in ra console.
7. Web cùng tên miền với API → không mở CORS. Caddy thêm `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`, `Referrer-Policy: no-referrer` cho `/admin/`.

## 3. Sửa nội dung không bị mất khi deploy

Trước đây server ghi đè 9 bài tập + 3 giáo án theo code mỗi lần khởi động. Nếu giữ nguyên, admin sửa xong, deploy lại là mất. V7 thêm cờ `admin_edited`: admin sửa bài / giáo án nào thì cờ bật, `DataSeeder` bỏ qua bài / giáo án đó.

## 4. Chạy thử trên máy (không cần cài backend)

```bash
cd admin-web
npm install
npm run dev          # mở http://localhost:5173/admin/  — request /api tự chuyển tới server Azure
npm test             # unit test
npm run build        # kiểm tra kiểu TypeScript + build ra dist/
```

Đăng nhập bằng tài khoản admin đã đặt trong `.env` của server.

## 5. Deploy (Trưởng nhóm chạy trên VM)

1. **Sao lưu database** (V7 sửa bảng `users`, `exercises`, `plans`):
   ```bash
   cd ~/nt118-mobile-app-development/backend
   docker compose exec -T db pg_dump -U aicoach aicoach > ~/backups/aicoach_$(date +%F_%H%M).sql
   ```
2. **Thêm tài khoản admin vào `.env`** (mở bằng `nano .env`, KHÔNG `cat` file này khi đang chia sẻ màn hình):
   ```
   ADMIN_EMAIL=<email quản trị>
   ADMIN_PASSWORD=<mật khẩu ≥ 8 ký tự>
   ```
3. Cập nhật code và build lại (lệnh quen thuộc):
   ```bash
   cd ~/nt118-mobile-app-development && git pull
   cd backend && docker compose up -d --build
   ```
4. Kiểm tra:
   ```bash
   docker compose logs api | grep -E "version \"7|Đã tạo tài khoản quản trị|Started AiCoach|ERROR"
   docker compose ps          # admin-web: Exited (0) là đúng — nó chỉ chép file rồi thoát
   ```
   Mở `https://aicoach-r15.koreacentral.cloudapp.azure.com/admin/` và đăng nhập.

## 6. Câu hỏi phản biện thường gặp

| Câu hỏi | Ý trả lời |
|---|---|
| Sao admin làm web mà không làm trong app? | Hai nhóm người dùng khác nhau: người tập dùng điện thoại + camera; admin ngồi máy tính xem bảng số liệu lớn |
| Sao chọn React? | Hệ sinh thái component admin có sẵn (Ant Design: bảng, form, phân trang), TypeScript cho kiểu dữ liệu chặt như Java, build ra file tĩnh phục vụ bằng Caddy không tốn thêm server |
| Làm sao biết người gọi API là admin? | Server đọc user từ token, gán quyền theo cột `role` trong DB; Spring Security chặn `/admin/**`. Test tự động chứng minh 401 / 403 |
| Khoá tài khoản thì app người đó thế nào? | Token cũ bị từ chối ngay ở request kế tiếp → app hiện "Tài khoản đã bị khoá" và về màn Đăng nhập; đăng nhập lại bị báo đã khoá |
| Dashboard lấy số liệu từ đâu? | Truy vấn trực tiếp `users`, `workout_sessions`, `session_form_errors`, `challenges`; ngày tính theo giờ Việt Nam. "Lỗi tư thế hay gặp" tổng hợp từ dữ liệu AI trên điện thoại gửi lên mỗi buổi tập |
| Sửa trên web thì app có đổi không? | Có: Thư viện / Chi tiết bài đọc `GET /exercises`, giáo án đọc `GET /plans` và `GET /home` — có test chứng minh |
| Nhiều người dùng thì sao? | Danh sách người dùng phân trang ở server; thống kê có index `workout_sessions(started_at)`, `users(created_at)`; lỗi tư thế cộng dồn bằng 1 câu SQL `GROUP BY` |
