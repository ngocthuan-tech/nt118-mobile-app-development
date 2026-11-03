# Sửa hồ sơ — Thành viên B

Ngày thực hiện: 03/11/2026. Màn 44, từ Hồ sơ → Chỉnh sửa.

## Phần hoàn thành trong đợt này

- Tải hồ sơ hiện tại qua `UserRepository.getMe()`.
- Lưu tên, chiều cao, cân nặng, mục tiêu, trình độ, số buổi/tuần qua `PUT users/me`.
- Chỉ gửi các trường thay đổi; không đụng email, giáo án đang theo hoặc trạng thái khảo sát.
- Kiểm tra tên 2–50 ký tự, chiều cao 100–250 cm, cân nặng 30–250 kg. Hỗ trợ dấu phẩy thập phân.
- Khoá nút Lưu khi chưa có thay đổi hoặc đang gửi; tránh gửi trùng. Chặn quay lại trong lúc lưu.
- Hiện lỗi tải/lưu; tải thất bại có nút thử lại, lưu thất bại giữ nguyên nội dung để gửi lại.
- Giữ bản nháp và yêu cầu đang chạy khi tái tạo Activity, không tự gửi lại.
- Chỉ quay về sau khi server trả thành công có dữ liệu; màn Hồ sơ tải lại và thông báo đã lưu.

Luồng: `EditProfileFragment → EditProfileViewModel → UserRepository → UserApi`.

## Kiểm thử tự động

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.aicoach.ui.profile.EditProfileFlowTest'
```

- 4 unit test mới: giới hạn dữ liệu, số không hợp lệ, giá trị trống ban đầu, định dạng tương đương không tạo thay đổi, PUT đúng endpoint và chỉ có các trường thay đổi.
- 2 test Android trên Medium Phone API 37: tải lỗi/thử lại, chống gửi lặp, giữ bản nháp, lỗi dưới ô nhập, Activity recreation, lỗi lưu và lưu lại thành công, quay về Hồ sơ với dữ liệu mới.
- Test dùng dữ liệu giả, không sửa tài khoản thật/demo. Chưa xác minh luồng sửa hồ sơ với server Azure trong đợt này.

## Kiểm tra khi demo với tài khoản riêng

1. Mở Hồ sơ → Chỉnh sửa. Kiểm tra dữ liệu cũ và nút Lưu chưa bật.
2. Đổi tên, nhập chiều cao 99: bấm Lưu phải hiện lỗi, không rời màn hình.
3. Sửa chiều cao hợp lệ, đổi mục tiêu/trình độ/số buổi rồi Lưu.
4. Xác nhận Hồ sơ cập nhật; mở lại Chỉnh sửa và khởi động lại app để xác nhận server đã lưu.
5. Thử mất mạng khi lưu: nội dung vẫn còn, có thông báo lỗi; kết nối lại rồi Lưu.

## Giới hạn và phần để sau

- Ẩn năm sinh/giới tính vì DTO hiện tại chưa có hai trường này; ẩn nút đổi ảnh vì chưa triển khai luồng tải ảnh. Xoá tài khoản vẫn hiển thị thông báo chưa hỗ trợ.
- Chiều cao/cân nặng chưa nhập có thể giữ trống; giá trị đã có không thể xoá thành null vì API bỏ qua null.
- ViewModel giữ bản nháp khi xoay màn hình; chưa lưu bản nháp qua việc Android kết thúc tiến trình.
- Tiếp theo: cài đặt giọng nói/nhắc tập, lịch sử–lịch tập–chi tiết buổi tập, thống kê, huy hiệu, thử thách và thông báo theo phân công B.

Phần Hồ sơ GET và Đổi mật khẩu đã được gộp trước đó qua PR #14; đợt này bổ sung Sửa hồ sơ.
