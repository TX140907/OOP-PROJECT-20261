package game.level;

import game.model.obstacle.Obstacle;
import java.awt.Color;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

/** Dữ liệu của một ngày; không chứa vòng lặp hay giao diện game. */
public record DayConfig(
        int number,                     // Số ngày, bắt đầu từ 1.
        String backgroundPath,          // Ảnh nền trong classpath; thiếu ảnh thì dùng màu nền.
        String roadPath,                // Ảnh đường, chỉ vẽ trong vùng ba làn ở giữa.
        Color backgroundColor,          // Màu nền dự phòng.
        Color roadLineColor,            // Màu vạch đường khi chưa có ảnh đường.
        int spawnThresholdY,            // Ngưỡng Y khi spawnIntervalSeconds = 0 (px).
        int spawnYGap,                  // Khoảng lệch dọc giữa các vật cản cùng đợt (px).
        double spawnIntervalSeconds,    // Nhịp sinh đợt (giây); 0 giữ cách chờ theo vị trí/trạng thái.
        int maxActiveObstacles,         // Giới hạn tổng vật cản đang tồn tại.
        List<ObstacleOption> obstacles  // Các loại vật cản và xác suất riêng mỗi lần xét sinh.
) {
    public DayConfig {
        if (number < 1 || spawnThresholdY < 0 || spawnYGap < 1) {
            throw new IllegalArgumentException("Cấu hình ngày hoặc khoảng cách sinh không hợp lệ");
        }
        if (!Double.isFinite(spawnIntervalSeconds) || spawnIntervalSeconds < 0 || maxActiveObstacles < 1) {
            throw new IllegalArgumentException("Nhịp sinh hoặc giới hạn vật cản không hợp lệ");
        }
        Objects.requireNonNull(backgroundPath);
        Objects.requireNonNull(roadPath);
        Objects.requireNonNull(backgroundColor);
        Objects.requireNonNull(roadLineColor);
        obstacles = List.copyOf(obstacles);
        if (obstacles.isEmpty()) throw new IllegalArgumentException("Cần ít nhất một loại vật cản");
    }

    /** Factory nhận (làn, độ lệch phía trên điểm sinh); mỗi lần gọi tạo một vật cản mới. */
    public record ObstacleOption(double chancePercent, BiFunction<Integer, Double, Obstacle> factory) {
        public ObstacleOption {
            if (!Double.isFinite(chancePercent) || chancePercent < 0 || chancePercent > 100) {
                throw new IllegalArgumentException("Xác suất phải trong khoảng 0 đến 100%");
            }
            Objects.requireNonNull(factory);
        }
    }
}
