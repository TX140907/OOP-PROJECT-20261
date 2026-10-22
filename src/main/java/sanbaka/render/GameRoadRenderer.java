package sanbaka.render;

import sanbaka.model.*;
import sanbaka.state.GameState;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;

public class GameRoadRenderer extends GameCharacterRenderer {
    private final Path2D.Double rainPath = new Path2D.Double();

    void drawRoad(Graphics2D g) {
        if (flightMode) drawFlightScene(g);
        else drawRoadScene(g);
        drawHud(g);
        long since = System.currentTimeMillis() - lastHitAt;
        if (since < 450) {
            g.setColor(new Color(255, 30, 60, (int) (120 * (1 - since / 450.0))));
            g.setStroke(new BasicStroke(36f));
            g.drawRect(-10, -10, W + 20, H + 20);
            g.setStroke(new BasicStroke(1f));
        }
    }

    void drawRoadScene(Graphics2D g) {
        long now = System.currentTimeMillis();
        double s = groundScroll;
        int rx0 = ROAD_X + 45, rx1 = ROAD_X + ROAD_W - 45;
        Color[] grass = {
            new Color(112, 169, 115), new Color(96, 146, 110), new Color(176, 198, 184),
            new Color(166, 172, 100), new Color(112, 169, 115)
        };
        g.setColor(grass[day]);
        g.fillRect(0, 0, W, H);
        for (int n = (int) Math.floor((s - H) / 120.0) - 1; n <= (int) Math.floor(s / 120.0) + 1; n++) {
            if ((n & 1) == 0) {
                int y = (int) (s - (n + 1) * 120.0);
                g.setColor(new Color(255, 255, 255, 14));
                g.fillRect(0, y, ROAD_X, 120);
                g.fillRect(ROAD_X + ROAD_W, y, W - ROAD_X - ROAD_W, 120);
            }
        }
        int n0 = (int) Math.floor((s - H) / 160.0) - 1, n1 = (int) Math.floor(s / 160.0) + 2;
        for (int n = n0; n <= n1; n++) {
            double topY = s - (n + 1) * 160.0;
            drawLot(g, 0, n, topY);
            drawLot(g, 1, n, topY);
        }
        // vỉa hè lát gạch
        g.setColor(day == 2 ? new Color(220, 226, 232) : new Color(206, 193, 168));
        g.fillRect(ROAD_X, 0, 45, H);
        g.fillRect(rx1, 0, 45, H);
        g.setColor(new Color(0, 0, 0, 22));
        for (double y = (s % 44) - 44; y < H; y += 44) {
            g.fillRect(ROAD_X, (int) y, 45, 2);
            g.fillRect(rx1, (int) y, 45, 2);
        }
        g.fillRect(ROAD_X + 22, 0, 1, H);
        g.fillRect(rx1 + 22, 0, 1, H);
        g.setColor(new Color(150, 140, 120));
        g.fillRect(rx0 - 4, 0, 4, H);
        g.fillRect(rx1, 0, 4, H);
        g.setColor(new Color(255, 255, 255, 70));
        g.fillRect(rx0 - 6, 0, 2, H);
        g.fillRect(rx1 + 4, 0, 2, H);
        for (int n = n0; n <= n1; n++) {
            double ly = s - (n + 1) * 160.0 + 100;
            drawLamp(g, ROAD_X + 9, ly, 1);
            drawLamp(g, ROAD_X + ROAD_W - 9, ly, -1);
        }
        // Ngày 2 (day == 1) bị ngập: bỏ toàn bộ mặt đường và vạch sơn bên dưới lớp nước.
        if (day != 1) {
            Color[] asphalt = {
                new Color(84, 92, 104), new Color(74, 86, 104), new Color(104, 112, 124),
                new Color(98, 98, 102), new Color(84, 92, 104)
            };
            g.setColor(asphalt[day]);
            g.fillRect(rx0, 0, rx1 - rx0, H);
            g.setColor(new Color(0, 0, 0, 16));
            for (int l = 0; l < 3; l++) {
                g.fillRect((int) laneX(l) - 30, 0, 14, H);
                g.fillRect((int) laneX(l) + 16, 0, 14, H);
            }
            for (int n = n0; n <= n1; n++) {
                int h = hash(n, 7);
                double py = s - (n + 1) * 160.0 + (h % 120);
                double px = rx0 + 20 + (h / 7) % (rx1 - rx0 - 90);
                g.setColor(new Color(0, 0, 0, 26));
                g.fill(new RoundRectangle2D.Double(px, py, 36 + h % 30, 12 + h % 10, 6, 6));
                g.setColor(new Color(0, 0, 0, 40));
                g.setStroke(new BasicStroke(1.2f));
                g.draw(new Line2D.Double(px + 60, py + 30, px + 74, py + 52));
                g.draw(new Line2D.Double(px + 74, py + 52, px + 70, py + 70));
                g.setStroke(new BasicStroke(1f));
                if (hash(n, 3) % 9 == 0) {
                    double cy = s - (n + 1) * 160.0 + 80;
                    g.setColor(new Color(245, 245, 238, 205));
                    for (int i = 0; i < 10; i++) g.fill(new Rectangle2D.Double(rx0 + 6 + i * 46, cy - 22, 26, 44));
                }
                if (day == 2 && h % 2 == 1) {
                    double iw = 60 + h % 70, ih = 40 + h % 40;
                    double ix = rx0 + 20 + (h / 3) % (rx1 - rx0 - 120), iy = py + 20;
                    g.setColor(new Color(225, 240, 252, 150));
                    g.fill(new Ellipse2D.Double(ix, iy, iw, ih));
                    g.setColor(new Color(255, 255, 255, 190));
                    double tw = 0.5 + 0.5 * Math.sin(now / 280.0 + h);
                    g.fill(new Ellipse2D.Double(ix + iw * 0.3, iy + ih * 0.3, 4 + 3 * tw, 4 + 3 * tw));
                }
            }
            g.setColor(new Color(240, 240, 235, 210));
            g.fillRect(rx0 + 6, 0, 3, H);
            g.fillRect(rx1 - 9, 0, 3, H);
            for (double y = (s % 80) - 80; y < H; y += 80) {
                g.fillRect((int) dividerX(1) - 2, (int) y, 4, 40);
                g.fillRect((int) dividerX(2) - 2, (int) y, 4, 40);
            }
        }
        if (day == 1) drawFlood(g, rx0, rx1, s, now);
        // cây ven đường (tán lá che nhẹ vỉa hè)
        for (int n = n0; n <= n1; n++) {
            int h = hash(n, 11);
            double ty = s - (n + 1) * 160.0 + 30 + h % 90;
            drawTree(g, ROAD_X - 4, ty, 28 + h % 8, h / 5);
            drawTree(g, ROAD_X + ROAD_W + 4, ty + 60, 28 + (h / 3) % 8, h / 9);
        }
        // trường học + vạch đích
        double fy = finishY();
        if (fy > 0) drawCampus(g, fy);
        // xe cộ, tia sét, người chơi
        for (Thing o : things) o.draw(g, this);
        player.draw(g, this);
        // thời tiết
        if (day == 1) {
            g.setColor(new Color(10, 24, 50, 30));
            g.fillRect(0, 0, W, H);
            g.setColor(new Color(205, 225, 250, 150));
            g.setStroke(new BasicStroke(1.6f));
            rainPath.reset();
            for (int i = 0; i < 110; i++) {
                double rx = (i * 137 + now * 0.12) % (W + 60) - 30;
                double ry = (i * 89 + now * 0.9) % (H + 40) - 20;
                rainPath.moveTo(rx, ry);
                rainPath.lineTo(rx - 4, ry + 16);
            }
            g.draw(rainPath);
            g.setStroke(new BasicStroke(1f));
        } else if (day == 2) {
            g.setColor(new Color(190, 215, 245, 30));
            g.fillRect(0, 0, W, H);
            g.setColor(new Color(255, 255, 255, 220));
            Ellipse2D.Double snowflake = new Ellipse2D.Double();
            for (int i = 0; i < 90; i++) {
                double fx = (i * 97 + Math.sin(now / 900.0 + i) * 18 + 2 * W) % W;
                double fyy = (i * 61 + now * 0.07 * (1 + i % 3)) % (H + 20) - 10;
                double fr = 2 + (i % 3);
                snowflake.setFrame(fx, fyy, fr, fr);
                g.fill(snowflake);
            }
        } else if (day == HOT_DAY) {
            g.setColor(new Color(255, 214, 110, 26));
            g.fillRect(0, 0, W, H);
            g.setPaint(new LinearGradientPaint(0, 0, 0, 250, new float[]{0f, 0.55f, 1f},
                    new Color[]{new Color(255, 255, 255, 255), new Color(255, 255, 255, 232), new Color(255, 255, 255, 0)}));
            g.fillRect(0, 0, W, 250);
            g.setColor(new Color(255, 255, 255, 60));
            g.setStroke(new BasicStroke(2f));
            Path2D.Double heatWave = new Path2D.Double();
            for (int i = 0; i < 5; i++) {
                heatWave.reset();
                double wy = 200 + i * 14;
                heatWave.moveTo(rx0, wy);
                for (int x = rx0; x <= rx1; x += 20) heatWave.lineTo(x, wy + Math.sin(now / 260.0 + x * 0.05 + i) * 4);
                g.draw(heatWave);
            }
            g.setStroke(new BasicStroke(1f));
        }
    }

}

