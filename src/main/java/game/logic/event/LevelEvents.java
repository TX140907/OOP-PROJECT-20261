package game.logic.event;

import game.level.BaseLevel;
import java.util.Random;

/** Chọn kịch bản từ properties; màn không khai báo giữ cách chơi cũ. */
public final class LevelEvents {
    private LevelEvents() {}
    public static LevelEvent create(BaseLevel world,Random random) {
        return switch(world.config().values().text("event.type","none")) {
            case "convoy" -> new ConvoyEvent(world,random);
            case "sharkRush" -> new SharkRushEvent(world,random);
            case "iceChoice" -> new IceChoiceEvent(world,random);
            case "heatCycle" -> new HeatCycleEvent(world);
            case "none" -> new LevelEvent() {};
            default -> throw new IllegalArgumentException("Unknown event.type");
        };
    }
}
