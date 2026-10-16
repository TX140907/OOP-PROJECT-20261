package game.model.obstacle;

import game.level.BaseLevel;
import game.level.DayConfig.ObstacleSpec;
import game.config.ConfigValues;
import game.config.GameConfig;
import game.logic.movement.LinearMovement;
import java.awt.geom.Rectangle2D;

/** Explicit FSM. A new warning always receives its full configured duration. */
public final class Shark extends Obstacle {
    public enum State { FIN_VISIBLE, DIVING, WARNING, ATTACK, DISAPPEAR }
    private final ConfigValues config;
    private State state=State.FIN_VISIBLE;
    private double stateTime;
    private boolean scripted;
    private double waveAge,targetX,targetY,warningAt;
    public Shark(ObstacleSpec spec,double x,double y,ConfigValues config) {
        super(spec,new LinearMovement(x,y,0,0)); this.config=config;
    }
    /** Đặt trước cả đợt để kiểm tra đường né; vẫn giữ đủ vây, lặn và cảnh báo cho từng con. */
    public static Shark wave(ObstacleSpec spec,int lane,double targetY,ConfigValues config,double delay) {
        if(delay<0)throw new IllegalArgumentException("Shark warning delay must not be negative");
        double x=GameConfig.laneX(lane,spec.width());
        Shark shark=new Shark(spec,x,-spec.height()/2+config.number("shark.finHeight")/2,config);
        shark.scripted=true;shark.targetX=x;shark.targetY=targetY;
        shark.warningAt=config.number("shark.finDuration")+config.number("shark.divingDuration")+delay;
        return shark;
    }
    @Override public void update(double dt,BaseLevel world) {
        if(scripted) {updateWave(dt);return;}
        stateTime+=dt;
        switch (state) {
            case FIN_VISIBLE -> {
                position(x(),y()+config.number("shark.finSpeed")*dt);
                if (stateTime+1e-9>=config.number("shark.finDuration")) enter(State.DIVING);
            }
            case DIVING -> {
                if (stateTime+1e-9>=config.number("shark.divingDuration")) world.chooseSharkAttack(this);
            }
            case WARNING -> { if (stateTime+1e-9>=config.number("shark.warningDuration")) enter(State.ATTACK); }
            case ATTACK -> { if (stateTime+1e-9>=config.number("shark.attackDuration")) { enter(State.DISAPPEAR); remove(); } }
            default -> { }
        }
    }
    private void updateWave(double dt) {
        waveAge+=dt;
        double fin=config.number("shark.finDuration"),warning=config.number("shark.warningDuration");
        if(waveAge+1e-9<fin) {state=State.FIN_VISIBLE;stateTime=waveAge;}
        else if(waveAge+1e-9<warningAt) {state=State.DIVING;stateTime=waveAge-fin;}
        else if(waveAge+1e-9<warningAt+warning) {state=State.WARNING;stateTime=waveAge-warningAt;}
        else if(waveAge+1e-9<warningAt+warning+config.number("shark.attackDuration")) {
            state=State.ATTACK;stateTime=waveAge-warningAt-warning;
        } else {state=State.DISAPPEAR;stateTime=0;remove();}
        Rectangle2D area=waveHitbox(waveAge);position(area.getX(),area.getY());
    }
    private Rectangle2D waveHitbox(double time) {
        if(time+1e-9>=warningAt)return hitboxAt(targetX,targetY);
        double finY=-height()/2+config.number("shark.finHeight")/2
                +config.number("shark.finSpeed")*Math.min(time,config.number("shark.finDuration"));
        return hitboxAt(targetX,finY);
    }
    public void proposeTarget(double x,double y) { position(x,y); }
    public void beginWarning() { enter(State.WARNING); }
    public void cancelTarget(double x,double y) { position(x,y); state=State.DIVING; stateTime=config.number("shark.divingDuration"); }
    private void enter(State next) { state=next; stateTime=0; }
    public State state() { return state; }
    public double stateTime() { return stateTime; }
    @Override public Rectangle2D predictedHitbox(double seconds) {
        if(scripted)return waveHitbox(waveAge+seconds);
        double travel=state==State.FIN_VISIBLE?config.number("shark.finSpeed")
                *Math.min(seconds,Math.max(0,config.number("shark.finDuration")-stateTime)):0;
        return hitboxAt(x(),y()+travel);
    }
    @Override public boolean activeAt(double seconds) {
        if (removed()) return false;
        if(scripted) {
            double time=waveAge+seconds;
            return time+1e-9>=warningAt+config.number("shark.warningDuration")
                    && time+1e-9<warningAt+config.number("shark.warningDuration")+config.number("shark.attackDuration");
        }
        if (state==State.WARNING) return stateTime+seconds>=config.number("shark.warningDuration")
                && stateTime+seconds<config.number("shark.warningDuration")+config.number("shark.attackDuration");
        return state==State.ATTACK && stateTime+seconds<config.number("shark.attackDuration");
    }
    @Override public boolean occupiesAt(double seconds) {
        if(scripted)return !removed() && waveAge+seconds<warningAt+config.number("shark.warningDuration")+config.number("shark.attackDuration");
        if (state==State.WARNING) return stateTime+seconds<config.number("shark.warningDuration")+config.number("shark.attackDuration");
        if (state==State.ATTACK) return activeAt(seconds);
        if (state==State.DIVING) return !removed(); // The ripple remains reserved while waiting for a safe target.
        return !removed() && seconds<Math.max(0,config.number("shark.finDuration")-stateTime)+config.number("shark.divingDuration");
    }
    public boolean targeting() { return state==State.WARNING || state==State.ATTACK; }
}
