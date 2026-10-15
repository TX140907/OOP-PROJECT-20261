package game.model.obstacle;

import game.config.GameConfig;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Vây bơi từ biên trên rồi lặn; cảnh báo và đớp ở làn ngẫu nhiên ngang nhân vật. */
public final class Shark extends Obstacle {
    public static final int WIDTH = 140; // Chiều rộng vùng đớp và hình vuông cảnh báo (px).
    public static final int HEIGHT = 140; // Chiều cao vùng đớp và hình cảnh báo (px).
    private static final double FIN_SPEED = 100; // Tốc độ vây bơi xuống màn hình (px/giây).
    private static final double FIN_SECONDS = 0.5; // Bơi xuống 0,5 giây rồi lặn tại chỗ.
    private static final double HIDDEN_SECONDS = 1; // Khoảng ẩn sau khi vây biến mất.
    private static final double WARNING_SECONDS = 1.25; // Cảnh báo đủ 1,25 giây trước khi đớp.
    private static final double BITE_SECONDS = 0.3; // Thời gian đớp, có va chạm.
    private static final double BLINK_SECONDS = 0.12; // Đổi sáng/tắt cảnh báo mỗi 0,12 giây.
    private static final int FIN_WIDTH = 36; // Chiều rộng vây tạm (px).
    private static final int FIN_HEIGHT = 40; // Chiều cao vây tạm (px).
    private static final double FIN_START_Y = (FIN_HEIGHT - HEIGHT) / 2.0; // Đỉnh vây bắt đầu ở biên trên.
    private static final int RIPPLE_SIZE = 24; // Đường kính dấu mặt nước tại chỗ lặn (px).
    private static final Color RIPPLE_COLOR = Color.WHITE; // Màu dấu mặt nước tạm.
    private static final Color FIN_COLOR = Color.GRAY; // Màu vây tạm.
    private static final Color WARNING_COLOR = Color.YELLOW; // Màu hình cảnh báo tạm.
    private static final Color BITE_COLOR = Color.DARK_GRAY; // Màu cá trồi lên tạm.
    private static final Color MOUTH_COLOR = Color.RED; // Màu miệng cá tạm.
    private static final double PLACEMENT_MARGIN_SECONDS = 0.05; // Dự phòng một bước cập nhật khi giữ chỗ đớp.

    public enum State { FIN, HIDDEN, WARNING, BITE, FINISHED }

    private State state = State.FIN;
    private double stateSeconds;
    private final double startY;

    public Shark(int lane, double spawnYOffset) {
        super(GameConfig.centeredX(lane, WIDTH), FIN_START_Y - spawnYOffset, WIDTH, HEIGHT);
        this.startY = y;
    }

    public State getState() {
        return state;
    }

    @Override
    public void update(double deltaSeconds) {
        update(deltaSeconds, List.of());
    }

    @Override
    public void update(double deltaSeconds, List<Obstacle> obstacles) {
        stateSeconds += deltaSeconds;
        switch (state) {
            case FIN -> {
                y = startY + FIN_SPEED * Math.min(stateSeconds, FIN_SECONDS);
                if (stateSeconds >= FIN_SECONDS) enter(State.HIDDEN);
            }
            case HIDDEN -> {
                if (stateSeconds >= HIDDEN_SECONDS) {
                    chooseAttackPosition(obstacles);
                }
            }
            case WARNING -> {
                if (stateSeconds >= WARNING_SECONDS) enter(State.BITE);
            }
            case BITE -> {
                if (stateSeconds >= BITE_SECONDS) enter(State.FINISHED);
            }
            default -> { }
        }
    }

    private void chooseAttackPosition(List<Obstacle> obstacles) {
        List<Integer> freeLanes = new ArrayList<>();
        double attackY = GameConfig.PLAYER_ROW_CENTER_Y - HEIGHT / 2.0;
        for (int lane = 0; lane < GameConfig.LANE_COUNT; lane++) {
            Rectangle2D area = new Rectangle2D.Double(GameConfig.centeredX(lane, WIDTH), attackY, WIDTH, HEIGHT);
            boolean blocked = obstacles.stream().anyMatch(other -> other != this
                    && !other.isOffScreen(GameConfig.HEIGHT)
                    && other.getMovementBounds(WARNING_SECONDS + BITE_SECONDS + PLACEMENT_MARGIN_SECONDS).intersects(area));
            if (!blocked) freeLanes.add(lane);
        }
        if (freeLanes.isEmpty()) return; // Giữ trạng thái lặn và chờ chỗ trống, chưa bắt đầu cảnh báo.
        int lane = freeLanes.get(ThreadLocalRandom.current().nextInt(freeLanes.size()));
        x = GameConfig.centeredX(lane, WIDTH);
        y = attackY;
        enter(State.WARNING); // Giữ nguyên vị trí từ cảnh báo đến hết đòn đớp.
    }

    @Override
    public Rectangle2D getMovementBounds(double secondsAhead) {
        if (state == State.FINISHED) return new Rectangle2D.Double();
        double travel = state == State.FIN
                ? FIN_SPEED * Math.min(secondsAhead, Math.max(0, FIN_SECONDS - stateSeconds)) : 0;
        return new Rectangle2D.Double(x, y, WIDTH, HEIGHT + travel);
    }

    @Override
    public boolean blocksSpawn(Obstacle candidate) {
        if (state == State.FINISHED) return false;
        double remaining = switch (state) {
            case FIN -> Math.max(0, FIN_SECONDS - stateSeconds) + HIDDEN_SECONDS;
            case HIDDEN -> HIDDEN_SECONDS; // Tiếp tục bảo vệ chỗ lặn khi cá phải chờ làn đớp trống.
            case WARNING -> Math.max(0, WARNING_SECONDS - stateSeconds) + BITE_SECONDS;
            case BITE -> Math.max(0, BITE_SECONDS - stateSeconds);
            default -> 0;
        };
        return candidate.getMovementBounds(remaining + PLACEMENT_MARGIN_SECONDS)
                .intersects(getMovementBounds(remaining));
    }

    private void enter(State next) {
        state = next;
        stateSeconds = 0; // Luôn cho đủ thời gian từng trạng thái, không bỏ qua cảnh báo khi khung hình trễ.
    }

    @Override
    public boolean collidesWith(Rectangle2D bounds) {
        return getState() == State.BITE && super.collidesWith(bounds);
    }

    @Override
    public boolean isOffScreen(int screenHeight) {
        return getState() == State.FINISHED;
    }

    @Override
    public boolean allowsNextWave(int thresholdY) {
        return getState() == State.FINISHED;
    }

    @Override
    public void draw(Graphics g) {
        int drawX = (int) Math.round(x);
        int drawY = (int) Math.round(y);
        switch (getState()) {
            case FIN -> {
                int centerX = drawX + WIDTH / 2;
                int centerY = drawY + HEIGHT / 2;
                g.setColor(FIN_COLOR);
                g.fillPolygon(new int[]{centerX - FIN_WIDTH / 2, centerX + FIN_WIDTH / 2, centerX + FIN_WIDTH / 4},
                        new int[]{centerY + FIN_HEIGHT / 2, centerY + FIN_HEIGHT / 2, centerY - FIN_HEIGHT / 2}, 3);
            }
            case HIDDEN -> {
                g.setColor(RIPPLE_COLOR);
                g.drawOval(drawX + (WIDTH - RIPPLE_SIZE) / 2,
                        drawY + (HEIGHT - RIPPLE_SIZE) / 2, RIPPLE_SIZE, RIPPLE_SIZE);
            }
            case WARNING -> {
                if ((int) (stateSeconds / BLINK_SECONDS) % 2 == 0) {
                    g.setColor(WARNING_COLOR);
                    // Hình vuông phủ đúng vùng đớp; thay bằng ảnh vòng tròn + dấu than sau.
                    g.fillRect(drawX, drawY, WIDTH, HEIGHT);
                }
            }
            case BITE -> {
                g.setColor(BITE_COLOR);
                g.fillOval(drawX, drawY, WIDTH, HEIGHT);
                g.setColor(MOUTH_COLOR);
                g.fillOval(drawX + WIDTH / 4, drawY + HEIGHT / 3, WIDTH / 2, HEIGHT / 2);
            }
            default -> { } // Không vẽ khi đã kết thúc.
        }
    }
}
