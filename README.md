# OOP-PROJECT-20261

README gồm hai phần: hướng dẫn làm hình ảnh ở trước, hướng dẫn code và cấu hình game ở sau.

## Dành cho người làm hình ảnh

### Nơi lưu ảnh

```text
src/main/resources/image/
├── common/
│   └── player/          Nhân vật dùng chung giữa các ngày
├── day1/                Ảnh riêng ngày 1
│   ├── background.png   Nền toàn màn hình (thêm sau)
│   └── road.png         Đường ở giữa màn hình (thêm sau)
├── day2/                Nền, đường và ảnh riêng ngày 2
└── day3/                Nền, đường và ảnh riêng ngày 3
```

Ảnh dùng chung cho nhiều ngày đặt trong `common/`. Ảnh riêng của ngày nào đặt
trong `dayN/` tương ứng. Có thể thêm thư mục con trong `common/` cho UI hoặc
vật cản dùng chung khi có ảnh thực tế. `.gitkeep` chỉ dùng để giữ thư mục trống.

### Kích thước và bố cục

Các kích thước dưới đây là kích thước hiển thị đang cần **cho tới hiện tại**, tính bằng pixel:

| Thành phần | Kích thước | Ghi chú cho người vẽ |
| --- | --- | --- |
| Nền toàn màn hình | 1000 × 700 | File `background.png` của từng ngày. |
| Đường/bản đồ giữa màn hình | 480 × 700 | File `road.png`; gồm 3 làn, mỗi làn rộng 160 px. |
| Nhân vật | 60 × 90 | Ảnh dùng chung trong `common/player/`. |
| Xe | 100 × 150 | Hình xe nhìn từ trên xuống. |
| Cá mập trồi lên | 140 × 140 | Vùng hiển thị và va chạm khi đớp. |
| Cảnh báo cá mập | 140 × 140 | Vòng tròn kèm dấu than; đang thay bằng hình vuông nhấp nháy. |
| Vây cá | 36 × 40 | Vây nhìn từ trên xuống, bơi về phía dưới màn hình. |
| Mặt nước lúc lặn | 24 × 24 | Thay vòng tròn trắng nhỏ tại chỗ vây lặn. |

Đường nằm chính giữa; hai bên chừa 260 px mỗi bên cho phần nền. Nhân vật ở gần
đáy màn hình. Với nhân vật và vật cản, nên xuất PNG có nền trong suốt và hạn chế
khoảng trống quanh hình để hình vẽ khớp với vùng va chạm.

### Các trạng thái cần ảnh cho cá mập

1. Vây xuất hiện từ biên trên, bơi xuống trong 0,5 giây.
2. Vây biến mất, để lại dấu mặt nước nhỏ tại chỗ lặn trong ít nhất 1 giây.
3. Cảnh báo xuất hiện ở một làn trống ngang hàng nhân vật, nhấp nháy trong 1,25 giây.
4. Cá trồi lên đớp trong 0,45 giây rồi biến mất.

Chỗ lặn và chỗ trồi lên đớp là hai vị trí riêng. Nếu các làn đang bị chiếm,
cá có thể chờ lâu hơn ở trạng thái lặn.

### Đưa ảnh vào game và xem thử

Hiện game tự đọc **ảnh nền và ảnh đường** có đúng tên `background.png`, `road.png`
trong `dayN/`. Khi chưa có ảnh, game dùng màu nền và các vạch đường hiện tại.
Đặt ảnh đúng tên rồi đóng, mở lại game để xem thay đổi.

Ảnh nhân vật, xe, vây, mặt nước, cảnh báo và cá đớp **chưa được nối vào phần vẽ**;
hiện vẫn là hình tạm. Khi bàn giao các ảnh này, gửi kèm tên file, trạng thái,
kích thước và thứ tự khung hình nếu có animation để người code tích hợp.

Để xem game, mở terminal trong thư mục `OOP-PROJECT-20261` và chạy:

```powershell
.\run.cmd
```

Ngày đang chạy được ghi trên tiêu đề cửa sổ. Phần hướng dẫn code bên dưới có
cách chọn ngày và chỉnh cấu hình.

## Dành cho người code

### Chạy game

Trong PowerShell hoặc CMD, tại thư mục `OOP-PROJECT-20261`:

```powershell
.\run.cmd
```

Nếu terminal đang ở thư mục cha `F:\OOP project`, dùng:

```powershell
.\OOP-PROJECT-20261\run.cmd
```

`run.cmd` biên dịch toàn bộ mã Java vào `build/classes` trước khi chạy,
tránh dùng các file `.class` cũ nằm cạnh mã nguồn.
Có thể dùng `run.cmd --build-only` để chỉ biên dịch.
Nếu chạy bằng IDE, đặt `src/main/java` làm source root và chọn entry point `main`.

### Cấu trúc Java

```text
src/main/java/
  main.java                        Khởi tạo cửa sổ, entry point giữ nguyên
  game/config/GameConfig.java      Thông số màn hình và đường ba làn
  game/level/DayConfig.java        Dữ liệu và các loại vật cản của một ngày
  game/level/DayConfigLoader.java  Đọc lựa chọn và tỉ lệ vật cản từ file cấu hình
  game/level/Level.java            Hợp đồng chung của các level
  game/level/Levels.java           Đăng ký level và chọn ngày khởi chạy
  game/level/day1/Day1Level.java    Level ngày 1
  game/level/day1/day1.properties  Cấu hình cạnh code ngày 1
  game/level/day2/Day2Level.java    Level ngày 2
  game/level/day2/day2.properties  Cấu hình cạnh code ngày 2
  game/level/day3/Day3Level.java    Level ngày 3
  game/level/day3/day3.properties  Cấu hình cạnh code ngày 3
  game/ui/GamePanel.java            Giao diện, nhận phím, điều phối vòng lặp
  game/model/Player.java            Nhân vật và chuyển làn mượt
  game/model/obstacle/Obstacle.java Lớp cha trừu tượng của vật cản
  game/model/obstacle/Vehicle.java  Xe xanh trượt xuống, kế thừa Obstacle
  game/model/obstacle/Shark.java    Cá mập: vây, ẩn, cảnh báo nhấp nháy, đớp
  game/logic/ObstacleSpawner.java   Chọn mẫu và tạo các đợt xe
```

Các tham số có ghi chú ở đầu lớp chịu trách nhiệm cho chúng.
`Obstacle` chứa hình học, kiểm tra va chạm và khai báo `update`/`draw`;
`Vehicle` quản lý màu sắc và cách di chuyển; tốc độ được truyền từ cấu hình ngày.
Loại vật cản mới có thể kế thừa `Obstacle` để có hành vi riêng.

### Cấu hình theo ngày

- Đổi `START_DAY` ở đầu `Levels.java` để chọn level khởi chạy.
- Ngày 2 hiện xét độc lập 60% có xe, 40% có cá mập trong mỗi lượt; ngày 1 và 3 dùng xe.
  Mặc định chạy ngày 1; chưa tự động chuyển ngày.
- Mỗi `DayConfig` có ảnh nền, ảnh đường, màu dự phòng, ngưỡng sinh đợt,
  khoảng lệch Y và danh sách `ObstacleOption`.
- Mỗi `ObstacleOption` có xác suất phần trăm riêng và factory tạo vật cản.
  Mỗi loại được xét một lần mỗi lượt, độc lập với các loại khác. Factory truyền các thông số
  riêng như tốc độ cho đối tượng, nên spawner không phụ thuộc trực tiếp vào `Vehicle`.
- Mỗi ngày được phát triển trong package `game.level.dayN`. Lựa chọn vật cản,
  xác suất, tốc độ xe, khoảng cách sinh và đường dẫn ảnh được chỉnh trong
  `src/main/java/game/level/dayN/dayN.properties`, ngay cạnh `DayNLevel.java`,
  không cần sửa hoặc biên dịch Java.
  Đóng rồi mở game lại để áp dụng cấu hình đã lưu.
- Khi ghép level mới, thêm lớp `DayNLevel implements Level`, đăng ký trong `Levels`
  cùng `dayN.properties` trong package ngày đó, đặt ảnh vào `image/dayN/`.
  `main` chọn level từ danh sách và đưa cấu hình cho
  `GamePanel`; hiện chưa có điều kiện hoàn thành hoặc tự chuyển ngày.
- `Shark` hiện vây từ biên trên, bơi xuống với tốc độ cố định 100 px/giây trong 0,5 giây
  (đi 50 px), sau đó lặn tại chỗ, để lại vòng tròn trắng nhỏ trong 1 giây.
  Hết thời gian ẩn, cá chọn ngẫu nhiên một làn trống ngang hàng nhân vật,
  cảnh báo tại vị trí mới trong 1,25 giây (hình vuông vàng nhấp nháy mỗi 0,12 giây),
  rồi đớp trong 0,45 giây và biến mất. Chỉ giai đoạn đớp gây va chạm.
  Tham số nằm đầu `Shark.java`; các hình tạm được vẽ trong `draw` để thay bằng ảnh sau.
- Ngày 2 xét sinh mỗi 1,75 giây, tối đa 5 vật cản cùng tồn tại.
  Ngày 1 xét mỗi 1,3 giây, ngày 3 mỗi 1 giây, tối đa 8 vật cản
  (giới hạn mặc định khi chưa khai báo `spawn.maxActive`).
  Lúc sinh, các vật cản cùng đợt lệch Y theo
  `spawn.yGap`; khi cá tấn công, vị trí đổi về hàng nhân vật như thiết kế.

### Tự chọn vật cản và tỉ lệ từng ngày

Mở `src/main/java/game/level/day1/day1.properties`, `day2/day2.properties`
hoặc `day3/day3.properties` trong cùng thư mục `game/level`.
Các loại hiện có là `vehicle` (xe) và `shark` (cá mập). Ví dụ mỗi lượt xét
xe có 70% khả năng xuất hiện, cá mập có 30% khả năng xuất hiện:

```properties
obstacles=vehicle,shark
vehicle.chance=70
shark.chance=30
vehicle.speed=312.5
spawn.thresholdY=200
spawn.yGap=60
spawn.intervalSeconds=1.0
spawn.maxActive=6
```

- `obstacles`: danh sách loại được chọn, cách nhau bằng dấu phẩy. Muốn chỉ có xe: `obstacles=vehicle`.
- `<loại>.chance`: phần trăm độc lập từ `0` đến `100`, nhận cả số thập phân như `12.5`.
  `0` tắt loại đó, `100` luôn chọn; không khai báo thì mặc định `100`.
- Không chia theo tổng. Hai loại cùng `100` thì cả hai cùng được chọn;
  cùng `0` thì không sinh loại nào. Tổng có thể lớn hơn 100.
- Ví dụ xe `60`, cá `40`: khi còn đủ chỗ, cả hai cùng xuất hiện khoảng 24% lượt,
  chỉ xe 36%, chỉ cá 16%, không loại nào 24%. Mỗi loại tối đa một con mỗi lượt.
- Nếu không đủ chỗ trống hoặc chạm giới hạn số lượng, lượt sinh có thể bị bỏ qua.
  Game không đổi sang loại khác hoặc quay lại xác suất ngay trong khung hình kế tiếp.
- Vật cản trong cùng đợt được đặt ở các làn khác nhau. Khi chọn chỗ sinh, game kiểm tra
  vật cản đang có. Cá mập chọn vùng cảnh báo/đớp tránh cả đường xe sẽ chạy qua trong
  thời gian cảnh báo và đớp; nếu mọi làn đều bị chiếm thì cá tiếp tục lặn chờ.
  Xe mới cũng không được sinh nếu sẽ chạy vào vùng cá đang giữ chỗ.
- `spawn.intervalSeconds`: thời gian giữa hai đợt, giảm để sinh dày hơn. Đặt `0`
  (mặc định nếu không khai báo) để chờ theo `spawn.thresholdY` và trạng thái vật cản;
  nếu hụt toàn bộ lượt thì chờ 1 giây mới xét lại.
- `spawn.maxActive`: giới hạn tổng số vật cản cùng tồn tại; khi đầy sẽ chờ có chỗ trống.
  Xác suất quyết định từng loại có được chọn không; nhịp sinh quyết định tần suất xét.
- Cần khai báo ít nhất một loại, cho phép đặt tất cả xác suất bằng 0.
  Tên loại sai, phần trăm ngoài khoảng 0–100 hoặc số không hợp lệ
  sẽ báo lỗi kèm đường dẫn cấu hình khi khởi chạy.
- Chọn các loại đã có bằng file cấu hình. Loại có hành vi hoàn toàn mới vẫn cần viết lớp
  kế thừa `Obstacle` và thêm cách tạo trong `DayConfigLoader`; sau đó dùng được ở mọi ngày.

### Kiểm thử

Kiểm tra cấu hình và hành vi vật cản (không mở cửa sổ game):

```powershell
.\run.cmd --build-only
$tests = @(Get-ChildItem src/test/java -Recurse -Filter *.java | ForEach-Object { $_.FullName })
javac -encoding UTF-8 -cp build/classes -d build/test-classes $tests
java -cp "build/classes;build/test-classes;src/main/java;src/main/resources" game.level.DayConfigLoaderTest
java -cp "build/classes;build/test-classes;src/main/java;src/main/resources" game.model.obstacle.SharkTest
java -cp "build/classes;build/test-classes;src/main/java;src/main/resources" game.logic.ObstacleSpawnerTest
```

### Nạp tài nguyên

Đặt ảnh đúng tên để game tự nạp khi khởi chạy lại. `run.cmd` đã đưa `src/main/java`
vào classpath để đọc các file properties cạnh code, và `src/main/resources` để đọc ảnh.
Nếu dùng IDE, đánh dấu `src/main/resources` là resources root và bảo đảm các file
`.properties` trong source root được sao chép cùng cấu trúc package vào thư mục biên dịch,
hoặc thêm `src/main/java` vào runtime classpath.

### Điều khiển

- Bấm trái/phải để đổi một làn trong 0,2 giây, tăng/giảm tốc mượt.
- Giữ phím không tự đổi tiếp; cần nhả và bấm lại.
- Không nhận lệnh đổi làn mới khi đang trượt; phím lên/xuống không còn di chuyển.
- Va chạm theo vị trí thực trong suốt chuyển động. Mỗi đợt xe chừa ít nhất một làn trống.
- Cửa sổ cố định 1000×700 để giữ ba làn giữa và lề trống 260 px mỗi bên.
