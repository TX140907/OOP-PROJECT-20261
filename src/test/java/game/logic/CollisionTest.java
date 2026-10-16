package game.logic;
import game.TestSupport;
import game.level.BaseLevel;
import game.logic.collision.*;
import game.logic.effect.EffectManager.Type;
import game.model.obstacle.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CollisionTest {
    private Obstacle touching(BaseLevel w,CollisionKind kind) {
        return TestSupport.object(ObstacleType.BARRIER,kind,w.player().x(),w.player().y(),60,90,0,0);
    }
    @Test void normalDamageOncePerContactAndInvulnerability() {
        BaseLevel w=TestSupport.level(1);w.addObstacle(touching(w,CollisionKind.NORMAL_DAMAGE));
        for(int i=0;i<150;i++) w.update(1.0/60,0);
        assertEquals(2,w.player().health());
    }
    @Test void lethalBypassesInvulnerabilityRegardlessOfOrder() {
        for(boolean lethalFirst:new boolean[]{true,false}) {
            BaseLevel w=TestSupport.level(1);w.player().hurt(1);assertTrue(w.player().invulnerability()>0);
            Obstacle normal=touching(w,CollisionKind.NORMAL_DAMAGE),lethal=touching(w,CollisionKind.LETHAL);
            w.addObstacle(lethalFirst?lethal:normal);w.addObstacle(lethalFirst?normal:lethal);
            w.update(1.0/60,0);assertTrue(w.player().dead());
        }
    }
    @Test void movingVehicleLethalButParkedTrafficIsNormal() {
        BaseLevel car=TestSupport.level(1);var s=car.config().obstacles().get(0);
        car.addObstacle(new Vehicle(s,car.player().x(),car.player().y(),200));car.update(1.0/60,0);assertTrue(car.player().dead());
        BaseLevel jam=TestSupport.level(51);var js=jam.config().obstacles().get(0);
        jam.addObstacle(new TrafficJamVehicle(js,jam.player().x(),jam.player().y(),0));jam.update(1.0/60,0);assertEquals(2,jam.player().health());
    }
    @Test void snowballFreezesWithoutDamage() {
        BaseLevel w=TestSupport.level(3);w.addObstacle(touching(w,CollisionKind.FREEZE));w.update(1.0/60,1);
        assertEquals(3,w.player().health());assertEquals(0,w.player().velocity());assertTrue(w.effects().active(Type.FREEZE));
    }
    @Test void drainDamagesAndSlowsWhileIceOnlySlides() {
        BaseLevel w=TestSupport.level(3);w.addObstacle(touching(w,CollisionKind.SLIDE));w.addObstacle(touching(w,CollisionKind.SLOW));w.update(1.0/60,0);
        assertEquals(2,w.player().health());assertTrue(w.player().blinking());assertTrue(w.effects().active(Type.SLIDE));assertTrue(w.effects().active(Type.DRAIN));
    }
    @Test void normalHitSlowsProgressWithoutMovingBack() {
        BaseLevel w=TestSupport.level(1);w.addObstacle(touching(w,CollisionKind.NORMAL_DAMAGE));w.update(.1,0);
        assertTrue(w.progress().fraction()>0);assertTrue(w.progress().fraction()<.1/60);assertTrue(w.effects().active(Type.HIT));
    }
    @Test void zeroHealthAndAttackKillImmediately() {
        BaseLevel w=TestSupport.level(1,"level.initialHealth","1");w.addObstacle(touching(w,CollisionKind.NORMAL_DAMAGE));w.update(.01,0);assertTrue(w.player().dead());
        BaseLevel s=TestSupport.level(2);var spec=s.config().obstacles().get(0);Shark shark=new Shark(spec,s.player().x(),s.player().y(),s.config().values());
        shark.beginWarning();s.addObstacle(shark);s.update(.1,0);assertFalse(s.player().dead());
        shark.update(1.3,s);s.update(.01,0);assertTrue(s.player().dead());
    }
    @Test void drainContactDoesNotDrainHealthEveryFrameButNewContactCanHurtAgain() {
        BaseLevel w=TestSupport.level(2);var spec=w.config().obstacles().stream().filter(o->o.type()==ObstacleType.DRAIN).findFirst().orElseThrow();
        w.addObstacle(new Drain(spec,w.player().x(),w.player().y(),0));
        for(int i=0;i<100;i++)w.update(1.0/60,0);
        assertEquals(2,w.player().health());
        w.addObstacle(new Drain(spec,w.player().x(),w.player().y(),0));w.update(1.0/60,0);
        assertEquals(1,w.player().health());assertTrue(w.player().blinking());
    }
}
