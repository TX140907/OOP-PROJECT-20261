package sanbaka;

import java.awt.*;
import java.awt.geom.*;

/** Thuyền NPC và phần vẽ riêng của nó. */
class RoadBoat extends Thing {
    @Override
    void draw(Graphics2D g, GameVisuals scene) {
        AffineTransform old = g.getTransform();
        Stroke oldStroke = g.getStroke();
        g.translate(x, y);
        g.rotate(angle);
        drawBoat(g, scene);
        g.setTransform(old);
        g.setStroke(oldStroke);
    }

    private void drawBoat(Graphics2D g, GameVisuals scene) {
        long now = System.currentTimeMillis();
        Color[] hulls = {
            new Color(176, 124, 90), new Color(81, 146, 185), new Color(94, 170, 128), new Color(219, 93, 96)
        };
        Color hullC = hulls[this.color % 4];
        Color[] jackets = {
            new Color(219, 93, 96), new Color(81, 146, 185), new Color(240, 200, 90), new Color(94, 170, 128),
            new Color(150, 110, 180), new Color(58, 62, 76), new Color(240, 242, 246), new Color(255, 143, 66)
        };
        Color jacket = jackets[(this.color + 2) % 8];
        Stroke oldStroke = g.getStroke();
        double sc = this.h / 112.0;
        g.scale(sc, sc);
        scene.drawBoatWake(g, 52, now);
        java.awt.image.BufferedImage art = ArtAssets.load("RoadBoat", "RoadBoat.png");
        if (art != null) {
            g.drawImage(art, (int) (-w / (2 * sc)), (int) (-h / (2 * sc)),
                    (int) (w / sc), (int) (h / sc), null);
            g.setStroke(oldStroke);
            return;
        }
        g.setColor(new Color(0, 0, 0, 50));
        g.fill(new Ellipse2D.Double(-28, -48, 56, 108));
        Path2D.Double hull = new Path2D.Double();
        hull.moveTo(0, -56);
        hull.quadTo(26, -30, 23, 10);
        hull.quadTo(21, 44, 13, 54);
        hull.lineTo(-13, 54);
        hull.quadTo(-21, 44, -23, 10);
        hull.quadTo(-26, -30, 0, -56);
        hull.closePath();
        g.setColor(hullC);
        g.fill(hull);
        g.setColor(scene.shade(hullC, 0.72));
        g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(hull);
        g.scale(0.74, 0.8);
        g.setColor(scene.shade(hullC, 0.66));
        g.fill(hull);
        g.scale(1 / 0.74, 1 / 0.8);
        g.setColor(jacket);
        g.fill(new Ellipse2D.Double(-14, -6, 28, 22));
        double swing = Math.sin(now / 300.0 + this.color) * 0.4;
        AffineTransform t0 = g.getTransform();
        g.translate(0, 0);
        g.rotate(swing);
        g.setColor(new Color(150, 110, 76));
        g.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(-40, 0, 40, 0));
        g.setColor(new Color(236, 230, 214));
        g.fill(new Ellipse2D.Double(-50, -6, 15, 26));
        g.fill(new Ellipse2D.Double(35, -6, 15, 26));
        g.setTransform(t0);
        g.setColor(new Color(232, 208, 150));
        g.fill(new Ellipse2D.Double(-16, -22, 32, 32));
        g.setColor(new Color(196, 166, 110));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(new Ellipse2D.Double(-16, -22, 32, 32));
        g.draw(new Ellipse2D.Double(-10.5, -16.5, 21, 21));
        g.draw(new Ellipse2D.Double(-5, -11, 10, 10));
        g.setColor(new Color(150, 60, 70));
        g.fill(new Ellipse2D.Double(-2.5, -8.5, 5, 5));
        g.setStroke(oldStroke);
    }
}


