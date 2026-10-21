# Tiến độ triển khai theo prompt.md

## Menu đầu game, menu pause và ba ô lưu mốc bắt đầu (17/10/2026)

- Main gọi openMainMenu trước mở cửa sổ; GameState thêm MAIN_MENU. Vào game tạo lại
  màn ban đầu theo Main/CLI, không đè dữ liệu lưu. Game engine constructor giữ chế độ
  PLAYING cho các lối gọi trực tiếp hiện có; GUI thật luôn mở menu trước.
- MenuOverlay/MenuButton trong ui: nền gradient, card, nút hover/pressed/focus,
  menu chính ba nút, pause năm nút đúng yêu cầu, danh sách ba ô và xác nhận ghi đè.
  Thêm nút chơi lại/menu/thoát ở màn kết thúc. Thoát dispose cửa sổ để GameLoop dừng.
  GamePanel.refresh cập nhật overlay khi GameState đổi, không sửa component khi vẽ.
  GameRenderer chỉ vẽ thế giới/fade, overlay xử lý menu; Main dùng panel::refresh.
- Esc trên danh sách/ghi đè lùi một menu, pause Esc tiếp tục. InputHandler nhận callback
  pauseAction, giữ constructor cũ; R không restart khi đang trong menu pause/ô lưu.
  Gameplay và mọi timer dừng khi đang menu/pause; input xóa khi đổi trạng thái.
- SaveData/SaveRepository: tối đa3 tệp slot-1..3.properties UTF-8, version1,
  levelId/name, checkpoint LEVEL_START/DIALOGUE_START, dialogueId, autoAdvance, savedAt.
  Ghi tệp tạm cùng thư mục rồi atomic move (fallback replace nếu không hỗ trợ).
  Chỉ đọc khi mở menu/bấm tải, không IO mỗi frame; ô lỗi hiển thị và có thể ghi đè.
- Game.createSave chỉ cho PAUSED, hiện luôn LEVEL_START; loadSave từ MAIN_MENU tạo
  fresh BaseLevel, reset toàn bộ HP/energy/progress/effect/boss/obstacle/cooldown,
  khôi phục autoAdvance. LevelManager.select tạo session trước khi đổi index.
  DIALOGUE_START chừa cho tương lai nhưng hiện từ chối tải rõ ràng, không giả đã hỗ trợ.
- save.directory=saves thêm trong game.properties kèm ghi chú VN; /saves/ trong gitignore.
  run.cmd có pushd project nên thư mục lưu ổn định; IDE phải dùng cùng working directory.
  README mô tả UI, save workflow, checkpoint semantics và điểm nối hội thoại tương lai.
- Không chạy kiểm thử/build/game theo yêu cầu người dùng; chỉ rà mã trực tiếp.

## Đội hình đàn cá mập, tăng mạnh mật độ màn 2–4 và rải hồi máu (17/10/2026)

- SharkRushEvent chọn một trong ba kiểu với singleWeight/pairWeight/sequenceWeight=1:
  một làn ngẫu nhiên, hai làn khác nhau đồng thời, hoặc lần lượt làn0/1/2, delay.5s.
  waveCount5 đếm đội hình, interval2s, maxActive3, thời gian thử đợt16s. Giữ kiểu đã
  chọn qua retry, không đổi xác suất theo độ dễ đặt; một/hai làn thử các cặp khác nhau.
- Shark.wave đặt sẵn đích ở hàng player và lịch fin.7/dive.9/warn1.2/attack.4,
  mỗi con chuỗi123 thêm delay .5*i. Update/activeAt/predictedHitbox dùng cùng waveAge;
  toàn bộ đội hình được trySpawnGroup xét đường né trước khi giữ lại. Không bỏ cảnh
  báo, không để cá tự chọn lại làn giữa chuỗi; Shark thường giữ FSM/chọn đích cũ.
- Day2/3/4 spawn independent .45–.75s, initial.7, maxActive16, attempts8, cap3/làn,
  gap100px; giữ SafeSpawnPolicy, khoảng dư phản ứng/đường né. Xác suất mỗi lượt:
  day2 shark70/drain85/crate80; day3 icePatch45/snowball75/snowman85/iceHole80;
  day4 bus65/cactus85/tumbleweed75. Trọng số weighted vẫn có để chuyển mode khi cần.
- IceChoice6 đợt interval1.5 maxDuration12, hố85% lệch sau260px; snowman60% ở đường
  khô đối diện băng, còn lại giữa, giữ lựa chọn qua retry. Đường khô không còn luôn
  trống để đứng mãi; mọi nhóm vẫn cần đường né. Heat hotSpawnMultiplier .75 -> .9.
- HealthPickupSpawner thêm randomCount5, randomStartSeconds3/randomEndSeconds29 cho
  cả ba màn: mỗi khoảng một mốc ngẫu nhiên theo tiến độ, lane ngẫu nhiên, amount1,
  speed220/retry.35. Lịch tối đa5 lần sinh thành công, không sinh định kỳ thêm;
  chưa an toàn thì giữ mốc chờ thử lại, có thể thiếu nếu tới cuối màn vẫn chật.
  randomCount0 giữ lịch/nhịp cũ, màn52 và boss không đổi. Day4 energy vẫn hồi máu.
- Bật lại hồi máu map nước theo yêu cầu mới, xóa lịch6/18/30 cũ; bổ sung đầy đủ
  config/ghi chú VN cho day3/4, sprite.healthPickup mỗi day. Loader xác thực khóa
  tùy chọn và ba trọng số/khả năng chứa đội hình. README cập nhật phần ảnh và code.
- Không chạy kiểm thử/build theo yêu cầu người dùng; chỉ rà mã/cấu hình trực tiếp.

## Chỉnh tỉ lệ xe đạp màn 1 (16/10/2026)

- day1 bicycle.width/height đổi160x140 -> 80x150: hẹp hơn nhân vật100 px và
  ô tô180x210, thân dài theo hướng chạy. Ảnh và hitbox đều dùng kích thước mới;
  factory vẫn căn giữa làn, bộ dự báo dùng cùng kích thước nên không có vùng chạm cũ.
- Cập nhật ghi chú tiếng Việt cạnh thông số và kích thước ảnh trong README.
  Không chạy kiểm thử/build theo yêu cầu người dùng.

## Sửa đúng cây xương rồng trên đường và làn băng liên tục (16/10/2026)

- Xóa loại/class FallingCactus mới thêm, bỏ enum/factory/renderer/config/ảnh tham chiếu
  fallingCactus. Chuyển cơ chế sang chính Cactus trên đường, sprite.cactus hiện có.
  Cây đứng100x220, đứng1.2s -> cảnh báo1.3s -> xoay .3s quanh gốc -> nằm220x100;
  làn ngoài đổ vào trong, giữa chọn trái/phải; đứng/nằm gây sát thương, báo/đổ không.
  Khung cảnh báo đúng vùng nằm, vẽ foreground; giữ chỗ cả vùng đứng và vùng đổ từ đầu,
  kiểm tra SafeSpawnPolicy/LaneSpawnPolicy, không ép sinh. cactus.consumed=false.
- Day3 iceChoice: làn băng cố định bên phải (event.ice.lane=2), nền xanh/viền sáng/vết
  nứt cuộn theo đường do IceRoadRenderer; làn trái là đường khô. RoadSequenceEvent giữ
  tối thiểu roadSeconds12 trong RUNNING/DRAINING trước trả quyền sinh thường.
  Tối đa3 đợt Snowman giữa, mỗi đợt65% thêm IceHole bên băng, lệch sau520px; lựa chọn
  được giữ qua retry, cả nhóm phải đặt an toàn. Bỏ IcePatch khỏi đội hình, sàn băng
  thường ngoài sự kiện vẫn sinh như cũ. Kịch bản cần iceHole/snowman trong obstacles.
- Băng là địa hình, không collider giả: EffectManager nhận làn và thời gian còn lại;
  Player làm mới SLIDE khi ở trên băng, cả sàn băng chạm liên tục cũng làm mới SLIDE.
  ice.speedMultiplier/progressMultiplier1.2, rampPerSecond.1, maxSpeedMultiplier1.7;
  5 giây trượt liên tục đạt +70%, hết trượt mới đặt lại, tail1.5s khi rời băng.
  Gia tốc ngang vẫn1.1, lực hãm.3. HorizontalMotion.State mang thời gian trượt và
  mốc hết trượt cho cả Player và SafeSpawnPolicy; directRoute dùng cùng luật băng.
  Cuộn cảnh và tiến độ dùng hệ số tăng dần, dự báo cuộn cảnh cũng tích phân hệ số này.
- Properties có đủ nhóm/ghi chú tiếng Việt cho thông số mới; README sửa cả phần ảnh
  và code. Không chạy kiểm thử hoặc build theo yêu cầu người dùng.

## Thu nhỏ người màn 51, tăng nhẹ màn 1/3, xương rồng đổ màn 4 (phần cây đổ đã được thay thế bên trên) (16/10/2026)

- Day51 player 100x100 -> 84x90, hitbox74x78 offset5/6; y530 để chân vẫn620,
  maxX776 để mép phải vẫn860 và tâm khởi đầu vẫn500. Các tốc độ/HP giữ nguyên.
- Day1/3 spawn interval1.1..1.7 -> 1.0..1.55 (~10% dày hơn); day1 đoàn xe
  interval2.8 -> 2.6, day3 iceChoice waveCount2 -> 3. Giữ cap2/làn, gap180,
  báo trước/đường khô, trySpawnGroup và SafeSpawnPolicy, không ép đợt thiếu chỗ.
- Thêm ObstacleType.FALLING_CACTUS và class FallingCactus, bản thường Cactus vẫn có.
  Day4 thêm loại vào obstacles/glare.types, weight25, footprint360x64. Xuất phát
  từ một mép đường, cảnh báo1.3s + đổ0.3s, trôi theo ScrollingMovement.
  Chỉ activeAt sau khi nằm xong; occupiesAt giữ chỗ ngay trong cảnh báo, prediction
  dùng cùng movement/age. Va chạm thực theo thân/nhánh Area, dự báo giữ cả hình chữ nhật.
- Factory tính Y theo warning+fall và boost cuộn cảnh lớn nhất để có leadY160 trước
  hàng người chơi khi nằm xong; width tối đa470 giữ ít nhất một làn ngoài còn trống,
  đồng thời cả bố cục tương lai vẫn phải qua kiểm tra đường né hiện có.
- Renderer xoay từ đứng sang nằm quanh gốc và lật ảnh theo hướng, vùng cảnh báo mặt
  đất có ! nhấp nháy .12s/khung vàng alpha.22 vẽ foreground để không bị chói/player che.
  sprite.fallingCactus=/image/day4/fallingCactus.png, ảnh gốc trái/ngọn phải, thiếu dùng hình tạm.
- Properties có đủ khóa/ghi chú VN; loader xác thực riêng loại mới. README cập nhật
  thông số/art/cơ chế. Không chạy kiểm thử hoặc build theo yêu cầu user.

## Cá mập trừ 1 máu và làm chậm (16/10/2026)

- Day2 shark.collisionType đổi LETHAL -> SHARK_SLOW cho cả cá thường/cá cao trào.
  Loader bỏ ép SHARK thành LETHAL, xác thực kiểu mới và các thông số riêng.
- shark.damage=1; slowMultiplier=.65; progressMultiplier=.65; slowDurationSeconds=1.2.
  Có fallback cho khóa mới, ghi chú tiếng Việt trong nhóm shark tại properties.
- ConfiguredCollision áp dụng sát thương thường qua damage/hurt để giữ nhấp nháy,
  miễn sát thương và chỉ trừ một lần mỗi tiếp xúc; thêm timer SHARK_SLOW riêng.
  EffectManager kết hợp chậm bằng min, không nhân chồng vô hạn; BaseLevel đưa timer
  vào dự báo cuộn cảnh/tiến độ, HUD hiện Chậm do cá mập. Chuỗi vây/lặn/cảnh báo giữ nguyên.
- README cập nhật; không chạy kiểm thử hoặc build theo yêu cầu user.

## Thêm cao trào riêng cho màn 1–4, ưu tiên đường né (16/10/2026)

- Package game.logic.event: interface LevelEvent, factory LevelEvents; RoadSequenceEvent
  chia WARNING -> CLEAR_OLD -> RUNNING -> DRAINING -> DONE, trạng thái theo từng session.
  Thiếu event.type/none giữ lối chơi cũ. BaseLevel cập nhật event trước sinh thường,
  ObstacleSpawner ngừng sinh khi kịch bản nắm quyền và nhân mật độ theo thời tiết.
- Day1 convoy bắt đầu cảnh báo ở tiến độ24, tối đa3 hàng hai Vehicle, cùng tốc độ
  chọn trong min/max*1.05, lệch Y60, cách2.8s. Làn trống đầu lấy gần player tại lúc
  sinh thật; đợt sau chỉ đổi làn kề. Mỗi hàng trySpawnGroup nguyên khối/rollback.
- Day2 sharkRush ở14, tối đa4 cá, cách ít nhất2s, tối đa2 cá tồn tại. Dừng cống/thùng
  và cá thường trong đợt; cá vẫn có vây/lặn/cảnh báo đủ thời gian, mỗi lần chọn vị trí
  đớp vẫn qua SafeSpawnPolicy, không gọi trực tiếp cả3làn.
- Day3 iceChoice ở16, hai hàng cách3s: IcePatch ở một làn ngoài chọn ngẫu nhiên,
  Snowman giữa, bên ngoài đối diện chừa đường khô. Không chèn sinh thường vào đoạn;
  Safety kiểm tra nguyên nhóm và giữ LaneSpawnPolicy/gap180/count2 như trước.
- Ba kịch bản trên báo1.2s, chờ đường cũ thoáng tối đa7s rồi bỏ nếu không được.
  Thử lại vị trí sau.35s, thời gian sinh tối đa10s rồi ngừng thêm đợt. Cuối đoạn chờ
  đường thông tối đa7s trước khi sinh thường lại; vật còn lại vẫn thuộc mọi dự báo.
  Không xóa/tele vật để ép kịch bản, không kéo dài thời lượng40s hoặc khóa completion.
- Day4 heatCycle từ tiến độ6, báo1.2s rồi nắng5s/dịu3s, fade.6s, hotSpawnMultiplier=.75.
  GlareEffect nhận strength từ event cho cả alpha vật cản và lớp phủ đường.
  Tiếp tế thường chờ khoảng dịu, tiếp tế khẩn cấp bỏ qua điều kiện thời tiết.
- HUD có thông báo kịch bản rõ, đoạn băng ghi bên băng/bên khô. Thời gian hoạt động
  dùng dt gameplay nên pause/focuslost không tick, restart tạo mới mọi trạng thái.
- LevelEventConfig validate kiểu/tham số/mẫu cần dùng; mốc bắt đầu có thể sau thời
  lượng nếu user rút ngắn màn, không làm game lỗi. Khóa optional dùng fallback.
- Properties có nhóm riêng event.* và ghi chú tiếng Việt; README cập nhật cách chơi,
  chỉnh thông số/cấu trúc. Không chạy kiểm thử hoặc build theo yêu cầu user.

## Giảm tốc độ cỏ khô màn 4 (16/10/2026)

- tumbleweed.minSpeed=230, maxSpeed=290 px/giây, giảm từ 350..430.
  Giữ stopLeadY=260; factory tự tính vị trí dọc theo thời gian lăn để vẫn dừng
  trước người chơi. Không chạy kiểm thử/build theo yêu cầu user.

## Boss hồi 1 hit máu sau đòn 2 (16/10/2026)

- AirBoss.healHits giảm hitsTaken theo số nguyên, chặn hồi quá đầy và không hồi sinh.
- chooseAction chỉ gọi hồi khi hành động trước là LANE_ATTACK; tức sau khi hết
  đòn 2 và toàn bộ vật cản cũ, trước khi chuyển sang 1. transitionPrepared và việc
  đổi action bảo đảm chỉ hồi đúng một lần, không lặp trong các tick chờ.
- boss.lane.healHitsAfterAction=1 có ghi chú trong day5-2.properties; lượng hồi là
  số hit/boss.hitsToDefeat, không cố định số máu. Khóa optional mặc định 1 cho cấu
  hình cũ, 0 để tắt; validate nguyên và không vượt tổng hit. Giữ hitsToDefeat=17
  cùng các thông số người dùng đang chỉnh.
- Cập nhật README, không chạy kiểm thử hoặc build theo yêu cầu user.

## Màn 3 thêm hố băng, màn 4 thêm cỏ khô/chói mạnh/tiếp tế máu (16/10/2026)

- Day3 thêm ObstacleType.ICE_HOLE, class IceHole cuộn theo đường, SLOW dùng nhóm
  drain giống cống: mất 1 máu, di chuyển/tiến độ còn 65% trong 1.2s; hình hố nứt
  và tối hơn khi đã sụp. sprite.iceHole=/image/day3/iceHole.png hoặc hình tạm.
- Sàn băng: ice.speedMultiplier=1.2, progressMultiplier=1.2, accelerationMultiplier=1.1,
  durationSeconds=1.5. HorizontalMotion dùng gia tốc mới; EffectManager và
  BaseLevel.travelSecondsAhead áp dụng boost cho cả cuộn cảnh/tiến độ và dự báo.
  Giữ lực hãm trượt hiện tại 0.3. Hai khóa mới có mặc định 1 cho cấu hình cũ.
- Day4 thêm ObstacleType.TUMBLEWEED, class Tumbleweed, LaneStopMovement có cùng
  phương trình cho update và predictedHitbox. Lăn ngang 350..430px/s từ bên gần
  làn đích rồi ngừng X/xoay; tiếp tục cuộn Y. Đường kính 120, collisionShape tròn,
  NORMAL_DAMAGE 1, không consumed. stopLeadY=260 giữ điểm dừng trước người chơi,
  kể cả khi có boost năng lượng. spawn.tumbleweed.weight=35.
- Obstacle.laneReservation cho phép giữ chỗ tại làn cỏ sẽ tới; LaneSpawnPolicy tính
  điểm dừng, SafeSpawnPolicy vẫn kiểm tra toàn bộ đường lăn/đứng lại còn đường né.
- Day4 glare minAlpha=.15, clearY=420, revealExponent=2; lớp phủ đường màu #fff0c0
  alpha=.38 mờ dần tới clearY. Hiệu ứng áp dụng bus/cactus/tumbleweed; HUD, cảnh báo,
  người chơi được vẽ sau. Thêm hình cỏ tự xoay và hố băng khi chưa có ảnh.
- Energy drain tăng 2 -> 4 điểm/s, pickupHealAmount=1 mỗi thanh, vẫn hồi 30 năng lượng
  và tăng tốc 40% trong 2s. Khóa hồi máu mặc định 0 cho cấu hình cũ.
- Properties mới có ghi chú UTF-8 và nhóm riêng, cập nhật README cho người làm ảnh/code.
  Không chạy kiểm thử hoặc build theo yêu cầu user.

## Quệt cạnh xe trừ 1 máu, thùng gỗ 1 máu và tắt hồi máu màn nước (16/10/2026)

- Vehicle (gồm Bicycle/Bus) lưu hitbox trước tick, Player lưu hitbox trước di chuyển.
  contactEffect dùng thời điểm đi qua biên theo chuyển động tương đối để phân biệt
  đầu xe hướng xuống và cạnh/phía sau; mặt trước giữ LETHAL, còn lại NORMAL_DAMAGE.
  Nếu bắt đầu chồng hitbox thì dùng mặt có độ xuyên nhỏ nhất. Không sửa hitbox hình ảnh.
- CollisionManager lưu hiệu ứng mỗi lần tiếp xúc, phân loại trước khi ưu tiên chí mạng;
  quệt cạnh không bị tính lại thành đâm đầu ở tick sau. Một va chạm trừ 1 máu có
  nhấp nháy/miễn sát thương/ảnh hưởng tiến độ theo luồng sát thương thường hiện có.
- Day1/4 collision.normalDamage vẫn 1, thêm ghi chú rõ các mặt xe. Xe tắc đường
  màn 51 giữ NORMAL_DAMAGE như trước; máy bay không áp dụng quy tắc xe đường bộ.
- Day2 floatingCrate.collisionType=NORMAL_DAMAGE, collision.normalDamage=1;
  healthPickup.enabled=false, giữ các khóa lịch để có thể bật lại nhưng không sinh.
- Cập nhật README; không chạy kiểm thử hoặc build theo yêu cầu user.

## Màn 1–4 còn 40 giây, hồi máu màn 2 và giảm dồn làn (16/10/2026)

- level.durationSeconds=40 cho day1..day4; giữ thời lượng ngày 5.
- Day2 bật healthPickup, amount=1, lịch spawnTimesSeconds=6,18,30 theo tiến độ
  hiệu dụng. HealthPickupSpawner chỉ nhận đúng số lượt trong lịch; thử lại mốc
  bị chặn và không trôi lịch sau khi thử lại. Vẫn kiểm tra đường an toàn/nhặt được.
  Tốc độ 140, kích thước 40x40, ảnh /image/day2/health.png hoặc hình tạm hiện có.
- LaneSpawnPolicy được gọi trước kiểm tra spawn an toàn; day1..day4 bật tối đa
  2 vật cản/làn, khoảng hở dọc 180px, không tính vật phẩm/vật đã qua người chơi.
  Tính mọi làn có hitbox chồng ngang, áp dụng cả lúc cá mập chọn lại vị trí đớp.
  Hai khóa optional mặc định 0 để ngày 5 và cấu hình cũ không thay đổi hành vi.
- Loader xác thực khóa mới/lịch tăng dần trong thời lượng màn; giữ ghi chú tiếng Việt.
  Cập nhật README và hằng thời lượng trong test hiện có. Không chạy kiểm thử/build
  theo yêu cầu user tự chơi kiểm tra.

## Phần trước boss còn 15 giây (16/10/2026)

- Đặt level.durationSeconds=15 trong day5-2.properties, cập nhật README và giá trị
  thời lượng mặc định trong test hiện có. Không chạy kiểm thử theo yêu cầu user.

## Đặt lại đòn 2 thành 10 giây (16/10/2026)

- User yêu cầu chỉ đổi boss.lane.durationSeconds về 10 thay vì bỏ validator.
  Giữ boss.waves.durationSeconds=8 và quy tắc đòn 2 phải dài hơn đòn 1.
- Đã hoàn tác thay đổi validator/test ở phương án trước; sửa giá trị và ghi chú
  properties. Theo yêu cầu user, không chạy kiểm thử; chỉ build bỏ qua test.

## Sửa lỗi thiếu boss.startAction khi mở game (16/10/2026)

- Kiểm tra thấy code mới nhưng cả properties nguồn và target/classes không còn
  ba khóa mới startAction/planeShotIntervalSeconds/planeShotRetrySeconds. Bổ sung
  các khóa có ghi chú UTF-8; đặt lại cooldown E=0.8 và giới hạn chuỗi=1 đúng yêu cầu.
- AirBossConfig.withDefaults tạo bản sao và thêm riêng ba khóa mới cho boss.enabled=true
  khi thiếu; các giá trị đã khai báo (kể cả sai/rỗng) không bị thay bằng mặc định.
  DayConfigLoader dùng cấu hình đã bổ sung cho cả validate lẫn runtime, tránh lỗi
  xuất hiện tiếp lúc boss bắt đầu hoặc máy bay bắn. Không sửa đối tượng Properties đầu vào.
- Thêm regression cho properties cũ thiếu cả ba khóa, vào được ACTIVE đòn 2;
  kiểm tra giá trị tùy chỉnh được giữ, giá trị rỗng vẫn báo lỗi. Maven package:
  158 tests PASS; JAR --smoke cả 6 màn PASS, config đóng gói trùng nguồn.

## Nhịp boss 2 -> 1 -> 3 và hồi E 0,8 giây (16/10/2026)

- Boss bắt đầu bằng boss.startAction=2, kể cả restart; lựa chọn mở đầu độc lập
  boss.action1Chance. maxConsecutiveWaveSequences=1 nên chỉ có một chuỗi 1 -> 3
  trước khi quay về 2; vẫn không lặp 2 -> 2. Có thể chỉnh startAction=1 khi cần
  thử riêng đòn 1; các fixture kiểm tra đòn 1 dùng khóa này rõ ràng.
- Máy bay đòn 2 bắn lại sau boss.lane.planeShotIntervalSeconds=0.65 giây;
  nếu lần thử bị giới hạn số vật/lối né chặn, thử lại sau planeShotRetrySeconds=0.15
  (cùng tiền tố boss.lane.). Trước đây mọi lần thử đều chờ 1.5 giây, nhiều máy bay
  rời vùng bắn trước khi thử lại. Không thêm bước bốc xác suất vì trước đây đã
  luôn thử bắn khi đủ điều kiện; cảnh báo và kiểm tra lối né vẫn áp dụng.
- boss.playerRocket.cooldownSeconds=0.8 theo yêu cầu mới, các thông số khác giữ nguyên.
- README và ghi chú UTF-8 trong properties đã cập nhật. Maven package: 156 tests
  PASS; JAR --smoke: cả 6 màn PASS. Kiểm tra JAR chứa đúng config/bytecode mới.

## Rà soát quy tắc không lặp đòn2 (16/10/2026)

- Kiểm tra mọi đường gán action: chooseAction là chỗ duy nhất chọn LANE_ATTACK,
  nhánh afterLaneAttack ép PLANE_WAVES trước xác suất/giới hạn. beginReverseAction
  chỉ từ PLANE_WAVES. Bộ đếm chuỗi reset khi vào LANE_ATTACK nên giới hạn2chuỗi
  không xung đột với việc bắt buộc2->1->3.
- Thêm kiểm tra drain thực với3viên bắn lúc né + blocker, action1Chance0 vẫn sang1
  sau khi hết vật; kiểm tra restart sau handoff2->1 tạo mới encounter và chọn2
  ban đầu theo chance0, không mang lịch sử trận trước.
- Phát hiện test cooldown cũ khóa default0.75 trong khi source hiện0.95. Giữ0.95
  của properties; test kiểm tra thời gian thực cấu hình cùng0.75/1.2 riêng, không
  ép giá trị mặc định khiến test fail khi người dùng chỉnh thuộc tính này.
- Sửa ghi chú action1Chance để nói rõ sau2luôn1. Không thay logic gameplay lần này.
- Maven package153 tests PASS; chạy trực tiếp JAR --smoke cả6màn PASS. Đối chiếu
  bytecode boss trong JAR trùng target/classes đã test, properties trùng nguồn/BOM.

## Không lặp hai đòn2 liên tiếp (16/10/2026)

- chooseAction nhận biết hành động trước là LANE_ATTACK và bắt buộc chọn
  PLANE_WAVES, kể cả action1Chance=0. Sau1 vẫn nối3; sau3 chọn1/2 theo xác suất
  và giới hạn tối đa2chuỗi1->3 như trước. Lúc vào boss lần đầu vẫn dùng xác suất.
- Không thay chuyển động mượt, khoảng nghỉ0.1s, bắn lúc né hay các thông số user.
  README và ghi chú action1Chance mô tả rõ ngoại lệ sau đòn2.
- Thêm kiểm thử actual loop với action1Chance0:2->1->3->2->1->3->2->1->3;
  kiểm thử giới hạn chuỗi cũ vẫn đạt. Maven package151 tests PASS.

## Ba kiểu đòn3 ngang xác suất và bắn lúc né chờ sau đòn2 (16/10/2026)

- Thay reverse.twoPlaneChance bằng singleWeight/pairWeight/sequenceWeight đều33,
  chuẩn hóa tổng99 nên mỗi kiểu đúng1/3, không có1% đợt rỗng. Chọn một lần và giữ
  pendingReversePlaneCount1/2/3 khi bị chặn; thứ tự1->2->3 không xáo trộn.
- Kiểu tuần tự tạo cả3 EnemyPlane reverse cùng đội hình, warningStart0/0.5/1s,
  cùng được SafeSpawnPolicy kiểm tra trước khi giữ. Thời gian bay lệch đúng0.5s,
  không phụ thuộc retry giữa từng chiếc. sequenceDelaySeconds0.5 có ghi chú.
- EnemyPlane thêm warningStartSeconds, awaitingWarning, warningElapsedSeconds;
  trước lượt thì ẩn, vào lượt mới cảnh báo đủ1.25s rồi bay0.5s. atSpawnX giữ lịch.
  Renderer blink theo thời gian cảnh báo riêng, không theo age toàn phần.
  Đòn3 không kết thúc khi còn máy bay chờ lượt/cảnh báo/bay, có thể vượt6s.
- Trong recovery LANE_ATTACK, tiếp tục né và bắn aimed tối đa3 viên, nhịp0.8s,
  retry0.15 khi chưa an toàn. Các khóa lane.recoveryShotLimit/IntervalSeconds/
  RetrySeconds có ghi chú; 0limit để tắt. Giới hạn tránh đạn tự kéo dài recovery
  vô hạn. Khi hazards hết thì chuyển ngay, không cố bắn đủ các viên còn lại.
- Test đủ3 vùng chọn bằng trọng số33; chuỗi bay đúng0.5s và vượt nominal1.75s;
  cap2 không nhận dở đội hình3 chiếc. Kiểm tra clone/lịch cảnh báo/dự báo;
  ảnh boss-reverse-sequence-1/2/3 cho thấy cảnh báo lần lượt, blink độc lập.
- Test bắn đủ3 viên lúc né, nhịp>=0.8s, sau đó dừng/đợi blocker, hết blocker
  vào lượt mới; fixture độc lập tắt recovery shots và sequence để giữ phạm vi.
- Maven package150 tests PASS; đã xem3ảnh sequence. Smoke cả6màn PASS, JAR
  properties trùng nguồn/UTF-8 BOM và git diff --check sạch.

## Boss tiếp tục né khi chờ kết thúc đòn2 (16/10/2026)

- Nguyên nhân đứng làm bia: recovering trả về ngay khi hasHazards, nên không đổi
  làn trong thời gian đợi chim/máy bay/đạn cũ rời màn (có thể nhiều giây).
- Khi recovering của LANE_ATTACK còn hazards, nếu boss không đang di chuyển
  thì chọn một làn khác và moveToLane ngay. Chuyển động dùng boss.moveSeconds,
  nối liên tục không thêm thời gian đứng; không sinh đòn mới khi đang đợi.
- Vẫn đợi đường thông để không chồng các đợt ép người chơi, sau đó dùng luồng
  chuyển mượt/nghỉ0.1s/chọn đòn hiện có. Không thay bất tử hay cơ chế bắn của player.
- Giữ thông số user vừa chỉnh: lane.changeIntervalSeconds1, doubleThrowChance40,
  planeChance80, birdChance50 và toàn bộ giá trị khác; chỉ bổ sung ghi chú duration.
- Thêm test blocker giữ đòn2 trong nhiều giây: boss đổi X đều, ở trong đường,
  moving liên tục, không spawn thêm; pause đóng băng và hết blocker thì vào lượt
  tấn công tiếp. Maven package144 tests PASS; smoke cả6màn PASS, JAR properties
  trùng nguồn/UTF-8 BOM và git diff --check sạch.

## Tăng nhịp đòn2 và 75% đạn chéo đòn3 (16/10/2026)

- Đòn2: changeIntervalSeconds2.4->1.25 (thời gian đứng), attackIntervalSeconds1->0.55,
  planeChance55->60, birdChance45->60. Giữ di chuyển mượt0.55s/nghỉ0.1s và kiểm tra
  spawn/đường né/giới hạn8 vật thể. Fixture so sánh10s đo được nhiều lần bắn,
  đổi làn và gọi máy bay hơn cấu hình cũ, loại bỏ vật sau mỗi lần để đo nhịp riêng.
- Mỗi đợt reverse wave được nhận, xét boss.reverse.shotChance75 đúng một lần.
  Nếu chọn bắn, thêm một BossProjectile.aimed từ tay boss tới ô/làn ngẫu nhiên
  cùng hàng player; có tia mờ/nhấp nháy rồi đạn chéo thật. Không đổi chuỗi1->3.
- Thêm reverse.shotWarningSeconds0.65, shotSpeed800, shotAfterTargetSeconds0.3,
  shotRetrySeconds0.15. Tất cả có ghi chú tiếng Việt; tay/kiểu tia dùng chung waves.*.
- reverseShotPending giữ quyết định khi cap/đường né đang chặn, retry không roll lại;
  không gọi đợt máy bay sau cho tới khi đặt được viên đã chọn. Không bỏ viên cuối
  lúc hết duration6s; có thể kéo dài để đủ đợt/đạn an toàn. Reset pending/cooldown
  khi bắt đầu reverse action; restart tạo encounter mới.
- Dùng spawnAimedStrike chung với đòn1 để collision, nguồn tay, khóa đích, tốc độ
  và hình cảnh báo nhất quán. Người chơi vẫn không phá đường đạn boss bằng rocket.
- Test ranh giới74.9%/75% với cả3đợt luôn2máy bay, đúng3 hoặc0viên; dự báo đạn
  khớp tốc độ650 và warning0.9 do test chỉnh riêng, cap<=8. Test cap1 chặn đạn,
  đổi roll sang99% sau lần chọn nhưng vẫn bắn khi máy bay rời. Các fixture cũ
  tắt reverse.shotChance để cô lập kiểm tra máy bay/luồng đòn; production vẫn75.
- Maven package143 tests PASS; smoke cả6màn PASS, JAR properties trùng nguồn,
  UTF-8 BOM/ghi chú tiếng Việt và git diff --check đều đạt.

## Boss chuyển mượt, nghỉ0.1s và máy bay trừ2máu (16/10/2026)

- Bỏ giới hạn moveSeconds theo actionGapSeconds. Mọi chuyển vị trí của boss dùng
  moveSeconds dương, không teleport. AirBoss từ chối thời gian<=0 và không tạo
  chuyển động chờ nếu đã ở đúng đích (như từ đòn1 sang3 ngoài lề).
- Theo chỉnh sửa mới nhất, boss.actionGapSeconds=0.1; bắt đầu đổi vị trí khi hazards
  cũ hết, khoảng nghỉ chạy cùng lúc chuyển mượt. Đủ nghỉ và di chuyển xong thì
  ra đòn ngay, không cộng openingDelay. Giữ chuỗi1->3/giới hạn2chuỗi/hồiE0.75s.
- Thêm CollisionKind.PLANE_DAMAGE và enemyPlane.damage=2 trong properties, có
  ghi chú tiếng Việt; loader kiểm tra số nguyên dương và loại va chạm đúng.
  Máy bay thường, unarmed và reverse cùng dùng spec này, không chết ngay khi chạm.
- PLANE_DAMAGE đi qua Player.hurt: blink, miễn sát thương, HIT/progress penalty
  như sát thương thường. Xét plane trước sát thương nhẹ cùng khung hình để không
  giảm thành1HP do thứ tự danh sách; LETHAL của xe/cá vẫn ưu tiên cao nhất.
- Test chuyển boss qua vị trí trung gian ở cả hai chiều, chờ đi xong/không chờ
  thêm mở đầu, đòn1->3 cùng vị trí chỉ nghỉ0.1s. Kiểm tra đa giác cánh/mũi trừ2HP,
  ba loại máy bay dùng damage cấu hình, khiên/chạm lại sau khiên, đứng cùng vùng
  chạm không mất máu mỗi frame,2HP thì chết và thứ tự sát thương trong khung hình.
- Maven package140 tests PASS; smoke cả6màn PASS, JAR properties trùng nguồn,
  UTF-8 BOM/ghi chú được giữ.

## Đổi chuỗi thành 1->3, bỏ nghỉ thêm, tia ngắm và đạn từ tay boss (16/10/2026)

- Chỉ PLANE_WAVES nối bắt buộc REVERSE_PLANES; LANE_ATTACK và REVERSE_PLANES xong
  mới chọn1/2. Đổi maxConsecutiveLaneSequences thành maxConsecutiveWaveSequences=2,
  sau1->3->1->3 ép2 rồi đặt lại bộ đếm. Giữ reverse.twoPlaneChance50/số3đợt.
- boss.actionGapSeconds=0: hết thời lượng thì dừng spawn/chờ hazards cũ; khi hết
  hazards, chuyển và sinh cảnh báo/vật đầu của đòn mới trong cùng update, không
  cộng moveSeconds/openingDelay. AirBoss.moveTo(...,0) đặt vị trí ngay, tránh boss
  đứng nguyên chỗ cũ khi thời gian di chuyển0. Vẫn hỗ trợ cấu hình gap>0.
- Đòn1 BossProjectile.aimed khóa nguồn tay và đích lúc cảnh báo, dùng chuyển động
  DelayedLinearMovement với vx/vy tính theo nguồn->đích. Hết cảnh báo mới có thân đạn
  nguy hiểm/chiếm chỗ, bay qua đích rồi sống thêm throwActiveSeconds. targetArea giữ
  X/Y đích bất kể vị trí đạn hoặc player di chuyển. Player rockets vẫn xuyên đạn boss.
- Thêm waves.throwSpeed800, handHeightRatio0.6, handInsetX8, aimLineAlpha0.22,
  aimLineWidth2, aimLineColor#ffcf33; tất cả có ghi chú tiếng Việt. Cảnh báo đòn1
  vẽ sau player, tia mờ luôn nối tay->đích trong thời gian cảnh báo, dấu! nhấp nháy.
- Hai viên cùng loạt chia sẻ volley token để được xuất phát cùng tay; đường né
  vẫn kiểm tra cả hai. Nhịp đạn thường xét trước máy bay để máy bay không luôn
  chiếm vị trí bắn; hai đồng hồ độc lập. Loạt mở đầu chờ cả chuyến bay cuối kết thúc.
- Tên lửa E hồi0.75s. Giữ các thông số khác người dùng tự chỉnh, bao gồm hitsToDefeat.
- Test chuỗi1->3/giới hạn lặp/0-gap khi có và hết blockers, đủ3đợt/50% số máy bay;
  đạn từ cả hai bên khớp dự báo, khóa đích, cảnh báo không gây damage, đạn thật trừ1HP;
  cooldown chính xác0.75s. Kiểm tra alpha tia và xem boss-aim-warning/flight PNG.
- Maven package137 tests PASS; smoke cả6màn PASS. JAR properties trùng nguồn,
  UTF-8 BOM và ghi chú tiếng Việt được giữ; git diff --check sạch.

## Đòn 3 xác suất hai máy bay, cảnh báo đòn 2 và giới hạn chuỗi (16/10/2026)

- Thay boss.reverse.planesPerWave bằng boss.reverse.twoPlaneChance=50: mỗi đợt
  chọn một/hai máy bay, hai chiếc khác làn. pendingReversePlaneCount giữ nguyên
  lựa chọn trong lúc thử lại, chỉ tăng reverseWavesSpawned khi nhận đủ nhóm.
  Vẫn3 đợt, cảnh báo1.25s/bay0.5s/nhịp2s và SafeSpawnPolicy kiểm tra đường né.
- boss.maxConsecutiveLaneSequences=2; chọn2 tăng bộ đếm, chọn1 đặt lại0.
  Sau2->3->2->3 ép1 trước khi lại chọn2, kể cả action1Chance=0. Bộ đếm chỉ
  cập nhật một lần lúc chuẩn bị đòn, không tăng lại trong khoảng nghỉ1s.
- EntityRenderer.foregroundWarnings chạy sau player: vật ném dọc làn và tên lửa
  địch trong boss phase có tam giác chấm than72px, cách thân18px, màu#ffcf33.
  Dấu đích luôn hiện qua các pha blink, tam giác giữ X của đích, không đuổi player.
  Các khóa boss.lane.warningSize/warningOffsetY/warningColor có ghi chú tiếng Việt;
  dùng sprite.bossProjectileWarning nếu có ảnh, fallback tam giác. Đòn1 giữ cảnh báo cũ.
- Thử nhóm một/hai máy bay đủ3đợt,120 seed có cả hai kiểu theo xác suất50%,
  pair bị cap chặn không quay lại xác suất. Quan sát hai chuỗi liên tiếp rồi ép1
  và đặt lại bộ đếm. Kiểm tra ảnh/điểm ảnh chứng minh tam giác trên thân, đích vẽ
  sau nhân vật, blink không làm mất viền đích; áp dụng cả vật ném và tên lửa.
- Maven package133 tests PASS; smoke cả6màn PASS; JAR properties trùng nguồn và UTF-8 BOM.
  Đã xem boss-lane-warning.png/boss-missile-warning.png.

## Loạt mở đầu đòn 1 và khoảng nghỉ đúng 1 giây (16/10/2026)

- Mỗi lần chọn đòn 1, bắn một phát lần lượt trái -> giữa -> phải, không xét
  xác suất bắn/đạn đôi. Chưa đặt được an toàn thì giữ nguyên làn, thử lại theo
  throwRetrySeconds; đợi phát cuối biến mất mới gọi máy bay và bắn thường.
- Thêm boss.waves.openingVolleyEnabled=true, openingVolleyIntervalSeconds=0.4
  (khoảng cách tối thiểu), openingVolleyWarningSeconds=0.8; ghi chú tiếng Việt.
  Cảnh báo 0.65s thường không đủ để né vật rộng50px từ giữa làn khi tính phản ứng
  0.35s, nên loạt mở đầu có cảnh báo riêng. Không bỏ kiểm tra đường né.
- boss.actionGapSeconds=1. Chờ vật cản cũ hết, chọn đòn/đổi vị trí trong 1s nghỉ;
  giới hạn thời gian di chuyển theo khoảng nghỉ. Khi hết 1s ra đòn luôn,
  không cộng openingDelaySeconds. Opening delay chỉ dùng lần đầu sau nạp máu.
  Giữ chuỗi bắt buộc đòn2 -> đòn3 rồi mới nghỉ/chọn lại đòn1 hoặc2.
- Kiểm thử loạt mở đầu lặp lại đủ3làn, chưa hết loạt không sinh máy bay;
  đo chuyển1->2 và2->3->1 chính xác1s với moveSeconds=1.5/openingDelay=250,
  đồng thời kiểm tra vật cản cũ trì hoãn bắt đầu đếm nghỉ. Kiểm tra cấu hình mới.
- Maven package130 tests passed; smoke cả6màn PASS, JAR properties trùng nguồn,
  UTF-8 BOM/ghi chú cạnh các thông số được giữ. Các fixture kiểm thử cơ chế
  riêng tắt openingVolleyEnabled để đo đúng nhịp thường.

## Máy bay chịu hai phát, tăng đòn 2 và đòn 3 bay ngược nối sau đòn 2 (16/10/2026)

- Obstacle.hitByPlayerRocket mặc định phá vật; EnemyPlane giữ bộ đếm riêng với
  enemyPlane.hitsToDestroy=2. Phát đầu chặn tên lửa, lóe sáng0.12s và vẫn nguy hiểm;
  phát thứ hai mới nổ/biến mất. Không gây damage xuyên tới boss ở phía sau.
  atSpawnX giữ trạng thái trúng đạn/vũ khí/đội hình; tất cả máy bay dùng cùng độ bền.
- Tên lửa người chơi hồi1s. Đòn2 nhịp xét1s, planeChance55 và birdChance45,
  tăng từ40/30. Giữ giới hạn8 vật thể và kiểm tra từng lượt sinh/đường né.
- Hết đòn2 chuyển bắt buộc sang Action.REVERSE_PLANES. Boss ra mép đường rồi
  gọi3 đợt máy bay bay từ dưới lên trong thời gian danh định6s; mỗi đợt một chiếc,
  bắt đầu cảnh báo cách2s. Mỗi đợt cảnh báo1.25s, sau đó vút qua toàn màn trong0.5s.
  Nếu chưa đặt an toàn được thì thử lại, giữ đủ3 đợt và có thể dài hơn6s.
- EnemyPlane.reverse dùng DelayedLinearMovement với vận tốc âm tính từ chiều cao
  màn/thân/margin. Chưa bay không có va chạm; kết thúc0.5s toàn thân đã ra biên trên
  và bị xóa. Mũi sprite và collisionShape quay180độ, máy bay này không bắn tên lửa.
- Vẽ ô nhấp nháy có mũi tên lên/dấu! tạiY625, tô nhẹ làn sắp bay; sprite tùy chọn
  reverse-plane-warning.png. Các thông số thuộc nhóm BOSS09, boss.reverse.*.
- Sau đòn1 hoặc chuỗi2->3, đợi vật cản hết và nghỉ rồi chọn lại đòn1/2.
- Thêm thử thực tế hai phát chặn boss, chuyển2->3/đúng3đợt, cảnh báo/đường bay
  dự báo khớp, quay hình và hướng lên. Maven package128 tests passed,0 errors/failures.
  Đã xem ảnh boss-reverse-warning/flight. Properties248 khóa, UTF-8 BOM, có ghi chú.

## Tránh máy bay bám đuôi bằng lệch X lúc sinh (16/10/2026)

- PlaneSpawnPlacement tính cả nhóm trước khi SafeSpawnPolicy kiểm tra đường né.
  Nếu máy bay thẳng hàng X trong8px và khoảng trống dọc không quá500px thì vị trí
  sinh lệch24px. Cả ba thông số enemyPlane.tailAlignmentPixels/tailGapPixels/
  tailOffsetX nằm trong day5-2.properties, có ghi chú tiếng Việt.
- Máy bay rộng240px nên khi lệch có thể qua ranh giới làn; toàn thân vẫn trong
  vùngX140–860. Thử tâm cũ, lệch phải/trái; tính lại cả đội hình để hai chiếc cùng
  đợt không chồng thân. Không có vị trí phù hợp thì từ chối/chờ, không cưỡng ép sinh.
- Chỉ tạo lại máy bay chưa sinh bằng EnemyPlane.atSpawnX, giữ tốc độ/vũ khí/đội hình;
  không dời máy bay đang bay. Vị trí mới dùng chung cho hình, hitbox và dự báo.
- BaseLevel.trySpawnGroup áp dụng placement; ObstacleSpawner thường cũng đi qua
  lối này nên cả máy bay thường, đòn1 và máy bay có tên lửa đòn2 đều được xử lý.
- Thêm4 test về lệch X/dự báo, ba tổ hợp cặp làn không chồng/không tràn lề,
  cấu hình độ lệch36px và bỏ qua máy bay xa/đã xóa, hoàn tác khi hết vị trí.
- Maven package:124 tests passed,0 failures/errors. Source properties235 khóa.
  Giữ thông số người dùng mới chỉnh: speedMultiplier1.6 và throwActiveSeconds0.3.

## Đòn 1: bắn đạn nhanh riêng, máy bay thưa hơn và 1/2 chiếc ngang xác suất (16/10/2026)

- Theo đính chính của người dùng, tăng nhịp bắn đạn của boss, không tăng mật độ
  máy bay. Tách throwCooldown khỏi attackCooldown đang dùng cho đợt máy bay.
- boss.waves.throwIntervalSeconds=0.7 và throwRetrySeconds=0.15: xét bắn theo nhịp
  riêng, vẫn xét khi chưa sinh được máy bay. Giữ throwChance90, doubleThrowChance20.
  Chọn bắn/đơn/đôi rồi giữ lựa chọn khi chưa đặt an toàn được, không quay lại mỗi retry.
- Máy bay giãn nhịp tối thiểu lên1.4s, gapPixels150. twoPlaneChance50: mỗi đợt
  chọn một hoặc hai chiếc ngang xác suất; giữ số lượng đã chọn trong lúc thử lại,
  không đổi đợt hai thành một nếu chưa đặt được. Hai chiếc vẫn khác làn, lệchY40,
  không bắn tên lửa. Chung tốc độ mỗi lượt đòn1, tính khoảng cách bằng chiều dài
  thực của đợt vừa sinh (một chiếc không cộng độ lệchY của chiếc thứ hai).
- Thêm thử400 seed cho số lượng1/2, thử khi giới hạn chỉ chứa được một vật: đợt
  hai máy bay vẫn chờ nhưng boss tiếp tục bắn đạn. Test cặp/spacing/render đặt rõ
  twoPlaneChance100 khi cần kiểm tra riêng đội hình hai chiếc.
- Maven package:120 tests passed,0 failures/errors. Source properties232 khóa,
  mọi tham số mới có ghi chú tiếng Việt; README cập nhật hai nhịp riêng.

## Tên lửa xuyên đạn boss và tăng nhịp đòn 1 (16/10/2026)

- Thêm Obstacle.blocksPlayerRocket(); BossProjectile và Missile trả false. Tên lửa
  người chơi xuyên cả vật ném boss lẫn tên lửa địch, không phá hoặc làm nổ chúng;
  máy bay/vật cản khác vẫn chặn tên lửa và bị phá. Đạn địch vẫn gây hại cho người chơi.
- Theo xác nhận của người dùng,75px là khoảng trống dọc giữa hai đợt liên tiếp,
  không phải độ lệch hai chiếc trong cùng đợt. boss.waves.gapPixels=75; giữ staggerY40.
  Mỗi lượt đòn1 chọn một tốc độ chung, tránh đợt sau nhanh hơn đuổi kịp đợt trước.
  Nhịp gọi = max(intervalSeconds,(height+staggerY+gapPixels)/speed). intervalSeconds
  giảm từ1 xuống0.65, giờ là mức chờ tối thiểu. Nếu đường chưa an toàn thì vẫn đợi thêm.
- boss.waves.doubleThrowChance=20: sau khi đã chọn ném,20% ném hai vật có cảnh báo
  vào hai làn khác nhau ngang hàng người chơi. Kiểm tra/hoàn tác cả nhóm nếu không an toàn.
- Giữ thông số người dùng đã sửa: hitsToDefeat12, cooldown3s, speedMultiplier1.3,
  throwChance90, lane.duration10s và lane.doubleThrowChance30. Các test hệ thống riêng
  khai báo rõ fixture8 hit/cooldown1.2 thay vì phụ thuộc các giá trị đang được cân chỉnh.
- Thêm kiểm tra đạn xuyên nhau nhưng vẫn trúng boss, khoảng cách mép75/150px theo
  tham số khi có đường trống, một/hai vật ném và cấu hình không hợp lệ.
- Maven package:118 tests passed,0 failures/errors. Source properties229 khóa, có
  ghi chú tiếng Việt cho hai tham số mới; README cập nhật hành vi/cách chỉnh.

## Màn 52: phần né 30 giây, boss hai đòn, tên lửa E và khiên hiếm (16/10/2026)

- Phần thường chỉ còn 30 giây tiến hiệu dụng, difficulty.mode=fixed với mật độ
  1.35 từ đầu, bỏ cấu hình ba giai đoạn ở màn 52. Hết phần này không hoàn thành
  level: ngừng sinh vật cản/tên lửa mới, chờ vật cản cũ hết rồi chờ thêm 1.5s.
- AirBossEncounter điều phối WARNING -> ENTERING -> LOADING -> ACTIVE -> EXITING
  -> COMPLETE. Boss tạm 100x150, có ảnh đại diện vuông ở mép thanh máu, chuyển mượt.
- Đòn 1 đứng ngoài đường, gọi cặp máy bay không bắn ở hai làn, nhanh x1.2, cách
  đợt 2.8s và lệch Y40; 40% thử ném vào một làn ngang hàng người chơi có cảnh báo.
  Cặp được kiểm tra/hoàn tác cùng nhau, không chồng thân, vẫn phải còn đường né.
- Đòn 2 kéo dài14s so với8s đòn1, đảo làn rồi ném dọc làn; 10% ném hai làn,
  10% gọi máy bay có tên lửa thay ném, 18% thử thêm chim thường. Chờ đợt cũ đi hết
  và nghỉ ngắn giữa hai hành động. Không tự giảm máu boss hoặc giới hạn giờ đấu.
- E chỉ bắn sau khi nạp máu xong. Gợi ý E nhấp nháy tới phát đầu; thanh hồi tên
  lửa luôn hiện khi giao chiến. Rocket riêng, bay thẳng lên, kiểm tra swept shape
  và vật trúng gần nhất. Vật cản chặn phát bắn bị phá/nổ; không phá pickup có lợi.
- Theo chỉnh sửa cuối của người dùng: không hiện số HP boss. boss.hitsToDefeat=x
  (mặc định8); mỗi phát trúng trừ1/x tổng thanh máu, tính bằng số hit để tránh sai số.
- Máu phần thường sinh theo lịch14s, boss ngẫu nhiên10..16s sau lần đầu8s.
  Khiên chỉ ở boss phase, tối đa2; từ10s, mỗi12s xét15%, có thể không sinh cái nào.
  Chọn trúng nhưng vị trí bị chặn thì giữ lượt để thử lại, không quay xác suất liên tục.
- Khiên bảo vệ8s sau nhặt, chặn cả LETHAL và hiệu ứng vật cản; tiếp xúc làm mờ còn45%
  trong0.25s, không tiêu hủy vật đang xuyên. Hết khiên vẫn ở trong vật thì tính lại va chạm.
- Tách model/boss/AirBoss, model/projectile/PlayerRocket, BossProjectile và ShieldPickup;
  AirBossConfig kiểm tra cấu hình, AirBossRenderer chỉ vẽ. Pause/restart áp dụng toàn luồng.
- day5-2.properties có227 tham số, tất cả ghi chú tiếng Việt cạnh giá trị,19 nhóm,
  UTF-8 BOM. README giữ phần người làm ảnh trước, bổ sung sprite/cơ chế/điều khiển boss.
- Maven package:115 tests passed,0 failures/errors. run.cmd --smoke PASS cả6 màn.
  Đã xem ảnh Java2D boss-loading/ready/wave/shield; chưa playtest GUI bằng tay.

## Sửa va chạm máy bay màn 52 (16/10/2026)

- EnemyPlane.collisionShape() dùng đường viền đa giác đúng hình máy bay tạm;
  EntityRenderer vẽ cùng shape. CollisionManager kiểm tra shape thay vì toàn bộ
  khung chữ nhật 240x140, tránh chết ở các góc trống quanh cánh/mũi.
- Obstacle mặc định vẫn có collisionShape hình chữ nhật. SafeSpawnPolicy kiểm tra
  tiếp xúc hiện tại bằng shape; dự báo đường đi/chống chồng lấn vẫn dùng khung bao
  chữ nhật bảo thủ để giữ khoảng né an toàn. Không thêm thông số hoặc đổi kích thước.
- Thêm 5 test: góc trống dưới hai phía, góc trống trên, chạm cánh, chạm mũi và hình
  vẽ khớp shape tại tọa độ lẻ. Maven package đạt 97 tests; JAR smoke PASS cả 6 màn.

## Màn 51 còn25s, hồi máu màn52 và chim bay vào rồi dừng (16/10/2026)

- day5-1.properties: level.durationSeconds=25 (thời gian tiến hiệu dụng; hit penalty
  vẫn làm chậm tiến độ như trước). Các màn còn lại giữ60s.
- Bỏ cách tạo chim đứng sẵn giữa màn. Mọi Bird xuất hiện ngoài biên trái/phải,
  FLYING trước; bird.stationaryChance=20 chọn con sẽ dừng tại tâm một làn còn ở
  trước nhân vật khi tới nơi. Sau đó STANDING, chỉ cuộn theo cảnh;80% bay xuyên qua.
  BirdMovement mô tả cả đoạn bay và dừng, dùng chung cho update/dự báo. Xóa key cũ
  bird.stationaryLeadY; cả hai hành vi cùng dùng crossing.leadY. Giữ minSpeed160..220.
- HealthPickup/HEAL là vật phẩm lợi ích, không bị coi là hazard. HealthPickupSpawner
  có lịch riêng, mặc định chỉ bật màn52: đầu6s, sau10..16s, một item hoạt động, thử
  lại sau1s khi không đặt an toàn. Vật phẩm40x40 bay từ ngoài biên trên xuống140px/s;
  kiểm tra cả không chồng lấn và có đường để thu thập, giữ maxActive của level.
- Nhặt hồi1HP, player.maxHealth=3; không hồi sinh hoặc vượt cap. HUD hiện HP hiện tại/
  tối đa. Chưa có ảnh dùng hộp đỏ dấu cộng trắng; sprite.healthPickup trỏ ảnh day5.
- Mọi cấu hình mới có nhóm/ghi chú tiếng Việt, lưu UTF-8 BOM. Hiện807 tham số trong
  7 properties. README cập nhật cơ chế, asset mới và danh sách class.
- Maven test:92 passed,0 failures/errors; có6 test health/cap/no revive/spawn/contact/
  schedule/retry/pause/restart, test chim vào từ biên trước khi dừng, tỷ lệ20% trên
  2.000 mẫu và đủ ba làn, forecast khớp chuyển động.1.000 safety vẫn621/379.
- run.cmd --smoke build JAR và PASS sáu màn; có ảnh preview air-health-bird. Chưa
  playtest GUI bằng tay hoặc ghép ảnh/âm thanh thật.

## Chó đứng lại và sửa bẫy giao thông đến muộn (16/10/2026)

- Dog đổi pha cuối thành STANDING. Bỏ timer dog.recoveryDurationSeconds và đặt
  dog.consumed=false: chó ở nguyên điểm đáp trong thế giới, vẫn có collider sau
  khi lao/chạm player, chỉ cuộn ra ngoài màn hoặc hết vòng đời chung mới bị dọn.
  Dự báo activeAt không còn coi chó là đã biến mất sau0,5s.
- Xác định điểm yếu của horizon4,5s: xe tắc đường từ mép trên mất gần5s mới chạm
  hàng player, nên mô hình cũ có thể chấp nhận vật cản trước lúc nó tạo bẫy.
- SafeSpawnPolicy dự báo tới lúc hazard cuối qua player +escapeBuffer0,5s, min4,5s,
  maxPrediction20s (validation phải đủ maxLifetime+buffer). Có pathClearance8px mỗi
  bên, không thay collider thật. Obstacle.activeAt dự báo cả giới hạn tuổi thọ.
- Tìm quỹ đạo thực tới các làn/giữ vị trí trước khi BFS; mọi quỹ đạo dùng cùng vật lý
  và swept collision. BFS giữ phân bố trạng thái cả trái/phải khi phải giới hạn số
  nhánh, sửa thiên lệch chỉ lấy những trạng thái đầu danh sách. Fast path cuộn cảnh
  không có energy/slow giúp tránh tính tích phân không cần thiết khi dự báo dài.
- Ba key spawn mới có ghi chú theo nhóm trong cả6 cấu hình level; README cập nhật.
  Hiện787 thông số trong7 properties, đã quét ghi chú/encoding.
- Maven package thành công:86 tests passed,0 failures/errors. Có regression tường
  xe tới sau4,5s, hàng xe lệch nhau, còn một làn trống, chó đứng tiếp tục chắn làn,
  chó không consume sau contact, và sinh thực tế5seed x15s (kiểm tra cả12s tương lai).
  1.000 tình huống safety:621 accepted,379 rejected. run.cmd --smoke PASS cả6màn.
- Chưa playtest GUI bằng tay; kiểm tra vẫn dùng mô hình hữu hạn với lưới trạng thái.
  Có đường né không đồng nghĩa mọi lựa chọn điều khiển đều tránh va chạm.

## Tỉ lệ chống đứng khe làn, cống gây damage, nhấp nháy và chó lao (16/10/2026)

- Nhân vật đường bộ100x100, hitbox88x86, X140..760. Các vật cản chiếm làn rộng
  ít nhất160px (chim170px; máy bay240px), ảnh được tăng cùng collider. Tổng bề
  ngang obstacle + hitbox player >240px để đứng trên vạch làn vẫn có thể bị trúng.
  Chó100x70 dùng quỹ đạo băng ngang/lao; giữ di chuyển player liên tục, không snap.
- Cống vẫn SLOW nhưng gây drain.damage=1, dùng cùng hurt/invulnerability/progress
  penalty như va chạm thường. Contact set ngăn mất máu mỗi frame.
- Player có timer nhấp nháy riêng: hitBlinkDurationSeconds1.1, interval0.1. Mọi
  hurt/kill đều kích hoạt. GameOver chỉ cập nhật timer hình ảnh, không gameplay;
  pause đóng băng timer, restart tạo Player mới. Renderer dùng Player.visible().
- DogMovement tách vị trí thế giới khỏi camera, dự báo/di chuyển dùng cùng công thức.
  Dog: CROSSING -> BARKING0.8s -> CHARGING0.45s -> RECOVERING0.5s rồi remove.
  Sinh trước380px, băng ngang700..900px/s tới tâm làn ngẫu nhiên; đích lao lệch±60px,
  giữ toàn thân trong làn, chốt trước warning và không đuổi theo player. Đánh dấu
  điểm lao + dấu!42px nhấp nháy. sprite.dog.warning sẵn đường dẫn; audio chưa phát,
  trạng thái BARKING dùng để ghép âm thanh sau. SafeSpawnPolicy dự báo cả cú lao.
- Tất cả key mới có ghi chú tiếng Việt, theo nhóm trong properties; hiện770 thông số.
  README cập nhật bảng kích thước, tài nguyên, cách chỉnh và các cơ chế mới.
- Maven package:81 tests passed,0 failures/errors; bao gồm va chạm ranh giới làn,
  cống trừ đúng máu/contact, blink render/pause/death, FSM/quỹ đạo/target/charge damage
  và1.000 tình huống safety (638 accepted,362 rejected). run.cmd --smoke PASS6màn.
- Đã xem ảnh Java2D lane-proportions, dog-warning, dog-charge trong target/smoke;
  chưa playtest GUI bằng tay hoặc cân bằng độ khó toàn bộ màn sau đổi kích thước.

## Ghi chú toàn bộ properties và chia nhóm (16/10/2026)

- Đã sắp lại cả 7 file properties thành các nhóm đánh số, ngăn bằng dòng # ===.
  743 thông số đều có ghi chú tiếng Việt ngay bên phải, gồm đơn vị và ý nghĩa.
- Thêm PropertiesReader dùng chung cho DayConfigLoader và AppSettings: đọc UTF-8
  có/không BOM, bỏ ghi chú cuối dòng bắt đầu bằng dấu # có khoảng trắng, giữ nguyên
  màu hex và dấu # trong giá trị/đường dẫn; dùng dấu gạch chéo ngược để thoát dấu #.
- Cả 7 properties lưu UTF-8 có BOM để tránh nhận sai mã hóa trên Windows.
- Đã so sánh bằng Java Properties trước/sau: đủ 7 file, 743 key/value giữ nguyên.
  Không đổi tham số gameplay. README cập nhật cách ghi chú và bộ đọc dùng chung.
- Maven package: 72 tests passed, 0 failures/errors, có kiểm tra bộ đọc mới với
  số, boolean, tên tiếng Việt, màu, đường dẫn, dấu # và BOM.
- run.cmd --smoke PASS cả sáu màn từ JAR.

## Nhận dạng encoding của day5-2.properties (16/10/2026)

- Người dùng chỉ rõ day5-2.properties vẫn hiển thị ghi chú lỗi. Nội dung UTF-8 đúng,
  nhưng đọc mặc định bằng Windows PowerShell bị giải mã ANSI. Đã tái hiện trước sửa.
- Lưu riêng file này UTF-8 có BOM để tự nhận dạng; sau sửa Get-Content mặc định đã
  in đúng toàn bộ dấu tiếng Việt. DayConfigLoader.read bỏ BOM tùy chọn trước khi
  Properties.load, không để dấu này thành một phần của key đầu tiên.
- Có regression test BOM/không BOM cho cùng dữ liệu, bao gồm tên màn tiếng Việt.
  Maven package: 69 tests passed; run.cmd --smoke PASS sáu màn. README cập nhật encoding.

## Rà tiếng Việt và encoding (16/10/2026)

- Rà 90 file văn bản source/config/script/tài liệu: sửa hai hướng dẫn Esc/R trong
  GameRenderer và chuyển 245 Unicode escapes ở day5-2.properties thành chữ UTF-8
  đọc trực tiếp (gồm ghi chú và tên màn). Không đổi giá trị gameplay.
- run.cmd bật code page65001, Java file/stdout/stderr UTF-8; smoke terminal đã in đúng
  tên tiếng Việt của cả sáu màn. README bổ sung cách lưu/đọc UTF-8.
- Maven package: 68 tests passed; run.cmd --smoke PASS sáu màn.

## Máy bay rộng một làn (16/10/2026)

- enemyPlane.width=240 px, chiều cao140px, cấu hình có ghi chú trong day5-2.properties.
- Loader cho phép riêng EnemyPlane rộng tối đa LANE_WIDTH; README cập nhật kích thước ảnh.
- Test bắn trái/phải đặt máy bay đúng tâm làn theo kích thước cấu hình, thay tọa độ cũ.
- Maven package thành công: 68 tests passed, 0 failures/errors.

## Chim đứng yên trên làn (16/10/2026)

- Thêm bird.stationaryChance=20 (%) trong day5-2.properties, xác suất chọn hành vi
  đứng trên làn của mỗi ứng viên chim; còn lại bay ngang. 0/100 đều được hỗ trợ.
- Chim đứng đặt ở tâm làn ngẫu nhiên, Y=player.y-bird.stationaryLeadY (mặc định300px).
  Vận tốc riêng bằng0, chỉ tiến gần do camera scroll; dùng cùng Bird/ScrollingMovement,
  collision, lifetime và safe-spawn như cũ. Không thêm class hoặc ảnh mới.
- Các key mới có comments/validation và README; bird.minSpeed/maxSpeed chỉ áp dụng
  chim bay. bird.chance vẫn là xác suất chọn loại chim của chế độ independent.
- Maven test: **68 tests passed, 0 failures/errors**, có kiểm tra đứng yên/xuất hiện
  trên làn, camera dừng, cấu hình0/100, phân bố mặc định20% và đủ cả ba làn.
- run.cmd --smoke build JAR và PASS sáu level. Chưa playtest GUI bằng tay.

## Điều chỉnh màn bay theo phản hồi tiếp theo (16/10/2026)

- Vùng di chuyển Day5Helicopter trở về [140,860] như đường bộ, ba làn giữa và lề
  140 px mỗi bên. Vì trực thăng rộng100px, player.minX=140/maxX=760; Y=520.
- Missile bỏ ngắm chéo vào trái/phải player. Chọn cánh trái/phải máy bay
  (missile.sideOffset=50), giữ nguyên X suốt lượt, bay thẳng xuống sau warning0,2s.
  Warning/đường ngắm thẳng trùng tâm collider đạn, không retarget/homing.
- Chim giữ ScrollingMovement: X tăng theo tốc độ ngang riêng, Y thế giới cố định;
  độ thay đổi Y màn hình đúng bằng camera scroll. Không thêm vận tốc dọc của chim.
  Test mới chứng minh khi cảnh dừng chim tiếp tục ngang mà không xuống.
- Maven test: **66 passed, 0 failures/errors**. Bounds, actual plane fire, random
  wing selection, warning/collision/render và missile constant-X đều đã kiểm tra.
- `run.cmd --smoke` PASS cả sáu level sau build JAR; chưa playtest GUI bằng tay.

## Cập nhật gameplay (16/10/2026)

Yêu cầu bổ sung sau prompt.md đã triển khai:

- Cống giảm tốc ngang và progress còn 65% trong 1,2s. Bóng tuyết đóng băng 0,6s
  (progress 0), sau đó di chuyển 60%/progress 50% thêm 1,5s. Có thông báo trên HUD.
- Day4: energy giảm tuyến tính tốc độ xuống tối đa 20%; pickup hồi energy và cho
  tốc độ 140% bình thường trong 2s, tạm thay mức mệt. Tất cả chỉnh trong properties.
- ScrollingMovement/DriftMovement dùng travel time chịu slow/boost: cảnh và vật tĩnh
  cuộn theo tiến độ, còn vật tự chạy/bay giữ tốc độ riêng. Forecast dùng cùng travel
  multiplier, có cache theo revision của effects/energy; xử lý epsilon biên timer.
- Chó/chim xuất hiện phía trước `crossing.leadY=300`, băng ngang đồng thời cuộn xuống,
  không sinh ngay hàng player. Horizontal speed vẫn do minSpeed/maxSpeed từng loại.
- Day5Helicopter thêm EnemyPlane (LETHAL) bay từ trên xuống, bắn Missile (NORMAL_DAMAGE).
  Đích ngẫu nhiên trái/phải, lệch tâm player 160px lúc khóa đích; không homing.
  Warning 0,2s, blink 0,05s rồi mới bay, tốc độ 2 × player.maxSpeed = 840px/s.
  Các key enemyPlane.* và missile.* nằm trong day5-2.properties; weighted bird45/plane55.
- Đạn được kiểm tra safe path/overlap trước khi thêm. Chỉ cho phép giao với máy bay
  phóng ra nó; các vật khác vẫn kiểm tra như trước. Dùng snapshot khi update danh sách
  để máy bay sinh đạn không gây ConcurrentModificationException; vẫn giữ maxActive.
- Pickup energy tính vị trí sinh từ travel đã điều chỉnh tốc độ, tránh hẹn nhặt sai
  thời điểm khi mệt/boost. Test chơi đứng yên nhặt các nguồn tiếp tế vẫn hoàn thành.
- Đã cập nhật README, bao gồm ảnh enemyPlane.png (140x140), missile.png (18x36),
  missile-warning.png (52x52); chưa tạo ảnh thật.

Kiểm chứng mới nhất: Maven offline package thành công, **65 tests, 0 failures/errors**,
bao gồm 1.000 tình huống safety, slow/progress/energy, pause/restart, chó/chim, máy bay
bắn thật trong level loop, warning không damage, nhấp nháy renderer và quỹ đạo đạn.
`run.cmd --smoke` PASS cả 1/2/3/4/51/52; đã xem ảnh màn 52. Runtime vẫn JDK25,
compile release17. Chưa playtest GUI bằng tay hoặc cân bằng lại độ khó toàn màn.
Các kết quả bên dưới là lịch sử triển khai trước cập nhật này.

## Hiện trạng và quyết định (15/10/2026)

Đã đọc prompt.md, README, Story.docx, toàn bộ source và các bài test hiện có.
Không có CONTEXT.md cũ hoặc tài liệu cuộc họp khác trong repository.
Repository ban đầu sạch, ngoại trừ prompt.md do người dùng thêm.

- Bản cũ có 3 cấu hình ngày, chuyển làn, xe/cá, xác suất độc lập và kiểm tra chống chồng lấn.
- GamePanel đang chứa gameplay, không có HP, tiến độ, energy, pause hoặc chuyển ngày.
- Tái sử dụng ý tưởng FSM cá, dự báo chuyển động, cấu hình cạnh level, asset fallback;
  chuyển rendering ra khỏi model, mở rộng các regression test theo kiến trúc mới.
- prompt.md mới là đặc tả đang thực thi: viewport 1000x700, đường 720px,
  di chuyển ngang có gia tốc, Java 17, Maven/JUnit 5, 6 level.
- Spawn mặc định theo trọng số như mục 9 của prompt; giữ tùy chọn independent để
  hỗ trợ yêu cầu xác suất độc lập trước đây, có config rõ ràng.
- Không làm story/dialogue/cutscene hoặc debug overlay.
- Maven chưa được cài toàn cục; đã tải bản Apache Maven 3.9.9 vào .tools (ignored).
  JDK hiện có là 25; sẽ compile với --release 17 và ghi rõ runtime kiểm chứng.

## Kế hoạch theo giai đoạn

1. Maven, Main, config immutable và resource packaging.
2. Engine, movement, HP/collision/effects, progress, spawn an toàn, Day1, HUD/input.
3. Day2/Shark/Drain/Crate và Day3/Ice/Freeze/Snowman.
4. Day4/Bus/Cactus/Glare/Energy/Pickup.
5. Day5Road/Dog/TrafficJam và Day5Helicopter/Bird.
6. Fade, chọn màn, auto advance và No-Story flow.
7. Unit/integration tests, JAR smoke tests, README và cập nhật context cuối.

## Kiểm chứng

Chưa chạy build Maven/test của phiên bản mới. Không coi các giai đoạn là hoàn tất
cho tới khi có kết quả thực tế. GUI trực quan chưa được kiểm chứng.

## Giai đoạn 1–3: nền tảng và các màn đầu

- Đã chuẩn hóa Maven, Main.java, --release 17, JUnit 5 và resource includes chỉ *.properties.
- Đã tách Entity/Player/Helicopter, HorizontalMotion, MovementBehavior, EffectManager,
  CollisionEffect/CollisionManager, ProgressManager, SafeSpawnPolicy, ObstacleFactory/Spawner.
- BaseLevel quản lý một phiên chơi; Day1/2/3 dùng chung các hệ thống, không sao chép loop.
- Config mới có đầy đủ nhóm vật lý, hitbox, thời lượng, mật độ/difficulty và obstacle spec.
- Đã triển khai Shark FSM, Drain, Crate, IcePatch, Snowball và Snowman.
- Chạy Maven test: 19 test, 0 failure/error. Bao gồm config cả sáu file, vật lý nhân vật,
  hitbox, freeze/ice và FSM/cảnh báo/đớp đúng hàng. Bộ test spawn sẽ được mở rộng tiếp.
- Maven package đã tạo JAR; smoke từ JAR tải được sáu cấu hình và render sáu ảnh
  vào target/smoke. Chưa coi là kiểm chứng cân bằng hoặc gameplay đủ 60 giây.
- Có renderer/HUD/input/engine cơ sở và các lớp Day4/Day5 đã nối vào registry;
  kiểm chứng chuyên sâu energy, chuyển màn và safe spawn vẫn đang thực hiện.
- Đã phát hiện vấn đề encoding PowerShell -> Python làm hỏng chữ Việt trong file sinh;
  đã chuyển pipeline UTF-8 và sửa lại toàn bộ tên màn/config comments/UI hints.

## Giai đoạn 4–6: energy, hai màn ngày 5 và transition

- Day4 dùng Bus/Cactus, GlareEffect riêng ở rendering, EnergySystem độc lập HP.
  Supply không phụ thuộc một lần quay RNG: lịch cố định và ngưỡng reserve tạo pickup
  tại vị trí có quỹ đạo tiếp cận được, ưu tiên thẳng phía trước người chơi.
- Day5Road dùng TrafficJamVehicle (NORMAL_DAMAGE, world scroll) và Dog băng ngang.
- Day5Helicopter dùng Helicopter kế thừa Player với vùng bay riêng và Bird băng ngang.
- Registry có thứ tự 1,2,3,4,51,52, factory tạo session mới cho restart/chuyển màn.
- Một GameLoop/Timer, fixed dt=1/60, giới hạn catch-up 0.25s; GameState quản lý
  pause/gameover/transition/completion. TransitionManager có fade out/in 0.5s.
- Các hệ thống không tick trong pause/fade/gameover; input clear khi focus mất,
  freeze, restart và chuyển màn; giữ Esc không toggle liên tục.
- 36 regression test đã pass trước khi mở rộng test safety/energy. Bộ mở rộng 49 test
  phát hiện 2 lỗi validation: ràng buộc thời gian pickup bị áp lên level không dùng energy.
  Đã sửa phạm vi validation; bộ test sau sửa đã pass.
- Energy tests đã chạy pass: drain/recover, pickup khẩn cấp thực sự thu thập được,
  hoàn thành 60 giây ở fixture không hazard mà không bị RNG starvation, pause và GameOver.
- Giới hạn safety: finite horizon, state-grid/beam pruning bảo thủ, không phải chứng minh
  toán học cho input của mọi người chơi. Mọi random target của Shark kiểm tra lại trước WARNING.

## Giai đoạn 7: kiểm chứng cuối

- `mvn clean test` (Maven local, `-Dmaven.repo.local=.tools/m2`): BUILD SUCCESS,
  **52 tests, 0 failures, 0 errors, 0 skipped**.
- `mvn package`: BUILD SUCCESS, cùng 52 test pass, tạo `target/exam-run.jar`.
- `run.cmd --build-only`: exit 0. `run.cmd --smoke`: exit 0, SMOKE PASS cho
  1,2,3,4,51,52, tạo sáu ảnh target/smoke/level-*.png.
- Chạy JAR bằng đường dẫn tuyệt đối với working directory `.tools/jar-smoke`:
  cả sáu config vẫn load/render được, không có dependency vào source working directory.
- Kiểm tra ZIP/JAR: sáu properties level + game.properties được đóng gói; không có
  source .java; có Main.class, không có main.class; classfile major version 61 (Java 17).
- Test safety: 1000 tình huống/seed, 645 được chấp nhận, 355 bị từ chối ở fixture cuối.
  Có thêm test tường ba làn, freeze/vận tốc/phản ứng, overlapping traffic, crossing/drift.
- Đã xem ảnh render offscreen Day1/Day5Helicopter; chữ Việt hiển thị đúng.
  Đây không phải playtest cửa sổ GUI tương tác. Không báo GUI thủ công đã pass.
- Runtime dùng để chạy Maven/test/JAR: JDK 25.0.4 trên Windows; compile `--release 17`.
  Máy không có runtime 17, nên chưa chạy lại thực tế bằng JVM 17 riêng.
- Dọn 20 classfile cũ cạnh source và thư mục build cũ; run.cmd chỉ chạy JAR Maven.
  Đổi tên main.java -> Main.java đã ghi nhận bằng git mv để không lỗi case khi checkout Linux.
  Không commit hoặc push. prompt.md và Story.docx được giữ nguyên.

## Bản đồ hệ thống cho phiên tiếp theo

| Nhóm | Class/interface và trách nhiệm |
| --- | --- |
| Config | GameConfig: tọa độ logic cố định. ConfigValues: snapshot immutable + cache số. DayConfigLoader: package-relative Properties/validation. AppSettings: UI/fade/loop chung. |
| Level | Level là hợp đồng; BaseLevel sở hữu một session; Day1/2/3/4/5Road/5Helicopter kế thừa; Levels giữ factory theo mã. DayConfig/ObstacleSpec là dữ liệu immutable. |
| Engine | Game: FSM, pause, restart, completion. LevelManager: thứ tự/factory session mới. TransitionManager: opacity/fade. GameLoop: duy nhất một Swing Timer/fixed accumulator. |
| Model | Entity: sprite/hitbox tách biệt, position đóng gói. Player: HP/vận tốc/invulnerability. Helicopter: biến thể vùng bay được cấu hình. |
| Movement | MovementBehavior/LinearMovement/DriftMovement: chuyển động dự báo được; HorizontalMotion dùng chung bởi Player và planner. |
| Collision | CollisionEffect/ConfiguredCollision: composition hiệu ứng. CollisionManager: lethal ưu tiên, contact identity set tránh damage mỗi frame. |
| Effects | EffectManager: HIT/DRAIN/SLIDE/FREEZE, refresh max duration, kết hợp multiplier theo min. EnergySystem độc lập HP. |
| Progress | ProgressManager tích lũy thời gian hiệu dụng, xử lý chính xác phần tick chịu hit penalty, clamp 0..1. |
| Spawn | ObstacleFactory xây subtype và movement/effect; ObstacleSpawner quản lý timer/seed/trọng số/density/cap; SafeSpawnPolicy kiểm tra quỹ đạo và reachable states. |
| Input | InputState giữ từng phím vật lý, hỗ trợ A+← đồng thời. InputHandler giữ latch riêng Esc/R để clear input không gây auto-repeat toggle. |
| Presentation | AssetLoader cache cả ảnh thiếu, EntityRenderer vẽ sprite/placeholder, GlareEffect environmental alpha, GameRenderer scene/overlays; GameHUD/ProgressBar cố định trên HUD. |

### Quan hệ OOP và collision

- Vehicle extends Obstacle; Bicycle và Bus extends Vehicle. Vật tĩnh và thùng/chó/chim/
  cầu tuyết dùng composition MovementBehavior, không kế thừa sai Vehicle.
- LETHAL: xe, xe đạp, bus, cá ATTACK; bỏ qua normal invulnerability.
- NORMAL_DAMAGE: barrier/crate/snowman/cactus/jam/dog/bird, HP mặc định 1/lượt tiếp xúc.
- SLOW/SLIDE/FREEZE: drain/ice/snowball; pickup ENERGY chỉ hồi energy và consume.
- Collision xử lý trước progress completion, nên không có thắng/thua đồng thời.

### SafeSpawnPolicy và các giới hạn

- Horizon 4.5s, branch input -1/0/1 theo bước 0.15s, physics substep 1/60.
- Trong reaction window giữ hướng input hiện tại, không giả định được teleport/hãm tức thì.
- State giữ X và velocity thực; grid 18px/70px/s chỉ gộp/prune nhánh, tối đa 120 states.
- Dự báo obstacle và swept hitbox được cache theo tick để hạn chế IO/allocation trên EDT.
- Chỉ chấp nhận nếu có ít nhất một quỹ đạo không va hazard, có tính freeze/slow/ice hiện tại.
- Từ chối overlap quỹ đạo giữa các obstacle, có minimumGap 12px, không cưỡng ép sinh.
- Shark: FIN_VISIBLE -> DIVING -> thử safe target -> WARNING -> ATTACK -> DISAPPEAR.
  Mỗi trạng thái được đủ thời gian; frame trễ không skip cảnh báo.
- Giới hạn hữu hạn, không chứng minh mọi tình huống ngoài horizon hoặc mọi config người dùng.
  Pickup safety kiểm tra tồn tại đường thu thập ở thời điểm hẹn, không ép người chơi đi theo đường đó.

### Thông số và tài nguyên

- Viewport 1000x700; road [140,860], làn 240px; player sprite60x90/hitbox48x76.
- Mỗi level duration=60s, HP=3; Day5Road và Helicopter có duration riêng.
- Các nhóm key: level/player/collision/progress/spawn/difficulty/shark/drain/ice/
  snowball/energy/glare/sprite. Tất cả nằm trong properties cạnh các class level.
- `spawn.mode=weighted` là mặc định theo prompt mới. `independent` giữ tính năng cũ,
  dùng <type>.chance độc lập (0..100), vẫn qua safety và maxActive.
- Properties source được copy vào JAR qua Maven resource includes, không load bằng path tương đối.
  Thay properties phải build lại. Renderer có sprite path cho mọi entity và trạng thái cá;
  thiếu ảnh luôn fallback. road.png thật không bị vẽ thêm vạch nếu overlayLines=false.

## Việc còn lại / giới hạn có chủ đích

- Chơi thử GUI bằng người trên các màn 60s, cân bằng nhịp spawn, energy và cảm giác trượt.
- Chạy cùng test/JAR trên JVM 17 thực nếu cần kiểm chứng runtime tối thiểu.
- Tích hợp ảnh/animation/audio thật; hiện chỉ có placeholder và đường dẫn.
- Story/dialogue/cutscene chưa làm theo đúng phạm vi No-Story Mode. GameState/transition
  là điểm tích hợp về sau; không thêm debug mode/F3.
- Chưa có save/load hoặc menu chọn màn trong UI; chọn qua Main/CLI theo prompt.
- .tools chứa Maven và dependency cache của workspace, không phải artifact cần commit.
