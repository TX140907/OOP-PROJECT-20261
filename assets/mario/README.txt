=== HƯỚNG DẪN THAY THẾ SPRITE & NHÂN VẬT MARIO 8-BIT ===

Game đã tích hợp sẵn toàn bộ hình ảnh pixel-art 8-bit chuẩn màu NES nguyên bản trong bộ nhớ.
Nếu bạn muốn THAY THẾ NHÂN VẬT hoặc BẤT KỲ VẬT THỂ NÀO, bạn chỉ cần thả file ảnh PNG (nền trong suốt) vào các thư mục tương ứng:

1. THAY THẾ NHÂN VẬT CHÍNH (assets/mario/player/):
   - idle.png    : Dáng đứng nhìn sang phải
   - walk_1.png  : Bước chạy 1
   - walk_2.png  : Bước chạy 2
   - walk_3.png  : Bước chạy 3
   - jump.png    : Dáng nhảy vung tay lên cao
   - skid.png    : Dáng xoay người trượt phanh khi quay đầu gấp
   - die.png     : Dáng văng lên khi mất mạng
   - flag.png    : Dáng bám cờ trượt xuống

2. THAY THẾ KẺ ĐỊCH (assets/mario/enemies/):
   - goomba_1.png     : Quái bước chân 1
   - goomba_2.png     : Quái bước chân 2
   - goomba_dead.png  : Quái bị dẫm bẹp dí

3. THAY THẾ GẠCH & ĐỊA HÌNH (assets/mario/tiles/):
   - ground.png            : Khối đất nền
   - brick.png             : Khối gạch nâu
   - question_1.png        : Khối hỏi chấm (?)
   - question_used.png     : Khối sắt rỗng khi đã húc hết xu
   - pipe_top_left.png     : Miệng ống cống bên trái
   - pipe_top_right.png    : Miệng ống cống bên phải
   - pipe_body_left.png    : Thân ống cống bên trái
   - pipe_body_right.png   : Thân ống cống bên phải

* Lưu ý: Khi nạp ảnh của bạn vào, hệ thống sẽ tự động hiển thị đè ảnh mới mà không cần chỉnh sửa bất kỳ dòng code nào!

