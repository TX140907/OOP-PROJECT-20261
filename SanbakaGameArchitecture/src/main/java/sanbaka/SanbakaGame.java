package sanbaka;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class SanbakaGame extends GameScreenRenderer {
    public SanbakaGame() {
        setPreferredSize(new Dimension(W, H));
        setFocusable(true);
        addKeyListener(this);
        lines.addAll(StoryData.build());
        findCallRange();
        timer.start();
        dialogBackground = loadDialogBackground();
    }

    private java.awt.image.BufferedImage loadDialogBackground() {
        String[] paths = {
            "sanbaka2.jpg",
            "src/main/java/sanbaka/sanbaka2.jpg"
        };
        for (String path : paths) {
            java.io.File imageFile = new java.io.File(path);
            if (!imageFile.isFile()) continue;
            try {
                return javax.imageio.ImageIO.read(imageFile);
            } catch (java.io.IOException e) {
                System.err.println("Không đọc được ảnh nền: " + imageFile.getAbsolutePath());
                return null;
            }
        }
        System.err.println("Không tìm thấy sanbaka2.jpg; game sẽ dùng nền mặc định.");
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Tuần thi của SVBK");
            SanbakaGame game = new SanbakaGame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(game);
            frame.pack();
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            game.requestFocusInWindow();
        });
    }
}
