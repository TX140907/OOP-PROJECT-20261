package sanbaka.logic;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import sanbaka.model.*;
import sanbaka.state.BossConfig;
import sanbaka.state.GameState;

/** Xử lý cơ chế chơi game: di chuyển, va chạm, spawn xe, trực thăng, phím bấm. */
public class GameLogic extends GameStory {
    public void beginDay() {
        resetDialogTyping();
        screen = ROAD;
        stageStarted = System.currentTimeMillis();
        lastSpawn = stageStarted;
        lastFrameAt = stageStarted;
        player = day == 1 ? new PlayerBoat() : new PlayerBike();
        player.x = trackX(2);
        player.y = 490;
        hp = 100;
        things.clear();
        score = 0;
        trafficSceneShown = false;
        airSceneShown = false;
        roadPausedForTraffic = false;
        flightMode = false;
        left = right = up = down = keyE = false;
        birds.clear();
        player.lean = 0;
        energy = 100;
        groundScroll = 0;
        jamBuilt = false;
        lastBolt = stageStarted - 800; // Xuất hiện tia sét sớm ngay đầu màn 4
        lastHitAt = 0;
        player.invulnerable = 0;
        playerVx = 0;
        iceSlipUntil = 0;
        windDir = 0;
        windWarningUntil = 0;
        windActiveUntil = 0;
        nextWindAt = stageStarted + 2400;
        frozenUntil = 0;
        windFreezeChecked = false;
        lastDogSpawn = stageStarted + 1400;
        lastStudentSpawn = stageStarted + 2200;
        studentSpawnCount = 0;
        stageProgressMs = 0.0;
        elapsed = 0;
        currentProgressFactor = 1.0;
        curbEdgeTime = 0.0;
        curbBurnFraction = 0.0;
        curbOnFire = false;
        map2CurbSlowFactor = 1.0;
        hitSlowUntil = 0;
        hitSlowFactor = 1.0;
        knockbackVy = 0.0;
        knockbackVx = 0.0;
        shieldActive = false;
        shieldSpawnCount = 0;
        lastShieldSpawn = stageStarted;
        teaSpawnCount = 0;
        lastTeaSpawn = stageStarted;
        warmUntil = 0;
        mysteryBoxSpawnedThisStage = false;
        if (day == 1) {
            map2MysteryBoxRolled = !mysteryBoxSpawned; // Tỉ lệ xuất hiện tuyệt đối (100%) ở màn 2
        }
        floatingTextManager.clear();
        beCause = "";
        retryDialogIndex = dialogIndex;
        requestFocusInWindow();
    }

    public void resumeRoad() {
        long now = System.currentTimeMillis();
        long pausedDelta = now - roadPausedAt;
        stageStarted += pausedDelta;
        if (hitSlowUntil > 0) hitSlowUntil += pausedDelta;
        if (nextWindAt > 0) nextWindAt += pausedDelta;
        if (windWarningUntil > 0) windWarningUntil += pausedDelta;
        if (windActiveUntil > 0) windActiveUntil += pausedDelta;
        if (frozenUntil > 0) frozenUntil += pausedDelta;
        if (iceSlipUntil > 0) iceSlipUntil += pausedDelta;
        if (lastDogSpawn > 0) lastDogSpawn += pausedDelta;
        if (lastStudentSpawn > 0) lastStudentSpawn += pausedDelta;
        if (warmUntil > 0) warmUntil += pausedDelta;
        if (lastShieldSpawn > 0) lastShieldSpawn += pausedDelta;
        if (lastTeaSpawn > 0) lastTeaSpawn += pausedDelta;
        lastFrameAt = now;
        screen = ROAD;
        roadPausedForTraffic = false;
        fadeInStart = now;
        if (day == 4) startFlight(now);   // 2/3 đường còn lại của ngày 5: lên trực thăng
    }

    public void startTrafficConversation() {
        if (dialogIndex < lines.size() && lines.get(dialogIndex).who.startsWith("@TRÊN ĐƯỜNG")) {
            dialogIndex++;
            roadPausedForTraffic = true;
            callStartedAt = 0;
            screen = DIALOG;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        long perfStart = PERF_LOG_ENABLED ? System.nanoTime() : 0L;
        long now = System.currentTimeMillis();
        if (screen != prevScreen) {
            if (screen == ENDING_HE) {
                startConfetti(now);
                won = true;
                saveProgress();
            } else if (screen == ENDING_SE) {
                startConfetti(now);
            }
            prevScreen = screen;
        }
        if (screen == AIR && now - airSceneStarted >= 3500) {
            screen = DIALOG;
        }
        if (screen == MARIO) {
            if (marioGame != null && !marioStoryMode && !marioIntroMode) marioGame.update(now);
            repaint();
            if (PERF_LOG_ENABLED) recordPerformance(false, System.nanoTime() - perfStart);
            return;
        }
        if (screen == ROAD) {
            if (flightMode) updateFlight(now);
            else updateRoad(now);
            if (hp <= 0 || (day == HOT_DAY && !flightMode && energy <= 0)) {
                if (hp <= 0 && beCause.isEmpty()) {
                    beCause = "Bạn đã hết HP giữa đường.";
                } else if (hp > 0) {
                    beCause = "Bạn đã kiệt sức vì trời nắng nóng.";
                }
                retryAtTrafficCheckpoint = day == 4 && flightMode;
                screen = ENDING_BE;
            }
            else if (day == 4 && !flightMode && !trafficSceneShown && elapsed >= ROAD_DURATION_MS / 3) {
                trafficSceneShown = true;
                roadPausedAt = now;
                unlockScene(SC_JAM, 4);
                startFade(FADE_JAM, now);
            }
            else if (elapsed >= getStageDurationMs()) {
                startFade(FADE_END, now);
            }
        }
        else if (screen == FADE && now - fadeStart >= FADE_MS) {
            if (fadeKind == FADE_JAM) {
                startTrafficConversation();
                fadeInStart = now;
            } else if (fadeKind == FADE_ROOM) {
                cardNarration = false;
                cardStart = now;
                screen = CARD;
            } else {
                skipExamMarker();
                unlockScene(SC_AFTER, day);
                cardTop = "NGÀY " + (day + 1);
                cardMain = "SAU KHI THI";
                cardNarration = false;
                cardStart = now;
                screen = CARD;
            }
        }
        else if (screen == CARD && now - cardStart >= CARD_MS) {
            endCard(now);
        }
        else if (screen == ENDING_HE || screen == ENDING_SE) {
            updateConfetti(now);
        }
        boolean needsRepaint = false;
        if (screen == ROAD || screen == AIR || screen == FADE || screen == CARD || screen == ENDING_HE || screen == ENDING_SE || screen == ENDING_BE) {
            needsRepaint = true;
        } else {
            long fadeIn = now - fadeInStart;
            if (videoFrameDirty || menuTransitionTo >= 0 || isDialogTyping() || (fadeIn >= 0 && fadeIn < 700)) {
                needsRepaint = true;
                videoFrameDirty = false;
            }
        }
        if (needsRepaint) {
            repaint();
        }
        if (PERF_LOG_ENABLED) recordPerformance(false, System.nanoTime() - perfStart);
    }

    public int trackX(int track) {
        int sidewalkWidth = 45;
        int roadStart = ROAD_X + sidewalkWidth;
        int roadWidth = ROAD_W - 2 * sidewalkWidth;
        return switch (track) {
            case 0 -> ROAD_X + sidewalkWidth / 2;
            case 1 -> roadStart + roadWidth / 6;
            case 2 -> roadStart + roadWidth / 2;
            case 3 -> roadStart + roadWidth * 5 / 6;
            default -> ROAD_X + ROAD_W - sidewalkWidth / 2;
        };
    }

    public void skipExamMarker() {
        if (dialogIndex < lines.size()
                && lines.get(dialogIndex).who.startsWith("@NGÀY ")
                && lines.get(dialogIndex).who.contains("SAU KHI THI")) {
            dialogIndex++;
        }
    }

    public void updateRoad(long now) {
        double dt = Math.min((now - lastFrameAt) / 1000.0, 0.05);
        lastFrameAt = now;
        double dur = getStageDurationMs() / 1000.0;
        boolean jamDay = day == 4;
        double jamT1 = dur / 3, jamT0 = jamT1 - JAM_WINDOW;

        // Hệ số tốc độ theo trạng thái Mệt mỏi (Màn 4 Energy < 50%), Băng lạnh/Vũng băng (Màn 3), Làm chậm ngắn khi va chạm và Sát lề màn 2
        double deliriumIntensity = (day == HOT_DAY && energy < 50.0) ? (50.0 - energy) / 50.0 : 0.0;
        double fatigueFactor = deliriumIntensity > 0 ? (0.60 + 0.40 * (1.0 - deliriumIntensity)) : 1.0;
        boolean isFrozen = (day == COLD_DAY && now < frozenUntil);
        boolean isIceSlipping = (day == COLD_DAY && now < iceSlipUntil);
        boolean isHitSlowed = (now < hitSlowUntil);

        // Quãng đường cố định (~40s nếu đi thẳng thuận lợi); NẾU VÀ CHỈ NẾU dính hiệu ứng làm chậm thì tiến độ thực sự chậm lại
        double progFactor = 1.0;
        if (isFrozen) progFactor = Math.min(progFactor, 0.70);
        if (isIceSlipping) progFactor = Math.min(progFactor, 0.72);
        if (day == HOT_DAY && energy < 50.0) progFactor = Math.min(progFactor, fatigueFactor);
        if (day == 1 && map2CurbSlowFactor < 0.99) progFactor = Math.min(progFactor, map2CurbSlowFactor);
        if (isHitSlowed) progFactor = Math.min(progFactor, hitSlowFactor);
        currentProgressFactor = progFactor;

        stageProgressMs += dt * 1000.0 * currentProgressFactor;
        elapsed = (long) stageProgressMs;
        double t = stageProgressMs / 1000.0;

        // Xử lý sự kiện Gió mùa Đông Bắc ở Màn 3 (day == COLD_DAY)
        if (day == COLD_DAY) {
            if (dur - t > 2.5 && windDir == 0 && now >= nextWindAt) {
                windDir = rng.nextBoolean() ? 1 : -1;
                windWarningUntil = now + 800;
                windActiveUntil = windWarningUntil + 3000;
                windFreezeChecked = false;
                nextWindAt = windActiveUntil + 2500 + rng.nextInt(1400);
            }
            if (windDir != 0 && now >= windWarningUntil && now < windActiveUntil && !windFreezeChecked) {
                windFreezeChecked = true;
                // Nếu đang giữ ấm bởi Trà gừng (now < warmUntil) thì miễn nhiễm đóng băng
                if (now >= warmUntil) {
                    // Xác suất 45% cơn gió lạnh làm xe bị đóng băng lạnh, giảm tốc độ tiến xuống 70%
                    if (rng.nextInt(100) < 45) {
                        frozenUntil = now + 2700;
                        floatingTextManager.spawn(player.x + 28, player.y - 12, "freeze", false);
                    }
                } else {
                    floatingTextManager.spawn(player.x + 28, player.y - 12, "ginger_tea", true);
                }
            }
            if (windDir != 0 && now >= windActiveUntil) {
                windDir = 0;
            }
        }

        double forwardSpeedFactor = isFrozen ? 0.70 : (day == 1 ? map2CurbSlowFactor : fatigueFactor);
        if (isHitSlowed) {
            forwardSpeedFactor = Math.min(forwardSpeedFactor, hitSlowFactor);
        }
        double controlHitPenalty = isHitSlowed ? 0.68 : 1.0;

        if (day == COLD_DAY) {
            boolean slipping = now < iceSlipUntil;
            if (slipping) {
                // Đang trượt bánh do cán qua vũng băng (IcePatch): xe bị trượt ngang theo quán tính
                playerVx -= Math.copySign(Math.min(Math.abs(playerVx), 340.0 * dt), playerVx);
                player.x += playerVx * dt;
                double targetLean = Math.max(-1.25, Math.min(1.25, playerVx / PLAYER_SPEED));
                player.lean += (targetLean - player.lean) * Math.min(1.0, dt * 10);
            } else {
                // Đi bình thường trên mặt đường: BỎ GIA TỐC NGANG, ĐIỀU KHIỂN TRỰC TIẾP NHƯ MỌI MAP KHÁC
                playerVx = 0;
                double hSpeed = PLAYER_SPEED * controlHitPenalty;
                if (left) player.x -= hSpeed * dt;
                if (right) player.x += hSpeed * dt;
                player.lean += ((right ? 1 : 0) - (left ? 1 : 0) - player.lean) * Math.min(1.0, dt * 10);
            }
            // Gió mùa Đông Bắc thổi tạt ngang (cân bằng lực vừa phải, người chơi vẫn giữ được quyền kiểm soát tay lái)
            if (windDir != 0 && now >= windWarningUntil && now < windActiveUntil) {
                double gustProgress = (now - windWarningUntil) / (double) (windActiveUntil - windWarningUntil);
                double gustEnvelope = Math.sin(Math.max(0.0, Math.min(1.0, gustProgress)) * Math.PI);
                double windPushSpeed = 105.0 * (0.35 + 0.65 * gustEnvelope);
                // Giảm bớt lực xô khi xe đã sát mép đường để tránh bị gió ép chặt vào vỉa hè
                if (windDir > 0 && player.x > PLAYER_MAX_X - 36) {
                    double curbDist = Math.max(0.0, (PLAYER_MAX_X - player.x) / 36.0);
                    windPushSpeed *= (0.25 + 0.75 * curbDist);
                } else if (windDir < 0 && player.x < PLAYER_MIN_X + 36) {
                    double curbDist = Math.max(0.0, (player.x - PLAYER_MIN_X) / 36.0);
                    windPushSpeed *= (0.25 + 0.75 * curbDist);
                }
                player.x += windDir * windPushSpeed * dt;
            }
            player.x = Math.max(PLAYER_MIN_X, Math.min(PLAYER_MAX_X, player.x));
        } else {
            double speedModifier = day == 1 ? map2CurbSlowFactor : fatigueFactor;
            double stageSpeedMult = (day == HOT_DAY) ? 1.25 : 1.0;
            double hSpeed = PLAYER_SPEED * stageSpeedMult * speedModifier * controlHitPenalty;
            if (left) player.x -= hSpeed * dt;
            if (right) player.x += hSpeed * dt;
            if (day == HOT_DAY && energy < 50.0) {
                // Chao đảo ảo giác say nắng khi năng lượng dưới 50% (nhanh hơn 1.25 lần)
                player.x += Math.sin(now / 152.0) * deliriumIntensity * 36.0 * dt;
            }
            player.x = Math.max(PLAYER_MIN_X, Math.min(PLAYER_MAX_X, player.x));
            player.lean += ((right ? 1 : 0) - (left ? 1 : 0) - player.lean) * Math.min(1.0, dt * 10);
        }

        double stageVerticalMult = (day == HOT_DAY) ? 1.25 : 1.0;
        double vSpeed = VERTICAL_SPEED * stageVerticalMult * forwardSpeedFactor;
        if (up) player.y -= vSpeed * dt;
        if (down) player.y += vSpeed * dt;
        if (isFrozen) {
            // Khi bị đóng băng lạnh (tốc độ tiến còn 70%), xe chỉ ghì nhẹ về phía sau
            player.y += 12.0 * dt;
        }
        // Hiệu ứng đẩy lùi mượt sau khi va chạm xe
        if (knockbackVy > 0) {
            player.y += knockbackVy * dt;
            knockbackVy = Math.max(0, knockbackVy - 540.0 * dt);
        }
        if (Math.abs(knockbackVx) > 1.0) {
            player.x += knockbackVx * dt;
            double dropKx = 480.0 * dt;
            knockbackVx = Math.abs(knockbackVx) <= dropKx ? 0 : knockbackVx - Math.copySign(dropKx, knockbackVx);
            player.x = Math.max(PLAYER_MIN_X, Math.min(PLAYER_MAX_X, player.x));
        }

        // Cơ chế Lề đường:
        // - Ở Map 2 (đường ngập): Bỏ bốc lửa, thay bằng tốc độ chậm dần về 50%
        // - Ở các map khác: Đi sát biên quá 3 giây sẽ bốc lửa xe và đốt 10 máu/giây; thoát lề đường sẽ hết dần
        boolean inJamStop = jamDay && t >= jamT0;
        boolean huggingCurb = !inJamStop
                && (player.x <= PLAYER_MIN_X + CURB_ZONE_MARGIN || player.x >= PLAYER_MAX_X - CURB_ZONE_MARGIN);

        if (day == 1) {
            curbOnFire = false;
            curbEdgeTime = 0.0;
            curbBurnFraction = 0.0;
            if (huggingCurb) {
                // Tốc độ thuyền chậm dần về 50%
                map2CurbSlowFactor = Math.max(0.50, map2CurbSlowFactor - 0.70 * dt);
                player.x += Math.sin(now / 35.0) * 1.0;
                player.x = Math.max(PLAYER_MIN_X, Math.min(PLAYER_MAX_X, player.x));
            } else {
                map2CurbSlowFactor = Math.min(1.0, map2CurbSlowFactor + 0.90 * dt);
            }
        } else {
            map2CurbSlowFactor = 1.0;
            if (huggingCurb) {
                curbEdgeTime = Math.min(CURB_FIRE_THRESHOLD_SEC, curbEdgeTime + dt);
                if (curbEdgeTime >= CURB_FIRE_THRESHOLD_SEC) {
                    curbOnFire = true;
                }
                player.x += Math.sin(now / 28.0) * 1.4;
                player.x = Math.max(PLAYER_MIN_X, Math.min(PLAYER_MAX_X, player.x));
            } else {
                curbEdgeTime = Math.max(0.0, curbEdgeTime - 1.2 * dt);
                if (curbEdgeTime <= 0.0) {
                    curbOnFire = false;
                    curbBurnFraction = 0.0;
                }
            }
            if (curbOnFire) {
                double burnRate = huggingCurb ? CURB_BURN_DPS : (CURB_BURN_DPS * (curbEdgeTime / CURB_FIRE_THRESHOLD_SEC) * 0.45);
                curbBurnFraction += burnRate * dt;
                if (curbBurnFraction >= 1.0) {
                    int burnDmg = (int) curbBurnFraction;
                    hp = Math.max(0, hp - burnDmg);
                    curbBurnFraction -= burnDmg;
                    floatingTextManager.spawnDebounced(player.x + 28, player.y - 12, "curb_fire", false, 1200);
                    if (hp <= 0 && beCause.isEmpty()) {
                        beCause = "Xe của bạn đã bốc cháy vì đi sát lề đường quá lâu.";
                    }
                }
            }
        }

        // Tốc độ cuộn mặt đường và chuyển động xung quanh giữ nguyên tự nhiên (không bị chậm theo người chơi)
        double base = (3.6 + day * 0.32) * 60;
        if (day == HOT_DAY) base *= 1.25; // Màn 4 diễn ra nhanh hơn 1.25 lần
        double factor = 1;
        if (jamDay) {
            if (t >= jamT0) factor = 1 - Math.min(1, (t - jamT0) / JAM_WINDOW);
        } else if (dur - t < SCHOOL_APPROACH) {
            factor = 0.2 + 0.8 * Math.max(0, dur - t) / SCHOOL_APPROACH;
        }
        double scroll = base * factor;
        groundScroll += scroll * dt;

        boolean canSpawn = jamDay ? t < jamT0 - 3.0 : dur - t > 5.5;
        long spawnInterval = day == 1
                ? (long) ((820 - day * 35) * 100.0 / 162.0)
                : (long) ((820 - day * 35) * 100.0 / 148.0);
        if (day == HOT_DAY) spawnInterval = (long) (spawnInterval / 1.25);
        if (canSpawn && now - lastSpawn > spawnInterval) {
            lastSpawn = now;
            trySpawnVehicle();
        }
        if (canSpawn && day == HOT_DAY && now - lastBolt > 1500 + rng.nextInt(350)) {
            if (trySpawnBolt()) {
                lastBolt = now;
            } else {
                lastBolt = now - 1000; // Thử lại sớm hơn nếu bị vướng xe cộ
            }
        }
        if (canSpawn && day == COLD_DAY && teaSpawnCount < 4 && now - lastTeaSpawn > 7500 + rng.nextInt(1500)) {
            if (trySpawnGingerTea()) {
                lastTeaSpawn = now;
            } else {
                lastTeaSpawn = now - 1000;
            }
        }
        if (canSpawn && day == HOT_DAY && shieldSpawnCount < 3 && now - lastShieldSpawn > 8000 + rng.nextInt(1500)) {
            if (trySpawnShield()) {
                lastShieldSpawn = now;
            } else {
                lastShieldSpawn = now - 1000;
            }
        }
        if (canSpawn && day == HOT_DAY && now - lastDogSpawn > 2560) {
            lastDogSpawn = now + rng.nextInt(640);
            trySpawnStrayDog(now);
        }
        if (canSpawn && day == 0 && studentSpawnCount < 4 && now - lastStudentSpawn > 8000 + rng.nextInt(1500)) {
            if (trySpawnStudent(now)) {
                lastStudentSpawn = now;
                studentSpawnCount++;
            } else {
                lastStudentSpawn = now - 1000;
            }
        }
        // Spawn Hộp bí ẩn (Mystery Box) Easter Egg:
        // - Xuất hiện ở Màn 2 với tỉ lệ tuyệt đối (100%), hoặc Màn 4 nếu Màn 2 chưa xuất hiện (đúng 1 lần duy nhất).
        // - Nếu Màn 2 đã xuất hiện rồi thì không cho xuất hiện lại nữa.
        if (canSpawn && !mysteryBoxSpawned && !mysteryBoxSpawnedThisStage && t >= 7.5 && dur - t > 7.0) {
            boolean shouldSpawn = (day == 1 && map2MysteryBoxRolled) || (day == HOT_DAY);
            if (shouldSpawn) {
                if (trySpawnMysteryBox()) {
                    mysteryBoxSpawned = true;
                    mysteryBoxSpawnedThisStage = true;
                }
            }
        }
        if (jamDay && !jamBuilt && t >= jamT0) buildJam(base);
        if (day == HOT_DAY) {
            energy = Math.max(0, energy - ENERGY_DRAIN * 1.25 * dt);
            if (energy < 30.0) {
                floatingTextManager.spawnDebounced(player.x + 28, player.y - 12, "low_energy", false, 3500);
            }
        }

        double tau = Math.max(0, Math.min(JAM_WINDOW, t - jamT0));
        double jamShift = base * (tau - tau * tau / (2 * JAM_WINDOW));
        double minY = 330;
        for (int i = 0; i < things.size(); i++) {
            Thing o = things.get(i);
            if (o.jam) {
                o.y = o.y0 + jamShift;
                if (o.y > -120) minY = Math.max(minY, o.y + o.h / 2 + 34);
            } else if (o instanceof Shark shark) {
                if (shark.phase == Shark.PHASE_FIN_APPROACH) {
                    for (int j = 0; j < things.size(); j++) {
                        if (i == j) continue;
                        Thing other = things.get(j);
                        if (!(other instanceof Shark)
                                && Math.abs(other.x - shark.x) < (other.w + shark.w) / 2 + 14
                                && Math.abs(other.y - shark.y) < (other.h + shark.h) / 2 + 26) {
                            shark.diveNow(now);
                            break;
                        }
                    }
                } else if (shark.readyToLockWarning(now)) {
                    lockSharkWarningTarget(shark, scroll, now);
                }
                shark.updateShark(dt, now);
            } else if (o instanceof StrayDog dog) {
                dog.updateDog(dt, scroll, now, ROAD_X, ROAD_W, things, player.x, player.y, player.w, player.h);
            } else if (o instanceof Student student) {
                student.updateStudent(dt, scroll, now, ROAD_X, ROAD_W, things, player.x, player.y, player.w, player.h);
            } else {
                // Giữ khoảng cách an toàn để không có 2 vật thể đè hình ảnh lên nhau trên cùng làn
                for (int j = 0; j < things.size(); j++) {
                    if (i == j) continue;
                    Thing other = things.get(j);
                    if (other.jam || other instanceof Shark || other instanceof StrayDog || other instanceof Student || other.snack || o.snack) continue;
                    // other nằm phía trên màn hình (nhỏ hơn y) nhưng trôi xuống nhanh hơn (other.fwd < o.fwd)
                    if (other.y < o.y && (o.y - other.y) < (o.h + other.h) / 2 + 24
                            && Math.abs(o.x - other.x) < (o.w + other.w) / 2 + 8) {
                        if (o.fwd > other.fwd) {
                            o.fwd = other.fwd;
                        }
                    }
                }
                double rel = scroll * (1 - o.fwd);
                o.y += rel * dt;
                if (o instanceof NinjaLead ninja) {
                    ninja.updateMovement(dt, laneX(0), laneX(1), laneX(2));
                } else if (o.wobbleAmp > 0) {
                    double ph = o.wobblePhase + t * o.wobbleFreq * 2 * Math.PI;
                    o.x = o.baseX + o.wobbleAmp * Math.sin(ph);
                    double vx = o.wobbleAmp * o.wobbleFreq * 2 * Math.PI * Math.cos(ph);
                    o.angle = Math.max(-0.3, Math.min(0.3, Math.atan2(vx, Math.max(60, rel))));
                }
            }
        }
        player.y = Math.max(minY, Math.min(565, player.y));

        Iterator<Thing> iterator = things.iterator();
        while (iterator.hasNext()) {
            Thing o = iterator.next();
            if (o instanceof Shark shark) {
                if (shark.isChomping(now) && Math.hypot(shark.x - player.x, shark.y - player.y) < 42) {
                    if (!shark.hit) {
                        shark.hit = true;
                        if (player.invulnerable <= now) {
                            double pushDir = player.x < shark.x ? -1 : 1;
                            applyCollisionHit(26, pushDir, 16, 22, 200, 0.38, 1400, 780, now, "shark");
                        }
                    }
                }
                if (shark.done) iterator.remove();
                continue;
            }
            if (o instanceof StrayDog dog) {
                if (dog.phase == StrayDog.PHASE_DASH_ACROSS && !dog.hit
                        && Math.abs(dog.x - player.x) < dog.w / 2 + 12
                        && Math.abs(dog.y - player.y) < dog.h / 2 + 12) {
                    dog.hit = true;
                    if (player.invulnerable <= now) {
                        applyCollisionHit(20, dog.dir, 14, 18, 165, 0.42, 1200, 750, now, "dog_bark");
                    }
                }
                if (dog.done || dog.y - dog.h / 2 > H + 30) iterator.remove();
                continue;
            }
            if (o instanceof Student student) {
                if (student.phase == Student.PHASE_CROSSING && !student.isHitInvulnerable(now)
                        && Math.abs(student.x - player.x) < student.w / 2 + 10
                        && Math.abs(student.y - player.y) < student.h / 2 + 12) {
                    student.onPlayerHit(now);
                    if (player.invulnerable <= now) {
                        applyCollisionHit(8, student.dir, 10, 14, 130, 0.50, 800, 750, now, null);
                    }
                }
                if (student.done || student.y - student.h / 2 > H + 40 || student.y + student.h / 2 < -120) {
                    iterator.remove();
                }
                continue;
            }
            if (!o.jam) {
                double hw = (o.kind == 19) ? 36.0 : (o.snack ? 22 : ((o.kind == 11 || o.kind == 14) ? o.w / 2 : o.w / 2 + 9));
                double hh = (o.kind == 19) ? 36.0 : (o.snack ? 28 : ((o.kind == 11 || o.kind == 14) ? o.h / 2 : o.h / 2 + 6));
                if (Math.abs(o.x - player.x) < hw && Math.abs(o.y - player.y) < hh) {
                    if (o.snack) {
                        if (o.kind == 16) {
                            // Trà gừng ở Màn 3: hóa giải đóng băng, giữ ấm và hồi phục máu
                            frozenUntil = 0;
                            warmUntil = now + 6500;
                            hp = Math.min(flightMode ? FLIGHT_START_HP : 100, hp + 15);
                            score += 60;
                            floatingTextManager.spawn(player.x + 28, player.y - 12, "ginger_tea", true);
                            iterator.remove();
                            continue;
                        } else if (o.kind == 17) {
                            // Khiên bảo vệ ở Màn 4: miễn 1 lần va chạm
                            shieldActive = true;
                            score += 60;
                            floatingTextManager.spawn(player.x + 28, player.y - 12, "shield_pickup", true);
                            iterator.remove();
                            continue;
                        } else if (o.kind == 19) {
                            // Hộp bí ẩn (Mystery Box) - Easter Egg kích hoạt Happy Ending ẩn
                            mysteryBoxCollected = true;
                            score += 150;
                            floatingTextManager.spawn(player.x + 28, player.y - 12, "mystery_box", true);
                            iterator.remove();
                            continue;
                        } else {
                            energy = Math.min(100, energy + BOLT_ENERGY);
                            hp = Math.min(flightMode ? FLIGHT_START_HP : 100, hp + BOLT_HP);
                            score += 50;
                            floatingTextManager.spawn(player.x + 28, player.y - 12, "energy_bolt", true);
                            iterator.remove();
                            continue;
                        }
                    }
                    if (!o.hit) {
                        o.hit = true;
                        if (o.kind == 14) {
                            // Cán qua Vũng băng ở Màn 3: làm xe trượt bánh văng ngang mất ma sát
                            iceSlipUntil = now + 900;
                            if (Math.abs(playerVx) > 30) {
                                playerVx = Math.copySign(PLAYER_SPEED * 1.08, playerVx);
                            } else {
                                playerVx = (rng.nextBoolean() ? 1 : -1) * PLAYER_SPEED * 0.90;
                            }
                            floatingTextManager.spawnDebounced(player.x + 28, player.y - 12, "ice_slip", false, 800);
                            continue;
                        }
                        if (player.invulnerable <= now) {
                            double pushDir = player.x < o.x ? -1 : 1;
                            if (o.kind == 11) {
                                // Ổ gà: 8 HP, giật lùi nhẹ + làm chậm ngắn 850ms
                                applyCollisionHit(8, pushDir, 8, 10, 105, 0.55, 850, 550, now, "pothole");
                            } else if (o.kind == 9) {
                                // Xe tải hạng nặng: 30 HP, đẩy lùi mạnh + làm chậm ngắn 1500ms (tốc độ còn 35%)
                                applyCollisionHit(30, pushDir, 18, 26, 230, 0.35, 1500, 800, now, "collision");
                                o.y -= 14;
                            } else if (o.kind == 6 || o.kind == 12) {
                                // Xe máy NPC / Ninja Lead: 20 HP, đẩy lùi + làm chậm ngắn 1200ms
                                applyCollisionHit(20, pushDir, 14, 18, 165, 0.42, 1200, 750, now, "collision");
                                o.y -= 12;
                            } else {
                                // Ô tô, xe buýt, thuyền, rào chắn: 24 HP, đẩy lùi + làm chậm ngắn 1350ms (tốc độ còn 38%)
                                applyCollisionHit(24, pushDir, 15, 22, 195, 0.38, 1350, 780, now, "collision");
                                if (o.kind != 10) o.y -= 14;
                            }
                        }
                    }
                }
            }
            if (o.y - o.h / 2 > H + 30) iterator.remove();
        }
    }

    private void applyCollisionHit(int dmg, double pushDirX, double pushX, double pushY,
            double kbVy, double slowFactor, long slowDurationMs, long invulnMs, long now) {
        applyCollisionHit(dmg, pushDirX, pushX, pushY, kbVy, slowFactor, slowDurationMs, invulnMs, now, "collision");
    }

    private void applyCollisionHit(int dmg, double pushDirX, double pushX, double pushY,
            double kbVy, double slowFactor, long slowDurationMs, long invulnMs, long now, String subcategory) {
        if (shieldActive) {
            shieldActive = false;
            player.invulnerable = now + 650;
            lastHitAt = now;
            floatingTextManager.spawn(player.x + 28, player.y - 12, "shield_block", true);
            return;
        }
        hp -= dmg;
        player.invulnerable = now + invulnMs;
        lastHitAt = now;
        player.x = Math.max(PLAYER_MIN_X, Math.min(PLAYER_MAX_X, player.x + pushDirX * pushX));
        knockbackVx = pushDirX * (pushX * 4.2);
        player.y = Math.min(565, player.y + pushY);
        knockbackVy = Math.max(knockbackVy, kbVy);
        hitSlowFactor = (now < hitSlowUntil) ? Math.min(hitSlowFactor, slowFactor) : slowFactor;
        hitSlowUntil = Math.max(hitSlowUntil, now + slowDurationMs);
        if (subcategory != null && !subcategory.isEmpty()) {
            floatingTextManager.spawn(player.x + 28, player.y - 12, subcategory, false);
        }
    }

    private void lockSharkWarningTarget(Shark shark, double scroll, long now) {
        double[] dxCandidates = {0, -48, 48, -92, 92, -136, 136, -180, 180};
        double[] dyCandidates = {0, -34, 30, -64};
        for (double dy : dyCandidates) {
            for (double dx : dxCandidates) {
                double cx = Math.max(PLAYER_MIN_X, Math.min(PLAYER_MAX_X, player.x + dx));
                double cy = Math.max(335, Math.min(545, player.y + dy));
                boolean clear = true;
                for (Thing o : things) {
                    if (o == shark) continue;
                    if (o instanceof Shark otherShark) {
                        if (otherShark.phase >= Shark.PHASE_WARNING && Math.hypot(cx - otherShark.x, cy - otherShark.y) < 84) {
                            clear = false;
                            break;
                        }
                        continue;
                    }
                    double yStart = o.y - (o.h + shark.h) / 2.0 - 16;
                    double yEnd = o.y + scroll * (1.0 - o.fwd) * 1.35 + (o.h + shark.h) / 2.0 + 16;
                    double xHalf = (o.w + shark.w) / 2.0 + o.wobbleAmp + 14;
                    if (Math.abs(cx - o.baseX) < xHalf && cy >= yStart && cy <= yEnd) {
                        clear = false;
                        break;
                    }
                }
                if (clear) {
                    shark.startWarningAt(cx, cy, now);
                    return;
                }
            }
        }
    }

    public boolean clearToSpawn(Thing t) {
        if (t instanceof Shark) {
            for (Thing o : things) {
                if (o.y < 250 && Math.abs(o.x - t.x) < (o.w + t.w) / 2 + 18) {
                    return false;
                }
            }
            return true;
        }
        int blockedLanes = 0;
        if (!t.snack) {
            for (int l = 0; l < 3; l++) {
                if (Math.abs(t.baseX - laneX(l)) < t.w / 2 + t.wobbleAmp + 22) {
                    blockedLanes |= (1 << l);
                }
            }
        }
        double tReach = (t instanceof NinjaLead) ? 156 : t.wobbleAmp;
        for (Thing o : things) {
            if (o instanceof Shark || o instanceof StrayDog || o instanceof Student) {
                if (Math.abs(o.x - t.x) < (o.w + t.w) / 2 + 18 && Math.abs(o.y - t.y) < 220) {
                    return false;
                }
                continue;
            }
            double oReach = (o instanceof NinjaLead) ? 156 : o.wobbleAmp;
            double a1 = t.baseX - tReach - t.w / 2, a2 = t.baseX + tReach + t.w / 2;
            double b1 = o.baseX - oReach - o.w / 2, b2 = o.baseX + oReach + o.w / 2;
            if (a2 + 12 > b1 && a1 - 12 < b2) {
                if (Math.abs(o.y - t.y) < (o.h + t.h) / 2 + 40) return false;
                // Kiểm tra quỹ đạo trôi đến hết màn hình để ổ gà/vũng băng/rào chắn/xe không bao giờ đè hình ảnh lên nhau
                double sMax = Math.max(0, (H + o.h / 2 + 30 - o.y) / Math.max(0.1, 1.0 - o.fwd));
                double oyEnd = o.y + (1.0 - o.fwd) * sMax;
                double tyEnd = t.y + (1.0 - t.fwd) * sMax;
                if (oyEnd - tyEnd < (o.h + t.h) / 2 + 38) return false;
            }
            if (!t.snack && !o.snack && Math.abs(o.y - t.y) < (o.h + t.h) / 2 + 85) {
                for (int l = 0; l < 3; l++) {
                    if (Math.abs(o.baseX - laneX(l)) < o.w / 2 + o.wobbleAmp + 22) {
                        blockedLanes |= (1 << l);
                    }
                }
            }
        }
        if (!t.snack && blockedLanes == 0b111) return false;
        return true;
    }

    public boolean trySpawnStrayDog(long now) {
        int activeDogs = 0;
        for (Thing o : things) {
            if (o instanceof StrayDog) activeDogs++;
        }
        if (activeDogs >= 2) return false;
        // Bảng các hàng xuất phát đa dạng từ phía trước tới sát hàng nhân vật chính (~480..520)
        double[] rowBases = { 185, 235, 285, 335, 385, 435, 475, 510 };
        for (int attempt = 0; attempt < 8; attempt++) {
            int side = rng.nextBoolean() ? -1 : 1; // -1: từ vỉa hè trái chạy sang phải, 1: từ vỉa hè phải chạy sang trái
            double startX = side < 0 ? (ROAD_X + 22) : (ROAD_X + ROAD_W - 22);
            double baseRow = rowBases[rng.nextInt(rowBases.length)];
            double startY = baseRow + (rng.nextDouble() - 0.5) * 26.0;
            startY = Math.max(165, Math.min(525, startY));
            boolean clear = true;
            for (Thing o : things) {
                if (o instanceof StrayDog && Math.abs(o.y - startY) < 70) {
                    clear = false;
                    break;
                }
                if (Math.abs(o.y - startY) < o.h / 2 + 30 && Math.abs(o.x - startX) < 95) {
                    clear = false;
                    break;
                }
            }
            if (Math.abs(player.y - startY) < 28 && Math.abs(player.x - startX) < 70) {
                clear = false;
            }
            if (clear) {
                StrayDog dog = (StrayDog) Thing.create(15);
                dog.kind = 15;
                dog.w = VEH_W[15];
                dog.h = VEH_H[15];
                dog.dir = -side;
                dog.x = dog.baseX = startX;
                dog.y = startY;
                dog.phase = StrayDog.PHASE_BARK_WARN;
                dog.phaseStartedAt = now;
                things.add(dog);
                floatingTextManager.spawnDebounced(player.x + 28, player.y - 12, "dog_bark", false, 2500);
                return true;
            }
        }
        return false;
    }

    public boolean trySpawnStudent(long now) {
        if (day != 0) return false;
        int activeStudents = 0;
        for (Thing o : things) {
            if (o instanceof Student) activeStudents++;
        }
        if (activeStudents >= 1) return false; // Chỉ tối đa 1 sinh viên qua đường cùng thời điểm ở Màn 1

        double[] rowBases = { 150, 195, 240, 285, 330 };
        for (int attempt = 0; attempt < 6; attempt++) {
            int side = rng.nextBoolean() ? -1 : 1; // -1: vỉa hè trái sang phải, 1: vỉa hè phải sang trái
            double startX = side < 0 ? (ROAD_X + 16) : (ROAD_X + ROAD_W - 16);
            double baseRow = rowBases[rng.nextInt(rowBases.length)];
            double startY = baseRow + (rng.nextDouble() - 0.5) * 18.0;

            boolean clear = true;
            for (Thing o : things) {
                if (o instanceof Student && Math.abs(o.y - startY) < 70) {
                    clear = false;
                    break;
                }
                if (Math.abs(o.y - startY) < o.h / 2 + 25 && Math.abs(o.x - startX) < 70) {
                    clear = false;
                    break;
                }
            }
            if (Math.abs(player.y - startY) < 35 && Math.abs(player.x - startX) < 60) {
                clear = false;
            }

            if (clear) {
                Student student = (Student) Thing.create(18);
                student.kind = 18;
                student.w = VEH_W[18];
                student.h = VEH_H[18];
                student.dir = -side;
                student.x = student.baseX = startX;
                student.y = startY;
                student.phase = Student.PHASE_SIDEWALK_WAIT;
                student.phaseStartedAt = now;
                things.add(student);
                return true;
            }
        }
        return false;
    }

    public boolean trySpawnVehicle() {
        int kind;
        if (day == 1) {
            // Màn 2 (đường ngập): giữ tỉ lệ các xe/thuyền cũ, thêm cá mập (kind = 13) với tỉ lệ gần ngang xe
            int activeSharks = 0;
            for (Thing o : things) {
                if (o instanceof Shark) activeSharks++;
            }
            int r = rng.nextInt(162);
            if (r >= 100 && activeSharks < 2) {
                kind = 13;
            } else {
                int r2 = r < 100 ? r : rng.nextInt(100);
                int baseKind = r2 < 28 ? 0 : r2 < 46 ? 1 : r2 < 58 ? 2 : r2 < 70 ? 3 : r2 < 80 ? 4 : r2 < 86 ? 5 : 0;
                kind = rng.nextInt(100) < 32 ? 8 : baseKind;
            }
        } else if (day == COLD_DAY) {
            // Màn 3 (trời rét, đường trơn): xe máy có mật độ tăng gấp 1.5 lần
            int r = rng.nextInt(167);
            kind = r < 28 ? 0 : r < 46 ? 1 : r < 58 ? 2 : r < 70 ? 3 : r < 80 ? 4 : r < 86 ? 5
                    : r < 107 ? 6   // Xe máy thường (kind = 6): trọng số tăng 1.5 lần (14 -> 21)
                    : r < 123 ? 9   // Xe tải (kind = 9)
                    : r < 131 ? 10  // Rào chắn (kind = 10)
                    : r < 149 ? 14  // Vũng băng (kind = 14)
                    : 12;           // Ninja Lead (kind = 12): trọng số tăng 1.5 lần (12 -> 18)
        } else {
            // Các màn đường bộ khác: giữ nguyên tỉ lệ 0..6 (tổng 100), thêm xe tải to (9), rào chắn (10), ổ gà (11), Ninja Lead (12)
            int r = rng.nextInt(148);
            kind = r < 28 ? 0 : r < 46 ? 1 : r < 58 ? 2 : r < 70 ? 3 : r < 80 ? 4 : r < 86 ? 5 : r < 100 ? 6
                    : r < 118 ? 9 : r < 128 ? 10 : r < 138 ? 11 : 12;
        }
        for (int attempt = 0; attempt < 6; attempt++) {
            Thing t = Thing.create(kind);
            t.kind = kind;
            t.w = VEH_W[kind];
            t.h = VEH_H[kind];
            t.color = rng.nextInt(8);

            if (kind == 13 && t instanceof Shark shark) {
                t.fwd = 1.0;
                t.y = -32;
                double lo = ROAD_X + 68;
                double hi = ROAD_X + ROAD_W - 68;
                t.baseX = t.x = lo + rng.nextDouble() * (hi - lo);
                t.wobbleAmp = 0;
                shark.diveY = 135 + rng.nextInt(55);
                if (clearToSpawn(t)) {
                    things.add(t);
                    return true;
                }
                continue;
            }

            // Tốc độ đa dạng: rào chắn/ổ gà/vũng băng đứng yên, xe tải/buýt chậm, taxi/ninja lead phóng nhanh
            double fwd;
            if (kind == 10 || kind == 11 || kind == 14) {
                fwd = 0.0; // rào chắn, ổ gà và vũng băng đứng yên trên mặt đường, trôi xuống với tốc độ cuộn đường
            } else if (kind == 9) {
                fwd = 0.18 + rng.nextDouble() * 0.05; // xe tải to chạy chậm
            } else if (kind == 5 || kind == 4) {
                fwd = 0.22 + rng.nextDouble() * 0.06; // xe buýt, xe tải nhỏ chạy chậm
            } else if (kind == 2) {
                fwd = 0.40 + rng.nextDouble() * 0.07; // taxi lướt nhanh
            } else if (kind == 12) {
                fwd = (0.43 + rng.nextDouble() * 0.07) * (day == COLD_DAY ? 1.5 : 1.0); // Màn 3 Ninja Lead tăng vận tốc 1.5 lần
                if (t instanceof NinjaLead ninja && day == COLD_DAY) ninja.speedScale = 1.5;
            } else if (kind == 6) {
                fwd = (0.32 + rng.nextDouble() * 0.10) * (day == COLD_DAY ? 1.5 : 1.0); // Màn 3 xe máy tăng vận tốc 1.5 lần
            } else {
                fwd = 0.34 + rng.nextDouble() * 0.06; // sedan, hatchback, thuyền
            }
            t.fwd = fwd;
            t.y = -t.h / 2 - 12;

            double lo = kind == 6 || kind == 12 ? ROAD_X + 14 + t.w / 2 : ROAD_X + 49 + t.w / 2;
            double hi = kind == 6 || kind == 12 ? ROAD_X + ROAD_W - 14 - t.w / 2 : ROAD_X + ROAD_W - 49 - t.w / 2;
            double bx;
            double amp;
            if (kind == 9) {
                // Xe tải to kín 1 làn: ưu tiên xuất hiện ở làn 1 (lane 0) và làn 3 (lane 2) để chặn đi sát biên
                int laneRoll = rng.nextInt(100);
                int preferredLane = laneRoll < 45 ? 0 : laneRoll < 90 ? 2 : 1;
                int lane = attempt == 0 ? preferredLane : (attempt == 1 ? (preferredLane == 0 ? 2 : 0) : (attempt % 3));
                bx = laneX(lane);
                amp = 0;
            } else if (kind == 10) {
                // Rào chắn công trình: đặt trên 1 trong 3 làn đường
                int lane = rng.nextInt(3);
                bx = laneX(lane) + (rng.nextDouble() - 0.5) * 16;
                amp = 0;
            } else if (kind == 11 || kind == 14) {
                // Ổ gà cỡ lớn hoặc Vũng băng trơn: xuất hiện trên các làn
                int lane = rng.nextInt(3);
                bx = laneX(lane) + (rng.nextDouble() - 0.5) * 20;
                amp = 0;
            } else if (kind == 12) {
                // Ninja Lead: bắt đầu ở 1 trong 3 làn, sau đó sẽ tạt sang làn khác
                int lane = rng.nextInt(3);
                bx = laneX(lane);
                amp = 0;
            } else {
                int pattern = rng.nextInt(100);
                if (kind == 6 || pattern >= 72) bx = lo + rng.nextDouble() * (hi - lo);
                else if (pattern < 30) bx = laneX(rng.nextInt(3));
                else bx = dividerX(1 + rng.nextInt(2)) + (rng.nextDouble() - 0.5) * 16;
                amp = rng.nextInt(100) < 35 ? 18 + rng.nextInt(22) : 0;
                bx = Math.max(lo + amp, Math.min(hi - amp, bx));
            }
            t.baseX = bx;
            t.x = bx;
            t.wobbleAmp = amp;
            t.wobbleFreq = (0.7 + rng.nextDouble() * 0.7) * (day == COLD_DAY && (kind == 6 || kind == 12) ? 1.5 : 1.0);
            t.wobblePhase = rng.nextDouble() * 6.28;
            if (clearToSpawn(t)) {
                things.add(t);
                if (day == COLD_DAY && t.kind != 6 && t.kind != 12 && rng.nextInt(100) < 36) {
                    trySpawnMotorcycle();
                }
                return true;
            }
        }
        return false;
    }

    public boolean trySpawnMotorcycle() {
        for (int attempt = 0; attempt < 4; attempt++) {
            Thing t = Thing.create(6);
            t.kind = 6;
            t.w = VEH_W[6];
            t.h = VEH_H[6];
            t.color = rng.nextInt(8);
            t.fwd = (0.32 + rng.nextDouble() * 0.10) * 1.5;
            t.y = -t.h / 2 - 12;
            double lo = ROAD_X + 14 + t.w / 2;
            double hi = ROAD_X + ROAD_W - 14 - t.w / 2;
            double bx = lo + rng.nextDouble() * (hi - lo);
            double amp = rng.nextInt(100) < 45 ? 20 + rng.nextInt(25) : 0;
            t.baseX = t.x = Math.max(lo + amp, Math.min(hi - amp, bx));
            t.wobbleAmp = amp;
            t.wobbleFreq = (0.7 + rng.nextDouble() * 0.7) * 1.5;
            t.wobblePhase = rng.nextDouble() * 6.28;
            if (clearToSpawn(t)) {
                things.add(t);
                return true;
            }
        }
        return false;
    }

    public boolean trySpawnBolt() {
        for (int attempt = 0; attempt < 10; attempt++) {
            Thing t = Thing.create(7);
            t.kind = 7;
            t.snack = true;
            t.w = 30;
            t.h = 40;
            t.baseX = t.x = ROAD_X + 75 + rng.nextDouble() * (ROAD_W - 150);
            t.y = -30;
            if (clearToSpawn(t)) {
                things.add(t);
                return true;
            }
        }
        return false;
    }

    public boolean trySpawnGingerTea() {
        for (int attempt = 0; attempt < 10; attempt++) {
            Thing t = Thing.create(16);
            t.kind = 16;
            t.snack = true;
            t.w = VEH_W[16];
            t.h = VEH_H[16];
            t.baseX = t.x = ROAD_X + 75 + rng.nextDouble() * (ROAD_W - 150);
            t.y = -30;
            if (clearToSpawn(t)) {
                things.add(t);
                teaSpawnCount++;
                return true;
            }
        }
        return false;
    }

    public boolean trySpawnShield() {
        for (int attempt = 0; attempt < 10; attempt++) {
            Thing t = Thing.create(17);
            t.kind = 17;
            t.snack = true;
            t.w = VEH_W[17];
            t.h = VEH_H[17];
            t.baseX = t.x = ROAD_X + 75 + rng.nextDouble() * (ROAD_W - 150);
            t.y = -30;
            if (clearToSpawn(t)) {
                things.add(t);
                shieldSpawnCount++;
                return true;
            }
        }
        return false;
    }

    public boolean trySpawnMysteryBox() {
        for (int attempt = 0; attempt < 12; attempt++) {
            Thing t = Thing.create(19);
            t.kind = 19;
            t.snack = true;
            t.w = VEH_W[19];
            t.h = VEH_H[19];
            t.y = -35;

            // Đặt vị trí chính xác ở mép lề đường (curb):
            // Một nửa (50%) nằm ở lòng đường, một nửa (50%) lồi lên vỉa hè
            // (50% cơ hội ở mép lề trái, 50% cơ hội ở mép lề phải)
            boolean leftSide = rng.nextBoolean();
            double curbX = leftSide ? (ROAD_X + 45.0) : (ROAD_X + ROAD_W - 45.0);
            t.baseX = t.x = curbX;

            if (clearToSpawn(t)) {
                things.add(t);
                return true;
            }
        }
        return false;
    }

    public void buildJam(double base) {
        jamBuilt = true;
        double d = base * JAM_WINDOW / 2;
        double cursor = 372;
        int[] pool = {0, 1, 0, 2, 3, 1, 4, 0};
        java.util.List<Thing> jamList = new ArrayList<>();

        // 1. Xếp các hàng ô tô / xe buýt / xe tải trên 3 làn chính
        while (cursor > -160) {
            double maxH = 0;
            for (int l = 0; l < 3; l++) {
                int kind;
                if ((l == 0 || l == 2) && rng.nextInt(9) == 0) {
                    kind = 9; // thỉnh thoảng có xe tải to chắn kín làn biên
                } else {
                    kind = rng.nextInt(14) == 0 ? 5 : pool[rng.nextInt(pool.length)];
                }
                Thing t = Thing.create(kind);
                t.kind = kind;
                t.w = VEH_W[t.kind];
                t.h = VEH_H[t.kind];
                t.color = rng.nextInt(8);
                double jitterX = kind == 9 ? (rng.nextDouble() - 0.5) * 4 : (rng.nextDouble() - 0.5) * 20;
                t.x = t.baseX = laneX(l) + jitterX;
                t.angle = kind == 9 ? (rng.nextDouble() - 0.5) * 0.03 : (rng.nextDouble() - 0.5) * 0.1;
                t.jam = true;
                double finalY = cursor - t.h / 2;
                t.y0 = finalY - d;
                t.y = t.y0;
                jamList.add(t);
                maxH = Math.max(maxH, t.h);
            }
            cursor -= maxH + 16;
        }

        // 2. Nhét kín xe máy (kind = 6) vào mọi khoảng trống giữa các ô tô trên cả 3 làn đường
        double roadLeft = ROAD_X + 45 + 15;
        double roadRight = ROAD_X + ROAD_W - 45 - 15;
        for (double my = 366; my > -160; my -= 52) {
            for (double mxBase = roadLeft; mxBase <= roadRight; mxBase += 28) {
                double mx = mxBase + (rng.nextDouble() - 0.5) * 6;
                mx = Math.max(roadLeft, Math.min(roadRight, mx));
                double finalY = my + (rng.nextDouble() - 0.5) * 10;
                double mw = VEH_W[6], mh = VEH_H[6];

                boolean overlaps = false;
                for (Thing o : jamList) {
                    double oy = o.y0 + d;
                    if (Math.abs(mx - o.x) < (mw + o.w) / 2.0 + 3 && Math.abs(finalY - oy) < (mh + o.h) / 2.0 + 4) {
                        overlaps = true;
                        break;
                    }
                }
                if (!overlaps) {
                    Thing m = Thing.create(6);
                    m.kind = 6;
                    m.w = mw;
                    m.h = mh;
                    m.color = rng.nextInt(8);
                    m.x = m.baseX = mx;
                    m.angle = (rng.nextDouble() - 0.5) * 0.15;
                    m.jam = true;
                    m.y0 = finalY - d;
                    m.y = m.y0;
                    jamList.add(m);
                }
            }
        }

        // 3. Xe máy leo lên cả hai bên vỉa hè khi tắc đường
        for (double my = 362; my > -155; my -= 58) {
            for (int sd = 0; sd < 2; sd++) {
                if (rng.nextInt(10) < 8) {
                    Thing m = Thing.create(6);
                    m.kind = 6;
                    m.w = VEH_W[6];
                    m.h = VEH_H[6];
                    m.color = rng.nextInt(8);
                    m.x = m.baseX = trackX(sd == 0 ? 0 : 4) + (rng.nextDouble() - 0.5) * 8;
                    m.angle = (rng.nextDouble() - 0.5) * 0.12;
                    m.jam = true;
                    double finalY = my + (rng.nextDouble() - 0.5) * 8;
                    m.y0 = finalY - d;
                    m.y = m.y0;
                    jamList.add(m);
                }
            }
        }

        jamList.sort(Comparator.comparingDouble(a -> a.y0));
        things.addAll(jamList);
    }

    public void endCard(long now) {
        if (cardNarration && dialogIndex < lines.size()) dialogIndex++;
        cardNarration = false;
        int next = cardNext;
        cardNext = CARD_TO_DIALOG;
        fadeInStart = now;
        if (next == CARD_TO_ROOM) {
            enterRoomMarker();
        } else if (next == CARD_TO_AIR) {
            airSceneShown = true;
            airSceneStarted = now;
            screen = AIR;
        } else {
            screen = DIALOG;
        }
    }

    public void burst(double x, double y, int n, double spMin, double spMax, double angDeg, double spreadDeg) {
        Color[] pal = {PINK, MINT, new Color(255, 214, 92), new Color(88, 160, 230),
                new Color(255, 140, 90), new Color(170, 120, 220), Color.WHITE};
        for (int i = 0; i < n && confetti.size() < 900; i++) {
            Confetti c = new Confetti();
            double a = Math.toRadians(angDeg + (rng.nextDouble() * 2 - 1) * spreadDeg);
            double sp = spMin + rng.nextDouble() * (spMax - spMin);
            c.x = x;
            c.y = y;
            c.vx = Math.cos(a) * sp;
            c.vy = Math.sin(a) * sp;
            c.rot = rng.nextDouble() * 6.28;
            c.vr = (rng.nextDouble() - 0.5) * 14;
            c.size = 6 + rng.nextDouble() * 7;
            c.shape = rng.nextInt(3);
            c.color = pal[rng.nextInt(pal.length)];
            confetti.add(c);
        }
    }

    public void startConfetti(long now) {
        confetti.clear();
        pops.clear();
        lastBurst = now;
        lastConfettiAt = now;
        burst(40, H - 20, 130, 650, 1150, -62, 16);
        burst(W - 40, H - 20, 130, 650, 1150, -118, 16);
    }

    public void updateConfetti(long now) {
        double dt = Math.min((now - lastConfettiAt) / 1000.0, 0.05);
        lastConfettiAt = now;
        if (now - lastBurst > 750) {
            lastBurst = now;
            double px = W * (0.15 + rng.nextDouble() * 0.7), py = H * (0.1 + rng.nextDouble() * 0.35);
            burst(px, py, 70, 160, 460, 0, 180);
            pops.add(new double[]{px, py, now});
            if (rng.nextInt(3) == 0) {
                burst(40, H - 20, 60, 650, 1100, -62, 14);
                burst(W - 40, H - 20, 60, 650, 1100, -118, 14);
            }
        }
        for (int i = 0; i < 2; i++) burst(rng.nextDouble() * W, -10, 1, 40, 120, 90, 10);
        pops.removeIf(p -> now - p[2] > 450);
        Iterator<Confetti> it = confetti.iterator();
        while (it.hasNext()) {
            Confetti c = it.next();
            double drag = Math.pow(0.2, dt);
            c.vx = c.vx * drag + Math.sin(c.rot) * 40 * dt;
            c.vy = c.vy * drag + 260 * dt;
            c.x += c.vx * dt;
            c.y += c.vy * dt;
            c.rot += c.vr * dt;
            if (c.y > H + 40) it.remove();
        }
    }

    public void startFlight(long now) {
        flightMode = true;
        flightRetryDialogIndex = dialogIndex;
        keyE = false;
        things.clear();
        birds.clear();
        missiles.clear();
        enemyPlanes.clear();
        enemyBombs.clear();
        bossBullets.clear();
        explosions.clear();
        boss = null;
        flightBossTriggered = false;
        flightStartedAt = now;
        lastMissileFiredAt = 0;
        heliX = 170;
        heliY = 300;
        heliTilt = 0;
        skyScroll = 0;
        lastBird = now;
        player.invulnerable = 0;
        hp = FLIGHT_START_HP;
        stageProgressMs = ROAD_DURATION_MS / 3.0;
        elapsed = (long) stageProgressMs;
        currentProgressFactor = 1.0;
        curbEdgeTime = 0.0;
        curbBurnFraction = 0.0;
        curbOnFire = false;
        map2CurbSlowFactor = 1.0;
        hitSlowUntil = 0;
        hitSlowFactor = 1.0;
        knockbackVy = 0.0;
        knockbackVx = 0.0;
    }

    public void retryFlight() {
        long now = System.currentTimeMillis();
        resetDialogTyping();
        dialogIndex = flightRetryDialogIndex;
        screen = ROAD;
        lastFrameAt = now;
        left = right = up = down = keyE = false;
        floatingTextManager.clear();
        beCause = "";
        fadeInStart = now;
        startFlight(now);
        requestFocusInWindow();
    }

    public void firePlayerMissile(long now) {
        if (boss == null || !boss.canPlayerFire(now)) return;
        long cd = (long) (BossConfig.missileCooldownSec * 1000);
        if (now - lastMissileFiredAt < cd) return;
        lastMissileFiredAt = now;
        missiles.add(new PlayerMissile(heliX + 28, heliY + 22, BossConfig.missileSpeed));
    }

    public void detonateBomb(double bx, double by, long now) {
        double radius = BossConfig.enemyBombBlastRadius;
        explosions.add(new BombExplosion(bx, by, radius, now));
        if (Math.hypot(bx - heliX, by - heliY) < radius + 15) {
            if (player.invulnerable <= now) {
                hp -= BossConfig.damageBombBlast;
                player.invulnerable = now + 780;
                lastHitAt = now;
                knockbackVx = -140.0;
                hitSlowFactor = (now < hitSlowUntil) ? Math.min(hitSlowFactor, 0.42) : 0.42;
                hitSlowUntil = Math.max(hitSlowUntil, now + 1250);
            }
        }
        Iterator<Bird> bit = birds.iterator();
        while (bit.hasNext()) {
            Bird b = bit.next();
            if (Math.hypot(bx - b.x, by - b.y) <= radius) {
                bit.remove();
            }
        }
        for (EnemyBomb eb : enemyBombs) {
            if (eb.active && Math.hypot(bx - eb.x, by - eb.y) <= radius && Math.hypot(bx - eb.x, by - eb.y) > 0.01) {
                eb.active = false;
                explosions.add(new BombExplosion(eb.x, eb.y, radius, now));
            }
        }
    }

    public void updateFlight(long now) {
        double dt = Math.min((now - lastFrameAt) / 1000.0, 0.05);
        lastFrameAt = now;
        boolean isHitSlowed = (now < hitSlowUntil);
        currentProgressFactor = isHitSlowed ? hitSlowFactor : 1.0;

        // Nếu Boss còn sống (hoặc chưa xong giai đoạn đánh Boss), giữ tiến trình trước khi xuất hiện trường học
        if (boss == null || !boss.defeated) {
            stageProgressMs = Math.min(ROAD_DURATION_MS - 7000.0, stageProgressMs + dt * 1000.0 * currentProgressFactor);
        } else {
            stageProgressMs += dt * 1000.0 * currentProgressFactor;
        }
        elapsed = (long) stageProgressMs;

        double flightLen = ROAD_DURATION_MS * 2.0 / 3.0;
        double progress = Math.max(0, Math.min(1, (stageProgressMs - ROAD_DURATION_MS / 3.0) / flightLen));

        double dx = (right ? 1 : 0) - (left ? 1 : 0);
        double dy = (down ? 1 : 0) - (up ? 1 : 0);
        double moveScale = isHitSlowed ? 0.65 : 1.0;
        if (Math.abs(knockbackVx) > 1.0) {
            heliX += knockbackVx * dt;
            double drop = 440.0 * dt;
            knockbackVx = Math.abs(knockbackVx) <= drop ? 0 : knockbackVx - Math.copySign(drop, knockbackVx);
        }
        heliX = Math.max(90, Math.min(W - 120, heliX + dx * PLAYER_SPEED * 1.15 * moveScale * dt));
        heliY = Math.max(80, Math.min(500, heliY + dy * VERTICAL_SPEED * moveScale * dt));
        heliTilt += (dx * 0.14 - heliTilt) * Math.min(1.0, dt * 8);
        skyScroll += (240 + 120 * progress) * dt;

        // Nếu người chơi giữ phím E: tự động bắn tên lửa ngay khi hết thời gian hồi chiêu
        if (keyE) {
            firePlayerMissile(now);
        }

        long flightTime = (flightStartedAt > 0) ? (now - flightStartedAt) : 0;
        long birdPhaseDurationMs = 15_000; // Đàn chim ở đầu màn 5 lượt 2 kéo dài tầm 15 giây

        // 1. Giai đoạn đàn chim đầu màn 5 lượt 2 (kéo dài 15 giây, khi chim hết boss mới xuất hiện)
        if (!flightBossTriggered) {
            if (flightTime < birdPhaseDurationMs) {
                double birdProgress = Math.min(1.0, flightTime / (double) birdPhaseDurationMs);
                if (now - lastBird > 760 - 310 * birdProgress) {
                    lastBird = now;
                    double speed = 300 + 150 * birdProgress;
                    int pattern = rng.nextInt(100);
                    if (pattern < 28) {
                        double yTop = 78 + rng.nextInt(50);
                        double yMid = 220 + rng.nextInt(140);
                        double yBot = 452 + rng.nextInt(50);
                        birds.add(new Bird(W + 40, yTop, -speed, rng.nextDouble() * 6.28));
                        birds.add(new Bird(W + 108, yMid, -speed, rng.nextDouble() * 6.28));
                        birds.add(new Bird(W + 58, yBot, -speed, rng.nextDouble() * 6.28));
                    } else if (pattern < 56) {
                        double targetY = Math.max(78, Math.min(502, heliY + (rng.nextDouble() - 0.5) * 44));
                        double edgeY = (heliY < 290) ? (458 + rng.nextInt(44)) : (78 + rng.nextInt(48));
                        if (Math.abs(targetY - edgeY) < 90) edgeY = 230 + rng.nextInt(120);
                        birds.add(new Bird(W + 40, targetY, -speed * 1.04, rng.nextDouble() * 6.28));
                        birds.add(new Bird(W + 86, edgeY, -speed, rng.nextDouble() * 6.28));
                    } else if (pattern < 80) {
                        double baseY = 140 + rng.nextInt(300);
                        double spread = 68 + rng.nextInt(22);
                        for (int i = 0; i < 3; i++) {
                            double off = i - 1.0;
                            double by = Math.max(76, Math.min(504, baseY + off * spread));
                            birds.add(new Bird(W + 40 + Math.abs(off) * 48, by, -speed, rng.nextDouble() * 6.28));
                        }
                    } else {
                        double y1 = 76 + rng.nextInt(429);
                        birds.add(new Bird(W + 40, y1, -speed * 1.06, rng.nextDouble() * 6.28));
                        if (rng.nextBoolean()) {
                            double y2 = 76 + rng.nextInt(429);
                            if (Math.abs(y2 - y1) >= 95) birds.add(new Bird(W + 76, y2, -speed, rng.nextDouble() * 6.28));
                        }
                    }
                }
            } else {
                // Đã hết 15s: ngừng spawn chim, đợi đàn chim bay hết khuất màn hình mới kích hoạt Boss
                if (birds.isEmpty() || flightTime >= birdPhaseDurationMs + 2800) {
                    birds.clear();
                    flightBossTriggered = true;
                    boss = new BossEnemy();
                    boss.triggerBossSequence(now);
                }
            }
        }

        // 2. Cập nhật Boss khi đã xuất hiện
        if (boss != null) {
            boss.update(dt, now, heliX, heliY, this);
        }

        // 3. Cập nhật các vụ nổ bom bán kính sát thương diện rộng
        Iterator<BombExplosion> exit = explosions.iterator();
        while (exit.hasNext()) {
            BombExplosion exp = exit.next();
            exp.update(now);
            if (!exp.active) exit.remove();
        }

        // 3. Cập nhật Tên lửa người chơi & Xử lý va chạm
        Iterator<PlayerMissile> mit = missiles.iterator();
        while (mit.hasNext()) {
            PlayerMissile m = mit.next();
            m.update(dt);
            if (!m.active) {
                mit.remove();
                continue;
            }
            if (boss != null && boss.combatActive && Math.abs(m.x - boss.x) < 38 && Math.abs(m.y - boss.y) < 52) {
                m.active = false;
                boss.takeMissileHit(BossConfig.missileDamage, now);
                floatingTextManager.spawn(boss.x - 20, boss.y, "missile_hit", true);
                mit.remove();
                continue;
            }
            boolean hitPlane = false;
            for (EnemyPlane ep : enemyPlanes) {
                if (ep.active && Math.abs(m.x - ep.x) < 46 && Math.abs(m.y - ep.y) < 24) {
                    m.active = false;
                    ep.takeHit(BossConfig.missileDamage, now);
                    floatingTextManager.spawn(ep.x, ep.y, "missile_hit", true);
                    hitPlane = true;
                    break;
                }
            }
            if (hitPlane) {
                mit.remove();
                continue;
            }
            boolean hitBomb = false;
            for (EnemyBomb eb : enemyBombs) {
                if (eb.active && Math.abs(m.x - eb.x) < 28 && Math.abs(m.y - eb.y) < 28) {
                    m.active = false;
                    eb.active = false;
                    detonateBomb(eb.x, eb.y, now);
                    floatingTextManager.spawn(eb.x, eb.y, "missile_hit", true);
                    hitBomb = true;
                    break;
                }
            }
            if (hitBomb) {
                mit.remove();
                continue;
            }
            boolean hitBird = false;
            Iterator<Bird> bit = birds.iterator();
            while (bit.hasNext()) {
                Bird b = bit.next();
                if (Math.abs(m.x - b.x) < 26 && Math.abs(m.y - b.y) < 22) {
                    m.active = false;
                    bit.remove();
                    hitBird = true;
                    break;
                }
            }
            if (hitBird) {
                mit.remove();
            }
        }

        // 4. Cập nhật Máy bay địch
        Iterator<EnemyPlane> pit = enemyPlanes.iterator();
        while (pit.hasNext()) {
            EnemyPlane ep = pit.next();
            ep.update(dt);
            if (!ep.hasShot && !ep.isFlybySquadron && ep.x < 720) {
                ep.hasShot = true;
                if (rng.nextDouble() < BossConfig.attack1PlaneShootChance) {
                    bossBullets.add(new BossBullet(ep.x - ep.w / 2, ep.y, -BossConfig.enemyPlaneBulletSpeed, 0, BossBullet.TYPE_PLANE_STRAIGHT));
                }
            }
            if (ep.active && Math.abs(ep.x - heliX) < 62 && Math.abs(ep.y - heliY) < 34) {
                if (player.invulnerable <= now) {
                    hp -= BossConfig.damagePlaneCollision;
                    player.invulnerable = now + 780;
                    lastHitAt = now;
                    knockbackVx = -180.0;
                    hitSlowFactor = (now < hitSlowUntil) ? Math.min(hitSlowFactor, 0.42) : 0.42;
                    hitSlowUntil = Math.max(hitSlowUntil, now + 1250);
                    floatingTextManager.spawn(heliX + 28, heliY - 12, "boss_hit", false);
                }
                ep.takeHit(1, now);
            }
            if (!ep.active) pit.remove();
        }

        // 5. Cập nhật Bom rơi chéo
        Iterator<EnemyBomb> ebit = enemyBombs.iterator();
        while (ebit.hasNext()) {
            EnemyBomb eb = ebit.next();
            eb.update(dt);
            if (eb.y > 540) {
                eb.active = false;
                detonateBomb(eb.x, eb.y, now);
            }
            if (eb.active && Math.abs(eb.x - heliX) < 52 && Math.abs(eb.y - heliY) < 36) {
                eb.active = false;
                detonateBomb(eb.x, eb.y, now);
            }
            if (!eb.active) ebit.remove();
        }

        // 6. Cập nhật Đạn của Boss & Máy bay
        Iterator<BossBullet> buit = bossBullets.iterator();
        while (buit.hasNext()) {
            BossBullet bb = buit.next();
            bb.update(dt);
            if (bb.active && Math.abs(bb.x - heliX) < 42 && Math.abs(bb.y - heliY) < 26) {
                if (player.invulnerable <= now) {
                    int dmg = (bb.kind == BossBullet.TYPE_AIMED) ? BossConfig.damageBossBullet : BossConfig.damagePlaneBullet;
                    hp -= dmg;
                    player.invulnerable = now + 780;
                    lastHitAt = now;
                    knockbackVx = -140.0;
                    hitSlowFactor = (now < hitSlowUntil) ? Math.min(hitSlowFactor, 0.42) : 0.42;
                    hitSlowUntil = Math.max(hitSlowUntil, now + 1200);
                    floatingTextManager.spawn(heliX + 28, heliY - 12, "boss_hit", false);
                }
                bb.active = false;
            }
            if (!bb.active) buit.remove();
        }

        // 7. Cập nhật Chim & Va chạm với trực thăng
        Iterator<Bird> it = birds.iterator();
        while (it.hasNext()) {
            Bird b = it.next();
            b.x += b.vx * dt;
            b.phase += dt * 13;
            double by = b.y + Math.sin(b.phase * 0.35) * 6;
            if (Math.abs(b.x - heliX) < 52 && Math.abs(by - heliY) < 30) {
                if (player.invulnerable <= now) {
                    hp -= BossConfig.damageBirdCollision;
                    player.invulnerable = now + 780;
                    lastHitAt = now;
                    heliX = Math.max(90, heliX - 22);
                    knockbackVx = -165.0;
                    hitSlowFactor = (now < hitSlowUntil) ? Math.min(hitSlowFactor, 0.42) : 0.42;
                    hitSlowUntil = Math.max(hitSlowUntil, now + 1250);
                    floatingTextManager.spawn(heliX + 28, heliY - 12, "collision", false);
                }
                it.remove();
            } else if (b.x < -80) {
                it.remove();
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (menuTransitionTo >= 0) return;
        int key = e.getKeyCode();
        if (screen == MARIO) {
            if (key == KeyEvent.VK_ESCAPE) {
                pauseGame();
                repaint();
                return;
            }
            if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE) {
                if (marioIntroMode) {
                    marioIntroMode = false;
                    if (marioGame != null) marioGame.start();
                    fadeInStart = System.currentTimeMillis();
                    repaint();
                    return;
                }
                if (marioGame != null && marioGame.player.isCompleted) {
                    if (!marioStoryMode) {
                        marioStoryMode = true;
                        marioRewardDrawerOpenAt = 0;
                        fadeInStart = System.currentTimeMillis();
                    } else {
                        if (dialogIndex < lines.size() && !lines.get(dialogIndex).who.startsWith("@")
                                && revealDialogImmediatelyIfTyping()) return;
                        advanceMarioStory();
                    }
                    repaint();
                    return;
                }
            }
            if (key == KeyEvent.VK_M && !marioStoryMode) {
                marioIntroMode = false;
                if (marioGame != null) marioGame.skipToVictory();
                marioStoryMode = true;
                marioRewardDrawerOpenAt = 0;
                fadeInStart = System.currentTimeMillis();
                repaint();
                return;
            }
            if (!marioStoryMode && !marioIntroMode && marioGame != null) marioGame.keyPressed(key);
            return;
        }
        if (key == KeyEvent.VK_ESCAPE) {
            handleEscape();
            return;
        }
        if (screen == ROAD) {
            if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) left = true;
            if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) right = true;
            if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) up = true;
            if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) down = true;
            if (key == KeyEvent.VK_E) {
                keyE = true;
                if (flightMode) {
                    firePlayerMissile(System.currentTimeMillis());
                }
            }
        }
        if (screen == TITLE || screen == PAUSE) {
            if (screen == TITLE && key == KeyEvent.VK_M) {
                startMarioMinigame();
                return;
            }
            if (key == KeyEvent.VK_ENTER) activateHoveredMenuButton();
            return;
        }
        if (screen == SCENES || screen == HOWTO || screen == INTRO) {
            if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE || key == KeyEvent.VK_BACK_SPACE) {
                if (screen == HOWTO && backTarget == TITLE
                        && (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE)) {
                    playPressed();
                } else {
                    returnFromMenuPage();
                }
            }
            return;
        }
        if (key != KeyEvent.VK_ENTER && key != KeyEvent.VK_SPACE) return;
        if (screen == ROOM) {
            if (dialogIndex < lines.size() && !lines.get(dialogIndex).who.startsWith("@")
                    && revealDialogImmediatelyIfTyping()) return;
            advanceRoom();
        }
        else if (screen == DIALOG) {
            if (dialogIndex < lines.size() && !lines.get(dialogIndex).who.startsWith("@")
                    && revealDialogImmediatelyIfTyping()) return;
            advanceDialog();
        }
        else if (screen == CARD) {
            if (System.currentTimeMillis() - cardStart > 700) endCard(System.currentTimeMillis());
        }
        else if (screen == ENDING_BE) {
            if (day == 4 && flightMode) {
                retryFlight();            // màn 5 chết ở lượt 2 (trực thăng) thì chơi lại ngay từ lượt 2
            } else {
                dialogIndex = retryDialogIndex;   // chơi lại đúng ngày vừa thua
                beginDay();
            }
        }
        else if (screen == ENDING_HE) {
            int secretIndex = sceneIndex(SC_SECRET, 4);
            if (secretIndex >= 0 && sceneUnlocked(secretIndex)) {
                dialogIndex = scenes.get(secretIndex).index + 1;
                day = 4;
                cardTop = "NGÀY 5";
                cardMain = "SAU BUỔI HẸN HÒ";
                cardNarration = false;
                cardNext = CARD_TO_DIALOG;
                cardStart = System.currentTimeMillis();
                screen = CARD;
            } else {
                screen = TITLE;
                menuSel = 0;
                dialogIndex = 0;
                day = 0;
            }
        }
        else if (screen == ENDING_SE) {
            screen = TITLE;
            menuSel = 0;
            dialogIndex = 0;
            day = 0;
        }
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (screen == MARIO || (screen == PAUSE && pausedScreen == MARIO)) {
            if (marioGame != null) marioGame.keyReleased(key);
            return;
        }
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) left = false;
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) right = false;
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) up = false;
        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) down = false;
        if (key == KeyEvent.VK_E) keyE = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public double laneX(int lane) {
        return ROAD_X + 45 + (ROAD_W - 90) * (2 * lane + 1) / 6.0;
    }

    public double dividerX(int i) {
        return ROAD_X + 45 + (ROAD_W - 90) * i / 3.0;
    }
}





