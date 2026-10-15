package game.level.day3;

import game.level.DayConfig;
import game.level.DayConfigLoader;
import game.level.Level;

/** Level ngày 3: cấu hình xe tạm, có thể chỉnh độc lập với ngày 1. */
public final class Day3Level implements Level {
    private static final int DAY = 3; // Số ngày.
    private final DayConfig config = DayConfigLoader.load(DAY);

    @Override
    public DayConfig config() {
        return config;
    }
}
