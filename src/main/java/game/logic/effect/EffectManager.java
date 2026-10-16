package game.logic.effect;

import game.config.ConfigValues;
import game.config.GameConfig;
import java.util.EnumMap;

/** Timers refresh by maximum duration; slowing combines by minimum, never unbounded multiplication. */
public final class EffectManager {
    public enum Type { HIT, DRAIN, SHARK_SLOW, SLIDE, FREEZE, SNOWBALL_SLOW, ENERGY_BOOST }
    private final EnumMap<Type,Double> timers = new EnumMap<>(Type.class);
    private double energyFraction=1;
    private int iceRoadLane=-1;
    private double iceRoadSeconds,iceSlideSeconds,icePosition;
    private long revision;
    public long revision() { return revision; }
    public void setIceRoad(int lane,double remaining) {
        if(iceRoadLane!=lane || iceRoadSeconds!=remaining)revision++;
        iceRoadLane=lane;iceRoadSeconds=remaining;
    }
    public void setIcePosition(double x) {
        if(icePosition!=x)revision++;
        icePosition=x;
    }
    public boolean onIceRoad(double x,ConfigValues c,double future) {
        if(iceRoadLane<0 || future>=iceRoadSeconds)return false;
        double center=x+c.number("player.hitboxOffsetX")+c.number("player.hitboxWidth")/2;
        double left=GameConfig.ROAD_LEFT+iceRoadLane*GameConfig.LANE_WIDTH;
        return center>=left && center<left+GameConfig.LANE_WIDTH;
    }
    public double iceSlideSeconds() {return iceSlideSeconds;}
    public void setIceSlideSeconds(double seconds) {
        if(iceSlideSeconds!=seconds)revision++;
        iceSlideSeconds=seconds;
    }
    private double iceMultiplier(ConfigValues c,double base,double seconds) {
        return Math.min(c.number("ice.maxSpeedMultiplier",base),base+c.number("ice.rampPerSecond",0)*seconds);
    }
    public double iceSpeedMultiplier(ConfigValues c,double seconds) {
        return iceMultiplier(c,c.number("ice.speedMultiplier"),seconds);
    }
    public void setEnergyFraction(double fraction) {
        double next=Math.max(0,Math.min(1,fraction));
        if (energyFraction!=next) {energyFraction=next;revision++;}
    }
    public void add(Type type,double seconds) { timers.merge(type,seconds,Math::max);revision++; }
    public void update(double dt) {
        if (dt>0 && timers.values().stream().anyMatch(t->t>0)) revision++;
        timers.replaceAll((type,time)->time-dt<1e-9?0:time-dt);
    }
    public double remaining(Type type) { return timers.getOrDefault(type,0.0); }
    public boolean active(Type type) { return remaining(type)>0; }
    public double movementMultiplier(ConfigValues c,double future) {
        double value=1;
        if (remaining(Type.HIT)>future+1e-9) value=Math.min(value,c.number("player.hitSlowMultiplier"));
        if (remaining(Type.DRAIN)>future+1e-9) value=Math.min(value,c.number("drain.slowMultiplier"));
        if (remaining(Type.SHARK_SLOW)>future+1e-9) value=Math.min(value,c.number("shark.slowMultiplier",.65));
        if (remaining(Type.SNOWBALL_SLOW)>future+1e-9) value=Math.min(value,c.number("snowball.slowMultiplier"));
        return value*energyMultiplier(c,future);
    }
    public double progressMultiplier(ConfigValues c) {
        return progressMultiplier(c,0);
    }
    public double progressMultiplier(ConfigValues c,double future) {
        if (remaining(Type.FREEZE)>future+1e-9) return c.number("snowball.freezeProgressMultiplier");
        double value=1;
        if (remaining(Type.DRAIN)>future+1e-9) value=Math.min(value,c.number("drain.progressMultiplier"));
        if (remaining(Type.SHARK_SLOW)>future+1e-9) value=Math.min(value,c.number("shark.progressMultiplier",.65));
        if (remaining(Type.SNOWBALL_SLOW)>future+1e-9) value=Math.min(value,c.number("snowball.progressMultiplier"));
        double slideUntil=remaining(Type.SLIDE);
        if(onIceRoad(icePosition,c,0))slideUntil=Math.max(slideUntil,iceRoadSeconds+c.number("ice.durationSeconds"));
        double ice=slideUntil>future+1e-9?iceMultiplier(c,c.number("ice.progressMultiplier",1),iceSlideSeconds+future):1;
        return value*energyMultiplier(c,future)*ice;
    }
    public double energyMultiplier(ConfigValues c,double future) {
        if (!c.bool("energy.enabled")) return 1;
        if (remaining(Type.ENERGY_BOOST)>future+1e-9) return c.number("energy.pickupBoostMultiplier");
        double fraction=Math.max(0,energyFraction-c.number("energy.drainPerSecond")*future/c.number("energy.max"));
        return 1-c.number("energy.maxSlowdown")*(1-fraction);
    }
    public void clear() { timers.clear();iceRoadLane=-1;iceRoadSeconds=iceSlideSeconds=0;revision++; }
}
