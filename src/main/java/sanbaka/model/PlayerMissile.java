package sanbaka.model;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import sanbaka.assets.ArtAssets;
import sanbaka.render.GameVisuals;

/**
 * Tên lửa do trực thăng người chơi bắn ra (phím E).
 * Xuất phát từ tầm chân / càng hạ cánh của trực thăng, bay thẳng sang phải với tốc độ cao.
 */
public class PlayerMissile {
    public double x;
    public double y;
    public double vx;
    public double vy;
    public double w = 26;
    public double h = 10;
    public boolean active = true;

    public PlayerMissile(double x, double y, double speed) {
        this.x = x;
        this.y = y;
        this.vx = speed;
        this.vy = 0;
    }

    public void update(double dt) {
        this.x += vx * dt;
        this.y += vy * dt;
        if (this.x > 1050) {
            active = false;
        }
    }

    public void draw(Graphics2D g, GameVisuals scene) {
        if (!active) return;
        long now = System.currentTimeMillis();
        BufferedImage art = ArtAssets.load("Missile", "Missile.png");
        if (art != null) {
            g.drawImage(art, (int) (x - w / 2), (int) (y - h / 2), (int) w, (int) h, null);
            return;
        }

        AffineTransform old = g.getTransform();
        g.translate(x, y);

        // Vệt lửa đẩy phản lực phía sau
        double flameW = 10 + (Math.sin(now / 20.0) + 1.0) * 4.0;
        GradientPaint gpFlame = new GradientPaint(
                (float) -w / 2 - (float) flameW, 0, new Color(255, 60, 20, 0),
                (float) -w / 2, 0, new Color(255, 220, 50, 240)
        );
        g.setPaint(gpFlame);
        Path2D.Double flame = new Path2D.Double();
        flame.moveTo(-w / 2, -3);
        flame.lineTo(-w / 2 - flameW, 0);
        flame.lineTo(-w / 2, 3);
        flame.closePath();
        g.fill(flame);

        // Khói động cơ
        g.setColor(new Color(255, 255, 255, 120));
        g.fill(new Ellipse2D.Double(-w / 2 - flameW * 0.7, -2, 4, 4));

        // Thân tên lửa
        g.setColor(new Color(228, 234, 242));
        g.fill(new RoundRectangle2D.Double(-w / 2 + 4, -h / 2 + 1, w - 8, h - 2, 4, 4));

        // Đầu tên lửa nhọn màu đỏ cam
        Path2D.Double tip = new Path2D.Double();
        tip.moveTo(w / 2 - 4, -h / 2 + 1);
        tip.lineTo(w / 2 + 4, 0);
        tip.lineTo(w / 2 - 4, h / 2 - 1);
        tip.closePath();
        g.setColor(new Color(235, 55, 45));
        g.fill(tip);

        // Cánh đuôi tên lửa
        g.setColor(new Color(90, 102, 120));
        Path2D.Double fins = new Path2D.Double();
        fins.moveTo(-w / 2 + 2, -h / 2 - 2);
        fins.lineTo(-w / 2 + 7, -h / 2 + 1);
        fins.lineTo(-w / 2 + 4, h / 2 - 1);
        fins.lineTo(-w / 2 + 2, h / 2 + 2);
        fins.closePath();
        g.fill(fins);

        // Viền nét mảnh
        g.setColor(new Color(40, 48, 60));
        g.setStroke(new BasicStroke(1f));
        g.draw(new RoundRectangle2D.Double(-w / 2 + 4, -h / 2 + 1, w - 8, h - 2, 4, 4));

        g.setTransform(old);
    }
}

