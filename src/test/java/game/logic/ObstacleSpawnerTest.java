package game.logic;

import game.config.GameConfig;
import game.level.DayConfig;
import game.level.Levels;
import game.model.obstacle.Obstacle;
import game.model.obstacle.Shark;
import game.model.obstacle.Vehicle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class ObstacleSpawnerTest {
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
    private static DayConfig config(double car, double shark, int cap, double interval) {
        return new DayConfig(2, "", "", GameConfig.BACKGROUND_COLOR, GameConfig.ROAD_COLOR,
                200, 60, interval, cap, List.of(
                new DayConfig.ObstacleOption(car, (lane, offset) -> new Vehicle(lane, -150 - offset, 175)),
                new DayConfig.ObstacleOption(shark, (lane, offset) -> new Shark(lane, offset))));
    }
    private static void noOverlap(List<Obstacle> obstacles) {
        for (int i = 0; i < obstacles.size(); i++) {
            for (int j = i + 1; j < obstacles.size(); j++) {
                check(!obstacles.get(i).getBounds().intersects(obstacles.get(j).getBounds()),
                        "Overlap: " + obstacles.get(i).getClass().getSimpleName() + obstacles.get(i).getBounds()
                        + " / " + obstacles.get(j).getClass().getSimpleName() + obstacles.get(j).getBounds());
            }
        }
    }
    public static void main(String[] args) {
        List<Obstacle> obstacles = new ArrayList<>();
        ObstacleSpawner spawner = new ObstacleSpawner(config(100, 100, 6, 1), new Random(42));
        spawner.spawnIfNeeded(obstacles, 0);
        check(obstacles.size() == 2, "Both at 100% must spawn");
        noOverlap(obstacles);
        spawner.spawnIfNeeded(obstacles, .99);
        check(obstacles.size() == 2, "Premature wave");
        obstacles.clear(); spawner.spawnIfNeeded(obstacles, .02);
        check(obstacles.size() == 2, "Interval");
        obstacles.clear(); spawner.reset(); spawner.spawnIfNeeded(obstacles, 0);
        check(obstacles.size() == 2, "Reset");
        spawner = new ObstacleSpawner(config(100, 100, 1, 1), new Random(42));
        obstacles.clear(); spawner.spawnIfNeeded(obstacles, 0);
        check(obstacles.size() == 1, "Active cap");
        spawner.spawnIfNeeded(obstacles, 2);
        check(obstacles.size() == 1, "Cap exceeded");

        spawner = new ObstacleSpawner(config(60, 40, 6, 1), new Random(7));
        int cars = 0, sharks = 0, both = 0, neither = 0;
        for (int i = 0; i < 10000; i++) {
            obstacles.clear(); spawner.spawnIfNeeded(obstacles, 1.01);
            boolean car = obstacles.stream().anyMatch(o -> o instanceof Vehicle);
            boolean shark = obstacles.stream().anyMatch(o -> o instanceof Shark);
            if (car) cars++;
            if (shark) sharks++;
            if (car && shark) both++;
            if (!car && !shark) neither++;
            noOverlap(obstacles);
        }
        check(cars > 5700 && cars < 6300, "Car 60% per cycle");
        check(sharks > 3700 && sharks < 4300, "Shark 40% per cycle");
        check(both > 2100 && both < 2700 && neither > 2100 && neither < 2700, "Independent joint outcomes");
        spawner = new ObstacleSpawner(config(0, 0, 6, 0), new Random(7));
        obstacles.clear(); spawner.spawnIfNeeded(obstacles, 0);
        check(obstacles.isEmpty(), "0% spawned");

        for (DayConfig day : List.of(Levels.get(2).config(), config(100, 100, 10, .1))) {
            spawner = new ObstacleSpawner(day, new Random(42));
            obstacles.clear();
            for (int frame = 0; frame < 12000; frame++) {
                for (Obstacle obstacle : obstacles) obstacle.update(.01, obstacles);
                obstacles.removeIf(obstacle -> obstacle.isOffScreen(GameConfig.HEIGHT));
                spawner.spawnIfNeeded(obstacles, .01);
                check(obstacles.size() <= day.maxActiveObstacles(), "Simulation cap");
                noOverlap(obstacles);
            }
        }
        System.out.println("PASS: independent rolls, both/none, timing, limits and no overlap in 120s simulation");
    }
}
