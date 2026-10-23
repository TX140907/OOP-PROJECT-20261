package sanbaka.model;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import sanbaka.assets.ArtAssets;
import sanbaka.render.GameVisuals;

/**
 * Chó chạy rông băng ngang đường (Màn 4 nắng nóng):
 * 1. PHASE_BARK_WARN: Đứng ở mép vỉa hè sủa báo hiệu ("GÂU!" + dấu !) trước khi lao ra.
 * 2. PHASE_DASH_ACROSS: Chạy cắt ngang qua các làn đường sang vỉa hè đối diện.
 *    - Biết quan sát né tránh xe cộ theo phương tiến/lùi (Y axis) vào chỗ trống không có xe.
 */
public class StrayDog extends Thing {
    public static final int PHASE_BARK_WARN = 0;
    public static final int PHASE_DASH_ACROSS = 1;

    public static final long WARN_MS = 880; // Thời gian đứng ở vỉa hè sủa báo hiệu trước khi phóng qua đường
    public static final double DASH_SPEED = 343.75; // 275.0 * 1.25

    private static final Font WARN_FONT = new Font("Dialog", Font.BOLD, 12);
    private static final BasicStroke WARN_DASH_STROKE = new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{8f, 6f}, 0f);
    private static final BasicStroke WARN_BOX_STROKE = new BasicStroke(1.5f);
    private static final BasicStroke STROKE_RESET_1F = new BasicStroke(1f);

    public int phase = PHASE_BARK_WARN;
    public long phaseStartedAt = System.currentTimeMillis();
    public int dir = 1; // 1: chạy từ trái sang phải, -1: chạy từ phải sang trái
    public boolean done = false;

    // Trạng thái né xe theo phương tiến/lùi (trục Y)
    public double dodgeVy = 0.0;
    public double targetDodgeY = -1;
    public double tiltAngle = 0.0;

    public StrayDog() {
        this.w = 48;
        this.h = 30;
        this.fwd = 0.72;
    }

    public void updateDog(double dt, double scroll, long now, int roadX, int roadW, java.util.List<Thing> things) {
        updateDog(dt, scroll, now, roadX, roadW, things, -9999, -9999, 0, 0);
    }

    public void updateDog(double dt, double scroll, long now, int roadX, int roadW, java.util.List<Thing> things,
                          double playerX, double playerY, double playerW, double playerH) {
        if (phase == PHASE_BARK_WARN) {
            this.y += scroll * 0.25 * dt;
            if (now - phaseStartedAt >= WARN_MS) {
                phase = PHASE_DASH_ACROSS;
                phaseStartedAt = now;
            }
        } else if (phase == PHASE_DASH_ACROSS) {
            // 1. Quét tìm xe cộ đường phố cản trở trực diện hướng chạy (CHỈ né các xe đường phố, KHÔNG né xe người chơi)
            Thing blocker = null;
            double minObsDistX = 9999.0;
            double lookahead = 96.0;

            for (Thing o : things) {
                if (o == this || o instanceof StrayDog || o instanceof Shark || o.snack || o.kind == 14) continue;
                // Kiểm tra xem vật thể có nằm phía trước mũi chạy theo trục X không
                double frontX = this.x + dir * (this.w / 2.0);
                double leftObs = o.x - o.w / 2.0;
                double rightObs = o.x + o.w / 2.0;
                boolean inPathX = (dir > 0)
                        ? (rightObs >= this.x - 12 && leftObs <= frontX + lookahead)
                        : (leftObs <= this.x + 12 && rightObs >= frontX - lookahead);

                if (inPathX) {
                    double ySpan = (o.h + this.h) / 2.0 + 16.0;
                    if (Math.abs(o.y - this.y) < ySpan) {
                        double distX = dir * (o.x - this.x);
                        if (distX < minObsDistX) {
                            minObsDistX = distX;
                            blocker = o;
                        }
                    }
                }
            }

            double speed = DASH_SPEED;

            if (blocker != null) {
                // 2. Có xe chắn đường: Tìm khoảng trống không có xe theo phương tiến (-Y / lên trên) hoặc lùi (+Y / xuống dưới)
                double candUp = blocker.y - (blocker.h / 2.0 + this.h / 2.0 + 26.0);
                double candDown = blocker.y + (blocker.h / 2.0 + this.h / 2.0 + 26.0);

                double atX = blocker.x;
                boolean upClear = isCandidateClear(candUp, atX, things, blocker);
                boolean downClear = isCandidateClear(candDown, atX, things, blocker);

                if (upClear && !downClear) {
                    targetDodgeY = candUp;
                } else if (downClear && !upClear) {
                    targetDodgeY = candDown;
                } else if (upClear && downClear) {
                    // Cả hai hướng đều thoáng: chọn hướng gần vị trí hiện tại của chó hơn
                    if (this.y <= blocker.y) {
                        targetDodgeY = candUp;
                    } else {
                        targetDodgeY = candDown;
                    }
                } else {
                    // Cả 2 phía đều có vật cản: chọn phía xa mép màn hình hơn để không bị ép góc
                    double distTop = Math.abs(candUp - 110);
                    double distBot = Math.abs(550 - candDown);
                    targetDodgeY = (distTop > distBot) ? candUp : candDown;
                }

                // Điều khiển né theo phương Y (lượn tiến hoặc lùi)
                double diffY = targetDodgeY - this.y;
                double maxVy = 312.5; // 250.0 * 1.25
                double desiredVy = Math.max(-maxVy, Math.min(maxVy, diffY * 10.0));
                dodgeVy += (desiredVy - dodgeVy) * Math.min(1.0, dt * 17.5);
                this.y += dodgeVy * dt;
                this.y = Math.max(90, Math.min(560, this.y));

                // Điều khiển tốc độ X:
                // Nếu vẫn đang nằm trong vùng nguy hiểm dọc của xe và đã tới gần theo trục X, chó chậm lại để kịp lách qua
                boolean inCarDangerY = Math.abs(blocker.y - this.y) < (blocker.h + this.h) / 2.0 + 8.0;
                double distToBlocker = dir * (blocker.x - this.x);
                if (inCarDangerY && distToBlocker > 0 && distToBlocker < (blocker.w + this.w) / 2.0 + 20.0) {
                    speed = DASH_SPEED * 0.20; // Hãm tốc độ ngang để lượn qua
                } else if (!inCarDangerY) {
                    speed = DASH_SPEED * 1.30; // Đã lách thoát thân xe: phóng nhanh vượt qua
                }
            } else {
                // Không có xe cản: kiểm tra xem đã qua hẳn điểm né cũ chưa
                if (targetDodgeY > 0 && Math.abs(targetDodgeY - this.y) < 12) {
                    targetDodgeY = -1;
                }
                dodgeVy *= Math.max(0.0, 1.0 - dt * 6.0);
                this.y += scroll * 0.18 * dt; // Trôi nhẹ tự nhiên theo cuộn đường
            }

            // Độ nghiêng thân chó khi lượn né xe
            tiltAngle = Math.max(-0.24, Math.min(0.24, (dodgeVy / 312.5) * 0.35));

            this.x += dir * speed * dt;
            this.baseX = this.x;

            if ((dir > 0 && this.x > roadX + roadW + 36) || (dir < 0 && this.x < roadX - 36)) {
                done = true;
            }
        }
    }

    private boolean isCandidateClear(double candY, double atX, java.util.List<Thing> things, Thing blocker) {
        if (candY < 105 || candY > 550) return false;
        for (Thing o : things) {
            if (o == this || o == blocker || o instanceof StrayDog || o instanceof Shark || o.snack || o.kind == 14) continue;
            if (Math.abs(o.x - atX) < (o.w + this.w) / 2.0 + 20.0
                    && Math.abs(o.y - candY) < (o.h + this.h) / 2.0 + 16.0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void draw(Graphics2D g, GameVisuals scene) {
        if (done) return;
        long now = System.currentTimeMillis();
        AffineTransform old = g.getTransform();
        Stroke oldStroke = g.getStroke();
        g.translate(this.x, this.y);

        if (phase == PHASE_BARK_WARN) {
            drawCrossingWarning(g, now);
        }

        // Lật hướng vẽ theo chiều chạy (mặc định vẽ hướng sang phải dir = 1)
        g.scale(dir, 1.0);
        if (Math.abs(tiltAngle) > 0.01) {
            g.rotate(tiltAngle);
        }
        double hw = this.w / 2, hh = this.h / 2;

        BufferedImage art = ArtAssets.load("StrayDog", "StrayDog.png");
        if (art != null) {
            g.setColor(new Color(0, 0, 0, 50));
            g.fill(new Ellipse2D.Double(-hw + 2, -hh + 6, this.w - 4, this.h - 4));
            g.drawImage(art, (int) -hw, (int) -hh, (int) this.w, (int) this.h, null);
            g.setTransform(old);
            g.setStroke(oldStroke);
            return;
        }

        double runCycle = phase == PHASE_DASH_ACROSS ? Math.sin(now / 42.0) : Math.sin(now / 140.0) * 0.25;
        double legSwing = 7.0 * runCycle;

        // Bóng đổ dưới chân chó
        g.setColor(new Color(0, 0, 0, 55));
        g.fill(new Ellipse2D.Double(-20, -6, 40, 18));

        Color fur = new Color(230, 168, 72);
        Color furDark = new Color(188, 126, 44);
        Color furLight = new Color(248, 212, 138);

        // 4 chân chạy lon ton
        g.setColor(furDark);
        g.setStroke(new BasicStroke(4.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(-12, -5, -15 - legSwing, -11));
        g.draw(new Line2D.Double(-12, 5, -15 + legSwing, 11));
        g.draw(new Line2D.Double(10, -5, 14 + legSwing, -11));
        g.draw(new Line2D.Double(10, 5, 14 - legSwing, 11));

        // Đuôi ngoắc cong phía sau
        Path2D.Double tail = new Path2D.Double();
        double wag = Math.sin(now / 55.0) * 4.0;
        tail.moveTo(-16, 0);
        tail.quadTo(-24, wag * 0.5, -26, -5 + wag);
        g.setColor(furDark);
        g.setStroke(new BasicStroke(3.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(tail);

        // Thân chó vàng
        g.setColor(fur);
        g.fill(new RoundRectangle2D.Double(-17, -8, 30, 16, 14, 14));
        g.setColor(furDark);
        g.fill(new Ellipse2D.Double(-12, -5, 18, 10));
        g.setStroke(new BasicStroke(1.3f));
        g.draw(new RoundRectangle2D.Double(-17, -8, 30, 16, 14, 14));

        // Vòng cổ đỏ
        g.setColor(new Color(220, 46, 58));
        g.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(9, -7, 9, 7));

        // Đầu & mõm chó hướng về phía trước (+x)
        g.setColor(fur);
        g.fill(new Ellipse2D.Double(8, -8, 15, 16));
        g.setColor(furLight);
        g.fill(new RoundRectangle2D.Double(18, -4, 8, 8, 5, 5));

        // Lưỡi hồng thè ra vì trời nắng nóng
        g.setColor(new Color(242, 105, 125));
        g.fill(new RoundRectangle2D.Double(22, 2, 5, 4, 3, 3));

        // Mũi đen & mắt
        g.setColor(new Color(30, 32, 38));
        g.fill(new Ellipse2D.Double(24, -2, 3.5, 3.5));
        g.fill(new Ellipse2D.Double(16, -5, 2.8, 2.8));
        g.fill(new Ellipse2D.Double(16, 2.2, 2.8, 2.8));

        // Đôi tai cụp
        g.setColor(furDark);
        g.fill(new RoundRectangle2D.Double(10, -10, 6, 4, 3, 3));
        g.fill(new RoundRectangle2D.Double(10, 6, 6, 4, 3, 3));

        g.setTransform(old);
        g.setStroke(oldStroke);
    }

    private void drawCrossingWarning(Graphics2D g, long now) {
        boolean blink = (now / 130) % 2 == 0;
        // Vệt mũi tên chấm cảnh báo hướng chó sắp lao ngang qua đường
        g.setColor(blink ? new Color(255, 80, 60, 220) : new Color(255, 180, 40, 160));
        g.setStroke(WARN_DASH_STROKE);
        g.draw(new Line2D.Double(dir * 24, 0, dir * 130, 0));
        g.setStroke(STROKE_RESET_1F);

        // Khung cảnh báo ! trên đầu chó
        double by = -28;
        g.setColor(new Color(255, 245, 210, 240));
        g.fill(new RoundRectangle2D.Double(-18, by - 14, 36, 18, 8, 8));
        g.setColor(new Color(220, 50, 50));
        g.setStroke(WARN_BOX_STROKE);
        g.draw(new RoundRectangle2D.Double(-18, by - 14, 36, 18, 8, 8));
        g.setFont(WARN_FONT);
        String warn = "!";
        int tw = g.getFontMetrics().stringWidth(warn);
        g.drawString(warn, -tw / 2, (int) by + 1);
    }
}
