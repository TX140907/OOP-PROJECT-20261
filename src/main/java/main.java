import game.level.Level;
import game.level.Levels;
import game.ui.GamePanel;
import javax.swing.*;

public class main {
    private static final String WINDOW_TITLE = "My Game"; // Tiêu đề cửa sổ game.

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Level level = Levels.get(Levels.START_DAY);
            JFrame window = new JFrame(WINDOW_TITLE + " - Ngày " + level.config().number());

            window.setDefaultCloseOperation(
                    JFrame.EXIT_ON_CLOSE
            );

            window.add(new GamePanel(level.config()));
            window.pack();
            window.setResizable(false);
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }
}
