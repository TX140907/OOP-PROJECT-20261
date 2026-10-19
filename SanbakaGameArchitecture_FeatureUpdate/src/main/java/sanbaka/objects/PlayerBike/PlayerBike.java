package sanbaka;

import java.awt.*;
import java.awt.geom.*;

/** Xe máy của người chơi, giữ trạng thái lái và tự vẽ chính nó. */
class PlayerBike extends PlayerVehicle {
    void draw(Graphics2D g, GameVisuals scene) {
        int px = (int) Math.round(x), py = (int) Math.round(y);
        long now = System.currentTimeMillis();
        Composite oldComposite = g.getComposite();
        AffineTransform oldTransform = g.getTransform();
        Stroke oldStroke = g.getStroke();
        if (invulnerable > now && (now / 90) % 2 == 0) {
            g.setComposite(AlphaComposite.SrcOver.derive(0.35f));
        }
        Color tire = new Color(32, 34, 42);
        Color orange = new Color(255, 143, 66);
        Color orangeD = new Color(214, 104, 36);
        Color jacket = new Color(45, 61, 87);
        Color jeans = new Color(62, 84, 122);
        Color skin = new Color(255, 224, 196);
        Color bar = new Color(40, 44, 56);
        // khói xả
        for (int i = 0; i < 3; i++) {
            g.setColor(new Color(235, 235, 235, 70 - i * 20));
            int s = 8 + i * 5;
            int wob = (int) (Math.sin(now / 130.0 + i * 1.7) * 3);
            g.fillOval(px - s / 2 + wob, py + 48 + i * 9, s, s);
        }
        java.awt.image.BufferedImage art = ArtAssets.load("PlayerBike", "PlayerBike.png");
        if (art != null) {
            g.drawImage(art, px - 30, py - 50, 60, 100, null);
            g.setTransform(oldTransform);
            g.setComposite(oldComposite);
            g.setStroke(oldStroke);
            return;
        }
        g.translate(px, py);
        g.scale(0.6, 0.6);
        g.translate(-px, -py);
        g.rotate(lean * 0.13, px, py + 20);
        // bóng đổ
        g.setColor(new Color(0, 0, 0, 48));
        g.fillOval(px - 20, py - 28, 40, 80);
        // bánh trước + chắn gió
        g.setColor(tire);
        g.fillRoundRect(px - 4, py - 48, 8, 18, 6, 6);
        g.setColor(orange);
        g.fillRoundRect(px - 9, py - 40, 18, 15, 10, 10);
        g.setColor(new Color(255, 255, 255, 90));
        g.fillRoundRect(px - 5, py - 38, 6, 5, 4, 4);
        // tay lái + gương
        g.setColor(bar);
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(px - 22, py - 31, px + 22, py - 31);
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(px - 20, py - 32, px - 24, py - 41);
        g.drawLine(px + 20, py - 32, px + 24, py - 41);
        g.setColor(new Color(186, 218, 238));
        g.fillOval(px - 30, py - 47, 11, 8);
        g.fillOval(px + 19, py - 47, 11, 8);
        g.setColor(bar);
        g.setStroke(new BasicStroke(1.4f));
        g.drawOval(px - 30, py - 47, 11, 8);
        g.drawOval(px + 19, py - 47, 11, 8);
        // thân xe, bánh sau (hoa lốp chạy), đuôi xe
        g.setColor(orange);
        g.fillRoundRect(px - 10, py - 26, 20, 36, 12, 12);
        g.setColor(tire);
        g.fillRoundRect(px - 6, py + 14, 12, 32, 8, 8);
        g.setColor(new Color(78, 82, 96));
        int off = (int) (((-now / 30) % 6 + 6) % 6);
        for (int ty = py + 17 + off; ty < py + 43; ty += 6) g.drawLine(px - 4, ty, px + 4, ty);
        g.setColor(orange);
        g.fillRoundRect(px - 11, py + 4, 22, 24, 12, 12);
        g.setColor(orangeD);
        g.fillRoundRect(px - 2, py + 7, 4, 17, 4, 4);
        g.setColor(new Color(255, 70, 80, 80));
        g.fillOval(px - 11, py + 21, 22, 12);
        g.setColor(new Color(255, 70, 80));
        g.fillRoundRect(px - 6, py + 25, 12, 4, 3, 3);
        // yên + chân
        g.setColor(new Color(58, 46, 48));
        g.fillRoundRect(px - 9, py - 6, 18, 20, 10, 10);
        g.setColor(jeans);
        g.fillRoundRect(px - 18, py - 8, 9, 22, 8, 8);
        g.fillRoundRect(px + 9, py - 8, 9, 22, 8, 8);
        g.setColor(new Color(245, 246, 250));
        g.fillRoundRect(px - 19, py - 16, 10, 9, 6, 6);
        g.fillRoundRect(px + 9, py - 16, 10, 9, 6, 6);
        // tay nắm ghi-đông
        g.setColor(jacket);
        g.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(px - 11, py - 12, px - 19, py - 30);
        g.drawLine(px + 11, py - 12, px + 19, py - 30);
        g.setColor(skin);
        g.fillOval(px - 24, py - 36, 9, 9);
        g.fillOval(px + 15, py - 36, 9, 9);
        // thân người + balo
        g.setColor(jacket);
        g.fillRoundRect(px - 13, py - 17, 26, 28, 14, 14);
        g.setColor(GameState.MINT);
        g.fillRoundRect(px - 10, py - 15, 20, 24, 10, 10);
        g.setColor(new Color(63, 145, 121));
        g.fillRoundRect(px - 7, py - 1, 14, 8, 6, 6);
        g.setColor(new Color(255, 255, 255, 150));
        g.setStroke(new BasicStroke(1.2f));
        g.drawLine(px, py - 13, px, py - 3);
        scene.drawHeart(g, px + 7, py + 9, 7, GameState.PINK);
        // tóc gáy, tai, mũ bảo hiểm
        g.setColor(new Color(52, 40, 44));
        g.fillOval(px - 8, py - 17, 16, 9);
        g.setColor(skin);
        g.fillOval(px - 14, py - 27, 4, 7);
        g.fillOval(px + 10, py - 27, 4, 7);
        g.setColor(new Color(250, 250, 252));
        g.fillOval(px - 12, py - 37, 24, 25);
        g.setColor(new Color(200, 208, 222));
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(px - 12, py - 37, 24, 25);
        g.setColor(GameState.PINK);
        g.fillRoundRect(px - 2, py - 36, 4, 22, 3, 3);
        g.setColor(new Color(150, 200, 235, 150));
        g.fillOval(px - 9, py - 34, 6, 4);
        g.setTransform(oldTransform);
        g.setComposite(oldComposite);
        g.setStroke(oldStroke);
    }
}
