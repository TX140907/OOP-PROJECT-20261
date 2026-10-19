package sanbaka;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;

class GameCharacterRenderer extends GameEnvironmentRenderer {
    void drawHud(Graphics2D g) {
        long now = System.currentTimeMillis();
        boolean hot = day == HOT_DAY && !flightMode;
        panel(g, 20, 18, 270, hot ? 144 : 118, new Color(255, 255, 255, 238));
        g.setColor(INK);
        int fs = 17;
        Font f;
        do {
            f = new Font("Dialog", Font.BOLD, fs--);
        } while (g.getFontMetrics(f).stringWidth(dayTitles[day]) > 236 && fs > 10);
        g.setFont(f);
        g.drawString(dayTitles[day], 38, 46);
        g.setFont(new Font("Dialog", Font.PLAIN, 15));
        int secondsLeft = Math.max(0, (ROAD_DURATION_MS / 1000) - (int) (elapsed / 1000));
        g.drawString(String.format("THỜI GIAN  %02d:%02d", secondsLeft / 60, secondsLeft % 60), 38, 70);
        // thanh HP
        int shake = now - lastHitAt < 350 ? (int) (Math.sin(now / 22.0) * 3) : 0;
        drawHeart(g, 47 + shake, 96, 22, new Color(232, 70, 100));
        int bx = 66 + shake, by = 88, bw = 206, bh = 16;
        g.setColor(new Color(222, 224, 228));
        g.fillRoundRect(bx, by, bw, bh, 10, 10);
        int fill = (int) (bw * Math.max(0, hp) / 100.0);
        if (fill > 0) {
            g.setColor(hp > 50 ? MINT : hp > 25 ? new Color(240, 176, 64) : new Color(232, 70, 90));
            g.fillRoundRect(bx, by, Math.max(fill, 10), bh, 10, 10);
        }
        g.setFont(new Font("Dialog", Font.BOLD, 11));
        String ht = "HP  " + Math.max(0, hp) + " / 100";
        int hx = bx + bw / 2 - g.getFontMetrics().stringWidth(ht) / 2;
        g.setColor(INK);
        g.drawString(ht, hx, by + 12);
        // thanh năng lượng (chỉ ngày nắng nóng)
        if (hot) {
            drawBolt(g, 47, 123, 0.62);
            g.setColor(new Color(222, 224, 228));
            g.fillRoundRect(66, 114, 206, 16, 10, 10);
            int ef = (int) (206 * Math.max(0, energy) / 100.0);
            boolean low = energy < 25;
            if (ef > 0) {
                g.setColor(low && (now / 250) % 2 == 0 ? new Color(232, 70, 90) : new Color(255, 196, 40));
                g.fillRoundRect(66, 114, Math.max(ef, 10), 16, 10, 10);
            }
            g.setFont(new Font("Dialog", Font.BOLD, 11));
            String et = "NĂNG LƯỢNG  " + (int) Math.ceil(Math.max(0, energy));
            int ex = 66 + 103 - g.getFontMetrics().stringWidth(et) / 2;
            g.setColor(INK);
            g.drawString(et, ex, 126);
        }
        panel(g, 700, 18, 238, 58, new Color(255, 255, 255, 225));
        g.setColor(INK);
        g.setFont(new Font("Dialog", Font.BOLD, 14));
        g.drawString(flightMode ? "ĐƯỜNG BAY" : "ĐƯỜNG ĐI", 720, 42);
        g.setColor(new Color(98, 102, 114));
        g.fillRoundRect(720, 49, 196, 8, 8, 8);
        g.setColor(PINK);
        g.fillRoundRect(720, 49, Math.min(196, (int) (196.0 * elapsed / ROAD_DURATION_MS)), 8, 8, 8);
        if (day == 4 && !flightMode && elapsed / 1000.0 >= ROAD_DURATION_MS / 3000.0 - JAM_WINDOW + 1.2) {
            g.setColor(new Color(245, 250, 255, 190));
            g.fillRoundRect(ROAD_X + 140, 150, ROAD_W - 280, 38, 16, 16);
            g.setColor(INK);
            g.setFont(new Font("Dialog", Font.BOLD, 16));
            String jam = "TẮC ĐƯỜNG…";
            g.drawString(jam, ROAD_X + ROAD_W / 2 - g.getFontMetrics().stringWidth(jam) / 2, 175);
        }
        g.setFont(new Font("Dialog", Font.PLAIN, 13));
        String hint = flightMode ? "Né đàn chim · Đừng để HP về 0"
                : day == 1 ? "Chèo thuyền né xe cộ và thuyền khác · Đừng để HP về 0"
                : hot ? "Nhặt tia sét để giữ năng lượng · Đừng để HP hay năng lượng về 0"
                : "Né xe cộ (có xe chạy lấn làn!) · Đừng để HP về 0";
        int hw = g.getFontMetrics().stringWidth(hint);
        g.setColor(new Color(255, 255, 255, 190));
        g.fillRoundRect(W / 2 - hw / 2 - 14, 600, hw + 28, 26, 14, 14);
        g.setColor(INK);
        g.drawString(hint, W / 2 - hw / 2, 618);
    }

    void drawSideBiker(Graphics2D g, double cx, double gy, double sc) {
        long now = System.currentTimeMillis();
        AffineTransform old = g.getTransform();
        Stroke oldStroke = g.getStroke();
        g.translate(cx, gy + Math.sin(now / 170.0) * 1.2);
        g.scale(sc, sc);
        Color orange = new Color(255, 143, 66), orangeD = new Color(214, 104, 36);
        Color tire = new Color(32, 34, 42), jacket = new Color(45, 61, 87);
        Color jeans = new Color(62, 84, 122), skin = new Color(255, 224, 196);
        Color dark = new Color(60, 64, 76);
        BasicStroke round = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g.setColor(new Color(0, 0, 0, 40));
        g.fill(new Ellipse2D.Double(-90, -5, 180, 12));
        // vệt gió
        g.setStroke(round);
        for (int i = 0; i < 3; i++) {
            double x = -96 - ((now / 8.0 + i * 37) % 70);
            double a = 1 - ((now / 8.0 + i * 37) % 70) / 70.0;
            g.setColor(new Color(255, 255, 255, (int) (190 * a)));
            g.draw(new Line2D.Double(x, -34 - i * 24, x - 26, -34 - i * 24));
        }
        // bánh xe
        for (double wx : new double[]{-52, 52}) {
            g.setColor(tire);
            g.fill(new Ellipse2D.Double(wx - 21, -42, 42, 42));
            g.setColor(new Color(196, 202, 212));
            g.fill(new Ellipse2D.Double(wx - 11, -32, 22, 22));
            g.setColor(new Color(120, 126, 138));
            g.setStroke(new BasicStroke(2f));
            double a = now / 70.0;
            for (int k = 0; k < 3; k++) {
                double aa = a + k * Math.PI / 3;
                g.draw(new Line2D.Double(wx - 10 * Math.cos(aa), -21 - 10 * Math.sin(aa),
                        wx + 10 * Math.cos(aa), -21 + 10 * Math.sin(aa)));
            }
            g.setColor(dark);
            g.fill(new Ellipse2D.Double(wx - 3, -24, 6, 6));
        }
        // thân xe
        g.setColor(orange);
        g.fill(new RoundRectangle2D.Double(-78, -66, 70, 38, 26, 26));
        g.setColor(orangeD);
        g.fill(new RoundRectangle2D.Double(-78, -40, 70, 12, 10, 10));
        g.fill(new RoundRectangle2D.Double(-14, -36, 56, 10, 8, 8));
        Path2D.Double shield = new Path2D.Double();
        shield.moveTo(30, -30);
        shield.lineTo(38, -88);
        shield.quadTo(46, -94, 54, -86);
        shield.lineTo(56, -30);
        shield.closePath();
        g.setColor(orange);
        g.fill(shield);
        g.setColor(dark);
        g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(48, -84, 53, -24));
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(42, -94, 34, -100));
        g.setColor(new Color(255, 246, 180, 70));
        poly(g, 62, -80, 124, -94, 124, -60);
        g.setColor(new Color(255, 246, 180));
        g.fill(new Ellipse2D.Double(52, -86, 12, 12));
        g.setColor(orange);
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Arc2D.Double(-77, -46, 50, 50, 15, 150, Arc2D.OPEN));
        g.draw(new Arc2D.Double(27, -46, 50, 50, 20, 140, Arc2D.OPEN));
        g.setColor(new Color(58, 46, 48));
        g.fill(new RoundRectangle2D.Double(-58, -76, 56, 13, 12, 12));
        g.setColor(new Color(255, 70, 80));
        g.fill(new RoundRectangle2D.Double(-82, -58, 7, 14, 4, 4));
        // balo
        g.setColor(MINT);
        g.fill(new RoundRectangle2D.Double(-52, -124, 22, 42, 12, 12));
        g.setColor(new Color(63, 145, 121));
        g.fill(new RoundRectangle2D.Double(-50, -98, 18, 11, 7, 7));
        // thân người, chân, tay
        g.setColor(jacket);
        g.setStroke(new BasicStroke(26f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(-26, -84, -18, -112));
        g.setColor(jeans);
        g.setStroke(new BasicStroke(12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D.Double leg = new Path2D.Double();
        leg.moveTo(-30, -78);
        leg.lineTo(8, -74);
        leg.lineTo(18, -40);
        g.draw(leg);
        g.setColor(new Color(245, 246, 250));
        g.fill(new Ellipse2D.Double(10, -42, 26, 11));
        g.setColor(jacket);
        g.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D.Double arm = new Path2D.Double();
        arm.moveTo(-16, -108);
        arm.lineTo(6, -92);
        arm.lineTo(34, -98);
        g.draw(arm);
        g.setColor(skin);
        g.fill(new Ellipse2D.Double(30, -104, 11, 11));
        // đầu + mũ bảo hiểm
        g.setColor(new Color(52, 40, 44));
        g.fill(new Ellipse2D.Double(-28, -148, 18, 22));
        g.setColor(skin);
        g.fill(new Ellipse2D.Double(-26, -152, 32, 32));
        g.setColor(INK);
        g.fill(new Ellipse2D.Double(-4, -141, 5, 7));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(-3, -140, 2, 2));
        g.setColor(new Color(255, 140, 150, 120));
        g.fill(new Ellipse2D.Double(0, -132, 8, 5));
        g.setColor(new Color(160, 80, 80));
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Arc2D.Double(-6, -135, 9, 6, 200, 140, Arc2D.OPEN));
        Arc2D.Double dome = new Arc2D.Double(-30, -160, 40, 38, 0, 180, Arc2D.CHORD);
        g.setColor(new Color(250, 250, 252));
        g.fill(dome);
        g.setColor(new Color(200, 208, 222));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(dome);
        g.setColor(PINK);
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g.draw(new Arc2D.Double(-26, -156, 32, 30, 65, 50, Arc2D.OPEN));
        g.setColor(new Color(200, 208, 222));
        g.fill(new RoundRectangle2D.Double(-3, -143, 14, 5, 4, 4));
        g.setTransform(old);
        g.setStroke(oldStroke);
    }
}
