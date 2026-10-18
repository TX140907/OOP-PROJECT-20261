package sanbaka;

import java.awt.*;
import java.awt.geom.*;

/** Thuyền của người chơi trong màn đường ngập, giữ trạng thái và tự vẽ. */
class PlayerBoat extends PlayerVehicle {
    void draw(Graphics2D g, GameVisuals scene) {
        int px = (int) Math.round(x), py = (int) Math.round(y);
        long now = System.currentTimeMillis();
        Composite oldComposite = g.getComposite();
        AffineTransform oldTransform = g.getTransform();
        Stroke oldStroke = g.getStroke();
        if (invulnerable > now && (now / 90) % 2 == 0) {
            g.setComposite(AlphaComposite.SrcOver.derive(0.35f));
        }
        Color orange = new Color(255, 143, 66), orangeD = new Color(214, 104, 36);
        Color jacket = new Color(45, 61, 87), skin = new Color(255, 224, 196);
        g.translate(px, py);
        g.scale(0.6, 0.6);
        g.rotate(lean * 0.1);
        scene.drawBoatWake(g, 52, now);
        java.awt.image.BufferedImage art = ArtAssets.load("PlayerBoat", "PlayerBoat.png");
        if (art != null) {
            g.drawImage(art, -50, -56, 100, 110, null);
            g.setTransform(oldTransform);
            g.setComposite(oldComposite);
            g.setStroke(oldStroke);
            return;
        }
        g.setColor(new Color(0, 0, 0, 50));
        g.fill(new Ellipse2D.Double(-28, -44, 56, 104));
        Path2D.Double hull = new Path2D.Double();
        hull.moveTo(0, -56);
        hull.quadTo(26, -30, 23, 10);
        hull.quadTo(21, 44, 13, 54);
        hull.lineTo(-13, 54);
        hull.quadTo(-21, 44, -23, 10);
        hull.quadTo(-26, -30, 0, -56);
        hull.closePath();
        g.setColor(orange);
        g.fill(hull);
        g.setColor(orangeD);
        g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(hull);
        g.scale(0.74, 0.8);
        g.setColor(new Color(150, 92, 52));
        g.fill(hull);
        g.scale(1 / 0.74, 1 / 0.8);
        g.setColor(new Color(176, 112, 66));
        for (int k = -2; k <= 3; k++) g.fill(new Rectangle2D.Double(-15, k * 13 - 3, 30, 2));
        g.setColor(GameState.PINK);
        scene.poly(g, 0, -56, -7, -44, 7, -44);
        // người chèo: ba lô, thân, mũ bảo hiểm
        g.setColor(GameState.MINT);
        g.fill(new RoundRectangle2D.Double(-9, 6, 18, 20, 9, 9));
        g.setColor(new Color(63, 145, 121));
        g.fill(new RoundRectangle2D.Double(-6, 15, 12, 7, 5, 5));
        g.setColor(jacket);
        g.fill(new Ellipse2D.Double(-14, -10, 28, 22));
        // mái chèo
        double swing = Math.sin(now / 240.0) * 0.38;
        AffineTransform t0 = g.getTransform();
        g.translate(0, -8);
        g.rotate(swing);
        g.setColor(new Color(176, 132, 90));
        g.setStroke(new BasicStroke(3.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(-40, 0, 40, 0));
        g.setColor(GameState.PINK);
        g.fill(new Ellipse2D.Double(-50, -6, 15, 26));
        g.fill(new Ellipse2D.Double(35, -6, 15, 26));
        g.setColor(skin);
        g.fill(new Ellipse2D.Double(-17, -4, 8, 8));
        g.fill(new Ellipse2D.Double(9, -4, 8, 8));
        g.setTransform(t0);
        g.setColor(new Color(235, 242, 248, 150));
        g.setStroke(new BasicStroke(1.6f));
        double dip = swing > 0 ? 1 : -1;
        g.draw(new Ellipse2D.Double(dip * 42 - 10, 10, 20, 9));
        g.setColor(new Color(52, 40, 44));
        g.fill(new Ellipse2D.Double(-8, -17, 16, 9));
        g.setColor(new Color(250, 250, 252));
        g.fill(new Ellipse2D.Double(-11, -27, 22, 23));
        g.setColor(new Color(200, 208, 222));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(new Ellipse2D.Double(-11, -27, 22, 23));
        g.setColor(GameState.PINK);
        g.fill(new RoundRectangle2D.Double(-2, -26, 4, 20, 3, 3));
        g.setTransform(oldTransform);
        g.setComposite(oldComposite);
        g.setStroke(oldStroke);
    }
}
