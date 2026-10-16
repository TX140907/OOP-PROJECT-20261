package game.level;
import game.model.Player;
import game.model.obstacle.Obstacle;
import java.util.List;
public interface Level {
    DayConfig config();
    void update(double delta,int direction);
    Player player();
    List<Obstacle> obstacles();
    boolean complete();
}
