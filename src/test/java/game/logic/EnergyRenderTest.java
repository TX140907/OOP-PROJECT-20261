package game.logic;
import game.TestSupport;
import game.engine.*;
import game.level.*;
import game.model.obstacle.*;
import game.logic.effect.EnergySystem;
import game.rendering.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class EnergyRenderTest {
    @Test void energyDrainsRecoversAndHasSeparateLifeCycle() {
        EnergySystem e=new EnergySystem(TestSupport.config(4).values());e.update(10);assertEquals(80,e.value());e.recover(99);assertEquals(100,e.value());e.update(100);assertTrue(e.empty());
        EnergySystem off=new EnergySystem(TestSupport.config(1).values());off.update(10000);assertFalse(off.empty());
    }
    @Test void urgentPickupIsActuallyReachableAndCollectible() {
        BaseLevel w=TestSupport.level(4,"energy.initial","10");w.update(.01,0);
        assertTrue(w.obstacles().stream().anyMatch(o->o.spec().type()==ObstacleType.ENERGY_PICKUP));
        for(int i=0;i<100;i++)w.update(1.0/60,0);
        assertFalse(w.player().dead());assertTrue(w.energy().value()>25,"Pickup was not collected");
    }
    @Test void noRngStarvationWhenPlayerKeepsTakingReachableSupplies() {
        BaseLevel w=TestSupport.level(4,"energy.initial","30");
        // Fatigue slows progress too: completion uses effective travel time, not 60 wall-clock seconds.
        for(int i=0;i<5400 && !w.complete() && !w.player().dead();i++)w.update(1.0/60,0);
        assertFalse(w.player().dead());assertTrue(w.complete());
    }
    @Test void energyZeroIsGameOverAndPauseDoesNotDrain() {
        Game g=new Game(new LevelManager(List.<Supplier<BaseLevel>>of(()->TestSupport.level(4,"energy.initial","0.01")),0),false);
        g.togglePause();g.update(100);assertEquals(.01,g.level().energy().value());g.togglePause();g.update(.1);assertEquals(GameState.GAME_OVER,g.state());
    }
    @Test void everyLevelRendersWithoutAssets() {
        AssetLoader assets=new AssetLoader();assertNull(assets.image("/image/nonexistent.png"));assertNull(assets.image("/image/nonexistent.png"));
        for(int id:Levels.ORDER) {
            Game game=new Game(new LevelManager(id),false);BufferedImage image=new BufferedImage(1000,700,BufferedImage.TYPE_INT_RGB);
            var g=image.createGraphics();assertDoesNotThrow(()->new GameRenderer().draw(g,game));g.dispose();
            assertNotEquals(0,image.getRGB(500,500));
        }
    }
    @Test void glareNeverHidesLethalBusCompletely() {
        BaseLevel w=TestSupport.level(4);var spec=w.config().obstacles().get(0);
        Bus bus=new Bus(spec,200,-200,170);GlareEffect glare=new GlareEffect();
        assertTrue(glare.alpha(bus,w.config().values())>=.39);bus.update(4,w);assertEquals(1,glare.alpha(bus,w.config().values()));
    }
}
