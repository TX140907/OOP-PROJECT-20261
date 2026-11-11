package sanbaka.model;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import sanbaka.assets.ArtAssets;
import sanbaka.render.GameVisuals;

/**
 * Viên đạn năng lượng do Boss hoặc máy bay địch bắn ra trong Màn 5.
 * Hỗ trợ gắn ảnh riêng tại: assets/objects/BossBullet/ (BossBullet_aimed.png, EnemyBullet.png, BossBullet_spray.png hoặc BossBullet.png).
 */
public class BossBullet {
    public static final int TYPE_AIMED = 0;          // Đạn ngắm bắn tỉa từ Boss
    public static final int TYPE_PLANE_STRAIGHT = 1; // Đạn máy bay địch bắn thẳng
    public static final int TYPE_SPRAY = 2;          // Đạn bắn loạn xạ giải tỏa khu vực

    public double x;
    public double y;
    public double vx;
    public double vy;
    public int kind;
    public double radius = 7.0;
    public boolean active = true;

    public BossBullet(double x, double y, double vx, double vy, int kind) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.kind = kind;
    }

    public void update(double dt) {
        this.x += vx * dt;
        this.y += vy * dt;
        if (this.x < -60 || this.x > 1050 || this.y < -60 || this.y > 700) {
            active = false;
        }
    }

    public void draw(Graphics2D g, GameVisuals scene) {
        if (!active) return;
        double r = radius;

        String specificName = kind == TYPE_AIMED ? "BossBullet_aimed.png"
                : kind == TYPE_PLANE_STRAIGHT ? "EnemyBullet.png"
                : "BossBullet_spray.png";
        BufferedImage art = ArtAssets.load("BossBullet", specificName);
        if (art == null && kind == TYPE_PLANE_STRAIGHT) {
            art = ArtAssets.load("BossBullet", "BossBullet_plane.png");
        }
        if (art == null) {
            art = ArtAssets.load("BossBullet", "BossBullet.png");
        }
        if (art != null) {
            int size = (int) Math.round(r * 3.6);
            AffineTransform old = g.getTransform();
            g.translate(x, y);
            if (Math.abs(vx) > 0.01 || Math.abs(vy) > 0.01) {
                g.rotate(Math.atan2(vy, vx));
            }
            g.drawImage(art, -size / 2, -size / 2, size, size, null);
            g.setTransform(old);
            return;
        }

        if (kind == TYPE_AIMED) {
            // Đạn bắn tỉa của Boss: màu đỏ thẫm / cam rực cháy
            g.setColor(new Color(255, 60, 40, 80));
            g.fill(new Ellipse2D.Double(x - r * 2.2, y - r * 2.2, r * 4.4, r * 4.4));
            g.setColor(new Color(255, 120, 30, 200));
            g.fill(new Ellipse2D.Double(x - r * 1.4, y - r * 1.4, r * 2.8, r * 2.8));
            g.setColor(new Color(255, 245, 220));
            g.fill(new Ellipse2D.Double(x - r * 0.7, y - r * 0.7, r * 1.4, r * 1.4));
        } else if (kind == TYPE_PLANE_STRAIGHT) {
            // Đạn máy bay: thoi nhọn màu vàng rực
            g.setColor(new Color(255, 200, 40, 100));
            g.fill(new Ellipse2D.Double(x - r * 1.8, y - r * 1.2, r * 3.6, r * 2.4));
            g.setColor(new Color(255, 240, 90));
            g.fill(new Ellipse2D.Double(x - r * 1.2, y - r * 0.7, r * 2.4, r * 1.4));
            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Double(x - r * 0.6, y - r * 0.4, r * 1.2, r * 0.8));
        } else {
            // Đạn bắn loạn xạ: màu tím hồng ma mị
            g.setColor(new Color(220, 50, 240, 90));
            g.fill(new Ellipse2D.Double(x - r * 1.9, y - r * 1.9, r * 3.8, r * 3.8));
            g.setColor(new Color(245, 90, 255, 210));
            g.fill(new Ellipse2D.Double(x - r * 1.2, y - r * 1.2, r * 2.4, r * 2.4));
            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Double(x - r * 0.6, y - r * 0.6, r * 1.2, r * 1.2));
        }
    }
}

