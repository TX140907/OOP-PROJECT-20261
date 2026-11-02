package sanbaka;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.IntConsumer;
import sanbaka.logic.StoryData;
import sanbaka.render.GameScreenRenderer;

public class SanbakaGame extends GameScreenRenderer {
    public SanbakaGame() {
        this(null, null, null);
        startGameLoop();
    }

    SanbakaGame(IntConsumer progressListener, BufferedImage loadingBackground, Font loadingTitleFont) {
        IntConsumer progress = progressListener == null ? value -> {} : progressListener;
        titleFont = loadingTitleFont == null ? loadTitleFont() : loadingTitleFont.deriveFont(Font.BOLD, 42f);
        progress.accept(22);
        setPreferredSize(new Dimension(W, H));
        setFocusable(true);
        addKeyListener(this);
        lines.addAll(StoryData.build());
        findCallRange();
        buildScenes();
        progress.accept(32);

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

        dialogBackground = loadDialogBackground(loadingBackground);
        progress.accept(48);
        afterExamBackground = loadAfterExamBackground("Toa_C1.png");
        progress.accept(56);
        afterExamRainBackground = loadAfterExamBackground("Toa_C1_mua.png");
        progress.accept(64);
        afterExamSnowBackground = loadAfterExamBackground("Toa_C1_tuyet.png");
        progress.accept(70);
        afterExamSunBackground = loadAfterExamBackground("Toa_C1_nang.png");
        progress.accept(76);
        secretEndingBackground = loadAfterExamBackground("sanbaka2.jpg");
        progress.accept(81);
        afterDateBackground = loadAfterExamBackground("Toa_C1_toi.png");
        progress.accept(85);
        weddingBackground = loadAfterExamBackground("Le_duong.png");
        progress.accept(89);
        trafficJamBackground = loadAfterExamBackground("tac_duong.png");
        progress.accept(92);
        sanbakaExpressionImages.put("binh_thuong", loadSanbakaExpression("sanbaka_ngay_thuong", "binh_thuong.png"));
        progress.accept(93);
        sanbakaExpressionImages.put("lo_lang", loadSanbakaExpression("sanbaka_ngay_thuong", "lo_lang.png"));
        progress.accept(94);
        sanbakaExpressionImages.put("vui_cuoi", loadSanbakaExpression("sanbaka_ngay_thuong", "vui_cuoi.png"));
        progress.accept(95);
        sanbakaExpressionImages.put("xau_ho", loadSanbakaExpression("sanbaka_ngay_thuong", "xau_ho.png"));
        progress.accept(96);
        sanbakaExpressionImages.put("ngai_ngung", loadSanbakaExpression("sanbaka_ngay_thuong", "ngai_ngung.png"));
        progress.accept(97);
        sanbakaExpressionImages.put("wedding_binh_thuong", loadSanbakaExpression("sanbaka_vay_cuoi", "binh_thuong.png"));
        progress.accept(98);
        sanbakaExpressionImages.put("wedding_vui_cuoi", loadSanbakaExpression("sanbaka_vay_cuoi", "vui_cuoi.png"));
        progress.accept(99);
        sanbakaExpressionImages.put("wedding_xau_ho", loadSanbakaExpression("sanbaka_vay_cuoi", "xau_ho.png"));
        progress.accept(100);
    }

    public void startGameLoop() {
        timer.start();
    }

    private java.awt.image.BufferedImage loadSanbakaExpression(String folder, String fileName) {
        String[] resources = {
            "/assets/expression/" + folder + "/" + fileName,
            "/expression/" + folder + "/" + fileName
        };
        for (String resource : resources) {
            try (java.io.InputStream in = SanbakaGame.class.getResourceAsStream(resource)) {
                if (in != null) return scaleExpressionImage(javax.imageio.ImageIO.read(in));
            } catch (Exception ignored) {}
        }

        java.io.File imageFile = new java.io.File(new java.io.File("assets/expression", folder), fileName);
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

    private java.awt.image.BufferedImage loadDialogBackground(BufferedImage preloadedBackground) {
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
        if (preloadedBackground != null) return scaleAfterExamBackground(preloadedBackground);
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
        if (args.length >= 2 && "--external-loader".equals(args[0])) {
            startWithExternalLoader(Paths.get(args[1]));
            return;
        }
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Tuần thi của SVBK");
            LoadingScreen loadingScreen = new LoadingScreen();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(loadingScreen);
            frame.pack();
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            Thread loadingThread = new Thread(() -> {
                try {
                    loadingScreen.setProgress(25);
                    BufferedImage loadingBackground = LoadingScreen.loadBackground();
                    SwingUtilities.invokeLater(() -> {
                        loadingScreen.setBackgroundImage(loadingBackground);
                        loadingScreen.setProgress(28);
                    });
                    Font loadingTitleFont = LoadingScreen.loadTitleFont();
                    SwingUtilities.invokeLater(() -> {
                        loadingScreen.setTitleFont(loadingTitleFont);
                        loadingScreen.setProgress(31);
                    });
                    SanbakaGame game = new SanbakaGame(
                            value -> SwingUtilities.invokeLater(() -> loadingScreen.setProgress(
                                    31 + (int) Math.round((value - 22) * 69.0 / 78.0))),
                            loadingBackground,
                            loadingTitleFont);
                    SwingUtilities.invokeLater(() -> {
                        frame.setContentPane(game);
                        frame.pack();
                        frame.setLocationRelativeTo(null);
                        game.startGameLoop();
                        game.requestFocusInWindow();
                    });
                } catch (Throwable error) {
                    error.printStackTrace();
                    String message = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
                    SwingUtilities.invokeLater(() -> loadingScreen.setError(message));
                }
            }, "SanbakaResourceLoader");
            loadingThread.setDaemon(true);
            loadingThread.start();
        });
    }

    private static void startWithExternalLoader(Path progressFile) {
        writeLoadProgress(progressFile, 25);
        Thread loadingThread = new Thread(() -> {
            try {
                SanbakaGame game = new SanbakaGame(
                        value -> writeLoadProgress(progressFile,
                                31 + (int) Math.round((value - 22) * 68.0 / 78.0)),
                        null,
                        null);
                SwingUtilities.invokeLater(() -> {
                    JFrame frame = new JFrame("Tuần thi của SVBK");
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setUndecorated(true);
                    frame.setTitle("SANBAKA_GAME_CONTENT");
                    frame.setContentPane(game);
                    frame.pack();
                    frame.setResizable(false);
                    frame.addNotify();
                    try {
                        Path windowFile = progressFile.resolveSibling("loading-window.txt");
                        Path embeddedFile = progressFile.resolveSibling("loading-embedded.txt");
                        Files.deleteIfExists(embeddedFile);
                        Files.write(windowFile, Long.toString(ProcessHandle.current().pid()).getBytes(StandardCharsets.US_ASCII));
                        writeLoadProgress(progressFile, 100);
                        Thread handoff = new Thread(() -> {
                            long deadline = System.currentTimeMillis() + 60_000;
                            while (System.currentTimeMillis() < deadline) {
                                if (Files.exists(embeddedFile)) {
                                    SwingUtilities.invokeLater(() -> {
                                        frame.setVisible(true);
                                        game.startGameLoop();
                                        game.requestFocusInWindow();
                                    });
                                    return;
                                }
                                try { Thread.sleep(20); } catch (InterruptedException ignored) { return; }
                            }
                            writeLoadProgress(progressFile, -1);
                        }, "SanbakaWindowHandoff");
                        handoff.setDaemon(true);
                        handoff.start();
                    } catch (Exception error) {
                        throw new RuntimeException(error);
                    }
                });
            } catch (Throwable error) {
                error.printStackTrace();
                writeLoadProgress(progressFile, -1);
            }
        }, "SanbakaResourceLoader");
        loadingThread.setDaemon(false);
        loadingThread.start();
    }

    private static void writeLoadProgress(Path progressFile, int progress) {
        try {
            Files.write(progressFile, Integer.toString(progress).getBytes(StandardCharsets.US_ASCII));
        } catch (Exception ignored) {}
    }
}
