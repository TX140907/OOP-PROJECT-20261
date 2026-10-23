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
  * *Chặng 2:* Bạn cùng phòng đón bằng trực thăng khẩn cấp (máu trực thăng tăng lên 150 HP), né tránh đàn chim xuất hiện bao quát toàn bộ không phận và nghênh chiến Trùm Cuối trên không để kịp giờ thi.
  * *Kết thúc:* Kết thúc có hậu (Happy Ending - HE) với pháo hoa giấy hoặc kết thúc trễ thi (Bad Ending - BE).
* **Quy tắc Vật lý & Cân bằng Gameplay cốt lõi:**
  * **Quãng đường cố định (~40 giây), Tách biệt Môi trường & Bỏ Bộ đếm Thời gian:** Nếu đi thẳng thuận lợi (không dính hiệu ứng làm chậm), hành trình kéo dài đúng 40 giây. **Nếu và chỉ nếu** người chơi dính hiệu ứng làm chậm (va chạm đẩy lùi, băng lạnh ở Màn 3, kiệt sức ở Màn 4, cạ lề ở Màn 2), tiến trình hoàn thành màn chơi sẽ thực sự chậm lại tương ứng. Giao diện HUD **loại bỏ hoàn toàn bộ đếm thời gian còn lại** (`THỜI GIAN  %02d:%02d`) ở tất cả các màn để giao diện tinh giản, thoáng đãng; thanh tiến trình quãng đường "ĐƯỜNG ĐI" / "ĐƯỜNG BAY" góc trên phải hiển thị trực quan mức độ hoàn thành chặng đường. **Đặc biệt:** Tốc độ cuộn mặt đường, dòng xe cộ giao thông xung quanh, mây trời và đàn chim **không bị chậm theo người chơi** (giữ nguyên nhịp trôi liên tục tự nhiên, không rơi vào tình trạng "bullet-time" cả thế giới cùng chậm lại).
  * **Cơ chế Lề đường phân hóa theo màn:**
    * *Màn 2 (Phố ngập):* Không có hiệu ứng bốc lửa và không mất máu. Khi chèo thuyền ép sát vỉa hè, thuyền sẽ bị bùn cạn làm tốc độ chậm dần về 50% (`map2CurbSlowFactor -> 0.50`) kèm hiệu ứng bọt nước té vỉa hè (`drawBoatCurbSplash`); khi lách ra làn giữa, tốc độ sẽ hồi phục dần về 100%.
    * *Các màn khác:* Đi ép sát biên lề đường liên tục quá **3 giây**, xe sẽ bốc lửa và đốt **10 HP/giây**; đánh lái thoát khỏi lề đường sẽ làm ngọn lửa yếu dần và tắt hẳn.
  * **Cơ chế Lái & Cân bằng Gió mùa Màn 3 (Băng giá, Trà gừng, Xe máy x1.5):** Khi xe chạy trên mặt đường thông thường, **loại bỏ hoàn toàn quán tính gia tốc ngang**; tay lái ăn lái trực tiếp và nhạy bén như mọi map khác. Thời gian gió thổi kéo dài tăng thêm 1 giây (`windActiveUntil = windWarningUntil + 3000ms`, trước là 2000ms). Lực đẩy gió cân bằng vừa phải (`105 px/s` có đường cong gia tốc mềm), người chơi luôn giữ trọn quyền làm chủ tay lái (`240 px/s` dễ dàng lái ngược chiều gió) và gió tự động giảm lực khi xe gần vỉa hè. Khi dính gió buốt đóng băng, tốc độ tiến của xe giảm về **70%** (trước là 40%), lực ghì lùi giảm xuống `12 px/s`, trong khi xe cộ xung quanh và cảnh vật vẫn chạy tốc độ bình thường (decoupled). Người chơi nhặt **Trà gừng (`GingerTea`)** (4 cái trên toàn map, tránh góc chết) để hóa giải ngay đóng băng, giữ ấm cơ thể miễn nhiễm gió lạnh và hồi phục +15 HP. **Đặc biệt:** Mật độ xe máy trên đường Màn 3 và vận tốc tiến `fwd` của xe máy đều được tăng gấp **1.5 lần** (luồn lách nhanh nhẹn, tạt làn tốc độ cao).
  * **Cơ chế Tốc độ & Sinh tồn Màn 4 (Nhanh hơn 1.25 lần, tổng thời gian 35 giây & Khiên chắn):** Màn 4 được tăng tốc độ diễn ra nhanh hơn 1.25 lần trên mọi phương diện: tốc độ cuộn đường (`base *= 1.25`), tốc độ điều khiển xe (`PLAYER_SPEED * 1.25`, `VERTICAL_SPEED * 1.25`), tần suất spawn xe cộ, tốc độ chó chạy rông (`DASH_SPEED = 343.75 px/s`) và tần suất chó xuất hiện, tốc độ tụt năng lượng (`ENERGY_DRAIN * 1.25`), tần số dao động say sóng (`1.25x`). Tổng thời gian hoàn thành chặng khi đi thuận lợi là đúng **35 giây** (`HOT_DAY_DURATION_MS = 35_000`). Tia sét năng lượng được tăng cường tần suất (~16-19 viên/màn), tăng thêm 7 điểm năng lượng hồi phục (`BOLT_ENERGY = 27`) và **hồi phục +5 Máu (HP)** khi ăn (`BOLT_HP = 5`). Bổ sung **Khiên bảo vệ (`ShieldItem`)** (3 cái trên toàn map, tránh góc chết): nhặt khiên sẽ kích hoạt vầng hào quang năng lượng bảo vệ quanh xe, giúp **miễn 1 lần va chạm** (hấp thụ 100% sát thương, 0 đẩy lùi/làm chậm, khiên vỡ sau va chạm).
  * **Hệ thống Chữ Nổi (Floating Combat Text) cấu hình qua file `floating_texts.json`:** Toàn bộ chữ nổi hiển thị dạng **CHỮ IN HOA** sắc nét bay lên nhẹ cạnh nhân vật. File cấu hình duy nhất `floating_texts.json` phân tách 2 mục lớn: `harmful` (bất lợi - màu đỏ cam viền tối) và `beneficial` (có lợi - màu xanh lá viền tối), cùng đầy đủ các tình huống con (`collision`, `curb_fire`, `ice_slip`, `freeze`, `pothole`, `dog_bark`, `shark`, `low_energy`, `boss_hit`, `shield_break`, `energy_bolt`, `ginger_tea`, `shield_pickup`, `shield_block`, `missile_hit`), cho phép người dùng tùy ý thêm bớt câu chữ mà không cần đụng đến code Java.
  * **Va chạm, Đẩy lùi & Làm chậm ngắn:** Đâm trúng xe/chướng ngại vật gây sát thương cao (28–38 HP), đồng thời hất đẩy lùi phương tiện về phía sau và gây hiệu ứng làm chậm ngắn. Nếu đang bật khiên bảo vệ, đòn đánh sẽ bị chặn đứng hoàn toàn.

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
        ├── HeavyTruck/        # Xe tải hạng nặng cỡ lớn chắn kín 1 làn đường
        ├── ConstructionBarrier/ # Rào chắn công trình thi công sọc đỏ trắng
        ├── Pothole/           # Ổ gà cỡ lớn sụt lún mặt đường
        ├── NinjaLead/         # Xe máy Ninja Lead tạt đầu cắt làn
        ├── Shark/             # Cá mập săn mồi màn 2 (vây lướt -> cảnh báo -> trồi lên đớp)
        ├── IcePatch/          # Vũng băng trơn thực thể làm trượt bánh (Màn 3)
        ├── StrayDog/          # Chó vàng chạy rông băng ngang đường, biết né xe cộ theo phương tiến/lùi (Màn 4)
        ├── Bird/              # Chim cản đường bay trực thăng (Màn 5 chặng 2)
        ├── VehicleFire/       # Hiệu ứng xe bốc lửa khi đi sát lề đường quá 3 giây
        ├── CollisionImpact/   # Hiệu ứng chấn động đẩy lùi & làm chậm ngắn khi va chạm
        ├── FreezeEffect/      # Hiệu ứng sương băng bao phủ xe (Màn 3)
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
| `assets/objects/HeavyTruck/` | `HeavyTruck.png` | PNG trong suốt | **136 × 172 px** | Xe tải hạng nặng chiếm trọn bề ngang 1 làn đường (chặn đi sát biên làn 1 & 3) |
| `assets/objects/ConstructionBarrier/` | `ConstructionBarrier.png` | PNG trong suốt | **68 × 36 px** | Rào chắn thi công sọc đỏ trắng, đèn chớp vàng (vật cản tĩnh trôi nhanh) |
| `assets/objects/Pothole/` | `Pothole.png` | PNG trong suốt | **76 × 48 px** | Ổ gà cỡ lớn sụt lún asphalt kèm vũng nước phản chiếu (vẽ dưới mặt đường, không đè lên xe) |
| `assets/objects/NinjaLead/` | `NinjaLead.png` | PNG trong suốt | **28 × 62 px** | Xe Lead be/đỏ, áo chống nắng hoa, nhấp nháy xi-nhan + báo hiệu `!` tạt làn |
| `assets/objects/Shark/` | `Shark.png` | PNG trong suốt | **68 × 68 px** | Cá mập màn 2 trồi thẳng lên khỏi mặt nước đớp sau khi hiện vòng tròn cảnh báo |
| `assets/objects/IcePatch/` | `IcePatch.png` | PNG trong suốt | **88 × 54 px** | Vũng băng trơn thực thể trên mặt đường Màn 3 (cán vào gây trượt bánh) |
| `assets/objects/StrayDog/` | `StrayDog.png` | PNG trong suốt | **48 × 30 px** | Chó vàng chạy rông băng ngang qua đường ở Màn 4 (sủa cảnh báo trước khi chạy) |
| `assets/objects/Bird/` | `Bird.png` | PNG trong suốt | **60 × 48 px** | Chim cản trở đường bay trực thăng ở Màn 5 chặng 2 (bao quát mọi độ cao) |
| `assets/objects/VehicleFire/` | `VehicleFire.png` | PNG trong suốt | **76 × 96 px** | Hiệu ứng ngọn lửa bốc cháy trên xe khi đi sát lề đường quá 3 giây (tắt dần khi rời lề) |
| `assets/objects/CollisionImpact/` | `CollisionImpact.png` | PNG trong suốt | **72 × 88 px** | Hiệu ứng chấn động va chạm và khói giảm tốc khi bị đâm đẩy lùi |
| `assets/objects/FreezeEffect/` | `FreezeEffect.png` | PNG trong suốt | **56 × 88 px** | Hiệu ứng lớp băng lạnh bao quanh xe khi trúng gió mùa ở Màn 3 |
| `assets/objects/RoadMotorcycle/` | `RoadMotorcycle.png` | PNG trong suốt | **50 × 100 px** | Xe máy người đi đường (NPC) |
| `assets/objects/RoadBoat/` | `RoadBoat.png` | PNG trong suốt | **70 × 130 px** | Thuyền người dân đi lại trên phố ngập |
| `assets/objects/EnergyBolt/` | `EnergyBolt.png` | PNG trong suốt | **40 × 60 px** | Biểu tượng tia sét vàng / bình nước tăng lực (+27 Năng lượng, +5 HP) |
| `assets/objects/GingerTea/` | `GingerTea.png` | PNG trong suốt | **44 × 44 px** | Tách trà gừng nghi ngút khói Màn 3 (hóa giải đóng băng, giữ ấm, +15 HP) |
| `assets/objects/ShieldItem/` | `ShieldItem.png` | PNG trong suốt | **48 × 48 px** | Khiên hộ mệnh phát sáng viền cyan Màn 4 (miễn 1 lần va chạm bất kỳ) |
| `assets/objects/SchoolBuilding/` | `SchoolBuilding.png` | PNG trong suốt | **300 × 200 px** | Cổng Parabol trường ĐH Bách Khoa Hà Nội |
| `assets/avatars/` | `Sanbaka.jpg` hoặc `.png` | JPG / PNG | **120 × 120 px** (1:1) | Chân dung nhân vật chính hiển thị cạnh khung thoại |
| `assets/background/` | `sanbaka2.jpg` hoặc `.png`| JPG / PNG | **960 × 640 px** | Hình nền bìa lúc ở Menu Start |
| `assets/fonts/` | `1FTV-VIP-Starborn.ttf` | TTF | TrueType Font | Phông chữ pixel phong cách retro cho tiêu đề |

---

## 4. Cấu trúc mã nguồn theo chức năng (Modular OOP Packages)

Toàn bộ 32 file Java đã được tái cấu trúc và phân loại chuẩn theo 5 nhóm trách nhiệm:

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
│   ├── HeavyTruck.java                 # Xe tải hạng nặng cỡ lớn kín 1 làn đường (ưu tiên làn 1 & 3, trừ màn 2)
│   ├── ConstructionBarrier.java        # Rào chắn thi công đường bộ (sọc phản quang đỏ trắng, đèn chớp vàng)
│   ├── Pothole.java                    # Ổ gà cỡ lớn sụt lún asphalt (vẽ lớp mặt đường, không đè lên xe khác)
│   ├── NinjaLead.java                  # Xe máy Ninja Lead áo chống nắng, phóng nhanh, nháy xi-nhan tạt đầu
│   ├── Shark.java                      # Cá mập săn mồi màn 2 (lướt vây -> lặn -> vòng cảnh báo -> trồi lên đớp)
│   ├── IcePatch.java                   # Vũng băng thực thể làm trượt bánh ở Màn 3
│   ├── StrayDog.java                   # Chó vàng chạy rông băng ngang đường ở Màn 4
│   ├── RoadMotorcycle.java             # Xe máy NPC (có cache sprite tối ưu khi tắc đường đông đặc)
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
| **Phím E** | **Bắn tên lửa chiến đấu (Màn 5 Không chiến Trực thăng)** (hồi chiêu 0.3s, xuất phát từ càng trực thăng) |
| **Phím Esc hoặc P** | Tạm dừng trò chơi (Pause) và mở Menu tùy chọn |

---

### 5.4. Trận Đại Chiến Trùm Cuối Màn 5 & Tệp Cân Bằng Game (`boss_config.properties`)

Ở 2/3 chặng cuối ngày thi thứ 5, nhân vật chính lên trực thăng di chuyển đến trường thi C1 và bước vào chặng không chiến:
* **Đàn chim đầu màn 5 lượt 2 (15 giây):** Trực thăng bay lượn né tránh các đàn chim qua 4 đội hình bao quát toàn dải độ cao trong đúng 15 giây đầu tiên. Khi hết 15 giây và đàn chim đã bay khuất màn hình, Boss mới chính thức xuất hiện.
* **Màn hình nhấp nháy đỏ & Xuất hiện Boss:** Boss bay vào từ rìa phải màn hình và bắt đầu nạp máu từ 0 đến 20 vạch (`intro_charge_ms = 2500ms`).
* **Nút `[E]` Nhấp Nháy & Tên Lửa Trực Thăng:** Khi nạp máu xong, biểu tượng nút `[E]` nhấp nháy cạnh trực thăng gợi ý người chơi bấm phím E để bắn tên lửa (vị trí xuất phát từ chân/càng đáp trực thăng, thời gian hồi chiêu `0.30s`). Boss có 20 HP, mỗi phát tên lửa trúng đích làm tụt 1/20 cây máu.
* **Giảm 40% Toàn Bộ Sát Thương Lên Trực Thăng:** Đạn Boss (13 HP), Đạn tiêm kích (11 HP), Va chạm tiêm kích (19 HP), Nổ bom (17 HP), Va chạm chim (18 HP).
* **3 Đòn Tấn Công Luân Phiên Tuần Hoàn:**
  1. **Đòn 1:** Máy bay tiêm kích yểm trợ ([`EnemyPlane`](src/main/java/sanbaka/model/EnemyPlane.java)) được thiết kế dáng tiêm kích siêu âm hiện đại, kích thước lớn tương xứng (`86×44 px`, nhỏ hơn trực thăng người chơi một xíu) với mũi nhọn, ống pitot, buồng lái kính xanh, cánh delta vuốt lùi, ray tên lửa đầu cánh, cánh đuôi đứng kép và động cơ phản lực kép rực cháy. Tiêm kích bay ngược hướng với mật độ vừa phải, xác suất 50% bắn đạn thẳng; các khoảng trống xen kẽ chim; Boss đảo trục Y siêu nhanh và chiếu tia laser đỏ nhấp nháy ngắm bắn tỉa trực thăng. Luôn có khe thoáng để người chơi ngắm bắn trúng Boss.
  2. **Chuyển giao (Spray & Retreat):** Boss đảo lên xuống liên tục bắn đạn tán xạ ngẫu nhiên về mọi hướng, hiện dấu `!`, sau đó vụt lùi siêu nhanh về bên phải khuất khỏi màn hình (hiển thị khung đen siêu mờ và vệt tàn ảnh ghost trail motion blur).
  3. **Đòn 2 (Bão chim dày đặc):** Cảnh báo chấm than to giữa màn hình ("CẢNH BÁO BÃO CHIM CẤP ĐỘ CAO · BẤM [E] BẮN PHÁ MỞ ĐƯỜNG!"), đàn chim bão táp bay với tốc độ cực nhanh (`470 px/s`) xuất hiện với mật độ dày đặc tràn ngập bầu trời (sinh đồng thời 3-4 chim ở 4 dải độ cao, chu kỳ spawn rút ngắn còn 220ms), buộc người chơi phải liên tục bắn tên lửa dọn đường để mở lối lách qua.
  4. **Đòn 3 (Không kích rải bom chéo thưa & to, nổ diện rộng):** Boss quay trở lại với hiệu ứng vụt về và bắn đạn loạn xạ một thoáng; phi đội tiêm kích bay vút qua hàng trên cùng (`y = 45..85`); các trái bom ([`EnemyBomb`](src/main/java/sanbaka/model/EnemyBomb.java)) được thả thưa hơn (`attack3_bomb_interval_ms = 780ms`) nhưng kích thước tăng lớn (`38×38 px`), rơi chéo xuống dưới. Khi bom bị bắn trúng hoặc chạm đất, phát nổ với bán kính sát thương diện rộng (`blastRadius = 76.0 px`), sinh hiệu ứng quầng lửa và sóng xung kích ([`BombExplosion`](src/main/java/sanbaka/model/BombExplosion.java)), gây sát thương cho trực thăng nếu đứng gần, phá hủy chim và kích nổ dây chuyền bom lân cận. Hết đòn 3, Boss tuần hoàn quay trở lại Đòn 1.
* **Độ bền các đối tượng bị phá hủy:**
  * **Máy bay tiêm kích địch:** Cần 3 phát tên lửa trúng đích (`hp = 3`), có vạch máu trên đầu và hiệu ứng chớp trắng khi dính đạn.
  * **Chim:** Cần 1 phát tên lửa (`hp = 1`).
  * **Bom:** Cần 1 phát tên lửa (`hp = 1`). Khi bị phá hủy phát nổ trong bán kính sát thương (blastRadius = 76.0 px), kích nổ dây chuyền các bom và tiêu diệt chim lân cận.
* **Khi Boss bị tiêu diệt:** Boss phát nổ và vụt bay mất về bên phải; trực thăng tiếp tục hành trình bay thảnh thơi đến trường thi C1, tòa nhà trường xuất hiện và hoàn thành màn chơi dẫn đến Happy Ending (HE).
* **Tệp cấu hình cân bằng độc lập (`boss_config.properties`):** Mọi thông số (HP, tốc độ máy bay/tên lửa/đạn/chim, thời gian hồi chiêu, sát thương, thời lượng các pha) được tập hợp đầy đủ với chú thích tiếng Việt chi tiết trong file `boss_config.properties` để Game Designer dễ dàng cân bằng game mà không cần can thiệp mã nguồn Java.

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
* [x] **Decoupled Surroundings:** Tốc độ cuộn đường và xe cộ xung quanh giữ nhịp tự nhiên, độc lập với việc người chơi bị làm chậm.
* [x] **Map 2 Curb Slowdown:** Màn 2 cạ vỉa hè giảm tốc dần về 50%, không bị cháy xe và tự hồi phục khi ra giữa làn.
* [x] **Map 3 Direct Steering:** Màn 3 bỏ quán tính gia tốc ngang khi lái bình thường, trượt bánh văng ngang khi gặp vũng băng.
* [x] **Map 4 Delirium Sway:** Màn 4 kích hoạt hiệu ứng say sóng chao đảo ảo giác khi năng lượng dưới 50%.
* [x] **Map 3 Wind Balance:** Gió mùa Đông Bắc cân bằng hợp lý, người chơi luôn kiểm soát được tay lái và không bị ép kẹt xe vào lề đường.
* [x] **Stray Dog Vehicle Avoidance:** Chó chạy rông Màn 4 tự nhận diện xe cộ đường phố và lượn né theo phương tiến hoặc lùi vào khoảng trống không có xe, không né nhân vật chính để giữ trọn thử thách.
* [x] **Stray Dog Row Diversity:** Chó chạy rông xuất phát đa dạng qua nhiều bậc hàng từ xa phía trước tới sát hàng của nhân vật chính.
* [x] **Boss Fight Lifecycle & Config:** Vòng đời Boss Màn 5 (Máu trực thăng 150 HP, cảnh báo nhấp nháy, nạp máu 2.5s, 3 đòn luân phiên tuần hoàn, nạp thông số tự động từ `boss_config.properties`).
* [x] **Player Missile & Destructible Entities:** Bắn tên lửa trực thăng phím E (cooldown 0.3s, bắn từ càng đáp; hỗ trợ **giữ phím E** tự động bắn liên tục ngay khi hồi chiêu), độ bền thực thể (Máy bay 3 hit, Bom 1 hit, Chim 1 hit, Boss hạ gục khi hết HP).
* [x] **Bomb Detonation Chain Reaction:** Bom rơi nổ với bán kính diện rộng (68 px), phá hủy chim và kích nổ dây chuyền bom lân cận.
* [x] **Map 4 Energy Bolt Balance:** Tăng cường tia sét Màn 4 (~16-19 tia sét/màn), tăng thêm 7 năng lượng (`BOLT_ENERGY = 27`) và cộng 5 máu (`BOLT_HP = 5`).
* [x] **Map 4 Pacing & 35s Duration:** Màn 4 tăng tốc mọi hoạt động nhanh hơn 1.25 lần và hoàn thành trong đúng 35 giây khi đi thuận lợi.
* [x] **Shield Item Collision Immunity:** Màn 4 có đúng 3 khiên bảo vệ trên map (`ShieldItem`), hấp thụ hoàn toàn 1 lần va chạm (0 sát thương, 0 đẩy lùi/làm chậm).
* [x] **Ginger Tea Freeze Cure & Warmth:** Màn 3 có đúng 4 trà gừng (`GingerTea`), hóa giải ngay đóng băng, giữ ấm cơ thể miễn nhiễm gió buốt và hồi +15 HP.
* [x] **Map 3 Wind +1s & Freeze 70%:** Thời gian gió mùa thổi tăng thêm 1 giây (3000ms), hiệu ứng đóng băng giảm tốc tiến về 70% (decoupled với môi trường).
* [x] **Floating Combat Text JSON System:** Nạp cấu hình từ file duy nhất `floating_texts.json`, hiển thị chữ IN HOA sắc nét phân tách có lợi/bất lợi kèm hiệu ứng viền nổi bật.
* [x] **Item Spawn Safe Balancing:** Tự động điều phối vật phẩm tránh góc chết và chướng ngại vật (`clearToSpawn`).
* [x] **Map 3 Motorcycle Density & Velocity x1.5:** Màn 3 tăng mật độ xuất hiện xe máy và vận tốc tiến + lạng lách gấp 1.5 lần.
* [x] **GPU Hardware Acceleration & Render Cache Optimization:** Kích hoạt Direct3D GPU hardware pipeline (`sun.java2d.d3d=true`, `transaccel=true`, `ddforcevram=true`) thay thế phần mềm software rasterizer; cache toàn bộ đối tượng vẽ tĩnh (Color, Stroke, Gradient, Shape) triệt tiêu hoàn toàn hiện tượng đột biến CPU 33% và giảm tải bộ thu gom rác (GC).
* [x] **Stage 1 Student Crossing & Anti-Clipping Avoidance:** Sinh viên Màn 1 băng qua đường với cơ chế né xe thông minh (tính cả hành lang lắc lư cực đại, nhường đường không xuyên thân xe); va chạm với người chơi cả 2 tiếp tục đi tiếp, người chơi nhận đủ hiệu ứng va chạm, không hiện chữ nổi; đồ họa vector hình người hỗ trợ nạp ảnh riêng.

