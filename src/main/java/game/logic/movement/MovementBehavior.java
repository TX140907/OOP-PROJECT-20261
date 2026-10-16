package game.logic.movement;
public interface MovementBehavior {
    record Position(double x, double y) {}
    Position at(double seconds);
    default Position at(double realSeconds,double scrollSeconds) { return at(realSeconds); }
}
