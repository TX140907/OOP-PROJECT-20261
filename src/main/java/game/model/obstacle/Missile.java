package game.model.obstacle;
import game.level.DayConfig.ObstacleSpec;
import game.logic.movement.DelayedLinearMovement;
import java.awt.geom.Rectangle2D;
/** Fires straight down from a fixed launcher X; the warning marks that same flight line. */
public final class Missile extends Obstacle {
    private final Obstacle launcher;
    private final double targetX,targetY,warningSeconds;
    public Missile(ObstacleSpec spec,Obstacle launcher,double x,double y,double targetY,double speed,double warningSeconds) {
        super(spec,movement(x,y,speed,warningSeconds));
        this.launcher=launcher;this.targetX=x+spec.width()/2;this.targetY=targetY;this.warningSeconds=warningSeconds;
    }
    private static DelayedLinearMovement movement(double x,double y,double speed,double warning) {
        if(speed<=0 || warning<=0) throw new IllegalArgumentException("Invalid missile trajectory");
        return new DelayedLinearMovement(x,y,0,speed,warning);
    }
    public boolean warning() { return age()+1e-9<warningSeconds; }
    @Override public boolean blocksPlayerRocket() { return false; }
    @Override public boolean activeAt(double future) { return !removed() && age()+future+1e-9>=warningSeconds; }
    @Override public boolean allowsOverlap(Obstacle other) { return other==launcher; }
    public Rectangle2D targetArea(double size) { return new Rectangle2D.Double(targetX-size/2,targetY-size/2,size,size); }
    public double targetX() { return targetX; }
    public double targetY() { return targetY; }
}
