# Đổi mật khẩu — Thành viên B

Hồ sơ → Đổi mật khẩu dùng `ChangePasswordDialogFragment` → `ChangePasswordViewModel`
→ `UserRepository.changePassword()` → `UserApi` (`PUT users/me/password`).

- Bắt buộc mật khẩu hiện tại; mật khẩu mới 8–64 ký tự, có chữ và số, khác mật khẩu hiện tại; nhập lại phải khớp.
- Không trim mật khẩu khi gửi. Lỗi nhập liệu hiện dưới từng ô; lỗi server/mạng hiện trong hộp thoại.
- Khi đang gửi: khóa các ô, nút gửi và thao tác đóng/kéo hộp thoại; hiện tiến trình.
- Thành công: thông báo, xóa nội dung ba ô và đóng hộp thoại. Thất bại: giữ hộp thoại để sửa/thử lại.
- ViewModel giữ yêu cầu đang chạy khi tạo lại giao diện, tránh gửi hai lần. Hộp thoại cuộn được khi bàn phím mở.

## Kiểm thử tự động

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.aicoach.ui.profile.ChangePasswordFlowTest'
```

Unit test kiểm tra các biên độ dài, mật khẩu yếu/trùng/không khớp và khoảng trắng.
Kiểm thử thiết bị kiểm tra đầu vào sai không gọi API, chặn gửi trùng khi không có observer,
giữ kết quả qua vòng đời observer và thử lại sau lỗi mạng giả lập.

Kiểm thử live **chỉ bật chủ động**: tạo một tài khoản riêng mỗi lần chạy trên backend cấu hình trong
`BuildConfig.BASE_URL`. Dùng email ngẫu nhiên `password-test-…@example.com`, mật khẩu ngẫu nhiên;
không dùng tài khoản demo. Tài khoản thử còn lại trên server vì hiện chưa có API xóa tài khoản.

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.aicoach.ui.profile.ChangePasswordFlowTest' '-Pandroid.testInstrumentationRunnerArguments.livePasswordTest=true'
```

Luồng live mở hộp thoại thật, thử bỏ trống và sai mật khẩu hiện tại, tạo lại Activity để kiểm tra
dữ liệu nhập, đổi mật khẩu thành công rồi xác nhận mật khẩu mới đăng nhập được, mật khẩu cũ bị từ chối.
Test khôi phục token đã có trên máy sau khi kết thúc. Các ô và nút được thao tác trên UI thread;
không dùng bộ giả lập chạm Espresso cũ vốn không tương thích API 37.

## Kiểm tra bằng tay

1. Đăng nhập bằng tài khoản tự tạo → Hồ sơ → Đổi mật khẩu.
2. Thử ô trống, mật khẩu mới ngắn/chỉ chữ/chỉ số, trùng mật khẩu cũ, nhập lại không khớp.
3. Nhập sai mật khẩu hiện tại nhưng mật khẩu mới hợp lệ: giữ hộp thoại, hiện lỗi server.
4. Tắt mạng, gửi: hiện lỗi và cho thử lại khi có mạng.
5. Gửi hợp lệ: nút bị khóa trong lúc chờ, báo thành công rồi đóng hộp thoại.
6. Đăng xuất; mật khẩu cũ không đăng nhập được, mật khẩu mới đăng nhập được.

Lưu ý: đây là đổi mật khẩu khi đang đăng nhập, không phải luồng quên mật khẩu gửi email.
Không thay đổi cơ chế hết hạn/thu hồi JWT của backend trong phần này.
