package sanbaka.model;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import sanbaka.assets.ArtAssets;
import sanbaka.render.GameVisuals;

/** Ninja Lead: xe máy trùm kín áo chống nắng, phóng nhanh và bất ngờ bật xi-nhan tạt đầu cắt làn. */
public class NinjaLead extends Thing {
    public int targetLane = -1;
    public double targetX = 0;
    public int cutDir = 0; // -1: tạt sang trái, 1: tạt sang phải
    public int cutPhase = 0; // 0: chờ, 1: nhấp nháy xi-nhan + báo hiệu, 2: đang tạt, 3: đã tạt xong
    public long phaseStart = 0;
    public double triggerY = 110;
    public double speedScale = 1.0;

    public void updateMovement(double dt, double lane0X, double lane1X, double lane2X) {
        if (cutPhase == 0) {
            if (this.y >= triggerY) {
                // Xác định làn hiện tại gần nhất
                int currentLane = 1;
                double d0 = Math.abs(this.x - lane0X);
                double d1 = Math.abs(this.x - lane1X);
                double d2 = Math.abs(this.x - lane2X);
                if (d0 <= d1 && d0 <= d2) currentLane = 0;
                else if (d2 <= d1 && d2 <= d0) currentLane = 2;

                if (currentLane == 0) cutDir = 1; // làn 1 tạt sang làn 2
                else if (currentLane == 2) cutDir = -1; // làn 3 tạt sang làn 2
                else cutDir = (System.currentTimeMillis() % 2 == 0) ? -1 : 1; // làn 2 tạt sang 1 hoặc 3

                targetLane = currentLane + cutDir;
                targetX = targetLane == 0 ? lane0X : (targetLane == 1 ? lane1X : lane2X);
                cutPhase = 1;
                phaseStart = System.currentTimeMillis();
            }
        } else if (cutPhase == 1) {
            // Giai đoạn nhấp nháy xi-nhan và báo hiệu dấu ! (kéo dài ~420ms)
            if (System.currentTimeMillis() - phaseStart >= 420) {
                cutPhase = 2;
                phaseStart = System.currentTimeMillis();
            }
        } else if (cutPhase == 2) {
            // Giai đoạn đánh lái tạt chéo
            double shiftSpeed = 160.0 * speedScale; // px/s
            this.x += cutDir * shiftSpeed * dt;
            this.angle = cutDir * 0.22;
            if ((cutDir > 0 && this.x >= targetX) || (cutDir < 0 && this.x <= targetX)) {
                this.x = targetX;
                this.baseX = targetX;
                this.angle = 0;
                cutPhase = 3;
            }
        } else if (cutPhase == 3) {
            this.angle = 0;
        }
    }

    @Override
    public void draw(Graphics2D g, GameVisuals scene) {
        AffineTransform old = g.getTransform();
        Stroke oldStroke = g.getStroke();
        g.translate(this.x, this.y);
        g.rotate(this.angle);
        double hw = this.w / 2, hh = this.h / 2;

        BufferedImage art = ArtAssets.load("NinjaLead", "NinjaLead.png");
        if (art != null) {
            g.setColor(new Color(0, 0, 0, 55));
            g.fill(new RoundRectangle2D.Double(-hw + 3, -hh + 6, this.w, this.h, 16, 16));
            g.drawImage(art, (int) -hw, (int) -hh, (int) this.w, (int) this.h, null);
            drawAlertAndBlinker(g, hh);
            g.setTransform(old);
            g.setStroke(oldStroke);
            return;
        }

        // Bóng đổ
        g.setColor(new Color(0, 0, 0, 55));
        g.fill(new RoundRectangle2D.Double(-hw + 3, -hh + 6, this.w, this.h, 16, 16));

        // Màu xe Honda Lead: be sữa kem hoặc đỏ mận
        Color leadBody = (this.color % 2 == 0) ? new Color(236, 230, 216) : new Color(175, 42, 52);
        Color leadSeat = new Color(125, 82, 54); // Yên xe nâu da bò
        // Áo chống nắng hoa nhí pastel
        Color floralDress = (this.color % 3 == 0) ? new Color(248, 185, 205) : ((this.color % 3 == 1) ? new Color(160, 215, 210) : new Color(245, 215, 140));
        Color floralDots = new Color(255, 255, 255, 190);

        // Bánh trước và sau
        g.setColor(new Color(28, 30, 36));
        g.fill(new RoundRectangle2D.Double(-3, -hh, 6, 15, 4, 4));
        g.fill(new RoundRectangle2D.Double(-4, hh - 22, 8, 22, 5, 5));

        // Thân sau xe Lead (bầu to đặc trưng)
        g.setColor(leadBody);
        g.fill(new RoundRectangle2D.Double(-11, hh - 32, 22, 26, 10, 10));
        g.setColor(scene.shade(leadBody, 0.75));
        g.draw(new RoundRectangle2D.Double(-11, hh - 32, 22, 26, 10, 10));

        // Yên xe rộng
        g.setColor(leadSeat);
        g.fill(new RoundRectangle2D.Double(-7, hh - 28, 14, 20, 6, 6));

        // Váy chống nắng phủ qua đùi và thân xe
        g.setColor(floralDress);
        g.fill(new RoundRectangle2D.Double(-9, -4, 18, 26, 8, 8));
        // Chấm bi hoa nhí trên váy
        g.setColor(floralDots);
        for (int i = -1; i <= 1; i++) {
            g.fill(new Ellipse2D.Double(i * 5 - 1.5, 0, 3, 3));
            g.fill(new Ellipse2D.Double(i * 5 - 1.5, 12, 3, 3));
        }

        // Thân áo chống nắng phía trên
        g.setColor(scene.shade(floralDress, 0.9));
        g.fill(new Ellipse2D.Double(-10, -18, 20, 22));

        // Cổ áo và khẩu trang trùm kín mặt
        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Double(-6, -22, 12, 8, 3, 3));

        // Kính râm đen
        g.setColor(new Color(20, 22, 28));
        g.fill(new RoundRectangle2D.Double(-7, -24, 14, 4, 2, 2));

        // Mũ bảo hiểm có vành chống nắng rộng xòe ra
        g.setColor(new Color(230, 90, 115));
        g.fill(new Ellipse2D.Double(-12, -hh + 5, 24, 14));
        g.setColor(new Color(255, 220, 230));
        g.fill(new Ellipse2D.Double(-8, -hh + 7, 16, 10));

        // Ghi đông xe Lead với 2 gương chiếu hậu tròn
        g.setColor(new Color(45, 48, 56));
        g.setStroke(new BasicStroke(2.2f));
        g.draw(new Line2D.Double(-12, -hh + 14, 12, -hh + 14));
        g.setStroke(new BasicStroke(1f));
        g.fill(new Ellipse2D.Double(-14, -hh + 10, 5, 5));
        g.fill(new Ellipse2D.Double(9, -hh + 10, 5, 5));

        // Đèn hậu xe Lead
        g.setColor(new Color(225, 40, 55));
        g.fill(new RoundRectangle2D.Double(-5, hh - 7, 10, 4, 2, 2));

        drawAlertAndBlinker(g, hh);

        g.setTransform(old);
        g.setStroke(oldStroke);
    }

    private void drawAlertAndBlinker(Graphics2D g, double hh) {
        long now = System.currentTimeMillis();
        boolean blink = (now / 150) % 2 == 0;

        // Xi-nhan nhấp nháy ở đuôi khi chuẩn bị hoặc đang tạt làn
        if ((cutPhase == 1 || cutPhase == 2) && blink) {
            g.setColor(new Color(255, 165, 20));
            double bx = cutDir < 0 ? -10 : 7;
            g.fill(new RoundRectangle2D.Double(bx, hh - 8, 5, 5, 2, 2));
            g.setColor(new Color(255, 200, 40, 120));
            g.fill(new Ellipse2D.Double(bx - 3, hh - 11, 11, 11));
        }

        // Biểu tượng dấu chấm than ! cảnh báo tạt đầu trên đầu xe
        if (cutPhase == 1 || cutPhase == 2) {
            double alertY = -hh - 18;
            // Biển tam giác cảnh báo màu vàng
            Polygon triangle = new Polygon();
            triangle.addPoint(0, (int) alertY - 14);
            triangle.addPoint(-10, (int) alertY + 4);
            triangle.addPoint(10, (int) alertY + 4);
            g.setColor(new Color(255, 195, 30));
            g.fill(triangle);
            g.setColor(new Color(30, 32, 40));
            g.setStroke(new BasicStroke(1.4f));
            g.draw(triangle);
            g.setStroke(new BasicStroke(1f));

            // Dấu chấm than ! màu đen
            g.setColor(new Color(30, 32, 40));
            g.fill(new Rectangle2D.Double(-1.5, alertY - 8, 3, 6));
            g.fill(new Ellipse2D.Double(-1.5, alertY + 0.5, 3, 3));
        }
    }
}

