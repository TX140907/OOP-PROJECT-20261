package sanbaka.model;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import sanbaka.assets.ArtAssets;
import sanbaka.render.GameVisuals;

/** Rào chắn công trình thi công đường bộ (vật cản tĩnh, đứng yên trên đường). */
public class ConstructionBarrier extends Thing {

    @Override
    public void draw(Graphics2D g, GameVisuals scene) {
        AffineTransform old = g.getTransform();
        Stroke oldStroke = g.getStroke();
        g.translate(this.x, this.y);
        g.rotate(this.angle);
        double w = this.w, h = this.h, hw = w / 2, hh = h / 2;

        BufferedImage art = ArtAssets.load("ConstructionBarrier", "ConstructionBarrier.png");
        if (art != null) {
            g.setColor(new Color(0, 0, 0, 50));
            g.fill(new RoundRectangle2D.Double(-hw + 3, -hh + 5, w, h, 8, 8));
            g.drawImage(art, (int) -hw, (int) -hh, (int) w, (int) h, null);
            g.setTransform(old);
            g.setStroke(oldStroke);
            return;
        }

        // Bóng đổ rào chắn
        g.setColor(new Color(0, 0, 0, 55));
        g.fill(new RoundRectangle2D.Double(-hw + 3, hh - 6, w - 6, 8, 6, 6));

        // Chân đế chữ A hai bên bằng thép sơn đen
        g.setColor(new Color(55, 60, 70));
        g.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        // Chân trái
        g.draw(new Line2D.Double(-hw + 8, hh - 1, -hw + 14, -hh + 6));
        g.draw(new Line2D.Double(-hw + 20, hh - 1, -hw + 14, -hh + 6));
        // Chân phải
        g.draw(new Line2D.Double(hw - 20, hh - 1, hw - 14, -hh + 6));
        g.draw(new Line2D.Double(hw - 8, hh - 1, hw - 14, -hh + 6));
        g.setStroke(new BasicStroke(1f));

        // Tấm chắn ngang chính (sọc đỏ trắng phản quang công trường)
        double barX = -hw + 4, barY = -hh + 8, barW = w - 8, barH = 18;
        RoundRectangle2D.Double board = new RoundRectangle2D.Double(barX, barY, barW, barH, 4, 4);
        g.setColor(new Color(246, 246, 250));
        g.fill(board);

        // Các sọc chéo đỏ phản quang 45 độ
        Shape clipOld = g.getClip();
        g.clip(board);
        g.setColor(new Color(225, 45, 45));
        for (double sx = barX - barH; sx < barX + barW + barH; sx += 18) {
            Path2D.Double stripe = new Path2D.Double();
            stripe.moveTo(sx, barY);
            stripe.lineTo(sx + 10, barY);
            stripe.lineTo(sx + 10 - barH, barY + barH);
            stripe.lineTo(sx - barH, barY + barH);
            stripe.closePath();
            g.fill(stripe);
        }
        g.setClip(clipOld);

        // Viền tấm chắn
        g.setColor(new Color(130, 136, 148));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(board);

        // Hai đinh ốc giữ tấm bảng hai đầu
        g.setColor(new Color(70, 75, 85));
        g.fill(new Ellipse2D.Double(-hw + 12, barY + 7, 4, 4));
        g.fill(new Ellipse2D.Double(hw - 16, barY + 7, 4, 4));

        // Đèn chớp cảnh báo công trình màu vàng cam trên đỉnh
        long now = System.currentTimeMillis();
        boolean blink = (now / 220) % 2 == 0;
        double lampX = 0, lampY = -hh + 3;
        // Đế đèn
        g.setColor(new Color(40, 44, 52));
        g.fill(new Rectangle2D.Double(lampX - 4, lampY + 2, 8, 4));
        // Chao đèn
        g.setColor(blink ? new Color(255, 200, 30) : new Color(180, 110, 10));
        g.fill(new RoundRectangle2D.Double(lampX - 5, lampY - 4, 10, 8, 3, 3));
        if (blink) {
            // Hào quang chớp sáng
            g.setColor(new Color(255, 220, 50, 85));
            g.fill(new Ellipse2D.Double(lampX - 12, lampY - 11, 24, 22));
        }

        g.setTransform(old);
        g.setStroke(oldStroke);
    }
}

