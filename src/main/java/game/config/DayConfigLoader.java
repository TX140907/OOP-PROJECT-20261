package game.config;

import game.level.DayConfig;
import game.level.DayConfig.ObstacleSpec;
import game.logic.collision.CollisionKind;
import game.model.obstacle.ObstacleType;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Package-relative resources work in IDE classpaths and in the packaged JAR. */
public final class DayConfigLoader {
    private DayConfigLoader() {}
    public static DayConfig load(Class<?> owner, String filename) {
        try (InputStream stream = owner.getResourceAsStream(filename)) {
            if (stream == null) throw new IllegalArgumentException("Missing config: " + owner.getPackageName() + "/" + filename);
            return read(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (IOException e) { throw new UncheckedIOException(e); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException(filename + ": " + e.getMessage(), e); }
    }
    public static DayConfig read(Reader reader) throws IOException {
        return from(PropertiesReader.read(reader));
    }
    public static DayConfig from(Properties properties) {
        ConfigValues c = new ConfigValues(AirBossConfig.withDefaults(properties));
        c.text("level.name"); c.range("level.id", 1, 1000);
        if (!Set.of("road", "air").contains(c.text("level.mode"))) throw new IllegalArgumentException("Invalid level.mode");
        for (String key : List.of("level.durationSeconds", "player.width", "player.height", "player.hitboxWidth",
                "player.hitboxHeight", "player.maxSpeed", "player.acceleration", "player.deceleration",
                "spawn.minIntervalSeconds", "spawn.maxIntervalSeconds", "spawn.horizonSeconds", "spawn.stepSeconds",
                "spawn.gridPixels", "spawn.velocityGrid", "spawn.maxPlannerStates")) c.range(key, .001, 100000);
        c.range("level.initialHealth", 1, 100); c.range("level.scrollSpeed", 0, 1000);
        c.range("player.maxHealth",c.number("level.initialHealth"),100);c.integer("player.maxHealth");
        c.range("spawn.maxActiveObstacles", 1, 100); c.range("spawn.attempts", 1, 50);
        for (String key : List.of("spawn.initialDelaySeconds", "spawn.safeReactionTimeSeconds", "spawn.minimumGap",
                "spawn.margin", "player.hitInvulnerabilitySeconds", "player.hitSlowDurationSeconds",
                "progress.hitPenaltyDurationSeconds")) c.range(key, 0, 10000);
        for (String key : List.of("player.hitSlowMultiplier", "progress.hitMultiplier", "drain.slowMultiplier",
                "drain.progressMultiplier", "snowball.slowMultiplier", "snowball.progressMultiplier",
                "ice.decelerationMultiplier")) c.range(key, .01, 1);
        c.range("snowball.freezeProgressMultiplier",0,1);
        c.range("snowball.slowDurationSeconds",0,100);
        c.range("energy.maxSlowdown",0,.95);
        c.range("energy.pickupBoostMultiplier",1,5);
        c.range("energy.pickupBoostDurationSeconds",.001,100);
        double pickupHeal=c.number("energy.pickupHealAmount",0);
        if(pickupHeal<0 || pickupHeal>c.number("player.maxHealth") || pickupHeal!=Math.rint(pickupHeal))
            throw new IllegalArgumentException("Invalid energy.pickupHealAmount");
        double iceAcceleration=c.number("ice.accelerationMultiplier",1),iceProgress=c.number("ice.progressMultiplier",1);
        if(iceAcceleration<.1 || iceAcceleration>3)throw new IllegalArgumentException("Out of range: ice.accelerationMultiplier");
        if(iceProgress<.01 || iceProgress>5)throw new IllegalArgumentException("Out of range: ice.progressMultiplier");
        for (String key : List.of("progress.normalMultiplier", "ice.speedMultiplier")) c.range(key, .01, 10);
        double iceMaximum=c.number("ice.maxSpeedMultiplier",Math.max(c.number("ice.speedMultiplier"),iceProgress));
        if(iceMaximum<Math.max(c.number("ice.speedMultiplier"),iceProgress) || iceMaximum>10)
            throw new IllegalArgumentException("ice.maxSpeedMultiplier must cover the initial ice multipliers and be at most 10");
        double iceRamp=c.number("ice.rampPerSecond",0);
        if(iceRamp<0 || iceRamp>5)throw new IllegalArgumentException("Out of range: ice.rampPerSecond");
        for (String key : List.of("shark.finDuration", "shark.divingDuration", "shark.warningDuration",
                "shark.attackDuration", "shark.blinkSeconds", "drain.durationSeconds", "ice.durationSeconds",
                "snowball.freezeDuration")) c.range(key, .001, 100);
        c.range("shark.finSpeed", 0, 1000);
        switch(c.text("difficulty.mode","phased")) {
            case "fixed" -> c.range("difficulty.fixedSpawnMultiplier",.01,10);
            case "phased" -> {
                double p1=c.range("difficulty.phase1End",0,1),p2=c.range("difficulty.phase2End",0,1);
                if(p1<=0 || p1>=p2 || p2>=1) throw new IllegalArgumentException("Difficulty phases must increase within (0,1)");
                for(String key:List.of("difficulty.phase1SpawnMultiplier","difficulty.phase2SpawnMultiplier","difficulty.phase3SpawnMultiplier")) c.range(key,.01,10);
            }
            default -> throw new IllegalArgumentException("Invalid difficulty.mode");
        }
        if (c.number("spawn.minIntervalSeconds") > c.number("spawn.maxIntervalSeconds")) throw new IllegalArgumentException("Spawn min > max");
        c.range("spawn.stepSeconds", .01, .5);
        c.range("spawn.escapeBufferSeconds",.01,5);
        c.range("spawn.pathClearancePixels",0,GameConfig.LANE_WIDTH/2);
        double perLane=c.number("spawn.maxObstaclesPerLane",0);
        if(perLane<0 || perLane>100 || perLane!=Math.rint(perLane))throw new IllegalArgumentException("Invalid spawn.maxObstaclesPerLane");
        double laneGap=c.number("spawn.sameLaneGapPixels",0);
        if(laneGap<0 || laneGap>GameConfig.HEIGHT*2)throw new IllegalArgumentException("Out of range: spawn.sameLaneGapPixels");
        c.range("spawn.maxPredictionSeconds",Math.max(c.number("spawn.horizonSeconds"),
                c.number("spawn.maxLifetimeSeconds")+c.number("spawn.escapeBufferSeconds")),60);
        if (c.number("spawn.horizonSeconds") <= c.number("spawn.safeReactionTimeSeconds")) throw new IllegalArgumentException("Prediction horizon must exceed reaction time");
        c.integer("level.initialHealth");c.integer("spawn.maxActiveObstacles");c.integer("spawn.attempts");c.integer("spawn.maxPlannerStates");
        c.range("collision.normalDamage",1,100);c.integer("collision.normalDamage");
        c.range("drain.damage",1,100);c.integer("drain.damage");
        c.range("player.hitBlinkDurationSeconds",.01,100);
        c.range("player.hitBlinkIntervalSeconds",.001,c.number("player.hitBlinkDurationSeconds"));
        for (String key:List.of("shark.finWidth","shark.finHeight","shark.rippleSize")) c.range(key,1,240);
        c.range("crossing.leadY",1,c.number("player.y"));
        c.range("energy.pickupTravelSeconds",.01,100);
        c.range("energy.urgentTravelSeconds",.01,c.number("energy.pickupTravelSeconds"));
        if (!Set.of("weighted", "independent").contains(c.text("spawn.mode"))) throw new IllegalArgumentException("Invalid spawn.mode");
        if (c.number("player.minX") < 0 || c.number("player.maxX") + c.number("player.width") > GameConfig.WIDTH
                || c.number("player.minX") >= c.number("player.maxX")) throw new IllegalArgumentException("Invalid player bounds");
        if (c.text("level.mode").equals("road") && (c.number("player.minX")<GameConfig.ROAD_LEFT
                || c.number("player.maxX")+c.number("player.width")>GameConfig.ROAD_RIGHT)) throw new IllegalArgumentException("Player outside road");
        for (String axis : List.of("X", "Y")) {
            double offset = c.number("player.hitboxOffset" + axis);
            double hit = c.number(axis.equals("X") ? "player.hitboxWidth" : "player.hitboxHeight");
            double sprite = c.number(axis.equals("X") ? "player.width" : "player.height");
            if (offset < 0 || offset + hit > sprite) throw new IllegalArgumentException("Hitbox outside player sprite");
        }
        c.range("player.y", 0, GameConfig.HEIGHT - c.number("player.height"));
        c.color("level.backgroundColor"); c.color("level.roadColor");
        c.bool("energy.enabled"); c.bool("glare.enabled"); c.bool("road.overlayLines");
        c.range("glare.minAlpha", .1, 1); c.range("glare.clearY", 1, GameConfig.HEIGHT);
        double reveal=c.number("glare.revealExponent",1),haze=c.number("glare.hazeAlpha",0);
        if(reveal<.1 || reveal>8)throw new IllegalArgumentException("Out of range: glare.revealExponent");
        if(haze<0 || haze>.9)throw new IllegalArgumentException("Out of range: glare.hazeAlpha");
        if(haze>0)c.color("glare.hazeColor");
        for (String type : c.text("glare.types").split(",")) ObstacleType.from(type.trim());
        if (c.bool("energy.enabled")) {
            c.range("energy.pickupTravelSeconds",.01,c.number("spawn.horizonSeconds"));
            c.range("energy.max", .01, 100000); c.range("energy.initial", .01, c.number("energy.max"));
            for (String key : List.of("energy.drainPerSecond", "energy.recovery", "energy.pickupIntervalSeconds",
                    "energy.minimumReserve", "energy.pickupWidth", "energy.pickupHeight")) c.range(key, .001, 10000);
            if (c.number("energy.minimumReserve") >= c.number("energy.max")) throw new IllegalArgumentException("Energy reserve >= max");
        }
        if(c.bool("healthPickup.enabled")) {
            String schedule=c.text("healthPickup.spawnTimesSeconds","");
            double randomCount=c.number("healthPickup.randomCount",0);
            if(randomCount<0 || randomCount>100 || randomCount!=Math.rint(randomCount))
                throw new IllegalArgumentException("Invalid healthPickup.randomCount");
            if(randomCount>0) {
                if(!schedule.isEmpty())throw new IllegalArgumentException("Choose healthPickup.randomCount or spawnTimesSeconds, not both");
                double start=c.range("healthPickup.randomStartSeconds",0,c.number("level.durationSeconds"));
                double end=c.range("healthPickup.randomEndSeconds",0,c.number("level.durationSeconds"));
                if(end<=start || end>=c.number("level.durationSeconds"))
                    throw new IllegalArgumentException("Health pickup random window must increase and end before the level ends");
            }
            if(!schedule.isEmpty()) {
                double previous=-1;
                for(String entry:schedule.split(",",-1)) {
                    double time;
                    try {time=Double.parseDouble(entry.trim());}
                    catch(NumberFormatException e) {throw new IllegalArgumentException("Invalid healthPickup.spawnTimesSeconds",e);}
                    if(!Double.isFinite(time) || time<0 || time>=c.number("level.durationSeconds") || time<=previous)
                        throw new IllegalArgumentException("healthPickup.spawnTimesSeconds must increase within the level duration");
                    previous=time;
                }
            }
            c.range("healthPickup.amount",1,c.number("player.maxHealth"));c.integer("healthPickup.amount");
            c.range("healthPickup.minIntervalSeconds",.1,1000);
            c.range("healthPickup.maxIntervalSeconds",c.number("healthPickup.minIntervalSeconds"),1000);
            c.range("healthPickup.firstDelaySeconds",0,1000);c.range("healthPickup.retrySeconds",.01,1000);
            c.range("healthPickup.width",1,GameConfig.LANE_WIDTH);c.range("healthPickup.height",1,GameConfig.HEIGHT/2);
            c.range("healthPickup.speed",1,1000);
            double travel=(c.number("player.y")+c.number("player.hitboxOffsetY")+c.number("player.hitboxHeight")/2
                    +c.number("healthPickup.height")/2+c.number("spawn.margin"))/c.number("healthPickup.speed");
            if(travel>c.number("spawn.maxPredictionSeconds")) throw new IllegalArgumentException("Health pickup travel exceeds prediction limit");
        }
        String seed = c.text("spawn.randomSeed");
        if (!seed.equals("random")) try { Long.parseLong(seed); } catch (NumberFormatException e) { throw new IllegalArgumentException("Invalid spawn.randomSeed", e); }
        List<ObstacleSpec> specs = new ArrayList<>(); Set<ObstacleType> seen = new HashSet<>();
        for (String name : c.text("obstacles").split(",", -1)) {
            ObstacleType type = ObstacleType.from(name.trim());
            if (!seen.add(type)) throw new IllegalArgumentException("Duplicate obstacle: " + name);
            String prefix = type.key() + ".";
            double weight = c.range("spawn." + type.key() + ".weight", 0, 1000000);
            double chance = c.range(prefix + "chance", 0, 100);
            double w = c.range(prefix + "width", 1,
                    type==ObstacleType.ENEMY_PLANE?GameConfig.LANE_WIDTH:GameConfig.LANE_WIDTH-10);
            double h = c.range(prefix + "height", 1, GameConfig.HEIGHT / 2);
            double min = c.range(prefix + "minSpeed", 0, 1000), max = c.range(prefix + "maxSpeed", 0, 1000);
            if (min > max) throw new IllegalArgumentException(prefix + "minSpeed > maxSpeed");
            CollisionKind collision;
            try { collision = CollisionKind.valueOf(c.text(prefix + "collisionType")); }
            catch (IllegalArgumentException e) { throw new IllegalArgumentException("Invalid " + prefix + "collisionType", e); }
            if (Set.of(ObstacleType.VEHICLE, ObstacleType.BICYCLE, ObstacleType.BUS).contains(type)
                    && collision != CollisionKind.LETHAL) throw new IllegalArgumentException(prefix + "must be LETHAL");
            if(type==ObstacleType.SHARK && collision!=CollisionKind.SHARK_SLOW)
                throw new IllegalArgumentException("shark.collisionType must be SHARK_SLOW");
            if(collision==CollisionKind.SHARK_SLOW && type!=ObstacleType.SHARK)
                throw new IllegalArgumentException("SHARK_SLOW requires shark");
            if(type==ObstacleType.ENEMY_PLANE && collision!=CollisionKind.PLANE_DAMAGE)
                throw new IllegalArgumentException("enemyPlane must be PLANE_DAMAGE");
            if(collision==CollisionKind.PLANE_DAMAGE && type!=ObstacleType.ENEMY_PLANE)
                throw new IllegalArgumentException("PLANE_DAMAGE requires enemyPlane");
            if (type == ObstacleType.SNOWBALL && collision != CollisionKind.FREEZE) throw new IllegalArgumentException("snowball must FREEZE");
            specs.add(new ObstacleSpec(type, weight, chance, w, h, min, max, collision,
                    c.bool(prefix + "consumed"), c.text("sprite." + type.key(), "")));
        }
        if(seen.contains(ObstacleType.SHARK)) {
            double damage=c.number("shark.damage",1),duration=c.number("shark.slowDurationSeconds",1.2);
            if(damage<1 || damage>100 || damage!=Math.rint(damage))throw new IllegalArgumentException("Invalid shark.damage");
            if(duration<0 || duration>100)throw new IllegalArgumentException("Out of range: shark.slowDurationSeconds");
            for(String key:List.of("shark.slowMultiplier","shark.progressMultiplier")) {
                double multiplier=c.number(key,.65);
                if(multiplier<.01 || multiplier>1)throw new IllegalArgumentException("Out of range: "+key);
            }
        }
        if (seen.contains(ObstacleType.DOG)) {
            c.range("dog.leadY",1,c.number("player.y"));
            c.range("dog.minSpeed",1,1000);
            c.range("dog.barkDurationSeconds",.1,10);c.range("dog.chargeDurationSeconds",.05,10);
            c.range("dog.targetOffsetX",0,(GameConfig.LANE_WIDTH-c.number("dog.width"))/2);
            c.range("dog.warningBlinkSeconds",.01,c.number("dog.barkDurationSeconds"));
            c.range("dog.warningSize",1,240);c.integer("dog.warningSize");
            c.range("dog.warningOffsetY",0,240);c.integer("dog.warningOffsetY");
        }
        if(seen.contains(ObstacleType.TUMBLEWEED)) {
            c.range("tumbleweed.minSpeed",1,1000);
            c.range("tumbleweed.stopLeadY",c.number("tumbleweed.height")+c.number("spawn.minimumGap"),c.number("player.y"));
        }
        if(seen.contains(ObstacleType.CACTUS)) {
            String enabled=c.text("cactus.fallEnabled","false");
            if(!Set.of("true","false").contains(enabled))throw new IllegalArgumentException("Invalid cactus.fallEnabled");
            if(Boolean.parseBoolean(enabled)) {
                c.range("cactus.standingSeconds",0,30);
                c.range("cactus.warningSeconds",.1,10);c.range("cactus.fallSeconds",.01,10);
                c.range("cactus.blinkSeconds",.01,c.number("cactus.warningSeconds"));
                c.range("cactus.warningFillAlpha",0,1);c.color("cactus.warningColor");
            }
        }
        if (seen.contains(ObstacleType.BIRD)) {
            c.range("bird.stationaryChance",0,100);
            c.range("bird.minSpeed",1,1000);
        }
        if (seen.contains(ObstacleType.ENEMY_PLANE) || seen.contains(ObstacleType.MISSILE)) {
            c.range("missile.speedMultiplier",.01,5);c.range("missile.warningSeconds",.01,10);
            c.range("missile.blinkSeconds",.001,c.number("missile.warningSeconds"));
            c.range("missile.sideOffset",1,GameConfig.WIDTH/2);
            c.range("missile.warningSize",1,240);c.range("missile.width",1,240);c.range("missile.height",1,240);
            if (!Set.of("LETHAL","NORMAL_DAMAGE").contains(c.text("missile.collisionType"))) throw new IllegalArgumentException("Invalid missile.collisionType");
        }
        if (seen.contains(ObstacleType.ENEMY_PLANE)) {
            c.range("enemyPlane.damage",1,100);c.integer("enemyPlane.damage");
            c.range("enemyPlane.hitsToDestroy",1,100);c.integer("enemyPlane.hitsToDestroy");
            c.range("enemyPlane.hitFlashSeconds",0,5);
            c.range("enemyPlane.tailOffsetX",1,GameConfig.LANE_WIDTH/4);
            c.range("enemyPlane.tailAlignmentPixels",0,c.number("enemyPlane.tailOffsetX")-.001);
            c.range("enemyPlane.tailGapPixels",0,GameConfig.HEIGHT*2);
            c.range("enemyPlane.firstShotDelaySeconds",0,100);c.range("enemyPlane.shotIntervalSeconds",.01,100);
            c.range("enemyPlane.minFireY",0,c.number("player.y"));
            c.range("enemyPlane.maxFireY",c.number("enemyPlane.minFireY"),c.number("player.y")-c.number("enemyPlane.height"));
        }
        if (c.text("spawn.mode").equals("weighted") && specs.stream().mapToDouble(ObstacleSpec::weight).sum() <= 0)
            throw new IllegalArgumentException("At least one positive spawn weight is required");
        String bossEnabled=c.text("boss.enabled","false");
        if(!Set.of("true","false").contains(bossEnabled))throw new IllegalArgumentException("Invalid boss.enabled");
        if(bossEnabled.equals("true")) AirBossConfig.validate(c);
        LevelEventConfig.validate(c,specs);
        return new DayConfig(c, specs);
    }
}
