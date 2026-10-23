package sanbaka.model;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import sanbaka.assets.ArtAssets;
import sanbaka.render.GameVisuals;

/** Mặt tiền trường học, tách khỏi renderer của đường và khuôn viên (tích hợp bộ đệm sprite GPU). */
public class SchoolBuilding {
    private static BufferedImage cachedFacade;

    public static void draw(Graphics2D g, double cx, double baseY, double sc, GameVisuals scene) {
        BufferedImage art = ArtAssets.load("SchoolBuilding", "SchoolBuilding.png");
        if (art != null) {
            int w = (int) Math.round(672 * sc), h = (int) Math.round(294 * sc);
            g.drawImage(art, (int) Math.round(cx - 336 * sc),
                    (int) Math.round(baseY - 294 * sc), w, h, null);
            return;
        }
        if (cachedFacade == null) {
            cachedFacade = buildFacadeSprite();
        }
        AffineTransform old = g.getTransform();
        Stroke oldStroke = g.getStroke();
        long now = System.currentTimeMillis();
        g.translate(cx, baseY);
        g.scale(sc, sc);
        g.drawImage(cachedFacade, -346, -336, null);

        // Lá cờ bay phấp phới trên đỉnh cột cờ
        Path2D.Double flag = new Path2D.Double();
        flag.moveTo(-34, -328);
        double w1 = Math.sin(now / 180.0) * 4;
        double w2 = Math.sin(now / 180.0 + 1.2) * 5;
        flag.quadTo(-16 + w1, -331, 2, -326);
        flag.lineTo(2, -308);
        flag.quadTo(-16 + w2, -313, -34, -310);
        flag.closePath();
        g.setColor(new Color(220, 50, 50));
        g.fill(flag);
        g.setColor(new Color(255, 215, 60));
        scene.drawStar(g, -16 + (w1 + w2) / 2.0, -318, 5, 5, 2.2);

        g.setTransform(old);
        g.setStroke(oldStroke);
    }

    private static BufferedImage buildFacadeSprite() {
        BufferedImage img = ArtAssets.createCompatibleImage(692, 352);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.translate(346, 336);

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
        g.setColor(wallD);
        g.fill(new Rectangle2D.Double(-105, -12, 210, 12));
        g.setColor(roofC);
        Path2D.Double ped = new Path2D.Double();
        ped.moveTo(-115, -232);
        ped.lineTo(0, -288);
        ped.lineTo(115, -232);
        ped.closePath();
        g.fill(ped);
        g.setColor(roofD);
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(ped);
        g.setColor(new Color(255, 255, 255, 210));
        g.fill(new Ellipse2D.Double(-22, -260, 44, 44));
        g.setColor(new Color(60, 64, 76));
        g.setStroke(new BasicStroke(2f));
        g.draw(new Ellipse2D.Double(-22, -260, 44, 44));
        g.setColor(new Color(210, 80, 80));
        g.fill(new Ellipse2D.Double(-14, -252, 28, 28));
        g.setColor(Color.WHITE);
        g.setFont(new Font("Dialog", Font.BOLD, 10));
        g.drawString("C1", -7, -234);
        g.setColor(new Color(110, 80, 60));
        g.fill(new RoundRectangle2D.Double(-40, -82, 80, 82, 12, 12));
        g.setColor(new Color(60, 40, 30));
        g.setStroke(new BasicStroke(2f));
        g.draw(new RoundRectangle2D.Double(-40, -82, 80, 82, 12, 12));
        g.drawLine(0, -82, 0, 0);
        g.setColor(new Color(255, 210, 100));
        g.fill(new Ellipse2D.Double(-8, -42, 5, 5));
        g.fill(new Ellipse2D.Double(3, -42, 5, 5));
        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 2; c++) {
                double wx = (c == 0 ? -82 : 36), wy = -200 + r * 56;
                g.setColor(frame);
                g.fill(new Rectangle2D.Double(wx - 2, wy - 2, 46, 44));
                g.setColor(glass);
                g.fill(new Rectangle2D.Double(wx, wy, 42, 40));
                g.setColor(new Color(255, 255, 255, 120));
                g.fill(new Rectangle2D.Double(wx + 4, wy + 4, 10, 16));
                g.setColor(frame);
                g.fill(new Rectangle2D.Double(wx + 20, wy, 2, 40));
            }
        }
        g.setColor(new Color(140, 110, 80));
        g.setStroke(new BasicStroke(2.2f));
        g.drawLine(-34, -288, -34, -328);
        g.setColor(new Color(248, 230, 180));
        g.fill(new Rectangle2D.Double(-125, -120, 250, 26));
        g.setColor(new Color(180, 50, 50));
        g.setFont(new Font("Dialog", Font.BOLD, 12));
        String banner = "HỘI ĐỒNG THI HỌC KỲ · ĐH BÁCH KHOA";
        int bw = g.getFontMetrics().stringWidth(banner);
        g.drawString(banner, (int) (-bw / 2.0), -103);
        g.dispose();
        return img;
    }
}
