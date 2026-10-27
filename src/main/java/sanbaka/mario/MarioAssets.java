package sanbaka.mario;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.HashMap;
import java.util.Map;

/**
 * Quản lý toàn bộ tài nguyên hình ảnh (Sprites/Tiles) của minigame Mario 8-bit.
 * Tự động sinh đồ họa pixel-art 16x16 chuẩn màu NES nguyên bản trong bộ nhớ (không cần cài thêm file),
 * đồng thời hỗ trợ đọc đè từ thư mục assets/mario/ để người dùng có thể thay thế toàn bộ nhân vật,
 * quái vật và địa hình theo ý muốn.
 */
public final class MarioAssets {
    private static final Map<String, BufferedImage> cache = new HashMap<>();

    // Bảng màu chuẩn NES Super Mario Bros 1985
    public static final Color NES_SKY = new Color(0x5C, 0x94, 0xFC);        // Xanh da trời NES
    public static final Color COLOR_RED = new Color(0xB8, 0x38, 0x00);      // Đỏ áo/mũ Mario
    public static final Color COLOR_BROWN = new Color(0x88, 0x70, 0x00);    // Nâu quần yếm/tóc Mario
    public static final Color COLOR_SKIN = new Color(0xFC, 0xE0, 0xA8);     // Màu da/mặt Mario
    public static final Color COLOR_DARK_BROWN = new Color(0x50, 0x30, 0x00);
    public static final Color COLOR_BRICK = new Color(0xB8, 0x44, 0x18);
    public static final Color COLOR_BRICK_DARK = new Color(0x00, 0x00, 0x00);
    public static final Color COLOR_QUESTION_GOLD = new Color(0xFC, 0x98, 0x38);
    public static final Color COLOR_QUESTION_SHINE = new Color(0xFC, 0xD8, 0xA8);
    public static final Color COLOR_QUESTION_SHADOW = new Color(0x80, 0x40, 0x00);
    public static final Color COLOR_PIPE_LIGHT = new Color(0x00, 0xA8, 0x00);
    public static final Color COLOR_PIPE_DARK = new Color(0x00, 0x58, 0x00);
    public static final Color COLOR_PIPE_HIGHLIGHT = new Color(0x80, 0xD0, 0x10);
    public static final Color COLOR_GOOMBA_BODY = new Color(0xA8, 0x40, 0x00);
    public static final Color COLOR_GOOMBA_FEET = new Color(0x00, 0x00, 0x00);
    public static final Color COLOR_GOOMBA_EYES = new Color(0xFC, 0xE0, 0xA8);

    static {
        loadAll();
    }

    public static void loadAll() {
        cache.clear();
        // 1. Tự sinh toàn bộ sprite pixel-art 16x16 mặc định
        generateDefaultMarioSprites();
        generateDefaultBigMarioSprites();
        generateDefaultGoombaSprites();
        generateDefaultTiles();

        // 2. Kiểm tra nạp đè từ thư mục assets/mario nếu có file ảnh người dùng thả vào
        loadOverridesFromDisk();
    }

    public static BufferedImage get(String key) {
        return cache.get(key);
    }

    private static void loadOverridesFromDisk() {
        File baseDir = new File("assets/mario");
        if (!baseDir.isDirectory()) {
            baseDir.mkdirs();
            new File(baseDir, "player").mkdirs();
            new File(baseDir, "enemies").mkdirs();
            new File(baseDir, "tiles").mkdirs();
            return;
        }

        loadOverrideImage(new File(baseDir, "player/idle.png"), "mario_idle");
        loadOverrideImage(new File(baseDir, "player/walk_1.png"), "mario_walk_1");
        loadOverrideImage(new File(baseDir, "player/walk_2.png"), "mario_walk_2");
        loadOverrideImage(new File(baseDir, "player/walk_3.png"), "mario_walk_3");
        loadOverrideImage(new File(baseDir, "player/jump.png"), "mario_jump");
        loadOverrideImage(new File(baseDir, "player/skid.png"), "mario_skid");
        loadOverrideImage(new File(baseDir, "player/die.png"), "mario_die");
        loadOverrideImage(new File(baseDir, "player/flag.png"), "mario_flag");
        loadOverrideImage(new File(baseDir, "player/big_idle.png"), "mario_big_idle");
        loadOverrideImage(new File(baseDir, "player/big_walk_1.png"), "mario_big_walk_1");
        loadOverrideImage(new File(baseDir, "player/big_walk_2.png"), "mario_big_walk_2");
        loadOverrideImage(new File(baseDir, "player/big_walk_3.png"), "mario_big_walk_3");
        loadOverrideImage(new File(baseDir, "player/big_jump.png"), "mario_big_jump");
        loadOverrideImage(new File(baseDir, "player/big_skid.png"), "mario_big_skid");
        loadOverrideImage(new File(baseDir, "player/big_flag.png"), "mario_big_flag");
        loadOverrideImage(new File(baseDir, "items/mushroom.png"), "item_mushroom");

        loadOverrideImage(new File(baseDir, "enemies/goomba_1.png"), "goomba_1");
        loadOverrideImage(new File(baseDir, "enemies/goomba_2.png"), "goomba_2");
        loadOverrideImage(new File(baseDir, "enemies/goomba_dead.png"), "goomba_dead");

        loadOverrideImage(new File(baseDir, "tiles/ground.png"), "tile_ground");
        loadOverrideImage(new File(baseDir, "tiles/brick.png"), "tile_brick");
        loadOverrideImage(new File(baseDir, "tiles/question_1.png"), "tile_question_1");
        loadOverrideImage(new File(baseDir, "tiles/question_used.png"), "tile_question_used");
        loadOverrideImage(new File(baseDir, "tiles/pipe_top_left.png"), "tile_pipe_top_l");
        loadOverrideImage(new File(baseDir, "tiles/pipe_top_right.png"), "tile_pipe_top_r");
        loadOverrideImage(new File(baseDir, "tiles/pipe_body_left.png"), "tile_pipe_body_l");
        loadOverrideImage(new File(baseDir, "tiles/pipe_body_right.png"), "tile_pipe_body_r");
    }

    private static void loadOverrideImage(File file, String key) {
        if (file.isFile()) {
            try {
                BufferedImage img = ImageIO.read(file);
                if (img != null) cache.put(key, img);
            } catch (Exception ignored) {}
        }
    }

    private static BufferedImage createPixelMap(int w, int h, String[] rows, Map<Character, Color> colMap) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            String row = y < rows.length ? rows[y] : "";
            for (int x = 0; x < w; x++) {
                char ch = x < row.length() ? row.charAt(x) : ' ';
                Color c = colMap.get(ch);
                if (c != null) {
                    img.setRGB(x, y, c.getRGB());
                } else {
                    img.setRGB(x, y, 0x00000000); // Trong suốt
                }
            }
        }
        return img;
    }

    // ==========================================
    // TẠO SPRITE MARIO GỐC (16x16 PIXELS)
    // R: Đỏ (Áo/Mũ), B: Nâu (Quần yếm/Tóc), S: Da mặt
    // ==========================================
    private static void generateDefaultMarioSprites() {
        Map<Character, Color> map = new HashMap<>();
        map.put('R', COLOR_RED);
        map.put('B', COLOR_BROWN);
        map.put('S', COLOR_SKIN);

        // Mario đứng yên (Idle)
        String[] idle = {
            "     RRRRR      ",
            "    RRRRRRRRR   ",
            "    BBBSSBS     ",
            "   BSBSSSBSSS   ",
            "   BSBBSSSBSSS  ",
            "   BBBSSSSBBBB  ",
            "     SSSSSSS    ",
            "    BBRBBB      ",
            "   BBBRBBRBBB   ",
            "  BBBBRRRRBBBB  ",
            "  SSBRSRRSRBSS  ",
            "  SSS RRRR SSS  ",
            "  SS  RRRR  SS  ",
            "     BBBBBB     ",
            "    BBB  BBB    ",
            "   BBB    BBB   "
        };
        cache.put("mario_idle", createPixelMap(16, 16, idle, map));

        // Mario bước chạy 1 (Walk 1)
        String[] walk1 = {
            "     RRRRR      ",
            "    RRRRRRRRR   ",
            "    BBBSSBS     ",
            "   BSBSSSBSSS   ",
            "   BSBBSSSBSSS  ",
            "   BBBSSSSBBBB  ",
            "     SSSSSSS    ",
            "    BBRBBB      ",
            "   BBBRBBRBBB   ",
            "  BBBBRRRRBBBB  ",
            "  SSBRSRRSRBSS  ",
            "  SSS RRRR SSS  ",
            "  SS  RRRR  SS  ",
            "    BBBBBBBB    ",
            "   BBB    BBB   ",
            "  BBB           "
        };
        cache.put("mario_walk_1", createPixelMap(16, 16, walk1, map));

        // Mario bước chạy 2 (Walk 2)
        String[] walk2 = {
            "     RRRRR      ",
            "    RRRRRRRRR   ",
            "    BBBSSBS     ",
            "   BSBSSSBSSS   ",
            "   BSBBSSSBSSS  ",
            "   BBBSSSSBBBB  ",
            "     SSSSSSS    ",
            "   BBBBRBBBB    ",
            "  BBBBBRRRRBB   ",
            "  SS BBRRRR BS  ",
            " SSS BRSRRS BSS ",
            " SSS BBRRRR BSS ",
            " SS   BBBB   SS ",
            "     BBBBBB     ",
            "    BBBBBBBB    ",
            "   BBB    BBB   "
        };
        cache.put("mario_walk_2", createPixelMap(16, 16, walk2, map));

        // Mario bước chạy 3 (Walk 3)
        String[] walk3 = {
            "     RRRRR      ",
            "    RRRRRRRRR   ",
            "    BBBSSBS     ",
            "   BSBSSSBSSS   ",
            "   BSBBSSSBSSS  ",
            "   BBBSSSSBBBB  ",
            "     SSSSSSS    ",
            "     BBRBBB     ",
            "   BBBRBBRBBB   ",
            "  BBBBRRRRBBBB  ",
            "  SSBRSRRSRBSS  ",
            "  SSS RRRR SSS  ",
            "  SS  RRRR  SS  ",
            "    BBBBBBBB    ",
            "   BBB    BBB   ",
            "           BBB  "
        };
        cache.put("mario_walk_3", createPixelMap(16, 16, walk3, map));

        // Mario nhảy (Jump)
        String[] jump = {
            "  SS RRRRR      ",
            "  SSSRRRRRRRR   ",
            "   SSBBBSSBS    ",
            "    BSBSSSBSSS  ",
            "    BSBBSSSBSSS ",
            "    BBBSSSSBBBB ",
            "      SSSSSSS   ",
            "    BBRBBBBB SS ",
            "   BBBRBBRBBBSSS",
            "  BBBBRRRRBBBBSS",
            "  SS BRRRRR     ",
            " SSS RRRRRRR    ",
            " SS  RRRRRRR    ",
            "     BBBBBBB    ",
            "    BBB   BBB   ",
            "   BBB     BBB  "
        };
        cache.put("mario_jump", createPixelMap(16, 16, jump, map));

        // Mario phanh trượt (Skid)
        String[] skid = {
            "      RRRRR     ",
            "    RRRRRRRRR   ",
            "    BBBSSBS     ",
            "   BSBSSSBSSS   ",
            "   BSBBSSSBSSS  ",
            "   BBBSSSSBBBB  ",
            "     SSSSSSS    ",
            "    BBRBBB      ",
            "   BBBRBBRBBB   ",
            "  BBBBRRRRBBBB  ",
            "  SSBRSRRSRBSS  ",
            "  SSS RRRR SSS  ",
            "  SS  RRRR  SS  ",
            "    BBBBBB      ",
            "   BBBBBBBBB    ",
            "  BBB    BBB    "
        };
        cache.put("mario_skid", createPixelMap(16, 16, skid, map));

        // Mario chết văng lên trời (Die)
        String[] die = {
            "     RRRRR      ",
            "    RRRRRRRRR   ",
            "    BBBSSBS     ",
            "   BSBSSSBSSS   ",
            "   BSBBSSSBSSS  ",
            "   BBBSSSSBBBB  ",
            "     SSSSSSS    ",
            "     RRRRRR     ",
            "   RRRRRRRRRR   ",
            "  RRRRRRRRRRRR  ",
            "  SS RRRRRR SS  ",
            " SSS RRRRRR SSS ",
            " SS  BBBBBB  SS ",
            "    BBB  BBB    ",
            "   BBB    BBB   ",
            "  BBB      BBB  "
        };
        cache.put("mario_die", createPixelMap(16, 16, die, map));

        // Mario bám cờ trượt xuống (Flag)
        String[] flag = {
            "     RRRRR      ",
            "    RRRRRRRRR   ",
            "    BBBSSBS     ",
            "   BSBSSSBSSS   ",
            "   BSBBSSSBSSS  ",
            "   BBBSSSSBBBB  ",
            "     SSSSSSS    ",
            "     BBRBBB S   ",
            "    BBRBBRBBBSS ",
            "   BBBBRRRRBBBS ",
            "   SSBRSRRSRB   ",
            "   SSSRRRRSSS   ",
            "   SS RRRRSS    ",
            "     BBBBBB     ",
            "    BBB  BBB    ",
            "    BBB  BBB    "
        };
        cache.put("mario_flag", createPixelMap(16, 16, flag, map));
    }

    // ==========================================
    // TẠO SPRITE MARIO HÓA TO (BIG MARIO 16x32)
    // Tỉ lệ thân hình to đều, vạm vỡ, mũ to, mũi to, ngực nở, chân ngắn chắc nịch chuẩn Mario NES
    // ==========================================
    private static void generateDefaultBigMarioSprites() {
        Map<Character, Color> map = new HashMap<>();
        map.put('R', COLOR_RED);
        map.put('B', COLOR_BROWN);
        map.put('S', COLOR_SKIN);

        String[] bigIdle = {
            "     RRRRRR     ",
            "   RRRRRRRRRR   ",
            "  RRRRRRRRRRRR  ",
            "  RRRRRRRRRRRRR ",
            "   BBBSSBS      ",
            "  BSBSSSBSSSS   ",
            "  BSBBSSSBSSSS  ",
            "  BBBSSSSBBBBSS ",
            "  BBBBSSBBBBBBB ",
            "    SSSSSSSSSS  ",
            "    SSSSSSSS    ",
            "   BBRRRRRRBB   ",
            "  BBBRRRRRRBBB  ",
            " BBBBRRRRRRBBBB ",
            " SS BRRRRRR BS  ",
            "SSS BRRRRRR BSS ",
            "SSS BRRRRRR BSS ",
            " SS  RRRRRR  SS ",
            "    RRRRRRRR    ",
            "   RRRRRRRRRR   ",
            "  RRRRRRRRRRRR  ",
            "  RRRRRRRRRRRR  ",
            "   RRRR  RRRR   ",
            "   RRRR  RRRR   ",
            "   RRRR  RRRR   ",
            "   BBBB  BBBB   ",
            "  BBBBB  BBBBB  ",
            "  BBBBB  BBBBB  ",
            " BBBBBB  BBBBBB ",
            "BBBBBBB  BBBBBBB",
            "BBBBBBB  BBBBBBB",
            " BBBBB    BBBBB "
        };
        cache.put("mario_big_idle", createPixelMap(16, 32, bigIdle, map));

        String[] bigWalk1 = {
            "     RRRRRR     ",
            "   RRRRRRRRRR   ",
            "  RRRRRRRRRRRR  ",
            "  RRRRRRRRRRRRR ",
            "   BBBSSBS      ",
            "  BSBSSSBSSSS   ",
            "  BSBBSSSBSSSS  ",
            "  BBBSSSSBBBBSS ",
            "  BBBBSSBBBBBBB ",
            "    SSSSSSSSSS  ",
            "    SSSSSSSS    ",
            "   BBRRRRRRBB   ",
            "  BBBRRRRRRBBB  ",
            " BBBBRRRRRRBBBB ",
            " SS BRRRRRR BS  ",
            "SSS BRRRRRR BSS ",
            "SSS BRRRRRR BSS ",
            " SS  RRRRRR  SS ",
            "    RRRRRRRR    ",
            "   RRRRRRRRRR   ",
            "  RRRRRRRRRRRR  ",
            "  RRRRRRRRRRRR  ",
            "   RRRR  RRRR   ",
            "   RRRR   RRRR  ",
            "   RRRR    RRRR ",
            "   BBBB     BBBB",
            "  BBBBB     BBBB",
            "  BBBBB     BBBB",
            " BBBBBB     BBBB",
            "BBBBBBB    BBBB ",
            "BBBBBBB         ",
            " BBBBB          "
        };
        cache.put("mario_big_walk_1", createPixelMap(16, 32, bigWalk1, map));

        String[] bigWalk2 = {
            "     RRRRRR     ",
            "   RRRRRRRRRR   ",
            "  RRRRRRRRRRRR  ",
            "  RRRRRRRRRRRRR ",
            "   BBBSSBS      ",
            "  BSBSSSBSSSS   ",
            "  BSBBSSSBSSSS  ",
            "  BBBSSSSBBBBSS ",
            "  BBBBSSBBBBBBB ",
            "    SSSSSSSSSS  ",
            "    SSSSSSSS    ",
            "  BBBBRRRRRRBB  ",
            " BBBBRRRRRRBBB  ",
            " SS BBRRRRBB SS ",
            "SSS BRSRRS BSS  ",
            "SSS BBRRRR BSS  ",
            " SS   BBBB   SS ",
            "     RRRRRR     ",
            "   RRRRRRRRRR   ",
            "  RRRRRRRRRRRR  ",
            "  RRRRRRRRRRRR  ",
            "    RRRRRRRR    ",
            "   RRRR  RRRR   ",
            "   RRRR  RRRR   ",
            "   BBBB  BBBB   ",
            "   BBBB  BBBB   ",
            "  BBBBB  BBBBB  ",
            "  BBBBB  BBBBB  ",
            " BBBBBB  BBBBBB ",
            "BBBBBBB  BBBBBBB",
            "BBBBBBB  BBBBBBB",
            " BBBBB    BBBBB "
        };
        cache.put("mario_big_walk_2", createPixelMap(16, 32, bigWalk2, map));

        String[] bigWalk3 = {
            "     RRRRRR     ",
            "   RRRRRRRRRR   ",
            "  RRRRRRRRRRRR  ",
            "  RRRRRRRRRRRRR ",
            "   BBBSSBS      ",
            "  BSBSSSBSSSS   ",
            "  BSBBSSSBSSSS  ",
            "  BBBSSSSBBBBSS ",
            "  BBBBSSBBBBBBB ",
            "    SSSSSSSSSS  ",
            "    SSSSSSSS    ",
            "   BBRRRRRRBB   ",
            "  BBBRRRRRRBBB  ",
            " BBBBRRRRRRBBBB ",
            " SS BRRRRRR BS  ",
            "SSS BRRRRRR BSS ",
            "SSS BRRRRRR BSS ",
            " SS  RRRRRR  SS ",
            "    RRRRRRRR    ",
            "   RRRRRRRRRR   ",
            "  RRRRRRRRRRRR  ",
            "  RRRRRRRRRRRR  ",
            "  RRRR    RRRR  ",
            " RRRR      RRRR ",
            " RRRR      RRRR ",
            " BBBB       BBBB",
            "BBBBB       BBBB",
            "BBBBB       BBBB",
            "BBBBBB      BBBB",
            " BBBBBB    BBBB ",
            "  BBBB          ",
            "                "
        };
        cache.put("mario_big_walk_3", createPixelMap(16, 32, bigWalk3, map));

        String[] bigJump = {
            "        SS      ",
            "       SSSS     ",
            "     RRRRRR     ",
            "   RRRRRRRRRR   ",
            "  RRRRRRRRRRRR  ",
            "  RRRRRRRRRRRRR ",
            "   BBBSSBS      ",
            "  BSBSSSBSSSS   ",
            "  BSBBSSSBSSSS  ",
            "  BBBSSSSBBBBSS ",
            "  BBBBSSBBBBBBB ",
            "    SSSSSSSSSS  ",
            "    SSSSSSSS    ",
            "   BBRRRRRRBB   ",
            "  BBBRRRRRRBBB  ",
            " BBBBRRRRRRBBBB ",
            " SS BRRRRRR     ",
            "SSS BRRRRRR     ",
            " SS  RRRRRR     ",
            "    RRRRRRRR    ",
            "   RRRRRRRRRR   ",
            "  RRRRRRRRRRRR  ",
            "  RRRRRRRRRRRR  ",
            "   RRRR  RRRR   ",
            "  RRRR    RRRR  ",
            " RRRR      RRRR ",
            " BBBB      BBBB ",
            "BBBBB      BBBBB",
            "BBBBB      BBBBB",
            "BBBBBB    BBBBBB",
            " BBBBB    BBBBB ",
            "  BBB      BBB  "
        };
        cache.put("mario_big_jump", createPixelMap(16, 32, bigJump, map));

        String[] bigSkid = {
            "     RRRRRR     ",
            "   RRRRRRRRRR   ",
            "  RRRRRRRRRRRR  ",
            "  RRRRRRRRRRRRR ",
            "   BBBSSBS      ",
            "  BSBSSSBSSSS   ",
            "  BSBBSSSBSSSS  ",
            "  BBBSSSSBBBBSS ",
            "  BBBBSSBBBBBBB ",
            "    SSSSSSSSSS  ",
            "    SSSSSSSS    ",
            "   BBRRRRRRBB   ",
            "  BBBRRRRRRBBB  ",
            "  BBBBRRRRRRBBBB",
            " SS  BRRRRRR  SS",
            "SSS  BRRRRRR SSS",
            " SS   RRRRRR  SS",
            "     RRRRRRRR   ",
            "    RRRRRRRRRR  ",
            "   RRRRRRRRRRRR ",
            "   RRRRRRRRRRRR ",
            "    RRRR  RRRR  ",
            "   RRRR    RRRR ",
            "  RRRR      RRRR",
            "  BBBB      BBBB",
            " BBBBB     BBBBB",
            " BBBBB     BBBBB",
            "BBBBBB    BBBBBB",
            "BBBBBB    BBBBBB",
            "BBBBBB    BBBBBB",
            " BBBBB    BBBBB ",
            "  BBB      BBB  "
        };
        cache.put("mario_big_skid", createPixelMap(16, 32, bigSkid, map));

        String[] bigFlag = {
            "     RRRRRR     ",
            "   RRRRRRRRRR   ",
            "  RRRRRRRRRRRR  ",
            "  RRRRRRRRRRRRR ",
            "   BBBSSBS      ",
            "  BSBSSSBSSSS   ",
            "  BSBBSSSBSSSS  ",
            "  BBBSSSSBBBBSS ",
            "  BBBBSSBBBBBBB ",
            "    SSSSSSSSSS  ",
            "    SSSSSSSS    ",
            "   BBRRRRRRBB   ",
            "  BBBRRRRRRBBB  ",
            "  BBBBRRRRRRBBBB",
            "  SS BRRRRRR    ",
            " SSS BRRRRRR    ",
            "  SS RRRRRRR    ",
            "    RRRRRRRR    ",
            "   RRRRRRRRRR   ",
            "  RRRRRRRRRRRR  ",
            "  RRRRRRRRRRRR  ",
            "   RRRR  RRRR   ",
            "   RRRR   RRRR  ",
            "   BBBB    BBBB ",
            "  BBBBB     BBBB",
            "  BBBBB     BBBB",
            "  BBBBB     BBBB",
            " BBBBBB     BBBB",
            "BBBBBBB    BBBB ",
            "BBBBBBB         ",
            " BBBBB          ",
            "                "
        };
        cache.put("mario_big_flag", createPixelMap(16, 32, bigFlag, map));
    }

    // ==========================================
    // TẠO SPRITE NẤM GOOMBA GỐC (16x16 PIXELS)
    // B: Nâu sẫm, E: Màu mắt/chân, F: Đen
    // ==========================================
    private static void generateDefaultGoombaSprites() {
        Map<Character, Color> map = new HashMap<>();
        map.put('B', COLOR_GOOMBA_BODY);
        map.put('E', COLOR_GOOMBA_EYES);
        map.put('F', COLOR_GOOMBA_FEET);

        // Goomba bước 1
        String[] g1 = {
            "     BBBBBB     ",
            "    BBBBBBBB    ",
            "   BBBBBBBBBB   ",
            "  BBBBBBBBBBBB  ",
            " BBBBBBBBBBBBBB ",
            " BBBBEBBBEBBBBB ",
            "BBBBEFBEBEFBBEBB",
            "BBBBEFBEBEFBBEBB",
            "BBBBEEBBEEBBEEBB",
            " BBBBBBBBBBBBBB ",
            "  BBBBEEEEBBBB  ",
            "   EEEEEEEEEE   ",
            "  EEEEEEEEEEEE  ",
            "  FFFF   FFFF   ",
            " FFFFFF  FFFFFF ",
            " FFFFFF  FFFFFF "
        };
        cache.put("goomba_1", createPixelMap(16, 16, g1, map));

        // Goomba bước 2
        String[] g2 = {
            "     BBBBBB     ",
            "    BBBBBBBB    ",
            "   BBBBBBBBBB   ",
            "  BBBBBBBBBBBB  ",
            " BBBBBBBBBBBBBB ",
            " BBBBEBBBEBBBBB ",
            "BBBBEFBEBEFBBEBB",
            "BBBBEFBEBEFBBEBB",
            "BBBBEEBBEEBBEEBB",
            " BBBBBBBBBBBBBB ",
            "  BBBBEEEEBBBB  ",
            "   EEEEEEEEEE   ",
            "  EEEEEEEEEEEE  ",
            "   FFFF   FFFF  ",
            "  FFFFFF FFFFFF ",
            "  FFFFFF FFFFFF "
        };
        cache.put("goomba_2", createPixelMap(16, 16, g2, map));

        // Goomba bị dẫm bẹp (Dead / Stomped)
        String[] gDead = {
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
            "   BBBBBBBBBB   ",
            "  BBBBBBBBBBBB  ",
            " BBBBBBBBBBBBBB ",
            "BBBBEEFBBFEEBBBB",
            "BBBBEEEEEEEEBBBB",
            " BBBBBBBBBBBBBB ",
            "  FFFFFFFFFFFF  ",
            "  FFFFFFFFFFFF  "
        };
        cache.put("goomba_dead", createPixelMap(16, 16, gDead, map));
    }

    // ==========================================
    // TẠO SPRITE TILES / BLOCKS (16x16 PIXELS)
    // ==========================================
    private static void generateDefaultTiles() {
        // 1. Tile đất nền (Ground Block)
        BufferedImage ground = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = ground.createGraphics();
        g.setColor(COLOR_BRICK);
        g.fillRect(0, 0, 16, 16);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, 15, 15);
        g.setColor(new Color(0xFC, 0x98, 0x38)); // Viền highlight trên
        g.drawLine(1, 1, 14, 1);
        g.drawLine(1, 1, 1, 14);
        g.setColor(new Color(0x70, 0x28, 0x08));
        g.fillRect(4, 4, 8, 8);
        g.dispose();
        cache.put("tile_ground", ground);

        // 2. Tile khối gạch (Brick Block)
        BufferedImage brick = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        g = brick.createGraphics();
        g.setColor(COLOR_BRICK);
        g.fillRect(0, 0, 16, 16);
        g.setColor(COLOR_BRICK_DARK);
        // Đường rãnh gạch chuẩn Mario NES
        g.drawLine(0, 0, 15, 0);
        g.drawLine(0, 4, 15, 4);
        g.drawLine(0, 8, 15, 8);
        g.drawLine(0, 12, 15, 12);
        g.drawLine(0, 15, 15, 15);
        g.drawLine(7, 0, 7, 4);
        g.drawLine(15, 0, 15, 4);
        g.drawLine(0, 4, 0, 8);
        g.drawLine(11, 4, 11, 8);
        g.drawLine(5, 8, 5, 12);
        g.drawLine(15, 8, 15, 12);
        g.drawLine(0, 12, 0, 15);
        g.drawLine(9, 12, 9, 15);
        g.dispose();
        cache.put("tile_brick", brick);

        // 3. Khối hỏi chấm (?) - 3 frame hoạt họa nhấp nháy
        for (int frame = 0; frame < 3; frame++) {
            BufferedImage q = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            g = q.createGraphics();
            Color base = frame == 2 ? COLOR_QUESTION_SHINE : COLOR_QUESTION_GOLD;
            g.setColor(base);
            g.fillRect(0, 0, 16, 16);
            g.setColor(Color.BLACK);
            g.drawRect(0, 0, 15, 15);
            // 4 đinh tán ở góc
            g.fillRect(1, 1, 1, 1);
            g.fillRect(14, 1, 1, 1);
            g.fillRect(1, 14, 1, 1);
            g.fillRect(14, 14, 1, 1);
            // Dấu hỏi chấm màu nâu đen
            g.setColor(COLOR_DARK_BROWN);
            g.fillRect(5, 3, 6, 2);
            g.fillRect(9, 5, 2, 3);
            g.fillRect(7, 7, 3, 2);
            g.fillRect(7, 9, 2, 2);
            g.fillRect(7, 12, 2, 2);
            g.dispose();
            cache.put("tile_question_" + (frame + 1), q);
        }

        // 4. Khối đã dùng hết (Used Block - khối sắt xám nâu)
        BufferedImage used = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        g = used.createGraphics();
        g.setColor(new Color(0x8B, 0x6C, 0x54));
        g.fillRect(0, 0, 16, 16);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, 15, 15);
        g.fillRect(1, 1, 2, 2);
        g.fillRect(13, 1, 2, 2);
        g.fillRect(1, 13, 2, 2);
        g.fillRect(13, 13, 2, 2);
        g.dispose();
        cache.put("tile_question_used", used);

        // 5. Khối ống cống xanh (Pipes)
        // Miệng ống trái
        BufferedImage ptl = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        g = ptl.createGraphics();
        g.setColor(COLOR_PIPE_LIGHT);
        g.fillRect(0, 0, 16, 16);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, 15, 15);
        g.setColor(COLOR_PIPE_HIGHLIGHT);
        g.fillRect(3, 1, 3, 14);
        g.dispose();
        cache.put("tile_pipe_top_l", ptl);

        // Miệng ống phải
        BufferedImage ptr = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        g = ptr.createGraphics();
        g.setColor(COLOR_PIPE_LIGHT);
        g.fillRect(0, 0, 16, 16);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, 15, 15);
        g.setColor(COLOR_PIPE_DARK);
        g.fillRect(10, 1, 5, 14);
        g.dispose();
        cache.put("tile_pipe_top_r", ptr);

        // Thân ống trái
        BufferedImage pbl = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        g = pbl.createGraphics();
        g.setColor(COLOR_PIPE_LIGHT);
        g.fillRect(2, 0, 14, 16);
        g.setColor(Color.BLACK);
        g.drawLine(2, 0, 2, 15);
        g.setColor(COLOR_PIPE_HIGHLIGHT);
        g.fillRect(5, 0, 3, 16);
        g.dispose();
        cache.put("tile_pipe_body_l", pbl);

        // Thân ống phải
        BufferedImage pbr = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        g = pbr.createGraphics();
        g.setColor(COLOR_PIPE_LIGHT);
        g.fillRect(0, 0, 14, 16);
        g.setColor(Color.BLACK);
        g.drawLine(13, 0, 13, 15);
        g.setColor(COLOR_PIPE_DARK);
        g.fillRect(8, 0, 5, 16);
        g.dispose();
        cache.put("tile_pipe_body_r", pbr);

        // 6. Cột cờ và Cờ
        BufferedImage flag = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        g = flag.createGraphics();
        g.setColor(Color.WHITE);
        int[] xPts = {1, 15, 1};
        int[] yPts = {1, 8, 15};
        g.fillPolygon(xPts, yPts, 3);
        g.setColor(COLOR_PIPE_LIGHT);
        g.fillRect(4, 5, 5, 6);
        g.dispose();
        cache.put("tile_flag", flag);

        // 7. Đồng xu xoay tít khi húc gạch
        BufferedImage coin = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        g = coin.createGraphics();
        g.setColor(COLOR_QUESTION_GOLD);
        g.fillOval(3, 1, 10, 14);
        g.setColor(COLOR_QUESTION_SHINE);
        g.fillOval(5, 3, 6, 10);
        g.setColor(COLOR_DARK_BROWN);
        g.drawOval(3, 1, 10, 14);
        g.dispose();
        cache.put("item_coin", coin);

        // 8. Nấm đỏ thần kỳ (Super Mushroom)
        Map<Character, Color> mshMap = new HashMap<>();
        mshMap.put('R', COLOR_RED);
        mshMap.put('S', COLOR_SKIN);
        mshMap.put('F', Color.BLACK);
        String[] msh = {
            "     RRRRRR     ",
            "   RRRSSSSRRR   ",
            "  RRSSSSSSSSRR  ",
            " RRRSSRRRRSSRRR ",
            " RRRRRRRRRRRRRR ",
            " RRRSSSSSSSSRRR ",
            " RRRSSSSSSSSRRR ",
            "  RRRRRRRRRRRR  ",
            "   FFFFFFFFF    ",
            "   FSSSSSSSF    ",
            "   FSFSFSFSF    ",
            "   FSFSFSFSF    ",
            "   FSSSSSSSF    ",
            "   FFFFFFFFF    ",
            "                ",
            "                "
        };
        cache.put("item_mushroom", createPixelMap(16, 16, msh, mshMap));
    }
}

