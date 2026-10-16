package game.logic.event;

/** Điểm nhấn của màn, chỉ cập nhật theo thời gian chơi và không thay luật va chạm. */
public interface LevelEvent {
    default void update(double dt) {}
    default boolean allowsNormalSpawns() { return true; }
    default double spawnMultiplier() { return 1; }
    default double glareStrength() { return 1; }
    default boolean energyWindow() { return true; }
    default int iceLane() { return -1; }
    default double iceRoadRemainingSeconds() { return 0; }
    default String message() { return ""; }
}
