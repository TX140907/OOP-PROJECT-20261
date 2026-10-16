package game.ui;

import game.engine.Game;
import game.config.GameConfig;
import game.rendering.GameRenderer;
import javax.swing.JPanel;
import java.awt.*;

/** Swing view only: no gameplay rules, collision, spawning or timers. */
public final class GamePanel extends JPanel {
    private final Game game;
    private final GameRenderer renderer=new GameRenderer();
    public GamePanel(Game game) { this.game=game;setPreferredSize(new Dimension(GameConfig.WIDTH,GameConfig.HEIGHT)); }
    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);Graphics2D g=(Graphics2D)graphics.create();
        try { renderer.draw(g,game); } finally { g.dispose(); }
    }
}
