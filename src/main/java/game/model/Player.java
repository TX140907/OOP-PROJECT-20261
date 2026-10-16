package game.model;

import game.config.ConfigValues;
import game.level.DayConfig;
import game.logic.effect.EffectManager;
import game.logic.movement.HorizontalMotion;
import java.awt.geom.Rectangle2D;

public class Player extends Entity {
    private final DayConfig config;
    private double velocity, invulnerability, blinkRemaining;
    private double shieldRemaining,phaseThroughRemaining;
    private int health;
    private Rectangle2D previousHitbox;
    public Player(DayConfig config) {
        super((config.minX()+config.maxX())/2,config.values().number("player.y"),
                config.values().number("player.width"),config.values().number("player.height"),
                config.values().number("player.hitboxWidth"),config.values().number("player.hitboxHeight"),
                config.values().number("player.hitboxOffsetX"),config.values().number("player.hitboxOffsetY"));
        this.config=config; health=config.values().integer("level.initialHealth");previousHitbox=hitbox();
    }
    public void update(double dt,int direction,EffectManager effects) {
        previousHitbox=hitbox();
        updateVisuals(dt);
        shieldRemaining=Math.max(0,shieldRemaining-dt);
        invulnerability=Math.max(0,invulnerability-dt);
        effects.setIcePosition(x());
        if(effects.onIceRoad(x(),config.values(),0))effects.add(EffectManager.Type.SLIDE,config.values().number("ice.durationSeconds"));
        HorizontalMotion.State next=HorizontalMotion.step(new HorizontalMotion.State(x(),velocity,
                effects.iceSlideSeconds(),effects.remaining(EffectManager.Type.SLIDE)),direction,dt,config.values(),effects,0);
        position(next.x(),y()); velocity=next.velocity();
        effects.setIcePosition(x());effects.setIceSlideSeconds(next.iceSeconds());
    }
    public boolean hurt(int amount) {
        if (shielded() || invulnerability>0 || health<=0) return false;
        health=Math.max(0,health-amount); invulnerability=config.values().number("player.hitInvulnerabilitySeconds");
        blinkRemaining=config.values().number("player.hitBlinkDurationSeconds");return true;
    }
    public void kill() {
        if (health>0) blinkRemaining=config.values().number("player.hitBlinkDurationSeconds");
        health=0;stop();
    }
    public int heal(int amount) {
        if(amount<0) throw new IllegalArgumentException("Negative healing");
        if(dead())return 0;
        int before=health;health=Math.min(maxHealth(),health+amount);return health-before;
    }
    public int maxHealth() { return config.values().integer("player.maxHealth"); }
    public void grantShield(double seconds) { shieldRemaining=Math.max(shieldRemaining,seconds); }
    public boolean shielded() { return shieldRemaining>1e-9; }
    public double shieldRemaining() { return shieldRemaining; }
    public void phaseThrough() { phaseThroughRemaining=config.values().number("boss.shield.contactFadeSeconds"); }
    public float opacity() { return phaseThroughRemaining>0?(float)config.values().number("boss.shield.contactAlpha"):1; }
    public void updateVisuals(double dt) {
        blinkRemaining=Math.max(0,blinkRemaining-dt);
        phaseThroughRemaining=Math.max(0,phaseThroughRemaining-dt);
    }
    public boolean blinking() { return blinkRemaining>0; }
    public boolean visible() {
        double elapsed=config.values().number("player.hitBlinkDurationSeconds")-blinkRemaining;
        return !blinking() || (int)((elapsed+1e-9)/config.values().number("player.hitBlinkIntervalSeconds"))%2==0;
    }
    public void stop() { velocity=0; }
    public int health() { return health; }
    public boolean dead() { return health<=0; }
    public double velocity() { return velocity; }
    public Rectangle2D previousHitbox() { return (Rectangle2D)previousHitbox.clone(); }
    public double invulnerability() { return invulnerability; }
    public DayConfig config() { return config; }
}
