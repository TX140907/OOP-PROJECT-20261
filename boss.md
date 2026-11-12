# TÀI LIỆU THIẾT KẾ & HƯỚNG DẪN PROMPT AI CHO TRÙM CUỐI MÀN 5 (`Boss`)

Tài liệu này mô tả chi tiết tạo hình, hệ thống **6 trạng thái động tác (Animation States)** của **Trùm Cuối: Giám Thị Tối Cao** ở Màn 5, kèm theo **hướng dẫn quy trình & câu lệnh Prompt chi tiết** để tạo bộ ảnh bằng các công cụ AI (ChatGPT / DALL-E 3, Midjourney, Leonardo.ai, Stable Diffusion) đảm bảo **đồng nhất nhân vật 100%** và có chuyển động sống động trong game.

---

## 1. THÔNG SỐ KỸ THUẬT & ĐƯỜNG DẪN ĐẶT FILE

- **Thư mục đặt ảnh:** `assets/objects/Boss/`
- **Kích thước hiển thị chuẩn trong game (1x):** **`130 × 176 px`** *(Tỷ lệ khung dọc `14 : 19`, phóng to ~1.85x so với bản cũ để tạo uy lực của Trùm Cuối)*.
- **Kích thước xuất file khuyên dùng cho Artist:**
  - **Chuẩn 2x HD:** **`260 × 352 px`**
  - **Chuẩn 3x Siêu nét:** **`390 × 528 px`** (Ảnh hiện tại bạn đã nạp vào `840 × 1140 px` hiển thị cực kỳ sắc nét trên màn hình).
- **Định dạng:** `.PNG` có **nền trong suốt hoàn toàn (Transparent Background - 32-bit RGBA)**.
- **Góc nhìn & Hướng bắt buộc:**
  - Góc nhìn ngang (**2D Side-View / 3/4 Front-Left Profile**).
  - **Mặt, ngực và cánh tay tung chiêu BẮT BUỘC quay sang BÊN TRÁI (`-X`, hướng 9 giờ)** để đối diện trực diện với trực thăng người chơi đang bay ở bên trái màn hình.
  - Phần áo choàng bay ngược về **BÊN PHẢI (`+X`, hướng 3 giờ)**.

---

## 2. Ý TƯỞNG TẠO HÌNH NHÂN VẬT ("GIÁM THỊ TỐI CAO")

- **Hình tượng:** Vị "Giám Thị Tối Cao" (hoặc giảng viên / học giả quyền năng của Bách Khoa hóa thân thành Boss cuối thử thách sinh viên trước giờ vào phòng thi C1).
- **Đặc điểm nhận diện cố định (Giữ nguyên xuyên suốt mọi trạng thái):**
  1. **Trang phục:** Mặc bộ **âu phục / áo vest măng tô đen lịch lãm (`#222630`)**, sơ mi trắng cổ cồn, thắt **cà vạt đỏ thẫm (`#D72630`)**.
  2. **Áo choàng:** Khoác **áo choàng dài màu đỏ máu (`#821C1C`)** trên vai, tà áo bay phấp phới ra phía sau lưng (bên phải).
  3. **Khuôn mặt & Đầu:** Tóc đen vuốt ngược gọn gàng đầy uy quyền, đeo **kính gọng vàng kim loại (`#FFD732`)**, đôi mắt phía sau kính **phát sáng rực lửa đỏ (`#FF2323`)**.
  4. **Năng lượng:** Bay lơ lửng giữa bầu trời, bàn tay điều khiển **quả cầu năng lượng lửa đỏ cam** hoặc tia chớp ma thuật, xung quanh người có vầng hào quang năng lượng mỏng.

---

## 3. HỆ THỐNG 6 TRẠNG THÁI (STATES / FRAMES) TRONG GAME

Hệ thống code trong `BossEnemy.java` đã được lập trình để **tự động chuyển đổi linh hoạt giữa 6 file ảnh dưới đây** tùy theo hành động thực tế của Boss trên màn hình *(nếu thiếu file nào, game sẽ tự động lùi về dùng ảnh `Boss_idle1.png` hoặc `Boss.png` mà không bao giờ lỗi)*:

| STT | Tên file trong `assets/objects/Boss/` | Khi nào xuất hiện trong Game? | Thay đổi nhẹ về động tác (Pose Variation) để tạo tính sinh động |
| :---: | :--- | :--- | :--- |
| **1** | `Boss_idle1.png`<br>*(hoặc `Boss.png`)* | **Trạng thái cơ bản (Nhịp 1)** — Khi Boss bay lơ lửng di chuyển lên xuống. | **Tư thế chuẩn:** Thân người hơi nghiêng nhẹ về phía trước (bên trái), tay phải khoanh trước ngực hoặc đặt sau lưng, **tay trái đưa nhẹ ra trước ngang bụng nâng một quả cầu lửa đỏ cam nhỏ**. Tà áo choàng phía sau (bên phải) lượn sóng **hơi võng xuống**. |
| **2** | `Boss_idle2.png` | **Trạng thái cơ bản (Nhịp 2)** — Tự động luân phiên với `idle1` mỗi `0.22 giây`. | **Thay đổi rất nhẹ so với Frame 1:** Toàn thân giữ nguyên góc độ nhưng **tà áo choàng phía sau phồng cao lên**, đuôi cà vạt đỏ bay nhẹ lên, quả cầu lửa trên bàn tay trái **bùng to hơn một chút** và tóc mai hơi bay nhẹ. *(Khi ghép `idle1` ↔ `idle2`, Boss sẽ có hiệu ứng áo choàng phần phật trong gió cực kỳ sống động)*. |
| **3** | `Boss_charge.png` | **Đầu trận (`STATE_CHARGING` - 2.5s)** — Khi Boss mới bay vào và gồng nạp đầy 20 vạch máu. | **Tư thế tụ khí / gồng năng lượng:** Hai cánh tay hơi co lại hai bên sườn (tư thế gồng lực), đầu hơi cúi nhẹ nhìn sắc lạnh qua gọng kính vàng, **mắt đỏ rực sáng chói**, áo choàng tung cao dựng đứng phía sau, **quầng hào quang lửa đỏ-vàng bùng lên quanh người**. |
| **4** | `Boss_attack_aim.png` | **Đòn bắn tỉa (`STATE_ATTACK1`)** — Khi Boss chiếu tia Laser đỏ khóa mục tiêu và bắn đạn lửa tốc độ cao. | **Tư thế ngắm bắn chuẩn xác:** Cánh tay trái **duỗi thẳng tắp về phía trước (hướng 9 giờ)** chỉ thẳng vào người chơi, lòng bàn tay hoặc đầu ngón tay phát ra **luồng sáng laser đỏ rực & quả cầu lửa nén chặt**, vai xoay nhẹ theo hướng tay chỉ, tà áo choàng bị lực giật thổi bay ngược mạnh về bên phải. |
| **5** | `Boss_attack_spray.png` | **Đòn xả mưa đạn & Gọi máy bay ném bom (`STATE_TRANSITION_SPRAY` & `STATE_ATTACK3`)** | **Tư thế tung chiêu diện rộng / Chỉ huy:** Một tay (hoặc cả hai tay) **vung cao lên trên đầu / mở rộng**, xung quanh bàn tay tỏa ra **các tia năng lượng màu Tím Hồng Neon & Đỏ**, biểu cảm ngạo nghễ như đang ra lệnh cho phi đội tiêm kích rải thảm bom. |
| **6** | `Boss_hit.png` | **Khi trúng tên lửa của người chơi (`lastHitAt`) hoặc lúc bị hạ gục (`STATE_DEFEATED`)** | **Tư thế trúng đòn / Choáng nhẹ:** Thân trên **ngửa giật nhẹ về phía sau (bên phải)** do lực nổ của tên lửa, một tay đưa lên che trước ngực, đầu hơi nghiêng, kính gọng vàng lóe sáng, có vài **tia lửa điện / vết khói nhỏ** quanh vai áo. |

---

## 4. HƯỚNG DẪN PROMPT CỤ THỂ CHO CÔNG CỤ AI TẠO SINH ẢNH

### 💡 Bí quyết số 1 để tạo 6 trạng thái KHÔNG BỊ LỆCH NHÂN VẬT:
Nếu bạn gõ 6 câu lệnh độc lập, AI sẽ vẽ ra 6 ông giám thị khác nhau. Hãy làm theo **Quy trình 2 bước chuẩn Studio** sau:
1. **Bước 1 (Tạo ảnh gốc `Boss_idle1.png` hoặc Sprite Sheet):** Dùng **Prompt Gốc (Master Prompt)** bên dưới để tạo ra nhân vật ưng ý nhất.
2. **Bước 2 (Biến đổi nhẹ cho các trạng thái sau):**
   - **Nếu dùng ChatGPT (DALL-E 3 / GPT-4o Image):** Tải ảnh `Boss_idle1.png` vừa tạo lên khung chat (hoặc bấm vào ảnh vừa tạo) và gõ lệnh chỉnh sửa trực tiếp (*"Keep the exact same character, outfit, art style, and proportion, only change the arm pose and cape slightly to..."*).
   - **Nếu dùng Midjourney:** Copy link ảnh `Boss_idle1.png` và thêm tham số `--cref <link_ảnh> --cw 100` vào cuối các Prompt trạng thái 2, 3, 4, 5, 6.
   - **Nếu muốn tạo 1 lần ra cả 6 tư thế đồng nhất tuyệt đối:** Sử dụng **Prompt tạo Sprite Sheet 6 ô** ở mục 4.1 bên dưới, sau đó cắt từng ô ra thành 6 file!

---

### 4.1. CÁCH NHANH NHẤT: Prompt tạo nguyên bộ Sprite Sheet 6 trạng thái trong 1 ảnh (Khuyên dùng!)
Copy nguyên đoạn Prompt tiếng Anh dưới đây dán vào **ChatGPT (GPT-4o) / Midjourney / Bing Image Creator / Leonardo.ai**:

```text
2D game character sprite sheet of a hovering anime/cartoon final boss called "The Supreme Proctor", 6 distinct action poses arranged in a 2x3 grid on a clean solid white background, full body side-profile view facing LEFT (looking toward the left side of the canvas).
Character design (must be 100% identical across all 6 poses): A powerful, sharp-featured male professor/proctor wearing a tailored dark charcoal suit (#222630), crisp white dress shirt, crimson red necktie (#D72630), and a long flowing blood-red cape (#821C1C) billowing to the right. Slicked-back black hair, gold-rimmed rectangular glasses (#FFD732) with glowing red eyes (#FF2323) behind the lenses. He floats in mid-air with subtle dark-red energy aura.
The 6 poses from top-left to bottom-right:
1. [Idle 1]: Hovering calmly facing left, left hand extended slightly at waist level holding a small glowing orange-red energy orb, cape flowing smoothly down-right.
2. [Idle 2]: Same hovering pose as Idle 1, but the red cape billows higher upward and the energy orb in his hand glows slightly brighter.
3. [Charging Power]: Both arms tensed near his sides in a power-up stance, head tilted slightly down with intense glowing red eyes, cape whipping upward, surrounded by a fiery red-gold aura.
4. [Laser Aiming / Sniper Shot]: Left arm stretched straight forward pointing horizontally to the left, firing a concentrated red laser beam/fireball from his palm, cape blown sharply backward to the right.
5. [Barrage / Summoning]: One arm raised high above his head commanding an airstrike, surrounded by glowing neon purple and crimson magic sparks, dramatic billowing cape.
6. [Hit / Damaged]: Recoiling slightly backward to the right from an impact, one arm raised defensively in front of his chest, gritting teeth, small impact sparks around his shoulder.
Clean 2D flat vector / modern 2D arcade game art style, bold crisp outlines, vibrant colors, no background clutter, each pose well-separated for easy cropping.
```

---

### 4.2. BỘ PROMPT CHI TIẾT CHO TỪNG FILE RIÊNG LẺ (Nếu tạo từng ảnh một)

#### 🔹 Trạng thái 1: `Boss_idle1.png` (Ảnh chuẩn gốc)
> **Prompt:**
> ```text
> 2D game boss sprite, single character, full body side-view profile facing LEFT, vertical aspect ratio. A powerful male academy proctor boss floating in mid-air, wearing a sleek dark charcoal business suit, white collar shirt, crimson red tie, and a long blood-red cape flowing to the right. Slicked-back black hair, gold-rimmed glasses, glowing red eyes. He hovers calmly facing left with his left hand slightly raised holding a small glowing orange-red energy sphere. Clean 2D vector game art style, sharp outlines, isolated on a plain solid white background, no ground shadow.
> ```

#### 🔹 Trạng thái 2: `Boss_idle2.png` (Biến thể nhịp thở / Áo choàng bay cao)
> **Prompt (Kèm ảnh `Boss_idle1.png` làm tham chiếu):**
> ```text
> Keep the exact same character design, outfit, glasses, facing LEFT direction, and 2D game art style from the reference image. Subtle animation frame variation: his blood-red cape billows higher upward in the wind, his red tie flutters slightly, and the orange-red energy sphere in his left hand pulses slightly larger and brighter. Isolated on a plain solid white background.
> ```

#### 🔹 Trạng thái 3: `Boss_charge.png` (Gồng nạp năng lượng đầu trận)
> **Prompt (Kèm ảnh `Boss_idle1.png` làm tham chiếu):**
> ```text
> Keep the exact same character design, outfit, glasses, facing LEFT direction, and 2D game art style from the reference image. Change pose slightly to a power-charging stance: both arms pulled slightly back near his waist clenching fists with glowing red-orange energy, his cape whipping upward, and a fiery red-gold aura surrounding his body as he charges full health. Isolated on a plain solid white background.
> ```

#### 🔹 Trạng thái 4: `Boss_attack_aim.png` (Giơ thẳng tay ngắm bắn Laser & Đạn lửa)
> **Prompt (Kèm ảnh `Boss_idle1.png` làm tham chiếu):**
> ```text
> Keep the exact same character design, outfit, glasses, facing LEFT direction, and 2D game art style from the reference image. Change pose to an aiming attack stance: his left arm is extended straight forward horizontally to the left, palm open aiming a concentrated glowing red laser core and fireball toward the left, his cape blown sharply backward to the right by the recoil. Isolated on a plain solid white background.
> ```

#### 🔹 Trạng thái 5: `Boss_attack_spray.png` (Vung tay xả mưa đạn Tím & Gọi máy bay ném bom)
> **Prompt (Kèm ảnh `Boss_idle1.png` làm tham chiếu):**
> ```text
> Keep the exact same character design, outfit, glasses, facing LEFT direction, and 2D game art style from the reference image. Change pose to a commanding barrage attack: he raises one hand high above his head summoning an airstrike while his other hand unleashes glowing neon purple and magenta energy orbs toward the left, cape flaring wide behind him. Isolated on a plain solid white background.
> ```

#### 🔹 Trạng thái 6: `Boss_hit.png` (Lảo đảo nhẹ khi trúng tên lửa)
> **Prompt (Kèm ảnh `Boss_idle1.png` làm tham chiếu):**
> ```text
> Keep the exact same character design, outfit, glasses, facing LEFT direction, and 2D game art style from the reference image. Change pose to a hit/damaged reaction: his upper body leans slightly backward to the right from taking a hit, one arm raised defensively across his chest, gritting teeth with a flash glint on his gold glasses and small yellow-orange impact sparks near his shoulder. Isolated on a plain solid white background.
> ```

---

### 4.3. Negative Prompt (Dành cho Stable Diffusion / Leonardo.ai)
Nếu công cụ tạo ảnh có ô **Negative Prompt (Những thứ không muốn xuất hiện)**, hãy dán đoạn này vào:
```text
facing right, front view facing camera, back view, cropped head, cropped feet, multiple characters in one frame, realistic photo, 3d render, blurry, messy background, text, watermark, signature, extra limbs, deformed hands, giant mecha, armor helmet.
```

---

## 5. QUY TRÌNH HẬU KỲ NHANH TRƯỚC KHI BỎ VÀO GAME

1. **Kiểm tra hướng quay mặt:** Đảm bảo Boss **luôn nhìn sang BÊN TRÁI (`←`)**. Nếu AI lỡ vẽ Boss nhìn sang bên phải (`→`), bạn chỉ cần dùng công cụ **Flip Horizontal (Lật ngang)** trong Photoshop / Canva / Photopea là xong.
2. **Xóa nền trắng (Remove Background):** Xóa sạch nền trắng để tạo file `.PNG` trong suốt hoàn toàn.
3. **Căn lề & Resize đồng bộ:**
   - Cắt sát mép nhân vật (giữ vị trí đầu và thân của cả 6 ảnh nằm cùng một trục tâm tương đối để khi chuyển trạng thái nhân vật không bị giật vị trí).
   - Xuất file ở kích thước **`140 × 190 px`** (hoặc **`210 × 285 px`**) và lưu vào thư mục `assets/objects/Boss/` với đúng 6 tên file trong bảng ở Mục 3.
