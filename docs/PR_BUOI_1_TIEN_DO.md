# Title

feat(progress): hoàn thiện lịch sử tập, lịch tháng và chi tiết buổi tập

# Description

## Làm gì trong PR này

Ba màn Tiến độ trước đây chỉ có khung và điều hướng. Nay lấy dữ liệu từ SessionApi qua Repository/ViewModel để xem lịch sử, biểu đồ, lịch tháng và chi tiết đúng buổi tập.

- Danh sách mới nhất trước; lọc Tuần/Tháng/Năm, thống kê và biểu đồ rep.
- Lịch tháng bắt đầu thứ Hai, đánh dấu ngày đã tập, chọn ngày để lọc danh sách.
- Chi tiết có rep đúng/sai, mục tiêu, điểm, thời lượng, kcal, XP và lỗi tư thế; tập lại đúng exerciseId và chia sẻ kết quả.
- Có tải/lỗi/rỗng/thử lại, giữ dữ liệu khi tải lại lỗi, tránh yêu cầu trùng và giữ lựa chọn khi tái tạo Activity.
- Ghi rõ thống kê từ tối đa 50 buổi gần nhất. Không hiển thị dữ liệu giả cho hiệp/góc khớp chưa có trong API.

## Màn hình / phần liên quan

- [x] Phần của Thành viên B — màn 37, 38, 39
- [ ] Phần của Thành viên A
- [ ] Phần của Trưởng nhóm
- [ ] File chung

Đã đồng bộ develop có PR #16. Không sửa navigation, API/model chung, backend hoặc Gradle.

## Kiểm thử

- Build debug và 34 unit test thành công (9 test mới).
- 3 test Android trên API 37 thành công: retry/empty/duplicate requests, missing ID, lọc lịch, điều hướng và Activity recreation.
- lintDebug: 0 lỗi; vẫn còn cảnh báo.
- Đã xem ảnh giao diện ba màn bằng fixture.
- Được phép đọc API demo Azure: danh sách trả 16 buổi; chi tiết đúng ID. Không ghi dữ liệu server. UI tự động dùng fixture; cần người dùng xác nhận checklist test tích hợp.

## Tự kiểm tra trước khi gửi

- [x] Build và test tự động thành công
- [x] Có trạng thái tải/lỗi/rỗng
- [x] Tái tạo Activity giữ lựa chọn
- [ ] Người dùng đã kiểm thử theo docs/BUOI_1_TIEN_DO_VA_CHECKLIST.md
- [ ] Đính kèm ảnh demo/đối chiếu Figma khi gửi PR

Tài liệu chi tiết: `docs/BUOI_1_TIEN_DO_VA_CHECKLIST.md`.
