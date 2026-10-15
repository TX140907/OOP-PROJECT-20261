package game.model;

import game.config.GameConfig;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

/** Nhân vật và trạng thái chuyển làn; va chạm theo vị trí thực khi đang trượt. */
public final class Player {
    public static final int WIDTH = 60; // Chiều rộng nhân vật (px).
    public static final int HEIGHT = 90; // Chiều cao nhân vật (px).
    private static final int START_LANE = 1; // Bắt đầu ở làn giữa (đếm từ 0).
    private static final int START_Y = GameConfig.PLAYER_ROW_CENTER_Y - HEIGHT / 2; // Căn giữa hàng nhân vật.
    private static final double LANE_CHANGE_SECONDS = 0.20; // Một lần đổi làn mất 0,2 giây.
    private static final Color COLOR = Color.RED; // Màu nhân vật.

    private int lane;
    private double x;
    private double startX;
    private double elapsed;
    private boolean changingLane;

    public Player() {
        reset();
    }

    public void reset() {
        lane = START_LANE;
        x = GameConfig.centeredX(lane, WIDTH);
        startX = x;
        elapsed = 0;
        changingLane = false;
    }

    public void changeLane(int direction) {
        if (changingLane) return;
        int nextLane = lane + direction;
        if (nextLane < 0 || nextLane >= GameConfig.LANE_COUNT) return;
        startX = x;
        lane = nextLane;
        elapsed = 0;
        changingLane = true;
    }

    public void update(double deltaSeconds) {
        if (!changingLane) return;
        elapsed = Math.min(elapsed + deltaSeconds, LANE_CHANGE_SECONDS);
        double t = elapsed / LANE_CHANGE_SECONDS;
        double smooth = t * t * (3 - 2 * t); // Tăng tốc rồi giảm tốc nhẹ ở hai đầu.
        x = startX + (GameConfig.centeredX(lane, WIDTH) - startX) * smooth;
        if (elapsed >= LANE_CHANGE_SECONDS) changingLane = false;
    }

    public Rectangle2D getBounds() {
        return new Rectangle2D.Double(x, START_Y, WIDTH, HEIGHT);
    }

    public void draw(Graphics g) {
        g.setColor(COLOR);
        g.fillRect((int) Math.round(x), START_Y, WIDTH, HEIGHT);
    }
}
