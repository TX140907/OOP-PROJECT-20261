package sanbaka.render;

import sanbaka.assets.ArtAssets;
import sanbaka.model.*;
import sanbaka.state.GameState;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.*;
import javax.swing.*;

public class GameEnvironmentRenderer extends GameVisuals {
    private static final int LOT_VARIANTS_PER_DAY = 28;
    private static final int LOT_CACHE_LIMIT = 320;
    private static final int TREE_CACHE_LIMIT = 128;
    private static final int WATER_RIPPLE_ALPHA_BUCKETS = 16;
    private static final long FLOOD_LAYER_REFRESH_MS = 40;
    private final Path2D.Double waterStreakPath = new Path2D.Double();
    private final Path2D.Double waterWavePath = new Path2D.Double();
    private final Path2D.Double[] waterRipplePaths = new Path2D.Double[WATER_RIPPLE_ALPHA_BUCKETS];
    private BufferedImage floodLayerCache;
    private long floodLayerUpdatedAt = Long.MIN_VALUE;
    private final Map<Long, BufferedImage> lotImageCache = new LinkedHashMap<>(LOT_CACHE_LIMIT, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, BufferedImage> eldest) {
            return size() > LOT_CACHE_LIMIT;
        }
    };
    private final Map<Integer, BufferedImage> treeImageCache = new LinkedHashMap<>(TREE_CACHE_LIMIT, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, BufferedImage> eldest) {
            return size() > TREE_CACHE_LIMIT;
        }
    };
    private final BufferedImage[] lampImageCache = new BufferedImage[2];

    GameEnvironmentRenderer() {
        for (int i = 0; i < waterRipplePaths.length; i++) waterRipplePaths[i] = new Path2D.Double();
    }

    void drawFlood(Graphics2D g, int rx0, int rx1, double s, long now) {
        if (floodLayerCache == null) floodLayerCache = ArtAssets.createCompatibleImage(W, H);
        if (floodLayerUpdatedAt == Long.MIN_VALUE || now - floodLayerUpdatedAt >= FLOOD_LAYER_REFRESH_MS) {
            Graphics2D layerGraphics = floodLayerCache.createGraphics();
            clearAndRenderFloodLayer(layerGraphics, rx0, rx1, s, now);
            floodLayerUpdatedAt = now;
        }
        g.drawImage(floodLayerCache, rx0, 0, rx1, H, rx0, 0, rx1, H, null);
    }

    private void clearAndRenderFloodLayer(Graphics2D layerGraphics, int rx0, int rx1, double s, long now) {
        layerGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        layerGraphics.setComposite(AlphaComposite.Clear);
        layerGraphics.fillRect(0, 0, W, H);
        layerGraphics.setComposite(AlphaComposite.SrcOver);
        renderFloodLayer(layerGraphics, rx0, rx1, s, now);
        layerGraphics.dispose();
    }

    private void renderFloodLayer(Graphics2D g, int rx0, int rx1, double s, long now) {
        int rw = rx1 - rx0;
        g.setPaint(new GradientPaint(rx0, 0, new Color(126, 112, 86, 234), rx1, 0, new Color(112, 100, 80, 234)));
        g.fillRect(rx0, 0, rw, H);
        Shape oldClip = g.getClip();
        g.clipRect(rx0, 0, rw, H);
        Stroke oldStroke = g.getStroke();
        // dòng nước chảy dọc theo đường
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        waterStreakPath.reset();
        for (int n = (int) Math.floor((s - H) / 120.0) - 1; n <= (int) Math.floor(s / 120.0) + 1; n++) {
            for (int k = 0; k < 3; k++) {
                int h = hash(n, 40 + k);
                double x = rx0 + 10 + h % (rw - 20);
                double y = s - (n + 1) * 120.0 + (h / 7) % 120;
                double len = 40 + h % 60;
                waterStreakPath.moveTo(x + Math.sin(now / 500.0 + h) * 3, y);
                waterStreakPath.lineTo(x, y + len);
            }
        }
        g.setColor(new Color(255, 244, 220, 40));
        g.draw(waterStreakPath);
        // sóng nước
        g.setStroke(new BasicStroke(2f));
        waterWavePath.reset();
        for (double y = ((s * 0.8) % 46) - 46; y < H; y += 46) {
            waterWavePath.moveTo(rx0, y);
            for (int x = rx0; x <= rx1; x += 16) waterWavePath.lineTo(x, y + Math.sin(now / 420.0 + x * 0.045 + y * 0.1) * 4);
        }
        g.setColor(new Color(255, 255, 255, 34));
        g.draw(waterWavePath);
        // gợn nước do mưa rơi
        g.setStroke(new BasicStroke(1.2f));
        for (Path2D.Double ripplePath : waterRipplePaths) ripplePath.reset();
        for (int i = 0; i < 70; i++) {
            double cyc = (now / 800.0 + i * 0.371) % 1.0;
            int gen = (int) Math.floor(now / 800.0 + i * 0.371);
            int h = hash(gen, i);
            double x = rx0 + 8 + h % (rw - 16), y = (h / 13) % H;
            double rr = cyc * 13;
            int alpha = (int) (150 * (1 - cyc));
            int bucket = Math.min(WATER_RIPPLE_ALPHA_BUCKETS - 1, alpha * WATER_RIPPLE_ALPHA_BUCKETS / 151);
            waterRipplePaths[bucket].append(new Ellipse2D.Double(x - rr, y - rr * 0.6, 2 * rr, 1.2 * rr), false);
        }
        for (int i = 0; i < waterRipplePaths.length; i++) {
            Path2D.Double ripplePath = waterRipplePaths[i];
            if (ripplePath.getCurrentPoint() == null) continue;
            int alpha = Math.min(150, (i * 151 + WATER_RIPPLE_ALPHA_BUCKETS / 2) / WATER_RIPPLE_ALPHA_BUCKETS);
            g.setColor(new Color(235, 240, 245, alpha));
            g.draw(ripplePath);
        }
        // rác, lá cây nổi trên nước
        for (int n = (int) Math.floor((s - H) / 160.0) - 1; n <= (int) Math.floor(s / 160.0) + 1; n++) {
            int h = hash(n, 55);
            if (h % 3 != 0) continue;
            double x = rx0 + 20 + (h / 3) % (rw - 40);
            double y = s - (n + 1) * 160.0 + (h / 11) % 160 + Math.sin(now / 600.0 + h) * 3;
            AffineTransform t0 = g.getTransform();
            g.translate(x, y);
            g.rotate((h % 628) / 100.0);
            if (h % 2 == 0) {
                g.setColor(new Color(86, 128, 70));
                g.fill(new Ellipse2D.Double(-8, -4, 16, 8));
                g.setColor(new Color(60, 96, 50));
                g.draw(new Line2D.Double(-8, 0, 8, 0));
            } else {
                g.setColor(new Color(225, 235, 245, 210));
                g.fill(new RoundRectangle2D.Double(-3, -9, 6, 18, 3, 3));
                g.setColor(new Color(81, 146, 185));
                g.fill(new Rectangle2D.Double(-2, -11, 4, 3));
            }
            g.setTransform(t0);
        }
        g.setClip(oldClip);
        // bọt nước sát vỉa hè
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int side = 0; side < 2; side++) {
            Path2D.Double foam = new Path2D.Double();
            double bx = side == 0 ? rx0 + 2 : rx1 - 2;
            foam.moveTo(bx, 0);
            for (int y = 0; y <= H; y += 14) foam.lineTo(bx + Math.sin(y * 0.06 + now / 400.0 + side * 2) * 2.5, y);
            g.setColor(new Color(244, 240, 228, 150));
            g.draw(foam);
        }
        g.setStroke(oldStroke);
    }

    void drawWindowView(Graphics2D g, int x, int y, int w, int h, int d, long now) {
        Color[] tops = {
            new Color(120, 190, 240), new Color(86, 106, 136), new Color(190, 205, 222),
            new Color(255, 170, 90), new Color(160, 196, 230)
        };
        Color[] bots = {
            new Color(200, 232, 250), new Color(130, 148, 172), new Color(226, 234, 242),
            new Color(255, 226, 160), new Color(222, 236, 248)
        };
        g.setPaint(new GradientPaint(x, y, tops[d], x, y + h, bots[d]));
        g.fillRect(x, y, w, h);
        g.setColor(new Color(150, 168, 196, d == 1 ? 200 : 150));
        for (int i = 0; i < 7; i++) g.fillRect(x + i * 30 - 8, y + h - (34 + (i * 37) % 46), 26, 90);
        if (d == 0 || d == 4) {
            drawCloud(g, x + 20 + (now / 90.0) % 40, y + 28, 0.7, 230);
            drawCloud(g, x + 110, y + 62, 0.5, 200);
        }
        if (d == 0) {
            g.setColor(new Color(255, 238, 160));
            g.fill(new Ellipse2D.Double(x + w - 62, y + 10, 40, 40));
        }
        if (d == 1) {
            g.setColor(new Color(92, 100, 120, 230));
            g.fill(new Ellipse2D.Double(x - 20, y - 24, 140, 56));
            g.fill(new Ellipse2D.Double(x + 80, y - 30, 150, 60));
            g.setColor(new Color(205, 228, 255, 190));
            for (int i = 0; i < 26; i++) {
                double rx = x + (i * 37 + now / 6.0) % (w + 20);
                double ry = y + (i * 53 + now / 3.0) % (h + 20);
                g.draw(new Line2D.Double(rx, ry, rx - 4, ry + 12));
            }
        }
        if (d == 2) {
            g.setColor(new Color(255, 255, 255, 170));
            for (int i = 0; i < 18; i++) {
                double sx = x + (i * 41 + Math.sin(now / 700.0 + i) * 8 + w) % w;
                double sy = y + (i * 29 + now / 25.0) % h;
                g.fill(new Ellipse2D.Double(sx, sy, 4, 4));
            }
            g.setColor(new Color(255, 255, 255, 120));
            poly(g, x, y, x + 46, y, x, y + 40);
            poly(g, x + w, y + h, x + w - 52, y + h, x + w, y + h - 46);
        }
        if (d == 3) {
            for (int i = 3; i >= 0; i--) {
                double r = 26 + i * 14;
                g.setColor(new Color(255, 240, 160, 40 + (3 - i) * 40));
                g.fill(new Ellipse2D.Double(x + w * 0.7 - r, y + 36 - r, 2 * r, 2 * r));
            }
        }
    }

    void drawLamp(Graphics2D g, double x, double y, int dir) {
        int cacheIndex = dir < 0 ? 0 : 1;
        BufferedImage sprite = lampImageCache[cacheIndex];
        if (sprite == null) {
            sprite = ArtAssets.createCompatibleImage(96, 48);
            Graphics2D spriteGraphics = sprite.createGraphics();
            spriteGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawLampArt(spriteGraphics, 48, 24, cacheIndex == 0 ? -1 : 1);
            spriteGraphics.dispose();
            lampImageCache[cacheIndex] = sprite;
        }
        g.drawImage(sprite, (int) (x - 48), (int) (y - 24), null);
    }

    private void drawLampArt(Graphics2D g, double x, double y, int dir) {
        g.setColor(new Color(255, 243, 190, 60));
        g.fill(new Ellipse2D.Double(x + dir * 24 - 16, y - 16, 32, 32));
        g.setColor(new Color(70, 76, 90));
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(x, y, x + dir * 22, y));
        g.setStroke(new BasicStroke(1f));
        g.fill(new Ellipse2D.Double(x - 4.5, y - 4.5, 9, 9));
        g.setColor(new Color(255, 243, 190));
        g.fill(new Ellipse2D.Double(x + dir * 24 - 6, y - 4, 12, 8));
    }

    void drawTree(Graphics2D g, double x, double y, double r, int variant) {
        int radius = (int) Math.round(r);
        int visualVariant = Math.floorMod(variant, 14);
        int key = (day << 16) | (radius << 8) | visualVariant;
        BufferedImage sprite = treeImageCache.get(key);
        if (sprite == null) {
            int padding = 8, size = radius * 2 + padding * 2;
            sprite = ArtAssets.createCompatibleImage(size, size);
            Graphics2D spriteGraphics = sprite.createGraphics();
            spriteGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawTreeArt(spriteGraphics, radius + padding, radius + padding, radius, visualVariant);
            spriteGraphics.dispose();
            treeImageCache.put(key, sprite);
        }
        int center = radius + 8;
        g.drawImage(sprite, (int) (x - center), (int) (y - center), null);
    }

    private void drawTreeArt(Graphics2D g, double x, double y, double r, int variant) {
        Color[][] pal = {
            {new Color(52, 122, 82), new Color(72, 150, 98), new Color(118, 188, 124)},
            {new Color(98, 128, 64), new Color(124, 156, 80), new Color(166, 190, 108)},
            {new Color(226, 132, 168), new Color(244, 160, 190), new Color(255, 208, 224)}
        };
        Color[] c = pal[(variant % 7 == 0 ? 2 : variant % 2) % 3];
        if (day == 1) c = new Color[]{shade(c[0], 0.8), shade(c[1], 0.8), shade(c[2], 0.8)};
        g.setColor(new Color(0, 0, 0, 45));
        g.fill(new Ellipse2D.Double(x - r + 5, y - r + 8, 2 * r, 2 * r));
        g.setColor(c[0]);
        g.fill(new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r));
        g.setColor(c[1]);
        g.fill(new Ellipse2D.Double(x - r * 0.9, y - r * 0.92, r * 1.64, r * 1.64));
        g.setColor(c[2]);
        g.fill(new Ellipse2D.Double(x - r * 0.55, y - r * 0.62, r * 0.7, r * 0.6));
        if (day == 2) {
            g.setColor(new Color(244, 248, 252, 190));
            g.fill(new Ellipse2D.Double(x - r * 0.7, y - r * 0.7, r * 1.4, r * 1.2));
        }
    }

    void drawHouse(Graphics2D g, double x, double y, double w, double h, Color roof) {
        g.setColor(new Color(0, 0, 0, 40));
        g.fill(new RoundRectangle2D.Double(x - 7, y + 8, w, h, 8, 8));
        g.setColor(shade(roof, 1.12));
        g.fill(new RoundRectangle2D.Double(x, y, w / 2, h, 6, 6));
        g.setColor(shade(roof, 0.82));
        g.fill(new RoundRectangle2D.Double(x + w / 2, y, w / 2, h, 6, 6));
        g.setColor(new Color(0, 0, 0, 38));
        for (double ty = y + 8; ty < y + h - 4; ty += 9) g.fill(new Rectangle2D.Double(x + 3, ty, w - 6, 1));
        g.setColor(new Color(255, 255, 255, 150));
        g.fill(new Rectangle2D.Double(x + w / 2 - 2, y, 4, h));
        g.setColor(new Color(110, 86, 76));
        g.fill(new Rectangle2D.Double(x + w * 0.7, y + h * 0.2, 12, 14));
        if (day == 2) {
            g.setColor(new Color(244, 248, 252, 200));
            g.fill(new RoundRectangle2D.Double(x, y, w, h * 0.35, 6, 6));
        }
    }

    void drawCampus(Graphics2D g, double fy) {
        int rx0 = ROAD_X + 45, rx1 = ROAD_X + ROAD_W - 45;
        g.setColor(new Color(132, 184, 120));
        g.fill(new Rectangle2D.Double(0, 0, W, fy));
        for (int i = 0; i < 7; i++) {
            double ty = fy - 50 - i * 76;
            if (ty < -40) break;
            drawTree(g, 40 + (i % 2) * 86, ty, 30, i);
            drawTree(g, W - 40 - (i % 2) * 86, ty, 30, i + 3);
        }
        g.setColor(new Color(226, 216, 194));
        g.fill(new Rectangle2D.Double(ROAD_X, 0, ROAD_W, fy));
        g.setColor(new Color(206, 196, 174));
        for (double y = fy - 24; y > 0; y -= 36) g.fill(new Rectangle2D.Double(ROAD_X, y, ROAD_W, 2));
        SchoolBuilding.draw(g, W / 2.0, fy - 38, 0.9, this);
        for (int side = 0; side < 2; side++) {
            double px = side == 0 ? ROAD_X + 2 : ROAD_X + ROAD_W - 26;
            g.setColor(new Color(236, 230, 218));
            g.fill(new RoundRectangle2D.Double(px, fy - 46, 24, 46, 4, 4));
            g.setColor(new Color(200, 96, 84));
            g.fill(new Ellipse2D.Double(px + 2, fy - 58, 20, 20));
        }
        int cell = 14, cols = (rx1 - rx0) / cell + 1;
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < 2; j++) {
                g.setColor((i + j) % 2 == 0 ? Color.WHITE : INK);
                g.fill(new Rectangle2D.Double(rx0 + i * cell, fy + j * cell, cell, cell));
            }
        }
    }

    void drawLot(Graphics2D g, int side, int n, double y0) {
        int rawHash = hash(n, side + 1);
        int variant = Math.floorMod(rawHash, LOT_VARIANTS_PER_DAY);
        long key = ((long) day << 16) | ((long) (side & 1) << 8) | variant;
        BufferedImage tile = lotImageCache.get(key);
        if (tile == null) {
            tile = ArtAssets.createCompatibleImage(210, 160);
            Graphics2D tileGraphics = tile.createGraphics();
            tileGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (side == 1) {
                tileGraphics.translate(210, 0);
                tileGraphics.scale(-1, 1);
            }
            drawLotContents(tileGraphics, side, variant, 0);
            tileGraphics.dispose();
            lotImageCache.put(key, tile);
        }
        g.drawImage(tile, side == 1 ? W - 210 : 0, (int) y0, null);
    }

    private void drawLotContents(Graphics2D g, int side, int n, double y0) {
        int h = hash(n, side + 1);
        int kind = h % 7;
        int h2 = h / 7;
        Color[] roofs = {
            new Color(196, 92, 84), new Color(120, 150, 190), new Color(92, 148, 120),
            new Color(214, 170, 92), new Color(150, 110, 150), new Color(112, 120, 140)
        };
        Color roof = roofs[h2 % 6];
        g.setColor(new Color(0, 0, 0, 14));
        g.fill(new RoundRectangle2D.Double(188, y0 + 4, 8, 152, 4, 4));
        switch (kind) {
            case 0:
            case 6:
                drawHouse(g, 48 + (h2 % 3) * 8, y0 + 30 + (h2 % 4) * 6, 116, 88, roof);
                g.setColor(new Color(255, 255, 255, 170));
                for (int i = 0; i < 8; i++) g.fill(new Rectangle2D.Double(182, y0 + 14 + i * 17, 3, 9));
                g.setColor(new Color(232, 196, 120, 190));
                g.fill(new Rectangle2D.Double(150, y0 + 118, 38, 6));
                break;
            case 1: {
                double sx = 40, sy = y0 + 36, sw = 140, sh = 76;
                g.setColor(new Color(0, 0, 0, 40));
                g.fill(new RoundRectangle2D.Double(sx - 7, sy + 8, sw, sh, 6, 6));
                g.setColor(new Color(176, 182, 194));
                g.fill(new RoundRectangle2D.Double(sx, sy, sw, sh, 6, 6));
                g.setColor(new Color(140, 146, 158));
                g.draw(new RoundRectangle2D.Double(sx + 3, sy + 3, sw - 6, sh - 6, 4, 4));
                g.setColor(new Color(214, 218, 226));
                g.fill(new Rectangle2D.Double(sx + 14, sy + 12, 26, 20));
                g.fill(new Rectangle2D.Double(sx + 50, sy + 12, 26, 20));
                g.setColor(new Color(110, 120, 140));
                g.fill(new Ellipse2D.Double(sx + 20, sy + 16, 14, 12));
                g.fill(new Ellipse2D.Double(sx + 56, sy + 16, 14, 12));
                Color[] aw = {PINK, new Color(255, 255, 255), MINT, new Color(255, 214, 120)};
                for (int i = 0; i < 6; i++) {
                    g.setColor(i % 2 == 0 ? aw[h2 % 4 == 1 ? 2 : 0] : aw[1]);
                    g.fill(new Rectangle2D.Double(sx + sw, sy + 4 + i * 12, 14, 12));
                }
                break;
            }
            case 2:
                drawTree(g, 150, y0 + 40, 30, h2);
                drawTree(g, 80, y0 + 96, 34, h2 + 1);
                drawTree(g, 160, y0 + 124, 26, h2 + 2);
                g.setColor(new Color(66, 140, 90));
                g.fill(new Ellipse2D.Double(30, y0 + 22, 30, 22));
                g.fill(new Ellipse2D.Double(36, y0 + 134, 28, 20));
                break;
            case 3: {
                g.setColor(new Color(150, 205, 140));
                g.fill(new RoundRectangle2D.Double(14, y0 + 8, 176, 144, 30, 30));
                g.setColor(new Color(214, 204, 180));
                g.fill(new RoundRectangle2D.Double(96, y0 + 8, 10, 144, 4, 4));
                g.setColor(new Color(110, 176, 214));
                g.fill(new Ellipse2D.Double(28, y0 + 40, 60, 40));
                g.setColor(new Color(190, 226, 244));
                g.fill(new Ellipse2D.Double(36, y0 + 46, 28, 12));
                g.setColor(new Color(150, 104, 76));
                g.fill(new RoundRectangle2D.Double(118, y0 + 70, 40, 12, 4, 4));
                Color[] fl = {PINK, new Color(255, 214, 92), Color.WHITE, new Color(170, 120, 220)};
                for (int i = 0; i < 16; i++) {
                    g.setColor(fl[(h2 + i) % 4]);
                    g.fill(new Ellipse2D.Double(24 + (i * 37 + h2) % 150, y0 + 100 + (i * 23 + h2) % 46, 6, 6));
                }
                drawTree(g, 150, y0 + 40, 22, h2);
                break;
            }
            case 4: {
                double ax = 44, ay = y0 + 18, aw2 = 134, ah = 124;
                g.setColor(new Color(0, 0, 0, 44));
                g.fill(new RoundRectangle2D.Double(ax - 8, ay + 9, aw2, ah, 6, 6));
                g.setColor(new Color(196, 200, 210));
                g.fill(new RoundRectangle2D.Double(ax, ay, aw2, ah, 6, 6));
                g.setColor(new Color(150, 156, 170));
                g.setStroke(new BasicStroke(3f));
                g.draw(new RoundRectangle2D.Double(ax + 4, ay + 4, aw2 - 8, ah - 8, 4, 4));
                g.setStroke(new BasicStroke(1f));
                g.setColor(new Color(130, 170, 200));
                g.fill(new Ellipse2D.Double(ax + 14, ay + 16, 32, 32));
                g.setColor(new Color(110, 140, 170));
                g.draw(new Ellipse2D.Double(ax + 14, ay + 16, 32, 32));
                g.setColor(new Color(120, 126, 140));
                g.fill(new Rectangle2D.Double(ax + 70, ay + 14, 40, 30));
                g.setColor(new Color(50, 84, 140));
                for (int i = 0; i < 3; i++) for (int j = 0; j < 2; j++) g.fill(new Rectangle2D.Double(ax + 14 + i * 34, ay + 66 + j * 26, 30, 22));
                break;
            }
            default: {
                Color[] um = {PINK, new Color(255, 214, 92), MINT, new Color(110, 176, 214)};
                for (int i = 0; i < 4; i++) {
                    double ux = 60 + (i % 2) * 74, uy = y0 + 36 + (i / 2) * 78;
                    g.setColor(new Color(0, 0, 0, 40));
                    g.fill(new Ellipse2D.Double(ux - 16, uy - 8, 36, 36));
                    for (int k = 0; k < 6; k++) {
                        g.setColor(k % 2 == 0 ? um[(h2 + i) % 4] : Color.WHITE);
                        g.fill(new Arc2D.Double(ux - 18, uy - 18, 36, 36, k * 60, 60, Arc2D.PIE));
                    }
                    g.setColor(new Color(90, 70, 60));
                    g.fill(new Ellipse2D.Double(ux - 2, uy - 2, 4, 4));
                    g.setColor(new Color(150, 104, 76));
                    g.fill(new Ellipse2D.Double(ux + 22, uy - 6, 12, 12));
                    g.fill(new Ellipse2D.Double(ux - 34, uy - 6, 12, 12));
                }
                break;
            }
        }
    }

    void drawSkyScene(Graphics2D g, double scroll) {
        g.setPaint(new GradientPaint(0, 0, new Color(84, 156, 228), 0, 560, new Color(208, 234, 252)));
        g.fillRect(0, 0, W, H);
        for (int i = 3; i >= 0; i--) {
            double r = 40 + i * 22;
            g.setColor(new Color(255, 244, 196, 30 + (3 - i) * 40));
            g.fill(new Ellipse2D.Double(790 - r, 95 - r, 2 * r, 2 * r));
        }
        double[] cloudSpeed = {0.18, 0.4, 0.8};
        double[] cloudSize = {0.7, 1.0, 1.5};
        int[] cloudAlpha = {120, 170, 225};
        int span = W + 360;
        for (int l = 0; l < 3; l++) {
            for (int i = 0; i < 5; i++) {
                double x = (((i * 264 + l * 110 - scroll * cloudSpeed[l]) % span) + span) % span - 180;
                double y = 50 + ((i * 97 + l * 61) % 300);
                drawCloud(g, x, y, cloudSize[l], cloudAlpha[l]);
            }
        }
        int gy = 556;
        g.setColor(new Color(160, 196, 190));
        Ellipse2D.Double hill = new Ellipse2D.Double();
        hill.setFrame(-120, gy - 40, 520, 120);
        g.fill(hill);
        hill.setFrame(330, gy - 56, 640, 140);
        g.fill(hill);
        Rectangle2D.Double cityRect = new Rectangle2D.Double();
        for (int i = 0; i < 22; i++) {
            double bw = 34 + (i * 29) % 26;
            double bh = 28 + (i * 53) % 52;
            double bx = (((i * 52 - scroll * 0.3) % 1144) + 1144) % 1144 - 60;
            g.setColor(new Color(166, 186, 214));
            cityRect.setRect(bx, gy - bh, bw, bh + 4);
            g.fill(cityRect);
            g.setColor(new Color(214, 228, 244, 200));
            for (int wy = (int) (gy - bh + 8); wy < gy - 6; wy += 12) {
                for (int wx = 5; wx < bw - 6; wx += 11) {
                    cityRect.setRect(bx + wx, wy, 5, 6);
                    g.fill(cityRect);
                }
            }
        }
        g.setColor(new Color(112, 169, 115));
        g.fillRect(0, gy, W, H - gy);
        g.setColor(new Color(90, 100, 111));
        g.fillRect(0, gy + 10, W, 28);
        RoundRectangle2D.Double car = new RoundRectangle2D.Double();
        for (int i = 0; i < 24; i++) {
            double cx = (((i * 46 - scroll * 0.5) % 1104) + 1104) % 1104 - 60;
            g.setColor(i % 3 == 0 ? new Color(219, 93, 96) : i % 3 == 1 ? new Color(81, 146, 185) : new Color(240, 200, 90));
            car.setRoundRect(cx, gy + 16, 36, 16, 8, 8);
            g.fill(car);
            g.setColor(new Color(225, 239, 242));
            cityRect.setRect(cx + 10, gy + 19, 14, 8);
            g.fill(cityRect);
        }
    }

    void drawFlightScene(Graphics2D g) {
        long now = System.currentTimeMillis();
        drawSkyScene(g, skyScroll);
        double rem = Math.max(0, (ROAD_DURATION_MS - stageProgressMs) / 1000.0);
        if (rem < 8.0) SchoolBuilding.draw(g, W / 2.0 + rem / 8.0 * 700, 572, 0.62, this);
        Stroke oldStroke = g.getStroke();
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 9; i++) {
            double x = (((i * 173 - skyScroll * 2.4) % (W + 200)) + (W + 200)) % (W + 200) - 100;
            double y = 90 + (i * 61) % 400;
            g.setColor(new Color(255, 255, 255, 70));
            g.draw(new Line2D.Double(x, y, x + 70, y));
        }
        g.setStroke(oldStroke);
        for (Bird b : birds) drawBird(g, b.x, b.y + Math.sin(b.phase * 0.35) * 6, b.phase);

        // Vẽ Bom rơi chéo
        for (sanbaka.model.EnemyBomb eb : enemyBombs) eb.draw(g, this);

        // Vẽ hiệu ứng nổ bom bán kính sát thương diện rộng
        for (sanbaka.model.BombExplosion exp : explosions) exp.draw(g, this);

        // Vẽ Máy bay địch
        for (sanbaka.model.EnemyPlane ep : enemyPlanes) ep.draw(g, this);

        // Vẽ Đạn của Boss & Máy bay
        for (sanbaka.model.BossBullet bb : bossBullets) bb.draw(g, this);

        // Vẽ Tên lửa người chơi
        for (sanbaka.model.PlayerMissile pm : missiles) pm.draw(g, this);

        // Vẽ Trùm Cuối (Boss), thanh máu 20 vạch đỉnh màn hình & nút [E] nhấp nháy
        if (boss != null) boss.draw(g, this, heliX, heliY);
        Composite oldComposite = g.getComposite();
        if (player.invulnerable > now && (now / 90) % 2 == 0) {
            g.setComposite(AlphaComposite.SrcOver.derive(0.4f));
        }
        drawHelicopter(g, heliX, heliY, heliTilt, now / 1000.0, 0.9);
        g.setComposite(oldComposite);
        if (now < hitSlowUntil) drawCollisionSlowEffect(g, heliX, heliY, now);
    }
}
