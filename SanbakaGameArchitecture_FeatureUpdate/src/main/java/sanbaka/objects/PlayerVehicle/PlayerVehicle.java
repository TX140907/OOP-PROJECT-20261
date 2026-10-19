package sanbaka;

import java.awt.Graphics2D;

/** Trạng thái dùng chung của phương tiện người chơi trên đường. */
abstract class PlayerVehicle {
    double x = 480;
    double y = 490;
    double lean;
    long invulnerable;

    abstract void draw(Graphics2D g, GameVisuals scene);
}
