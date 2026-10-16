package game.model.boss;

import game.config.ConfigValues;
import game.model.Entity;

/** Thân boss và chuyển động; nhịp ra đòn do AirBossEncounter điều khiển. */
public final class AirBoss extends Entity {
    private final int hitsToDefeat;
    private int hitsTaken;
    private double fromX,fromY,toX,toY,moveTime,moveDuration;
    public AirBoss(ConfigValues c) {
        super((1000-c.number("boss.width"))/2,-c.number("boss.height"),c.number("boss.width"),c.number("boss.height"),
                c.number("boss.width"),c.number("boss.height"),0,0);
        hitsToDefeat=c.integer("boss.hitsToDefeat");
    }
    public void moveTo(double x,double y,double seconds) {
        if(seconds<=0)throw new IllegalArgumentException("Boss movement duration must be positive");
        fromX=x();fromY=y();toX=x;toY=y;moveTime=0;moveDuration=seconds;
        if(fromX==toX && fromY==toY)moveDuration=0;
    }
    public void update(double dt) {
        if(!moving())return;
        moveTime=Math.min(moveDuration,moveTime+dt);
        double t=moveTime/moveDuration,s=t*t*(3-2*t);
        position(fromX+(toX-fromX)*s,fromY+(toY-fromY)*s);
    }
    public boolean moving() { return moveTime<moveDuration-1e-9; }
    public void takeHit() { hitsTaken=Math.min(hitsToDefeat,hitsTaken+1); }
    /** Hồi theo đơn vị phát bắn, tối đa đầy máu và không hồi sinh boss đã bị hạ. */
    public void healHits(int amount) {
        if(amount<0)throw new IllegalArgumentException("Boss healing must not be negative");
        if(!defeated())hitsTaken=Math.max(0,hitsTaken-amount);
    }
    public double healthFraction() { return 1-(double)hitsTaken/hitsToDefeat; }
    public boolean defeated() { return hitsTaken>=hitsToDefeat; }
}
