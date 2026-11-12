# PNG nghệ thuật — Boss (Trùm Cuối Màn 5)

Đặt các file ảnh PNG vào thư mục này (`assets/objects/Boss/`). Game tự động chuyển đổi trạng thái theo hành động thực tế của Boss trong trận đánh (và tự động lùi về `Boss_idle1.png` hoặc `Boss.png` nếu thiếu các frame phụ):

Các tên file được nhận diện tự động:
- `Boss_idle1.png` (hoặc `Boss.png`) — Bay lơ lửng nhịp 1 (Tư thế chuẩn)
- `Boss_idle2.png` — Bay lơ lửng nhịp 2 (Áo choàng tung cao & cầu lửa sáng hơn, luân phiên với idle1 mỗi 0.22s)
- `Boss_charge.png` — Gồng tụ năng lượng / nạp đầy thanh máu đầu trận (`STATE_CHARGING`)
- `Boss_attack_aim.png` — Giơ thẳng tay ngắm tia Laser đỏ & bắn tỉa (`STATE_ATTACK1`)
- `Boss_attack_spray.png` — Vung tay xả mưa đạn & triệu hồi phi đội ném bom (`STATE_TRANSITION_SPRAY`, `STATE_ATTACK3`)
- `Boss_hit.png` — Lảo đảo nhẹ khi trúng tên lửa của người chơi hoặc lúc bị đánh bại

Kích thước chuẩn (1x): `130 × 176 px` (Đã được phóng to ~1.85x để tương xứng với kích thước trực thăng và máy bay tiêm kích).
Hitbox tự động co dãn theo kích thước của Boss (`w * 0.42` ngang và `h * 0.45` dọc).
Tất cả ảnh `840 × 1140 px` bạn nạp vào đều được GPU co dãn mượt mà và hiển thị sắc nét.
Xem hướng dẫn chi tiết và bộ Prompt AI tại file `boss.md` ở thư mục gốc của dự án.
