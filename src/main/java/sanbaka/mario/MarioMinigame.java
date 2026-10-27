package sanbaka.mario;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Minigame Mario 8-bit hoàn chỉnh:
 * - Hiển thị ở giữa màn hình bên trong khung máy chơi game cổ điển (Handheld Console).
 * - Xung quanh máy chơi game là nền đen.
 * - Khung máy có D-pad dấu +, 2 nút chức năng tròn B/A, phím Select/Start và khe loa retro.
 * - Phím bấm trên máy phản hồi trực quan khi người chơi thao tác bàn phím thật.
 * - Chơi mượt mà bằng bàn phím: Mũi tên / WASD, Space/W để nhảy, Shift/J để chạy nhanh, ESC để thoát.
 */
public class MarioMinigame {
    public final MarioWorld world;
    public final MarioPlayer player;

    // Vị trí và kích thước khung máy chơi game (Handheld Console)
    public static final int CONSOLE_W = 560;
    public static final int CONSOLE_H = 600;
    public static final int CONSOLE_X = (960 - CONSOLE_W) / 2; // 200
    public static final int CONSOLE_Y = 20;

    // Vị trí màn hình hiển thị game bên trong máy
    public static final int SCREEN_X = CONSOLE_X + 40; // 240
    public static final int SCREEN_Y = CONSOLE_Y + 36; // 56
    public static final int SCREEN_W = 480;
    public static final int SCREEN_H = 360;

    // Bộ đệm hình ảnh màn hình Mario riêng biệt
    private final BufferedImage screenBuffer = new BufferedImage(SCREEN_W, SCREEN_H, BufferedImage.TYPE_INT_RGB);

    private long lastTime = 0;
    private long victoryTimer = 0;
    private boolean restartRequested = false;

    public MarioMinigame() {
        this.world = new MarioWorld();
        this.player = new MarioPlayer(2.5 * MarioWorld.TILE, 12 * MarioWorld.TILE);
        this.lastTime = System.currentTimeMillis();
    }

    public void start() {
        world.resetWorld();
        player.reset(2.5 * MarioWorld.TILE, 12 * MarioWorld.TILE);
        lastTime = System.currentTimeMillis();
        victoryTimer = 0;
        restartRequested = false;
    }

    public void update(long now) {
        if (lastTime == 0) lastTime = now;
        double dt = (now - lastTime) / 1000.0;
        if (dt > 0.05) dt = 0.05; // Giới hạn delta time tránh giật bước
        lastTime = now;

        player.update(dt, world);
        world.update(dt, player);

        // Tự động hồi sinh sau khi chết 2.6 giây hoặc sau khi về đích đã đi vào lâu đài 4.0 giây
        if (player.isDead && (now - player.deadTimer > 2600)) {
            start();
        } else if (player.enteredCastle) {
            if (victoryTimer == 0) {
                victoryTimer = now;
            } else if (now - victoryTimer > 4000) {
                start();
            }
        }
    }

    public void keyPressed(int key) {
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
            player.keyLeft = true;
        } else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            player.keyRight = true;
        } else if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W || key == KeyEvent.VK_SPACE || key == KeyEvent.VK_K) {
            player.keyJump = true;
        } else if (key == KeyEvent.VK_SHIFT || key == KeyEvent.VK_J) {
            player.keyRun = true;
        } else if (key == KeyEvent.VK_R) {
            start();
        }
    }

    public void keyReleased(int key) {
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
            player.keyLeft = false;
        } else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            player.keyRight = false;
        } else if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W || key == KeyEvent.VK_SPACE || key == KeyEvent.VK_K) {
            player.keyJump = false;
        } else if (key == KeyEvent.VK_SHIFT || key == KeyEvent.VK_J) {
            player.keyRun = false;
        }
    }

    // ==========================================
    // VẼ TOÀN BỘ KHUNG MÁY CHƠI GAME VÀ NỀN ĐEN
    // ==========================================
    public void render(Graphics2D g) {
        // 1. Xung quanh máy chơi game là nền đen hoàn toàn
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 960, 640);

        // 2. Vẽ màn hình game Mario vào buffer riêng
        Graphics2D sg = screenBuffer.createGraphics();
        sg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        world.render(sg, player);
        sg.dispose();

        // 3. Vẽ vỏ máy chơi game cổ điển (Handheld Console Body)
        drawHandheldConsole(g);

        // 4. Vẽ màn hình game Mario lọt vào giữa viền màn hình máy
        g.drawImage(screenBuffer, SCREEN_X, SCREEN_Y, SCREEN_W, SCREEN_H, null);

        // 5. Hiệu ứng chữ hướng dẫn điều khiển thanh lịch ở rìa
        drawOverlayHints(g);
    }

    private void drawHandheldConsole(Graphics2D g) {
        // Bóng đổ của máy
        g.setColor(new Color(0, 0, 0, 160));
        g.fill(new RoundRectangle2D.Double(CONSOLE_X + 10, CONSOLE_Y + 12, CONSOLE_W, CONSOLE_H, 46, 46));

        // Thân máy: Màu xám retro đặc trưng của Game Boy cổ điển (#D0CAC0)
        Color bodyColor = new Color(0xCE, 0xC8, 0xBC);
        g.setColor(bodyColor);
        g.fill(new RoundRectangle2D.Double(CONSOLE_X, CONSOLE_Y, CONSOLE_W, CONSOLE_H, 44, 44));

        // Đường gờ vát cạnh thân máy tạo khối nổi 3D
        g.setColor(new Color(255, 255, 255, 110));
        g.draw(new RoundRectangle2D.Double(CONSOLE_X + 1, CONSOLE_Y + 1, CONSOLE_W - 2, CONSOLE_H - 2, 44, 44));
        g.setColor(new Color(0, 0, 0, 60));
        g.draw(new RoundRectangle2D.Double(CONSOLE_X + 3, CONSOLE_Y + 3, CONSOLE_W - 6, CONSOLE_H - 6, 42, 42));

        // ------------------------------------------
        // KHUNG KÍNH MÀN HÌNH (Screen Bezel)
        // ------------------------------------------
        int bezelX = CONSOLE_X + 26;
        int bezelY = CONSOLE_Y + 18;
        int bezelW = CONSOLE_W - 52;
        int bezelH = SCREEN_H + 36;

        g.setColor(new Color(0x3E, 0x43, 0x4E)); // Khung xám đen
        g.fill(new RoundRectangle2D.Double(bezelX, bezelY, bezelW, bezelH, 24, 24));

        // Viền bóng của khung kính
        g.setColor(new Color(0x28, 0x2C, 0x35));
        g.draw(new RoundRectangle2D.Double(bezelX, bezelY, bezelW, bezelH, 24, 24));

        // Đèn LED nguồn pin (Battery LED)
        g.setColor(new Color(0x22, 0x22, 0x22));
        g.fillOval(bezelX + 22, bezelY + 12, 10, 10);
        g.setColor(new Color(0xFF, 0x30, 0x30)); // Đèn đỏ sáng rực
        g.fillOval(bezelX + 23, bezelY + 13, 8, 8);
        g.setColor(new Color(0xFF, 0xAA, 0xAA));
        g.fillOval(bezelX + 25, bezelY + 14, 3, 3);

        // Chữ "BATTERY" nhỏ xíu bên dưới đèn LED
        g.setFont(new Font("Monospaced", Font.BOLD, 9));
        g.setColor(new Color(0x9E, 0xA4, 0xB2));
        g.drawString("BATTERY", bezelX + 12, bezelY + 34);

        // Dòng chữ thương hiệu retro trên mép màn hình
        g.setFont(new Font("Dialog", Font.BOLD, 12));
        g.setColor(new Color(0xB8, 0xBE, 0xD0));
        String brand = "SANBAKA 8-BIT ENTERTAINMENT SYSTEM";
        g.drawString(brand, bezelX + (bezelW - g.getFontMetrics().stringWidth(brand)) / 2, bezelY + 15);

        // Viền khoét màn hình LCD
        g.setColor(new Color(0x18, 0x1A, 0x20));
        g.fillRect(SCREEN_X - 2, SCREEN_Y - 2, SCREEN_W + 4, SCREEN_H + 4);

        // ------------------------------------------
        // PHÍM ĐIỀU HƯỚNG CHỮ THẬP (D-PAD DẤU +)
        // ------------------------------------------
        int dpadCenterX = CONSOLE_X + 110;
        int dpadCenterY = CONSOLE_Y + 490;
        drawDpad(g, dpadCenterX, dpadCenterY);

        // ------------------------------------------
        // 2 NÚT CHỨC NĂNG TRÒN (B và A)
        // ------------------------------------------
        int btnBx = CONSOLE_X + 400;
        int btnBy = CONSOLE_Y + 505;
        int btnAx = CONSOLE_X + 468;
        int btnAy = CONSOLE_Y + 468;
        drawActionButton(g, btnBx, btnBy, "B", player.keyRun);
        drawActionButton(g, btnAx, btnAy, "A", player.keyJump);

        // ------------------------------------------
        // PHÍM SELECT VÀ START
        // ------------------------------------------
        int pillY = CONSOLE_Y + 555;
        drawPillButton(g, CONSOLE_X + 230, pillY, "SELECT");
        drawPillButton(g, CONSOLE_X + 295, pillY, "START");

        // ------------------------------------------
        // KHE LOA RETRO (Speaker Grille Slits)
        // ------------------------------------------
        int speakerX = CONSOLE_X + 460;
        int speakerY = CONSOLE_Y + 540;
        g.setColor(new Color(0x8C, 0x86, 0x7B));
        for (int i = 0; i < 6; i++) {
            g.fillRoundRect(speakerX + i * 11, speakerY + i * 4, 6, 32, 4, 4);
        }
    }

    private void drawDpad(Graphics2D g, int cx, int cy) {
        int armL = 36;
        int armW = 26;
        int halfW = armW / 2;

        // Vùng lõm tròn xung quanh D-pad
        g.setColor(new Color(0xBA, 0xB4, 0xA8));
        g.fillOval(cx - armL - 6, cy - armL - 6, (armL + 6) * 2, (armL + 6) * 2);

        // Thân chữ thập màu than sẫm
        Color dpadColor = new Color(0x24, 0x28, 0x2E);
        g.setColor(dpadColor);
        // Trục dọc
        g.fillRoundRect(cx - halfW, cy - armL, armW, armL * 2, 6, 6);
        // Trục ngang
        g.fillRoundRect(cx - armL, cy - halfW, armL * 2, armW, 6, 6);

        // Điểm lõm tròn ở chính tâm D-pad
        g.setColor(new Color(0x18, 0x1A, 0x1E));
        g.fillOval(cx - 7, cy - 7, 14, 14);

        // Tam giác nổi hướng mũi tên
        g.setColor(new Color(0x45, 0x4B, 0x56));
        // Lên
        if (player.keyJump) g.setColor(Color.WHITE); else g.setColor(new Color(0x55, 0x5D, 0x6A));
        drawArrowTriangle(g, cx, cy - 23, 0);
        // Xuống
        g.setColor(new Color(0x55, 0x5D, 0x6A));
        drawArrowTriangle(g, cx, cy + 23, 2);
        // Trái
        if (player.keyLeft) g.setColor(Color.WHITE); else g.setColor(new Color(0x55, 0x5D, 0x6A));
        drawArrowTriangle(g, cx - 23, cy, 3);
        // Phải
        if (player.keyRight) g.setColor(Color.WHITE); else g.setColor(new Color(0x55, 0x5D, 0x6A));
        drawArrowTriangle(g, cx + 23, cy, 1);
    }

    private void drawArrowTriangle(Graphics2D g, int x, int y, int dir) {
        int size = 5;
        int[] xp = new int[3];
        int[] yp = new int[3];
        switch (dir) {
            case 0 -> { // Lên
                xp = new int[]{x, x - size, x + size};
                yp = new int[]{y - size, y + size, y + size};
            }
            case 1 -> { // Phải
                xp = new int[]{x + size, x - size, x - size};
                yp = new int[]{y, y - size, y + size};
            }
            case 2 -> { // Xuống
                xp = new int[]{x, x - size, x + size};
                yp = new int[]{y + size, y - size, y - size};
            }
            case 3 -> { // Trái
                xp = new int[]{x - size, x + size, x + size};
                yp = new int[]{y, y - size, y + size};
            }
        }
        g.fillPolygon(xp, yp, 3);
    }

    private void drawActionButton(Graphics2D g, int x, int y, String label, boolean pressed) {
        int r = 20;
        // Đế vát nghiêng
        g.setColor(new Color(0xBA, 0xB4, 0xA8));
        g.fillOval(x - r - 4, y - r - 4, (r + 4) * 2, (r + 4) * 2);

        // Nút tròn màu đỏ mận Famicom/GameBoy (#9E1B32)
        int offsetY = pressed ? 3 : 0;
        g.setColor(new Color(0x58, 0x0A, 0x16));
        g.fillOval(x - r, y - r + 3, r * 2, r * 2);

        Color btnColor = pressed ? new Color(0x78, 0x12, 0x22) : new Color(0xA8, 0x1E, 0x36);
        g.setColor(btnColor);
        g.fillOval(x - r, y - r + offsetY, r * 2, r * 2);

        // Ánh sáng nổi 3D
        g.setColor(new Color(255, 255, 255, 80));
        g.fillOval(x - r + 5, y - r + 4 + offsetY, r * 2 - 10, r - 3);

        // Chữ B hoặc A bên dưới nút
        g.setFont(new Font("Dialog", Font.BOLD, 15));
        g.setColor(new Color(0x35, 0x3A, 0x48));
        g.drawString(label, x - 5, y + r + 18);
    }

    private void drawPillButton(Graphics2D g, int x, int y, String label) {
        // Nút cao su Select / Start vát chéo 25 độ
        g.setColor(new Color(0x38, 0x3C, 0x44));
        g.fillRoundRect(x, y, 42, 13, 10, 10);
        g.setFont(new Font("Monospaced", Font.BOLD, 11));
        g.setColor(new Color(0x35, 0x3A, 0x48));
        g.drawString(label, x - 2, y + 25);
    }

    private void drawOverlayHints(Graphics2D g) {
        g.setFont(new Font("Dialog", Font.BOLD, 13));
        g.setColor(new Color(200, 205, 215));
        String hint = "[ESC] Quay lại Menu  ·  [A / D / Mũi tên] Di chuyển  ·  [Space / W] Nhảy  ·  [Shift] Chạy  ·  [R] Chơi lại";
        int tw = g.getFontMetrics().stringWidth(hint);
        g.drawString(hint, (960 - tw) / 2, 630);

        if (player.isCompleted) {
            String winMsg = "★ CHÚC MỪNG BẠN ĐÃ VỀ ĐÍCH CỘT CỜ WORLD 1-1! (Nhấn R để chơi lại) ★";
            g.setFont(new Font("Dialog", Font.BOLD, 14));
            g.setColor(new Color(0xFF, 0xD7, 0x00));
            int ww = g.getFontMetrics().stringWidth(winMsg);
            g.drawString(winMsg, (960 - ww) / 2, SCREEN_Y + 45);
        }
    }
}

