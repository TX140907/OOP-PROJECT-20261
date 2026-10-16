package game.logic.movement;
public record LinearMovement(double startX, double startY, double vx, double vy) implements MovementBehavior {
    @Override public Position at(double seconds) { return new Position(startX+vx*seconds,startY+vy*seconds); }
}
