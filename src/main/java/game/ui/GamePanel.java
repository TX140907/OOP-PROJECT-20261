package game.ui;

import game.config.GameConfig;
import game.logic.ObstacleSpawner;
import game.level.DayConfig;
import game.model.Player;
import game.model.obstacle.Obstacle;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;

/** Giao diện Swing, nhận phím và điều phối vòng lặp game. */
public final class GamePanel extends JPanel {
    private static final String RESTART_TEXT = "Chơi lại"; // Nhãn nút.
    private static final int RESTART_X = 420; // Vị trí nút X (px).
    private static final int RESTART_Y = 400; // Vị trí nút Y (px).
    private static final int RESTART_WIDTH = 160; // Chiều rộng nút (px).
    private static final int RESTART_HEIGHT = 50; // Chiều cao nút (px).
    private static final String GAME_OVER_TEXT = "GAME OVER"; // Thông báo kết thúc.
    private static final Font GAME_OVER_FONT = new Font("Arial", Font.BOLD, 48); // Phông thông báo.
    private static final Color GAME_OVER_COLOR = Color.BLACK; // Màu thông báo.
    private static final int GAME_OVER_X = 350; // Vị trí thông báo X (px).
    private static final int GAME_OVER_Y = 300; // Đường cơ sở chữ Y (px).
    private static final double MAX_DELTA_SECONDS = 0.05; // Giới hạn bước cập nhật khi luồng giao diện bị trễ.

    private final Player player = new Player();
    private final List<Obstacle> obstacles = new ArrayList<>();
    private final ObstacleSpawner spawner;
    private final DayConfig day;
    private final Image backgroundImage;
    private final Image roadImage;
    private final JButton restartButton = new JButton(RESTART_TEXT);
    private final Timer gameTimer;
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean gameOver;
    private long lastUpdateNanos;

    public GamePanel(DayConfig day) {
        this.day = day;
        spawner = new ObstacleSpawner(day);
        backgroundImage = loadImage(day.backgroundPath());
        roadImage = loadImage(day.roadPath());
        setPreferredSize(new Dimension(GameConfig.WIDTH, GameConfig.HEIGHT));
        setLayout(null);
        bindLaneKey("LEFT", -1);
        bindLaneKey("RIGHT", 1);
        restartButton.setBounds(RESTART_X, RESTART_Y, RESTART_WIDTH, RESTART_HEIGHT);
        restartButton.setVisible(false);
        restartButton.addActionListener(e -> restartGame());
        add(restartButton);
        gameTimer = new Timer(GameConfig.FRAME_DELAY_MS, e -> tick());
    }

    private Image loadImage(String path) {
        URL resource = GamePanel.class.getResource(path);
        if (resource == null) return null; // Chưa có ảnh: giữ giao diện màu hiện tại.
        try {
            Image image = ImageIO.read(resource);
            if (image == null) throw new IOException("Định dạng ảnh không được hỗ trợ: " + path);
            return image;
        } catch (IOException e) {
            throw new UncheckedIOException("Không đọc được ảnh: " + path, e);
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        lastUpdateNanos = System.nanoTime();
        if (!gameOver) gameTimer.start();
    }

    @Override
    public void removeNotify() {
        gameTimer.stop();
        super.removeNotify();
    }

    private void bindLaneKey(String key, int direction) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), key);
        getActionMap().put(key, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean held = direction < 0 ? leftPressed : rightPressed;
                if (direction < 0) leftPressed = true;
                else rightPressed = true;
                if (!held && !gameOver) player.changeLane(direction);
            }
        });
        String released = "released " + key;
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(released), released);
        getActionMap().put(released, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (direction < 0) leftPressed = false;
                else rightPressed = false;
            }
        });
    }

    private void tick() {
        long now = System.nanoTime();
        double delta = Math.min((now - lastUpdateNanos) / 1_000_000_000.0, MAX_DELTA_SECONDS);
        lastUpdateNanos = now;
        player.update(delta);
        spawner.spawnIfNeeded(obstacles, delta);
        for (Obstacle obstacle : obstacles) {
            obstacle.update(delta, obstacles);
            if (obstacle.collidesWith(player.getBounds())) {
                gameOver = true;
                gameTimer.stop();
                restartButton.setVisible(true);
                break;
            }
        }
        obstacles.removeIf(obstacle -> obstacle.isOffScreen(GameConfig.HEIGHT));
        repaint();
    }

    private void restartGame() {
        player.reset();
        obstacles.clear();
        spawner.reset();
        leftPressed = false;
        rightPressed = false;
        gameOver = false;
        restartButton.setVisible(false);
        lastUpdateNanos = System.nanoTime();
        gameTimer.start();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(day.backgroundColor());
        g.fillRect(0, 0, getWidth(), getHeight());
        if (backgroundImage != null) g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        if (roadImage != null) {
            g.drawImage(roadImage, GameConfig.ROAD_LEFT, 0,
                    GameConfig.ROAD_RIGHT - GameConfig.ROAD_LEFT, GameConfig.HEIGHT, this);
        }
        g.setColor(day.roadLineColor());
        for (int lane = 0; lane <= GameConfig.LANE_COUNT; lane++) {
            g.fillRect(GameConfig.ROAD_LEFT + lane * GameConfig.LANE_WIDTH, 0,
                    GameConfig.ROAD_LINE_WIDTH, GameConfig.HEIGHT);
        }
        player.draw(g);
        for (Obstacle obstacle : obstacles) obstacle.draw(g);
        if (gameOver) {
            g.setColor(GAME_OVER_COLOR);
            g.setFont(GAME_OVER_FONT);
            g.drawString(GAME_OVER_TEXT, GAME_OVER_X, GAME_OVER_Y);
        }
    }
}
