package game.level;

import game.config.GameConfig;
import game.model.obstacle.Shark;
import game.model.obstacle.Vehicle;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

/** Đọc dayN.properties nằm cạnh DayNLevel.java trong package của ngày đó. */
public final class DayConfigLoader {
    private static final int DEFAULT_THRESHOLD_Y = 200; // Ngưỡng sinh khi chưa cấu hình (px).
    private static final int DEFAULT_Y_GAP = 60; // Khoảng lệch dọc mặc định (px).
    private static final double DEFAULT_VEHICLE_SPEED = 312.5; // Tốc độ xe mặc định (px/giây).
    private static final int DEFAULT_MAX_ACTIVE = 8; // Giới hạn vật cản cùng tồn tại khi chưa cấu hình.

    private DayConfigLoader() {}

    public static DayConfig load(int day) {
        String path = "/game/level/day" + day + "/day" + day + ".properties";
        InputStream stream = DayConfigLoader.class.getResourceAsStream(path);
        if (stream == null) throw new IllegalArgumentException("Không tìm thấy cấu hình " + path);
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return load(day, reader);
        } catch (IOException e) {
            throw new UncheckedIOException("Không đọc được " + path, e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(path + ": " + e.getMessage(), e);
        }
    }

    static DayConfig load(int day, Reader reader) throws IOException {
        Properties values = new Properties();
        values.load(reader);
        String selected = values.getProperty("obstacles", "").trim();
        if (selected.isEmpty()) throw new IllegalArgumentException("Cần khai báo obstacles=vehicle,shark");
        List<DayConfig.ObstacleOption> options = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String name : selected.split(",", -1)) {
            String type = name.trim().toLowerCase(Locale.ROOT);
            if (!type.equals("vehicle") && !type.equals("shark")) {
                throw new IllegalArgumentException("Loại vật cản không hợp lệ: '" + name + "'. Có sẵn: vehicle, shark");
            }
            if (!seen.add(type)) throw new IllegalArgumentException("Loại vật cản bị lặp: " + type);
            if (values.containsKey(type + ".weight")) {
                throw new IllegalArgumentException("Đổi " + type + ".weight thành " + type
                        + ".chance (phần trăm độc lập từ 0 đến 100)");
            }
            double chance = nonNegativeNumber(values, type + ".chance", 100);
            if (chance > 100) throw new IllegalArgumentException(type + ".chance phải <= 100");
            if (type.equals("vehicle")) {
                double speed = positiveNumber(values, "vehicle.speed", DEFAULT_VEHICLE_SPEED);
                options.add(new DayConfig.ObstacleOption(chance,
                        (lane, offset) -> new Vehicle(lane, -Vehicle.HEIGHT - offset, speed)));
            } else {
                options.add(new DayConfig.ObstacleOption(chance,
                        (lane, offset) -> new Shark(lane, offset)));
            }
        }
        String imageFolder = "/image/day" + day + "/";
        return new DayConfig(day,
                values.getProperty("background", imageFolder + "background.png").trim(),
                values.getProperty("road", imageFolder + "road.png").trim(),
                GameConfig.BACKGROUND_COLOR, GameConfig.ROAD_COLOR,
                integer(values, "spawn.thresholdY", DEFAULT_THRESHOLD_Y),
                integer(values, "spawn.yGap", DEFAULT_Y_GAP),
                nonNegativeNumber(values, "spawn.intervalSeconds", 0),
                integer(values, "spawn.maxActive", DEFAULT_MAX_ACTIVE), options);
    }

    private static double nonNegativeNumber(Properties values, String key, double fallback) {
        try {
            double number = Double.parseDouble(values.getProperty(key, Double.toString(fallback)).trim());
            if (!Double.isFinite(number) || number < 0) throw new NumberFormatException();
            return number;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(key + " phải là số không âm hữu hạn", e);
        }
    }

    private static int integer(Properties values, String key, int fallback) {
        try {
            return Integer.parseInt(values.getProperty(key, Integer.toString(fallback)).trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(key + " phải là số nguyên", e);
        }
    }

    private static double positiveNumber(Properties values, String key, double fallback) {
        try {
            double number = Double.parseDouble(values.getProperty(key, Double.toString(fallback)).trim());
            if (!Double.isFinite(number) || number <= 0) throw new NumberFormatException();
            return number;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(key + " phải là số dương hữu hạn", e);
        }
    }
}
