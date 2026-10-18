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

Các file gameplay, truyện và renderer cảnh vẫn nằm trong src/main/java/sanbaka/: GameLogic.java xử lý luật chơi; GameRoadRenderer.java ghép cảnh đường; GameEnvironmentRenderer.java giữ nền đường, khuôn viên và phòng trọ; GameScreenRenderer.java vẽ giao diện.

## Chọn ngày bắt đầu test

Sửa TEST_START_DAY trong src/main/java/sanbaka/GameState.java thành số từ 1 đến 5. GameStory.begin() đọc giá trị này để tìm ngày bắt đầu. Không sửa biến day.

## Chạy

Mở terminal tại thư mục project rồi chạy:

    $sources = Get-ChildItem .\src\main\java\sanbaka -Recurse -Filter *.java |
        ForEach-Object FullName
    New-Item -ItemType Directory -Force .\out | Out-Null
    javac -encoding UTF-8 -d .\out $sources
    if ($LASTEXITCODE -eq 0) {
        java -cp .\out sanbaka.SanbakaGame
    }
## Chi tiết cấu trúc:
Khởi chạy, trạng thái và truyện
- SanbakaGame.java — Điểm bắt đầu chương trình. Tạo cửa sổ game, khởi tạo nội dung truyện và nạp ảnh nền sanbaka2.jpg. Nếu không thấy ảnh, game dùng nền mặc định.
- GameState.java — Trạng thái chung: màn hình hiện tại, ngày, HP, điểm, vị trí người chơi, danh sách vật thể, trạng thái phím và thông số game. Biến TEST_START_DAY ở đây, nhận giá trị từ 1 đến 5.
- GameStory.java — Điều khiển truyện và hội thoại. Tìm vị trí bắt đầu của ngày, bắt đầu game theo TEST_START_DAY, chuyển giữa phòng trọ và các đoạn hội thoại.
- StoryData.java — Nội dung truyện và hội thoại.
- GameLogic.java — Luật chơi: di chuyển, va chạm, sinh xe và vật phẩm, tạo tắc đường, chuyển cảnh, chặng trực thăng và hiệu ứng kết thúc.
Vẽ cảnh và giao diện
- GameVisuals.java — Các hình và hàm vẽ dùng chung như tia sét, trái tim, avatar, chim, trực thăng, mây, vệt nước và hình đa giác.
- GameEnvironmentRenderer.java — Vẽ cảnh nền: nước ngập, cửa sổ nhìn từ phòng, đèn đường, cây, nhà, khuôn viên trường và bầu trời.
- GameCharacterRenderer.java — Vẽ HUD và nhân vật phụ đi xe máy.
- GameRoadRenderer.java — Vẽ và ghép cảnh đường: mặt đường, vỉa hè, cảnh nền, vật thể, phương tiện người chơi, thời tiết và HUD.
- GameScreenRenderer.java — Vẽ các màn hình như tiêu đề, giới thiệu, phòng trọ, hội thoại, thi cử, kết thúc và giao diện điện thoại.
Các đối tượng
- PlayerVehicle/PlayerVehicle.java — Lớp cơ sở, giữ vị trí, độ nghiêng và thời gian bất tử tạm thời của phương tiện người chơi.
- PlayerBike/PlayerBike.java — Xe máy người chơi và phần vẽ xe. Ảnh PNG thay thế có tên PlayerBike.png.
- PlayerBoat/PlayerBoat.java — Thuyền người chơi và phần vẽ thuyền. Ảnh thay thế có tên PlayerBoat.png.
- Thing/Thing.java — Dữ liệu chung và factory chọn loại vật thể trên đường.
- RoadCar/RoadCar.java — Ô tô, hatchback, taxi, van, xe tải và xe buýt. Các ảnh thay thế lần lượt là Sedan.png, Hatchback.png, Taxi.png, Van.png, Truck.png, Bus.png.
- RoadMotorcycle/RoadMotorcycle.java — Xe máy NPC. Ảnh thay thế có tên RoadMotorcycle.png.
- RoadBoat/RoadBoat.java — Thuyền NPC. Ảnh thay thế có tên RoadBoat.png.
- EnergyBolt/EnergyBolt.java — Vật phẩm tia năng lượng. Ảnh thay thế có tên EnergyBolt.png.
- SchoolBuilding/SchoolBuilding.java — Mặt tiền trường học. Ảnh thay thế có tên SchoolBuilding.png.
Ảnh PNG được đặt trong thư mục tương ứng, cạnh file Java. Nếu ảnh chưa có hoặc không đọc được, game tiếp tục dùng hình vẽ Java. Mỗi thư mục đối tượng có file ART_README.md ghi tên ảnh được nhận.
Dữ liệu hiệu ứng và ảnh nền
- Line.java — Người nói và nội dung của một dòng hội thoại.
- Bird.java — Trạng thái chim trong chặng trực thăng; hình chim được vẽ trong GameVisuals.java.
- Confetti.java — Trạng thái của một hạt pháo giấy; phần cập nhật và vẽ hiệu ứng nằm trong GameLogic.java và GameVisuals.java.
- ArtAssets.java — Tìm và lưu cache ảnh PNG của từng đối tượng. Nếu không tìm thấy hoặc không đọc được ảnh, phần vẽ Java sẽ được dùng.
- sanbaka2.jpg — Ảnh nền hội thoại.

## Hướng phát triển tiếp
Đây vẫn là 1 game chưa hoàn thiện (vì vibe code), theo tôi thì cần thêm 1 số chức năng nữa:
- Menu game: phần này chatgpt vẽ khá xấu, cần thiết kế lại 1 menu bắt đầu game, theo tôi thì vẫn lấy hình nền chat và cho thêm các chức năng (nút ấn):
  +) Vào chơi: Khi vào chơi thì, nếu người chơi chưa thắng game thì sẽ cho bắt đầu từ ngày chưa mở khóa, còn đã thắng game thì cho chơi lại từ đầu
  +) Menu: hiện ra list các cảnh mà người chơi đã mở khóa
  +) Hướng dẫn chơi game: Các nút mũi tên; AWDS để di chuyển, Esc để pause game...
- Màn hình pause: hiện các nút tương tự ở trên menu game
- Nút pause khi đang chơi trên đường
- Cá mập tele ở ngày số 2, chó chạy ra đường ở ngày thứ 4 hoặc 5 (hoặc cả 2)
- (Cái này là ý tưởng của bạn Thắng, tôi tháy khá hay) Ngày số 5 đoạn lái trực thăng sẽ cho đánh boss, có thể bắn đạn vào boss và né đạn từ boss
- Cuối cùng, theo tôi thì ở những đoạn hội thoại, thay vì chỉ dùng mỗi ảnh nền của tôi thì tôi nghĩ với mỗi câu thoại sẽ thêm art nhân vật cùng biểu cảm khi nói câu thoại (cái này có thể vibe ra art với các biểu cảm khác nhau, art gốc thì có thể tìm sau)
