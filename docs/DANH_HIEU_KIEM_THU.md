# Kiểm thử huy hiệu và hạng tài khoản

1. Mở Hồ sơ → Xem huy hiệu. Tổng EXP của tài khoản xác định hạng Novice (0), Intermediate (200), Advanced (750), Expert (2.000), Master (5.000) hoặc Grandmaster (12.000).
2. Kiểm tra huy hiệu 100/500/1.000 lần lặp và chuỗi 3/7/30 ngày từ hồ sơ. Chuỗi dùng số ngày liên tiếp hiện tại do server trả về.
3. Tại 6.000 EXP tổng và 2.500 EXP hít đất, tài khoản đạt Master và huy hiệu Master Hít đất vẫn khóa. Đúng 5.000 EXP riêng của hít đất thì huy hiệu mở. EXP từng bài lấy từ `GET users/me/achievements`.
4. Chọn Đã đạt hoặc Chưa đạt rồi xoay màn hình. Bộ lọc áp dụng cho cả hai danh sách. Mở bảng mốc hạng để xem hạng hiện tại.
5. Tắt mạng và thử làm mới. Kiểm tra thông báo lỗi, bật mạng rồi thử lại. Nếu server chưa có API danh hiệu, hạng tài khoản và huy hiệu rep/chuỗi vẫn dùng dữ liệu từ `GET users/me`.
