package game.model;
import game.TestSupport;
import game.logic.effect.EffectManager;
import game.logic.effect.EffectManager.Type;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PlayerTest {
    private void ticks(Player p,EffectManager e,int direction,int count) { for(int i=0;i<count;i++) {p.update(1.0/60,direction,e);e.update(1.0/60);} }
    @Test void accelerateAndRespectSpeedCap() {
        Player p=new Player(TestSupport.config(1));EffectManager e=new EffectManager();double x=p.x();
        ticks(p,e,1,1);assertEquals(25,p.velocity(),.001);assertTrue(p.x()>x);
        ticks(p,e,1,12);assertTrue(p.velocity()<=420);assertTrue(p.velocity()>250);
    }
    @Test void leftRightDecelerationAndReversal() {
        Player p=new Player(TestSupport.config(1));EffectManager e=new EffectManager();ticks(p,e,1,5);
        double speed=p.velocity();ticks(p,e,0,1);assertTrue(p.velocity()<speed);
        ticks(p,e,-1,1);assertTrue(p.velocity()>=0);ticks(p,e,-1,10);assertTrue(p.velocity()<0);
    }
    @Test void clampAtBothEdgesAndClearOutwardVelocity() {
        Player p=new Player(TestSupport.config(1));EffectManager e=new EffectManager();ticks(p,e,-1,200);
        assertEquals(140,p.x());assertEquals(0,p.velocity());ticks(p,e,1,300);
        assertEquals(760,p.x());assertEquals(0,p.velocity());
    }
    @Test void hitboxIsCenteredAndSmallerThanSprite() {
        Player p=new Player(TestSupport.config(1));assertEquals(100,p.width());assertEquals(100,p.height());
        assertEquals(88,p.hitbox().getWidth());assertEquals(86,p.hitbox().getHeight());
        assertEquals(p.spriteBounds().getCenterX(),p.hitbox().getCenterX());
    }
    @Test void freezeStopsAndDoesNotReleaseStoredVelocity() {
        Player p=new Player(TestSupport.config(1));EffectManager e=new EffectManager();ticks(p,e,1,4);
        e.add(Type.FREEZE,.6);double x=p.x();ticks(p,e,1,20);assertEquals(x,p.x());assertEquals(0,p.velocity());
        ticks(p,e,0,30);assertEquals(x,p.x());
    }
    @Test void slowAndIceHaveControlledEffect() {
        Player normal=new Player(TestSupport.config(1)),slow=new Player(TestSupport.config(1));
        EffectManager none=new EffectManager(),drain=new EffectManager();drain.add(Type.DRAIN,10);
        ticks(normal,none,1,12);ticks(slow,drain,1,12);assertTrue(slow.velocity()<normal.velocity());
        EffectManager ice=new EffectManager();ice.add(Type.SLIDE,10);
        double before=normal.velocity();ticks(normal,ice,0,1);assertTrue(normal.velocity()>before-2000.0/60);
    }
    @Test void helicopterStaysInsideTheSameCentralPlayAreaAsRoadLevels() {
        Player p=new Helicopter(TestSupport.config(52));EffectManager e=new EffectManager();ticks(p,e,-1,300);
        assertEquals(140,p.x());assertEquals(0,p.velocity());
        ticks(p,e,1,400);assertEquals(860,p.x()+p.width());assertEquals(0,p.velocity());
        assertEquals(TestSupport.config(1).values().number("player.y"),p.y());
    }
}
