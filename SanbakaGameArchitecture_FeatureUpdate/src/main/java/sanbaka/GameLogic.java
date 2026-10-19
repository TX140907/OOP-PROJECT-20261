package sanbaka;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;

class GameLogic extends GameStory {
    void beginDay() {
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
        birds.clear();
        player.lean = 0;
        energy = 100;
        groundScroll = 0;
        jamBuilt = false;
        lastBolt = stageStarted;
        lastHitAt = 0;
        player.invulnerable = 0;
        beCause = "";
        retryDialogIndex = dialogIndex;
        requestFocusInWindow();
    }

    void resumeRoad() {
        long now = System.currentTimeMillis();
        stageStarted += now - roadPausedAt;
        lastFrameAt = now;
        screen = ROAD;
        roadPausedForTraffic = false;
        fadeInStart = now;
        if (day == 4) startFlight(now);   // 2/3 đường còn lại của ngày 5: lên trực thăng
    }

    void startTrafficConversation() {
        if (dialogIndex < lines.size() && lines.get(dialogIndex).who.startsWith("@TRÊN ĐƯỜNG")) {
            dialogIndex++;
            roadPausedForTraffic = true;
            callStartedAt = 0;
            screen = DIALOG;
        }
    }

    public void actionPerformed(ActionEvent e) {
        long perfStart = PERF_LOG_ENABLED ? System.nanoTime() : 0L;
        long now = System.currentTimeMillis();
        if (screen != prevScreen) {
            if (screen == ENDING_HE) {
                startConfetti(now);
                won = true;
                saveProgress();
            }
            prevScreen = screen;
        }
        if (screen == AIR && now - airSceneStarted >= 3500) {
            screen = DIALOG;
        }
        if (screen == ROAD) {
            elapsed = now - stageStarted;
            if (flightMode) updateFlight(now);
            else updateRoad(now);
            if (hp <= 0 || (day == HOT_DAY && !flightMode && energy <= 0)) {
                beCause = hp <= 0 ? "Bạn đã hết HP giữa đường." : "Bạn đã kiệt sức vì trời nắng nóng.";
                screen = ENDING_BE;
            }
            else if (day == 4 && !flightMode && !trafficSceneShown && elapsed >= ROAD_DURATION_MS / 3) {
                trafficSceneShown = true;
                roadPausedAt = now;
                unlockScene(SC_JAM, 4);
                startFade(FADE_JAM, now);
            }
            else if (elapsed >= ROAD_DURATION_MS) {
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
        else if (screen == ENDING_HE) {
            updateConfetti(now);
        }
        repaint();
        if (PERF_LOG_ENABLED) recordPerformance(false, System.nanoTime() - perfStart);
    }

    int trackX(int track) {
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

    void skipExamMarker() {
        if (dialogIndex < lines.size()
                && lines.get(dialogIndex).who.startsWith("@NGÀY ")
                && lines.get(dialogIndex).who.contains("SAU KHI THI")) {
            dialogIndex++;
        }
    }

    void updateRoad(long now) {
        double dt = Math.min((now - lastFrameAt) / 1000.0, 0.05);
        lastFrameAt = now;
        double t = elapsed / 1000.0, dur = ROAD_DURATION_MS / 1000.0;
        boolean jamDay = day == 4;
        double jamT1 = dur / 3, jamT0 = jamT1 - JAM_WINDOW;

        if (left) player.x -= PLAYER_SPEED * dt;
        if (right) player.x += PLAYER_SPEED * dt;
        player.x = Math.max(PLAYER_MIN_X, Math.min(PLAYER_MAX_X, player.x));
        player.lean += ((right ? 1 : 0) - (left ? 1 : 0) - player.lean) * Math.min(1.0, dt * 10);
        if (up) player.y -= VERTICAL_SPEED * dt;
        if (down) player.y += VERTICAL_SPEED * dt;

        double base = (3.6 + day * 0.32) * 60;
        double factor = 1;
        if (jamDay) {
            if (t >= jamT0) factor = 1 - Math.min(1, (t - jamT0) / JAM_WINDOW);
        } else if (dur - t < SCHOOL_APPROACH) {
            factor = 0.2 + 0.8 * Math.max(0, dur - t) / SCHOOL_APPROACH;
        }
        double scroll = base * factor;
        groundScroll += scroll * dt;

        boolean canSpawn = jamDay ? t < jamT0 - 3.0 : dur - t > 5.5;
        if (canSpawn && now - lastSpawn > 820 - day * 35) {
            lastSpawn = now;
            trySpawnVehicle();
        }
        if (canSpawn && day == HOT_DAY && now - lastBolt > 2300) {
            lastBolt = now;
            trySpawnBolt();
        }
        if (jamDay && !jamBuilt && t >= jamT0) buildJam(base);
        if (day == HOT_DAY) energy = Math.max(0, energy - ENERGY_DRAIN * dt);

        double tau = Math.max(0, Math.min(JAM_WINDOW, t - jamT0));
        double jamShift = base * (tau - tau * tau / (2 * JAM_WINDOW));
        double minY = 330;
        for (Thing o : things) {
            if (o.jam) {
                o.y = o.y0 + jamShift;
                if (o.y > -120) minY = Math.max(minY, o.y + o.h / 2 + 34);
            } else {
                double rel = scroll * (1 - o.fwd);
                o.y += rel * dt;
                if (o.wobbleAmp > 0) {
                    double ph = o.wobblePhase + t * o.wobbleFreq * 2 * Math.PI;
                    o.x = o.baseX + o.wobbleAmp * Math.sin(ph);
                    double vx = o.wobbleAmp * o.wobbleFreq * 2 * Math.PI * Math.cos(ph);
                    o.angle = Math.max(-0.3, Math.min(0.3, Math.atan2(vx, Math.max(60, rel))));
                }
            }
        }
        player.y = Math.max(minY, Math.min(555, player.y));

        Iterator<Thing> iterator = things.iterator();
        while (iterator.hasNext()) {
            Thing o = iterator.next();
            if (!o.jam) {
                double hw = o.snack ? 22 : o.w / 2 + 9, hh = o.snack ? 28 : o.h / 2 + 6;
                if (Math.abs(o.x - player.x) < hw && Math.abs(o.y - player.y) < hh) {
                    if (o.snack) {
                        energy = Math.min(100, energy + BOLT_ENERGY);
                        score += 50;
                        iterator.remove();
                        continue;
                    }
                    if (!o.hit) {
                        o.hit = true;
                        if (player.invulnerable <= now) {
                            hp -= 22;
                            player.invulnerable = now + 950;
                            lastHitAt = now;
                            player.x += (player.x < o.x ? -1 : 1) * 10;
                            player.x = Math.max(PLAYER_MIN_X, Math.min(PLAYER_MAX_X, player.x));
                        }
                    }
                }
            }
            if (o.y - o.h / 2 > H + 30) iterator.remove();
        }
    }

    boolean clearToSpawn(Thing t) {
        for (Thing o : things) {
            if (Math.abs(o.y - t.y) < (o.h + t.h) / 2 + 40) {
                double a1 = t.baseX - t.wobbleAmp - t.w / 2, a2 = t.baseX + t.wobbleAmp + t.w / 2;
                double b1 = o.baseX - o.wobbleAmp - o.w / 2, b2 = o.baseX + o.wobbleAmp + o.w / 2;
                if (a2 + 10 > b1 && a1 - 10 < b2) return false;
            }
        }
        return true;
    }

    boolean trySpawnVehicle() {
        int r = rng.nextInt(100);
        int kind = r < 28 ? 0 : r < 46 ? 1 : r < 58 ? 2 : r < 70 ? 3 : r < 80 ? 4 : r < 86 ? 5 : 6;
        if (day == 1) kind = rng.nextInt(100) < 32 ? 8 : (kind == 6 ? 0 : kind);   // ngày ngập: không xe máy, có thêm thuyền chèo
        for (int attempt = 0; attempt < 6; attempt++) {
            Thing t = Thing.create(kind);
            t.kind = kind;
            t.w = VEH_W[kind];
            t.h = VEH_H[kind];
            t.color = rng.nextInt(8);
            t.fwd = 0.35;
            t.y = -t.h / 2 - 12;
            double lo = kind == 6 ? ROAD_X + 14 + t.w / 2 : ROAD_X + 49 + t.w / 2;
            double hi = kind == 6 ? ROAD_X + ROAD_W - 14 - t.w / 2 : ROAD_X + ROAD_W - 49 - t.w / 2;
            int pattern = rng.nextInt(100);
            double bx;
            if (kind == 6 || pattern >= 72) bx = lo + rng.nextDouble() * (hi - lo);
            else if (pattern < 30) bx = laneX(rng.nextInt(3));
            else bx = dividerX(1 + rng.nextInt(2)) + (rng.nextDouble() - 0.5) * 16;
            double amp = rng.nextInt(100) < 35 ? 18 + rng.nextInt(22) : 0;
            bx = Math.max(lo + amp, Math.min(hi - amp, bx));
            t.baseX = bx;
            t.x = bx;
            t.wobbleAmp = amp;
            t.wobbleFreq = 0.7 + rng.nextDouble() * 0.7;
            t.wobblePhase = rng.nextDouble() * 6.28;
            if (clearToSpawn(t)) {
                things.add(t);
                return true;
            }
        }
        return false;
    }

    boolean trySpawnBolt() {
        for (int attempt = 0; attempt < 6; attempt++) {
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

    void buildJam(double base) {
        jamBuilt = true;
        double d = base * JAM_WINDOW / 2;
        double cursor = 372;
        int[] pool = {0, 1, 0, 2, 3, 1, 4, 0};
        while (cursor > -160) {
            double maxH = 0;
            java.util.List<Thing> row = new ArrayList<>();
            for (int l = 0; l < 3; l++) {
                int kind = rng.nextInt(14) == 0 ? 5 : pool[rng.nextInt(pool.length)];
                Thing t = Thing.create(kind);
                t.kind = kind;
                t.w = VEH_W[t.kind];
                t.h = VEH_H[t.kind];
                t.color = rng.nextInt(8);
                t.x = t.baseX = laneX(l) + (rng.nextDouble() - 0.5) * 22;
                t.angle = (rng.nextDouble() - 0.5) * 0.1;
                t.jam = true;
                row.add(t);
                maxH = Math.max(maxH, t.h);
            }
            for (int sd = 0; sd < 2; sd++) {
                if (rng.nextInt(10) < 7) {
                    Thing m = Thing.create(6);
                    m.kind = 6;
                    m.w = VEH_W[6];
                    m.h = VEH_H[6];
                    m.color = rng.nextInt(8);
                    m.x = m.baseX = trackX(sd == 0 ? 0 : 4) + (rng.nextDouble() - 0.5) * 8;
                    m.jam = true;
                    row.add(m);
                }
            }
            for (Thing t : row) {
                double finalY = cursor - t.h / 2;
                if (t.kind == 6) finalY = cursor - 30 - rng.nextInt(16);
                t.y0 = finalY - d;
                t.y = t.y0;
                things.add(t);
            }
            cursor -= maxH + 16;
        }
    }

    void endCard(long now) {
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

    void burst(double x, double y, int n, double spMin, double spMax, double angDeg, double spreadDeg) {
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

    void startConfetti(long now) {
        confetti.clear();
        pops.clear();
        lastBurst = now;
        lastConfettiAt = now;
        burst(40, H - 20, 130, 650, 1150, -62, 16);
        burst(W - 40, H - 20, 130, 650, 1150, -118, 16);
    }

    void updateConfetti(long now) {
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

    void startFlight(long now) {
        flightMode = true;
        things.clear();
        birds.clear();
        heliX = 170;
        heliY = 300;
        heliTilt = 0;
        lastBird = now;
        player.invulnerable = 0;
        hp = FLIGHT_START_HP;
    }

    void updateFlight(long now) {
        double dt = Math.min((now - lastFrameAt) / 1000.0, 0.05);
        lastFrameAt = now;
        double flightLen = ROAD_DURATION_MS * 2.0 / 3.0;
        double progress = Math.max(0, Math.min(1, (elapsed - ROAD_DURATION_MS / 3.0) / flightLen));

        double dx = (right ? 1 : 0) - (left ? 1 : 0);
        double dy = (down ? 1 : 0) - (up ? 1 : 0);
        heliX = Math.max(90, Math.min(W - 120, heliX + dx * PLAYER_SPEED * 1.15 * dt));
        heliY = Math.max(80, Math.min(500, heliY + dy * VERTICAL_SPEED * dt));
        heliTilt += (dx * 0.14 - heliTilt) * Math.min(1.0, dt * 8);
        skyScroll += (240 + 120 * progress) * dt;

        boolean canSpawn = elapsed < ROAD_DURATION_MS - 3000;
        if (canSpawn && now - lastBird > 820 - 340 * progress) {
            lastBird = now;
            double speed = 300 + 150 * progress;
            double baseY = 130 + rng.nextInt(340);
            int count = rng.nextInt(5) == 0 ? 3 : 1;
            for (int i = 0; i < count; i++) {
                double off = i - (count - 1) / 2.0;
                birds.add(new Bird(W + 40 + Math.abs(off) * 42, baseY + off * 62, -speed, rng.nextDouble() * 6.28));
            }
        }

        Iterator<Bird> it = birds.iterator();
        while (it.hasNext()) {
            Bird b = it.next();
            b.x += b.vx * dt;
            b.phase += dt * 13;
            double by = b.y + Math.sin(b.phase * 0.35) * 6;
            if (Math.abs(b.x - heliX) < 52 && Math.abs(by - heliY) < 30) {
                if (player.invulnerable <= now) {
                    hp -= 22;
                    player.invulnerable = now + 950;
                    lastHitAt = now;
                }
                it.remove();
            } else if (b.x < -80) {
                it.remove();
            }
        }
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_ESCAPE) {
            handleEscape();
            return;
        }
        if (screen == ROAD) {
            if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) left = true;
            if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) right = true;
            if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) up = true;
            if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) down = true;
        }
        if (screen == TITLE || screen == PAUSE) {
            if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) menuSel = (menuSel + 3) % 4;
            else if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) menuSel = (menuSel + 1) % 4;
            else if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE) activateMenuSel();
            return;
        }
        if (screen == SCENES || screen == HOWTO || screen == INTRO) {
            if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE || key == KeyEvent.VK_BACK_SPACE) {
                screen = backTarget;
                menuSel = 0;
            }
            return;
        }
        if (key != KeyEvent.VK_ENTER && key != KeyEvent.VK_SPACE) return;
        if (screen == ROOM) {
            advanceRoom();
        }
        else if (screen == DIALOG) {
            advanceDialog();
        }
        else if (screen == CARD) {
            if (System.currentTimeMillis() - cardStart > 700) endCard(System.currentTimeMillis());
        }
        else if (screen == ENDING_BE) {
            dialogIndex = retryDialogIndex;   // chơi lại đúng ngày vừa thua
            beginDay();
        }
        else if (screen == ENDING_HE) {
            screen = TITLE;
            menuSel = 0;
            dialogIndex = 0;
            day = 0;
        }
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) left = false;
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) right = false;
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) up = false;
        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) down = false;
    }

    public void keyTyped(KeyEvent e) {
    }

    double laneX(int lane) {
        return ROAD_X + 45 + (ROAD_W - 90) * (2 * lane + 1) / 6.0;
    }

    double dividerX(int i) {
        return ROAD_X + 45 + (ROAD_W - 90) * i / 3.0;
    }
}
