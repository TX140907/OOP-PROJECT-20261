package game.model.obstacle;
import game.level.DayConfig.ObstacleSpec;
import game.logic.movement.BirdMovement;
/** Flies into view first; a selected bird then stops in a lane and follows camera scroll only. */
public final class Bird extends Obstacle {
    public enum State { FLYING, STANDING }
    private final boolean willStop;
    private final double stopSeconds,stopX;
    public Bird(ObstacleSpec spec,double x,double y,double speed,double scrollSpeed) {
        this(spec,x,y,speed,scrollSpeed,false,0);
    }
    public Bird(ObstacleSpec spec,double x,double y,double speed,double scrollSpeed,boolean stopInLane,double stopX) {
        super(spec,new BirdMovement(x,y,speed,scrollSpeed,stopInLane,stopX));
        this.willStop=stopInLane;this.stopX=stopX;
        this.stopSeconds=stopInLane?Math.abs(stopX-x)/Math.abs(speed):Double.POSITIVE_INFINITY;
    }
    public boolean willStop() { return willStop; }
    public double stopSeconds() { return stopSeconds; }
    public double stopX() { return stopX; }
    public State state() { return willStop && age()+1e-9>=stopSeconds?State.STANDING:State.FLYING; }
}
