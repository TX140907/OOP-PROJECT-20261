package game.model.obstacle;

import game.level.BaseLevel;
import game.level.DayConfig.ObstacleSpec;
import game.logic.collision.CollisionEffect;
import game.logic.movement.ScrollingMovement;

/** Hố băng sụp gây mất máu và làm chậm theo nhóm drain, giống cống nước. */
public final class IceHole extends Obstacle {
    private boolean collapsed;
    public IceHole(ObstacleSpec spec,double x,double y,double scrollSpeed) {
        super(spec,new ScrollingMovement(x,y,0,scrollSpeed));
    }
    @Override public CollisionEffect contactEffect(BaseLevel world) { collapsed=true;return effect(); }
    public boolean collapsed() { return collapsed; }
}
