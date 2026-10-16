import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import game.engine.Game;
import game.engine.GameLoop;
import game.engine.LevelManager;
import game.input.InputHandler;
import game.level.Levels;
import game.rendering.GameRenderer;
import game.ui.GamePanel;

/** Single application entry point. No-Story Mode only. */
public final class Main {
    private static final boolean RUN_ALL_LEVELS =  true; // true: Day1 -> ... -> Day5Helicopter.
    private static final int SELECTED_LEVEL = 3; // 1, 2, 3, 4, 51, 52 when RUN_ALL_LEVELS=false.
    private static final boolean AUTO_ADVANCE = true; // false: stop at Level Complete.
    public static void main(String[] args) {
        boolean all=RUN_ALL_LEVELS,advance=AUTO_ADVANCE,smoke=false;
        int selected=SELECTED_LEVEL;
        for (String arg:args) {
            if (arg.startsWith("--level=")) { selected=Integer.parseInt(arg.substring(8));all=false; }
            else if (arg.equals("--all")) all=true;
            else if (arg.equals("--no-auto")) advance=false;
            else if (arg.equals("--smoke")) smoke=true;
            else throw new IllegalArgumentException("Unknown option: "+arg);
        }
        if (smoke) { smoke();return; }
        if (GraphicsEnvironment.isHeadless()) throw new IllegalStateException("No display; use --smoke for headless verification");
        Game game=new Game(new LevelManager(all?1:selected),advance);
        SwingUtilities.invokeLater(()->{
            JFrame frame=new JFrame("HUST Exam Run - No-Story Mode");
            GamePanel panel=new GamePanel(game);InputHandler handler=new InputHandler(panel,game);
            GameLoop loop=new GameLoop(game,panel::repaint);
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);frame.add(panel);frame.pack();
            frame.setResizable(false);frame.setLocationRelativeTo(null);
            frame.addWindowFocusListener(new WindowAdapter() {
                @Override public void windowLostFocus(WindowEvent event) { handler.clear();game.focusLost(); }
            });
            frame.addWindowListener(new WindowAdapter() { @Override public void windowClosed(WindowEvent event) { loop.stop(); } });
            frame.setVisible(true);loop.start();
        });
    }
    private static void smoke() {
        try {
            File directory=new File("target/smoke"); if (!directory.isDirectory() && !directory.mkdirs()) throw new IllegalStateException("Cannot create smoke output");
            for (int id:Levels.ORDER) {
                Game game=new Game(new LevelManager(id),false);
                for (int frame=0;frame<240;frame++) game.update(1.0/60);
                BufferedImage image=new BufferedImage(1000,700,BufferedImage.TYPE_INT_RGB);
                Graphics2D g=image.createGraphics();new GameRenderer().draw(g,game);g.dispose();
                ImageIO.write(image,"png",new File(directory,"level-"+id+".png"));
                System.out.println("SMOKE PASS level="+id+" config="+game.level().config().name());
            }
        } catch (Exception e) { throw new IllegalStateException("Smoke test failed",e); }
    }
}
