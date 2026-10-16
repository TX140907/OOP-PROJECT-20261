package game.level.day5;
import game.config.DayConfigLoader;
import game.level.BaseLevel;
/** Independent level session; shared engine and systems are inherited through BaseLevel. */
public final class Day5HelicopterLevel extends BaseLevel {
    public Day5HelicopterLevel() { super(DayConfigLoader.load(Day5HelicopterLevel.class,"day5-2.properties")); }
}
