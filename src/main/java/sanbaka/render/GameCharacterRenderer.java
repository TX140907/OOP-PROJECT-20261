package sanbaka.render;

import sanbaka.model.*;
import sanbaka.state.GameState;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;

public class GameCharacterRenderer extends GameEnvironmentRenderer {
    private static final Font[] DAY_TITLE_FONTS = {
        new Font("Dialog", Font.BOLD, 16),
        new Font("Dialog", Font.BOLD, 13),
        new Font("Dialog", Font.BOLD, 13),
        new Font("Dialog", Font.BOLD, 16),
        new Font("Dialog", Font.BOLD, 15)
    };
    private static final Font FONT_HUD_11 = new Font("Dialog", Font.BOLD, 11);
    private static final Font FONT_HUD_14 = new Font("Dialog", Font.BOLD, 14);
    private static final Font FONT_HUD_16 = new Font("Dialog", Font.BOLD, 16);
    private static final Color COLOR_FPS_TAG = new Color(125, 134, 148);

    void drawHud(Graphics2D g) {
        long now = System.currentTimeMillis();
        boolean hot = day == HOT_DAY && !flightMode;
        panel(g, 20, 18, 270, hot ? 116 : 88, new Color(255, 255, 255, 238));
        g.setColor(INK);
        g.setFont(DAY_TITLE_FONTS[Math.max(0, Math.min(4, day))]);
        g.drawString(dayTitles[day], 38, 44);

        // thanh HP (Màn 5 trực thăng max HP = 150, các màn đường bộ max HP = 100)
        int maxHp = flightMode ? FLIGHT_START_HP : 100;
        int shake = now - lastHitAt < 350 ? (int) (Math.sin(now / 22.0) * 3) : 0;
        drawHeart(g, 47 + shake, 68, 22, new Color(232, 70, 100));
        int bx = 66 + shake, by = 60, bw = 206, bh = 16;
        g.setColor(new Color(222, 224, 228));
        g.fillRoundRect(bx, by, bw, bh, 10, 10);
        int fill = (int) (bw * Math.max(0, hp) / (double) maxHp);
        if (fill > 0) {
            double hpRatio = (double) hp / maxHp;
            g.setColor(hpRatio > 0.5 ? MINT : hpRatio > 0.25 ? new Color(240, 176, 64) : new Color(232, 70, 90));
            g.fillRoundRect(bx, by, Math.max(fill, 10), bh, 10, 10);
        }
        g.setFont(FONT_HUD_11);
        String ht = "HP  " + Math.max(0, hp) + " / " + maxHp;
        int hx = bx + bw / 2 - g.getFontMetrics().stringWidth(ht) / 2;
        g.setColor(INK);
        g.drawString(ht, hx, by + 12);
        // thanh năng lượng (chỉ ngày nắng nóng)
        if (hot) {
            drawBolt(g, 47, 96, 0.62);
            g.setColor(new Color(222, 224, 228));
            g.fillRoundRect(66, 88, 206, 16, 10, 10);
            int ef = (int) (206 * Math.max(0, energy) / 100.0);
            boolean low = energy < 25;
            if (ef > 0) {
                g.setColor(low && (now / 250) % 2 == 0 ? new Color(232, 70, 90) : new Color(255, 196, 40));
                g.fillRoundRect(66, 88, Math.max(ef, 10), 16, 10, 10);
            }
            g.setFont(FONT_HUD_11);
            String et = "NĂNG LƯỢNG  " + (int) Math.ceil(Math.max(0, energy));
            int ex = 66 + 103 - g.getFontMetrics().stringWidth(et) / 2;
            g.setColor(INK);
            g.drawString(et, ex, 100);
        }
        panel(g, 700, 18, 238, 58, new Color(255, 255, 255, 225));
        g.setColor(INK);
        g.setFont(FONT_HUD_14);
        g.drawString(flightMode ? "ĐƯỜNG BAY" : "ĐƯỜNG ĐI", 720, 42);
        g.setColor(new Color(98, 102, 114));
        g.fillRoundRect(720, 49, 196, 8, 8, 8);
        g.setColor(PINK);
        g.fillRoundRect(720, 49, Math.min(196, (int) (196.0 * stageProgressMs / getStageDurationMs())), 8, 8, 8);
        if (currentProgressFactor < 0.99) {
            g.setFont(FONT_HUD_11);
            g.setColor(new Color(220, 62, 48));
            String slowTag = "TỐC ĐỘ " + (int) Math.round(currentProgressFactor * 100) + "%";
            g.drawString(slowTag, 916 - g.getFontMetrics().stringWidth(slowTag), 42);
        }
        // Hiển thị chỉ số FPS thực tế
        g.setFont(FONT_HUD_11);
        g.setColor(COLOR_FPS_TAG);
        g.drawString(Math.round(currentFps) + " FPS", 702, 14);
        if (day == 4 && !flightMode && elapsed / 1000.0 >= ROAD_DURATION_MS / 3000.0 - JAM_WINDOW + 1.2) {
            g.setColor(new Color(245, 250, 255, 190));
            g.fillRoundRect(ROAD_X + 140, 150, ROAD_W - 280, 38, 16, 16);
            g.setColor(INK);
            g.setFont(new Font("Dialog", Font.BOLD, 16));
            String jam = "TẮC ĐƯỜNG…";
            g.drawString(jam, ROAD_X + ROAD_W / 2 - g.getFontMetrics().stringWidth(jam) / 2, 175);
        }
        int bannerY = 22;
        if (day == COLD_DAY && !flightMode) {
            if (windDir != 0 && now < windActiveUntil) {
                boolean warning = now < windWarningUntil;
                String arrow = windDir > 0 ? "►►" : "◄◄";
                String windMsg = warning
                        ? (windDir > 0 ? "CẢNH BÁO GIÓ MÙA ĐÔNG BẮC  " + arrow : arrow + "  CẢNH BÁO GIÓ MÙA ĐÔNG BẮC")
                        : (windDir > 0 ? "GIÓ MÙA ĐANG THỔI TẠT PHẢI  " + arrow : arrow + "  GIÓ MÙA ĐANG THỔI TẠT TRÁI");
                g.setFont(new Font("Dialog", Font.BOLD, 13));
                int mw = g.getFontMetrics().stringWidth(windMsg);
                g.setColor(warning ? new Color(255, 235, 150, 230) : new Color(205, 238, 255, 235));
                g.fillRoundRect(W / 2 - mw / 2 - 16, bannerY, mw + 32, 28, 14, 14);
                g.setColor(INK);
                g.drawString(windMsg, W / 2 - mw / 2, bannerY + 19);
                bannerY += 34;
            }
            if (now < frozenUntil) {
                String freezeMsg = "BĂNG LẠNH: TỐC ĐỘ TIẾN GIẢM CÒN 70%";
                g.setFont(new Font("Dialog", Font.BOLD, 12));
                int fw = g.getFontMetrics().stringWidth(freezeMsg);
                g.setColor(new Color(165, 225, 255, 235));
                g.fillRoundRect(W / 2 - fw / 2 - 14, bannerY, fw + 28, 26, 12, 12);
                g.setColor(new Color(20, 48, 82));
                g.drawString(freezeMsg, W / 2 - fw / 2, bannerY + 17);
                bannerY += 32;
            }
        }
        if (hot && energy < 50.0 && (now / 240) % 2 == 0) {
            String tiredMsg = energy < 25.0
                    ? "ẢO GIÁC MÊ SẢNG: SAY SÓNG CHAO ĐẢO · KHẨN CẤP NHẶT TIA SÉT!"
                    : "SAY NẮNG / KIỆT SỨC: ĐƯỜNG ĐANG CHAO ĐẢO · HÃY NHẶT TIA SÉT!";
            g.setFont(new Font("Dialog", Font.BOLD, 13));
            int tw = g.getFontMetrics().stringWidth(tiredMsg);
            g.setColor(energy < 25.0 ? new Color(255, 205, 140, 240) : new Color(255, 225, 160, 235));
            g.fillRoundRect(W / 2 - tw / 2 - 16, bannerY, tw + 32, 28, 14, 14);
            g.setColor(new Color(175, 42, 28));
            g.drawString(tiredMsg, W / 2 - tw / 2, bannerY + 19);
            bannerY += 34;
        }
        if (!flightMode && day == 1 && map2CurbSlowFactor < 0.98) {
            int slowPct = (int) Math.round(map2CurbSlowFactor * 100);
            String curbMsg = String.format("MẮC CẠN SÁT VỈA HÈ: TỐC ĐỘ GIẢM CÒN %d%% · THOÁT RA LÀN GIỮA!", slowPct);
            g.setFont(new Font("Dialog", Font.BOLD, 12));
            int cw = g.getFontMetrics().stringWidth(curbMsg);
            int boxX = W / 2 - cw / 2 - 16;
            int boxW = cw + 32;
            g.setColor(new Color(185, 225, 255, 235));
            g.fillRoundRect(boxX, bannerY, boxW, 28, 14, 14);
            g.setColor(new Color(20, 60, 110));
            g.drawString(curbMsg, W / 2 - cw / 2, bannerY + 18);
            bannerY += 34;
        } else if (!flightMode && day != 1 && (curbOnFire || curbEdgeTime >= 0.20)) {
            double ratio = Math.max(0.0, Math.min(1.0, curbEdgeTime / CURB_FIRE_THRESHOLD_SEC));
            String curbMsg = curbOnFire
                    ? "XE BỐC LỬA (-10 HP/s) · RỜI KHỎI LỀ ĐƯỜNG ĐỂ LỬA TẮT DẦN!"
                    : String.format("MA SÁT LỀ ĐƯỜNG: %.1fs / %.1fs SẼ BỐC LỬA!", curbEdgeTime, CURB_FIRE_THRESHOLD_SEC);
            g.setFont(new Font("Dialog", Font.BOLD, 12));
            int cw = Math.max(240, g.getFontMetrics().stringWidth(curbMsg));
            int boxX = W / 2 - cw / 2 - 16;
            int boxW = cw + 32;
            g.setColor(curbOnFire
                    ? ((now / 140) % 2 == 0 ? new Color(235, 48, 36, 238) : new Color(255, 118, 28, 238))
                    : new Color(255, 238, 185, 235));
            g.fillRoundRect(boxX, bannerY, boxW, 34, 14, 14);
            g.setColor(curbOnFire ? Color.WHITE : new Color(168, 42, 20));
            g.drawString(curbMsg, W / 2 - g.getFontMetrics().stringWidth(curbMsg) / 2, bannerY + 17);
            g.setColor(new Color(0, 0, 0, 55));
            g.fillRoundRect(boxX + 14, bannerY + 23, boxW - 28, 6, 6, 6);
            g.setColor(curbOnFire ? new Color(255, 240, 120) : new Color(235, 90, 28));
            g.fillRoundRect(boxX + 14, bannerY + 23, (int) ((boxW - 28) * ratio), 6, 6, 6);
            bannerY += 40;
        }
        if (now < hitSlowUntil) {
            String slowMsg = "CHẤN ĐỘNG VA CHẠM: BỊ ĐẨY LÙI & LÀM CHẬM NGẮN!";
            g.setFont(new Font("Dialog", Font.BOLD, 12));
            int sw = g.getFontMetrics().stringWidth(slowMsg);
            g.setColor(new Color(255, 210, 140, 230));
            g.fillRoundRect(W / 2 - sw / 2 - 14, bannerY, sw + 28, 24, 12, 12);
            g.setColor(new Color(140, 32, 24));
            g.drawString(slowMsg, W / 2 - sw / 2, bannerY + 16);
        }
        g.setFont(new Font("Dialog", Font.PLAIN, 13));
        String hint = flightMode ? "Né đàn chim · Đừng để HP về 0"
                : day == 1 ? "Chèo thuyền né xe cộ, thuyền và cá mập · Tránh sát lề kẻo mắc cạn giảm 50% tốc độ"
                : day == COLD_DAY ? "Tránh vũng băng làm trượt bánh · Ghì lái khi có gió mùa thổi tạt ngang"
                : hot ? "Nhặt tia sét giữ năng lượng chống ảo giác say sóng · Coi chừng chó chạy rông!"
                : "Né xe cộ, rào chắn, ổ gà & Ninja Lead tạt làn · Đừng để HP về 0";
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
