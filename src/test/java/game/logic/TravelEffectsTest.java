package game.logic;

import game.TestSupport;
import game.level.BaseLevel;
import game.logic.collision.*;
import game.logic.effect.EffectManager;
import game.logic.effect.EffectManager.Type;
import game.logic.movement.HorizontalMotion;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TravelEffectsTest {
    private static void advance(BaseLevel level,int ticks,int direction) {
        for(int i=0;i<ticks;i++) level.update(1.0/60,direction);
    }
    @Test void drainDamagesAndReducesMovementWorldTravelAndProgressThenExpires() {
        BaseLevel slow=TestSupport.level(2),normal=TestSupport.level(2);
        new ConfiguredCollision(CollisionKind.SLOW).apply(slow);
        advance(slow,30,1);advance(normal,30,1);
        assertEquals(normal.player().health()-1,slow.player().health());
        assertEquals(.65,slow.player().velocity()/normal.player().velocity(),1e-8);
        assertEquals(.65,slow.scroll()/normal.scroll(),1e-8);
        assertEquals(.65*.8,slow.progress().fraction()/normal.progress().fraction(),1e-8);
        advance(slow,60,0);
        assertFalse(slow.effects().active(Type.DRAIN));
        assertEquals(1,slow.effects().progressMultiplier(slow.config().values()));
    }
    @Test void snowballStopsTravelThenLeavesConfiguredSlowAndFinallyRecovers() {
        BaseLevel level=TestSupport.level(3);
        new ConfiguredCollision(CollisionKind.FREEZE).apply(level);
        double x=level.player().x();advance(level,24,1);
        assertEquals(x,level.player().x());assertEquals(0,level.scroll());assertEquals(0,level.progress().fraction());
        advance(level,24,1);
        assertFalse(level.effects().active(Type.FREEZE));assertTrue(level.effects().active(Type.SNOWBALL_SLOW));
        assertTrue(level.player().x()>x);assertTrue(level.progress().fraction()>0);
        assertEquals(.6,level.effects().movementMultiplier(level.config().values(),0));
        assertEquals(.5,level.effects().progressMultiplier(level.config().values()));
        advance(level,90,0);
        assertEquals(1,level.effects().movementMultiplier(level.config().values(),0));
        assertEquals(1,level.effects().progressMultiplier(level.config().values()));
    }
    @Test void energyFatigueIsLinearAndPickupOverridesItForExactlyConfiguredDuration() {
        var c=TestSupport.config(4).values();EffectManager effects=new EffectManager();
        effects.setEnergyFraction(1);assertEquals(1,effects.energyMultiplier(c,0));
        effects.setEnergyFraction(.5);assertEquals(.9,effects.energyMultiplier(c,0),1e-9);
        effects.setEnergyFraction(0);assertEquals(.8,effects.energyMultiplier(c,0),1e-9);
        effects.add(Type.ENERGY_BOOST,2);
        assertEquals(1.4,effects.movementMultiplier(c,0));assertEquals(1.4,effects.progressMultiplier(c));
        assertEquals(1.4,effects.progressMultiplier(c,1.999));assertEquals(.8,effects.progressMultiplier(c,2),1e-9);
        effects.update(2);assertEquals(.8,effects.progressMultiplier(c),1e-9);
    }
    @Test void energyBoostChangesActualMovementAndProgressAndExpires() {
        BaseLevel level=TestSupport.level(4,"energy.initial","50","energy.drainPerSecond","0.001");
        new ConfiguredCollision(CollisionKind.ENERGY).apply(level);
        assertEquals(80,level.energy().value());
        level.update(.1,1);
        assertEquals(1.4,level.effects().movementMultiplier(level.config().values(),0));
        assertEquals(.14/60,level.progress().fraction(),1e-9);
        assertEquals(level.config().values().number("level.scrollSpeed")*.14,level.scroll(),1e-8);
        advance(level,120,0);
        assertFalse(level.effects().active(Type.ENERGY_BOOST));
        assertEquals(1-.2*(1-level.energy().fraction()),level.effects().movementMultiplier(level.config().values(),0),1e-9);
    }
    @Test void futureWorldTravelAccountsForBoostEndingAndFreezeEnding() {
        BaseLevel level=TestSupport.level(4,"energy.initial","50","energy.drainPerSecond","0.001");
        level.effects().add(Type.ENERGY_BOOST,1);
        assertEquals(1.4+.9,level.travelSecondsAhead(2),1e-5);
        level.effects().add(Type.FREEZE,.5);
        assertEquals(.5*1.4+.9,level.travelSecondsAhead(2),1e-5);
        var move=HorizontalMotion.step(new HorizontalMotion.State(400,0),1,.1,level.config().values(),level.effects(),0);
        assertEquals(400,move.x());assertEquals(0,move.velocity());
    }
}
