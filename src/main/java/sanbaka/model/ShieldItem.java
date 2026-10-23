package sanbaka.model;

import java.awt.Graphics2D;
import sanbaka.assets.ArtAssets;
import sanbaka.render.GameVisuals;

/** Vật phẩm Khiên bảo vệ ở Màn 4: miễn 1 lần va chạm với bất kỳ chướng ngại nào. */
public class ShieldItem extends Thing {
    public ShieldItem() {
        kind = 17;
        snack = true;
        w = 36;
        h = 36;
    }

    @Override
    public void draw(Graphics2D g, GameVisuals scene) {
        double scale = 1.0 + 0.08 * Math.sin(System.currentTimeMillis() / 170.0);
        java.awt.image.BufferedImage art = ArtAssets.load("ShieldItem", "ShieldItem.png");
        if (art == null) {
            scene.drawShieldItem(g, x, y, scale);
            return;
        }
        int dw = (int) Math.round(w * scale), dh = (int) Math.round(h * scale);
        g.drawImage(art, (int) Math.round(x - dw / 2.0), (int) Math.round(y - dh / 2.0), dw, dh, null);
    }
}

