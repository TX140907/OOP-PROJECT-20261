package game.ui;

import game.engine.Game;
import game.config.GameConfig;
import game.config.AppSettings;
import game.rendering.GameRenderer;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import game.save.SaveRepository;
import java.nio.file.Path;
import java.awt.*;

/** Swing view only: no gameplay rules, collision, spawning or timers. */
public final class GamePanel extends JPanel {
    private final Game game;
    private final GameRenderer renderer=new GameRenderer();
    private final MenuOverlay menus;
    public GamePanel(Game game) {
        this.game=game;setPreferredSize(new Dimension(GameConfig.WIDTH,GameConfig.HEIGHT));setLayout(null);
        SaveRepository saves=new SaveRepository(Path.of(AppSettings.load().text("save.directory","saves")));
        menus=new MenuOverlay(game,saves,()->{
            Window window=SwingUtilities.getWindowAncestor(this);if(window!=null)window.dispose();
        });
        add(menus);
    }
    public void refresh() {menus.refresh();repaint();}
    public void escape() {if(!menus.escape())game.togglePause();refresh();}
    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);Graphics2D g=(Graphics2D)graphics.create();
        try { renderer.draw(g,game); } finally { g.dispose(); }
    }
}
