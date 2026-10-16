package game.model.obstacle;

import game.level.DayConfig.ObstacleSpec;
import game.logic.movement.LinearMovement;

/** Nhặt vào để bật khiên trong một khoảng thời gian; không phải vật gây sát thương. */
public final class ShieldPickup extends Obstacle {
    public ShieldPickup(ObstacleSpec spec,double x,double y,double speed) {
        super(spec,new LinearMovement(x,y,0,speed));
    }
}
