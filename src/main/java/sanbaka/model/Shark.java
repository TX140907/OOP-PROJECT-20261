package sanbaka.model;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import sanbaka.assets.ArtAssets;
import sanbaka.render.GameVisuals;

/**
 * Cá mập màn 2 (đường ngập):
 * 1. PHASE_FIN_APPROACH: Xuất hiện từ mép trên màn hình (hiện vây lưng rẽ nước) lao nhanh xuống rồi lặn.
 * 2. PHASE_SUBMERGED: Lặn ngầm dưới nước một nhịp ngắn.
 * 3. PHASE_WARNING: Hiện vòng tròn cảnh báo dưới chân người chơi.
 * 4. PHASE_CHOMP: Trồi vọt lên khỏi mặt nước đớp mạnh rồi lặn mất.
 */
public class Shark extends Thing {
    public static final int PHASE_FIN_APPROACH = 0;
    public static final int PHASE_SUBMERGED = 1;
    public static final int PHASE_WARNING = 2;
    public static final int PHASE_CHOMP = 3;

    private static final long SUBMERGED_MS = 450;
    private static final long WARNING_MS = 750;
    private static final long CHOMP_MS = 520;
    private static final double FIN_SPEED = 540.0;

    public int phase = PHASE_FIN_APPROACH;
    public long phaseStartedAt = System.currentTimeMillis();
    public double diveY = 155;
    public boolean done = false;

    public Shark() {
        this.w = 68;
        this.h = 68;
        this.fwd = 1.0;
    }

    /** Vòng tròn cảnh báo và lúc lặn vẽ dưới lớp thuyền người chơi. */
    public boolean isGroundLayer() {
        return phase == PHASE_SUBMERGED || phase == PHASE_WARNING;
    }

    /** Cá mập chỉ gây sát thương khi đang trồi lên đớp (PHASE_CHOMP). */
    public boolean isChomping(long now) {
        if (phase != PHASE_CHOMP) return false;
        long elapsed = now - phaseStartedAt;
        return elapsed >= 40 && elapsed <= CHOMP_MS - 90;
    }

    public void diveNow(long now) {
        if (phase == PHASE_FIN_APPROACH) {
            phase = PHASE_SUBMERGED;
            phaseStartedAt = now;
        }
    }

    public boolean readyToLockWarning(long now) {
        return phase == PHASE_SUBMERGED && (now - phaseStartedAt >= SUBMERGED_MS);
    }

    public void startWarningAt(double targetX, double targetY, long now) {
        this.x = this.baseX = targetX;
        this.y = targetY;
        this.phase = PHASE_WARNING;
        this.phaseStartedAt = now;
    }

    public void updateShark(double dt, long now) {
        if (phase == PHASE_FIN_APPROACH) {
            this.y += FIN_SPEED * dt;
            if (this.y >= diveY) {
                diveNow(now);
            }
        } else if (phase == PHASE_WARNING) {
            if (now - phaseStartedAt >= WARNING_MS) {
                phase = PHASE_CHOMP;
                phaseStartedAt = now;
            }
        } else if (phase == PHASE_CHOMP) {
            if (now - phaseStartedAt >= CHOMP_MS) {
                done = true;
            }
        }
    }

    @Override
    public void draw(Graphics2D g, GameVisuals scene) {
        if (done) return;
        long now = System.currentTimeMillis();
        AffineTransform old = g.getTransform();
        Stroke oldStroke = g.getStroke();
        g.translate(this.x, this.y);

        if (phase == PHASE_FIN_APPROACH) {
            drawFinApproach(g, now);
        } else if (phase == PHASE_SUBMERGED) {
            drawDiveSplash(g, now);
        } else if (phase == PHASE_WARNING) {
            drawWarningCircle(g, now);
        } else if (phase == PHASE_CHOMP) {
            drawChompEruption(g, now);
        }

        g.setTransform(old);
        g.setStroke(oldStroke);
    }

    private void drawFinApproach(Graphics2D g, long now) {
        double diveDist = Math.max(0, diveY - this.y);
        double sinkScale = diveDist < 28 ? diveDist / 28.0 : 1.0;
        double sway = Math.sin(now / 70.0) * 2.5;

        // Bóng cá mập lờ mờ dưới mặt nước
        g.setColor(new Color(18, 38, 62, (int) (95 * sinkScale)));
        g.fill(new Ellipse2D.Double(-18 + sway * 0.5, -30, 36, 62));

        // Vệt sóng chữ V rẽ nước phía sau vây
        g.setColor(new Color(230, 244, 255, (int) (185 * sinkScale)));
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(-4 + sway, 8, -20, -24));
        g.draw(new Line2D.Double(4 + sway, 8, 20, -24));
        g.setColor(new Color(230, 244, 255, (int) (110 * sinkScale)));
        g.draw(new Line2D.Double(-8 + sway, 0, -26, -34));
        g.draw(new Line2D.Double(8 + sway, 0, 26, -34));

        // Vây lưng cá mập nhô lên rẽ nước
        Path2D.Double fin = new Path2D.Double();
        double fh = 26 * sinkScale;
        fin.moveTo(sway, -fh);
        fin.curveTo(12 + sway, -fh * 0.3, 13 + sway, 8, 9 + sway, 14);
        fin.lineTo(-9 + sway, 14);
        fin.curveTo(-11 + sway, 4, -6 + sway, -fh * 0.5, sway, -fh);
        fin.closePath();

        g.setColor(new Color(68, 86, 110));
        g.fill(fin);
        g.setColor(new Color(34, 46, 64));
        g.setStroke(new BasicStroke(1.8f));
        g.draw(fin);

        // Bọt sóng trắng dưới chân vây
        g.setColor(new Color(242, 250, 255, (int) (210 * sinkScale)));
        g.fill(new Arc2D.Double(-14 + sway, 7, 28, 12, 180, 180, Arc2D.CHORD));
    }

    private void drawDiveSplash(Graphics2D g, long now) {
        double p = Math.min(1.0, (now - phaseStartedAt) / 260.0);
        if (p >= 1.0) return;
        int alpha = (int) (180 * (1.0 - p));
        double r = 10 + p * 26;
        g.setColor(new Color(235, 246, 255, alpha));
        g.setStroke(new BasicStroke(2.0f));
        g.draw(new Ellipse2D.Double(-r, -r * 0.65, r * 2, r * 1.3));
        double r2 = 4 + p * 15;
        g.draw(new Ellipse2D.Double(-r2, -r2 * 0.65, r2 * 2, r2 * 1.3));
    }

    private void drawWarningCircle(Graphics2D g, long now) {
        double p = Math.max(0.0, Math.min(1.0, (now - phaseStartedAt) / (double) WARNING_MS));
        double pulse = 0.5 + 0.5 * Math.sin(now / 55.0);
        double outerR = 38;

        // Nền tròn đỏ cảnh báo nguy hiểm dưới chân nhân vật
        g.setColor(new Color(235, 40, 52, 45 + (int) (55 * pulse)));
        g.fill(new Ellipse2D.Double(-outerR, -outerR, outerR * 2, outerR * 2));

        // Bóng đen cá mập đang trồi dần lên từ đáy nước
        double shadowR = 12 + p * 22;
        g.setColor(new Color(16, 28, 46, 90 + (int) (110 * p)));
        g.fill(new Ellipse2D.Double(-shadowR, -shadowR, shadowR * 2, shadowR * 2));

        // Vòng tròn cảnh báo ngoài cùng
        g.setColor(new Color(255, 65, 70, 230));
        g.setStroke(new BasicStroke(2.8f));
        g.draw(new Ellipse2D.Double(-outerR, -outerR, outerR * 2, outerR * 2));

        // Vòng tròn thu hẹp dần báo hiệu thời điểm cá mập đớp
        double innerR = Math.max(6, outerR * (1.0 - p * 0.78));
        g.setColor(new Color(255, 210, 60, 220));
        g.setStroke(new BasicStroke(2.0f));
        g.draw(new Ellipse2D.Double(-innerR, -innerR, innerR * 2, innerR * 2));

        // Dấu chấm than cảnh báo ở tâm
        g.setColor(new Color(255, 240, 120, 235));
        g.fill(new RoundRectangle2D.Double(-2.5, -13, 5, 15, 2, 2));
        g.fill(new Ellipse2D.Double(-2.5, 5, 5, 5));
    }

    private void drawChompEruption(Graphics2D g, long now) {
        double p = Math.max(0.0, Math.min(1.0, (now - phaseStartedAt) / (double) CHOMP_MS));
        double emerge = p < 0.22 ? (p / 0.22) : (p > 0.76 ? Math.max(0.0, (1.0 - p) / 0.24) : 1.0);
        // Độ mở hàm: há to lúc mới trồi lên rồi ngoạm sập lại
        double jawOpen = p < 0.35 ? 1.0 : Math.max(0.12, 1.0 - (p - 0.35) / 0.35);

        BufferedImage art = ArtAssets.load("Shark", "Shark.png");
        if (art != null) {
            int sw = (int) (this.w * emerge), sh = (int) (this.h * emerge);
            g.drawImage(art, -sw / 2, -sh / 2, sw, sh, null);
            return;
        }

        // Vòng sóng nước & bọt trắng tung tóe quanh miệng hố nước
        double splashR = 34 + 10 * Math.sin(p * Math.PI);
        g.setColor(new Color(225, 244, 255, (int) (210 * emerge)));
        g.fill(new Ellipse2D.Double(-splashR, -splashR * 0.55 + 8, splashR * 2, splashR * 1.1));
        g.setColor(new Color(82, 132, 175, (int) (180 * emerge)));
        g.setStroke(new BasicStroke(2.4f));
        g.draw(new Ellipse2D.Double(-splashR - 4, -splashR * 0.6 + 8, (splashR + 4) * 2, (splashR + 4) * 1.2));

        g.scale(emerge, emerge);

        // Thân đầu cá mập trồi thẳng lên
        Path2D.Double head = new Path2D.Double();
        head.moveTo(0, -38);
        head.curveTo(26, -26, 32, 0, 28, 22);
        head.lineTo(-28, 22);
        head.curveTo(-32, 0, -26, -26, 0, -38);
        head.closePath();

        g.setColor(new Color(64, 84, 108));
        g.fill(head);
        g.setColor(new Color(32, 44, 60));
        g.setStroke(new BasicStroke(2.0f));
        g.draw(head);

        // Phần bụng/cằm trắng xám
        g.setColor(new Color(216, 224, 234));
        g.fill(new Ellipse2D.Double(-22, -16, 44, 34));

        // Hai mắt dữ tợn hai bên đầu
        g.setColor(new Color(255, 215, 70));
        g.fill(new Ellipse2D.Double(-22, -22, 6, 6));
        g.fill(new Ellipse2D.Double(16, -22, 6, 6));
        g.setColor(new Color(20, 24, 30));
        g.fill(new Ellipse2D.Double(-20, -21, 3, 4));
        g.fill(new Ellipse2D.Double(17, -21, 3, 4));

        // Vòm miệng đỏ thẫm há to rồi đớp sập lại
        double mouthH = 10 + 18 * jawOpen;
        double mouthY = -4 - mouthH / 2;
        RoundRectangle2D.Double mouth = new RoundRectangle2D.Double(-19, mouthY, 38, mouthH, 14, 14);
        g.setColor(new Color(148, 28, 42));
        g.fill(mouth);
        g.setColor(new Color(210, 68, 82));
        g.fill(new Ellipse2D.Double(-13, mouthY + 3, 26, Math.max(4, mouthH - 6)));

        // Hàm răng nhọn sắc trắng muốt (hàm trên + hàm dưới)
        g.setColor(new Color(248, 250, 252));
        int teethCount = 6;
        double toothStep = 32.0 / teethCount;
        double toothLen = 4 + 4 * jawOpen;
        for (int i = 0; i < teethCount; i++) {
            double tx = -16 + i * toothStep;
            // Răng hàm trên
            Path2D.Double upperTooth = new Path2D.Double();
            upperTooth.moveTo(tx, mouthY + 1);
            upperTooth.lineTo(tx + toothStep * 0.5, mouthY + 1 + toothLen);
            upperTooth.lineTo(tx + toothStep, mouthY + 1);
            upperTooth.closePath();
            g.fill(upperTooth);

            // Răng hàm dưới
            Path2D.Double lowerTooth = new Path2D.Double();
            double by = mouthY + mouthH - 1;
            lowerTooth.moveTo(tx, by);
            lowerTooth.lineTo(tx + toothStep * 0.5, by - toothLen);
            lowerTooth.lineTo(tx + toothStep, by);
            lowerTooth.closePath();
            g.fill(lowerTooth);
        }

        g.setColor(new Color(32, 44, 60));
        g.setStroke(new BasicStroke(1.6f));
        g.draw(mouth);
    }
}

