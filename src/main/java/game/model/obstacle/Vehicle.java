package game.model.obstacle;

import game.config.GameConfig;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

/** Vật cản hình xe màu xanh chạy từ trên xuống. */
public final class Vehicle extends Obstacle {
    public static final int WIDTH = 100; // Chiều rộng xe, chiếm 62,5% làn.
    public static final int HEIGHT = 150; // Chiều cao xe (px).
    private static final Color COLOR = Color.GREEN; // Màu xe.

    private final double speed;

    public Vehicle(int lane, double startY, double speed) {
        super(GameConfig.centeredX(lane, WIDTH), startY, WIDTH, HEIGHT);
        if (!Double.isFinite(speed) || speed <= 0) {
            throw new IllegalArgumentException("Tốc độ xe phải dương và hữu hạn");
        }
        this.speed = speed;
    }

    @Override
    public void update(double deltaSeconds) {
        y += speed * deltaSeconds;
    }

    @Override
    public Rectangle2D getMovementBounds(double secondsAhead) {
        return new Rectangle2D.Double(x, y, getWidth(), getHeight() + speed * secondsAhead);
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(COLOR);
        g.fillRect((int) Math.round(x), (int) Math.round(y), getWidth(), getHeight());
    }
}
