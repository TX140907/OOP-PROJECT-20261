package game.logic.movement;
/** Slow lateral drift with reflecting boundaries, plus world scrolling. Prediction uses the same equation. */
public record DriftMovement(double startX, double startY, double speedX, double scrollSpeed,
        double minX, double maxX) implements MovementBehavior {
    @Override public Position at(double seconds) {
        return at(seconds,seconds);
    }
    @Override public Position at(double seconds,double scrollSeconds) {
        double range=maxX-minX, phase=(startX-minX+speedX*seconds)%(2*range);
        if (phase<0) phase+=2*range;
        return new Position(minX+(phase<=range?phase:2*range-phase),startY+scrollSpeed*scrollSeconds);
    }
}
