package sanbaka.mario;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Thế giới màn chơi Mario 8-bit tái hiện chính xác World 1-1:
 * - Kích thước gạch: 24x24 px (15 hàng gạch cao, vừa vặn khung 480x360 của máy cầm tay)
 * - Địa hình gạch, khối ?, ống cống 2-3-4 tầng, hố vực, bậc thang đá
 * - Nấm Goomba tuần tra
 * - Cột cờ (Flagpole) và lâu đài về đích
 * - Cơ chế húc gạch từ dưới lên nảy tưng (Bump Animation) và đồng xu bật lên
 */
public class MarioWorld {
    public static final double TILE = 24.0;

    // Các loại ô gạch
    public static final int EMPTY = 0;
    public static final int GROUND = 1;
    public static final int BRICK = 2;
    public static final int QUESTION = 3;
    public static final int QUESTION_USED = 4;
    public static final int PIPE_TL = 5;
    public static final int PIPE_TR = 6;
    public static final int PIPE_BL = 7;
    public static final int PIPE_BR = 8;
    public static final int STEP = 9;
    public static final int POLE = 10;
    public static final int CASTLE = 11;

    public final int cols = 212;
    public final int rows = 15;
    public final int[][] map = new int[cols][rows];

    // Quản lý khối gạch đang nảy lên (Bump Animation)
    private static class BumpingBlock {
        int c, r;
        double offsetY = 0;
        double timer = 0;
        boolean hasCoin = false;
        double coinY = 0;
    }
    private final List<BumpingBlock> activeBumps = new ArrayList<>();

    // Danh sách kẻ địch Goomba
    public final List<Goomba> goombas = new ArrayList<>();

    // Danh sách Nấm đỏ thần kỳ (Super Mushroom)
    public final List<Mushroom> mushrooms = new ArrayList<>();

    // Tọa độ cột cờ và lá cờ
    public final int flagCol = 198;
    public final int flagRowTop = 2;
    public final int flagRowBottom = 11;
    public double flagY;
    public boolean flagSlidingDown = false;
    public final double castleDoorX = 205 * TILE;

    // Camera cuộn ngang
    public double cameraX = 0;
    public final double viewWidth = 480.0;
    public final double viewHeight = 360.0;

    // Hoạt ảnh khối hỏi chấm nhấp nháy
    private double questionAnimTimer = 0;
    private int questionFrame = 0;

    public MarioWorld() {
        buildWorld1_1();
        resetWorld();
    }

    public void resetWorld() {
        activeBumps.clear();
        goombas.clear();
        mushrooms.clear();
        cameraX = 0;
        flagY = flagRowTop * TILE;
        flagSlidingDown = false;

        // Sinh danh sách Goomba ở các vị trí kinh điển của World 1-1
        int[] goombaCols = {22, 40, 51, 53, 80, 82, 97, 99, 114, 116, 124, 126, 128, 174, 176};
        for (int c : goombaCols) {
            goombas.add(new Goomba(c * TILE, 12 * TILE));
        }

        // Đặt lại các khối ? đã dùng về trạng thái ban đầu
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                if (map[c][r] == QUESTION_USED) {
                    map[c][r] = QUESTION;
                }
            }
        }
    }

    private void buildWorld1_1() {
        // 1. Mặt đất (Ground: 2 hàng dưới cùng r=13 và r=14)
        for (int c = 0; c < cols; c++) {
            // Các hố vực kinh điển: cột 69-70, 86-88, 153-154
            boolean isPit = (c >= 69 && c <= 70) || (c >= 86 && c <= 88) || (c >= 153 && c <= 154);
            if (!isPit) {
                map[c][13] = GROUND;
                map[c][14] = GROUND;
            }
        }

        // 2. Đoạn mở đầu (Cột 16 đến 24)
        map[16][9] = QUESTION; // Khối ? đầu tiên

        map[20][9] = BRICK;
        map[21][9] = QUESTION;
        map[22][9] = BRICK;
        map[22][5] = QUESTION; // Khối ? trên cao
        map[23][9] = QUESTION;
        map[24][9] = BRICK;

        // 3. Các ống cống xanh (Pipes)
        addPipe(28, 2); // Ống cao 2 khối
        addPipe(38, 3); // Ống cao 3 khối
        addPipe(46, 4); // Ống cao 4 khối
        addPipe(57, 4); // Ống cao 4 khối

        // 4. Đoạn giữa (Sau hố vực thứ nhất)
        map[77][9] = BRICK;
        map[78][9] = QUESTION;
        map[79][9] = BRICK;

        // Dãy gạch dài 2 tầng ở cột 80 đến 85
        for (int c = 80; c <= 85; c++) {
            map[c][5] = BRICK;
            map[c][9] = BRICK;
        }
        map[82][9] = QUESTION;

        // 5. Sau hố vực thứ hai
        addPipe(91, 3);
        map[94][9] = QUESTION;
        map[100][9] = BRICK;
        map[101][9] = QUESTION;
        map[102][9] = BRICK;

        // Dãy gạch bậc cao
        for (int c = 106; c <= 109; c++) map[c][5] = BRICK;
        for (int c = 118; c <= 122; c++) map[c][9] = BRICK;
        map[120][5] = QUESTION;

        // 6. Bậc thang kim tự tháp (Stairs) trước hố vực thứ 3
        buildStaircase(134, 4, true);   // Lên 4 bậc
        buildStaircase(140, 4, false);  // Xuống 4 bậc

        // 7. Bậc thang sau hố vực
        addPipe(163, 2);
        for (int c = 168; c <= 170; c++) map[c][9] = BRICK;
        map[169][9] = QUESTION;

        // 8. Bậc thang lớn dẫn tới Cột cờ (Final Giant Staircase: 8 bậc)
        buildStaircase(181, 8, true);
        for (int r = 13 - 8; r < 13; r++) map[189][r] = STEP; // Bậc đỉnh 189

        // 9. Cột cờ (Flagpole) ở cột 198
        map[flagCol][12] = STEP; // Đế cờ
        for (int r = flagRowTop; r <= flagRowBottom; r++) {
            map[flagCol][r] = POLE;
        }

        // 10. Lâu đài đích đến: được vẽ trực tiếp qua drawCastle() chuẩn nguyên tác NES 1985 (cột 203 đến 207)
    }

    private void addPipe(int startCol, int heightInTiles) {
        int topRow = 13 - heightInTiles;
        map[startCol][topRow] = PIPE_TL;
        map[startCol + 1][topRow] = PIPE_TR;
        for (int r = topRow + 1; r < 13; r++) {
            map[startCol][r] = PIPE_BL;
            map[startCol + 1][r] = PIPE_BR;
        }
    }

    private void buildStaircase(int startCol, int height, boolean ascending) {
        for (int i = 0; i < height; i++) {
            int c = ascending ? (startCol + i) : (startCol + i);
            int stepHeight = ascending ? (i + 1) : (height - i);
            for (int h = 0; h < stepHeight; h++) {
                map[c][13 - 1 - h] = STEP;
            }
        }
    }

    public boolean isSolid(int c, int r) {
        if (c < 0 || c >= cols || r < 0 || r >= rows) return false;
        int t = map[c][r];
        return t == GROUND || t == BRICK || t == QUESTION || t == QUESTION_USED
                || t == PIPE_TL || t == PIPE_TR || t == PIPE_BL || t == PIPE_BR || t == STEP;
    }

    public void update(double dt, MarioPlayer player) {
        questionAnimTimer += dt * 5.0;
        questionFrame = ((int) questionAnimTimer) % 3;

        // 1. Cập nhật các khối gạch đang nảy lên (Bump Animation)
        for (int i = activeBumps.size() - 1; i >= 0; i--) {
            BumpingBlock b = activeBumps.get(i);
            b.timer += dt * 14.0;
            if (b.timer >= Math.PI) {
                activeBumps.remove(i);
            } else {
                b.offsetY = -Math.sin(b.timer) * 9.0;
                b.coinY = -Math.sin(b.timer) * 28.0;
            }
        }

        // 2. Cập nhật Goombas
        for (int i = goombas.size() - 1; i >= 0; i--) {
            Goomba g = goombas.get(i);
            g.update(dt, this, player);
            if (g.isDead) {
                goombas.remove(i);
            }
        }

        // 3. Cập nhật Nấm đỏ thần kỳ (Super Mushroom)
        for (int i = mushrooms.size() - 1; i >= 0; i--) {
            Mushroom m = mushrooms.get(i);
            m.update(dt, this, player);
            if (m.collected) {
                mushrooms.remove(i);
            }
        }

        // 4. Cập nhật va chạm cột cờ nếu người chơi chạm vào
        checkFlagpoleCollision(player, dt);

        // 5. Cờ luôn tự động tụt xuống đáy cột kể cả khi Mario đã buông cờ và đi vào lâu đài
        if (flagSlidingDown) {
            double flagBottomLimit = (flagRowBottom - 1) * TILE;
            if (flagY < flagBottomLimit) {
                flagY += 135.0 * dt;
                if (flagY > flagBottomLimit) {
                    flagY = flagBottomLimit;
                }
            }
        }

        // 6. Camera cuộn mượt theo nhân vật (chỉ tiến không lùi)
        double targetCamX = player.x - 140.0;
        if (targetCamX > cameraX) {
            cameraX = targetCamX;
        }
        double maxCamX = cols * TILE - viewWidth;
        if (cameraX > maxCamX) cameraX = maxCamX;
        if (cameraX < 0) cameraX = 0;
    }

    private void checkFlagpoleCollision(MarioPlayer player, double dt) {
        if (player.isFlagSliding || player.isCompleted || player.isDead) {
            return;
        }

        double poleLeft = flagCol * TILE + 6;
        double poleRight = poleLeft + 8;
        double poleTop = flagRowTop * TILE;
        double poleBottom = (flagRowBottom + 1) * TILE;

        double px = player.x + 2;
        double py = player.y;
        double pw = player.getWidth() - 4;
        double ph = player.getHeight();

        if (px + pw >= poleLeft && px <= poleRight && py + ph >= poleTop && py <= poleBottom) {
            flagSlidingDown = true;
            player.startFlagSlide(poleLeft, 13 * TILE, castleDoorX);
        }
    }

    public void bumpBlock(int c, int r) {
        if (c < 0 || c >= cols || r < 0 || r >= rows) return;
        int t = map[c][r];
        if (t != BRICK && t != QUESTION) return;

        // Tránh húc trùng lặp khi đang nảy
        for (BumpingBlock b : activeBumps) {
            if (b.c == c && b.r == r) return;
        }

        BumpingBlock b = new BumpingBlock();
        b.c = c;
        b.r = r;
        if (t == QUESTION) {
            map[c][r] = QUESTION_USED;
            if (isMushroomBlock(c, r)) {
                b.hasCoin = false;
                mushrooms.add(new Mushroom(c * TILE, r * TILE));
            } else {
                b.hasCoin = true;
            }
        }
        activeBumps.add(b);
    }

    private boolean isMushroomBlock(int c, int r) {
        // Các khối ? chứa Nấm đỏ kinh điển của World 1-1
        return (c == 21 && r == 9) || (c == 22 && r == 5) || (c == 78 && r == 9)
                || (c == 109 && r == 5) || (c == 120 && r == 5) || (c == 169 && r == 9);
    }

    // ==========================================
    // XỬ LÝ VA CHẠM VẬT LÝ AABB CHUẨN XÁC
    // ==========================================
    public void resolveCollisionX(MarioPlayer player) {
        int leftTile = (int) Math.floor(player.x / TILE);
        int rightTile = (int) Math.floor((player.x + player.getWidth()) / TILE);
        int topTile = (int) Math.floor(player.y / TILE);
        int bottomTile = (int) Math.floor((player.y + player.getHeight() - 1) / TILE);

        if (player.vx > 0) {
            for (int r = topTile; r <= bottomTile; r++) {
                if (isSolid(rightTile, r)) {
                    player.x = rightTile * TILE - player.getWidth();
                    player.vx = 0;
                    break;
                }
            }
        } else if (player.vx < 0) {
            for (int r = topTile; r <= bottomTile; r++) {
                if (isSolid(leftTile, r)) {
                    player.x = (leftTile + 1) * TILE;
                    player.vx = 0;
                    break;
                }
            }
        }
    }

    public void resolveCollisionY(MarioPlayer player) {
        int leftTile = (int) Math.floor((player.x + 2) / TILE);
        int rightTile = (int) Math.floor((player.x + player.getWidth() - 2) / TILE);
        int topTile = (int) Math.floor(player.y / TILE);
        int bottomTile = (int) Math.floor((player.y + player.getHeight()) / TILE);

        if (player.vy > 0) { // Rơi xuống đất
            for (int c = leftTile; c <= rightTile; c++) {
                if (isSolid(c, bottomTile)) {
                    player.y = bottomTile * TILE - player.getHeight();
                    player.vy = 0;
                    player.onGround = true;
                    break;
                }
            }
        } else if (player.vy < 0) { // Húc đầu lên trần
            for (int c = leftTile; c <= rightTile; c++) {
                if (isSolid(c, topTile)) {
                    player.y = (topTile + 1) * TILE;
                    player.vy = 0;
                    bumpBlock(c, topTile); // Kích hoạt hiệu ứng nảy gạch và nhặt xu / nấm
                    break;
                }
            }
        }
    }

    public void resolveGoombaCollisionX(Goomba g) {
        int leftTile = (int) Math.floor(g.x / TILE);
        int rightTile = (int) Math.floor((g.x + Goomba.WIDTH) / TILE);
        int topTile = (int) Math.floor(g.y / TILE);
        int bottomTile = (int) Math.floor((g.y + Goomba.HEIGHT - 1) / TILE);

        if (g.vx > 0) {
            for (int r = topTile; r <= bottomTile; r++) {
                if (isSolid(rightTile, r)) {
                    g.x = rightTile * TILE - Goomba.WIDTH;
                    g.vx = -Goomba.SPEED;
                    break;
                }
            }
        } else if (g.vx < 0) {
            for (int r = topTile; r <= bottomTile; r++) {
                if (isSolid(leftTile, r)) {
                    g.x = (leftTile + 1) * TILE;
                    g.vx = Goomba.SPEED;
                    break;
                }
            }
        }
    }

    public void resolveGoombaCollisionY(Goomba g) {
        int leftTile = (int) Math.floor((g.x + 2) / TILE);
        int rightTile = (int) Math.floor((g.x + Goomba.WIDTH - 2) / TILE);
        int bottomTile = (int) Math.floor((g.y + Goomba.HEIGHT) / TILE);

        if (g.vy >= 0) {
            for (int c = leftTile; c <= rightTile; c++) {
                if (isSolid(c, bottomTile)) {
                    g.y = bottomTile * TILE - Goomba.HEIGHT;
                    g.vy = 0;
                    g.onGround = true;
                    break;
                }
            }
        }
    }

    // ==========================================
    // VẼ THẾ GIỚI 8-BIT WORLD 1-1 (RENDER)
    // ==========================================
    public void render(Graphics2D g, MarioPlayer player) {
        // 1. Nền trời xanh chuẩn NES
        g.setColor(MarioAssets.NES_SKY);
        g.fillRect(0, 0, (int) viewWidth, (int) viewHeight);

        // 2. Mây trắng NES lặp lại phía xa (đã loại bỏ hoàn toàn các khối oval xanh lá cây thừa)
        drawScenery(g);

        // 3. Địa hình & Khối gạch
        int startCol = Math.max(0, (int) Math.floor(cameraX / TILE));
        int endCol = Math.min(cols - 1, (int) Math.ceil((cameraX + viewWidth) / TILE));

        for (int c = startCol; c <= endCol; c++) {
            double rx = c * TILE - cameraX;
            for (int r = 0; r < rows; r++) {
                int t = map[c][r];
                if (t == EMPTY) continue;

                // Tính toán độ nảy nếu khối này đang bị húc đầu từ dưới lên
                double bumpY = 0;
                for (BumpingBlock b : activeBumps) {
                    if (b.c == c && b.r == r) {
                        bumpY = b.offsetY;
                        break;
                    }
                }

                double ry = r * TILE + bumpY;
                drawTile(g, t, rx, ry);
            }
        }

        // 4. Vẽ lâu đài đích đến chuẩn nguyên tác NES Super Mario Bros 1985
        drawCastle(g);

        // 5. Vẽ đồng xu nảy lên khi húc trúng khối ?
        for (BumpingBlock b : activeBumps) {
            if (b.hasCoin) {
                double cx = b.c * TILE - cameraX + 4;
                double cy = (b.r - 1) * TILE + b.coinY;
                BufferedImage coin = MarioAssets.get("item_coin");
                if (coin != null) {
                    g.drawImage(coin, (int) Math.round(cx), (int) Math.round(cy), (int) TILE - 8, (int) TILE - 8, null);
                }
            }
        }

        // 6. Vẽ Nấm đỏ thần kỳ (Super Mushroom)
        for (Mushroom m : mushrooms) {
            double mx = m.x - cameraX;
            double my = m.y;
            if (mx >= -TILE && mx <= viewWidth + TILE) {
                m.draw(g, mx, my);
            }
        }

        // 7. Cột cờ và Lá cờ trượt
        drawFlag(g);

        // 8. Vẽ Goombas
        for (Goomba goomba : goombas) {
            double gx = goomba.x - cameraX;
            double gy = goomba.y;
            if (gx >= -TILE && gx <= viewWidth + TILE) {
                goomba.draw(g, gx, gy);
            }
        }

        // 9. Vẽ Người chơi Mario
        double px = player.x - cameraX;
        double py = player.y;
        player.draw(g, px, py);
    }

    private void drawTile(Graphics2D g, int type, double rx, double ry) {
        String key = switch (type) {
            case GROUND -> "tile_ground";
            case BRICK -> "tile_brick";
            case QUESTION -> "tile_question_" + (questionFrame + 1);
            case QUESTION_USED -> "tile_question_used";
            case PIPE_TL -> "tile_pipe_top_l";
            case PIPE_TR -> "tile_pipe_top_r";
            case PIPE_BL -> "tile_pipe_body_l";
            case PIPE_BR -> "tile_pipe_body_r";
            case STEP -> "tile_question_used"; // Khối bậc thang phong cách đá nâu/xám
            case POLE -> null;
            case CASTLE -> null;
            default -> null;
        };

        if (key != null) {
            BufferedImage sprite = MarioAssets.get(key);
            if (sprite != null) {
                g.drawImage(sprite, (int) Math.round(rx), (int) Math.round(ry), (int) TILE, (int) TILE, null);
            }
        }
    }

    private void drawFlag(Graphics2D g) {
        double poleScreenX = flagCol * TILE - cameraX + 8;
        if (poleScreenX < -40 || poleScreenX > viewWidth + 40) return;

        // Trụ cờ màu xanh lá
        g.setColor(new Color(0x00, 0xA8, 0x00));
        g.fillRect((int) Math.round(poleScreenX + 2), (int) Math.round(flagRowTop * TILE), 4, (int) ((flagRowBottom - flagRowTop + 1) * TILE));
        // Đỉnh cờ quả cầu vàng
        g.setColor(new Color(0xFC, 0xD8, 0x00));
        g.fillOval((int) Math.round(poleScreenX), (int) Math.round(flagRowTop * TILE - 8), 8, 8);

        // Lá cờ trắng trượt từ từ xuống đáy cột
        BufferedImage flag = MarioAssets.get("tile_flag");
        double flagScreenY = flagY;
        if (flag != null) {
            g.drawImage(flag, (int) Math.round(poleScreenX - 16), (int) Math.round(flagScreenY), 18, 18, null);
        }
    }

    // ==========================================
    // VẼ LÂU ĐÀI ĐÍCH ĐẾN NGUYÊN BẢN NES (WORLD 1-1 CASTLE)
    // ==========================================
    private void drawCastle(Graphics2D g) {
        double startX = 203 * TILE - cameraX;
        if (startX < -180 || startX > viewWidth + 180) return;

        // 1. Tường thấp cánh trái (cột 203, hàng 10-12)
        drawCastleBattlement(g, 203 * TILE - cameraX, 10 * TILE);
        drawCastleBlock(g, 203 * TILE - cameraX, 11 * TILE);
        drawCastleBlock(g, 203 * TILE - cameraX, 12 * TILE);

        // 2. Tháp giữa cánh trái (cột 204, hàng 8-12)
        drawCastleBattlement(g, 204 * TILE - cameraX, 8 * TILE);
        drawCastleBlock(g, 204 * TILE - cameraX, 9 * TILE);
        drawCastleBlock(g, 204 * TILE - cameraX, 10 * TILE);
        drawCastleBlock(g, 204 * TILE - cameraX, 11 * TILE);
        drawCastleBlock(g, 204 * TILE - cameraX, 12 * TILE);

        // 3. Tháp chính trung tâm (cột 205, hàng 8-12)
        // Cột cờ nhỏ và cờ đỏ tam giác trên đỉnh nóc lâu đài
        double flagPoleX = 205 * TILE - cameraX + 11;
        double flagBaseY = 8 * TILE;
        g.setColor(Color.BLACK);
        g.fillRect((int) Math.round(flagPoleX), (int) Math.round(flagBaseY - 14), 2, 14);
        g.setColor(new Color(0xB8, 0x38, 0x00));
        int[] fX = {(int) Math.round(flagPoleX + 2), (int) Math.round(flagPoleX + 12), (int) Math.round(flagPoleX + 2)};
        int[] fY = {(int) Math.round(flagBaseY - 14), (int) Math.round(flagBaseY - 9), (int) Math.round(flagBaseY - 4)};
        g.fillPolygon(fX, fY, 3);

        drawCastleBlock(g, 205 * TILE - cameraX, 8 * TILE);
        drawCastleBlock(g, 205 * TILE - cameraX, 9 * TILE);
        drawCastleBlock(g, 205 * TILE - cameraX, 10 * TILE);
        drawCastleBlock(g, 205 * TILE - cameraX, 11 * TILE);
        drawCastleBlock(g, 205 * TILE - cameraX, 12 * TILE);

        // Khe cửa sổ nhỏ đen trên tầng 9
        g.setColor(Color.BLACK);
        g.fillRoundRect((int) Math.round(205 * TILE - cameraX + 9), (int) Math.round(9 * TILE + 6), 6, 12, 3, 3);

        // 4. Tháp giữa cánh phải (cột 206, hàng 8-12)
        drawCastleBattlement(g, 206 * TILE - cameraX, 8 * TILE);
        drawCastleBlock(g, 206 * TILE - cameraX, 9 * TILE);
        drawCastleBlock(g, 206 * TILE - cameraX, 10 * TILE);
        drawCastleBlock(g, 206 * TILE - cameraX, 11 * TILE);
        drawCastleBlock(g, 206 * TILE - cameraX, 12 * TILE);

        // 5. Tường thấp cánh phải (cột 207, hàng 10-12)
        drawCastleBattlement(g, 207 * TILE - cameraX, 10 * TILE);
        drawCastleBlock(g, 207 * TILE - cameraX, 11 * TILE);
        drawCastleBlock(g, 207 * TILE - cameraX, 12 * TILE);

        // 6. Cửa vòm đen huyền thoại (Doorway) ở cột 205, hàng 11-12
        int doorX = (int) Math.round(205 * TILE - cameraX + 3);
        int doorY = (int) Math.round(11 * TILE + 4);
        int doorW = (int) Math.round(TILE - 6);
        int doorH = (int) Math.round(TILE * 2 - 4);
        g.setColor(Color.BLACK);
        g.fillRoundRect(doorX, doorY, doorW, doorH, 12, 12);
    }

    private void drawCastleBlock(Graphics2D g, double x, double y) {
        int ix = (int) Math.round(x);
        int iy = (int) Math.round(y);
        int s = (int) TILE;

        // Màu gạch cam nâu chuẩn NES
        g.setColor(new Color(0xD8, 0x80, 0x38));
        g.fillRect(ix, iy, s, s);

        // Viền bóng đen
        g.setColor(Color.BLACK);
        g.drawRect(ix, iy, s - 1, s - 1);

        // Các đường vân đá chuẩn phong cách NES
        g.drawLine(ix, iy + s / 2, ix + s - 1, iy + s / 2);
        g.drawLine(ix + s / 2, iy, ix + s / 2, iy + s / 2);
        g.drawLine(ix + s / 4, iy + s / 2, ix + s / 4, iy + s - 1);
        g.drawLine(ix + 3 * s / 4, iy + s / 2, ix + 3 * s / 4, iy + s - 1);

        // Đường highlight sáng trên mép từng viên đá
        g.setColor(new Color(0xFC, 0xE0, 0xA8));
        g.drawLine(ix + 1, iy + 1, ix + s / 2 - 2, iy + 1);
        g.drawLine(ix + s / 2 + 1, iy + 1, ix + s - 2, iy + 1);
        g.drawLine(ix + 1, iy + s / 2 + 1, ix + s / 4 - 2, iy + s / 2 + 1);
        g.drawLine(ix + s / 4 + 1, iy + s / 2 + 1, ix + 3 * s / 4 - 2, iy + s / 2 + 1);
    }

    private void drawCastleBattlement(Graphics2D g, double x, double y) {
        int ix = (int) Math.round(x);
        int iy = (int) Math.round(y);
        int s = (int) TILE;

        int toothW = s / 3;
        int toothH = s / 2;

        g.setColor(new Color(0xD8, 0x80, 0x38));
        g.fillRect(ix, iy + toothH, s, toothH);
        g.fillRect(ix, iy, toothW, toothH);
        g.fillRect(ix + s - toothW, iy, toothW, toothH);

        g.setColor(Color.BLACK);
        g.drawRect(ix, iy + toothH, s - 1, toothH - 1);
        g.drawRect(ix, iy, toothW, toothH);
        g.drawRect(ix + s - toothW, iy, toothW, toothH);

        g.setColor(new Color(0xFC, 0xE0, 0xA8));
        g.drawLine(ix + 1, iy + 1, ix + toothW - 1, iy + 1);
        g.drawLine(ix + s - toothW + 1, iy + 1, ix + s - 2, iy + 1);
    }

    private void drawScenery(Graphics2D g) {
        // Mây trắng NES lặp lại phía xa với hiệu ứng thị sai Parallax nhẹ
        int[] cloudCols = {8, 19, 36, 56, 74, 90, 110, 130, 150, 170, 190};
        for (int c : cloudCols) {
            double cx = c * TILE - cameraX * 0.7; // Hiệu ứng thị sai Parallax nhẹ
            if (cx >= -80 && cx <= viewWidth + 80) {
                g.setColor(Color.WHITE);
                g.fillRoundRect((int) Math.round(cx), (int) (3 * TILE), 48, 22, 16, 16);
                g.fillRoundRect((int) Math.round(cx + 10), (int) (2.5 * TILE), 28, 24, 16, 16);
            }
        }
    }
}

