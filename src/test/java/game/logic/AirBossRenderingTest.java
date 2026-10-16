package game.logic;

import game.TestSupport;
import game.engine.*;
import game.level.*;
import game.logic.boss.AirBossEncounter.State;
import game.logic.collision.CollisionKind;
import game.model.obstacle.*;
import game.rendering.*;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AirBossRenderingTest {
    private Game game(String... options) {
        var settings=new ArrayList<>(List.of("boss.enabled","true","level.durationSeconds","0.05",
                "boss.clearDelaySeconds","0.05","boss.warningSeconds","0.1","boss.entranceSeconds","0.1",
                "healthPickup.enabled","false","boss.shield.count","0","boss.reverse.shotChance","0","boss.reverse.sequenceWeight","0","boss.waves.openingVolleyEnabled","false"));
        settings.addAll(List.of(options));
        return new Game(new LevelManager(List.<Supplier<BaseLevel>>of(()->TestSupport.level(52,settings.toArray(String[]::new))),0),false);
    }
    private void advanceTo(Game game,State target) {
        for(int i=0;i<600 && game.level().bossEncounter().state()!=target;i++)game.update(1.0/60);
        assertEquals(target,game.level().bossEncounter().state());
    }
    private BufferedImage render(Game game,String name) throws Exception {
        var image=new BufferedImage(1000,700,BufferedImage.TYPE_INT_ARGB);
        var g=image.createGraphics();new GameRenderer().draw(g,game);g.dispose();
        Files.createDirectories(Path.of("target/smoke"));ImageIO.write(image,"png",Path.of("target/smoke/"+name+".png").toFile());
        return image;
    }
    @Test void healthBarLoadsBeforeEHintAndCooldownReactsToActualShot() throws Exception {
        Game game=game("boss.action1Chance","0","boss.openingDelaySeconds","250");advanceTo(game,State.LOADING);
        game.update(.7);var loading=render(game,"boss-loading");
        var c=game.level().config().values();int barX=c.integer("boss.hud.x"),barY=c.integer("boss.hud.y");
        assertEquals(c.color("boss.healthBarColor").getRGB(),loading.getRGB(barX+10,barY+5));
        assertNotEquals(c.color("boss.healthBarColor").getRGB(),loading.getRGB(barX+c.integer("boss.hud.width")-10,barY+5));
        advanceTo(game,State.ACTIVE);var ready=render(game,"boss-ready");
        var p=game.level().player();int hintX=(int)(p.x()+p.width())+c.integer("boss.hintOffsetX");
        int hintY=(int)(p.y()+p.height()/2)-c.integer("boss.hintSize")/2;
        assertEquals(Color.YELLOW.getRGB(),ready.getRGB(hintX+c.integer("boss.hintSize")/2,hintY+4));
        int coolX=c.integer("boss.cooldown.x")+c.integer("boss.cooldown.width")-4,coolY=c.integer("boss.cooldown.y")+5;
        assertEquals(Color.GREEN.getRGB(),ready.getRGB(coolX,coolY));
        game.input().press(KeyEvent.VK_E);game.update(.01);var fired=render(game,"boss-fired");
        assertNotEquals(Color.GREEN.getRGB(),fired.getRGB(coolX,coolY));assertTrue(game.level().bossEncounter().hasFired());
    }
    @Test void waveBossStandsInMarginAndShieldContactMakesPlayerTranslucent() throws Exception {
        Game game=game("boss.action1Chance","100","boss.startAction","1","boss.openingDelaySeconds","0.05","boss.waves.throwChance","100",
                "boss.waves.twoPlaneChance","100");
        advanceTo(game,State.ACTIVE);var world=game.level();world.player().grantShield(100);
        for(int i=0;i<120 && world.obstacles().stream().noneMatch(o->o instanceof EnemyPlane);i++)game.update(1.0/60);
        assertEquals(2,world.obstacles().stream().filter(o->o instanceof EnemyPlane).count());game.update(1);render(game,"boss-wave");
        var p=world.player();world.addObstacle(TestSupport.object(ObstacleType.BARRIER,CollisionKind.NORMAL_DAMAGE,p.x(),p.y(),120,100,0,0));
        game.update(0);render(game,"boss-shield");
        var layer=new BufferedImage(1000,700,BufferedImage.TYPE_INT_ARGB);var g=layer.createGraphics();
        new EntityRenderer(new AssetLoader()).player(g,world);g.dispose();
        int alpha=layer.getRGB((int)(p.x()+p.width()/2),(int)(p.y()+p.height()/2))>>>24;
        assertEquals(255*world.config().values().number("boss.shield.contactAlpha"),alpha,1);
    }
    @Test void reverseWaveWarnsAtBottomThenRendersPlanePointingUp() throws Exception {
        Game game=game("boss.action1Chance","100","boss.startAction","1","boss.waves.durationSeconds","0.1","boss.lane.durationSeconds","0.2",
                "boss.openingDelaySeconds","250","boss.moveSeconds","0.01");
        advanceTo(game,State.ACTIVE);
        for(int i=0;i<120 && game.level().obstacles().stream().noneMatch(o->o instanceof EnemyPlane);i++)game.update(1.0/60);
        var plane=(EnemyPlane)game.level().obstacles().stream().filter(o->o instanceof EnemyPlane).findFirst().orElseThrow();
        assertTrue(plane.warning());var warning=render(game,"boss-reverse-warning");var c=game.level().config().values();
        int left=(int)(plane.x()+(plane.width()-c.integer("boss.reverse.warningSize"))/2);
        assertEquals(Color.ORANGE.getRGB(),warning.getRGB(left+4,c.integer("boss.reverse.warningY")+c.integer("boss.reverse.warningSize")/2));
        plane.update(1.5,game.level());assertFalse(plane.warning());var flying=render(game,"boss-reverse-flight");
        assertEquals(new Color(175,70,90).getRGB(),flying.getRGB((int)(plane.x()+plane.width()/2),(int)plane.y()+10));
    }
    @Test void sequentialReverseWarningsAppearLeftMiddleRightWithIndependentBlinkClocks() throws Exception {
        Game game=game("boss.action1Chance","100","boss.startAction","1","boss.waves.durationSeconds","0.1","boss.lane.durationSeconds","0.2",
                "boss.openingDelaySeconds","250","boss.moveSeconds","0.01","boss.reverse.singleWeight","0",
                "boss.reverse.pairWeight","0","boss.reverse.sequenceWeight","100","boss.reverse.blinkSeconds","0.3",
                "sprite.reversePlaneWarning","/missing-sequence-warning.png");
        advanceTo(game,State.ACTIVE);
        for(int i=0;i<200 && game.level().obstacles().isEmpty();i++)game.update(.01);
        var planes=game.level().obstacles().stream().filter(o->o instanceof EnemyPlane).map(o->(EnemyPlane)o)
                .sorted(Comparator.comparingDouble(Obstacle::x)).toList();assertEquals(3,planes.size());
        var c=game.level().config().values();int size=c.integer("boss.reverse.warningSize"),y=c.integer("boss.reverse.warningY")+size/2;
        for(int step=0;step<3;step++) {
            if(step>0)game.update(.5);
            var frame=render(game,"boss-reverse-sequence-"+(step+1));
            for(int lane=step;lane<3;lane++) {
                int x=(int)(planes.get(lane).x()+(planes.get(lane).width()-size)/2)+4;
                if(lane==step)assertEquals(Color.ORANGE.getRGB(),frame.getRGB(x,y));
                else assertNotEquals(Color.ORANGE.getRGB(),frame.getRGB(x,y));
            }
        }
    }
    @Test void laneAndMissileWarningsStayAbovePlayerAndTheirTargetRemainsVisibleBetweenBlinks() throws Exception {
        for(boolean missile:List.of(false,true)) {
            Game game=game("boss.action1Chance","0","boss.openingDelaySeconds","250",
                    "sprite.bossProjectileWarning","/missing-warning.png");
            advanceTo(game,State.ACTIVE);var world=game.level();var p=world.player();var c=world.config().values();
            var before=render(game,"boss-lane-before");
            var spec=new DayConfig.ObstacleSpec(missile?ObstacleType.MISSILE:ObstacleType.BOSS_PROJECTILE,
                    0,0,50,50,0,0,CollisionKind.BOSS_DAMAGE,true,"");
            double x=p.hitbox().getCenterX()-spec.width()/2,y=p.hitbox().getCenterY();
            Obstacle shot=missile?new Missile(spec,null,x,200,y,500,.65):new BossProjectile(spec,x,200,y,500,.65,0,false);
            world.addObstacle(shot);
            int size=c.integer("boss.lane.warningSize"),left=(int)(p.hitbox().getCenterX()-size/2);
            int top=(int)(y-p.height()/2-size-c.integer("boss.lane.warningOffsetY"));
            assertTrue(top+size<p.y());
            int sampleX=left+12,sampleY=top+size-10;
            String name=missile?"boss-missile-warning":"boss-lane-warning";
            var visible=render(game,name);int color=c.color("boss.lane.warningColor").getRGB();
            assertEquals(color,visible.getRGB(sampleX,sampleY));
            int markerSize=c.integer(missile?"missile.warningSize":"boss.projectile.warningSize");
            int markerX=(int)(p.hitbox().getCenterX()-markerSize/2.0);
            assertEquals(color,visible.getRGB(markerX,(int)y)); // Drawn on top of the player's body.
            shot.update(c.number("boss.projectile.blinkSeconds")*1.5,world);
            var off=render(game,name+"-off");
            assertEquals(before.getRGB(sampleX,sampleY),off.getRGB(sampleX,sampleY));
            assertEquals(color,off.getRGB(markerX,(int)y));assertFalse(shot.harmfulAt(0));
        }
    }
    @Test void firstActionShowsAFaintAimLineFromHandThenRendersARealMovingBullet() throws Exception {
        Game game=game("boss.action1Chance","100","boss.startAction","1","boss.openingDelaySeconds","250");advanceTo(game,State.ACTIVE);
        game.update(.6);var world=game.level();var boss=world.bossEncounter().boss();var c=world.config().values();
        double sourceX=boss.x()<500?boss.x()+boss.width()-c.number("boss.waves.handInsetX"):boss.x()+c.number("boss.waves.handInsetX");
        double sourceY=boss.y()+boss.height()*c.number("boss.waves.handHeightRatio");
        var spec=new DayConfig.ObstacleSpec(ObstacleType.BOSS_PROJECTILE,0,0,50,50,0,0,CollisionKind.BOSS_DAMAGE,true,"");
        var shot=BossProjectile.aimed(spec,sourceX,sourceY,500,world.player().hitbox().getCenterY(),800,.8,.3);
        world.addObstacle(shot);render(game,"boss-aim-warning");
        var layer=new BufferedImage(1000,700,BufferedImage.TYPE_INT_ARGB);var g=layer.createGraphics();
        new EntityRenderer(new AssetLoader()).foregroundWarnings(g,world);g.dispose();
        int midX=(int)Math.round((sourceX+500)/2),midY=(int)Math.round((sourceY+world.player().hitbox().getCenterY())/2);
        int alpha=layer.getRGB(midX,midY)>>>24;
        assertTrue(alpha>0 && alpha<=Math.ceil(255*c.number("boss.waves.aimLineAlpha")));
        shot.update(1,world);assertFalse(shot.warning());
        var flying=render(game,"boss-aim-flight");
        assertEquals(c.color("boss.projectile.color").getRGB(),flying.getRGB((int)shot.hitbox().getCenterX(),(int)shot.hitbox().getCenterY()));
        assertEquals(500,shot.targetArea(48).getCenterX());
    }
}
