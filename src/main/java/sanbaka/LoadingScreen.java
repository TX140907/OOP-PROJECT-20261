package sanbaka;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/** Immediate splash screen shown while the game and its assets are initialized. */
final class LoadingScreen extends JPanel {
    private static final int WIDTH = 960;
    private static final int HEIGHT = 640;
    private volatile BufferedImage background;
    private volatile Font titleFont = new Font("SansSerif", Font.BOLD, 52);
    private volatile int progress;
    private volatile String error;

    LoadingScreen() {
        setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT));
        setDoubleBuffered(true);
    }

    void setProgress(int value) {
        // Quy đổi tiến độ nội bộ (25–100) sang 0–100 chỉ để hiển thị trên splash.
        progress = Math.max(0, Math.min(100, (value - 25) * 100 / 75));
        repaint();
    }

    void setBackgroundImage(BufferedImage image) {
        background = image;
        repaint();
    }

    void setTitleFont(Font font) {
        if (font != null) titleFont = font.deriveFont(Font.BOLD, 52f);
        repaint();
    }

    void setError(String message) {
        error = message;
        repaint();
    }

    static BufferedImage loadBackground() {
        String[] paths = {"assets/background/sanbaka2.jpg", "OOP-PROJECT-20261/assets/background/sanbaka2.jpg"};
        for (String path : paths) {
            File file = new File(path);
            if (!file.isFile()) continue;
            try { return ImageIO.read(file); } catch (Exception ignored) {}
        }
        for (String path : new String[]{"/assets/background/sanbaka2.jpg", "/background/sanbaka2.jpg"}) {
            try (InputStream in = LoadingScreen.class.getResourceAsStream(path)) {
                if (in != null) return ImageIO.read(in);
            } catch (Exception ignored) {}
        }
        return null;
    }

    static Font loadTitleFont() {
        String[] paths = {"assets/fonts/1FTV-VIP-Starborn.ttf", "OOP-PROJECT-20261/assets/fonts/1FTV-VIP-Starborn.ttf"};
        for (String path : paths) {
            File file = new File(path);
            if (!file.isFile()) continue;
            try { return Font.createFont(Font.TRUETYPE_FONT, file).deriveFont(Font.BOLD, 52f); }
            catch (Exception ignored) {}
        }
        for (String path : new String[]{"/assets/fonts/1FTV-VIP-Starborn.ttf", "/fonts/1FTV-VIP-Starborn.ttf"}) {
            try (InputStream in = LoadingScreen.class.getResourceAsStream(path)) {
                if (in != null) return Font.createFont(Font.TRUETYPE_FONT, in).deriveFont(Font.BOLD, 52f);
            } catch (Exception ignored) {}
        }
        return new Font("SansSerif", Font.BOLD, 52);
    }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        BufferedImage image = background;
        if (image != null) {
            double scale = Math.max(WIDTH / (double) image.getWidth(), HEIGHT / (double) image.getHeight());
            int drawW = (int) Math.ceil(image.getWidth() * scale);
            int drawH = (int) Math.ceil(image.getHeight() * scale);
            g.drawImage(image, (WIDTH - drawW) / 2, (HEIGHT - drawH) / 2, drawW, drawH, null);
        } else {
            g.setPaint(new GradientPaint(0, 0, new Color(41, 43, 61), 0, HEIGHT, new Color(127, 70, 93)));
            g.fillRect(0, 0, WIDTH, HEIGHT);
        }
        g.setPaint(new GradientPaint(0, 0, new Color(12, 17, 30, 110), 0, HEIGHT, new Color(12, 17, 30, 205)));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setFont(titleFont);
        FontMetrics fm = g.getFontMetrics();
        String title = "TUẦN THI CỦA SVBK";
        int titleX = (WIDTH - fm.stringWidth(title)) / 2;
        g.setColor(new Color(0, 0, 0, 130));
        g.drawString(title, titleX + 3, 247 + 5);
        g.setColor(new Color(255, 246, 231));
        g.drawString(title, titleX, 247);

        int barX = 220, barY = 374, barW = 520, barH = 25;
        g.setColor(new Color(18, 25, 37, 210));
        g.fillRoundRect(barX, barY, barW, barH, barH, barH);
        g.setColor(new Color(255, 255, 255, 225));
        g.drawRoundRect(barX, barY, barW, barH, barH, barH);
        int fillW = (barW - 6) * progress / 100;
        if (fillW > 0) {
            g.setPaint(new GradientPaint(barX + 3, barY, Color.WHITE, barX + 3, barY + barH, new Color(232, 238, 245)));
            g.fillRoundRect(barX + 3, barY + 3, fillW, barH - 6, barH - 6, barH - 6);
        }
        g.setFont(new Font("Dialog", Font.BOLD, 16));
        String label = error == null ? "ĐANG TẢI TÀI NGUYÊN...  " + progress + "%" : "KHÔNG THỂ KHỞI ĐỘNG GAME";
        fm = g.getFontMetrics();
        g.setColor(Color.WHITE);
        g.drawString(label, (WIDTH - fm.stringWidth(label)) / 2, barY + 57);
        if (error != null) {
            g.setFont(new Font("Dialog", Font.PLAIN, 13));
            g.drawString(error, 60, barY + 82);
        }
        g.dispose();
    }
}
