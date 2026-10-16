package game.model.obstacle;

import game.TestSupport;
import game.level.DayConfig.ObstacleSpec;
import game.logic.collision.CollisionKind;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BossProjectileTest {
    private final ObstacleSpec spec=new ObstacleSpec(ObstacleType.BOSS_PROJECTILE,0,0,50,50,0,0,CollisionKind.BOSS_DAMAGE,true,"");

    @Test void aimedShotWaitsAtHandThenFollowsPredictedTrajectoryThroughTheLockedTarget() {
        for(double sourceX:new double[]{116,884}) {
            var world=TestSupport.level(52);
            var shot=BossProjectile.aimed(spec,sourceX,220,500,565,800,.8,.3);
            shot.attachTo(world);
            double flight=Math.hypot(500-sourceX,565-220)/800;
            assertEquals(sourceX,shot.hitbox().getCenterX());assertEquals(220,shot.hitbox().getCenterY());
            assertFalse(shot.harmfulAt(.79));assertFalse(shot.occupiesAt(.79));assertFalse(shot.blocksPlayerRocket());
            assertTrue(shot.harmfulAt(.8));
            var target=shot.predictedHitbox(.8+flight);
            assertEquals(500,target.getCenterX(),1e-8);assertEquals(565,target.getCenterY(),1e-8);
            var expected=shot.predictedHitbox(1);shot.update(1,world);
            assertEquals(expected,shot.hitbox());assertFalse(shot.warning());
            assertTrue(shot.y()>220 && shot.y()<565);
            world.player().update(.5,1,world.effects());
            assertEquals(500,shot.targetArea(48).getCenterX());assertEquals(565,shot.targetArea(48).getCenterY());
            assertEquals(sourceX,shot.sourceX());
            shot.update(shot.lifetimeSeconds()-shot.age(),world);assertTrue(shot.removed());
        }
    }
    @Test void warningAndAimLineDoNoDamageButTheFlyingProjectileCanHitThePlayer() {
        var world=TestSupport.level(52);
        var shot=BossProjectile.aimed(spec,116,220,world.player().hitbox().getCenterX(),world.player().hitbox().getCenterY(),800,.8,.3);
        world.addObstacle(shot);
        world.update(.79,0);assertEquals(5,world.player().health());assertTrue(shot.warning());
        for(int i=0;i<100 && world.player().health()==5;i++)world.update(.01,0);
        assertEquals(4,world.player().health());assertTrue(shot.removed());
    }
}
