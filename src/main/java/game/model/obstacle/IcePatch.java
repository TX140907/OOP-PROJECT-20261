package game.model.obstacle;
import game.level.DayConfig.ObstacleSpec;
import game.logic.movement.ScrollingMovement;
/** World object moves on screen through scrolling, not active Vehicle behavior. */
public final class IcePatch extends Obstacle {
    public IcePatch(ObstacleSpec spec,double x,double y,double scrollSpeed) { super(spec,new ScrollingMovement(x,y,0,scrollSpeed)); }
}
