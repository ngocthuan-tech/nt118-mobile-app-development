# Quy trình Git của nhóm

## Các nhánh

| Nhánh | Dùng để | Ai được đẩy vào |
|---|---|---|
| `main` | Bản ổn định để nộp và demo | Chỉ merge từ `develop` qua Pull Request |
| `develop` | Bản tích hợp, luôn build được | Merge từ nhánh feature qua Pull Request |
| `feature/lead-ai-workout` | Module tập AI (camera, đếm rep, kết quả) | Trưởng nhóm |
| `feature/a-auth-content` | Đăng nhập, Trang chủ, Bài tập, Giáo án, Hồ sơ | Thành viên A |
| `feature/b-progress-social` | Tiến độ, Thống kê, Huy hiệu, Bạn bè, Thử thách | Thành viên B |

Làm xong một phần lớn (ví dụ xong màn Đăng nhập) có thể tách nhánh nhỏ hơn từ nhánh của mình, ví dụ `feature/a-s03-login`, rồi mở Pull Request như bình thường.

## Mỗi ngày làm việc (trong Android Studio, Windows)

1. **Lấy code mới nhất của nhóm về nhánh mình**
   - Chuyển sang nhánh của mình (bấm tên nhánh trên thanh công cụ trên cùng).
   - Menu **Git → Pull** (Ctrl + T). Chọn kéo từ `origin/develop` để cập nhật phần của 2 bạn kia.
2. **Code** phần của mình.
3. **Commit**: Ctrl + K → tick các file của mình → viết message → **Commit**.
   - Mẫu message: `feat(auth): kiểm tra email khi đăng nhập`, `fix(home): streak không cập nhật`, `ui(result): dựng màn kết quả theo Figma`.
4. **Push**: Ctrl + Shift + K → Push.

## Đưa code vào `develop` (Pull Request)

1. Lên GitHub → repo → tab **Pull requests** → **New pull request**.
2. base: `develop` ← compare: nhánh của mình → **Create pull request**.
3. Điền mẫu có sẵn (làm màn nào, ảnh chụp màn hình so với Figma).
4. Chờ dấu xanh của **Android CI** (GitHub tự build APK, khoảng 5 phút).
5. Một người khác trong nhóm review → **Approve** → **Merge**.

Cuối mỗi tuần (hoặc trước buổi báo cáo), Trưởng nhóm mở Pull Request `develop → main`.

## Quy tắc tránh xung đột

- Chỉ sửa file thuộc phần của mình (xem bảng trong `README.md`).
- Cần sửa file chung (`colors.xml`, `themes.xml`, `nav_root.xml`, `common/`, `data/model/`) thì nhắn Trưởng nhóm trước.
- Chuỗi chữ đặt trong file `strings_<module>.xml` riêng của mình: `strings_home.xml`, `strings_workout.xml`… để không đụng nhau.
- Không commit: `local.properties`, thư mục `build/`, `.idea/`, `google-services.json`.

## Lấy APK để cài thử

Mỗi lần build xong trên GitHub: tab **Actions** → chọn lần chạy mới nhất → mục **Artifacts** → tải `ai-coach-debug-apk`.
