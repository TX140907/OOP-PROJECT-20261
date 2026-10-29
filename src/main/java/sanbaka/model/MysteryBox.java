package sanbaka.model;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import sanbaka.assets.ArtAssets;
import sanbaka.render.GameVisuals;

/**
 * Hộp bí ẩn (Mystery Box) - Easter Egg của game:
 * - Khối vuông màu vàng với dấu hỏi chấm ? ở giữa.
 * - Xuất hiện ở mép lề đường (curb): một nửa nằm ở lòng đường và một nửa lồi lên vỉa hè (cần cọ lề để nhặt).
 * - Xuất hiện tối đa đúng 1 lần trong suốt hành trình (ở Màn 2 với tỉ lệ tuyệt đối 100%, hoặc Màn 4 nếu Màn 2 chưa xuất hiện).
 * - Nhặt hộp này sẽ mở khóa Happy Ending ẩn (Minigame Mario 8-bit sau khi vượt qua kì thi).
 */
public class MysteryBox extends Thing {
    public MysteryBox() {
        kind = 19;
        snack = true;
        w = 36;
        h = 36;
    }

    @Override
    public void draw(Graphics2D g, GameVisuals scene) {
        long now = System.currentTimeMillis();
        double scale = 1.0 + 0.08 * Math.sin(now / 150.0);
        int dw = (int) Math.round(w * scale);
        int dh = (int) Math.round(h * scale);
        int dx = (int) Math.round(x - dw / 2.0);
        int dy = (int) Math.round(y - dh / 2.0);

        BufferedImage art = ArtAssets.load("MysteryBox", "MysteryBox.png");
        if (art != null) {
            g.drawImage(art, dx, dy, dw, dh, null);
            return;
        }

        // Vẽ hộp bí ẩn hình vuông có dấu ? màu vàng kim retro
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Hiệu ứng hào quang phát sáng (Aura)
        float pulse = (float) (0.5 + 0.5 * Math.sin(now / 180.0));
        g2.setColor(new Color(255, 215, 0, (int) (60 + 50 * pulse)));
        g2.fillOval(dx - 6, dy - 6, dw + 12, dh + 12);

        // Thân khối vuông màu vàng cam
        g2.setColor(new Color(0xFC, 0x98, 0x38));
        g2.fillRoundRect(dx, dy, dw, dh, 8, 8);

        // Viền bóng đen retro
        g2.setColor(new Color(0x30, 0x18, 0x00));
        g2.drawRoundRect(dx, dy, dw, dh, 8, 8);

        // Highlight sáng cạnh trên và cạnh trái
        g2.setColor(new Color(0xFC, 0xD8, 0xA8));
        g2.drawLine(dx + 2, dy + 2, dx + dw - 3, dy + 2);
        g2.drawLine(dx + 2, dy + 2, dx + 2, dy + dh - 3);

        // Bóng tối cạnh dưới và cạnh phải
        g2.setColor(new Color(0x80, 0x40, 0x00));
        g2.drawLine(dx + 3, dy + dh - 2, dx + dw - 2, dy + dh - 2);
        g2.drawLine(dx + dw - 2, dy + 3, dx + dw - 2, dy + dh - 2);

        // 4 đinh tán ở góc
        g2.setColor(new Color(0x50, 0x28, 0x00));
        int rivet = Math.max(2, dw / 12);
        g2.fillRect(dx + 3, dy + 3, rivet, rivet);
        g2.fillRect(dx + dw - 3 - rivet, dy + 3, rivet, rivet);
        g2.fillRect(dx + 3, dy + dh - 3 - rivet, rivet, rivet);
        g2.fillRect(dx + dw - 3 - rivet, dy + dh - 3 - rivet, rivet, rivet);

        // Dấu hỏi chấm '?' ở giữa
        g2.setColor(new Color(0x50, 0x20, 0x00));
        g2.setFont(new Font("Monospaced", Font.BOLD, (int) (dw * 0.65)));
        String q = "?";
        int qw = g2.getFontMetrics().stringWidth(q);
        int qh = g2.getFontMetrics().getAscent();
        g2.drawString(q, (int) Math.round(x - qw / 2.0), (int) Math.round(y + qh / 2.0 - 3));

        g2.dispose();
    }
}

