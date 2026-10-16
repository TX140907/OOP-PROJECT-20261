package game.model.obstacle;

import game.TestSupport;
import game.level.BaseLevel;
import game.rendering.AssetLoader;
import game.rendering.EntityRenderer;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EnemyPlaneCollisionTest {
    private EnemyPlane plane(BaseLevel world,double x,double y) {
        var spec=world.config().obstacles().stream().filter(s->s.type()==ObstacleType.ENEMY_PLANE).findFirst().orElseThrow();
        EnemyPlane plane=new EnemyPlane(spec,x,y,0,world.config().values());
        world.addObstacle(plane);
        return plane;
    }

    @Test void emptyLowerCornersDoNotKillPlayerDespiteOverlappingBoundingBoxes() {
        for(boolean left:new boolean[]{true,false}) {
            BaseLevel world=TestSupport.level(52);
            Rectangle2D player=world.player().hitbox();
            EnemyPlane plane=plane(world,left?player.getMinX()-230:player.getMaxX()-10,player.getMinY()-100);
            assertEquals(240,plane.width());
            assertTrue(plane.hitbox().intersects(player));
            assertFalse(plane.collisionShape().intersects(player));
            world.update(1.0/60,0);
            assertFalse(world.player().dead());
            assertEquals(5,world.player().health());
        }
    }

    @Test void emptyUpperCornersDoNotKillPlayer() {
        BaseLevel world=TestSupport.level(52);
        Rectangle2D player=world.player().hitbox();
        EnemyPlane plane=plane(world,player.getMinX()-230,player.getMaxY()-10);
        assertTrue(plane.hitbox().intersects(player));
        assertFalse(plane.collisionShape().intersects(player));
        world.update(1.0/60,0);
        assertFalse(world.player().dead());
    }

    @Test void visibleWingDealsTwoDamageAndBlinksWithoutDamagingEveryFrame() {
        BaseLevel world=TestSupport.level(52);
        Rectangle2D player=world.player().hitbox();
        EnemyPlane plane=plane(world,player.getMinX()-230,player.getMinY()-140.0/3+1);
        assertTrue(plane.collisionShape().intersects(player));
        world.update(1.0/60,0);
        assertEquals(3,world.player().health());assertFalse(world.player().dead());assertTrue(world.player().blinking());
        for(int i=0;i<100;i++)world.update(1.0/60,0);
        assertEquals(3,world.player().health());assertFalse(plane.removed());
    }

    @Test void visibleNoseDealsTwoDamage() {
        BaseLevel world=TestSupport.level(52);
        Rectangle2D player=world.player().hitbox();
        EnemyPlane plane=plane(world,player.getCenterX()-120,player.getMinY()-140+8);
        assertTrue(plane.collisionShape().intersects(player));
        world.update(1.0/60,0);
        assertEquals(3,world.player().health());assertTrue(world.player().blinking());
    }

    @Test void armedUnarmedAndReversePlanesRespectConfiguredDamageAndShield() {
        for(int mode=0;mode<3;mode++) {
            BaseLevel world=TestSupport.level(52,"enemyPlane.damage","3");var p=world.player();
            var spec=world.config().obstacles().stream().filter(s->s.type()==ObstacleType.ENEMY_PLANE).findFirst().orElseThrow();
            EnemyPlane plane=mode==2?EnemyPlane.reverse(spec,p.hitbox().getCenterX()-120,world.config().values(),new Object()):
                    new EnemyPlane(spec,p.hitbox().getCenterX()-120,p.hitbox().getMinY()-140+8,0,world.config().values(),mode==0,null);
            if(mode==2)plane.update(1.33,world);
            world.addObstacle(plane);assertTrue(plane.collisionShape().intersects(p.hitbox()));
            p.grantShield(.05);world.update(0,0);assertEquals(5,p.health());assertTrue(p.opacity()<1);
            // Keep the reverse plane at the contact point while advancing the player's shield clock.
            p.update(.06,0,world.effects());world.update(0,0);
            assertEquals(2,p.health());assertTrue(p.blinking());assertFalse(plane.removed());
        }
    }
    @Test void planeDamageWinsOverLightContactInTheSameFrameAndIsFatalAtTwoHealth() {
        assertEquals(2,TestSupport.level(52).config().values().integer("enemyPlane.damage"));
        for(boolean planeFirst:new boolean[]{true,false}) {
            BaseLevel world=TestSupport.level(52);var p=world.player();
            if(!planeFirst)world.addObstacle(game.TestSupport.object(ObstacleType.BIRD,game.logic.collision.CollisionKind.NORMAL_DAMAGE,p.x(),p.y(),100,100,0,0));
            plane(world,p.hitbox().getCenterX()-120,p.hitbox().getMinY()-140+8);
            if(planeFirst)world.addObstacle(game.TestSupport.object(ObstacleType.BIRD,game.logic.collision.CollisionKind.NORMAL_DAMAGE,p.x(),p.y(),100,100,0,0));
            world.update(0,0);assertEquals(3,p.health());
        }
        BaseLevel low=TestSupport.level(52,"level.initialHealth","2");
        plane(low,low.player().hitbox().getCenterX()-120,low.player().hitbox().getMinY()-140+8);
        low.update(0,0);assertTrue(low.player().dead());assertEquals(0,low.player().health());
    }

    @Test void renderedPlaneUsesTheCollisionOutlineIncludingFractionalPosition() {
        BaseLevel world=TestSupport.level(52);
        EnemyPlane plane=plane(world,200.5,200.5);
        BufferedImage image=new BufferedImage(1000,700,BufferedImage.TYPE_INT_ARGB);
        var g=image.createGraphics();new EntityRenderer(new AssetLoader()).obstacle(g,plane,world);g.dispose();
        int[][] samples={{205,330},{435,330},{205,205},{435,205},{320,210},{210,248},{430,248},{320,330}};
        for(int[] point:samples) {
            boolean painted=(image.getRGB(point[0],point[1])>>>24)!=0;
            assertEquals(plane.collisionShape().contains(point[0]+.5,point[1]+.5),painted);
        }
    }
}
