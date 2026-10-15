package game.model.obstacle;

import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.util.List;

/** Lớp cha của vật cản: hình học, va chạm và giao diện cập nhật/vẽ chung. */
public abstract class Obstacle {
    protected double x;
    protected double y;
    private final int width;
    private final int height;

    protected Obstacle(double x, double y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void update(double deltaSeconds);
    public abstract void draw(Graphics g);

    public void update(double deltaSeconds, List<Obstacle> obstacles) {
        update(deltaSeconds);
    }

    public Rectangle2D getBounds() {
        return new Rectangle2D.Double(x, y, width, height);
    }

    /** Vùng đối tượng sẽ đi qua trong khoảng thời gian tới, để tránh đặt đè lên đường đi. */
    public Rectangle2D getMovementBounds(double secondsAhead) {
        return getBounds();
    }

    public boolean blocksSpawn(Obstacle candidate) {
        return getBounds().intersects(candidate.getBounds());
    }

    public boolean collidesWith(Rectangle2D bounds) {
        return bounds.intersects(x, y, width, height);
    }

    public boolean isOffScreen(int screenHeight) {
        return y > screenHeight;
    }

    public boolean allowsNextWave(int thresholdY) {
        return y > thresholdY;
    }

    public double getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
