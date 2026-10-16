package game.logic;
import game.TestSupport;
import game.level.*;
import game.model.obstacle.*;
import game.logic.collision.CollisionKind;
import game.logic.effect.EffectManager.Type;
import game.logic.spawn.*;
import game.config.GameConfig;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ObstacleSpawnerTest {
    @Test void configuredSpawnerIsPerSession() { assertNotSame(TestSupport.level(1).spawner(),TestSupport.level(1).spawner()); }
    @Test void wallAcrossAllThreeLanesHasNoReachablePath() {
        BaseLevel w=TestSupport.level(1,"spawn.minimumGap","0");
        for(int lane=0;lane<2;lane++)w.addObstacle(TestSupport.object(ObstacleType.VEHICLE,CollisionKind.LETHAL,140+lane*240,220,240,130,0,200));
        Obstacle third=TestSupport.object(ObstacleType.VEHICLE,CollisionKind.LETHAL,620,220,240,130,0,200);
        assertFalse(w.safety().accepts(w,third));
    }
    @Test void currentVelocityFreezeAndReactionWindowAreConsidered() {
        BaseLevel w=TestSupport.level(1);w.effects().add(Type.FREEZE,2);
        Obstacle fast=TestSupport.object(ObstacleType.VEHICLE,CollisionKind.LETHAL,400,200,200,120,0,200);
        assertFalse(w.safety().accepts(w,fast));
        assertTrue(TestSupport.level(1).safety().accepts(TestSupport.level(1),fast),"Without freezing there is enough time to move aside");
        BaseLevel moving=TestSupport.level(1);for(int i=0;i<8;i++)moving.player().update(1.0/60,1,moving.effects());
        assertTrue(moving.player().velocity()>0);
        assertFalse(moving.safety().accepts(moving,TestSupport.object(ObstacleType.VEHICLE,CollisionKind.LETHAL,140,450,720,200,0,200)));
    }
    @Test void overlapPlayerAndOtherTrafficRejected() {
        BaseLevel w=TestSupport.level(1);Obstacle atPlayer=TestSupport.object(ObstacleType.VEHICLE,CollisionKind.LETHAL,w.player().x(),w.player().y(),100,100,0,180);
        assertFalse(w.safety().accepts(w,atPlayer));
        Obstacle old=TestSupport.object(ObstacleType.VEHICLE,CollisionKind.LETHAL,200,0,100,150,0,180);w.addObstacle(old);
        assertFalse(w.safety().accepts(w,TestSupport.object(ObstacleType.VEHICLE,CollisionKind.LETHAL,200,-20,100,150,0,200)));
    }
    @Test void crossingForecastAndDriftMatchActualMovement() {
        BaseLevel w=TestSupport.level(2);var spec=w.config().obstacles().get(2);
        FloatingCrate crate=new FloatingCrate(spec,800,-100,50,130,140,775);
        var expected=crate.predictedHitbox(2);crate.update(2,w);
        assertEquals(expected.getX(),crate.hitbox().getX(),1e-8);assertEquals(expected.getY(),crate.hitbox().getY(),1e-8);
        Obstacle crossing=TestSupport.object(ObstacleType.DOG,CollisionKind.NORMAL_DAMAGE,0,520,75,50,180,0);
        assertEquals(360,crossing.predictedHitbox(2).getX());
    }
    @Test void weightedSelectionAndIndependentModeBothWork() {
        BaseLevel w=TestSupport.level(1,"spawn.initialDelaySeconds","0","spawn.horizonSeconds","0.6","spawn.safeReactionTimeSeconds","0.05");
        Map<ObstacleType,Integer> counts=new EnumMap<>(ObstacleType.class);
        for(int i=0;i<1000;i++) {
            for(Obstacle old:w.obstacles())old.remove();w.update(0,0);w.spawner().update(100,w);
            for(Obstacle o:w.obstacles())counts.merge(o.spec().type(),1,Integer::sum);
        }
        assertTrue(counts.getOrDefault(ObstacleType.VEHICLE,0)>450);assertTrue(counts.getOrDefault(ObstacleType.BICYCLE,0)>120);assertTrue(counts.getOrDefault(ObstacleType.BARRIER,0)>170);
        BaseLevel both=TestSupport.level(1,"spawn.mode","independent","vehicle.chance","100","bicycle.chance","0","barrier.chance","100","spawn.initialDelaySeconds","0","spawn.horizonSeconds","0.6","spawn.safeReactionTimeSeconds","0.05","spawn.attempts","50");
        both.spawner().update(0,both);assertEquals(2,both.obstacles().size());
        BaseLevel none=TestSupport.level(1,"spawn.mode","independent","vehicle.chance","0","bicycle.chance","0","barrier.chance","0","spawn.initialDelaySeconds","0");
        none.spawner().update(0,none);assertTrue(none.obstacles().isEmpty());
    }
    @Test void maxActiveAndDifficultyAndCleanup() {
        BaseLevel w=TestSupport.level(1,"spawn.maxActiveObstacles","1","spawn.initialDelaySeconds","0");
        w.spawner().update(0,w);assertTrue(w.obstacles().size()<=1);w.spawner().update(100,w);assertTrue(w.obstacles().size()<=1);
        double initial=w.spawner().density(w);w.progress().update(45,1,1,1);assertTrue(w.spawner().density(w)>initial);
        for(Obstacle o:w.obstacles())o.remove();w.update(0,0);assertTrue(w.obstacles().isEmpty());
        Obstacle exit=TestSupport.object(ObstacleType.VEHICLE,CollisionKind.LETHAL,200,900,100,100,0,180);w.addObstacle(exit);exit.update(.1,w);w.update(0,0);assertTrue(exit.removed());
    }
    @Test void thousandSeededScenariosExerciseSafetyAndRejectImpossibleCandidates() {
        ObstacleFactory factory=new ObstacleFactory();int accepted=0,rejected=0;
        for(int seed=0;seed<1000;seed++) {
            BaseLevel w=TestSupport.level(1,"spawn.horizonSeconds","1.2","spawn.safeReactionTimeSeconds","0.2");
            if(seed%4==0) w.effects().add(Type.FREEZE,2);else w.effects().clear();
            Random r=new Random(seed);
            Obstacle candidate=seed%5==0?TestSupport.object(ObstacleType.VEHICLE,CollisionKind.LETHAL,140,300,720,150,0,200)
                    :seed%2==0?TestSupport.object(ObstacleType.VEHICLE,CollisionKind.LETHAL,
                        GameConfig.laneX(r.nextInt(3),150),100+r.nextInt(300),150,100,0,180+r.nextInt(100))
                    :factory.create(w.config().obstacles().get(r.nextInt(3)),w.config(),r);
            if(seed%3==0)w.addObstacle(TestSupport.object(ObstacleType.VEHICLE,CollisionKind.LETHAL,
                    GameConfig.laneX(r.nextInt(3),110),200+r.nextInt(200),110,120,0,180));
            if(w.safety().accepts(w,candidate)) {accepted++;assertFalse(candidate.hitbox().intersects(w.player().hitbox()));}
            else rejected++;
        }
        assertTrue(accepted>0 && rejected>=200);System.out.println("1000 seeded safety scenarios: accepted="+accepted+", rejected="+rejected);
    }
}
