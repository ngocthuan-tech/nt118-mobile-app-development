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

PostgreSQL 16, 10 bảng. Cấu trúc nằm trong `src/main/resources/db/migration/` và được **Flyway** áp dụng tự động khi server khởi động:

- `V1__init_schema.sql` — tạo bảng
- `V2__relations.sql` — khoá ngoại, ràng buộc, index

Hibernate chạy ở chế độ `validate`: entity Java lệch với bảng thì server không khởi động. Sơ đồ quan hệ: [`docs/DATABASE.md`](../docs/DATABASE.md). Muốn đổi cấu trúc → thêm file `V3__...sql`, **không sửa file đã chạy**.

## Danh sách API

Mọi response có dạng `{ "success": true|false, "message": "...", "data": ... }`.
Các API (trừ auth, health, swagger) cần header `Authorization: Bearer <accessToken>`.

| Method | Path | Mô tả |
|---|---|---|
| GET | `/api/v1/health` | Kiểm tra server + database |
| POST | `/api/v1/auth/register` | Đăng ký `{displayName, email, password}` → token + hồ sơ (201) |
| POST | `/api/v1/auth/login` | Đăng nhập `{email, password}` → token + hồ sơ |
| GET | `/api/v1/users/me` | Hồ sơ + tiến độ (streak, XP, tổng rep) |
| PUT | `/api/v1/users/me` | Sửa hồ sơ, chỉ gửi field cần đổi (khảo sát, sửa hồ sơ, chọn giáo án qua `activePlanId`) |
| PUT | `/api/v1/users/me/password` | Đổi mật khẩu `{currentPassword, newPassword}` |
| GET | `/api/v1/home/streak` | **Chuỗi 7 ngày**: số ngày liên tiếp + 7 ngày T2–CN tuần này |
| GET | `/api/v1/home/today-workout` | **Bài tập hôm nay**: buổi tiếp theo của giáo án, hoặc 3 bài gợi ý |
| GET | `/api/v1/home` | Gộp toàn bộ Trang chủ trong 1 lần gọi (tuỳ chọn) |
| GET | `/api/v1/exercises?muscleGroup=&difficulty=&q=` | 9 bài tập, lọc + tìm không dấu |
| GET | `/api/v1/exercises/{id}` | Chi tiết bài tập (`squat`, `pushup`, `plank`, `lunge`, `glute_bridge`, `chair_dip`, `jumping_jack`, `high_knees`, `toe_touch`) |
| GET | `/api/v1/plans` · `/api/v1/plans/{id}` | Giáo án và các buổi |
| POST | `/api/v1/sessions` | Lưu buổi tập → server tự tính streak, XP |
| GET | `/api/v1/sessions?limit=20` · `/api/v1/sessions/{id}` | Lịch sử buổi tập |

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
