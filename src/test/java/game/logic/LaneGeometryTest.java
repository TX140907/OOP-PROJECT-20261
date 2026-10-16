package game.logic;

import game.TestSupport;
import game.config.GameConfig;
import game.level.Levels;
import game.model.obstacle.ObstacleType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LaneGeometryTest {
    @Test void centeredLaneHazardsReachAPlayerStandingOnEitherLaneBoundary() {
        for(int id:Levels.ORDER) {
            var world=TestSupport.level(id);var player=world.player();
            for(var spec:world.config().obstacles()) {
                if(spec.type()==ObstacleType.DOG)continue; // Dog crosses and charges to random X within its lane.
                for(int boundary=1;boundary<3;boundary++) {
                    double center=GameConfig.ROAD_LEFT+boundary*GameConfig.LANE_WIDTH;
                    var standing=player.hitboxAt(center-player.width()/2,player.y());
                    for(int lane:new int[]{boundary-1,boundary}) {
                        var hazard=new java.awt.geom.Rectangle2D.Double(GameConfig.laneX(lane,spec.width()),player.y(),spec.width(),spec.height());
                        assertTrue(hazard.intersects(standing),"Boundary exploit: level="+id+", type="+spec.type()+", boundary="+boundary);
                    }
                }
            }
        }
    }
}
