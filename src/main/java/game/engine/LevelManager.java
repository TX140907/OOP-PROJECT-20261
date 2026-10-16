package game.engine;

import game.level.BaseLevel;
import game.level.Levels;
import java.util.*;
import java.util.function.Supplier;

/** Keeps session creation separate from order and transition decisions. */
public final class LevelManager {
    private final List<Supplier<BaseLevel>> factories;
    private int index;
    public LevelManager(int selectedId) {
        this(Levels.ORDER.stream().<Supplier<BaseLevel>>map(id->()->Levels.create(id)).toList(),Levels.ORDER.indexOf(selectedId));
    }
    public LevelManager(List<Supplier<BaseLevel>> factories,int index) {
        if (factories.isEmpty() || index<0 || index>=factories.size()) throw new IllegalArgumentException("Invalid level selection");
        this.factories=List.copyOf(factories); this.index=index;
    }
    public BaseLevel create() { return factories.get(index).get(); }
    public boolean hasNext() { return index+1<factories.size(); }
    public BaseLevel next() { if (!hasNext()) throw new IllegalStateException("No next level"); index++; return create(); }
    public int index() { return index; }
}
