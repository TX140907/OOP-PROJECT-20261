package game.logic.movement;

/** All birds enter from off screen; selected birds stop later at a fixed world X. */
public record BirdMovement(double startX,double startY,double speed,double scrollSpeed,boolean stopInLane,double stopX)
        implements MovementBehavior {
    @Override public Position at(double seconds) { return at(seconds,seconds); }
    @Override public Position at(double realSeconds,double scrollSeconds) {
        double x=startX+speed*realSeconds;
        if (stopInLane) x=speed>0?Math.min(x,stopX):Math.max(x,stopX);
        return new Position(x,startY+scrollSpeed*scrollSeconds);
    }
}
