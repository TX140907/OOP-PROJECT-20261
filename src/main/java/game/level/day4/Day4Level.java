package game.level.day4;
import game.config.DayConfigLoader;
import game.level.BaseLevel;
/** Independent level session; shared engine and systems are inherited through BaseLevel. */
public final class Day4Level extends BaseLevel {
    public Day4Level() { super(DayConfigLoader.load(Day4Level.class,"day4.properties")); }
}
