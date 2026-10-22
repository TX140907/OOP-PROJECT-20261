# SANBAKA GAME — TUẦN THI CỦA SVBK
> **Dự án Môn học Lập trình Hướng đối tượng (OOP)**  
> Trò chơi 2D kết hợp cốt truyện (Story Runner) và mô phỏng giao thông trên nền tảng **Java Swing & Java 2D Graphics**.

---

## 1. Giới thiệu tổng quan

**Tuần thi của SVBK** là tựa game 2D kể về hành trình đầy thử thách của một sinh viên Đại học Bách Khoa trong tuần thi học kỳ cam go. Người chơi phải điều khiển phương tiện vượt qua 5 ngày thi với các điều kiện thời tiết khắc nghiệt và chướng ngại vật bất ngờ để đến được giảng đường C1 đúng giờ:

* **Ngày 1 (Trời đẹp):** Lái xe máy trên đường phố quen thuộc, làm quen với cách điều khiển và né tránh ô tô.
* **Ngày 2 (Mưa lớn, đường ngập):** Nước ngập sâu, chuyển sang chèo xuồng máy vượt phố ngập nước.
* **Ngày 3 (Trời rét, đường trơn):** Mặt đường mùa đông trơn trượt, ma sát giảm, đòi hỏi phản xạ khéo léo.
* **Ngày 4 (Nắng nóng gay gắt):** Nhiệt độ cao làm cạn kiệt thanh năng lượng; cần nhặt tia năng lượng trên đường để duy trì thể lực.
* **Ngày 5 (Ngày thi cuối cùng):**
  * *Chặng 1:* Đi xe máy nhưng gặp cảnh tắc đường kẹt cứng.
  * *Chặng 2:* Bạn cùng phòng đón bằng trực thăng khẩn cấp, né tránh đàn chim trên không để kịp giờ thi.
  * *Kết thúc:* Kết thúc có hậu (Happy Ending - HE) với pháo hoa giấy hoặc kết thúc trễ thi (Bad Ending - BE).

---

## 2. Phân chia vai trò & Cơ chế phối hợp (Dev & Art Collaboration)

Dự án được thiết kế theo mô hình **tách biệt hoàn toàn giữa Logic Game và Tài nguyên Đồ họa (Separation of Concerns)**. Điều này giúp **Người làm hình ảnh (Artist / Designer)** và **Người làm game (Game Developer)** có thể làm việc song song, độc lập mà không bao giờ gây xung đột mã nguồn (merge conflict) khi làm việc nhóm trên GitHub.

```
       ┌────────────────────────┐         ┌────────────────────────┐
       │   ARTIST / DESIGNER    │         │     GAME DEVELOPER     │
       │  (Người làm hình ảnh)  │         │    (Người làm game)    │
       └───────────┬────────────┘         └───────────┬────────────┘
                   │                                  │
    Vẽ Sprite PNG, Avatar, Nền          Viết Logic, Physics, State, Render
    Xuất file vào folder assets/        Gọi ArtAssets.load("Folder", "Name.png")
                   │                                  │
                   └─────────────────┬────────────────┘
                                     ▼
                   ┌──────────────────────────────────┐
                   │    BỘ NẠP THÔNG MINH ARTASSETS   │
                   │  - Có ảnh PNG -> Nạp Sprite đẹp  │
                   │  - Thiếu ảnh  -> Tự vẽ vector    │
                   │    (Không bao giờ crash game)    │
                   └──────────────────────────────────┘
```

### 👥 Vai trò 1: Người làm game (Game Developers / Programmers)
* **Trách nhiệm:**
  * Xây dựng kiến trúc hướng đối tượng (OOP), quản lý vòng đời game, state machine, vật lý/hitbox, controller và kịch bản đối thoại.
  * Tách biệt các tầng thành 5 package chức năng rõ ràng: `model`, `state`, `logic`, `render`, `assets`.
  * **Cơ chế Fallback an toàn tuyệt đối:** Toàn bộ thực thể trong `sanbaka.render` đều có sẵn mã vẽ vector 2D (hình học, màu sắc, đổ bóng). Ngay cả khi Artist chưa vẽ xong hoặc file ảnh bị thiếu, game vẫn khởi chạy mượt mà 60 FPS mà không gặp lỗi `NullPointerException`.
  * **Không hardcode đường dẫn:** Lập trình viên chỉ gọi ảnh thông qua lớp cầu nối [`ArtAssets.load("ObjectFolder", "FileName.png")`](src/main/java/sanbaka/assets/ArtAssets.java). Lớp này tự động tìm kiếm trên cả Classpath và thư mục tương đối `assets/`.

### 🎨 Vai trò 2: Người làm hình ảnh (Artists / 2D Designers)
* **Trách nhiệm:**
  * Thiết kế đồ họa cho phương tiện di chuyển, chướng ngại vật giao thông, avatar biểu cảm của nhân vật, phông nền và hiệu ứng.
  * **Không cần chạm vào mã Java:** Người làm hình ảnh chỉ làm việc trong thư mục `assets/` (hoặc `src/main/resources/assets/`), không cần biết code Java hay sửa bất kỳ file logic nào.
  * **Cơ chế Hot-Plug tiện lợi:** Chỉ cần vẽ ảnh PNG nền trong suốt (transparent PNG), đặt đúng tên quy định vào đúng thư mục trong `assets/objects/`, sau đó khởi động lại game là sprite mới sẽ lập tức xuất hiện thay thế hình vẽ vector mẫu.
  * **Đẩy Git an toàn:** Khi commit lên GitHub, Artist chỉ cần commit các file ảnh trong thư mục `assets/`, hoàn toàn không lo sợ đè code hay conflict với Developer.

---

## 3. Cẩm nang dành cho Người làm hình ảnh (Artist's Asset Guide)

### 3.1. Cấu trúc thư mục tài nguyên
Tất cả tài nguyên hình ảnh được phân loại theo thư mục chuẩn như sau:

```text
OOP-PROJECT-20261/
└── assets/
    ├── avatars/               # Ảnh chân dung nhân vật cho khung thoại (Sanbaka.jpg / .png)
    ├── background/            # Ảnh nền menu chính, phòng trọ, đường phố (sanbaka2.jpg)
    ├── fonts/                 # Phông chữ nghệ thuật (1FTV-VIP-Starborn.ttf)
    └── objects/               # Thư mục sprite các đối tượng trong game
        ├── PlayerBike/        # Xe máy của người chơi
        ├── PlayerBoat/        # Xuồng máy người chơi (vượt lũ)
        ├── RoadCar/           # Các dòng xe ô tô NPC
        ├── RoadMotorcycle/    # Xe máy NPC trên đường
        ├── RoadBoat/          # Thuyền NPC trên phố ngập
        ├── EnergyBolt/        # Tia năng lượng sạc pin (Ngày 4)
        └── SchoolBuilding/    # Cổng trường C1 Bách Khoa (vạch đích hoàn thành)
```

### 3.2. Bảng quy chuẩn tên file & Thông số khuyến nghị

> [!TIP]
> **Quy tắc xuất file cho Artist:**
> 1. Định dạng chuẩn: **PNG nền trong suốt (32-bit ARGB)** đối với tất cả Sprite phương tiện và vật phẩm.
> 2. Góc nhìn: **Top-down nghiêng nhẹ (2.5D perspective)**, đầu xe hướng lên trên, đuôi xe hướng xuống dưới theo chiều di chuyển.
> 3. Tỉ lệ: Game tự động co giãn sprite vừa vặn theo khung va chạm (bounding box), nhưng nên vẽ theo kích thước khuyến nghị để hình ảnh sắc nét nhất.

| Thư mục đối tượng | Tên file bắt buộc | Định dạng | Kích thước khuyến nghị | Hướng nhìn & Chi tiết |
| :--- | :--- | :---: | :---: | :--- |
| `assets/objects/PlayerBike/` | `PlayerBike.png` | PNG trong suốt | **60 × 120 px** | Xe máy người chơi nhìn từ phía sau |
| `assets/objects/PlayerBoat/` | `PlayerBoat.png` | PNG trong suốt | **70 × 130 px** | Xuồng máy người chơi vượt dòng nước ngập |
| `assets/objects/RoadCar/` | `Sedan.png`<br>`Hatchback.png`<br>`Taxi.png`<br>`Van.png`<br>`Truck.png`<br>`Bus.png` | PNG trong suốt | Sedan: **70 × 130 px**<br>Van/Truck: **80 × 160 px**<br>Bus: **90 × 200 px** | Xe ô tô cản đường (đuôi xe hướng về người chơi) |
| `assets/objects/RoadMotorcycle/` | `RoadMotorcycle.png` | PNG trong suốt | **50 × 100 px** | Xe máy người đi đường (NPC) |
| `assets/objects/RoadBoat/` | `RoadBoat.png` | PNG trong suốt | **70 × 130 px** | Thuyền người dân đi lại trên phố ngập |
| `assets/objects/EnergyBolt/` | `EnergyBolt.png` | PNG trong suốt | **40 × 60 px** | Biểu tượng tia sét vàng / bình nước tăng lực |
| `assets/objects/SchoolBuilding/` | `SchoolBuilding.png` | PNG trong suốt | **300 × 200 px** | Cổng Parabol trường ĐH Bách Khoa Hà Nội |
| `assets/avatars/` | `Sanbaka.jpg` hoặc `.png` | JPG / PNG | **120 × 120 px** (1:1) | Chân dung nhân vật chính hiển thị cạnh khung thoại |
| `assets/background/` | `sanbaka2.jpg` hoặc `.png`| JPG / PNG | **960 × 640 px** | Hình nền bìa lúc ở Menu Start |
| `assets/fonts/` | `1FTV-VIP-Starborn.ttf` | TTF | TrueType Font | Phông chữ pixel phong cách retro cho tiêu đề |

---

## 4. Cấu trúc mã nguồn theo chức năng (Modular OOP Packages)

Toàn bộ 28 file Java đã được tái cấu trúc và phân loại chuẩn theo 5 nhóm trách nhiệm:

```text
src/main/java/sanbaka/
├── SanbakaGame.java                    # Entry point chính (JFrame 960x640, khởi tạo game loop 60 FPS)
│
├── model/                              # [CHỨC NĂNG 1: DỮ LIỆU & THỰC THỂ OOP]
│   ├── PlayerVehicle.java              # Lớp trừu tượng phương tiện người chơi (tọa độ x, y, tốc độ, bất tử)
│   ├── PlayerBike.java                 # Xe máy người chơi (Ngày 1, 3, 4, 5)
│   ├── PlayerBoat.java                 # Thuyền máy người chơi (Ngày 2 phố ngập)
│   ├── Thing.java                      # Lớp trừu tượng chướng ngại vật & Factory Method Thing.create(kind)
│   ├── RoadCar.java                    # Xe ô tô NPC có bộ nhớ đệm cache sprite
│   ├── RoadMotorcycle.java             # Xe máy NPC
│   ├── RoadBoat.java                   # Thuyền NPC trên phố ngập
│   ├── EnergyBolt.java                 # Tia sét hồi năng lượng ngày nắng nóng
│   ├── SchoolBuilding.java             # Cổng trường C1 Bách Khoa (vạch đích kết thúc ngày)
│   ├── Bird.java                       # Chim cản trở đường bay trực thăng
│   ├── Confetti.java                   # Pháo giấy chúc mừng kết thúc có hậu (HE)
│   ├── Line.java                       # Model đại diện một câu thoại kịch bản
│   ├── Scene.java                      # Model quản lý phân cảnh mở khóa
│   └── MenuButton.java                 # Model nút bấm tương tác trên Menu
│
├── state/                              # [CHỨC NĂNG 2: QUẢN LÝ TRẠNG THÁI & TIẾN TRÌNH]
│   ├── GameState.java                  # Trạng thái phiên chơi, thông số màn hình, timer, HP, energy
│   └── GameMenuState.java              # Trạng thái menu, chọn 11 cảnh, lưu/nạp tiến trình (.sanbaka_svbk_progress.properties)
│
├── logic/                              # [CHỨC NĂNG 3: VẬT LÝ, LUẬT CHƠI & KỊCH BẢN]
│   ├── GameLogic.java                  # Vật lý, va chạm hitbox, sinh xe ngẫu nhiên, cơ chế tắc đường, bàn phím
│   ├── GameStory.java                  # Quản lý dòng thời gian cốt truyện, hiệu ứng typewriter chữ chạy, chuyển cảnh fade
│   └── StoryData.java                  # Toàn bộ 118 dòng thoại chi tiết cho cả 5 ngày thi
│
├── render/                             # [CHỨC NĂNG 4: ĐỒ HỌA & HIỂN THỊ JAVA 2D]
│   ├── GameVisuals.java                # Vẽ vector cơ sở: sấm sét, mây, mưa, trực thăng, avatar dự phòng
│   ├── GameEnvironmentRenderer.java    # Vẽ cảnh nền: phòng trọ, ngập nước mùa lũ, giảng đường C1
│   ├── GameCharacterRenderer.java      # Vẽ giao diện HUD, thanh máu, thanh năng lượng, nhân vật lái xe
│   ├── GameRoadRenderer.java           # Vẽ lòng đường 3 làn xe, hiệu ứng cuộn mặt đường, bóng đổ xe
│   ├── GameMenuRenderer.java           # Vẽ giao diện Start Menu, Chọn cảnh, Hướng dẫn, Pause Menu
│   └── GameScreenRenderer.java         # Điều phối hiển thị Swing (paintComponent) theo từng trạng thái màn hình
│
└── assets/                             # [CHỨC NĂNG 5: QUẢN LÝ TÀI NGUYÊN THÔNG MINH]
    └── ArtAssets.java                  # Bộ nạp ảnh đa tầng (Classpath -> File System) có cache ConcurrentHashMap
```

---

## 5. Hướng dẫn Khởi chạy & Điều khiển trò chơi

### 5.1. Khởi chạy nhanh
* **Cách 1: Nhấp đúp chuột (Dành cho mọi thành viên)**
  Vào thư mục `OOP-PROJECT-20261` và **nhấp đúp chuột** vào file:
  👉 [`run.cmd`](run.cmd)
* **Cách 2: Chạy bằng PowerShell**
  ```powershell
  .\run.ps1
  ```
* **Cách 3: Chạy bằng Command Prompt (CMD)**
  ```cmd
  run.cmd
  ```
* **Cách 4: Chỉ biên dịch kiểm tra lỗi mà không mở game**
  ```powershell
  .\run.ps1 -BuildOnly
  ```

### 5.2. Chạy từ IDE (IntelliJ IDEA / VS Code / Eclipse)
1. Mở thư mục `OOP-PROJECT-20261` trong IDE.
2. Cấu hình thư mục:
   * `src/main/java`: **Sources Root**
   * `src/main/resources`: **Resources Root**
   * `src/test/java`: **Test Sources Root**
3. Chạy file [`src/main/java/sanbaka/SanbakaGame.java`](src/main/java/sanbaka/SanbakaGame.java) hoặc `main.java`.

### 5.3. Bảng phím điều khiển khi chơi (Controls)

| Phím điều khiển | Chức năng trong game |
| :--- | :--- |
| **Chuột trái** | Tương tác các nút bấm trên Menu (Bắt đầu, Chọn cảnh, Hướng dẫn, Âm lượng) |
| **Phím Space (Cách) / Enter** | Đọc tiếp hội thoại (Bấm lần 1 để hiện trọn vẹn văn bản gõ máy, lần 2 để qua câu tiếp) |
| **A / D** hoặc **← / →** | Đổi làn đường sang trái / sang phải né tránh chướng ngại vật |
| **W / S** hoặc **↑ / ↓** | Tăng tốc / Phanh giảm tốc (ở chặng bay trực thăng: bay lên / bay xuống) |
| **Phím Esc hoặc P** | Tạm dừng trò chơi (Pause) và mở Menu tùy chọn |

---

## 6. Bộ Kiểm thử Tự động (Automated Smoke Test)

Dự án có sẵn bộ test tự động [`SanbakaSmokeTest.java`](src/test/java/sanbaka/SanbakaSmokeTest.java) để cả Dev và Artist có thể chạy kiểm tra xem việc thêm hình ảnh hoặc chỉnh sửa logic có làm hỏng game hay không:

```powershell
javac -encoding UTF-8 -cp "build/classes" -d "build/classes" src/test/java/sanbaka/SanbakaSmokeTest.java
java -cp "build/classes;src/main/resources;assets;." sanbaka.SanbakaSmokeTest
```

*Các tiêu chí được kiểm định tự động:*
* [x] **Story Integrity:** Kiểm tra đủ 118 dòng thoại của 5 ngày thi trong `StoryData`.
* [x] **Scene Linking:** Kiểm tra tính toàn vẹn của 11 phân đoạn kịch bản mở khóa.
* [x] **Asset Fallback:** Kiểm tra bộ nạp `ArtAssets` tự động fallback mượt mà khi thiếu file ảnh.
* [x] **Factory Pattern:** Kiểm tra khởi tạo đa hình các loại xe cộ và chướng ngại vật qua `Thing.create()`.

