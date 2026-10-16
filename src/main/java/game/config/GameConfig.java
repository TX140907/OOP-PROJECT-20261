package game.config;

/** Immutable logical viewport; gameplay settings belong to each level's properties. */
public final class GameConfig {
    public static final int WIDTH = 1000;
    public static final int HEIGHT = 700;
    public static final int ROAD_LEFT = 140;
    public static final int LANE_WIDTH = 240;
    public static final int LANE_COUNT = 3;
    public static final int ROAD_RIGHT = 860;
    public static final double STEP = 1.0 / 60;
    private GameConfig() {}
    public static double laneX(int lane, double width) {
        return ROAD_LEFT + (lane + .5) * LANE_WIDTH - width / 2;
    }
}
