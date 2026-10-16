package game.logic.movement;

/** Di chuyển ngang tới một vị trí cố định rồi dừng; tọa độ dọc vẫn theo cuộn cảnh. */
public record LaneStopMovement(double startX,double startY,double speed,double scrollSpeed,double stopX)
        implements MovementBehavior {
    @Override public Position at(double seconds) { return at(seconds,seconds); }
    @Override public Position at(double realSeconds,double scrollSeconds) {
        double x=startX+speed*realSeconds;
        x=speed>0?Math.min(x,stopX):Math.max(x,stopX);
        return new Position(x,startY+scrollSpeed*scrollSeconds);
    }
}
