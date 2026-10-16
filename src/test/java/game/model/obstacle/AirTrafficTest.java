package game.model.obstacle;

import game.TestSupport;
import game.level.*;
import game.logic.collision.CollisionKind;
import game.logic.spawn.ObstacleFactory;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AirTrafficTest {
    private static DayConfig.ObstacleSpec spec(BaseLevel level,ObstacleType type) {
        return level.config().obstacles().stream().filter(s->s.type()==type).findFirst().orElseThrow();
    }
    private static DayConfig.ObstacleSpec missileSpec() {
        return new DayConfig.ObstacleSpec(ObstacleType.MISSILE,0,0,18,36,0,0,CollisionKind.NORMAL_DAMAGE,true,"");
    }
    @Test void dogAndBirdStartAheadAndApproachWhileCrossing() {
        for(int id:List.of(51,52)) {
            BaseLevel world=id==52?TestSupport.level(id,"bird.stationaryChance","0"):TestSupport.level(id);
            Obstacle crossing=new ObstacleFactory().create(spec(world,id==51?ObstacleType.DOG:ObstacleType.BIRD),world.config(),new Random(42));
            world.addObstacle(crossing);
            assertEquals(world.player().y()-(id==51?world.config().values().number("dog.leadY"):300),crossing.y());
            assertTrue(crossing.y()+crossing.height()<world.player().y());
            double x=crossing.x(),y=crossing.y();var predicted=crossing.predictedHitbox(1);
            crossing.update(1,world);
            assertNotEquals(x,crossing.x());assertTrue(crossing.y()>y);
            assertEquals(predicted.getX(),crossing.hitbox().getX(),1e-8);
            assertEquals(predicted.getY(),crossing.hitbox().getY(),1e-8);
        }
    }
    @Test void missileWarnsThenFliesAtTwicePlayerSpeedWithoutRetargeting() {
        BaseLevel world=TestSupport.level(52);double speed=world.config().values().number("player.maxSpeed")*2;
        Missile missile=new Missile(missileSpec(),null,200,200,530,speed,.2);world.addObstacle(missile);
        assertTrue(missile.warning());assertFalse(missile.harmfulAt(0));assertFalse(missile.activeAt(.199));assertTrue(missile.activeAt(.2));
        missile.update(.1,world);assertEquals(200,missile.x());assertEquals(200,missile.y());
        var expected=missile.predictedHitbox(.2);missile.update(.2,world);
        assertFalse(missile.warning());assertTrue(missile.activeAt(0));
        assertEquals(200,missile.x());assertEquals(speed*.1,missile.y()-200,1e-8);
        assertEquals(expected.getX(),missile.x(),1e-8);assertEquals(expected.getY(),missile.y(),1e-8);
        world.player().update(.2,1,world.effects());assertEquals(209,missile.targetX());assertEquals(530,missile.targetY());
    }
    @Test void selectedBirdFliesInBeforeStoppingAndThenOnlyFollowsCamera() {
        BaseLevel world=TestSupport.level(52,"bird.stationaryChance","100");
        Bird bird=(Bird)new ObstacleFactory().create(spec(world,ObstacleType.BIRD),world.config(),new Random(42));
        world.addObstacle(bird);double x=bird.x(),y=bird.y();
        assertTrue(bird.x()+bird.width()<=0 || bird.x()>=1000);assertEquals(Bird.State.FLYING,bird.state());assertTrue(bird.willStop());
        bird.update(bird.stopSeconds()/2,world);assertNotEquals(x,bird.x());assertEquals(Bird.State.FLYING,bird.state());
        double remaining=bird.stopSeconds()-bird.age();var expected=bird.predictedHitbox(remaining);
        bird.update(remaining,world);assertEquals(Bird.State.STANDING,bird.state());assertEquals(bird.stopX(),bird.x());
        assertEquals(expected.getY(),bird.y(),1e-8);assertTrue(bird.y()+bird.height()<=world.player().hitbox().getMinY()+1e-8);
        double stillX=bird.x(),stillY=bird.y();bird.update(.25,world);
        assertEquals(stillX,bird.x());assertEquals(stillY+20,bird.y(),1e-8);
        world.effects().add(game.logic.effect.EffectManager.Type.FREEZE,1);stillY=bird.y();
        bird.update(.1,world);assertEquals(stillX,bird.x());assertEquals(stillY,bird.y());
    }
    @Test void defaultTwentyPercentSelectsBirdsThatWillStopNotBirdsThatSpawnStanding() {
        BaseLevel world=TestSupport.level(52);ObstacleFactory factory=new ObstacleFactory();Random random=new Random(20261);
        int selected=0;Set<Double> lanePositions=new HashSet<>();
        for(int i=0;i<2000;i++) {
            Bird bird=(Bird)factory.create(spec(world,ObstacleType.BIRD),world.config(),random);
            assertEquals(Bird.State.FLYING,bird.state());assertTrue(bird.x()+bird.width()<=0 || bird.x()>=1000);
            if(bird.willStop()) {selected++;lanePositions.add(bird.stopX());}
        }
        assertEquals(20,world.config().values().number("bird.stationaryChance"));
        assertTrue(selected>=340 && selected<=460,"Stopping birds: "+selected);assertEquals(3,lanePositions.size());
    }
    @Test void birdWorldHeightStaysFixedAndOnlyCameraTravelBringsItCloser() {
        BaseLevel world=TestSupport.level(52);
        Bird bird=new Bird(spec(world,ObstacleType.BIRD),200,220,180,world.config().values().number("level.scrollSpeed"));
        world.addObstacle(bird);double worldY=bird.y()-world.scroll();
        for(int i=0;i<30;i++)world.update(1.0/60,0);
        assertEquals(290,bird.x(),1e-8);assertEquals(worldY,bird.y()-world.scroll(),1e-8);
        double x=bird.x(),y=bird.y();world.effects().add(game.logic.effect.EffectManager.Type.FREEZE,1);
        for(int i=0;i<12;i++)world.update(1.0/60,0);
        assertTrue(bird.x()>x);assertEquals(y,bird.y(),1e-8);
        assertEquals(worldY,bird.y()-world.scroll(),1e-8);
    }
    @Test void warningCannotDamageButLiveMissileCan() {
        BaseLevel world=TestSupport.level(52);
        var spec=missileSpec();
        Missile missile=new Missile(spec,null,world.player().hitbox().getCenterX()-spec.width()/2,
                world.player().hitbox().getMinY(),600,840,.2);world.addObstacle(missile);
        world.update(.1,0);assertEquals(5,world.player().health());
        world.update(.1,0);assertEquals(4,world.player().health());assertTrue(missile.removed());
    }
    @Test void warningRendersAtTargetAndBlinksBeforeFlight() {
        BaseLevel world=TestSupport.level(52);
        Missile missile=new Missile(missileSpec(),null,200,200,530,840,.2);
        var renderer=new game.rendering.EntityRenderer(new game.rendering.AssetLoader());
        var image=new java.awt.image.BufferedImage(1000,700,java.awt.image.BufferedImage.TYPE_INT_ARGB);
        var g=image.createGraphics();renderer.obstacle(g,missile,world);g.dispose();
        assertNotEquals(0,image.getRGB(199,520));
        missile.update(.06,world);
        image=new java.awt.image.BufferedImage(1000,700,java.awt.image.BufferedImage.TYPE_INT_ARGB);
        g=image.createGraphics();renderer.obstacle(g,missile,world);g.dispose();
        assertEquals(0,image.getRGB(199,520));
    }
    @Test void actualPlaneCrossesFromTopAndFiresWithWarningDuringLevelUpdate() {
        BaseLevel world=TestSupport.level(52);
        EnemyPlane plane=new EnemyPlane(spec(world,ObstacleType.ENEMY_PLANE),200,-160,220,world.config().values());world.addObstacle(plane);
        for(int i=0;i<110 && world.obstacles().stream().noneMatch(o->o instanceof Missile);i++)world.update(1.0/60,0);
        Missile missile=(Missile)world.obstacles().stream().filter(o->o instanceof Missile).findFirst().orElseThrow();
        assertTrue(plane.y()>=70);assertTrue(plane.y()<280);assertTrue(missile.warning());
        assertEquals(world.player().hitbox().getCenterY(),missile.targetY());
        assertEquals(50,Math.abs(missile.targetX()-plane.hitbox().getCenterX()));
        assertEquals(missile.hitbox().getCenterX(),missile.targetX());
    }
    private BaseLevel planeFireWorld(boolean boss,int cap) {
        BaseLevel world=TestSupport.level(52,"boss.enabled",Boolean.toString(boss),"level.durationSeconds",boss?"0.05":"30",
                "boss.clearDelaySeconds","0.01","boss.warningSeconds","0.01","boss.entranceSeconds","0.01",
                "boss.healthLoadSeconds","0.01","boss.openingDelaySeconds","250","healthPickup.enabled","false",
                "boss.healthPickup.firstDelaySeconds","250","boss.shield.count","0",
                "spawn.maxActiveObstacles",Integer.toString(cap),"enemyPlane.firstShotDelaySeconds","0");
        if(boss) {
            for(int i=0;i<100 && !world.bossEncounter().active();i++)world.update(.01,0);
            assertTrue(world.bossEncounter().active());
            assertEquals(game.logic.boss.AirBossEncounter.Action.LANE_ATTACK,world.bossEncounter().action());
        }
        return world;
    }
    @Test void laneActionPlanesFireMoreOftenThanNormalPlanesWhileKeepingMissileWarnings() {
        for(boolean boss:List.of(false,true)) {
            BaseLevel world=planeFireWorld(boss,8);
            EnemyPlane plane=new EnemyPlane(spec(world,ObstacleType.ENEMY_PLANE),200,100,220,world.config().values());
            world.addObstacle(plane);
            Set<Missile> missiles=Collections.newSetFromMap(new IdentityHashMap<>());
            List<Double> shotTimes=new ArrayList<>();
            for(int tick=0;tick<70;tick++) {
                world.update(.01,0);
                for(var obstacle:world.obstacles())if(obstacle instanceof Missile missile && missiles.add(missile)) {
                    assertTrue(missile.warning());shotTimes.add(plane.age());
                }
            }
            assertEquals(boss?2:1,missiles.size());
            if(boss)assertEquals(world.config().values().number("boss.lane.planeShotIntervalSeconds"),shotTimes.get(1)-shotTimes.get(0),.011);
        }
    }
    @Test void laneActionPlanesRetrySoonAfterTrafficCapClearsWithoutExceedingIt() {
        for(boolean boss:List.of(false,true)) {
            BaseLevel world=planeFireWorld(boss,2);
            EnemyPlane plane=new EnemyPlane(spec(world,ObstacleType.ENEMY_PLANE),200,100,220,world.config().values());
            var blocker=TestSupport.object(ObstacleType.BARRIER,CollisionKind.NORMAL_DAMAGE,850,100,10,10,0,0);
            world.addObstacle(plane);world.addObstacle(blocker);world.update(.01,0);
            assertTrue(world.obstacles().stream().noneMatch(o->o instanceof Missile));
            blocker.remove();
            for(int tick=0;tick<17;tick++) {
                world.update(.01,0);assertTrue(world.obstacles().size()<=2);
            }
            assertEquals(boss?1:0,world.obstacles().stream().filter(o->o instanceof Missile).count());
        }
    }
    @Test void randomShotsUseBothSidesAndRespectActiveLimit() {
        Set<Integer> sides=new HashSet<>();Random seeds=new Random(20261);
        for(int seed=0;seed<20;seed++) {
            BaseLevel world=TestSupport.level(52,"spawn.randomSeed",Long.toString(seeds.nextLong()));
            var planeSpec=spec(world,ObstacleType.ENEMY_PLANE);
            EnemyPlane plane=new EnemyPlane(planeSpec,game.config.GameConfig.laneX(1,planeSpec.width()),100,220,world.config().values());world.addObstacle(plane);
            assertTrue(world.fireMissile(plane));
            Missile missile=(Missile)world.obstacles().get(1);
            sides.add((int)Math.signum(missile.targetX()-plane.hitbox().getCenterX()));
            assertTrue(missile.allowsOverlap(plane));
            assertFalse(missile.allowsOverlap(TestSupport.object(ObstacleType.BIRD,CollisionKind.NORMAL_DAMAGE,0,0,80,45,0,0)));
        }
        assertEquals(Set.of(-1,1),sides);
        BaseLevel capped=TestSupport.level(52,"spawn.maxActiveObstacles","1");
        EnemyPlane plane=new EnemyPlane(spec(capped,ObstacleType.ENEMY_PLANE),200,100,220,capped.config().values());capped.addObstacle(plane);
        assertFalse(capped.fireMissile(plane));assertEquals(1,capped.obstacles().size());
    }
}
