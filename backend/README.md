# AI Coach API (backend)

REST API cho app AI Coach — Spring Boot 3 (Java 17), PostgreSQL, JWT, Swagger. Chạy bằng Docker Compose trên máy ảo Azure, HTTPS tự động qua Caddy.

- **Swagger:** `https://aicoach-r15.koreacentral.cloudapp.azure.com/swagger-ui.html`
- **Base URL cho app:** `https://aicoach-r15.koreacentral.cloudapp.azure.com/api/v1/`
- **Tài khoản demo:** `demo@aicoach.vn` / `Demo1234` (có sẵn giáo án và 8 buổi tập)

## Kiến trúc

```
📱 App Android (Retrofit)
        │ HTTPS 443
        ▼
☁️ Azure VM (Ubuntu, Korea Central)
   ├─ caddy    : HTTPS Let's Encrypt, chuyển tiếp vào api:8080
   ├─ api      : Spring Boot — controller → service → repository (JPA)
   └─ db       : PostgreSQL 16 (không mở ra Internet), cấu trúc bảng do Flyway tạo
```

Mỗi module một package: `auth`, `user`, `home`, `exercise`, `plan`, `session`, `progress` (luật streak/XP), `security` (JWT), `common` (envelope, xử lý lỗi).

## Database

PostgreSQL 16, 11 bảng. Cấu trúc nằm trong `src/main/resources/db/migration/` và được **Flyway** áp dụng tự động khi server khởi động:

- `V1__init_schema.sql` — tạo bảng
- `V2__relations.sql` — khoá ngoại, ràng buộc, index
- `V3__password_reset.sql` — bảng mã quên mật khẩu, mốc đổi mật khẩu

Hibernate chạy ở chế độ `validate`: entity Java lệch với bảng thì server không khởi động. Sơ đồ quan hệ: [`docs/DATABASE.md`](../docs/DATABASE.md). Muốn đổi cấu trúc → thêm file `V3__...sql`, **không sửa file đã chạy**.

## Danh sách API

Mọi response có dạng `{ "success": true|false, "message": "...", "data": ... }`.
Các API (trừ auth, health, swagger) cần header `Authorization: Bearer <accessToken>`.

| Method | Path | Mô tả |
|---|---|---|
| GET | `/api/v1/health` | Kiểm tra server + database |
| POST | `/api/v1/auth/register` | Đăng ký `{displayName, email, password}` → token + hồ sơ (201) |
| POST | `/api/v1/auth/login` | Đăng nhập `{email, password}` → token + hồ sơ |
| POST | `/api/v1/auth/forgot-password` | Quên mật khẩu `{email}` → gửi mã 6 số qua email (hiệu lực 10 phút, 60 giây mới gửi lại) |
| POST | `/api/v1/auth/verify-code` | Kiểm tra mã `{email, code}`; sai 5 lần thì khoá mã |
| POST | `/api/v1/auth/reset-password` | Đặt mật khẩu mới `{email, code, newPassword}`; mọi token cũ bị huỷ |
| GET | `/api/v1/users/me` | Hồ sơ + tiến độ (streak, XP, tổng rep) |
| PUT | `/api/v1/users/me` | Sửa hồ sơ, chỉ gửi field cần đổi (khảo sát, sửa hồ sơ, chọn giáo án qua `activePlanId`) |
| PUT | `/api/v1/users/me/password` | Đổi mật khẩu `{currentPassword, newPassword}` |
| GET | `/api/v1/home` | Trang chủ trong 1 lần gọi: **Chuỗi 7 ngày** (`streak`, `week`) + **Bài tập hôm nay** (`todayWorkout`), kèm phút/kcal, giáo án |
| GET | `/api/v1/exercises?muscleGroup=&difficulty=&q=` | 9 bài tập, lọc + tìm không dấu |
| GET | `/api/v1/exercises/{id}` | Chi tiết bài tập (`squat`, `pushup`, `plank`, `lunge`, `glute_bridge`, `chair_dip`, `jumping_jack`, `high_knees`, `toe_touch`) |
| GET | `/api/v1/plans` · `/api/v1/plans/{id}` | Giáo án và các buổi |
| POST | `/api/v1/sessions` | Lưu buổi tập → server tự tính streak, XP |
| GET | `/api/v1/sessions?limit=20` · `/api/v1/sessions/{id}` | Lịch sử buổi tập |
| GET | `/api/v1/challenges` · `/api/v1/challenges/{id}` | Thử thách mình tham gia (đang diễn ra trước), tiến độ mọi thành viên tính từ buổi tập |
| POST | `/api/v1/challenges` | Tạo thử thách `{title, exerciseId, dailyTarget, days, privateGroup}` — 3/7/14/30 ngày, 10–1000 lần/ngày (201) |
| GET | `/api/v1/challenges/{id}/candidates?q=` | Người có thể mời: bạn bè (lọc tên không dấu) hoặc đúng email một người dùng |
| POST | `/api/v1/challenges/{id}/invites` | Mời `{userIds:[...]}` (tối đa 20) → người được mời nhận thông báo có nút Tham gia / Từ chối |
| POST | `/api/v1/challenges/join` | Tham gia bằng mã mời `{code:"7Q2KX9"}` (6 ký tự, không phân biệt hoa thường) |
| GET | `/api/v1/social/friends` | Bạn bè (kết bạn khi cùng tham gia thử thách) |
| GET | `/api/v1/social/leaderboard?scope=friends\|national` | Bảng xếp hạng theo XP tích luỹ; luôn kèm hạng của mình |
| GET | `/api/v1/notifications?type=all\|challenge\|system` | Thông báo mới nhất trước + số chưa đọc |
| POST | `/api/v1/notifications/read-all` · `/{id}/read` | Đánh dấu đã đọc |
| POST | `/api/v1/notifications/{id}/respond` | Trả lời lời mời `{accept: true\|false}` — chấp nhận thì vào thử thách và thành bạn với người mời |

Mã lỗi: 400 dữ liệu sai · 401 chưa đăng nhập / sai mật khẩu / token hết hạn · 404 không tìm thấy · 409 email đã tồn tại · 500 lỗi server.

Quy tắc dữ liệu: mật khẩu 8–64 ký tự có chữ và số · `goal`: `lose_weight|build_muscle|stay_fit` · `level`: `beginner|intermediate|advanced` · chiều cao 100–250 cm · cân nặng 30–250 kg.

## Triển khai lên máy ảo (lần đầu)

```bash
git clone https://github.com/ngocthuan-tech/nt118-mobile-app-development.git
cd nt118-mobile-app-development/backend
cp .env.example .env
nano .env            # điền DB_PASSWORD và JWT_SECRET (lệnh tạo chuỗi ngẫu nhiên có trong file)
docker compose up -d --build
docker compose logs -f api   # chờ dòng "Started AiCoachApiApplication"
```

## Cập nhật khi có code mới

```bash
cd ~/nt118-mobile-app-development && git pull
cd backend && docker compose up -d --build
```

## Bảo mật

- Mật khẩu băm BCrypt, không lưu bản gốc; đăng nhập sai trả cùng một câu lỗi để không lộ email nào đã đăng ký.
- JWT HS256 hết hạn sau 7 ngày, khoá ký đọc từ biến môi trường, không nằm trong code.
- Database chỉ nằm trong mạng nội bộ Docker; cổng SSH của VM chỉ mở cho IP của Trưởng nhóm.

## Gửi email (quên mật khẩu)

Điền vào `.env` trên server rồi `docker compose up -d`:

```
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=<gmail riêng của app>
MAIL_PASSWORD=<Mật khẩu ứng dụng 16 ký tự, viết liền>
```

Để trống `MAIL_HOST` thì không gửi email, mã được ghi vào log: `docker compose logs api | grep "Mã quên"`.
