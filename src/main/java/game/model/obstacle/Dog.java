package game.model.obstacle;

import game.config.ConfigValues;
import game.config.GameConfig;
import game.level.DayConfig.ObstacleSpec;
import game.logic.movement.DogMovement;
import java.awt.geom.Rectangle2D;

/** A seeded, non-homing attack: cross, bark, charge inside a lane, then stay on the road. */
public final class Dog extends Obstacle {
    public enum State { CROSSING, BARKING, CHARGING, STANDING }
    private final DogMovement trajectory;
    private final int lane;
    public Dog(ObstacleSpec spec,double x,double y,double speed,int lane,double targetOffset,ConfigValues config) {
        this(spec,plan(spec,x,y,speed,lane,targetOffset,config),lane);
    }
    private Dog(ObstacleSpec spec,DogMovement trajectory,int lane) {
        super(spec,trajectory);this.trajectory=trajectory;this.lane=lane;
    }
    private static DogMovement plan(ObstacleSpec spec,double x,double y,double speed,int lane,double offset,ConfigValues c) {
        double stopX=GameConfig.laneX(lane,spec.width());
        double cross=Math.abs(stopX-x)/Math.abs(speed);
        if (speed==0 || cross<=0 || lane<0 || lane>=GameConfig.LANE_COUNT) throw new IllegalArgumentException("Invalid dog crossing");
        double range=Math.min(c.number("dog.targetOffsetX"),(GameConfig.LANE_WIDTH-spec.width())/2);
        double targetX=stopX+Math.max(-range,Math.min(range,offset));
        double bark=c.number("dog.barkDurationSeconds"),charge=c.number("dog.chargeDurationSeconds");
        double targetY=c.number("player.y")+c.number("player.hitboxOffsetY")+c.number("player.hitboxHeight")/2-spec.height()/2;
        double distance=Math.max(0,targetY-y-c.number("level.scrollSpeed")*(cross+bark+charge));
        return new DogMovement(x,y,stopX,targetX,cross,bark,charge,distance,c.number("level.scrollSpeed"));
    }
    public State state() {
        if (age()+1e-9<trajectory.crossSeconds()) return State.CROSSING;
        if (age()+1e-9<chargeStart()) return State.BARKING;
        if (age()+1e-9<chargeEnd()) return State.CHARGING;
        return State.STANDING;
    }
    public double stateTime() {
        return age()-switch(state()) {case CROSSING->0;case BARKING->trajectory.crossSeconds();case CHARGING->chargeStart();case STANDING->chargeEnd();};
    }
    public double chargeStart() { return trajectory.crossSeconds()+trajectory.barkSeconds(); }
    public double chargeEnd() { return chargeStart()+trajectory.chargeSeconds(); }
    public double crossSeconds() { return trajectory.crossSeconds(); }
    public int lane() { return lane; }
    public double targetX() { return trajectory.targetX(); }
    public Rectangle2D chargeTarget() { return predictedHitbox(Math.max(0,chargeEnd()-age())); }
}
