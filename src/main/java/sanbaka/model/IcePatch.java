package sanbaka.model;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import sanbaka.assets.ArtAssets;
import sanbaka.render.GameVisuals;

/** Vũng băng trơn thực thể trên mặt đường Màn 3 (cán qua sẽ bị trượt bánh mất ma sát ngang). */
public class IcePatch extends Thing {

    @Override
    public void draw(Graphics2D g, GameVisuals scene) {
        AffineTransform old = g.getTransform();
        Stroke oldStroke = g.getStroke();
        g.translate(this.x, this.y);
        g.rotate(this.angle);
        double w = this.w, h = this.h, hw = w / 2, hh = h / 2;

        BufferedImage art = ArtAssets.load("IcePatch", "IcePatch.png");
        if (art != null) {
            g.drawImage(art, (int) -hw, (int) -hh, (int) w, (int) h, null);
            g.setTransform(old);
            g.setStroke(oldStroke);
            return;
        }

        // Viền băng mờ lan ra mặt đường nhựa
        g.setColor(new Color(195, 228, 250, 115));
        g.fill(new Ellipse2D.Double(-hw - 5, -hh - 3, w + 10, h + 6));

        // Thân mảng băng xanh ngọc trong suốt
        g.setColor(new Color(210, 238, 255, 185));
        g.fill(new RoundRectangle2D.Double(-hw, -hh, w, h, 32, 24));

        // Lớp băng sáng trung tâm
        g.setColor(new Color(235, 248, 255, 195));
        g.fill(new Ellipse2D.Double(-hw * 0.72, -hh * 0.68, w * 1.44, h * 1.36));

        // Viền băng kết tinh trắng sáng
        g.setColor(new Color(248, 252, 255, 220));
        g.setStroke(new BasicStroke(1.8f));
        g.draw(new RoundRectangle2D.Double(-hw, -hh, w, h, 32, 24));

        // Các vệt bóng gương phản chiếu chéo trên mặt băng
        g.setColor(new Color(255, 255, 255, 210));
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(-hw * 0.45, -hh * 0.25, -hw * 0.10, -hh * 0.52));
        g.draw(new Line2D.Double(-hw * 0.20, hh * 0.20, hw * 0.35, -hh * 0.30));
        g.draw(new Line2D.Double(hw * 0.15, hh * 0.38, hw * 0.48, hh * 0.05));

        // Hoa tuyết / tinh thể lấp lánh trên mặt băng
        long now = System.currentTimeMillis();
        double twinkle = 0.5 + 0.5 * Math.sin(now / 240.0 + this.color);
        g.setColor(new Color(255, 255, 255, (int) (150 + 100 * twinkle)));
        double sx = -hw * 0.3, sy = -hh * 0.1, sr = 3.5 + 2.5 * twinkle;
        g.draw(new Line2D.Double(sx - sr, sy, sx + sr, sy));
        g.draw(new Line2D.Double(sx, sy - sr, sx, sy + sr));

        g.setTransform(old);
        g.setStroke(oldStroke);
    }
}

