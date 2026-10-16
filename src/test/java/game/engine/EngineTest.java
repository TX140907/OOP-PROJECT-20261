package game.engine;
import game.TestSupport;
import game.config.GameConfig;
import game.level.*;
import game.logic.effect.EffectManager.Type;
import game.model.obstacle.ObstacleType;
import game.logic.collision.CollisionKind;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class EngineTest {
    private Game game(int id,boolean advance,String... overrides) {
        return new Game(new LevelManager(List.<Supplier<BaseLevel>>of(()->TestSupport.level(id,overrides)),0),advance);
    }
    @Test void allSixCanBeSelectedAndInvalidCodeFails() {
        for(int id:Levels.ORDER)assertEquals(id,new Game(new LevelManager(id),false).level().config().id());
        assertThrows(IllegalArgumentException.class,()->new LevelManager(5));
    }
    @Test void pauseFreezesAllGameplayAndClearsHeldInput() {
        Game g=game(4,false);g.level().effects().add(Type.DRAIN,2);g.input().press(KeyEvent.VK_D);g.update(.1);
        g.togglePause();double p=g.level().progress().fraction(),e=g.level().energy().value(),timer=g.level().effects().remaining(Type.DRAIN),x=g.level().player().x();
        g.update(10);assertEquals(p,g.level().progress().fraction());assertEquals(e,g.level().energy().value());
        assertEquals(timer,g.level().effects().remaining(Type.DRAIN));assertEquals(x,g.level().player().x());assertEquals(0,g.input().direction());
        g.togglePause();g.update(.1);assertEquals(x,g.level().player().x());
    }
    @Test void opposingKeysCancelAndFocusLossClearsAll() {
        Game g=game(1,false);g.input().press(KeyEvent.VK_A);g.input().press(KeyEvent.VK_D);g.update(.1);assertEquals(0,g.level().player().velocity());
        g.focusLost();assertEquals(GameState.PAUSED,g.state());assertEquals(0,g.input().direction());
    }
    @Test void newSlowAndBoostTimersPauseAndRestartWithTheirSession() {
        Game g=game(4,false);g.level().effects().add(Type.ENERGY_BOOST,2);g.level().effects().add(Type.SNOWBALL_SLOW,1.5);
        g.togglePause();g.update(10);
        assertEquals(2,g.level().effects().remaining(Type.ENERGY_BOOST));
        assertEquals(1.5,g.level().effects().remaining(Type.SNOWBALL_SLOW));
        g.togglePause();g.level().player().kill();g.update(.01);g.restart();
        assertEquals(0,g.level().effects().remaining(Type.ENERGY_BOOST));
        assertEquals(0,g.level().effects().remaining(Type.SNOWBALL_SLOW));
        assertEquals(1,g.level().effects().energyMultiplier(g.level().config().values(),0));
    }
    @Test void transitionDoesNotUpdateProgressAndMoves51To52() {
        List<Supplier<BaseLevel>> sequence=List.of(()->TestSupport.level(51,"level.durationSeconds","0.05"),()->TestSupport.level(52));
        Game g=new Game(new LevelManager(sequence,0),true);g.input().press(KeyEvent.VK_A);g.update(.06);assertEquals(GameState.TRANSITION,g.state());
        double progress=g.level().progress().fraction();g.update(.2);assertEquals(progress,g.level().progress().fraction());
        g.update(.31);assertEquals(52,g.level().config().id());assertEquals(0,g.level().progress().fraction());assertTrue(g.level().obstacles().isEmpty());
        g.update(.5);assertEquals(GameState.PLAYING,g.state());assertEquals(0,g.level().progress().fraction());assertEquals(0,g.input().direction());
    }
    @Test void completionOfLastAndStandaloneAndRestart() {
        Game last=game(52,true,"level.durationSeconds","0.01");last.update(.02);last.update(.5);assertEquals(GameState.GAME_COMPLETE,last.state());
        Game single=game(2,false,"level.durationSeconds","0.01");single.update(.02);single.update(.5);assertEquals(GameState.LEVEL_COMPLETE,single.state());
        BaseLevel old=single.level();single.restart();assertNotSame(old,single.level());assertEquals(0,single.level().progress().fraction());assertEquals(3,single.level().player().health());
    }
    @Test void deathWinsOverCompletionAndRestartResetsEverySystem() {
        Game g=game(4,false,"level.durationSeconds","0.01");BaseLevel w=g.level();
        w.addObstacle(TestSupport.object(ObstacleType.BUS,CollisionKind.LETHAL,w.player().x(),w.player().y(),60,90,0,0));
        g.update(.02);assertEquals(GameState.GAME_OVER,g.state());double p=w.progress().fraction();g.update(5);assertEquals(p,w.progress().fraction());
        g.restart();assertEquals(GameState.PLAYING,g.state());assertTrue(g.level().obstacles().isEmpty());assertEquals(100,g.level().energy().value());assertEquals(0,g.level().player().velocity());
    }
    @Test void loopIsFixedStepBoundedAndStartsOnlyOnce() {
        Game g=game(1,false);GameLoop loop=new GameLoop(g,()->{});loop.advance(1.0/120);assertEquals(0,g.level().progress().fraction());
        loop.advance(1.0/120);assertEquals(GameConfig.STEP/60,g.level().progress().fraction(),1e-8);
        double before=g.level().progress().fraction();loop.advance(100);assertTrue(g.level().progress().fraction()-before<=.251/60);
        assertFalse(loop.running());
    }
    @Test void entireNoStorySequenceAdvancesInOfficialOrder() {
        List<Supplier<BaseLevel>> sequence=Levels.ORDER.stream().<Supplier<BaseLevel>>map(id->
                ()->TestSupport.level(id,"level.durationSeconds","0.05")).toList();
        Game game=new Game(new LevelManager(sequence,0),true);
        List<Integer> seen=new ArrayList<>();seen.add(game.level().config().id());
        for(int i=0;i<600;i++) {
            game.update(1.0/60);int id=game.level().config().id();
            if(seen.get(seen.size()-1)!=id)seen.add(id);
        }
        assertEquals(Levels.ORDER,seen);assertEquals(GameState.GAME_COMPLETE,game.state());
    }
    @Test void startingLoopTwiceDoesNotCreateAnotherTimerAndStopWorks() throws Exception {
        Game g=game(1,false);GameLoop loop=new GameLoop(g,()->{});
        javax.swing.SwingUtilities.invokeAndWait(()->{
            loop.start();loop.start();assertTrue(loop.running());loop.stop();assertFalse(loop.running());
        });
    }
    @Test void holdingEscapeDoesNotRapidlyTogglePause() {
        Game g=game(1,false);javax.swing.JPanel panel=new javax.swing.JPanel();
        new game.input.InputHandler(panel,g);
        var down=panel.getActionMap().get("down27");var up=panel.getActionMap().get("up27");
        down.actionPerformed(null);assertEquals(GameState.PAUSED,g.state());
        down.actionPerformed(null);assertEquals(GameState.PAUSED,g.state());
        up.actionPerformed(null);down.actionPerformed(null);assertEquals(GameState.PLAYING,g.state());
    }
}
