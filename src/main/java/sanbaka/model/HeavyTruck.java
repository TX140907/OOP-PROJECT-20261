package sanbaka.model;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;
import sanbaka.assets.ArtAssets;
import sanbaka.render.GameVisuals;
import sanbaka.state.GameState;

/** Xe tải hạng nặng cỡ lớn chiếm trọn bề ngang một làn đường (chặn đi sát mép làn 1 và 3). */
public class HeavyTruck extends Thing {
    private static final int SPRITE_CACHE_LIMIT = 48;
    private static final ThreadLocal<Boolean> BUILDING_SPRITE = ThreadLocal.withInitial(() -> false);
    private static final Map<String, BufferedImage> SPRITE_CACHE = new LinkedHashMap<>(SPRITE_CACHE_LIMIT, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> eldest) {
            return size() > SPRITE_CACHE_LIMIT;
        }
    };

    @Override
    public void draw(Graphics2D g, GameVisuals scene) {
        if (!BUILDING_SPRITE.get()) {
            drawCached(g, scene);
            return;
        }
        AffineTransform old = g.getTransform();
        Stroke oldStroke = g.getStroke();
        g.translate(this.x, this.y);
        g.rotate(this.angle);
        double w = this.w, h = this.h, hw = w / 2, hh = h / 2;

        boolean brake = this.jam || this.fwd == 0;
        // Đổ bóng gầm xe
        g.setColor(new Color(0, 0, 0, 55));
        g.fill(new RoundRectangle2D.Double(-hw + 5, -hh + 8, w, h, 18, 18));

        BufferedImage art = ArtAssets.load("HeavyTruck", "HeavyTruck.png");
        if (art != null) {
            g.drawImage(art, (int) -hw, (int) -hh, (int) w, (int) h, null);
            g.setTransform(old);
            g.setStroke(oldStroke);
            return;
        }

        Color glass = new Color(52, 68, 92), glassHi = new Color(255, 255, 255, 70);
        Color body = GameState.CAR_COLORS[this.color % 8];

        // 4 trục bánh xe tải hạng nặng
        double[] wy = {-hh + 22, -hh + 64, hh - 52, hh - 20};
        g.setColor(new Color(24, 26, 32));
        for (double yy : wy) {
            g.fill(new RoundRectangle2D.Double(-hw - 3, yy - 9, 7, 18, 3, 3));
            g.fill(new RoundRectangle2D.Double(hw - 4, yy - 9, 7, 18, 3, 3));
        }

        // Khung thân xe chính
        RoundRectangle2D.Double shape = new RoundRectangle2D.Double(-hw, -hh, w, h, 14, 14);
        g.setColor(body);
        g.fill(shape);
        g.setColor(scene.shade(body, 0.7));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(shape);

        // Gương chiếu hậu hai bên cabin
        g.setColor(scene.shade(body, 0.76));
        g.fill(new RoundRectangle2D.Double(-hw - 4, -hh + 15, 5, 9, 2, 2));
        g.fill(new RoundRectangle2D.Double(hw - 1, -hh + 15, 5, 9, 2, 2));

        // Cabin đầu kéo phía trước
        g.setColor(scene.shade(body, 1.12));
        g.fill(new RoundRectangle2D.Double(-hw + 4, -hh + 3, w - 8, 38, 11, 11));
        g.setColor(glass);
        scene.poly(g, -hw + 10, -hh + 28, hw - 10, -hh + 28, hw - 14, -hh + 11, -hw + 14, -hh + 11);
        g.setColor(glassHi);
        scene.poly(g, -hw + 16, -hh + 13, -hw + 34, -hh + 13, -hw + 22, -hh + 26);
        scene.poly(g, hw - 36, -hh + 13, hw - 18, -hh + 13, hw - 30, -hh + 26);

        // Mui lướt gió trên nóc cabin
        g.setColor(scene.shade(body, 0.88));
        g.fill(new RoundRectangle2D.Double(-hw + 22, -hh + 30, w - 44, 10, 5, 5));

        // Đèn nóc định vị cabin
        g.setColor(new Color(255, 206, 92));
        for (int i = -2; i <= 2; i++) {
            g.fill(new RoundRectangle2D.Double(i * 18 - 3, -hh + 4, 6, 3, 2, 2));
        }

        // Thùng hàng container cỡ lớn kín làn
        double boxY = -hh + 44;
        double boxH = h - 46;
        g.setColor(new Color(214, 218, 226));
        g.fill(new RoundRectangle2D.Double(-hw + 3, boxY, w - 6, boxH, 7, 7));

        // Viền màu đồng bộ dọc hai bên thùng xe
        g.setColor(body);
        g.fill(new Rectangle2D.Double(-hw + 4, boxY + 2, 9, boxH - 4));
        g.fill(new Rectangle2D.Double(hw - 13, boxY + 2, 9, boxH - 4));

        // Dải màu trang trí giữa nóc thùng
        g.setColor(scene.shade(body, 0.94));
        g.fill(new RoundRectangle2D.Double(-18, boxY + 14, 36, boxH - 28, 6, 6));
        g.setColor(new Color(238, 240, 244, 200));
        g.fill(new RoundRectangle2D.Double(-12, boxY + 20, 24, boxH - 40, 4, 4));

        // Các gân sóng kim loại trên nóc thùng container
        g.setColor(new Color(0, 0, 0, 38));
        for (int i = 0; i < 9; i++) {
            g.fill(new Rectangle2D.Double(-hw + 5, boxY + 10 + i * 12, w - 10, 2));
        }
        g.fill(new Rectangle2D.Double(-1, boxY + 4, 2, boxH - 8));

        g.setColor(new Color(150, 156, 170));
        g.draw(new RoundRectangle2D.Double(-hw + 3, boxY, w - 6, boxH, 7, 7));

        // Cụm đèn hậu & đèn phanh kép hai bên đuôi xe
        Color lamp = brake ? new Color(255, 60, 70) : new Color(176, 34, 48);
        if (brake) {
            g.setColor(new Color(255, 60, 70, 75));
            g.fill(new Ellipse2D.Double(-hw + 1, hh - 13, 30, 13));
            g.fill(new Ellipse2D.Double(hw - 31, hh - 13, 30, 13));
        }
        g.setColor(lamp);
        g.fill(new RoundRectangle2D.Double(-hw + 4, hh - 7, 13, 5, 3, 3));
        g.fill(new RoundRectangle2D.Double(-hw + 20, hh - 7, 11, 5, 3, 3));
        g.fill(new RoundRectangle2D.Double(hw - 31, hh - 7, 11, 5, 3, 3));
        g.fill(new RoundRectangle2D.Double(hw - 17, hh - 7, 13, 5, 3, 3));

        // Đèn cảnh báo vàng ở hai góc đuôi thùng
        g.setColor(new Color(240, 190, 70));
        g.fill(new Rectangle2D.Double(-hw + 4, hh - 11, 8, 3));
        g.fill(new Rectangle2D.Double(hw - 12, hh - 11, 8, 3));

        // Biển số xe ở giữa đuôi
        g.setColor(new Color(245, 245, 240));
        g.fill(new RoundRectangle2D.Double(-10, hh - 8, 20, 5, 2, 2));

        g.setTransform(old);
        g.setStroke(oldStroke);
    }

    private void drawCached(Graphics2D g, GameVisuals scene) {
        boolean brake = this.jam || this.fwd == 0;
        BufferedImage suppliedArt = ArtAssets.load("HeavyTruck", "HeavyTruck.png");
        if (suppliedArt != null) {
            AffineTransform old = g.getTransform();
            Stroke oldStroke = g.getStroke();
            g.translate(this.x, this.y);
            g.rotate(this.angle);
            double hw = this.w / 2, hh = this.h / 2;
            g.setColor(new Color(0, 0, 0, 55));
            g.fill(new RoundRectangle2D.Double(-hw + 5, -hh + 8, this.w, this.h, 18, 18));
            g.drawImage(suppliedArt, (int) -hw, (int) -hh, (int) this.w, (int) this.h, null);
            g.setTransform(old);
            g.setStroke(oldStroke);
            return;
        }
        String key = this.color + ":" + (int) this.w + ":" + (int) this.h + ":" + brake;
        BufferedImage sprite = SPRITE_CACHE.get(key);
        int padX = (int) Math.ceil(this.w / 2.0 + 12);
        int padY = (int) Math.ceil(this.h / 2.0 + 14);
        if (sprite == null) {
            sprite = createSprite(padX, padY, scene, brake);
            SPRITE_CACHE.put(key, sprite);
        }

        AffineTransform old = g.getTransform();
        Stroke oldStroke = g.getStroke();
        g.translate(this.x, this.y);
        g.rotate(this.angle);
        g.drawImage(sprite, -padX, -padY, null);
        g.setTransform(old);
        g.setStroke(oldStroke);
    }

    private BufferedImage createSprite(int padX, int padY, GameVisuals scene, boolean brake) {
        BufferedImage sprite = ArtAssets.createCompatibleImage(padX * 2, padY * 2);
        Graphics2D sg = sprite.createGraphics();
        sg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        sg.translate(padX, padY);

        HeavyTruck source = new HeavyTruck();
        source.kind = this.kind;
        source.color = this.color;
        source.w = this.w;
        source.h = this.h;
        source.jam = brake;
        source.fwd = brake ? 0 : 1;
        boolean oldBuilding = BUILDING_SPRITE.get();
        int oldDay = scene.day;
        try {
            BUILDING_SPRITE.set(true);
            scene.day = 0;
            source.draw(sg, scene);
        } finally {
            scene.day = oldDay;
            BUILDING_SPRITE.set(oldBuilding);
            sg.dispose();
        }
        return sprite;
    }
}

