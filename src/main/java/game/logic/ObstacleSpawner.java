package game.logic;

import game.model.obstacle.Obstacle;
import game.level.DayConfig;
import game.config.GameConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Xét xác suất độc lập cho từng loại trong mỗi đợt; có thể ra cả hai hoặc không ra loại nào. */
public final class ObstacleSpawner {
    private static final double EMPTY_WAVE_RETRY_SECONDS = 1.0; // Khi dùng ngưỡng Y mà hụt cả đợt, chờ 1 giây mới xét lại.
    private final Random random;
    private final DayConfig day;
    private double cooldownSeconds;

    public ObstacleSpawner(DayConfig day) {
        this(day, new Random());
    }

    ObstacleSpawner(DayConfig day, Random random) {
        this.day = day;
        this.random = random;
    }

    public void reset() {
        cooldownSeconds = 0;
    }

    public void spawnIfNeeded(List<Obstacle> obstacles, double deltaSeconds) {
        cooldownSeconds = Math.max(0, cooldownSeconds - deltaSeconds);
        if (cooldownSeconds > 0) return;
        if (day.spawnIntervalSeconds() == 0
                && obstacles.stream().anyMatch(obstacle -> !obstacle.allowsNextWave(day.spawnThresholdY()))) return;
        int slots = day.maxActiveObstacles() - obstacles.size();
        if (slots <= 0) return;
        List<DayConfig.ObstacleOption> selected = new ArrayList<>();
        for (DayConfig.ObstacleOption option : day.obstacles()) {
            if (random.nextDouble() * 100 < option.chancePercent()) selected.add(option);
        }
        List<Integer> lanes = new ArrayList<>();
        for (int lane = 0; lane < GameConfig.LANE_COUNT; lane++) lanes.add(lane);
        Collections.shuffle(lanes, random);
        Collections.shuffle(selected, random); // Nếu gần đầy, không luôn ưu tiên loại khai báo trước.
        int count = Math.min(selected.size(), Math.min(slots, GameConfig.LANE_COUNT - 1));
        int spawned = 0;
        for (DayConfig.ObstacleOption option : selected) {
            if (spawned >= count) break;
            for (int index = 0; index < lanes.size(); index++) {
                Obstacle candidate = option.factory().apply(lanes.get(index), (double) spawned * day.spawnYGap());
                if (obstacles.stream().anyMatch(other -> other.blocksSpawn(candidate) || candidate.blocksSpawn(other))) continue;
                obstacles.add(candidate);
                lanes.remove(index);
                spawned++;
                break;
            }
        }
        // Kể cả khi hụt tất cả, vẫn tiêu thụ lượt xét để giữ đúng xác suất theo mỗi chu kỳ.
        cooldownSeconds = day.spawnIntervalSeconds();
        if (spawned == 0 && cooldownSeconds == 0) cooldownSeconds = EMPTY_WAVE_RETRY_SECONDS;
    }
}
