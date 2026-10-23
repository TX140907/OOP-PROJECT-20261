package sanbaka.model;

import java.awt.Graphics2D;
import sanbaka.assets.ArtAssets;
import sanbaka.render.GameVisuals;

/** Vật phẩm Trà gừng ở Màn 3: hóa giải đóng băng, giữ ấm và hồi phục 15 máu. */
public class GingerTea extends Thing {
    public GingerTea() {
        kind = 16;
        snack = true;
        w = 34;
        h = 34;
    }

    @Override
    public void draw(Graphics2D g, GameVisuals scene) {
        double scale = 1.0 + 0.08 * Math.sin(System.currentTimeMillis() / 190.0);
        java.awt.image.BufferedImage art = ArtAssets.load("GingerTea", "GingerTea.png");
        if (art == null) {
            scene.drawGingerTea(g, x, y, scale);
            return;
        }
        int dw = (int) Math.round(w * scale), dh = (int) Math.round(h * scale);
        g.drawImage(art, (int) Math.round(x - dw / 2.0), (int) Math.round(y - dh / 2.0), dw, dh, null);
    }
}

