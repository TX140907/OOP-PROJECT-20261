package game.level.day3;
import game.config.DayConfigLoader;
import game.level.BaseLevel;
/** Independent level session; shared engine and systems are inherited through BaseLevel. */
public final class Day3Level extends BaseLevel {
    public Day3Level() { super(DayConfigLoader.load(Day3Level.class,"day3.properties")); }
}
