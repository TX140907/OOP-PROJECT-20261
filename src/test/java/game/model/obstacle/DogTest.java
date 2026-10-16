package game.model.obstacle;

import game.TestSupport;
import game.config.GameConfig;
import game.level.*;
import game.logic.spawn.ObstacleFactory;
import game.rendering.*;
import java.awt.image.BufferedImage;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DogTest {
    private Dog create(BaseLevel w,int seed) {
        var s=w.config().obstacles().stream().filter(o->o.type()==ObstacleType.DOG).findFirst().orElseThrow();
        Dog dog=(Dog)new ObstacleFactory().create(s,w.config(),new Random(seed));w.addObstacle(dog);return dog;
    }
    @Test void crossesBarksChargesThenStandsUntilScrollingOffScreen() {
        BaseLevel w=TestSupport.level(51);Dog d=create(w,42);
        assertEquals(Dog.State.CROSSING,d.state());
        d.update(d.crossSeconds(),w);assertEquals(Dog.State.BARKING,d.state());
        double stopX=d.x(),target=d.targetX(),bark=w.config().values().number("dog.barkDurationSeconds");
        d.update(bark-.01,w);assertEquals(Dog.State.BARKING,d.state());assertEquals(stopX,d.x(),1e-8);
        d.update(.01,w);assertEquals(Dog.State.CHARGING,d.state());
        var expected=d.predictedHitbox(w.config().values().number("dog.chargeDurationSeconds"));
        w.player().update(.1,1,w.effects());assertEquals(target,d.targetX());
        d.update(w.config().values().number("dog.chargeDurationSeconds"),w);
        assertEquals(Dog.State.STANDING,d.state());assertEquals(target,d.x(),1e-8);
        assertEquals(expected.getY(),d.y(),1e-8);
        assertEquals(w.player().hitbox().getCenterY(),d.hitbox().getCenterY(),1e-8);
        double y=d.y();d.update(.5,w);assertFalse(d.removed());assertTrue(d.activeAt(1));
        assertEquals(target,d.x(),1e-8);assertEquals(y+.5*w.config().values().number("level.scrollSpeed"),d.y(),1e-8);
        d.update(3,w);assertTrue(d.removed());
    }
    @Test void chargesRemainInsideTheirChosenLaneAndTargetsAreSeeded() {
        Set<Integer> lanes=new HashSet<>();Set<Long> offsets=new HashSet<>();
        for(int seed=0;seed<60;seed++) {
            BaseLevel w=TestSupport.level(51);Dog d=create(w,seed);lanes.add(d.lane());
            double laneLeft=GameConfig.ROAD_LEFT+d.lane()*GameConfig.LANE_WIDTH;
            assertTrue(d.targetX()>=laneLeft);assertTrue(d.targetX()+d.width()<=laneLeft+GameConfig.LANE_WIDTH);
            offsets.add(Math.round(d.targetX()-GameConfig.laneX(d.lane(),d.width())));
            d.update(d.crossSeconds(),w);
            for(int i=0;i<70 && !d.removed();i++) {
                d.update(1.0/60,w);
                assertTrue(d.x()>=laneLeft-1e-8);assertTrue(d.x()+d.width()<=laneLeft+GameConfig.LANE_WIDTH+1e-8);
            }
            Dog same=create(TestSupport.level(51),seed);assertEquals(same.targetX(),d.targetX());
        }
        assertEquals(3,lanes.size());assertTrue(offsets.size()>20);
    }
    @Test void spawnerForecastMatchesActualDogAndAllowsDodging() {
        BaseLevel w=TestSupport.level(51);Dog d=create(w,7);
        assertTrue(w.safety().accepts(w,d));
        var expected=d.predictedHitbox(2);
        for(int i=0;i<120;i++)d.update(1.0/60,w);
        assertEquals(expected.getX(),d.x(),1e-8);assertEquals(expected.getY(),d.y(),1e-8);
    }
    @Test void barkingShowsBlinkingExclamationEvenWithoutSprites() {
        BaseLevel w=TestSupport.level(51);Dog d=create(w,42);d.update(d.crossSeconds(),w);
        var renderer=new EntityRenderer(new AssetLoader());var c=w.config().values();
        int x=(int)d.hitbox().getCenterX(),y=(int)d.y()-c.integer("dog.warningOffsetY")-c.integer("dog.warningSize")/2;
        var image=new BufferedImage(1000,700,BufferedImage.TYPE_INT_ARGB);var g=image.createGraphics();renderer.obstacle(g,d,w);g.dispose();
        assertNotEquals(0,image.getRGB(x,y));
        d.update(c.number("dog.warningBlinkSeconds")*1.2,w);
        image=new BufferedImage(1000,700,BufferedImage.TYPE_INT_ARGB);g=image.createGraphics();renderer.obstacle(g,d,w);g.dispose();
        y=(int)d.y()-c.integer("dog.warningOffsetY")-c.integer("dog.warningSize")/2;
        assertEquals(0,image.getRGB(x,y));
    }
    @Test void actualChargeHitsStationaryPlayerOnceAndStartsBlink() {
        BaseLevel w=TestSupport.level(51);var spec=w.config().obstacles().stream().filter(o->o.type()==ObstacleType.DOG).findFirst().orElseThrow();
        Dog dog=new Dog(spec,-spec.width()-20,w.player().y()-w.config().values().number("dog.leadY"),800,1,0,w.config().values());
        w.addObstacle(dog);boolean sawWarning=false;
        for(int i=0;i<240 && w.player().health()==3;i++) {
            w.update(1.0/60,0);sawWarning|=dog.state()==Dog.State.BARKING;
        }
        assertTrue(sawWarning);assertEquals(2,w.player().health());assertTrue(w.player().blinking());assertFalse(dog.removed());
        for(int i=0;i<12;i++)w.update(1.0/60,0);
        assertEquals(2,w.player().health());assertFalse(dog.removed());
    }
}
