package game.logic;

import game.TestSupport;
import game.config.GameConfig;
import game.level.BaseLevel;
import game.logic.spawn.PlaneSpawnPlacement;
import game.model.obstacle.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlaneSpawnPlacementTest {
    private EnemyPlane plane(BaseLevel world,int lane,double y,boolean armed,Object formation) {
        var spec=world.config().obstacles().stream().filter(s->s.type()==ObstacleType.ENEMY_PLANE).findFirst().orElseThrow();
        return new EnemyPlane(spec,GameConfig.laneX(lane,spec.width()),y,200,world.config().values(),armed,formation);
    }
    @Test void followingPlaneSpawnsWithOffsetAndPredictionMatchesActualPosition() {
        BaseLevel world=TestSupport.level(52);
        world.player().update(1,-1,world.effects());world.player().stop();
        EnemyPlane leader=plane(world,1,50,true,null);world.addObstacle(leader);
        EnemyPlane proposed=plane(world,1,-160,true,null);
        assertTrue(world.trySpawnGroup(List.of(proposed)));
        EnemyPlane follower=(EnemyPlane)world.obstacles().get(1);
        assertEquals(24,Math.abs(follower.x()-leader.x()));assertEquals(380,leader.x());
        assertEquals(proposed.y(),follower.y());assertTrue(follower.armed());assertNotSame(proposed,follower);
        var predicted=follower.predictedHitbox(.5);follower.update(.5,world);
        assertEquals(predicted,follower.hitbox());assertEquals(404,follower.x());
    }
    @Test void shiftedPairsStayInsideCentralRoadAndDoNotOverlapEachOther() {
        for(int[] lanes:new int[][]{{0,1},{1,2},{0,2}}) {
            BaseLevel world=TestSupport.level(52);Object formation=new Object();
            world.addObstacle(plane(world,lanes[0],100,false,new Object()));
            world.addObstacle(plane(world,lanes[1],100,false,new Object()));
            var proposed=List.of(plane(world,lanes[0],-160,false,formation),plane(world,lanes[1],-200,false,formation));
            var placed=PlaneSpawnPlacement.place(world,proposed).orElseThrow();assertEquals(2,placed.size());
            for(int i=0;i<placed.size();i++) {
                EnemyPlane actual=(EnemyPlane)placed.get(i);
                assertTrue(actual.x()>=GameConfig.ROAD_LEFT);assertTrue(actual.x()+actual.width()<=GameConfig.ROAD_RIGHT);
                assertEquals(24,Math.abs(actual.x()-proposed.get(i).x()));assertFalse(actual.armed());
                assertEquals(proposed.get(i).y(),actual.y());
            }
            assertFalse(placed.get(0).hitbox().intersects(placed.get(1).hitbox()));
            assertTrue(placed.get(0).allowsOverlap(placed.get(1)),"Formation identity must survive relocation");
        }
    }
    @Test void distantOrRemovedPlanesDoNotChangeSpawnAndConfiguredOffsetIsUsed() {
        BaseLevel world=TestSupport.level(52,"enemyPlane.tailOffsetX","36");
        EnemyPlane leader=plane(world,1,700,true,null);world.addObstacle(leader);
        EnemyPlane proposed=plane(world,1,-160,true,null);
        assertSame(proposed,PlaneSpawnPlacement.place(world,List.of(proposed)).orElseThrow().get(0));
        leader.remove();EnemyPlane close=plane(world,1,50,true,null);world.addObstacle(close);
        var shifted=PlaneSpawnPlacement.place(world,List.of(proposed)).orElseThrow().get(0);
        assertEquals(36,Math.abs(shifted.x()-proposed.x()));
        close.remove();assertSame(proposed,PlaneSpawnPlacement.place(world,List.of(proposed)).orElseThrow().get(0));
    }
    @Test void blockedOffsetsRejectWholeWaveWithoutMovingExistingPlanes() {
        BaseLevel world=TestSupport.level(52);
        EnemyPlane first=plane(world,0,100,false,null),second=plane(world,0,300,false,null).atSpawnX(164);
        world.addObstacle(first);world.addObstacle(second);
        assertFalse(world.trySpawnGroup(List.of(plane(world,0,-160,false,new Object()),plane(world,1,-200,false,new Object()))));
        assertEquals(List.of(first,second),world.obstacles());assertEquals(140,first.x());assertEquals(164,second.x());
    }
}
