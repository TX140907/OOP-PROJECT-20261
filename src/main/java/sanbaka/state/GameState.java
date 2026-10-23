package sanbaka.state;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;
import sanbaka.model.*;

/** Quản lý trạng thái chung, thông số game và vòng lặp. */
public abstract class GameState extends JPanel implements ActionListener, KeyListener {

    public static final boolean PERF_LOG_ENABLED = Boolean.getBoolean("sanbaka.perfLog");
    private long perfWindowStartNanos;
    private long perfPaintCount, perfUpdateCount;
    private long perfPaintTotalNanos, perfUpdateTotalNanos;
    private long perfPaintMaxNanos, perfUpdateMaxNanos;

    public double currentFps = 60.0;
    private long fpsTimerStart = System.currentTimeMillis();
    private int fpsFrameCount = 0;

    public void countFrame() {
        fpsFrameCount++;
        long now = System.currentTimeMillis();
        long diff = now - fpsTimerStart;
        if (diff >= 400) {
            currentFps = (fpsFrameCount * 1000.0) / Math.max(1, diff);
            fpsFrameCount = 0;
            fpsTimerStart = now;
        }
    }

    public void recordPerformance(boolean paint, long durationNanos) {
        if (!PERF_LOG_ENABLED) return;
        long now = System.nanoTime();
        if (perfWindowStartNanos == 0) perfWindowStartNanos = now;
        if (paint) {
            perfPaintCount++;
            perfPaintTotalNanos += durationNanos;
            perfPaintMaxNanos = Math.max(perfPaintMaxNanos, durationNanos);
        } else {
            perfUpdateCount++;
            perfUpdateTotalNanos += durationNanos;
            perfUpdateMaxNanos = Math.max(perfUpdateMaxNanos, durationNanos);
        }
        long windowNanos = now - perfWindowStartNanos;
        if (windowNanos >= 1_000_000_000L) {
            double seconds = windowNanos / 1_000_000_000.0;
            double fps = perfPaintCount / seconds;
            double avgPaintMs = perfPaintCount == 0 ? 0 : perfPaintTotalNanos / 1_000_000.0 / perfPaintCount;
            double avgUpdateMs = perfUpdateCount == 0 ? 0 : perfUpdateTotalNanos / 1_000_000.0 / perfUpdateCount;
            perfWindowStartNanos = now;
            perfPaintCount = perfUpdateCount = 0;
            perfPaintTotalNanos = perfUpdateTotalNanos = 0;
            perfPaintMaxNanos = perfUpdateMaxNanos = 0;
        }
    }

    public static final int W = 960;
    public static final int H = 640;
    public static final int ROAD_X = 205;
    public static final int ROAD_W = 550;
    public static final int TRACKS = 5;
    public static final int ROAD_DURATION_MS = 40_000;
    public static final int HOT_DAY_DURATION_MS = 35_000;

    public int getStageDurationMs() {
        return day == HOT_DAY ? HOT_DAY_DURATION_MS : ROAD_DURATION_MS;
    }

    public static final double PLAYER_SPEED = 260.0;
    public static final double VERTICAL_SPEED = 240.0;
    public static final Color INK = new Color(35, 42, 60), CREAM = new Color(250, 247, 239), PINK = new Color(237, 119, 155), MINT = new Color(94, 181, 154);
    public final javax.swing.Timer timer = new javax.swing.Timer(15, this);
    public final java.util.List<Line> lines = new ArrayList<>();
    public final Random rng = new Random();
    public static final int TITLE = 0;
    public static final int DIALOG = 1;
    public static final int ROAD = 2;
    public static final int ROOM = 3;
    public static final int EXAM = 4;
    public static final int ENDING_HE = 5;
    public static final int ENDING_BE = 6;
    public static final int AIR = 7;
    public java.awt.image.BufferedImage dialogBackground;
    public int screen = TITLE;
    public int dialogIndex = 0;
    private static final int TYPEWRITER_CHARS_PER_SECOND = 42;
    private int typedDialogIndex = -1;
    private long typedDialogStartedAt;
    public int day = 0;
    public PlayerVehicle player = new PlayerBike();
    public int hp = 100;
    public int score = 0;
    public boolean trafficSceneShown = false;
    public boolean airSceneShown = false;
    public boolean roadPausedForTraffic = false;
    public long airSceneStarted = 0;
    public long roadPausedAt = 0;
    public long lastSpawn = 0;
    public long stageStarted = 0;
    public long elapsed = 0;
    public long lastFrameAt = 0;
    public double roadOffset = 0;
    public boolean left = false, right = false, up = false, down = false, keyE = false;
    // ---- trực thăng: ngày 5, 2/3 đường sau ----
    public static final int FLIGHT_START_HP = 150;
    public boolean flightMode = false;
    public double heliX = 170, heliY = 300, heliTilt = 0;
    public double skyScroll = 0;
    public long lastBird = 0;
    public final java.util.List<Bird> birds = new ArrayList<>();
    // ---- khung chat ----
    public int lastChatIndex = -1;
    public long chatAnimStart = 0;
    // ---- đường bộ: cảnh vật, tắc đường, năng lượng ngày nắng nóng ----
    public static final int HOT_DAY = 3;
    public static final double ENERGY_DRAIN = 3.5;
    public static final double BOLT_ENERGY = 27;
    public static final int BOLT_HP = 5;
    public static final double JAM_WINDOW = 3.6;
    public static final double SCHOOL_APPROACH = 3.5;
    public static final double FINISH_Y = 335;
    public double energy = 100;
    public double groundScroll = 0;
    public long lastBolt = 0;
    public long lastHitAt = 0;
    public boolean jamBuilt = false;
    public int retryDialogIndex = 0;
    public String beCause = "";
    // ---- Màn 3 (Trời rét, đường trơn: quán tính, vũng băng, gió mùa & đóng băng 70% tốc độ tiến, trà gừng) ----
    public static final int COLD_DAY = 2;
    public double playerVx = 0;
    public long iceSlipUntil = 0;
    public int windDir = 0;
    public long windWarningUntil = 0;
    public long windActiveUntil = 0;
    public long nextWindAt = 0;
    public long frozenUntil = 0;
    public boolean windFreezeChecked = false;
    public int teaSpawnCount = 0;
    public long lastTeaSpawn = 0;
    public long warmUntil = 0;
    // ---- Màn 1 (Trời đẹp: sinh viên băng qua đường) ----
    public long lastStudentSpawn = 0;
    public int studentSpawnCount = 0;
    // ---- Màn 4 (Nắng nóng: chó chạy rông & mệt mỏi khi Energy thấp, khiên bảo vệ 1 lần va chạm) ----
    public long lastDogSpawn = 0;
    public boolean shieldActive = false;
    public int shieldSpawnCount = 0;
    public long lastShieldSpawn = 0;
    // ---- Hệ thống chữ nổi Floating Combat Text cấu hình qua floating_texts.json ----
    public sanbaka.render.FloatingTextManager floatingTextManager = new sanbaka.render.FloatingTextManager();
    // ---- Cơ chế Quãng đường cố định (40s nếu đi thuận lợi), Bốc lửa lề đường & Đẩy lùi/Làm chậm khi va chạm ----
    public double stageProgressMs = 0.0;
    public double currentProgressFactor = 1.0;
    public static final double CURB_ZONE_MARGIN = 26.0;
    public static final double CURB_FIRE_THRESHOLD_SEC = 3.0;
    public static final double CURB_BURN_DPS = 10.0;
    public double curbEdgeTime = 0.0;
    public double curbBurnFraction = 0.0;
    public boolean curbOnFire = false;
    public double map2CurbSlowFactor = 1.0;
    public long hitSlowUntil = 0;
    public double hitSlowFactor = 1.0;
    public double knockbackVy = 0.0;
    public double knockbackVx = 0.0;
    // ---- Màn 5: Boss Trận Không Chiến & Tên Lửa Trực Thăng (phím E) ----
    public BossEnemy boss = null;
    public java.util.List<PlayerMissile> missiles = new ArrayList<>();
    public java.util.List<EnemyPlane> enemyPlanes = new ArrayList<>();
    public java.util.List<EnemyBomb> enemyBombs = new ArrayList<>();
    public java.util.List<BossBullet> bossBullets = new ArrayList<>();
    public java.util.List<BombExplosion> explosions = new ArrayList<>();
    public long lastMissileFiredAt = 0;
    public long flightStartedAt = 0;
    public boolean flightBossTriggered = false;
    // ---- chuyển cảnh ----
    public static final int FADE = 8, CARD = 9, INTRO = 10;
    public static final double PLAYER_MIN_X = ROAD_X + 45 + 22;
    public static final double PLAYER_MAX_X = ROAD_X + ROAD_W - 45 - 22;
    public int callStart = -1, callEnd = -1;
    public long callStartedAt = 0;
    public static final int FADE_JAM = 0, FADE_END = 1;
    public static final long FADE_MS = 1100, CARD_MS = 2600;
    public int fadeKind = FADE_END;
    public long fadeStart = 0, cardStart = 0, fadeInStart = 0;
    public String cardTop = "", cardMain = "";
    public boolean cardNarration = false;
    public int prevScreen = -1;
    // ---- pháo giấy (HE) ----
    public final java.util.List<Confetti> confetti = new ArrayList<>();
    public final java.util.List<double[]> pops = new ArrayList<>();
    public long lastBurst = 0, lastConfettiAt = 0;
    public java.util.List<Thing> things = new ArrayList<>();
    public String[] dayTitles = {
        "NGÀY 1 · TRỜI ĐẸP", "NGÀY 2 · MƯA LỚN, ĐƯỜNG NGẬP", "NGÀY 3 · TRỜI RÉT, ĐƯỜNG TRƠN", "NGÀY 4 · NẮNG NÓNG", "NGÀY 5 · NGÀY THI CUỐI"
    };
    public Color[] skies = {
        new Color(148, 205, 240), new Color(94, 119, 151), new Color(186, 202, 218), new Color(255, 194, 117), new Color(175, 202, 226)
    };

    public static final double[] VEH_W = {50, 48, 50, 54, 58, 60, 26, 30, 40, 136, 68, 76, 28, 68, 88, 48, 34, 36, 32};
    public static final double[] VEH_H = {86, 76, 86, 100, 132, 168, 60, 40, 84, 172, 36, 48, 62, 68, 54, 30, 34, 36, 38};
    public static final Color[] CAR_COLORS = {
        new Color(219, 93, 96), new Color(81, 146, 185), new Color(240, 242, 246), new Color(176, 184, 196),
        new Color(240, 200, 90), new Color(94, 170, 128), new Color(150, 110, 180), new Color(58, 62, 76)
    };

    public abstract void beginDay();
    public abstract void resumeRoad();

    public String visibleDialogText(String fullText) {
        if (dialogIndex < 0 || dialogIndex >= lines.size()) return fullText;
        if (typedDialogIndex != dialogIndex) {
            typedDialogIndex = dialogIndex;
            typedDialogStartedAt = System.currentTimeMillis();
        }
        long elapsed = Math.max(0, System.currentTimeMillis() - typedDialogStartedAt);
        int count = Math.min(fullText.length(), (int) (elapsed * TYPEWRITER_CHARS_PER_SECOND / 1000));
        return fullText.substring(0, count);
    }

    public boolean revealDialogImmediatelyIfTyping() {
        if (dialogIndex < 0 || dialogIndex >= lines.size()) return false;
        String fullText = lines.get(dialogIndex).text;
        if (typedDialogIndex != dialogIndex) {
            typedDialogIndex = dialogIndex;
            typedDialogStartedAt = System.currentTimeMillis();
        }
        long revealDuration = (fullText.length() * 1000L + TYPEWRITER_CHARS_PER_SECOND - 1)
                / TYPEWRITER_CHARS_PER_SECOND;
        if (System.currentTimeMillis() - typedDialogStartedAt >= revealDuration) return false;
        typedDialogStartedAt = System.currentTimeMillis() - revealDuration;
        repaint();
        return true;
    }

    public void resetDialogTyping() {
        typedDialogIndex = -1;
        typedDialogStartedAt = 0;
    }
}

