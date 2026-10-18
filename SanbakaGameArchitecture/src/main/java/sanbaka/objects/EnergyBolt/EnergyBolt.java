package sanbaka;

import java.awt.Graphics2D;

/** Vật phẩm tia năng lượng có thể nhặt trên đường. */
class EnergyBolt extends Thing {
    @Override
    void draw(Graphics2D g, GameVisuals scene) {
        double scale = 1 + 0.12 * Math.sin(System.currentTimeMillis() / 160.0);
        java.awt.image.BufferedImage art = ArtAssets.load("EnergyBolt", "EnergyBolt.png");
        if (art == null) {
            scene.drawBolt(g, x, y, scale);
            return;
        }
        int w = (int) Math.round(30 * scale), h = (int) Math.round(40 * scale);
        g.drawImage(art, (int) Math.round(x - w / 2.0), (int) Math.round(y - h / 2.0), w, h, null);
    }
}
