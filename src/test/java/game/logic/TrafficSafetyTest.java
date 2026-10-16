package game.logic;

import game.TestSupport;
import game.config.GameConfig;
import game.level.BaseLevel;
import game.logic.collision.CollisionKind;
import game.model.obstacle.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TrafficSafetyTest {
    private Obstacle car(BaseLevel w,int lane,double y) {
        var s=w.config().obstacles().stream().filter(o->o.type()==ObstacleType.TRAFFIC_JAM_VEHICLE).findFirst().orElseThrow();
        return new TrafficJamVehicle(s,GameConfig.laneX(lane,s.width()),y,w.config().values().number("level.scrollSpeed"));
    }
    @Test void rejectsAFullRoadWallEvenWhenItArrivesAfterTheOldHorizon() {
        BaseLevel w=TestSupport.level(51);w.addObstacle(car(w,0,-230));w.addObstacle(car(w,1,-230));
        Obstacle last=car(w,2,-230);last.attachTo(w);
        assertTrue(last.predictedHitbox(4.5).getMaxY()<w.player().hitbox().getMinY());
        assertTrue(w.safety().predictionHorizon(w,List.of(last))>7);
        assertFalse(w.safety().accepts(w,last));
    }
    @Test void noPhysicalGapAcrossStaggeredRowsIsRejected() {
        BaseLevel w=TestSupport.level(51);
        w.addObstacle(car(w,0,-200));w.addObstacle(car(w,1,-90));
        assertFalse(w.safety().accepts(w,car(w,2,20)));
    }
    @Test void wideHitboxesStillAllowARealOpenLane() {
        BaseLevel w=TestSupport.level(51);w.addObstacle(car(w,0,-230));
        assertTrue(w.safety().accepts(w,car(w,1,-230)));
    }
    @Test void dogStandingAfterChargeContinuesBlockingItsLaneInForecast() {
        BaseLevel w=TestSupport.level(51);var s=w.config().obstacles().stream().filter(o->o.type()==ObstacleType.DOG).findFirst().orElseThrow();
        Dog dog=new Dog(s,-120,w.player().y()-w.config().values().number("dog.leadY"),800,1,0,w.config().values());
        w.addObstacle(dog);dog.update(dog.chargeEnd(),w);
        assertEquals(Dog.State.STANDING,dog.state());assertTrue(dog.activeAt(.6));
        w.addObstacle(TestSupport.object(ObstacleType.TRAFFIC_JAM_VEHICLE,CollisionKind.NORMAL_DAMAGE,140,480,240,220,0,0));
        Obstacle right=TestSupport.object(ObstacleType.TRAFFIC_JAM_VEHICLE,CollisionKind.NORMAL_DAMAGE,620,480,240,220,0,0);
        assertFalse(w.safety().accepts(w,right));
    }
    @Test void generatedTrafficNeverClosesTheEntireRoadIncludingFutureArrivals() {
        int checked=0;
        for(int seed=0;seed<5;seed++) {
            BaseLevel w=TestSupport.level(51,"spawn.initialDelaySeconds","0","spawn.randomSeed",Integer.toString(seed),
                    "player.hitInvulnerabilitySeconds","1000");
            w.player().hurt(1); // Keep the simulation running even if this deliberately stationary player chooses badly.
            Set<Obstacle> previous=Collections.newSetFromMap(new IdentityHashMap<>());
            for(int tick=0;tick<900;tick++) {
                w.update(1.0/60,0);
                boolean spawned=w.obstacles().stream().anyMatch(o->!previous.contains(o));
                if(spawned) {
                    checked++;
                    for(double future=0;future<=12;future+=.1)
                        assertFalse(fullRoadBlocked(w,future),"seed="+seed+", tick="+tick+", future="+future);
                }
                previous.clear();previous.addAll(w.obstacles());
            }
        }
        assertTrue(checked>=10,"No meaningful traffic generated: "+checked);
    }
    private boolean fullRoadBlocked(BaseLevel w,double future) {
        var c=w.config().values();var p=w.player().hitbox();List<double[]> forbidden=new ArrayList<>();
        for(Obstacle o:w.obstacles()) if(o.harmfulAt(future)) {
            var r=o.predictedHitbox(future);
            if(r.getMaxY()<=p.getMinY() || r.getMinY()>=p.getMaxY())continue;
            forbidden.add(new double[]{r.getMinX()-c.number("player.hitboxOffsetX")-p.getWidth(),
                    r.getMaxX()-c.number("player.hitboxOffsetX")});
        }
        forbidden.sort(Comparator.comparingDouble(r->r[0]));double covered=c.number("player.minX");
        for(double[] range:forbidden) {
            if(range[0]>covered+1e-8)return false;
            covered=Math.max(covered,range[1]);
            if(covered>=c.number("player.maxX"))return true;
        }
        return false;
    }
}
