package sanbaka;

import java.util.List;
import sanbaka.assets.ArtAssets;
import sanbaka.logic.StoryData;
import sanbaka.model.*;
import sanbaka.state.GameMenuState;
import sanbaka.state.GameState;

public class SanbakaSmokeTest {
    public static void main(String[] args) {
        System.out.println("Running SanbakaSmokeTest with modular packages...");

        testStoryData();
        testSceneBuilding();
        testArtAssetsFallback();
        testEntityInstantiation();
        testGameplayMechanics();
        testDecoupledSurroundings();
        testMap2CurbSlowdownNoFire();
        testMap3DirectSteering();
        testMap4DeliriumSwayAtLowEnergy();
        testMap3WindBalance();
        testStrayDogAvoidsObstacles();
        testStrayDogRowDiversity();
        testBossFightLifecycleAndConfig();
        testPlayerMissileAndDestructibleEntities();
        testBombDetonationChainReaction();
        testMap4EnergyBoltBalance();

        System.out.println("[PASS] All SanbakaSmokeTest checks passed successfully!");
    }

    private static void testStoryData() {
        List<Line> lines = StoryData.build();
        if (lines == null || lines.isEmpty()) {
            throw new AssertionError("StoryData lines should not be empty!");
        }
        if (lines.size() < 50) {
            throw new AssertionError("Expected at least 50 story lines, got: " + lines.size());
        }

        boolean foundDay1 = false, foundDay5 = false;
        for (Line line : lines) {
            if (line.who.contains("NGÀY 1")) foundDay1 = true;
            if (line.who.contains("NGÀY 5")) foundDay5 = true;
        }

        if (!foundDay1 || !foundDay5) {
            throw new AssertionError("Story must contain both Day 1 and Day 5!");
        }
        System.out.println("  ✓ testStoryData passed (" + lines.size() + " lines)");
    }

    private static void testSceneBuilding() {
        GameMenuState menu = new GameMenuState() {
            @Override public void beginDay() {}
            @Override public void resumeRoad() {}
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {}
            @Override public void keyTyped(java.awt.event.KeyEvent e) {}
            @Override public void keyPressed(java.awt.event.KeyEvent e) {}
            @Override public void keyReleased(java.awt.event.KeyEvent e) {}
        };

        menu.lines.addAll(StoryData.build());
        menu.buildScenes();

        if (menu.scenes.size() != 11) {
            throw new AssertionError("Expected 11 scenes (5 room + 1 jam + 5 after), got: " + menu.scenes.size());
        }
        if (!menu.sceneUnlocked(0)) {
            throw new AssertionError("Scene 0 should be unlocked by default or under UNLOCK_ALL!");
        }
        System.out.println("  ✓ testSceneBuilding passed (" + menu.scenes.size() + " scenes)");
    }

    private static void testArtAssetsFallback() {
        java.awt.image.BufferedImage img = ArtAssets.load("NonExistent", "no_image.png");
        if (img != null) {
            throw new AssertionError("Expected null for non-existent image!");
        }
        System.out.println("  ✓ testArtAssetsFallback passed");
    }

    private static void testEntityInstantiation() {
        PlayerVehicle bike = new PlayerBike();
        if (bike.x <= 0 || bike.y <= 0) {
            throw new AssertionError("PlayerBike default position is invalid");
        }

        PlayerVehicle boat = new PlayerBoat();
        if (boat.x <= 0 || boat.y <= 0) {
            throw new AssertionError("PlayerBoat default position is invalid");
        }

        RoadCar car = new RoadCar();
        RoadMotorcycle moto = new RoadMotorcycle();
        RoadBoat roadBoat = new RoadBoat();
        EnergyBolt bolt = new EnergyBolt();
        SchoolBuilding school = new SchoolBuilding();
        HeavyTruck heavyTruck = new HeavyTruck();
        
        Thing thingCar = Thing.create(0);
        Thing thingMoto = Thing.create(6);
        Thing thingBolt = Thing.create(7);
        Thing thingBoat = Thing.create(8);
        Thing thingHeavyTruck = Thing.create(9);
        Thing thingBarrier = Thing.create(10);
        Thing thingPothole = Thing.create(11);
        Thing thingNinja = Thing.create(12);
        Thing thingShark = Thing.create(13);
        Thing thingIce = Thing.create(14);
        Thing thingDog = Thing.create(15);

        if (car == null || moto == null || roadBoat == null || bolt == null || school == null || heavyTruck == null) {
            throw new AssertionError("Entity instantiation failed");
        }
        if (thingCar == null || thingMoto == null || thingBolt == null || thingBoat == null
                || !(thingHeavyTruck instanceof HeavyTruck)
                || !(thingBarrier instanceof ConstructionBarrier)
                || !(thingPothole instanceof Pothole)
                || !(thingNinja instanceof NinjaLead)
                || !(thingShark instanceof Shark)
                || !(thingIce instanceof IcePatch)
                || !(thingDog instanceof StrayDog)) {
            throw new AssertionError("Thing factory creation failed");
        }
        System.out.println("  ✓ testEntityInstantiation passed");
    }

    private static void testGameplayMechanics() {
        sanbaka.logic.GameLogic logic = new sanbaka.logic.GameLogic();
        logic.day = 0;
        logic.beginDay();
        long t0 = logic.lastFrameAt;

        // 1. Kiểm tra đi thẳng thuận lợi (ở giữa đường): tốc độ tiến trình = 1.0x
        logic.player.x = logic.trackX(2);
        logic.things.clear();
        logic.updateRoad(t0 + 40);
        if (Math.abs(logic.currentProgressFactor - 1.0) > 1e-6 || Math.abs(logic.stageProgressMs - 40.0) > 1e-3) {
            throw new AssertionError("Unimpeded progress should advance at 1.0x!");
        }

        // 2. Kiểm tra đi sát lề đường quá 3 giây -> bốc lửa & đốt 10 HP/s; thoát lề -> hết dần
        logic.beginDay();
        long now = logic.lastFrameAt;
        for (int i = 0; i < 65; i++) {
            now += 50;
            logic.things.clear();
            logic.player.x = sanbaka.state.GameState.PLAYER_MIN_X;
            logic.updateRoad(now);
        }
        if (!logic.curbOnFire || logic.hp >= 100) {
            throw new AssertionError("Vehicle should catch fire and lose HP after >3s on curb!");
        }
        double heatBeforeExit = logic.curbEdgeTime;
        now += 50;
        logic.player.x = logic.trackX(2); // thoát lề đường ra giữa
        logic.things.clear();
        logic.updateRoad(now);
        if (logic.curbEdgeTime >= heatBeforeExit) {
            throw new AssertionError("Leaving curb should gradually reduce curbEdgeTime!");
        }

        // 3. Kiểm tra va chạm xe: nâng sát thương, đẩy lùi và làm chậm ngắn làm chậm tiến trình hoàn thành game
        logic.beginDay();
        now = logic.lastFrameAt;
        Thing car = Thing.create(0);
        car.kind = 0;
        car.w = sanbaka.state.GameState.VEH_W[0];
        car.h = sanbaka.state.GameState.VEH_H[0];
        car.x = car.baseX = logic.player.x;
        car.y = logic.player.y;
        logic.things.add(car);
        double yBefore = logic.player.y;
        now += 40;
        logic.updateRoad(now);
        if (logic.hp != 68 || logic.player.y <= yBefore || logic.hitSlowUntil <= now) {
            throw new AssertionError("Car collision should deal 32 dmg, knock back player, and apply short slowdown!");
        }
        double progBefore = logic.stageProgressMs;
        now += 40;
        logic.things.clear();
        logic.updateRoad(now);
        double progDelta = logic.stageProgressMs - progBefore;
        if (progDelta >= 40.0 || logic.currentProgressFactor >= 1.0) {
            throw new AssertionError("Slowdown effect must genuinely slow down stage completion progress!");
        }

        System.out.println("  ✓ testGameplayMechanics passed");
    }

    private static void testDecoupledSurroundings() {
        sanbaka.logic.GameLogic logic = new sanbaka.logic.GameLogic();
        logic.day = 0;
        logic.beginDay();
        long now = logic.lastFrameAt;
        logic.things.clear();
        logic.player.x = logic.trackX(2);

        // Frame normal:
        now += 40;
        double scrollBefore = logic.groundScroll;
        logic.updateRoad(now);
        double deltaNormal = logic.groundScroll - scrollBefore;

        // Force a slow condition (ví dụ va chạm bị làm chậm):
        logic.hitSlowUntil = now + 5000;
        logic.hitSlowFactor = 0.35;
        now += 40;
        scrollBefore = logic.groundScroll;
        logic.updateRoad(now);
        double deltaSlowed = logic.groundScroll - scrollBefore;

        // Tốc độ trôi cảnh vật xung quanh KHÔNG được chậm theo người chơi!
        if (Math.abs(deltaNormal - deltaSlowed) > 1e-4) {
            throw new AssertionError("Surroundings scroll delta must be decoupled from player slowdown! Normal: "
                    + deltaNormal + ", Slowed: " + deltaSlowed);
        }
        if (logic.currentProgressFactor >= 1.0) {
            throw new AssertionError("Stage completion progress factor should be slowed, but surroundings must not!");
        }
        System.out.println("  ✓ testDecoupledSurroundings passed");
    }

    private static void testMap2CurbSlowdownNoFire() {
        sanbaka.logic.GameLogic logic = new sanbaka.logic.GameLogic();
        logic.day = 1; // Map 2: đường ngập
        logic.beginDay();
        long now = logic.lastFrameAt;
        logic.things.clear();

        // Đi sát mép trong 4 giây (80 frame x 50ms)
        for (int i = 0; i < 80; i++) {
            now += 50;
            logic.things.clear();
            logic.player.x = sanbaka.state.GameState.PLAYER_MIN_X;
            logic.updateRoad(now);
        }

        // Bỏ bốc lửa hoàn toàn trên map 2:
        if (logic.curbOnFire) {
            throw new AssertionError("Map 2 must NOT catch fire when hugging curb!");
        }
        if (logic.curbEdgeTime != 0.0) {
            throw new AssertionError("Map 2 curbEdgeTime must remain 0.0!");
        }
        if (logic.hp != 100) {
            throw new AssertionError("Map 2 curb scraping must NOT deal fire burn damage! HP: " + logic.hp);
        }
        // Tốc độ phải giảm dần về 50%:
        if (Math.abs(logic.map2CurbSlowFactor - 0.50) > 0.05) {
            throw new AssertionError("Map 2 curb slow factor should approach 0.50, got: " + logic.map2CurbSlowFactor);
        }

        // Rời khỏi lề đường ra giữa:
        double slowBeforeExit = logic.map2CurbSlowFactor;
        for (int i = 0; i < 20; i++) {
            now += 50;
            logic.things.clear();
            logic.player.x = logic.trackX(2);
            logic.updateRoad(now);
        }
        if (logic.map2CurbSlowFactor <= slowBeforeExit || logic.map2CurbSlowFactor > 1.0) {
            throw new AssertionError("Steering away from curb on Map 2 must recover speed towards 1.0!");
        }
        System.out.println("  ✓ testMap2CurbSlowdownNoFire passed");
    }

    private static void testMap3DirectSteering() {
        sanbaka.logic.GameLogic logic = new sanbaka.logic.GameLogic();
        logic.day = sanbaka.state.GameState.COLD_DAY; // Map 3: lạnh giá
        logic.beginDay();
        long now = logic.lastFrameAt;
        logic.things.clear();
        logic.windDir = 0; // không có gió tạt ngang để test điều khiển thuần

        // Bình thường không dẫm băng: không có gia tốc ngang, điều khiển trực tiếp
        logic.player.x = logic.trackX(2);
        double startX = logic.player.x;
        logic.right = true;
        now += 40;
        logic.updateRoad(now);
        logic.right = false;

        double dt = 0.04;
        double expectedMove = sanbaka.state.GameState.PLAYER_SPEED * dt;
        if (Math.abs((logic.player.x - startX) - expectedMove) > 1.0) {
            throw new AssertionError("Map 3 normal steering must be direct without horizontal inertia! Moved: "
                    + (logic.player.x - startX) + ", expected: " + expectedMove);
        }
        if (logic.playerVx != 0) {
            throw new AssertionError("Map 3 normal driving should have playerVx = 0!");
        }

        // Khi trượt băng (iceSlipUntil > now): quán tính trượt ngang hoạt động
        logic.iceSlipUntil = now + 1000;
        logic.playerVx = 200.0;
        double xBeforeSlip = logic.player.x;
        now += 40;
        logic.updateRoad(now);
        if (logic.player.x <= xBeforeSlip || Math.abs(logic.playerVx) >= 200.0) {
            throw new AssertionError("Map 3 ice slipping must preserve inertia sliding and friction decay!");
        }
        System.out.println("  ✓ testMap3DirectSteering passed");
    }

    private static void testMap4DeliriumSwayAtLowEnergy() {
        sanbaka.logic.GameLogic logic = new sanbaka.logic.GameLogic();
        logic.day = sanbaka.state.GameState.HOT_DAY; // Map 4: Nắng nóng
        logic.beginDay();
        long now = logic.lastFrameAt;
        logic.things.clear();
        logic.player.x = logic.trackX(2);

        // Khi Energy = 100 (>= 50%): Không có ảo giác mê sảng, tiến độ bình thường
        logic.energy = 100.0;
        now += 40;
        logic.updateRoad(now);
        if (Math.abs(logic.currentProgressFactor - 1.0) > 1e-4) {
            throw new AssertionError("Map 4 at full energy should have 1.0x progress!");
        }

        // Khi Energy < 50% (ví dụ 30%): Hiệu ứng mệt mỏi say sóng kích hoạt
        logic.energy = 30.0;
        now += 40;
        logic.updateRoad(now);
        double expectedDelirium = (50.0 - 30.0) / 50.0; // 0.40
        double expectedFatigue = 0.60 + 0.40 * (1.0 - expectedDelirium); // 0.84
        if (Math.abs(logic.currentProgressFactor - expectedFatigue) > 0.05) {
            throw new AssertionError("Map 4 low energy should slow progress according to delirium! Factor: "
                    + logic.currentProgressFactor + ", expected ~" + expectedFatigue);
        }

        System.out.println("  ✓ testMap4DeliriumSwayAtLowEnergy passed");
    }

    private static void testMap3WindBalance() {
        sanbaka.logic.GameLogic logic = new sanbaka.logic.GameLogic();
        logic.day = sanbaka.state.GameState.COLD_DAY;
        logic.beginDay();
        long now = logic.lastFrameAt;
        logic.things.clear();

        // Giả lập gió đang thổi mạnh sang phải: windDir = 1
        logic.windDir = 1;
        logic.windWarningUntil = now;
        logic.windActiveUntil = now + 2200;

        // 1. Kiểm tra quyền kiểm soát tay lái: người chơi đánh lái sang trái vẫn di chuyển được sang trái!
        logic.player.x = logic.trackX(2);
        double startX = logic.player.x;
        logic.left = true;
        now += 40;
        logic.updateRoad(now);
        logic.left = false;

        if (logic.player.x >= startX) {
            throw new AssertionError("Player steering must have authority over wind! startX: " + startX
                    + ", currentX: " + logic.player.x);
        }

        // 2. Kiểm tra xe ở sát mép phải (gần lề đường): gió không được ép chết người chơi vào vỉa hè
        logic.player.x = sanbaka.state.GameState.PLAYER_MAX_X - 10;
        double nearEdgeX = logic.player.x;
        now += 40;
        logic.left = true; // cố gắng thoát ra khỏi vỉa hè
        logic.updateRoad(now);
        logic.left = false;
        if (logic.player.x >= nearEdgeX) {
            throw new AssertionError("Wind must not pin the player into the curb! nearEdgeX: " + nearEdgeX
                    + ", currentX: " + logic.player.x);
        }

        System.out.println("  ✓ testMap3WindBalance passed");
    }

    private static void testStrayDogAvoidsObstacles() {
        StrayDog dog = new StrayDog();
        dog.x = dog.baseX = 300;
        dog.y = 300;
        dog.dir = 1;
        dog.phase = StrayDog.PHASE_DASH_ACROSS;

        // Đặt một chiếc xe ô tô nằm chặn ngay trước mặt chó ở cùng độ cao Y = 300
        RoadCar car = new RoadCar();
        car.x = car.baseX = 360;
        car.y = 300;
        car.w = 66;
        car.h = 104;

        java.util.List<Thing> things = new java.util.ArrayList<>();
        things.add(dog);
        things.add(car);

        long now = System.currentTimeMillis();
        // Cập nhật di chuyển của chó trong 5 frame
        double initialY = dog.y;
        for (int i = 0; i < 5; i++) {
            now += 30;
            dog.updateDog(0.03, 0.0, now, sanbaka.state.GameState.ROAD_X, sanbaka.state.GameState.ROAD_W, things, 480, 500, 34, 60);
        }

        // Chó phải nhận diện được xe và đánh lái né dọc theo phương Y (tiến hoặc lùi)
        if (Math.abs(dog.y - initialY) < 1.0) {
            throw new AssertionError("Stray dog must dodge in Y (forward/backward) when encountering a vehicle ahead! Initial Y: "
                    + initialY + ", current Y: " + dog.y);
        }
        if (dog.targetDodgeY < 0) {
            throw new AssertionError("Dog must choose a target dodge Y when a car is in its path!");
        }

        // Kiểm tra chó KHÔNG né nhân vật chính (chó là chướng ngại vật thách thức người chơi, không được tự động né người chơi)
        StrayDog dogPlayerPath = new StrayDog();
        dogPlayerPath.x = dogPlayerPath.baseX = 300;
        dogPlayerPath.y = 300;
        dogPlayerPath.dir = 1;
        dogPlayerPath.phase = StrayDog.PHASE_DASH_ACROSS;
        java.util.List<Thing> thingsWithoutCars = new java.util.ArrayList<>();
        thingsWithoutCars.add(dogPlayerPath);
        double dogPlayerInitialY = dogPlayerPath.y;
        long nowPlayer = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            nowPlayer += 30;
            // Player nằm ngay phía trước mặt chó ở (360, 300)
            dogPlayerPath.updateDog(0.03, 0.0, nowPlayer, sanbaka.state.GameState.ROAD_X, sanbaka.state.GameState.ROAD_W,
                    thingsWithoutCars, 360, 300, 34, 60);
        }
        if (dogPlayerPath.targetDodgeY >= 0 || Math.abs(dogPlayerPath.y - dogPlayerInitialY) > 0.01) {
            throw new AssertionError("Stray dog must NOT dodge the player vehicle! Dog should keep charging ahead.");
        }

        System.out.println("  ✓ testStrayDogAvoidsObstacles passed");
    }

    private static void testStrayDogRowDiversity() {
        sanbaka.logic.GameLogic logic = new sanbaka.logic.GameLogic();
        logic.day = sanbaka.state.GameState.HOT_DAY;
        logic.beginDay();
        long now = logic.lastFrameAt;

        boolean spawnedFar = false;
        boolean spawnedNearPlayer = false;

        for (int i = 0; i < 50; i++) {
            logic.things.clear();
            logic.trySpawnStrayDog(now + i * 100);
            for (Thing t : logic.things) {
                if (t instanceof StrayDog dog) {
                    if (dog.y < 300) spawnedFar = true;
                    if (dog.y > 440) spawnedNearPlayer = true;
                }
            }
            if (spawnedFar && spawnedNearPlayer) break;
        }

        if (!spawnedFar || !spawnedNearPlayer) {
            throw new AssertionError("Stray dog must spawn across diverse rows including near the player's row (y > 440) and far (y < 300)!");
        }
        System.out.println("  ✓ testStrayDogRowDiversity passed");
    }

    private static void testBossFightLifecycleAndConfig() {
        sanbaka.state.BossConfig.load();
        if (sanbaka.state.GameState.FLIGHT_START_HP != 150) {
            throw new AssertionError("Helicopter start HP should be 150, got: " + sanbaka.state.GameState.FLIGHT_START_HP);
        }
        if (sanbaka.state.BossConfig.maxHp <= 0) {
            throw new AssertionError("Boss max HP should be positive, got: " + sanbaka.state.BossConfig.maxHp);
        }
        if (sanbaka.state.BossConfig.missileCooldownSec <= 0.0) {
            throw new AssertionError("Missile cooldown should be positive, got: " + sanbaka.state.BossConfig.missileCooldownSec);
        }
        if (sanbaka.state.BossConfig.enemyPlaneHp <= 0 || sanbaka.state.BossConfig.enemyBombHp <= 0) {
            throw new AssertionError("Plane HP and Bomb HP must be positive!");
        }
        if (sanbaka.state.BossConfig.enemyPlaneWidth < 70.0 || sanbaka.state.BossConfig.enemyPlaneHeight < 35.0) {
            throw new AssertionError("Fighter plane must be larger than bird and sized near helicopter!");
        }
        if (sanbaka.state.BossConfig.damageBossBullet > 20 || sanbaka.state.BossConfig.damagePlaneBullet > 18
                || sanbaka.state.BossConfig.damageBombBlast > 25 || sanbaka.state.BossConfig.damageBirdCollision > 25) {
            throw new AssertionError("Helicopter damage must be reduced by 40%!");
        }

        sanbaka.logic.GameLogic logic = new sanbaka.logic.GameLogic();
        long now = 100000L;
        BossEnemy boss = new BossEnemy();
        boss.triggerBossSequence(now);

        if (boss.state != BossEnemy.STATE_WARNING) {
            throw new AssertionError("Boss initial state after trigger must be STATE_WARNING!");
        }
        if (boss.canPlayerFire(now)) {
            throw new AssertionError("Player must NOT be able to fire missile during warning!");
        }

        // Sau thời gian cảnh báo -> boss di chuyển vào
        now += sanbaka.state.BossConfig.warningFlashMs + 50;
        boss.update(0.05, now, 170, 300, logic);
        if (boss.state != BossEnemy.STATE_ENTERING) {
            throw new AssertionError("Boss state should transition to STATE_ENTERING, got: " + boss.state);
        }

        // Cho Boss lướt vào vị trí chiến đấu
        boss.x = 830.0;
        boss.update(0.05, now, 170, 300, logic);
        if (boss.state != BossEnemy.STATE_CHARGING) {
            throw new AssertionError("Boss state should transition to STATE_CHARGING, got: " + boss.state);
        }
        if (boss.canPlayerFire(now)) {
            throw new AssertionError("Player must NOT be able to fire missile while boss is charging HP!");
        }

        // Sau khi nạp máu xong -> vào Đòn 1
        now += sanbaka.state.BossConfig.introChargeMs + 50;
        boss.update(0.05, now, 170, 300, logic);
        if (boss.state != BossEnemy.STATE_ATTACK1) {
            throw new AssertionError("Boss state should transition to STATE_ATTACK1, got: " + boss.state);
        }
        if (!boss.canPlayerFire(now)) {
            throw new AssertionError("Player MUST be able to fire missile during active combat!");
        }
        if (boss.currentHp != sanbaka.state.BossConfig.maxHp) {
            throw new AssertionError("Boss HP should be fully charged to " + sanbaka.state.BossConfig.maxHp + ", got: " + boss.currentHp);
        }

        // Bị bắn trúng 5 phát tên lửa (mỗi phát trừ 1 HP)
        for (int i = 0; i < 5; i++) {
            boss.takeMissileHit(1, now);
        }
        if (boss.currentHp != sanbaka.state.BossConfig.maxHp - 5) {
            throw new AssertionError("Boss HP after 5 hits should be " + (sanbaka.state.BossConfig.maxHp - 5) + ", got: " + boss.currentHp);
        }

        // Chuyển sang Đòn 1 -> Pha chuyển giao (bắn loạn xạ & rút lui)
        now += sanbaka.state.BossConfig.attack1DurationMs + 50;
        boss.update(0.05, now, 170, 300, logic);
        if (boss.state != BossEnemy.STATE_TRANSITION_SPRAY) {
            throw new AssertionError("Boss state should transition to STATE_TRANSITION_SPRAY, got: " + boss.state);
        }

        // Hết chuyển giao -> Đòn 2 (Bão chim)
        now += sanbaka.state.BossConfig.transitionSprayDurationMs + 50;
        boss.update(0.05, now, 170, 300, logic);
        if (boss.state != BossEnemy.STATE_ATTACK2) {
            throw new AssertionError("Boss state should transition to STATE_ATTACK2, got: " + boss.state);
        }

        // Hết Đòn 2 -> Pha trở lại
        now += sanbaka.state.BossConfig.attack2DurationMs + 50;
        boss.update(0.05, now, 170, 300, logic);
        if (boss.state != BossEnemy.STATE_TRANSITION_RETURN) {
            throw new AssertionError("Boss state should transition to STATE_TRANSITION_RETURN, got: " + boss.state);
        }

        // Trở lại xong -> Đòn 3 (Không kích rải bom chéo)
        boss.x = 830.0;
        now += sanbaka.state.BossConfig.attack3IntroSprayMs + 50;
        boss.update(0.05, now, 170, 300, logic);
        if (boss.state != BossEnemy.STATE_ATTACK3) {
            throw new AssertionError("Boss state should transition to STATE_ATTACK3, got: " + boss.state);
        }

        // Hết Đòn 3 -> Tuần hoàn quay trở lại Đòn 1
        now += sanbaka.state.BossConfig.attack3DurationMs + 50;
        boss.update(0.05, now, 170, 300, logic);
        if (boss.state != BossEnemy.STATE_ATTACK1) {
            throw new AssertionError("Boss cycle should loop back to STATE_ATTACK1, got: " + boss.state);
        }

        // Bắn nốt số phát còn lại -> Boss bị hạ gục
        boolean killed = false;
        int remainingHits = boss.currentHp;
        for (int i = 0; i < remainingHits; i++) {
            killed = boss.takeMissileHit(1, now);
        }
        if (!killed || !boss.defeated || boss.state != BossEnemy.STATE_DEFEATED) {
            throw new AssertionError("Boss should be defeated when HP drops to 0!");
        }

        System.out.println("  ✓ testBossFightLifecycleAndConfig passed");
    }

    private static void testPlayerMissileAndDestructibleEntities() {
        sanbaka.logic.GameLogic logic = new sanbaka.logic.GameLogic();
        long now = 200000L;
        logic.startFlight(now);

        // Giai đoạn đầu màn 5 lượt 2 kéo dài 15 giây là đàn chim, Boss chưa xuất hiện
        if (logic.boss != null) {
            throw new AssertionError("Boss must NOT appear at the very start of helicopter flight!");
        }

        // Cập nhật trong 15s đầu khi nhịp chim đến -> chim xuất hiện
        logic.lastBird = now - 1000;
        logic.lastFrameAt = now;
        logic.updateFlight(now + 50);
        if (logic.birds.isEmpty()) {
            throw new AssertionError("Birds must spawn during the initial 15-second flight phase!");
        }

        // Sau 15s chim và khi chim đã hết -> Boss mới xuất hiện
        long after15s = now + 15200;
        logic.birds.clear();
        logic.updateFlight(after15s);
        if (logic.boss == null || !logic.flightBossTriggered) {
            throw new AssertionError("Boss must appear after the 15-second bird phase ends!");
        }

        logic.boss.state = BossEnemy.STATE_ATTACK1;
        logic.boss.combatActive = true;

        // Bắn tên lửa lần 1
        logic.firePlayerMissile(after15s);
        if (logic.missiles.size() != 1) {
            throw new AssertionError("Expected 1 missile fired, got: " + logic.missiles.size());
        }
        PlayerMissile m1 = logic.missiles.get(0);
        if (Math.abs(m1.x - (logic.heliX + 28)) > 1.0 || Math.abs(m1.y - (logic.heliY + 22)) > 1.0) {
            throw new AssertionError("Missile must originate from landing gear/skids: (" + (logic.heliX + 28) + ", " + (logic.heliY + 22) + ")");
        }

        // Bắn ngay lập tức (nửa thời gian hồi chiêu) -> bị chặn bởi hồi chiêu
        long halfCd = (long) (sanbaka.state.BossConfig.missileCooldownSec * 1000 / 2);
        logic.firePlayerMissile(after15s + Math.max(10, halfCd));
        if (logic.missiles.size() != 1) {
            throw new AssertionError("Missile cooldown failed to block rapid fire!");
        }

        // Bắn sau khi hết hồi chiêu -> bắn thành công phát thứ 2
        long afterCd = (long) (sanbaka.state.BossConfig.missileCooldownSec * 1000) + 60;
        logic.firePlayerMissile(after15s + afterCd);
        if (logic.missiles.size() != 2) {
            throw new AssertionError("Expected 2 missiles after cooldown expired, got: " + logic.missiles.size());
        }

        // Kiểm tra độ bền & kích thước Máy bay tiêm kích địch (cần 3 phát đạn, kích thước tiêm kích lớn tương xứng)
        EnemyPlane plane = new EnemyPlane(500, 300, 350, 3, false);
        if (plane.w < 80.0 || plane.h < 40.0) {
            throw new AssertionError("Fighter plane width and height must match fighter jet specs (>= 80x40), got: " + plane.w + "x" + plane.h);
        }
        if (plane.takeHit(1, after15s)) throw new AssertionError("Plane should not die in 1 hit!");
        if (plane.hp != 2) throw new AssertionError("Plane HP should be 2, got: " + plane.hp);
        if (plane.takeHit(1, after15s)) throw new AssertionError("Plane should not die in 2 hits!");
        if (plane.hp != 1) throw new AssertionError("Plane HP should be 1, got: " + plane.hp);
        if (!plane.takeHit(1, after15s)) throw new AssertionError("Plane MUST die on 3rd hit!");
        if (plane.active) throw new AssertionError("Destroyed plane must be inactive!");

        // Kiểm tra độ bền Chim (1 phát)
        Bird bird = new Bird(500, 250, -300, 0);
        logic.birds.clear();
        logic.birds.add(bird);
        logic.missiles.clear();
        logic.missiles.add(new PlayerMissile(500, 250, 680));
        logic.lastFrameAt = after15s;
        logic.updateFlight(after15s + 16);
        if (logic.birds.contains(bird)) {
            throw new AssertionError("Bird must be destroyed upon 1 missile hit!");
        }

        System.out.println("  ✓ testPlayerMissileAndDestructibleEntities passed");
    }

    private static void testBombDetonationChainReaction() {
        sanbaka.logic.GameLogic logic = new sanbaka.logic.GameLogic();
        long now = 300000L;
        logic.startFlight(now);

        // Tạo 2 quả bom lân cận kích thước to và 1 con chim nằm trong bán kính nổ
        EnemyBomb b1 = new EnemyBomb(400, 300, -165, 240, 76.0);
        EnemyBomb b2 = new EnemyBomb(440, 300, -165, 240, 76.0); // cách 40px < bán kính nổ 76px
        if (b1.w < 36.0 || b1.h < 36.0) {
            throw new AssertionError("Bomb size must be increased (>= 36x36), got: " + b1.w + "x" + b1.h);
        }
        Bird bird = new Bird(420, 300, -300, 0);

        logic.enemyBombs.clear();
        logic.enemyBombs.add(b1);
        logic.enemyBombs.add(b2);
        logic.birds.clear();
        logic.birds.add(bird);

        // Kích nổ quả bom b1
        logic.detonateBomb(400, 300, now);

        // Con chim trong bán kính nổ phải bị tiêu diệt
        if (logic.birds.contains(bird)) {
            throw new AssertionError("Bird inside bomb blast radius must be eliminated!");
        }

        // Quả bom b2 trong bán kính nổ phải bị kích nổ dây chuyền (active == false)
        if (b2.active) {
            throw new AssertionError("Adjacent bomb inside blast radius must be detonated by chain reaction!");
        }
        if (logic.explosions.isEmpty()) {
            throw new AssertionError("Detonating bomb must spawn visual blast radius explosion!");
        }

        System.out.println("  ✓ testBombDetonationChainReaction passed");
    }

    private static void testMap4EnergyBoltBalance() {
        sanbaka.logic.GameLogic logic = new sanbaka.logic.GameLogic();
        logic.day = sanbaka.state.GameState.HOT_DAY;
        logic.beginDay();
        long start = logic.stageStarted;
        int boltCount = 0;

        // Giả lập 35 giây màn 4 (thời gian spawn hiệu lực)
        long current = start;
        for (int step = 0; step < 350; step++) {
            current += 100;
            logic.things.removeIf(t -> t.y > 600);
            for (Thing t : logic.things) {
                t.y += 20;
            }
            double dur = sanbaka.state.GameState.ROAD_DURATION_MS / 1000.0;
            double t = (current - start) / 1000.0;
            boolean canSpawn = dur - t > 5.5;
            if (canSpawn && current - logic.lastBolt > 2700 + logic.rng.nextInt(500)) {
                if (logic.trySpawnBolt()) {
                    logic.lastBolt = current;
                    boltCount++;
                }
            }
        }

        if (boltCount < 10 || boltCount > 14) {
            throw new AssertionError("Stage 4 must spawn around 11-13 energy bolts (approx +4 bolts), got: " + boltCount);
        }
        System.out.println("  ✓ testMap4EnergyBoltBalance passed (" + boltCount + " bolts spawned across stage)");
    }
}
