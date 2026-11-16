# Title

feat(b): hoàn thiện cài đặt, huy hiệu và cộng đồng cho bản giữa kỳ

# Description

Các màn B còn khung giao diện nay có dữ liệu và thao tác theo phân công giữa kỳ:

- Lưu cài đặt hồ sơ/giọng nói; chọn và nghe thử giọng tiếng Việt thực tế trên máy, có trạng thái thiếu giọng/lỗi. Không khẳng định giọng nữ miền Nam khi engine không cung cấp bằng chứng.
- Tính 6 huy hiệu từ hồ sơ API, hiển thị tiến độ, lọc đã/chưa đạt, xử lý tải/lỗi/rỗng. Bổ sung dải huy hiệu và phân loại BMI tham khảo cho người từ 20 tuổi trên Hồ sơ.
- Hoàn thiện thử thách, tạo thử thách, tìm/mời bạn, bảng xếp hạng và thông báo bằng dữ liệu mẫu qua Repository/ViewModel. Giữ lựa chọn khi xoay màn hình; không mời trùng; đọc thông báo không tự từ chối lời mời.
- Chuẩn hoá ngày lịch sử/biểu đồ/lịch tháng theo Asia/Ho_Chi_Minh để khớp server.
- Thêm kiểm thử và checklist bàn giao trong docs/BUOI_2_KIEM_THU_VA_BAN_GIAO.md.

## Validation

- Build debug và APK kiểm thử thành công.
- 40 unit test + 18 test Android API 37 đạt, có hồi quy các luồng B trước đó.
- TTS thật tạo được âm thanh với vi-VN-language; chưa xác nhận giới tính/vùng miền.
- Lint: 0 lỗi, 279 cảnh báo toàn dự án.
- API demo Azure: chỉ đọc hồ sơ, 16 buổi tập và chi tiết thành công; không sửa dữ liệu demo.
- Đã kiểm tra ảnh màn hình và mô phỏng tích hợp nhánh liên quan.
- [ ] Người dùng hoàn thành checklist trên máy thật trước khi mở PR.

## Giới hạn và tích hợp

Cộng đồng là mock trong bộ nhớ theo yêu cầu giữa kỳ, không gửi lời mời thật/không cộng XP và mất khi đóng tiến trình hoặc đăng xuất. Cài đặt giờ nhắc chỉ lưu cấu hình, chưa có bộ lập lịch thông báo nền. Module AI chưa được tích hợp thêm trong PR này. Thống kê chỉ tính trong tối đa 50 buổi API trả về.

PR này kế tiếp #17. Nên merge #17 trước rồi đặt base develop, head codex/b-settings-social để tránh hiển thị lặp phần Lịch sử. Thay đổi file chung chỉ gồm queries TTS_SERVICE trong AndroidManifest, cần trưởng nhóm chú ý khi review. Không nâng Gradle/AGP hoặc sửa API/backend.
