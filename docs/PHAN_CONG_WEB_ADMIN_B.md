---
title: "AI Coach — Phân công Thành viên B: Web Admin (cuối kỳ)"
date: "26/11/2026"
---

**Người làm:** Thành viên B — Trương Đức Thịnh
**Người duyệt PR:** Trưởng nhóm — Nguyễn Ngọc Thuận
**Nhánh làm việc:** `codex/b-web-admin` (tách từ `develop` mới nhất) → Pull Request vào `develop`
**Thời gian:** khoảng 2 tuần, chia 4 mốc ở mục 7

> **Vì sao có việc này:** thầy yêu cầu **bắt buộc** đồ án phải có **web quản trị viết bằng React hoặc Angular**. Repo hiện chưa có. Thiếu phần này là thiếu yêu cầu bắt buộc, có thể bị trừ nặng hơn mọi lỗi giao diện.
> Backend làm giống cách B đã làm API danh hiệu và migration V4 (controller + service + DTO + Flyway + test chạy trên PostgreSQL thật).
> Dùng AI hỗ trợ code thì đưa **nguyên file này** làm ngữ cảnh. File đã ghi đủ API, bảng, quyền, cách test và cách deploy.

---

## 1. Phạm vi — làm đúng 4 chức năng, không hơn

| # | Chức năng | Màn hình web | Ý nghĩa khi bảo vệ |
|---|---|---|---|
| 1 | **Đăng nhập admin** | `/admin/login` | Phân quyền thật: server kiểm tra vai trò, không chỉ ẩn nút |
| 2 | **Dashboard thống kê** | `/admin` | Số người dùng, số buổi tập theo ngày, bài phổ biến, **lỗi tư thế AI phát hiện nhiều nhất** |
| 3 | **Quản lý người dùng** | `/admin/users` | Tìm, xem chi tiết, **khoá / mở khoá** tài khoản (khoá xong token cũ mất hiệu lực ngay) |
| 4 | **Quản lý bài tập + giáo án** | `/admin/exercises`, `/admin/plans` | Sửa tên, mô tả, kcal, số hiệp/lần mặc định; xem giáo án và sửa khối lượng từng bài |

**Không làm** (đừng sa đà): tạo/xoá bài tập mới, upload ảnh, phân quyền nhiều cấp, đa ngôn ngữ, dark mode, quản lý thử thách (backend Thử thách đã có ở V6 — nếu còn thời gian, có thể thêm 1 thẻ "Số thử thách đang diễn ra" ở Dashboard, đếm bảng `challenges` có `end_date ≥ hôm nay`).

---

## 2. Kiến trúc

```
App Android (người tập) ─┐
                         ├──►  Caddy (HTTPS)  ──►  Spring Boot /api/v1/**  ──►  PostgreSQL
Web Admin React (admin) ─┘          │
                                    └── /admin/*  → file tĩnh của web (build từ thư mục admin-web/)
```

- Web admin **gọi chính backend Spring Boot hiện có**, thêm nhóm API `/api/v1/admin/**`.
- Web được phục vụ **cùng tên miền** với API: `https://aicoach-r15.koreacentral.cloudapp.azure.com/admin/` → **không cần cấu hình CORS**, không tốn thêm tiền.
- **Công nghệ web:** React 18 + Vite + TypeScript, Ant Design 5 (bảng, form, layout), Recharts (biểu đồ), axios, React Router.
- **Nếu thầy chỉ định Angular** thì báo Trưởng nhóm trước khi code. Phần backend (mục 3, 4) giữ nguyên, chỉ đổi phần web.

---

## 3. Backend — phần B làm trong thư mục `backend/`

### 3.1. Database: migration `V7__admin_role.sql`

Tạo file `backend/src/main/resources/db/migration/V7__admin_role.sql`. **Không sửa các file V1–V6 đã có.**

```sql
-- Vai trò tài khoản (USER | ADMIN) và trạng thái khoá do admin đặt.
ALTER TABLE users ADD COLUMN role VARCHAR(10) NOT NULL DEFAULT 'USER';
ALTER TABLE users ADD COLUMN locked BOOLEAN NOT NULL DEFAULT FALSE;
```

Thêm 2 field tương ứng vào `user/UserAccount.java` (`role` mặc định `"USER"`, `locked` mặc định `false`) kèm getter/setter.

### 3.2. Tài khoản admin đầu tiên

Trong `seed/DataSeeder.java`: nếu biến môi trường `ADMIN_EMAIL` và `ADMIN_PASSWORD` có giá trị và chưa có user nào email đó → tạo user `role = ADMIN`.
- Mật khẩu **đọc từ biến môi trường**, không ghi trong code.
- Thêm 2 dòng mẫu vào `backend/.env.example`: `ADMIN_EMAIL=` và `ADMIN_PASSWORD=`.
- Thêm `ADMIN_EMAIL`, `ADMIN_PASSWORD` vào phần `environment` của service `api` trong `docker-compose.yml`.
- Test dùng: `admin@aicoach.vn` / `Admin1234` (đặt trong `application-test.yml`).

### 3.3. Phân quyền (bắt buộc làm đúng — hội đồng hay bắt lỗi chỗ này)

1. `security/JwtAuthFilter.java`: khi tạo `UsernamePasswordAuthenticationToken`, thay `List.of()` bằng quyền theo vai trò:
   `List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))`.
   (Hàm `stillValid` đã đọc user từ DB — tận dụng luôn, đừng query 2 lần.)
2. Cũng trong filter: user `locked = true` → coi như token không hợp lệ, đặt header lý do `X-Session-Reason: ACCOUNT_LOCKED` (giống cách đang làm với `PASSWORD_CHANGED`).
3. `security/SecurityConfig.java`: thêm **trước** `anyRequest()`:
   `.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")`
4. `auth/AuthService.login`: tài khoản bị khoá → trả **403** với câu `"Tài khoản đã bị khoá. Liên hệ quản trị viên."`

### 3.4. Danh sách API admin

Tạo package `backend/src/main/java/com/aicoach/api/admin/` gồm `AdminController`, `AdminService`, `AdminDtos`.
Mọi response giữ đúng khuôn `{ success, message, data }` (dùng `ApiResponse` có sẵn).

| Method | Path | Body / tham số | Trả về |
|---|---|---|---|
| GET | `/api/v1/admin/stats?days=30` | `days` 7–90 | `{ totalUsers, newUsers, activeUsers, totalSessions, sessionsPerDay: [{date, count}], topExercises: [{exerciseId, name, sessions}], topFormErrors: [{key, count}] }` |
| GET | `/api/v1/admin/users?q=&page=0&size=20` | `q` tìm theo email / tên | Trang `{ items: [{id, email, displayName, role, locked, streak, totalXp, createdAt, lastWorkoutDate}], total, page, size }` |
| GET | `/api/v1/admin/users/{id}` | — | Chi tiết user + 10 buổi tập gần nhất |
| PUT | `/api/v1/admin/users/{id}/lock` | `{ "locked": true }` | User sau khi đổi. **Khoá thì tăng `sessionVersion` thêm 1** để token cũ chết ngay. **Không được tự khoá chính mình** → 400 |
| GET | `/api/v1/admin/exercises` | — | Danh sách 9 bài (đủ field) |
| PUT | `/api/v1/admin/exercises/{id}` | `{ name, description, defaultSets, defaultReps, caloriesPerRep }` | Bài sau khi sửa. Kiểm tra: tên 2–50 ký tự, sets 1–10, reps 1–200, kcal 0–5 |
| GET | `/api/v1/admin/plans` | — | Danh sách giáo án kèm các buổi và bài |
| PUT | `/api/v1/admin/plans/{planId}/items/{itemId}` | `{ sets, reps }` | Mục giáo án sau khi sửa |

**Gợi ý tính thống kê:** `sessionsPerDay` đếm theo **ngày giờ Việt Nam** (dùng `common/VnTime` có sẵn, đừng tự cộng 7 giờ). `topFormErrors` cộng dồn map `formErrors` của các buổi tập trong khoảng ngày — key là mã lỗi AI như `knees_in`, `hip_sag`; web tự đổi sang tên tiếng Việt (bảng ở mục 4.4).

### 3.5. Test backend — `backend/src/test/java/com/aicoach/api/AdminFlowTest.java`

Viết giống `ApiFlowTest` (chạy trên PostgreSQL thật trong CI). **Bắt buộc có đủ 6 trường hợp:**

1. Không có token gọi `/admin/stats` → **401**
2. Token user thường gọi `/admin/stats` → **403**
3. Token admin gọi `/admin/stats` → **200**, `totalUsers ≥ 2`
4. Admin khoá user → user đó gọi `users/me` bằng token cũ → **401**
5. User bị khoá đăng nhập lại → **403**; admin mở khoá → đăng nhập được
6. Admin sửa `defaultReps` của `squat` → `GET /api/v1/exercises/squat` (API của app) thấy giá trị mới

CI (GitHub Actions → **Backend CI**) phải **xanh** mới được mở PR.

---

## 4. Web — thư mục mới `admin-web/` ở gốc repo

### 4.1. Khởi tạo

```bash
npm create vite@latest admin-web -- --template react-ts
cd admin-web
npm install antd @ant-design/icons recharts axios react-router-dom dayjs
```

- `vite.config.ts`: đặt `base: '/admin/'`, và `server.proxy` chuyển `/api` sang `https://aicoach-r15.koreacentral.cloudapp.azure.com` để chạy thử trên máy (`npm run dev`).
- **Không commit** `node_modules/` và `dist/` (thêm vào `.gitignore`).

### 4.2. Cấu trúc gợi ý

```
admin-web/src/
├── api/client.ts          axios: baseURL "/api/v1", gắn "Authorization: Bearer <token>", gặp 401/403 → về /admin/login
├── api/admin.ts           các hàm gọi đúng bảng API ở mục 3.4 (có kiểu TypeScript cho từng response)
├── auth/AuthContext.tsx   lưu token trong sessionStorage; đăng nhập = POST /api/v1/auth/login rồi gọi /admin/stats để chắc là ADMIN
├── layout/AdminLayout.tsx thanh menu trái: Tổng quan · Người dùng · Bài tập · Giáo án · Đăng xuất
├── pages/LoginPage.tsx
├── pages/DashboardPage.tsx
├── pages/UsersPage.tsx    + UserDetailDrawer.tsx
├── pages/ExercisesPage.tsx
├── pages/PlansPage.tsx
└── constants/formErrors.ts   bảng đổi mã lỗi → tên tiếng Việt (mục 4.4)
```

### 4.3. Từng màn

| Màn | Nội dung bắt buộc |
|---|---|
| **Đăng nhập** | Email + mật khẩu; user thường đăng nhập được API nhưng gọi `/admin/stats` bị 403 → báo "Tài khoản không có quyền quản trị", xoá token |
| **Tổng quan** | 4 thẻ số (Tổng người dùng · Người dùng mới · Đang hoạt động · Tổng buổi tập); biểu đồ cột **buổi tập theo ngày**; biểu đồ thanh **bài phổ biến**; bảng **lỗi tư thế hay gặp** (tên tiếng Việt). Bộ chọn 7 / 30 / 90 ngày |
| **Người dùng** | Bảng có tìm kiếm + phân trang (phân trang ở server); cột trạng thái (Hoạt động / Đã khoá); nút Khoá / Mở khoá **có hộp xác nhận**; bấm 1 dòng mở ngăn chi tiết với 10 buổi tập gần nhất |
| **Bài tập** | Bảng 9 bài; nút Sửa mở form (tên, mô tả, số hiệp, số lần, kcal) có kiểm tra giống server |
| **Giáo án** | Danh sách giáo án → mở ra các buổi → sửa số hiệp × lần từng bài |

Màu chủ đạo dùng đúng màu app: `#1E5EFF` (ConfigProvider của Ant Design → `colorPrimary`). Giao diện tiếng Việt, ngày giờ hiển thị theo giờ Việt Nam.

### 4.4. Bảng mã lỗi AI → tên tiếng Việt (khớp `FormIssue.java` trong app)

| Mã | Tên hiển thị |
|---|---|
| `squat_depth` | Squat chưa xuống đủ sâu |
| `knees_in` | Gối chụm vào trong |
| `torso_lean` | Ngả người quá nhiều |
| `hip_sag` | Võng hông |
| `hip_pike` | Nhô mông quá cao |
| `pushup_depth` | Hít đất chỉ hạ nửa chừng |
| `lunge_depth` | Lunge gối chưa gập đủ |
| `bridge_height` | Cầu mông chưa nâng hông đủ |
| `dip_depth` | Chống ghế chưa hạ đủ sâu |
| `arms_low` | Bật nhảy tay chưa đưa lên hết |
| `legs_closed` | Bật nhảy chân chưa dang rộng |
| `knee_low` | Nâng gối quá thấp |
| `toe_reach` | Chưa với tới mũi chân |

Mã lạ (sau này thêm) → hiện nguyên mã, không được crash.

---

## 5. Deploy lên server (B chuẩn bị file, Trưởng nhóm chạy trên VM)

B thêm vào repo:

1. **`admin-web/Dockerfile`** (2 giai đoạn): `node:20-alpine` chạy `npm ci && npm run build` → copy `dist/` ra image `alpine` nhỏ ở `/srv/admin`.
2. **`backend/docker-compose.yml`**: thêm service `admin-web` build từ `../admin-web`, chỉ để chép file tĩnh vào một volume `admin_static`; service `caddy` gắn thêm volume đó vào `/srv/admin` (read-only).
3. **`backend/Caddyfile`**: thêm khối phục vụ web trước `reverse_proxy`:

```
{$API_DOMAIN} {
	encode gzip
	handle /admin* {
		root * /srv
		try_files {path} /admin/index.html
		file_server
	}
	handle {
		reverse_proxy api:8080
	}
}
```

Trưởng nhóm cập nhật server bằng đúng 2 lệnh quen thuộc: `git pull` rồi `docker compose up -d --build`.

---

## 6. Bảo mật — tự kiểm trước khi mở PR

- [ ] Mọi API `/admin/**` bị chặn ở **server** (test 401/403 ở mục 3.5 xanh) — ẩn menu trên web **không phải** là bảo mật
- [ ] Mật khẩu admin chỉ nằm trong `.env` trên server, **không** có trong code, test chỉ dùng mật khẩu riêng của môi trường test
- [ ] Khoá tài khoản làm token cũ mất hiệu lực ngay (tăng `sessionVersion`)
- [ ] Admin không tự khoá được chính mình
- [ ] Token admin lưu `sessionStorage` (đóng tab là hết), **không** lưu `localStorage`
- [ ] Không in token, mật khẩu ra console

---

## 7. Mốc thời gian (khoảng 2 tuần)

| Mốc | Việc | Cách kiểm tra xong |
|---|---|---|
| **M1** (ngày 1–3) | V7 + role/locked + phân quyền + seed admin + `AdminFlowTest` trường hợp 1–3 | Backend CI xanh; Swagger gọi `/admin/stats` bằng token admin ra 200, token user ra 403 |
| **M2** (ngày 4–6) | Đủ API mục 3.4 + test 4–6 | Backend CI xanh |
| **M3** (ngày 7–11) | Web: Đăng nhập, Tổng quan, Người dùng, Bài tập, Giáo án chạy được bằng `npm run dev` | Quay màn hình 1 phút gửi Trưởng nhóm |
| **M4** (ngày 12–14) | Dockerfile + Caddy + README chạy web; mở PR | Trưởng nhóm deploy, mở được `https://aicoach-r15.koreacentral.cloudapp.azure.com/admin/` |

Mỗi mốc xong thì **commit + push** lên `codex/b-web-admin` (đừng dồn cuối), nhắn nhóm 1 câu kèm ảnh.

---

## 8. Quy tắc chung (giữ như cả nhóm)

- Chỉ sửa trong: `admin-web/`, `backend/.../admin/`, `V7__admin_role.sql`, và **đúng các dòng** đã nêu trong `UserAccount`, `JwtAuthFilter`, `SecurityConfig`, `AuthService`, `DataSeeder`, `docker-compose.yml`, `Caddyfile`, `.env.example`. Muốn sửa file khác → hỏi Trưởng nhóm trước.
- **Không sửa** các migration V1–V6 và không đổi API app Android đang dùng.
- Trước khi mở PR: `git pull origin develop` vào nhánh mình, chạy lại test, CI xanh.
- PR mô tả: làm gì, ảnh 5 màn web, kết quả 6 test ở mục 3.5.

---

## 9. Hội đồng có thể hỏi — chuẩn bị sẵn

| Câu hỏi | Ý trả lời |
|---|---|
| Sao admin làm web mà không làm trong app? | Hai nhóm người dùng khác nhau: người tập dùng điện thoại + camera; admin ngồi máy tính xem bảng số liệu lớn. Chọn nền tảng theo người dùng |
| Sao chọn React mà không chọn Angular? | Học nhanh hơn, hệ sinh thái component admin (Ant Design) có sẵn bảng, form, phân trang; dùng TypeScript nên vẫn có kiểu dữ liệu chặt như Java |
| Làm sao biết người gọi API là admin? | Server đọc user từ token, gán quyền `ROLE_ADMIN` theo cột `role` trong DB; Spring Security chặn `/admin/**` nếu thiếu quyền. Có test tự động chứng minh 401/403 |
| Khoá tài khoản xong thì app của người đó thế nào? | Tăng `sessionVersion` → token cũ bị từ chối ngay ở lần gọi API kế tiếp → app tự về màn Đăng nhập; đăng nhập lại bị báo đã khoá |
| Dashboard lấy số liệu từ đâu? | Truy vấn trực tiếp bảng `workout_sessions` và `users` trong PostgreSQL; "lỗi tư thế hay gặp" là tổng hợp từ dữ liệu AI trên điện thoại gửi lên mỗi buổi tập |
| Sửa trên web thì app có đổi không? | Có: màn Thư viện / Chi tiết bài tập đọc `defaultSets/defaultReps` qua `GET /exercises` (test số 6 chứng minh); sửa khối lượng trong **giáo án** thì buổi tập theo giáo án dùng ngay số mới, vì app lấy khối lượng từ `GET /plans` và `GET /home` |
