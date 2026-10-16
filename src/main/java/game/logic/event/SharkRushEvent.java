package game.logic.event;

import game.level.BaseLevel;
import game.config.GameConfig;
import game.model.obstacle.*;
import java.util.*;

/** Ba đội hình như đòn 3 boss: một làn, hai làn đồng thời, hoặc lần lượt trái–giữa–phải. */
final class SharkRushEvent extends RoadSequenceEvent {
    private int pendingCount;
    SharkRushEvent(BaseLevel world,Random random) {super(world,random,"event.sharks",4,2);}
    @Override protected boolean spawnWave() {
        long active=world.obstacles().stream().filter(o->!o.removed() && o instanceof Shark).count();
        if(pendingCount==0) {
            double single=config.number("event.sharks.singleWeight",1),pair=config.number("event.sharks.pairWeight",1);
            double roll=random.nextDouble()*(single+pair+config.number("event.sharks.sequenceWeight",1));
            pendingCount=roll<single?1:roll<single+pair?2:GameConfig.LANE_COUNT;
        }
        if(active+pendingCount>config.number("event.sharks.maxActive",3))return false;
        var shark=spec(ObstacleType.SHARK);
        double targetY=world.player().hitbox().getCenterY()-shark.height()/2;
        List<Integer> lanes=new ArrayList<>(List.of(0,1,2));Collections.shuffle(lanes,random);
        boolean sequence=pendingCount==GameConfig.LANE_COUNT;
        for(int start=0;start<(sequence?1:lanes.size());start++) {
            List<Obstacle> wave=new ArrayList<>();
            for(int i=0;i<pendingCount;i++) {
                int lane=sequence?i:lanes.get((start+i)%lanes.size());
                double delay=sequence?i*config.number("event.sharks.sequenceDelaySeconds",.5):0;
                wave.add(Shark.wave(shark,lane,targetY,config,delay));
            }
            if(world.trySpawnGroup(wave)) {pendingCount=0;return true;}
        }
        return false;
    }
    @Override protected String warningMessage() {return "Nước động – cá mập sắp trồi lên!";}
    @Override protected String activeMessage() {return "Đàn cá mập – đớp một, hai làn hoặc lần lượt trái–giữa–phải";}
}
