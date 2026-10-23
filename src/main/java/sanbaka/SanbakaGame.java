package sanbaka;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import sanbaka.logic.StoryData;
import sanbaka.render.GameScreenRenderer;

public class SanbakaGame extends GameScreenRenderer {
    public SanbakaGame() {
        titleFont = loadTitleFont();
        setPreferredSize(new Dimension(W, H));
        setFocusable(true);
        addKeyListener(this);
        lines.addAll(StoryData.build());
        findCallRange();
        buildScenes();

        // loadProgress(); // hàm load tiến độ chơi game (nếu muốn tiếp tục)
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
        timer.setCoalesce(true);
        // Khởi động thread giữ độ phân giải timer 1ms cho Windows OS (tránh trễ 15.6ms)
        Thread timerPrecisionThread = new Thread(() -> {
            try {
                while (true) Thread.sleep(Integer.MAX_VALUE);
            } catch (InterruptedException ignored) {}
        });
        timerPrecisionThread.setDaemon(true);
        timerPrecisionThread.setName("HighPrecisionTimerFixer");
        timerPrecisionThread.start();

        timer.start();
        dialogBackground = loadDialogBackground();
    }

    private java.awt.Font loadTitleFont() {
        // 1. Thử nạp từ classpath (JAR hoặc target/classes hoặc resources)
        try (java.io.InputStream in = SanbakaGame.class.getResourceAsStream("/assets/fonts/1FTV-VIP-Starborn.ttf")) {
            if (in != null) {
                return java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, in)
                        .deriveFont(java.awt.Font.BOLD, 42f);
            }
        } catch (Exception ignored) {}

        // 2. Thử nạp từ file hệ thống theo các đường dẫn tương đối
        String[] fontPaths = {
            "assets/fonts/1FTV-VIP-Starborn.ttf",
            "src/main/resources/assets/fonts/1FTV-VIP-Starborn.ttf"
        };
        for (String path : fontPaths) {
            java.io.File fontFile = new java.io.File(path);
            if (fontFile.isFile()) {
                try {
                    return java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, fontFile)
                            .deriveFont(java.awt.Font.BOLD, 42f);
                } catch (java.awt.FontFormatException | java.io.IOException ignored) {}
            }
        }
        return new java.awt.Font("SansSerif", java.awt.Font.BOLD, 42);
    }

    private java.awt.image.BufferedImage loadDialogBackground() {
        // 1. Thử nạp từ classpath
        try (java.io.InputStream in = SanbakaGame.class.getResourceAsStream("/assets/background/sanbaka2.jpg")) {
            if (in != null) {
                return javax.imageio.ImageIO.read(in);
            }
        } catch (Exception ignored) {}

        // 2. Thử nạp từ file hệ thống
        String[] paths = {
            "assets/background/sanbaka2.jpg",
            "src/main/resources/assets/background/sanbaka2.jpg"
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

    static {
        if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
            if (System.getProperty("sun.java2d.d3d") == null) {
                System.setProperty("sun.java2d.d3d", "true");
            }
            if (System.getProperty("sun.java2d.transaccel") == null) {
                System.setProperty("sun.java2d.transaccel", "true");
            }
            if (System.getProperty("sun.java2d.ddforcevram") == null) {
                System.setProperty("sun.java2d.ddforcevram", "true");
            }
            if (System.getProperty("sun.java2d.noddraw") == null) {
                System.setProperty("sun.java2d.noddraw", "false");
            }
            if (System.getProperty("sun.java2d.opengl") == null) {
                System.setProperty("sun.java2d.opengl", "false");
            }
            if (System.getProperty("sun.awt.noerasebackground") == null) {
                System.setProperty("sun.awt.noerasebackground", "true");
            }
        }
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
