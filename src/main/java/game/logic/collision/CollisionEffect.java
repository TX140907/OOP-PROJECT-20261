package game.logic.collision;
import game.level.BaseLevel;
public interface CollisionEffect {
    CollisionKind kind();
    void apply(BaseLevel level);
}
