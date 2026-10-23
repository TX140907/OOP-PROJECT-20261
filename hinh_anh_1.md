# TÀI LIỆU ĐẶC TẢ HÌNH ẢNH GAME (PHẦN 1: CÁC THÀNH PHẦN CHUNG)

Tài liệu này mô tả chi tiết các tài nguyên hình ảnh (Sprites / Assets) thuộc nhóm **Dùng chung cho các màn chơi** (Nhân vật chính, phương tiện giao thông đường bộ, người đi bộ, chướng ngại vật, đường sá, cây cối và tòa nhà trường).  
*(Ghi chú: Các yếu tố đặc thù riêng của Màn 2 như sông nước, thuyền bè, cá mập... sẽ được tách sang Phần 2).*

---

## 1. TỔNG QUAN BỐI CẢNH GAME (GAME CONTEXT)

- **Tên dự án:** *Tuần thi của SVBK* (Sinh viên Bách Khoa đi thi).
- **Thể loại:** 2D Vertical Scrolling Road Runner (Game đua xe / vượt chướng ngại vật cuộn dọc từ trên xuống dưới).
- **Cốt truyện & Bối cảnh:** Người chơi vào vai một sinh viên Đại học Bách Khoa điều khiển chiếc xe máy quen thuộc vượt qua dòng giao thông đông đúc và hỗn loạn tại Việt Nam (ô tô, xe buýt, xe tải, "Ninja Lead" tạt đầu, ổ gà, rào chắn công trường, sinh viên băng qua đường...) để kịp giờ đến cổng trường (Tòa nhà C1 Bách Khoa) dự thi trong 5 ngày liên tiếp.
- **Độ phân giải khung hình game (Viewport):** `960 × 640 px`.
  - **Lòng đường (Road):** Nằm ở chính giữa màn hình (`X = 205` đến `X = 755`, tổng bề ngang `550 px`), bao gồm:
    - Hai dải **vỉa hè** hai bên: mỗi bên rộng `45 px`.
    - **Mặt đường nhựa 3 làn xe**: rộng `460 px` (mỗi làn rộng khoảng `153 px`).
  - **Hai bên lề ngoài vỉa hè (`0..205` và `755..960`):** Khu vực trồng cây xanh, cột đèn đường và mái nhà dân cư ven đường.
- **Hướng di chuyển:**
  - Xe của người chơi và luồng phương tiện giao thông cùng chiều luôn **hướng mũi xe lên phía trên màn hình (Hướng Bắc / `-Y`)**.
  - Mặt đường, cây cối và các xe cộ phía trước trôi từ trên xuống dưới để tạo cảm giác xe người chơi đang lao về phía trước.

---

## 2. QUY CHUẨN KỸ THUẬT CHUNG CHO NGƯỜI TÌM / VẼ ẢNH

1. **Định dạng file:** `.PNG` có **nền trong suốt hoàn toàn (Transparent Background - 32-bit RGBA)**. Tuyệt đối không để viền trắng hoặc nền caro xám trắng.
2. **Góc nhìn camera (Perspective):**
   - **Góc nhìn chung:** **Top-Down (90° nhìn thẳng từ đỉnh xuống)** hoặc **2.5D Oblique Top-Down (góc nhìn từ trên cao chếch khoảng 75°–85°)** — tức là nhìn thấy rõ toàn bộ phần nóc xe/đỉnh đầu nhân vật, đồng thời thấy một chút phần kính chắn gió/mặt trước hoặc đuôi xe để tạo chiều sâu.
   - **Lưu ý đồng bộ:** Toàn bộ xe cộ và nhân vật nên thống nhất cùng một phong cách (Flat Vector 2D sắc nét, Cartoon 2D hiện đại, hoặc Pixel Art độ phân giải cao).
3. **Kích thước ảnh (Dimensions):**
   - Trong bảng bên dưới có ghi **Kích thước hiển thị chuẩn trong game (1x)** và **Kích thước xuất file khuyên dùng (2x HD)**.
   - Người làm ảnh nên tìm/xuất ảnh theo **Kích thước 2x HD** (hoặc lớn hơn nhưng giữ đúng **tỷ lệ khung hình W:H**) để khi đưa vào game hình ảnh sắc nét, không bị vỡ hạt.
4. **Quy tắc cắt lề (Cropping):** Cắt sát (tight crop) vào mép đối tượng, chừa lề trong suốt tối đa `2–4 px` mỗi cạnh để khung hình ảnh khớp chính xác với vùng va chạm (hitbox) trong game.

---

## 3. CHI TIẾT TỪNG NHÓM HÌNH ẢNH & TRẠNG THÁI (FRAMES)

### 3.1. NHÂN VẬT CHÍNH (`PlayerBike` — Sinh viên đi xe máy)
- **Thư mục lưu:** `assets/objects/PlayerBike/`
- **Mô tả ngoại hình:** Sinh viên đội mũ bảo hiểm (màu xanh dương hoặc trắng/đỏ Bách Khoa), mặc áo khoác/áo đồng phục, đeo balo sau lưng, đang ngồi lái xe máy số (kiểu Honda Wave / Exciter / Winner).
- **Góc nhìn:** Nhìn từ trên cao hơi chếch phía sau lưng (`75°–85°` Top-Down Rear/Overhead), **đầu xe hướng thẳng lên cạnh trên của ảnh (Hướng Bắc `-Y`)**, đèn hậu màu đỏ quay xuống cạnh dưới của ảnh.
- **Kích thước chuẩn (1x):** `60 × 100 px` *(Khuyên dùng xuất file 2x: `120 × 200 px`)*.
- **Đa trạng thái (4 Frames riêng biệt):**

| STT | Tên file | Trạng thái | Mô tả chi tiết Frame |
| :--- | :--- | :--- | :--- |
| 1 | `PlayerBike.png` *(hoặc `PlayerBike_straight.png`)* | **Đi thẳng (Mặc định)** | Xe và người ngồi cân đối chính giữa trục dọc. Hai tay nắm ghi-đông thẳng, đầu xe hướng 12h. *(Đây là file bắt buộc ưu tiên số 1)*. |
| 2 | `PlayerBike_left.png` | **Bẻ lái / Nghiêng sang Trái** | Khi người chơi bấm phím sang trái (`A` / `Left`): Thân xe và vai người lái hơi nghiêng nhẹ sang trái khoảng `10°–15°`, tay lái (ghi-đông) và bánh trước hơi đánh chếch sang trái, tạo cảm giác ôm cua/chuyển làn trái. |
| 3 | `PlayerBike_right.png` | **Bẻ lái / Nghiêng sang Phải** | Khi người chơi bấm phím sang phải (`D` / `Right`): Thân xe và vai người lái hơi nghiêng nhẹ sang phải khoảng `10°–15°`, tay lái và bánh trước đánh chếch sang phải. *(Có thể lật gương ngang từ ảnh nghiêng trái nếu thiết kế xe đối xứng)*. |
| 4 | `PlayerBike_hit.png` *(Tùy chọn)* | **Loạng choạng khi va chạm** | Khi vừa đâm trúng xe khác/ổ gà: Đầu xe hơi lệch, người lái hơi chúi hoặc chao đảo, có thể kèm hiệu ứng nháy đỏ hoặc vệt khói nhỏ ở đuôi xe. |

> **Từ khóa tìm kiếm gợi ý:** `top down motorcycle rider sprite png`, `2d bike rider top view game asset`, `scooter rider overhead view sprite sheet`.

---

### 3.2. XE Ô TÔ CON & XE CÔNG CỘNG (`RoadCar` — 6 loại xe)
- **Thư mục lưu:** `assets/objects/RoadCar/`
- **Góc nhìn:** Top-Down (`80°–90°` nhìn từ trên nóc xuống).
- **Hướng xe:** **Tất cả đều hướng mũi xe lên cạnh trên của ảnh (Hướng Bắc `-Y`)**, phần cốp sau/đèn hậu hướng xuống cạnh dưới của ảnh.
- **Trạng thái:** **Trạng thái đơn lẻ (1 ảnh tĩnh/loại xe)**. Trong trường hợp xe chuyển làn lắc lư, code của game sẽ tự xử lý dịch chuyển hoặc xoay góc nhẹ.

| STT | Tên file chính xác | Loại xe | Kích thước 1x (`W × H`) | Kích thước 2x khuyên dùng | Mô tả đặc điểm nhận diện |
| :--- | :--- | :--- | :---: | :---: | :--- |
| 1 | `Sedan.png` | Ô tô 4 chỗ Sedan | `50 × 86 px` | `100 × 172 px` | Xe 4 chỗ có đầu xe, khoang lái (kính chắn gió trước/sau màu xanh/đen) và cốp sau cân đối. Màu gợi ý: Đỏ đô, Xanh dương hoặc Bạc. |
| 2 | `Hatchback.png` | Ô tô cỡ nhỏ (Kia Morning / i10) | `48 × 76 px` | `96 × 152 px` | Xe đô thị cỡ nhỏ ngắn đuôi (không có cốp dài phía sau). Màu gợi ý: Vàng tươi, Cam hoặc Trắng. |
| 3 | `Taxi.png` | Xe Taxi | `50 × 86 px` | `100 × 172 px` | Kiểu dáng giống Sedan nhưng sơn màu **Xanh lá cây (Mai Linh)** hoặc **Vàng/Xanh**, trên nóc xe có gắn **hộp đèn biển hiệu "TAXI"** nổi bật. |
| 4 | `Van.png` | Xe 7 chỗ / Minivan / Xe cứu thương-bán tải kín | `54 × 100 px` | `108 × 200 px` | Thân xe dài và vuông vức hơn Sedan, phần nóc xe kéo dài đến tận đuôi. Màu gợi ý: Trắng hoặc Bạc kim loại. |
| 5 | `Truck.png` | Xe tải thùng nhỏ (1.5 – 2.5 tấn) | `58 × 132 px` | `116 × 264 px` | Phía trên là **đầu cabin** (màu xanh dương hoặc trắng), phía dưới là **thùng hàng chữ nhật** (mui bạt hoặc thùng kín màu xám/trắng có gân sóng). |
| 6 | `Bus.png` | Xe buýt Hà Nội | `60 × 168 px` | `120 × 336 px` | Thân xe hình chữ nhật rất dài, bo góc nhẹ. Sơn màu **Đỏ - Vàng** hoặc **Xanh dương** đặc trưng của xe buýt đô thị, trên nóc có 2–3 cụm điều hòa/cửa thông gió. |

> **Từ khóa tìm kiếm gợi ý:** `2d top down cars sprite sheet png`, `top view vehicles game asset sedan taxi bus truck`.

---

### 3.3. XE TẢI HẠNG NẶNG / XE CONTAINER (`HeavyTruck`)
- **Thư mục lưu:** `assets/objects/HeavyTruck/`
- **Tên file:** `HeavyTruck.png`
- **Kích thước chuẩn (1x):** `136 × 172 px` *(Khuyên dùng 2x: `272 × 344 px`)*.
- **Góc nhìn & Hướng:** Top-Down (`80°–90°`), **đầu kéo hướng lên trên (`-Y`)**, thùng container phía sau.
- **Mô tả chi tiết:**
  - Đây là "hung thần xa lộ" chiếm trọn gần 1 làn đường lớn.
  - Bề ngang rất rộng (`136 px` — gấp hơn 2 lần ô tô con), gồm phần đầu kéo hầm hố có gương chiếu hậu lớn hai bên và thùng hàng tải trọng nặng phía sau (màu cam sẫm / xám thép / nâu gỉ sét công nghiệp).
- **Trạng thái:** **1 trạng thái đơn lẻ** (`HeavyTruck.png`). *(Tùy chọn nâng cao: có thể thêm `HeavyTruck_brake.png` có đèn hậu phía dưới phát sáng đỏ rực khi xe phanh).*

---

### 3.4. XE MÁY LƯU THÔNG TRÊN ĐƯỜNG (`RoadMotorcycle` & `NinjaLead`)

#### A. Xe máy NPC thông thường (`RoadMotorcycle`)
- **Thư mục lưu:** `assets/objects/RoadMotorcycle/`
- **Tên file:** `RoadMotorcycle.png`
- **Kích thước chuẩn (1x):** `26 × 60 px` *(Khuyên dùng 2x: `52 × 120 px`)*.
- **Góc nhìn & Hướng:** Top-Down nhìn từ trên xuống, **mũi xe hướng lên trên (`-Y`)**.
- **Mô tả:** Người đi đường đội mũ bảo hiểm chạy xe máy thông thường (màu sắc trung tính, gọn hơn xe nhân vật chính để dễ phân biệt).
- **Trạng thái:** 1 trạng thái đi thẳng (`RoadMotorcycle.png`).

#### B. "Ninja Lead" (`NinjaLead` — Chướng ngại vật biết tạt đầu bất ngờ)
- **Thư mục lưu:** `assets/objects/NinjaLead/`
- **Kích thước chuẩn (1x):** `28 × 62 px` *(Khuyên dùng 2x: `56 × 124 px`)*.
- **Góc nhìn & Hướng:** Top-Down nhìn từ trên xuống, **mũi xe hướng lên trên (`-Y`)**.
- **Mô tả ngoại hình:** Người lái mặc **áo chống nắng trùm kín mít từ đầu đến chân** (họa tiết hoa nhí hồng/tím/xanh đặc trưng), đội mũ trùm đầu + kính râm, điều khiển chiếc xe tay ga thân bầu bĩnh ở phần đuôi (kiểu Honda Lead màu bạc/vàng đồng).
- **Đa trạng thái (3 Frames khuyên dùng để báo hiệu cho người chơi):**

| STT | Tên file | Trạng thái | Mô tả chi tiết Frame |
| :--- | :--- | :--- | :--- |
| 1 | `NinjaLead.png` | **Đi thẳng bình thường** | Xe tay ga chạy thẳng, đèn xi-nhan tắt. |
| 2 | `NinjaLead_left.png` *(Tùy chọn)* | **Tạt đầu sang Trái** | Thân xe hơi nghiêng trái, đèn xi-nhan phía sau có chấm sáng màu vàng cam (hoặc bật xi-nhan phải nhưng rẽ trái đúng chất "Ninja Lead"). |
| 3 | `NinjaLead_right.png` *(Tùy chọn)* | **Tạt đầu sang Phải** | Thân xe hơi nghiêng phải, đèn xi-nhan bật sáng. |

---

### 3.5. SINH VIÊN BĂNG QUA ĐƯỜNG (`Student`)
- **Thư mục lưu:** `assets/objects/Student/`
- **Kích thước chuẩn (1x):** `32 × 38 px` *(Khuyên dùng 2x: `64 × 76 px`)*.
- **Góc nhìn & Hướng chuẩn:**
  - Góc nhìn từ trên cao chếch xuống (`Top-Down / 3/4 Overhead`).
  - **Mặt và mũi chân mặc định hướng sang PHẢI (`+X`, hướng 3 giờ)**. *(Khi sinh viên băng từ phải sang trái, code game sẽ tự động lật gương ngang `Flip Horizontal`, nên người làm ảnh chỉ cần chuẩn bị bộ ảnh hướng sang phải).*
- **Mô tả ngoại hình:** Sinh viên mặc áo sơ mi trắng / áo đồng phục, quần tối màu, đeo **balo trên lưng**, tóc đen gọn gàng.
- **Đa trạng thái (4 Frames chuyển động để ghép Animation):**

| STT | Tên file | Trạng thái trong game | Mô tả chi tiết Frame |
| :--- | :--- | :--- | :--- |
| 1 | `Student.png` *(hoặc `Student_idle.png`)* | **Đứng chờ (Idle)** | Hai chân khép tự nhiên trên vỉa hè, hai tay buông nhẹ hai bên hông, mặt nhìn sang phải chờ qua đường. |
| 2 | `Student_walk1.png` | **Bước đi - Nhịp 1** | Chân trái bước lên phía trước (về bên phải), chân phải đưa ra sau, tay phải đánh nhẹ ra trước, tay trái đánh ra sau. |
| 3 | `Student_walk2.png` | **Bước đi - Nhịp 2** | Đổi chân: Chân phải bước lên phía trước, chân trái đưa ra sau, hai tay đánh ngược lại với Nhịp 1. *(Luân phiên `walk1` ↔ `walk2` tạo bước chạy băng qua đường)*. |
| 4 | `Student_yield.png` | **Nhường xe / Giơ tay xin đường** | Khi phát hiện ô tô đang lao tới: Sinh viên đứng khựng lại, **một tay giơ cao lên phía trước/trên đầu** (ra hiệu xin đường hoặc giật mình dừng lại nhường xe). |

> **Từ khóa tìm kiếm gợi ý:** `top down pedestrian walking sprite sheet`, `2d character walk cycle top view png`, `rpg character top down walk side view`.

---

### 3.6. CHƯỚNG NGẠI VẬT TRÊN MẶT ĐƯỜNG (`ConstructionBarrier` & `Pothole`)

#### A. Rào chắn công trường (`ConstructionBarrier`)
- **Thư mục lưu:** `assets/objects/ConstructionBarrier/`
- **Tên file:** `ConstructionBarrier.png`
- **Kích thước chuẩn (1x):** `68 × 36 px` *(Khuyên dùng 2x: `136 × 72 px`)*.
- **Góc nhìn:** 2.5D nhìn từ trên chếch xuống mặt trước của rào chắn.
- **Mô tả:** Thanh chắn ngang bằng sắt/nhựa kẻ **sọc chéo Đỏ - Trắng phản quang**, có 2 chân đế màu tối giữ bên dưới và **1–2 đèn cảnh báo màu vàng cam** gắn phía trên thanh chắn.
- **Đa trạng thái (Tùy chọn):**
  - `ConstructionBarrier.png`: Đèn cảnh báo sáng bình thường.
  - `ConstructionBarrier_blink.png` *(Tùy chọn)*: Đèn cảnh báo phát quầng sáng vàng rực (để nhấp nháy).

#### B. Ổ gà / Mặt đường sụt lún (`Pothole`)
- **Thư mục lưu:** `assets/objects/Pothole/`
- **Tên file:** `Pothole.png`
- **Kích thước chuẩn (1x):** `76 × 48 px` *(Khuyên dùng 2x: `152 × 96 px`)*.
- **Góc nhìn:** **Top-Down 90°** (nằm bệt hoàn toàn trên mặt nhựa đường).
- **Mô tả:** Hố nhựa đường bị vỡ nứt lởm chởm hình bầu dục bất đối xứng, bên trong lòng hố có màu đất đá tối sẫm kèm một vũng nước đọng nhỏ phản chiếu ánh sáng, xung quanh mép có các vết nứt chân chim lan ra mặt đường.
- **Trạng thái:** 1 trạng thái tĩnh đơn lẻ (`Pothole.png`).

---

### 3.7. CẢNH QUAN, ĐƯỜNG SÁ, CÂY CỐI & TRƯỜNG ĐẠI HỌC (DÙNG CHUNG)

#### A. Tòa nhà Trường Đại học — Đích đến cuối màn (`SchoolBuilding`)
- **Thư mục lưu:** `assets/objects/SchoolBuilding/`
- **Tên file:** `SchoolBuilding.png`
- **Kích thước chuẩn (1x):** `672 × 294 px` *(Khuyên dùng xuất đúng `672 × 294 px` hoặc `1344 × 588 px` @2x)*.
- **Góc nhìn:** Mặt đứng nhìn hơi chếch từ trên xuống (**2.5D Front-Oblique Elevation**), đặt chắn ngang phía cuối con đường khi người chơi về đích.
- **Mô tả chi tiết:**
  - Lấy cảm hứng từ **Tòa nhà C1 & Cổng Parabol / Cổng chính Đại học Bách Khoa Hà Nội**.
  - Bố cục đối xứng bề thế: Tường sơn màu vàng kem cổ điển, mái ngói đỏ nâu, các dãy cửa sổ kính chia ô đều đặn, chính giữa có sảnh lớn, cột cờ Tổ quốc và biển hiệu trường / biểu tượng Bách Khoa.
  - Phần cổng chính ở giữa bên dưới mở rộng để khớp với lòng đường (`rộng khoảng 460–550 px` ở giữa) cho xe người chơi chạy thẳng vào sân trường.
- **Trạng thái:** 1 trạng thái tĩnh (`SchoolBuilding.png`).

#### B. Cây cối hai bên vỉa hè (`RoadsideTree` — Dùng được cho cả Màn 1, Màn 2 sông nước, Màn 3...)
- **Thư mục lưu đề xuất:** `assets/objects/Environment/` *(hoặc `assets/objects/RoadsideTree/`)*
- **Kích thước chuẩn (1x):** `72 × 72 px` *(Khuyên dùng 2x: `144 × 144 px`)*.
- **Góc nhìn:** **Top-Down 90°** (nhìn thẳng từ đỉnh xuống tán cây tròn/bầu dục, thấy các lớp lá xếp chồng lên nhau tạo khối cầu và một chút bóng đổ nhẹ dưới gốc).
- **Các biến thể đơn lẻ khuyên tìm (3–4 mẫu để trồng xen kẽ cho tự nhiên):**
  1. `Tree_green_dark.png`: Cây cổ thụ tán lá xanh đậm (như cây xà cừ, sấu Hà Nội).
  2. `Tree_green_light.png`: Cây tán lá xanh non tươi mát.
  3. `Tree_blossom.png`: Cây hoa bằng lăng tím hoặc hoa phượng đỏ / anh đào hồng tạo điểm nhấn màu sắc hai bên đường.
  4. `Tree_snow.png` *(Dành cho màn thời tiết lạnh/tuyết nếu cần)*: Tán cây có phủ lớp tuyết trắng trên đỉnh lá.

#### C. Cột đèn đường (`StreetLamp`)
- **Thư mục lưu đề xuất:** `assets/objects/Environment/`
- **Kích thước chuẩn (1x):** `96 × 48 px` *(Khuyên dùng 2x: `192 × 96 px`)*.
- **Góc nhìn:** Top-Down. Phần chân đế gắn ở vỉa hè, cần đèn vươn ngang ra phía lòng đường.
- **Đa trạng thái (2 trạng thái):**
  - `StreetLamp_day.png`: Cột đèn thép màu xám bạc ban ngày (đèn tắt).
  - `StreetLamp_night.png`: Đầu bóng đèn phát sáng vàng ấm kèm quầng sáng tỏa tròn mờ (glow) cho các màn chiều tối/sương mù.

#### D. Nhà cửa / Ô đất hai bên đường (`RoadsideHouse`)
- **Thư mục lưu đề xuất:** `assets/objects/Environment/`
- **Kích thước chuẩn (1x):** `210 × 160 px` *(Khuyên dùng 2x: `420 × 320 px`)*.
- **Góc nhìn:** Top-Down 90° nhìn xuống phần **mái nhà** (mái ngói đỏ, mái tôn xanh/xám hoặc sân thượng bê tông) nằm trên nền thảm cỏ/sân gạch có hàng rào bao quanh, đặt ở dải đất ngoài cùng hai bên màn hình.

#### E. Mặt đường nhựa & Vỉa hè lặp lại (`RoadTile` & `SidewalkTile` — Tùy chọn)
- Hiện tại code đang vẽ mặt đường nhựa, vạch kẻ phân làn đứt đoạn và gạch lát vỉa hè tự động rất mượt bằng GPU. Tuy nhiên, nếu nhóm muốn dùng ảnh chất lượng cao (Seamless Texture lặp dọc theo trục Y):
  - `Road_tile.png`: Kích thước `550 × 256 px` (gồm `45 px` vỉa hè trái + `460 px` mặt nhựa đường 3 làn + `45 px` vỉa hè phải). Yêu cầu **Seamless Vertical Tile** (cạnh trên và cạnh dưới khớp hoàn toàn với nhau khi ghép nối liên tiếp).

---

## 4. BẢNG TỔNG HỢP CHECKLIST NHANH CHO NGƯỜI TÌM ẢNH

| Nhóm đối tượng | Tên thư mục (`assets/objects/...`) | Danh sách file cần chuẩn bị | Kích thước 1x | Kích thước 2x (Chuẩn HD) | Số trạng thái (Frames) |
| :--- | :--- | :--- | :---: | :---: | :---: |
| **Nhân vật chính** | `PlayerBike/` | `PlayerBike.png`<br>`PlayerBike_left.png`<br>`PlayerBike_right.png`<br>`PlayerBike_hit.png` *(tùy chọn)* | `60 × 100` | `120 × 200` | **3 – 4 frames** (Thẳng, Nghiêng trái, Nghiêng phải, Va chạm) |
| **Ô tô 4 chỗ** | `RoadCar/` | `Sedan.png` | `50 × 86` | `100 × 172` | 1 frame tĩnh |
| **Ô tô nhỏ** | `RoadCar/` | `Hatchback.png` | `48 × 76` | `96 × 152` | 1 frame tĩnh |
| **Xe Taxi** | `RoadCar/` | `Taxi.png` | `50 × 86` | `100 × 172` | 1 frame tĩnh |
| **Xe 7 chỗ / Van** | `RoadCar/` | `Van.png` | `54 × 100` | `108 × 200` | 1 frame tĩnh |
| **Xe tải nhỏ** | `RoadCar/` | `Truck.png` | `58 × 132` | `116 × 264` | 1 frame tĩnh |
| **Xe buýt** | `RoadCar/` | `Bus.png` | `60 × 168` | `120 × 336` | 1 frame tĩnh |
| **Xe tải nặng** | `HeavyTruck/` | `HeavyTruck.png` | `136 × 172` | `272 × 344` | 1 frame tĩnh |
| **Xe máy NPC** | `RoadMotorcycle/` | `RoadMotorcycle.png` | `26 × 60` | `52 × 120` | 1 frame tĩnh |
| **Ninja Lead** | `NinjaLead/` | `NinjaLead.png`<br>`NinjaLead_left.png` *(tùy chọn)*<br>`NinjaLead_right.png` *(tùy chọn)* | `28 × 62` | `56 × 124` | **1 – 3 frames** (Thẳng, Tạt trái, Tạt phải) |
| **Sinh viên qua đường** | `Student/` | `Student.png` (Đứng)<br>`Student_walk1.png` (Bước 1)<br>`Student_walk2.png` (Bước 2)<br>`Student_yield.png` (Giơ tay nhường) | `32 × 38` | `64 × 76` | **4 frames** (Đứng, Bước 1, Bước 2, Giơ tay nhường xe) |
| **Rào chắn** | `ConstructionBarrier/` | `ConstructionBarrier.png` | `68 × 36` | `136 × 72` | 1 – 2 frames |
| **Ổ gà** | `Pothole/` | `Pothole.png` | `76 × 48` | `152 × 96` | 1 frame tĩnh |
| **Trường Bách Khoa** | `SchoolBuilding/` | `SchoolBuilding.png` | `672 × 294` | `1344 × 588` | 1 frame tĩnh |
| **Cây bên đường** | `Environment/` | `Tree_green_dark.png`<br>`Tree_green_light.png`<br>`Tree_blossom.png` | `72 × 72` | `144 × 144` | 3 mẫu đơn lẻ |

