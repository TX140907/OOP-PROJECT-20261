package sanbaka.model;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import sanbaka.assets.ArtAssets;
import sanbaka.render.GameVisuals;
import sanbaka.state.BossConfig;
import sanbaka.state.GameState;

/**
 * Thực thể Trùm Cuối (Boss) trong trận không chiến ở Màn 5.
 * Tạm thời dựng hình người làm placeholder để đội Artist dễ dàng gắn asset ảnh PNG sau.
 * Quản lý vòng đời nạp máu, 3 đòn tấn công luân phiên tuần hoàn, hiệu ứng vụt biến mất/trở lại và vệt mờ motion blur.
 */
public class BossEnemy {
    public static final int STATE_INACTIVE = 0;
    public static final int STATE_WARNING = 1;
    public static final int STATE_ENTERING = 2;
    public static final int STATE_CHARGING = 3;
    public static final int STATE_ATTACK1 = 4;
    public static final int STATE_TRANSITION_SPRAY = 5;
    public static final int STATE_ATTACK2 = 6;
    public static final int STATE_TRANSITION_RETURN = 7;
    public static final int STATE_ATTACK3 = 8;
    public static final int STATE_DEFEATED = 9;

    public int state = STATE_INACTIVE;
    public double x = 1100.0;
    public double y = 280.0;
    public double targetY = 280.0;
    public double vy = 0.0;
    public double w = 70.0;
    public double h = 95.0;

    public int currentHp = 0;
    public int maxHp = 20;
    public double chargeProgress = 0.0;

    public long stateStartedAt = 0;
    public long lastDashPickTime = 0;
    public long lastAimShootTime = 0;
    public long laserAimStart = 0;
    public boolean laserAiming = false;
    public double laserTargetX = 0;
    public double laserTargetY = 0;

    public long lastPlaneSpawn = 0;
    public long lastBirdSpawn = 0;
    public long lastBombSpawn = 0;
    public long lastSquadronSpawn = 0;
    public long lastSprayShoot = 0;

    public long lastHitAt = 0;
    public boolean defeated = false;
    public boolean combatActive = false;

    private final Random rng = new Random();

    // Bộ đệm hiệu ứng tàn ảnh mờ (motion blur / ghost trail) khi lướt nhanh
    public static class Afterimage {
        public double x, y;
        public float alpha;
        public long spawnTime;
        public Afterimage(double x, double y, float alpha, long now) {
            this.x = x;
            this.y = y;
            this.alpha = alpha;
            this.spawnTime = now;
        }
    }
    public final List<Afterimage> afterimages = new ArrayList<>();

    public BossEnemy() {
        this.maxHp = BossConfig.maxHp;
        this.currentHp = 0;
    }

    public void triggerBossSequence(long now) {
        BossConfig.load();
        this.maxHp = BossConfig.maxHp;
        this.currentHp = 0;
        this.chargeProgress = 0.0;
        this.state = STATE_WARNING;
        this.stateStartedAt = now;
        this.x = 1060.0;
        this.y = 280.0;
        this.targetY = 280.0;
        this.defeated = false;
        this.combatActive = false;
        this.laserAiming = false;
        this.afterimages.clear();
    }

    public boolean canPlayerFire(long now) {
        return combatActive && state != STATE_INACTIVE && state != STATE_WARNING
                && state != STATE_ENTERING && state != STATE_CHARGING && state != STATE_DEFEATED;
    }

    public boolean takeMissileHit(int dmg, long now) {
        if (!combatActive || currentHp <= 0 || state == STATE_DEFEATED) return false;
        currentHp = Math.max(0, currentHp - dmg);
        lastHitAt = now;
        if (currentHp <= 0) {
            state = STATE_DEFEATED;
            stateStartedAt = now;
            defeated = true;
            combatActive = false;
            return true; // Boss vừa bị hạ gục
        }
        return false;
    }

    public void addAfterimage(long now, float initialAlpha) {
        afterimages.add(new Afterimage(this.x, this.y, initialAlpha, now));
    }

    public void updateAfterimages(long now) {
        Iterator<Afterimage> it = afterimages.iterator();
        while (it.hasNext()) {
            Afterimage af = it.next();
            long age = now - af.spawnTime;
            if (age > 450) {
                it.remove();
            } else {
                af.alpha = Math.max(0f, 0.45f * (1f - age / 450f));
            }
        }
    }

    public void update(double dt, long now, double heliX, double heliY, GameState gs) {
        if (state == STATE_INACTIVE) return;
        updateAfterimages(now);

        switch (state) {
            case STATE_WARNING: {
                if (now - stateStartedAt >= BossConfig.warningFlashMs) {
                    state = STATE_ENTERING;
                    stateStartedAt = now;
                    x = 1060.0;
                    y = 280.0;
                    targetY = 280.0;
                }
                break;
            }
            case STATE_ENTERING: {
                double targetX = 830.0;
                if (x > targetX) {
                    x -= BossConfig.dashSpeed * dt * 1.3;
                    if (rng.nextDouble() < 0.35) addAfterimage(now, 0.4f);
                    if (x <= targetX) {
                        x = targetX;
                        state = STATE_CHARGING;
                        stateStartedAt = now;
                        chargeProgress = 0.0;
                    }
                } else {
                    x = targetX;
                    state = STATE_CHARGING;
                    stateStartedAt = now;
                }
                break;
            }
            case STATE_CHARGING: {
                chargeProgress = Math.min(1.0, (now - stateStartedAt) / (double) BossConfig.introChargeMs);
                currentHp = (int) Math.round(chargeProgress * maxHp);
                if (chargeProgress >= 1.0) {
                    currentHp = maxHp;
                    combatActive = true;
                    state = STATE_ATTACK1;
                    stateStartedAt = now;
                    lastDashPickTime = now;
                    lastPlaneSpawn = now;
                    lastAimShootTime = now;
                }
                break;
            }
            case STATE_ATTACK1: {
                // Đảo vị trí nhanh theo trục Y
                updateYMovement(dt, now);

                // Sinh máy bay yểm trợ ngược chiều người chơi + chim ở chỗ trống
                if (now - lastPlaneSpawn >= BossConfig.attack1PlaneIntervalMs) {
                    lastPlaneSpawn = now;
                    double planeY = pickPlaneY(y);
                    EnemyPlane plane = new EnemyPlane(980, planeY, BossConfig.enemyPlaneSpeed, BossConfig.enemyPlaneHp, false);
                    gs.enemyPlanes.add(plane);
                    // Nhét chim vào các khe trống để tăng tính thử thách
                    if (rng.nextDouble() < 0.55) {
                        double birdY = (planeY < 290) ? (planeY + 115 + rng.nextInt(60)) : (planeY - 115 - rng.nextInt(60));
                        birdY = Math.max(80, Math.min(490, birdY));
                        gs.birds.add(new Bird(1020, birdY, -BossConfig.enemyPlaneSpeed * 1.1, rng.nextDouble() * 6.28));
                    }
                }

                // Boss ngắm bắn tỉa: cảnh báo tia laser + bắn viên đạn năng lượng
                long aimElapsed = now - lastAimShootTime;
                if (!laserAiming && aimElapsed >= BossConfig.attack1AimShootIntervalMs - BossConfig.attack1LaserWarningMs) {
                    laserAiming = true;
                    laserAimStart = now;
                    laserTargetX = heliX;
                    laserTargetY = heliY;
                }
                if (laserAiming) {
                    if (now - laserAimStart < BossConfig.attack1LaserWarningMs * 0.72) {
                        laserTargetX = heliX;
                        laserTargetY = heliY;
                    }
                    if (aimElapsed >= BossConfig.attack1AimShootIntervalMs) {
                        laserAiming = false;
                        lastAimShootTime = now;
                        double dx = laserTargetX - (x - 20);
                        double dy = laserTargetY - y;
                        double dist = Math.hypot(dx, dy);
                        if (dist > 1) {
                            double bvx = (dx / dist) * BossConfig.bossAimBulletSpeed;
                            double bvy = (dy / dist) * BossConfig.bossAimBulletSpeed;
                            gs.bossBullets.add(new BossBullet(x - 20, y, bvx, bvy, BossBullet.TYPE_AIMED));
                        }
                    }
                }

                // Hết thời gian Đòn 1 -> chuyển giao
                if (now - stateStartedAt >= BossConfig.attack1DurationMs) {
                    state = STATE_TRANSITION_SPRAY;
                    stateStartedAt = now;
                    laserAiming = false;
                    lastSprayShoot = 0;
                }
                break;
            }
            case STATE_TRANSITION_SPRAY: {
                updateYMovement(dt, now);
                // Bắn đạn loạn xạ về các hướng ngẫu nhiên
                if (now - lastSprayShoot >= 200) {
                    lastSprayShoot = now;
                    double angle = Math.PI + (rng.nextDouble() - 0.5) * 1.35;
                    double spd = 360 + rng.nextInt(120);
                    gs.bossBullets.add(new BossBullet(x - 16, y, Math.cos(angle) * spd, Math.sin(angle) * spd, BossBullet.TYPE_SPRAY));
                    addAfterimage(now, 0.45f);
                }

                // Nửa sau của pha: Boss vụt lùi nhanh về bên phải màn hình
                long sprayElapsed = now - stateStartedAt;
                if (sprayElapsed > BossConfig.transitionSprayDurationMs * 0.45) {
                    x += BossConfig.retreatSpeed * dt;
                    if (rng.nextDouble() < 0.6) addAfterimage(now, 0.5f);
                }
                if (sprayElapsed >= BossConfig.transitionSprayDurationMs) {
                    state = STATE_ATTACK2;
                    stateStartedAt = now;
                    x = 1100;
                    lastBirdSpawn = now;
                }
                break;
            }
            case STATE_ATTACK2: {
                // Đòn 2: Triệu hồi bão chim tốc độ cao & dày đặc (buộc người chơi dùng phím E bắn phá dọn đường)
                long flockElapsed = now - stateStartedAt;
                if (flockElapsed >= BossConfig.attack2WarningMs) {
                    if (now - lastBirdSpawn >= BossConfig.attack2BirdIntervalMs) {
                        lastBirdSpawn = now;
                        // Phủ kín 3-4 dải độ cao cùng lúc, tạo màn bão chim dày đặc
                        double[] yBands = {
                            75 + rng.nextInt(85),
                            170 + rng.nextInt(85),
                            275 + rng.nextInt(90),
                            385 + rng.nextInt(95)
                        };
                        int skipBand = rng.nextInt(4); // 1 dải có thể ngẫu nhiên để hở lối hẹp
                        for (int b = 0; b < 4; b++) {
                            if (b == skipBand && rng.nextDouble() < 0.65) continue;
                            double spawnX = 1000 + rng.nextInt(70);
                            double bSpeed = BossConfig.attack2BirdSpeed * (0.95 + rng.nextDouble() * 0.12);
                            gs.birds.add(new Bird(spawnX, yBands[b], -bSpeed, rng.nextDouble() * 6.28));
                        }
                    }
                }
                if (flockElapsed >= BossConfig.attack2DurationMs) {
                    state = STATE_TRANSITION_RETURN;
                    stateStartedAt = now;
                    x = 1060;
                    y = 280;
                    targetY = 280;
                    lastSprayShoot = 0;
                }
                break;
            }
            case STATE_TRANSITION_RETURN: {
                // Boss quay trở lại với hiệu ứng vụt + bắn đạn loạn xạ
                double targetX = 830.0;
                if (x > targetX) {
                    x -= BossConfig.dashSpeed * 1.6 * dt;
                    addAfterimage(now, 0.5f);
                }
                if (now - lastSprayShoot >= 220) {
                    lastSprayShoot = now;
                    double angle = Math.PI + (rng.nextDouble() - 0.5) * 1.25;
                    double spd = 350 + rng.nextInt(110);
                    gs.bossBullets.add(new BossBullet(x - 16, y, Math.cos(angle) * spd, Math.sin(angle) * spd, BossBullet.TYPE_SPRAY));
                }
                if (now - stateStartedAt >= BossConfig.attack3IntroSprayMs && x <= targetX + 10) {
                    x = targetX;
                    state = STATE_ATTACK3;
                    stateStartedAt = now;
                    lastSquadronSpawn = now;
                    lastBombSpawn = now;
                    lastAimShootTime = now;
                }
                break;
            }
            case STATE_ATTACK3: {
                updateYMovement(dt, now);

                // Máy bay vút qua hàng trên cùng (y = 45..85), đồng thời cắt thêm bom
                // Phi đội tiêm kích vút qua hàng trên cùng (y = 45..85)
                if (now - lastSquadronSpawn >= BossConfig.attack3SquadronIntervalMs) {
                    lastSquadronSpawn = now;
                    double sqY = 46 + rng.nextInt(40);
                    EnemyPlane sqPlane = new EnemyPlane(1000, sqY, BossConfig.enemyPlaneSpeed * 1.5, 1, true);
                    gs.enemyPlanes.add(sqPlane);
                }

                // Bom rơi chéo thưa hơn nhưng kích cỡ to và bán kính nổ lớn
                if (now - lastBombSpawn >= BossConfig.attack3BombIntervalMs) {
                    lastBombSpawn = now;
                    double bombX = 360 + rng.nextInt(560);
                    double bombY = 55 + rng.nextInt(25);
                    double bvx = BossConfig.enemyBombVx + (rng.nextDouble() - 0.5) * 25.0;
                    double bvy = BossConfig.enemyBombVy + (rng.nextDouble() - 0.5) * 20.0;
                    gs.enemyBombs.add(new EnemyBomb(bombX, bombY, bvx, bvy, BossConfig.enemyBombBlastRadius));
                }

                // Boss ngắm bắn tỉa thi thoảng giống đòn 1
                long aimElapsed = now - lastAimShootTime;
                if (!laserAiming && aimElapsed >= BossConfig.attack1AimShootIntervalMs - BossConfig.attack1LaserWarningMs) {
                    laserAiming = true;
                    laserAimStart = now;
                    laserTargetX = heliX;
                    laserTargetY = heliY;
                }
                if (laserAiming) {
                    if (now - laserAimStart < BossConfig.attack1LaserWarningMs * 0.72) {
                        laserTargetX = heliX;
                        laserTargetY = heliY;
                    }
                    if (aimElapsed >= BossConfig.attack1AimShootIntervalMs) {
                        laserAiming = false;
                        lastAimShootTime = now;
                        double dx = laserTargetX - (x - 20);
                        double dy = laserTargetY - y;
                        double dist = Math.hypot(dx, dy);
                        if (dist > 1) {
                            double bvx = (dx / dist) * BossConfig.bossAimBulletSpeed;
                            double bvy = (dy / dist) * BossConfig.bossAimBulletSpeed;
                            gs.bossBullets.add(new BossBullet(x - 20, y, bvx, bvy, BossBullet.TYPE_AIMED));
                        }
                    }
                }

                // Hết Đòn 3 -> Tuần hoàn quay trở lại Đòn 1!
                if (now - stateStartedAt >= BossConfig.attack3DurationMs) {
                    state = STATE_ATTACK1;
                    stateStartedAt = now;
                    laserAiming = false;
                    lastPlaneSpawn = now;
                    lastAimShootTime = now;
                }
                break;
            }
            case STATE_DEFEATED: {
                x += BossConfig.retreatSpeed * 1.35 * dt;
                addAfterimage(now, 0.55f);
                break;
            }
        }
    }

    private void updateYMovement(double dt, long now) {
        if (now - lastDashPickTime > 650 || Math.abs(y - targetY) < 12) {
            lastDashPickTime = now;
            targetY = BossConfig.minY + rng.nextDouble() * (BossConfig.maxY - BossConfig.minY);
        }
        double diff = targetY - y;
        double step = BossConfig.dashSpeed * dt;
        if (Math.abs(diff) <= step) {
            y = targetY;
        } else {
            y += Math.signum(diff) * step;
            if (rng.nextDouble() < 0.3) addAfterimage(now, 0.32f);
        }
    }

    private double pickPlaneY(double bossY) {
        // Chừa khe ngắm bắn Boss cho người chơi
        double py;
        for (int i = 0; i < 5; i++) {
            py = 110 + rng.nextInt(370);
            if (Math.abs(py - bossY) > 65) return py;
        }
        return (bossY < 300) ? (bossY + 120) : (bossY - 120);
    }

    public void draw(Graphics2D g, GameVisuals scene, double heliX, double heliY) {
        if (state == STATE_INACTIVE) return;
        long now = System.currentTimeMillis();

        // 1. Cảnh báo toàn màn hình khi Boss chuẩn bị xuất hiện
        if (state == STATE_WARNING) {
            drawWarningFlash(g, now);
            return;
        }

        // 2. Vẽ hình vuông đen siêu nhạt biểu thị quá trình vụt đi mất (Đòn 1 sang Đòn 2)
        if (state == STATE_TRANSITION_SPRAY && (now - stateStartedAt > BossConfig.transitionSprayDurationMs * 0.45)) {
            Composite oldComp = g.getComposite();
            g.setColor(new Color(15, 18, 26, 38)); // Siêu nhạt
            g.fill(new RoundRectangle2D.Double(x - 70, y - h / 2 - 12, 140, h + 24, 14, 14));
            g.setComposite(oldComp);
        }

        // 3. Vẽ các tàn ảnh lướt nhanh (Motion Blur Afterimages)
        for (Afterimage af : afterimages) {
            if (af.alpha <= 0.01f) continue;
            Composite oldComp = g.getComposite();
            g.setComposite(AlphaComposite.SrcOver.derive(af.alpha));
            drawBossFigure(g, af.x, af.y, now, true);
            g.setComposite(oldComp);
        }

        // 4. Vẽ tia laser ngắm bắn cảnh báo trước khi Boss nhả đạn
        if (laserAiming && (state == STATE_ATTACK1 || state == STATE_ATTACK3)) {
            drawAimLaser(g, laserTargetX, laserTargetY, now);
        }

        // 5. Vẽ thực thể chính của Boss (nếu đang ở trong màn hình)
        if (x < 1040 && state != STATE_ATTACK2) {
            drawBossFigure(g, x, y, now, false);
            // Dấu chấm than cảnh báo cạnh Boss khi chuẩn bị lướt biến mất
            if (state == STATE_TRANSITION_SPRAY) {
                drawWarningExclamation(g, x - 28, y - 38, now);
            }
        }

        // 6. Cảnh báo chấm than to trước Đòn 2 (Bão chim)
        if (state == STATE_ATTACK2 && (now - stateStartedAt < BossConfig.attack2WarningMs)) {
            drawBigFlockWarning(g, now);
        }

        // 7. Vẽ thanh máu Boss trên đỉnh màn hình (20 vạch / phân đoạn)
        if (state != STATE_DEFEATED && state != STATE_INACTIVE) {
            drawTopHealthBar(g, now);
        }

        // 8. Nút [E] nhấp nháy cạnh người chơi gợi ý dùng phím E để bắn tên lửa
        if (canPlayerFire(now)) {
            drawKeyEPrompt(g, heliX, heliY, now);
        }
    }

    private void drawWarningFlash(Graphics2D g, long now) {
        boolean flash = (now / 140) % 2 == 0;
        if (flash) {
            Composite old = g.getComposite();
            g.setColor(new Color(255, 30, 30, 48));
            g.fillRect(0, 0, 960, 600);
            g.setComposite(old);
        }
        // Viền đỏ cảnh báo
        g.setColor(flash ? new Color(255, 45, 45, 230) : new Color(255, 140, 30, 200));
        g.setStroke(new BasicStroke(6f));
        g.drawRect(3, 3, 954, 594);

        // Biển cảnh báo Boss xuất hiện
        int cx = 480, cy = 180;
        String title = "CẢNH BÁO: TRÙM CUỐI ĐANG TIẾP CẬN!";
        String sub = "HÃY CHUẨN BỊ CHIẾN ĐẤU!";
        g.setFont(new Font("Dialog", Font.BOLD, 18));
        int tw = g.getFontMetrics().stringWidth(title);
        g.setColor(new Color(25, 28, 38, 220));
        g.fillRoundRect(cx - tw / 2 - 28, cy - 28, tw + 56, 56, 16, 16);
        g.setColor(flash ? new Color(255, 60, 60) : new Color(255, 200, 40));
        g.drawString(title, cx - tw / 2, cy - 2);
        g.setFont(new Font("Dialog", Font.PLAIN, 12));
        g.setColor(Color.WHITE);
        g.drawString(sub, cx - g.getFontMetrics().stringWidth(sub) / 2, cy + 18);
    }

    private void drawTopHealthBar(Graphics2D g, long now) {
        int barW = 420;
        int barH = 14;
        int bx = 480 - barW / 2;
        int by = 34;

        // Khung nền
        g.setColor(new Color(24, 28, 38, 220));
        g.fillRoundRect(bx - 12, by - 22, barW + 24, barH + 34, 12, 12);
        g.setColor(new Color(80, 90, 110, 180));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(bx - 12, by - 22, barW + 24, barH + 34, 12, 12);

        // Nhãn tên Boss
        g.setFont(new Font("Dialog", Font.BOLD, 12));
        g.setColor(new Color(255, 220, 120));
        String label = (state == STATE_CHARGING)
                ? "TRÙM CUỐI: GIÁM THỊ TỐI CAO [ĐANG NẠP MÁU...]"
                : "TRÙM CUỐI: GIÁM THỊ TỐI CAO";
        g.drawString(label, bx, by - 7);

        // Số HP
        String hpStr = currentHp + " / " + maxHp;
        g.setFont(new Font("Dialog", Font.BOLD, 11));
        g.setColor(Color.WHITE);
        g.drawString(hpStr, bx + barW - g.getFontMetrics().stringWidth(hpStr), by - 7);

        // Vẽ các vạch phân đoạn của cây máu (20 vạch tương ứng 20 HP)
        int segCount = Math.max(1, maxHp);
        double segSpacing = 2.0;
        double totalSpacing = (segCount - 1) * segSpacing;
        double segW = (barW - totalSpacing) / segCount;

        for (int i = 0; i < segCount; i++) {
            double sx = bx + i * (segW + segSpacing);
            if (i < currentHp) {
                // Vạch còn máu: Gradient đỏ rực sang vàng cam
                g.setColor(new Color(235, 50, 45));
                g.fill(new RoundRectangle2D.Double(sx, by, segW, barH, 3, 3));
                g.setColor(new Color(255, 175, 40));
                g.fill(new Rectangle2D.Double(sx, by, segW, 3));
            } else {
                // Vạch đã mất máu: Xám đen rỗng
                g.setColor(new Color(48, 54, 68));
                g.fill(new RoundRectangle2D.Double(sx, by, segW, barH, 3, 3));
            }
        }
    }

    private void drawKeyEPrompt(Graphics2D g, double heliX, double heliY, long now) {
        boolean blink = (now / 220) % 2 == 0;
        double px = heliX + 44;
        double py = heliY - 26;

        AffineTransform old = g.getTransform();
        g.translate(px, py);

        // Nút phím E vuông nhấp nháy cạnh trực thăng
        g.setColor(blink ? new Color(255, 215, 0, 240) : new Color(255, 140, 0, 220));
        g.fillRoundRect(0, 0, 26, 26, 7, 7);
        g.setColor(new Color(40, 24, 0));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(0, 0, 26, 26, 7, 7);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Dialog", Font.BOLD, 15));
        g.drawString("E", 7, 19);

        // Dòng chữ phụ chú thích
        g.setFont(new Font("Dialog", Font.BOLD, 10));
        g.setColor(blink ? Color.WHITE : new Color(255, 230, 160));
        g.drawString("BẮN TÊN LỬA", 30, 17);

        g.setTransform(old);
    }

    private void drawAimLaser(Graphics2D g, double targetX, double targetY, long now) {
        boolean blink = (now / 45) % 2 == 0;
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(blink ? 2.5f : 1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(blink ? new Color(255, 40, 40, 230) : new Color(255, 120, 40, 160));
        g.draw(new Line2D.Double(x - 16, y - 4, targetX, targetY));
        g.setColor(new Color(255, 240, 180));
        g.fill(new Ellipse2D.Double(targetX - 4, targetY - 4, 8, 8));
        g.setStroke(old);
    }

    private void drawWarningExclamation(Graphics2D g, double bx, double by, long now) {
        boolean blink = (now / 90) % 2 == 0;
        g.setColor(blink ? new Color(255, 45, 45, 240) : new Color(255, 210, 40, 220));
        g.fill(new RoundRectangle2D.Double(bx - 10, by - 14, 20, 28, 8, 8));
        g.setColor(Color.WHITE);
        g.setFont(new Font("Dialog", Font.BOLD, 17));
        g.drawString("!", (int) (bx - 4), (int) (by + 7));
    }

    private void drawBigFlockWarning(Graphics2D g, long now) {
        boolean blink = (now / 110) % 2 == 0;
        int cx = 480, cy = 200;
        String warnText = "CẢNH BÁO BÃO CHIM CẤP ĐỘ CAO · BẤM [E] BẮN PHÁ MỞ ĐƯỜNG!";
        g.setFont(new Font("Dialog", Font.BOLD, 14));
        int tw = g.getFontMetrics().stringWidth(warnText);

        g.setColor(blink ? new Color(245, 40, 40, 225) : new Color(255, 170, 30, 225));
        g.fillRoundRect(cx - tw / 2 - 24, cy - 18, tw + 48, 36, 16, 16);
        g.setColor(Color.WHITE);
        g.drawString(warnText, cx - tw / 2, cy + 6);

        // Chấm than khổng lồ
        g.setFont(new Font("Dialog", Font.BOLD, 28));
        g.drawString("!", cx - tw / 2 - 17, cy + 10);
        g.drawString("!", cx + tw / 2 + 7, cy + 10);
    }

    private void drawBossFigure(Graphics2D g, double bx, double by, long now, boolean ghost) {
        BufferedImage art = ArtAssets.load("Boss", "Boss.png");
        if (art != null) {
            g.drawImage(art, (int) (bx - w / 2), (int) (by - h / 2), (int) w, (int) h, null);
            return;
        }

        AffineTransform old = g.getTransform();
        g.translate(bx, by);

        boolean hit = lastHitAt > 0 && now - lastHitAt < 160;
        double floatOffset = Math.sin(now / 180.0) * 3.5;

        // 1. Vầng hào quang năng lượng bao quanh Boss
        if (!ghost) {
            float pulse = 0.5f + 0.5f * (float) Math.sin(now / 110.0);
            int auraAlpha = (int) (35 + 35 * pulse);
            g.setColor(new Color(255, 60, 40, auraAlpha));
            g.fill(new Ellipse2D.Double(-w / 2 - 14, -h / 2 - 14 + floatOffset, w + 28, h + 28));
            g.setColor(new Color(255, 170, 40, auraAlpha / 2));
            g.fill(new Ellipse2D.Double(-w / 2 - 22, -h / 2 - 22 + floatOffset, w + 44, h + 44));
        }

        // 2. Áo choàng tung bay phía sau
        Path2D.Double cape = new Path2D.Double();
        double capeWiggle = Math.sin(now / 90.0) * 8.0;
        cape.moveTo(-8, -22 + floatOffset);
        cape.lineTo(w / 2 + 10, -10 + floatOffset + capeWiggle * 0.5);
        cape.lineTo(w / 2 + 18, 35 + floatOffset + capeWiggle);
        cape.lineTo(-4, 38 + floatOffset);
        cape.closePath();
        g.setColor(hit ? Color.WHITE : (ghost ? new Color(60, 20, 20) : new Color(130, 28, 28)));
        g.fill(cape);

        // 3. Thân người (Áo vest / Măng tô trang nghiêm)
        g.setColor(hit ? Color.WHITE : (ghost ? new Color(30, 30, 40) : new Color(34, 38, 48)));
        g.fill(new RoundRectangle2D.Double(-18, -24 + floatOffset, 36, 62, 10, 10));

        // Cà vạt đỏ thẫm
        Path2D.Double tie = new Path2D.Double();
        tie.moveTo(0, -20 + floatOffset);
        tie.lineTo(4, -14 + floatOffset);
        tie.lineTo(2, 6 + floatOffset);
        tie.lineTo(0, 10 + floatOffset);
        tie.lineTo(-2, 6 + floatOffset);
        tie.lineTo(-4, -14 + floatOffset);
        tie.closePath();
        g.setColor(hit ? Color.WHITE : new Color(215, 38, 48));
        g.fill(tie);

        // Cổ áo sơ mi trắng
        Path2D.Double collar = new Path2D.Double();
        collar.moveTo(-8, -24 + floatOffset);
        collar.lineTo(0, -16 + floatOffset);
        collar.lineTo(8, -24 + floatOffset);
        collar.lineTo(4, -26 + floatOffset);
        collar.lineTo(-4, -26 + floatOffset);
        collar.closePath();
        g.setColor(Color.WHITE);
        g.fill(collar);

        // 4. Đầu & Khuôn mặt giám thị / người bí ẩn
        g.setColor(hit ? Color.WHITE : new Color(255, 218, 185));
        g.fill(new Ellipse2D.Double(-12, -44 + floatOffset, 24, 24));

        // Mái tóc đen vuốt ngược
        Path2D.Double hair = new Path2D.Double();
        hair.moveTo(-13, -36 + floatOffset);
        hair.quadTo(-14, -48 + floatOffset, 0, -48 + floatOffset);
        hair.quadTo(14, -48 + floatOffset, 13, -36 + floatOffset);
        hair.lineTo(11, -40 + floatOffset);
        hair.lineTo(0, -44 + floatOffset);
        hair.lineTo(-11, -40 + floatOffset);
        hair.closePath();
        g.setColor(hit ? Color.WHITE : new Color(24, 26, 32));
        g.fill(hair);

        // Đôi mắt phát sáng rực lửa quyết tâm
        g.setColor(hit ? Color.WHITE : new Color(255, 35, 35));
        g.fill(new Ellipse2D.Double(-8, -34 + floatOffset, 4, 3));
        g.fill(new Ellipse2D.Double(2, -34 + floatOffset, 4, 3));

        // Kính gọng vàng quyền lực
        g.setColor(hit ? Color.WHITE : new Color(255, 215, 50));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new Rectangle2D.Double(-9, -36 + floatOffset, 6, 5));
        g.draw(new Rectangle2D.Double(1, -36 + floatOffset, 6, 5));
        g.draw(new Line2D.Double(-3, -34 + floatOffset, 1, -34 + floatOffset));

        // 5. Tay và cầu năng lượng trên bàn tay
        g.setColor(hit ? Color.WHITE : new Color(34, 38, 48));
        g.fill(new Ellipse2D.Double(-24, -6 + floatOffset, 10, 10));
        g.setColor(new Color(255, 80, 40, 220));
        g.fill(new Ellipse2D.Double(-27, -9 + floatOffset, 16, 16));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(-23, -5 + floatOffset, 8, 8));

        g.setTransform(old);
    }
}

