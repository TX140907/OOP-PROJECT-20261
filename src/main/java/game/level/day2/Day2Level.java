package game.level.day2;

import game.level.DayConfig;
import game.level.DayConfigLoader;
import game.level.Level;

/** Level ngày 2: cá mập cảnh báo rồi trồi lên đớp. */
public final class Day2Level implements Level {
    private static final int DAY = 2; // Số ngày.
    private final DayConfig config = DayConfigLoader.load(DAY);

    @Override
    public DayConfig config() {
        return config;
    }
}
