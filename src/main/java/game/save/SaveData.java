package game.save;

import game.level.Levels;
import java.time.Instant;
import java.util.Objects;

/** Chỉ lưu mốc bắt đầu, không tuần tự hóa player, vật cản, timer hoặc cả màn chơi. */
public record SaveData(int levelId,String levelName,Checkpoint checkpoint,String dialogueId,
                       boolean autoAdvance,Instant savedAt) {
    public enum Checkpoint { LEVEL_START, DIALOGUE_START }
    public SaveData {
        if(!Levels.ORDER.contains(levelId))throw new IllegalArgumentException("Unknown saved level");
        if(levelName==null || levelName.isBlank() || levelName.length()>160)
            throw new IllegalArgumentException("Invalid saved level name");
        Objects.requireNonNull(checkpoint);Objects.requireNonNull(savedAt);
        dialogueId=dialogueId==null?"":dialogueId.trim();
        if(dialogueId.length()>160 || (checkpoint==Checkpoint.DIALOGUE_START && dialogueId.isEmpty())
                || (checkpoint==Checkpoint.LEVEL_START && !dialogueId.isEmpty()))
            throw new IllegalArgumentException("Invalid dialogue checkpoint");
    }
}
