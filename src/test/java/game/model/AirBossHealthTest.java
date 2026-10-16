package game.model;

import game.TestSupport;
import game.model.boss.AirBoss;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AirBossHealthTest {
    @Test void movingBossHasIntermediatePositionsAndDoesNotWaitWhenAlreadyAtDestination() {
        var boss=new AirBoss(TestSupport.config(52).values());double startX=boss.x(),startY=boss.y();
        boss.moveTo(24,130,.5);assertEquals(startX,boss.x());assertEquals(startY,boss.y());assertTrue(boss.moving());
        boss.update(.25);assertEquals((startX+24)/2,boss.x(),1e-8);assertEquals((startY+130)/2,boss.y(),1e-8);
        boss.update(.25);assertEquals(24,boss.x());assertEquals(130,boss.y());assertFalse(boss.moving());
        boss.moveTo(24,130,.5);assertFalse(boss.moving());
        assertThrows(IllegalArgumentException.class,()->boss.moveTo(400,130,0));
    }
    @Test void configuredHitCountRemovesOneNthOfBarAndExactlyNthHitDefeatsBoss() {
        for(int count:new int[]{1,3,7,8,13,100}) {
            var boss=new AirBoss(TestSupport.config(52,"boss.hitsToDefeat",Integer.toString(count)).values());
            assertEquals(1,boss.healthFraction());assertFalse(boss.defeated());
            for(int hit=1;hit<=count;hit++) {
                boss.takeHit();assertEquals(1-hit/(double)count,boss.healthFraction(),1e-12);
                assertEquals(hit==count,boss.defeated());
            }
            boss.takeHit();assertEquals(0,boss.healthFraction());
        }
    }
}
