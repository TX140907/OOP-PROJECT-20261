package game.model.obstacle;
import game.TestSupport;
import game.config.GameConfig;
import game.level.BaseLevel;
import game.logic.collision.CollisionKind;
import game.logic.spawn.ObstacleFactory;
import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;
class SharkTest {
    private Shark shark(BaseLevel world) {
        var spec=world.config().obstacles().stream().filter(s->s.type()==ObstacleType.SHARK).findFirst().orElseThrow();
        return (Shark)new ObstacleFactory().create(spec,world.config(),new Random(1));
    }
    @Test void finDiveWarningAttackAndDisappearHaveFullTimers() {
        BaseLevel world=TestSupport.level(2);Shark s=shark(world);world.addObstacle(s);
        double start=s.y();s.update(.3,world);assertTrue(s.y()>start);assertFalse(s.activeAt(0));
        s.update(.4,world);assertEquals(Shark.State.DIVING,s.state());double dive=s.y();
        s.update(.89,world);assertEquals(dive,s.y());assertEquals(Shark.State.DIVING,s.state());
        s.update(.02,world);assertEquals(Shark.State.WARNING,s.state());assertFalse(s.activeAt(0));
        double x=s.x(),y=s.y();assertEquals(world.player().hitbox().getCenterY(),s.hitbox().getCenterY());
        s.update(1.19,world);assertEquals(Shark.State.WARNING,s.state());
        s.update(.02,world);assertEquals(Shark.State.ATTACK,s.state());assertTrue(s.activeAt(0));
        assertEquals(x,s.x());assertEquals(y,s.y());s.update(.41,world);assertTrue(s.removed());
    }
    @Test void delayedFrameNeverSkipsWarning() {
        BaseLevel world=TestSupport.level(2);Shark s=shark(world);world.addObstacle(s);
        s.update(100,world);s.update(100,world);assertEquals(Shark.State.WARNING,s.state());assertFalse(s.activeAt(0));
    }
    @Test void occupiedTargetsWaitInsteadOfOverlapping() {
        BaseLevel world=TestSupport.level(2);Shark s=shark(world);world.addObstacle(s);
        for(int lane=0;lane<3;lane++) world.addObstacle(TestSupport.object(ObstacleType.VEHICLE,CollisionKind.LETHAL,GameConfig.laneX(lane,230),400,230,300,0,0));
        s.update(.7,world);s.update(.9,world);assertEquals(Shark.State.DIVING,s.state());
    }
}
