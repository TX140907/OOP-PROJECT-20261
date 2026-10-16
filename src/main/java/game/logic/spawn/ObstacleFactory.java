package game.logic.spawn;

import game.config.GameConfig;
import game.level.DayConfig;
import game.level.DayConfig.ObstacleSpec;
import game.model.obstacle.*;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;

/** All concrete construction is centralized; the update/collision pipeline uses Obstacle polymorphism. */
public final class ObstacleFactory {
    public Obstacle create(ObstacleSpec spec,DayConfig config,Random random) {
        var c=config.values();
        double speed=spec.minSpeed()+random.nextDouble()*(spec.maxSpeed()-spec.minSpeed());
        double x=GameConfig.laneX(random.nextInt(GameConfig.LANE_COUNT),spec.width());
        double y=-spec.height()-c.number("spawn.margin");
        double scroll=c.number("level.scrollSpeed");
        boolean right=random.nextBoolean();
        return switch (spec.type()) {
            case VEHICLE -> new Vehicle(spec,x,y,speed);
            case BICYCLE -> new Bicycle(spec,x,y,speed);
            case BUS -> new Bus(spec,x,y,speed);
            case BARRIER -> new Barrier(spec,x,y,scroll);
            case DRAIN -> new Drain(spec,x,y,scroll);
            case ICE_PATCH -> new IcePatch(spec,x,y,scroll);
            case ICE_HOLE -> new IceHole(spec,x,y,scroll);
            case TUMBLEWEED -> {
                double center=x+spec.width()/2;
                boolean fromLeft=center<GameConfig.WIDTH/2.0 || (center==GameConfig.WIDTH/2.0 && right);
                double startX=fromLeft?-spec.width()-c.number("spawn.margin"):GameConfig.WIDTH+c.number("spawn.margin");
                double stopTime=Math.abs(x-startX)/speed;
                double maxTravel=Math.max(1,c.number("energy.pickupBoostMultiplier"))*Math.max(1,c.number("ice.progressMultiplier",1));
                // Kể cả đang tăng tốc, cỏ vẫn dừng trước hàng người chơi một khoảng stopLeadY.
                double startY=c.number("player.y")-c.number("tumbleweed.stopLeadY")-scroll*stopTime*maxTravel;
                yield new Tumbleweed(spec,startX,startY,fromLeft?speed:-speed,scroll,x);
            }
            case SNOWMAN -> new Snowman(spec,x,y,scroll);
            case CACTUS -> new Cactus(spec,x,y,scroll,c,x+spec.width()/2<GameConfig.WIDTH/2.0
                    || (x+spec.width()/2==GameConfig.WIDTH/2.0 && right));
            case TRAFFIC_JAM_VEHICLE -> new TrafficJamVehicle(spec,x,y,scroll);
            case SNOWBALL -> new Snowball(spec,x,y,speed);
            case SHARK -> new Shark(spec,x,-spec.height()/2+c.number("shark.finHeight")/2,c);
            case FLOATING_CRATE -> new FloatingCrate(spec,x,y,right?speed:-speed,scroll,
                    GameConfig.ROAD_LEFT,GameConfig.ROAD_RIGHT-spec.width());
            case DOG -> {
                double startX=right?-spec.width()-c.number("spawn.margin"):GameConfig.WIDTH+c.number("spawn.margin");
                double offset=(random.nextDouble()*2-1)*c.number("dog.targetOffsetX");
                yield new Dog(spec,startX,c.number("player.y")-c.number("dog.leadY"),speed,
                        random.nextInt(GameConfig.LANE_COUNT),offset,c);
            }
            case BIRD -> {
                double crossY=config.values().number("player.y")-c.number("crossing.leadY");
                double crossX=right?-spec.width()-c.number("spawn.margin"):GameConfig.WIDTH+c.number("spawn.margin");
                if (random.nextDouble()*100<c.number("bird.stationaryChance")) {
                    var stops=new ArrayList<Double>();
                    for(int lane=0;lane<GameConfig.LANE_COUNT;lane++) {
                        double stopX=GameConfig.laneX(lane,spec.width());
                        double stopTime=Math.abs(stopX-crossX)/speed;
                        if(crossY+scroll*stopTime+spec.height()<=c.number("player.y")+c.number("player.hitboxOffsetY")) stops.add(stopX);
                    }
                    Collections.shuffle(stops,random);
                    if(!stops.isEmpty()) yield new Bird(spec,crossX,crossY,right?speed:-speed,scroll,true,stops.get(0));
                }
                yield new Bird(spec,crossX,crossY,right?speed:-speed,scroll);
            }
            case ENERGY_PICKUP -> new EnergyPickup(spec,x,y,scroll);
            case HEALTH_PICKUP -> new HealthPickup(spec,x,y,speed);
            case ENEMY_PLANE -> new EnemyPlane(spec,x,y,speed,c);
            case MISSILE -> new Missile(spec,null,x,y,c.number("player.y"),
                    c.number("player.maxSpeed")*c.number("missile.speedMultiplier"),c.number("missile.warningSeconds"));
            case BOSS_PROJECTILE,SHIELD_PICKUP -> throw new IllegalArgumentException("Boss items use the boss encounter spawner");
        };
    }
}
