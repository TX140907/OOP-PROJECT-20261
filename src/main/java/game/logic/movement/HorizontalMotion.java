package game.logic.movement;

import game.config.ConfigValues;
import game.logic.effect.EffectManager;
import static game.logic.effect.EffectManager.Type.*;

/** Reused by both real player physics and the reachability planner. */
public final class HorizontalMotion {
    /** slideUntil là mốc thời gian tính từ đầu lần dự báo, không phải đồng hồ giảm dần. */
    public record State(double x,double velocity,double iceSeconds,double slideUntil) {
        public State(double x,double velocity) {this(x,velocity,0,0);}
    }
    private HorizontalMotion() {}
    public static State step(State state,int direction,double dt,ConfigValues c,EffectManager effects,double future) {
        if (effects.remaining(FREEZE)>future+1e-9) return new State(state.x(),0,state.iceSeconds(),state.slideUntil());
        double multiplier=effects.movementMultiplier(c,future);
        double until=Math.max(state.slideUntil(),effects.remaining(SLIDE));
        if(effects.onIceRoad(state.x(),c,future))until=Math.max(until,future+c.number("ice.durationSeconds"));
        boolean ice=until>future+1e-9;
        double elapsed=ice?state.iceSeconds():0;
        double limit=c.number("player.maxSpeed")*multiplier*(ice?effects.iceSpeedMultiplier(c,elapsed):1);
        double deceleration=c.number("player.deceleration")*(ice?c.number("ice.decelerationMultiplier"):1);
        double acceleration=c.number("player.acceleration")*multiplier*(ice?c.number("ice.accelerationMultiplier",1):1);
        double velocity=state.velocity();
        if (direction==0 || (velocity!=0 && Math.signum(velocity)!=direction)) velocity=approach(velocity,0,deceleration*dt);
        else velocity=approach(velocity,direction*limit,acceleration*dt);
        velocity=Math.max(-limit,Math.min(limit,velocity));
        double x=state.x()+velocity*dt;
        if (x<=c.number("player.minX")) { x=c.number("player.minX"); if (velocity<0) velocity=0; }
        if (x>=c.number("player.maxX")) { x=c.number("player.maxX"); if (velocity>0) velocity=0; }
        return new State(x,velocity,ice?elapsed+Math.min(dt,Math.max(0,until-future)):0,until);
    }
    public static boolean sliding(State state,ConfigValues c,EffectManager effects,double future) {
        return Math.max(state.slideUntil(),effects.remaining(SLIDE))>future+1e-9 || effects.onIceRoad(state.x(),c,future);
    }
    private static double approach(double value,double target,double amount) {
        return value<target?Math.min(target,value+amount):Math.max(target,value-amount);
    }
}
