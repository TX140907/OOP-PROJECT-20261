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
  * `RoadCar.java`: Các loại xe ô tô (`kind` 0..5: Sedan, Hatchback, Taxi, Van, Truck, Bus) có cơ chế cache sprite tối ưu.
  * `HeavyTruck.java`: *(Mới bổ sung, `kind = 9`)* Xe tải hạng nặng cỡ lớn (`136 × 172 px`) chiếm trọn bề ngang một làn đường, đồng bộ bảng màu `CAR_COLORS` và chi tiết thùng hàng container. Chuyên xuất hiện ở làn 1 và làn 3 (trừ màn 2 đường ngập) để chống người chơi đi sát mép biên lách luật.
  * `RoadMotorcycle.java`: Xe máy NPC (`kind = 6`, có cache sprite tối ưu).
  * `ConstructionBarrier.java`: *(Mới bổ sung, `kind = 10`)* Rào chắn thi công sọc phản quang đỏ trắng kèm đèn chớp cảnh báo vàng. Vật cản tĩnh (`fwd = 0.0`) trôi nhanh theo tốc độ đường, tạo chướng ngại vật bất ngờ.
  * `Pothole.java`: *(Mới bổ sung, `kind = 11`)* Ổ gà mặt đường cỡ lớn (`76 × 48 px`) sụt lún có vũng nước đọng phản chiếu (`fwd = 0.0`), luôn vẽ ở lớp mặt đường dưới các xe và kiểm tra quỹ đạo không bao giờ đè hình ảnh lên xe khác; va chạm gây xóc nảy và mất 12 HP.
  * `NinjaLead.java`: *(Mới bổ sung, `kind = 12`)* Xe máy Honda Lead áo chống nắng hoa nhí khẩu trang kín mít, phóng nhanh (`fwd = 0.43`), khi tới gần bật xi-nhan nhấp nháy + báo hiệu `!` rồi tạt đầu cắt ngang làn đường.
  * `Shark.java`: *(Mới bổ sung, `kind = 13`)* Cá mập săn mồi ở Màn 2 (đường ngập) với 4 pha hành vi: (1) lao nhanh từ mép trên màn hình xuống (chỉ lộ vây lưng rẽ nước) rồi lặn ngầm, (2) lặn ngầm ngắn, (3) hiện vòng tròn cảnh báo đỏ dưới chân nhân vật (tự động chọn vị trí không đè lên quỹ đạo xe/thuyền khác), (4) trồi vọt lên há hàm răng nhọn đớp mạnh rồi lặn mất.
  * `IcePatch.java`: *(Mới bổ sung, `kind = 14`)* Vũng băng thực thể (`88 × 54 px`) trên mặt đường Màn 3 (Trời rét, đường trơn), vẽ ở lớp mặt đường; khi cán qua làm xe trượt bánh văng ngang và giảm sâu ma sát ngang.
  * `StrayDog.java`: *(Mới bổ sung, `kind = 15`)* Chó vàng chạy rông ở Màn 4 (Nắng nóng), đứng ở vỉa hè sủa báo hiệu (`"GÂU! !"` kèm đường chấm cảnh báo) rồi phóng cắt ngang qua các làn đường. **Đặc biệt:** 
    * Trang bị AI nhận diện xe cộ: khi gặp xe cộ đường phố (`RoadCar`, `HeavyTruck`, `RoadMotorcycle`) chắn đường phía trước, chó tự động quét tìm khoảng trống và lượn né theo phương tiến (-Y / lên trên) hoặc lùi (+Y / xuống dưới) vào chỗ không có xe, nghiêng thân mình theo góc cua và giảm tốc ngang để lách qua thân xe mượt mà, triệt tiêu hiện tượng chạy đè thẳng lên xe.
    * **Không né nhân vật chính:** Chó chỉ coi các xe đường phố là vật cản để né; đối với xe người chơi, chó vẫn giữ nguyên quỹ đạo lao ngang đường như một chướng ngại vật thực sự, đòi hỏi người chơi phải chủ động quan sát đánh lái né tránh để không bị đâm.
    * Xuất phát từ nhiều hàng đa dạng: phân bổ qua 8 bậc hàng (`185, 235, 285, 335, 385, 435, 475, 510 px`) từ xa phía trước tới ngang hàng nhân vật chính (`475..510 px`), tạo tình huống bất ngờ sát bên xe người chơi.
  * `RoadBoat.java`: Thuyền NPC trên phố ngập (`kind = 8`).
  * `EnergyBolt.java`: Vật phẩm tia sét hồi phục năng lượng cho Ngày 4 nắng nóng (`kind = 7`). Nhịp spawn được cân bằng lại (`2.7s - 3.2s`, bổ sung thêm khoảng 4 tia sét trên toàn map, đạt khoảng 11-13 viên/màn), vừa đảm bảo đủ năng lượng để người chơi sống sót nếu biết quan sát nhặt đồ, vừa duy trì tính thử thách của cơ chế kiệt sức/say sóng.
  * `SchoolBuilding.java`: Cổng trường C1 Bách Khoa (vạch đích hoàn thành chặng đường).
* **Thực thể hỗ trợ & Kịch bản:**
  * `Bird.java`: Chim chướng ngại vật trên không ở chặng bay trực thăng.
  * `Confetti.java`: Hạt pháo giấy chúc mừng kết thúc có hậu (HE).
  * `Line.java`: Một câu thoại trong kịch bản.
  * `Scene.java`: *(Tách từ inner class của GameMenuState)* Model cảnh phân đoạn mở khóa.
  * `MenuButton.java`: *(Tách từ inner class `Btn` của GameMenuState)* Model nút bấm giao diện menu.

### 2.2. Nhóm `sanbaka.state` (Trạng thái trò chơi)
Quản lý toàn bộ vòng đời trạng thái, bộ đếm thời gian, thanh máu/năng lượng và lưu trữ tiến trình:
* `GameState.java`: Lớp cơ sở chứa thông số màn hình (960×640), tốc độ người chơi, timer (16ms ~ 60 FPS), máu trực thăng khởi đầu và tối đa (`FLIGHT_START_HP = 150`), quản lý typewriter hiệu ứng chữ chạy, trạng thái Boss (`boss`), danh sách tên lửa người chơi (`missiles`), máy bay địch (`enemyPlanes`), bom (`enemyBombs`) và đạn Boss (`bossBullets`).
* `GameMenuState.java`: Quản lý 11 phân cảnh mở khóa, menu chọn cảnh (Scene Select), màn hình tạm dừng (Pause), nạp/ghi tệp tiến trình `.sanbaka_svbk_progress.properties`, reset trạng thái run (`resetRunState()`).
* `BossConfig.java`: *(Mới)* Quản lý và nạp toàn bộ thông số cân bằng cho màn Boss (HP, tốc độ, hồi chiêu, sát thương, thời gian các pha) từ tệp cấu hình `boss_config.properties`.

### 2.3. Nhóm `sanbaka.logic` (Luật chơi, Vật lý & Cốt truyện)
Điều phối các quy tắc gameplay và tiến trình hội thoại:
* `GameLogic.java`:
  * Cập nhật chuyển động xe cộ, gia tốc, né làn, va chạm vật lý (hitbox), tụt năng lượng do nắng nóng và điều khiển phím (`keyPressed`, `keyReleased`).
  * **Trận Đại Chiến Trùm Cuối Màn 5 (Boss Fight Không Chiến Trực Thăng):**
    * **Đàn chim đầu màn 5 lượt 2 (15 giây):** Khi lên trực thăng ở 2/3 chặng cuối ngày 5, người chơi bay lượn né tránh các đàn chim (4 đội hình bao quát toàn dải độ cao) trong đúng 15 giây đầu tiên. Khi hết 15 giây và đàn chim đã bay khuất khỏi màn hình, màn hình nhấp nháy đỏ báo động (`STATE_WARNING`) và Boss mới chính thức xuất hiện (`STATE_ENTERING`).
    * **Pha Nạp Máu & Gợi ý Phím [E]:** Boss bay lơ lửng và nạp đầy cây máu từ 0 lên 20 (`intro_charge_ms = 2500ms`). Khi nạp xong, nút `[E]` nhấp nháy cạnh trực thăng gợi ý bắn tên lửa (`firePlayerMissile`, xuất phát từ tầm chân/càng trực thăng, hồi chiêu 0.3s). Máu Boss mặc định 20 phát (`boss.max_hp=20`), mỗi phát tên lửa trúng đích trừ 1/20 cây máu.
    * **Giảm 40% Toàn Bộ Sát Thương Lên Trực Thăng:** Đạn Boss (22 -> 13), Đạn tiêm kích (18 -> 11), Va chạm tiêm kích (32 -> 19), Sát thương nổ bom (28 -> 17), Va chạm chim (30 -> 18).
    * **3 Đòn Tấn Công Luân Phiên Tuần Hoàn:**
      1. *Đòn 1 (Máy bay tiêm kích yểm trợ + Boss bắn tỉa):* Máy bay địch ([`EnemyPlane`](src/main/java/sanbaka/model/EnemyPlane.java)) được thiết kế theo dáng tiêm kích siêu âm hiện đại, kích thước lớn tương xứng (`86×44 px`, nhỏ hơn trực thăng người chơi một xíu với mũi nhọn, ống pitot, buồng lái kính xanh phản quang, cánh delta vuốt lùi, ray tên lửa đầu cánh, cánh đuôi đứng kép và động cơ phản lực kép rực lửa). Tiêm kích bay ngược hướng mật độ vừa, xác suất 50% nhả đạn thẳng; các khoảng trống xen kẽ chim; Boss đảo trục Y siêu nhanh và chiếu tia laser đỏ nhấp nháy ngắm bắn tỉa trực thăng. Luôn có khe ngắm thoáng để người chơi bắn trúng Boss.
      2. *Chuyển giao (Spray & Retreat):* Boss đảo lên xuống liên tục bắn đạn tán xạ ngẫu nhiên, hiện dấu chấm than cảnh báo `!`, rồi lùi vụt nhanh về bên phải khuất khỏi màn hình kèm khung đen mờ và sau ảnh ghost trail biểu thị quá trình vụt biến mất.
      3. *Đòn 2 (Bão chim dày đặc cấp độ cao):* Xuất hiện cảnh báo chấm than to giữa màn hình ("CẢNH BÁO BÃO CHIM CẤP ĐỘ CAO · BẤM [E] BẮN PHÁ MỞ ĐƯỜNG!"), đàn chim bão táp tốc độ cao (`470 px/s`) xuất hiện với mật độ dày đặc tràn ngập bầu trời (sinh đồng thời 3-4 chim ở 4 dải độ cao, nhịp spawn rút ngắn còn 220ms), buộc người chơi phải liên tục dùng phím E bắn tên lửa dọn đường.
      4. *Đòn 3 (Không kích rải bom chéo thưa & to, nổ diện rộng):* Boss quay trở lại với hiệu ứng vụt quay về, bắn loạn xạ đạn chào sân; sau đó phi đội tiêm kích bay vút qua hàng trên cùng (`y = 45..85`); các trái bom ([`EnemyBomb`](src/main/java/sanbaka/model/EnemyBomb.java)) được thả thưa hơn (`attack3_bomb_interval_ms = 780ms`) nhưng kích thước tăng lớn (`38×38 px`), rơi chéo xuống dưới. Khi bom bị bắn trúng hoặc chạm đất, phát nổ với bán kính sát thương diện rộng (`blastRadius = 76.0 px`), sinh hiệu ứng quầng lửa và sóng xung kích ([`BombExplosion`](src/main/java/sanbaka/model/BombExplosion.java)), gây sát thương cho trực thăng nếu đứng gần, phá hủy chim và kích nổ dây chuyền bom lân cận. Hết đòn 3, Boss tuần hoàn quay lại Đòn 1.
    * **Độ Bền & Khả Năng Bị Phá Của Các Thực Thể:**
      * Máy bay tiêm kích địch: cần 3 phát tên lửa (`enemyPlaneHp = 3`), có vạch máu trên đầu và hiệu ứng chớp trắng khi dính đạn.
      * Chim: 1 phát tên lửa (`enemyBirdHp = 1`).
      * Bom: 1 phát tên lửa (`enemyBombHp = 1`), khi bị phá hủy phát nổ trong bán kính sát thương (blastRadius = 76.0 px), kích nổ dây chuyền các bom và tiêu diệt chim lân cận.
    * **Kết Thúc Trận Đánh:** Khi Boss hết máu, Boss phát nổ, vụt bay mất về bên phải; trực thăng người chơi tiếp tục bay thảnh thơi tới trường thi C1 và hoàn thành màn chơi dẫn tới Happy Ending (HE).
  * **Cơ chế Quãng đường cố định & Tách biệt Môi trường (`stageProgressMs`, `currentProgressFactor`, `scroll`):** Nếu người chơi đi thẳng thuận lợi, màn chơi hoàn thành trong đúng 40 giây (`ROAD_DURATION_MS = 40_000`). Nếu dính hiệu ứng làm chậm (va chạm đẩy lùi, băng lạnh, kiệt sức, cạ lề màn 2), tiến trình người chơi `stageProgressMs` và vận tốc xe thực sự chậm lại. Trong trận Boss, `stageProgressMs` được giữ lại trước vạch đích trường học cho đến khi Boss bị hạ gục. **Đặc biệt:** Tốc độ cuộn mặt đường (`scroll = base * factor`), dòng xe cộ giao thông xung quanh, mây trời và đàn chim được **tách biệt hoàn toàn (decoupled)** khỏi tốc độ người chơi, giữ nguyên nhịp trôi thực tế tự nhiên, không bị hiệu ứng "bullet-time" làm chậm theo người chơi.
  * **Cơ chế Lề đường phân hóa (Màn 2 vs Các màn khác):**
    * *Ở Màn 2 (đường ngập lụt):* Bỏ hoàn toàn bốc lửa và sát thương lửa. Thuyền cạ mép vỉa hè sẽ bị bùn cạn làm tốc độ chậm dần về 50% (`map2CurbSlowFactor -> 0.50`) kèm hiệu ứng nước té vỉa hè (`drawBoatCurbSplash`). Đánh lái thoát ra làn giữa sẽ hồi phục dần về 100%.
    * *Ở các màn khác:* Đi sát mép biên (`CURB_ZONE_MARGIN = 26 px`) quá **3 giây** sẽ bốc lửa dữ dội và đốt **10 máu/giây** (`CURB_BURN_DPS = 10.0`); thoát lề đường sẽ hạ nhiệt và tắt lửa dần.
  * **Cơ chế Điều khiển & Cân bằng Gió mùa Màn 3 (Băng giá):**
    * Khi xe chạy trên mặt đường bình thường, **loại bỏ hoàn toàn gia tốc quán tính ngang** (`playerVx = 0`); tay lái nhạy bén, ăn lái trực tiếp như mọi map khác. Quán tính trượt ngang chỉ kích hoạt khi xe cán qua Vũng băng thực thể (`IcePatch`).
    * **Cân bằng lực & Tăng tần suất gió:** Gió thổi thường xuyên hơn (xuất hiện sớm từ giây 2.4, thời gian chờ giữa các đợt gió giảm xuống còn 2.5s - 3.9s, tạo khoảng 6-7 đợt gió/màn). Lực đẩy gió ở mức cân bằng tự nhiên (`105 px/s`) kèm đường cong gia tốc hình chuông mềm mại (`gustEnvelope = sin(...)`), người chơi luôn giữ trọn quyền kiểm soát tay lái (`240 px/s` dễ dàng áp chế gió để lách làn). Cơ chế giảm lực gần mép vỉa hè (`< 36 px`) ngăn chặn hoàn toàn việc xe bị gió ép chặt vào lề đường.
  * **Cơ chế Ảo giác Mê sảng & Độ hiếm Năng lượng Màn 4 (Nắng nóng):**
    * Bổ sung thêm khoảng 4 tia năng lượng trên toàn map (nhịp spawn rút từ 4.0s - 5.0s xuống 2.7s - 3.2s, đạt ~11-13 viên/màn kèm cơ chế retry thông minh nếu vị trí spawn bị vướng xe), giúp màn chơi không bị cạn kiệt năng lượng quá gắt nhưng vẫn tạo thử thách nhặt đồ để tránh bị ảo giác say sóng.
    * Khi năng lượng hạ xuống dưới 50% (`energy < 50.0`), khung cảnh đường sá bắt đầu lắc lư, bồng bềnh, nghiêng ngả và co giãn nhịp thở như say sóng (`deliriumIntensity = (50 - energy) / 50.0`), tăng dần cường độ khi năng lượng càng thấp về 0, kèm theo độ trĩu tay lái và sóng nhiệt chập chờn.
  * **Nâng sát thương va chạm, Đẩy lùi & Làm chậm ngắn (`applyCollisionHit`):** Tăng sát thương va chạm (Xe tải to: 38 HP, Cá mập: 34 HP, Ô tô/Xe buýt/Rào chắn: 32 HP, Chim: 30 HP, Xe máy/Ninja Lead/Chó: 28 HP, Ổ gà: 16 HP). Khi đâm xe, người chơi bị hất đẩy lùi về phía sau (`knockbackVy`, `knockbackVx`) kèm hiệu ứng làm chậm ngắn (`hitSlowUntil`), triệt tiêu hoàn toàn lối chơi thả tay chịu đâm qua màn.
  * **Cơ chế Spawn (`trySpawnVehicle`, `clearToSpawn`) & Chim trực thăng bao quát (`updateFlight`):** Giữ nguyên tỉ lệ và tần suất xuất hiện của các phương tiện cũ (`0..6`), bổ sung `HeavyTruck` (`kind = 9`, rộng `136 px` kín 1 làn) ưu tiên ở làn 1 và làn 3. Ở chặng bay trực thăng (Ngày 5), chim xuất hiện bao quát toàn bộ chiều dọc vùng bay `[76..504]` với 4 mẫu đội hình (3 tầng Biên trên - Giữa - Biên dưới, Đón độ cao trực thăng + Khóa biên, Chữ V rộng, và Chim rải đều) để không còn góc chết an toàn ở mép trên/dưới.
  * **Cơ chế Tắc đường Ngày 5 (`buildJam`):** Xếp các hàng ô tô/xe buýt/xe tải trên 3 làn chính, sau đó tự động lấp kín mọi khoảng trống giữa các xe và hai bên vỉa hè bằng hàng chục xe máy (`RoadMotorcycle`) để tái hiện cảnh tắc đường đặc kín chân thực, không còn kẽ hở vô lý.
* `GameStory.java`: Quản lý timeline hội thoại, cuộc gọi điện thoại (`isCallLine`), chuyển cảnh mờ (Fade), thông báo phân cảnh (Card).
* `StoryData.java`: Toàn bộ kịch bản và 118 câu thoại chi tiết cho cả 5 ngày thi.

### 2.4. Nhóm `sanbaka.render` (Tầng Hiển thị & Đồ họa)
Phân chia các lớp vẽ chuyên biệt bằng Java 2D:
* `GameVisuals.java`: Các hàm vẽ hiệu ứng đồ họa cơ sở: tia sét, trái tim, ngôi sao, đám mây, vệt nước rẽ sóng, máy bay trực thăng, chim (`Bird`), avatar nhân vật và các hiệu ứng trạng thái mới: **Xe bốc lửa (`drawVehicleFireEffect` — `VehicleFire.png`)**, **Chấn động đẩy lùi & làm chậm (`drawCollisionSlowEffect` — `CollisionImpact.png`)**, **Đóng băng xe (`drawFreezeEffect` — `FreezeEffect.png`)**, **Bọt nước cạ lề mắc cạn (`drawBoatCurbSplash`)** (tất cả đều hỗ trợ nạp ảnh PNG riêng từ `assets/objects/` cho đội làm hình ảnh hoặc tự động fallback vẽ vector Java 2D).
* `GameEnvironmentRenderer.java`: Vẽ cảnh nền: phòng trọ, hiệu ứng ngập nước mưa rơi, cảnh đồi núi bầu trời chặng trực thăng.
* `GameCharacterRenderer.java`: Vẽ giao diện HUD, thanh máu (tự động co dãn 100 HP cho đường bộ và 150 HP cho trực thăng màn 5), thanh năng lượng và người lái xe máy. Loại bỏ dòng đếm ngược thời gian còn lại (`THỜI GIAN  %02d:%02d`) ở tất cả các màn để giao diện gọn gàng, thanh thoát; giữ nguyên thanh tiến trình quãng đường "ĐƯỜNG ĐI" / "ĐƯỜNG BAY".
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
│   │   │       │   ├── BombExplosion.java       *(mới: hiệu ứng nổ bom diện rộng)*
│   │   │       │   ├── BossBullet.java          *(mới: đạn Boss & máy bay)*
│   │   │       │   ├── BossEnemy.java           *(mới: Trùm Cuối không chiến Màn 5)*
│   │   │       │   ├── Confetti.java
│   │   │       │   ├── ConstructionBarrier.java *(mới: rào chắn thi công)*
│   │   │       │   ├── EnemyBomb.java           *(mới: bom nổ bán kính diện rộng)*
│   │   │       │   ├── EnemyPlane.java          *(mới: máy bay yểm trợ & phi đội)*
│   │   │       │   ├── EnergyBolt.java
│   │   │       │   ├── HeavyTruck.java          *(mới: xe tải to kín 1 làn)*
│   │   │       │   ├── IcePatch.java            *(mới: vũng băng trượt bánh)*
│   │   │       │   ├── Line.java
│   │   │       │   ├── MenuButton.java          *(tách từ inner class)*
│   │   │       │   ├── NinjaLead.java           *(mới: xe Ninja Lead tạt làn)*
│   │   │       │   ├── PlayerBike.java
│   │   │       │   ├── PlayerBoat.java
│   │   │       │   ├── PlayerMissile.java       *(mới: tên lửa trực thăng phím E)*
│   │   │       │   ├── PlayerVehicle.java
│   │   │       │   ├── Pothole.java             *(mới: ổ gà mặt đường)*
│   │   │       │   ├── RoadBoat.java
│   │   │       │   ├── RoadCar.java
│   │   │       │   ├── RoadMotorcycle.java
│   │   │       │   ├── Scene.java               *(tách từ inner class)*
│   │   │       │   ├── SchoolBuilding.java
│   │   │       │   ├── Shark.java               *(mới: cá mập săn mồi Màn 2)*
│   │   │       │   ├── StrayDog.java            *(mới: chó chạy rông né xe Màn 4)*
│   │   │       │   └── Thing.java
│   │   │       ├── render/                      # [CHỨC NĂNG 4] Đồ họa & Giao diện
│   │   │       │   ├── GameCharacterRenderer.java
│   │   │       │   ├── GameEnvironmentRenderer.java
│   │   │       │   ├── GameMenuRenderer.java
│   │   │       │   ├── GameRoadRenderer.java
│   │   │       │   ├── GameScreenRenderer.java
│   │   │       │   └── GameVisuals.java
│   │   │       └── state/                       # [CHỨC NĂNG 5] Trạng thái & Tiến trình
│   │   │           ├── BossConfig.java          *(mới: nạp bảng thông số cân bằng Boss)*
│   │   │           ├── GameMenuState.java
│   │   │           └── GameState.java
│   │   └── resources/
│   │       ├── assets/                          # Tài nguyên đóng gói classpath
│   │       └── boss_config.properties           # Tệp thông số cân bằng Boss đóng gói classpath
│   └── test/
│       └── java/
│           └── sanbaka/
│               └── SanbakaSmokeTest.java        # Unit test tự động kiểm thử toàn diện
├── .gitignore
├── boss_config.properties                       # Bảng thông số cân bằng Boss độc lập
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
  *Kết quả test:* Đạt 100% các bài test về StoryData (118 câu thoại), SceneBuilding (11 phân cảnh), ArtAssets fallback, khởi tạo thực thể qua Factory Method, Quãng đường cố định/Làm chậm, Tách biệt cuộn cảnh với người chơi, Lề đường Màn 2 giảm tốc không cháy, Lái trực tiếp không quán tính mặt đường Màn 3, Cân bằng lực gió Màn 3 giữ quyền kiểm soát lái, Ảo giác mê sảng say sóng Màn 4 khi Energy < 50%, Bổ sung ~4 tia năng lượng Màn 4 (đạt 11-13 viên/màn), Chó chạy rông Màn 4 né xe cộ đường phố và không né nhân vật chính, Vòng đời Trùm Cuối Màn 5 (Máu trực thăng 150 HP, Boss nạp máu, 3 đòn luân phiên tuần hoàn, nạp file `boss_config.properties`), Bắn tên lửa trực thăng phím E, Độ bền thực thể (Máy bay 3 hit, Bom 1 hit, Chim 1 hit, Boss hạ gục khi cạn HP), và Kích nổ dây chuyền bom diện rộng (blastRadius = 76-90 px).

---

## 5. Định hướng phát triển tiếp theo (Roadmap)
1. **Đội ngũ Artist gắn ảnh PNG:** Bổ sung sprite hình ảnh vào `assets/objects/Boss/Boss.png`, `EnemyPlane/EnemyPlane.png`, `Missile/Missile.png`, `Bomb/Bomb.png`. Hệ thống `ArtAssets` đã dựng sẵn cầu nối nạp ảnh tự động.
2. **Cân bằng game designer:** Điều chỉnh trực tiếp các giá trị số trong `boss_config.properties` mà không cần biên dịch lại mã nguồn Java.
3. **Hội thoại:** Bổ sung sprite cảm xúc cho nhân vật tương ứng với các phân đoạn đối thoại.
