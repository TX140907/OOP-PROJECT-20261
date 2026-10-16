package game.logic;
import game.logic.progress.ProgressManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ProgressTest {
    @Test void thirtyAndSixtyEffectiveSeconds() {
        ProgressManager p=new ProgressManager(60);for(int i=0;i<1800;i++)p.update(1.0/60,1,1,1);
        assertEquals(.5,p.fraction(),1e-9);for(int i=0;i<1801;i++)p.update(1.0/60,1,1,1);assertEquals(1,p.fraction());
    }
    @Test void hitPenaltyExpiresAndProgressNeverGoesBack() {
        ProgressManager p=new ProgressManager(60);p.update(30,1,1,1);p.hit(1);p.update(2,1,.8,1);
        assertEquals(31.8/60,p.fraction(),1e-10);p.reset();assertEquals(0,p.fraction());
    }
    @Test void effectsUseBoundedMultiplier() {
        ProgressManager p=new ProgressManager(60);p.hit(1);p.update(1,1,.8,.65);assertEquals(.52/60,p.fraction(),1e-9);
    }
}
