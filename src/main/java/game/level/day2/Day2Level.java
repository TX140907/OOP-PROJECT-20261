package game.level.day2;
import game.config.DayConfigLoader;
import game.level.BaseLevel;
/** Independent level session; shared engine and systems are inherited through BaseLevel. */
public final class Day2Level extends BaseLevel {
    public Day2Level() { super(DayConfigLoader.load(Day2Level.class,"day2.properties")); }
}
