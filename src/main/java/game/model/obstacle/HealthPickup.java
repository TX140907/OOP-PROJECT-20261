package game.model.obstacle;
import game.level.DayConfig.ObstacleSpec;
import game.logic.movement.LinearMovement;
/** An airborne healing item; entering its hitbox heals once and consumes it. */
public final class HealthPickup extends Obstacle {
    public HealthPickup(ObstacleSpec spec,double x,double y,double speed) {
        super(spec,new LinearMovement(x,y,0,speed));
    }
}
