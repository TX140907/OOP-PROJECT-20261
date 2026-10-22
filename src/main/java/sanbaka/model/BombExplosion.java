package sanbaka.model;

import java.awt.*;
import java.awt.geom.*;
import sanbaka.render.GameVisuals;

/**
 * Hiệu ứng vụ nổ diện rộng của trái bom ở Màn 5.
 * Thể hiện trực quan bán kính sát thương (blastRadius), vòng sóng xung kích và ngọn lửa mở rộng.
 */
public class BombExplosion {
    public double x;
    public double y;
    public double radius;
    public long startTime;
    public long durationMs = 460;
    public boolean active = true;

    public BombExplosion(double x, double y, double radius, long now) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.startTime = now;
    }

    public void update(long now) {
        if (now - startTime >= durationMs) {
            active = false;
        }
    }

    public void draw(Graphics2D g, GameVisuals scene) {
        long now = System.currentTimeMillis();
        long age = now - startTime;
        if (age < 0 || age >= durationMs) return;

        float progress = (float) age / (float) durationMs;
        float alpha = Math.max(0f, 1f - progress);

        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.SrcOver.derive(alpha));

        // Sóng xung kích mở rộng dần từ tâm ra bán kính sát thương
        double curR = radius * (0.35 + 0.65 * Math.sin(progress * Math.PI * 0.5));

        // 1. Quầng lửa bán kính sát thương
        RadialGradientPaint rgp = new RadialGradientPaint(
                (float) x, (float) y, (float) Math.max(1.0, curR),
                new float[]{0.0f, 0.42f, 0.82f, 1.0f},
                new Color[]{
                        new Color(255, 255, 230, (int) (245 * alpha)),
                        new Color(255, 175, 35, (int) (190 * alpha)),
                        new Color(245, 50, 30, (int) (120 * alpha)),
                        new Color(230, 30, 20, 0)
                }
        );
        g.setPaint(rgp);
        g.fill(new Ellipse2D.Double(x - curR, y - curR, curR * 2, curR * 2));

        // 2. Vòng sóng xung kích (Shockwave Ring) bao quanh bán kính sát thương
        g.setColor(new Color(255, 210, 50, (int) (230 * alpha)));
        g.setStroke(new BasicStroke(3.0f * (1f - progress * 0.5f)));
        g.draw(new Ellipse2D.Double(x - curR, y - curR, curR * 2, curR * 2));

        // 3. Các tia lửa phát tán xung quanh chu vi nổ
        int sparkCount = 8;
        for (int i = 0; i < sparkCount; i++) {
            double angle = i * (Math.PI * 2 / sparkCount) + progress * 1.5;
            double sparkDist = curR * (0.8 + 0.3 * Math.sin(i * 1.7));
            double sx = x + Math.cos(angle) * sparkDist;
            double sy = y + Math.sin(angle) * sparkDist;
            double sr = 3.5 * (1f - progress);
            g.setColor(new Color(255, 240, 110, (int) (240 * alpha)));
            g.fill(new Ellipse2D.Double(sx - sr, sy - sr, sr * 2, sr * 2));
        }

        g.setComposite(old);
    }
}

