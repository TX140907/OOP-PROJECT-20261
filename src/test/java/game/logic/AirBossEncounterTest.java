package game.logic;

import game.TestSupport;
import game.config.GameConfig;
import game.engine.*;
import game.level.*;
import game.logic.boss.AirBossEncounter;
import game.logic.boss.AirBossEncounter.State;
import game.logic.collision.CollisionKind;
import game.model.obstacle.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AirBossEncounterTest {
    private BaseLevel level(String... overrides) {
        List<String> settings=new ArrayList<>(List.of("boss.enabled","true","level.durationSeconds","0.05",
                "boss.clearDelaySeconds","0.05","boss.warningSeconds","0.1","boss.entranceSeconds","0.1",
                "boss.healthLoadSeconds","0.2","boss.moveSeconds","0.1","boss.finishDelaySeconds","0.05",
                "boss.exitSeconds","0.1","boss.openingDelaySeconds","250","boss.healthPickup.firstDelaySeconds","250",
                "boss.shield.count","0","boss.lane.recoveryShotLimit","0","boss.reverse.shotChance","0",
                "boss.reverse.singleWeight","50","boss.reverse.pairWeight","50","boss.reverse.sequenceWeight","0","boss.waves.openingVolleyEnabled","false","boss.playerRocket.cooldownSeconds","1.2","boss.hitsToDefeat","8"));
        settings.addAll(List.of(overrides));return TestSupport.level(52,settings.toArray(String[]::new));
    }
    private void advanceTo(BaseLevel world,State state) {
        for(int i=0;i<600 && world.bossEncounter().state()!=state;i++)world.update(1.0/60,0);
        assertEquals(state,world.bossEncounter().state());
    }
    private void centerBossOnPlayer(BaseLevel world) {
        var boss=world.bossEncounter().boss();boss.moveTo(world.player().hitbox().getCenterX()-boss.width()/2,130,.01);boss.update(.01);
    }
    @Test void bossStartsWithLaneAttackRegardlessOfTheRandomActionChance() {
        assertEquals(2,Levels.create(52).config().values().integer("boss.startAction"));
        for(String chance:List.of("0","50","100")) {
            BaseLevel world=level("boss.action1Chance",chance);
            assertEquals(State.NORMAL,world.bossEncounter().state());
            advanceTo(world,State.ACTIVE);
            assertEquals(AirBossEncounter.Action.LANE_ATTACK,world.bossEncounter().action());
        }
    }
    @Test void everyWaveActionOpensWithExactlyOneShotPerLaneBeforeNormalAttacks() {
        BaseLevel world=level("boss.action1Chance","100","boss.startAction","1","boss.openingDelaySeconds","0.01",
                "boss.waves.openingVolleyEnabled","true","boss.waves.throwChance","0",
                "boss.waves.doubleThrowChance","100");
        assertTrue(Levels.create(52).config().values().bool("boss.waves.openingVolleyEnabled"));
        advanceTo(world,State.ACTIVE);world.player().grantShield(100);
        Set<Obstacle> seen=Collections.newSetFromMap(new IdentityHashMap<>());
        List<BossProjectile> opening=new ArrayList<>();List<Double> times=new ArrayList<>();
        int volleys=0;boolean normal=false;
        for(int i=0;i<4000 && volleys<2;i++) {
            world.update(.01,0);
            if(world.bossEncounter().action()!=AirBossEncounter.Action.PLANE_WAVES)continue;
            for(var obstacle:world.obstacles())if(seen.add(obstacle)) {
                if(obstacle instanceof EnemyPlane) {
                    if(!((EnemyPlane)obstacle).reverseFlight()) {
                        assertEquals(3,opening.size());
                        assertTrue(opening.stream().allMatch(Obstacle::removed));normal=true;
                    }
                } else if(obstacle instanceof BossProjectile projectile) {
                    if(normal) {
                        normal=false;opening.clear();times.clear();
                    }
                    if(opening.size()<3) {
                        assertTrue(projectile.aimed());
                        assertEquals(game.config.GameConfig.laneX(opening.size(),0),projectile.targetArea(1).getCenterX(),1e-8);
                        opening.add(projectile);times.add(world.bossEncounter().activeSeconds());
                        if(opening.size()==3) {
                            assertEquals(.4,times.get(1)-times.get(0),.011);
                            // Standing still during the middle warning can delay the next shot for safety.
                            assertTrue(times.get(2)-times.get(1)>=.4-1e-9);volleys++;
                        }
                    }
                }
            }
        }
        assertEquals(2,volleys,"shots="+opening.size()+", active="+world.bossEncounter().activeSeconds()+", action="+world.bossEncounter().action()+", obstacles="+world.obstacles().size());assertFalse(world.player().dead());
    }
    @Test void transitionsStartSmoothMovementAsSoonAsTrafficClearsAndAttackWhenMovementAndBriefGapFinish() {
        BaseLevel world=level("boss.action1Chance","50","boss.moveSeconds","1.5",
                "boss.waves.durationSeconds","0.1","boss.lane.durationSeconds","0.2",
                "boss.lane.planeChance","0","boss.lane.birdChance","0",
                "boss.reverse.durationSeconds","0.02","boss.reverse.waveCount","1",
                "boss.reverse.warningSeconds","0.01","boss.reverse.blinkSeconds","0.01",
                "boss.reverse.flightSeconds","0.01","boss.reverse.intervalSeconds","0.02");
        assertEquals(.1,world.config().values().number("boss.actionGapSeconds"));
        class Choice extends Random {double roll=.99;@Override public double nextDouble(){return roll;}}
        Choice choice=new Choice();var encounter=new AirBossEncounter(world,choice);
        world.progress().update(1,1,1,1);
        for(int i=0;i<600 && !encounter.active();i++)encounter.update(.01);
        assertTrue(encounter.active());assertEquals(AirBossEncounter.Action.LANE_ATTACK,encounter.action());
        var blocker=TestSupport.object(ObstacleType.BARRIER,CollisionKind.NORMAL_DAMAGE,200,100,50,50,0,0);
        world.addObstacle(blocker);encounter.update(1.5);encounter.update(2);
        assertEquals(AirBossEncounter.Action.LANE_ATTACK,encounter.action());
        double oldX=encounter.boss().x(),oldY=encounter.boss().y();
        blocker.remove();choice.roll=0;encounter.update(0);
        assertEquals(AirBossEncounter.Action.PLANE_WAVES,encounter.action());assertTrue(encounter.boss().moving());
        assertEquals(oldX,encounter.boss().x());assertEquals(oldY,encounter.boss().y());
        assertTrue(world.obstacles().stream().allMatch(Obstacle::removed));
        encounter.update(.5);assertTrue(encounter.boss().moving());assertNotEquals(oldX,encounter.boss().x());
        assertTrue(world.obstacles().stream().allMatch(Obstacle::removed));
        for(int i=0;i<110 && encounter.boss().moving();i++)encounter.update(.01);
        assertFalse(encounter.boss().moving());assertEquals(AirBossEncounter.Action.PLANE_WAVES,encounter.action());
        assertTrue(encounter.boss().x()+encounter.boss().width()<game.config.GameConfig.ROAD_LEFT
                || encounter.boss().x()>game.config.GameConfig.ROAD_RIGHT);
        assertTrue(world.obstacles().stream().anyMatch(o->!o.removed() && o instanceof EnemyPlane));
        world.obstacles().forEach(Obstacle::remove);encounter.update(.11);
        assertEquals(AirBossEncounter.Action.REVERSE_PLANES,encounter.action());assertFalse(encounter.boss().moving());
        assertEquals(0,encounter.reverseWavesSpawned()); // Already at the side; only the configured brief gap remains.
        encounter.update(.09);assertEquals(0,encounter.reverseWavesSpawned());
        encounter.update(.01);assertEquals(1,encounter.reverseWavesSpawned());
        world.obstacles().forEach(Obstacle::remove);choice.roll=.99;oldX=encounter.boss().x();encounter.update(.03);
        assertEquals(AirBossEncounter.Action.LANE_ATTACK,encounter.action());assertTrue(encounter.boss().moving());
        assertEquals(oldX,encounter.boss().x());
        for(int i=0;i<170 && encounter.boss().moving();i++)encounter.update(.01);
        assertFalse(encounter.boss().moving());
        assertEquals(1,world.obstacles().stream().filter(o->!o.removed() && o instanceof BossProjectile).count());
    }
    @Test void normalPartIsThirtySecondsAndHardDensityIsConstant() {
        var world=Levels.create(52);assertEquals(30,world.config().duration());assertNotNull(world.bossEncounter());
        double density=world.spawner().density(world);assertEquals(1.35,density);
        world.progress().update(29,1,1,1);assertEquals(density,world.spawner().density(world));
        assertFalse(world.config().copyProperties().containsKey("difficulty.phase1End"));
    }
    @Test void threeReversePatternsHaveEqualWeightsAndSelectionUsesAllThreeRanges() {
        for(String key:List.of("singleWeight","pairWeight","sequenceWeight"))
            assertEquals(33,Levels.create(52).config().values().number("boss.reverse."+key));
        for(int pattern=1;pattern<=3;pattern++) {
            var world=level("boss.action1Chance","100","boss.startAction","1","boss.waves.durationSeconds","0.1","boss.lane.durationSeconds","0.2",
                    "boss.moveSeconds","0.01","boss.reverse.singleWeight","33","boss.reverse.pairWeight","33","boss.reverse.sequenceWeight","33");
            double roll=(pattern-.5)/3;
            var encounter=new AirBossEncounter(world,new Random(52) {@Override public double nextDouble(){return roll;}});
            world.progress().update(1,1,1,1);
            for(int i=0;i<200 && encounter.reverseWavesSpawned()==0;i++)encounter.update(.01);
            assertEquals(1,encounter.reverseWavesSpawned());assertEquals(pattern,world.obstacles().size());
        }
    }
    @Test void reverseSequenceFliesLeftMiddleRightHalfASecondApartAndOutlastsNominalDuration() {
        var world=level("boss.action1Chance","100","boss.startAction","1","boss.waves.durationSeconds","0.1","boss.lane.durationSeconds","0.2",
                "boss.moveSeconds","0.01","boss.reverse.singleWeight","0","boss.reverse.pairWeight","0","boss.reverse.sequenceWeight","100",
                "boss.reverse.waveCount","1","boss.reverse.durationSeconds","1.75");
        advanceTo(world,State.ACTIVE);world.player().grantShield(100);
        for(int i=0;i<200 && world.obstacles().isEmpty();i++)world.update(.01,0);
        var planes=world.obstacles().stream().filter(o->o instanceof EnemyPlane).map(o->(EnemyPlane)o).sorted(Comparator.comparingDouble(Obstacle::x)).toList();
        assertEquals(3,planes.size());assertEquals(1,world.bossEncounter().reverseWavesSpawned());
        for(int lane=0;lane<3;lane++) {
            var plane=planes.get(lane);assertEquals(GameConfig.laneX(lane,plane.width()),plane.x());
            assertEquals(lane>0,plane.awaitingWarning());assertEquals(lane==0,plane.warning());
            double launch=1.25+lane*.5;
            assertFalse(plane.harmfulAt(launch-.01));assertTrue(plane.harmfulAt(launch));
        }
        double[] starts={-1,-1,-1};int ticks=0;
        for(;ticks<400;ticks++) {
            world.update(.01,0);
            for(int lane=0;lane<3;lane++)if(starts[lane]<0 && planes.get(lane).harmfulAt(0))starts[lane]=(ticks+1)*.01;
            if(ticks==199)assertEquals(AirBossEncounter.Action.REVERSE_PLANES,world.bossEncounter().action());
            if(world.bossEncounter().action()!=AirBossEncounter.Action.REVERSE_PLANES)break;
        }
        assertEquals(.5,starts[1]-starts[0],.011);assertEquals(.5,starts[2]-starts[1],.011);
        assertTrue(ticks*.01>=2.74);assertTrue(planes.stream().allMatch(Obstacle::removed));
        assertFalse(world.player().dead());
    }
    @Test void sequentialReverseGroupDoesNotPartiallySpawnOrChangePatternWhenCapIsTooSmall() {
        var world=level("boss.action1Chance","100","boss.startAction","1","boss.waves.durationSeconds","0.1","boss.lane.durationSeconds","0.2",
                "boss.moveSeconds","0.01","boss.reverse.singleWeight","0","boss.reverse.pairWeight","0","boss.reverse.sequenceWeight","100",
                "spawn.maxActiveObstacles","2");
        advanceTo(world,State.ACTIVE);for(int i=0;i<400;i++)world.update(.01,0);
        assertEquals(AirBossEncounter.Action.REVERSE_PLANES,world.bossEncounter().action());
        assertEquals(0,world.bossEncounter().reverseWavesSpawned());assertTrue(world.obstacles().isEmpty());
    }
    @Test void laneRecoveryFiresALimitedVolleyWhileDodgingAndThenStopsFeedingTheDrain() {
        var world=level("boss.action1Chance","0","boss.waves.durationSeconds","0.1","boss.lane.durationSeconds","0.2",
                "boss.moveSeconds","0.5","boss.lane.recoveryShotLimit","3","boss.lane.recoveryShotIntervalSeconds","0.8");
        advanceTo(world,State.ACTIVE);world.player().grantShield(100);
        var blocker=TestSupport.object(ObstacleType.BARRIER,CollisionKind.NORMAL_DAMAGE,200,100,50,50,0,0);world.addObstacle(blocker);
        Set<Obstacle> seen=Collections.newSetFromMap(new IdentityHashMap<>());List<Double> times=new ArrayList<>();
        for(int tick=0;tick<800;tick++) {
            world.update(.01,0);
            for(var obstacle:world.obstacles())if(seen.add(obstacle) && obstacle instanceof BossProjectile shot) {
                assertTrue(shot.aimed());assertTrue(shot.warning());assertFalse(shot.harmfulAt(0));
                times.add(world.bossEncounter().activeSeconds());
            }
        }
        assertEquals(3,times.size());
        assertTrue(times.get(1)-times.get(0)>=.8-1e-8);assertTrue(times.get(2)-times.get(1)>=.8-1e-8);
        assertTrue(world.bossEncounter().boss().moving());assertEquals(List.of(blocker),world.obstacles());
        blocker.remove();world.update(.01,0);
        for(int i=0;i<100 && world.bossEncounter().boss().moving();i++)world.update(.01,0);
        assertTrue(world.obstacles().stream().anyMatch(o->o instanceof EnemyPlane || o instanceof BossProjectile));
    }
    @Test void bossKeepsDodgingWhileLaneActionDrainsWhenRecoveryShotsAreDisabledAndPauseFreezesIt() {
        Game game=new Game(new LevelManager(List.<Supplier<BaseLevel>>of(()->level("boss.action1Chance","0",
                "boss.waves.durationSeconds","0.1","boss.lane.durationSeconds","0.2","boss.moveSeconds","0.5")),0),false);
        for(int i=0;i<600 && !game.level().bossEncounter().active();i++)game.update(.01);
        var world=game.level();var encounter=world.bossEncounter();var boss=encounter.boss();
        var blocker=TestSupport.object(ObstacleType.BARRIER,CollisionKind.NORMAL_DAMAGE,200,100,50,50,0,0);
        world.addObstacle(blocker);
        for(int i=0;i<100 && !boss.moving();i++)game.update(.01);
        // Allow the initial position change and short action to end; the blocker holds the drain open.
        for(int i=0;i<100;i++)game.update(.01);
        assertEquals(AirBossEncounter.Action.LANE_ATTACK,encounter.action());
        for(int sample=0;sample<30;sample++) {
            double previous=boss.x();
            for(int tick=0;tick<10;tick++)game.update(.01);
            assertNotEquals(previous,boss.x(),"Boss became a stationary target while draining action 2");
            assertTrue(boss.x()>=GameConfig.ROAD_LEFT);
            assertTrue(boss.x()+boss.width()<=GameConfig.ROAD_RIGHT);
            assertTrue(boss.moving());assertEquals(List.of(blocker),world.obstacles());
        }
        double pausedX=boss.x();game.togglePause();game.update(3);assertEquals(pausedX,boss.x());game.togglePause();
        blocker.remove();game.update(.01);
        for(int i=0;i<100 && boss.moving();i++)game.update(.01);
        assertFalse(boss.moving());
        assertTrue(world.obstacles().stream().anyMatch(o->o instanceof BossProjectile || o instanceof EnemyPlane));
    }
    @Test void rocketCooldownHonorsTheConfiguredInterval() {
        double configured=Levels.create(52).config().values().number("boss.playerRocket.cooldownSeconds");
        for(double cooldown:List.of(configured,.75,1.2)) {
            var world=level("boss.action1Chance","0","boss.playerRocket.cooldownSeconds",Double.toString(cooldown));
            advanceTo(world,State.ACTIVE);var encounter=world.bossEncounter();
            assertTrue(encounter.tryFire());encounter.updateProjectiles(cooldown-.01);assertFalse(encounter.tryFire());
            encounter.updateProjectiles(.01);assertTrue(encounter.tryFire());
        }
    }
    @Test void endOfNormalPartWaitsForOldTrafficAndStopsNewSpawnsAndMissiles() {
        BaseLevel world=level("spawn.initialDelaySeconds","0.06");
        var spec=world.config().obstacles().stream().filter(s->s.type()==ObstacleType.ENEMY_PLANE).findFirst().orElseThrow();
        EnemyPlane old=new EnemyPlane(spec,200,100,0,world.config().values());world.addObstacle(old);
        advanceTo(world,State.DRAINING);world.update(2,0);
        assertEquals(State.DRAINING,world.bossEncounter().state());assertFalse(world.complete());
        assertEquals(List.of(old),world.obstacles());assertFalse(world.allowsPlaneFire());
        old.remove();world.update(.01,0);assertEquals(State.DRAINING,world.bossEncounter().state());
        world.update(.06,0);assertEquals(State.WARNING,world.bossEncounter().state());
        assertFalse(world.bossEncounter().tryFire());
        advanceTo(world,State.LOADING);assertFalse(world.bossEncounter().tryFire());
        world.update(.1,0);assertEquals(State.LOADING,world.bossEncounter().state());
        assertEquals(.5,world.bossEncounter().loadingFraction(),1e-8);
        advanceTo(world,State.ACTIVE);assertTrue(world.bossEncounter().tryFire());
    }
    @Test void bossHealthOnlyFallsFromEightActualHitsAndExitWaitsForTraffic() {
        BaseLevel world=level("boss.action1Chance","0");advanceTo(world,State.ACTIVE);centerBossOnPlayer(world);
        var encounter=world.bossEncounter();encounter.update(2);assertEquals(1,encounter.boss().healthFraction());
        centerBossOnPlayer(world);
        for(int i=0;i<8;i++) {
            assertTrue(encounter.tryFire());assertFalse(encounter.tryFire());
            encounter.updateProjectiles(1.2);assertEquals((7-i)/8.0,encounter.boss().healthFraction());
        }
        assertTrue(encounter.hasFired());assertTrue(encounter.rockets().isEmpty());
        Obstacle remaining=TestSupport.object(ObstacleType.BIRD,CollisionKind.NORMAL_DAMAGE,0,100,20,20,0,0);world.addObstacle(remaining);
        world.update(.01,0);assertEquals(State.EXITING,encounter.state());assertFalse(encounter.tryFire());
        world.update(1,0);assertFalse(world.complete());remaining.remove();
        world.update(.06,0);assertTrue(world.complete());
    }
    @Test void playerRocketHitsNearestObstacleBeforeBossAndDoesNotTunnel() {
        BaseLevel world=level("boss.action1Chance","0");advanceTo(world,State.ACTIVE);centerBossOnPlayer(world);
        var encounter=world.bossEncounter();
        Obstacle cover=TestSupport.object(ObstacleType.BARRIER,CollisionKind.NORMAL_DAMAGE,
                world.player().hitbox().getCenterX()-40,400,80,40,0,0);world.addObstacle(cover);
        assertTrue(encounter.tryFire());encounter.updateProjectiles(1.2);
        assertTrue(cover.removed());assertEquals(1,encounter.boss().healthFraction());assertTrue(encounter.rockets().isEmpty());
        assertFalse(encounter.explosions().isEmpty());assertEquals(440,encounter.explosions().get(0).y());
        assertTrue(encounter.tryFire());encounter.updateProjectiles(1.2);assertEquals(7/8.0,encounter.boss().healthFraction());
    }
    @Test void planesAbsorbTwoRocketsAndProtectBossBehindThemUntilDestroyed() {
        for(boolean armed:new boolean[]{false,true}) {
            BaseLevel world=level("boss.action1Chance","0");advanceTo(world,State.ACTIVE);centerBossOnPlayer(world);
            var encounter=world.bossEncounter();
            var spec=world.config().obstacles().stream().filter(s->s.type()==ObstacleType.ENEMY_PLANE).findFirst().orElseThrow();
            var plane=new EnemyPlane(spec,world.player().hitbox().getCenterX()-spec.width()/2,350,0,world.config().values(),armed,null);
            world.addObstacle(plane);
            assertTrue(encounter.tryFire());encounter.updateProjectiles(1.2);
            assertFalse(plane.removed());assertTrue(plane.hitFlashing());assertTrue(plane.harmfulAt(0));
            assertTrue(encounter.rockets().isEmpty());assertTrue(encounter.explosions().isEmpty());assertEquals(1,encounter.boss().healthFraction());
            assertTrue(encounter.tryFire());encounter.updateProjectiles(1.2);
            assertTrue(plane.removed());assertEquals(1,encounter.explosions().size());assertEquals(1,encounter.boss().healthFraction());
            assertTrue(encounter.tryFire());encounter.updateProjectiles(1.2);
            assertEquals(7/8.0,encounter.boss().healthFraction());
        }
    }
    @Test void playerRocketPassesThroughLiveBossProjectilesAndEnemyMissilesWithoutDestroyingThem() {
        BaseLevel world=level("boss.action1Chance","0");advanceTo(world,State.ACTIVE);centerBossOnPlayer(world);
        double center=world.player().hitbox().getCenterX();
        var throwSpec=new DayConfig.ObstacleSpec(ObstacleType.BOSS_PROJECTILE,0,0,24,24,0,0,CollisionKind.BOSS_DAMAGE,true,"");
        var bossShot=new BossProjectile(throwSpec,center-12,400,550,0,.1,0,false);
        var missileSpec=new DayConfig.ObstacleSpec(ObstacleType.MISSILE,0,0,18,36,0,0,CollisionKind.NORMAL_DAMAGE,true,"");
        var missile=new Missile(missileSpec,null,center-9,350,550,1,.1);
        world.addObstacle(bossShot);world.addObstacle(missile);bossShot.update(.2,world);missile.update(.2,world);
        assertTrue(bossShot.harmfulAt(0));assertTrue(missile.harmfulAt(0));
        var encounter=world.bossEncounter();assertTrue(encounter.tryFire());encounter.updateProjectiles(1.2);
        assertEquals(7/8.0,encounter.boss().healthFraction());assertTrue(encounter.rockets().isEmpty());
        assertFalse(bossShot.removed());assertFalse(missile.removed());
        assertTrue(bossShot.harmfulAt(0));assertTrue(missile.harmfulAt(0));
        assertEquals(1,encounter.explosions().size());assertEquals(encounter.boss().hitbox().getMaxY(),encounter.explosions().get(0).y());
    }
    @Test void waveSpacingUsesConfiguredEdgeGapAndEqualSpeedsRatherThanAConstantDelay() {
        for(int gap:new int[]{75,150}) {
            BaseLevel world=level("boss.action1Chance","100","boss.startAction","1","boss.openingDelaySeconds","0.05","boss.waves.throwChance","0",
                    "boss.waves.gapPixels",Integer.toString(gap),"boss.waves.intervalSeconds","0.01","boss.waves.twoPlaneChance","100");
            advanceTo(world,State.ACTIVE);world.player().grantShield(100);
            for(int i=0;i<120 && world.obstacles().size()<2;i++)world.update(1.0/60,0);
            var previous=new ArrayList<>(world.obstacles());assertEquals(2,previous.size());
            // Đặt người chơi trong làn trống để đo nhịp sinh, không bị trì hoãn vì đang đứng chắn đường bay.
            int open=java.util.stream.IntStream.range(0,3).filter(l->previous.stream().noneMatch(o->
                    Math.abs(o.x()-game.config.GameConfig.laneX(l,o.width()))<1e-8)).findFirst().orElseThrow();
            if(open!=1)world.player().update(1,open==0?-1:1,world.effects());
            world.player().stop();
            for(int i=0;i<240 && world.obstacles().size()<4;i++)world.update(1.0/60,0);
            var next=world.obstacles().stream().filter(o->!previous.contains(o)).toList();assertEquals(2,next.size());
            double speed=previous.get(0).predictedHitbox(1).getY()-previous.get(0).y();
            for(var plane:next)assertEquals(speed,plane.predictedHitbox(1).getY()-plane.y(),1e-8);
            double actualGap=previous.stream().mapToDouble(Obstacle::y).min().orElseThrow()
                    -next.stream().mapToDouble(o->o.y()+o.height()).max().orElseThrow();
            assertTrue(actualGap>=gap-1e-8,"Expected at least "+gap+", got "+actualGap);
            assertTrue(actualGap<=gap+speed/60+1e-8,"Nominal gap delayed unexpectedly: "+actualGap);
        }
    }
    @Test void firstActionCanWarnAndThrowOnceOrTwiceInDifferentLanes() {
        for(int doubleChance:new int[]{0,100}) {
            BaseLevel world=level("boss.action1Chance","100","boss.startAction","1","boss.openingDelaySeconds","0.05","boss.waves.throwChance","100",
                    "boss.waves.doubleThrowChance",Integer.toString(doubleChance));
            advanceTo(world,State.ACTIVE);
            for(int i=0;i<120 && world.obstacles().isEmpty();i++)world.update(1.0/60,0);
            var shots=world.obstacles().stream().filter(o->o instanceof BossProjectile).map(o->(BossProjectile)o).toList();
            assertEquals(doubleChance==0?1:2,shots.size());
            assertEquals(shots.size(),shots.stream().map(o->o.targetArea(1).getCenterX()).distinct().count());
            for(var shot:shots) {
                assertTrue(shot.rowStrike());assertTrue(shot.warning());assertFalse(shot.harmfulAt(0));
                assertTrue(shot.aimed());
                assertEquals(world.player().hitbox().getCenterY(),shot.targetArea(1).getCenterY());
                assertEquals(world.bossEncounter().boss().y()+world.bossEncounter().boss().height()*.6,shot.sourceY());
                assertEquals(shot.sourceX(),shot.hitbox().getCenterX());
            }
        }
    }
    @Test void firstActionCreatesTwoDifferentLanesAndUnarmedPlanes() {
        BaseLevel world=level("boss.action1Chance","100","boss.startAction","1","boss.openingDelaySeconds","0.05","boss.waves.throwChance","0",
                "boss.waves.twoPlaneChance","100");
        advanceTo(world,State.ACTIVE);world.player().grantShield(100);
        for(int i=0;i<120 && world.obstacles().size()<2;i++)world.update(1.0/60,0);
        var planes=world.obstacles().stream().filter(o->o instanceof EnemyPlane).map(o->(EnemyPlane)o).toList();
        assertEquals(2,planes.size());assertNotEquals(planes.get(0).x(),planes.get(1).x());
        assertEquals(40,Math.abs(planes.get(0).y()-planes.get(1).y()),1e-8);
        for(var plane:planes) {
            assertFalse(plane.armed());assertTrue(plane.predictedHitbox(1).getY()-plane.y()>=240);
        }
        for(int i=0;i<75;i++)world.update(1.0/60,0);
        assertTrue(world.obstacles().stream().noneMatch(o->o instanceof Missile));
        assertTrue(world.bossEncounter().boss().x()+world.bossEncounter().boss().width()<=140 || world.bossEncounter().boss().x()>=860);
    }
    @Test void waveSizeCanBeOneOrTwoWithEqualConfiguredOdds() {
        Random seeds=new Random(20261);int single=0,pair=0;
        for(int sample=0;sample<400;sample++) {
            BaseLevel world=level("boss.action1Chance","100","boss.startAction","1","boss.openingDelaySeconds","0.01","boss.waves.throwChance","0",
                    "boss.waves.twoPlaneChance","50","spawn.randomSeed",Long.toString(seeds.nextLong()));
            advanceTo(world,State.ACTIVE);
            for(int i=0;i<120 && world.obstacles().isEmpty();i++)world.update(1.0/60,0);
            var planes=world.obstacles().stream().filter(o->o instanceof EnemyPlane).toList();
            assertTrue(planes.size()==1 || planes.size()==2);
            if(planes.size()==1)single++;else pair++;
            assertEquals(planes.size(),planes.stream().map(Obstacle::x).distinct().count());
        }
        assertTrue(single>=160 && single<=240,"One-plane waves: "+single);
        assertEquals(400,single+pair);
    }
    @Test void rapidShotsContinueWhenTwoPlaneWaveCannotFitAndFailedWaveNeverBecomesOnePlane() {
        BaseLevel world=level("boss.action1Chance","100","boss.startAction","1","boss.openingDelaySeconds","0.05","boss.waves.twoPlaneChance","100",
                "spawn.maxActiveObstacles","1","boss.waves.throwChance","100","boss.waves.doubleThrowChance","0",
                "boss.waves.throwIntervalSeconds","0.2","boss.waves.throwRetrySeconds","0.05");
        advanceTo(world,State.ACTIVE);int shots=0;
        for(int i=0;i<120;i++) {
            world.update(1.0/60,0);
            assertTrue(world.obstacles().stream().noneMatch(o->o instanceof EnemyPlane));
            for(var obstacle:world.obstacles())if(obstacle instanceof BossProjectile) {shots++;obstacle.remove();}
        }
        assertTrue(shots>=5,"Separate shot timer stalled behind blocked planes: "+shots);
    }
    @Test void secondActionCanThrowTwoDistinctLanesWithWarning() {
        BaseLevel world=level("boss.action1Chance","0","boss.openingDelaySeconds","0.05","boss.lane.doubleThrowChance","100",
                "boss.lane.planeChance","0","boss.lane.birdChance","0","boss.lane.changeIntervalSeconds","100");
        advanceTo(world,State.ACTIVE);
        for(int i=0;i<120 && world.obstacles().isEmpty();i++)world.update(1.0/60,0);
        var throwsList=world.obstacles().stream().filter(o->o instanceof BossProjectile).map(o->(BossProjectile)o).toList();
        assertEquals(2,throwsList.size());assertNotEquals(throwsList.get(0).x(),throwsList.get(1).x());
        for(var projectile:throwsList) {assertTrue(projectile.warning());assertFalse(projectile.harmfulAt(0));assertFalse(projectile.rowStrike());}
    }
    @Test void secondActionCanSummonAnArmedPlaneInsteadOfThrowing() {
        BaseLevel world=level("boss.action1Chance","0","boss.openingDelaySeconds","0.05","boss.lane.planeChance","100","boss.lane.birdChance","0");
        advanceTo(world,State.ACTIVE);
        for(int i=0;i<120 && world.obstacles().isEmpty();i++)world.update(1.0/60,0);
        assertTrue(world.obstacles().stream().anyMatch(o->o instanceof EnemyPlane plane && plane.armed()));
        assertTrue(world.obstacles().stream().noneMatch(o->o instanceof BossProjectile));
    }
    @Test void thirdActionFollowsFirstAndLaunchesExactlyThreeWarnedReverseWaves() {
        for(String key:List.of("singleWeight","pairWeight","sequenceWeight"))
            assertEquals(33,Levels.create(52).config().values().number("boss.reverse."+key));
        for(int chance:List.of(0,100))assertThreeReverseWaves(chance);
    }
    private void assertThreeReverseWaves(int chance) {
        BaseLevel world=level("boss.action1Chance","100","boss.startAction","1","boss.waves.durationSeconds","0.1",
                "boss.lane.durationSeconds","0.2","boss.moveSeconds","0.01","boss.reverse.singleWeight",Integer.toString(100-chance),"boss.reverse.pairWeight",Integer.toString(chance));
        advanceTo(world,State.ACTIVE);world.player().grantShield(100);
        for(int i=0;i<120 && world.bossEncounter().action()!=AirBossEncounter.Action.REVERSE_PLANES;i++)world.update(1.0/60,0);
        assertEquals(AirBossEncounter.Action.REVERSE_PLANES,world.bossEncounter().action());
        Set<EnemyPlane> seen=Collections.newSetFromMap(new IdentityHashMap<>());
        for(int i=0;i<375;i++) {
            world.update(1.0/60,0);
            if(world.bossEncounter().action()!=AirBossEncounter.Action.REVERSE_PLANES)break;
            for(var obstacle:world.obstacles())if(obstacle instanceof EnemyPlane plane && seen.add(plane)) {
                assertTrue(plane.reverseFlight());assertFalse(plane.armed());assertTrue(plane.warning());
                assertFalse(plane.harmfulAt(0));assertTrue(plane.activeAt(1.25));assertFalse(plane.activeAt(1.75));
                assertTrue(plane.x()>=140 && plane.x()+plane.width()<=860);
            }
        }
        assertEquals(chance==100?6:3,seen.size());assertEquals(3,world.bossEncounter().reverseWavesSpawned());
        assertTrue(seen.stream().allMatch(Obstacle::removed));assertFalse(world.player().dead());
        assertEquals(1,world.bossEncounter().boss().healthFraction());
    }
    @Test void reverseWaveChoiceIsEvenAndARejectedPairDoesNotTurnIntoASinglePlane() {
        int singles=0,pairs=0;
        Random seeds=new Random(51052);
        for(int sample=0;sample<120;sample++) {
            BaseLevel world=level("boss.action1Chance","100","boss.startAction","1","boss.waves.durationSeconds","0.1",
                    "boss.lane.durationSeconds","0.2","boss.moveSeconds","0.01","spawn.randomSeed",Long.toString(seeds.nextLong()));
            advanceTo(world,State.ACTIVE);
            for(int i=0;i<120 && world.obstacles().isEmpty();i++)world.update(1.0/60,0);
            long count=world.obstacles().stream().filter(o->o instanceof EnemyPlane).count();
            assertTrue(count==1 || count==2);if(count==1)singles++;else pairs++;
        }
        assertTrue(singles>=40 && pairs>=40,"Single/pair reverse waves: "+singles+"/"+pairs);
        BaseLevel blocked=level("boss.action1Chance","100","boss.startAction","1","boss.waves.durationSeconds","0.1",
                "boss.lane.durationSeconds","0.2","boss.moveSeconds","0.01",
                "boss.reverse.singleWeight","50","boss.reverse.pairWeight","50","spawn.maxActiveObstacles","1");
        class Rolls extends Random {double value=.75;@Override public double nextDouble(){return value;}}
        Rolls rolls=new Rolls();var encounter=new AirBossEncounter(blocked,rolls);blocked.progress().update(1,1,1,1);
        for(int i=0;i<120;i++)encounter.update(.01); // Roll a pair; the cap cannot accept it.
        rolls.value=.25; // A new roll would produce a single plane and fit, but retries must retain the pair.
        for(int i=0;i<300;i++)encounter.update(.01);
        assertEquals(AirBossEncounter.Action.REVERSE_PLANES,encounter.action());
        assertEquals(0,encounter.reverseWavesSpawned());assertTrue(blocked.obstacles().isEmpty());
    }
    @Test void aSecondConsecutiveWaveReverseSequenceIsReplacedByLaneActionAndTheLimitResets() {
        assertEquals(1,Levels.create(52).config().values().integer("boss.maxConsecutiveWaveSequences"));
        BaseLevel world=level("boss.action1Chance","100","boss.startAction","1","boss.waves.durationSeconds","0.1",
                "boss.lane.durationSeconds","0.2","boss.moveSeconds","0.01","boss.retrySeconds","0.01",
                "boss.reverse.waveCount","1","boss.reverse.durationSeconds","0.02","boss.reverse.intervalSeconds","0.02",
                "boss.reverse.warningSeconds","0.01","boss.reverse.blinkSeconds","0.01","boss.reverse.flightSeconds","0.01");
        advanceTo(world,State.ACTIVE);world.player().grantShield(100);
        List<AirBossEncounter.Action> actions=new ArrayList<>();actions.add(world.bossEncounter().action());
        for(int i=0;i<3000 && actions.size()<12;i++) {
            world.obstacles().forEach(Obstacle::remove);world.update(.01,0);
            var action=world.bossEncounter().action();if(actions.get(actions.size()-1)!=action)actions.add(action);
        }
        var lane=AirBossEncounter.Action.LANE_ATTACK;var reverse=AirBossEncounter.Action.REVERSE_PLANES;var wave=AirBossEncounter.Action.PLANE_WAVES;
        assertEquals(List.of(wave,reverse,lane,wave,reverse,lane,wave,reverse,lane,wave,reverse,lane),actions);
    }
    @Test void laneAttackNeverRepeatsEvenWhenRandomChoiceAlwaysPrefersIt() {
        BaseLevel world=level("boss.action1Chance","0","boss.waves.durationSeconds","0.1",
                "boss.lane.durationSeconds","0.2","boss.moveSeconds","0.01",
                "boss.reverse.waveCount","1","boss.reverse.durationSeconds","0.02","boss.reverse.intervalSeconds","0.02",
                "boss.reverse.warningSeconds","0.01","boss.reverse.blinkSeconds","0.01","boss.reverse.flightSeconds","0.01");
        advanceTo(world,State.ACTIVE);world.player().grantShield(100);
        List<AirBossEncounter.Action> actions=new ArrayList<>();actions.add(world.bossEncounter().action());
        for(int i=0;i<1500 && actions.size()<9;i++) {
            world.obstacles().forEach(Obstacle::remove);world.update(.01,0);
            var action=world.bossEncounter().action();if(actions.get(actions.size()-1)!=action)actions.add(action);
        }
        var lane=AirBossEncounter.Action.LANE_ATTACK;var wave=AirBossEncounter.Action.PLANE_WAVES;var reverse=AirBossEncounter.Action.REVERSE_PLANES;
        assertEquals(List.of(lane,wave,reverse,lane,wave,reverse,lane,wave,reverse),actions);
    }
    @Test void laneRecoveryWithActualBulletsStillHandsOffToWaveActionAtZeroProbability() {
        var world=level("boss.action1Chance","0","boss.waves.durationSeconds","0.1","boss.lane.durationSeconds","0.2",
                "boss.moveSeconds","0.1","boss.lane.recoveryShotLimit","3","boss.lane.recoveryShotIntervalSeconds","0.2",
                "boss.projectile.warningSeconds","0.2","boss.lane.throwSpeed","1000","boss.waves.throwActiveSeconds","0.05");
        advanceTo(world,State.ACTIVE);world.player().grantShield(100);
        var blocker=TestSupport.object(ObstacleType.BARRIER,CollisionKind.NORMAL_DAMAGE,200,100,50,50,0,0);world.addObstacle(blocker);
        Set<BossProjectile> shots=Collections.newSetFromMap(new IdentityHashMap<>());
        for(int i=0;i<500;i++) {
            world.update(.01,0);
            assertEquals(AirBossEncounter.Action.LANE_ATTACK,world.bossEncounter().action());
            for(var obstacle:world.obstacles())if(obstacle instanceof BossProjectile shot)shots.add(shot);
        }
        assertEquals(3,shots.size());assertTrue(shots.stream().allMatch(Obstacle::removed));
        blocker.remove();
        for(int i=0;i<600 && world.bossEncounter().action()==AirBossEncounter.Action.LANE_ATTACK;i++)world.update(.01,0);
        assertEquals(AirBossEncounter.Action.PLANE_WAVES,world.bossEncounter().action());
    }
    @Test void restartingAfterLaneToWaveHandoffResetsTheInitialChoice() {
        Game game=new Game(new LevelManager(List.<Supplier<BaseLevel>>of(()->level("boss.action1Chance","100",
                "boss.waves.durationSeconds","0.1","boss.lane.durationSeconds","0.2","boss.moveSeconds","0.01")),0),false);
        for(int i=0;i<600 && !game.level().bossEncounter().active();i++)game.update(.01);
        var previous=game.level().bossEncounter();assertEquals(AirBossEncounter.Action.LANE_ATTACK,previous.action());
        for(int i=0;i<600 && previous.action()==AirBossEncounter.Action.LANE_ATTACK;i++) {
            game.level().obstacles().forEach(Obstacle::remove);game.update(.01);
        }
        assertEquals(AirBossEncounter.Action.PLANE_WAVES,previous.action());
        game.level().player().kill();game.update(0);game.restart();assertNotSame(previous,game.level().bossEncounter());
        for(int i=0;i<600 && !game.level().bossEncounter().active();i++)game.update(.01);
        assertEquals(AirBossEncounter.Action.LANE_ATTACK,game.level().bossEncounter().action());
    }
    @Test void reverseActionRollsSeventyFivePercentOncePerWaveAndFiresWarnedDiagonalShotsFromHand() {
        assertEquals(75,Levels.create(52).config().values().number("boss.reverse.shotChance"));
        for(double roll:new double[]{.749,.75}) {
            BaseLevel world=level("boss.action1Chance","100","boss.startAction","1","boss.waves.durationSeconds","0.1",
                    "boss.lane.durationSeconds","0.2","boss.moveSeconds","0.01","boss.reverse.singleWeight","0","boss.reverse.pairWeight","100",
                    "boss.reverse.shotChance","75","boss.reverse.shotWarningSeconds","0.9","boss.reverse.shotSpeed","650");
            Random random=new Random(52) {@Override public double nextDouble(){return roll;}};
            var encounter=new AirBossEncounter(world,random);world.progress().update(1,1,1,1);world.player().grantShield(100);
            Set<Obstacle> seen=Collections.newSetFromMap(new IdentityHashMap<>());int shots=0;boolean entered=false,finished=false;
            for(int i=0;i<2400;i++) {
                for(var obstacle:world.obstacles())if(!obstacle.removed())obstacle.update(.01,world);
                world.update(0,0);encounter.update(.01);assertTrue(world.obstacles().size()<=8);
                if(encounter.action()==AirBossEncounter.Action.REVERSE_PLANES)entered=true;
                else if(entered) {finished=true;break;}
                for(var obstacle:world.obstacles())if(seen.add(obstacle) && obstacle instanceof BossProjectile shot) {
                    shots++;assertTrue(shot.aimed());assertTrue(shot.warning());assertFalse(shot.harmfulAt(0));
                    assertFalse(shot.blocksPlayerRocket());assertEquals(CollisionKind.BOSS_DAMAGE,shot.effect().kind());
                    var target=shot.targetArea(1);
                    assertNotEquals(shot.sourceX(),target.getCenterX());assertNotEquals(shot.sourceY(),target.getCenterY());
                    assertEquals(encounter.boss().y()+encounter.boss().height()*.6,shot.sourceY());
                    assertFalse(shot.harmfulAt(.89));assertTrue(shot.harmfulAt(.9));
                    double flight=Math.hypot(target.getCenterX()-shot.sourceX(),target.getCenterY()-shot.sourceY())/650;
                    var impact=shot.predictedHitbox(.9+flight);
                    assertEquals(target.getCenterX(),impact.getCenterX(),1e-8);assertEquals(target.getCenterY(),impact.getCenterY(),1e-8);
                }
            }
            assertTrue(finished);assertEquals(3,encounter.reverseWavesSpawned());assertEquals(roll<.75?3:0,shots);
        }
    }
    @Test void selectedReverseShotWaitsForSpaceAndDoesNotRerollAway() {
        BaseLevel world=level("boss.action1Chance","100","boss.startAction","1","boss.waves.durationSeconds","0.1",
                "boss.lane.durationSeconds","0.2","boss.moveSeconds","0.01","boss.reverse.singleWeight","100","boss.reverse.pairWeight","0",
                "boss.reverse.shotChance","75","spawn.maxActiveObstacles","1");
        class Rolls extends Random {double value=.74;@Override public double nextDouble(){return value;}}
        Rolls random=new Rolls();var encounter=new AirBossEncounter(world,random);world.progress().update(1,1,1,1);world.player().grantShield(100);
        for(int i=0;i<200 && encounter.reverseWavesSpawned()==0;i++)encounter.update(.01);
        assertEquals(1,encounter.reverseWavesSpawned());assertEquals(1,world.obstacles().size());
        assertTrue(world.obstacles().get(0) instanceof EnemyPlane);
        random.value=.99;boolean fired=false;
        for(int i=0;i<400 && !fired;i++) {
            for(var obstacle:world.obstacles())if(!obstacle.removed())obstacle.update(.01,world);
            world.update(0,0);encounter.update(.01);
            assertTrue(world.obstacles().size()<=1);
            fired=world.obstacles().stream().anyMatch(o->o instanceof BossProjectile);
        }
        assertTrue(fired,"A chosen shot must retry after the blocking plane leaves even when the next roll would skip it");
    }
    @Test void fasterLaneSettingsProduceMoreShotsLaneChangesAndAircraftCalls() {
        int[] oldShots=laneActivity(false,false),newShots=laneActivity(true,false);
        assertTrue(newShots[0]>oldShots[0]);assertTrue(newShots[1]>oldShots[1]);
        int[] oldPlanes=laneActivity(false,true),newPlanes=laneActivity(true,true);
        assertTrue(newPlanes[0]>oldPlanes[0]);
    }
    private int[] laneActivity(boolean faster,boolean planes) {
        List<String> settings=new ArrayList<>(List.of("boss.action1Chance","0","boss.openingDelaySeconds","0.01",
                "boss.lane.durationSeconds","30","boss.moveSeconds","0.55","boss.lane.doubleThrowChance","0",
                "boss.lane.planeChance",planes?"100":"0","boss.lane.birdChance","0"));
        if(!faster)settings.addAll(List.of("boss.lane.changeIntervalSeconds","2.4","boss.lane.attackIntervalSeconds","1"));
        BaseLevel world=level(settings.toArray(String[]::new));advanceTo(world,State.ACTIVE);
        Set<Obstacle> seen=Collections.newSetFromMap(new IdentityHashMap<>());int events=0,moves=0;boolean moving=world.bossEncounter().boss().moving();
        for(int i=0;i<600;i++) {
            world.update(1.0/60,0);boolean next=world.bossEncounter().boss().moving();if(next && !moving)moves++;moving=next;
            for(var obstacle:world.obstacles())if(seen.add(obstacle)) {
                if(obstacle instanceof BossProjectile || obstacle instanceof EnemyPlane)events++;
                obstacle.remove();
            }
        }
        return new int[]{events,moves};
    }
    @Test void suppliesOnlyStartDuringCombatAndExactlyTwoShieldsAreScheduled() {
        BaseLevel world=level("boss.shield.count","2","boss.shield.spawnChance","100","boss.shield.firstSpawnSeconds","0.05","boss.shield.intervalSeconds","0.15",
                "boss.healthPickup.firstDelaySeconds","0.05","boss.healthPickup.minIntervalSeconds","0.2","boss.healthPickup.maxIntervalSeconds","0.3");
        advanceTo(world,State.LOADING);assertTrue(world.obstacles().isEmpty());advanceTo(world,State.ACTIVE);
        int shields=0,health=0;
        for(int i=0;i<120;i++) {
            world.update(1.0/60,0);
            for(var item:world.obstacles()) {
                if(item instanceof ShieldPickup) {shields++;item.remove();}
                if(item instanceof HealthPickup) {health++;item.remove();}
            }
        }
        assertEquals(2,shields);assertEquals(2,world.bossEncounter().shieldSpawnCount());assertTrue(health>=3);
    }
    @Test void zeroShieldChanceNeverSpawnsEvenWhenRollsAreFrequent() {
        BaseLevel world=level("boss.shield.count","2","boss.shield.spawnChance","0",
                "boss.shield.firstSpawnSeconds","0.01","boss.shield.intervalSeconds","0.1");
        advanceTo(world,State.ACTIVE);for(int i=0;i<300;i++)world.update(1.0/60,0);
        assertEquals(0,world.bossEncounter().shieldSpawnCount());
        assertTrue(world.obstacles().stream().noneMatch(o->o instanceof ShieldPickup));
        assertEquals(15,Levels.create(52).config().values().number("boss.shield.spawnChance"));
        assertEquals(8,Levels.create(52).config().values().number("boss.shield.durationSeconds"));
    }
    @Test void pauseFreezesBossRocketsAndCooldownAndRestartCreatesFreshEncounter() {
        Game game=new Game(new LevelManager(List.<Supplier<BaseLevel>>of(()->level("boss.action1Chance","0")),0),false);
        for(int i=0;i<120 && !game.level().bossEncounter().active();i++)game.update(1.0/60);
        var encounter=game.level().bossEncounter();centerBossOnPlayer(game.level());
        game.input().press(KeyEvent.VK_E);game.update(.01);assertTrue(encounter.hasFired());
        var rocket=encounter.rockets().get(0);double y=rocket.y(),time=encounter.activeSeconds(),cooldown=encounter.cooldownFraction();
        game.togglePause();game.update(10);assertEquals(y,rocket.y());assertEquals(time,encounter.activeSeconds());assertEquals(cooldown,encounter.cooldownFraction());
        game.togglePause();game.level().player().kill();game.update(0);assertEquals(GameState.GAME_OVER,game.state());game.restart();
        assertNotSame(encounter,game.level().bossEncounter());assertEquals(State.NORMAL,game.level().bossEncounter().state());
        assertEquals(1,game.level().bossEncounter().boss().healthFraction());assertFalse(game.input().firing());
        assertEquals(0,game.level().bossEncounter().shieldSpawnCount());
    }
    @Test void actualEKeyBindingFiresWithCooldownAndEightHitsCompleteTheLevel() {
        Game game=new Game(new LevelManager(List.<Supplier<BaseLevel>>of(()->level("boss.action1Chance","0",
                "boss.lane.changeIntervalSeconds","100")),0),false);
        for(int i=0;i<120 && !game.level().bossEncounter().active();i++)game.update(1.0/60);
        centerBossOnPlayer(game.level());
        var panel=new javax.swing.JPanel();new game.input.InputHandler(panel,game);
        panel.getActionMap().get("down69").actionPerformed(null);assertTrue(game.input().firing());
        for(int i=0;i<700 && game.state()==GameState.PLAYING;i++)game.update(1.0/60);
        assertTrue(game.level().bossEncounter().boss().defeated());
        assertEquals(GameState.TRANSITION,game.state());game.update(.5);
        assertEquals(GameState.LEVEL_COMPLETE,game.state());assertEquals(5,game.level().player().health());
    }
}
