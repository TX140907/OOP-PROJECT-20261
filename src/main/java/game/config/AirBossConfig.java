package game.config;

import java.util.List;
import java.util.Properties;

/** Kiểm tra cấu hình boss trước khi mở game để lỗi chỉ rõ khóa cần sửa. */
public final class AirBossConfig {
    private AirBossConfig() {}
    /** Bổ sung khóa mới cho cấu hình boss cũ, không thay giá trị đã được người dùng đặt. */
    public static Properties withDefaults(Properties source) {
        Properties values=new Properties();values.putAll(source);
        if("true".equals(source.getProperty("boss.enabled","false").trim())) {
            values.putIfAbsent("boss.startAction","2");
            values.putIfAbsent("boss.lane.planeShotIntervalSeconds","0.65");
            values.putIfAbsent("boss.lane.planeShotRetrySeconds","0.15");
        }
        return values;
    }
    public static void validate(ConfigValues c) {
        if(!c.text("level.mode").equals("air"))throw new IllegalArgumentException("boss.enabled requires air mode");
        var types=List.of(c.text("obstacles").split(","));
        if(types.stream().noneMatch(t->t.trim().equals("bird")) || types.stream().noneMatch(t->t.trim().equals("enemyPlane")))
            throw new IllegalArgumentException("Boss requires bird and enemyPlane obstacle definitions");
        c.range("boss.width",1,GameConfig.ROAD_LEFT);
        c.range("boss.height",1,GameConfig.HEIGHT/2);
        c.range("boss.y",80,c.number("player.y")-c.number("boss.height")-c.number("boss.projectile.height"));
        c.range("boss.sideMargin",0,GameConfig.ROAD_LEFT-c.number("boss.width"));
        c.range("boss.hitsToDefeat",1,10000);c.integer("boss.hitsToDefeat");
        double healHits=c.number("boss.lane.healHitsAfterAction",1);
        if(healHits<0 || healHits>c.integer("boss.hitsToDefeat") || healHits!=Math.rint(healHits))
            throw new IllegalArgumentException("Invalid boss.lane.healHitsAfterAction");
        c.range("boss.startAction",1,2);c.integer("boss.startAction");
        c.range("boss.maxConsecutiveWaveSequences",1,10);c.integer("boss.maxConsecutiveWaveSequences");
        c.range("boss.actionGapSeconds",0,300);
        for(String key:List.of("boss.clearDelaySeconds","boss.warningSeconds","boss.entranceSeconds","boss.healthLoadSeconds",
                "boss.moveSeconds","boss.exitSeconds","boss.finishDelaySeconds","boss.retrySeconds",
                "boss.openingDelaySeconds","boss.waves.durationSeconds","boss.waves.intervalSeconds","boss.waves.throwActiveSeconds",
                "boss.waves.throwIntervalSeconds","boss.waves.throwRetrySeconds","boss.waves.openingVolleyIntervalSeconds","boss.waves.openingVolleyWarningSeconds",
                "boss.lane.durationSeconds","boss.lane.changeIntervalSeconds","boss.lane.attackIntervalSeconds",
                "boss.lane.planeShotIntervalSeconds","boss.lane.planeShotRetrySeconds",
                "boss.projectile.warningSeconds","boss.playerRocket.cooldownSeconds","boss.explosion.seconds",
                "boss.shield.durationSeconds","boss.shield.contactFadeSeconds","boss.shield.intervalSeconds")) c.range(key,.01,300);
        if(c.number("boss.lane.durationSeconds")<=c.number("boss.waves.durationSeconds"))
            throw new IllegalArgumentException("boss.lane.durationSeconds must exceed boss.waves.durationSeconds");
        for(String key:List.of("boss.action1Chance","boss.waves.throwChance","boss.waves.doubleThrowChance","boss.waves.twoPlaneChance","boss.lane.doubleThrowChance",
                "boss.lane.planeChance","boss.lane.birdChance","boss.shield.spawnChance"))c.range(key,0,100);
        c.bool("boss.waves.openingVolleyEnabled");
        c.range("boss.waves.speedMultiplier",1,3);
        c.range("enemyPlane.minSpeed",1,1000);
        c.range("boss.waves.staggerY",1,GameConfig.HEIGHT/2);
        c.range("boss.waves.gapPixels",c.number("spawn.minimumGap"),GameConfig.HEIGHT*2);
        for(String key:List.of("boss.projectile.width","boss.projectile.height","boss.shield.width","boss.shield.height",
                "boss.playerRocket.width","boss.playerRocket.height"))c.range(key,1,GameConfig.LANE_WIDTH);
        for(String key:List.of("boss.lane.throwSpeed","boss.waves.throwSpeed","boss.shield.speed","boss.playerRocket.speed"))c.range(key,1,2000);
        c.range("boss.waves.handHeightRatio",0,1);
        c.range("boss.waves.handInsetX",0,c.number("boss.width")/2);
        c.range("boss.waves.aimLineAlpha",0,1);c.range("boss.waves.aimLineWidth",.1,20);c.color("boss.waves.aimLineColor");
        c.range("boss.projectile.damage",1,100);c.integer("boss.projectile.damage");
        c.range("boss.shield.count",0,10);c.integer("boss.shield.count");
        c.range("boss.shield.firstSpawnSeconds",0,300);
        c.range("boss.healthPickup.firstDelaySeconds",0,300);
        c.range("boss.healthPickup.minIntervalSeconds",.1,300);
        c.range("boss.healthPickup.maxIntervalSeconds",c.number("boss.healthPickup.minIntervalSeconds"),300);
        c.range("boss.shield.contactAlpha",.1,1);
        for(String key:List.of("boss.warningBlinkSeconds","boss.hintBlinkSeconds","boss.projectile.blinkSeconds"))c.range(key,.01,5);
        c.range("boss.projectile.blinkSeconds",.01,c.number("boss.projectile.warningSeconds"));
        for(String key:List.of("boss.warningSize","boss.hintSize","boss.projectile.warningSize","boss.explosion.size",
                "boss.hud.width","boss.hud.height","boss.hud.portraitSize","boss.cooldown.width","boss.cooldown.height")) {
            c.range(key,1,1000);c.integer(key);
        }
        for(String key:List.of("boss.warningOffsetX","boss.hintOffsetX","boss.hud.x","boss.hud.y","boss.hud.portraitGap",
                "boss.cooldown.x","boss.cooldown.y")) {c.range(key,0,1000);c.integer(key);}
        c.range("boss.lane.warningSize",1,GameConfig.LANE_WIDTH);c.integer("boss.lane.warningSize");
        c.range("boss.lane.warningOffsetY",0,c.number("player.y")-c.number("boss.lane.warningSize"));c.integer("boss.lane.warningOffsetY");
        c.color("boss.lane.warningColor");
        for(String key:List.of("boss.color","boss.projectile.color","boss.healthBarColor","boss.shield.color","boss.playerRocket.color"))c.color(key);
        double shieldTravel=(c.number("player.y")+c.number("player.hitboxOffsetY")+c.number("player.hitboxHeight")/2
                +c.number("boss.shield.height")/2+c.number("spawn.margin"))/c.number("boss.shield.speed");
        if(shieldTravel>c.number("spawn.maxPredictionSeconds"))throw new IllegalArgumentException("Boss shield travel exceeds prediction limit");
        for(String key:List.of("boss.reverse.durationSeconds","boss.reverse.warningSeconds","boss.reverse.flightSeconds","boss.reverse.intervalSeconds"))c.range(key,.01,300);
        c.range("boss.reverse.waveCount",1,10);c.integer("boss.reverse.waveCount");
        double reverseWeight=0;
        for(String key:List.of("boss.reverse.singleWeight","boss.reverse.pairWeight","boss.reverse.sequenceWeight"))reverseWeight+=c.range(key,0,1000000);
        if(reverseWeight<=0)throw new IllegalArgumentException("At least one positive boss.reverse pattern weight is required");
        c.range("boss.reverse.sequenceDelaySeconds",.01,10);
        c.range("boss.lane.recoveryShotLimit",0,20);c.integer("boss.lane.recoveryShotLimit");
        c.range("boss.lane.recoveryShotIntervalSeconds",.01,300);c.range("boss.lane.recoveryShotRetrySeconds",.01,300);
        c.range("boss.reverse.shotChance",0,100);c.range("boss.reverse.shotSpeed",1,2000);
        for(String key:List.of("boss.reverse.shotWarningSeconds","boss.reverse.shotAfterTargetSeconds","boss.reverse.shotRetrySeconds"))c.range(key,.01,300);
        c.range("boss.reverse.spawnMargin",0,c.number("enemyPlane.height"));
        c.range("boss.reverse.blinkSeconds",.01,c.number("boss.reverse.warningSeconds"));
        c.range("boss.reverse.warningSize",1,GameConfig.LANE_WIDTH);c.integer("boss.reverse.warningSize");
        c.range("boss.reverse.warningY",76,GameConfig.HEIGHT-c.number("boss.reverse.warningSize"));c.integer("boss.reverse.warningY");
        double waveSeconds=c.number("boss.reverse.warningSeconds")+c.number("boss.reverse.flightSeconds");
        if(c.number("boss.reverse.intervalSeconds")<waveSeconds)throw new IllegalArgumentException("boss.reverse.intervalSeconds must cover warning and flight");
        if(c.number("boss.reverse.durationSeconds")<(c.integer("boss.reverse.waveCount")-1)*c.number("boss.reverse.intervalSeconds")+waveSeconds)
            throw new IllegalArgumentException("boss.reverse.durationSeconds is too short for all waves");
    }
}
