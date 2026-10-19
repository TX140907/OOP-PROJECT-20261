package sanbaka;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class SanbakaGame extends GameScreenRenderer {
    public SanbakaGame() {
        titleFont = loadTitleFont();
        setPreferredSize(new Dimension(W, H));
        setFocusable(true);
        addKeyListener(this);
        lines.addAll(StoryData.build());
        findCallRange();
        buildScenes();

        //loadProgress(); //hàm load tiến độ chơi game
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                if (e.getButton() == java.awt.event.MouseEvent.BUTTON1) {
                    handleClick(e.getX(), e.getY());
                }
                requestFocusInWindow();
                repaint();
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                onMouse(-1, -1);
            }
        });
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseMoved(java.awt.event.MouseEvent e) { onMouse(e.getX(), e.getY()); }
            @Override public void mouseDragged(java.awt.event.MouseEvent e) { onMouse(e.getX(), e.getY()); }
        });
        timer.start();
        dialogBackground = loadDialogBackground();
    }

    private java.awt.Font loadTitleFont() {
        java.io.File fontFile = new java.io.File("assets/fonts/1FTV-VIP-Starborn.ttf");
        if (fontFile.isFile()) {
            try {
                return java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, fontFile)
                        .deriveFont(java.awt.Font.BOLD, 42f);
            } catch (java.awt.FontFormatException | java.io.IOException ignored) {
                // Dùng font hệ thống nếu font tùy chỉnh không đọc được.
            }
        }
        return new java.awt.Font("SansSerif", java.awt.Font.BOLD, 42);
    }

    private java.awt.image.BufferedImage loadDialogBackground() {
        String[] paths = {
            "assets/background/sanbaka2.jpg"
        };
        for (String path : paths) {
            java.io.File imageFile = new java.io.File(path);
            if (!imageFile.isFile()) continue;
            try {
                return javax.imageio.ImageIO.read(imageFile);
            } catch (java.io.IOException e) {
                System.err.println("Không đọc được ảnh nền: " + imageFile.getAbsolutePath());
                return null;
            }
        }
        System.err.println("Không tìm thấy sanbaka2.jpg; game sẽ dùng nền mặc định.");
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Tuần thi của SVBK");
            SanbakaGame game = new SanbakaGame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(game);
            frame.pack();
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            game.requestFocusInWindow();
        });
    }
}
