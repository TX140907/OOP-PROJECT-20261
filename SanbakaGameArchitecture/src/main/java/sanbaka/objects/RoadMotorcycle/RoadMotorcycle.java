package sanbaka;

import java.awt.*;
import java.awt.geom.*;

/** Xe máy NPC và phần vẽ riêng của nó. */
class RoadMotorcycle extends Thing {
    @Override
    void draw(Graphics2D g, GameVisuals scene) {
        AffineTransform old = g.getTransform();
        Stroke oldStroke = g.getStroke();
        g.translate(x, y);
        g.rotate(angle);
        double hh = h / 2;
        g.setColor(new Color(0, 0, 0, 55));
        g.fill(new RoundRectangle2D.Double(-w / 2 + 4, -hh + 7, w, h, 20, 20));
        java.awt.image.BufferedImage art = ArtAssets.load("RoadMotorcycle", "RoadMotorcycle.png");
        if (art != null) {
            g.drawImage(art, (int) (-w / 2), (int) (-h / 2), (int) w, (int) h, null);
            g.setTransform(old);
            g.setStroke(oldStroke);
            return;
        }
        drawBike(g, scene);
        g.setTransform(old);
        g.setStroke(oldStroke);
    }

    private void drawBike(Graphics2D g, GameVisuals scene) {
        double hh = this.h / 2;
        Color[] jackets = {new Color(219, 93, 96), new Color(81, 146, 185), new Color(240, 200, 90), new Color(94, 170, 128), new Color(150, 110, 180), new Color(58, 62, 76), new Color(240, 242, 246), new Color(255, 143, 66)};
        Color jacket = jackets[this.color % 8], helmet = jackets[(this.color + 3) % 8];
        g.setColor(new Color(32, 34, 42));
        g.fill(new RoundRectangle2D.Double(-4, hh - 24, 8, 24, 6, 6));
        g.fill(new RoundRectangle2D.Double(-3, -hh, 6, 16, 5, 5));
        g.setColor(scene.shade(jacket, 0.8));
        g.fill(new RoundRectangle2D.Double(-6, -4, 12, 30, 8, 8));
        g.setColor(jacket);
        g.fill(new Ellipse2D.Double(-10, -14, 20, 24));
        g.setColor(new Color(40, 44, 56));
        g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(-13, -hh + 14, 13, -hh + 14));
        g.setStroke(new BasicStroke(1f));
        g.setColor(helmet);
        g.fill(new Ellipse2D.Double(-7.5, -24, 15, 15));
        g.setColor(new Color(255, 255, 255, 110));
        g.fill(new Ellipse2D.Double(-4, -22, 5, 4));
        g.setColor(this.jam || this.fwd == 0 ? new Color(255, 60, 70) : new Color(190, 40, 52));
        g.fill(new Rectangle2D.Double(-3, hh - 4, 6, 3));
    }
}
