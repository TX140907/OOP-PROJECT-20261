package game.logic.spawn;

import game.config.GameConfig;
import game.level.BaseLevel;
import game.model.obstacle.EnemyPlane;
import game.model.obstacle.Obstacle;
import java.awt.geom.Rectangle2D;
import java.util.*;

/** Lệch nhẹ vị trí sinh nếu có máy bay bám đuôi, trước khi kiểm tra đường né cho cả đợt. */
public final class PlaneSpawnPlacement {
    private PlaneSpawnPlacement() {}
    public static Optional<List<Obstacle>> place(BaseLevel world,List<? extends Obstacle> proposed) {
        if(proposed.stream().noneMatch(o->o instanceof EnemyPlane))return Optional.of(new ArrayList<>(proposed));
        List<Obstacle> placed=new ArrayList<>();
        return choose(world,proposed,0,placed)?Optional.of(placed):Optional.empty();
    }
    private static boolean choose(BaseLevel world,List<? extends Obstacle> proposed,int index,List<Obstacle> placed) {
        if(index==proposed.size())return true;
        Obstacle original=proposed.get(index);
        if(!(original instanceof EnemyPlane plane)) {
            placed.add(original);
            if(choose(world,proposed,index+1,placed))return true;
            placed.remove(placed.size()-1);return false;
        }
        var c=world.config().values();double offset=c.number("enemyPlane.tailOffsetX");
        Set<Double> positions=new LinkedHashSet<>();
        for(double shift:new double[]{0,offset,-offset})
            positions.add(Math.max(GameConfig.ROAD_LEFT,Math.min(GameConfig.ROAD_RIGHT-plane.width(),plane.x()+shift)));
        for(double x:positions) {
            if(hasTail(world,placed,plane,x))continue;
            Rectangle2D bounds=plane.hitboxAt(x,plane.y());
            // Thành viên cùng đội hình được sát mép, nhưng không được chồng thân khi lệch X.
            if(placed.stream().anyMatch(o->o instanceof EnemyPlane && o.hitbox().intersects(bounds)))continue;
            placed.add(x==plane.x()?plane:plane.atSpawnX(x));
            if(choose(world,proposed,index+1,placed))return true;
            placed.remove(placed.size()-1);
        }
        return false;
    }
    private static boolean hasTail(BaseLevel world,List<Obstacle> placed,EnemyPlane candidate,double x) {
        var c=world.config().values();
        double alignment=c.number("enemyPlane.tailAlignmentPixels"),maxGap=c.number("enemyPlane.tailGapPixels");
        double center=x+candidate.width()/2;
        List<Obstacle> neighbors=new ArrayList<>(world.obstacles());neighbors.addAll(placed);
        for(Obstacle other:neighbors)if(other instanceof EnemyPlane && !other.removed()) {
            double gap=Math.max(other.y()-(candidate.y()+candidate.height()),candidate.y()-(other.y()+other.height()));
            if(gap>=0 && gap<=maxGap && Math.abs(other.hitbox().getCenterX()-center)<=alignment)return true;
        }
        return false;
    }
}
