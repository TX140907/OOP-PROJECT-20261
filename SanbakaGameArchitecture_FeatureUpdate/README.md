# SanbakaGame — source project

## Đối tượng và thư mục code

Mỗi đối tượng có thư mục riêng trong src/main/java/sanbaka/objects/, tên thư mục trùng tên class. File Java và ảnh PNG tùy chọn nằm chung trong thư mục đó:

    src/main/java/sanbaka/objects/
    ├── PlayerVehicle/PlayerVehicle.java
    ├── PlayerBike/PlayerBike.java
    ├── PlayerBoat/PlayerBoat.java
    ├── Thing/Thing.java
    ├── RoadCar/RoadCar.java
    ├── RoadMotorcycle/RoadMotorcycle.java
    ├── RoadBoat/RoadBoat.java
    ├── EnergyBolt/EnergyBolt.java
    └── SchoolBuilding/SchoolBuilding.java

Các class vẫn khai báo package sanbaka để giữ nguyên kết nối với gameplay hiện tại. Vì vậy hãy biên dịch toàn bộ source đệ quy bằng lệnh bên dưới, thay vì chỉ biên dịch file đang mở.

## Thêm ảnh PNG

Đặt PNG ngay trong thư mục đối tượng tương ứng, cạnh file Java. Ví dụ:

    src/main/java/sanbaka/objects/PlayerBike/
    ├── PlayerBike.java
    ├── PlayerBike.png
    └── ART_README.md

Game tự dùng ảnh PNG nếu tên đúng; nếu chưa có ảnh thì phần vẽ Java tiếp tục chạy. Các file ART_README.md trong từng thư mục liệt kê tên ảnh được nhận. Sau khi thêm hoặc thay ảnh, khởi động lại game để nạp lại cache.

Font tiêu đề tùy chỉnh nằm trong assets/fonts/1FTV-VIP-Starborn.ttf; nếu không nạp được thì game tự dùng SansSerif.

Các file gameplay, truyện và renderer cảnh vẫn nằm trong src/main/java/sanbaka/: GameLogic.java xử lý luật chơi; GameRoadRenderer.java ghép cảnh đường; GameEnvironmentRenderer.java giữ nền đường, khuôn viên và phòng trọ. GameMenuState.java quản lý menu, tạm dừng, chọn cảnh và tiến trình; GameMenuRenderer.java vẽ các màn hình đó; GameScreenRenderer.java ghép các màn hình game.

## Menu, lưu tiến trình và chọn cảnh

Menu hỗ trợ vào game, xem giới thiệu nhân vật, hướng dẫn, tạm dừng và chơi lại các cảnh đã mở khóa. Tiến trình lưu trong file `.sanbaka_svbk_progress.properties` ở thư mục người dùng Windows. Các cảnh mới được mở khóa sau khi vào phòng trọ, hoàn thành chặng đường và vượt đoạn tắc đường.

## Chọn cảnh để test

Trong `GameMenuState.java`, đặt `UNLOCK_ALL = true` để mở khóa toàn bộ cảnh trong mục Menu khi cần test. Game vẫn luôn khởi động ở màn hình chính. Đặt `UNLOCK_ALL = false` để dùng tiến trình mở khóa bình thường. “Vào chơi” khi bắt đầu lượt mới sẽ luôn chạy từ phần mở đầu.

## Chạy

Mở terminal VS Code tại thư mục gốc dự án, rồi chạy:

    .\run.ps1

Script tự tìm toàn bộ file Java, biên dịch bằng JDK 27 vào thư mục out, thử bật OpenGL (có in trạng thái khởi tạo) và in FPS/thời gian vẽ-cập nhật mỗi giây để chẩn đoán. Nếu PowerShell chặn script theo execution policy, chạy:

    powershell -ExecutionPolicy Bypass -File .\run.ps1

## Hướng phát triển tiếp
Tôi đã xử lý xong menu game, màn hình bắt đầu cùng 1 số tính năng khác, cùng với tối ưu đồ họa hơn so với bản trước đó. Bây giờ game chỉ còn cần thêm những tính năng sau:
- Cá mập tele ở ngày số 2, chó chạy ra đường ở ngày thứ 4 hoặc 5 (hoặc cả 2)
- (Cái này là ý tưởng của bạn Thắng, tôi tháy khá hay) Ngày số 5 đoạn lái trực thăng sẽ cho đánh boss, có thể bắn đạn vào boss và né đạn từ boss
- Cuối cùng, theo tôi thì ở những đoạn hội thoại, thay vì chỉ dùng mỗi ảnh nền của tôi thì tôi nghĩ với mỗi câu thoại sẽ thêm art nhân vật cùng biểu cảm khi nói câu thoại (cái này có thể vibe ra art với các biểu cảm khác nhau, art gốc thì có thể tìm sau)
