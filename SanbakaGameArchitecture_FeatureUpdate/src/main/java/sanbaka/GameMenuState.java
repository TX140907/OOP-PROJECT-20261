package sanbaka;

import java.awt.*;
import java.util.*;

/** Menu, điều hướng và tiến trình mở khóa cảnh. */
abstract class GameMenuState extends GameState {
    Font titleFont = new Font("SansSerif", Font.BOLD, 42);
    static final boolean UNLOCK_ALL = true;      // true: mở khóa mọi cảnh và vào thẳng menu chọn cảnh để test
    static final int SCENES = 11, HOWTO = 12, PAUSE = 13;
    static final int SC_ROOM = 0, SC_JAM = 1, SC_AFTER = 2;
    static final int CARD_TO_DIALOG = 0, CARD_TO_ROOM = 1, CARD_TO_AIR = 2;
    static final int FADE_ROOM = 2;
    static final java.io.File SAVE_FILE = new java.io.File(System.getProperty("user.home"), ".sanbaka_svbk_progress.properties");

    static class Scene {
        int dayIdx, type, index, marker;
        String name;
    }
    static class Btn {
        String id;
        double x, y, w, h;
        int menuIndex;
    }
    final java.util.List<Scene> scenes = new ArrayList<>();
    final java.util.List<Btn> btns = new ArrayList<>();
    int maxScene = -1;           // cảnh xa nhất đã mở khóa
    boolean won = false;         // đã thắng game (HE) chưa
    int cardNext = CARD_TO_DIALOG;
    int fadeDialogIndex = 0;
    int mouseX = -1, mouseY = -1, menuSel = 0, backTarget = TITLE;
    boolean menuKeyboardActive = false;
    boolean menuForegroundOnly = false;
    int menuTransitionFrom = -1;
    int menuTransitionTo = -1;
    long menuTransitionStart = 0;
    static final long MENU_TRANSITION_MS = 520;
    long pauseStart = 0;

    int indexOfMarker(String marker) {
        for (int i = 0; i < lines.size(); i++) if (lines.get(i).who.equals(marker)) return i;
        throw new IllegalArgumentException("Không tìm thấy " + marker);
    }
    void buildScenes() {
        scenes.clear();
        for (int d = 1; d <= 5; d++) {
            Scene r = new Scene();
            r.dayIdx = d - 1;
            r.type = SC_ROOM;
            r.name = "Ở trọ";
            r.index = indexOfMarker("@NGÀY " + d + " · TẠI PHÒNG TRỌ");
            scenes.add(r);
            if (d == 5) {
                Scene j = new Scene();
                j.dayIdx = 4;
                j.type = SC_JAM;
                j.name = "Tắc đường";
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).who.startsWith("@TRÊN ĐƯỜNG")) j.marker = i;
                    if (lines.get(i).text.startsWith("5 phút sau")) j.index = i;
                }
                scenes.add(j);
            }
            Scene a = new Scene();
            a.dayIdx = d - 1;
            a.type = SC_AFTER;
            a.name = "Sau khi thi";
            a.index = indexOfMarker("@NGÀY " + d + " · SAU KHI THI");
            scenes.add(a);
        }
    }
    int sceneIndex(int type, int dayIdx) {
        for (int i = 0; i < scenes.size(); i++) {
            if (scenes.get(i).type == type && scenes.get(i).dayIdx == dayIdx) return i;
        }
        return -1;
    }
    boolean sceneUnlocked(int i) {
        return UNLOCK_ALL || i <= maxScene;
    }
    void unlockScene(int type, int dayIdx) {
        int i = sceneIndex(type, dayIdx);
        if (i > maxScene) {
            maxScene = i;
            saveProgress();
        }
    }
    void loadProgress() {
        try (java.io.InputStream in = new java.io.FileInputStream(SAVE_FILE)) {
            Properties p = new Properties();
            p.load(in);
            maxScene = Math.max(-1, Math.min(scenes.size() - 1, Integer.parseInt(p.getProperty("maxScene", "-1").trim())));
            won = Boolean.parseBoolean(p.getProperty("won", "false").trim());
        } catch (Exception e) {
            // chưa có tiến trình đã lưu
        }
    }
    void saveProgress() {
        try (java.io.OutputStream out = new java.io.FileOutputStream(SAVE_FILE)) {
            Properties p = new Properties();
            p.setProperty("maxScene", String.valueOf(maxScene));
            p.setProperty("won", String.valueOf(won));
            p.store(out, "Tuan thi cua SVBK - tien trinh choi");
        } catch (Exception e) {
            // không ghi được thì bỏ qua
        }
    }
    String sceneLabel(int i) {
        Scene s = scenes.get(i);
        return "Ngày " + (s.dayIdx + 1) + " · " + s.name;
    }
    String playSubtitle() {
        if (won) return "Đã thắng · chơi lại từ đầu";
        if (maxScene < 0) return "Bắt đầu cuộc hành trình";
        return "Tiếp tục: " + sceneLabel(maxScene);
    }
    /** "Vào chơi": chưa thắng thì tiếp tục từ cảnh xa nhất đã mở khóa, thắng rồi thì chơi lại từ đầu. */
    void playPressed() {
        if (won || maxScene < 0) newGame();
        else startScene(maxScene);
    }
    void resetRunState() {
        left = right = up = down = false;
        flightMode = false;
        roadPausedForTraffic = false;
        trafficSceneShown = false;
        airSceneShown = false;
        things.clear();
        birds.clear();
        cardNarration = false;
        cardNext = CARD_TO_DIALOG;
    }
    void newGame() {
        resetRunState();
        day = 0;
        dialogIndex = 0;
        screen = DIALOG;
        fadeInStart = System.currentTimeMillis();
    }
    /** Bắt đầu một cảnh: màn hình đen in tên cảnh, rồi vào cảnh. */
    void startScene(int i) {
        Scene s = scenes.get(i);
        long now = System.currentTimeMillis();
        resetRunState();
        day = s.dayIdx;
        cardStart = now;
        cardTop = "NGÀY " + (s.dayIdx + 1);
        if (s.type == SC_ROOM) {
            dialogIndex = s.index;
            cardMain = "Ở TRỌ";
            cardNext = CARD_TO_ROOM;
        } else if (s.type == SC_AFTER) {
            dialogIndex = s.index + 1;
            cardMain = "SAU KHI THI";
        } else {
            retryDialogIndex = s.marker;
            dialogIndex = s.index;
            cardTop = "";
            cardMain = lines.get(s.index).text;
            cardNarration = true;
            roadPausedForTraffic = true;
            trafficSceneShown = true;
            roadPausedAt = now;
            stageStarted = now - ROAD_DURATION_MS / 3;
            callStartedAt = 0;
            hp = 100;
            elapsed = ROAD_DURATION_MS / 3;
            unlockScene(SC_JAM, 4);
        }
        screen = CARD;
    }
    void pauseGame() {
        if (screen != ROAD) return;
        pauseStart = System.currentTimeMillis();
        screen = PAUSE;
        menuSel = 0;
        menuKeyboardActive = false;
    }
    void resumeFromPause() {
        long now = System.currentTimeMillis();
        long d = now - pauseStart;
        stageStarted += d;
        lastSpawn += d;
        lastBolt += d;
        lastBird += d;
        if (player.invulnerable > 0) player.invulnerable += d;
        if (lastHitAt > 0) lastHitAt += d;
        lastFrameAt = now;
        screen = ROAD;
        requestFocusInWindow();
    }
    void handleEscape() {
        if (screen == ROAD) pauseGame();
        else if (screen == PAUSE) resumeFromPause();
        else if (screen == SCENES || screen == HOWTO || screen == INTRO) {
            returnFromMenuPage();
        }
    }
    void activate(String id) {
        if (menuTransitionTo >= 0) return;
        if (id.startsWith("scene:")) {
            startScene(Integer.parseInt(id.substring(6)));
            return;
        }
        switch (id) {
            case "play": playPressed(); break;
            case "scenes": openMenuPage(SCENES); break;
            case "chars": openMenuPage(INTRO); break;
            case "howto": openMenuPage(HOWTO); break;
            case "back": returnFromMenuPage(); break;
            case "pause": pauseGame(); break;
            case "resume": resumeFromPause(); break;
            case "main": screen = TITLE; menuSel = 0; dialogIndex = 0; break;
            default: break;
        }
    }
    void openMenuPage(int target) {
        backTarget = screen;
        menuSel = 0;
        menuKeyboardActive = false;
        if (screen == TITLE) {
            menuTransitionFrom = TITLE;
            menuTransitionTo = target;
            menuTransitionStart = System.currentTimeMillis();
        } else {
            screen = target;
        }
    }
    void returnFromMenuPage() {
        menuSel = 0;
        menuKeyboardActive = false;
        if (backTarget == TITLE && (screen == SCENES || screen == INTRO || screen == HOWTO)) {
            menuTransitionFrom = screen;
            menuTransitionTo = TITLE;
            menuTransitionStart = System.currentTimeMillis();
        } else {
            screen = backTarget;
        }
    }
    void activateMenuSel() {
        String[] ids = screen == PAUSE
                ? new String[]{"resume", "scenes", "howto", "main"}
                : new String[]{"play", "scenes", "chars", "howto"};
        activate(ids[Math.max(0, Math.min(ids.length - 1, menuSel))]);
    }
    void activateHoveredMenuButton() {
        for (int i = btns.size() - 1; i >= 0; i--) {
            Btn b = btns.get(i);
            if (b.menuIndex >= 0 && mouseX >= b.x && mouseX <= b.x + b.w
                    && mouseY >= b.y && mouseY <= b.y + b.h) {
                activate(b.id);
                return;
            }
        }
    }
    void handleClick(int x, int y) {
        if (menuTransitionTo >= 0) return;
        for (int i = btns.size() - 1; i >= 0; i--) {
            Btn b = btns.get(i);
            if (x >= b.x && x <= b.x + b.w && y >= b.y && y <= b.y + b.h) {
                activate(b.id);
                return;
            }
        }
    }
    void onMouse(int x, int y) {
        mouseX = x;
        mouseY = y;
        menuKeyboardActive = false;
        boolean over = false;
        for (Btn b : btns) {
            if (x >= b.x && x <= b.x + b.w && y >= b.y && y <= b.y + b.h) {
                over = true;
                if (b.menuIndex >= 0) menuSel = b.menuIndex;
            }
        }
        setCursor(Cursor.getPredefinedCursor(over ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    // ---------- vẽ nút bấm ----------
    Btn addBtn(String id, double x, double y, double w, double h, int menuIndex) {
        Btn b = new Btn();
        b.id = id;
        b.x = x;
        b.y = y;
        b.w = w;
        b.h = h;
        b.menuIndex = menuIndex;
        btns.add(b);
        return b;
    }
    boolean isHover(double x, double y, double w, double h) {
        return !menuForegroundOnly && mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

}

