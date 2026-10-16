package game.level;

import game.config.ConfigValues;
import game.logic.collision.CollisionKind;
import game.model.obstacle.ObstacleType;
import java.util.List;
import java.util.Properties;

/** Immutable level settings and immutable obstacle definitions. */
public final class DayConfig {
    public record ObstacleSpec(ObstacleType type, double weight, double chance,
            double width, double height, double minSpeed, double maxSpeed,
            CollisionKind collision, boolean consumed, String sprite) {}
    private final ConfigValues values;
    private final List<ObstacleSpec> obstacles;
    public DayConfig(ConfigValues values, List<ObstacleSpec> obstacles) {
        this.values = values;
        this.obstacles = List.copyOf(obstacles);
    }
    public ConfigValues values() { return values; }
    public List<ObstacleSpec> obstacles() { return obstacles; }
    public int id() { return values.integer("level.id"); }
    public String name() { return values.text("level.name"); }
    public double duration() { return values.number("level.durationSeconds"); }
    public boolean helicopter() { return values.text("level.mode").equals("air"); }
    public Properties copyProperties() { return values.copy(); }
    public double minX() { return values.number("player.minX"); }
    public double maxX() { return values.number("player.maxX"); }
}
