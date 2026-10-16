package game.model.obstacle;

import game.TestSupport;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReversePlaneTest {
    @Test void scheduledPlaneWaitsBeforeShowingAFullWarningAndRelocationKeepsItsSchedule() {
        var world=TestSupport.level(52);
        var spec=world.config().obstacles().stream().filter(s->s.type()==ObstacleType.ENEMY_PLANE).findFirst().orElseThrow();
        var plane=EnemyPlane.reverse(spec,380,world.config().values(),new Object(),.5).atSpawnX(404);
        assertTrue(plane.awaitingWarning());assertFalse(plane.warning());assertFalse(plane.harmfulAt(1.74));
        assertTrue(plane.harmfulAt(1.75));assertFalse(plane.harmfulAt(2.25));
        plane.update(.49,world);assertTrue(plane.awaitingWarning());
        plane.update(.01,world);assertTrue(plane.warning());assertFalse(plane.awaitingWarning());assertEquals(0,plane.warningElapsedSeconds(),1e-8);
        plane.update(1.25,world);assertFalse(plane.warning());assertEquals(720,plane.y());
        var predicted=plane.predictedHitbox(.25);plane.update(.25,world);assertEquals(predicted,plane.hitbox());
        plane.update(.25,world);assertTrue(plane.removed());assertTrue(plane.y()+plane.height()<0);
    }
    @Test void warningDoesNotMoveOrDamageThenFullPlaneCrossesScreenInHalfASecond() {
        var world=TestSupport.level(52);
        var spec=world.config().obstacles().stream().filter(s->s.type()==ObstacleType.ENEMY_PLANE).findFirst().orElseThrow();
        var plane=EnemyPlane.reverse(spec,380,world.config().values(),null);world.addObstacle(plane);
        assertTrue(plane.warning());assertFalse(plane.harmfulAt(0));assertEquals(720,plane.y());
        plane.update(1.24,world);assertTrue(plane.warning());assertEquals(720,plane.y());
        plane.update(.01,world);assertFalse(plane.warning());assertTrue(plane.harmfulAt(0));assertEquals(720,plane.y());
        var predicted=plane.predictedHitbox(.25);plane.update(.25,world);
        assertEquals(predicted,plane.hitbox());assertEquals(280,plane.y(),1e-8);
        assertTrue(plane.collisionShape().contains(plane.x()+plane.width()/2,plane.y()+1));
        assertFalse(plane.collisionShape().contains(plane.x()+1,plane.y()+plane.height()-1));
        plane.hitByPlayerRocket();assertFalse(plane.removed());plane.hitByPlayerRocket();assertTrue(plane.removed());
        var leaving=EnemyPlane.reverse(spec,380,world.config().values(),null);
        leaving.update(1.75,world);assertTrue(leaving.removed());assertTrue(leaving.y()+leaving.height()<0);
    }
}
