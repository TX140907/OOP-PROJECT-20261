package game.model.obstacle;
import game.level.DayConfig.ObstacleSpec;
import game.logic.movement.LinearMovement;
/** Rolling movement reuses composition; freezing is its collision effect, never vehicle lethality. */
public final class Snowball extends Obstacle {
    public Snowball(ObstacleSpec spec,double x,double y,double speed) { super(spec,new LinearMovement(x,y,0,speed)); }
}
