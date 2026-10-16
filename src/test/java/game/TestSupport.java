package game;
import game.config.DayConfigLoader;
import game.level.*;
import game.model.obstacle.*;
import game.logic.collision.CollisionKind;
import game.logic.movement.LinearMovement;
import java.util.Properties;
public final class TestSupport {
    private TestSupport() {}
    public static DayConfig config(int id,String... pairs) {
        Properties p=Levels.create(id).config().copyProperties();
        p.setProperty("spawn.initialDelaySeconds","10000");p.setProperty("spawn.randomSeed","42");
        p.setProperty("boss.enabled","false"); // Các test hệ thống độc lập bật boss khi cần kiểm tra riêng encounter.
        for (int i=0;i<pairs.length;i+=2) p.setProperty(pairs[i],pairs[i+1]);
        return DayConfigLoader.from(p);
    }
    public static BaseLevel level(int id,String... pairs) { return new BaseLevel(config(id,pairs)) {}; }
    public static Obstacle object(ObstacleType type,CollisionKind kind,double x,double y,double width,double height,double vx,double vy) {
        DayConfig.ObstacleSpec spec=new DayConfig.ObstacleSpec(type,1,100,width,height,0,0,kind,false,"");
        return new Obstacle(spec,new LinearMovement(x,y,vx,vy)) {};
    }
}
