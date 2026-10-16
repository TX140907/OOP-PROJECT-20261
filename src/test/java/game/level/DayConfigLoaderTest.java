package game.level;
import game.TestSupport;
import game.config.DayConfigLoader;
import game.model.obstacle.ObstacleType;
import org.junit.jupiter.api.Test;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;
class DayConfigLoaderTest {
    @Test void sixPackageRelativeResourcesLoad() {
        for (int id:Levels.ORDER) { BaseLevel level=Levels.create(id);assertEquals(id,level.config().id());assertEquals(id==51?25:id==52?15:40,level.config().duration());
            assertNotNull(level.getClass().getResource(id==51?"day5-1.properties":id==52?"day5-2.properties":"day"+id+".properties")); }
    }
    @Test void missingKeyHasUsefulError() {
        Properties p=TestSupport.config(1).copyProperties();p.remove("level.durationSeconds");
        assertTrue(assertThrows(IllegalArgumentException.class,()->DayConfigLoader.from(p)).getMessage().contains("level.durationSeconds"));
    }
    @Test void olderBossPropertiesGetNewDefaultsWithoutChangingTheSource() {
        Properties source=TestSupport.config(52,"boss.enabled","true").copyProperties();
        for(String key:java.util.List.of("boss.startAction","boss.lane.planeShotIntervalSeconds","boss.lane.planeShotRetrySeconds"))source.remove(key);
        Properties before=new Properties();before.putAll(source);
        DayConfig loaded=DayConfigLoader.from(source);
        assertEquals(2,loaded.values().integer("boss.startAction"));
        assertEquals(.65,loaded.values().number("boss.lane.planeShotIntervalSeconds"));
        assertEquals(.15,loaded.values().number("boss.lane.planeShotRetrySeconds"));
        assertEquals(before,source);
        var world=new BaseLevel(loaded) {};
        world.progress().update(1000,1,1,1);
        for(int tick=0;tick<200 && !world.bossEncounter().active();tick++)world.bossEncounter().update(.1);
        assertTrue(world.bossEncounter().active());
        assertEquals(game.logic.boss.AirBossEncounter.Action.LANE_ATTACK,world.bossEncounter().action());
    }
    @Test void explicitNewBossSettingsOverrideCompatibilityDefaultsAndBlankValuesStillFail() {
        DayConfig loaded=TestSupport.config(52,"boss.enabled","true","boss.startAction","1",
                "boss.lane.planeShotIntervalSeconds","0.9","boss.lane.planeShotRetrySeconds","0.3");
        assertEquals(1,loaded.values().integer("boss.startAction"));
        assertEquals(.9,loaded.values().number("boss.lane.planeShotIntervalSeconds"));
        assertEquals(.3,loaded.values().number("boss.lane.planeShotRetrySeconds"));
        for(String key:java.util.List.of("boss.startAction","boss.lane.planeShotIntervalSeconds","boss.lane.planeShotRetrySeconds"))
            assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"boss.enabled","true",key,""),key);
    }
    @Test void optionalUtf8BomDoesNotBecomePartOfTheFirstProperty() throws Exception {
        var writer=new java.io.StringWriter();TestSupport.config(52).copyProperties().store(writer,null);
        String body=writer.toString().lines().filter(line->!line.startsWith("#"))
                .collect(java.util.stream.Collectors.joining("\n"));
        DayConfig plain=DayConfigLoader.read(new java.io.StringReader(body));
        DayConfig marked=DayConfigLoader.read(new java.io.StringReader(Character.toString(0xFEFF)+body));
        assertEquals(plain.copyProperties(),marked.copyProperties());
        assertEquals("Ngày 5 - Trực thăng",marked.name());
    }
    @Test void invalidBoundsAndMinMaxFail() {
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(1,"player.hitboxWidth","110"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(1,"spawn.minIntervalSeconds","9"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(1,"vehicle.minSpeed","999"));
    }
    @Test void weightsPhasesAndDurationValidated() {
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(1,"spawn.vehicle.weight","-1"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(1,"difficulty.phase1End","0.9"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(1,"level.durationSeconds","0"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(1,"player.acceleration","NaN"));
    }
    @Test void independentModeAndDisabledTypesRemainAvailable() {
        DayConfig c=TestSupport.config(1,"spawn.mode","independent","vehicle.chance","70","bicycle.chance","30");
        assertEquals(70,c.obstacles().get(0).chance());assertEquals(30,c.obstacles().get(1).chance());
    }
    @Test void noVehicleClassRequiredForWorldScrollingDamage() {
        assertEquals(ObstacleType.BARRIER,TestSupport.config(1).obstacles().get(2).type());
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(1,"vehicle.collisionType","NORMAL_DAMAGE"));
    }
    @Test void configCopiesCannotMutateAnotherLevel() {
        DayConfig original=TestSupport.config(1);Properties p=original.copyProperties();p.setProperty("level.durationSeconds","5");
        assertEquals(40,original.duration());assertEquals(5,DayConfigLoader.from(p).duration());
        assertEquals(40,Levels.create(2).config().duration());
    }
    @Test void dayFiveDurationsIndependent() {
        assertEquals(12,TestSupport.config(51,"level.durationSeconds","12").duration());
        assertEquals(7,TestSupport.config(52,"level.durationSeconds","7").duration());
    }
    @Test void travelAndAircraftConfigurationRejectInvalidValues() {
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(2,"drain.progressMultiplier","1.5"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(3,"snowball.slowDurationSeconds","-1"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(4,"energy.maxSlowdown","1"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(4,"energy.pickupBoostDurationSeconds","0"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(51,"crossing.leadY","0"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"missile.speedMultiplier","0"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"missile.blinkSeconds","0.3"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"enemyPlane.maxFireY","499"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"enemyPlane.damage","0"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"enemyPlane.damage","1.5"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"enemyPlane.collisionType","LETHAL"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"bird.stationaryChance","-1"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"bird.stationaryChance","101"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"bird.minSpeed","0"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"healthPickup.amount","0"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(2,"drain.damage","0"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(1,"player.hitBlinkIntervalSeconds","2"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(51,"dog.targetOffsetX","100"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(51,"dog.barkDurationSeconds","0"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(51,"spawn.maxPredictionSeconds","4.5"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(51,"spawn.pathClearancePixels","-1"));
    }
    @Test void bossSettingsValidateProbabilitiesGeometryDamageAndTiming() {
        for(String[] bad:new String[][]{{"boss.action1Chance","101"},{"boss.startAction","0"},{"boss.startAction","3"},{"boss.startAction","1.5"},
                {"boss.lane.planeShotIntervalSeconds","0"},{"boss.lane.planeShotRetrySeconds","-1"},{"boss.lane.doubleThrowChance","-1"},
                {"boss.lane.planeChance","101"},{"boss.shield.contactAlpha","1.1"},{"boss.hitsToDefeat","0"},
                {"boss.playerRocket.cooldownSeconds","0"},{"boss.lane.durationSeconds","8"},
                {"boss.sideMargin","80"},{"boss.projectile.damage","0"},{"boss.shield.count","2.5"},{"boss.shield.spawnChance","101"},
                {"boss.healthPickup.minIntervalSeconds","20"}})
            assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"boss.enabled","true",bad[0],bad[1]),bad[0]);
        var properties=TestSupport.config(52).copyProperties();properties.setProperty("boss.enabled","true");properties.remove("boss.hitsToDefeat");
        assertTrue(assertThrows(IllegalArgumentException.class,()->DayConfigLoader.from(properties)).getMessage().contains("boss.hitsToDefeat"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"boss.enabled","yes"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"boss.enabled","true","boss.waves.gapPixels","-1"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"boss.enabled","true","boss.waves.doubleThrowChance","101"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"boss.enabled","true","boss.waves.twoPlaneChance","101"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"boss.enabled","true","boss.waves.throwIntervalSeconds","0"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"boss.enabled","true","boss.waves.throwRetrySeconds","0"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"boss.enabled","true","boss.waves.openingVolleyEnabled","yes"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"boss.enabled","true","boss.waves.openingVolleyIntervalSeconds","0"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"boss.enabled","true","boss.waves.openingVolleyWarningSeconds","0"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"boss.enabled","true","enemyPlane.minSpeed","0"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"enemyPlane.tailOffsetX","0"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"enemyPlane.tailAlignmentPixels","24"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"enemyPlane.tailGapPixels","-1"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"enemyPlane.hitsToDestroy","0"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"enemyPlane.hitsToDestroy","1.5"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"enemyPlane.hitFlashSeconds","-1"));
        assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"boss.enabled","true","boss.reverse.singleWeight","0","boss.reverse.pairWeight","0","boss.reverse.sequenceWeight","0"));
        for(String[] bad:new String[][]{{"boss.reverse.waveCount","0"},{"boss.reverse.singleWeight","-1"},
                {"boss.reverse.pairWeight","-1"},{"boss.reverse.sequenceWeight","-1"},{"boss.reverse.sequenceDelaySeconds","0"},
                {"boss.lane.recoveryShotLimit","-1"},{"boss.lane.recoveryShotLimit","2.5"},{"boss.lane.recoveryShotIntervalSeconds","0"},{"boss.lane.recoveryShotRetrySeconds","0"},{"boss.actionGapSeconds","-1"},
                {"boss.waves.throwSpeed","0"},{"boss.waves.handHeightRatio","1.1"},{"boss.waves.handInsetX","51"},
                {"boss.waves.aimLineAlpha","1.1"},{"boss.waves.aimLineWidth","0"},{"boss.waves.aimLineColor","invalid"},{"boss.maxConsecutiveWaveSequences","0"},{"boss.maxConsecutiveWaveSequences","2.5"},
                {"boss.lane.warningSize","241"},{"boss.lane.warningOffsetY","-1"},{"boss.lane.warningColor","invalid"},
                {"boss.reverse.shotChance","101"},{"boss.reverse.shotChance","-1"},{"boss.reverse.shotSpeed","0"},
                {"boss.reverse.shotWarningSeconds","0"},{"boss.reverse.shotAfterTargetSeconds","-1"},{"boss.reverse.shotRetrySeconds","0"},
                {"boss.reverse.flightSeconds","0"},{"boss.reverse.intervalSeconds","1"},{"boss.reverse.durationSeconds","5"}})
            assertThrows(IllegalArgumentException.class,()->TestSupport.config(52,"boss.enabled","true",bad[0],bad[1]),bad[0]);
    }
}
