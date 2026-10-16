package game.engine;

import game.config.AppSettings;
import game.input.InputState;
import game.level.BaseLevel;
import game.logic.effect.EffectManager;

/** Owns state transitions only; BaseLevel owns gameplay. */
public final class Game {
    private final LevelManager levels;
    private final boolean autoAdvance;
    private final InputState input=new InputState();
    private final TransitionManager transition;
    private BaseLevel level;
    private GameState state=GameState.PLAYING;
    public Game(LevelManager levels,boolean autoAdvance) {
        this.levels=levels; this.autoAdvance=autoAdvance; level=levels.create();
        var settings=AppSettings.load();
        transition=new TransitionManager(settings.number("transition.fadeOutSeconds"),settings.number("transition.fadeInSeconds"));
    }
    public void update(double dt) {
        if (dt<0 || !Double.isFinite(dt)) throw new IllegalArgumentException("Invalid timestep");
        if (state==GameState.TRANSITION) {
            input.clear();
            if (transition.update(dt)) {
                if (transition.fadingIn()) state=GameState.PLAYING;
                else if (!autoAdvance) state=GameState.LEVEL_COMPLETE;
                else if (!levels.hasNext()) state=GameState.GAME_COMPLETE;
                else { level=levels.next(); transition.beginIn(); }
            }
            return;
        }
        if (state==GameState.GAME_OVER) {level.player().updateVisuals(dt);return;}
        if (state!=GameState.PLAYING) return;
        if(input.firing())level.fire();
        level.update(dt,input.direction());
        if (level.effects().active(EffectManager.Type.FREEZE)) input.clear();
        if (level.player().dead()) { state=GameState.GAME_OVER; input.clear(); }
        else if (level.complete()) { input.clear(); level.player().stop(); transition.beginOut(); state=GameState.TRANSITION; }
    }
    public void togglePause() {
        if (state==GameState.PLAYING) { state=GameState.PAUSED; input.clear(); level.player().stop(); }
        else if (state==GameState.PAUSED) { state=GameState.PLAYING; input.clear(); }
    }
    public void focusLost() { input.clear(); level.player().stop(); if (state==GameState.PLAYING) state=GameState.PAUSED; }
    public void restart() {
        if (state!=GameState.GAME_OVER && state!=GameState.LEVEL_COMPLETE && state!=GameState.GAME_COMPLETE) return;
        input.clear(); level=levels.create(); state=GameState.PLAYING;
    }
    public boolean acceptsMovement() { return state==GameState.PLAYING && !level.effects().active(EffectManager.Type.FREEZE); }
    public BaseLevel level() { return level; }
    public InputState input() { return input; }
    public GameState state() { return state; }
    public double fadeAlpha() { return state==GameState.TRANSITION?transition.alpha():0; }
}
