package game.level;

import game.level.day1.Day1Level;
import game.level.day2.Day2Level;
import game.level.day3.Day3Level;
import java.util.List;

/** Danh sách level theo thứ tự ngày; đăng ký level mới tại đây. */
public final class Levels {
    public static final int START_DAY = 1; // Ngày chạy thử; đổi thành 1 khi bắt đầu game từ đầu.
    private static final List<Level> LEVELS = List.of(
            new Day1Level(),
            new Day2Level(),
            new Day3Level()
    );

    private Levels() {}

    public static Level get(int day) {
        return LEVELS.stream().filter(level -> level.config().number() == day).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Chưa đăng ký level ngày " + day));
    }
}
