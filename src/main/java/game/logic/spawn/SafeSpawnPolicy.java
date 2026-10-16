package game.logic.spawn;

import game.config.ConfigValues;
import game.config.GameConfig;
import game.logic.effect.EffectManager.Type;
import game.level.BaseLevel;
import game.logic.movement.HorizontalMotion;
import game.model.obstacle.Obstacle;
import java.awt.geom.Rectangle2D;
import java.util.*;

/** Conservative finite-horizon reachability, preserving real positions and velocities in every path.
 * Grid/beam pruning can reject a possible path, but never joins disconnected states by teleporting.
 * Predictions cover implemented deterministic movements; future random attacks are checked when targeted.
 */
public final class SafeSpawnPolicy {
    public boolean accepts(BaseLevel world,Obstacle candidate) {
        candidate.attachTo(world);
        if(!LaneSpawnPolicy.accepts(world,candidate))return false;
        if (candidate.harmfulAt(0) && candidate.collisionShape().intersects(world.player().hitbox())) return false;
        List<Obstacle> hazards=new ArrayList<>(world.obstacles());
        if (!hazards.contains(candidate)) hazards.add(candidate);
        ConfigValues c=world.config().values();
        double step=c.number("spawn.stepSeconds")/2, horizon=predictionHorizon(world,hazards);
        double gap=c.number("spawn.minimumGap");
        for (Obstacle existing:world.obstacles()) if (existing!=candidate && !existing.removed()
                && !candidate.allowsOverlap(existing) && !existing.allowsOverlap(candidate)) {
            for (double t=0;t<=horizon;t+=step) {
                if (!candidate.occupiesAt(t) || !existing.occupiesAt(t)) continue;
                Rectangle2D a=candidate.predictedHitbox(t), b=existing.predictedHitbox(t);
                Rectangle2D expanded=new Rectangle2D.Double(a.getX()-gap,a.getY()-gap,a.getWidth()+2*gap,a.getHeight()+2*gap);
                if (expanded.intersects(b)) return false;
            }
        }
        return reachable(world,hazards,null,0,horizon);
    }
    public boolean canCollect(BaseLevel world,Obstacle pickup,double seconds) {
        return reachable(world,world.obstacles(),pickup.predictedHitbox(seconds),seconds,seconds);
    }
    /** Include late arrivals and their entire blocking interval, rather than stopping before they reach the player. */
    public double predictionHorizon(BaseLevel world,List<Obstacle> hazards) {
        ConfigValues c=world.config().values();
        double limit=c.number("spawn.maxPredictionSeconds"),step=c.number("spawn.stepSeconds");
        double lastThreat=0,top=world.player().hitbox().getY(),height=world.player().hitbox().getHeight();
        double left=c.number("player.minX"),width=c.number("player.maxX")+world.player().width()-left;
        for (Obstacle hazard:hazards) if (!hazard.removed()) {
            for (double t=0;t<limit-1e-9;t+=step) {
                double end=Math.min(limit,t+step);
                if (!(hazard.harmfulAt(t)||hazard.harmfulAt(end))) continue;
                Rectangle2D swept=hazard.predictedHitbox(t).createUnion(hazard.predictedHitbox(end));
                if (swept.intersects(left,top,width,height)) lastThreat=Math.max(lastThreat,end);
            }
        }
        return Math.min(limit,Math.max(c.number("spawn.horizonSeconds"),lastThreat+c.number("spawn.escapeBufferSeconds")));
    }
    private boolean reachable(BaseLevel world,List<Obstacle> hazards,Rectangle2D goal,double goalSeconds,double horizon) {
        ConfigValues c=world.config().values();
        double step=c.number("spawn.stepSeconds");
        Map<Long,List<Rectangle2D>> forecast=new HashMap<>();
        double clearance=c.number("spawn.pathClearancePixels");
        double hitOffset=c.number("player.hitboxOffsetX")-clearance,hitWidth=c.number("player.hitboxWidth")+2*clearance;
        double hitY=world.player().hitbox().getY(),hitHeight=world.player().hitbox().getHeight();
        if (goal==null && directRoute(world,hazards,horizon,forecast,hitOffset,hitWidth,hitY,hitHeight)) return true;
        List<HorizontalMotion.State> states=List.of(new HorizontalMotion.State(world.player().x(),world.player().velocity(),world.effects().iceSlideSeconds(),world.effects().remaining(Type.SLIDE)));
        for (double time=0;time<horizon-1e-9;time+=step) {
            double interval=Math.min(step,horizon-time);
            Map<String,HorizontalMotion.State> next=new LinkedHashMap<>();
            for (HorizontalMotion.State state:states) {
                for (int direction=-1;direction<=1;direction++) {
                    HorizontalMotion.State move=state; boolean safe=true;
                    for (double sub=0;sub<interval-1e-9;sub+=1.0/60) {
                        double dt=Math.min(1.0/60,interval-sub), at=time+sub;
                        int input=at<c.number("spawn.safeReactionTimeSeconds")?world.currentDirection():direction;
                        HorizontalMotion.State after=HorizontalMotion.step(move,input,dt,c,world.effects(),at);
                        List<Rectangle2D> dangers=dangersAt(hazards,forecast,at,dt);
                        double left=Math.min(move.x(),after.x())+hitOffset;
                        double width=Math.abs(move.x()-after.x())+hitWidth;
                        for (Rectangle2D danger:dangers) {
                            if (danger.intersects(left,hitY,width,hitHeight)) { safe=false; break; }
                        }
                        if (!safe) break;
                        move=after;
                    }
                    if (safe) {
                        if (goal!=null && time+interval>=horizon-1e-9 && world.player().hitboxAt(move.x(),world.player().y()).intersects(goal)) return true;
                        String key=Math.round(move.x()/c.number("spawn.gridPixels"))+":"+Math.round(move.velocity()/c.number("spawn.velocityGrid"))
                                +":"+Math.round(move.iceSeconds()*2)+":"+Math.round(move.slideUntil()*2);
                        next.putIfAbsent(key,move);
                    }
                }
            }
            if (next.isEmpty()) return false;
            states=new ArrayList<>(next.values());
            int limit=c.integer("spawn.maxPlannerStates");
            if (states.size()>limit) {
                states.sort(Comparator.comparingDouble(HorizontalMotion.State::x).thenComparingDouble(HorizontalMotion.State::velocity));
                List<HorizontalMotion.State> spread=new ArrayList<>();
                for(int i=0;i<limit;i++) spread.add(states.get(limit==1?states.size()/2:(int)Math.round(i*(states.size()-1.0)/(limit-1))));
                states=spread;
            }
        }
        return goal==null && !states.isEmpty();
    }
    /** Cheap complete trajectories handle open lanes first; difficult layouts still use branching search. */
    private boolean directRoute(BaseLevel world,List<Obstacle> hazards,double horizon,Map<Long,List<Rectangle2D>> forecast,
                                double hitOffset,double hitWidth,double hitY,double hitHeight) {
        ConfigValues c=world.config().values();
        double[] targets={world.player().x(),GameConfig.laneX(0,world.player().width()),
                GameConfig.laneX(1,world.player().width()),GameConfig.laneX(2,world.player().width())};
        for(double target:targets) {
            HorizontalMotion.State move=new HorizontalMotion.State(world.player().x(),world.player().velocity(),world.effects().iceSlideSeconds(),world.effects().remaining(Type.SLIDE));boolean safe=true;
            for(double at=0;at<horizon-1e-9;at+=GameConfig.STEP) {
                double dt=Math.min(GameConfig.STEP,horizon-at),distance=target-move.x();
                double deceleration=c.number("player.deceleration");
                if(HorizontalMotion.sliding(move,c,world.effects(),at)) deceleration*=c.number("ice.decelerationMultiplier");
                double stopDistance=move.velocity()*move.velocity()/(2*deceleration)+Math.abs(move.velocity())*dt;
                int direction=Math.signum(move.velocity())==Math.signum(distance) && Math.abs(distance)<=stopDistance?0:(int)Math.signum(distance);
                if(at<c.number("spawn.safeReactionTimeSeconds")) direction=world.currentDirection();
                HorizontalMotion.State after=HorizontalMotion.step(move,direction,dt,c,world.effects(),at);
                double left=Math.min(move.x(),after.x())+hitOffset,width=Math.abs(move.x()-after.x())+hitWidth;
                for(Rectangle2D danger:dangersAt(hazards,forecast,at,dt)) if(danger.intersects(left,hitY,width,hitHeight)) {safe=false;break;}
                if(!safe)break;
                move=after;
            }
            if(safe)return true;
        }
        return false;
    }
    private List<Rectangle2D> dangersAt(List<Obstacle> hazards,Map<Long,List<Rectangle2D>> forecast,double at,double dt) {
        return forecast.computeIfAbsent(Double.doubleToLongBits(at),key->{
            List<Rectangle2D> predicted=new ArrayList<>();
            for(Obstacle hazard:hazards) if(!hazard.removed() && (hazard.harmfulAt(at)||hazard.harmfulAt(at+dt)))
                predicted.add(hazard.predictedHitbox(at).createUnion(hazard.predictedHitbox(at+dt)));
            return predicted;
        });
    }
}
