package game.level;

import game.level.day1.Day1Level;
import game.level.day2.Day2Level;
import game.level.day3.Day3Level;
import game.level.day4.Day4Level;
import game.level.day5.Day5RoadLevel;
import game.level.day5.Day5HelicopterLevel;
import java.util.*;
import java.util.function.Supplier;

/** Factories return fresh sessions; no shared player/effect/spawn state across days or restarts. */
public final class Levels {
    public static final List<Integer> ORDER=List.of(1,2,3,4,51,52);
    private static final Map<Integer,Supplier<BaseLevel>> FACTORIES=Map.of(
            1,Day1Level::new,2,Day2Level::new,3,Day3Level::new,4,Day4Level::new,
            51,Day5RoadLevel::new,52,Day5HelicopterLevel::new);
    private Levels() {}
    public static BaseLevel create(int id) {
        Supplier<BaseLevel> factory=FACTORIES.get(id);
        if (factory==null) throw new IllegalArgumentException("Unknown level: "+id);
        return factory.get();
    }
}
