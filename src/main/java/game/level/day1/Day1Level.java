package game.level.day1;

import game.level.DayConfig;
import game.level.DayConfigLoader;
import game.level.Level;

/** Level ngày 1: xe di chuyển từ trên xuống. */
public final class Day1Level implements Level {
    private static final int DAY = 1; // Số ngày.
    private final DayConfig config = DayConfigLoader.load(DAY);

    @Override
    public DayConfig config() {
        return config;
    }
}
