# AI Coach — Huấn luyện viên thể dục tại nhà với AI

Đồ án môn Lập trình ứng dụng di động Android (NT118) — Nhóm 8.

App dùng camera điện thoại + ML Kit Pose Detection để **tự đếm số lần tập, phát hiện sai tư thế và nhắc bằng giọng nói tiếng Việt**, kèm giáo án, lịch sử, thống kê, thử thách và bảng xếp hạng với bạn bè.

## Công nghệ

| Phần | Công nghệ |
|---|---|
| Ngôn ngữ / giao diện | Java 11, XML View, Material 3 |
| Kiến trúc | MVVM (Fragment → ViewModel → Repository), Single Activity + Navigation Component, ViewBinding |
| Backend | REST API Spring Boot 3 + PostgreSQL + JWT, chạy Docker trên Azure VM (thư mục [`backend/`](backend/README.md)) |
| Gọi API | Retrofit + OkHttp, mỗi nhóm API một interface trong `data/remote/api/` |
| AI | CameraX + ML Kit Pose Detection (chạy trên máy, không gửi hình lên mạng) |
| Khác | Glide, MPAndroidChart, GitHub Actions (tự build APK) |

## Chạy project lần đầu

1. Android Studio → **File → New → Project from Version Control** → dán link repo này.
2. Chuyển sang nhánh `develop` (góc trên bên trái, chỗ tên nhánh).
3. Bấm **Sync Now** rồi **Run**. App gọi thẳng backend trên Azure, không cần file cấu hình riêng.
4. Đăng nhập thử bằng tài khoản demo `demo@aicoach.vn` / `Demo1234`. Xem toàn bộ API: [Swagger](https://aicoach-r15.koreacentral.cloudapp.azure.com/swagger-ui.html).

## Cấu trúc thư mục

```
app/src/main/java/com/aicoach
├── common/           Resource<T>, BaseFragment, ErrorMapper                    (Trưởng nhóm)
├── data/model/       User, Exercise, Plan, WorkoutSession, HomeData, Challenge (Trưởng nhóm)
├── data/remote/      ApiClient, AuthInterceptor, ApiResponse, api/*Api, dto/   (Trưởng nhóm)
├── data/local/       TokenManager, SessionEvents, SettingsPrefs                (Trưởng nhóm)
├── data/repository/  BaseRepository (Trưởng nhóm) · mỗi người Repository riêng   (A, B)
├── data/mock/        Dữ liệu mẫu cho màn chưa có API (A, B)
├── domain/           BadgeRules — luật huy hiệu                                (B)
├── pose/             PoseOverlayView, bộ phân tích tư thế 9 bài                (Trưởng nhóm)
├── ui/auth, home, exercise, plan                                               (A)
├── ui/workout                                                                  (Trưởng nhóm)
└── ui/profile, history, stats, social, challenge                               (B)
backend/              REST API Spring Boot                                      (Trưởng nhóm)
```

Tài liệu phân công chi tiết: [`docs/PHAN_CONG_THANH_VIEN_A.md`](docs/PHAN_CONG_THANH_VIEN_A.md), [`docs/PHAN_CONG_THANH_VIEN_B.md`](docs/PHAN_CONG_THANH_VIEN_B.md).

## Màn hình theo Figma

Toàn bộ 55 màn (ảnh + người phụ trách + trạng thái) nằm ở [`docs/MAN_HINH.md`](docs/MAN_HINH.md), ảnh gốc trong [`docs/figma/`](docs/figma/).

- Hệ màu, kiểu chữ, style nút đã khớp Figma (`res/values/`); 62 icon dùng chung cho mọi màn giữa kỳ — tra tên ở [`docs/ICONS.md`](docs/ICONS.md).
- Thanh 5 tab có nút tròn "Tập AI" nổi ở giữa đúng như Figma.
- Luồng tập AI của Trưởng nhóm (Figma 18, 27–36) đã dựng đủ giao diện, chạy được bằng dữ liệu mẫu.
- Các màn của Thành viên A và B: mỗi người tự dựng theo ảnh trong `docs/figma/`.

## Quy trình làm việc với Git

Xem [`docs/QUY_TRINH_GIT.md`](docs/QUY_TRINH_GIT.md). Tóm tắt:

```
main      ← bản ổn định để nộp/demo (chỉ merge từ develop)
develop   ← bản tích hợp của cả nhóm (merge qua Pull Request)
feature/lead-ai-workout     ← Trưởng nhóm (module AI)
feature/lead-backend        ← Trưởng nhóm (backend + lớp Retrofit)
feature/a-auth-content      ← Thành viên A
feature/b-progress-social   ← Thành viên Trương Đức Thịnh
```

Mỗi Pull Request được GitHub Actions tự build APK (và test backend nếu sửa `backend/`); build đỏ thì chưa được merge.
