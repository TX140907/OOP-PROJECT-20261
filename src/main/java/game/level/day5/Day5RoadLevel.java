package game.level.day5;
import game.config.DayConfigLoader;
import game.level.BaseLevel;
/** Independent level session; shared engine and systems are inherited through BaseLevel. */
public final class Day5RoadLevel extends BaseLevel {
    public Day5RoadLevel() { super(DayConfigLoader.load(Day5RoadLevel.class,"day5-1.properties")); }
}
