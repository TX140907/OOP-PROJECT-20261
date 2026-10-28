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
            @Override public void mouseMoved(java.awt.event.MouseEvent e) { onMouse(e.getX(), e.getY()); repaint(); }
            @Override public void mouseDragged(java.awt.event.MouseEvent e) { onMouse(e.getX(), e.getY()); repaint(); }
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
        afterExamBackground = loadAfterExamBackground("Toa_C1.png");
        afterExamRainBackground = loadAfterExamBackground("Toa_C1_mua.png");
        afterExamSnowBackground = loadAfterExamBackground("Toa_C1_tuyet.png");
        afterExamSunBackground = loadAfterExamBackground("Toa_C1_nang.png");
        secretEndingBackground = loadAfterExamBackground("sanbaka2.jpg");
        sanbakaExpressionImages.put("binh_thuong", loadSanbakaExpression("binh_thuong.png"));
        sanbakaExpressionImages.put("lo_lang", loadSanbakaExpression("lo_lang.png"));
        sanbakaExpressionImages.put("vui_cuoi", loadSanbakaExpression("vui_cuoi.png"));
        sanbakaExpressionImages.put("xau_ho", loadSanbakaExpression("xau_ho.png"));
    }

    private java.awt.image.BufferedImage loadSanbakaExpression(String fileName) {
        String[] resources = {
            "/assets/expression/" + fileName,
            "/expression/" + fileName
        };
        for (String resource : resources) {
            try (java.io.InputStream in = SanbakaGame.class.getResourceAsStream(resource)) {
                if (in != null) return scaleExpressionImage(javax.imageio.ImageIO.read(in));
            } catch (Exception ignored) {}
        }

        java.io.File imageFile = new java.io.File("assets/expression", fileName);
        if (!imageFile.isFile()) return null;
        try {
            return scaleExpressionImage(javax.imageio.ImageIO.read(imageFile));
        } catch (java.io.IOException ignored) {
            return null;
        }
    }

    private java.awt.image.BufferedImage scaleExpressionImage(java.awt.image.BufferedImage source) {
        if (source == null) return null;
        int targetH = (int) Math.round(H * 1.25);
        int targetW = (int) Math.round(source.getWidth() * (targetH / (double) source.getHeight()));
        if (source.getWidth() == targetW && source.getHeight() == targetH) return source;
        java.awt.image.BufferedImage scaled = new java.awt.image.BufferedImage(
                targetW, targetH, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(source, 0, 0, targetW, targetH, null);
        g.dispose();
        source.flush();
        return scaled;
    }

    private java.awt.image.BufferedImage loadAfterExamBackground(String fileName) {
        String[] resources = {
            "/assets/background/" + fileName,
            "/background/" + fileName
        };
        for (String resource : resources) {
            try (java.io.InputStream in = SanbakaGame.class.getResourceAsStream(resource)) {
                if (in != null) return scaleAfterExamBackground(javax.imageio.ImageIO.read(in));
            } catch (Exception ignored) {}
        }

        java.io.File imageFile = new java.io.File("assets/background", fileName);
        if (!imageFile.isFile()) return null;
        try {
            return scaleAfterExamBackground(javax.imageio.ImageIO.read(imageFile));
        } catch (java.io.IOException ignored) {
            return null;
        }
    }

    private java.awt.image.BufferedImage scaleAfterExamBackground(java.awt.image.BufferedImage source) {
        if (source == null || (source.getWidth() == W && source.getHeight() == H)) return source;

        int imageType = source.getColorModel().hasAlpha()
                ? java.awt.image.BufferedImage.TYPE_INT_ARGB
                : java.awt.image.BufferedImage.TYPE_INT_RGB;
        java.awt.image.BufferedImage scaled = new java.awt.image.BufferedImage(W, H, imageType);
        java.awt.Graphics2D graphics = scaled.createGraphics();
        graphics.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        graphics.drawImage(source, 0, 0, W, H, null);
        graphics.dispose();
        source.flush();
        return scaled;
    }

    private java.awt.Font loadTitleFont() {
        // 1. Nạp trực tiếp từ thư mục assets ngoài cùng của dự án
        String[] fontPaths = {
            "assets/fonts/1FTV-VIP-Starborn.ttf",
            "OOP-PROJECT-20261/assets/fonts/1FTV-VIP-Starborn.ttf"
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
        // 2. Fallback qua classpath (khi thư mục assets được đưa vào -cp)
        for (String res : new String[]{"/fonts/1FTV-VIP-Starborn.ttf", "/assets/fonts/1FTV-VIP-Starborn.ttf"}) {
            try (java.io.InputStream in = SanbakaGame.class.getResourceAsStream(res)) {
                if (in != null) {
                    return java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, in)
                            .deriveFont(java.awt.Font.BOLD, 42f);
                }
            } catch (Exception ignored) {}
        }
        return new java.awt.Font("SansSerif", java.awt.Font.BOLD, 42);
    }

    private java.awt.image.BufferedImage loadDialogBackground() {
        // 0. Ưu tiên phát video lặp lại (loop) opening_video.mp4 thay cho ảnh nền sanbaka2
        String[] videoPaths = {
            "assets/background/opening_video.mp4",
            "OOP-PROJECT-20261/assets/background/opening_video.mp4"
        };
        for (String vPath : videoPaths) {
            java.io.File videoFile = new java.io.File(vPath);
            if (videoFile.isFile()) {
                Thread videoStartupThread = new Thread(() ->
                        sanbaka.assets.VideoBackgroundPlayer.startLoopingVideo(
                                videoFile,
                                W,
                                H,
                                () -> screen != ROAD && screen != AIR && screen != EXAM && screen != ENDING_BE && screen != ENDING_HE && screen != ENDING_SE && screen != MARIO,
                                frame -> {
                                    dialogBackground = frame;
                                    videoFrameDirty = true;
                                }
                        ), "OpeningVideoStartupThread");
                videoStartupThread.setDaemon(true);
                videoStartupThread.start();
                break;
            }
        }
        // 1. Nạp trực tiếp từ thư mục assets ngoài cùng của dự án (fallback nếu không có video)
        String[] paths = {
            "assets/background/sanbaka2.jpg",
            "OOP-PROJECT-20261/assets/background/sanbaka2.jpg"
        };
        for (String path : paths) {
            java.io.File imageFile = new java.io.File(path);
            if (!imageFile.isFile()) continue;
            try {
                return scaleAfterExamBackground(javax.imageio.ImageIO.read(imageFile));
            } catch (java.io.IOException e) {
                System.err.println("Không đọc được ảnh nền: " + imageFile.getAbsolutePath());
                return null;
            }
        }
        // 2. Fallback qua classpath
        for (String res : new String[]{"/background/sanbaka2.jpg", "/assets/background/sanbaka2.jpg"}) {
            try (java.io.InputStream in = SanbakaGame.class.getResourceAsStream(res)) {
                if (in != null) {
                    return javax.imageio.ImageIO.read(in);
                }
            } catch (Exception ignored) {}
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
            java.net.URL iconUrl = SanbakaGame.class.getResource("/assets/avatars/Sanbaka.jpg");
            if (iconUrl != null) {
                try {
                    frame.setIconImage(javax.imageio.ImageIO.read(iconUrl));
                } catch (java.io.IOException ignored) {
                    // Keep the default window icon if the image cannot be loaded.
                }
            }
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
