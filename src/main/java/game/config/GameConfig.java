package game.config;

import java.awt.Color;

/** Các tham số dùng chung; tham số riêng của từng đối tượng nằm đầu lớp tương ứng. */
public final class GameConfig {
    public static final int WIDTH = 1000; // Chiều rộng vùng chơi (px).
    public static final int HEIGHT = 700; // Chiều cao vùng chơi (px).
    public static final int PLAYER_ROW_CENTER_Y = 565; // Tâm hàng nhân vật, cũng là hàng cá mập tấn công.
    public static final int ROAD_LEFT = 260; // Lề trái 260 px.
    public static final int LANE_WIDTH = 160; // Chiều rộng mỗi làn (px).
    public static final int LANE_COUNT = 3; // Ba làn nằm giữa màn hình.
    public static final int ROAD_RIGHT = ROAD_LEFT + LANE_WIDTH * LANE_COUNT; // Lề phải 260 px.
    public static final int FRAME_DELAY_MS = 16; // Chu kỳ timer (ms).
    public static final Color BACKGROUND_COLOR = Color.BLUE; // Màu nền.
    public static final Color ROAD_COLOR = Color.BLACK; // Màu vạch đường.
    public static final int ROAD_LINE_WIDTH = 1; // Độ dày vạch đường (px).

    private GameConfig() {}

    public static int centeredX(int lane, int objectWidth) {
        return ROAD_LEFT + lane * LANE_WIDTH + (LANE_WIDTH - objectWidth) / 2;
    }
}
