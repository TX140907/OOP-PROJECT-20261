package game.logic.movement;
/** Waits for a warning, then follows the exact trajectory used by prediction. */
public record DelayedLinearMovement(double x,double y,double vx,double vy,double delay) implements MovementBehavior {
    @Override public Position at(double seconds) {
        double flight=Math.max(0,seconds-delay);
        return new Position(x+vx*flight,y+vy*flight);
    }
}
