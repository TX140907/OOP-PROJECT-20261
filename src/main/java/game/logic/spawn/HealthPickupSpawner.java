package game.logic.spawn;

import game.config.ConfigValues;
import game.config.GameConfig;
import game.level.BaseLevel;
import game.level.DayConfig.ObstacleSpec;
import game.logic.collision.CollisionKind;
import game.model.obstacle.*;
import java.util.*;

/** Per-level healing schedule, independent of obstacle weights, paused with gameplay. */
public final class HealthPickupSpawner {
    private final ConfigValues config;
    private final Random random;
    private final SafeSpawnPolicy safety;
    private final boolean enabled;
    private final double[] spawnTimes;
    private int scheduledSpawns;
    private double cooldown;
    public HealthPickupSpawner(BaseLevel world,Random random,SafeSpawnPolicy safety) {
        this.config=world.config().values();this.random=random;this.safety=safety;
        enabled=config.bool("healthPickup.enabled");
        String schedule=enabled?config.text("healthPickup.spawnTimesSeconds",""):"";
        int randomCount=enabled?(int)config.number("healthPickup.randomCount",0):0;
        if(randomCount>0) {
            // Chia màn thành các khoảng rồi chọn ngẫu nhiên trong từng khoảng, tránh dồn cả năm hộp cùng lúc.
            double start=config.number("healthPickup.randomStartSeconds"),end=config.number("healthPickup.randomEndSeconds");
            spawnTimes=new double[randomCount];
            for(int i=0;i<randomCount;i++)spawnTimes[i]=start+(i+random.nextDouble())*(end-start)/randomCount;
        } else spawnTimes=schedule.isEmpty()?new double[0]:Arrays.stream(schedule.split(",")).mapToDouble(value->Double.parseDouble(value.trim())).toArray();
        cooldown=enabled && spawnTimes.length==0?config.number("healthPickup.firstDelaySeconds"):0;
    }
    public void update(double dt,BaseLevel world) {
        if(!enabled)return;
        cooldown-=dt;
        if(cooldown>1e-9)return;
        if(spawnTimes.length>0) {
            if(scheduledSpawns>=spawnTimes.length || world.progress().fraction()*world.config().duration()+1e-9<spawnTimes[scheduledSpawns])return;
            if(trySpawn(world)) {scheduledSpawns++;cooldown=0;}
            else cooldown=config.number("healthPickup.retrySeconds");
            return;
        }
        if(trySpawn(world)) cooldown=config.number("healthPickup.minIntervalSeconds")+random.nextDouble()
                *(config.number("healthPickup.maxIntervalSeconds")-config.number("healthPickup.minIntervalSeconds"));
        else cooldown=config.number("healthPickup.retrySeconds");
    }
    public boolean trySpawn(BaseLevel world) {
        if(!enabled || world.obstacles().size()>=config.integer("spawn.maxActiveObstacles")
                || world.obstacles().stream().anyMatch(o->o.spec().type()==ObstacleType.HEALTH_PICKUP))return false;
        double width=config.number("healthPickup.width"),height=config.number("healthPickup.height"),speed=config.number("healthPickup.speed");
        double y=-height-config.number("spawn.margin");
        double travel=(world.player().hitbox().getCenterY()-height/2-y)/speed;
        ObstacleSpec spec=new ObstacleSpec(ObstacleType.HEALTH_PICKUP,0,0,width,height,speed,speed,CollisionKind.HEAL,true,
                config.text("sprite.healthPickup",""));
        List<Integer> lanes=new ArrayList<>(List.of(0,1,2));Collections.shuffle(lanes,random);
        for(int lane:lanes) {
            HealthPickup pickup=new HealthPickup(spec,GameConfig.laneX(lane,width),y,speed);
            if(safety.accepts(world,pickup) && safety.canCollect(world,pickup,travel)) {
                world.addObstacle(pickup);
                return true;
            }
        }
        return false;
    }
}
