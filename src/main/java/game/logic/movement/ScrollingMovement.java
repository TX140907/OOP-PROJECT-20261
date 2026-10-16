package game.logic.movement;

/** Screen = world + camera travel: world Y stays fixed, lateral motion uses real time. */
public record ScrollingMovement(double startX,double startY,double vx,double scrollSpeed) implements MovementBehavior {
    @Override public Position at(double seconds) { return at(seconds,seconds); }
    @Override public Position at(double realSeconds,double scrollSeconds) {
        return new Position(startX+vx*realSeconds,startY+scrollSpeed*scrollSeconds);
    }
}
