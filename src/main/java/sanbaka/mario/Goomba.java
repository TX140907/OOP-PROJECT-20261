package sanbaka.mario;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Kẻ địch Nấm Goomba 8-bit nguyên bản:
 * - Đi tuần tra qua lại, tự đổi chiều khi va phải ống cống hoặc chướng ngại vật
 * - Cơ chế dẫm trúng đầu 100% nhạy bén và chuẩn xác (kể cả khi rơi tốc độ cao)
 * - Nếu người chơi ở dạng Super Mario (ăn nấm hóa to), va chạm ngang sẽ làm người chơi teo nhỏ chứ không chết ngay
 */
public class Goomba {
    public static final double WIDTH = 22.0;
    public static final double HEIGHT = 22.0;
    public static final double SPEED = 48.0; // px/s

    public double x;
    public double y;
    public double vx = -SPEED;
    public double vy = 0.0;
    public boolean onGround = false;

    public boolean isDead = false;
    public boolean isStomped = false;
    public long stompedTimer = 0;
    private double animTime = 0.0;

    public Goomba(double startX, double startY) {
        this.x = startX;
        this.y = startY;
    }

    public void stomp() {
        if (isStomped || isDead) return;
        isStomped = true;
        vx = 0;
        vy = 0;
        stompedTimer = System.currentTimeMillis();
    }

    public void update(double dt, MarioWorld world, MarioPlayer player) {
        long now = System.currentTimeMillis();

        if (isStomped) {
            if (now - stompedTimer > 420) {
                isDead = true;
            }
            return;
        }

        if (isDead) return;

        // Chỉ kích hoạt hoạt động khi camera gần tới Goomba (trong bán kính nhìn thấy)
        if (x > world.cameraX + world.viewWidth + 120 || x < world.cameraX - 120) {
            return;
        }

        // Di chuyển ngang và đổi chiều khi chạm vật cản
        x += vx * dt;
        world.resolveGoombaCollisionX(this);

        // Trọng lực rơi
        vy += MarioPlayer.GRAVITY * dt;
        if (vy > MarioPlayer.MAX_FALL_SPEED) vy = MarioPlayer.MAX_FALL_SPEED;
        y += vy * dt;
        onGround = false;
        world.resolveGoombaCollisionY(this);

        if (y > world.rows * MarioWorld.TILE + 40) {
            isDead = true;
            return;
        }

        // Hoạt ảnh 2 frame bước chân
        animTime += dt * 6.0;

        // Kiểm tra va chạm với người chơi (nếu người chơi chưa chết)
        if (!player.isDead && !player.isFlagSliding && !player.isCompleted) {
            checkPlayerCollision(player);
        }
    }

    private void checkPlayerCollision(MarioPlayer player) {
        double px = player.x + 2;
        double py = player.y;
        double pw = player.getWidth() - 4;
        double ph = player.getHeight();
        double playerBottom = py + ph;
        double prevBottom = player.prevY + ph;

        double gx = this.x;
        double gy = this.y;
        double gw = WIDTH;
        double gh = HEIGHT;

        // 1. Kiểm tra chồng lấn ngang
        boolean overlapX = (px + pw > gx + 3) && (px < gx + gw - 3);

        // 2. Kiểm tra chồng lấn dọc (mở rộng lên trên để nhạy bén đón Mario rơi xuống)
        boolean overlapY = (playerBottom >= gy - 6) && (py <= gy + gh);

        if (overlapX && overlapY) {
            // Cơ chế đạp đầu nhạy bén và chuẩn xác 100%:
            // - Mario đang rơi hoặc vừa rơi từ trên xuống
            // - Hoặc chân Mario ở nửa trên của Goomba
            // - Hoặc frame trước Mario ở trên Goomba
            boolean isFalling = player.vy > 0 || player.wasFalling || (player.y > player.prevY);
            boolean isAboveCenter = playerBottom <= (gy + gh * 0.75);
            boolean cameFromAbove = prevBottom <= (gy + 16.0);

            boolean isStomp = (isFalling && (isAboveCenter || cameFromAbove))
                           || cameFromAbove
                           || (isAboveCenter && player.vy >= 0);

            if (isStomp) {
                stomp();
                player.y = gy - player.getHeight();
                player.onGround = false;
                if (player.keyJump) {
                    player.vy = -450.0; // Giữ phím nhảy để nảy cao
                } else {
                    player.bounce();    // Nảy bình thường
                }
            } else {
                player.hitByEnemy();
            }
        }
    }

    public void draw(Graphics2D g, double renderX, double renderY) {
        if (isDead) return;

        BufferedImage sprite;
        if (isStomped) {
            sprite = MarioAssets.get("goomba_dead");
        } else {
            sprite = (((int) animTime) % 2 == 0)
                    ? MarioAssets.get("goomba_1")
                    : MarioAssets.get("goomba_2");
        }

        if (sprite != null) {
            int dx = (int) Math.round(renderX);
            int dy = (int) Math.round(renderY);
            int dw = (int) Math.round(WIDTH);
            int dh = (int) Math.round(HEIGHT);
            g.drawImage(sprite, dx, dy, dw, dh, null);
        }
    }
}
