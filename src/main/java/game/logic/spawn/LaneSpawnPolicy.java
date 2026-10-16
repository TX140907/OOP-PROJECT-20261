package game.logic.spawn;

import game.config.GameConfig;
import game.level.BaseLevel;
import game.model.obstacle.Obstacle;
import java.awt.geom.Rectangle2D;

/** Hạn chế hàng vật cản dài trên một làn; không tính vật phẩm hoặc vật đã qua người chơi. */
public final class LaneSpawnPolicy {
    private LaneSpawnPolicy() {}
    public static boolean accepts(BaseLevel world,Obstacle candidate) {
        var config=world.config().values();
        int limit=(int)config.number("spawn.maxObstaclesPerLane",0);
        double gap=config.number("spawn.sameLaneGapPixels",0);
        if((limit==0 && gap==0) || !reservesLane(candidate))return true;
        Rectangle2D area=candidate.laneReservation();
        double playerBottom=world.player().hitbox().getMaxY();
        if(area.getMinY()>playerBottom)return true;
        for(int lane=0;lane<GameConfig.LANE_COUNT;lane++) {
            double left=GameConfig.ROAD_LEFT+lane*GameConfig.LANE_WIDTH;
            double right=left+GameConfig.LANE_WIDTH;
            if(area.getMaxX()<=left || area.getMinX()>=right)continue;
            int count=0;
            for(Obstacle existing:world.obstacles()) {
                if(existing==candidate || !reservesLane(existing))continue;
                Rectangle2D other=existing.laneReservation();
                if(other.getMinY()>playerBottom || other.getMaxX()<=left || other.getMinX()>=right)continue;
                count++;
                double separation=Math.max(area.getMinY()-other.getMaxY(),other.getMinY()-area.getMaxY());
                if(gap>0 && separation<gap)return false;
            }
            if(limit>0 && count>=limit)return false;
        }
        return true;
    }
    private static boolean reservesLane(Obstacle obstacle) {
        if(obstacle.removed())return false;
        return switch(obstacle.effect().kind()) {
            case HEAL,ENERGY,SHIELD -> false;
            default -> true;
        };
    }
}
