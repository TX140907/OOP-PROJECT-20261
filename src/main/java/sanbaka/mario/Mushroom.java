package sanbaka.mario;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Vật phẩm Nấm đỏ thần kỳ (Super Mushroom) trồi lên từ khối ?:
 * - Trồi từ từ lên khỏi khối gạch trong 0.4 giây
 * - Sau khi trồi lên xong, tự động trượt sang phải, gặp ống cống/tường sẽ tự đổi chiều
 * - Khi Mario chạm vào: Mario hóa to (Super Mario), nhận thêm 1 mạng va chạm
 */
public class Mushroom {
    public static final double WIDTH = 22.0;
    public static final double HEIGHT = 22.0;
    public static final double SPEED = 65.0;

    public double x;
    public double y;
    public double vx = SPEED;
    public double vy = 0;
    public final double startY;

    public boolean emerged = false;
    public double emergeProgress = 0.0;
    public boolean collected = false;

    public Mushroom(double x, double y) {
        this.x = x;
        this.startY = y;
        this.y = y;
    }

    public void update(double dt, MarioWorld world, MarioPlayer player) {
        if (collected) return;

        // 1. Giai đoạn trồi lên từ trong khối gạch
        if (!emerged) {
            emergeProgress += dt * 2.4;
            if (emergeProgress >= 1.0) {
                emerged = true;
                y = startY - MarioWorld.TILE;
                vy = 0;
            } else {
                y = startY - emergeProgress * MarioWorld.TILE;
            }
            return;
        }

        // 2. Di chuyển ngang và đổi chiều khi chạm vật cản
        x += vx * dt;
        resolveCollisionX(world);

        // 3. Rơi theo trọng lực
        vy += MarioPlayer.GRAVITY * 0.9 * dt;
        if (vy > MarioPlayer.MAX_FALL_SPEED) vy = MarioPlayer.MAX_FALL_SPEED;
        y += vy * dt;
        resolveCollisionY(world);

        // Rơi xuống vực
        if (y > world.rows * MarioWorld.TILE + 40) {
            collected = true;
            return;
        }

        // 4. Kiểm tra va chạm với người chơi để ăn nấm hóa to
        if (!player.isDead && !player.isFlagSliding) {
            double px = player.x;
            double py = player.y;
            double pw = player.getWidth();
            double ph = player.getHeight();

            boolean overlapX = (px + pw > x) && (px < x + WIDTH);
            boolean overlapY = (py + ph > y) && (py < y + HEIGHT);

            if (overlapX && overlapY) {
                collected = true;
                player.growBig();
            }
        }
    }

    private void resolveCollisionX(MarioWorld world) {
        int leftTile = (int) Math.floor(x / MarioWorld.TILE);
        int rightTile = (int) Math.floor((x + WIDTH) / MarioWorld.TILE);
        int topTile = (int) Math.floor(y / MarioWorld.TILE);
        int bottomTile = (int) Math.floor((y + HEIGHT - 1) / MarioWorld.TILE);

        if (vx > 0) {
            for (int r = topTile; r <= bottomTile; r++) {
                if (world.isSolid(rightTile, r)) {
                    x = rightTile * MarioWorld.TILE - WIDTH;
                    vx = -SPEED;
                    break;
                }
            }
        } else if (vx < 0) {
            for (int r = topTile; r <= bottomTile; r++) {
                if (world.isSolid(leftTile, r)) {
                    x = (leftTile + 1) * MarioWorld.TILE;
                    vx = SPEED;
                    break;
                }
            }
        }
    }

    private void resolveCollisionY(MarioWorld world) {
        int leftTile = (int) Math.floor((x + 2) / MarioWorld.TILE);
        int rightTile = (int) Math.floor((x + WIDTH - 2) / MarioWorld.TILE);
        int bottomTile = (int) Math.floor((y + HEIGHT) / MarioWorld.TILE);

        if (vy >= 0) {
            for (int c = leftTile; c <= rightTile; c++) {
                if (world.isSolid(c, bottomTile)) {
                    y = bottomTile * MarioWorld.TILE - HEIGHT;
                    vy = 0;
                    break;
                }
            }
        }
    }

    public void draw(Graphics2D g, double renderX, double renderY) {
        if (collected) return;
        BufferedImage sprite = MarioAssets.get("item_mushroom");
        if (sprite != null) {
            int dx = (int) Math.round(renderX);
            int dy = (int) Math.round(renderY);
            g.drawImage(sprite, dx, dy, (int) WIDTH, (int) HEIGHT, null);
        }
    }
}

