package game.model.obstacle;
import game.level.DayConfig.ObstacleSpec;
import game.level.BaseLevel;
import game.logic.collision.CollisionEffect;
import game.logic.collision.CollisionKind;
import game.logic.collision.ConfiguredCollision;
import game.logic.movement.LinearMovement;
import java.awt.geom.Rectangle2D;
/** Xe chạy dọc đường: đâm trực diện đầu xe chí mạng, chạm cạnh hoặc phía sau gây sát thương thường. */
public class Vehicle extends Obstacle {
    private Rectangle2D previousHitbox;
    public Vehicle(ObstacleSpec spec,double x,double y,double speed) {
        super(spec,new LinearMovement(x,y,0,speed));previousHitbox=hitbox();
    }
    @Override public void update(double dt,BaseLevel world) {
        previousHitbox=hitbox();super.update(dt,world);
    }
    @Override public CollisionEffect contactEffect(BaseLevel world) {
        if(effect().kind()!=CollisionKind.LETHAL)return effect();
        Rectangle2D playerBefore=world.player().previousHitbox(),playerNow=world.player().hitbox(),vehicleNow=hitbox();
        boolean front;
        if(previousHitbox.intersects(playerBefore)) {
            // Trường hợp bắt đầu trong vùng chạm: dùng mặt gần nhất, không coi mọi cạnh là đầu xe.
            double frontDepth=vehicleNow.getMaxY()-playerNow.getMinY();
            double backDepth=playerNow.getMaxY()-vehicleNow.getMinY();
            double sideDepth=Math.min(playerNow.getMaxX()-vehicleNow.getMinX(),vehicleNow.getMaxX()-playerNow.getMinX());
            front=frontDepth<=sideDepth && frontDepth<=backDepth;
        } else {
            // So thời điểm đi qua hai biên theo chuyển động tương đối của cả xe và người chơi.
            double dx=(playerNow.getX()-playerBefore.getX())-(vehicleNow.getX()-previousHitbox.getX());
            double dy=(playerNow.getY()-playerBefore.getY())-(vehicleNow.getY()-previousHitbox.getY());
            double xEntry=entryTime(playerBefore.getMinX(),playerBefore.getMaxX(),previousHitbox.getMinX(),previousHitbox.getMaxX(),dx);
            double yEntry=entryTime(playerBefore.getMinY(),playerBefore.getMaxY(),previousHitbox.getMinY(),previousHitbox.getMaxY(),dy);
            // Xe hướng xuống: mặt trước là mép dưới; đụng góc cùng lúc vẫn tính trực diện.
            front=dy<0 && yEntry+1e-9>=xEntry;
        }
        return front?effect():new ConfiguredCollision(CollisionKind.NORMAL_DAMAGE);
    }
    private static double entryTime(double min,double max,double otherMin,double otherMax,double movement) {
        if(movement>0)return (otherMin-max)/movement;
        if(movement<0)return (otherMax-min)/movement;
        return max>=otherMin && min<=otherMax?Double.NEGATIVE_INFINITY:Double.POSITIVE_INFINITY;
    }
}
