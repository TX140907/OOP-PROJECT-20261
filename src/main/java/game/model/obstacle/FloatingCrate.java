package game.model.obstacle;
import game.level.DayConfig.ObstacleSpec;
import game.logic.movement.DriftMovement;
public final class FloatingCrate extends Obstacle {
    public FloatingCrate(ObstacleSpec spec,double x,double y,double drift,double scroll,double min,double max) {
        super(spec,new DriftMovement(x,y,drift,scroll,min,max));
    }
}
