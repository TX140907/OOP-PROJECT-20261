# TÀI LIỆU ĐẶC TẢ HÌNH ẢNH GAME (PHẦN 2: CÁC ĐỐI TƯỢNG GAMEPLAY CÒN THIẾU & MÀN 2, 3, 4, 5)

Tài liệu này được biên soạn dành riêng cho **Người phụ trách Hình ảnh (Artist / Designer)** của nhóm, tổng hợp toàn bộ các hình ảnh **trong Gameplay** (không tính Cutscene cốt truyện, Menu hay Minigame Mario) hiện **chưa có ảnh** hoặc **còn thiếu các trạng thái (frames)** từ `hinh_anh_1.md`.

---

## 1. TỔNG KẾT TIẾN ĐỘ HIỆN TẠI (ĐÃ CÓ TRONG DỰ ÁN)

Trước khi bắt đầu, dưới đây là danh sách các hạng mục mà bạn **ĐÃ HOÀN THÀNH RẤT TỐT** và đã nạp thành công vào thư mục `assets/objects/`:
- ✅ **Xe máy nhân vật chính (`PlayerBike`):** Đã có `PlayerBike.png`, `PlayerBike_left.png`, `PlayerBike_right.png`.
- ✅ **Ô tô & Xe buýt (`RoadCar`):** Đã đủ cả 6/6 xe (`Sedan.png`, `Hatchback.png`, `Taxi.png`, `Van.png`, `Truck.png`, `Bus.png`).
- ✅ **Xe tải hạng nặng (`HeavyTruck`):** Đã đủ `HeavyTruck.png` và `HeavyTruck_brake.png`.
- ✅ **Xe máy NPC (`RoadMotorcycle`):** Đã có `RoadMotorcycle.png`.
- ✅ **Ninja Lead (`NinjaLead`):** Đã có `NinjaLead.png` (bản đi thẳng).
- ✅ **Chướng ngại vật đường bộ:** Đã đủ Rào chắn (`ConstructionBarrier.png`, `ConstructionBarrier_blink.png`), Ổ gà (`Pothole.png`), Chó thả rông (`StrayDog.png`), Vũng băng (`IcePatch.png`).
- ✅ **Hiệu ứng & Môi trường:** Đã có Tòa nhà trường (`SchoolBuilding.png`), Đóng băng (`FreezeEffect.png`), Bốc cháy (`VehicleFire.png`), Chim bay (`Bird.png`), Cá mập ngoạm (`Shark.png`).

👉 **Nhiệm vụ trong `hinh_anh_2.md` này:** Hoàn thiện nốt các mục còn trống bên dưới để **100% thực thể Gameplay trong cả 5 Màn chơi** đều có đồ họa riêng đồng bộ!

---

## 2. QUY CHUẨN KỸ THUẬT BẮT BUỘC

1. **Định dạng file:** `.PNG` (32-bit RGBA) có **nền trong suốt hoàn toàn (Transparent Background)**. Không để nền trắng hoặc lưới caro.
2. **Kích thước xuất ảnh:**
   - Trong từng mục đều ghi rõ **Kích thước hiển thị chuẩn trong game (1x)** và **Kích thước xuất file khuyên dùng (2x HD)**.
   - Khuyến khích xuất ở **Kích thước 2x HD** (hoặc cao hơn nhưng giữ đúng **tỷ lệ Rộng × Cao**) để hình ảnh sắc nét trên màn hình độ phân giải cao.
3. **Cắt sát lề (Tight Crop):** Cắt sát vào mép hình vẽ đối tượng (chỉ chừa lề trong suốt tối đa `2–4 px`) để khớp chính xác với khung va chạm (Hitbox) đã lập trình trong code.
4. **Đặt đúng tên file & đúng thư mục:** Hệ thống nạp ảnh tự động phân biệt chữ hoa/thường. Hãy copy chính xác tên file được in đậm trong bảng hướng dẫn.

---

## 3. PHẦN I: CÁC MỤC TỪ `hinh_anh_1.md` CHƯA HOÀN THIỆN HẾT

### 3.1. CẢNH QUAN & MÔI TRƯỜNG VEN ĐƯỜNG (`Environment` — Đang trống)
- **Đường dẫn đặt tất cả các file mục này:** `assets/objects/Environment/`

#### A. Cây xanh hai bên vỉa hè (4 mẫu cây)
- **Kích thước chuẩn (1x):** `72 × 72 px` | **Kích thước khuyên dùng (2x HD):** `144 × 144 px`
- **Góc nhìn:** **Top-Down 90°** (nhìn thẳng từ đỉnh trời xuống tán cây hình tròn/bầu dục tự nhiên, thấy rõ các cụm tán lá xếp lớp tạo chiều sâu khối cầu).
- *(Lưu ý: Code game đã tự vẽ bóng đổ dưới đất nên bạn chỉ cần vẽ tán cây nền trong suốt, không cần vẽ bóng đen lệch ra ngoài).*
- **Danh sách 4 file:**
  1. `assets/objects/Environment/Tree_green_dark.png`: **Cây cổ thụ lá xanh đậm** (tông màu xanh lục đậm `#347A52` ở rìa, sáng dần lên `#76BC7C` ở đỉnh tán — kiểu cây sấu, xà cừ Hà Nội).
  2. `assets/objects/Environment/Tree_green_light.png`: **Cây tán lá xanh non / vàng lục tươi mát** (tông màu xanh lá mạ `#628040` đến `#A6BE6C`).
  3. `assets/objects/Environment/Tree_blossom.png`: **Cây hoa nở rực rỡ ven đường** (tán hoa màu hồng phấn / hồng anh đào `#E284A8` -> `#FFD0E0` hoặc tím bằng lăng tạo điểm nhấn tươi sáng).
  4. `assets/objects/Environment/Tree_snow.png`: **Cây phủ tuyết trắng mùa đông (riêng cho Màn 3)** — Tán cây thông hoặc cây cổ thụ có lớp tuyết trắng xốp (`#F4F8FC`) đọng dày trên phần đỉnh các chùm lá.

#### B. Cột đèn đường cao áp (`StreetLamp` — 2 trạng thái Ngày & Đêm/Mưa)
- **Kích thước chuẩn (1x):** `96 × 48 px` | **Kích thước khuyên dùng (2x HD):** `192 × 96 px`
- **Góc nhìn & Hướng chuẩn:**
  - Góc nhìn **Top-Down 90°**.
  - **Định hướng bắt buộc:** Chân đế cột đèn nằm ở **nửa bên trái gần tâm ảnh (`X ≈ 44–48 px`)**, tay cần đèn bằng thép vươn ngang sang **bên phải (`+X`)**, chao đèn gắn ở đầu bên phải (`X ≈ 72 px`).
  - *(Với hàng đèn bên phải đường, code game sẽ tự động lật gương ngang `scale(-1, 1)` để cần đèn vươn vào lòng đường).*
- **Danh sách 2 file:**
  1. `assets/objects/Environment/StreetLamp_day.png`: Dùng cho Màn 1 & Màn 4 ban ngày. Đế cột tròn màu xám thép đậm (`#464C5A`), cần đèn kim loại vươn sang phải, bóng đèn thủy tinh màu trắng ngà đang tắt.
  2. `assets/objects/Environment/StreetLamp_night.png`: Dùng cho Màn 2 (mưa bão tối trời) & Màn 3 (băng tuyết âm u). Giống cột đèn ban ngày nhưng bóng đèn ở đầu cần phát sáng màu vàng ấm (`#FFF3BE`) kèm **quầng sáng tròn tỏa mờ (Radial Glow)** bán kính khoảng `18–22 px` xung quanh đầu đèn.

#### C. Nhà dân hai bên đường (`RoadsideHouse`)
- **Đường dẫn đặt file:** `assets/objects/Environment/RoadsideHouse.png`
- **Kích thước chuẩn (1x):** `116 × 88 px` | **Kích thước khuyên dùng (2x HD):** `232 × 176 px`
- **Góc nhìn:** **Top-Down 90°** nhìn thẳng từ trên cao xuống phần **mái nhà**.
- **Mô tả chi tiết:**
  - Mái nhà chữ nhật bo góc nhẹ, chia làm **2 nửa mái dốc trái - phải** ngăn cách bởi đường sống nóc màu trắng/xám sáng chạy dọc chính giữa (`X = 58 px`).
  - Nửa mái bên trái đón sáng nên có tông màu sáng hơn một chút so với nửa mái bên phải.
  - Trên bề mặt mái có các đường kẻ ngang biểu thị hàng ngói đỏ/cam truyền thống hoặc gân mái tôn, góc phải phía trên có một ống khói hoặc bồn nước/giếng trời nhỏ.

#### D. Mặt đường nhựa & Vỉa hè lặp dọc (`Road_tile` — Tùy chọn nâng cao)
- **Đường dẫn đặt file:** `assets/objects/Environment/Road_tile.png`
- **Kích thước chuẩn (1x):** `550 × 256 px` | **Kích thước khuyên dùng (2x HD):** `1100 × 512 px`
- **Yêu cầu sống còn:** Phải là **Seamless Vertical Texture** (cạnh trên cùng `Y = 0` và cạnh dưới cùng `Y = 256` phải ăn khớp liền mạch 100% để khi game xếp chồng lặp dọc không thấy vết cắt ngang).
- **Cấu trúc bố cục theo chiều ngang `550 px` (tính ở tỷ lệ 1x):**
  - **Từ `X = 0` đến `X = 45` (`45 px`):** Vỉa hè bên trái lát gạch vuông màu kem vàng/xám, có bó vỉa bê tông dày `4 px` ngăn cách với lòng đường tại `X = 41..45`.
  - **Từ `X = 45` đến `X = 505` (`460 px`):** Mặt đường nhựa Asphalt màu xám đen (`#545C68`) chia làm **3 làn xe đều nhau** (mỗi làn rộng `~153 px`):
    - Vạch sơn liền màu trắng mảnh dọc hai mép đường tại `X = 51` và `X = 496`.
    - Hai hàng **vạch sơn trắng đứt đoạn** phân chia 3 làn xe nằm tại tọa độ **`X = 198`** và **`X = 352`**.
  - **Từ `X = 505` đến `X = 550` (`45 px`):** Bó vỉa bê tông và vỉa hè lát gạch bên phải đối xứng với bên trái.

---

### 3.3. CÁC FRAME BỔ SUNG CỦA XE MÁY & NINJA LEAD (Còn thiếu 3 ảnh phụ)

| STT | Đường dẫn & Tên file chính xác | Kích thước 1x | Kích thước 2x HD | Mô tả chi tiết Frame |
| :--- | :--- | :---: | :---: | :--- |
| 1 | `assets/objects/NinjaLead/NinjaLead_left.png` | `28 × 62 px` | `56 × 124 px` | Dựa trên file `NinjaLead.png` bạn đã làm: Vẽ thân xe tay ga và người mặc áo chống nắng hơi **nghiêng sang trái khoảng `12°–15°`** khi đang tạt đầu sang làn trái, **đèn xi-nhan sau phát sáng chấm vàng cam**. |
| 2 | `assets/objects/NinjaLead/NinjaLead_right.png` | `28 × 62 px` | `56 × 124 px` | Thân xe và người lái **nghiêng sang phải khoảng `12°–15°`** khi tạt đầu sang làn phải, đèn xi-nhan bật sáng vàng cam. *(Có thể lật gương ngang từ `NinjaLead_left.png`)*. |
| 3 | `assets/objects/PlayerBike/PlayerBike_hit.png` | `60 × 100 px` | `120 × 200 px` | Dựa trên file `PlayerBike.png` bạn đã làm: Trạng thái xe nhân vật chính bị **loạng choạng / chao đảo ngay lúc vừa va chạm**, ghi-đông hơi lệch, người lái hơi chúi về trước, có thể thêm vài tia lửa nhỏ hoặc vệt khói xám ở đuôi xe. |

---

## 4. PHẦN II: CÁC ĐỐI TƯỢNG MÀN 2 — ĐƯỜNG NGẬP LỤT & CHÈO THUYỀN (CHƯA CÓ ẢNH)

Ở Màn 2, mưa bão làm ngập toàn bộ con đường thành sông. Người chơi và người đi đường đều chuyển sang chèo thuyền vượt lũ và né cá mập.

### 4.1. Thuyền của Nhân vật chính (`PlayerBoat`)
- **Đường dẫn đặt file:** `assets/objects/PlayerBoat/PlayerBoat.png`
- **Kích thước chuẩn (1x):** `100 × 110 px` *(trong game tự động thu nhỏ 0.6x thành `60 × 66 px` cực nét)* | **Kích thước khuyên dùng (2x HD):** `200 × 220 px`
- **Góc nhìn & Hướng:** **Top-Down 90°** nhìn thẳng từ trên xuống. **Mũi thuyền nhọn hướng thẳng lên cạnh trên (`-Y`, hướng 12 giờ)**, đuôi thuyền phẳng ở cạnh dưới (`+Y`).
- **Mô tả cực kỳ chi tiết:**
  - **Thân thuyền (nằm ở giữa, rộng khoảng `52 px`, dài `110 px`):** Thuyền gỗ / xuồng cứu hộ sơn màu **cam tươi (`#FF8F42`)** viền cam đậm (`#D66824`), lòng thuyền bên trong màu gỗ nâu ấm (`#965C34`) có 5–6 thanh ván gỗ bắc ngang. Chóp mũi thuyền phía trên sơn tam giác nhỏ màu hồng (`#ED779B`).
  - **Hai mái chèo (vươn rộng sang 2 bên mạn thuyền tới tận mép ảnh `100 px`):** Cán chèo bằng gỗ vươn ngang từ vị trí người ngồi sang hai bên trái/phải, đầu bản mái chèo hình bầu dục sơn màu **hồng phấn (`#ED779B`)**.
  - **Nhân vật chính Thắng ngồi giữa thuyền:**
    - Đầu đội **mũ bảo hiểm màu trắng có vạch sọc hồng ở chính giữa đỉnh mũ**.
    - Mặc áo khoác xanh đen (`#2D3D57`), hai bàn tay nắm lấy hai cán mái chèo.
    - Sau lưng đeo **chiếc balo màu xanh ngọc Mint (`#63B89E`)** quen thuộc.
  - *(Lưu ý: Không cần vẽ sóng nước phía sau đuôi thuyền vào ảnh vì code game đã có hiệu ứng sóng rẽ nước động tự xoay theo góc lái).*

### 4.2. Thuyền của Người đi đường / NPC (`RoadBoat`)
- **Đường dẫn đặt file:** `assets/objects/RoadBoat/RoadBoat.png`
- **Kích thước chuẩn (1x):** `56 × 112 px` | **Kích thước khuyên dùng (2x HD):** `112 × 224 px`
- **Góc nhìn & Hướng:** **Top-Down 90°**, **mũi thuyền nhọn hướng thẳng lên cạnh trên (`-Y`, hướng 12 giờ)**.
- **Mô tả cực kỳ chi tiết:**
  - Kiểu dáng thuyền gỗ dân sinh hoặc thuyền thúng/xuồng ba lá thon gọn hơn thuyền nhân vật chính, màu gỗ nâu tự nhiên hoặc xanh lam trầm, có 2 mái chèo gỗ hai bên.
  - **Đặc điểm nhận diện nổi bật nhất:** Người ngồi chèo thuyền ở giữa đội **chiếc Nón Lá Việt Nam hình tròn màu vàng rơm (`#E8D096`)** nhìn từ đỉnh xuống (thấy rõ các vòng tròn đồng tâm của vành nón tre và chóp nón ở chính giữa), mặc áo bà ba / áo mưa màu sắc nổi bật.

### 4.3. Vây Cá Mập Bơi Rẽ Nước (`Shark_fin` — Giai đoạn 1 của Cá mập)
> **Ghi chú:** Bạn đã làm xong file `Shark.png` (lúc cá mập trồi vọt lên há hàm răng đớp ở Giai đoạn 4). Tuy nhiên, cá mập còn **Giai đoạn 1** khi mới xuất hiện từ mép trên màn hình và lao thẳng xuống dưới mặt nước chỉ lộ chiếc vây lưng!
- **Đường dẫn đặt file:** `assets/objects/Shark/Shark_fin.png`
- **Kích thước chuẩn (1x):** `44 × 56 px` | **Kích thước khuyên dùng (2x HD):** `88 × 112 px`
- **Góc nhìn & Hướng:** Top-Down / 2.5D nhìn xuống mặt nước, cá mập đang **bơi lao từ trên xuống dưới (`+Y`, hướng 6 giờ)**.
- **Mô tả chi tiết:**
  - Nổi lên ở chính giữa là **chiếc vây lưng cá mập hình tam giác cong sắc lẹm màu xám xanh đen (`#44566E`)**, viền tối (`#222E40`).
  - Dưới chân vây (phía dưới) là **đám bọt nước trắng xóa sủi tăm (`#F2FAFF`)**.
  - Từ mũi vây tỏa ngược lên phía trên (phía sau lưng khi cá bơi xuống) là **2 đường vệt sóng nước hình chữ V ngược màu trắng xanh** rẽ nước.
  - Thấp thoáng bên dưới mặt nước xung quanh vây là bóng đen mờ (Alpha `~35%`) của thân cá mập đang lướt ngầm.

### 4.4. Hiệu ứng Bọt Nước Bắn Tung Tóe Khi Thuyền Cạ Vỉa Hè (`BoatCurbSplash`)
- **Đường dẫn đặt file:** `assets/objects/BoatCurbSplash/BoatCurbSplash.png`
- **Kích thước chuẩn (1x):** `56 × 72 px` | **Kích thước khuyên dùng (2x HD):** `112 × 144 px`
- **Định hướng bắt buộc:** Thiết kế cho trường hợp **thuyền đang cạ mạn PHẢI vào vỉa hè bên phải** (nếu thuyền cạ vào vỉa hè bên trái, code game sẽ tự động lật gương ngang `scale(-1, 1)`).
- **Mô tả chi tiết:**
  - Phần nửa bên phải của ảnh (`X = 28..56 px`): Các tia nước trắng xóa, chuỗi giọt nước văng tung tóe và bọt sóng màu xanh băng (`#EBF8FF`) bắn bật ra từ mạn thuyền khi ma sát mạnh vào bờ đá vỉa hè ngập nước.
  - Nửa bên trái của ảnh để trống (trong suốt) để ôm vừa khít vào mạn phải của chiếc thuyền.

---

## 5. PHẦN III: CÁC VẬT PHẨM HỖ TRỢ (ITEMS) & HIỆU ỨNG TRÊN ĐƯỜNG ĐUA (MÀN 1 – 4)

### 5.1. Vật phẩm Trà Gừng Ấm Nóng (`GingerTea` — Xuất hiện ở Màn 3 Băng Giá)
- **Đường dẫn đặt file:** `assets/objects/GingerTea/GingerTea.png`
- **Kích thước chuẩn (1x):** `34 × 34 px` | **Kích thước khuyên dùng (2x HD):** `68 × 68 px` *(hoặc `128 × 128 px`)*
- **Công dụng trong game:** Khi xe bị đóng băng hoặc đi dưới trời tuyết Màn 3, nhặt cốc Trà Gừng sẽ phá băng ngay lập tức, làm ấm người và hồi `+15 HP`.
- **Mô tả chi tiết:**
  - Một **tách / cốc sứ trắng hoặc cốc giữ nhiệt màu đỏ cam ấm áp**, bên trong chứa nước trà gừng màu vàng hổ phách / đỏ cam rực ấm, trên miệng cốc cài một lát gừng tươi màu vàng.
  - Phía trên miệng cốc có **2 làn hơi nước nóng màu trắng mờ uốn lượn bốc lên**.
  - Xung quanh cốc tỏa một **quầng sáng tròn ấm áp (Warm Orange/Red Glow)** để người chơi dễ dàng nhìn thấy từ xa trên nền đường phủ tuyết trắng.

### 5.2. Vật phẩm Cốc Nước Mixue Giải Nhiệt (`EnergyBolt` — Xuất hiện ở Màn 4 Nắng Nóng)
- **Đường dẫn đặt file:** `assets/objects/EnergyBolt/EnergyBolt.png` *(hoặc `assets/objects/EnergyBolt/Mixue.png`)*
- **Kích thước chuẩn (1x):** `30 × 40 px` | **Kích thước khuyên dùng (2x HD):** `60 × 80 px` *(hoặc `120 × 160 px`)*
- **Công dụng trong game:** Vật phẩm giải nhiệt "quốc dân" của sinh viên Bách Khoa ở Màn 4 nắng nóng gay gắt. Khi nhặt sẽ hồi phục thanh Năng Lượng (Energy), giúp nhân vật tỉnh táo không bị say nắng, hoa mắt chóng mặt và giữ tốc độ tối đa.
- **Mô tả cực kỳ chi tiết:**
  - Hình một **cốc nước Mixue mát lạnh** (kiểu cốc trà chanh giã tay / trà đào / kem sữa lắc đặc trưng của Mixue khu Bách Khoa - Tạ Quang Bửu):
  - **Thân cốc:** Cốc nhựa trong suốt dáng cao thon gọn, bên trong thấy màu nước **vàng chanh tươi mát (`#FFE146`)** kèm vài lát chanh tươi, đá viên lấp lánh (hoặc lớp kem trắng mịn phía trên).
  - **Nhận diện thương hiệu Mixue:** Giữa thân cốc có nhãn/họa tiết **màu Đỏ tươi (`#E61E28`) và hình linh vật Người Tuyết đội vương miện (Tuyết Vương Mixue)** nhỏ nhắn dễ nhận biết.
  - **Nắp & Ống hút:** Nắp cốc viền đỏ/trắng có cắm chiếc **ống hút màu đỏ hoặc cam** nhô lên trên đỉnh.
  - **Hiệu ứng:** Xung quanh cốc tỏa một **quầng sáng mát lạnh (vàng tươi hoặc xanh băng nhạt Alpha `35%–50%`)** kèm vài giọt sương đá lạnh bám ngoài thành cốc để nổi bật trên mặt đường nhựa nóng hầm hập.

### 5.3. Vật phẩm Khiên Hộ Mệnh (`ShieldItem`) & Vòng Khiên Bảo Vệ (`ShieldBubble`) — Màn 4
- **Đường dẫn thư mục:** `assets/objects/ShieldItem/`
- Cần chuẩn bị **2 file ảnh riêng biệt** cho 2 trạng thái (Lúc nằm trên đường và Lúc đã kích hoạt bao quanh xe):

| STT | Đường dẫn & Tên file chính xác | Kích thước 1x | Kích thước 2x HD | Mô tả chi tiết từng ảnh |
| :--- | :--- | :---: | :---: | :--- |
| 1 | `assets/objects/ShieldItem/ShieldItem.png` | `36 × 36 px` | `72 × 72 px` | **Vật phẩm rơi trên mặt đường:** Hình chiếc **Khiên Hiệp Sĩ (Heraldic Shield)** cách điệu màu xanh lam ngọc bích (`#1EC8F5` chuyển sắc xuống `#0A78BE`), viền kim loại trắng bạc sáng bóng. Chính giữa mặt khiên có **ngôi sao 4 cánh màu trắng lấp lánh**, xung quanh tỏa hào quang xanh dương. |
| 2 | `assets/objects/ShieldItem/ShieldBubble.png` | `72 × 88 px` | `144 × 176 px` | **Vòng năng lượng bao quanh xe sau khi nhặt:** Quả cầu năng lượng hình bầu dục đứng (`Oval Forcefield`). **Lưu ý cực kỳ quan trọng:** Phần ruột bên trong phải **trong suốt mờ (Alpha khoảng `25%–35%`)** để nhìn xuyên thấu thấy rõ chiếc xe máy nhân vật chính ở bên trong; phần viền ngoài phát sáng màu xanh Cyan (`#50E1FF`) rực rỡ kèm các vệt vòng cung điện từ bo theo mép vỏ khiên. |

### 5.4. Hộp Bí Ẩn Easter Egg (`MysteryBox` — Xuất hiện sát mép vỉa hè Màn 2 hoặc Màn 4)
- **Đường dẫn đặt file:** `assets/objects/MysteryBox/MysteryBox.png`
- **Kích thước chuẩn (1x):** `36 × 36 px` | **Kích thước khuyên dùng (2x HD):** `72 × 72 px` *(hoặc `144 × 144 px`)*
- **Công dụng:** Vật phẩm bí mật nằm nửa dưới lòng đường nửa trên vỉa hè (người chơi phải mạo hiểm cạ lề mới ăn được) để mở khóa Secret Ending & Minigame Mario.
- **Mô tả chi tiết:**
  - Khối hộp vuông phong cách **Question Block (`?`) kinh điển của dòng game Super Mario**:
  - Thân hộp màu **vàng cam / vàng đồng retro (`#FC9838`)**, viền cạnh vát nổi khối 3D (cạnh trên và trái sáng `#FCD8A8`, cạnh dưới và phải đổ bóng `#804000`), 4 góc hộp đính 4 đinh tán nhỏ màu nâu sẫm.
  - Chính giữa mặt hộp là **dấu chấm hỏi `?` lớn** rõ nét, xung quanh hộp có quầng sáng vàng kim mời gọi.

### 5.5. Hiệu ứng Chấn Động & Khói Giảm Tốc Khi Đâm Xe (`CollisionImpact`)
- **Đường dẫn đặt file:** `assets/objects/CollisionImpact/CollisionImpact.png`
- **Kích thước chuẩn (1x):** `72 × 88 px` | **Kích thước khuyên dùng (2x HD):** `144 × 176 px`
- **Mô tả chi tiết:**
  - Đây là lớp hiệu ứng vẽ đè lên xung quanh xe của người chơi trong khoảng `1 giây` ngay sau khi đâm trúng chướng ngại vật (xe bị đẩy lùi và giảm tốc độ).
  - Phần giữa và phía trên: **Vòng sóng chấn động (Shockwave ring) màu vàng cam** kèm vài tia chớp va chạm / mảnh vỡ nhỏ bắn ra hai bên, giữ khoảng trong suốt ở tâm để vẫn thấy xe.
  - Phần phía dưới (`Y = 55..88 px`): **2–3 cụm khói xám đen (`#5A606C`)** tròn bốc ra từ ống xả/đuôi xe thể hiện xe đang bị khựng lại.

---

## 6. PHẦN IV: CÁC ĐỐI TƯỢNG MÀN 5 — BAY TRỰC THĂNG & KHÔNG CHIẾN (TẠM TÁCH RIÊNG TRÙM CUỐI)

> **Lưu ý quan trọng về Góc nhìn Màn 5:**
> Khác với Màn 1–4 nhìn từ trên đỉnh đầu xuống (Top-Down), **toàn bộ Màn 5 là màn chơi Bay trực thăng cuộn ngang trên bầu trời (2D Side-Scrolling)**:
> - **Góc nhìn:** **Nhìn ngang 100% (Side-View)**.
> - **Phe người chơi (bên trái màn hình):** Hướng mũi sang **PHẢI (`+X`, hướng 3 giờ)**.
> - **Phe Máy bay địch (bên phải màn hình):** Hướng mũi sang **TRÁI (`-X`, hướng 9 giờ)**.
> *(Ghi chú: Tạo hình riêng của nhân vật Trùm Cuối `Boss.png` sẽ được tách riêng ra một bản thiết kế độc lập sau).*

### 6.1. Trực thăng của Nhân vật chính & Komori (`PlayerHelicopter`)
- **Đường dẫn thư mục:** `assets/objects/PlayerHelicopter/`
- **Kích thước chuẩn (1x):** `204 × 104 px` | **Kích thước khuyên dùng (2x HD):** `408 × 208 px`
- **Góc nhìn & Hướng:** **Side-View (nhìn ngang)**, **mũi buồng lái trực thăng quay sang BÊN PHẢI (`+X`)**, đuôi chong chóng kéo dài về **BÊN TRÁI (`-X`)**.
- **Mô tả cực kỳ chi tiết (bám sát thiết kế trong game):**
  - **Thân máy bay:** Kiểu trực thăng dân dụng hiện đại bầu bĩnh, sơn tông màu chủ đạo **Xanh Ngọc Mint (`#5EB59A`)** phối đường sọc ngang màu **Trắng**, cánh đuôi đứng ở bên trái sơn màu **Hồng (`#ED779B`)**.
  - **Càng đáp (Landing Skids):** Hai thanh càng thép màu đen xám (`#232A3C`) uốn cong ở mũi dưới bụng trực thăng.
  - **Cánh quạt:** Phía trên nóc là trục rotor và cánh quạt chính sải rộng ngang (`~190 px` ở tỷ lệ 1x) có hiệu ứng vệt mờ quay tít; ở chóp đuôi bên trái có cánh quạt đuôi nhỏ.
  - **Khoang lái (Cockpit):** Có 2 ô cửa sổ kính màu xanh da trời nhạt (`#C4E6F6`):
    - **File 1 (Bắt buộc) — `assets/objects/PlayerHelicopter/PlayerHelicopter.png`:** Ở ô kính buồng lái phía trước (bên phải) thấy đầu của **Komori** (tóc nâu, đeo kính tròn) đang cầm lái; ở ô kính khoang sau (bên trái) thấy đầu của **Thắng** (tóc đen) đang ngồi cùng.
    - **File 2 (Tùy chọn) — `assets/objects/PlayerHelicopter/PlayerHelicopter_rescue.png`:** Dùng cho cảnh trực thăng mới bay tới thả thang dây cứu Thắng cuối Màn 4 — giống hệt ảnh trên nhưng **ô cửa sổ khoang sau trống (chưa có Thắng ngồi)**, chỉ có một mình Komori ở ghế lái trước.

### 6.2. Tên lửa của Trực thăng người chơi (`PlayerMissile`)
- **Đường dẫn đặt file:** `assets/objects/Missile/Missile.png`
- **Kích thước chuẩn (1x):** `26 × 10 px` | **Kích thước khuyên dùng (2x HD):** `52 × 20 px` *(hoặc `104 × 40 px`)*
- **Góc nhìn & Hướng:** Side-View nhìn ngang, **mũi tên lửa nhọn quay sang BÊN PHẢI (`+X`, hướng 3 giờ)**, đuôi lửa ở bên trái (`-X`).
- **Mô tả chi tiết:**
  - Thân tên lửa hình trụ ngang màu **trắng xám kim loại (`#E4EAF2`)** viền thép tối.
  - Đầu đạn hình nón nhọn sơn màu **đỏ tươi (`#EB372D`)**, phần đuôi có cánh ổn định màu xám xanh.
  - Phía sau đuôi (bên trái) phụt luồng **lửa phản lực màu vàng trắng chuyển sang cam đỏ** kèm vệt khói trắng nhỏ.

### 6.3. Máy bay Tiêm kích của Địch (`EnemyPlane`)
- **Đường dẫn đặt file:** `assets/objects/EnemyPlane/EnemyPlane.png`
- **Kích thước chuẩn (1x):** `86 × 44 px` | **Kích thước khuyên dùng (2x HD):** `172 × 88 px`
- **Góc nhìn & Hướng:** Side-View (hoặc hơi chếch nhẹ 3/4 nhìn thấy sải cánh delta), **mũi máy bay nhọn hoắt lao sang BÊN TRÁI (`-X`, hướng 9 giờ)** về phía người chơi, đuôi động cơ ở bên phải (`+X`).
- **Mô tả chi tiết:**
  - Máy bay chiến đấu phản lực siêu âm hiện đại (kiểu tiêm kích tàng hình F-22 Raptor / Su-57).
  - Thân sơn màu **xám đen quân sự (`#2A303E`)** kết hợp các đường kẻ sọc cảnh báo màu **đỏ tươi (`#E6322D`)** trên cánh chính và cánh đuôi đứng.
  - Nắp kính buồng lái (Canopy) ở phía trước màu xanh lam sáng (`#41C3F5`).
  - Dưới bụng gắn giá treo vũ khí, phía đuôi bên phải có **2 họng đốt phản lực kép (Twin Afterburners)** đang phụt luồng lửa cam vàng rực cháy.
  - *(Lưu ý: Không cần vẽ thanh máu vào ảnh vì code game đã tự động vẽ thanh máu 3 vạch ngay phía trên máy bay).*

### 6.4. Đạn Năng Lượng Trên Không (`BossBullet` — 3 loại đạn)
- **Đường dẫn thư mục:** `assets/objects/BossBullet/`
- **Kích thước chuẩn (1x):** `26 × 26 px` | **Kích thước khuyên dùng (2x HD):** `52 × 52 px` *(hoặc `64 × 64 px`)*
- **Định hướng chuẩn:** Code game **tự động xoay góc ảnh (`Math.atan2(vy, vx)`) theo hướng bay thực tế của viên đạn**! Do đó:
  - Nếu vẽ viên đạn hình cầu tròn đối xứng tâm thì hướng nào cũng đẹp.
  - Nếu vẽ viên đạn có đầu nhọn và đuôi lửa thì **BẮT BUỘC vẽ đầu đạn hướng sang BÊN PHẢI (`+X`, góc 0 độ — hướng 3 giờ)**, đuôi lửa ở bên trái (`-X`). Khi đạn bay sang trái hoặc bay chéo, code sẽ tự xoay mũi đạn theo đúng quỹ đạo bay!
- **Danh sách 3 file cho 3 kiểu đạn:**

| STT | Đường dẫn & Tên file chính xác | Loại đạn | Mô tả chi tiết hình dáng & màu sắc |
| :--- | :--- | :--- | :--- |
| 1 | `assets/objects/BossBullet/BossBullet_aimed.png` | **Đạn lửa ngắm bắn tỉa** | Quả cầu lửa năng lượng nhiệt độ cao: Lõi tâm màu trắng vàng rực sáng (`#FFF5DC`), bao quanh bởi quầng lửa màu **Cam - Đỏ thẫm (`#FF781E` -> `#FF3C28`)** rực cháy. |
| 2 | `assets/objects/BossBullet/EnemyBullet.png` | **Đạn bắn thẳng của Máy bay địch** | Viên đạn tia laze / plasma hình bầu dục thuôn nhọn (hình thoi) màu **Vàng kim rực rỡ (`#FFF05A`)**, tâm trắng sáng lóa, viền hào quang vàng cam. |
| 3 | `assets/objects/BossBullet/BossBullet_spray.png` | **Đạn mưa năng lượng tỏa tròn** | Quả cầu năng lượng ma mị màu **Tím Hồng Neon (`#F55AFF` -> `#DC32F0`)**, tâm trắng sáng rực, tỏa quầng sáng tím xung quanh. |

*(Mẹo: Nếu muốn dùng nhanh 1 ảnh cho cả 3 loại, có thể đặt tên là `assets/objects/BossBullet/BossBullet.png`, nhưng khuyến khích làm đủ 3 màu trên để người chơi dễ phân biệt đòn đánh).*

### 6.5. Trái Bom Rơi Chéo (`Bomb`) & Vụ Nổ Bom Diện Rộng (`BombExplosion`)
Khi phi đội máy bay ném bom rải thảm chéo từ trên cao xuống, bom phát nổ tạo vùng sát thương dây chuyền.

#### A. Trái Bom Hạng Nặng (`Bomb.png`)
- **Đường dẫn đặt file:** `assets/objects/Bomb/Bomb.png`
- **Kích thước chuẩn (1x):** `38 × 38 px` | **Kích thước khuyên dùng (2x HD):** `76 × 76 px`
- **Góc nhìn & Hướng:** Bom luôn rơi chéo từ trên-phải xuống dưới-trái (`vx < 0, vy > 0`), nên hãy vẽ **mũi bom chúc chéo xuống góc dưới bên trái (hướng 7h – 8h)**, cánh đuôi bom hướng lên góc trên bên phải (hướng 1h – 2h).
- **Mô tả chi tiết:**
  - Thân bom kim loại nặng hình bầu dục mập màu **xám thép đen (`#2D323C`)**.
  - Giữa thân bom sơn **dải băng cảnh báo nguy hiểm kẻ sọc Vàng - Đen (`#F5C81E`)**.
  - Chóp mũi bom bọc thép sơn màu **đỏ sẫm (`#E12D28`)** có đầu kích nổ kim loại ở mũi.
  - Phía đuôi có khung vây ổn định và một **đèn LED tròn màu đỏ tươi đang phát sáng**.

#### B. Hiệu ứng Vụ Nổ Bom Diện Rộng (`BombExplosion.png`)
- **Đường dẫn đặt file:** `assets/objects/BombExplosion/BombExplosion.png`
- **Kích thước chuẩn (1x):** `152 × 152 px` *(tương ứng bán kính sát thương nổ `76 px`)* | **Kích thước khuyên dùng (2x HD):** `304 × 304 px`
- **Mô tả chi tiết:**
  - Hình tròn đồng tâm thể hiện vụ nổ cầu lửa giữa không trung (code game sẽ tự động phóng to dần từ `35%` lên `100%` kích thước và làm mờ dần Alpha).
  - Tâm vụ nổ màu trắng vàng chói lòa, chuyển dần sang cam rực và đỏ thẫm ở rìa.
  - **Quan trọng:** Ở sát mép ngoài cùng của hình tròn (đường kính `152 px`), hãy vẽ một **vòng sóng xung kích (Shockwave Ring) hình tròn màu vàng cam rõ nét** cùng các tia lửa văng ra để người chơi nhìn thấy chính xác ranh giới vùng gây sát thương nổ lan.

### 6.6. Đám Mây Bầu Trời Màn 5 (`Cloud`)
- **Đường dẫn đặt file:** `assets/objects/Environment/Cloud.png`
- **Kích thước chuẩn (1x):** `110 × 52 px` | **Kích thước khuyên dùng (2x HD):** `220 × 104 px`
- **Góc nhìn:** Side-View nhìn ngang trên bầu trời.
- **Mô tả chi tiết:**
  - Đám mây trắng xốp bồng bềnh (phần trên gồm 3–4 cụm tròn phồng mềm mại, phần đáy mây hơi phẳng nhẹ tự nhiên).
  - Vẽ bằng màu trắng tinh khôi có chút đổ khối xám xanh cực nhẹ ở đáy mây trên nền trong suốt (code game sẽ tự động nhân bản thành 3 tầng mây xa-gần với kích thước và độ trong suốt Alpha khác nhau để tạo hiệu ứng chiều sâu Parallax).

---

## 7. BẢNG CHECKLIST TỔNG HỢP (DÀNH CHO ARTIST ĐÁNH DẤU TIẾN ĐỘ)

| STT | Nhóm / Màn chơi | Đường dẫn đặt file chính xác (`assets/objects/...`) | Kích thước 1x | Kích thước xuất 2x HD | Số lượng file |
| :---: | :--- | :--- | :---: | :---: | :---: |
| **1** | **Cây xanh ven đường** *(Màn 1–4)* | `assets/objects/Environment/Tree_green_dark.png`<br>`assets/objects/Environment/Tree_green_light.png`<br>`assets/objects/Environment/Tree_blossom.png`<br>`assets/objects/Environment/Tree_snow.png` | `72 × 72` | `144 × 144` | **4 files** |
| **3** | **Cột đèn đường** *(Màn 1–4)* | `assets/objects/Environment/StreetLamp_day.png`<br>`assets/objects/Environment/StreetLamp_night.png` | `96 × 48` | `192 × 96` | **2 files** |
| **4** | **Nhà dân ven đường** *(Màn 1–4)* | `assets/objects/Environment/RoadsideHouse.png` | `116 × 88` | `232 × 176` | **1 file** |
| **5** | **Mặt đường lặp dọc** *(Tùy chọn)* | `assets/objects/Environment/Road_tile.png` | `550 × 256` | `1100 × 512` | **1 file** |
| **6** | **Ninja Lead tạt đầu** *(Bổ sung)* | `assets/objects/NinjaLead/NinjaLead_left.png`<br>`assets/objects/NinjaLead/NinjaLead_right.png` | `28 × 62` | `56 × 124` | **2 files** |
| **7** | **Xe máy bị đâm** *(Bổ sung)* | `assets/objects/PlayerBike/PlayerBike_hit.png` | `60 × 100` | `120 × 200` | **1 file** |
| **8** | **Thuyền nhân vật chính** *(Màn 2)* | `assets/objects/PlayerBoat/PlayerBoat.png` | `100 × 110` | `200 × 220` | **1 file** |
| **9** | **Thuyền người dân NPC** *(Màn 2)* | `assets/objects/RoadBoat/RoadBoat.png` | `56 × 112` | `112 × 224` | **1 file** |
| **10** | **Vây cá mập bơi rẽ nước** *(Màn 2)* | `assets/objects/Shark/Shark_fin.png` | `44 × 56` | `88 × 112` | **1 file** |
| **11** | **Thuyền cạ vỉa hè tóe nước** *(Màn 2)* | `assets/objects/BoatCurbSplash/BoatCurbSplash.png` | `56 × 72` | `112 × 144` | **1 file** |
| **12** | **Vật phẩm Trà Gừng** *(Màn 3)* | `assets/objects/GingerTea/GingerTea.png` | `34 × 34` | `68 × 68` | **1 file** |
| **13** | **Vật phẩm Cốc nước Mixue** *(Màn 4)* | `assets/objects/EnergyBolt/EnergyBolt.png`<br>*(hoặc `Mixue.png`)* | `30 × 40` | `60 × 80` | **1 file** |
| **14** | **Vật phẩm Khiên & Vòng Khiên** *(Màn 4)* | `assets/objects/ShieldItem/ShieldItem.png`<br>`assets/objects/ShieldItem/ShieldBubble.png` | `36 × 36`<br>`72 × 88` | `72 × 72`<br>`144 × 176` | **2 files** |
| **15** | **Hộp Bí Ẩn Easter Egg** *(Màn 2, 4)* | `assets/objects/MysteryBox/MysteryBox.png` | `36 × 36` | `72 × 72` | **1 file** |
| **16** | **Hiệu ứng Chấn động va chạm** *(Màn 1–4)* | `assets/objects/CollisionImpact/CollisionImpact.png` | `72 × 88` | `144 × 176` | **1 file** |
| **17** | **Trực thăng người chơi** *(Màn 5)* | `assets/objects/PlayerHelicopter/PlayerHelicopter.png`<br>`assets/objects/PlayerHelicopter/PlayerHelicopter_rescue.png` *(tùy chọn)* | `204 × 104` | `408 × 208` | **1 – 2 files** |
| **18** | **Tên lửa trực thăng** *(Màn 5)* | `assets/objects/Missile/Missile.png` | `26 × 10` | `52 × 20` | **1 file** |
| **19** | **Máy bay tiêm kích địch** *(Màn 5)* | `assets/objects/EnemyPlane/EnemyPlane.png` | `86 × 44` | `172 × 88` | **1 file** |
| **20** | **Đạn năng lượng trên không** *(Màn 5)* | `assets/objects/BossBullet/BossBullet_aimed.png`<br>`assets/objects/BossBullet/EnemyBullet.png`<br>`assets/objects/BossBullet/BossBullet_spray.png` | `26 × 26` | `52 × 52` | **3 files** |
| **21** | **Trái Bom rơi chéo** *(Màn 5)* | `assets/objects/Bomb/Bomb.png` | `38 × 38` | `76 × 76` | **1 file** |
| **22** | **Vụ nổ bom diện rộng** *(Màn 5)* | `assets/objects/BombExplosion/BombExplosion.png` | `152 × 152` | `304 × 304` | **1 file** |
| **23** | **Đám mây bầu trời** *(Màn 5)* | `assets/objects/Environment/Cloud.png` | `110 × 52` | `220 × 104` | **1 file** |
