package game.model.obstacle;

import game.config.GameConfig;
import game.model.Player;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/** Hồi quy: độ lệch sinh không được dùng làm tọa độ đớp; cảnh báo phải đủ thời gian. */
public final class SharkTest {
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }

    private static BufferedImage render(Shark shark) {
        BufferedImage image = new BufferedImage(GameConfig.WIDTH, GameConfig.HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = image.getGraphics();
        shark.draw(graphics);
        graphics.dispose();
        return image;
    }

    public static void main(String[] args) {
        for (int offset : new int[]{0, 60}) {
            Shark shark = new Shark(1, offset);
            check(shark.getY() == -50 - offset, "Vây không bắt đầu ở biên trên");
            shark.update(.25);
            check(shark.getY() == -25 - offset, "Sai tốc độ vây");
            shark.update(.25);
            check(shark.getState() == Shark.State.HIDDEN && shark.getY() == -offset, "Sai chỗ lặn");
            if (offset == 0) {
                check((render(shark).getRGB(500, 58) >>> 24) != 0, "Thiếu vòng tròn chỗ lặn");
            }
            shark.update(.99);
            check(shark.getState() == Shark.State.HIDDEN, "Ẩn chưa đủ một giây");
            shark.update(.02);
            check(shark.getState() == Shark.State.WARNING, "Chưa bắt đầu cảnh báo");
            double centerY = new Player().getBounds().getCenterY();
            check(shark.getY() + Shark.HEIGHT / 2.0 == centerY, "Cá phải cùng hàng nhân vật");
            BufferedImage warning = render(shark);
            int target = -1;
            for (int lane = 0; lane < GameConfig.LANE_COUNT; lane++) {
                if ((warning.getRGB(GameConfig.centeredX(lane, 0), (int) centerY) >>> 24) != 0) {
                    check(target == -1, "Cảnh báo nhiều vị trí");
                    target = lane;
                }
            }
            check(target >= 0, "Không vẽ cảnh báo ở hàng nhân vật");
            check(!shark.collidesWith(new Rectangle2D.Double(260, 520, 480, 90)), "Cảnh báo gây sát thương");
            shark.update(.12);
            check((render(shark).getRGB(GameConfig.centeredX(target, 0), (int) centerY) >>> 24) == 0,
                    "Cảnh báo không nhấp nháy");
            shark.update(1.12);
            check(shark.getState() == Shark.State.WARNING, "Đớp sớm trước 1,25 giây");
            shark.update(.02);
            check(shark.getState() == Shark.State.BITE, "Không đớp sau cảnh báo");
            for (int lane = 0; lane < GameConfig.LANE_COUNT; lane++) {
                Rectangle2D player = new Rectangle2D.Double(GameConfig.centeredX(lane, Player.WIDTH),
                        centerY - Player.HEIGHT / 2.0, Player.WIDTH, Player.HEIGHT);
                check(shark.collidesWith(player) == (lane == target), "Đớp khác vị trí cảnh báo");
            }
            shark.update(.46);
            check(shark.isOffScreen(GameConfig.HEIGHT), "Cá không được xóa sau đòn đớp");
        }
        Shark delayed = new Shark(0, 0);
        delayed.update(10); // Khung hình trễ không được bỏ qua cảnh báo.
        delayed.update(10);
        check(delayed.getState() == Shark.State.WARNING, "Bỏ qua cảnh báo khi trễ");
        check(!delayed.collidesWith(new Rectangle2D.Double(260, 520, 480, 90)), "Sát thương ngay lúc đổi mục tiêu");
        Shark waiting = new Shark(0, 0);
        List<Obstacle> occupied = new ArrayList<>();
        occupied.add(waiting);
        for (int lane = 0; lane < GameConfig.LANE_COUNT; lane++) {
            occupied.add(new Vehicle(lane, 495, 175));
        }
        waiting.update(.5, occupied);
        waiting.update(1, occupied);
        check(waiting.getState() == Shark.State.HIDDEN, "Phải chờ nếu mọi làn đang bị chiếm");
        occupied.remove(2); // Chỉ giải phóng làn giữa.
        waiting.update(.01, occupied);
        check(waiting.getState() == Shark.State.WARNING, "Không chọn được làn vừa trống");
        check(waiting.getBounds().getCenterX() == GameConfig.centeredX(1, 0), "Chọn làn bị chiếm");
        Vehicle incoming = new Vehicle(1, -150, 1000);
        check(waiting.blocksSpawn(incoming), "Xe mới sẽ chạy vào vùng cảnh báo nhưng không bị chặn");
        Shark another = new Shark(2, 0);
        occupied.add(another);
        another.update(.5, occupied);
        another.update(1, occupied);
        check(another.getState() == Shark.State.HIDDEN, "Hai cá cùng chọn một chỗ cảnh báo");
        System.out.println("PASS: shark timing, player row, matching bite and occupied lane reservations");
    }
}
