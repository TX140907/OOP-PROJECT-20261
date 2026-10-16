# CODEX MASTER PROMPT — HOÀN THIỆN GAME JAVA OOP 2D
Nên đối chiếu giữa prompt.md và context.md liên tục để đối chiếu tiến độ
## 0. Vai trò và nhiệm vụ

Bạn là Senior Java Game Developer, Software Architect và QA Engineer, chuyên về Java OOP, Java Swing, game 2D và thiết kế hệ thống gameplay có thể mở rộng.

Nhiệm vụ của bạn là **trực tiếp triển khai và hoàn thiện game Java OOP trong repository hiện tại**, bắt đầu rà soát và làm lại gameplay từ Day 1, sau đó triển khai toàn bộ các level còn lại.

Không chỉ đề xuất kiến trúc, viết pseudocode hay tạo skeleton. Hãy thực sự tạo/sửa source code, chạy build, viết test, sửa lỗi và cập nhật tài liệu.

Dự án là game 2D dạng né chướng ngại vật, lấy bối cảnh sinh viên HUST vượt qua các điều kiện giao thông và thời tiết bất thường để đến trường thi.

Tham khảo các tài liệu trong repository:

- `README.md`
- `Story.docx`
- Các file Java hiện có.
- Các file `.properties` hiện có.
- Các bài test có sẵn.
- Tài liệu cuộc họp game nếu có trong repository.

`Story.docx` là nguồn tham khảo về bối cảnh và nội dung từng ngày. **Chưa triển khai cốt truyện, hội thoại hoặc cutscene thực tế ở giai đoạn này.**

### Các nguyên tắc bắt buộc

1. Thiết kế hướng đối tượng triệt để, đúng bản chất OOP.
2. Tuân thủ tính đóng gói, trừu tượng, kế thừa, đa hình và nguyên tắc SOLID ở mức phù hợp.
3. Không dồn toàn bộ logic vào `GamePanel`, `Main` hoặc một class khổng lồ.
4. Logic, model, input, rendering, cấu hình và quản lý level phải tách biệt.
5. Các level độc lập nhưng tái sử dụng tối đa hệ thống chung.
6. Các thông số cân chỉnh gameplay phải được đưa vào `.properties`.
7. `.properties` phải nằm ngay cạnh class Java của từng level.
8. Chưa cần ảnh thật hoặc âm thanh thật. Game phải chạy đầy đủ bằng hình khối placeholder.
9. Việc thêm hình ảnh sau này không được yêu cầu viết lại logic gameplay.
10. Không có Developer Debug Mode, không có F3 debug overlay.
11. Code phải compile, chạy được và có kiểm thử.
12. Sau mỗi giai đoạn phải cập nhật `CONTEXT.md`.
13. Giữ và cải tiến code hiện có nếu phù hợp; không xóa hoặc viết lại một cách thiếu kiểm soát.
14. Không báo cáo đã hoàn thành một chức năng nếu chưa triển khai hoặc chưa xác minh.
15. Ưu tiên tính ổn định, trải nghiệm chơi hợp lý và khả năng bảo trì thay vì thêm các tính năng ngoài yêu cầu.

---

# 1. Công nghệ và cách triển khai

Sử dụng:

- Java 17.
- Java Swing và Java2D.
- Maven.
- JUnit 5.
- Java `Properties`.
- Git repository hiện tại.

Nếu project hiện có sử dụng công nghệ khác, trước hết kiểm tra mức độ tương thích. Ưu tiên giữ những thành phần tốt và chuẩn hóa về cấu trúc đề xuất.

Game phải chạy được trên Windows bằng `run.cmd`.

`Main.java` là điểm khởi chạy duy nhất của ứng dụng.

Không yêu cầu IDE phải mở để chạy game.

Không dùng thư viện game engine lớn khi Java Swing/Java2D đã đáp ứng được yêu cầu.

---

# 2. Rà soát repository trước khi sửa code

Trước khi thực hiện bất kỳ thay đổi lớn nào:

1. Đọc toàn bộ cấu trúc thư mục.
2. Xác định các class, interface và quan hệ đang tồn tại.
3. Đọc logic `Player`, `Vehicle`, `Shark`, `ObstacleSpawner`, `GamePanel`, các Level và Config.
4. Đọc các bài test cũ.
5. Kiểm tra cách build và chạy hiện tại.
6. Xác định những phần có thể tái sử dụng.
7. Xác định code trùng lặp, hardcode, phụ thuộc sai trách nhiệm và lỗi kiến trúc.
8. Ghi lại hiện trạng và kế hoạch thay đổi vào `CONTEXT.md`.

Sau đó tiến hành refactor.

Không làm mất những tính năng đã hoạt động tốt. Nếu cần thay đổi tên class, package hoặc đường dẫn, hãy cập nhật mọi import, resource path, test và tài liệu liên quan.

---

# 3. Cấu trúc thư mục mục tiêu

Giữ tổ chức `src/main/java`, `src/main/resources` và `src/test/java`.

Cấu trúc tham chiếu:

    OOP-PROJECT-20261/
    ├── .gitignore
    ├── README.md
    ├── CONTEXT.md
    ├── Story.docx
    ├── pom.xml
    ├── run.cmd
    └── src/
        ├── main/
        │   ├── java/
        │   │   ├── Main.java
        │   │   └── game/
        │   │       ├── config/
        │   │       │   ├── GameConfig.java
        │   │       │   └── DayConfigLoader.java
        │   │       ├── engine/
        │   │       │   ├── Game.java
        │   │       │   ├── GameLoop.java
        │   │       │   ├── GameState.java
        │   │       │   ├── LevelManager.java
        │   │       │   └── TransitionManager.java
        │   │       ├── level/
        │   │       │   ├── Level.java
        │   │       │   ├── BaseLevel.java
        │   │       │   ├── Levels.java
        │   │       │   ├── day1/
        │   │       │   │   ├── Day1Level.java
        │   │       │   │   └── day1.properties
        │   │       │   ├── day2/
        │   │       │   │   ├── Day2Level.java
        │   │       │   │   └── day2.properties
        │   │       │   ├── day3/
        │   │       │   │   ├── Day3Level.java
        │   │       │   │   └── day3.properties
        │   │       │   ├── day4/
        │   │       │   │   ├── Day4Level.java
        │   │       │   │   └── day4.properties
        │   │       │   └── day5/
        │   │       │       ├── Day5RoadLevel.java
        │   │       │       ├── day5-1.properties
        │   │       │       ├── Day5HelicopterLevel.java
        │   │       │       └── day5-2.properties
        │   │       ├── model/
        │   │       │   ├── Entity.java
        │   │       │   ├── Player.java
        │   │       │   ├── Helicopter.java
        │   │       │   └── obstacle/
        │   │       │       ├── Obstacle.java
        │   │       │       ├── Vehicle.java
        │   │       │       ├── Shark.java
        │   │       │       └── ...
        │   │       ├── logic/
        │   │       │   ├── movement/
        │   │       │   ├── spawn/
        │   │       │   ├── collision/
        │   │       │   ├── effect/
        │   │       │   └── progress/
        │   │       ├── input/
        │   │       │   └── InputHandler.java
        │   │       ├── rendering/
        │   │       │   └── ...
        │   │       └── ui/
        │   │           ├── GamePanel.java
        │   │           ├── GameHUD.java
        │   │           └── ProgressBar.java
        │   └── resources/
        │       ├── image/
        │       │   ├── common/player/
        │       │   ├── day1/
        │       │   ├── day2/
        │       │   ├── day3/
        │       │   ├── day4/
        │       │   └── day5/
        │       └── audio/
        └── test/
            └── java/
                └── game/
                    ├── engine/
                    ├── level/
                    ├── logic/
                    └── model/

Đây là cấu trúc tham chiếu về trách nhiệm. Có thể bổ sung các class, enum, interface và package hợp lý để đáp ứng OOP, nhưng không tạo class rỗng hoặc abstraction không cần thiết.

## 3.1. Quy tắc đặc biệt với `.properties`

Các file cấu hình bắt buộc nằm tại:

- `src/main/java/game/level/day1/day1.properties`
- `src/main/java/game/level/day2/day2.properties`
- `src/main/java/game/level/day3/day3.properties`
- `src/main/java/game/level/day4/day4.properties`
- `src/main/java/game/level/day5/day5-1.properties`
- `src/main/java/game/level/day5/day5-2.properties`

KHÔNG chuyển các file này sang `src/main/resources/config`.

Vì chúng nằm trong source tree Java, phải cấu hình Maven sao cho:

- Java vẫn được compile đúng.
- `.properties` cạnh class được copy vào classpath khi build.
- Các file cấu hình được đóng gói vào JAR.
- Không vô tình đóng gói source `.java` thành resources.
- Các file `.properties` đọc được cả khi chạy từ IDE và JAR.

Có thể dùng Maven resource configuration bổ sung `src/main/java` với include chỉ dành cho `**/*.properties`.

Dùng classpath resource loading, ví dụ:

`Day1Level.class.getResourceAsStream("day1.properties")`

Không sử dụng đường dẫn tương đối phụ thuộc vào working directory.

Kiểm thử việc load cấu hình từ artifact đã build.

Ghi rõ trong README rằng nếu chỉnh `.properties` trong source thì phải build lại để cấu hình mới có hiệu lực trong JAR.

---

# 4. Các yêu cầu OOP bắt buộc

Thiết kế cần thể hiện rõ:

## 4.1. Encapsulation

- Thuộc tính của object phải có phạm vi truy cập phù hợp, ưu tiên `private`.
- Cung cấp getter hoặc phương thức thao tác khi cần thiết.
- Không để các class khác tùy tiện sửa HP, vị trí, vận tốc, state.
- Kiểm tra tính hợp lệ của dữ liệu khi khởi tạo.

## 4.2. Abstraction

Sử dụng interface hoặc abstract class cho các hệ thống có hành vi chung, chẳng hạn:

- Level.
- Entity.
- Obstacle.
- MovementBehavior.
- CollisionEffect.
- SpawnPolicy.

Chỉ tạo abstraction khi có trách nhiệm hoặc biến thể rõ ràng.

## 4.3. Inheritance

Có thể tham khảo quan hệ:

- `BaseLevel` triển khai `Level`.
- Các `DayXLevel` kế thừa `BaseLevel`.
- `Obstacle` kế thừa một entity cơ sở khi hợp lý.
- `Vehicle` kế thừa một loại `Obstacle` thích hợp.
- `Car`, `Bus`, `Bicycle` có thể kế thừa `Vehicle` khi có sự khác biệt hành vi rõ ràng.

Không ép đối tượng không phải xe kế thừa `Vehicle` chỉ vì chuyển động giống xe. Hãy tái sử dụng `MovementBehavior` bằng composition.

## 4.4. Polymorphism

Các hệ thống cập nhật, collision, rendering và spawn phải hoạt động thông qua abstraction chung.

Ví dụ, xử lý nhiều `Obstacle` mà không phải tạo những chuỗi `if-else` lớn để kiểm tra từng class.

Tránh phụ thuộc vào `instanceof` để triển khai toàn bộ gameplay.

Cho phép sử dụng Strategy, Factory và State Pattern khi chúng thực sự đơn giản hóa kiến trúc.

## 4.5. Separation of Concerns

- `Main`: khởi tạo ứng dụng và các tùy chọn chạy.
- `GameLoop`: vòng lặp cập nhật.
- `GamePanel`: hiển thị, không sở hữu luật gameplay.
- `Level`: quản lý một màn chơi.
- `Player`: trạng thái và hành vi nhân vật.
- `Obstacle`: trạng thái và hành vi vật cản.
- `CollisionManager`: phát hiện và điều phối va chạm.
- `EffectManager`: quản lý các hiệu ứng đang tác động.
- `ObstacleSpawner`: sinh đối tượng.
- `SafeSpawnPolicy`: đánh giá tính an toàn khi sinh.
- `ProgressManager`: tính tiến độ.
- `TransitionManager`: fade và chuyển màn.
- `AssetLoader` hoặc renderer tương đương: xử lý tài nguyên ảnh và placeholder.

Không sử dụng static mutable state tràn lan để chia sẻ dữ liệu giữa các hệ thống.

Ưu tiên constructor injection để cung cấp dependencies.

---

# 5. Kích thước và bố cục màn hình

Các giá trị cơ sở đã chốt:

| Thành phần | Kích thước |
|---|---|
| Game viewport | 1000 × 700 px |
| Road | 720 × 700 px |
| Số làn | 3 |
| Mỗi làn | 240 px |
| Player render size | 60 × 90 px |
| Player hitbox | 48 × 76 px |

Tọa độ:

- Road bắt đầu tại X = 140.
- Road kết thúc tại X = 860.
- Làn 1: X = 140–380, tâm X = 260.
- Làn 2: X = 380–620, tâm X = 500.
- Làn 3: X = 620–860, tâm X = 740.
- Hai vùng ngoài road rộng 140 px mỗi bên, chỉ dùng trang trí.

Player đứng ở vùng gần phía dưới màn hình, vị trí Y cố định theo cấu hình khi chơi các level đường bộ.

Xem tọa độ là vị trí góc trái của sprite, nếu chọn quy ước khác phải sử dụng nhất quán toàn project.

Player render rộng 60 px nên giới hạn X góc trái trong `[140, 800]`, đảm bảo toàn bộ sprite nằm trên mặt đường.

Phải phân biệt:

- Kích thước sprite.
- Kích thước hitbox.
- Vị trí thực thể.
- Tâm làn đường.

Hitbox được căn giữa sprite, không dùng toàn bộ 60 × 90 để va chạm.

Có thể chỉnh offset và kích thước hitbox bằng cấu hình.

Giao diện dùng kích thước logic 1000 × 700. Nếu hỗ trợ co giãn cửa sổ, việc scale phải nhất quán cho cả rendering, hitbox và input, không làm biến dạng logic tọa độ.

---

# 6. Điều khiển và vật lý nhân vật

Player được điều khiển bằng:

- `A` hoặc `←`: di chuyển trái.
- `D` hoặc `→`: di chuyển phải.
- `Esc`: Pause/Resume.
- `R`: Restart khi Game Over hoặc ở màn hoàn thành riêng lẻ, nếu phù hợp giao diện.

Không sử dụng F3 debug mode.

## 6.1. Di chuyển ngang tự do

Player di chuyển ngang mượt, không snap giữa ba tâm làn đường.

Không bắt buộc player luôn đứng đúng tâm làn.

Không cho player di chuyển tự do theo chiều dọc trong các level đường bộ.

Camera hoặc môi trường tạo cảm giác nhân vật đang tiến về phía trước bằng cách cho đường và vật cản cuộn theo chiều dọc.

## 6.2. Gia tốc hợp lý

Sử dụng mô hình chuyển động có:

- Vận tốc ngang.
- Gia tốc ngang.
- Giảm tốc khi thả phím.
- Giới hạn tốc độ tối đa.
- Xử lý đổi hướng.
- Xử lý va chạm biên.

Thông số gợi ý ban đầu:

- Maximum horizontal speed: 420 px/s.
- Horizontal acceleration: 1500 px/s².
- Horizontal deceleration: 2000 px/s².

Các giá trị này là mặc định để playtest, không phải hằng số cố định bắt buộc.

Quy tắc:

1. Không tăng tốc vô hạn khi giữ phím.
2. Không cộng vị trí theo lượng cố định mỗi frame.
3. Nhấn hai hướng trái/phải cùng lúc thì không tạo gia tốc trái/phải.
4. Đổi hướng phải giảm vận tốc hiện tại trước khi tăng theo chiều ngược lại.
5. Khi chạm biên phải xóa thành phần vận tốc đang đẩy ra ngoài.
6. Không tích lũy gia tốc khi pause.
7. Không tích lũy phím đang giữ khi focus bị mất hoặc khi fade.
8. Sau hiệu ứng đóng băng, nhân vật không được bất ngờ lao đi do input hoặc vận tốc tích lũy.
9. Khi bị làm chậm, áp dụng multiplier lên thông số vận động một cách có kiểm soát.
10. Hiệu ứng trượt phải làm thay đổi chuyển động theo cách mượt, không tạo cú nhảy vận tốc bất thường.

Ưu tiên fixed timestep, ví dụ 1/60 giây cho logic.

GameLoop phải xử lý chênh lệch thời gian thực, giới hạn lượng thời gian cập nhật dồn khi cửa sổ bị treo hoặc mất focus.

Nếu dùng Swing Timer, không được để các xử lý nặng làm nghẽn Event Dispatch Thread.

Rendering và logic phải sử dụng nhất quán các vị trí từ game state.

---

# 7. Hệ thống máu và va chạm

Player mặc định có 3 HP khi bắt đầu mỗi level.

HP có thể cấu hình.

Thiết kế các hành vi va chạm qua hệ thống OOP, không hardcode toàn bộ trong `CollisionManager`.

## 7.1. Các nhóm va chạm

**LETHAL**

Gây chết ngay, bỏ qua số HP còn lại.

Ví dụ:

- Xe đang chạy dọc xuống theo làn đường.
- Xe đạp hoạt động như phương tiện chạy dọc.
- Xe bus đang di chuyển.
- Cá mập trong trạng thái ATTACK.

**NORMAL_DAMAGE**

Trừ lượng HP cấu hình, mặc định 1 HP.

Ví dụ:

- Rào chắn.
- Xương rồng.
- Người tuyết.
- Xe đứng yên trong tình huống tắc đường.
- Chó chạy ngang.
- Chim bay.
- Thùng hàng trôi ngang.

**SPECIAL_EFFECT**

Áp dụng cơ chế riêng, có thể không trừ HP.

Ví dụ:

- Cống nước: làm chậm mạnh hơn.
- Tảng băng: gây trượt.
- Cầu tuyết: gây đóng băng.

Không tự động coi mọi vật cuộn xuống theo camera là xe chí mạng.

Phân biệt chuyển động do scrolling của thế giới với hành vi thực sự di chuyển dọc làn kiểu Vehicle.

Những obstacle được cấu hình sử dụng hành vi chủ động chạy dọc làn từ trên xuống phải tái sử dụng logic di chuyển kiểu Vehicle và mặc định chí mạng, ngoại trừ Snowball có hiệu ứng riêng.

## 7.2. Va chạm thường

Khi nhận sát thương thường:

- Trừ HP một lần cho lượt tiếp xúc.
- Player nhấp nháy.
- Player giảm tốc nhẹ.
- Tiến độ giảm tốc tạm thời.
- Player được miễn sát thương thường trong thời gian ngắn.

Thông số mặc định gợi ý:

- Invulnerability: 1,1 giây.
- Hit movement multiplier: 0,92.
- Hit slow duration: 0,4 giây.

Các thông số cần có khả năng override trong `.properties`.

Không trừ HP liên tục mỗi frame khi hai hitbox còn chồng nhau.

Một lượt tiếp xúc không được gây nhiều lần sát thương không kiểm soát.

Có thể kết hợp cơ chế invulnerability và theo dõi trạng thái tiếp xúc để tránh gây sát thương lặp.

Va chạm chí mạng vẫn phải gây chết ngay trong lúc player đang miễn sát thương thường.

## 7.3. Thứ tự xử lý

Nếu cùng frame có cả vật gây sát thương thường và vật chí mạng, va chạm chí mạng phải được ưu tiên.

Không được để thứ tự duyệt `ArrayList` quyết định player sống hay chết.

Khi HP về 0 hoặc xảy ra va chạm chí mạng:

- Chuyển GameState sang GAME_OVER.
- Dừng gameplay và spawn.
- Hiển thị màn hình Game Over.
- Cho phép restart.

Restart phải reset đầy đủ HP, velocity, obstacle, hiệu ứng, progress, spawn timer và level state.

---

# 8. Cơ chế tiến độ từng màn

Mỗi level có ProgressManager và thanh tiến độ riêng.

Đây là tiến độ gameplay, không phải số pixel nhân vật đã di chuyển.

## 8.1. Thời lượng

Mặc định:

`level.durationSeconds = 60`

Nếu chơi bình thường, không pause, không gặp hiệu ứng cản trở, thanh tiến độ đầy 100% sau 60 giây gameplay.

Giá trị này được cấu hình độc lập cho từng level, kể cả Day5Road và Day5Helicopter.

Không tự ý cố định thời lượng ngày 5 trong Java. Sau này có thể rút ngắn ngày cuối bằng hai `.properties`.

## 8.2. Công thức

Sử dụng accumulated effective progress time.

Mỗi tick:

`effectiveProgress += deltaTime * progressMultiplier`

`progress = clamp(effectiveProgress / durationSeconds, 0, 1)`

Khi bình thường:

`progressMultiplier = 1.0`

Khi nhận va chạm thường, progressMultiplier giảm nhẹ trong khoảng thời gian được cấu hình.

Ví dụ:

- Normal hit multiplier: 0,8.
- Normal hit penalty duration: 0,8 giây.

Đây chỉ là thông số khởi tạo.

Không giảm ngược phần tiến độ đã đạt được.

Không tính tiến độ trong:

- PAUSED.
- GAME_OVER.
- FADE_OUT.
- FADE_IN.
- Cutscene hoặc dialogue về sau.

Nếu có nhiều hiệu ứng làm chậm, sử dụng chính sách kết hợp rõ ràng; không nhân chồng vô hạn gây tiến độ gần như bằng 0.

Phải tránh áp dụng cùng một hiệu ứng hai lần ngoài ý muốn.

Việc giảm vận tốc ngang và giảm tốc độ tiến độ là hai cơ chế độc lập.

## 8.3. Độ khó theo tiến độ

Độ khó tăng theo phần trăm tiến độ đã đạt được, không phụ thuộc trực tiếp vào thời gian đồng hồ.

Ba giai đoạn mặc định:

- 0–25%: dễ.
- 25–67%: trung bình.
- 67–100%: khó hơn.

Các mốc và hệ số phải cấu hình được.

Khi tiến độ 100%:

1. Ngừng sinh obstacle.
2. Khóa xử lý tạo va chạm mới trong transition.
3. Bắt đầu fade out.
4. Chuyển level hoặc hiển thị hoàn thành.
5. Reset các trạng thái cần thiết.
6. Fade in level mới.

Không chuyển level khi player đã chết trong cùng tick.

---

# 9. Hệ thống sinh chướng ngại vật

Đây là một phần ưu tiên rất cao.

Tạo hoặc refactor:

- `ObstacleSpawner`.
- `SafeSpawnPolicy`.
- `ObstacleFactory`.
- Các cấu hình spawn và các abstraction liên quan.

## 9.1. Sinh theo trọng số

Không dùng xác suất riêng cho từng frame.

Một lượt spawn diễn ra dựa trên khoảng thời gian cấu hình được.

Khi tới lượt, hệ thống chọn loại obstacle theo trọng số trong `.properties`.

Ví dụ:

`spawn.vehicle.weight=55`

`spawn.bicycle.weight=20`

`spawn.barrier.weight=25`

Các giá trị trên là trọng số tương đối.

Không hardcode số lượng obstacle hoặc số phần tử của danh sách.

Dùng cấu trúc động như `ArrayList`, quản lý lifecycle và loại bỏ những object đã ra khỏi màn hình.

Các tham số phải được cấu hình:

- Spawn interval min/max.
- Max active obstacles.
- Spawn weights.
- Speed min/max.
- Width/height.
- Spawn margin.
- Min gap.
- Lane availability.
- Warning delay.
- Safe reaction time.
- Density multiplier.
- Random seed nếu được cung cấp.

## 9.2. Tăng độ khó theo từng giai đoạn

Ban đầu spawn thưa.

Giai đoạn giữa tăng mật độ vừa phải.

Giai đoạn cuối tăng mật độ và sự phối hợp giữa obstacle, nhưng vẫn đảm bảo có đường né.

Ưu tiên thay đổi nhịp độ spawn thay vì đột ngột tăng tốc độ xe.

Không tạo các tổ hợp tấn công khiến player bị khóa điều khiển rồi chắc chắn bị xe đâm.

## 9.3. SafeSpawnPolicy

Bắt buộc thiết kế hệ thống tránh tạo ra tình huống hết đường đi.

Vì game chỉ có ba làn, không được chỉ đếm số làn trống.

SafeSpawnPolicy phải xét:

- X/Y và hitbox của player.
- Vận tốc ngang hiện tại.
- Gia tốc, giảm tốc và tốc độ tối đa.
- Hiệu ứng làm chậm hoặc đóng băng.
- Vị trí và quỹ đạo của các obstacle hiện có.
- Kích thước hitbox.
- Thời gian vật cản đến vùng player.
- Thời gian phản ứng tối thiểu.
- Khả năng di chuyển từ vùng an toàn hiện tại sang vùng an toàn tiếp theo.

Có thể triển khai thuật toán kiểm tra khả năng tiếp cận bằng không gian trạng thái rời rạc theo trục ngang và thời gian.

Do player di chuyển ngang tự do, mô hình phải bảo thủ và tính đến bề rộng hitbox, không được giả định có thể dịch chuyển tức thời sang làn khác.

Một ứng viên spawn chỉ được chấp nhận khi còn ít nhất một quỹ đạo né khả thi trong khoảng thời gian dự đoán.

Phải bảo đảm không sinh hàng ba xe chí mạng che kín cả ba làn tại cùng một thời điểm va chạm.

Không sinh obstacle đè trực tiếp lên player.

Không sinh xe chồng hitbox bất hợp lý với các xe khác.

Với các obstacle băng ngang, phải dự đoán thời điểm giao cắt.

Với cá mập, phải xét trước vùng cảnh báo và vùng tấn công.

Với Day 4, hệ thống còn phải tránh tình huống vật phẩm hồi năng lượng cần thiết xuất hiện ở vị trí không thể lấy được.

Nếu một lần spawn không có vị trí hợp lệ:

- Thử các vị trí/thời điểm khác trong giới hạn hợp lý.
- Nếu vẫn không hợp lệ, bỏ qua lượt spawn.
- Không cưỡng ép sinh obstacle.

Cần có random seed để tái hiện tình huống lỗi trong test.

Không được tuyên bố thuật toán bảo đảm an toàn tuyệt đối chỉ dựa vào việc còn một làn trống.

Mục tiêu là bảo đảm tính khả thi theo mô hình vật lý và các chuyển động đã triển khai, đồng thời kiểm thử rộng rãi bằng mô phỏng.

---

# 10. DAY 1 — GIAO THÔNG BÌNH THƯỜNG

File:

- `Day1Level.java`
- `day1.properties`

Bối cảnh: đường phố bình thường, bắt đầu tuần thi.

Có tối đa ba nhóm chướng ngại vật:

1. Vehicle/Car.
2. Bicycle.
3. Barrier.

## 10.1. Vehicle/Car

- Di chuyển dọc xuống theo làn.
- Có tốc độ ngẫu nhiên trong khoảng cấu hình.
- Có thể thay đổi tốc độ hoặc đổi làn ở mức giới hạn.
- Tất cả hành vi đổi làn phải được đưa vào dự đoán SafeSpawnPolicy.
- Không đổi làn bất ngờ sát player.
- Va chạm gây chết ngay.

## 10.2. Bicycle

- Tái sử dụng logic di chuyển dọc làn của Vehicle.
- Thường chậm hơn ô tô.
- Có kích thước nhỏ hơn.
- Va chạm gây chết ngay theo quy tắc phương tiện chạy dọc.

## 10.3. Barrier

- Vật cản đứng yên trong thế giới game.
- Không có hành vi Vehicle.
- Gây mất 1 HP.
- Kích hoạt nhấp nháy và giảm tốc nhẹ.

Đường, xe và barrier được render bằng các hình khối có màu dễ phân biệt.

Cấu hình gợi ý ban đầu:

- duration: 60 giây.
- car weight: 55.
- bicycle weight: 20.
- barrier weight: 25.
- spawn interval ban đầu: khoảng 1,1–1,7 giây.
- Bắt đầu màn có một khoảng ngắn không spawn để player làm quen.

Cân chỉnh thêm các tốc độ, kích thước và khoảng cách an toàn sau khi kiểm thử.

---

# 11. DAY 2 — MƯA LỚN, NGẬP ĐƯỜNG

File:

- `Day2Level.java`
- `day2.properties`

Ba loại chướng ngại vật:

1. Shark.
2. Drain.
3. FloatingCrate.

## 11.1. Shark

Đây là obstacle đặc biệt, phải sử dụng Finite State Machine.

Các trạng thái:

`FIN_VISIBLE → DIVING → WARNING → ATTACK → DISAPPEAR`

**FIN_VISIBLE**

Vây cá mập nhô khỏi mặt nước trong thời gian ngắn.

Chưa gây sát thương chí mạng.

**DIVING**

Cá mập lặn xuống, không còn hiển thị thân/vây.

Có thể xác định vùng tấn công tiếp theo theo logic cấu hình.

Không được xuất hiện bất ngờ trực tiếp trên player rồi cắn ngay.

**WARNING**

Tại vị trí dự kiến tấn công xuất hiện:

- Vùng nước tối hoặc đen.
- Dấu chấm than `!` nhấp nháy.
- Có thời gian cảnh báo rõ ràng.

Vùng cảnh báo phải tương ứng với hitbox nguy hiểm khi ATTACK, đủ để người chơi hiểu nơi cần né.

Không được thay đổi đột ngột vị trí tấn công sau khi đã cảnh báo.

**ATTACK**

Cá mập trồi lên đớp.

Nếu hitbox tấn công giao với hitbox player:

**GAME OVER NGAY LẬP TỨC.**

Không trừ 1 HP rồi cho player tiếp tục.

ATTACK phải gây chết ngay kể cả khi player đang trong thời gian miễn sát thương thường.

**DISAPPEAR**

Cá mập biến mất, được đánh dấu để xóa khỏi danh sách khi phù hợp.

Thời gian tham khảo:

- FIN_VISIBLE: 0,7 giây.
- DIVING: 0,9 giây.
- WARNING: 1,2 giây.
- ATTACK: 0,4 giây.

Tất cả nằm trong `day2.properties`.

Việc chuyển trạng thái phải dựa trên timer và state rõ ràng, không viết bằng nhiều boolean không kiểm soát.

## 11.2. Drain

Cống nước gây giảm tốc mạnh khi player đi vào.

Đề xuất:

- Movement multiplier: 0,65.
- Thời gian hiệu ứng: cấu hình được.

Không mặc định gây chết.

Có thể áp dụng mức giảm tiến độ riêng cho cống, nhưng phải bảo đảm không nhân chồng quá mức với hiệu ứng khác.

## 11.3. FloatingCrate

Thùng hàng trôi trong nước.

- Trôi ngang hoặc lệch ngang chậm.
- Không sử dụng hành vi chủ động lao xuống dọc làn kiểu Vehicle.
- Va chạm gây 1 HP damage.
- Không xuất hiện dày đặc.

Mô hình hóa chuyển động do cuộn thế giới và chuyển động riêng của crate một cách rõ ràng.

---

# 12. DAY 3 — BĂNG TUYẾT

File:

- `Day3Level.java`
- `day3.properties`

Ba loại chướng ngại vật:

1. IcePatch.
2. Snowball.
3. Snowman.

## 12.1. IcePatch

Tảng băng/vùng đường trơn.

Khi player đi qua:

- Player bị trượt.
- Có thể tăng tốc độ dịch chuyển ngang nhẹ.
- Giảm hiệu quả hãm trong thời gian ngắn.
- Không được tạo gia tốc bất thường.
- Không mặc định gây sát thương.

Các thông số độ trượt, tốc độ tối đa và thời gian hiệu ứng được cấu hình.

## 12.2. Snowball

Cầu tuyết lăn xuống.

Đây là ngoại lệ của quy tắc di chuyển dọc.

Có thể sử dụng riêng movement behavior kiểu lăn xuống.

Khi va chạm:

- Không chết ngay.
- Player bị đóng băng trong thời gian ngắn.
- Mặc định không trừ HP, trừ khi cấu hình đặc biệt yêu cầu.

Thời gian gợi ý: khoảng 0,6 giây.

Không giữ lại gia tốc gây lao vọt khi hết đóng băng.

SafeSpawnPolicy phải xét trạng thái đóng băng để tránh tạo tình huống người chơi bị xe hoặc vật cản khác đâm mà không có cách né.

## 12.3. Snowman

Người tuyết đứng yên.

- Gây 1 HP damage khi va chạm.
- Sử dụng logic vật cản tĩnh.
- Có thể vỡ/biến mất khi bị chạm, bằng placeholder đơn giản.

---

# 13. DAY 4 — NẮNG NÓNG

File:

- `Day4Level.java`
- `day4.properties`

Các thành phần:

1. Bus.
2. Cactus.
3. GlareEffect.

Ngoài ra có hệ thống Energy và EnergyPickup.

## 13.1. Bus

- Tái sử dụng logic Vehicle.
- Di chuyển dọc xuống.
- Kích thước lớn hơn Car.
- Tốc độ tương đối thấp.
- Va chạm gây chết ngay.
- Không sinh trong trường hợp chiếm hết đường né.

## 13.2. Cactus

Xương rồng đứng yên.

- Gây mất 1 HP.
- Nhấp nháy player.
- Giảm tốc nhẹ.

## 13.3. GlareEffect

Hiệu ứng chói mắt.

Đây là environmental effect, không phải vật cản vật lý thông thường.

Một số chướng ngại vật ở xa sẽ bị giảm độ rõ hoặc bị che mờ, nhưng không được biến mất hoàn toàn một cách bất công.

Khi đến gần thì hiện rõ dần.

Khoảng cách hiện rõ, cường độ mờ và loại obstacle chịu ảnh hưởng phải cấu hình được.

Các chướng ngại vật lớn và chí mạng phải được nhìn thấy đủ sớm để có thể phản ứng.

## 13.4. Energy System

Tạo hệ thống năng lượng riêng, không gộp trực tiếp với HP.

- Energy giảm dần khi gameplay đang chạy.
- Không giảm trong pause, fade hoặc game over.
- EnergyPickup hồi năng lượng.
- Có EnergyBar trên HUD.
- Khi Energy = 0, Game Over.

Các thông số gồm:

- Initial energy.
- Maximum energy.
- Drain per second.
- Recovery amount.
- Pickup frequency.
- Minimum safe energy reserve.
- Pickup hitbox.
- Pickup sprite path.

Việc spawn pickup phải bảo đảm mức độ khả thi: không được để người chơi chắc chắn hết năng lượng chỉ vì RNG không tạo vật phẩm hồi phục.

Pickup không phải obstacle gây sát thương.

---

# 14. DAY 5 — HAI LEVEL ĐỘC LẬP

Ngày 5 phải tách thành hai level thực sự độc lập.

Không gộp hai giai đoạn trong cùng một `Day5Level`.

## 14.1. Day5RoadLevel

File:

- `Day5RoadLevel.java`
- `day5-1.properties`

Mã test: `51`.

Bối cảnh đường phố bị tắc.

Chướng ngại vật:

- TrafficJamVehicle.
- Dog.

**TrafficJamVehicle**

- Xe đứng yên hoặc gần như đứng yên.
- Dùng để tạo cảm giác tắc đường.
- Không mặc định gây chết ngay như Vehicle đang chạy tốc độ cao.
- Va chạm gây 1 HP damage.
- Phải tạo thành các cụm xe hợp lý nhưng luôn chừa đường đi khả thi.

Không được sinh ba xe đứng chắn toàn bộ chiều ngang mà không có lối vượt.

**Dog**

- Chó chạy ngang đường.
- Di chuyển từ trái sang phải hoặc ngược lại.
- Tốc độ và thời điểm xuất hiện ngẫu nhiên có giới hạn.
- Va chạm gây 1 HP damage.
- Có thể có cảnh báo ngắn bằng placeholder nếu cần.

Đây là giai đoạn đơn giản và tương đối ngắn về nội dung gameplay, không cần thêm nhiều loại vật cản.

Thời lượng vẫn lấy từ `.properties`, mặc định 60 giây theo quy tắc chung. Có thể rút ngắn sau khi playtest.

Khi hoàn thành:

- Fade out.
- Chuyển trực tiếp sang Day5HelicopterLevel.
- Fade in.

Chưa làm cutscene trực thăng xuất hiện. Chỉ chuẩn bị điểm chuyển tiếp rõ ràng để bổ sung sau này.

## 14.2. Day5HelicopterLevel

File:

- `Day5HelicopterLevel.java`
- `day5-2.properties`

Mã test: `52`.

Đây là level chơi riêng, không phải một trạng thái tạm bên trong Day5RoadLevel.

Bối cảnh: Nam lái trực thăng đưa Thắng đến trường.

Khi chưa có ảnh thật:

- Trực thăng có hình khối placeholder riêng.
- Nền bầu trời placeholder.
- Chim được biểu diễn bằng hình khối dễ nhìn.

Có thể tạo class `Helicopter` dùng chung abstraction của controllable entity với Player.

Điều khiển ngang tự do, mượt, có vận tốc và gia tốc giới hạn.

Không bắt buộc áp dụng giới hạn ba làn đường cho màn trên không. Giới hạn ngang dựa trên vùng bay được cấu hình trong viewport.

Obstacle chính:

**Bird**

- Bay ngang qua vùng chơi.
- Tốc độ vừa phải.
- Có vị trí và hướng bay ngẫu nhiên.
- Va chạm gây 1 HP.
- Không cần AI đuổi theo trực thăng.

SafeSpawnPolicy vẫn phải được áp dụng để các đàn chim không tạo ra tình huống bất khả thi.

Có thanh tiến độ riêng, bắt đầu từ 0%.

Có `.properties` riêng và thời lượng mặc định 60 giây.

HP khởi tạo theo cấu hình của level này, mặc định 3 HP.

Hoàn thành level này sẽ dẫn đến màn hình thông báo hoàn thành game ở No-Story Mode.

Không triển khai hội thoại Happy Ending tại giai đoạn này.

---

# 15. Cấu hình `.properties` chi tiết

Mọi giá trị cần cân chỉnh gameplay phải nằm trong config, thay vì rải rác hardcode trong các phương thức.

Ví dụ các nhóm key cần có:

## Level

`level.durationSeconds`

`level.initialHealth`

`level.scrollSpeed`

`level.backgroundColor`

## Progress

`progress.normalMultiplier`

`progress.hitMultiplier`

`progress.hitPenaltyDurationSeconds`

## Player

`player.maxSpeed`

`player.acceleration`

`player.deceleration`

`player.hitboxWidth`

`player.hitboxHeight`

`player.hitboxOffsetX`

`player.hitboxOffsetY`

`player.hitInvulnerabilitySeconds`

`player.hitSlowMultiplier`

`player.hitSlowDurationSeconds`

## Spawn

`spawn.minIntervalSeconds`

`spawn.maxIntervalSeconds`

`spawn.maxActiveObstacles`

`spawn.initialDelaySeconds`

`spawn.randomSeed`

`spawn.safeReactionTimeSeconds`

`spawn.minimumGap`

## Difficulty

`difficulty.phase1End`

`difficulty.phase2End`

`difficulty.phase1SpawnMultiplier`

`difficulty.phase2SpawnMultiplier`

`difficulty.phase3SpawnMultiplier`

## Obstacle-specific

Ví dụ:

`vehicle.spawnWeight`

`vehicle.minSpeed`

`vehicle.maxSpeed`

`vehicle.width`

`vehicle.height`

`vehicle.collisionType`

`shark.finDuration`

`shark.divingDuration`

`shark.warningDuration`

`shark.attackDuration`

`drain.slowMultiplier`

`snowball.freezeDuration`

Các giá trị phải có đơn vị nhất quán và được tài liệu hóa.

Không cần giữ chính xác tên key ví dụ trên nếu đã có naming convention tốt hơn, nhưng phải bảo đảm tất cả hệ thống liên quan đều đọc được config.

## 15.1. Config validation

DayConfigLoader cần:

- Kiểm tra key bắt buộc.
- Kiểm tra giá trị số hợp lệ.
- Kiểm tra khoảng giá trị.
- Kiểm tra spawn weight không âm.
- Kiểm tra min <= max.
- Kiểm tra duration > 0.
- Kiểm tra kích thước hitbox hợp lệ.
- Kiểm tra mốc difficulty tăng dần.

Nếu cấu hình lỗi, phải báo lỗi rõ ràng.

Không âm thầm thay mọi dữ liệu sai bằng giá trị mặc định khiến người lập trình không biết config đang bị lỗi.

Có thể sử dụng immutable config objects và các config object chuyên biệt theo hệ thống.

Phân biệt thông số bất biến của viewport và thông số gameplay thay đổi theo từng ngày.

---

# 16. Hình ảnh, tài nguyên và placeholder

Chưa cần sử dụng hình ảnh thật.

Tất cả thành phần của game phải render được bằng Java2D:

- Player.
- Helicopter.
- Road.
- Background.
- Lane markings.
- Vehicle.
- Barrier.
- Shark.
- Warning area.
- Drain.
- Snowball.
- EnergyPickup.
- Dog.
- Bird.
- Các obstacle khác.

Dùng màu sắc và hình dạng khác nhau để người chơi phân biệt được.

Đặc biệt:

- Cá mập phải có vây, dấu `!` và vùng nước đen.
- IcePatch phải trông khác Snowman.
- Vehicle đang chạy phải khác xe đứng tắc đường.
- EnergyPickup phải khác obstacle gây hại.

Mỗi entity nên hỗ trợ một đường dẫn sprite trong config, ví dụ:

`sprite.player=/image/common/player/player.png`

`sprite.background=/image/day1/background.png`

`sprite.road=/image/day1/road.png`

`sprite.vehicle=/image/day1/vehicle.png`

Tài nguyên được load thông qua classpath.

Nếu file không tồn tại:

- Không crash.
- Dùng placeholder renderer.
- Không thay đổi logic hoặc hitbox.

Tách renderer khỏi model.

Khi thêm sprite thật, giữ nguyên kích thước logic và collider, trừ khi config được thay đổi có chủ đích.

`background.png` dự kiến kích thước 1000 × 700.

`road.png` dự kiến kích thước 720 × 700, đã bao gồm ba làn.

Khi render road.png thật, không tự vẽ thêm các vạch làn đè lên ảnh, trừ khi cấu hình cho phép.

Có thể tạo lớp resource manager/cache để tránh đọc lại file ảnh mỗi frame.

Không tự tìm hoặc tải asset từ Internet.

---

# 17. Giao diện, HUD và thanh tiến độ

Tạo HUD hoạt động thực sự, không chỉ trang trí.

HUD gồm:

- HP hiện tại.
- Level hiện tại.
- ProgressBar.
- EnergyBar chỉ ở level cần năng lượng.
- Thông báo Pause/Game Over/Level Complete khi thích hợp.

## 17.1. ProgressBar

Đặt ở góc trên phải, có kích thước và lề chỉnh được bằng cấu hình giao diện.

Gồm:

1. Một thanh chữ nhật nằm ngang.
2. Phần đã hoàn thành được tô dần.
3. Một ô vuông nhỏ tại đầu phần đã hoàn thành.
4. Ô vuông là vị trí để gắn ảnh mặt nhân vật.

Trong giai đoạn placeholder:

- Ô vuông vẽ hình đơn giản hoặc ký hiệu nhân vật.
- Không cần ảnh mặt thật.

Ô vuông di chuyển theo tiến độ từ 0% đến 100%.

Phải clamp vị trí để ô vuông không bị vẽ ra ngoài vùng HUD ở hai đầu thanh.

HUD phải đứng yên khi map cuộn.

## 17.2. Pause

Phím `Esc` chuyển giữa PLAYING và PAUSED.

Khi PAUSED:

- Dừng chuyển động.
- Dừng spawn.
- Dừng progress.
- Dừng effect timers.
- Dừng Energy drain.
- Không nhận thêm damage.
- Hiển thị overlay PAUSED.

Nhấn Esc lần nữa tiếp tục.

Không tạo hiện tượng dồn thời gian cập nhật khi resume.

Nếu cửa sổ mất focus, phải xóa trạng thái phím đang giữ. Có thể tự động pause nếu hợp lý.

## 17.3. Game Over

Hiển thị overlay rõ ràng.

Có hướng dẫn chơi lại bằng `R`.

Restart không tạo thêm GameLoop hoặc Timer cũ còn chạy.

## 17.4. Fade transition

Khi level hoàn thành:

1. Chuyển sang trạng thái TRANSITION.
2. Mờ dần sang màu đen.
3. Khi đã che hết màn hình, thay level.
4. Khởi tạo level mới.
5. Mờ dần từ đen về gameplay.

Thời gian gợi ý:

- Fade out: 0,5 giây.
- Fade in: 0,5 giây.

Đặt thời gian fade trong config chung.

Hiệu ứng phải mượt, sử dụng alpha từ 0 đến 1 thay vì đổi màn đột ngột.

Không update gameplay, progress, damage hoặc spawn trong lúc fade.

Không cho phép nhiều yêu cầu chuyển màn được kích hoạt chồng nhau.

---

# 18. No-Story Mode và chọn màn test

Chưa triển khai Story Mode.

Chỉ có **No-Story Mode**.

Mục tiêu là test và hoàn thiện đầy đủ gameplay trước khi thêm story/cutscene.

Không cần viết hội thoại từ Story.docx vào UI ở giai đoạn này.

Tuy nhiên, LevelManager và GameState phải đủ mở để tích hợp intro, cutscene và hội thoại về sau.

## 18.1. Chạy tất cả màn

Luồng mặc định:

`Day1 → Fade → Day2 → Fade → Day3 → Fade → Day4 → Fade → Day5Road → Fade → Day5Helicopter → Game Complete`

## 18.2. Chạy riêng một màn

Trong `Main.java`, cung cấp các biến cấu hình dễ tìm ở đầu class, ví dụ:

`RUN_ALL_LEVELS = true`

`SELECTED_LEVEL = 1`

`AUTO_ADVANCE = true`

Khi cần test riêng:

`RUN_ALL_LEVELS = false`

`SELECTED_LEVEL = 3`

`AUTO_ADVANCE = false`

Các mã level:

| Mã | Level |
|---|---|
| 1 | Day1Level |
| 2 | Day2Level |
| 3 | Day3Level |
| 4 | Day4Level |
| 51 | Day5RoadLevel |
| 52 | Day5HelicopterLevel |

Khi test riêng một level:

- Bắt đầu trực tiếp level đó.
- Không yêu cầu chơi những level trước.
- Không phát story.
- Tiến độ bắt đầu 0%.
- Dùng config riêng của level.
- Khi hoàn thành, hiển thị Level Complete nếu AUTO_ADVANCE = false.
- Có thể restart level mà không thoát ứng dụng.

Nếu AUTO_ADVANCE được bật, LevelManager có thể tiếp tục đến level kế tiếp trong thứ tự chính thức.

Thực hiện mapping level bằng enum, registry hoặc factory phù hợp, không dùng các chuỗi điều kiện thiếu tổ chức.

---

# 19. Game Loop và quản lý trạng thái

Tạo hệ thống quản lý trạng thái rõ ràng.

Các trạng thái cần thiết có thể gồm:

- PLAYING.
- PAUSED.
- TRANSITION.
- GAME_OVER.
- LEVEL_COMPLETE.
- GAME_COMPLETE.

Có thể bổ sung trạng thái khác khi thật sự cần.

Không tạo một boolean độc lập cho mỗi tình trạng rồi để chúng mâu thuẫn nhau.

GameLoop chỉ cập nhật những hệ thống được phép hoạt động trong trạng thái hiện tại.

Các phép tính thời gian, vật lý, spawn và hiệu ứng phải ổn định ở nhiều mức FPS.

Bảo đảm:

- Không có nhiều loop hoạt động cùng lúc.
- Không rò rỉ timer sau restart.
- Không tạo obstacle mới sau game over.
- Không bị ConcurrentModificationException khi xóa obstacle.
- Không còn object của level trước sau transition.
- Không truyền input từ màn trước sang màn mới một cách ngoài ý muốn.
- Không có NullPointerException khi thiếu asset.
- Không xử lý collision với entity đã bị xóa.
- Không bị thắng và thua đồng thời do thứ tự xử lý sai.

---

# 20. Kiểm thử bắt buộc

Viết unit test bằng JUnit 5.

Không chỉ giữ các test cũ. Phải sửa và mở rộng theo kiến trúc mới.

## 20.1. Config tests

Kiểm tra:

- Load đúng cả sáu file `.properties`.
- Config nằm cạnh class Java.
- Load thành công từ classpath sau build.
- Thiếu key bắt buộc.
- Thông số âm hoặc vượt giới hạn.
- Spawn weight và difficulty phase.
- Asset path thiếu không làm game crash.

## 20.2. Player movement tests

Kiểm tra:

- Di chuyển trái/phải.
- Giới hạn vận tốc.
- Gia tốc đúng.
- Giảm tốc khi thả phím.
- Đổi hướng.
- Hai phím đối lập cùng nhấn.
- Không vượt khỏi road.
- Không tích lũy vận tốc ở biên.
- Không tích lũy chuyển động khi pause.
- Hit slow.
- Freeze.
- Ice sliding.

## 20.3. Collision tests

Kiểm tra:

- Hitbox nhỏ hơn render size.
- Collider đúng vị trí.
- Mất 1 HP khi chạm vật tĩnh.
- Không mất HP liên tục mỗi frame.
- Invulnerability hoạt động.
- Va chạm chí mạng bỏ qua invulnerability.
- Vehicle đang chạy gây chết.
- TrafficJamVehicle đứng yên chỉ gây sát thương thường.
- Shark WARNING chưa giết player.
- Shark ATTACK giết ngay.
- Snowball đóng băng nhưng không giết ngay.
- HP về 0 gây Game Over.

## 20.4. Spawn tests

Dùng random seed cố định.

Kiểm tra:

- Không spawn chồng player.
- Không spawn ba vật chí mạng bít toàn bộ đường.
- Không sinh obstacle quá sát để player không kịp phản ứng.
- Không chấp nhận spawn khi không còn đường né khả thi.
- Có thể bỏ qua lượt spawn không an toàn.
- Có thể xóa obstacle ra khỏi màn.
- Max active obstacles được tôn trọng.
- Spawn weight và density thay đổi đúng.
- Không xuất hiện overlap vô lý khi obstacle đổi làn.
- Pickup năng lượng có thể xuất hiện ở vùng tiếp cận được.

Chạy mô phỏng với ít nhất 1000 seed/tình huống khởi tạo khác nhau nếu thời gian thực thi cho phép.

Có thể dùng nhiều tick giả lập cho mỗi seed.

Tạo các bài test tình huống khó được thiết kế thủ công, không chỉ random test.

Ghi rõ giới hạn của mô hình kiểm tra an toàn nếu chưa thể chứng minh mọi tình huống.

## 20.5. Shark tests

Kiểm tra chuỗi chuyển state:

`FIN_VISIBLE → DIVING → WARNING → ATTACK → DISAPPEAR`

Kiểm tra các timer.

Kiểm tra attack hitbox.

Kiểm tra warning duration và vị trí cảnh báo.

Kiểm tra shark không giết player trước trạng thái ATTACK.

## 20.6. Progress tests

Kiểm tra:

- Sau 60 giây gameplay bình thường đạt 100%.
- Sau 30 giây bình thường đạt khoảng 50%.
- Pause không tăng tiến độ.
- Fade không tăng tiến độ.
- Va chạm làm progress tăng chậm trong thời gian quy định.
- Progress không giảm ngược.
- Progress không vượt 100%.
- Restart reset progress.
- Level mới có progress riêng.
- Thời lượng riêng của ngày 5 được đọc đúng.

## 20.7. Level and transition tests

Kiểm tra:

- Khởi chạy được 1, 2, 3, 4, 51, 52.
- Level hoàn thành thì chuyển đúng level tiếp theo.
- Day5Road chuyển sang Day5Helicopter.
- Không phát sinh gameplay trong transition.
- Không có hai lần transition chồng nhau.
- Restart không giữ lại object của lượt trước.
- Hoàn thành level cuối chuyển sang GAME_COMPLETE.

## 20.8. Build và smoke test

Chạy các lệnh phù hợp, bao gồm:

- `mvn clean test`
- `mvn package`

Kiểm tra `run.cmd`.

Nếu môi trường hỗ trợ GUI, chạy smoke test các level.

Nếu môi trường headless không mở được Swing, sử dụng headless tests để kiểm chứng logic và báo rõ phần GUI chưa được kiểm thử trực quan.

Không được báo cáo pass nếu test thực tế chưa chạy.

---

# 21. README.md và CONTEXT.md

Đây là yêu cầu bắt buộc, không được bỏ qua.

## README.md

Cập nhật:

- Tổng quan game.
- Yêu cầu Java/Maven.
- Cách chạy bằng `run.cmd`.
- Cách build.
- Cách chạy các test.
- Cách chọn level trong Main.java.
- Mã level 1, 2, 3, 4, 51, 52.
- Các phím điều khiển.
- Các cơ chế từng ngày.
- Cách chỉnh `.properties`.
- Cách thêm sprite.
- Vị trí tài nguyên.
- Giới hạn hiện tại của No-Story Mode.

## CONTEXT.md

Tạo hoặc cập nhật tài liệu kỹ thuật để Codex có thể tiếp tục làm việc ở các phiên sau mà không mất context.

Bắt buộc ghi:

1. Mục tiêu dự án.
2. Các quyết định thiết kế đã chốt.
3. Cấu trúc thư mục hiện tại.
4. Danh sách class/interface và trách nhiệm.
5. Mối quan hệ OOP quan trọng.
6. Cách hoạt động của GameLoop.
7. Cách quản lý Level và GameState.
8. Cách hoạt động của CollisionManager.
9. Cách hoạt động của ObstacleSpawner và SafeSpawnPolicy.
10. Các movement behavior.
11. Các loại obstacle và collision effect.
12. Shark state machine.
13. Progress system.
14. Các thông số cấu hình quan trọng.
15. Cách load `.properties` đặt trong `src/main/java`.
16. Cách tích hợp hình ảnh trong tương lai.
17. Các bước đã hoàn thành.
18. Kết quả build và test.
19. Các lỗi hoặc hạn chế chưa xử lý.
20. Các nhiệm vụ còn lại.

Sau mỗi giai đoạn lớn, cập nhật CONTEXT.md ngay.

Không đợi tới cuối toàn bộ dự án mới viết context.

Nếu có thay đổi kiến trúc so với kế hoạch, phải giải thích trong CONTEXT.md.

---

# 22. Quy trình triển khai bắt buộc

Hãy triển khai tuần tự theo các giai đoạn sau, nhưng tiếp tục tự động đến giai đoạn cuối nếu môi trường cho phép.

## PHASE 1 — Rà soát và chuẩn hóa project

- Đọc project hiện có.
- Xác định các phần tái sử dụng được.
- Chuẩn hóa Maven.
- Chuẩn hóa entry point.
- Chuyển cấu trúc về các package phù hợp.
- Giữ `.properties` cạnh các level.
- Xây config loader.
- Cập nhật README và CONTEXT.

Điều kiện hoàn thành: project compile được và config loader có test.

## PHASE 2 — Core Engine và Day 1

Triển khai:

- GameLoop.
- GameState.
- Input.
- Player movement.
- Entity/Obstacle abstraction.
- Vehicle.
- Collision.
- Spawn.
- SafeSpawnPolicy.
- Progress.
- HUD.
- Pause.
- Restart.
- Day1Level.

Hoàn thiện Day 1 từ đầu.

Day 1 phải chơi được bằng hình khối.

Không tiếp tục tạo ra các bản sao GameLoop cho từng ngày.

Chạy test sau giai đoạn này.

## PHASE 3 — Day 2 và Day 3

Triển khai:

- Shark state machine.
- Drain.
- FloatingCrate.
- IcePatch.
- Snowball.
- Snowman.
- EffectManager.

Tái sử dụng core systems đã tạo.

Chạy test cho state machine, movement effect, collision và spawn.

## PHASE 4 — Day 4

Triển khai:

- Bus.
- Cactus.
- GlareEffect.
- EnergySystem.
- EnergyPickup.

Kiểm thử khả năng hoàn thành màn và khả năng thu thập năng lượng.

## PHASE 5 — Hai level ngày 5

Triển khai:

- Day5RoadLevel.
- TrafficJamVehicle.
- Dog.
- Day5HelicopterLevel.
- Helicopter.
- Bird.

Hai level phải có progress và properties riêng.

Kiểm thử việc chọn trực tiếp level 51 và 52.

## PHASE 6 — Fade và toàn bộ No-Story flow

Hoàn thiện:

- TransitionManager.
- Fade out / fade in.
- Auto advance.
- Game Over overlay.
- Game Complete overlay.
- Restart.
- Pause.
- Chạy liên tục toàn bộ sáu level.

Không yêu cầu intro, story dialogue hay cutscene.

## PHASE 7 — Kiểm thử, cân bằng và tài liệu

- Chạy toàn bộ unit tests.
- Chạy build.
- Chạy các smoke tests có thể thực hiện.
- Tìm và sửa lỗi.
- Kiểm tra memory/timer lifecycle.
- Cân chỉnh spawn rate mặc định.
- Kiểm tra khả năng né vật cản.
- Kiểm tra tất cả config và asset fallback.
- Hoàn thiện README.md.
- Hoàn thiện CONTEXT.md.

Không bỏ qua lỗi chỉ để có báo cáo hoàn thành.

---

# 23. Các lỗi đặc biệt phải tránh

Không được:

1. Hardcode spawn rate trong level.
2. Đọc `.properties` bằng đường dẫn phụ thuộc thư mục hiện tại.
3. Vẽ và xử lý toàn bộ collision trực tiếp trong GamePanel.
4. Dùng một GameLoop riêng cho mỗi level.
5. Sử dụng nhiều Timer mà không quản lý lifecycle.
6. Để player tăng tốc vô hạn.
7. Để player lọt ra ngoài vùng hợp lệ.
8. Để xe sinh che kín cả ba làn mà không có lối thoát.
9. Để player chịu damage liên tục mỗi frame.
10. Để Shark cắn trước khi cảnh báo hoàn tất.
11. Để va chạm chí mạng bị vô hiệu hóa bởi invulnerability thường.
12. Để pause nhưng effect timer hoặc progress vẫn tăng.
13. Để hiệu ứng fade hoàn thành rồi spawn cũ vẫn tồn tại trong level mới.
14. Để energy tiếp tục giảm khi pause.
15. Để missing sprite gây crash.
16. Để config của Day 1 ảnh hưởng sai đến Day 2.
17. Để test mode không thể chạy trực tiếp Day5Road hoặc Day5Helicopter.
18. Dùng các chuỗi `instanceof` lớn thay cho polymorphism.
19. Dùng public mutable fields khi không cần.
20. Viết quá nhiều code trùng lặp giữa các level.
21. Tạo hàng chục design pattern hoặc class không có trách nhiệm thực sự.
22. Xóa test cũ chỉ để làm build xanh.
23. Để lại các phần gameplay bắt buộc dưới dạng TODO hoặc implementation giả.
24. Tự tuyên bố tính năng hoàn thành nếu chỉ có skeleton.
25. Thêm Debug Mode hoặc F3 overlay trái yêu cầu.

---

# 24. Tiêu chí nghiệm thu cuối cùng

Chỉ coi nhiệm vụ hoàn thành khi đạt các điều kiện sau:

- [ ] Game Java Swing chạy được từ entry point chính.
- [ ] Có `run.cmd` hoạt động.
- [ ] Maven build thành công.
- [ ] Có đủ 6 level: 1, 2, 3, 4, 51, 52.
- [ ] Mỗi level có class riêng.
- [ ] Mỗi level có `.properties` nằm cạnh file Java.
- [ ] Các `.properties` được đóng gói đúng vào JAR.
- [ ] Có thể chọn level test qua biến trong Main.java.
- [ ] Có thể chơi liên tục trong No-Story Mode.
- [ ] Game dùng viewport 1000 × 700.
- [ ] Các level đường bộ có 3 làn rộng 240 px.
- [ ] Player render 60 × 90, hitbox 48 × 76.
- [ ] Player di chuyển ngang tự do, không có gia tốc bất thường.
- [ ] Có HP, damage, invulnerability và lethal collision.
- [ ] Có hệ thống hiệu ứng và logic riêng cho từng obstacle.
- [ ] Shark có đầy đủ các state và tấn công chí mạng.
- [ ] Có SafeSpawnPolicy dùng chung.
- [ ] Spawn theo config và độ khó tăng theo tiến độ.
- [ ] Không sinh tình huống bít đường theo các quy tắc an toàn đã thiết kế.
- [ ] Có progress 60 giây mặc định cho mỗi level.
- [ ] Va chạm thường làm tiến độ chậm đi một chút.
- [ ] Pause không làm gameplay hay progress tiếp tục.
- [ ] Nhấn Esc Pause/Resume được.
- [ ] Có thanh tiến độ và ô avatar di chuyển.
- [ ] Có hệ thống Energy cho Day 4.
- [ ] Có Fade Transition khi hoàn thành màn.
- [ ] Day5Road tự động chuyển sang Day5Helicopter khi chơi liên tục.
- [ ] Không có story/cutscene thật trong phiên bản này.
- [ ] Toàn bộ game chạy được khi không có sprite thật.
- [ ] Có đường dẫn để thêm sprite sau này.
- [ ] Restart reset đúng tất cả trạng thái.
- [ ] Có unit test cho các hệ thống chính.
- [ ] Các test được chạy và kết quả được ghi nhận trung thực.
- [ ] README.md được cập nhật.
- [ ] CONTEXT.md được cập nhật đầy đủ.
- [ ] Các nguyên tắc OOP được áp dụng rõ ràng, hợp lý.

---

# 25. Yêu cầu đối với báo cáo cuối của Codex

Sau khi hoàn thành, hãy báo cáo ngắn gọn nhưng đầy đủ:

**A. Những gì đã triển khai**

Liệt kê các chức năng và level đã hoàn thiện.

**B. Kiến trúc OOP**

Nêu các class/interface chính, vai trò và ví dụ áp dụng tính đóng gói, kế thừa, đa hình, trừu tượng và composition.

**C. File đã tạo hoặc chỉnh sửa**

Liệt kê theo nhóm chức năng, không cần sao chép toàn bộ mã nguồn vào báo cáo.

**D. Kiểm thử**

Nêu chính xác các lệnh đã chạy, số lượng test pass/fail và trạng thái build.

Phân biệt kiểm thử tự động với kiểm thử GUI thủ công.

**E. Các thông số có thể cân chỉnh**

Chỉ rõ `.properties` tương ứng với từng level và các nhóm key quan trọng.

**F. Vấn đề còn tồn đọng**

Ghi nhận mọi hạn chế hoặc phần chưa hoàn thiện, không che giấu.

**G. Hướng phát triển tiếp theo**

Tập trung vào việc:

- Thay hình khối bằng sprite.
- Thêm animation.
- Thêm âm thanh.
- Tích hợp Story.docx.
- Thêm dialogue và cutscene.
- Tinh chỉnh độ khó sau playtest.

---

# 26. Chỉ thị thực thi cuối cùng

Bây giờ hãy bắt đầu bằng cách kiểm tra repository thực tế.

Không chỉ trả lời bằng một kế hoạch triển khai.

Hãy trực tiếp sửa code và hoàn thiện game theo các giai đoạn trên.

Không cần hỏi lại những quyết định đã được mô tả rõ trong prompt.

Nếu có những chi tiết nhỏ chưa xác định, hãy chọn mặc định đơn giản, hợp lý, dễ chỉnh bằng config và ghi lại quyết định trong CONTEXT.md.

Nếu gặp giới hạn môi trường, thiếu dependency hoặc không thể chạy GUI, hãy chủ động tìm phương án kiểm thử phù hợp và ghi rõ giới hạn thực tế.

Không tự ý mở rộng thành một RPG, hệ thống inventory, skill tree hoặc cơ chế phức tạp không được yêu cầu.

**Mục tiêu cuối cùng: một game Java OOP 2D chơi được, đủ sáu level, cơ chế va chạm và spawn hợp lý, kiến trúc hướng đối tượng rõ ràng, sẵn sàng tích hợp hình ảnh và story, có test và tài liệu đầy đủ.**

Hãy bắt đầu triển khai.
