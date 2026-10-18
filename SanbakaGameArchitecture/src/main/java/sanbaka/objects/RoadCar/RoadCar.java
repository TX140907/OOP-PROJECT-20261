package sanbaka;

import java.awt.*;
import java.awt.geom.*;

/** Ô tô, taxi, xe tải và xe buýt trên đường. */
class RoadCar extends Thing {
    void draw(Graphics2D g, GameVisuals scene) {
        AffineTransform old = g.getTransform();
        Stroke oldStroke = g.getStroke();
        g.translate(this.x, this.y);
        g.rotate(this.angle);
        double w = this.w, h = this.h, hw = w / 2, hh = h / 2;
        
        boolean brake = this.jam || this.fwd == 0;
        g.setColor(new Color(0, 0, 0, 55));
        g.fill(new RoundRectangle2D.Double(-hw + 4, -hh + 7, w, h, 20, 20));
        String[] artNames = {"Sedan.png", "Hatchback.png", "Taxi.png", "Van.png", "Truck.png", "Bus.png"};
        java.awt.image.BufferedImage art = ArtAssets.load("RoadCar", artNames[Math.max(0, Math.min(5, kind))]);
        if (art != null) {
            g.drawImage(art, (int) -hw, (int) -hh, (int) w, (int) h, null);
            g.setTransform(old);
            g.setStroke(oldStroke);
            return;
        }

        Color glass = new Color(52, 68, 92), glassHi = new Color(255, 255, 255, 70);
        Color body = this.kind == 2 ? new Color(52, 160, 110) : this.kind == 3 ? new Color(238, 240, 244) : GameState.CAR_COLORS[this.color % 8];
        if (this.kind == 5) body = this.color % 2 == 0 ? new Color(219, 93, 96) : new Color(81, 146, 185);
        if (this.kind == 4) body = GameState.CAR_COLORS[this.color % 8];
        double[] wy = this.kind == 4 ? new double[]{-hh + 20, hh - 40, hh - 18}
                : this.kind == 5 ? new double[]{-hh + 26, hh - 30} : new double[]{-hh + 17, hh - 17};
        g.setColor(new Color(24, 26, 32));
        for (double yy : wy) {
            g.fill(new RoundRectangle2D.Double(-hw - 2, yy - 8, 6, 16, 3, 3));
            g.fill(new RoundRectangle2D.Double(hw - 4, yy - 8, 6, 16, 3, 3));
        }
        double arc = this.kind >= 4 ? 12 : 22;
        RoundRectangle2D.Double shape = new RoundRectangle2D.Double(-hw, -hh, w, h, arc, arc);
        g.setColor(body);
        g.fill(shape);
        g.setColor(scene.shade(body, 0.7));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(shape);
        Color lite = scene.shade(body, 1.14);
        if (this.kind <= 2) {
            double y1 = -hh + h * 0.27, y2 = -hh + h * 0.40;
            double y3 = -hh + h * (this.kind == 1 ? 0.60 : 0.64), y4 = -hh + h * (this.kind == 1 ? 0.82 : 0.76);
            g.setColor(lite);
            g.fill(new RoundRectangle2D.Double(-hw + 4, -hh + 3, w - 8, h * 0.25, 14, 14));
            g.setColor(scene.shade(body, 0.8));
            g.fill(new Rectangle2D.Double(-1, -hh + 6, 2, h * 0.2));
            g.setColor(glass);
            scene.poly(g, -hw + 8, y2, hw - 8, y2, hw - 11, y1, -hw + 11, y1);
            g.setColor(lite);
            g.fill(new RoundRectangle2D.Double(-hw + 7, y2, w - 14, y3 - y2, 6, 6));
            g.setColor(glassHi);
            scene.poly(g, -hw + 10, y2 + 3, -hw + 20, y2 + 3, -hw + 12, y3 - 3);
            g.setColor(glass);
            scene.poly(g, -hw + 8, y3, hw - 8, y3, hw - 11, y4, -hw + 11, y4);
            if (this.kind == 2) {
                g.setColor(Color.WHITE);
                g.fill(new RoundRectangle2D.Double(-10, (y2 + y3) / 2 - 4, 20, 8, 3, 3));
                g.setColor(new Color(52, 160, 110));
                g.fill(new Rectangle2D.Double(-6, (y2 + y3) / 2 - 1, 12, 2));
            }
            g.setColor(scene.shade(body, 0.8));
            g.fill(new Rectangle2D.Double(-hw - 3, y1 + 4, 4, 7));
            g.fill(new Rectangle2D.Double(hw - 1, y1 + 4, 4, 7));
        } else if (this.kind == 3) {
            g.setColor(glass);
            scene.poly(g, -hw + 7, -hh + 22, hw - 7, -hh + 22, hw - 10, -hh + 7, -hw + 10, -hh + 7);
            g.setColor(scene.shade(body, 0.92));
            g.fill(new RoundRectangle2D.Double(-hw + 5, -hh + 26, w - 10, h - 34, 6, 6));
            g.setColor(new Color(0, 0, 0, 36));
            for (int i = 0; i < 4; i++) g.fill(new Rectangle2D.Double(-hw + 7, -hh + 36 + i * 14, w - 14, 2));
            g.fill(new Rectangle2D.Double(-1, hh - 34, 2, 30));
            g.setColor(GameState.CAR_COLORS[this.color % 8]);
            g.fill(new Rectangle2D.Double(-hw + 5, -hh + 26, 5, h - 34));
            g.fill(new Rectangle2D.Double(hw - 10, -hh + 26, 5, h - 34));
        } else if (this.kind == 4) {
            g.setColor(scene.shade(body, 1.1));
            g.fill(new RoundRectangle2D.Double(-hw + 3, -hh + 3, w - 6, 34, 10, 10));
            g.setColor(glass);
            scene.poly(g, -hw + 8, -hh + 26, hw - 8, -hh + 26, hw - 10, -hh + 12, -hw + 10, -hh + 12);
            g.setColor(new Color(214, 218, 226));
            g.fill(new RoundRectangle2D.Double(-hw + 3, -hh + 40, w - 6, h - 42, 6, 6));
            g.setColor(new Color(0, 0, 0, 40));
            for (int i = 0; i < 7; i++) g.fill(new Rectangle2D.Double(-hw + 4, -hh + 48 + i * 11, w - 8, 2));
            g.setColor(new Color(150, 156, 170));
            g.draw(new RoundRectangle2D.Double(-hw + 3, -hh + 40, w - 6, h - 42, 6, 6));
        } else {
            g.setColor(glass);
            scene.poly(g, -hw + 6, -hh + 30, hw - 6, -hh + 30, hw - 9, -hh + 8, -hw + 9, -hh + 8);
            g.setColor(scene.shade(body, 1.12));
            g.fill(new RoundRectangle2D.Double(-hw + 5, -hh + 34, w - 10, h - 62, 6, 6));
            g.setColor(new Color(255, 255, 255, 190));
            for (int i = 0; i < 3; i++) g.fill(new RoundRectangle2D.Double(-12, -hh + 48 + i * 40, 24, 18, 5, 5));
            g.fill(new Rectangle2D.Double(-hw + 3, -hh + 34, 3, h - 62));
            g.fill(new Rectangle2D.Double(hw - 6, -hh + 34, 3, h - 62));
            g.setColor(glass);
            scene.poly(g, -hw + 8, hh - 26, hw - 8, hh - 26, hw - 11, hh - 10, -hw + 11, hh - 10);
        }
        Color lamp = brake ? new Color(255, 60, 70) : new Color(176, 34, 48);
        if (brake) {
            g.setColor(new Color(255, 60, 70, 70));
            g.fill(new Ellipse2D.Double(-hw + 1, hh - 12, 18, 12));
            g.fill(new Ellipse2D.Double(hw - 19, hh - 12, 18, 12));
        }
        g.setColor(lamp);
        g.fill(new RoundRectangle2D.Double(-hw + 3, hh - 7, 12, 5, 3, 3));
        g.fill(new RoundRectangle2D.Double(hw - 15, hh - 7, 12, 5, 3, 3));
        g.setColor(new Color(245, 245, 240));
        g.fill(new RoundRectangle2D.Double(-7, hh - 8, 14, 5, 2, 2));
        if (scene.day == 1 && this.kind <= 5) {
            long nowW = System.currentTimeMillis();
            g.setColor(new Color(124, 110, 86, 120));
            g.fill(new RoundRectangle2D.Double(-hw - 2, -hh * 0.05, w + 4, h * 0.55, 16, 16));
            g.setStroke(new BasicStroke(1.8f));
            for (int k = 0; k < 2; k++) {
                double ph = ((nowW / 900.0) + k * 0.5) % 1.0;
                double ex = 4 + ph * 10;
                g.setColor(new Color(240, 244, 248, (int) (130 * (1 - ph))));
                g.draw(new RoundRectangle2D.Double(-hw - ex, -hh - ex, w + 2 * ex, h + 2 * ex, 26 + ex, 26 + ex));
            }
        }
        g.setTransform(old);
        g.setStroke(oldStroke);
    }
}

