# TÀI LIỆU ĐẶC TẢ ÂM THANH & NHẠC NỀN GAME (SOUND DESIGN & AUDIO ASSET GUIDE)

> **Dự án:** *Sanbaka Game — Tuần thi của SVBK*  
> **Mục tiêu:** Đặc tả chi tiết toàn bộ danh mục Nhạc nền (BGM), Âm thanh môi trường (Ambience) và Hiệu ứng âm thanh (SFX) từ tổng quan đến các vi chi tiết (micro-interactions) để chuẩn bị tài nguyên và tích hợp vào kiến trúc Java của dự án.

---

## 1. QUY CHUẨN KỸ THUẬT CHUNG CHO NGƯỜI TÌM / LÀM ÂM THANH

Để tương thích trực tiếp với thư viện âm thanh chuẩn của Java (`javax.sound.sampled.Clip` & `AudioSystem`) mà **không cần cài đặt thêm thư viện ngoài**, toàn bộ tệp âm thanh cần tuân thủ quy chuẩn sau:

1. **Định dạng tệp bắt buộc:** **`.WAV` (Waveform Audio File Format)**.
2. **Chuẩn mã hóa (Encoding):** **PCM Signed 16-bit, Little-Endian** *(Tuyệt đối tránh dùng WAV 32-bit float hoặc ADPCM nén vì bộ giải mã mặc định của Java có thể từ chối phát)*.
3. **Tần số lấy mẫu (Sample Rate):** **`44,100 Hz` (44.1 kHz)** hoặc `48,000 Hz`.
4. **Kênh âm thanh (Channels):**
   - **Nhạc nền (BGM) & Môi trường (Ambience):** `Stereo` (2 kênh) để tạo không gian rộng.
   - **Hiệu ứng ngắn (SFX):** `Mono` (1 kênh) hoặc `Stereo` (dung lượng nhỏ, nạp vào RAM tức thì, độ trễ `0ms`).
5. **Xử lý đầu/cuối file (Trimming & Looping):**
   - Với **SFX (One-shot — phát 1 lần):** Cắt sát khoảng lặng (silence) ở đầu file (`0.00s`) để khi va chạm hoặc bấm phím là âm thanh phát ra ngay lập tức không bị trễ nhịp.
   - Với **Loop (Phát lặp vô hạn — động cơ, mưa, cháy, nhạc nền):** Điểm đầu và điểm cuối phải nối khớp mượt mà (Zero-crossing seamless loop), không có tiếng "tạch" khi lặp lại.

---

## 2. CẤU TRÚC THƯ MỤC LƯU TRỮ TÀI NGUYÊN ÂM THANH

Tương tự như cơ chế `ArtAssets` dành cho hình ảnh, toàn bộ âm thanh được phân loại vào thư mục `assets/sounds/` như sau:

```text
OOP-PROJECT-20261/
└── assets/
    └── sounds/
        ├── bgm/                # Nhạc nền theo từng màn hình, từng ngày thi, Boss & Ending
        ├── ambience/           # Âm thanh thời tiết & môi trường nền (mưa, sấm, gió mùa, nắng, tắc đường)
        ├── ui/                 # Âm thanh giao diện Menu, nút bấm, chữ chạy, điện thoại, tin nhắn
        ├── player/             # Phương tiện người chơi (xe máy, xuồng, trực thăng) & tương tác mặt đường/lề đường
        ├── hazards/            # Va chạm giao thông, còi xe, Ninja Lead, ổ gà, rào chắn, cá mập, băng, chó sủa
        ├── items/              # Vật phẩm hỗ trợ (Tia sét, Trà gừng, Khiên bảo vệ, Hộp bí ẩn Mystery Box)
        ├── boss/               # Không chiến Màn 5 (Tên lửa, đạn Boss, laser, tiêm kích, bão chim, bom nổ)
        └── mario/              # Bộ âm thanh 8-bit cho Minigame Mario & Secret Ending
```

---

## 3. DANH MỤC CHI TIẾT CÁC ÂM THANH CẦN THÊM VÀO GAME

### 3.1. NHẠC NỀN (BGM — BACKGROUND MUSIC)
*Thư mục:* `assets/sounds/bgm/`  
*Đặc điểm:* Phát lặp lại (Loop), tự động chuyển bài mượt mà khi đổi màn hình (`screen`) hoặc đổi ngày thi (`day`).

| STT | Tên file chuẩn | Ngữ cảnh phát trong game | Phong cách & Cảm xúc gợi ý | Thời lượng gợi ý |
| :-: | :--- | :--- | :--- | :---: |
| 1 | `bgm_main_menu.wav` / `bgm_title_menu.wav` | Màn hình Menu chính (`TITLE`), Chọn cảnh, Hướng dẫn | Tươi sáng, năng động, pha chút Chiptune / Synth-pop hiện đại mang hơi thở đời sống sinh viên. | `60s – 120s` (Loop) |
| 2 | `bgm_room_dialog.wav` | Cảnh Phòng trọ (`ROOM`) & Hội thoại đầu/cuối ngày (`DIALOG`) | Nhẹ nhàng, Lo-fi Chill hoặc Guitar/Piano ấm áp, thư giãn nhưng hơi bâng khuâng mùa thi. | `60s – 90s` (Loop) |
| 3 | `bgm_day1_sunny.wav` | **Ngày 1 (Trời đẹp):** Lái xe máy ngày đầu đi thi | Nhịp độ vừa phải (`~120 BPM`), vui tươi, hứng khởi, giai điệu bắt tai. | `45s – 60s` (Loop) |
| 4 | `bgm_day2_flood.wav` | **Ngày 2 (Mưa lớn, đường ngập):** Chèo xuồng máy vượt lũ | Nhịp điệu dồn dập hơn, pha âm hưởng phiêu lưu vượt sóng nước và cạm bẫy cá mập. | `45s – 60s` (Loop) |
| 5 | `bgm_day3_cold.wav` | **Ngày 3 (Trời rét, đường trơn):** Gió mùa & xe máy phóng nhanh x1.5 | Âm sắc trong trẻo, sắc lạnh (Bell / Synth arpeggio), nhịp nhanh (`~135 BPM`) khớp với tốc độ xe máy tăng 1.5 lần. | `45s – 60s` (Loop) |
| 6 | `bgm_day4_hot.wav` | **Ngày 4 (Nắng nóng gay gắt):** Màn chơi tăng tốc x1.25 (35 giây) | Tempo rất nhanh (`145 – 155 BPM`), gấp gáp, hối hả, thúc giục người chơi chạy đua trước khi cạn thanh Năng lượng. | `35s – 60s` (Loop) |
| 7 | `bgm_day5_jam.wav` | **Ngày 5 (Chặng 1):** Đường đông & Sự kiện kẹt xe cứng ngắc | Căng thẳng, dồn nén, tiếng bass/trống dồn dập thể hiện sự sốt ruột khi sắp trễ giờ thi môn cuối. | `40s – 60s` (Loop) |
| 8 | `bgm_day5_flight.wav` | **Ngày 5 (Chặng 2 - 15s đầu):** Thắng đón bằng trực thăng bay vượt đàn chim | Hào hùng, bay bổng, tiết tấu hành động giải cứu trên không trung. | `30s – 60s` (Loop) |
| 9 | `bgm_day5_boss.wav` | **Ngày 5 (Trận Đại Chiến Trùm Cuối):** Không chiến với Boss | **Cao trào nhất game:** Epic Boss Battle (Rock điện tử / Fast Synthwave / Orchestral Chiptune) cực kỳ kịch tính. | `60s – 90s` (Loop) |
| 10 | `bgm_exam_hall.wav` | Cảnh Phòng thi C1 (`EXAM`) sau khi tới trường | Giai điệu hồi hộp nhẹ pha tiếng tích tắc đồng hồ làm bài thi. | `15s – 30s` (Loop) |
| 11 | `bgm_ending_he.wav` | **Happy Ending (`ENDING_HE`):** Qua môn rực rỡ, pháo giấy | Hân hoan, chiến thắng, tự hào, giai điệu tươi vui trọn vẹn. | `30s – 60s` (Loop) |
| 12 | `bgm_ending_be.wav` | **Bad Ending (`ENDING_BE`):** Nhập viện / Trễ giờ thi / Kiệt sức | Trầm buồn pha chút hài hước kiểu "thôi xong tạch rồi" (Piano chậm hoặc kèn Trombone thất vọng). | `15s – 30s` |
| 13 | `bgm_mario_8bit.wav` | **Minigame Mario (`MARIO`):** Chơi máy điện tử cầm tay | Nhạc nền 8-bit NES Chiptune cổ điển, vui nhộn, hoài niệm tuổi thơ. | `30s – 60s` (Loop) |
| 14 | `bgm_ending_se.wav` | **Secret Ending (`ENDING_SE`):** Hẹn hò & Đám cưới với Komori | Lãng mạn, ngọt ngào, sâu lắng (Music Box / Piano & Violin du dương). | `60s – 90s` (Loop) |

---

### 3.2. ÂM THANH MÔI TRƯỜNG & THỜI TIẾT (AMBIENCE & WEATHER)
*Thư mục:* `assets/sounds/ambience/`  
*Đặc điểm:* Lớp âm thanh nền (Ambient Layer) phát chồng nhẹ dưới nhạc BGM để tạo cảm giác chân thực cho từng điều kiện thời tiết khắc nghiệt.

| STT | Tên file chuẩn | Tình huống kích hoạt | Mô tả chi tiết âm thanh | Loại |
| :-: | :--- | :--- | :--- | :---: |
| 1 | `amb_street_traffic.wav` | Màn 1, 3, 4, 5 (Đường bộ) | Tiếng rì rầm phố xá đô thị Hà Nội (tiếng động cơ xe cộ lưu thông nền nhẹ nhàng). | Loop |
| 2 | `amb_rain_heavy.wav` | **Màn 2 (Đường ngập):** Suốt chặng đua | Tiếng mưa rào nặng hạt rơi lộp độp xuống mặt nước và mái tôn hai bên đường. | Loop |
| 3 | `amb_thunder_strike.wav` | **Màn 2 (Đường ngập):** Khi trời chớp sáng | Tiếng sấm sét đánh **"ĐÙNG! ĐOÀNG!"** vang rền trên bầu trời mưa bão (phát ngẫu nhiên mỗi 8–12 giây). | One-shot |
| 4 | `amb_wind_warning.wav` | **Màn 3 (Trời rét):** Pha cảnh báo gió (`windWarningUntil`) | Tiếng gió rít nhẹ bắt đầu nổi lên từ xa (khoảng `1.0s`), báo hiệu cho người chơi chuẩn bị giữ vững tay lái. | One-shot |
| 5 | `amb_wind_blizzard.wav` | **Màn 3 (Trời rét):** Pha gió mùa thổi (`windActiveUntil` - 3 giây) | Tiếng **gió mùa Đông Bắc rít mạnh, lạnh buốt ("Vùuuuuu — Huuuuu!")** thổi tạt ngang qua tai trong suốt 3 giây gió đẩy xe. | Loop / 3s |
| 6 | `amb_summer_heat.wav` | **Màn 4 (Nắng nóng):** Suốt chặng đua | Tiếng ve sầu kêu râm ran mùa hè oi bức kết hợp âm thanh không khí hầm hập bốc hơi nóng. | Loop |
| 7 | `amb_traffic_jam_horns.wav` | **Màn 5:** Khi gặp đám tắc đường đặc kín (`buildJam`) | Tiếng hỗn tạp của hàng chục xe máy, ô tô rú ga, bấm còi inh ỏi đặc trưng cảnh tắc đường giờ cao điểm. | Loop |

---

### 3.3. GIAO DIỆN, HỘI THOẠI & ĐIỆN THOẠI (UI, DIALOGUE & PHONE)
*Thư mục:* `assets/sounds/ui/`  
*Đặc điểm:* Phản hồi tức thì với từng thao tác chuột, bàn phím và diễn biến cốt truyện.

| STT | Tên file chuẩn | Tình huống kích hoạt | Mô tả chi tiết âm thanh | Loại |
| :-: | :--- | :--- | :--- | :---: |
| 1 | `ui_btn_hover.wav` | Di chuột vào các nút trên Menu (`MenuButton`) | Tiếng "Tick" hoặc "Pop" gỗ/điện tử thật nhẹ, ngắn (`~0.05s`), êm tai. | One-shot |
| 2 | `ui_btn_click.wav` | Nhấp chuột trái chọn nút trên Menu / Pause | Tiếng bấm nút xác nhận rõ ràng, dứt khoát (`~0.15s`). | One-shot |
| 3 | `ui_pause_toggle.wav` | Bấm phím `Esc` hoặc `P` để Tạm dừng / Tiếp tục | Tiếng "Whoosh-Click" mở/đóng bảng giao diện Pause. | One-shot |
| 4 | `ui_typewriter_blip.wav` | Khi chữ hội thoại đang chạy (`isDialogTyping()`) | Tiếng gõ phím cơ nhẹ hoặc tiếng "blip-blip" retro ngắn (`~0.03s`, phát cách nhịp mỗi 2–3 ký tự để không bị rè). | One-shot |
| 5 | `ui_dialog_advance.wav` | Bấm `Space` / `Enter` để sang câu thoại mới | Tiếng lật trang nhẹ hoặc tiếng "Ting" nhỏ báo hiệu chuyển câu thoại. | One-shot |
| 6 | `ui_phone_ring_vibrate.wav` | Khi có cuộc gọi đến (`callStart` — Thắng/Komori gọi) | Tiếng **chuông điện thoại smartphone reo kèm tiếng rung "Rè... rè..."** trên mặt bàn. | Loop ngắn |
| 7 | `ui_phone_pickup.wav` | Khi bắt đầu nghe điện thoại | Tiếng "Bíp!" nhận cuộc gọi. | One-shot |
| 8 | `ui_phone_hangup.wav` | Khi kết thúc phân đoạn gọi điện (`callEnd`) | Tiếng "Tút-tút" cúp máy kết thúc cuộc gọi. | One-shot |
| 9 | `ui_chat_message_pop.wav` | Khi một bong bóng tin nhắn mới hiện lên khung Chat | Tiếng **"Ting!" / "Pop!"** thông báo tin nhắn Messenger/Zalo quen thuộc. | One-shot |
| 10 | `ui_card_transition.wav` | Khi hiện màn hình chuyển cảnh (`CARD`: *"NGÀY 1 · TRỜI ĐẸP"*) | Tiếng gió chuyển cảnh ("Swoosh") kết hợp một nốt chuông ngân mở đầu ngày mới. | One-shot |
| 11 | `ui_reach_school_bell.wav` | Khi xe người chơi cán đích Cổng trường C1 (`SchoolBuilding`) | Tiếng **trống trường / chuông báo vào giờ thi** kết hợp tiếng thở phào nhẹ nhõm vì đã kịp giờ! | One-shot |

---

### 3.4. PHƯƠNG TIỆN NGƯỜI CHƠI & VẬT LÝ MẶT ĐƯỜNG / LỀ ĐƯỜNG (PLAYER & ROAD PHYSICS)
*Thư mục:* `assets/sounds/player/`  
*Đặc điểm:* Thể hiện rõ cảm giác lái (Game Feel) qua tiếng động cơ, tiếng phanh, tiếng lách làn và đặc biệt là **cơ chế cọ lề đường bốc cháy / mắc cạn bùn**.

| STT | Tên file chuẩn | Tình huống kích hoạt | Mô tả chi tiết âm thanh (Micro-details) | Loại |
| :-: | :--- | :--- | :--- | :---: |
| 1 | `bike_engine_loop.wav` | Lái xe máy (`PlayerBike` — Màn 1, 3, 4, 5) | Tiếng động cơ xe máy số (kiểu Wave/Exciter) nổ đều đặn, êm ái khi chạy tốc độ chuẩn. | Loop |
| 2 | `bike_throttle_rev.wav` | Giữ phím tiến `W` / `↑` để vọt lên | Tiếng **vít ga rồ máy ("Rừmmmm — Vồnnn!")**, cao độ động cơ đẩy lên cao. | One-shot / Loop |
| 3 | `bike_brake_skid.wav` | Giữ phím lùi `S` / `↓` để phanh giảm tốc | Tiếng **má phanh kẹp đĩa + tiếng lốp cao su miết nhẹ xuống mặt nhựa đường ("Kítttt...")**. | One-shot |
| 4 | `bike_lane_swerve.wav` | Bấm `A` / `D` hoặc `←` / `→` lách làn nhanh | Tiếng gió tạt nhẹ qua mũ bảo hiểm + tiếng lốp xe nghiêng bám đường khi chuyển làn. | One-shot |
| 5 | `boat_motor_loop.wav` | Lái xuồng máy (`PlayerBoat` — Màn 2 đường ngập) | Tiếng động cơ xuồng máy nổ phành phạch kèm tiếng mũi thuyền rẽ sóng nước liên tục. | Loop |
| 6 | `boat_turn_splash.wav` | Xuồng máy bẻ lái sang trái/phải ở Màn 2 | Tiếng nước tạt mạnh vào mạn thuyền ("Ào! Xoạt!"). | One-shot |
| 7 | `heli_rotor_loop.wav` | Bay trực thăng (`flightMode` — Màn 5 Chặng 2) | Tiếng **cánh quạt trực thăng chém gió "Phành-phạch-phạch-phạch"** đặc trưng. | Loop |
| 8 | `curb_scrape_sparks.wav` | **Cọ xát lề đường** (Màn 1, 3, 4, 5 khi `curbEdgeTime > 0`) | Tiếng **kim loại gác chân & vành xe cọ sát ken két vào bê tông vỉa hè ("Xoẹt... Kétttt...")** kèm tiếng tia lửa điện nổ lẹt đẹt cảnh báo sắp cháy! | Loop |
| 9 | `curb_fire_ignite.wav` | **Xe bắt đầu bốc cháy** (`curbOnFire` chuyển sang `true`) | Tiếng **"BÙNG! PHỰT!"** ngọn lửa bén vào động cơ bùng lên dữ dội khi đi sát vỉa hè quá ngưỡng. | One-shot |
| 10 | `curb_fire_burn_loop.wav` | **Đang cháy xe** (`VehicleFire` rút 10 HP/giây) | Tiếng **lửa cháy phừng phừng, reo phần phật trong gió + tiếng nổ lách tách** liên tục thúc giục người chơi phải rời lề đường ngay. | Loop |
| 11 | `curb_fire_extinguish.wav` | Khi lái xe thoát khỏi lề và lửa tắt hẳn | Tiếng **"Xèoooo..."** hơi nóng hạ nhiệt và ngọn lửa lụi tắt. | One-shot |
| 12 | `boat_curb_mud_drag.wav` | **Cạ lề đường ngập (Riêng Màn 2 — `map2CurbSlowFactor`)** | Tiếng **đáy thuyền cọ sột soạt vào lớp bùn đất nông ven vỉa hè + tiếng nước té bì bõm** (`drawBoatCurbSplash`) khi thuyền bị ghì chậm về 50% tốc độ. | Loop |

---

### 3.5. VA CHẠM GIAO THÔNG, CHƯỚNG NGẠI VẬT & TRẠNG THÁI BẤT LỢI (HAZARDS & COLLISIONS)
*Thư mục:* `assets/sounds/hazards/`  
*Đặc điểm:* Mỗi loại phương tiện, chướng ngại vật và hiệu ứng thời tiết đều có âm thanh đặc trưng riêng biệt giúp người chơi nghe là nhận biết được ngay chuyện gì đang xảy ra trên màn hình.

#### A. Âm thanh Va chạm & Giao thông (`RoadCar`, `HeavyTruck`, `RoadMotorcycle`, `NinjaLead`)
| STT | Tên file chuẩn | Tình huống kích hoạt | Mô tả chi tiết âm thanh | Loại |
| :-: | :--- | :--- | :--- | :---: |
| 1 | `crash_heavy_truck.wav` | Đâm trúng **Xe tải hạng nặng (`HeavyTruck`)** hoặc **Xe buýt (`Bus`)** (-38 HP) | Tiếng **đâm sầm chấn động cực nặng ("RẦM! XOẢNG!")** vào khối thép khổng lồ, kèm tiếng phanh hơi xe tải xì mạnh. | One-shot |
| 2 | `crash_car_normal.wav` | Đâm trúng **Ô tô con (`Sedan`, `Hatchback`, `Taxi`, `Van`, `Truck`)** (-32 HP) | Tiếng va đập thân vỏ ô tô, tiếng nhựa vỡ và tiếng lốp xe phanh kít gấp. | One-shot |
| 3 | `crash_motorcycle.wav` | Đâm trúng **Xe máy NPC (`RoadMotorcycle`)** hoặc **`NinjaLead`** (-28 HP) | Tiếng hai xe máy va quệt dàn áo nhựa lạch cạch, tiếng kim loại chạm nhau. | One-shot |
| 4 | `crash_knockback_stun.wav` | Hiệu ứng **Đẩy lùi & Làm chậm (`CollisionImpact`)** sau va chạm | Tiếng gió hất ngược + tiếng nhân vật chính xuýt xoa (*"Á!" / "Ui da!"*) khi xe bị bật lùi về phía sau. | One-shot |
| 5 | `horn_car_beep.wav` | Ô tô NPC bấm còi khi người chơi lách sát đầu xe | Tiếng còi ô tô **"Bíp-bíp!"** cảnh báo. | One-shot |
| 6 | `horn_heavy_truck.wav` | Xe tải lớn (`HeavyTruck`) xuất hiện hoặc khi người chơi đi gần | Tiếng **còi hơi xe container trầm đục, vang rền ("PÔÔÔ — PÔÔÔ!")** đầy uy lực. | One-shot |
| 7 | `ninja_lead_blinker.wav` | **Ninja Lead bật xi-nhan báo hiệu `!`** trước khi tạt đầu | Tiếng rơ-le xi-nhan **"Tạch-tạch-tạch-tạch"** dồn dập kết hợp tiếng chuông cảnh báo ngắn (`!`) ngay trước cú bẻ lái cắt mặt thương hiệu. | One-shot |
| 8 | `ninja_lead_cut_in.wav` | Khi Ninja Lead bắt đầu vọt lên tạt ngang làn | Tiếng ga xe tay ga rồ lên ("Vùuuu!") kèm tiếng lốp miết ngang mặt đường. | One-shot |

#### B. Chướng ngại vật Đặc thù theo từng Màn chơi (`Pothole`, `Barrier`, `Student`, `Shark`, `Ice`, `Dog`)
| STT | Tên file chuẩn | Màn chơi & Đối tượng | Mô tả chi tiết âm thanh (Micro-details) | Loại |
| :-: | :--- | :--- | :--- | :---: |
| 9 | `hazard_barrier_smash.wav` | **Rào chắn công trường (`ConstructionBarrier`)** (-32 HP) | Tiếng **đâm gãy thanh chắn sắt/nhựa sọc đỏ trắng kêu "Rầm — Loảng xoảng!"**, đèn cảnh báo rơi vỡ. | One-shot |
| 10 | `hazard_pothole_drop.wav` | **Ổ gà mặt đường (`Pothole`)** (-16 HP) | Tiếng bánh xe **sập xuống hố nhựa đường ("KỊCH! KHỰT!")**, phuộc nhún kịch sàn kèm tiếng nước đọng trong ổ gà bắn tung tóe. | One-shot |
| 11 | `student_footsteps.wav` | **Màn 1 — Sinh viên qua đường (`Student`)** đang bước đi | Tiếng giày thể thao bước nhanh lúp xúp trên mặt đường nhựa. | Loop ngắn |
| 12 | `student_yield_stop.wav` | **Màn 1 — Sinh viên giơ tay xin đường / nhường xe** (`isYielding`) | Tiếng bước chân khựng lại (giày miết nhẹ) + tiếng giật mình *"Ấy!" / "Ối!"* khi thấy xe lao tới. | One-shot |
| 13 | `student_collide.wav` | **Màn 1 — Va quệt vào Sinh viên qua đường** | Tiếng va chạm trầm ("Bịch!") kèm tiếng kêu bất ngờ của sinh viên. | One-shot |
| 14 | `shark_fin_glide.wav` | **Màn 2 — Cá mập (`Shark`) Pha 1:** Vây lưng lao từ trên xuống | Tiếng **vây cá mập rẽ nước lao vút ("Xoạt — Rào!")** đầy rùng rợn trước khi lặn ngầm. | One-shot |
| 15 | `shark_target_lock.wav` | **Màn 2 — Cá mập (`Shark`) Pha 3:** Hiện vòng tròn đỏ dưới thuyền | Tiếng **Sonar cảnh báo nguy hiểm dưới đáy nước ("Ping... Ping... Ping!")** tăng tốc dần báo hiệu sắp bị đớp. | One-shot |
| 16 | `shark_jaw_bite.wav` | **Màn 2 — Cá mập (`Shark`) Pha 4:** Trồi vọt lên đớp (-34 HP) | Tiếng **mặt nước nổ tung ("ÙM!") + tiếng hàm răng sắc nhọn táp mạnh ("PHẬP! RẮC!")** rồi lặn xuống nước. | One-shot |
| 17 | `ice_patch_slip.wav` | **Màn 3 — Cán vào Vũng băng (`IcePatch`)** | Tiếng **lốp xe trượt xoẹt trên mặt băng bóng loáng ("Xoẹttttt — Vù!")**, phản ánh cảm giác mất ma sát văng ngang. | One-shot |
| 18 | `freeze_ice_crust.wav` | **Màn 3 — Trúng gió mùa bị Đóng băng (`FreezeEffect`)** | Tiếng **băng giá kết tinh đóng tảng quanh xe ("Rắc-rắc-KENG!")** sắc lạnh, làm tốc độ xe bị ghì xuống 70%. | One-shot |
| 19 | `freeze_shiver_teeth.wav` | **Màn 3 — Đang trong trạng thái Tê cóng (`frozenUntil`)** | Tiếng răng va lập cập vì rét buốt ("Cập-cập-cập...") kèm tiếng băng kêu lạo xạo. | Loop ngắn |
| 20 | `dog_bark_alert.wav` | **Màn 4 — Chó vàng (`StrayDog`) đứng ở vỉa hè sủa `"GÂU! !"`** | Tiếng **chó sủa vang dội 2 tiếng ("GÂU! GÂU!")** đúng nhịp 520ms cảnh báo trước khi phóng ra đường. *(Cực kỳ quan trọng để người chơi phản xạ bằng thính giác)*. | One-shot |
| 21 | `dog_paws_dash.wav` | **Màn 4 — Chó vàng phóng nhanh cắt ngang & lượn né xe** | Tiếng móng chân chó chạy lạch bạch cực nhanh trên mặt đường nhựa nóng. | One-shot |
| 22 | `dog_collide_yelp.wav` | **Màn 4 — Đâm trúng Chó chạy rông** (-28 HP) | Tiếng va chạm kèm tiếng chó kêu *"Ẳng! Ẳng!"* rồi chạy mất. | One-shot |
| 23 | `heat_dizzy_heartbeat.wav` | **Màn 4 — Kiệt sức / Say nắng (`energy < 50%`)** | Tiếng **tim đập thình thịch trầm nặng + tiếng thở hồng hộc mệt lả + âm ù tai nhẹ** đồng bộ với hiệu ứng màn hình chao đảo say sóng. | Loop |

---

### 3.6. VẬT PHẨM HỖ TRỢ & BẢO HỘ (ITEMS & POWER-UPS)
*Thư mục:* `assets/sounds/items/`  
*Đặc điểm:* Âm sắc tươi sáng, tích cực (Major chord / Chime) đối lập hoàn toàn với âm thanh trầm đục của va chạm, tạo cảm giác tưởng thưởng (Rewarding) khi nhặt trúng.

| STT | Tên file chuẩn | Vật phẩm & Tình huống | Mô tả chi tiết âm thanh | Loại |
| :-: | :--- | :--- | :--- | :---: |
| 1 | `item_energy_bolt.wav` | **Tia sét Năng lượng (`EnergyBolt` — Màn 4):** Hồi +27 Energy & +5 HP | Tiếng **nạp điện năng lượng cao ("Zzzap — Ting!")** sảng khoái, tràn đầy sinh lực. | One-shot |
| 2 | `item_ginger_tea.wav` | **Trà gừng (`GingerTea` — Màn 3):** Giải băng, giữ ấm & hồi +15 HP | Tiếng **uống ngụm nước ấm ("Ực — Ahhh!") kết hợp tiếng lớp băng vỡ tan ("Keng-xoảng nhẹ")** và tiếng chuông ấm áp lan tỏa. | One-shot |
| 3 | `item_warm_buff_fade.wav` | Khi hết thời gian hiệu ứng giữ ấm của Trà gừng (`warmUntil` kết thúc) | Tiếng hơi ấm tan nhẹ trong gió, báo hiệu người chơi có thể bị đóng băng trở lại. | One-shot |
| 4 | `item_shield_pickup.wav` | **Khiên bảo vệ (`ShieldItem` — Màn 4):** Nhặt khiên hộ mệnh | Tiếng **kích hoạt lá chắn năng lượng Sci-Fi ("Vuuu — Chime!")** trong trẻo, mạnh mẽ. | One-shot |
| 5 | `item_shield_active_hum.wav` | Khi vòng hào quang Khiên đang xoay quanh xe (`shieldActive = true`) | Tiếng ngân năng lượng tần số thấp rất nhẹ ("Hummmm...") tạo cảm giác an toàn. | Loop |
| 6 | `item_shield_shatter.wav` | **Khiên chặn đòn va chạm & Vỡ (`shield_block` / `shield_break`)** | Tiếng **lá chắn năng lượng hấp thụ lực đâm ("ĐOÀNG!") nối tiếp bằng tiếng pha lê/kính cường lực vỡ tan ("CHOẢNG — KENG!")**, bảo toàn 100% máu cho người chơi. | One-shot |
| 7 | `item_mystery_box.wav` | **Hộp bí ẩn (`MysteryBox` — Easter Egg Màn 2 / Màn 4)** | Tiếng **ăn vật phẩm 8-bit huyền thoại (kiểu 1-UP / Power-Up cổ điển)** vang lên đầy bất ngờ khi người chơi lách sát vỉa hè nhặt được hộp `?`. | One-shot |

---

### 3.7. KHÔNG CHIẾN TRỰC THĂNG & ĐẠI CHIẾN TRÙM CUỐI (STAGE 5 AIR COMBAT & BOSS FIGHT)
*Thư mục:* `assets/sounds/boss/`  
*Đặc điểm:* Âm thanh chiến đấu không quân nhịp độ cao, bao gồm cả tiếng vũ khí, tiếng đường đạn rít, tiếng máy bay gầm rú và tiếng nổ dây chuyền.

| STT | Tên file chuẩn | Tình huống trong Trận Boss Màn 5 | Mô tả chi tiết âm thanh (Micro-details) | Loại |
| :-: | :--- | :--- | :--- | :---: |
| 1 | `boss_alarm_siren.wav` | Màn hình nhấp nháy đỏ cảnh báo Boss xuất hiện (`STATE_WARNING`) | Tiếng **còi báo động phòng không/quân sự ("WEE-OOOH! WEE-OOOH!")** dồn dập, đẩy sự kịch tính lên đỉnh điểm. | Loop ngắn |
| 2 | `boss_jet_entrance.wav` | Boss bay vào từ rìa phải màn hình (`STATE_ENTERING`) | Tiếng động cơ phản lực khổng lồ gầm rú uy hiếp bầu trời. | One-shot |
| 3 | `boss_hp_charging.wav` | Cây máu Boss nạp từ 0 lên 20 vạch (`intro_charge_ms = 2500ms`) | Tiếng **nạp thanh năng lượng cao độ tăng dần liên tục (Pitch Rising Synth)** trong đúng 2.5 giây và kết thúc bằng tiếng "KENG!" khi đầy cây máu. | One-shot (2.5s) |
| 4 | `missile_launch_e.wav` | Người chơi bấm/giữ phím **`[E]` bắn Tên lửa (`PlayerMissile`)** | Tiếng **khai hỏa tên lửa tầm nhiệt từ càng trực thăng ("Phụt — Xèooo!")**, gọn và đanh (vì hồi chiêu nhanh 0.3s/phát nên âm thanh cần ngắn `~0.2s` để nghe liên thanh cực đã tai). | One-shot |
| 5 | `missile_hit_armor.wav` | Tên lửa bắn trúng **Boss** hoặc **Máy bay tiêm kích (`EnemyPlane`)** | Tiếng **đầu đạn nổ va vào vỏ thép máy bay ("Bùm — Keng!")** kèm tia lửa. | One-shot |
| 6 | `missile_hit_bird.wav` | Tên lửa bắn hạ **Chim cản đường (`Bird`)** | Tiếng nổ nhỏ ("Bụp!") kèm tiếng lông vũ tung tóe. | One-shot |
| 7 | `boss_laser_lockon.wav` | **Đòn 1:** Boss chiếu **tia Laser đỏ nhấp nháy** ngắm bắn trực thăng | Tiếng **khóa mục tiêu điện tử ("Bíp-bíp-bíp-bíp!")** tốc độ cao báo hiệu Boss sắp nhả đạn bắn tỉa. | One-shot |
| 8 | `boss_bullet_fire.wav` | Boss hoặc Tiêm kích địch bắn đạn (`BossBullet`) | Tiếng **pháo năng lượng / đại liên trên không khai hỏa ("Pằng! Pằng!" / "Pew-Pew!")**. | One-shot |
| 9 | `bullet_whiz_pass.wav` | **Đường đạn bay sượt qua** gần trực thăng người chơi | Tiếng **đạn rít gió vút qua tai ("Chíuuu! Vút!")** tạo cảm giác không chiến ác liệt. | One-shot |
| 10 | `enemy_jet_flyby.wav` | **Đòn 1 & 3:** Máy bay tiêm kích siêu âm (`EnemyPlane`) lao qua | Tiếng **động cơ phản lực tiêm kích xé gió ("Vútttt — Rồồồ!")** bay ngược chiều. | One-shot |
| 11 | `enemy_jet_explode.wav` | Máy bay tiêm kích địch bị bắn nổ (sau 3 phát tên lửa) | Tiếng nổ tầm trung trên không trung ("Đùng — Xoảng!"). | One-shot |
| 12 | `boss_spray_retreat.wav` | **Pha Chuyển giao:** Boss xả đạn loạn xạ rồi lùi vụt mất (Ghost trail) | Tiếng xả đạn liên thanh nối tiếp bằng tiếng **Sonic Boom ("BÙM — VÚT!")** khi Boss bứt tốc biến mất về bên phải. | One-shot |
| 13 | `bird_storm_warning.wav` | **Đòn 2:** Hiện cảnh báo *"BÃO CHIM CẤP ĐỘ CAO · BẤM [E] MỞ ĐƯỜNG!"* | Tiếng còi cảnh báo ngắn kết hợp **tiếng hàng trăm đôi cánh chim vỗ rào rạt** cùng tiếng chim kêu vang trời. | One-shot |
| 14 | `bird_flock_rush.wav` | **Đòn 2:** Đàn chim bão táp lao vút qua ở tốc độ `470 px/s` | Tiếng gió lốc và tiếng vỗ cánh dồn dập lướt qua màn hình. | Loop ngắn |
| 15 | `heli_hit_bird.wav` | Trực thăng đâm trúng Chim trên không (-18 HP) | Tiếng va đập mạnh vào thân trực thăng ("Bịch! Quác!"). | One-shot |
| 16 | `heli_hit_bullet_jet.wav` | Trực thăng trúng đạn Boss (-13 HP), đạn tiêm kích (-11 HP) hoặc va tiêm kích (-19 HP) | Tiếng **kim loại buồng lái trúng đạn ("Choang! Rầm!") kèm tiếng bíp báo động đỏ trong khoang lái**. | One-shot |
| 17 | `bomb_drop_whistle.wav` | **Đòn 3:** Phi đội tiêm kích thả **Bom (`EnemyBomb`)** rơi chéo xuống | Tiếng **quả bom rơi tự do rít gió từ cao xuống thấp ("Chíuuuuuuuu...")**. | One-shot |
| 18 | `bomb_explosion_huge.wav` | **Đòn 3:** Bom phát nổ diện rộng (`BombExplosion`, bán kính `76 px`) | Tiếng **đại chấn nổ bom uy lực ("ẦM! ĐÙNG!")** kèm tiếng sóng xung kích lan tỏa. | One-shot |
| 19 | `bomb_chain_detonate.wav` | **Đòn 3:** Kích nổ dây chuyền nhiều quả bom liên tiếp | Tiếng nổ liên hoàn dồn dập có độ vang (Reverb) cực kỳ mãn nhãn khi 1 phát tên lửa dọn sạch cả cụm bom và chim. | One-shot |
| 20 | `boss_defeated_boom.wav` | **Hạ gục Trùm Cuối (`boss.hp == 0`):** Boss phát nổ và tháo chạy | Chuỗi **3–4 tiếng nổ lớn liên tiếp ("ĐÙNG! ĐÙNG! ĐOÀNG!") + tiếng động cơ cháy khét lẹt rớt cao độ** khi Boss bại trận rút lui, trả lại bầu trời bình yên cho trực thăng bay tới trường C1. | One-shot (`~3.0s`) |

---

### 3.8. KẾT THÚC GAME, MINIGAME MARIO 8-BIT & SECRET ENDING
*Thư mục:* `assets/sounds/mario/`  
*Đặc điểm:* Tái hiện trọn vẹn âm hưởng máy chơi game cầm tay cổ điển (Handheld Console) và cảm xúc vỡ òa của các đoạn kết.

| STT | Tên file chuẩn | Tình huống kích hoạt | Mô tả chi tiết âm thanh | Loại |
| :-: | :--- | :--- | :--- | :---: |
| 1 | `he_confetti_burst.wav` | **Happy Ending (`ENDING_HE`):** Pháo giấy (`Confetti`) bắn tung tóe | Tiếng **ống pháo giấy nổ "Bụp! Bụp!"** giòn giã kèm tiếng reo hò chúc mừng chiến thắng. | One-shot |
| 2 | `se_drawer_open.wav` | Mở ngăn kéo bàn học lấy phần thưởng bí ẩn (`marioRewardDrawerOpenAt`) | Tiếng **kéo ngăn kéo gỗ ("Rẹt... Cạch!")** kèm âm thanh phát sáng lấp lánh khi thấy máy chơi game cổ điển. | One-shot |
| 3 | `console_key_clack.wav` | Người chơi bấm phím điều khiển trên khung máy Handheld Console | Tiếng **nút bấm nhựa cơ học ("Tách! Cạch!")** của máy điện tử cầm tay phản hồi theo thao tác bàn phím. | One-shot |
| 4 | `mario_jump_8bit.wav` | Mario nhảy lên (`Space` / `W` / `↑` trong `MarioMinigame`) | Tiếng nhảy **"Boing / Phát sóng vuông 8-bit"** đi lên kinh điển. | One-shot |
| 5 | `mario_stomp_8bit.wav` | Mario dẫm bẹp quái nấm Goomba | Tiếng **"Bẹp / Blip 8-bit"** khi tiêu diệt quái vật. | One-shot |
| 6 | `mario_bump_block.wav` | Mario nhảy cụng đầu vào khối gạch / khối dấu hỏi | Tiếng **"Bụp 8-bit"** trầm ngắn. | One-shot |
| 7 | `mario_coin_pickup.wav` | Mario ăn đồng xu vàng hoặc vật phẩm | Tiếng **"Bling! (B5 -> E6)"** 8-bit quen thuộc. | One-shot |
| 8 | `mario_player_die.wav` | Mario chạm quái hoặc rơi xuống hố (`player.isDead`) | Đoạn Jingle 8-bit ngắn (`~2.0s`) báo hiệu mất mạng trước khi tự động hồi sinh. | One-shot |
| 9 | `mario_flagpole_slide.wav` | Mario chạm cột cờ cuối màn và trượt xuống (`isFlagSliding`) | Tiếng trượt cột cờ 8-bit kéo dài từ cao xuống thấp. | One-shot |
| 10 | `mario_stage_clear.wav` | Mario bước vào cửa lâu đài hoàn thành màn chơi (`enteredCastle`) | Bản nhạc hiệu **Chiến thắng màn chơi 8-bit (Stage Clear Fanfare `~4.0s`)**, dẫn dắt mượt mà sang cốt truyện **Secret Ending (`ENDING_SE`)**. | One-shot (`~4.0s`) |

---

## 4. HƯỚNG DẪN PHỐI HỢP GIỮA NGƯỜI LÀM ÂM THANH & DEVELOPER

Để việc thêm âm thanh vào dự án diễn ra trơn tru như cách nhóm đã làm với hình ảnh (`ArtAssets`), kiến trúc âm thanh được đề xuất theo mô hình **An toàn tuyệt đối & Chống rè loa (Safe Fallback & Anti-Spam Audio)**:

```text
  ┌──────────────────────────┐           ┌──────────────────────────┐
  │     SOUND DESIGNER       │           │      GAME DEVELOPER      │
  │  (Người chuẩn bị Audio)  │           │     (Người viết Code)    │
  └────────────┬─────────────┘           └────────────┬─────────────┘
               │                                      │
   Xuất file .WAV (16-bit PCM)            Gọi SoundAssets.play("hazards", "dog_bark_alert.wav")
   Đặt vào folder assets/sounds/          hoặc SoundAssets.loop("bgm", "bgm_day5_boss.wav")
               │                                      │
               └──────────────────┬───────────────────┘
                                  ▼
               ┌──────────────────────────────────────┐
               │     BỘ QUẢN LÝ ÂM THANH THÔNG MINH   │
               │  - Có file .wav -> Phát âm thanh     │
               │  - Thiếu file   -> Tự động bỏ qua    │
               │    (Không bao giờ văng lỗi crash)    │
               │  - Tích hợp Cooldown chống đè tiếng  │
               │  - Đồng bộ nút Âm lượng trên Menu    │
               └──────────────────────────────────────┘
```

### Các lưu ý kỹ thuật quan trọng khi lập trình (Dành cho Dev):
1. **Cơ chế Anti-Spam (Debounce / Cooldown):**
   - Các âm thanh có tần suất gọi dày đặc trong vòng lặp `16ms (60 FPS)` như: *tiếng gõ chữ typewriter (`ui_typewriter_blip.wav`), tiếng cọ lề đường (`curb_scrape_sparks.wav`), tiếng bắn tên lửa giữ phím E (`missile_launch_e.wav`), hoặc tiếng nổ dây chuyền (`bomb_chain_detonate.wav`)* cần có biến kiểm tra thời gian tối thiểu giữa 2 lần phát (ví dụ: `minIntervalMs = 60ms – 150ms`) để tránh tạo hàng chục luồng `Clip` đè lên nhau gây vỡ tiếng.
2. **Quản lý vòng đời Loop (State Cleanup):**
   - Các âm thanh `Loop` (như tiếng xe cháy `curb_fire_burn_loop.wav`, tiếng gió mùa `amb_wind_blizzard.wav`, tiếng tim đập kiệt sức `heat_dizzy_heartbeat.wav`, tiếng động cơ xe/thuyền/trực thăng) **phải tự động gọi `.stop()` ngay lập tức** khi người chơi chuyển sang màn hình Hội thoại (`DIALOG`), Tạm dừng (`Pause`), Kết thúc màn (`EXAM` / `ENDING_HE` / `ENDING_BE`) hoặc bấm Chơi lại (`resetRunState()`).
3. **Đồng bộ thanh Âm lượng (Volume Control):**
   - Kết nối trực tiếp với cấu hình âm lượng sẵn có trong `GameMenuState` thông qua `FloatControl.Type.MASTER_GAIN` để người chơi có thể tăng/giảm hoặc tắt tiếng ngay trong Menu và màn hình Pause.

---

## 5. BẢNG CHECKLIST ƯU TIÊN DÀNH CHO NGƯỜI TÌM ÂM THANH

Nếu thời gian có hạn, nhóm có thể chia làm **3 mức độ ưu tiên** để hoàn thiện dần:

* 🔴 **Ưu tiên 1 (P1 - Cốt lõi, cần có trước tiên):** Nhạc nền chính, tiếng va chạm, tiếng nhặt vật phẩm, tiếng chó sủa, tiếng cá mập đớp, tiếng bắn tên lửa & bom nổ màn Boss, tiếng chiến thắng/thất bại.
* 🟡 **Ưu tiên 2 (P2 - Tăng mạnh cảm giác chơi):** Tiếng cọ lề bốc cháy, tiếng trượt băng/đóng băng, tiếng gió mùa, tiếng mưa sấm, tiếng Ninja Lead xi-nhan, tiếng ổ gà, tiếng điện thoại/tin nhắn, bộ âm thanh Mario 8-bit.
* 🟢 **Ưu tiên 3 (P3 - Hoàn thiện chi tiết nhỏ):** Tiếng bước chân sinh viên, tiếng nút bấm Menu, tiếng gõ chữ typewriter, tiếng đường đạn rít qua tai, tiếng còi xe NPC.

---

## 6. TIẾN ĐỘ TÍCH HỢP & TỐI ƯU ÂM THANH MỚI NHẤT

1. **Nhạc nền & Môi trường Ngày 2:**
   - Khi chơi (`ROAD`): Nhạc `bgm_day2.wav` phát song song cùng tiếng mưa to `amb_rain_heavy.wav` (âm lượng $45\%$).
   - Kết thúc màn (`FADE_END` / `CARD`): Ngắt ngay lập tức `bgm_day2.wav` và chuyển sang nhạc hội thoại.
   - Hội thoại sau thi (`NGÀY 2 · SAU KHI THI`): Tiếng mưa chuyển sang mưa rơi tí tách nhẹ nhàng ngoài hiên C1 (âm lượng $22\%$).
2. **Nhạc nền & Tiếng gió mùa Ngày 3:**
   - `bgm_day3.wav`: Tối ưu nén từ $13.28\text{ MB} \to 2.41\text{ MB}$ ($-82\%$, chuẩn `16kHz 8-bit Stereo`), phát lặp ở Màn 3.
   - `winter_wind.wav`: Trích xuất $3.8\text{ giây}$ đầu tương ứng thời lượng mỗi đợt gió lạnh, fade-out mượt mà, nén từ $2.58\text{ MB} \to 121.6\text{ KB}$ ($-95\%$), lưu tại `assets/sound/ambience/amb_winter_wind.wav`. Âm lượng gió được cân chỉnh ở mức $30\%$ (nhỏ hơn nhạc nền, tạo không khí rét buốt chân thực).
3. **Nhạc nền Màn 1 & Màn 5 đợt 1:**
   - `bgm_day1+5.wav`: Tối ưu nén từ $12.86\text{ MB} \to 2.33\text{ MB}$ ($-81.9\%$, chuẩn `16kHz 8-bit Stereo`, thời lượng $72.9\text{s}$ loop).
   - Tích hợp phát tự động cho:
     - **Màn 1 (`day == 0`):** Đoạn đường đi thi ngày đầu tiên.
     - **Màn 5 đợt 1 (`day == 4`, đường bộ):** Đoạn đường đông và kẹt xe trước khi chuyển sang trực thăng.
4. **Âm thanh đường phố & Trực thăng Màn 5:**
   - `street.wav`: Nén từ $2.78\text{ MB} \to 505\text{ KB}$ ($-82\%$, thời lượng $15.8\text{s}$ loop), phát khi xe dừng kẹt xe và vào hội thoại gọi điện cho Komori (âm lượng $30\%$).
   - `helicopter.wav`: Nén từ $1.14\text{ MB} \to 206\text{ KB}$ ($-82\%$, thời lượng $6.5\text{s}$ loop), bắt đầu phát từ câu *"Một lúc sau, tiếng cánh quạt trực thăng vang lên từ phía trên"* qua phân cảnh chuyển tiếp `AIR` và kéo dài xuyên suốt cuộc trò chuyện với Komori trên loa cho đến hết hội thoại (âm lượng $35\%$). Khi chuyển sang điều khiển trực thăng thì ngắt cả 2 âm và chuyển sang nhạc bay `bgm_day5_flight.wav`.
