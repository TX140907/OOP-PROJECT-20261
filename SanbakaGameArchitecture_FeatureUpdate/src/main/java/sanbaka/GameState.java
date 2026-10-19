package sanbaka;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;

/** Sanbaka: Tuan Thi — a self-contained Vietnamese story runner. */
abstract class GameState extends JPanel implements ActionListener, KeyListener {

    static final boolean PERF_LOG_ENABLED = Boolean.getBoolean("sanbaka.perfLog");
    private long perfWindowStartNanos;
    private long perfPaintCount, perfUpdateCount;
    private long perfPaintTotalNanos, perfUpdateTotalNanos;
    private long perfPaintMaxNanos, perfUpdateMaxNanos;

    void recordPerformance(boolean paint, long durationNanos) {
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
            System.out.printf(java.util.Locale.ROOT,
                    "[perf] screen=%d fps=%.1f paint avg/max=%.2f/%.2f ms update avg/max=%.2f/%.2f ms%n",
                    screen, fps, avgPaintMs, perfPaintMaxNanos / 1_000_000.0,
                    avgUpdateMs, perfUpdateMaxNanos / 1_000_000.0);
            perfWindowStartNanos = now;
            perfPaintCount = perfUpdateCount = 0;
            perfPaintTotalNanos = perfUpdateTotalNanos = 0;
            perfPaintMaxNanos = perfUpdateMaxNanos = 0;
        }
    }

    static final int W = 960;
    static final int H = 640;
    static final int ROAD_X = 205;
    static final int ROAD_W = 550;
    static final int TRACKS = 5;
    static final int ROAD_DURATION_MS = 40_000;
    static final double PLAYER_SPEED = 260.0;
    static final double VERTICAL_SPEED = 240.0;
    static final Color INK=new Color(35,42,60), CREAM=new Color(250,247,239), PINK=new Color(237,119,155), MINT=new Color(94,181,154);
    final javax.swing.Timer timer=new javax.swing.Timer(16,this);
    final java.util.List<Line> lines=new ArrayList<>();
    final Random rng=new Random();
    static final int TITLE = 0;
    static final int DIALOG = 1;
    static final int ROAD = 2;
    static final int ROOM = 3;
    static final int EXAM = 4;
    static final int ENDING_HE = 5;
    static final int ENDING_BE = 6;
    static final int AIR = 7;
    java.awt.image.BufferedImage dialogBackground;
    int screen = TITLE;
    int dialogIndex = 0;
    int day = 0;
    PlayerVehicle player = new PlayerBike();
    int hp = 100;
    int score = 0;
    boolean trafficSceneShown = false;
    boolean airSceneShown = false;
    boolean roadPausedForTraffic = false;
    long airSceneStarted = 0;
    long roadPausedAt = 0;
    long lastSpawn = 0;
    long stageStarted = 0;
    long elapsed = 0;
    long lastFrameAt = 0;
    double roadOffset = 0;
    boolean left=false,right=false,up=false,down=false;
    // ---- trực thăng: ngày 5, 2/3 đường sau ----
    static final int FLIGHT_START_HP = 100;   // năng lượng khi bắt đầu bay (đổi nếu muốn khó/dễ hơn)
    boolean flightMode = false;
    double heliX = 170, heliY = 300, heliTilt = 0;
    double skyScroll = 0;
    long lastBird = 0;
    final java.util.List<Bird> birds = new ArrayList<>();
    // ---- khung chat ----
    int lastChatIndex = -1;
    long chatAnimStart = 0;
    // ---- đường bộ: cảnh vật, tắc đường, năng lượng ngày nắng nóng ----
    static final int HOT_DAY = 3;                  // ngày 4: nắng nóng
    static final double ENERGY_DRAIN = 3.5;        // năng lượng tự mất mỗi giây khi trời nóng
    static final double BOLT_ENERGY = 20;          // mỗi tia sét hồi bao nhiêu
    static final double JAM_WINDOW = 3.6;          // giây dựng cảnh tắc đường trước mốc 1/3 quãng đường (ngày 5)
    static final double SCHOOL_APPROACH = 3.5;     // giây cuối: đường chậm dần, trường học hiện ra
    static final double FINISH_Y = 335;            // vị trí vạch đích khi kết thúc
    double energy = 100;
    double groundScroll = 0;
    long lastBolt = 0;
    long lastHitAt = 0;
    boolean jamBuilt = false;
    int retryDialogIndex = 0;
    String beCause = "";
    // ---- chuyển cảnh ----
    static final int FADE = 8, CARD = 9, INTRO = 10;
    static final double PLAYER_MIN_X = ROAD_X + 45 + 22;                 // người chơi chỉ đi trong lòng đường
    static final double PLAYER_MAX_X = ROAD_X + ROAD_W - 45 - 22;
    int callStart = -1, callEnd = -1;      // phạm vi các dòng đang nói chuyện điện thoại
    long callStartedAt = 0;
    static final int FADE_JAM = 0, FADE_END = 1;
    static final long FADE_MS = 1100, CARD_MS = 2600;
    int fadeKind = FADE_END;
    long fadeStart = 0, cardStart = 0, fadeInStart = 0;
    String cardTop = "", cardMain = "";
    boolean cardNarration = false;
    int prevScreen = -1;
    // ---- pháo giấy (HE) ----
    final java.util.List<Confetti> confetti = new ArrayList<>();
    final java.util.List<double[]> pops = new ArrayList<>();
    long lastBurst = 0, lastConfettiAt = 0;
    java.util.List<Thing> things=new ArrayList<>();
    String[] dayTitles= {
        "NGÀY 1 · TRỜI ĐẸP","NGÀY 2 · MƯA LỚN, ĐƯỜNG NGẬP","NGÀY 3 · TRỜI RÉT, ĐƯỜNG TRƠN","NGÀY 4 · NẮNG NÓNG","NGÀY 5 · NGÀY THI CUỐI"
    };
    Color[] skies= {
        new Color(148,205,240),new Color(94,119,151),new Color(186,202,218),new Color(255,194,117),new Color(175,202,226)
    };

    static final double[] VEH_W = {50, 48, 50, 54, 58, 60, 26, 30, 40};
    static final double[] VEH_H = {86, 76, 86, 100, 132, 168, 60, 40, 84};
    static final Color[] CAR_COLORS = {
        new Color(219, 93, 96), new Color(81, 146, 185), new Color(240, 242, 246), new Color(176, 184, 196),
        new Color(240, 200, 90), new Color(94, 170, 128), new Color(150, 110, 180), new Color(58, 62, 76)
    };
    abstract void beginDay();
    abstract void resumeRoad();
}
