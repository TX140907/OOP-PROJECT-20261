import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ArrayDeque;
public class GamePanel extends JPanel {
    public int WIDTH = 1000;
    public int HEIGHT = 700;
    public int WIDTH_PLayer = 48;
    public int HEIGHT_Player = 72;
    private int carX = 350;
    private int carY = 400;
    private boolean upPressed;
    private boolean downPressed;
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean gameOver = false;
    private JButton restartButton;
    private final Timer gameTimer;
    private List<Obstacle> obstacles = new ArrayList<>();
    private int []posStart = new int[3];

    private void bindKey(
            String key,
            String name,
            Runnable action
    ) {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(key), name);

        getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(
                    java.awt.event.ActionEvent e
            ) {
                action.run();
            }
        });

        String releaseName = name + "Released";

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(
                        "released " + key
                ), releaseName);

        getActionMap().put(releaseName, new AbstractAction() {
            @Override
            public void actionPerformed(
                    java.awt.event.ActionEvent e
            ) {
                switch (key) {
                    case "UP" -> upPressed = false;
                    case "DOWN" -> downPressed = false;
                    case "LEFT" -> leftPressed = false;
                    case "RIGHT" -> rightPressed = false;
                }
            }
        });
    }
    private void setupKeyBindings() {
        bindKey("UP", "upPressed", () -> upPressed = true);
        bindKey("DOWN", "downPressed", () -> downPressed = true);
        bindKey("LEFT", "leftPressed", () -> leftPressed = true);
        bindKey("RIGHT", "rightPressed", () -> rightPressed = true);
    }
    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setupKeyBindings();

        posStart[0] = 230;
        posStart[1] = 470;
        posStart[2] = 710;
        setLayout(null);

        restartButton = new JButton("Chơi lại");
        restartButton.setBounds(420, 400, 160, 50);
        restartButton.setVisible(false);

        restartButton.addActionListener(e -> restartGame());
        add(restartButton);

        gameTimer = new Timer(16, e -> {
            if (!gameOver) {
                updateGame();
            }

            repaint();
        });

        gameTimer.start();
    }
    private  boolean out(int x1, int y1, int x2, int y2, int u1, int v1, int u2, int v2) {
        if(x2 < u1 || x1 > u2 || y2 < v1 || v2 < y1) return true;
        return false;
    }
    private boolean checkCollision() {
        for(Obstacle ob:obstacles) {
            if(!out(ob.getposX(), ob.getPosY(), ob.getposX() + ob.getWidth(), ob.getPosY() + ob.getHeight(), carX, carY, carX + WIDTH_PLayer, carY + HEIGHT_Player)) return true;
        }
        return false;
    }
    private void restartGame() {
        // Đưa xe về vị trí ban đầu
        carX = 350;
        carY = 400;

        // Xóa toàn bộ chướng ngại vật cũ
        obstacles.clear();

        // Reset trạng thái phím
        upPressed = false;
        downPressed = false;
        leftPressed = false;
        rightPressed = false;

        // Bắt đầu lại
        gameOver = false;
        restartButton.setVisible(false);
        gameTimer.start();

        repaint();
    }
    private void endGame() {
        gameOver = true;
        gameTimer.stop();
        restartButton.setVisible(true);
        repaint();
    }
    // Cập nhật trạng thái
    private void updateGame() {
        if(rightPressed && carX + 10 <= 870 - WIDTH_PLayer) carX += 10;
        if(leftPressed && carX - 10 >= 0) carX -= 10;
        if(upPressed && carY - 10 >= 0) carY -= 10;
        if(downPressed && carY + 10 <= 650 - HEIGHT_Player) carY += 10;
        if(obstacles.isEmpty() || obstacles.get(obstacles.size() - 1).getPosY() > 200) {
            int type = (int) (Math.random() * (6 - 0 + 1)) + 0;
            if(type <= 2) {
                obstacles.add(new Obstacle(posStart[type], -120, 80, 120));
            }
            else if(type == 3) {
                obstacles.add(new Obstacle(posStart[0], -120, 80, 120));
                obstacles.add(new Obstacle(posStart[1], -120, 80, 120));
            }
            else if(type == 4) {
                obstacles.add(new Obstacle(posStart[0], -120, 80, 120));
                obstacles.add(new Obstacle(posStart[2], -120, 80, 120));
            }
            else if(type == 5) {
                obstacles.add(new Obstacle(posStart[1], -120, 80, 120));
                obstacles.add(new Obstacle(posStart[2], -120, 80, 120));
            }
            else if(type == 6) {
                obstacles.add(new Obstacle(posStart[0], -120, 80, 120));
                obstacles.add(new Obstacle(posStart[1], -120, 80, 120));
                obstacles.add(new Obstacle(posStart[2], -120, 80, 120));
            }
        }
        if(!obstacles.isEmpty()) {
            if(obstacles.get(0).getPosY() > HEIGHT)obstacles.remove(0);
        }
        for(Obstacle obs: obstacles) {
            obs.move(3, 5);
        }
        if (checkCollision()) {
            endGame();
        }
    }

    // Vẽ game
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Day 1
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.BLACK);
        g.fillRect(150, 0, 1, HEIGHT);
        g.fillRect(390, 0, 1, HEIGHT);
        g.fillRect(630, 0, 1, HEIGHT);
        g.fillRect(870, 0, 1, HEIGHT);
        g.setColor(Color.RED);
        g.fillRect(carX, carY, WIDTH_PLayer, HEIGHT_Player);
        g.setColor(Color.green);
        for(Obstacle obs: obstacles) {
            g.fillRect(obs.getposX(), obs.getPosY(), obs.getWidth(), obs.getHeight());
        }
        if (gameOver) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("GAME OVER", 350, 300);
        }
        // Day 1
    }
}