package game.logic;

import game.TestSupport;
import game.engine.*;
import game.level.*;
import game.logic.collision.CollisionKind;
import game.model.obstacle.*;
import java.util.*;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HealthPickupTest {
    private HealthPickup touching(BaseLevel w) {
        return new HealthPickup(new DayConfig.ObstacleSpec(ObstacleType.HEALTH_PICKUP,0,0,40,40,0,0,CollisionKind.HEAL,true,""),
                w.player().x(),w.player().y(),0);
    }
    @Test void collectsOnceHealsOneAndNeverExceedsMaxHealth() {
        BaseLevel w=TestSupport.level(52);w.player().hurt(2);HealthPickup item=touching(w);w.addObstacle(item);
        assertFalse(item.harmfulAt(0));w.update(0,0);assertEquals(4,w.player().health());assertTrue(item.removed());
        w.update(0,0);assertEquals(4,w.player().health());
        w.addObstacle(touching(w));w.update(0,0);assertEquals(5,w.player().health());
        w.addObstacle(touching(w));w.update(0,0);assertEquals(5,w.player().health());
    }
    @Test void cannotReviveAndLethalCollisionWinsOverHealing() {
        BaseLevel w=TestSupport.level(52);w.addObstacle(touching(w));
        w.addObstacle(TestSupport.object(ObstacleType.ENEMY_PLANE,CollisionKind.LETHAL,w.player().x(),w.player().y(),240,140,0,0));
        w.update(0,0);assertTrue(w.player().dead());assertEquals(0,w.player().heal(10));assertTrue(w.player().dead());
    }
    @Test void spawnsFromTopIsReachableAndActuallyCollects() {
        BaseLevel w=TestSupport.level(52,"healthPickup.firstDelaySeconds","0","healthPickup.speed","1000");w.player().hurt(2);
        w.update(0,0);
        HealthPickup item=(HealthPickup)w.obstacles().stream().filter(o->o instanceof HealthPickup).findFirst().orElseThrow();
        assertTrue(item.y()+item.height()<0);
        assertTrue(item.x()>=140 && item.x()+item.width()<=860);
        for(int i=0;i<60;i++)w.update(1.0/60,0);
        assertEquals(4,w.player().health());assertTrue(item.removed());
        assertTrue(w.obstacles().stream().noneMatch(o->o instanceof HealthPickup));
    }
    @Test void scheduleRepeatsWithConfiguredIntervalAndOnlyOneActiveItem() {
        BaseLevel w=TestSupport.level(52,"healthPickup.firstDelaySeconds","0","healthPickup.minIntervalSeconds","1",
                "healthPickup.maxIntervalSeconds","1","healthPickup.speed","1000");
        Set<Obstacle> seen=Collections.newSetFromMap(new IdentityHashMap<>());
        for(int i=0;i<150;i++) {
            w.update(1.0/60,0);var items=w.obstacles().stream().filter(o->o instanceof HealthPickup).toList();
            assertTrue(items.size()<=1);seen.addAll(items);
        }
        assertEquals(3,seen.size());
    }
    @Test void blockedPlacementRetriesAndOtherLevelsDoNotSpawnHealth() {
        BaseLevel w=TestSupport.level(52,"healthPickup.firstDelaySeconds","0");
        Obstacle wall=TestSupport.object(ObstacleType.BIRD,CollisionKind.NORMAL_DAMAGE,140,-100,720,120,0,0);w.addObstacle(wall);
        w.update(0,0);assertTrue(w.obstacles().stream().noneMatch(o->o instanceof HealthPickup));
        wall.remove();w.update(.5,0);assertTrue(w.obstacles().isEmpty());
        w.update(.51,0);assertTrue(w.obstacles().stream().anyMatch(o->o instanceof HealthPickup));
        BaseLevel road=TestSupport.level(51);for(int i=0;i<600;i++)road.update(1.0/60,0);
        assertTrue(road.obstacles().isEmpty());
    }
    @Test void pauseStopsScheduleAndMovementAndRestartResetsIt() {
        Game game=new Game(new LevelManager(List.<Supplier<BaseLevel>>of(()->TestSupport.level(52)),0),false);
        game.togglePause();game.update(20);assertTrue(game.level().obstacles().isEmpty());
        game.togglePause();game.update(14.01);HealthPickup item=(HealthPickup)game.level().obstacles().stream().filter(o->o instanceof HealthPickup).findFirst().orElseThrow();
        double y=item.y();game.togglePause();game.update(20);assertEquals(y,item.y());
        game.togglePause();game.level().player().kill();game.update(0);game.restart();
        assertTrue(game.level().obstacles().isEmpty());game.update(13.9);assertTrue(game.level().obstacles().isEmpty());
    }
}
