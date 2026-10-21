package game.save;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

/** Ba ô độc lập, đọc khi mở menu; ghi tệp tạm rồi thay thế để tránh bản lưu dở dang. */
public final class SaveRepository {
    public static final int SLOT_COUNT=3;
    private static final String VERSION="1";
    private final Path directory;
    public record Slot(int number,SaveData data,String error) {
        public boolean empty() {return data==null && error.isEmpty();}
        public boolean occupied() {return !empty();}
    }
    public SaveRepository(Path directory) {this.directory=directory.toAbsolutePath().normalize();}
    private Path file(int slot) {
        if(slot<1 || slot>SLOT_COUNT)throw new IllegalArgumentException("Invalid save slot");
        return directory.resolve("slot-"+slot+".properties");
    }
    public List<Slot> slots() {
        List<Slot> result=new ArrayList<>();
        for(int slot=1;slot<=SLOT_COUNT;slot++)result.add(read(slot));
        return List.copyOf(result);
    }
    public Slot read(int slot) {
        Path path=file(slot);
        try {
            if(Files.notExists(path))return new Slot(slot,null,"");
            if(!Files.isRegularFile(path) || Files.size(path)>65536)throw new IOException("Invalid save file");
            Properties p=new Properties();
            try(Reader reader=Files.newBufferedReader(path,StandardCharsets.UTF_8)) {p.load(reader);}
            if(!VERSION.equals(p.getProperty("version")))throw new IOException("Unsupported save version");
            String advance=p.getProperty("autoAdvance");
            if(!Set.of("true","false").contains(advance))throw new IOException("Invalid saved mode");
            SaveData data=new SaveData(Integer.parseInt(p.getProperty("levelId")),p.getProperty("levelName"),
                    SaveData.Checkpoint.valueOf(p.getProperty("checkpoint")),p.getProperty("dialogueId",""),
                    Boolean.parseBoolean(advance),Instant.parse(p.getProperty("savedAt")));
            return new Slot(slot,data,"");
        } catch(IOException | RuntimeException e) {
            return new Slot(slot,null,"Không đọc được bản lưu hoặc phiên bản không tương thích.");
        }
    }
    public void write(int slot,SaveData data) throws IOException {
        Path destination=file(slot);Objects.requireNonNull(data);
        Properties p=new Properties();
        p.setProperty("version",VERSION);p.setProperty("levelId",Integer.toString(data.levelId()));
        p.setProperty("levelName",data.levelName());p.setProperty("checkpoint",data.checkpoint().name());
        p.setProperty("dialogueId",data.dialogueId());p.setProperty("autoAdvance",Boolean.toString(data.autoAdvance()));
        p.setProperty("savedAt",data.savedAt().toString());
        Files.createDirectories(directory);
        Path temporary=Files.createTempFile(directory,"slot-"+slot+"-",".tmp");
        try {
            try(Writer writer=Files.newBufferedWriter(temporary,StandardCharsets.UTF_8)) {
                p.store(writer,"HUST Exam Run - checkpoint save v"+VERSION);
            }
            try {Files.move(temporary,destination,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
            catch(AtomicMoveNotSupportedException e) {Files.move(temporary,destination,StandardCopyOption.REPLACE_EXISTING);}
        } finally {Files.deleteIfExists(temporary);}
    }
}
