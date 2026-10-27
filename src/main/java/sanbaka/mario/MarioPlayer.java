package sanbaka.mario;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Xử lý cơ chế vật lý (Physics), chuyển động và hoạt ảnh nguyên bản của Mario 8-bit:
 * - Hóa to khi ăn Nấm thần kỳ (Super Mario: nhận thêm 1 mạng va chạm, va chạm sẽ teo nhỏ lại)
 * - Gia tốc chạy & quán tính
 * - Phanh trượt (Skid) khi đổi chiều đột ngột
 * - Nhảy biến thiên (Variable Jump Height)
 * - Tự động đi từ từ vào sâu bên trong lâu đài và biến mất khi hoàn thành cột cờ
 */
public class MarioPlayer {
    public static final double TILE = 24.0;
    public static final double SMALL_WIDTH = 20.0;
    public static final double SMALL_HEIGHT = 24.0;
    public static final double BIG_WIDTH = 26.0;
    public static final double BIG_HEIGHT = 36.0;

    // Hằng số vật lý chuẩn NES Super Mario Bros 1985
    public static final double WALK_ACCEL = 480.0;       // px/s^2
    public static final double RUN_ACCEL = 720.0;        // px/s^2
    public static final double MAX_WALK_SPEED = 170.0;   // px/s
    public static final double MAX_RUN_SPEED = 260.0;    // px/s
    public static final double FRICTION = 420.0;         // ma sát khi nhả phím
    public static final double SKID_DECEL = 980.0;       // phanh trượt khi đổi hướng
    public static final double GRAVITY = 1350.0;         // trọng lực thông thường
    public static final double LOW_GRAVITY = 660.0;      // trọng lực khi giữ chặt phím nhảy
    public static final double JUMP_VELOCITY = -450.0;   // lực nhảy ban đầu
    public static final double STOMP_BOUNCE = -320.0;    // lực nảy lên khi dẫm Goomba
    public static final double MAX_FALL_SPEED = 520.0;

    public double x;
    public double y;
    public double prevY;
    public double vx = 0;
    public double vy = 0;
    public boolean wasFalling = false;

    public boolean isBig = false;
    public long invulnerableUntil = 0;

    public boolean onGround = false;
    public boolean facingRight = true;
    public boolean skidding = false;
    public boolean isDead = false;
    public boolean isCompleted = false;
    public boolean enteredCastle = false;
    public boolean isFlagSliding = false;
    public double flagCastleX = 0;

    public long deadTimer = 0;
    private double animTime = 0;
    private int walkFrame = 0;

    // Trạng thái phím điều khiển
    public boolean keyLeft = false;
    public boolean keyRight = false;
    public boolean keyJump = false;
    public boolean keyRun = false;

    public MarioPlayer(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.prevY = startY;
    }

    public double getWidth() {
        return isBig ? BIG_WIDTH : SMALL_WIDTH;
    }

    public double getHeight() {
        return isBig ? BIG_HEIGHT : SMALL_HEIGHT;
    }

    public void reset(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.prevY = startY;
        this.vx = 0;
        this.vy = 0;
        this.wasFalling = false;
        this.isBig = false;
        this.invulnerableUntil = 0;
        this.onGround = false;
        this.facingRight = true;
        this.skidding = false;
        this.isDead = false;
        this.isCompleted = false;
        this.enteredCastle = false;
        this.isFlagSliding = false;
        this.deadTimer = 0;
        this.animTime = 0;
        this.walkFrame = 0;
    }

    public void growBig() {
        if (!isBig) {
            y -= (BIG_HEIGHT - SMALL_HEIGHT); // Nhấc lên để chân tiếp đất đúng chỗ
            x -= (BIG_WIDTH - SMALL_WIDTH) / 2.0; // Nở rộng đều sang 2 bên
            isBig = true;
        }
    }

    public void hitByEnemy() {
        if (isDead || isFlagSliding || isCompleted) return;
        if (System.currentTimeMillis() < invulnerableUntil) return;

        if (isBig) {
            // Có nấm bảo vệ: không chết mà teo nhỏ và nhận 2 giây nhấp nháy bất tử
            isBig = false;
            y += (BIG_HEIGHT - SMALL_HEIGHT);
            x += (BIG_WIDTH - SMALL_WIDTH) / 2.0;
            invulnerableUntil = System.currentTimeMillis() + 2000;
        } else {
            kill();
        }
    }

    public void kill() {
        if (isDead || isCompleted || isFlagSliding) return;
        isDead = true;
        isBig = false;
        vx = 0;
        vy = -380.0;
        deadTimer = System.currentTimeMillis();
    }

    public void bounce() {
        vy = STOMP_BOUNCE;
        onGround = false;
    }

    public void startFlagSlide(double poleX, double groundY, double castleDoorX) {
        if (isFlagSliding || isCompleted) return;
        isFlagSliding = true;
        x = poleX - getWidth() + 6;
        vx = 0;
        vy = 120.0;
        this.flagCastleX = castleDoorX;
    }

    public void update(double dt, MarioWorld world) {
        long now = System.currentTimeMillis();
        wasFalling = vy > 0 || (y > prevY);
        prevY = y;

        // 1. Trạng thái chết (Die Sequence)
        if (isDead) {
            if (now - deadTimer > 280) { // Đứng hình một tích tắc trước khi văng lên
                vy += GRAVITY * 0.9 * dt;
                if (vy > MAX_FALL_SPEED) vy = MAX_FALL_SPEED;
                y += vy * dt;
            }
            return;
        }

        // 2. Trạng thái bám cờ trượt xuống (Flagpole Sequence)
        if (isFlagSliding) {
            y += vy * dt;
            double groundLimit = (world.rows - 2) * TILE - getHeight();
            if (y >= groundLimit) {
                y = groundLimit;
                vy = 0;
                facingRight = true;
                x += 16.0; // Nhảy sang bên phải cột cờ
                isFlagSliding = false;
                isCompleted = true;
            }
            return;
        }

        // 3. Trạng thái hoàn thành màn (Đi từ từ vào sâu trong lâu đài tới khi biến mất)
        if (isCompleted) {
            if (!enteredCastle) {
                vx = 65.0; // Đi thong thả vào lâu đài
                x += vx * dt;
                animTime += dt * 6.5;
                walkFrame = ((int) animTime) % 3;

                // Khi đã bước lọt hẳn vào cửa lâu đài: biến mất hoàn toàn
                if (x >= flagCastleX + 14.0) {
                    enteredCastle = true;
                    vx = 0;
                }
            }
            return;
        }

        // 4. Vật lý di chuyển ngang (Horizontal Movement)
        double maxSpeed = keyRun ? MAX_RUN_SPEED : MAX_WALK_SPEED;
        double accel = keyRun ? RUN_ACCEL : WALK_ACCEL;

        if (keyLeft && !keyRight) {
            if (vx > 15) {
                // Phanh trượt khi đang chạy sang phải mà bấm sang trái
                skidding = true;
                vx -= SKID_DECEL * dt;
                if (vx < 0) vx = 0;
            } else {
                skidding = false;
                facingRight = false;
                vx -= accel * dt;
                if (vx < -maxSpeed) vx = -maxSpeed;
            }
        } else if (keyRight && !keyLeft) {
            if (vx < -15) {
                // Phanh trượt khi đang chạy sang trái mà bấm sang phải
                skidding = true;
                vx += SKID_DECEL * dt;
                if (vx > 0) vx = 0;
            } else {
                skidding = false;
                facingRight = true;
                vx += accel * dt;
                if (vx > maxSpeed) vx = maxSpeed;
            }
        } else {
            skidding = false;
            // Ma sát giảm tốc tự nhiên khi không bấm phím
            if (vx > 0) {
                vx -= FRICTION * dt;
                if (vx < 0) vx = 0;
            } else if (vx < 0) {
                vx += FRICTION * dt;
                if (vx > 0) vx = 0;
            }
        }

        // Cập nhật vị trí X và kiểm tra va chạm tường/ống cống
        x += vx * dt;
        world.resolveCollisionX(this);

        // Không cho phép đi lùi ra khỏi mép trái màn hình
        double minScreenX = world.cameraX;
        if (x < minScreenX) {
            x = minScreenX;
            if (vx < 0) vx = 0;
        }

        // 5. Vật lý nhảy và trọng lực (Vertical Movement & Variable Jump)
        if (keyJump && onGround) {
            vy = JUMP_VELOCITY;
            onGround = false;
        }

        double currentGravity = (keyJump && vy < 0) ? LOW_GRAVITY : GRAVITY;
        vy += currentGravity * dt;
        if (vy > MAX_FALL_SPEED) vy = MAX_FALL_SPEED;

        // Cập nhật vị trí Y và kiểm tra va chạm sàn/trần gạch
        y += vy * dt;
        onGround = false;
        world.resolveCollisionY(this);

        // Kiểm tra rơi xuống hố vực
        if (y > world.rows * TILE + 40) {
            kill();
        }

        // 6. Hoạt ảnh bước chạy (Walking animation cycle)
        if (onGround && Math.abs(vx) > 8 && !skidding) {
            double speedFactor = Math.abs(vx) / MAX_WALK_SPEED;
            animTime += dt * 10.0 * Math.max(0.6, speedFactor);
            walkFrame = ((int) animTime) % 3;
        } else {
            animTime = 0;
            walkFrame = 0;
        }
    }

    public void draw(Graphics2D g, double renderX, double renderY) {
        // Nếu đã bước hẳn vào trong lâu đài thì không còn hiển thị nữa
        if (enteredCastle) return;

        // Hiệu ứng nhấp nháy bất tử khi vừa va chạm teo nhỏ
        if (System.currentTimeMillis() < invulnerableUntil) {
            if ((System.currentTimeMillis() / 70) % 2 == 0) return;
        }

        String spriteKey;
        if (isDead) {
            spriteKey = "mario_die";
        } else if (isBig) {
            if (isFlagSliding) {
                spriteKey = "mario_big_flag";
            } else if (!onGround) {
                spriteKey = "mario_big_jump";
            } else if (skidding) {
                spriteKey = "mario_big_skid";
            } else if (Math.abs(vx) > 10) {
                spriteKey = switch (walkFrame) {
                    case 1 -> "mario_big_walk_2";
                    case 2 -> "mario_big_walk_3";
                    default -> "mario_big_walk_1";
                };
            } else {
                spriteKey = "mario_big_idle";
            }
        } else {
            if (isFlagSliding) {
                spriteKey = "mario_flag";
            } else if (!onGround) {
                spriteKey = "mario_jump";
            } else if (skidding) {
                spriteKey = "mario_skid";
            } else if (Math.abs(vx) > 10) {
                spriteKey = switch (walkFrame) {
                    case 1 -> "mario_walk_2";
                    case 2 -> "mario_walk_3";
                    default -> "mario_walk_1";
                };
            } else {
                spriteKey = "mario_idle";
            }
        }

        BufferedImage sprite = MarioAssets.get(spriteKey);
        if (sprite == null) sprite = MarioAssets.get(isBig ? "mario_big_idle" : "mario_idle");

        int drawW = (int) Math.round(getWidth());
        int drawH = (int) Math.round(getHeight());
        int dx = (int) Math.round(renderX);
        int dy = (int) Math.round(renderY);

        if (facingRight || isDead || isFlagSliding) {
            g.drawImage(sprite, dx, dy, drawW, drawH, null);
        } else {
            // Lật ảnh khi quay sang trái
            g.drawImage(sprite, dx + drawW, dy, -drawW, drawH, null);
        }
    }
}
