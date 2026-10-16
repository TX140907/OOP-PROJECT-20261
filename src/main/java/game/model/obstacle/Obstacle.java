package game.model.obstacle;

import game.model.Entity;
import game.level.BaseLevel;
import game.level.DayConfig.ObstacleSpec;
import game.logic.collision.CollisionEffect;
import game.logic.collision.ConfiguredCollision;
import game.logic.collision.CollisionKind;
import game.logic.movement.MovementBehavior;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

/** Geometry and lifecycle only; rendering and collision policy are injected/composed. */
public abstract class Obstacle extends Entity {
    private final ObstacleSpec spec;
    private final MovementBehavior movement;
    private final CollisionEffect effect;
    private double age;
    private double scrollAge;
    private BaseLevel world;
    private boolean removed;
    protected Obstacle(ObstacleSpec spec,MovementBehavior movement) {
        super(movement.at(0).x(),movement.at(0).y(),spec.width(),spec.height(),spec.width(),spec.height(),0,0);
        this.spec=spec; this.movement=movement; effect=new ConfiguredCollision(spec.collision());
    }
    public void update(double dt,BaseLevel world) {
        this.world=world;
        age+=dt; scrollAge+=world.travelSecondsAhead(dt);
        var p=movement.at(age,scrollAge); position(p.x(),p.y());
        if (age>world.config().values().number("spawn.maxLifetimeSeconds") || y()>700+height()
                || x() < -width()-100 || x()>1100) remove();
    }
    public Rectangle2D predictedHitbox(double seconds) {
        var p=movement.at(age+seconds,scrollAge+(world==null?seconds:world.travelSecondsAhead(seconds)));
        return hitboxAt(p.x(),p.y());
    }
    /** Vùng va chạm thực; hitbox chữ nhật vẫn dùng làm giới hạn bao ngoài khi dự báo spawn. */
    public Shape collisionShape() { return hitbox(); }
    /** Vùng giữ chỗ trên làn; vật đang lăn tới làn có thể giữ chỗ trước tại điểm dừng. */
    public Rectangle2D laneReservation() { return predictedHitbox(0); }
    public void attachTo(BaseLevel world) { this.world=world; }
    public boolean allowsOverlap(Obstacle other) { return false; }
    public boolean activeAt(double seconds) {
        return !removed && (world==null || age+seconds<=world.config().values().number("spawn.maxLifetimeSeconds"));
    }
    public boolean harmfulAt(double seconds) {
        return activeAt(seconds) && effect.kind()!=CollisionKind.ENERGY && effect.kind()!=CollisionKind.HEAL && effect.kind()!=CollisionKind.SHIELD;
    }
    /** Vật cản có thể chặn và bị phá bởi tên lửa người chơi; đạn địch tự khai báo ngoại lệ. */
    public boolean blocksPlayerRocket() { return harmfulAt(0); }
    /** Tên lửa bị chặn ở vật trúng đầu tiên; mỗi loại vật tự xử lý độ bền của nó. */
    public void hitByPlayerRocket() { remove(); }
    public boolean occupiesAt(double seconds) { return activeAt(seconds); }
    public boolean removed() { return removed; }
    public void remove() { removed=true; }
    public ObstacleSpec spec() { return spec; }
    public CollisionEffect effect() { return effect; }
    /** Hiệu ứng tại lúc bắt đầu tiếp xúc; xe có thể phân biệt va vào đầu hoặc cạnh bên. */
    public CollisionEffect contactEffect(BaseLevel world) { return effect; }
    public double age() { return age; }
}
