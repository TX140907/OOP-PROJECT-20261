package game.logic;

import game.TestSupport;
import game.level.*;
import game.logic.collision.CollisionKind;
import game.model.obstacle.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BossShieldTest {
    @Test void pickupGrantsShieldAndNormalAndLethalContactsPassThroughWithoutConsumingObstacle() {
        BaseLevel world=TestSupport.level(52);var p=world.player();
        var spec=new DayConfig.ObstacleSpec(ObstacleType.SHIELD_PICKUP,0,0,44,44,0,0,CollisionKind.SHIELD,true,"");
        var item=new ShieldPickup(spec,p.hitbox().getX(),p.hitbox().getY(),0);world.addObstacle(item);world.update(0,0);
        assertTrue(item.removed());assertTrue(p.shielded());assertFalse(item.harmfulAt(0));
        var plane=TestSupport.object(ObstacleType.VEHICLE,CollisionKind.LETHAL,p.x(),p.y(),240,140,0,0);
        var drain=TestSupport.object(ObstacleType.DRAIN,CollisionKind.SLOW,p.x(),p.y(),170,100,0,0);
        world.addObstacle(plane);world.addObstacle(drain);world.update(.1,0);
        assertEquals(5,p.health());assertFalse(p.dead());assertEquals(.45,p.opacity(),1e-6);assertFalse(p.blinking());
        assertFalse(plane.removed());assertFalse(world.effects().active(game.logic.effect.EffectManager.Type.DRAIN));
        for(int i=0;i<world.config().values().number("boss.shield.durationSeconds")*60 && !p.dead();i++)world.update(1.0/60,0);
        assertTrue(p.dead(),"Remaining inside a plane after shield expiration must be lethal");
    }
    @Test void normalDamageIsNotSuppressedByAnOldContactAfterShieldExpires() {
        BaseLevel world=TestSupport.level(52);var p=world.player();p.grantShield(.1);
        world.addObstacle(TestSupport.object(ObstacleType.BARRIER,CollisionKind.NORMAL_DAMAGE,p.x(),p.y(),100,100,0,0));
        world.update(.05,0);assertEquals(5,p.health());assertTrue(p.opacity()<1);
        world.update(.06,0);assertEquals(4,p.health());assertTrue(p.blinking());
    }
    @Test void rowStrikeWarnsThenDamagesOnlyItsFixedTargetAndDisappears() {
        BaseLevel world=TestSupport.level(52);var p=world.player();
        var spec=new DayConfig.ObstacleSpec(ObstacleType.BOSS_PROJECTILE,0,0,24,24,0,0,CollisionKind.NORMAL_DAMAGE,true,"");
        var projectile=new BossProjectile(spec,p.hitbox().getCenterX()-12,p.hitbox().getCenterY()-12,
                p.hitbox().getCenterY(),0,.65,.6,true);world.addObstacle(projectile);
        world.update(.64,0);assertEquals(5,p.health());assertTrue(projectile.warning());
        world.update(.02,0);assertEquals(4,p.health());assertTrue(projectile.removed());
        var missed=new BossProjectile(spec,200,530,542,0,.65,.6,true);world.addObstacle(missed);
        world.update(1.3,0);assertTrue(missed.removed());
    }
}
