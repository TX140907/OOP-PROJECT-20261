package sanbaka.assets;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import sanbaka.state.GameMenuState;
import sanbaka.state.GameState;

/**
 * Quản lý phát nhạc nền (BGM) và hiệu ứng âm thanh (SFX) cho game Sanbaka.
 * Hỗ trợ nạp bất đồng bộ (không nghẽn khung hình EDT), phát lặp vô hạn (loop),
 * chuyển bài mượt mà giữa các cảnh, tạm dừng (pause/resume), chỉnh âm lượng và tắt tiếng (mute).
 */
public final class SoundManager {

    private static final Object LOCK = new Object();
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "SanbakaAudioWorker");
        t.setDaemon(true);
        return t;
    });

    private static volatile float volume = 0.8f;
    private static volatile float ambienceVolumeFactor = 0.45f; // Căn âm lượng tiếng mưa nền hài hòa (~45% âm lượng nhạc)
    private static volatile float windVolumeFactor = 0.30f; // Tiếng gió mùa Đông Bắc nhẹ nhàng (~30% âm lượng nhạc)
    private static volatile boolean muted = false;
    private static volatile boolean isPaused = false;
    private static volatile long pausePositionUs = 0;

    private static volatile String currentTrackName = null;
    private static volatile String targetTrackName = null;
    private static volatile Clip currentClip = null;

    // Lớp âm thanh môi trường (Ambience Layer) phát song song với BGM (ví dụ tiếng mưa Ngày 2)
    private static volatile String currentAmbienceName = null;
    private static volatile String targetAmbienceName = null;
    private static volatile Clip currentAmbienceClip = null;
    private static volatile boolean isAmbiencePaused = false;
    private static volatile long pauseAmbiencePositionUs = 0;

    // Hiệu ứng tiếng gió lạnh mùa Đông Bắc (Ngày 3)
    private static volatile Clip windClip = null;
    private static volatile boolean isWindPaused = false;

    // Hiệu ứng âm thanh đường phố Màn 5 (kẹt xe)
    private static volatile float streetVolumeFactor = 0.30f; // Tiếng vừa vừa (~30% âm lượng nhạc)
    private static volatile Clip streetClip = null;
    private static volatile boolean isStreetPaused = false;

    // Hiệu ứng tiếng cánh quạt trực thăng Màn 5 (cứu viện)
    private static volatile float helicopterVolumeFactor = 0.35f; // Tiếng vừa vừa (~35% âm lượng nhạc)
    private static volatile Clip helicopterClip = null;
    private static volatile boolean isHelicopterPaused = false;

    private static final Map<String, Clip> SFX_CACHE = new ConcurrentHashMap<>();

    private SoundManager() {}

    /**
     * Tìm file âm thanh trong các thư mục assets hoặc trong classpath.
     */
    public static File findAudioFile(String relativeName) {
        if (relativeName == null || relativeName.isBlank()) return null;
        String clean = relativeName.replace('\\', '/').trim();

        String[] baseDirs = {
            "assets/sound",
            "assets/sounds",
            "OOP-PROJECT-20261/assets/sound",
            "OOP-PROJECT-20261/assets/sounds"
        };
        String[] subDirs = {"", "bgm/", "sfx/", "ui/", "player/", "hazards/", "items/", "boss/", "ambience/", "mario/"};

        for (String base : baseDirs) {
            for (String sub : subDirs) {
                File f = new File(base + "/" + sub + clean);
                if (f.isFile() && f.length() > 0) return f;
                if (!clean.toLowerCase().endsWith(".wav")) {
                    File fw = new File(base + "/" + sub + clean + ".wav");
                    if (fw.isFile() && fw.length() > 0) return fw;
                }
            }
        }
        return null;
    }

    /**
     * Kiểm tra nhanh xem file âm thanh có tồn tại trên máy hoặc trong classpath không.
     */
    public static boolean hasAudio(String relativeName) {
        if (findAudioFile(relativeName) != null) return true;
        String clean = relativeName.replace('\\', '/').trim();
        String[] resPaths = {
            "/assets/sound/bgm/" + clean,
            "/assets/sounds/bgm/" + clean,
            "/assets/sound/" + clean,
            "/sound/bgm/" + clean
        };
        for (String res : resPaths) {
            try (InputStream in = SoundManager.class.getResourceAsStream(res)) {
                if (in != null) return true;
            } catch (Exception ignored) {}
        }
        return false;
    }

    /**
     * Yêu cầu phát một bài nhạc nền (BGM) lặp vô hạn.
     * Chạy hoàn toàn ngầm trên background thread để không gây tụt FPS giao diện.
     */
    public static void playBgm(String trackName) {
        if (trackName == null || trackName.isBlank()) {
            stopBgm();
            return;
        }

        String clean = trackName.trim();
        synchronized (LOCK) {
            if (clean.equalsIgnoreCase(targetTrackName) && currentClip != null && (currentClip.isRunning() || isPaused)) {
                return; // Đang chạy hoặc pause đúng bài này, không xếp hàng nạp lại
            }
            targetTrackName = clean;
        }

        EXECUTOR.execute(() -> loadAndStartBgm(clean));
    }

    private static void loadAndStartBgm(String trackName) {
        synchronized (LOCK) {
            if (!trackName.equalsIgnoreCase(targetTrackName)) return;
        }

        File file = findAudioFile(trackName);
        if (file == null || !file.exists()) {
            // Không tìm thấy file âm thanh trên đĩa, dừng BGM hiện tại để tránh phát nhầm bài cũ
            stopBgm();
            return;
        }

        try {
            // Đóng clip cũ nếu có
            Clip oldClip;
            synchronized (LOCK) {
                oldClip = currentClip;
                currentClip = null;
            }
            if (oldClip != null) {
                try {
                    oldClip.stop();
                    oldClip.close();
                } catch (Exception ignored) {}
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(file);
            Clip newClip = AudioSystem.getClip();
            newClip.open(ais);
            ais.close(); // Giải phóng file stream sau khi đã nạp vào buffer

            synchronized (LOCK) {
                if (!trackName.equalsIgnoreCase(targetTrackName)) {
                    newClip.close();
                    return;
                }
                currentClip = newClip;
                currentTrackName = trackName;
                isPaused = false;
                applyVolume();
                newClip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception e) {
            System.err.println("[SoundManager] Không thể phát BGM " + trackName + ": " + e.getMessage());
        }
    }

    /**
     * Dừng BGM và giải phóng tài nguyên.
     */
    public static void stopBgm() {
        synchronized (LOCK) {
            targetTrackName = null;
            currentTrackName = null;
            isPaused = false;
            pausePositionUs = 0;
            if (currentClip != null) {
                try {
                    currentClip.stop();
                    currentClip.close();
                } catch (Exception ignored) {}
                currentClip = null;
            }
        }
        stopAmbience();
        stopWindSound();
        stopStreetSound();
        stopHelicopterSound();
    }

    /**
     * Tạm dừng BGM và âm thanh môi trường (lưu vị trí để tiếp tục sau).
     */
    public static void pauseBgm() {
        synchronized (LOCK) {
            if (currentClip != null && currentClip.isRunning()) {
                pausePositionUs = currentClip.getMicrosecondPosition();
                currentClip.stop();
                isPaused = true;
            }
            if (currentAmbienceClip != null && currentAmbienceClip.isRunning()) {
                pauseAmbiencePositionUs = currentAmbienceClip.getMicrosecondPosition();
                currentAmbienceClip.stop();
                isAmbiencePaused = true;
            }
            if (windClip != null && windClip.isRunning()) {
                windClip.stop();
                isWindPaused = true;
            }
            if (streetClip != null && streetClip.isRunning()) {
                streetClip.stop();
                isStreetPaused = true;
            }
            if (helicopterClip != null && helicopterClip.isRunning()) {
                helicopterClip.stop();
                isHelicopterPaused = true;
            }
        }
    }

    /**
     * Tiếp tục phát BGM và âm thanh môi trường từ vị trí tạm dừng.
     */
    public static void resumeBgm() {
        synchronized (LOCK) {
            if (isPaused && currentClip != null && !muted) {
                currentClip.setMicrosecondPosition(pausePositionUs);
                currentClip.start();
                currentClip.loop(Clip.LOOP_CONTINUOUSLY);
                isPaused = false;
            }
            if (isAmbiencePaused && currentAmbienceClip != null && !muted) {
                currentAmbienceClip.setMicrosecondPosition(pauseAmbiencePositionUs);
                currentAmbienceClip.start();
                currentAmbienceClip.loop(Clip.LOOP_CONTINUOUSLY);
                isAmbiencePaused = false;
            }
            if (isWindPaused && windClip != null && !muted) {
                windClip.start();
                isWindPaused = false;
            }
            if (isStreetPaused && streetClip != null && !muted) {
                streetClip.start();
                streetClip.loop(Clip.LOOP_CONTINUOUSLY);
                isStreetPaused = false;
            }
            if (isHelicopterPaused && helicopterClip != null && !muted) {
                helicopterClip.start();
                helicopterClip.loop(Clip.LOOP_CONTINUOUSLY);
                isHelicopterPaused = false;
            }
        }
    }

    /**
     * Cài đặt âm lượng tổng (từ 0.0f đến 1.0f).
     */
    public static void setVolume(float vol) {
        volume = Math.max(0.0f, Math.min(1.0f, vol));
        applyVolume();
    }

    public static float getVolume() {
        return volume;
    }

    public static void changeVolume(float delta) {
        setVolume(volume + delta);
    }

    /**
     * Bật / tắt chế độ im lặng (Mute).
     */
    public static void setMuted(boolean mute) {
        muted = mute;
        applyVolume();
        synchronized (LOCK) {
            if (currentClip != null) {
                if (muted && currentClip.isRunning()) {
                    currentClip.stop();
                } else if (!muted && isPaused) {
                    resumeBgm();
                }
            }
            if (currentAmbienceClip != null) {
                if (muted && currentAmbienceClip.isRunning()) {
                    currentAmbienceClip.stop();
                } else if (!muted && isAmbiencePaused) {
                    resumeBgm();
                }
            }
            if (windClip != null) {
                if (muted && windClip.isRunning()) {
                    windClip.stop();
                } else if (!muted && isWindPaused) {
                    resumeBgm();
                }
            }
            if (streetClip != null) {
                if (muted && streetClip.isRunning()) {
                    streetClip.stop();
                } else if (!muted && isStreetPaused) {
                    resumeBgm();
                }
            }
            if (helicopterClip != null) {
                if (muted && helicopterClip.isRunning()) {
                    helicopterClip.stop();
                } else if (!muted && isHelicopterPaused) {
                    resumeBgm();
                }
            }
        }
    }

    public static boolean isMuted() {
        return muted;
    }

    public static void toggleMute() {
        setMuted(!muted);
    }

    public static void setAmbienceVolumeFactor(float factor) {
        ambienceVolumeFactor = Math.max(0.0f, Math.min(1.0f, factor));
        applyVolume();
    }

    public static float getAmbienceVolumeFactor() {
        return ambienceVolumeFactor;
    }

    public static void setWindVolumeFactor(float factor) {
        windVolumeFactor = Math.max(0.0f, Math.min(1.0f, factor));
        applyVolume();
    }

    public static float getWindVolumeFactor() {
        return windVolumeFactor;
    }

    public static void setStreetVolumeFactor(float factor) {
        streetVolumeFactor = Math.max(0.0f, Math.min(1.0f, factor));
        applyVolume();
    }

    public static float getStreetVolumeFactor() {
        return streetVolumeFactor;
    }

    public static void setHelicopterVolumeFactor(float factor) {
        helicopterVolumeFactor = Math.max(0.0f, Math.min(1.0f, factor));
        applyVolume();
    }

    public static float getHelicopterVolumeFactor() {
        return helicopterVolumeFactor;
    }

    private static void applyVolume() {
        applyClipVolume(currentClip, volume);
        applyClipVolume(currentAmbienceClip, volume * ambienceVolumeFactor);
        applyClipVolume(windClip, volume * windVolumeFactor);
        applyClipVolume(streetClip, volume * streetVolumeFactor);
        applyClipVolume(helicopterClip, volume * helicopterVolumeFactor);
    }

    /**
     * Phát âm thanh đường phố kẹt xe (Màn 5: street.wav) lặp liên tục, âm lượng vừa phải.
     */
    public static void playStreetSound() {
        if (muted) return;
        synchronized (LOCK) {
            if (streetClip != null && (streetClip.isRunning() || isStreetPaused)) {
                return;
            }
        }
        EXECUTOR.execute(() -> {
            try {
                synchronized (LOCK) {
                    if (streetClip == null || !streetClip.isOpen()) {
                        File f = findAudioFile("street.wav");
                        if (f == null || !f.exists()) f = findAudioFile("amb_street.wav");
                        if (f != null && f.exists()) {
                            try (AudioInputStream ais = AudioSystem.getAudioInputStream(f)) {
                                streetClip = AudioSystem.getClip();
                                streetClip.open(ais);
                            }
                        }
                    }
                    if (streetClip != null && streetClip.isOpen()) {
                        applyClipVolume(streetClip, volume * streetVolumeFactor);
                        if (!streetClip.isRunning()) {
                            streetClip.setFramePosition(0);
                            streetClip.start();
                            streetClip.loop(Clip.LOOP_CONTINUOUSLY);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[SoundManager] Không thể phát tiếng đường phố: " + e.getMessage());
            }
        });
    }

    /**
     * Dừng âm thanh đường phố kẹt xe.
     */
    public static void stopStreetSound() {
        synchronized (LOCK) {
            isStreetPaused = false;
            if (streetClip != null && streetClip.isOpen()) {
                try {
                    streetClip.stop();
                } catch (Exception ignored) {}
            }
        }
    }

    /**
     * Phát âm thanh trực thăng cứu viện (Màn 5: helicopter.wav) lặp liên tục đến hết hội thoại, âm lượng vừa phải.
     */
    public static void playHelicopterSound() {
        if (muted) return;
        synchronized (LOCK) {
            if (helicopterClip != null && (helicopterClip.isRunning() || isHelicopterPaused)) {
                return;
            }
        }
        EXECUTOR.execute(() -> {
            try {
                synchronized (LOCK) {
                    if (helicopterClip == null || !helicopterClip.isOpen()) {
                        File f = findAudioFile("helicopter.wav");
                        if (f == null || !f.exists()) f = findAudioFile("amb_helicopter.wav");
                        if (f != null && f.exists()) {
                            try (AudioInputStream ais = AudioSystem.getAudioInputStream(f)) {
                                helicopterClip = AudioSystem.getClip();
                                helicopterClip.open(ais);
                            }
                        }
                    }
                    if (helicopterClip != null && helicopterClip.isOpen()) {
                        applyClipVolume(helicopterClip, volume * helicopterVolumeFactor);
                        if (!helicopterClip.isRunning()) {
                            helicopterClip.setFramePosition(0);
                            helicopterClip.start();
                            helicopterClip.loop(Clip.LOOP_CONTINUOUSLY);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[SoundManager] Không thể phát tiếng trực thăng: " + e.getMessage());
            }
        });
    }

    /**
     * Dừng âm thanh trực thăng cứu viện.
     */
    public static void stopHelicopterSound() {
        synchronized (LOCK) {
            isHelicopterPaused = false;
            if (helicopterClip != null && helicopterClip.isOpen()) {
                try {
                    helicopterClip.stop();
                } catch (Exception ignored) {}
            }
        }
    }

    /**
     * Phát tiếng gió mùa Đông Bắc cho Ngày 3 (lấy các giây đầu của winter_wind, âm lượng nhẹ nhàng hơn nhạc).
     */
    public static void playWindSound() {
        if (muted) return;
        EXECUTOR.execute(() -> {
            try {
                synchronized (LOCK) {
                    if (windClip == null || !windClip.isOpen()) {
                        File f = findAudioFile("amb_winter_wind.wav");
                        if (f == null || !f.exists()) f = findAudioFile("winter_wind.wav");
                        if (f != null && f.exists()) {
                            try (AudioInputStream ais = AudioSystem.getAudioInputStream(f)) {
                                windClip = AudioSystem.getClip();
                                windClip.open(ais);
                            }
                        }
                    }
                    if (windClip != null && windClip.isOpen()) {
                        applyClipVolume(windClip, volume * windVolumeFactor);
                        windClip.stop();
                        windClip.setFramePosition(0);
                        windClip.start();
                    }
                }
            } catch (Exception e) {
                System.err.println("[SoundManager] Không thể phát tiếng gió: " + e.getMessage());
            }
        });
    }

    /**
     * Dừng tiếng gió mùa Đông Bắc.
     */
    public static void stopWindSound() {
        synchronized (LOCK) {
            isWindPaused = false;
            if (windClip != null && windClip.isOpen()) {
                try {
                    windClip.stop();
                } catch (Exception ignored) {}
            }
        }
    }

    private static void applyClipVolume(Clip clip, float targetVol) {
        if (clip != null && clip.isOpen()) {
            try {
                if (clip.isControlSupported(BooleanControl.Type.MUTE)) {
                    BooleanControl muteControl = (BooleanControl) clip.getControl(BooleanControl.Type.MUTE);
                    muteControl.setValue(muted);
                }
                if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    if (muted || targetVol <= 0.001f) {
                        gain.setValue(gain.getMinimum());
                    } else {
                        float dB = (float) (Math.log10(targetVol) * 20.0);
                        dB = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB));
                        gain.setValue(dB);
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    private static final int BEEP_POOL_SIZE = 4;
    private static Clip[] dialogueBeepPool = null;
    private static int beepPoolIndex = 0;
    private static boolean beepPoolInitialized = false;

    // Bản đồ gán nhạc BGM riêng cho từng đoạn hội thoại theo từ khóa (section title hoặc nội dung)
    private static final Map<String, String> DIALOGUE_BGM_MAP = new ConcurrentHashMap<>();
    // Nhạc nền mặc định cho mọi đoạn hội thoại (tạm thời áp dụng toàn bộ file này):
    private static volatile String defaultDialogueBgm = "bgm_dialouge.wav";

    /**
     * Gán nhạc nền riêng cho một phân đoạn hội thoại cụ thể.
     * Ví dụ:
     *   SoundManager.setDialogueBgm("TẮC ĐƯỜNG", "bgm_day5_jam.wav");
     *   SoundManager.setDialogueBgm("SAU KHI THI", "bgm_exam_hall.wav");
     *   SoundManager.setDialogueBgm("SECRET ENDING", "bgm_ending_se.wav");
     */
    public static void setDialogueBgm(String keyword, String bgmFileName) {
        if (keyword != null && bgmFileName != null) {
            DIALOGUE_BGM_MAP.put(keyword.trim().toUpperCase(), bgmFileName.trim());
        }
    }

    /**
     * Đặt BGM mặc định cho toàn bộ các đoạn hội thoại nếu chưa gán nhạc riêng.
     */
    public static void setDefaultDialogueBgm(String bgmFileName) {
        if (bgmFileName != null && !bgmFileName.isBlank()) {
            defaultDialogueBgm = bgmFileName.trim();
        }
    }

    /**
     * Lấy bài BGM phù hợp cho đoạn hội thoại hiện tại.
     */
    public static String getDialogueBgm(GameState state) {
        if (state == null || state.lines == null || state.lines.isEmpty()) {
            return hasAudio(defaultDialogueBgm) ? defaultDialogueBgm : null;
        }

        int idx = Math.max(0, Math.min(state.dialogIndex, state.lines.size() - 1));

        // 1. Tìm tiêu đề phân cảnh gần nhất (dòng bắt đầu bằng @)
        String currentSection = "";
        for (int i = idx; i >= 0; i--) {
            sanbaka.model.Line line = state.lines.get(i);
            if (line.who != null && line.who.startsWith("@")) {
                currentSection = line.who.substring(1).trim().toUpperCase();
                break;
            }
        }

        // 2. Tra cứu cấu hình riêng trong DIALOGUE_BGM_MAP
        if (!currentSection.isEmpty()) {
            for (Map.Entry<String, String> entry : DIALOGUE_BGM_MAP.entrySet()) {
                if (currentSection.contains(entry.getKey())) {
                    String track = entry.getValue();
                    if (hasAudio(track)) return track;
                }
            }
        }

        // 3. Fallback về BGM mặc định cho hội thoại
        if (hasAudio(defaultDialogueBgm)) return defaultDialogueBgm;
        if (hasAudio("bgm_dialogue.wav")) return "bgm_dialogue.wav";
        if (hasAudio("bgm_dialouge.wav")) return "bgm_dialouge.wav";
        if (hasAudio("bgm_room_dialog.wav")) return "bgm_room_dialog.wav";

        return null;
    }

    /**
     * Lấy tên phân cảnh hội thoại hiện tại từ marker gần nhất (bắt đầu bằng @).
     */
    public static String getCurrentSection(GameState state) {
        if (state == null || state.lines == null || state.lines.isEmpty()) return "";
        int idx = Math.max(0, Math.min(state.dialogIndex, state.lines.size() - 1));
        for (int i = idx; i >= 0; i--) {
            sanbaka.model.Line line = state.lines.get(i);
            if (line.who != null && line.who.startsWith("@")) {
                if (line.who.startsWith("@Một lúc sau")) continue;
                return line.who.substring(1).trim().toUpperCase();
            }
        }
        return "";
    }

    /**
     * Kiểm tra xem trạng thái game hiện tại có phải là phân cảnh ngay sau khi thi Ngày 2 hay không.
     */
    public static boolean isDay2AfterExam(GameState state) {
        if (state == null) return false;
        if (state.screen == GameState.CARD) {
            return state.day == 1 && state.cardMain != null && state.cardMain.contains("SAU KHI THI");
        }
        String section = getCurrentSection(state);
        return section.contains("NGÀY 2") && section.contains("SAU KHI THI");
    }

    /**
     * Kiểm tra xem có đang ở phân cảnh hội thoại tắc đường (Màn 5) hay không.
     */
    public static boolean isTrafficJamSection(GameState state) {
        if (state == null || state.lines == null || state.lines.isEmpty()) return false;
        if (state.screen != GameState.DIALOG && state.screen != GameState.CARD && state.screen != GameState.AIR) {
            return false;
        }
        int idx = Math.max(0, Math.min(state.dialogIndex, state.lines.size() - 1));
        for (int i = idx; i >= 0; i--) {
            sanbaka.model.Line line = state.lines.get(i);
            if (line.who != null && line.who.startsWith("@")) {
                return line.who.contains("TẮC ĐƯỜNG");
            }
        }
        return false;
    }

    /**
     * Kiểm tra xem đã đến phân đoạn trực thăng xuất hiện hay chưa (từ câu cánh quạt đến hết hội thoại).
     */
    public static boolean isHelicopterSection(GameState state) {
        if (!isTrafficJamSection(state)) return false;
        // 1. Nếu đang ở màn hình chuyển cảnh AIR (trực thăng bay) hoặc CARD trực thăng
        if (state.screen == GameState.AIR) return true;
        if (state.screen == GameState.CARD && state.cardMain != null && state.cardMain.contains("trực thăng")) {
            return true;
        }
        // 2. Hoặc trong hội thoại, câu hiện tại hoặc câu đã qua có nhắc đến trực thăng
        int idx = Math.max(0, Math.min(state.dialogIndex, state.lines.size() - 1));
        for (int i = idx; i >= 0; i--) {
            sanbaka.model.Line line = state.lines.get(i);
            if (line.who != null && line.who.startsWith("@")) return false; // Không vượt quá marker phân cảnh
            if (line.text != null && (line.text.contains("cánh quạt trực thăng") || line.text.startsWith("…Cái méo gì thế?!"))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Phát hiệu ứng âm thanh ngắn (SFX) một lần.
     */
    public static void playSfx(String sfxName) {
        if (muted || sfxName == null || sfxName.isBlank()) return;
        EXECUTOR.execute(() -> {
            try {
                Clip clip = SFX_CACHE.computeIfAbsent(sfxName, name -> {
                    File file = findAudioFile(name);
                    if (file == null || !file.exists()) return null;
                    try {
                        AudioInputStream ais = AudioSystem.getAudioInputStream(file);
                        Clip c = AudioSystem.getClip();
                        c.open(ais);
                        ais.close();
                        return c;
                    } catch (Exception e) {
                        return null;
                    }
                });
                if (clip != null) {
                    clip.setFramePosition(0);
                    clip.start();
                }
            } catch (Exception ignored) {}
        });
    }

    /**
     * Phát tiếng beep gõ chữ retro (Undertale style) cho mỗi ký tự trong hội thoại.
     */
    public static void playDialogueBeep() {
        if (muted) return;
        EXECUTOR.execute(() -> {
            try {
                if (!beepPoolInitialized) {
                    beepPoolInitialized = true;
                    File f = findAudioFile("dialogue_beep.wav");
                    if (f != null && f.exists()) {
                        dialogueBeepPool = new Clip[BEEP_POOL_SIZE];
                        for (int i = 0; i < BEEP_POOL_SIZE; i++) {
                            try (AudioInputStream ais = AudioSystem.getAudioInputStream(f)) {
                                dialogueBeepPool[i] = AudioSystem.getClip();
                                dialogueBeepPool[i].open(ais);
                            }
                        }
                    }
                }
                if (dialogueBeepPool == null) return;

                Clip clip = dialogueBeepPool[beepPoolIndex];
                beepPoolIndex = (beepPoolIndex + 1) % BEEP_POOL_SIZE;

                if (clip != null && clip.isOpen()) {
                    if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                        float beepVol = Math.max(0.001f, volume * 0.7f);
                        float dB = (float) (Math.log10(beepVol) * 20.0);
                        dB = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB));
                        gain.setValue(dB);
                    }
                    clip.stop();
                    clip.setFramePosition(0);
                    clip.start();
                }
            } catch (Exception ignored) {}
        });
    }

    /**
     * Xác định bài BGM phù hợp nhất cho trạng thái hiện tại của GameState.
     */
    public static String determineBgm(GameState state) {
        if (state == null) return null;
        int screen = state.screen;

        // 1. Menu chính, Chọn màn, Hướng dẫn, Giới thiệu
        if (screen == GameState.TITLE || screen == GameMenuState.HOWTO || screen == GameMenuState.SCENES || screen == GameState.INTRO) {
            if (hasAudio("bgm_main_menu.wav")) return "bgm_main_menu.wav";
            if (hasAudio("bgm_title_menu.wav")) return "bgm_title_menu.wav";
            return null;
        }

        // 2. Màn hình Pause: giữ nguyên bản nhạc đang phát
        if (screen == GameMenuState.PAUSE) {
            return targetTrackName;
        }

        // 3. Kết thúc Bad Ending (Tạch môn / Hết máu / Tai nạn)
        if (screen == GameState.ENDING_BE) {
            return "bgm_ending_be.wav";
        }

        // 4. Kết thúc Happy Ending / Secret Ending
        if (screen == GameState.ENDING_HE) {
            return hasAudio("bgm_ending_he.wav") ? "bgm_ending_he.wav" : null;
        }
        if (screen == GameState.ENDING_SE) {
            return hasAudio("bgm_ending_se.wav") ? "bgm_ending_se.wav" : null;
        }

        // 5. Minigame Mario
        if (screen == GameState.MARIO) {
            return hasAudio("bgm_mario_8bit.wav") ? "bgm_mario_8bit.wav" : null;
        }

        // 6. Cảnh chuyển cảnh bay trực thăng
        if (screen == GameState.AIR) {
            return "bgm_day5_flight.wav";
        }

        // 7. Cảnh thi & Phòng trọ
        if (screen == GameState.EXAM) {
            return "bgm_exam_hall.wav";
        }
        if (screen == GameState.ROOM) {
            return getDialogueBgm(state);
        }

        // 8. Chơi chính trên đường (ROAD)
        if (screen == GameState.ROAD) {
            if (state.day == 4) { // Ngày 5: Môn thi cuối
                if (state.boss != null) {
                    return "bgm_day5_boss.wav";
                } else if (state.flightMode) {
                    return "bgm_day5_flight.wav";
                } else {
                    // Màn 5 đợt 1 (trên đường / tắc đường trước khi lên trực thăng)
                    if (hasAudio("bgm_day1+5.wav")) return "bgm_day1+5.wav";
                    if (hasAudio("bgm_day1_5.wav")) return "bgm_day1_5.wav";
                    return "bgm_day5_jam.wav";
                }
            } else if (state.day == 1) { // Ngày 2: Mưa lớn, đường ngập lụt
                if (hasAudio("bgm_day2.wav")) return "bgm_day2.wav";
                return "bgm_day2_flood.wav";
            } else if (state.day == 0) { // Ngày 1: Trời đẹp (Màn 1)
                if (hasAudio("bgm_day1+5.wav")) return "bgm_day1+5.wav";
                if (hasAudio("bgm_day1_5.wav")) return "bgm_day1_5.wav";
                return "bgm_day1_sunny.wav";
            } else if (state.day == 2) { // Ngày 3: Trời rét
                if (hasAudio("bgm_day3.wav")) return "bgm_day3.wav";
                return hasAudio("bgm_day3_cold.wav") ? "bgm_day3_cold.wav" : "bgm_day1_sunny.wav";
            } else if (state.day == 3) { // Ngày 4: Nắng nóng gay gắt
                return hasAudio("bgm_day4_hot.wav") ? "bgm_day4_hot.wav" : "bgm_day1_sunny.wav";
            }
            return "bgm_day1_sunny.wav";
        }

        // 9. Hội thoại (DIALOG)
        if (screen == GameState.DIALOG) {
            return getDialogueBgm(state);
        }

        // Khi vừa kết thúc chạy xe (FADE_END) hoặc hiện bảng thông báo (CARD) sau khi thi,
        // lập tức dừng nhạc đua xe (bgm_day2 / các bài road) và chuyển sang nhạc hội thoại.
        if (screen == GameState.FADE && state.fadeKind == GameMenuState.FADE_END) {
            return getDialogueBgm(state);
        }
        if (screen == GameState.CARD && state.cardMain != null && state.cardMain.contains("SAU KHI THI")) {
            return getDialogueBgm(state);
        }

        // Giữ nguyên track khi đang chuyển cảnh khác (FADE_JAM, FADE_ROOM, CARD khác)
        return targetTrackName;
    }

    /**
     * Tự động đồng bộ và chuyển BGM theo trạng thái game hiện tại.
     * Phương thức này thực thi cực nhanh (chỉ so sánh chuỗi) nên có thể gọi an toàn trong vòng lặp game.
     */
    public static void updateBgm(GameState state) {
        if (state == null) return;
        String appropriateTrack = determineBgm(state);
        if (appropriateTrack == null) {
            stopBgm();
        } else {
            playBgm(appropriateTrack);
        }

        // Cập nhật lớp âm thanh môi trường song song (tiếng mưa Ngày 2)
        updateAmbience(state);
    }

    /**
     * Phát âm thanh môi trường nền (Ambience) lặp vô hạn song song với BGM.
     */
    public static void playAmbience(String trackName) {
        if (trackName == null || trackName.isBlank()) {
            stopAmbience();
            return;
        }

        String clean = trackName.trim();
        synchronized (LOCK) {
            if (clean.equalsIgnoreCase(targetAmbienceName) && currentAmbienceClip != null && (currentAmbienceClip.isRunning() || isAmbiencePaused)) {
                return;
            }
            targetAmbienceName = clean;
        }

        EXECUTOR.execute(() -> loadAndStartAmbience(clean));
    }

    private static void loadAndStartAmbience(String trackName) {
        synchronized (LOCK) {
            if (!trackName.equalsIgnoreCase(targetAmbienceName)) return;
        }

        File file = findAudioFile(trackName);
        if (file == null || !file.exists()) {
            stopAmbience();
            return;
        }

        try {
            Clip oldClip;
            synchronized (LOCK) {
                oldClip = currentAmbienceClip;
                currentAmbienceClip = null;
            }
            if (oldClip != null) {
                try {
                    oldClip.stop();
                    oldClip.close();
                } catch (Exception ignored) {}
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(file);
            Clip newClip = AudioSystem.getClip();
            newClip.open(ais);
            ais.close();

            synchronized (LOCK) {
                if (!trackName.equalsIgnoreCase(targetAmbienceName)) {
                    newClip.close();
                    return;
                }
                currentAmbienceClip = newClip;
                currentAmbienceName = trackName;
                isAmbiencePaused = false;
                applyClipVolume(newClip, volume * ambienceVolumeFactor);
                newClip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception e) {
            System.err.println("[SoundManager] Không thể phát Ambience " + trackName + ": " + e.getMessage());
        }
    }

    /**
     * Dừng âm thanh môi trường nền.
     */
    public static void stopAmbience() {
        synchronized (LOCK) {
            targetAmbienceName = null;
            currentAmbienceName = null;
            isAmbiencePaused = false;
            pauseAmbiencePositionUs = 0;
            if (currentAmbienceClip != null) {
                try {
                    currentAmbienceClip.stop();
                    currentAmbienceClip.close();
                } catch (Exception ignored) {}
                currentAmbienceClip = null;
            }
        }
    }

    /**
     * Tự động điều khiển âm thanh môi trường theo ngày và trạng thái game.
     */
    public static void updateAmbience(GameState state) {
        if (state == null) {
            stopAmbience();
            stopStreetSound();
            stopHelicopterSound();
            return;
        }

        // 1. Màn 2 khi đang chơi trên đường (ROAD): Mưa to song song với nhạc nền
        if (state.screen == GameState.ROAD && state.day == 1) {
            setAmbienceVolumeFactor(0.45f);
            playAmbience("amb_rain_heavy.wav");
        } else {
            // 2. Phân cảnh ngay sau khi thi Ngày 2 (chuyển cảnh CARD hoặc hội thoại DIALOG/ROOM):
            // Tiếng mưa rơi nhẹ nhẹ bên ngoài hiên/hành lang C1
            boolean isDay2Transition = (state.day == 1 && state.screen == GameState.FADE && state.fadeKind == GameMenuState.FADE_END);
            if (isDay2Transition || isDay2AfterExam(state)) {
                if (state.screen == GameState.DIALOG || state.screen == GameState.ROOM || state.screen == GameState.CARD || isDay2Transition) {
                    setAmbienceVolumeFactor(0.22f); // Tiếng mưa nhẹ dịu (~22% âm lượng)
                    playAmbience("amb_rain_heavy.wav");
                } else {
                    stopAmbience();
                }
            } else {
                stopAmbience();
            }
        }

        // 3. Phân cảnh kẹt xe Màn 5: Phát tiếng đường phố street.wav
        if (isTrafficJamSection(state)) {
            playStreetSound();
            if (isHelicopterSection(state)) {
                // Khi đến câu trực thăng: thêm tiếng trực thăng helicopter.wav đến hết hội thoại
                playHelicopterSound();
            } else {
                stopHelicopterSound();
            }
        } else {
            stopStreetSound();
            stopHelicopterSound();
        }
    }
}
