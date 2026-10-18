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
