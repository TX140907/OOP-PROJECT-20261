# HUST Exam Run — Java OOP 2D

Game né chướng ngại vật trên đường đi thi. Bản hiện tại có **6 level, No-Story Mode**,
chạy bằng Java Swing/Java2D và hình khối placeholder. Chưa triển khai hội thoại,
cutscene hoặc âm thanh từ `Story.docx`.

## Dành cho người làm hình ảnh

### Thư mục và kích thước

```text
src/main/resources/
├── image/
│   ├── common/player/     player.png, face.png dùng chung
│   ├── day1/              background.png, road.png, vehicle.png, bicycle.png, barrier.png
│   ├── day2/              background.png, road.png, fin.png, ripple.png, warning.png,
│   │                      shark.png, drain.png, floatingCrate.png, health.png
│   ├── day3/              background.png, road.png, icePatch.png, iceHole.png, snowball.png, snowman.png, health.png
│   ├── day4/              background.png, road.png, bus.png, cactus.png, tumbleweed.png, energy.png, health.png
│   └── day5/              background.png, road.png, trafficJamVehicle.png, dog.png,
│                          helicopter.png, bird.png, enemyPlane.png,
│                          missile.png, missile-warning.png, dog-warning.png, health.png,
│                          boss.png, boss-portrait.png, boss-projectile.png,
│                          boss-projectile-warning.png, player-rocket.png, explosion.png, shield.png,
│                          reverse-plane-warning.png
└── audio/                 Chưa có âm thanh thực tế
```

Tên file trên là đường dẫn cấu hình mặc định, **ảnh chưa được tạo**. File `.gitkeep`
chỉ giữ các thư mục trống. Có thể đổi đường dẫn trong properties của level.

| Thành phần | Kích thước hiển thị mặc định (px) |
| --- | --- |
| Nền toàn màn hình | 1000 × 700 |
| Đường, đã gồm ba làn | 720 × 700; mỗi làn 240 |
| Nhân vật đường bộ màn 1–4 | 100 × 100; hitbox 88 × 86 ở giữa |
| Người đi bộ màn 51 | 84 × 90; hitbox 74 × 78, lệch 5 px ngang và 6 px dọc |
| Xe / xe đạp / rào | 180 × 210 / 80 × 150 / 180 × 70 |
| Cá / vùng cảnh báo | 160 × 160 |
| Vây / dấu nước lúc lặn | 36 × 40 / 24 × 24 (placeholder) |
| Cống / thùng trôi | 170 × 100 / 160 × 120 |
| Băng / hố băng sụp / cầu tuyết / người tuyết | 180 × 90 / 170 × 100 / 160 × 160 / 160 × 170 |
| Bus / xương rồng / cỏ khô / năng lượng | 200 × 260 / 100 × 220 / 120 × 120 / 34 × 34 |
| Xương rồng sau khi đổ / vùng cảnh báo | 220 × 100; dùng chính ảnh `cactus.png` đang đứng, xoay quanh gốc |
| Xe tắc đường / chó / cảnh báo chó | 190 × 210 / 100 × 70 / 42 × 42 |
| Trực thăng / chim | 120 × 90 (hitbox 84 × 54 ở giữa, lệch 18 px mỗi trục) / 170 × 45 |
| Máy bay đối đầu / tên lửa / cảnh báo tên lửa | 240 × 140 (rộng một làn) / 18 × 36 / 52 × 52 |
| Vật phẩm hồi máu màn 2, 3, 4 và 52 | 40 × 40; hộp đỏ có dấu cộng trắng khi thiếu ảnh |
| Boss / ảnh đại diện boss | 100 × 150 / 44 × 44 |
| Vật ném boss / cảnh báo | 50 × 50 / 48 × 48 (đòn 1), 72 × 72 (đòn 2) |
| Tên lửa người chơi / hiệu ứng nổ | 14 × 30 / tối đa 52 × 52 |
| Vật phẩm khiên | 44 × 44 |
| Avatar đầu thanh tiến độ | 28 × 28 |

Đường ở X=140–860, lề trang trí **140 px mỗi bên**. Nhân vật đường bộ màn 1–4 ở Y=520,
chỉ di chuyển ngang. Màn trực thăng cũng giới hạn trong X=140–860, giữ lề
140 px mỗi bên và bố cục ba làn ở giữa; trực thăng rộng 120 px nên X tối đa là 740. Kích thước trên có thể
cân chỉnh trong properties; người code cần cập nhật tài liệu nếu đổi tỉ lệ.
Người đi bộ màn 51 ở Y=530, giới hạn X=140–776; chân vẫn ngang Y=620 như trước.

Xuất PNG nền trong suốt cho nhân vật, vật cản và hiệu ứng; hạn chế khoảng trống
quanh sprite để hình ảnh khớp collider. Không vẽ lại vạch đường bằng code đè lên
`road.png` thật, trừ khi bật `road.overlayLines=true`.

Ngày 3 thêm `iceHole.png`: hố tối với mép băng nứt, khác với sàn băng sáng.
Ngày 4 thêm `tumbleweed.png`: quả cỏ khô **tròn**, PNG nền trong suốt vừa sát
hình tròn; game tự xoay ảnh khi lăn và ngừng xoay khi tới làn.
`cactus.png` là chính cây xương rồng **đứng 100 × 220** trên đường, gốc giữa cạnh dưới;
game dùng cùng ảnh này xoay ngang quanh gốc, không cần ảnh cây đổ riêng.
Khi nằm, vùng bao ngoài là 220 × 100. Vùng va chạm theo thân/nhánh hình tạm;
nên giữ dáng ảnh sát hình tạm, hạn chế lề trong suốt. Cỏ khô và cây có hình tạm
nếu thiếu ảnh. Thanh năng lượng ngày 4 hồi cả năng lượng lẫn 1 máu.
Làn băng cao trào ngày 3 ở bên phải có nền xanh, viền sáng và vết nứt cuộn theo
đường do code vẽ; các hố tối dùng `iceHole.png` là vật cản thật, khác vết nứt trang trí.

### Các trạng thái cá mập cần ảnh

1. **FIN_VISIBLE:** vây bơi từ biên trên, mặc định 0,7 giây.
2. **DIVING:** lặn, có dấu nước nhỏ trong ít nhất 0,9 giây.
3. **WARNING:** vùng nước tối và dấu `!` nhấp nháy, 1,2 giây, ngang hàng nhân vật.
4. **ATTACK:** trồi lên đớp trong 0,4 giây, đúng chỗ đã cảnh báo.
5. **DISAPPEAR:** biến mất, không còn va chạm.

Chỗ lặn và chỗ đớp là hai vị trí riêng. Cá chờ thêm nếu chưa có vị trí tấn công an toàn.

Máy bay đối đầu và tên lửa hướng mũi xuống; chim giữ hướng bay ngang.
Máy bay hiện dùng chung đường viền đa giác cho hình tạm và va chạm, bỏ các góc
trống quanh cánh/mũi. Khi ghép `enemyPlane.png`, cần khớp thân/cánh với đường viền
trong `EnemyPlane.collisionShape()` hoặc nhờ người code chỉnh đường viền theo ảnh.
Cảnh báo tên lửa nhấp nháy trong **0,2 giây**,
trên đường đạn thẳng dọc, trước khi tên lửa rời máy bay. Chuẩn bị `missile-warning.png` trong suốt;
logic nhấp nháy đã có sẵn, không cần làm GIF.

### Hình ảnh boss màn 52

Boss xuất hiện sau phần né vật cản 15 giây và đoạn chờ đường thông. Cần ảnh boss
toàn thân `boss.png`, hướng nhìn về người chơi; hiện dùng hình chữ nhật tím 100 × 150.
Ở đòn 1 boss đứng trong lề, ở đòn 2 boss chuyển mượt giữa ba làn.
`boss-portrait.png` là ảnh vuông 44 × 44 tại mép trái thanh máu. Thanh máu tự nạp
từ trống tới đầy trong 1,8 giây, ảnh đại diện giữ nguyên.

`boss-projectile.png` là vật nhỏ boss ném; `boss-projectile-warning.png` là ô cảnh báo,
code tự nhấp nháy. Với đòn 2, cảnh báo 72 × 72 nằm trên hàng nhân vật, cách thân
18 px và vẽ sau nhân vật; nên vẽ tam giác vàng/chấm than rõ ở kích thước này.
Cảnh báo đòn 1 vẫn đặt ngay tại đích với kích thước 48 × 48.
`player-rocket.png` hướng mũi lên, `explosion.png` là hiệu ứng nổ.
`shield.png` là vật phẩm khiên, viền xanh quanh trực thăng được vẽ khi đang bảo vệ.
Đòn 1 và đòn 3 có tia ngắm mờ nối từ tay boss tới ô dấu chấm than; sau đó đạn bay thật
theo hướng này. Tia được vẽ bằng code, không cần làm ảnh riêng.
Đòn 3 nối sau đòn 1, dùng `reverse-plane-warning.png` 52 × 52, có mũi tên lên/dấu chấm than gần
biên dưới; code tự nhấp nháy trước khi máy bay lao lên. Kiểu tuần tự hiện cảnh báo
lần lượt ở làn 1, 2, 3 cách nhau 0,5 giây; dùng chung ảnh cảnh báo này. Ảnh `enemyPlane.png` được
quay 180 độ cho đợt bay ngược, không cần vẽ một bộ ảnh máy bay khác.
Khi xuyên vật cản, code giảm độ rõ của nhân vật; không cần vẽ sẵn bản mờ.
Các ảnh này đều nằm trong `image/day5/`. Gợi ý `!` và phím E cạnh nhân vật dùng hình tạm.

### Chó: băng ngang, sủa và lao

Chó băng ngang tới tâm một làn ngẫu nhiên, dừng **0,8 giây** ở trạng thái `BARKING`,
hiện dấu `!` nhấp nháy và đánh dấu nơi lao tới. Sau đó chó lao trong **0,45 giây**,
lệch tối đa **±60 px** quanh tâm làn đó, giữ toàn thân trong làn. Điểm lao chốt từ
trước cảnh báo, không đổi đích đuổi theo người chơi. Hết cú lao, chó đứng lại đúng
điểm đáp trong thế giới; cảnh cuộn đưa nó dần ra khỏi màn. Va chạm không làm chó
biến mất (`dog.consumed=false`). Chó và xe tắc đường đều gây sát thương thường.

`dog-warning.png` là ảnh cảnh báo 42 × 42 px, thiếu ảnh thì dùng ô cam có dấu `!`.
Âm thanh sủa chưa được phát; trạng thái `BARKING` là chỗ ghép âm thanh về sau.

### Cách tích hợp và xem thử

Game đã hỗ trợ nạp **sprite của nhân vật, tất cả vật cản, các trạng thái cá mập,
ảnh nền, ảnh đường và avatar HUD** qua classpath; chưa có sprite thì dùng hình tạm.
Không cần sửa logic gameplay để thêm một ảnh tĩnh. Mỗi frame animation nhiều ảnh
cần được người code tích hợp thêm bộ phát animation, giữ nguyên hitbox.

Đặt ảnh vào đúng thư mục, rồi chạy lại `run.cmd` để build và cập nhật JAR.
Để xem riêng ngày 2: `run.cmd --level=2 --no-auto`. Level hiện tại ghi trên HUD.
Khi bàn giao animation, ghi rõ kích thước khung, số khung, thứ tự và tốc độ phát.

## Dành cho người code

### Yêu cầu và chạy

- **JDK 17+**, Maven 3.9+, Windows để dùng `run.cmd`.
- Không cần IDE, không cần game engine hoặc thư viện runtime ngoài JDK.
- Maven cần mạng lần đầu để tải plugin/JUnit; các lần sau có thể dùng cache.
- `Main.java` là entry point duy nhất. Không chạy lại `main.java`/`.class` của bản cũ.

Tại thư mục dự án:

```powershell
.\run.cmd                         # Build rồi chạy toàn bộ No-Story flow
.\run.cmd --level=3 --no-auto      # Chơi riêng ngày 3, hoàn thành thì dừng
.\run.cmd --level=51              # Bắt đầu phần đường ngày 5, rồi tự sang trực thăng
.\run.cmd --build-only            # Chỉ build JAR
.\run.cmd --smoke                 # Kiểm tra logic/render cả 6 level, không mở cửa sổ
```

Nếu terminal ở thư mục cha `F:\OOP project`, gọi `.\OOP-PROJECT-20261\run.cmd`.
Script dùng Maven trên PATH; trong workspace này có thể dùng bản local
`.tools/apache-maven-3.9.9/bin/mvn.cmd`. `.tools/` không được commit.

```powershell
mvn clean test
mvn package
java -jar target/exam-run.jar
java -jar target/exam-run.jar --level=52 --no-auto
```

Sau build, JAR chạy độc lập và không cần source tree/IDE. Smoke tạo ảnh kiểm tra
trong `target/smoke/`, không phải Developer Debug Mode. Không có F3 overlay.

### Chọn màn và điều khiển

Ba biến ở đầu `Main.java`:

```java
private static final boolean RUN_ALL_LEVELS = true;
private static final int SELECTED_LEVEL = 1;
private static final boolean AUTO_ADVANCE = true;
```

Muốn test riêng: đặt `RUN_ALL_LEVELS=false`, chọn mã và đặt `AUTO_ADVANCE=false`.
Có thể dùng CLI như trên mà không sửa Java. `--all` chạy từ ngày 1, `--no-auto`
dừng ở Level Complete. Luồng chính: **1 → 2 → 3 → 4 → 51 → 52 → Game Complete**.

| Mã | Level | Cơ chế |
| --- | --- | --- |
| 1 | Day1Level | Đầu xe/xe đạp chí mạng, cạnh/phía sau trừ 1 máu; rào trừ 1 máu |
| 2 | Day2Level | Cá đớp trừ 1 máu và làm chậm; cống trừ 1 máu và làm chậm; thùng gỗ trừ 1 máu; khoảng 5 hộp hồi máu rải ngẫu nhiên |
| 3 | Day3Level | Sàn băng tăng tốc/gia tốc 1,5 giây; hố băng trừ máu và làm chậm; bóng tuyết đóng băng rồi làm chậm |
| 4 | Day4Level | Bus, xương rồng thường/đổ ngang, cỏ khô lăn rồi dừng; chói mạnh; năng lượng tụt 4/giây; pickup hồi năng lượng, 1 máu và tăng tốc |
| 51 | Day5RoadLevel | 25 giây tiến độ; xe tắc đường và chó băng ngang/sủa/lao rồi đứng lại |
| 52 | Day5HelicopterLevel | Né chim/máy bay 15 giây ở mức khó cố định, sau đó đấu boss bằng tên lửa E |

Các màn **1–4 kéo dài 40 giây tiến độ hiệu dụng**. Màn nước, băng và sa mạc
mỗi màn rải ngẫu nhiên 5 hộp hồi 1 máu, có kiểm tra đường nhặt. Thùng gỗ gây
**1 máu** qua `floatingCrate.collisionType=NORMAL_DAMAGE` và `collision.normalDamage=1`.

Xe ô tô, xe đạp và xe buýt vẫn gây chí mạng khi đâm vào **mặt trước** (mép dưới
của xe đang chạy xuống). Quệt cạnh hoặc chạm phía sau gây **1 máu** qua
`collision.normalDamage=1`, có nhấp nháy và miễn sát thương tạm thời như va chạm thường.
Hướng va chạm lấy từ chuyển động tương đối lúc bắt đầu tiếp xúc; chỉ tính một
lần cho đến khi tách ra, không chuyển quệt cạnh thành chí mạng ở khung hình sau.

Trong properties màn 2–4, `spawn.maxObstaclesPerLane=3` giới hạn số vật cản
trên mỗi làn trước khi qua người chơi, còn `spawn.sameLaneGapPixels=100` giữ
khoảng hở dọc khi sinh/chọn vị trí cá mập. Màn 1 giữ 2 vật/làn và khoảng hở 180 px.
Một vật chắn hai làn được tính ở cả hai.
Vật phẩm không bị tính vào giới hạn này. Đặt từng giá trị bằng `0` để tắt giới hạn
tương ứng; các kiểm tra đường né vẫn được giữ.

- **A/←, D/→:** di chuyển ngang tự do, có tăng/giảm tốc; không snap giữa làn.
- **E:** bắn tên lửa thẳng lên khi boss đã nạp máu xong; có thể giữ phím để bắn theo hồi chiêu.
- **Esc:** mở menu tạm dừng/tiếp tục. Trong danh sách ô lưu, Esc quay về menu trước. Mất focus tự pause và xóa phím giữ.
- **R:** chơi lại màn hiện tại sau Game Over/Level Complete/Game Complete.
- Không điều khiển dọc ở các màn đường bộ. Freeze xóa vận tốc/input đang giữ.

### Menu đầu game, tạm dừng và ba ô lưu

Mở ứng dụng sẽ vào menu **Vào game / Chơi màn đã lưu / Thoát game**.
Vào game bắt đầu lượt mới từ màn được chọn bởi Main hoặc CLI (`--all` / `--level`).
Trong game, Esc mở menu **Tiếp tục chơi / Chơi lại màn / Lưu game / Ra menu / Thoát game**.
Các nút có hiệu ứng khi trỏ chuột, khi nhấn và khi chọn bằng Tab; có thể bấm bằng chuột
hoặc dùng Space cho nút đang focus. Ở màn kết thúc cũng có nút chơi lại, ra menu và thoát.

Chọn **Lưu game** khi đang tạm dừng để chọn một trong đúng **3 ô**.
Ô đã có dữ liệu yêu cầu xác nhận ghi đè; lưu xong vẫn ở menu pause.
Chọn **Chơi màn đã lưu** ở menu đầu game rồi bấm một ô có dữ liệu để chơi lại màn đó.
Tải sẽ tạo session mới **từ đầu màn**, khôi phục cách tự chuyển màn của lượt đã lưu;
HP, năng lượng, vật cản, cooldown và tiến độ trong màn đều khởi tạo lại theo properties.
Ví dụ lưu giữa boss màn 52 thì tải lại từ phần né chướng ngại vật đầu màn 52.
Ra menu hoặc thoát không tự lưu; Vào game cũng không ghi đè các ô có sẵn.

Bản lưu nằm ở `saves/slot-1.properties` tới `slot-3.properties`.
Chỉnh `save.directory` trong `game/config/game.properties` để đổi thư mục.
Đường dẫn tương đối tính từ thư mục chạy ứng dụng; `run.cmd` luôn chạy từ thư mục dự án,
IDE cần đặt working directory về cùng thư mục nếu muốn dùng chung bản lưu.
Thư mục mặc định `saves/` đã được bỏ qua bởi Git. Muốn mang bản lưu sang máy khác,
sao chép các tệp này vào thư mục lưu tương ứng. Mỗi ô đọc/ghi lỗi được báo trong menu.

`game.save.SaveData` chỉ lưu `levelId`, tên màn, `checkpoint`, `dialogueId`,
`autoAdvance`, `savedAt`; `SaveRepository` thêm phiên bản định dạng `version=1`.
Không lưu cả cây đối tượng Java hoặc ảnh chụp trạng thái giữa khung hình. Chỉ đọc ba ô
khi mở danh sách hoặc bấm tải; không đọc ổ đĩa mỗi frame, không ghi liên tục.
Ghi tệp tạm cùng thư mục rồi thay thế tệp đích để hạn chế bản lưu dở dang.
Dữ liệu sai/phiên bản lạ không được tải, vẫn cho ghi đè từ menu lưu.

Hiện chỉ tạo mốc `LEVEL_START`. Kiểu `DIALOGUE_START` và trường `dialogueId` đã được
chừa trong định dạng để về sau lưu **đầu cuộc hội thoại**, không lưu chỉ số câu đang nói.
Khi thêm hệ hội thoại/cutscene, nối bộ tạo mốc và nhánh khôi phục trong
`Game.createSave()` / `Game.loadSave()` vào bộ điều khiển hội thoại. Bản hiện tại
chưa phát hội thoại, nên không tạo hoặc tải giả các mốc này.

### Điểm nhấn riêng của màn 1–4

| Màn | `event.type` | Mốc bắt đầu cảnh báo theo tiến độ | Đoạn đặc biệt |
| --- | --- | --- | --- |
| 1 | `convoy` | 24 giây | Tối đa 3 đợt, mỗi đợt 2 xe lệch dọc 60 px và chừa 1 làn; khoảng trống đầu gần nhân vật, các lần sau chỉ đổi sang làn kề bên |
| 2 | `sharkRush` | 14 giây | Tối đa 5 đội hình: đớp một làn, hai làn đồng thời hoặc lần lượt làn 1–2–3; mỗi kiểu khoảng 33,3%, chuỗi lệch 0,5 giây; cống/thùng/cá thường tạm ngừng sinh |
| 3 | `iceChoice` | 16 giây | Làn băng xanh bên phải kéo dài 12 giây, tối đa 6 đợt cách ít nhất 1,5 giây; hố bên băng, người tuyết đổi giữa làn giữa và đường khô. Trượt lâu tăng tốc từ 20% tới tối đa 70% |
| 4 | `heatCycle` | 6 giây | Sau báo trước, lặp 5 giây nắng gắt và 3 giây nhìn rõ; lớp chói tăng/giảm trong 0,6 giây, mật độ sinh khi nắng gắt còn 90% |

Màn 2–4 dùng `spawn.mode=independent`, nhịp sinh thường **0,45–0,75 giây** trước
hệ số độ khó: mỗi loại có xác suất riêng và có thể cùng được chọn trong một lượt.
Màn 1 giữ nhịp `1.0–1.55` giây, đoàn xe cách tối thiểu `2.6` giây.
Giới hạn dồn làn, không chồng vật cản và bộ tìm đường vẫn được áp dụng;
không tìm được vị trí an toàn thì bỏ lượt hoặc thử lại, không ép sinh.

Các màn vẫn có thời lượng **40 giây tiến độ**. HUD nhắc khi đoạn đặc biệt bắt đầu;
đoạn băng ghi rõ bên tăng tốc và bên đường khô. Mốc bắt đầu dùng tiến độ hiệu dụng,
còn khoảng cách giữa các đợt và chu kỳ nắng dùng giây chơi thực; pause dừng cả hai.

Đoàn xe, đợt cá và đoạn băng ngừng sinh thường từ lúc cảnh báo, chờ vật cản cũ
đi qua rồi mới bắt đầu. Mỗi hàng được xét **cả nhóm** qua giới hạn dồn làn, khoảng hở
và bộ tìm đường dự báo chuyển động. Nếu không đặt được an toàn thì thử lại sau
`event.retrySeconds=0.35`, không ép sinh và không xóa vật cản để mở chỗ.
Chờ đường cũ quá `event.clearTimeoutSeconds=7` thì bỏ sự kiện; thử sinh quá
`event.maxDurationSeconds` (màn 1: 10, màn 2: 16, màn 3: 12 giây) thì bỏ các đợt còn lại và chờ đường thoáng để trả
quyền sinh thường. Riêng đoạn băng giữ đủ `event.ice.roadSeconds=12` giây trước khi kết thúc.
Thời gian chờ vật cản cuối cũng được giới hạn bằng `clearTimeoutSeconds`;
những vật còn lại tiếp tục được tính trong kiểm tra an toàn của các lần sinh sau.
Đàn cá chọn sẵn toàn bộ đội hình và kiểm tra cả nhóm trước khi sinh, bao gồm
vị trí đớp ở hàng nhân vật và thời điểm gây sát thương. Kiểu một/hai làn chọn làn
ngẫu nhiên; kiểu 1–2–3 đi từ trái qua phải, cảnh báo từng con đủ 1,2 giây, lệch nhau
`event.sharks.sequenceDelaySeconds=0.5`, mỗi lần đớp 0,4 giây nên không đớp kín cả ba
làn đồng thời. Giữ tối đa 3 cá đang tồn tại; chưa có chỗ cho cả nhóm thì chờ thử lại.
Chỉnh ba trọng số `event.sharks.singleWeight`, `pairWeight`, `sequenceWeight`;
mặc định đều bằng 1, tương ứng khoảng 33,3% mỗi kiểu. `waveCount` đếm đội hình, không đếm cá.
Cá thường và cá trong đợt đặc biệt đều dùng `shark.collisionType=SHARK_SLOW`: đớp
mất 1 máu và làm chậm, không còn chí mạng. Giữ đủ chuỗi vây/lặn/cảnh báo trước khi đớp.

Màn 4 chỉ tạo tiếp tế thường khi đến nhịp và đang có khoảng dịu nắng.
**Tiếp tế khẩn cấp vẫn được phép sinh lúc nắng gắt** nếu tìm được đường nhặt an toàn,
để không phải hết năng lượng chỉ vì chờ chu kỳ thời tiết.

Các nhóm `event.*` nằm cuối properties từng màn và có ghi chú tiếng Việt.
Đặt `event.type=none` để tắt đoạn đặc biệt; thiếu khóa này trong cấu hình cũ cũng
mặc định tắt. Số đợt trong kịch bản dùng `waveCount`, không phụ thuộc trọng số sinh
thường. Kịch bản cần các mẫu tương ứng trong `obstacles`: `vehicle` cho đoàn xe,
`shark` cho đợt cá, `iceHole,snowman` cho đoạn băng. Nếu bỏ những mẫu đó, tắt hoặc
đổi kịch bản; `heatCycle` cần bật cả hệ nắng và năng lượng.

### Kiến trúc và thư mục

```text
src/main/java/
├── Main.java
└── game/
    ├── config/        ConfigValues, DayConfigLoader, AirBossConfig, LevelEventConfig, PropertiesReader, GameConfig, AppSettings, game.properties
    ├── engine/        Game, GameLoop, GameState, LevelManager, TransitionManager
    ├── level/         Level, BaseLevel, Levels, DayConfig
    │   ├── day1/      Day1Level.java + day1.properties
    │   ├── day2/      Day2Level.java + day2.properties
    │   ├── day3/      Day3Level.java + day3.properties
    │   ├── day4/      Day4Level.java + day4.properties
    │   └── day5/      Day5RoadLevel.java + day5-1.properties
    │                  Day5HelicopterLevel.java + day5-2.properties
    ├── model/         Entity, Player, Helicopter
    │   ├── boss/      AirBoss
    │   ├── projectile/ PlayerRocket
    │   └── obstacle/  Obstacle, Vehicle, Bicycle, Barrier, Shark, Drain, FloatingCrate,
    │                  IcePatch, IceHole, Snowball, Snowman, Bus, Cactus, Tumbleweed, EnergyPickup, HealthPickup,
    │                  TrafficJamVehicle, Dog, Bird, EnemyPlane, Missile, BossProjectile, ShieldPickup, ObstacleType
    ├── logic/
    │   ├── boss/      AirBossEncounter (luồng boss, ba đòn, tên lửa và vật phẩm boss)
    │   ├── event/     LevelEvent, LevelEvents, RoadSequenceEvent, ConvoyEvent, SharkRushEvent, IceChoiceEvent, HeatCycleEvent
    │   ├── movement/  HorizontalMotion, MovementBehavior, LinearMovement, DriftMovement, DogMovement,
    │   │              ScrollingMovement, DelayedLinearMovement, BirdMovement, LaneStopMovement
    │   ├── collision/ CollisionEffect, ConfiguredCollision, CollisionKind, CollisionManager
    │   ├── effect/    EffectManager, EnergySystem
    │   ├── progress/  ProgressManager
    │   └── spawn/     ObstacleFactory, ObstacleSpawner, HealthPickupSpawner, PlaneSpawnPlacement, LaneSpawnPolicy, SafeSpawnPolicy
    ├── input/         InputState, InputHandler
    ├── save/          SaveData, SaveRepository
    ├── rendering/     AssetLoader, EntityRenderer, AirBossRenderer, GameRenderer, IceRoadRenderer, GlareEffect
    └── ui/            GamePanel, MenuOverlay, MenuButton, GameHUD, ProgressBar
src/test/java/game/     JUnit 5: config, physics, collision, effects, spawn, FSM, engine, render
```

`GamePanel` chỉ vẽ. `BaseLevel` điều phối một phiên gameplay. `Entity` tách sprite
và hitbox; `Obstacle` kết hợp movement với collision effect. `Bicycle`/`Bus` kế thừa
`Vehicle`; vật cản tĩnh, chó, thùng, cầu tuyết không bị ép kế thừa Vehicle.
`Helicopter` dùng chung vật lý `Player` và cùng vùng giữa X=140–860; sprite/hitbox riêng.

### Chỉnh properties

Mỗi thông số có ghi chú tiếng Việt ngay bên phải; các nhóm có tiêu đề và đường ngăn:

```properties
# ============================================================================
# NHÂN VẬT — DI CHUYỂN
# ============================================================================
player.maxSpeed=420       # Tốc độ ngang tối đa (px/giây).
player.acceleration=1500  # Gia tốc ngang khi giữ phím (px/giây²).
```

Chỉ sửa giá trị ở trước ghi chú. Bộ đọc chung `PropertiesReader` hỗ trợ dấu ` # `
(có khoảng trắng) ở cuối dòng; vẫn giữ nguyên màu như `#467ca2`, tên màn tiếng Việt
và đường dẫn ảnh. Nếu cần dấu `#` có khoảng trắng trong chính giá trị, viết `\#`
để phân biệt với ghi chú. Không dùng trực tiếp `Properties.load` thay bộ đọc chung
vì Java chuẩn không tự loại ghi chú cuối dòng.

Properties nằm **ngay cạnh class level**, không chuyển sang resources/config.
`pom.xml` copy riêng `**/*.properties` từ source tree vào classpath/JAR, không copy
`.java`. Loader dùng resource tương đối theo package, không dựa vào thư mục đang mở.
**Sửa properties trong source phải build lại**; `run.cmd` làm bước này tự động.

Lưu source Java bằng **UTF-8 không BOM**, ghi chú tiếng Việt viết trực tiếp.
Properties dùng **UTF-8**; bộ đọc hỗ trợ cả có và không có BOM.
Cả bảy file hiện có BOM để các công cụ Windows tự nhận đúng mã hóa.
Nếu đọc bằng Windows PowerShell 5.1, dùng `Get-Content -Encoding UTF8 <file>`.
`run.cmd` đã bật đầu ra UTF-8 để tên màn và thông báo trong terminal giữ đúng dấu.
VS Code dùng cấu hình UTF-8 trong `.vscode/settings.json`. Nếu một tab đang hiển thị
chữ lỗi do đã mở bằng mã hóa khác, bấm tên encoding ở thanh trạng thái →
**Reopen with Encoding → UTF-8**; thao tác này đọc lại file, không ghi đè nội dung.

| Nhóm | Key quan trọng |
| --- | --- |
| Level | `level.durationSeconds` (màn 51: 25; màn 52: 15 trước boss; màn 1–4: 40), `level.initialHealth=3`, `level.scrollSpeed` |
| Vật lý | `player.maxSpeed`, `acceleration`, `deceleration`, `minX`, `maxX`, `y` |
| Hitbox | `player.hitboxWidth=88`, `hitboxHeight=86`, `hitboxOffsetX=6`, `hitboxOffsetY=7` |
| Damage | `collision.normalDamage`, `player.hitInvulnerabilitySeconds`, `hitSlowMultiplier` |
| Progress | `progress.normalMultiplier`, `hitMultiplier`, `hitPenaltyDurationSeconds` |
| Nhịp sinh | `spawn.minIntervalSeconds`, `maxIntervalSeconds`, `initialDelaySeconds`, `maxActiveObstacles` |
| Dự báo | `spawn.safeReactionTimeSeconds`, `horizonSeconds`, `maxPredictionSeconds`, `escapeBufferSeconds`, `pathClearancePixels`, `stepSeconds`, `minimumGap` |
| Độ khó | `difficulty.phase1End`, `phase2End`, `phase1/2/3SpawnMultiplier` |
| Vật cản | `<loại>.width`, `height`, `minSpeed`, `maxSpeed`, `collisionType`, `consumed` |
| Cá mập | `shark.finDuration`, `divingDuration`, `warningDuration`, `attackDuration`, `finSpeed` |
| Hiệu ứng | `drain.*`, `ice.*`, `snowball.*` |
| Chó/chim | `crossing.leadY` (khoảng cách dọc phía trước), tốc độ ngang trong `<loại>.minSpeed/maxSpeed` |
| Máy bay/tên lửa | `enemyPlane.*`, `missile.*` trong `day5-2.properties` |
| Energy | `energy.initial`, `max`, `drainPerSecond`, `recovery`, `pickupIntervalSeconds`, `minimumReserve` |
| Ảnh | `sprite.player`, `sprite.background`, `sprite.road`, `sprite.<loại>` |

Đơn vị vật lý: px và giây. `spawn.randomSeed=random` tạo lượt khác nhau;
đặt số nguyên như `42` để tái hiện spawn và lựa chọn đích của cá.
Các giá trị không hợp lệ/thiếu key bắt buộc sẽ báo lỗi cụ thể khi load.

Mặc định theo trọng số như đặc tả mới trong `prompt.md`:

```properties
spawn.mode=weighted
obstacles=vehicle,bicycle,barrier
spawn.vehicle.weight=55
spawn.bicycle.weight=20
spawn.barrier.weight=25
```

Mỗi lượt chọn một loại theo tổng trọng số, thử vị trí an toàn trong giới hạn
`spawn.attempts`, không có vị trí thì bỏ lượt. Chỉ dùng các loại đã triển khai.

Vẫn hỗ trợ xác suất riêng từng loại nếu cần:

```properties
spawn.mode=independent
vehicle.chance=60
bicycle.chance=0
barrier.chance=40
```

Mỗi loại được xét độc lập theo phần trăm, có thể cùng ra hoặc cùng không ra.
Không chuẩn hóa theo tổng. Khi bị giới hạn số lượng/không có vị trí an toàn,
vật cản đã được chọn có thể không được sinh.

Chính **cây xương rồng trên đường** đứng 1,2 giây rồi hiện **khung chữ nhật trên mặt đất
và dấu chấm than nhấp nháy** trong 1,3 giây, sau đó xoay đổ quanh gốc trong 0,3 giây.
Khung đi theo cuộn cảnh, đúng vùng cây sẽ nằm; vẽ sau lớp chói và nhân vật.
Cây đứng gây sát thương thường; lúc cảnh báo/đang đổ chưa gây sát thương, khi nằm xong
va chạm theo thân và nhánh, giữ lại trên đường. Cây ở làn ngoài đổ vào trong;
cây làn giữa chọn hướng trái/phải. Chỉ có loại `cactus`, dùng ảnh `sprite.cactus`.
Bộ dự báo giữ cả vùng đứng và vùng đổ ngay khi sinh, đồng thời kiểm tra các vật khác;
không tìm được đường né thì không sinh. Thời gian/kích thước/màu/nhấp nháy ở nhóm `cactus.*`.

Làn băng bên phải là mặt đường liên tục, không phải một dãy collider sàn băng kín làn.
Tốc độ ngang và cuộn cảnh/tiến độ bắt đầu tăng 20%; mỗi giây trượt liên tục tăng thêm
10 điểm phần trăm, đạt tối đa **+70% sau 5 giây**. Rời băng còn trượt 1,5 giây;
hết trượt mới đặt lại mức tăng. Gia tốc ngang vẫn chỉ tăng 10%, lực hãm còn 30%.
Mỗi đợt có 85% cơ hội thêm hố ở làn băng, lệch sau người tuyết 260 px.
Người tuyết có 60% xuất hiện ở làn khô, còn lại ở làn giữa, tránh đứng mãi một bên
là an toàn. Mọi nhóm vẫn qua kiểm tra đường né và dồn làn; đường khô chỉ khác
băng về địa hình, vẫn có vật cản.
Chỉnh độ dài/màu/vết nứt/hố trong `event.ice.*`, độ tăng tốc trong `ice.*`.

Cỏ khô được kiểm tra **toàn bộ quỹ đạo lăn và giai đoạn đứng lại** trước khi sinh.
Bộ giới hạn dồn làn giữ chỗ ở làn cỏ sẽ tới, còn bộ dự báo kiểm tra cả lúc cỏ băng
qua các làn khác. Nếu không còn đường né thì không sinh ứng viên đó.

### Thông số cơ chế mới

Các giá trị là hệ số tốc độ: `0.65` = còn 65%, `1.4` = tăng 40%.

| File cạnh class level | Key | Mặc định / ý nghĩa |
| --- | --- | --- |
| `day2.properties` | `shark.damage`, `shark.slowMultiplier`, `shark.progressMultiplier`, `shark.slowDurationSeconds` | Cá đớp mất 1 máu; di chuyển/cuộn cảnh/tiến độ còn 65% trong 1,2 giây, có nhấp nháy/miễn sát thương thường; áp dụng cả cá thường và đợt cá cao trào |
| `day2.properties` | `drain.slowMultiplier`, `drain.progressMultiplier`, `drain.durationSeconds` | Cống gây 1 HP qua `drain.damage`; di chuyển/cuộn cảnh còn 65% trong 1,2 giây, thêm phạt tiến độ do nhận sát thương |
| `day3.properties` | `ice.speedMultiplier`, `ice.progressMultiplier`, `ice.maxSpeedMultiplier`, `ice.rampPerSecond` | Bắt đầu `1.2`, tăng `0.1` mỗi giây trượt, tối đa `1.7` (+70%); `ice.durationSeconds=1.5` là thời gian còn trượt sau khi rời băng, gia tốc ngang tăng nhẹ theo `ice.accelerationMultiplier=1.1` |
| `day3.properties` | `iceHole.*`, `spawn.iceHole.weight`, `drain.*` | Hố băng cuộn theo đường; va chạm `SLOW` như cống: mất 1 máu, tốc độ và tiến độ còn 65% trong 1,2 giây |
| `day3.properties` | `snowball.freezeDuration`, `snowball.freezeProgressMultiplier` | Đóng băng 0,6 giây, tiến độ bằng 0 |
| `day3.properties` | `snowball.slowMultiplier`, `snowball.progressMultiplier`, `snowball.slowDurationSeconds` | Sau đóng băng: di chuyển 60%, tiến độ 50%, thêm 1,5 giây |
| `day4.properties` | `tumbleweed.*`, `spawn.tumbleweed.weight` | Cỏ khô đường kính 120 px, lăn ngang 230–290 px/giây vào một làn rồi đứng yên trong thế giới; `stopLeadY=260` giữ điểm dừng phía trước nhân vật; chạm mất 1 máu |
| `day4.properties` | `cactus.fallEnabled`, `cactus.standingSeconds`, `cactus.warningSeconds`, `cactus.fallSeconds`, `cactus.*` | Chính cây trên đường đứng 1,2 giây, cảnh báo 1,3 giây rồi xoay đổ 0,3 giây; đứng 100 × 220, nằm 220 × 100; vẫn kiểm tra toàn bộ đường né |
| `day4.properties` | `glare.minAlpha`, `clearY`, `revealExponent`, `hazeAlpha`, `hazeColor` | `0.15`, `420`, `2`, `0.38`, `#fff0c0`: giữ vật cản mờ lâu hơn và phủ lớp chói lên đường, không che HUD/người chơi |
| `day4.properties` | `energy.drainPerSecond`, `energy.pickupHealAmount` | Tụt 4 điểm năng lượng/giây; mỗi thanh hồi thêm 1 máu, không vượt máu tối đa; đặt heal amount 0 để tắt hồi máu |
| `day4.properties` | `energy.maxSlowdown` | `0.2`: giảm tuyến tính từ tốc độ 100% khi đầy energy tới 80% khi cạn |
| `day4.properties` | `energy.pickupBoostMultiplier`, `energy.pickupBoostDurationSeconds` | `1.4`, `2`: pickup cho tốc độ 140% bình thường trong 2 giây, tạm thay mức mệt do thiếu energy |
| `day5-1.properties`, `day5-2.properties` | `crossing.leadY` | `300` px: chim bay ngang xuất hiện phía trước rồi trôi xuống theo cảnh; chó dùng `dog.leadY` riêng |
| `day5-2.properties` | `bird.stationaryChance` | `20` (%): chọn những con đang bay sẽ dừng sau khi tới một làn; 0 = bay xuyên qua, 100 = chọn dừng; mọi con đều bay vào từ ngoài màn |
| `day5-2.properties` | `bird.minSpeed`, `bird.maxSpeed` | Tốc độ ngang của chim bay; chim đứng yên không có tốc độ riêng |
| `day5-2.properties` | `enemyPlane.minSpeed`, `enemyPlane.maxSpeed` | Máy bay đối đầu bay từ trên xuống 200–260 px/giây |
| `day5-2.properties` | `enemyPlane.firstShotDelaySeconds`, `enemyPlane.shotIntervalSeconds` | Chờ bắn lần đầu 1 giây, mỗi lượt bắn cách 1,5 giây khi còn trong vùng bắn |
| `day5-2.properties` | `enemyPlane.minFireY`, `enemyPlane.maxFireY` | Chỉ bắn khi Y của máy bay nằm trong 70–280 |
| `day5-2.properties` | `missile.warningSeconds`, `missile.blinkSeconds` | Cảnh báo 0,2 giây, đổi sáng/tắt mỗi 0,05 giây |
| `day5-2.properties` | `missile.speedMultiplier` | `2`: tên lửa bay ở `2 × player.maxSpeed` = 840 px/giây mặc định |
| `day5-2.properties` | `missile.sideOffset` | `50` px từ tâm máy bay tới điểm phóng cánh trái/phải; chọn ngẫu nhiên, đạn giữ nguyên X |

Fatigue, pickup boost, sàn băng, hố băng, cống và bóng tuyết ảnh hưởng **di chuyển ngang, tốc độ cuộn
cảnh/vật cản tĩnh và tiến độ**. Vật cản tự chạy/bay vẫn có tốc độ riêng.
Tất cả chim đều xuất phát ngoài biên trái/phải và bay vào. Mặc định 20% được chọn
để dừng tại tâm một làn mà chúng kịp bay tới khi còn phía trước nhân vật; phần còn
lại bay xuyên qua đường. Không tạo một con chim đứng sẵn giữa màn. Chim giữ Y trong
thế giới; phần dịch xuống màn hình chỉ do cuộn cảnh. Khi cảnh dừng, chim đang bay
vẫn đi ngang, chim đã dừng thì đứng hoàn toàn. `bird.stationaryChance` chọn việc
dừng sau khi bay vào, khác với `bird.chance` chọn sinh loại chim ở chế độ independent. Mỗi ứng viên vẫn qua kiểm tra an toàn/chống chồng
lấn, nên số chim thực tế được sinh có thể khác tỉ lệ chọn ban đầu.
Tên lửa chờ hết cảnh báo mới bay thẳng xuống ở X của điểm phóng; dấu cảnh báo và
đường đạn cùng X, không ngắm chéo theo vị trí người chơi. Tên lửa gây damage thường,
va chạm thân máy bay trừ **2 máu** (`enemyPlane.damage=2`), có nhấp nháy và miễn sát thương ngắn. Spawn/bắn chỉ được nhận khi còn đường né theo planner.
Chỉnh trọng số máy bay/chim qua `spawn.enemyPlane.weight`, `spawn.bird.weight`,
hoặc dùng `<loại>.chance` khi bật chế độ independent.

### Luật gameplay và an toàn

Màn 2–4 bật `healthPickup.randomCount=5`: chia khoảng tiến độ từ giây 3 đến 29
thành 5 phần rồi chọn ngẫu nhiên một mốc trong mỗi phần. Mỗi hộp chọn làn ngẫu nhiên,
hồi 1 HP và không vượt máu tối đa. Chưa có chỗ an toàn hoặc còn hộp trước thì
chờ `healthPickup.retrySeconds=0.35` để thử lại; không sinh thêm ngoài số lượng đã đặt.
Nếu đường chật kéo dài tới hết màn, có thể không đặt kịp toàn bộ năm hộp.
Chỉnh trong properties từng màn: `healthPickup.randomCount`, `randomStartSeconds`,
`randomEndSeconds`, `amount`, `speed`. Đặt `randomCount=0` để dùng lịch
`spawnTimesSeconds` hoặc nhịp thời gian cũ; không khai báo cả lịch cố định và số lượng ngẫu nhiên.
Màn 4 vẫn giữ hồi máu kèm thanh năng lượng.

Màn 52 giữ vật phẩm hồi máu bay xuống từ ngoài biên trên, lịch độc lập với
trọng số chim/máy bay. Phần thường mặc định sinh ở giây 14 rồi cách mỗi 14 giây;
trong phần boss, lần đầu sau 8 giây giao chiến rồi ngẫu nhiên cách 10–16 giây;
chỉ giữ tối đa một vật phẩm hồi máu đang bay. Nhặt được hồi 1 HP, không vượt
`player.maxHealth=5` ở màn 52, không hồi sinh khi đã chết. Hệ thống chỉ đặt khi có đường
để nhặt và không chồng lên vật cản; bị chặn thì thử lại sau 1 giây. Pause dừng
cả lịch sinh và chuyển động; restart đặt lại lịch.

Chỉnh trong nhóm **VẬT PHẨM HỒI MÁU** của `day5-2.properties`: `healthPickup.enabled`,
`amount`, `firstDelaySeconds`, `minIntervalSeconds`, `maxIntervalSeconds`, `retrySeconds`,
`width`, `height`, `speed`, `sprite.healthPickup`. Màn 1 và 51 mặc định tắt.
Ảnh `health.png` 40 × 40 px nằm trong thư mục ảnh từng ngày 2, 3, 4 hoặc 5; chưa có ảnh dùng hộp
đỏ với dấu cộng trắng. HUD hiện HP hiện tại/tối đa.

### Màn 52: phần né 15 giây và trận boss riêng

`level.durationSeconds=15` chỉ là phần né vật cản ban đầu. `difficulty.mode=fixed`
và `difficulty.fixedSpawnMultiplier=1.35` giữ mức khó cao ngay từ đầu, không chia
ba giai đoạn. Hết tiến độ thì ngừng sinh đợt thường và ngừng phóng thêm tên lửa;
mọi vật cản đang có tiếp tục đi hết. Sau khoảng trống 1,5 giây, dấu `!` cạnh
nhân vật nhấp nháy 1,2 giây, boss xuất hiện trong 1,2 giây rồi nạp máu trong 1,8 giây.

Trận boss không có giới hạn thời gian. `boss.hitsToDefeat=x` đặt số phát cần trúng;
mỗi phát trừ đúng **1/x tổng thanh máu**, đủ x phát thì hạ boss.
Thanh máu chỉ thể hiện tỷ lệ còn lại, không hiện số HP. Khi nạp máu xong mới hiện
gợi ý **E** nhấp nháy và thanh hồi tên lửa. Gợi ý dừng sau phát đầu; thanh hồi vẫn
hiện suốt giao chiến. Tên lửa bay thẳng lên, cần căn làn boss; vật cản đứng trước
boss sẽ chặn phát bắn đó. Máy bay cần `enemyPlane.hitsToDestroy=2` phát mới nổ;
phát đầu lóe sáng trong `enemyPlane.hitFlashSeconds=0.12` giây, còn các vật cản
khác vẫn bị phá bởi một phát.
Va chạm thân máy bay thường, boss gọi và bay ngược đều trừ `enemyPlane.damage=2`
máu; khiên chặn sát thương, ở lại cùng một vùng chạm không mất máu mỗi khung hình.
Hồi tên lửa là `boss.playerRocket.cooldownSeconds=0.8` giây.
Riêng vật ném boss và tên lửa địch không chặn
tên lửa người chơi: hai bên bay xuyên nhau, đạn địch vẫn gây hại như cũ.
Hạ boss xong còn chờ vật cản đi hết mới hoàn thành.

Boss luôn mở đầu bằng **đòn 2** (`boss.startAction=2`), kể cả khi chơi lại.
**Đòn 2 luôn chuyển sang đòn 1; đòn 1 luôn nối sang đòn 3**.
`boss.maxConsecutiveWaveSequences=1` giới hạn tối đa một chuỗi **1 → 3** trước
khi bắt buộc về đòn 2: nhịp mặc định là **2 → 1 → 3 → 2 → 1 → 3**.
`boss.action1Chance` chỉ có tác dụng sau đòn 3 khi tăng giới hạn chuỗi lên trên 1.
Khi dùng properties cũ thiếu `boss.startAction`, `boss.lane.planeShotIntervalSeconds`
hoặc `boss.lane.planeShotRetrySeconds`, bộ đọc bổ sung lần lượt `2`, `0.65`, `0.15`.
Giá trị đã khai báo vẫn được ưu tiên và kiểm tra hợp lệ.

Hết thời lượng thì dừng gọi các đợt chính; đòn 2 còn một loạt bắn giới hạn lúc
chờ. Khi vật cản/đạn cũ đi hết, boss bắt đầu
chuyển mượt sang vị trí đòn tiếp ngay. Khoảng nghỉ ngắn
`boss.actionGapSeconds=0.1` chạy cùng lúc chuyển động `boss.moveSeconds=0.55`;
đủ nghỉ và tới vị trí là ra đòn, không cộng thêm chờ mở đầu. Boss không dịch
chuyển tức thì; nếu đã đứng đúng chỗ thì chỉ chờ khoảng nghỉ 0,1 giây.
`boss.openingDelaySeconds` chỉ áp dụng lần đầu sau nạp máu.

1. **Đứng ngoài đường**, mỗi lần bắt đầu đòn 1 bắn đúng một phát vào từng làn
   theo thứ tự **trái → giữa → phải**, có cảnh báo. `boss.waves.openingVolleyEnabled`
   bật/tắt loạt này; `openingVolleyIntervalSeconds=0.4` đặt khoảng cách tối thiểu,
   `openingVolleyWarningSeconds=0.8` đặt thời gian cảnh báo. Chưa an toàn thì
   chờ và giữ đúng làn kế tiếp. Đợi phát cuối biến mất mới gọi máy bay và bắn
   ngẫu nhiên như thường. Các khóa mở đầu đều có tiền tố `boss.waves.`.
   Mỗi đợt thường gọi một hoặc hai máy bay với xác suất ngang nhau:
   `boss.waves.twoPlaneChance=50`. Máy bay không bắn, nhanh gấp 1,6 lần máy bay
   thường; nếu có hai chiếc thì chúng ở hai làn khác nhau, lệch Y 40 px.
   `boss.waves.gapPixels=150` giữ khoảng trống từ mép cuối đợt trước tới mép đầu
   đợt sau. Cả lượt đòn 1 dùng cùng tốc độ để đợt sau không đuổi kịp đợt trước.
   Nhịp gọi tính từ chiều dài đội hình, khoảng trống và tốc độ, chờ tối thiểu
   `boss.waves.intervalSeconds=1.4` giây; nếu chưa an toàn thì đợi thêm.
   Nhịp bắn đạn tách riêng: `boss.waves.throwIntervalSeconds=0.7` giây, mỗi nhịp có
   `boss.waves.throwChance=90` tức 90% chọn bắn. Boss vẫn bắn khi đang chờ máy bay;
   nếu đích đạn chưa an toàn thì thử lại sau `boss.waves.throwRetrySeconds=0.15` giây.
   Trong lượt ném, `boss.waves.doubleThrowChance=20` cho 20% ném hai làn khác nhau,
   còn lại nhắm một làn, đích ngang hàng người chơi. Trong thời gian cảnh báo,
   tia mờ nối **tay boss → ô dấu chấm than**; hết cảnh báo, đạn thật bay thẳng
   từ tay boss qua ô đã khóa. Tia ngắm không gây sát thương, đạn có va chạm khi
   đang bay, không đuổi theo người chơi. `boss.waves.throwSpeed=800` đặt tốc độ,
   `handHeightRatio=0.6` và `handInsetX=8` đặt vị trí tay; `aimLineAlpha=0.22`,
   `aimLineWidth=2`, `aimLineColor` chỉnh tia ngắm (cùng tiền tố `boss.waves.`).
   `throwActiveSeconds=0.3` giữ đạn thêm sau khi qua đích. Hai viên cùng loạt có
   thể xuất phát chung một tay, nhưng cả hai đường đạn vẫn phải có lối né.
   Hết đòn 1 và vật cản cũ, vào đòn 3 luôn.
2. **Đứng trong đường và đảo làn**, ném thẳng xuống dọc làn đang đứng, có thể
   ném thêm một làn theo `boss.lane.doubleThrowChance`. Thời gian đứng tại làn,
   nhịp tấn công, khả năng gọi máy bay/chim chỉnh bằng `changeIntervalSeconds`,
   `attackIntervalSeconds`, `planeChance`, `birdChance` (tiền tố `boss.lane.`).
   Máy bay riêng ở đòn 2 có `boss.lane.planeShotIntervalSeconds=0.65` giây
   giữa các lần bắn thành công; khi chỗ bắn chưa an toàn hoặc đầy vật thể thì
   thử lại sau `boss.lane.planeShotRetrySeconds=0.15` giây. Cảnh báo tên lửa vẫn
   theo `missile.warningSeconds`; nhịp bắn trước boss vẫn theo `enemyPlane.shotIntervalSeconds`.
   Từng vật vẫn kiểm tra còn đường né và giới hạn tổng số vật thể.
   Sau khi đòn 2 và vật cản cũ kết thúc, boss hồi **1 hit máu** đúng một lần
   trước khi sang đòn 1 (`boss.lane.healHitsAfterAction=1`; đặt `0` để tắt).
   Nếu `boss.hitsToDefeat=17` thì hồi `1/17` thanh máu, không vượt máu tối đa
   và không hồi sinh khi đã bị hạ.
   Hết thời lượng, boss tiếp tục **lướt đổi làn** bằng `boss.moveSeconds` và bắn
   tối đa `boss.lane.recoveryShotLimit=3` viên có cảnh báo khi chờ vật cản cũ đi hết.
   Mỗi viên cách tối thiểu `recoveryShotIntervalSeconds=0.8` giây; nếu chưa đặt
   được an toàn thì thử lại sau `recoveryShotRetrySeconds=0.15` giây. Hai khóa này
   cùng tiền tố `boss.lane.`; tốc độ đạn dùng `lane.throwSpeed`, cảnh báo dùng
   `boss.projectile.warningSeconds`. Bắn đủ giới hạn thì dừng, tránh tự kéo dài
   thời gian chờ vô hạn. Đường thông thì chuyển lượt với khoảng nghỉ ngắn đã cấu hình.
   Cảnh báo vật ném và tên lửa địch có tam giác lớn phía trên hàng nhân vật,
   vẽ sau nhân vật, đúng X đích. Viền đích vẫn hiện giữa các lần nhấp nháy;
   chỉnh bằng `boss.lane.warningSize`, `warningOffsetY`, `warningColor`.
3. **Máy bay bay ngược từ dưới lên**, boss đứng ngoài đường và gọi 3 đợt. Mỗi đợt
   chọn một trong ba kiểu với cơ hội ngang nhau, **khoảng 33,33% mỗi kiểu**:
   một chiếc; hai chiếc cùng lúc ở hai làn khác nhau; hoặc ba chiếc lần lượt
   **làn 1 → 2 → 3**. `boss.reverse.singleWeight`, `pairWeight`, `sequenceWeight`
   đều đặt 33; đây là trọng số tương đối nên không có 1% còn dư. Đặt trọng số0
   để tắt một kiểu; bỏ khóa cũ `boss.reverse.twoPlaneChance`.
   Kiểu tuần tự lệch cảnh báo và giờ bay mỗi chiếc `boss.reverse.sequenceDelaySeconds=0.5`
   giây. Cả đội hình được kiểm tra/nhận cùng nhau trước, giữ đúng thứ tự và độ trễ;
   chưa nhận được thì chờ, không đổi sang kiểu khác. Mỗi chiếc có đủ 1,25 giây
   cảnh báo riêng rồi vút qua màn trong 0,5 giây; trước lượt cảnh báo thì chưa hiện.
   Các đợt cách tối thiểu 2 giây. Thời lượng 6 giây là danh định: đòn 3 chờ đủ
   các đợt và chiếc cuối ra khỏi màn, nên kiểu tuần tự có thể kéo dài hơn.
   Các thông số thuộc nhóm `boss.reverse.*` trong properties.
   Mỗi đợt máy bay có **75%** khả năng boss bắn thêm **một viên đạn chéo** từ tay
   tới một ô ở hàng người chơi (`boss.reverse.shotChance=75`). Có tia ngắm mờ,
   cảnh báo 0,65 giây, sau đó đạn bay thẳng với tốc độ 800 px/giây; khóa đích lúc
   bắt đầu cảnh báo. Đạn chưa an toàn thì chờ/thử lại, giữ lựa chọn và không
   xét lại xác suất; xong mới gọi đợt máy bay kế tiếp nếu còn. Các khóa
   `shotWarningSeconds`, `shotSpeed`, `shotAfterTargetSeconds`, `shotRetrySeconds`
   nằm cùng nhóm `boss.reverse.*`; vị trí tay và kiểu tia dùng chung `boss.waves.*`.

Đợt máy bay được nhận hoặc từ chối cùng nhau và luôn chừa được đường né theo
SafeSpawnPolicy. Chọn một/hai chiếc một lần rồi giữ lựa chọn trong lúc thử lại,
không quay lại xác suất khi bị chặn. Hai chiếc cùng đợt có thể sát mép ở hai làn khác nhau; không
chồng thân. Mọi vật ném, chim, máy bay bổ sung và vật phẩm vẫn phải qua kiểm tra an toàn.

Máy bay thường và máy bay boss gọi đều được kiểm tra bám đuôi trước khi sinh:
`enemyPlane.tailAlignmentPixels=8` coi hai tâm lệch X tối đa 8 px là thẳng hàng;
`enemyPlane.tailGapPixels=500` giới hạn khoảng trống dọc để xét bám đuôi.
Nếu gặp trường hợp này, vị trí sinh lệch nhẹ `enemyPlane.tailOffsetX=24` px.
Máy bay rộng đúng một làn nên có thể lệch qua ranh giới làn, nhưng toàn bộ vẫn ở
trong X=140–860, giữ lề hai bên. Cả đợt được tính cùng nhau để hai chiếc không
chồng thân; không có vị trí phù hợp thì chờ đợt sau. Máy bay đã bay giữ nguyên
quỹ đạo, hình vẽ/va chạm/dự báo cùng dùng vị trí mới ngay từ lúc sinh.

Chỉ boss phase mới sinh khiên, **tối đa hai vật phẩm** mỗi trận. Sau 10 giây giao
chiến, mỗi 12 giây xét một lượt với xác suất **15%**, nên có trận không có khiên.
Nếu đã chọn được khiên nhưng chỗ đặt bị chặn thì thử lại, không tăng số lần quay xác suất.
Nhặt khiên bảo vệ 8 giây, chặn cả va chạm chí mạng và
hiệu ứng vật cản; không tiêu hủy vật đang xuyên qua. Khi chạm, nhân vật mờ còn 45%
và có viền khiên. Khiên hết lúc vẫn nằm trong vật cản thì va chạm được tính lại.

Mọi chỉnh sửa nằm trong 9 nhóm **BOSS** của `day5-2.properties`: xuất hiện/máu,
chọn hành động, đòn 1, đòn 2, vật ném, tên lửa người chơi, hồi máu/khiên hiếm, HUD và đòn 3.
Mỗi thông số có ghi chú tiếng Việt bên cạnh. Pause dừng toàn bộ đồng hồ/tên lửa;
restart đặt lại 15 giây đầu, máu boss, hai lượt khiên và hồi chiêu.

Nhân vật màn 1–4 rộng 100 px, hitbox ngang 88 px; màn 51 rộng 84 px, hitbox ngang 74 px; các vật cản chiếm làn
rộng ít nhất 160 px, nên tổng bề ngang > 240 px. Vì vậy đứng đúng ranh giới hai làn
vẫn chạm vật cản ở một trong hai làn đó. Hình hiển thị đã tăng cùng vùng va chạm;
không mở rộng vùng va chạm vô hình. Chó nhỏ hơn và dùng quỹ đạo tấn công, còn chim
rộng 170 px để vùng đứng giữa làn cũng không trở thành chỗ tránh chim cố định.
Người chơi vẫn di chuyển ngang tự do, không bị ép về tâm làn.

Mọi lần giảm máu đều bắt đầu nhấp nháy: `player.hitBlinkDurationSeconds=1.1`,
`player.hitBlinkIntervalSeconds=0.1`. Cống dùng `drain.damage=1` và vẫn làm chậm.
Miễn sát thương và ghi nhận tiếp xúc ngăn cống trừ máu mỗi frame. Nhấp nháy được
quản lý riêng với miễn sát thương, dừng khi pause; sau chết chỉ hiệu ứng hình ảnh
còn chạy tới hết thời gian, gameplay đã dừng.

Chỉnh chó trong nhóm **VẬT CẢN — CHÓ** của `day5-1.properties`: `minSpeed/maxSpeed`
(700–900 px/giây khi băng ngang), `leadY` (380 px phía trước), `barkDurationSeconds`,
`chargeDurationSeconds`, `targetOffsetX`, `warningBlinkSeconds`,
`warningSize`, `warningOffsetY`. Mọi thông số mới có ghi chú tiếng Việt ngay bên phải.

Player màn 52 bắt đầu với 5 HP và hồi tối đa 5 HP; các màn còn lại mặc định 3 HP.
Xe đang chạy và cá ATTACK gây chết ngay, kể cả đang
miễn sát thương thường. Va chạm thường chỉ tính một lần mỗi lượt tiếp xúc, giảm HP,
nhấp nháy và giảm tốc/progress tạm thời. Slow kết hợp bằng mức mạnh nhất, không
nhân chồng vô hạn. Freeze, slide và năng lượng được quản lý riêng.

Progress tích lũy thời gian hiệu dụng: màn 51 đầy sau 25 giây, màn 52 sau 15 giây
rồi chuyển sang boss; các màn 1–4 sau 40 giây. Slow/freeze
kéo dài thời gian hoàn thành; pickup boost có thể rút ngắn thời gian ở ngày 4. Pause/fade/
gameover không tăng progress, không tick effects/energy/spawn. Death được xử lý
trước completion. Restart/chuyển màn tạo session mới, không giữ obstacle hoặc effect cũ.

SafeSpawnPolicy dự báo theo thời gian và tìm đường đi ngang bằng cùng phương trình
vật lý với Player: vị trí, vận tốc, gia tốc, hãm, phản ứng và hiệu ứng hiện tại.
Không chỉ đếm làn trống. Xe/thùng/chó/chim có quỹ đạo dự báo; cá kiểm tra lại vị trí
trước khi bắt đầu cảnh báo và không đổi mục tiêu sau đó. Spawn không hợp lệ bị bỏ qua.

Khoảng dự báo không dừng cứng ở 4,5 giây: `spawn.horizonSeconds` là mức tối thiểu,
bộ kiểm tra tự kéo dài tới lúc vật cản cuối đi qua hàng nhân vật, cộng
`spawn.escapeBufferSeconds=0.5`. `spawn.maxPredictionSeconds=20` phải đủ vòng đời
vật cản và phần dự phòng. Việc này bắt được xe tắc đường sinh ở trên cao chỉ chặn
đường sau gần 5 giây, kể cả các hàng xe lệch nhau. Chó đứng lại sau cú lao vẫn nằm
trong dự báo, thay vì bị coi là đã biến mất. `spawn.pathClearancePixels=8` chừa thêm
8 px mỗi bên đường né trong bộ dự báo; vùng va chạm thật giữ nguyên. Bộ tìm đường
thử các quỹ đạo đi tới làn trống bằng đúng vật lý trước, rồi mới tìm nhánh cho bố cục
phức tạp; khi phải giới hạn số trạng thái, giữ phân bố cả trái lẫn phải thay vì ưu
tiên một bên đường.

**Giới hạn mô hình:** kiểm tra trong horizon cấu hình với lưới trạng thái và giới hạn
số nhánh; có thể từ chối một vị trí vẫn né được. Không phải bảo đảm an toàn tuyệt đối
cho mọi chuỗi thao tác hoặc mọi cấu hình tùy ý. Cần playtest thêm sau khi đổi tốc độ,
kích thước hoặc animation. Pickup energy có lịch và ngưỡng khẩn cấp, không phụ thuộc
việc RNG có chọn pickup hay không; người chơi vẫn phải thu thập.

### Build, test và smoke

```powershell
mvn clean test
mvn package
java -Djava.awt.headless=true -jar target/exam-run.jar --smoke
```

Trong workspace chưa có Maven trên PATH, thay `mvn` bằng
`.\.tools\apache-maven-3.9.9\bin\mvn.cmd -Dmaven.repo.local=.tools/m2`;
trong PowerShell nên đặt đối số `'-Dmaven.repo.local=.tools/m2'` trong dấu nháy.

Bộ JUnit kiểm tra config cả sáu màn, movement, HP/lethal/contacts, effects, shark FSM,
progress, energy/pickup, input/pause/restart/fade và chuỗi No-Story. Spawn có tình huống
bít đường dựng riêng và 1.000 seed/tình huống. Smoke dựng ảnh bằng Java2D cho sáu màn;
đây không phải playtest GUI thủ công. Kết quả thực tế và hạn chế ghi trong `CONTEXT.md`.

Các bước tiếp theo: playtest cân bằng độ khó, thêm sprite/animation, âm thanh rồi mới
tích hợp story/dialogue/cutscene từ `Story.docx` qua các điểm chuyển màn sẵn có.
