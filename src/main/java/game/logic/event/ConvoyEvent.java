package game.logic.event;

import game.config.GameConfig;
import game.level.BaseLevel;
import game.model.obstacle.*;
import java.util.*;

/** Mỗi hàng xe chừa một làn; khoảng trống tiếp theo chỉ dịch sang làn kề bên. */
final class ConvoyEvent extends RoadSequenceEvent {
    private int openLane;
    private double speed;
    private boolean firstWave=true;
    ConvoyEvent(BaseLevel world,Random random) {super(world,random,"event.convoy",3,2.8);}
    @Override protected void onStart() {
        var vehicle=spec(ObstacleType.VEHICLE);
        speed=(vehicle.minSpeed()+random.nextDouble()*(vehicle.maxSpeed()-vehicle.minSpeed()))*config.number("event.convoy.speedMultiplier",1.05);
    }
    @Override protected boolean spawnWave() {
        if(firstWave)openLane=Math.max(0,Math.min(GameConfig.LANE_COUNT-1,
                (int)((world.player().hitbox().getCenterX()-GameConfig.ROAD_LEFT)/GameConfig.LANE_WIDTH)));
        var vehicle=spec(ObstacleType.VEHICLE);
        List<Obstacle> row=new ArrayList<>();
        for(int lane=0;lane<GameConfig.LANE_COUNT;lane++)if(lane!=openLane) {
            double y=-vehicle.height()-config.number("spawn.margin")-row.size()*config.number("event.convoy.staggerY",60);
            row.add(new Vehicle(vehicle,GameConfig.laneX(lane,vehicle.width()),y,speed));
        }
        if(!world.trySpawnGroup(row))return false;
        firstWave=false;
        openLane=openLane==1?(random.nextBoolean()?0:2):1;
        return true;
    }
    @Override protected String warningMessage() {return "Đoàn xe sắp tới – chú ý khoảng trống!";}
    @Override protected String activeMessage() {return "Đoàn xe – tìm lối trống giữa từng đợt";}
}
