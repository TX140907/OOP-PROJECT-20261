import javax.swing.*;

public class main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("My Game");

            window.setDefaultCloseOperation(
                    JFrame.EXIT_ON_CLOSE
            );

            window.add(new GamePanel());
            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }
}