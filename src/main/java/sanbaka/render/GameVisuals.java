package sanbaka.render;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
import sanbaka.assets.ArtAssets;
import sanbaka.logic.GameLogic;
import sanbaka.model.Bird;
import sanbaka.model.Confetti;

/** Lớp hỗ trợ vẽ các chi tiết đồ họa thủ công bằng Java 2D Graphics. */
public class GameVisuals extends GameLogic {
    private static final Color GINGER_C1 = new Color(255, 105, 90, 48);
    private static final Color GINGER_C2 = new Color(255, 90, 75, 92);
    private static final Color GINGER_C3 = new Color(245, 75, 55, 160);
    private static final Color GINGER_C4 = new Color(220, 45, 35, 230);
    private static final Color GINGER_C5 = new Color(255, 225, 120, 245);
    private static final Color GINGER_STEAM_C = new Color(255, 240, 225, 175);
    private static final BasicStroke GINGER_STEAM_STROKE = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke STROKE_RESET_1F = new BasicStroke(1f);

    private static final Color SHIELD_AURA_FILL = new Color(60, 215, 255, 45);
    private static final Color SHIELD_AURA_BORDER = new Color(110, 235, 255, 180);
    private static final Color SHIELD_AURA_ARC = new Color(200, 250, 255, 210);
    private static final BasicStroke SHIELD_BORDER_STROKE = new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke SHIELD_ARC_STROKE = new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private static final Map<String, Optional<BufferedImage>> AVATAR_IMAGES = new HashMap<>();
    private static final Map<Integer, BufferedImage> AVATAR_CIRCLE_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    private BufferedImage loadAvatarImage(String name) {
        synchronized (AVATAR_IMAGES) {
            return AVATAR_IMAGES.computeIfAbsent(name, key -> {
                // 1. Nạp trực tiếp từ thư mục assets/avatars ngoài cùng của dự án
                Path cwd = Paths.get("").toAbsolutePath();
                for (Path dir = cwd; dir != null; dir = dir.getParent()) {
                    Path imageDir = dir.resolve("assets/avatars");
                    for (String extension : new String[]{".png", ".jpg", ".jpeg"}) {
                        Path imagePath = imageDir.resolve(key + extension);
                        if (!Files.isRegularFile(imagePath)) continue;
                        try {
                            BufferedImage image = ImageIO.read(imagePath.toFile());
                            if (image != null) return Optional.of(image);
                        } catch (IOException | RuntimeException ignored) {
                            // Ảnh thiếu hoặc không đọc được thì tiếp tục dùng avatar vẽ bằng Java.
                        }
                    }
                }
                // 2. Fallback qua ClassLoader resource
                for (String prefix : new String[]{"/avatars/", "/assets/avatars/"}) {
                    for (String extension : new String[]{".png", ".jpg", ".jpeg"}) {
                        try (InputStream in = GameVisuals.class.getResourceAsStream(prefix + key + extension)) {
                            if (in != null) {
                                BufferedImage img = ImageIO.read(in);
                                if (img != null) return Optional.of(img);
                            }
                        } catch (Exception ignored) {}
                    }
                }
                return Optional.empty();
            }).orElse(null);
        }
    }

    public void drawConfetti(Graphics2D g, long now) {
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

    public void drawPopper(Graphics2D g, double x, double y, double rot) {
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

    public void drawBoatWake(Graphics2D g, double sternY, long now) {
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

    public void callIcon(Graphics2D g, int type, double cx, double cy, Color c) {
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

    public void panel(Graphics2D g, int x, int y, int w, int h, Color c) {
        g.setColor(new Color(27, 36, 53, 35));
        g.fill(new RoundRectangle2D.Double(x + 5, y + 7, w, h, 24, 24));
        g.setColor(c);
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 24, 24));
    }

    public void drawCurtain(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(new Color(244, 170, 190));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 18, 18));
        g.setColor(new Color(224, 130, 158));
        for (int i = 0; i < 3; i++) g.fill(new RoundRectangle2D.Double(x + 8 + i * 14, y + 8, 5, h - 18, 4, 4));
    }

    public void drawBolt(Graphics2D g, double x, double y, double sc) {
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

    public void drawGingerTea(Graphics2D g, double x, double y, double sc) {
        AffineTransform old = g.getTransform();
        g.translate(x, y);
        g.scale(sc, sc);
        long now = System.currentTimeMillis();

        // Hình tròn đồng tâm đỏ nhạt dần từ lõi ra viền ngoài
        // Vòng 1 (ngoài cùng, đỏ rất nhạt): r=22
        g.setColor(GINGER_C1);
        g.fill(new Ellipse2D.Double(-22, -22, 44, 44));

        // Vòng 2 (giữa ngoài, đỏ nhạt): r=17
        g.setColor(GINGER_C2);
        g.fill(new Ellipse2D.Double(-17, -17, 34, 34));

        // Vòng 3 (giữa trong, đỏ cam ấm): r=12
        g.setColor(GINGER_C3);
        g.fill(new Ellipse2D.Double(-12, -12, 24, 24));

        // Vòng 4 (lõi trà gừng ấm nóng, đỏ đậm): r=7
        g.setColor(GINGER_C4);
        g.fill(new Ellipse2D.Double(-7, -7, 14, 14));

        // Điểm nhấn vàng gừng ở tâm
        g.setColor(GINGER_C5);
        g.fill(new Ellipse2D.Double(-3, -3, 6, 6));

        // Hơi nước bốc lên ấm áp
        g.setColor(GINGER_STEAM_C);
        g.setStroke(GINGER_STEAM_STROKE);
        double steamOffset = (now % 1000) / 1000.0;
        Path2D.Double steam1 = new Path2D.Double();
        steam1.moveTo(-4, -10 - steamOffset * 8);
        steam1.curveTo(-8, -16 - steamOffset * 8, 0, -22 - steamOffset * 8, -4, -28 - steamOffset * 8);
        g.draw(steam1);

        Path2D.Double steam2 = new Path2D.Double();
        steam2.moveTo(4, -12 - steamOffset * 8);
        steam2.curveTo(0, -18 - steamOffset * 8, 8, -24 - steamOffset * 8, 4, -30 - steamOffset * 8);
        g.draw(steam2);

        g.setStroke(STROKE_RESET_1F);
        g.setTransform(old);
    }

    public void drawShieldItem(Graphics2D g, double x, double y, double sc) {
        AffineTransform old = g.getTransform();
        g.translate(x, y);
        g.scale(sc, sc);

        // Vầng hào quang bảo hộ xanh lam
        g.setColor(new Color(50, 200, 255, 55));
        g.fill(new Ellipse2D.Double(-24, -24, 48, 48));
        g.setColor(new Color(80, 225, 255, 95));
        g.fill(new Ellipse2D.Double(-17, -17, 34, 34));

        // Hình chiếc khiên hộ mệnh
        Path2D.Double shield = new Path2D.Double();
        shield.moveTo(0, -14);
        shield.lineTo(13, -8);
        shield.lineTo(11, 4);
        shield.quadTo(8, 14, 0, 18);
        shield.quadTo(-8, 14, -11, 4);
        shield.lineTo(-13, -8);
        shield.closePath();

        g.setColor(new Color(40, 170, 240, 220));
        g.fill(shield);
        g.setColor(new Color(200, 245, 255, 250));
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(shield);

        // Biểu tượng chữ thập năng lượng ở giữa khiên
        g.setColor(new Color(255, 255, 255, 240));
        g.fill(new Rectangle2D.Double(-1.5, -7, 3, 14));
        g.fill(new Rectangle2D.Double(-6, -2.5, 12, 3));

        g.setStroke(new BasicStroke(1f));
        g.setTransform(old);
    }

    public void drawShieldAura(Graphics2D g, double px, double py, long now) {
        AffineTransform old = g.getTransform();
        g.translate(px, py);
        double pulse = 1.0 + 0.05 * Math.sin(now / 150.0);
        g.scale(pulse, pulse);

        // Bong bóng năng lượng bảo hộ bao quanh xe
        g.setColor(SHIELD_AURA_FILL);
        g.fill(new Ellipse2D.Double(-34, -42, 68, 84));

        // Viền khiên năng lượng phát sáng
        g.setColor(SHIELD_AURA_BORDER);
        g.setStroke(SHIELD_BORDER_STROKE);
        g.draw(new Ellipse2D.Double(-34, -42, 68, 84));

        // Các cung năng lượng xoay quanh khiên
        double rot = (now / 220.0) % (2 * Math.PI);
        g.setColor(SHIELD_AURA_ARC);
        g.setStroke(SHIELD_ARC_STROKE);
        g.draw(new Arc2D.Double(-36, -44, 72, 88, Math.toDegrees(rot), 55, Arc2D.OPEN));
        g.draw(new Arc2D.Double(-36, -44, 72, 88, Math.toDegrees(rot + Math.PI), 55, Arc2D.OPEN));

        g.setStroke(STROKE_RESET_1F);
        g.setTransform(old);
    }

    public void poly(Graphics2D g, double... p) {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(p[0], p[1]);
        for (int i = 2; i + 1 < p.length; i += 2) path.lineTo(p[i], p[i + 1]);
        path.closePath();
        g.fill(path);
    }

    public void drawHeart(Graphics2D g, double cx, double cy, double s, Color c) {
        Path2D.Double h = new Path2D.Double();
        h.moveTo(cx, cy + s * 0.42);
        h.curveTo(cx - s * 0.75, cy - s * 0.05, cx - s * 0.4, cy - s * 0.6, cx, cy - s * 0.2);
        h.curveTo(cx + s * 0.4, cy - s * 0.6, cx + s * 0.75, cy - s * 0.05, cx, cy + s * 0.42);
        g.setColor(c);
        g.fill(h);
    }

    public void drawStar(Graphics2D g, double cx, double cy, int points, double rOuter, double rInner) {
        Path2D.Double star = new Path2D.Double();
        double angleStep = Math.PI / points;
        for (int i = 0; i < 2 * points; i++) {
            double r = (i % 2 == 0) ? rOuter : rInner;
            double a = i * angleStep - Math.PI / 2.0;
            double x = cx + r * Math.cos(a);
            double y = cy + r * Math.sin(a);
            if (i == 0) star.moveTo(x, y);
            else star.lineTo(x, y);
        }
        star.closePath();
        g.fill(star);
    }

    public void drawAvatar(Graphics2D g, String who, int cx, int cy, int r) {
        int kind = who.startsWith("Sanbaka") ? 0
                : who.startsWith("Thắng") ? 1
                : who.startsWith("Komori") ? 2
                : (who.startsWith("Chủ trọ") || who.startsWith("Người chủ trì")) ? 3 : 4;
        int cacheKey = (kind << 16) | (r & 0xFFFF);
        BufferedImage cached = AVATAR_CIRCLE_CACHE.get(cacheKey);
        int pad = 4;
        if (cached == null) {
            int size = (r + pad) * 2;
            cached = ArtAssets.createCompatibleImage(size, size);
            Graphics2D sg = cached.createGraphics();
            sg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            renderAvatarCircle(sg, kind, r + pad, r + pad, r);
            sg.dispose();
            AVATAR_CIRCLE_CACHE.put(cacheKey, cached);
        }
        g.drawImage(cached, cx - r - pad, cy - r - pad, null);
    }

    private void renderAvatarCircle(Graphics2D g, int kind, int cx, int cy, int r) {
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
        String avatarName = kind == 0 ? "Sanbaka" : kind == 1 ? "Thắng"
                : kind == 2 ? "Komori" : kind == 3 ? "Chủ trọ" : "Other";
        BufferedImage avatarImage = loadAvatarImage(avatarName);
        if (avatarImage != null) {
            g.drawImage(avatarImage, cx - r, cy - r, 2 * r, 2 * r, null);
            g.setTransform(oldT);
            g.setClip(oldClip);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(Math.max(1.5f, r / 14f)));
            g.draw(circle);
            g.setStroke(oldStroke);
            return;
        }
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

    public void drawBird(Graphics2D g, double x, double y, double phase) {
        BufferedImage sprite = ArtAssets.load("Bird", "Bird.png");
        if (sprite != null) {
            AffineTransform old = g.getTransform();
            g.translate(x, y + Math.sin(phase) * 2.5);
            g.drawImage(sprite, -30, -24, 60, 48, null);
            g.setTransform(old);
            return;
        }
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

    /**
     * Vẽ hiệu ứng xe bốc lửa khi đi sát lề đường quá 1.5 giây (hoặc tia lửa ma sát trước khi bốc cháy).
     * Hỗ trợ đội hình ảnh thay thế bằng file PNG riêng tại: assets/objects/VehicleFire/VehicleFire.png
     */
    public void drawVehicleFireEffect(Graphics2D g, double cx, double cy, double curbTime, boolean onFire, long now) {
        if (curbTime <= 0.15) return;
        double ratio = Math.max(0.0, Math.min(1.0, curbTime / CURB_FIRE_THRESHOLD_SEC));
        AffineTransform old = g.getTransform();
        Stroke oldStroke = g.getStroke();
        Composite oldComp = g.getComposite();

        if (!onFire) {
            // Giai đoạn ma sát lề đường (< 3 giây): tia lửa xẹt dưới bánh xe và khói nóng
            int sideDir = cx < W / 2.0 ? -1 : 1;
            double sparkX = cx + sideDir * 14;
            g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < 6; i++) {
                double phase = (now / 45.0 + i * 1.1) % 1.0;
                double sx = sparkX - sideDir * phase * 14 + Math.sin(now / 30.0 + i) * 4;
                double sy = cy - 10 + i * 7 + phase * 18;
                int alpha = (int) (220 * ratio * (1.0 - phase));
                g.setColor(new Color(255, 180 + (i % 2) * 60, 40, Math.max(0, Math.min(255, alpha))));
                g.draw(new Line2D.Double(sx, sy, sx - sideDir * 5, sy + 9));
            }
            g.setStroke(oldStroke);
            return;
        }

        // Giai đoạn bốc lửa (đốt 10 HP/s, và giảm dần kích thước/alpha khi người chơi thoát khỏi lề đường)
        float alpha = (float) Math.max(0.18, Math.min(1.0, 0.25 + 0.75 * ratio));
        double scale = 0.45 + 0.60 * ratio + Math.sin(now / 42.0) * 0.06;
        BufferedImage fireSprite = ArtAssets.load("VehicleFire", "VehicleFire.png");
        if (fireSprite != null) {
            g.setComposite(AlphaComposite.SrcOver.derive(alpha));
            g.translate(cx, cy);
            g.scale(scale, scale);
            g.drawImage(fireSprite, -38, -54, 76, 96, null);
            g.setTransform(old);
            g.setComposite(oldComp);
            return;
        }

        g.setComposite(AlphaComposite.SrcOver.derive(alpha));
        g.translate(cx, cy + 8);
        g.scale(scale, scale);

        // Khói đen bốc lên phía trên ngọn lửa
        for (int i = 0; i < 4; i++) {
            double p = ((now / 340.0) + i * 0.25) % 1.0;
            double kx = Math.sin(now / 120.0 + i * 2) * 10;
            double ky = -28 - p * 42;
            double kr = 8 + p * 12;
            g.setColor(new Color(45, 45, 52, (int) (135 * (1.0 - p))));
            g.fill(new Ellipse2D.Double(kx - kr, ky - kr, kr * 2, kr * 1.6));
        }

        // Ngọn lửa 3 lớp (Đỏ cam ngoài -> Cam rực giữa -> Vàng trắng lõi)
        double wave1 = Math.sin(now / 48.0) * 6;
        double wave2 = Math.cos(now / 38.0) * 5;
        Path2D.Double outerFlame = new Path2D.Double();
        outerFlame.moveTo(0, 26);
        outerFlame.curveTo(-30, 18, -32, -16, -12 + wave1 * 0.5, -36);
        outerFlame.lineTo(-4, -18);
        outerFlame.lineTo(wave1, -52);
        outerFlame.lineTo(8, -20);
        outerFlame.lineTo(16 + wave2 * 0.5, -38);
        outerFlame.curveTo(32, -14, 30, 18, 0, 26);
        outerFlame.closePath();
        g.setColor(new Color(235, 52, 24, 225));
        g.fill(outerFlame);

        Path2D.Double midFlame = new Path2D.Double();
        midFlame.moveTo(0, 22);
        midFlame.curveTo(-20, 14, -22, -10, -7 + wave2 * 0.4, -28);
        midFlame.lineTo(wave2, -40);
        midFlame.lineTo(9 - wave1 * 0.4, -26);
        midFlame.curveTo(22, -10, 20, 14, 0, 22);
        midFlame.closePath();
        g.setColor(new Color(255, 142, 24, 235));
        g.fill(midFlame);

        Path2D.Double coreFlame = new Path2D.Double();
        coreFlame.moveTo(0, 18);
        coreFlame.curveTo(-11, 10, -11, -6, wave1 * 0.3, -24);
        coreFlame.curveTo(11, -6, 11, 10, 0, 18);
        coreFlame.closePath();
        g.setColor(new Color(255, 238, 115, 240));
        g.fill(coreFlame);

        g.setTransform(old);
        g.setComposite(oldComp);
        g.setStroke(oldStroke);
    }

    /**
     * Vẽ hiệu ứng bọt nước và tia nước tóe lên khi xuồng máy cọ sát lề đường ngập nước ở Màn 2.
     */
    public void drawBoatCurbSplash(Graphics2D g, double cx, double cy, double intensity, long now) {
        Stroke oldStroke = g.getStroke();
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int sideDir = cx < W / 2.0 ? -1 : 1;
        for (int i = 0; i < 5; i++) {
            double ph = ((now / 130.0) + i * 0.22) % 1.0;
            double sx = cx + sideDir * (ph * 16 + Math.sin(now / 35.0 + i) * 3);
            double sy = cy - 8 + i * 6 + ph * 14;
            int alpha = (int) (210 * intensity * (1.0 - ph));
            g.setColor(new Color(235, 248, 255, Math.max(0, Math.min(255, alpha))));
            g.draw(new Line2D.Double(sx, sy, sx + sideDir * 6, sy - 8));
            g.fill(new Ellipse2D.Double(sx - 2, sy - 2, 4, 4));
        }
        g.setStroke(oldStroke);
    }

    /**
     * Vẽ hiệu ứng chấn động đẩy lùi & làm chậm ngắn sau khi đâm xe.
     * Hỗ trợ đội hình ảnh thay thế bằng file PNG riêng tại: assets/objects/CollisionImpact/CollisionImpact.png
     */
    public void drawCollisionSlowEffect(Graphics2D g, double cx, double cy, long now) {
        if (now >= hitSlowUntil) return;
        BufferedImage impactSprite = ArtAssets.load("CollisionImpact", "CollisionImpact.png");
        if (impactSprite != null) {
            AffineTransform old = g.getTransform();
            g.translate(cx, cy);
            g.drawImage(impactSprite, -36, -44, 72, 88, null);
            g.setTransform(old);
            return;
        }
        Stroke oldStroke = g.getStroke();
        long sinceHit = now - lastHitAt;
        if (sinceHit >= 0 && sinceHit < 320) {
            double p = sinceHit / 320.0;
            double r = 18 + p * 34;
            g.setColor(new Color(255, 210, 70, (int) (210 * (1.0 - p))));
            g.setStroke(new BasicStroke(3.0f * (float) (1.0 - p) + 1.0f));
            g.draw(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
        }
        // Vệt khói xám giảm tốc quanh xe khi đang bị làm chậm ngắn
        for (int i = 0; i < 3; i++) {
            double ph = ((now / 260.0) + i * 0.33) % 1.0;
            double sx = cx + (i - 1) * 14;
            double sy = cy + 16 + ph * 22;
            double sr = 6 + ph * 7;
            g.setColor(new Color(90, 96, 108, (int) (130 * (1.0 - ph))));
            g.fill(new Ellipse2D.Double(sx - sr, sy - sr, sr * 2, sr * 2));
        }
        g.setStroke(oldStroke);
    }

    /**
     * Vẽ hiệu ứng đóng băng xe ở Màn 3 (Trời rét).
     * Hỗ trợ đội hình ảnh thay thế bằng file PNG riêng tại: assets/objects/FreezeEffect/FreezeEffect.png
     */
    public void drawFreezeEffect(Graphics2D g, double cx, double cy, long now) {
        if (now >= frozenUntil) return;
        BufferedImage freezeSprite = ArtAssets.load("FreezeEffect", "FreezeEffect.png");
        if (freezeSprite != null) {
            g.drawImage(freezeSprite, (int) Math.round(cx - 28), (int) Math.round(cy - 44), 56, 88, null);
            return;
        }
        g.setColor(new Color(145, 220, 255, 110));
        g.fill(new Ellipse2D.Double(cx - 24, cy - 42, 48, 84));
        g.setColor(new Color(225, 248, 255, 210));
        g.setStroke(new BasicStroke(2.0f));
        g.draw(new Ellipse2D.Double(cx - 24, cy - 42, 48, 84));
        g.setStroke(new BasicStroke(1f));
    }

    public void drawBird(Graphics2D g, Bird b) {
        drawBird(g, b.x, b.y, b.phase);
    }

    public void drawHelicopter(Graphics2D g, double cx, double cy, double tilt, double time, double sc) {
        drawHelicopter(g, cx, cy, tilt, time, sc, true);
    }

    public void drawHelicopter(Graphics2D g, double cx, double cy, double tilt, double time, double sc, boolean showThang) {
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
        if (showThang) drawAvatar(g, "Thắng", -19, -8, 9);
        drawAvatar(g, "Komori", 30, -6, 11);
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

    public void drawCloud(Graphics2D g, double x, double y, double s, int a) {
        g.setColor(new Color(255, 255, 255, a));
        g.fill(new Ellipse2D.Double(x, y, 110 * s, 34 * s));
        g.fill(new Ellipse2D.Double(x + 20 * s, y - 18 * s, 56 * s, 42 * s));
        g.fill(new Ellipse2D.Double(x + 54 * s, y - 10 * s, 48 * s, 36 * s));
    }

    public int hash(int a, int b) {
        int h = a * 73856093 ^ b * 19349663 ^ 0x5bd1e995;
        h ^= h >>> 13;
        h *= 0x5bd1e995;
        h ^= h >>> 15;
        return h & 0x7fffffff;
    }

    public double finishY() {
        if (day == 4) return -9999;
        double r = Math.max(0, (getStageDurationMs() - stageProgressMs) / 1000.0);
        if (r >= SCHOOL_APPROACH) return -9999;
        double base = (3.6 + day * 0.32) * 60;
        if (day == HOT_DAY) base *= 1.25;
        return FINISH_Y - base * (0.2 * r + 0.4 * r * r / SCHOOL_APPROACH);
    }

    public Color shade(Color c, double k) {
        return new Color(Math.max(0, Math.min(255, (int) (c.getRed() * k))),
                Math.max(0, Math.min(255, (int) (c.getGreen() * k))),
                Math.max(0, Math.min(255, (int) (c.getBlue() * k))), c.getAlpha());
    }

    public void drawWrapped(Graphics2D g, String s, int x, int y, int width, int step) {
        FontMetrics fm = g.getFontMetrics();
        StringBuilder line = new StringBuilder();
        int yy = y;
        for (String word : s.split(" ")) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(candidate) > width && line.length() > 0) {
                g.drawString(line.toString(), x, yy);
                yy += step;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(candidate);
            }
        }
        g.drawString(line.toString(), x, yy);
    }
}

