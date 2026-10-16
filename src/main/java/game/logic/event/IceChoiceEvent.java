package game.logic.event;

import game.config.GameConfig;
import game.level.BaseLevel;
import game.model.obstacle.*;
import java.util.*;

/** Làn băng có hố, người tuyết đổi giữa làn giữa và đường khô; cả đội hình phải có đường né. */
final class IceChoiceEvent extends RoadSequenceEvent {
    private int iceLane;
    private Boolean holeInWave;
    private Integer blockerLane;
    IceChoiceEvent(BaseLevel world,Random random) {super(world,random,"event.ice",2,3);}
    @Override protected void onStart() {iceLane=(int)config.number("event.ice.lane",2);}
    @Override protected double holdSeconds() {return config.number("event.ice.roadSeconds",12);}
    @Override public int iceLane() {return sequenceActive() && sequenceTime<holdSeconds()?iceLane:-1;}
    @Override public double iceRoadRemainingSeconds() {return iceLane()<0?0:holdSeconds()-sequenceTime;}
    @Override protected boolean spawnWave() {
        var snowman=spec(ObstacleType.SNOWMAN);
        double margin=config.number("spawn.margin"),scroll=config.number("level.scrollSpeed");
        if(blockerLane==null)blockerLane=random.nextDouble()*100<config.number("event.ice.dryLaneObstacleChance",0)?(iceLane==2?0:2):1;
        var blocker=new Snowman(snowman,GameConfig.laneX(blockerLane,snowman.width()),
                -snowman.height()-margin-config.number("event.ice.staggerY",60),scroll);
        List<Obstacle> group=new ArrayList<>();group.add(blocker);
        if(holeInWave==null)holeInWave=random.nextDouble()*100<config.number("event.ice.hazardChance",65);
        if(holeInWave) {
            var ice=spec(ObstacleType.ICE_HOLE);
            group.add(new IceHole(ice,GameConfig.laneX(iceLane,ice.width()),
                    -ice.height()-margin-config.number("event.ice.staggerY",60)
                    -config.number("event.ice.hazardStaggerY",520),scroll));
        }
        boolean accepted=world.trySpawnGroup(group);
        if(accepted) {holeInWave=null;blockerLane=null;}
        return accepted;
    }
    private String choiceMessage() {
        return iceLane==0?"Băng bên trái tăng tốc – bên phải là đường khô":"Băng bên phải tăng tốc – bên trái là đường khô";
    }
    @Override protected String warningMessage() {return choiceMessage();}
    @Override protected String activeMessage() {return iceLane()<0?"Đang ra khỏi đoạn băng":choiceMessage();}
}
