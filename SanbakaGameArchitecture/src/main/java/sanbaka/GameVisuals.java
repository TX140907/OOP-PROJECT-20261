package sanbaka;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;

class GameVisuals extends GameLogic {
    void drawConfetti(Graphics2D g, long now) {
        for (double[] p : pops) {
            double age = (now - p[2]) / 450.0;
            g.setColor(new Color(255, 245, 190, (int) (200 * (1 - age))));
            g.setStroke(new BasicStroke(4f * (float) (1 - age) + 1f));
            double r = 14 + age * 80;
            g.draw(new Ellipse2D.Double(p[0] - r, p[1] - r, 2 * r, 2 * r));
        }
        g.setStroke(new BasicStroke(1f));
        AffineTransform old = g.getTransform();
        for (Confetti c : confetti) {
            g.translate(c.x, c.y);
            g.rotate(c.rot);
            g.setColor(c.color);
            double flip = Math.abs(Math.cos(c.rot * 1.7)) * 0.8 + 0.2;
            if (c.shape == 0) g.fill(new Rectangle2D.Double(-c.size / 2, -c.size * 0.3 * flip, c.size, c.size * 0.6 * flip));
            else if (c.shape == 1) g.fill(new Ellipse2D.Double(-c.size * 0.3, -c.size * 0.3 * flip, c.size * 0.6, c.size * 0.6 * flip));
            else g.fill(new Rectangle2D.Double(-c.size * 0.9, -1.5, c.size * 1.8, 3 * flip + 0.5));
            g.setTransform(old);
        }
    }

    void drawPopper(Graphics2D g, double x, double y, double rot) {
        AffineTransform old = g.getTransform();
        g.translate(x, y);
        g.rotate(rot);
        g.setColor(PINK);
        poly(g, -22, 0, 22, 0, 0, -70);
        g.setColor(new Color(255, 214, 92));
        poly(g, -14, -18, 14, -18, 10, -30);
        poly(g, -8, -42, 8, -42, 4, -52);
        g.setColor(new Color(160, 60, 100));
        g.fill(new Ellipse2D.Double(-22, -4, 44, 10));
        g.setTransform(old);
    }

    void drawBoatWake(Graphics2D g, double sternY, long now) {
        Stroke oldStroke = g.getStroke();
        g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int k = 0; k < 3; k++) {
            double ph = ((now / 600.0) + k / 3.0) % 1.0;
            double yy = sternY + ph * 46;
            double spread = 10 + ph * 26;
            g.setColor(new Color(245, 248, 250, (int) (150 * (1 - ph))));
            g.draw(new Line2D.Double(-6, yy, -spread, yy + 12));
            g.draw(new Line2D.Double(6, yy, spread, yy + 12));
        }
        g.setStroke(oldStroke);
    }

    void callIcon(Graphics2D g, int type, double cx, double cy, Color c) {
        Stroke old = g.getStroke();
        g.setColor(c);
        g.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (type) {
            case 0:
                g.fill(new RoundRectangle2D.Double(cx - 5, cy - 14, 10, 17, 10, 10));
                g.draw(new Arc2D.Double(cx - 9, cy - 8, 18, 18, 200, 140, Arc2D.OPEN));
                g.draw(new Line2D.Double(cx, cy + 10, cx, cy + 15));
                g.draw(new Line2D.Double(cx - 12, cy + 14, cx + 12, cy - 14));
                break;
            case 1:
                for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++) g.fill(new Ellipse2D.Double(cx - 12 + i * 10, cy - 12 + j * 10, 5, 5));
                break;
            case 2:
                poly(g, cx - 13, cy - 4, cx - 6, cy - 4, cx + 2, cy - 11, cx + 2, cy + 11, cx - 6, cy + 4, cx - 13, cy + 4);
                g.draw(new Arc2D.Double(cx - 4, cy - 8, 14, 16, -55, 110, Arc2D.OPEN));
                g.draw(new Arc2D.Double(cx - 8, cy - 14, 24, 28, -55, 110, Arc2D.OPEN));
                break;
            case 3:
                g.draw(new Line2D.Double(cx, cy - 12, cx, cy + 12));
                g.draw(new Line2D.Double(cx - 12, cy, cx + 12, cy));
                break;
            case 4:
                g.fill(new RoundRectangle2D.Double(cx - 14, cy - 8, 18, 16, 5, 5));
                poly(g, cx + 6, cy - 3, cx + 15, cy - 9, cx + 15, cy + 9, cx + 6, cy + 3);
                break;
            default:
                g.fill(new Ellipse2D.Double(cx - 6, cy - 14, 12, 12));
                g.fill(new Arc2D.Double(cx - 13, cy + 1, 26, 22, 0, 180, Arc2D.PIE));
                break;
        }
        g.setStroke(old);
    }

    void panel(Graphics2D g,int x,int y,int w,int h,Color c) {
        g.setColor(new Color(27,36,53,35));
        g.fill(new RoundRectangle2D.Double(x+5,y+7,w,h,24,24));
        g.setColor(c);
        g.fill(new RoundRectangle2D.Double(x,y,w,h,24,24));
    }

    void drawCurtain(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(new Color(244, 170, 190));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 18, 18));
        g.setColor(new Color(224, 130, 158));
        for (int i = 0; i < 3; i++) g.fill(new RoundRectangle2D.Double(x + 8 + i * 14, y + 8, 5, h - 18, 4, 4));
    }

    void drawBolt(Graphics2D g, double x, double y, double sc) {
        AffineTransform old = g.getTransform();
        g.translate(x, y);
        g.scale(sc, sc);
        g.setColor(new Color(255, 230, 90, 80));
        g.fill(new Ellipse2D.Double(-26, -26, 52, 52));
        g.setColor(new Color(255, 255, 255, 120));
        g.fill(new Ellipse2D.Double(-18, -18, 36, 36));
        Path2D.Double bolt = new Path2D.Double();
        bolt.moveTo(3, -18);
        bolt.lineTo(-10, 2);
        bolt.lineTo(-2, 2);
        bolt.lineTo(-6, 18);
        bolt.lineTo(10, -4);
        bolt.lineTo(2, -4);
        bolt.lineTo(8, -18);
        bolt.closePath();
        g.setColor(new Color(255, 214, 40));
        g.fill(bolt);
        g.setColor(new Color(224, 140, 20));
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(bolt);
        g.setStroke(new BasicStroke(1f));
        g.setTransform(old);
    }

    void poly(Graphics2D g, double... p) {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(p[0], p[1]);
        for (int i = 2; i + 1 < p.length; i += 2) path.lineTo(p[i], p[i + 1]);
        path.closePath();
        g.fill(path);
    }

    void drawHeart(Graphics2D g, double cx, double cy, double s, Color c) {
        Path2D.Double h = new Path2D.Double();
        h.moveTo(cx, cy + s * 0.42);
        h.curveTo(cx - s * 0.75, cy - s * 0.05, cx - s * 0.4, cy - s * 0.6, cx, cy - s * 0.2);
        h.curveTo(cx + s * 0.4, cy - s * 0.6, cx + s * 0.75, cy - s * 0.05, cx, cy + s * 0.42);
        g.setColor(c);
        g.fill(h);
    }

    void drawAvatar(Graphics2D g, String who, int cx, int cy, int r) {
        int kind = who.startsWith("Sanbaka") ? 0
                : who.startsWith("Thắng") ? 1
                : who.startsWith("Nam") ? 2
                : who.startsWith("Chủ trọ") ? 3 : 4;
        Color[] bgs = {
            new Color(255, 214, 226), new Color(200, 226, 246), new Color(255, 226, 190),
            new Color(214, 236, 216), new Color(226, 230, 238)
        };
        Shape oldClip = g.getClip();
        AffineTransform oldT = g.getTransform();
        Stroke oldStroke = g.getStroke();
        Ellipse2D circle = new Ellipse2D.Double(cx - r, cy - r, 2.0 * r, 2.0 * r);
        g.setColor(bgs[kind]);
        g.fill(circle);
        g.clip(circle);
        g.translate(cx, cy + r * 0.16);
        double s = r / 32.0;
        g.scale(s, s);
        if (kind == 4) {
            g.setColor(new Color(150, 158, 176));
            for (int i = 0; i < 3; i++) g.fill(new Ellipse2D.Double(-19 + i * 14, -5, 9, 9));
        } else {
            Color skin = new Color(255, 224, 196);
            Color hair = kind == 0 ? new Color(58, 40, 48) : kind == 1 ? new Color(48, 38, 44)
                    : kind == 2 ? new Color(122, 78, 52) : new Color(208, 208, 216);
            Color cloth = kind == 0 ? PINK : kind == 1 ? new Color(45, 61, 87)
                    : kind == 2 ? new Color(240, 150, 70) : new Color(110, 160, 122);
            g.setColor(hair);
            if (kind == 0) g.fill(new Ellipse2D.Double(-27, -30, 54, 68));
            if (kind == 3) g.fill(new Ellipse2D.Double(-9, -44, 18, 18));
            g.setColor(new Color(240, 200, 170));
            g.fill(new Rectangle2D.Double(-6, 14, 12, 14));
            g.setColor(cloth);
            g.fill(new RoundRectangle2D.Double(-30, 24, 60, 44, 30, 30));
            if (kind == 1) {
                g.setColor(MINT);
                g.fill(new Rectangle2D.Double(-19, 26, 6, 30));
                g.fill(new Rectangle2D.Double(13, 26, 6, 30));
            }
            if (kind == 0) {
                g.setColor(Color.WHITE);
                poly(g, -9, 24, 0, 36, 9, 24);
            }
            if (kind == 2) {
                g.setColor(new Color(255, 255, 255, 220));
                g.fill(new Rectangle2D.Double(-7, 28, 2.5, 12));
                g.fill(new Rectangle2D.Double(4.5, 28, 2.5, 12));
            }
            g.setColor(skin);
            g.fill(new Ellipse2D.Double(-20, -22, 40, 44));
            g.setColor(hair);
            if (kind == 0) {
                g.fill(new Arc2D.Double(-23, -29, 46, 40, 0, 180, Arc2D.CHORD));
                g.fill(new RoundRectangle2D.Double(-25, -8, 8, 38, 8, 8));
                g.fill(new RoundRectangle2D.Double(17, -8, 8, 38, 8, 8));
            } else if (kind == 1) {
                g.fill(new Arc2D.Double(-22, -29, 44, 40, 0, 180, Arc2D.CHORD));
                poly(g, -16, -22, -11, -37, -4, -24);
                poly(g, -5, -25, 2, -39, 9, -25);
                poly(g, 6, -24, 15, -35, 18, -18);
            } else if (kind == 2) {
                g.fill(new Arc2D.Double(-23, -30, 46, 42, 0, 180, Arc2D.CHORD));
                g.fill(new Ellipse2D.Double(-19, -36, 17, 13));
                g.fill(new Ellipse2D.Double(2, -38, 19, 14));
            } else {
                g.fill(new Arc2D.Double(-21, -28, 42, 38, 0, 180, Arc2D.CHORD));
            }
            g.setColor(INK);
            g.fill(new Ellipse2D.Double(-12, -3, 7, 9));
            g.fill(new Ellipse2D.Double(5, -3, 7, 9));
            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Double(-10, -1.5, 3, 3));
            g.fill(new Ellipse2D.Double(7, -1.5, 3, 3));
            if (kind == 2) {
                g.setColor(new Color(70, 72, 86));
                g.setStroke(new BasicStroke(2f));
                g.draw(new Ellipse2D.Double(-16, -7, 15, 17));
                g.draw(new Ellipse2D.Double(1, -7, 15, 17));
                g.draw(new Line2D.Double(-1, 0, 1, 0));
            }
            g.setColor(new Color(255, 140, 150, 110));
            g.fill(new Ellipse2D.Double(-19, 7, 10, 6));
            g.fill(new Ellipse2D.Double(9, 7, 10, 6));
            g.setColor(new Color(160, 80, 80));
            g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Arc2D.Double(-6, 7, 12, 9, 205, 130, Arc2D.OPEN));
            if (kind == 3) {
                g.setColor(new Color(196, 150, 120));
                g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Line2D.Double(-17, -8, -11, -9));
                g.draw(new Line2D.Double(11, -9, 17, -8));
            }
            if (kind == 0) {
                g.setColor(PINK);
                g.fill(new Ellipse2D.Double(8, -27, 11, 8));
                g.fill(new Ellipse2D.Double(16, -27, 11, 8));
                g.setColor(Color.WHITE);
                g.fill(new Ellipse2D.Double(14, -25, 5, 5));
            }
        }
        g.setTransform(oldT);
        g.setClip(oldClip);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(Math.max(1.5f, r / 14f)));
        g.draw(circle);
        g.setStroke(oldStroke);
    }

    void drawBird(Graphics2D g, double x, double y, double phase) {
        double flap = Math.sin(phase);
        Color body = new Color(44, 50, 70);
        Path2D.Double wing = new Path2D.Double();
        wing.moveTo(x + 2, y - 2);
        wing.quadTo(x + 14, y - 4 - flap * 20, x + 34, y - flap * 30);
        wing.quadTo(x + 18, y + 4 - flap * 8, x + 4, y + 5);
        wing.closePath();
        g.setColor(new Color(30, 36, 54));
        g.fill(wing);
        g.setColor(body);
        g.fill(new Ellipse2D.Double(x - 15, y - 8, 32, 17));
        poly(g, x + 14, y - 3, x + 30, y - 6, x + 28, y + 5);
        g.fill(new Ellipse2D.Double(x - 22, y - 11, 14, 14));
        g.setColor(new Color(214, 222, 236));
        g.fill(new Ellipse2D.Double(x - 11, y - 1, 22, 9));
        g.setColor(new Color(255, 176, 64));
        poly(g, x - 21, y - 7, x - 32, y - 3.5, x - 21, y - 1);
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(x - 19, y - 8, 5, 5));
        g.setColor(INK);
        g.fill(new Ellipse2D.Double(x - 18.5, y - 7, 2.6, 2.6));
        wing = new Path2D.Double();
        wing.moveTo(x + 4, y);
        wing.quadTo(x + 14, y - 2 - flap * 16, x + 28, y - flap * 22 + 4);
        wing.quadTo(x + 16, y + 6 - flap * 6, x + 2, y + 6);
        wing.closePath();
        g.setColor(new Color(60, 68, 94));
        g.fill(wing);
    }

    void drawHelicopter(Graphics2D g, double cx, double cy, double tilt, double time, double sc) {
        AffineTransform old = g.getTransform();
        Stroke oldStroke = g.getStroke();
        g.translate(cx, cy + Math.sin(time * 4.2) * 2);
        g.rotate(tilt);
        g.scale(sc, sc);
        Color bodyD = new Color(63, 145, 121);
        Color glass = new Color(196, 230, 246);
        // đuôi + cánh quạt đuôi
        g.setColor(bodyD);
        poly(g, -40, -10, -128, -14, -128, -5, -40, 12);
        g.setColor(PINK);
        poly(g, -126, -12, -140, -40, -124, -40, -108, -10);
        double ta = time * 30;
        g.setColor(new Color(40, 50, 70, 200));
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(-130, -8 - 15 * Math.cos(ta), -130, -8 + 15 * Math.cos(ta)));
        // càng đáp
        g.setColor(INK);
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(-46, 44, 52, 44));
        g.draw(new Line2D.Double(52, 44, 62, 38));
        g.draw(new Line2D.Double(-22, 26, -26, 44));
        g.draw(new Line2D.Double(24, 26, 28, 44));
        // thân
        g.setColor(MINT);
        g.fill(new RoundRectangle2D.Double(-54, -32, 112, 62, 52, 52));
        g.setColor(new Color(255, 255, 255, 210));
        g.fill(new RoundRectangle2D.Double(-46, 8, 92, 9, 9, 9));
        g.setColor(glass);
        g.fill(new RoundRectangle2D.Double(6, -25, 48, 38, 30, 30));
        g.fill(new RoundRectangle2D.Double(-34, -21, 30, 24, 12, 12));
        drawAvatar(g, "Thắng", -19, -8, 9);
        drawAvatar(g, "Nam", 30, -6, 11);
        g.setColor(new Color(255, 255, 255, 110));
        g.fill(new Ellipse2D.Double(36, -22, 13, 7));
        // cánh quạt chính
        g.setColor(INK);
        g.fill(new Rectangle2D.Double(-5, -46, 10, 16));
        g.setColor(new Color(40, 50, 70, 40));
        g.fill(new Ellipse2D.Double(-100, -54, 200, 14));
        double a = time * 38;
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(40, 50, 70, 200));
        g.draw(new Line2D.Double(-96 * Math.cos(a), -47, 96 * Math.cos(a), -47));
        g.setColor(new Color(40, 50, 70, 120));
        g.draw(new Line2D.Double(-96 * Math.sin(a), -47, 96 * Math.sin(a), -47));
        g.setTransform(old);
        g.setStroke(oldStroke);
    }

    void drawCloud(Graphics2D g, double x, double y, double s, int a) {
        g.setColor(new Color(255, 255, 255, a));
        g.fill(new Ellipse2D.Double(x, y, 110 * s, 34 * s));
        g.fill(new Ellipse2D.Double(x + 20 * s, y - 18 * s, 56 * s, 42 * s));
        g.fill(new Ellipse2D.Double(x + 54 * s, y - 10 * s, 48 * s, 36 * s));
    }

    int hash(int a, int b) {
        int h = a * 73856093 ^ b * 19349663 ^ 0x5bd1e995;
        h ^= h >>> 13;
        h *= 0x5bd1e995;
        h ^= h >>> 15;
        return h & 0x7fffffff;
    }

    double finishY() {
        if (day == 4) return -9999;
        double r = Math.max(0, (ROAD_DURATION_MS - elapsed) / 1000.0);
        if (r >= SCHOOL_APPROACH) return -9999;
        double base = (3.6 + day * 0.32) * 60;
        return FINISH_Y - base * (0.2 * r + 0.4 * r * r / SCHOOL_APPROACH);
    }

    Color shade(Color c, double k) {
        return new Color(Math.max(0, Math.min(255, (int) (c.getRed() * k))),
                Math.max(0, Math.min(255, (int) (c.getGreen() * k))),
                Math.max(0, Math.min(255, (int) (c.getBlue() * k))), c.getAlpha());
    }

    void drawWrapped(Graphics2D g,String s,int x,int y,int width,int step) {
        FontMetrics fm=g.getFontMetrics();
        StringBuilder line=new StringBuilder();
        int yy=y;
        for(String word:s.split(" ")) {
            String candidate=line.length()==0?word:line+" "+word;
            if(fm.stringWidth(candidate)>width&&line.length()>0) {
                g.drawString(line.toString(),x,yy);
                yy+=step;
                line=new StringBuilder(word);
            }
            else line=new StringBuilder(candidate);
        }
        g.drawString(line.toString(),x,yy);
    }
}
