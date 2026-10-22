package sanbaka.model;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import sanbaka.assets.ArtAssets;
import sanbaka.render.GameVisuals;

/** Ổ gà mặt đường cỡ lớn (vật cản tĩnh nằm dưới mặt đường gây xóc nảy và trừ máu xe). */
public class Pothole extends Thing {

    @Override
    public void draw(Graphics2D g, GameVisuals scene) {
        AffineTransform old = g.getTransform();
        Stroke oldStroke = g.getStroke();
        g.translate(this.x, this.y);
        g.rotate(this.angle);
        double w = this.w, h = this.h, hw = w / 2, hh = h / 2;

        BufferedImage art = ArtAssets.load("Pothole", "Pothole.png");
        if (art != null) {
            g.drawImage(art, (int) -hw, (int) -hh, (int) w, (int) h, null);
            g.setTransform(old);
            g.setStroke(oldStroke);
            return;
        }

        // Viền nhựa đường nứt vỡ lan rộng xung quanh ổ gà
        g.setColor(new Color(56, 60, 70, 195));
        g.fill(new Ellipse2D.Double(-hw - 5, -hh - 4, w + 10, h + 8));

        // Bậc thềm nhựa đường bị sụt (tạo chiều sâu 3D cho miệng hố)
        g.setColor(new Color(46, 50, 58));
        g.fill(new RoundRectangle2D.Double(-hw - 1, -hh - 1, w + 2, h + 2, 22, 16));

        // Lòng hố sâu sẫm đen
        g.setColor(new Color(28, 30, 36));
        g.fill(new RoundRectangle2D.Double(-hw + 3, -hh + 4, w - 6, h - 6, 18, 14));

        // Vũng nước đọng phản chiếu ở giữa lòng ổ gà
        g.setColor(new Color(66, 88, 118, 210));
        g.fill(new Ellipse2D.Double(-hw + 9, -hh + 8, w - 18, h - 15));

        // Các đường nứt mặt đường tỏa ra từ mép hố
        g.setColor(new Color(24, 26, 32, 210));
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(-hw + 4, -hh + 5, -hw - 8, -hh - 4));
        g.draw(new Line2D.Double(-hw - 8, -hh - 4, -hw - 13, -hh - 1));
        g.draw(new Line2D.Double(hw - 5, -hh + 4, hw + 7, -hh - 3));
        g.draw(new Line2D.Double(hw - 4, hh - 4, hw + 9, hh + 4));
        g.draw(new Line2D.Double(-hw + 8, hh - 3, -hw - 5, hh + 5));

        // Vài viên đá dăm / sỏi vỡ quanh mép ổ gà
        g.setColor(new Color(120, 126, 138));
        g.fill(new Ellipse2D.Double(-hw - 4, 2, 4, 3));
        g.fill(new Ellipse2D.Double(hw + 1, -4, 4, 3));
        g.fill(new Ellipse2D.Double(-6, hh + 1, 5, 3));

        // Vệt sáng lấp lánh phản chiếu trên mặt nước đọng
        long now = System.currentTimeMillis();
        double shimmer = 0.5 + 0.5 * Math.sin(now / 340.0);
        g.setColor(new Color(235, 245, 255, (int) (115 + 85 * shimmer)));
        g.fill(new Ellipse2D.Double(-hw * 0.45, -hh * 0.25, w * 0.22, h * 0.14));
        g.fill(new Ellipse2D.Double(hw * 0.15, hh * 0.10, w * 0.12, h * 0.10));

        g.setTransform(old);
        g.setStroke(oldStroke);
    }
}
