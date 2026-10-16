package game.model.projectile;

import game.model.Entity;
import java.awt.geom.Rectangle2D;

/** Tên lửa bay thẳng lên, tách khỏi danh sách vật cản gây hại cho người chơi. */
public final class PlayerRocket extends Entity {
    private final double speed;
    public PlayerRocket(double x,double y,double width,double height,double speed) {
        super(x,y,width,height,width,height,0,0);this.speed=speed;
    }
    public Rectangle2D sweptBounds(double dt) {
        return new Rectangle2D.Double(x(),y()-speed*dt,width(),height()+speed*dt);
    }
    public void update(double dt) { position(x(),y()-speed*dt); }
}
