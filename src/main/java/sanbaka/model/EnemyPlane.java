package sanbaka.model;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import sanbaka.assets.ArtAssets;
import sanbaka.render.GameVisuals;
import sanbaka.state.BossConfig;

/**
 * Máy bay tiêm kích chiến đấu yểm trợ của Boss ở Màn 5 (Đòn 1 và Đòn 3).
 * Thiết kế kiểu dáng tiêm kích siêu âm, kích thước lớn tương xứng (nhỏ hơn trực thăng người chơi một xíu).
 * Cần 3 phát tên lửa để bị tiêu diệt.
 */
public class EnemyPlane {
    public double x;
    public double y;
    public double vx;
    public double vy;
    public double w = 86;
    public double h = 44;
    public int hp = 3;
    public int maxHp = 3;
    public boolean hasShot = false;
    public boolean active = true;
    public boolean isFlybySquadron = false; // Thuộc phi đội bay vút qua hàng trên ở Đòn 3
    public long hitFlashUntil = 0;

    public EnemyPlane(double x, double y, double speed, int hp, boolean isFlyby) {
        this.x = x;
        this.y = y;
        this.vx = -speed;
        this.vy = 0;
        this.w = BossConfig.enemyPlaneWidth > 0 ? BossConfig.enemyPlaneWidth : 86;
        this.h = BossConfig.enemyPlaneHeight > 0 ? BossConfig.enemyPlaneHeight : 44;
        this.hp = hp;
        this.maxHp = hp;
        this.isFlybySquadron = isFlyby;
    }

    public void update(double dt) {
        this.x += vx * dt;
        this.y += vy * dt;
        if (this.x < -160) {
            active = false;
        }
    }

    public boolean takeHit(int damage, long now) {
        this.hp -= damage;
        this.hitFlashUntil = now + 140;
        if (this.hp <= 0) {
            this.active = false;
            return true; // Bị tiêu diệt
        }
        return false;
    }

    public void draw(Graphics2D g, GameVisuals scene) {
        if (!active) return;
        long now = System.currentTimeMillis();
        BufferedImage art = ArtAssets.load("EnemyPlane", "EnemyPlane.png");
        if (art != null) {
            if (hitFlashUntil > now) {
                // Nhấp nháy khi bị bắn trúng
                g.drawImage(art, (int) (x - w / 2), (int) (y - h / 2), (int) w, (int) h, null);
                g.setColor(new Color(255, 255, 255, 150));
                g.fill(new RoundRectangle2D.Double(x - w / 2, y - h / 2, w, h, 8, 8));
            } else {
                g.drawImage(art, (int) (x - w / 2), (int) (y - h / 2), (int) w, (int) h, null);
            }
            drawHealthBar(g);
            return;
        }

        AffineTransform old = g.getTransform();
        g.translate(x, y);

        boolean flash = hitFlashUntil > now;
        Color bodyColor = flash ? Color.WHITE : (isFlybySquadron ? new Color(52, 60, 74) : new Color(42, 48, 62));
        Color darkAccent = flash ? Color.WHITE : (isFlybySquadron ? new Color(38, 44, 56) : new Color(28, 32, 42));
        Color wingColor = flash ? Color.WHITE : (isFlybySquadron ? new Color(48, 55, 68) : new Color(36, 42, 54));
        Color canopy = flash ? Color.WHITE : new Color(65, 195, 245, 235);

        // 1. Lửa phản lực kép (Twin Jet Engine Afterburners) phía sau (+x)
        double flameL = 16.0 + Math.sin(now / 22.0) * 5.0;
        double flameW = 4.5;
        double[] nozzleYs = {-5.5, 5.5};
        for (double ny : nozzleYs) {
            // Vệt lửa ngoài màu đỏ cam rực rỡ
            Path2D.Double flameOut = new Path2D.Double();
            flameOut.moveTo(w / 2 - 2, ny - flameW);
            flameOut.lineTo(w / 2 + flameL, ny);
            flameOut.lineTo(w / 2 - 2, ny + flameW);
            flameOut.closePath();
            g.setColor(new Color(255, 95, 20, 225));
            g.fill(flameOut);

            // Lõi lửa trong vàng trắng rực sáng
            Path2D.Double flameCore = new Path2D.Double();
            flameCore.moveTo(w / 2 - 2, ny - flameW * 0.55);
            flameCore.lineTo(w / 2 + flameL * 0.62, ny);
            flameCore.lineTo(w / 2 - 2, ny + flameW * 0.55);
            flameCore.closePath();
            g.setColor(new Color(255, 245, 120, 245));
            g.fill(flameCore);
        }

        // 2. Cánh tam giác lùi quét góc rộng (Swept-back Delta Wings)
        Path2D.Double wings = new Path2D.Double();
        wings.moveTo(-10, -h / 2);        // Mút cánh trên
        wings.lineTo(12, -h / 2);         // Mép sau mút cánh
        wings.lineTo(26, -9);             // Gốc cánh sau
        wings.lineTo(26, 9);              // Gốc cánh dưới sau
        wings.lineTo(12, h / 2);          // Mép sau mút cánh dưới
        wings.lineTo(-10, h / 2);         // Mút cánh dưới
        wings.lineTo(-16, 12);            // Gốc cánh trước dưới
        wings.lineTo(-24, 6);             // Cạnh trước hút gió
        wings.lineTo(-24, -6);
        wings.lineTo(-16, -12);           // Gốc cánh trước trên
        wings.closePath();
        g.setColor(wingColor);
        g.fill(wings);

        // Viền cánh và sọc nhận diện quân sự ở đầu cánh
        g.setColor(darkAccent);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(wings);

        // Ray treo tên lửa đầu cánh (Wingtip Missile Pylons)
        g.setColor(flash ? Color.WHITE : new Color(22, 25, 32));
        g.fill(new RoundRectangle2D.Double(-12, -h / 2 - 2, 22, 3, 2, 2));
        g.fill(new RoundRectangle2D.Double(-12, h / 2 - 1, 22, 3, 2, 2));
        // Đầu tên lửa nhỏ màu đỏ
        g.setColor(new Color(230, 45, 40));
        g.fillRect(-14, (int) (-h / 2 - 2), 3, 3);
        g.fillRect(-14, (int) (h / 2 - 1), 3, 3);

        // 3. Thân máy bay khí động học tiêm kích (Sleek Supersonic Fuselage)
        Path2D.Double fuselage = new Path2D.Double();
        fuselage.moveTo(-w / 2, 0);                 // Mũi tiêm kích
        fuselage.lineTo(-w / 2 + 16, -5.5);
        fuselage.lineTo(-w / 2 + 32, -8.0);
        fuselage.lineTo(w / 2 - 6, -7.5);
        fuselage.lineTo(w / 2 - 1, -6.5);
        fuselage.lineTo(w / 2 - 1, 6.5);
        fuselage.lineTo(w / 2 - 6, 7.5);
        fuselage.lineTo(-w / 2 + 32, 8.0);
        fuselage.lineTo(-w / 2 + 16, 5.5);
        fuselage.closePath();
        g.setColor(bodyColor);
        g.fill(fuselage);

        // Ống pitot nhọn hoắt ở mũi
        g.setColor(flash ? Color.WHITE : new Color(180, 190, 205));
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(-w / 2, 0, -w / 2 - 7, 0));

        // 4. Họng hút khí phản lực hai bên sườn (Jet Engine Intakes)
        g.setColor(darkAccent);
        g.fill(new RoundRectangle2D.Double(-14, -8.5, 18, 3.5, 2, 2));
        g.fill(new RoundRectangle2D.Double(-14, 5.0, 18, 3.5, 2, 2));

        // 5. Cánh đuôi đứng kép chếch góc (Twin Canted Vertical Stabilizers)
        Path2D.Double finTop = new Path2D.Double();
        finTop.moveTo(8, -7);
        finTop.lineTo(24, -18);
        finTop.lineTo(34, -18);
        finTop.lineTo(28, -7);
        finTop.closePath();
        g.setColor(darkAccent);
        g.fill(finTop);

        Path2D.Double finBot = new Path2D.Double();
        finBot.moveTo(8, 7);
        finBot.lineTo(24, 18);
        finBot.lineTo(34, 18);
        finBot.lineTo(28, 7);
        finBot.closePath();
        g.setColor(darkAccent);
        g.fill(finBot);

        // 6. Buồng lái kính phản quang phi công (Cockpit Canopy)
        Path2D.Double cockpit = new Path2D.Double();
        cockpit.moveTo(-w / 2 + 18, -2.5);
        cockpit.lineTo(-w / 2 + 32, -4.5);
        cockpit.lineTo(-w / 2 + 42, -2.5);
        cockpit.lineTo(-w / 2 + 28, 0);
        cockpit.closePath();
        g.setColor(canopy);
        g.fill(cockpit);

        // Vệt sáng phản quang trên kính buồng lái
        g.setColor(new Color(255, 255, 255, 180));
        g.setStroke(new BasicStroke(1.0f));
        g.draw(new Line2D.Double(-w / 2 + 22, -2.8, -w / 2 + 35, -3.8));

        // 7. Ống xả động cơ phản lực kép kim loại ở đuôi
        g.setColor(new Color(22, 24, 30));
        g.fillRect((int) (w / 2 - 4), -8, 4, 5);
        g.fillRect((int) (w / 2 - 4), 3, 4, 5);

        g.setTransform(old);

        drawHealthBar(g);
    }

    private void drawHealthBar(Graphics2D g) {
        if (isFlybySquadron || hp >= maxHp) return;
        int barW = 46, barH = 5;
        int bx = (int) (x - barW / 2);
        int by = (int) (y - h / 2 - 10);
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(bx - 1, by - 1, barW + 2, barH + 2, 4, 4);
        int fill = (int) (barW * ((double) hp / maxHp));
        g.setColor(hp == 2 ? new Color(245, 190, 45) : new Color(235, 60, 50));
        g.fillRoundRect(bx, by, fill, barH, 3, 3);
        g.setColor(new Color(255, 255, 255, 120));
        g.fillRect(bx, by, fill, 1);
    }
}
