package game.level;

import game.model.obstacle.Obstacle;
import game.model.obstacle.Shark;
import game.model.obstacle.Vehicle;
import java.io.StringReader;

public final class DayConfigLoaderTest {
    private static DayConfig parse(String text) throws Exception {
        return DayConfigLoader.load(2, new StringReader(text));
    }
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
    private static void rejects(String text, String message) throws Exception {
        try { parse(text); }
        catch (IllegalArgumentException e) {
            check(e.getMessage().contains(message), "Missing detail: " + e.getMessage());
            return;
        }
        throw new AssertionError("Invalid config accepted: " + text);
    }
    public static void main(String[] args) throws Exception {
        for (int day = 1; day <= 3; day++) {
            DayConfig config = Levels.get(day).config();
            check(config.number() == day, "Wrong day");
            check(config.backgroundPath().equals("/image/day" + day + "/background.png"), "Wrong image");
        }
        DayConfig mixed = parse("obstacles=vehicle,shark\nvehicle.chance=80\nshark.chance=70\n");
        check(mixed.obstacles().get(0).chancePercent() == 80
                && mixed.obstacles().get(1).chancePercent() == 70, "Total may exceed 100");
        check(mixed.obstacles().get(0).factory().apply(0, 0.0) instanceof Vehicle, "Vehicle factory");
        check(mixed.obstacles().get(1).factory().apply(0, 0.0) instanceof Shark, "Shark factory");
        DayConfig disabled = parse("obstacles=vehicle,shark\nvehicle.chance=0\nshark.chance=0\n");
        check(disabled.obstacles().stream().allMatch(option -> option.chancePercent() == 0), "Disable all types");
        DayConfig fractional = parse("obstacles=shark\nshark.chance=12.5\n");
        check(fractional.obstacles().get(0).chancePercent() == 12.5, "Decimal chance");
        DayConfig custom = parse("obstacles=vehicle\nvehicle.speed=100\nspawn.thresholdY=300\nspawn.yGap=90\n");
        Obstacle vehicle = custom.obstacles().get(0).factory().apply(1, 90.0);
        check(vehicle.getY() == -240, "Spawn offset");
        vehicle.update(1);
        check(vehicle.getY() == -140, "Configured speed");
        check(custom.spawnThresholdY() == 300 && custom.spawnYGap() == 90, "Spawn settings");
        check(custom.obstacles().get(0).chancePercent() == 100, "Default 100%");
        rejects("obstacles=unknown", "vehicle, shark");
        rejects("obstacles=vehicle,vehicle", "vehicle");
        rejects("obstacles=vehicle\nvehicle.chance=-1", "vehicle.chance");
        rejects("obstacles=vehicle\nvehicle.chance=101", "vehicle.chance");
        rejects("obstacles=vehicle\nvehicle.chance=NaN", "vehicle.chance");
        rejects("obstacles=vehicle\nvehicle.chance=abc", "vehicle.chance");
        rejects("obstacles=vehicle\nvehicle.weight=60", "vehicle.chance");
        rejects("obstacles=vehicle\nvehicle.speed=NaN", "vehicle.speed");
        rejects("obstacles=vehicle\nspawn.yGap=0", "không hợp lệ");
        rejects("obstacles=vehicle\nspawn.intervalSeconds=-1", "spawn.intervalSeconds");
        rejects("obstacles=vehicle\nspawn.maxActive=0", "không hợp lệ");
        rejects("", "obstacles");
        System.out.println("PASS: independent probabilities, 0/100%, decimals and invalid config");
    }
}