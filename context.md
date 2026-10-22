# CONTEXT DỰ ÁN & BÁO CÁO REFACTOR — SANBAKA GAME

Tài liệu này lưu trữ toàn bộ bối cảnh dự án, hiện trạng trước và sau refactor, cấu trúc kiến trúc mã nguồn và định hướng phát triển tiếp theo cho nhóm.

---

## 1. Bối cảnh ban đầu & Lý do Refactor

### Tình trạng trước khi refactor:
* **Cấu trúc bị phân mảnh:** Thư mục gốc trên GitHub chứa mã nguồn thử nghiệm cũ trong `src/main/java/game/` (chia theo `DayConfig`, `Levels`, chưa đủ tính năng).
* **Bị copy-paste thô:** Thành viên trong nhóm phát triển tính năng mới trong thư mục `SanbakaGameArchitecture_FeatureUpdate` (chứa toàn bộ cốt truyện 5 ngày, menu game, hệ thống lưu tiến trình, renderer) nhưng thay vì tích hợp vào repo thì lại **copy nguyên cả thư mục `SanbakaGameArchitecture_FeatureUpdate` thả vào thư mục gốc**.
* **Tồn tại thư mục legacy:** Thư mục `SanbakaGameArchitecture` (bản cũ hơn trước khi có menu) vẫn nằm song song gây rối rắm.
* **Lỗi cấu trúc Java Package:** Trong `SanbakaGameArchitecture_FeatureUpdate`, các file entity bị chia thành nhiều folder con (`objects/PlayerBike/PlayerBike.java`, `objects/RoadCar/RoadCar.java`,...) nhưng bên trong đều khai báo `package sanbaka;`. Điều này vi phạm quy ước thư mục của Java, khiến IDE báo lỗi package mismatch.
* **Mã nguồn bị gom một cục (Monolithic inheritance chain):** Toàn bộ các lớp từ trạng thái, logic, kịch bản, đến vẽ đồ họa đều dồn chung vào một package duy nhất với một chuỗi kế thừa kéo dài 11 cấp (`GameState` -> `GameMenuState` -> `GameStory` -> `GameLogic` -> `GameVisuals` -> `GameEnvironmentRenderer` -> `GameCharacterRenderer` -> `GameRoadRenderer` -> `GameMenuRenderer` -> `GameScreenRenderer` -> `SanbakaGame`), không phân chia thư mục theo chức năng (Separation of Concerns).
* **Hardcode đường dẫn môi trường:** File script `run.ps1` cũ bị gắn cứng đường dẫn `C:\Program Files\Java\jdk-27\bin`, gây lỗi khi chạy trên máy tính cài các bản JDK khác (như JDK 17, 21, 25).
* **Rác biên dịch:** Hàng chục file `.class` biên dịch cũ nằm rải rác ngay bên trong thư mục nguồn `src/main/java`.

---

## 2. Quá trình Refactor theo chức năng (Giai đoạn hoàn thiện)

Dự án đã được phân tách và tái cấu trúc thành **5 nhóm chức năng (Packages) chuyên biệt**, tuân thủ nguyên tắc thiết kế hướng đối tượng (OOP):

### 2.1. Nhóm `sanbaka.model` (Thực thể & Dữ liệu)
Chứa toàn bộ các lớp mô hình dữ liệu, phương tiện người chơi, chướng ngại vật giao thông, kịch bản:
* **Phương tiện người chơi:**
  * `PlayerVehicle.java`: Lớp trừu tượng định nghĩa vị trí `(x, y)`, độ nghiêng và trạng thái bất tử.
  * `PlayerBike.java`: Xe máy của người chơi (dùng cho Ngày 1, 3, 4, 5).
  * `PlayerBoat.java`: Thuyền máy của người chơi (dùng cho Ngày 2 vượt ngập lụt).
* **Vật cản giao thông (Factory Method Pattern):**
  * `Thing.java`: Lớp trừu tượng đại diện cho mọi chướng ngại vật trên đường. Chứa static factory `Thing.create(kind)`.
  * `RoadCar.java`: Các loại xe ô tô (Sedan, Hatchback, Taxi, Van, Truck, Bus) có cơ chế cache sprite tối ưu.
  * `RoadMotorcycle.java`: Xe máy NPC.
  * `RoadBoat.java`: Thuyền NPC trên phố ngập.
  * `EnergyBolt.java`: Vật phẩm tia sét hồi phục năng lượng cho Ngày 4 nắng nóng.
  * `SchoolBuilding.java`: Cổng trường C1 Bách Khoa (vạch đích hoàn thành chặng đường).
* **Thực thể hỗ trợ & Kịch bản:**
  * `Bird.java`: Chim chướng ngại vật trên không ở chặng bay trực thăng.
  * `Confetti.java`: Hạt pháo giấy chúc mừng kết thúc có hậu (HE).
  * `Line.java`: Một câu thoại trong kịch bản.
  * `Scene.java`: *(Tách từ inner class của GameMenuState)* Model cảnh phân đoạn mở khóa.
  * `MenuButton.java`: *(Tách từ inner class `Btn` của GameMenuState)* Model nút bấm giao diện menu.

### 2.2. Nhóm `sanbaka.state` (Trạng thái trò chơi)
Quản lý toàn bộ vòng đời trạng thái, bộ đếm thời gian, thanh máu/năng lượng và lưu trữ tiến trình:
* `GameState.java`: Lớp cơ sở chứa thông số màn hình (960×640), tốc độ người chơi, timer (16ms ~ 60 FPS), quản lý typewriter hiệu ứng chữ chạy.
* `GameMenuState.java`: Quản lý 11 phân cảnh mở khóa, menu chọn cảnh (Scene Select), màn hình tạm dừng (Pause), nạp/ghi tệp tiến trình `.sanbaka_svbk_progress.properties`.

### 2.3. Nhóm `sanbaka.logic` (Luật chơi, Vật lý & Cốt truyện)
Điều phối các quy tắc gameplay và tiến trình hội thoại:
* `GameLogic.java`: Cập nhật chuyển động xe cộ, gia tốc, né làn, va chạm vật lý (hitbox), tụt năng lượng do nắng nóng, cơ chế tắc đường (Traffic Jam) và điều khiển phím (`keyPressed`, `keyReleased`).
* `GameStory.java`: Quản lý timeline hội thoại, cuộc gọi điện thoại (`isCallLine`), chuyển cảnh mờ (Fade), thông báo phân cảnh (Card).
* `StoryData.java`: Toàn bộ kịch bản và 118 câu thoại chi tiết cho cả 5 ngày thi.

### 2.4. Nhóm `sanbaka.render` (Tầng Hiển thị & Đồ họa)
Phân chia các lớp vẽ chuyên biệt bằng Java 2D:
* `GameVisuals.java`: Các hàm vẽ hiệu ứng đồ họa cơ sở: tia sét, trái tim, ngôi sao, đám mây, vệt nước rẽ sóng, máy bay trực thăng, avatar nhân vật (hỗ trợ nạp ảnh PNG hoặc tự vẽ vector).
* `GameEnvironmentRenderer.java`: Vẽ cảnh nền: phòng trọ, hiệu ứng ngập nước mưa rơi, cảnh đồi núi bầu trời chặng trực thăng.
* `GameCharacterRenderer.java`: Vẽ giao diện HUD, thanh máu, thanh năng lượng và người lái xe máy.
* `GameRoadRenderer.java`: Vẽ lòng đường 3 làn, hiệu ứng cuộn mặt đường, ghép các phương tiện và bóng đổ.
* `GameMenuRenderer.java`: Vẽ Menu khởi động, trang Chọn cảnh, trang Hướng dẫn và Menu tạm dừng.
* `GameScreenRenderer.java`: Lớp điều phối cấp cao nhất (chứa `paintComponent`), điều hướng render tới màn hình tương ứng (TITLE, DIALOG, ROAD, ROOM, EXAM, HE, BE, AIR, CARD, FADE).

### 2.5. Nhóm `sanbaka.assets` (Quản lý Tài nguyên)
* `ArtAssets.java`: Nạp ảnh PNG thông minh, hỗ trợ đa tầng: ưu tiên đọc từ classpath (`/assets/...`, `/image/...`) và fallback ra hệ thống tệp đĩa cứng. Có bộ đệm `ConcurrentHashMap` tránh đọc lại tệp nhiều lần.

### 2.6. Nhóm ứng dụng `sanbaka` (Entry Point)
* `SanbakaGame.java`: Cửa sổ `JFrame` chính (960×640 px), thiết lập Event Listener, tải phông chữ Starborn và ảnh nền `sanbaka2.jpg`.
* `main.java`: Lớp bọc ở default package để khởi chạy nhanh qua lệnh `java main`.

---

## 3. Bản đồ cấu trúc thư mục sau Refactor

```text
OOP-PROJECT-20261/
├── assets/                                      # Tài nguyên gốc dự án
│   ├── avatars/ (Sanbaka.jpg)
│   ├── background/ (sanbaka2.jpg)
│   ├── fonts/ (1FTV-VIP-Starborn.ttf)
│   └── objects/ (ART_README.md cho các đối tượng)
├── build/classes/                               # Bytecode (.class) được phân package chuẩn
│   └── sanbaka/
│       ├── assets/
│       ├── logic/
│       ├── model/
│       ├── render/
│       └── state/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── main.java                        # Default package launcher
│   │   │   └── sanbaka/                         # Package chính
│   │   │       ├── SanbakaGame.java             # Entry point
│   │   │       ├── assets/                      # [CHỨC NĂNG 1] Nạp tài nguyên
│   │   │       │   └── ArtAssets.java
│   │   │       ├── logic/                       # [CHỨC NĂNG 2] Luật chơi & Kịch bản
│   │   │       │   ├── GameLogic.java
│   │   │       │   ├── GameStory.java
│   │   │       │   └── StoryData.java
│   │   │       ├── model/                       # [CHỨC NĂNG 3] Dữ liệu & Thực thể
│   │   │       │   ├── Bird.java
│   │   │       │   ├── Confetti.java
│   │   │       │   ├── EnergyBolt.java
│   │   │       │   ├── Line.java
│   │   │       │   ├── MenuButton.java          *(tách từ inner class)*
│   │   │       │   ├── PlayerBike.java
│   │   │       │   ├── PlayerBoat.java
│   │   │       │   ├── PlayerVehicle.java
│   │   │       │   ├── RoadBoat.java
│   │   │       │   ├── RoadCar.java
│   │   │       │   ├── RoadMotorcycle.java
│   │   │       │   ├── Scene.java               *(tách từ inner class)*
│   │   │       │   ├── SchoolBuilding.java
│   │   │       │   └── Thing.java
│   │   │       ├── render/                      # [CHỨC NĂNG 4] Đồ họa & Giao diện
│   │   │       │   ├── GameCharacterRenderer.java
│   │   │       │   ├── GameEnvironmentRenderer.java
│   │   │       │   ├── GameMenuRenderer.java
│   │   │       │   ├── GameRoadRenderer.java
│   │   │       │   ├── GameScreenRenderer.java
│   │   │       │   └── GameVisuals.java
│   │   │       └── state/                       # [CHỨC NĂNG 5] Trạng thái & Tiến trình
│   │   │           ├── GameMenuState.java
│   │   │           └── GameState.java
│   │   └── resources/
│   │       └── assets/                          # Tài nguyên đóng gói classpath
│   └── test/
│       └── java/
│           └── sanbaka/
│               └── SanbakaSmokeTest.java        # Unit test tự động kiểm thử toàn diện
├── .gitignore
├── context.md                                   # Tài liệu bối cảnh & kiến trúc
├── README.md                                    # Hướng dẫn sử dụng & phát triển
├── run.cmd                                      # Script Windows CMD
├── run.ps1                                      # Script Windows PowerShell
└── Story.docx                                   # Kịch bản kịch bản gốc
```

---

## 4. Hướng dẫn Khởi chạy & Kiểm thử

* **Khởi chạy trò chơi:**
  ```cmd
  .\run.cmd
  ```
  hoặc với PowerShell:
  ```powershell
  .\run.ps1
  ```
* **Chỉ biên dịch kiểm tra lỗi:**
  ```cmd
  .\run.cmd --build-only
  ```
* **Chạy bộ kiểm thử tự động (Smoke Test):**
  ```powershell
  javac -encoding UTF-8 -cp "build/classes" -d "build/classes" src/test/java/sanbaka/SanbakaSmokeTest.java
  java -cp "build/classes;src/main/resources;assets;." sanbaka.SanbakaSmokeTest
  ```
  *Kết quả test:* Đạt 100% các bài test về StoryData (118 câu thoại), SceneBuilding (11 phân cảnh), ArtAssets fallback và khởi tạo thực thể qua Factory Method.

---

## 5. Định hướng phát triển tiếp theo (Roadmap)
1. **Ngày 2 (Cá mập):** Hiện thực hóa cơ chế cá mập tele/trồi sụt trên đường ngập lụt.
2. **Ngày 4 (Chó chạy rông):** Thêm chướng ngại vật động bất ngờ băng qua đường.
3. **Ngày 5 (Boss trực thăng):** Màn đánh Boss trên không ở 1/3 chặng cuối của ngày thi cuối.
4. **Hội thoại:** Bổ sung sprite cảm xúc cho nhân vật tương ứng với các phân đoạn đối thoại.
