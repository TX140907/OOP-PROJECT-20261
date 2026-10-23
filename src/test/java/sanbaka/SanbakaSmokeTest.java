package sanbaka;

import java.util.*;
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
        testMap4PacingAndDuration();
        testShieldItemAbsorbsCollision();
        testGingerTeaCuresFreezeAndHeals();
        testMap3WindDurationAndFreeze70();
        testFloatingTextParserAndDisplay();
        testItemSpawnCounts();
        testMap3MotorcycleDensityAndVelocity();
        testGpuHardwareAccelerationFlags();
        testStudentCrossingMechanics();

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

        // Kiểm tra cơ chế giữ phím E: tên lửa tự động bắn ngay lúc vừa hồi chiêu
        logic.keyE = true;
        int missilesBeforeHoldingE = logic.missiles.size();
        // Khi chưa hết hồi chiêu -> không bắn
        logic.updateFlight(after15s + afterCd + 10);
        if (logic.missiles.size() != missilesBeforeHoldingE) {
            throw new AssertionError("Holding E must not fire before cooldown expires!");
        }
        // Khi vừa hết hồi chiêu -> tự động bắn ngay
        long nextReadyTime = after15s + afterCd + (long)(sanbaka.state.BossConfig.missileCooldownSec * 1000) + 20;
        logic.updateFlight(nextReadyTime);
        if (logic.missiles.size() != missilesBeforeHoldingE + 1) {
            throw new AssertionError("Holding E must automatically fire missile immediately upon cooldown expiration!");
        }
        // Nhả phím E -> dừng bắn tự động
        logic.keyE = false;
        long afterReleaseTime = nextReadyTime + (long)(sanbaka.state.BossConfig.missileCooldownSec * 1000) + 50;
        logic.updateFlight(afterReleaseTime);
        if (logic.missiles.size() != missilesBeforeHoldingE + 1) {
            throw new AssertionError("Releasing key E must stop automatic continuous missile firing!");
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
            double dur = logic.getStageDurationMs() / 1000.0;
            double t = (current - start) / 1000.0;
            boolean canSpawn = dur - t > 5.5;
            if (canSpawn && current - logic.lastBolt > 1500 + logic.rng.nextInt(350)) {
                if (logic.trySpawnBolt()) {
                    logic.lastBolt = current;
                    boltCount++;
                }
            }
        }

        if (boltCount < 15 || boltCount > 21) {
            throw new AssertionError("Stage 4 must spawn around 16-19 energy bolts (increased frequency), got: " + boltCount);
        }

        // Kiểm tra cơ chế ăn tia sét: +27 Năng lượng và +5 Máu (HP)
        logic.energy = 50.0;
        logic.hp = 80;
        EnergyBolt bolt = new EnergyBolt();
        bolt.x = logic.player.x;
        bolt.y = logic.player.y;
        bolt.snack = true;
        logic.things.clear();
        logic.things.add(bolt);
        logic.lastFrameAt = current;
        logic.updateRoad(current + 16);
        if (logic.energy < 76.5) { // 50 + 27 = 77 (trừ chút ít do energy drain trong 1 frame)
            throw new AssertionError("Collecting EnergyBolt must restore +27 energy, got: " + logic.energy);
        }
        if (logic.hp != 85) { // 80 + 5 = 85
            throw new AssertionError("Collecting EnergyBolt must heal +5 HP, got: " + logic.hp);
        }

        System.out.println("  ✓ testMap4EnergyBoltBalance passed (" + boltCount + " bolts spawned, +27 Energy & +5 HP verified)");
    }

    private static void testMap4PacingAndDuration() {
        sanbaka.logic.GameLogic logic = new sanbaka.logic.GameLogic();
        // Kiểm tra thời lượng Màn 4 = 35 giây, các màn khác = 40 giây
        logic.day = sanbaka.state.GameState.HOT_DAY;
        if (logic.getStageDurationMs() != 35_000) {
            throw new AssertionError("Stage 4 duration must be 35,000 ms (35 seconds), got: " + logic.getStageDurationMs());
        }

        for (int d = 0; d < 5; d++) {
            if (d == sanbaka.state.GameState.HOT_DAY) continue;
            logic.day = d;
            if (logic.getStageDurationMs() != 40_000) {
                throw new AssertionError("Stage " + (d + 1) + " duration must be 40,000 ms, got: " + logic.getStageDurationMs());
            }
        }

        // Kiểm tra tốc độ chó chạy rông tăng 1.25 lần
        if (Math.abs(StrayDog.DASH_SPEED - 343.75) > 0.01) {
            throw new AssertionError("Stray dog dash speed must be 343.75 px/s (275 * 1.25), got: " + StrayDog.DASH_SPEED);
        }

        System.out.println("  ✓ testMap4PacingAndDuration passed");
    }

    private static void testShieldItemAbsorbsCollision() {
        sanbaka.logic.GameLogic logic = new sanbaka.logic.GameLogic();
        logic.day = sanbaka.state.GameState.HOT_DAY;
        logic.beginDay();
        long now = logic.lastFrameAt;

        if (logic.shieldActive) {
            throw new AssertionError("Shield must be inactive initially!");
        }

        // Nhặt vật phẩm khiên chắn (ShieldItem)
        ShieldItem shield = new ShieldItem();
        shield.x = logic.player.x;
        shield.y = logic.player.y;
        logic.things.clear();
        logic.things.add(shield);
        now += 16;
        logic.updateRoad(now);

        if (!logic.shieldActive) {
            throw new AssertionError("Shield must be active after collecting ShieldItem!");
        }
        if (!logic.things.isEmpty()) {
            throw new AssertionError("ShieldItem must be consumed after pickup!");
        }

        // Thử va chạm với ô tô chướng ngại vật
        logic.player.invulnerable = 0;
        int preHp = logic.hp;
        RoadCar car = new RoadCar();
        car.x = logic.player.x;
        car.y = logic.player.y;
        logic.things.add(car);
        now += 16;
        logic.updateRoad(now);

        if (logic.shieldActive) {
            throw new AssertionError("Shield must break after absorbing 1 collision!");
        }
        if (logic.hp != preHp) {
            throw new AssertionError("Shield must completely absorb damage (HP unchanged)! Expected: " + preHp + ", got: " + logic.hp);
        }
        if (logic.knockbackVx != 0 || logic.knockbackVy != 0) {
            throw new AssertionError("Shield must negate knockback! Vx: " + logic.knockbackVx + ", Vy: " + logic.knockbackVy);
        }
        if (now < logic.hitSlowUntil) {
            throw new AssertionError("Shield must negate hit slow effect!");
        }

        // Va chạm lần thứ hai khi không còn khiên: phải mất máu bình thường
        logic.player.invulnerable = 0;
        RoadCar car2 = new RoadCar();
        car2.x = logic.player.x;
        car2.y = logic.player.y;
        logic.things.clear();
        logic.things.add(car2);
        now += 16;
        logic.updateRoad(now);

        if (logic.hp >= preHp) {
            throw new AssertionError("Subsequent hit without shield must inflict damage! hp: " + logic.hp);
        }

        System.out.println("  ✓ testShieldItemAbsorbsCollision passed");
    }

    private static void testGingerTeaCuresFreezeAndHeals() {
        sanbaka.logic.GameLogic logic = new sanbaka.logic.GameLogic();
        logic.day = sanbaka.state.GameState.COLD_DAY;
        logic.beginDay();
        long now = logic.lastFrameAt;

        // Giả lập trạng thái bị đóng băng và máu thấp
        logic.frozenUntil = now + 5000;
        logic.hp = 60;

        // Sinh vật phẩm Trà gừng (GingerTea) tại vị trí người chơi
        GingerTea tea = new GingerTea();
        tea.x = logic.player.x;
        tea.y = logic.player.y;
        logic.things.clear();
        logic.things.add(tea);
        now += 16;
        logic.updateRoad(now);

        if (logic.frozenUntil != 0) {
            throw new AssertionError("Ginger tea must immediately cure freeze effect (frozenUntil == 0)!");
        }
        if (logic.warmUntil <= now) {
            throw new AssertionError("Ginger tea must grant warm effect (warmUntil > now)!");
        }
        if (logic.hp != 75) { // 60 + 15 = 75
            throw new AssertionError("Ginger tea must heal +15 HP! Expected 75, got: " + logic.hp);
        }

        // Khi đang có hiệu ứng giữ ấm (warmUntil), luồng gió lạnh thổi qua không thể làm đóng băng
        logic.windDir = 1;
        logic.windWarningUntil = now;
        logic.windActiveUntil = now + 3000;
        logic.windFreezeChecked = false;
        now += 20;
        logic.updateRoad(now);

        if (logic.frozenUntil > now) {
            throw new AssertionError("Player with ginger tea warm buff must be immune to wind freeze!");
        }

        System.out.println("  ✓ testGingerTeaCuresFreezeAndHeals passed");
    }

    private static void testMap3WindDurationAndFreeze70() {
        sanbaka.logic.GameLogic logic = new sanbaka.logic.GameLogic();
        logic.day = sanbaka.state.GameState.COLD_DAY;
        logic.beginDay();
        long now = logic.lastFrameAt;

        // 1. Kiểm tra thời gian gió thổi tăng thêm 1 giây (active duration = 3000ms)
        logic.windDir = 0;
        logic.nextWindAt = now;
        now += 16;
        logic.updateRoad(now);

        if (logic.windDir != 0) {
            long activeDuration = logic.windActiveUntil - logic.windWarningUntil;
            if (activeDuration != 3000) {
                throw new AssertionError("Wind active duration must be 3000ms (+1s), got: " + activeDuration);
            }
        }

        // 2. Kiểm tra hiệu ứng đóng băng làm tốc độ tiến giảm về 70%
        logic.frozenUntil = now + 4000;
        now += 16;
        logic.updateRoad(now);

        if (Math.abs(logic.currentProgressFactor - 0.70) > 0.001) {
            throw new AssertionError("Freeze progress factor must be exactly 0.70 (70%), got: " + logic.currentProgressFactor);
        }

        System.out.println("  ✓ testMap3WindDurationAndFreeze70 passed");
    }

    private static void testFloatingTextParserAndDisplay() {
        sanbaka.render.FloatingTextManager ftm = new sanbaka.render.FloatingTextManager();

        Map<String, List<String>> harmful = ftm.getHarmfulMap();
        Map<String, List<String>> beneficial = ftm.getBeneficialMap();

        if (harmful == null || harmful.isEmpty() || beneficial == null || beneficial.isEmpty()) {
            throw new AssertionError("Harmful and beneficial text categories must be loaded!");
        }

        // Kiểm tra các tình huống con bắt buộc
        String[] requiredHarmful = {"collision", "curb_fire", "ice_slip", "freeze", "pothole", "dog_bark", "low_energy", "shark", "boss_hit", "shield_break"};
        for (String req : requiredHarmful) {
            if (!harmful.containsKey(req) || harmful.get(req).isEmpty()) {
                throw new AssertionError("Missing harmful subcategory: " + req);
            }
        }

        String[] requiredBeneficial = {"energy_bolt", "ginger_tea", "shield_pickup", "shield_block", "heal", "missile_hit", "speed_up"};
        for (String req : requiredBeneficial) {
            if (!beneficial.containsKey(req) || beneficial.get(req).isEmpty()) {
                throw new AssertionError("Missing beneficial subcategory: " + req);
            }
        }

        // Kiểm tra tất cả câu chữ phải là CHỮ IN HOA
        for (Map.Entry<String, List<String>> e : harmful.entrySet()) {
            for (String s : e.getValue()) {
                if (!s.equals(s.toUpperCase())) {
                    throw new AssertionError("Harmful phrase must be UPPERCASE: " + s);
                }
            }
        }
        for (Map.Entry<String, List<String>> e : beneficial.entrySet()) {
            for (String s : e.getValue()) {
                if (!s.equals(s.toUpperCase())) {
                    throw new AssertionError("Beneficial phrase must be UPPERCASE: " + s);
                }
            }
        }

        // Kiểm tra spawn chữ nổi cạnh nhân vật
        ftm.clear();
        ftm.spawn(300, 400, "ginger_tea", true);
        ftm.spawn(300, 400, "collision", false);

        if (ftm.getActiveCount() != 2) {
            throw new AssertionError("Expected 2 active floating texts, got: " + ftm.getActiveCount());
        }

        // Kiểm tra hàm draw không ném lỗi
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(960, 640, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        ftm.draw(g, System.currentTimeMillis() + 200);
        g.dispose();

        System.out.println("  ✓ testFloatingTextParserAndDisplay passed");
    }

    private static void testItemSpawnCounts() {
        // Map 4: Kiểm tra spawn đúng 3 khiên trên toàn map
        sanbaka.logic.GameLogic logic4 = new sanbaka.logic.GameLogic();
        logic4.day = sanbaka.state.GameState.HOT_DAY;
        logic4.beginDay();
        long start4 = logic4.stageStarted;
        int shieldsSpawned = 0;
        for (long t = 0; t <= 35_000; t += 100) {
            long now = start4 + t;
            double dur = logic4.getStageDurationMs() / 1000.0;
            double sec = t / 1000.0;
            boolean canSpawn = dur - sec > 5.5;
            if (canSpawn && logic4.shieldSpawnCount < 3 && now - logic4.lastShieldSpawn > 8000 + logic4.rng.nextInt(1500)) {
                if (logic4.trySpawnShield()) {
                    logic4.lastShieldSpawn = now;
                    shieldsSpawned++;
                }
            }
        }
        if (shieldsSpawned != 3) {
            throw new AssertionError("Stage 4 must spawn exactly 3 shields, got: " + shieldsSpawned);
        }

        // Map 3: Kiểm tra spawn đúng 4 trà gừng trên toàn map
        sanbaka.logic.GameLogic logic3 = new sanbaka.logic.GameLogic();
        logic3.day = sanbaka.state.GameState.COLD_DAY;
        logic3.beginDay();
        long start3 = logic3.stageStarted;
        int teasSpawned = 0;
        for (long t = 0; t <= 40_000; t += 100) {
            long now = start3 + t;
            double dur = logic3.getStageDurationMs() / 1000.0;
            double sec = t / 1000.0;
            boolean canSpawn = dur - sec > 5.5;
            if (canSpawn && logic3.teaSpawnCount < 4 && now - logic3.lastTeaSpawn > 7500 + logic3.rng.nextInt(1500)) {
                if (logic3.trySpawnGingerTea()) {
                    logic3.lastTeaSpawn = now;
                    teasSpawned++;
                }
            }
        }
        if (teasSpawned != 4) {
            throw new AssertionError("Stage 3 must spawn exactly 4 ginger teas, got: " + teasSpawned);
        }

        System.out.println("  ✓ testItemSpawnCounts passed (3 shields in map 4, 4 ginger teas in map 3)");
    }

    private static void testMap3MotorcycleDensityAndVelocity() {
        sanbaka.logic.GameLogic logic = new sanbaka.logic.GameLogic();
        logic.day = sanbaka.state.GameState.COLD_DAY;
        logic.beginDay();

        int motoCountCold = 0;
        int totalCold = 600;
        double totalColdFwd = 0;
        for (int i = 0; i < totalCold; i++) {
            logic.things.clear();
            if (logic.trySpawnVehicle()) {
                for (Thing t : logic.things) {
                    if (t.kind == 6 || t.kind == 12) {
                        motoCountCold++;
                        totalColdFwd += t.fwd;
                    }
                }
            }
        }

        double avgColdFwd = totalColdFwd / motoCountCold;
        // Kiểm tra vận tốc fwd xe máy ở Màn 3 tăng 1.5 lần (~0.48 - 0.63, trung bình ~0.55 so với 0.37 thông thường)
        if (avgColdFwd < 0.45) {
            throw new AssertionError("Stage 3 motorcycle forward speed must be increased 1.5x (avg > 0.45), got: " + avgColdFwd);
        }

        // So sánh mật độ với ngày bình thường (Day 0)
        sanbaka.logic.GameLogic logic0 = new sanbaka.logic.GameLogic();
        logic0.day = 0;
        logic0.beginDay();
        int motoCountDay0 = 0;
        for (int i = 0; i < totalCold; i++) {
            logic0.things.clear();
            if (logic0.trySpawnVehicle()) {
                for (Thing t : logic0.things) {
                    if (t.kind == 6 || t.kind == 12) {
                        motoCountDay0++;
                    }
                }
            }
        }

        double ratio = (double) motoCountCold / Math.max(1, motoCountDay0);
        if (ratio < 1.30) {
            throw new AssertionError("Stage 3 motorcycle density must be around 1.5x higher than normal day, ratio: " + ratio);
        }

        System.out.println("  ✓ testMap3MotorcycleDensityAndVelocity passed (Speed: " + String.format("%.2f", avgColdFwd) + ", Density ratio: " + String.format("%.2f", ratio) + "x)");
    }

    private static void testGpuHardwareAccelerationFlags() {
        try {
            Class.forName("sanbaka.SanbakaGame");
        } catch (ClassNotFoundException e) {
            throw new AssertionError("SanbakaGame class not found", e);
        }
        if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
            String d3d = System.getProperty("sun.java2d.d3d");
            String transaccel = System.getProperty("sun.java2d.transaccel");
            String ddforcevram = System.getProperty("sun.java2d.ddforcevram");
            if (!"true".equalsIgnoreCase(d3d)) {
                throw new AssertionError("GPU Direct3D acceleration flag sun.java2d.d3d must be true on Windows, got: " + d3d);
            }
            if (!"true".equalsIgnoreCase(transaccel)) {
                throw new AssertionError("GPU translucent acceleration flag sun.java2d.transaccel must be true on Windows, got: " + transaccel);
            }
            if (!"true".equalsIgnoreCase(ddforcevram)) {
                throw new AssertionError("GPU VRAM allocation flag sun.java2d.ddforcevram must be true on Windows, got: " + ddforcevram);
            }
        }
        System.out.println("  ✓ testGpuHardwareAccelerationFlags passed (Direct3D & VRAM acceleration enabled)");
    }

    private static void testStudentCrossingMechanics() {
        Thing t = Thing.create(18);
        if (!(t instanceof Student)) {
            throw new AssertionError("Thing.create(18) should produce an instance of Student, got: " + t.getClass());
        }
        Student s = (Student) t;
        if (s.w != 32.0 || s.h != 38.0) {
            throw new AssertionError("Student dimensions should be 32x38, got: " + s.w + "x" + s.h);
        }

        List<Thing> things = new ArrayList<>();
        RoadCar car = (RoadCar) Thing.create(0);
        car.x = car.baseX = 350.0;
        car.y = 200.0;
        car.w = 50.0;
        car.h = 86.0;
        car.wobbleAmp = 0;
        things.add(car);

        s.x = 280.0;
        s.y = 200.0;
        s.dir = 1;
        s.phase = Student.PHASE_CROSSING;

        s.updateStudent(0.016, 260.0, System.currentTimeMillis(), 220, 520, things, 0, 0, 0, 0);
        if (!s.isYielding) {
            throw new AssertionError("Student must yield when approaching a car in path X with vertical overlap!");
        }
        if (s.x + s.w / 2.0 > car.x - car.w / 2.0) {
            throw new AssertionError("Student must not penetrate into the car body!");
        }

        RoadMotorcycle moto = (RoadMotorcycle) Thing.create(6);
        moto.baseX = 450.0;
        moto.x = 450.0;
        moto.y = 200.0;
        moto.w = 26.0;
        moto.h = 60.0;
        moto.wobbleAmp = 25.0;
        things.clear();
        things.add(moto);

        s.x = 360.0;
        s.y = 200.0;
        s.dir = 1;
        s.updateStudent(0.016, 260.0, System.currentTimeMillis(), 220, 520, things, 0, 0, 0, 0);
        if (!s.isYielding) {
            throw new AssertionError("Student must yield before entering the swept width of a wobbling motorcycle!");
        }
        double motoDangerMinX = moto.baseX - moto.w / 2.0 - moto.wobbleAmp - 18.0;
        if (s.x + s.w / 2.0 > motoDangerMinX) {
            throw new AssertionError("Student must remain strictly outside the wobbling danger zone!");
        }

        sanbaka.logic.GameLogic logic = new sanbaka.logic.GameLogic();
        logic.day = 0;
        logic.beginDay();
        int initialHp = logic.hp;
        Student crossingStudent = (Student) Thing.create(18);
        crossingStudent.x = logic.player.x;
        crossingStudent.y = logic.player.y;
        crossingStudent.phase = Student.PHASE_CROSSING;
        logic.things.add(crossingStudent);

        int activeTextBefore = logic.floatingTextManager.getActiveCount();
        logic.updateRoad(System.currentTimeMillis() + 100);

        if (logic.hp >= initialHp) {
            throw new AssertionError("Player must receive collision damage when hitting a student, initialHp: " + initialHp + ", current: " + logic.hp);
        }
        if (logic.floatingTextManager.getActiveCount() > activeTextBefore) {
            throw new AssertionError("No floating text should be spawned for student collision, got count: " + logic.floatingTextManager.getActiveCount());
        }
        if (!logic.things.contains(crossingStudent)) {
            throw new AssertionError("Student must continue crossing the street after collision with player!");
        }

        logic.day = 1;
        if (logic.trySpawnStudent(System.currentTimeMillis())) {
            throw new AssertionError("Student must not spawn in Day 1 (flooded day)!");
        }
        logic.day = 0;
        logic.things.clear();
        boolean spawned = logic.trySpawnStudent(System.currentTimeMillis());
        if (!spawned) {
            throw new AssertionError("trySpawnStudent should succeed on empty Day 0 road!");
        }

        // Kiểm tra chết ở Màn 5 Lượt 2 (flightMode) -> bấm Enter ở ENDING_BE phải chơi lại ngay Lượt 2
        logic.day = 4;
        logic.dialogIndex = 88;
        logic.startFlight(System.currentTimeMillis());
        logic.stageProgressMs = 31000.0;
        logic.hp = 0;
        logic.screen = GameState.ENDING_BE;
        java.awt.event.KeyEvent enterKey = new java.awt.event.KeyEvent(
                logic, java.awt.event.KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0,
                java.awt.event.KeyEvent.VK_ENTER, '\n');
        logic.keyPressed(enterKey);
        if (logic.screen != GameState.ROAD || !logic.flightMode) {
            throw new AssertionError("Dying in Day 5 Turn 2 (flightMode) must retry directly in Turn 2 (flightMode)!");
        }
        if (logic.hp != GameState.FLIGHT_START_HP) {
            throw new AssertionError("Retrying Day 5 Turn 2 must restore HP to " + GameState.FLIGHT_START_HP + ", got: " + logic.hp);
        }
        if (Math.abs(logic.stageProgressMs - GameState.ROAD_DURATION_MS / 3.0) > 1.0) {
            throw new AssertionError("Retrying Day 5 Turn 2 must reset progress to 1/3 road duration, got: " + logic.stageProgressMs);
        }
        if (logic.dialogIndex != 88) {
            throw new AssertionError("Retrying Day 5 Turn 2 must preserve post-jam dialogIndex, got: " + logic.dialogIndex);
        }

        System.out.println("  ✓ testStudentCrossingMechanics passed (Yields to cars/wobble, no clipping, continues after player collision, no text)");
    }
}
