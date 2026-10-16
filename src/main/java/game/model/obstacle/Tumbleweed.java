package game.model.obstacle;

import game.level.DayConfig.ObstacleSpec;
import game.logic.movement.LaneStopMovement;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/** Cỏ khô tròn lăn vào một làn rồi đứng yên trong thế giới, tiếp tục trôi theo cảnh. */
public final class Tumbleweed extends Obstacle {
    private final double startX,stopSeconds;
    public Tumbleweed(ObstacleSpec spec,double x,double y,double speed,double scrollSpeed,double stopX) {
        super(spec,new LaneStopMovement(x,y,speed,scrollSpeed,stopX));
        startX=x;stopSeconds=Math.abs(stopX-x)/Math.abs(speed);
    }
    public boolean rolling() { return age()+1e-9<stopSeconds; }
    public double rollingRadians() { return (x()-startX)/(width()/2); }
    @Override public Shape collisionShape() { return new Ellipse2D.Double(x(),y(),width(),height()); }
    @Override public Rectangle2D laneReservation() { return predictedHitbox(Math.max(0,stopSeconds-age())); }
}
