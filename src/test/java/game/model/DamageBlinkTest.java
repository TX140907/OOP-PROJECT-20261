package game.model;

import game.TestSupport;
import game.engine.*;
import game.level.*;
import game.logic.collision.*;
import game.rendering.*;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DamageBlinkTest {
    @Test void everyDamagingCollisionStartsBlinkAndRendererAlternates() {
        for(CollisionKind kind:new CollisionKind[]{CollisionKind.NORMAL_DAMAGE,CollisionKind.SLOW,CollisionKind.LETHAL}) {
            BaseLevel w=TestSupport.level(2);new ConfiguredCollision(kind).apply(w);
            assertTrue(w.player().blinking());assertTrue(w.player().health()<3);
            var renderer=new EntityRenderer(new AssetLoader());
            var image=new BufferedImage(1000,700,BufferedImage.TYPE_INT_ARGB);var g=image.createGraphics();renderer.player(g,w);g.dispose();
            int x=(int)w.player().spriteBounds().getCenterX(),y=(int)w.player().spriteBounds().getCenterY();
            assertNotEquals(0,image.getRGB(x,y));
            w.player().updateVisuals(w.config().values().number("player.hitBlinkIntervalSeconds")*1.2);
            image=new BufferedImage(1000,700,BufferedImage.TYPE_INT_ARGB);g=image.createGraphics();renderer.player(g,w);g.dispose();
            assertEquals(0,image.getRGB(x,y));
            w.player().updateVisuals(2);assertFalse(w.player().blinking());assertTrue(w.player().visible());
        }
    }
    @Test void pauseFreezesBlinkAndDeathContinuesOnlyVisualTime() {
        Game game=new Game(new LevelManager(2),false);game.level().player().hurt(1);
        game.togglePause();game.update(.15);assertTrue(game.level().player().visible());
        game.togglePause();game.level().player().kill();game.update(0);assertEquals(GameState.GAME_OVER,game.state());
        double progress=game.level().progress().fraction();game.update(.15);
        assertFalse(game.level().player().visible());assertEquals(progress,game.level().progress().fraction());
        game.restart();assertFalse(game.level().player().blinking());
    }
}
