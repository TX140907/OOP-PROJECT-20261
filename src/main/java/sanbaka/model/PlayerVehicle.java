package sanbaka.model;

import java.awt.Graphics2D;
import sanbaka.render.GameVisuals;

/** Trạng thái dùng chung của phương tiện người chơi trên đường. */
public abstract class PlayerVehicle {
    public double x = 480;
    public double y = 490;
    public double w = 34;
    public double h = 60;
    public double lean;
    public long invulnerable;

    public abstract void draw(Graphics2D g, GameVisuals scene);
}
