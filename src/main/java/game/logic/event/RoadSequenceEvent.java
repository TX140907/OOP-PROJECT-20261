package game.logic.event;

import game.config.ConfigValues;
import game.level.BaseLevel;
import game.level.DayConfig.ObstacleSpec;
import game.model.obstacle.*;
import java.util.Random;

/** Báo trước, ngừng sinh thường, chờ đường thông rồi chạy từng đợt an toàn và chờ vật cản đi hết. */
abstract class RoadSequenceEvent implements LevelEvent {
    private enum Phase { WAITING, WARNING, CLEAR_OLD, RUNNING, DRAINING, DONE }
    protected final BaseLevel world;
    protected final ConfigValues config;
    protected final Random random;
    private final int waveCount;
    private final double interval;
    private Phase phase=Phase.WAITING;
    private double phaseTime,cooldown;
    protected double sequenceTime;
    private int wavesSpawned;
    RoadSequenceEvent(BaseLevel world,Random random,String prefix,int waves,double seconds) {
        this.world=world;this.random=random;config=world.config().values();
        waveCount=(int)config.number(prefix+".waveCount",waves);
        interval=config.number(prefix+".intervalSeconds",seconds);
    }
    @Override public final void update(double dt) {
        if(phase==Phase.DONE)return;
        if(phase==Phase.WAITING) {
            if(world.progress().fraction()*world.config().duration()+1e-9<config.number("event.startSeconds",16))return;
            onStart();enter(Phase.WARNING);return;
        }
        phaseTime+=dt;
        if(sequenceActive())sequenceTime+=dt;
        switch(phase) {
            case WARNING -> {
                if(phaseTime>=config.number("event.warningSeconds",1.2))enter(Phase.CLEAR_OLD);
            }
            case CLEAR_OLD -> {
                if(!hasHazardsAhead())enter(Phase.RUNNING);
                else if(phaseTime>=config.number("event.clearTimeoutSeconds",7))enter(Phase.DONE);
            }
            case RUNNING -> {
                if(phaseTime>=config.number("event.maxDurationSeconds",10)) {enter(Phase.DRAINING);return;}
                cooldown-=dt;
                if(cooldown>1e-9)return;
                if(spawnWave()) {
                    wavesSpawned++;cooldown=interval;
                    if(wavesSpawned>=waveCount)enter(Phase.DRAINING);
                } else cooldown=config.number("event.retrySeconds",.35);
            }
            case DRAINING -> {
                if(sequenceTime>=holdSeconds() && (!hasHazardsAhead()
                        || phaseTime>=config.number("event.clearTimeoutSeconds",7)))enter(Phase.DONE);
            }
            default -> { }
        }
    }
    private void enter(Phase next) {phase=next;phaseTime=0;}
    private boolean hasHazardsAhead() {
        return world.obstacles().stream().anyMatch(o->!o.removed() && switch(o.effect().kind()) {
            case HEAL,ENERGY,SHIELD -> false;
            default -> o instanceof Shark || o.hitbox().getMinY()<=world.player().hitbox().getMaxY();
        });
    }
    protected final ObstacleSpec spec(ObstacleType type) {
        return world.config().obstacles().stream().filter(s->s.type()==type).findFirst().orElseThrow();
    }
    protected void onStart() {}
    protected final boolean sequenceActive() {return phase==Phase.RUNNING || phase==Phase.DRAINING;}
    protected double holdSeconds() {return 0;}
    protected abstract boolean spawnWave();
    protected abstract String warningMessage();
    protected abstract String activeMessage();
    @Override public final boolean allowsNormalSpawns() {return phase==Phase.WAITING || phase==Phase.DONE;}
    @Override public final String message() {
        return switch(phase) {
            case WAITING,DONE -> "";
            case WARNING,CLEAR_OLD -> warningMessage();
            case RUNNING,DRAINING -> activeMessage();
        };
    }
}
