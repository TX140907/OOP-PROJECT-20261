package game.level.day1;
import game.config.DayConfigLoader;
import game.level.BaseLevel;
/** Independent level session; shared engine and systems are inherited through BaseLevel. */
public final class Day1Level extends BaseLevel {
    public Day1Level() { super(DayConfigLoader.load(Day1Level.class,"day1.properties")); }
}
