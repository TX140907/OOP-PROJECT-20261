package sanbaka.model;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.List;
import sanbaka.assets.ArtAssets;
import sanbaka.render.GameVisuals;

/**
 * Sinh viên băng qua đường (Màn 1: Trời đẹp):
 * - Xuất phát từ vỉa hè (trái hoặc phải), rảo bước ngang qua các làn đường.
 * - Cơ chế né xe thông minh (không bao giờ đi xuyên qua xe):
 *   + Quét hành lang lắc lư cực đại của các xe cộ đang lao tới (tính cả wobbleAmp).
 *   + Nếu có xe chắn trước mặt: dừng chân nhường đường (Yield), giơ tay xin đường;
 *     nếu xe quá sát thì lùi nhẹ lại giữ khoảng cách an toàn.
 *   + Nếu đã vượt quá tim xe: rảo bước nhanh để thoát hẳn mũi xe.
 * - Va chạm với xe người chơi: cả người chơi và sinh viên đều tiếp tục đi tiếp
 *   (người chơi dính sát thương & hiệu ứng va chạm như thường; không hiện chữ nổi).
 * - Hình dạng vector mô phỏng người đi bộ (balo, áo, đầu, chân bước), tự động nạp
 *   ảnh thay thế nếu có file `assets/objects/Student/Student.png`.
 */
public class Student extends Thing {
    public static final int PHASE_SIDEWALK_WAIT = 0;
    public static final int PHASE_CROSSING = 1;

    public static final long SIDEWALK_WAIT_MS = 650;
    public static final double WALK_SPEED = 88.0;

    private static final BasicStroke STROKE_ARM = new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke STROKE_RESET_1F = new BasicStroke(1f);
    private static final BasicStroke STROKE_WARN_DASH = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{6f, 5f}, 0f);

    private static final Color COLOR_SHADOW = new Color(0, 0, 0, 50);
    private static final Color COLOR_PANTS = new Color(38, 48, 68);
    private static final Color COLOR_SHOES = new Color(30, 30, 34);
    private static final Color COLOR_SHIRT = new Color(36, 106, 186); // Xanh Bách Khoa
    private static final Color COLOR_COLLAR = new Color(245, 245, 250);
    private static final Color COLOR_BACKPACK = new Color(60, 68, 80);
    private static final Color COLOR_STRAP = new Color(110, 120, 135);
    private static final Color COLOR_SKIN = new Color(252, 218, 188);
    private static final Color COLOR_HAIR = new Color(32, 28, 28);

    public int phase = PHASE_SIDEWALK_WAIT;
    public long phaseStartedAt = System.currentTimeMillis();
    public int dir = 1; // 1: đi từ trái sang phải, -1: đi từ phải sang trái
    public boolean done = false;

    public boolean isYielding = false;
    public boolean isSprinting = false;

    public long hitInvulnUntil = 0;
    public long lastStumbleUntil = 0;

    public Student() {
        this.kind = 18;
        this.w = 32.0;
        this.h = 38.0;
        this.fwd = 0.0;
    }

    public boolean isHitInvulnerable(long now) {
        return now < hitInvulnUntil;
    }

    public void onPlayerHit(long now) {
        this.hit = true;
        this.hitInvulnUntil = now + 900;
        this.lastStumbleUntil = now + 420;
    }

    public void updateStudent(double dt, double scroll, long now, int roadX, int roadW, List<Thing> things,
                              double playerX, double playerY, double playerW, double playerH) {
        // Trôi nhẹ tự nhiên theo cuộn cảnh mặt đường
        this.y += scroll * 0.45 * dt;

        if (phase == PHASE_SIDEWALK_WAIT) {
            if (now - phaseStartedAt >= SIDEWALK_WAIT_MS) {
                phase = PHASE_CROSSING;
                phaseStartedAt = now;
            }
            return;
        }

        // Giai đoạn băng qua đường (PHASE_CROSSING)
        // 1. Quét tìm các xe cộ đường phố cản trở (CHỈ né các xe đường phố, KHÔNG né xe người chơi)
        isYielding = false;
        isSprinting = false;

        double currentSpeed = dir * WALK_SPEED;

        Thing blocker = null;
        double minDangerDist = 9999.0;
        double lookaheadDist = 48.0;

        for (Thing o : things) {
            if (o == this || o instanceof Student || o instanceof Shark || o.snack || o.kind == 11 || o.kind == 14) {
                continue;
            }

            // Khoảng cách theo phương dọc Y giữa sinh viên và xe cộ
            double ySpan = (o.h + this.h) / 2.0 + 20.0;
            boolean inDangerY = Math.abs(o.y - this.y) < ySpan;

            if (!inDangerY) continue;

            // Xác định hành lang quét ngang X của xe (bao gồm cả biên độ lắc lư cực đại nếu có)
            double carCenter = (o.wobbleAmp > 0) ? o.baseX : o.x;
            double carSweep = (o.wobbleAmp > 0) ? o.wobbleAmp : 0.0;
            double dangerMinX = carCenter - o.w / 2.0 - carSweep - 18.0;
            double dangerMaxX = carCenter + o.w / 2.0 + carSweep + 18.0;

            if (dir > 0) {
                // Đi từ trái sang phải
                double frontX = this.x + this.w / 2.0;
                boolean inFront = (frontX <= dangerMinX + 12.0) && (dangerMinX <= frontX + lookaheadDist);
                boolean insideZone = (this.x >= dangerMinX - 10.0) && (this.x <= dangerMaxX + 10.0);

                if (inFront || insideZone) {
                    double dist = dangerMinX - frontX;
                    if (dist < minDangerDist) {
                        minDangerDist = dist;
                        blocker = o;

                        if (inFront) {
                            // Xe đang lao tới phía trước mặt: dừng chân nhường đường
                            isYielding = true;
                            currentSpeed = 0.0;
                            // Nếu đã lỡ tiến quá sát mép nguy hiểm: lùi lại nửa bước an toàn
                            if (frontX > dangerMinX - 8.0) {
                                currentSpeed = -24.0;
                            }
                        } else {
                            // Đang nằm trong vùng ảnh hưởng của xe
                            if (this.x > carCenter + 4.0) {
                                // Đã vượt qua nửa thân xe: rảo bước nhanh thoát qua mũi xe
                                isSprinting = true;
                                currentSpeed = WALK_SPEED * 1.45;
                            } else {
                                // Chưa qua tim xe: lùi lại hoặc đứng lại chờ xe qua
                                isYielding = true;
                                currentSpeed = -22.0;
                            }
                        }
                    }
                }
            } else {
                // Đi từ phải sang trái (dir < 0)
                double frontX = this.x - this.w / 2.0;
                boolean inFront = (frontX >= dangerMaxX - 12.0) && (dangerMaxX >= frontX - lookaheadDist);
                boolean insideZone = (this.x <= dangerMaxX + 10.0) && (this.x >= dangerMinX - 10.0);

                if (inFront || insideZone) {
                    double dist = frontX - dangerMaxX;
                    if (dist < minDangerDist) {
                        minDangerDist = dist;
                        blocker = o;

                        if (inFront) {
                            isYielding = true;
                            currentSpeed = 0.0;
                            if (frontX < dangerMaxX + 8.0) {
                                currentSpeed = 24.0;
                            }
                        } else {
                            if (this.x < carCenter - 4.0) {
                                isSprinting = true;
                                currentSpeed = -WALK_SPEED * 1.45;
                            } else {
                                isYielding = true;
                                currentSpeed = 22.0;
                            }
                        }
                    }
                }
            }
        }

        // Cập nhật vị trí X
        this.x += currentSpeed * dt;

        // Kiểm tra xem đã qua đường thành công tới vỉa hè đối diện chưa
        if (dir > 0 && this.x > roadX + roadW + 18.0) {
            this.done = true;
        } else if (dir < 0 && this.x < roadX - 18.0) {
            this.done = true;
        }
    }

    @Override
    public void draw(Graphics2D g, GameVisuals scene) {
        long now = System.currentTimeMillis();

        // 1. Nếu có ảnh vẽ riêng từ Artist, ưu tiên nạp và vẽ ảnh
        BufferedImage art = ArtAssets.load("Student", "Student.png");
        if (art == null) art = ArtAssets.load("Student", "student.png");
        if (art != null) {
            int dw = (int) this.w;
            int dh = (int) this.h;
            AffineTransform old = g.getTransform();
            g.translate(this.x, this.y);
            if (this.dir < 0) {
                g.scale(-1, 1);
            }
            g.drawImage(art, -dw / 2, -dh / 2, dw, dh, null);
            g.setTransform(old);
            return;
        }

        // 2. Tạm vẽ hình người vector sinh viên đi bộ (hỗ trợ chèn ảnh vào sau)
        AffineTransform old = g.getTransform();
        Stroke oldStroke = g.getStroke();

        g.translate(this.x, this.y);
        if (this.dir < 0) {
            g.scale(-1, 1);
        }

        // Chỉ báo hướng sang đường khi đang đứng chờ ở vỉa hè
        if (phase == PHASE_SIDEWALK_WAIT) {
            drawSidewalkIndicator(g, now);
        }

        // Chu kỳ bước đi theo hàm sin
        double walkCycle = (isYielding) ? 0 : Math.sin(now / 115.0);
        double legSwing = (isYielding) ? 0 : 7.0 * walkCycle;

        // Bóng đổ mờ dưới chân
        g.setColor(COLOR_SHADOW);
        g.fill(new Ellipse2D.Double(-12, 13, 24, 10));

        // Hai chân bước đi (quần tối màu)
        g.setColor(COLOR_PANTS);
        g.fill(new RoundRectangle2D.Double(-7 + legSwing, 4, 6, 13, 3, 3));
        g.fill(new RoundRectangle2D.Double(1 - legSwing, 4, 6, 13, 3, 3));

        // Giày
        g.setColor(COLOR_SHOES);
        g.fill(new RoundRectangle2D.Double(-7 + legSwing, 15, 7, 4, 2, 2));
        g.fill(new RoundRectangle2D.Double(1 - legSwing, 15, 7, 4, 2, 2));

        // Thân người (Áo phông/sơ mi xanh Bách Khoa)
        g.setColor(COLOR_SHIRT);
        g.fill(new RoundRectangle2D.Double(-9, -8, 18, 15, 6, 6));

        // Cổ áo trắng
        g.setColor(COLOR_COLLAR);
        int[] vx = {-3, 0, 3};
        int[] vy = {-8, -5, -8};
        g.fillPolygon(vx, vy, 3);

        // Balo sinh viên đeo sau lưng (phía ngược với hướng nhìn/tiến)
        g.setColor(COLOR_BACKPACK);
        g.fill(new RoundRectangle2D.Double(-14, -7, 7, 13, 4, 4));
        g.setColor(COLOR_STRAP);
        g.draw(new Line2D.Double(-11, -5, -11, 4));

        // Đầu & Khuôn mặt
        g.setColor(COLOR_SKIN);
        g.fill(new Ellipse2D.Double(-6, -20, 12, 12));

        // Tóc đen sinh viên
        g.setColor(COLOR_HAIR);
        g.fill(new Arc2D.Double(-6.5, -21, 13, 11, 0, 180, Arc2D.CHORD));
        g.fill(new RoundRectangle2D.Double(-7, -18, 4, 6, 2, 2));

        // Cánh tay & Bàn tay
        if (isYielding) {
            // Đứng nhường xe: một tay giơ lên xin đường
            g.setColor(COLOR_SHIRT);
            g.setStroke(STROKE_ARM);
            g.draw(new Line2D.Double(4, -3, 10, -11));
            g.setColor(COLOR_SKIN);
            g.fill(new Ellipse2D.Double(9, -14, 5, 5));

            // Tay sau thả tự nhiên
            g.setColor(COLOR_SHIRT);
            g.draw(new Line2D.Double(-5, -2, -8, 6));
        } else {
            // Bước đi bình thường: tay đánh nhịp nhàng ngược pha với chân
            g.setColor(COLOR_SHIRT);
            g.setStroke(STROKE_ARM);
            g.draw(new Line2D.Double(4, -3, 5 - legSwing * 0.7, 5));
            g.draw(new Line2D.Double(-5, -3, -6 + legSwing * 0.7, 5));

            g.setColor(COLOR_SKIN);
            g.fill(new Ellipse2D.Double(4 - legSwing * 0.7, 4, 4, 4));
            g.fill(new Ellipse2D.Double(-7 + legSwing * 0.7, 4, 4, 4));
        }

        g.setStroke(oldStroke);
        g.setTransform(old);
    }

    private void drawSidewalkIndicator(Graphics2D g, long now) {
        boolean blink = (now / 150) % 2 == 0;
        g.setColor(blink ? new Color(255, 230, 90, 210) : new Color(255, 180, 40, 130));
        g.setStroke(STROKE_WARN_DASH);
        g.draw(new Line2D.Double(14, 0, 85, 0));
        g.setStroke(STROKE_RESET_1F);
    }
}

