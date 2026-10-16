package game.config;

import game.level.DayConfig.ObstacleSpec;
import game.model.obstacle.ObstacleType;
import java.util.List;

/** Khóa sự kiện là tùy chọn; cấu hình cũ không khai báo event.type vẫn chạy bình thường. */
public final class LevelEventConfig {
    private LevelEventConfig() {}
    public static void validate(ConfigValues c,List<ObstacleSpec> obstacles) {
        String type=c.text("event.type","none");
        if(type.equals("none"))return;
        if(!List.of("convoy","sharkRush","iceChoice","heatCycle").contains(type))
            throw new IllegalArgumentException("Invalid event.type: "+type);
        if(!c.text("level.mode").equals("road"))throw new IllegalArgumentException("Road events require level.mode=road");
        // Cho phép mốc nằm sau cuối màn: khi đó không kích hoạt, không chặn việc chỉnh thời lượng.
        number(c,"event.startSeconds",16,0,100000);
        number(c,"event.warningSeconds",1.2,0,30);
        if(type.equals("heatCycle")) {
            if(!c.bool("glare.enabled") || !c.bool("energy.enabled"))
                throw new IllegalArgumentException("event.type=heatCycle requires glare.enabled and energy.enabled");
            number(c,"event.heat.hotSeconds",5,.1,300);
            number(c,"event.heat.clearSeconds",3,.1,300);
            number(c,"event.heat.fadeSeconds",.6,0,30);
            number(c,"event.heat.hotSpawnMultiplier",.75,.1,3);
            return;
        }
        number(c,"event.clearTimeoutSeconds",7,.1,60);
        number(c,"event.maxDurationSeconds",10,.1,300);
        number(c,"event.retrySeconds",.35,.01,10);
        switch(type) {
            case "convoy" -> {
                require(obstacles,ObstacleType.VEHICLE,type);
                integer(c,"event.convoy.waveCount",3,1,12);
                number(c,"event.convoy.intervalSeconds",2.8,.1,60);
                number(c,"event.convoy.speedMultiplier",1.05,.25,2);
                number(c,"event.convoy.staggerY",60,1,GameConfig.HEIGHT);
            }
            case "sharkRush" -> {
                require(obstacles,ObstacleType.SHARK,type);
                integer(c,"event.sharks.waveCount",4,1,12);
                integer(c,"event.sharks.maxActive",3,1,12);
                number(c,"event.sharks.intervalSeconds",2,.1,60);
                double single=number(c,"event.sharks.singleWeight",1,0,1000000);
                double pair=number(c,"event.sharks.pairWeight",1,0,1000000);
                double sequence=number(c,"event.sharks.sequenceWeight",1,0,1000000);
                if(single+pair+sequence<=0)throw new IllegalArgumentException("Shark formation weights must have a positive sum");
                int largest=sequence>0?3:pair>0?2:1;
                if(c.number("event.sharks.maxActive",3)<largest)
                    throw new IllegalArgumentException("event.sharks.maxActive is too small for the enabled shark formations");
                number(c,"event.sharks.sequenceDelaySeconds",.5,.1,10);
            }
            case "iceChoice" -> {
                require(obstacles,ObstacleType.ICE_HOLE,type);require(obstacles,ObstacleType.SNOWMAN,type);
                integer(c,"event.ice.waveCount",2,1,12);
                number(c,"event.ice.intervalSeconds",3,.1,60);
                number(c,"event.ice.staggerY",60,1,GameConfig.HEIGHT);
                integer(c,"event.ice.lane",2,0,2);
                if(c.number("event.ice.lane",2)==1)throw new IllegalArgumentException("event.ice.lane must be 0 or 2 to preserve a dry outer lane");
                number(c,"event.ice.roadSeconds",12,1,60);
                number(c,"event.ice.hazardChance",65,0,100);
                number(c,"event.ice.dryLaneObstacleChance",0,0,100);
                number(c,"event.ice.hazardStaggerY",520,1,GameConfig.HEIGHT*2);
                number(c,"event.ice.roadAlpha",.85,0,1);
                number(c,"event.ice.detailSpacing",72,20,GameConfig.HEIGHT);
                for(String key:List.of("event.ice.roadColor","event.ice.edgeColor","event.ice.crackColor"))
                    if(c.copy().containsKey(key))c.color(key);
            }
            default -> { }
        }
    }
    private static void require(List<ObstacleSpec> specs,ObstacleType type,String event) {
        if(specs.stream().noneMatch(spec->spec.type()==type))
            throw new IllegalArgumentException("event.type="+event+" requires "+type.key()+" in obstacles; use event.type=none to disable");
    }
    private static double number(ConfigValues c,String key,double fallback,double min,double max) {
        double value=c.number(key,fallback);
        if(value<min || value>max)throw new IllegalArgumentException("Out of range: "+key);
        return value;
    }
    private static void integer(ConfigValues c,String key,int fallback,int min,int max) {
        double value=number(c,key,fallback,min,max);
        if(value!=Math.rint(value))throw new IllegalArgumentException("Invalid integer: "+key);
    }
}
