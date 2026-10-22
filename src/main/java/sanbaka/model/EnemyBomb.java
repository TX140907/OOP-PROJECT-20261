package sanbaka.model;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import sanbaka.assets.ArtAssets;
import sanbaka.render.GameVisuals;

/**
 * Trái bom do phi đội máy bay thả xuống ở Đòn 3.
 * Kích thước lớn, rơi chéo xuống phía dưới bên trái.
 * Khi bị phá hủy hoặc chạm đất, phát nổ với bán kính sát thương (blastRadius), tiêu diệt chim và kích nổ dây chuyền bom lân cận.
 */
public class EnemyBomb {
    public double x;
    public double y;
    public double vx;
    public double vy;
    public double w = 38;
    public double h = 38;
    public int hp = 1;
    public boolean active = true;
    public double blastRadius = 76.0;

    public EnemyBomb(double x, double y, double vx, double vy, double blastRadius) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.blastRadius = blastRadius;
    }

    public void update(double dt) {
        this.x += vx * dt;
        this.y += vy * dt;
        if (this.x < -80 || this.y > 620) {
            active = false;
        }
    }

    public void draw(Graphics2D g, GameVisuals scene) {
        if (!active) return;
        long now = System.currentTimeMillis();
        BufferedImage art = ArtAssets.load("Bomb", "Bomb.png");
        if (art != null) {
            g.drawImage(art, (int) (x - w / 2), (int) (y - h / 2), (int) w, (int) h, null);
            return;
        }

        AffineTransform old = g.getTransform();
        g.translate(x, y);

        // Góc xoay theo hướng bay chéo
        double angle = Math.atan2(vy, vx);
        g.rotate(angle);

        // 1. Cánh đuôi quả bom (Tail Fins)
        g.setColor(new Color(28, 30, 36));
        g.fillRect(-22, -14, 9, 28);
        g.fillRect(-20, -10, 8, 20);

        // Khung giằng cánh đuôi
        g.setColor(new Color(55, 60, 70));
        g.drawRect(-22, -14, 9, 28);

        // 2. Thân bom kim loại nặng (Heavy Metal Bomb Casing)
        g.setColor(new Color(45, 50, 60));
        g.fill(new RoundRectangle2D.Double(-16, -11, 26, 22, 10, 10));

        // Dải sọc cảnh báo nguy hiểm vàng - đen ở giữa thân bom
        g.setColor(new Color(245, 200, 30));
        g.fillRect(-4, -11, 10, 22);
        g.setColor(new Color(25, 28, 34));
        g.fillRect(-2, -11, 3, 22);
        g.fillRect(3, -11, 3, 22);

        // 3. Mũi bom tròn bọc thép đỏ đậm (Crimson Armored Warhead)
        Path2D.Double nose = new Path2D.Double();
        nose.moveTo(8, -10);
        nose.quadTo(18, 0, 8, 10);
        nose.closePath();
        g.setColor(new Color(225, 45, 40));
        g.fill(nose);

        // Đầu kích nổ ngòi chạm ở chóp mũi
        g.setColor(new Color(210, 215, 225));
        g.fill(new Ellipse2D.Double(14, -3, 5, 6));

        // 4. Đèn chớp ngòi nổ điện tử nhấp nháy đỏ rực
        boolean blink = (now / 100) % 2 == 0;
        g.setColor(blink ? new Color(255, 35, 30) : new Color(110, 20, 20));
        g.fill(new Ellipse2D.Double(-10, -4, 7, 7));
        g.setColor(new Color(255, 255, 255, blink ? 200 : 50));
        g.fill(new Ellipse2D.Double(-9, -3, 3, 3));

        // 5. Viền nét kim loại chi tiết
        g.setColor(new Color(24, 26, 32));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new RoundRectangle2D.Double(-16, -11, 26, 22, 10, 10));

        g.setTransform(old);
    }
}
