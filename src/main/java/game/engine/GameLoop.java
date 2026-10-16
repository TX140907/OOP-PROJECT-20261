package game.engine;

import game.config.AppSettings;
import game.config.GameConfig;
import javax.swing.Timer;

/** One EDT timer and a bounded fixed-step accumulator, independent of rendering FPS. */
public final class GameLoop {
    private final Game game;
    private final Timer timer;
    private final double maxFrame;
    private double accumulator;
    private long previous;
    public GameLoop(Game game,Runnable repaint) {
        this.game=game; var c=AppSettings.load(); maxFrame=c.number("loop.maxFrameSeconds");
        timer=new Timer(c.integer("loop.timerMillis"),event->{
            long now=System.nanoTime(); advance((now-previous)/1_000_000_000.0); previous=now; repaint.run();
        });
    }
    public void advance(double seconds) {
        accumulator+=Math.max(0,Math.min(maxFrame,seconds));
        while (accumulator+1e-10>=GameConfig.STEP) { game.update(GameConfig.STEP); accumulator-=GameConfig.STEP; }
    }
    public void start() { if (!timer.isRunning()) { previous=System.nanoTime(); accumulator=0; timer.start(); } }
    public void stop() { timer.stop(); accumulator=0; }
    public boolean running() { return timer.isRunning(); }
}
