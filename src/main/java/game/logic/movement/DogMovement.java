package game.logic.movement;

/** Fixed world-space crossing, warning pause, then charge. Camera travel is applied separately. */
public record DogMovement(double startX,double startY,double stopX,double targetX,double crossSeconds,
                          double barkSeconds,double chargeSeconds,double chargeDistanceY,double scrollSpeed)
        implements MovementBehavior {
    @Override public Position at(double seconds) { return at(seconds,seconds); }
    @Override public Position at(double realSeconds,double scrollSeconds) {
        double cross=Math.max(0,Math.min(1,realSeconds/crossSeconds));
        double charge=Math.max(0,Math.min(1,(realSeconds-crossSeconds-barkSeconds)/chargeSeconds));
        double x=realSeconds<crossSeconds?startX+(stopX-startX)*cross:stopX+(targetX-stopX)*charge;
        return new Position(x,startY+scrollSpeed*scrollSeconds+chargeDistanceY*charge);
    }
}
