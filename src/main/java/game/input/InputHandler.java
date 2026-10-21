package game.input;

import game.engine.Game;
import game.engine.GameState;
import java.awt.event.*;
import javax.swing.*;

public final class InputHandler {
    private final java.util.Set<Integer> actionKeys=new java.util.HashSet<>();
    public void clear() { actionKeys.clear(); }
    public InputHandler(JComponent component,Game game) {
        this(component,game,game::togglePause);
    }
    public InputHandler(JComponent component,Game game,Runnable pauseAction) {
        for (int key:new int[]{KeyEvent.VK_A,KeyEvent.VK_D,KeyEvent.VK_LEFT,KeyEvent.VK_RIGHT,KeyEvent.VK_E,KeyEvent.VK_ESCAPE,KeyEvent.VK_R}) {
            String down="down"+key,up="up"+key;
            component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key,0,false),down);
            component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key,0,true),up);
            component.getActionMap().put(down,new AbstractAction() {
                @Override public void actionPerformed(ActionEvent event) {
                    if (key==KeyEvent.VK_ESCAPE) { if (actionKeys.add(key)) pauseAction.run(); }
                    else if (key==KeyEvent.VK_R) { if (actionKeys.add(key) && game.state()!=GameState.PAUSED) game.restart(); }
                    else if (game.acceptsMovement()) game.input().press(key);
                }
            });
            component.getActionMap().put(up,new AbstractAction() {
                @Override public void actionPerformed(ActionEvent event) { game.input().release(key);actionKeys.remove(key); }
            });
        }
    }
}
