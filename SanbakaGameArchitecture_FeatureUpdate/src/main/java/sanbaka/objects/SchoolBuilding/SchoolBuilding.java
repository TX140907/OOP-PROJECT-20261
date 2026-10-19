package sanbaka;

import java.awt.*;
import java.awt.geom.*;

/** Mặt tiền trường học, tách khỏi renderer của đường và khuôn viên. */
class SchoolBuilding {
    static void draw(Graphics2D g, double cx, double baseY, double sc, GameVisuals scene) {
        java.awt.image.BufferedImage art = ArtAssets.load("SchoolBuilding", "SchoolBuilding.png");
        if (art != null) {
            int w = (int) Math.round(672 * sc), h = (int) Math.round(294 * sc);
            g.drawImage(art, (int) Math.round(cx - 336 * sc),
                    (int) Math.round(baseY - 294 * sc), w, h, null);
            return;
        }
        AffineTransform old = g.getTransform();
        long now = System.currentTimeMillis();
        g.translate(cx, baseY);
        g.scale(sc, sc);
        Color wall = new Color(250, 240, 222), wallD = new Color(232, 218, 196);
        Color roofC = new Color(200, 96, 84), roofD = new Color(166, 70, 66);
        Color glass = new Color(186, 222, 240), frame = new Color(150, 120, 96);
        g.setColor(new Color(0, 0, 0, 40));
        g.fill(new Rectangle2D.Double(-336, -4, 672, 12));
        for (int side = -1; side <= 1; side += 2) {
            double x0 = side < 0 ? -330 : 105;
            g.setColor(wall);
            g.fill(new Rectangle2D.Double(x0, -150, 225, 150));
            g.setColor(wallD);
            g.fill(new Rectangle2D.Double(x0, -12, 225, 12));
            g.setColor(roofC);
            g.fill(new Rectangle2D.Double(x0 - 8, -172, 241, 26));
            g.setColor(roofD);
            g.fill(new Rectangle2D.Double(x0 - 8, -150, 241, 6));
            for (int r = 0; r < 2; r++) {
                for (int c = 0; c < 5; c++) {
                    double wx = x0 + 14 + c * 42, wy = -128 + r * 58;
                    g.setColor(frame);
                    g.fill(new Rectangle2D.Double(wx - 2, wy - 2, 32, 44));
                    g.setColor(glass);
                    g.fill(new Rectangle2D.Double(wx, wy, 28, 40));
                    g.setColor(new Color(255, 255, 255, 120));
                    g.fill(new Rectangle2D.Double(wx + 3, wy + 3, 8, 14));
                    g.setColor(frame);
                    g.fill(new Rectangle2D.Double(wx + 13, wy, 2, 40));
                }
            }
        }
        g.setColor(wall);
        g.fill(new Rectangle2D.Double(-105, -232, 210, 232));
        g.setColor(roofD);
        scene.poly(g, -128, -232, 0, -294, 128, -232);
        g.setColor(roofC);
        scene.poly(g, -116, -234, 0, -284, 116, -234);
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(-19, -270, 38, 38));
        g.setColor(frame);
        g.setStroke(new BasicStroke(3f));
        g.draw(new Ellipse2D.Double(-19, -270, 38, 38));
        g.setColor(GameState.INK);
        g.draw(new Line2D.Double(0, -251, 0, -262));
        g.draw(new Line2D.Double(0, -251, 8, -247));
        g.setStroke(new BasicStroke(1f));
        g.setColor(new Color(36, 52, 92));
        g.fill(new RoundRectangle2D.Double(-140, -222, 280, 40, 12, 12));
        g.setColor(new Color(255, 214, 120));
        g.setStroke(new BasicStroke(2f));
        g.draw(new RoundRectangle2D.Double(-136, -218, 272, 32, 8, 8));
        g.setStroke(new BasicStroke(1f));
        g.setColor(Color.WHITE);
        g.setFont(new Font("Dialog", Font.BOLD, 22));
        String sign = "HUST";
        g.drawString(sign, -g.getFontMetrics().stringWidth(sign) / 2, -193);
        for (int c = 0; c < 4; c++) {
            double wx = -84 + c * 44;
            g.setColor(frame);
            g.fill(new Rectangle2D.Double(wx - 2, -170, 32, 44));
            g.setColor(glass);
            g.fill(new Rectangle2D.Double(wx, -168, 28, 40));
            g.setColor(frame);
            g.fill(new Rectangle2D.Double(wx + 13, -168, 2, 40));
        }
        g.setColor(Color.WHITE);
        g.fill(new Rectangle2D.Double(-56, -112, 12, 112));
        g.fill(new Rectangle2D.Double(44, -112, 12, 112));
        g.setColor(wallD);
        g.fill(new Rectangle2D.Double(-66, -120, 132, 10));
        g.setColor(new Color(110, 78, 60));
        g.fill(new RoundRectangle2D.Double(-36, -88, 72, 88, 36, 36));
        g.setColor(GameState.MINT);
        g.fill(new RoundRectangle2D.Double(-32, -84, 31, 84, 28, 28));
        g.fill(new RoundRectangle2D.Double(1, -84, 31, 84, 28, 28));
        g.setColor(glass);
        g.fill(new RoundRectangle2D.Double(-26, -76, 19, 40, 14, 14));
        g.fill(new RoundRectangle2D.Double(7, -76, 19, 40, 14, 14));
        g.setColor(new Color(224, 218, 206));
        g.fill(new Rectangle2D.Double(-84, -10, 168, 10));
        g.setColor(new Color(210, 204, 190));
        g.fill(new Rectangle2D.Double(-100, -4, 200, 6));
        g.setColor(new Color(150, 150, 160));
        g.fill(new Rectangle2D.Double(266, -262, 4, 92));
        double wv = Math.sin(now / 240.0) * 5;
        g.setColor(new Color(218, 37, 29));
        Path2D.Double flag = new Path2D.Double();
        flag.moveTo(270, -262);
        flag.curveTo(286, -268 + wv, 300, -256 - wv, 316, -262);
        flag.lineTo(316, -230);
        flag.curveTo(300, -224 - wv, 286, -236 + wv, 270, -230);
        flag.closePath();
        g.fill(flag);
        g.setColor(new Color(255, 236, 0));
        double[] sp = new double[20];
        for (int i = 0; i < 10; i++) {
            double rr = i % 2 == 0 ? 8 : 3.2, aa = -Math.PI / 2 + i * Math.PI / 5;
            sp[i * 2] = 293 + rr * Math.cos(aa);
            sp[i * 2 + 1] = -246 + rr * Math.sin(aa);
        }
        scene.poly(g, sp);
        for (int i = 0; i < 12; i++) {
            if (i >= 5 && i <= 6) continue;
            double bx = -318 + i * 53;
            g.setColor(new Color(66, 140, 90));
            g.fill(new Ellipse2D.Double(bx, -16, 40, 24));
            g.setColor(new Color(92, 168, 112));
            g.fill(new Ellipse2D.Double(bx + 6, -14, 22, 12));
        }
        g.setTransform(old);
    }
}
