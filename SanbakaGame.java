import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;
/** Sanbaka: Tuan Thi — a self-contained Vietnamese story runner. */
public class SanbakaGame extends JPanel implements ActionListener, KeyListener {
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
    private java.awt.image.BufferedImage dialogBackground;
    int screen = TITLE;
    int dialogIndex = 0;
    int day = 0;
    double playerX = 480;
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
    double playerY = 490;
    double roadOffset = 0;
    double invulnerable = 0;
    boolean left=false,right=false,up=false,down=false;
    // ---- xe máy (độ nghiêng khi đổi làn) ----
    double lean = 0;
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
    static class Line {
        String who,text;
        Line(String w,String t) {
            who=w;
            text=t;
        }
    }
    /** Xe cộ (hoặc tia sét năng lượng nếu snack = true) trên đường. */
    static class Thing {
        double x, y, w, h;
        double baseX, wobbleAmp, wobbleFreq, wobblePhase;
        double fwd;          // tốc độ tự chạy của xe (tỉ lệ so với tốc độ cuộn đường)
        double y0;           // vị trí ban đầu của xe trong đoạn tắc đường
        double angle;        // góc nghiêng khi lạng lách
        int kind, color;     // kind: 0 sedan, 1 hatchback, 2 taxi, 3 van, 4 xe tải, 5 xe buýt, 6 xe máy, 7 tia sét
        boolean snack;       // tia sét năng lượng (chỉ ngày nắng nóng)
        boolean jam;         // xe nằm trong đoạn tắc đường (không va chạm)
        boolean hit;         // đã va chạm với người chơi (không tính lại)
    }
    /** Chim bay từ phải sang trái trong chặng trực thăng. */
    static class Bird {
        double x, y, vx, phase;
        Bird(double x, double y, double vx, double phase) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.phase = phase;
        }
    }
    static class Confetti {
        double x, y, vx, vy, rot, vr, size;
        int shape;
        Color color;
    }
    void add(String who, String... texts) {
        boolean section = who.startsWith("NGÀY ") || who.startsWith("TRÊN ĐƯỜNG") || who.startsWith("HE ·") || who.startsWith("BE ·") || who.startsWith("Một lúc sau");
        if (section) {
            lines.add(new Line("@" + who, ""));
            for (String text : texts) {
                int separator = text.indexOf(':');
                if (separator > 0) {
                    lines.add(new Line( text.substring(0, separator).trim(), text.substring(separator + 1).trim()));
                }
                else {
                    lines.add(new Line("Thắng", text));
                }
            }
            return;
        }
        lines.add(new Line(who, String.join(" ", texts)));
    }
    void buildStory() {
        add("Sanbaka (tin nhắn)",
            "Anh ơi, mai bắt đầu tuần thi rồi. Anh chuẩn bị hết chưa?"
        );
        add("Thắng (tin nhắn)",
            "Gần xong rồi em. Chỉ sợ sáng mai ngủ quên thôi :P"
        );
        add("Sanbaka (tin nhắn)",
            "Anh còn dám nói à? Lần trước em gọi mãi anh mới chịu dậy đấy!"
        );
        add("Thắng (tin nhắn)",
            "Tại cái giường quyến rũ anh quá mà…"
        );
        add("Sanbaka (tin nhắn)",
            "Thế này nhé, nếu anh hoàn thành tuần thi và đến C1 gặp em ngay sau môn cuối cùng, "
                    + "em sẽ thưởng cho anh một buổi hẹn hò. Mình đi ăn rồi đi chơi đâu đó nhé!"
        );
        add("Thắng (tin nhắn)",
            "Chốt kèo! Anh nhất định không ngủ quên, cũng không để em phải chờ đâu!"
        );
        add("Sanbaka (tin nhắn)",
            "Anh nhớ đấy nhé! Giờ ngủ sớm đi, mai em gọi anh dậy."
        );
        add("Thắng (tin nhắn)",
            "Dạ vợ :P"
        );
        add("NGÀY 1 · TẠI PHÒNG TRỌ",
            "Sanbaka (tin nhắn): Dậy đi! Hôm nay anh có lịch thi đấy, đừng ngủ quên nhé.",
            "Thắng (tin nhắn): Cho anh ngủ thêm năm phút thôi em…",
            "Sanbaka (tin nhắn): Năm phút của anh hôm nào chẳng thành nửa tiếng. Anh mà không dậy là em giận đấy!",
            "Thắng (tin nhắn): Anh dậy ngay đây! Đừng giận anh mà.",
            "Nam: Mày dậy rồi à? Tao tưởng phải chờ người yêu mày gọi đến lần thứ mười cơ.",
            "Thắng: Kệ tao. Có người yêu quan tâm thì mày ghen à?",
            "Nam: Ừ, tao ghen với cái chăn của mày đấy. Nó được ôm mày cả đêm.",
            "Chủ trọ: Hai đứa chuẩn bị đi thi thì nhanh lên. Đừng có đến muộn rồi lại đổ tại đường đông nhé!",
            "Thắng: Vâng ạ, cháu đi ngay đây!"
        );
        add("NGÀY 1 · SAU KHI THI",
            "Nam: Thế nào, còn sống không?",
            "Thắng: Còn. Nhưng mấy câu cuối suýt lấy mạng tao.",
            "Nam: Thế thì về ôn tiếp đi, đừng ngồi đây than thở nữa.",
            "Sanbaka (tin nhắn): Anh thi xong rồi à? Làm bài ổn không anh?",
            "Thắng (tin nhắn): Cũng tạm em ạ. Giờ anh chỉ muốn gặp em thôi.",
            "Sanbaka (tin nhắn): Cố thêm mấy ngày nữa nhé. Thi xong em sẽ thực hiện lời hứa hôm qua với anh!",
            "Thắng (tin nhắn): Em nhớ đấy nhé!"
        );
        add("NGÀY 2 · TẠI PHÒNG TRỌ",
            "Nam: Ê mày, nhìn đường ngoài kia kìa. Hôm nay đi kiểu gì đây?",
            "Thắng: Bơi đi cu",
            "Nam: Mày bơi xong tao ngồi trên mày nhể",
            "Thắng: Hmm hay là cúp cho khỏe",
            "Nam: ? Hôm nay thi đấy cu",
            "Thắng: Chết bà! Thế giờ bơi thật à?",
            "Chủ trọ: Dưới gầm giường có cái thuyền phao đấy. Lôi ra mà chèo!",
            "Thắng: ...Bác nói thật ạ?",
            "Chủ trọ: Cháu thử tìm xem, biết đâu có thật!",
            "Thắng: Đi thôi mày. Nhanh không làm cổ đông cho trường giờ!"
        );
        add("NGÀY 2 · SAU KHI THI",
            "Sanbaka: Anh đây rồi! Sao ướt hết thế này?",
            "Thắng: Anh không sao. Mưa lớn quá, suýt nữa anh thành cá luôn rồi.",
            "Sanbaka: Đồ ngốc! Em đã bảo anh đi cẩn thận mà. Mau lau khô người đi, em mua đồ ăn nóng cho anh rồi đây.",
            "Thắng: Em đợi anh lâu chưa? Còn phải đi mua đồ ăn nữa…",
            "Sanbaka: Không lâu đâu. Em chỉ sợ anh mải thi xong lại nhịn đói thôi.",
            "Thắng: Có em quan tâm thế này, đường ngập cũng đáng!",
            "Sanbaka: Anh cứ nói ngọt đi! Lần sau mà để mình ướt sũng thế này nữa, em sẽ giận thật đấy!",
            "Thắng: Dạ vợ :P"
        );
        add("NGÀY 3 · TẠI PHÒNG TRỌ",
            "Thắng: Sao nay rét thế nhể",
            "Nam: Mặc thêm áo vào đi mày. Hôm nay rét thế này mà ốm thì xác định nghỉ thi.",
            "Thắng: Hôm nay mày chu đáo thế?",
            "Nam: Tao chỉ sợ mày ốm rồi bắt tao chăm thôi.",
            "Thắng: Kê"
        );
        add("NGÀY 3 · SAU KHI THI",
            "Thắng: Giải tích 2 đúng là không dễ thở chút nào.",
            "Sanbaka: Vất vả rồi. Anh ngồi xuống nghỉ đi, em có đồ uống ấm cho anh đây.",
            "Thắng: Được ngồi cạnh em thế này là anh hồi sức nhanh nhất rồi.",
            "Sanbaka: Miệng ngọt ghê. Nhưng tối nay phải ngủ sớm đấy, không được thức khuya ôn bài nữa!",
            "Thắng: Anh hứa!"
        );
        add("NGÀY 4 · TẠI PHÒNG TRỌ",
            "Thắng: Hôm nay nóng như đổ lửa. Tao chỉ muốn nằm nhà bật điều hòa thôi.",
            "Nam: Thế mày định để giảng viên phát đề qua bưu điện à cu?",
            "Thắng: Tao đang cân nhắc đề xuất phương án đó.",
            "Nam: Mơ tiếp đi, đến giờ đi thi rồi!"
        );
        add("NGÀY 4 · SAU KHI THI",
            "Thắng: Toán rời rạc mà còn khó hiểu hơn cả cuộc đời tao.",
            "Nam: Mày cứ làm như tối qua mày ôn bài chăm chỉ lắm ấy.",
            "Thắng: Tao có ôn thật!",
            "Nam: Ừ, ôn được mười phút rồi ngủ gục trên bàn.",
            "Thắng: Mày nín!",
            "Sanbaka (tin nhắn): Anh thi xong chưa? Hôm nay có khó lắm không anh?",
            "Thắng (tin nhắn): Cũng hơi khó, nhưng anh làm hết rồi. Còn 1 ngày cuối thôi!",
            "Sanbaka (tin nhắn): Anh giỏi lắm! Nhớ nghỉ ngơi một chút rồi hãy ôn tiếp nhé.",
            "Thắng (tin nhắn): Có em động viên, anh thấy nhẹ nhõm hơn nhiều."
        );
        add("NGÀY 5 · TẠI PHÒNG TRỌ",
            "Nam: Ngày cuối rồi đấy. Thi xong thì đi gặp vợ mày đi, nghe mày than nhớ vợ cả tuần tao phát ngán rồi.",
            "Thắng: Ghen tị thì tìm người yêu đi em.",
            "Sanbaka (tin nhắn): Anh ơi, hôm nay là ngày thi cuối cùng rồi! Cố lên nhé, "
                    + "thi xong mình thực hiện lời hứa từ đầu tuần thôi. Em chờ anh ở C1 đấy!",
            "Thắng (tin nhắn): Anh nhớ mà! Hôm nay nhất định anh sẽ đến đúng giờ. Không để em chờ lâu đâu!",
            "Thắng: …",
            "Mày cười cái chó gì, nhanh tao còn đi thi!"
        );
        add("TRÊN ĐƯỜNG ĐẾN TRƯỜNG · TẮC ĐƯỜNG",
            "Thắng: Ủa? Sao xe đứng im hết thế này?",
            "Mới đi được một phần ba đường thôi mà! Đừng nói là cả thành phố quyết định đi học cùng một giờ nhé?!",
            "5 phút sau, vẫn không xe nào nhúc nhích",
            "Thắng (điện thoại): Alo, mày đang ở đâu đấy?",
            "Nam (điện thoại): Ở trọ chứ ở đâu cu, nay tao ko có lịch thi",
            "Thắng (điện thoại): Tao đang tắc đường, đứng đây nãy giờ rồi. Cứ thế này thì tao đến trường bằng đường tâm linh mất!",
            "Nam (điện thoại): Thế mày muốn tao làm gì? Ra đấy cõng mày à?",
            "Thắng (điện thoại): Mày mà cõng được tao qua cả đống xe này thì tao phong mày làm ân nhân cả đời!",
            "Nam (điện thoại): Được. Đứng yên đấy, tao đến đón.",
            "Thắng (điện thoại): Hả? Mày có cách thật à?",
            "Nam (điện thoại): Tin tao đi. Hôm nay tao sẽ cho mày thấy tình bạn đích thực là thế nào!",
            "Một lúc sau, tiếng cánh quạt trực thăng vang lên từ phía trên.",
            "Thắng: …Cái méo gì thế?!",
            "Nam (loa ngoài): Ê thằng kia! Ngẩng đầu lên! Thấy chiếc trực thăng không?",
            "Thắng: THẰNG ĐIÊN! MÀY LẤY ĐÂU RA TRỰC THĂNG THẾ?!",
            "Nam: Tao bảo rồi, hôm nay tao sẽ cho mày thấy tình bạn đích thực!",
            "Thắng: MÀY BIẾT LÁI TRỰC THĂNG TỪ BAO GIỜ?!",
            "Nam: Từ lúc mày còn đang ngủ nướng ấy!",
            "Thắng: THẾ SAO MÀY KHÔNG KỂ TAO NGHE?!",
            "Nam: Mày có bao giờ dậy đúng giờ để nghe tao kể chuyện đâu!",
            "Thắng: …Được rồi, vụ này tao không cãi được.",
            "Nam: Bám chắc vào nhé! Tao đưa mày đến trường.",
            "Thắng: Tao thề sau kỳ thi tao sẽ viết hẳn một bài báo cáo về độ bất thường của mày!",
            "Nam: Viết đi, nhớ ghi tên tao vào phần cảm ơn!"
        );
        add("NGÀY 5 · SAU KHI THI",
            "Thắng: Cuối cùng cũng xong! Tao chính thức được trả lại cuộc đời rồi.",
            "Nam: Chúc mừng thằng cu, còn tao thì được trả từ hôm trước rồi. Giờ thì đi gặp vợ mày đi cu.",
            "Thắng: Ừ, tao đi đây! Nhưng mày, tao còn nhiều chuyện để hỏi đấy, chờ đi. Mày chưa xong vụ trực thăng với tao đâu.",
            "Nam: Ừ, hỏi gì cứ hỏi, giờ lượn đi em."
        );
        add("Một lúc sau...",
            "Sanbaka: Anh đến rồi! Em biết anh sẽ làm được mà!",
            "Thắng: Anh đã hứa với em thì nhất định phải đến chứ.",
            "Sanbaka: Tuần này anh vất vả rồi. Giờ mình cùng đi ăn gì nhé, em bao!",
            "Thắng: Anh chờ câu này suốt cả tuần đấy.",
            "Sanbaka: Nhưng lần sau vẫn phải chăm chỉ như thế, nghe chưa?",
            "Thắng: Được, miễn là người đi ăn cùng anh vẫn là em.",
            "Sanbaka: Đồ ngốc! Đi thôi! ><"
        );
    }
    public SanbakaGame() {
        setPreferredSize(new Dimension(W, H));
        setFocusable(true);
        addKeyListener(this);
        buildStory();
        findCallRange();
        timer.start();
        try {
            dialogBackground = javax.imageio.ImageIO.read(
                new java.io.File("C:\\Users\\New folder\\OneDrive\\Documents\\sanbaka2.jpg")
            );
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
    void beginDay() {
        screen = ROAD;
        stageStarted = System.currentTimeMillis();
        lastSpawn = stageStarted;
        lastFrameAt = stageStarted;
        playerX = trackX(2);
        playerY = 490;
        hp = 100;
        things.clear();
        score = 0;
        trafficSceneShown = false;
        airSceneShown = false;
        roadPausedForTraffic = false;
        flightMode = false;
        birds.clear();
        lean = 0;
        energy = 100;
        groundScroll = 0;
        jamBuilt = false;
        lastBolt = stageStarted;
        lastHitAt = 0;
        invulnerable = 0;
        beCause = "";
        retryDialogIndex = dialogIndex;
        requestFocusInWindow();
    }
    void begin() {
        screen = DIALOG;
        dialogIndex = 0;
    }
    void enterRoomMarker() {
        Line marker = lines.get(dialogIndex++);
        String title = marker.who;
        int dayNumberPosition = title.indexOf("NGÀY ") + "NGÀY ".length();
        int dayNumber = Character.digit(title.charAt(dayNumberPosition), 10);
        day = Math.max(0, Math.min(dayTitles.length - 1, dayNumber - 1));
        screen = ROOM;
    }
    void advanceRoom() {
        if (dialogIndex < lines.size()
                && !lines.get(dialogIndex).who.startsWith("@")) {
            dialogIndex++;
        }

        if (dialogIndex >= lines.size()
                || lines.get(dialogIndex).who.startsWith("@")) {
            if (dialogIndex < lines.size()
                    && lines.get(dialogIndex).who.startsWith("@NGÀY ")
                    && lines.get(dialogIndex).who.contains("SAU KHI THI")) {
                dialogIndex++;
            }
            beginDay();
        }
    }
    void advanceDialog() {
        if (dialogIndex >= lines.size()) {
            screen = ENDING_HE;
            return;
        }
        Line current = lines.get(dialogIndex);
        if (current.who.startsWith("@NGÀY ") && current.who.contains("TẠI PHÒNG TRỌ")) {
            enterRoomMarker();
            return;
        }
        if (current.who.startsWith("@NGÀY ") && current.who.contains("SAU KHI THI")) {
            dialogIndex++;
            return;
        }
        if (current.who.startsWith("@Một lúc sau")) {
            dialogIndex++;
            return;
        }
        if (current.who.startsWith("@HE ·")) {
            dialogIndex++;
            return;
        }
        if (current.who.startsWith("@BE ·")) {
            dialogIndex++;
            screen = ENDING_HE;
            return;
        }
        if (current.who.startsWith("@TRÊN ĐƯỜNG")) {
            dialogIndex++;
            return;
        }
        dialogIndex++;
        if (day == 4 && !airSceneShown && current.text.startsWith("Một lúc sau, tiếng cánh quạt trực thăng")) {
            airSceneShown = true;
            airSceneStarted = System.currentTimeMillis();
            screen = AIR;
        }
        else if (dialogIndex < lines.size() && lines.get(dialogIndex).who.startsWith("@Một lúc sau")) {
            cardTop = "";
            cardMain = lines.get(dialogIndex).who.substring(1);
            cardNarration = true;
            cardStart = System.currentTimeMillis();
            screen = CARD;
        }
        else if (day == 4 && dialogIndex < lines.size() && lines.get(dialogIndex).text.startsWith("5 phút sau")) {
            cardTop = "";
            cardMain = lines.get(dialogIndex).text;
            cardNarration = true;
            cardStart = System.currentTimeMillis();
            screen = CARD;
        }
        else if (roadPausedForTraffic && dialogIndex < lines.size() && lines.get(dialogIndex).who.startsWith("@NGÀY 5 · SAU KHI THI")) {
            dialogIndex++;
            resumeRoad();
        }
    }
    void resumeRoad() {
        long now = System.currentTimeMillis();
        stageStarted += now - roadPausedAt;
        lastFrameAt = now;
        screen = ROAD;
        roadPausedForTraffic = false;
        fadeInStart = now;
        if (day == 4) startFlight(now);   // 2/3 đường còn lại của ngày 5: lên trực thăng
    }
    void startTrafficConversation() {
        if (dialogIndex < lines.size() && lines.get(dialogIndex).who.startsWith("@TRÊN ĐƯỜNG")) {
            dialogIndex++;
            roadPausedForTraffic = true;
            callStartedAt = 0;
            screen = DIALOG;
        }
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        long now = System.currentTimeMillis();
        if (screen != prevScreen) {
            if (screen == ENDING_HE) startConfetti(now);
            prevScreen = screen;
        }
        if (screen == AIR && now - airSceneStarted >= 3500) {
            screen = DIALOG;
        }
        if (screen == ROAD) {
            elapsed = now - stageStarted;
            if (flightMode) updateFlight(now);
            else updateRoad(now);
            if (hp <= 0 || (day == HOT_DAY && !flightMode && energy <= 0)) {
                beCause = hp <= 0 ? "Bạn đã hết HP giữa đường." : "Bạn đã kiệt sức vì trời nắng nóng.";
                screen = ENDING_BE;
            }
            else if (day == 4 && !flightMode && !trafficSceneShown && elapsed >= ROAD_DURATION_MS / 3) {
                trafficSceneShown = true;
                roadPausedAt = now;
                startFade(FADE_JAM, now);
            }
            else if (elapsed >= ROAD_DURATION_MS) {
                startFade(FADE_END, now);
            }
        }
        else if (screen == FADE && now - fadeStart >= FADE_MS) {
            if (fadeKind == FADE_JAM) {
                startTrafficConversation();
                fadeInStart = now;
            } else {
                skipExamMarker();
                cardTop = "NGÀY " + (day + 1);
                cardMain = "SAU KHI THI";
                cardNarration = false;
                cardStart = now;
                screen = CARD;
            }
        }
        else if (screen == CARD && now - cardStart >= CARD_MS) {
            endCard(now);
        }
        else if (screen == ENDING_HE) {
            updateConfetti(now);
        }
        repaint();
    }
    void skipExamMarker() {
        if (dialogIndex < lines.size()
                && lines.get(dialogIndex).who.startsWith("@NGÀY ")
                && lines.get(dialogIndex).who.contains("SAU KHI THI")) {
            dialogIndex++;
        }
    }
    // =====================================================================
    //  LOGIC ĐƯỜNG BỘ: xe lấn làn, tắc đường, năng lượng ngày nóng
    // =====================================================================
    void updateRoad(long now) {
        double dt = Math.min((now - lastFrameAt) / 1000.0, 0.05);
        lastFrameAt = now;
        double t = elapsed / 1000.0, dur = ROAD_DURATION_MS / 1000.0;
        boolean jamDay = day == 4;
        double jamT1 = dur / 3, jamT0 = jamT1 - JAM_WINDOW;

        if (left) playerX -= PLAYER_SPEED * dt;
        if (right) playerX += PLAYER_SPEED * dt;
        playerX = Math.max(PLAYER_MIN_X, Math.min(PLAYER_MAX_X, playerX));
        lean += ((right ? 1 : 0) - (left ? 1 : 0) - lean) * Math.min(1.0, dt * 10);
        if (up) playerY -= VERTICAL_SPEED * dt;
        if (down) playerY += VERTICAL_SPEED * dt;

        double base = (3.6 + day * 0.32) * 60;
        double factor = 1;
        if (jamDay) {
            if (t >= jamT0) factor = 1 - Math.min(1, (t - jamT0) / JAM_WINDOW);
        } else if (dur - t < SCHOOL_APPROACH) {
            factor = 0.2 + 0.8 * Math.max(0, dur - t) / SCHOOL_APPROACH;
        }
        double scroll = base * factor;
        groundScroll += scroll * dt;

        boolean canSpawn = jamDay ? t < jamT0 - 3.0 : dur - t > 5.5;
        if (canSpawn && now - lastSpawn > 820 - day * 35) {
            lastSpawn = now;
            trySpawnVehicle();
        }
        if (canSpawn && day == HOT_DAY && now - lastBolt > 2300) {
            lastBolt = now;
            trySpawnBolt();
        }
        if (jamDay && !jamBuilt && t >= jamT0) buildJam(base);
        if (day == HOT_DAY) energy = Math.max(0, energy - ENERGY_DRAIN * dt);

        double tau = Math.max(0, Math.min(JAM_WINDOW, t - jamT0));
        double jamShift = base * (tau - tau * tau / (2 * JAM_WINDOW));
        double minY = 330;
        for (Thing o : things) {
            if (o.jam) {
                o.y = o.y0 + jamShift;
                if (o.y > -120) minY = Math.max(minY, o.y + o.h / 2 + 34);
            } else {
                double rel = scroll * (1 - o.fwd);
                o.y += rel * dt;
                if (o.wobbleAmp > 0) {
                    double ph = o.wobblePhase + t * o.wobbleFreq * 2 * Math.PI;
                    o.x = o.baseX + o.wobbleAmp * Math.sin(ph);
                    double vx = o.wobbleAmp * o.wobbleFreq * 2 * Math.PI * Math.cos(ph);
                    o.angle = Math.max(-0.3, Math.min(0.3, Math.atan2(vx, Math.max(60, rel))));
                }
            }
        }
        playerY = Math.max(minY, Math.min(555, playerY));

        Iterator<Thing> iterator = things.iterator();
        while (iterator.hasNext()) {
            Thing o = iterator.next();
            if (!o.jam) {
                double hw = o.snack ? 22 : o.w / 2 + 9, hh = o.snack ? 28 : o.h / 2 + 6;
                if (Math.abs(o.x - playerX) < hw && Math.abs(o.y - playerY) < hh) {
                    if (o.snack) {
                        energy = Math.min(100, energy + BOLT_ENERGY);
                        score += 50;
                        iterator.remove();
                        continue;
                    }
                    if (!o.hit) {
                        o.hit = true;
                        if (invulnerable <= now) {
                            hp -= 22;
                            invulnerable = now + 950;
                            lastHitAt = now;
                            playerX += (playerX < o.x ? -1 : 1) * 10;
                            playerX = Math.max(PLAYER_MIN_X, Math.min(PLAYER_MAX_X, playerX));
                        }
                    }
                }
            }
            if (o.y - o.h / 2 > H + 30) iterator.remove();
        }
    }
    boolean clearToSpawn(Thing t) {
        for (Thing o : things) {
            if (Math.abs(o.y - t.y) < (o.h + t.h) / 2 + 40) {
                double a1 = t.baseX - t.wobbleAmp - t.w / 2, a2 = t.baseX + t.wobbleAmp + t.w / 2;
                double b1 = o.baseX - o.wobbleAmp - o.w / 2, b2 = o.baseX + o.wobbleAmp + o.w / 2;
                if (a2 + 10 > b1 && a1 - 10 < b2) return false;
            }
        }
        return true;
    }
    /** Xe chạy không đúng làn: có xe đè lên vạch (nửa làn này, nửa làn kia), có xe lạng lách. */
    boolean trySpawnVehicle() {
        int r = rng.nextInt(100);
        int kind = r < 28 ? 0 : r < 46 ? 1 : r < 58 ? 2 : r < 70 ? 3 : r < 80 ? 4 : r < 86 ? 5 : 6;
        if (day == 1) kind = rng.nextInt(100) < 32 ? 8 : (kind == 6 ? 0 : kind);   // ngày ngập: không xe máy, có thêm thuyền chèo
        for (int attempt = 0; attempt < 6; attempt++) {
            Thing t = new Thing();
            t.kind = kind;
            t.w = VEH_W[kind];
            t.h = VEH_H[kind];
            t.color = rng.nextInt(8);
            t.fwd = 0.35;
            t.y = -t.h / 2 - 12;
            double lo = kind == 6 ? ROAD_X + 14 + t.w / 2 : ROAD_X + 49 + t.w / 2;
            double hi = kind == 6 ? ROAD_X + ROAD_W - 14 - t.w / 2 : ROAD_X + ROAD_W - 49 - t.w / 2;
            int pattern = rng.nextInt(100);
            double bx;
            if (kind == 6 || pattern >= 72) bx = lo + rng.nextDouble() * (hi - lo);
            else if (pattern < 30) bx = laneX(rng.nextInt(3));
            else bx = dividerX(1 + rng.nextInt(2)) + (rng.nextDouble() - 0.5) * 16;
            double amp = rng.nextInt(100) < 35 ? 18 + rng.nextInt(22) : 0;
            bx = Math.max(lo + amp, Math.min(hi - amp, bx));
            t.baseX = bx;
            t.x = bx;
            t.wobbleAmp = amp;
            t.wobbleFreq = 0.7 + rng.nextDouble() * 0.7;
            t.wobblePhase = rng.nextDouble() * 6.28;
            if (clearToSpawn(t)) {
                things.add(t);
                return true;
            }
        }
        return false;
    }
    boolean trySpawnBolt() {
        for (int attempt = 0; attempt < 6; attempt++) {
            Thing t = new Thing();
            t.kind = 7;
            t.snack = true;
            t.w = 30;
            t.h = 40;
            t.baseX = t.x = ROAD_X + 75 + rng.nextDouble() * (ROAD_W - 150);
            t.y = -30;
            if (clearToSpawn(t)) {
                things.add(t);
                return true;
            }
        }
        return false;
    }
    /** Dựng các hàng xe nằm kín đường phía trước (ngày 5, trước mốc 1/3 quãng đường). */
    void buildJam(double base) {
        jamBuilt = true;
        double d = base * JAM_WINDOW / 2;
        double cursor = 372;
        int[] pool = {0, 1, 0, 2, 3, 1, 4, 0};
        while (cursor > -160) {
            double maxH = 0;
            java.util.List<Thing> row = new ArrayList<>();
            for (int l = 0; l < 3; l++) {
                Thing t = new Thing();
                t.kind = rng.nextInt(14) == 0 ? 5 : pool[rng.nextInt(pool.length)];
                t.w = VEH_W[t.kind];
                t.h = VEH_H[t.kind];
                t.color = rng.nextInt(8);
                t.x = t.baseX = laneX(l) + (rng.nextDouble() - 0.5) * 22;
                t.angle = (rng.nextDouble() - 0.5) * 0.1;
                t.jam = true;
                row.add(t);
                maxH = Math.max(maxH, t.h);
            }
            for (int sd = 0; sd < 2; sd++) {
                if (rng.nextInt(10) < 7) {
                    Thing m = new Thing();
                    m.kind = 6;
                    m.w = VEH_W[6];
                    m.h = VEH_H[6];
                    m.color = rng.nextInt(8);
                    m.x = m.baseX = trackX(sd == 0 ? 0 : 4) + (rng.nextDouble() - 0.5) * 8;
                    m.jam = true;
                    row.add(m);
                }
            }
            for (Thing t : row) {
                double finalY = cursor - t.h / 2;
                if (t.kind == 6) finalY = cursor - 30 - rng.nextInt(16);
                t.y0 = finalY - d;
                t.y = t.y0;
                things.add(t);
            }
            cursor -= maxH + 16;
        }
    }

    // =====================================================================
    //  CHUYỂN CẢNH: mờ dần → thẻ chữ → hội thoại
    // =====================================================================
    void startFade(int kind, long now) {
        fadeKind = kind;
        fadeStart = now;
        screen = FADE;
    }
    void endCard(long now) {
        if (cardNarration && dialogIndex < lines.size()) dialogIndex++;
        cardNarration = false;
        screen = DIALOG;
        fadeInStart = now;
    }
    void drawFade(Graphics2D g) {
        drawRoad(g);
        double a = Math.min(1.0, (System.currentTimeMillis() - fadeStart) / (double) FADE_MS);
        g.setColor(new Color(0, 0, 0, (int) (255 * a)));
        g.fillRect(0, 0, W, H);
    }
    void drawCard(Graphics2D g) {
        long tc = System.currentTimeMillis() - cardStart;
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, W, H);
        double a = Math.max(0, Math.min(1, Math.min(tc / 600.0, (CARD_MS - tc) / 600.0)));
        int al = (int) (255 * a);
        if (cardNarration) {
            g.setColor(new Color(236, 236, 242, al));
            g.setFont(new Font("Dialog", Font.ITALIC, 30));
            FontMetrics fm = g.getFontMetrics();
            int tw = fm.stringWidth(cardMain);
            if (tw <= 820) {
                g.drawString(cardMain, W / 2 - tw / 2, H / 2 + 10);
            } else {
                drawWrapped(g, cardMain, 70, H / 2 - 10, 820, 40);
            }
        } else {
            g.setColor(new Color(237, 119, 155, al));
            g.setFont(new Font("Dialog", Font.BOLD, 20));
            int tw = g.getFontMetrics().stringWidth(cardTop);
            g.drawString(cardTop, W / 2 - tw / 2, H / 2 - 46);
            java.util.Map<java.awt.font.TextAttribute, Object> attrs = new java.util.HashMap<>();
            attrs.put(java.awt.font.TextAttribute.TRACKING, 0.12f);
            g.setFont(new Font("Dialog", Font.BOLD, 56).deriveFont(attrs));
            g.setColor(new Color(255, 255, 255, al));
            int mw = g.getFontMetrics().stringWidth(cardMain);
            g.drawString(cardMain, W / 2 - mw / 2, H / 2 + 22);
            g.setColor(new Color(237, 119, 155, al));
            int lw = (int) (mw * Math.min(1.0, tc / 900.0));
            g.fillRoundRect(W / 2 - lw / 2, H / 2 + 44, lw, 4, 4, 4);
        }
    }

    // =====================================================================
    //  PHÁO GIẤY (kết thúc HE)
    // =====================================================================
    void burst(double x, double y, int n, double spMin, double spMax, double angDeg, double spreadDeg) {
        Color[] pal = {PINK, MINT, new Color(255, 214, 92), new Color(88, 160, 230),
                new Color(255, 140, 90), new Color(170, 120, 220), Color.WHITE};
        for (int i = 0; i < n && confetti.size() < 900; i++) {
            Confetti c = new Confetti();
            double a = Math.toRadians(angDeg + (rng.nextDouble() * 2 - 1) * spreadDeg);
            double sp = spMin + rng.nextDouble() * (spMax - spMin);
            c.x = x;
            c.y = y;
            c.vx = Math.cos(a) * sp;
            c.vy = Math.sin(a) * sp;
            c.rot = rng.nextDouble() * 6.28;
            c.vr = (rng.nextDouble() - 0.5) * 14;
            c.size = 6 + rng.nextDouble() * 7;
            c.shape = rng.nextInt(3);
            c.color = pal[rng.nextInt(pal.length)];
            confetti.add(c);
        }
    }
    void startConfetti(long now) {
        confetti.clear();
        pops.clear();
        lastBurst = now;
        lastConfettiAt = now;
        burst(40, H - 20, 130, 650, 1150, -62, 16);
        burst(W - 40, H - 20, 130, 650, 1150, -118, 16);
    }
    void updateConfetti(long now) {
        double dt = Math.min((now - lastConfettiAt) / 1000.0, 0.05);
        lastConfettiAt = now;
        if (now - lastBurst > 750) {
            lastBurst = now;
            double px = W * (0.15 + rng.nextDouble() * 0.7), py = H * (0.1 + rng.nextDouble() * 0.35);
            burst(px, py, 70, 160, 460, 0, 180);
            pops.add(new double[]{px, py, now});
            if (rng.nextInt(3) == 0) {
                burst(40, H - 20, 60, 650, 1100, -62, 14);
                burst(W - 40, H - 20, 60, 650, 1100, -118, 14);
            }
        }
        for (int i = 0; i < 2; i++) burst(rng.nextDouble() * W, -10, 1, 40, 120, 90, 10);
        pops.removeIf(p -> now - p[2] > 450);
        Iterator<Confetti> it = confetti.iterator();
        while (it.hasNext()) {
            Confetti c = it.next();
            double drag = Math.pow(0.2, dt);
            c.vx = c.vx * drag + Math.sin(c.rot) * 40 * dt;
            c.vy = c.vy * drag + 260 * dt;
            c.x += c.vx * dt;
            c.y += c.vy * dt;
            c.rot += c.vr * dt;
            if (c.y > H + 40) it.remove();
        }
    }
    void drawConfetti(Graphics2D g, long now) {
        for (double[] p : pops) {
            double age = (now - p[2]) / 450.0;
            g.setColor(new Color(255, 245, 190, (int) (200 * (1 - age))));
            g.setStroke(new BasicStroke(4f * (float) (1 - age) + 1f));
            double r = 14 + age * 80;
            g.draw(new Ellipse2D.Double(p[0] - r, p[1] - r, 2 * r, 2 * r));
        }
        g.setStroke(new BasicStroke(1f));
        AffineTransform old = g.getTransform();
        for (Confetti c : confetti) {
            g.translate(c.x, c.y);
            g.rotate(c.rot);
            g.setColor(c.color);
            double flip = Math.abs(Math.cos(c.rot * 1.7)) * 0.8 + 0.2;
            if (c.shape == 0) g.fill(new Rectangle2D.Double(-c.size / 2, -c.size * 0.3 * flip, c.size, c.size * 0.6 * flip));
            else if (c.shape == 1) g.fill(new Ellipse2D.Double(-c.size * 0.3, -c.size * 0.3 * flip, c.size * 0.6, c.size * 0.6 * flip));
            else g.fill(new Rectangle2D.Double(-c.size * 0.9, -1.5, c.size * 1.8, 3 * flip + 0.5));
            g.setTransform(old);
        }
    }
    void drawPopper(Graphics2D g, double x, double y, double rot) {
        AffineTransform old = g.getTransform();
        g.translate(x, y);
        g.rotate(rot);
        g.setColor(PINK);
        poly(g, -22, 0, 22, 0, 0, -70);
        g.setColor(new Color(255, 214, 92));
        poly(g, -14, -18, 14, -18, 10, -30);
        poly(g, -8, -42, 8, -42, 4, -52);
        g.setColor(new Color(160, 60, 100));
        g.fill(new Ellipse2D.Double(-22, -4, 44, 10));
        g.setTransform(old);
    }
    // =====================================================================
    //  NGÀY 2: đường ngập thành sông, chèo thuyền
    // =====================================================================
    void drawFlood(Graphics2D g, int rx0, int rx1, double s, long now) {
        int rw = rx1 - rx0;
        g.setPaint(new GradientPaint(rx0, 0, new Color(126, 112, 86, 234), rx1, 0, new Color(112, 100, 80, 234)));
        g.fillRect(rx0, 0, rw, H);
        Shape oldClip = g.getClip();
        g.clipRect(rx0, 0, rw, H);
        Stroke oldStroke = g.getStroke();
        // dòng nước chảy dọc theo đường
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int n = (int) Math.floor((s - H) / 120.0) - 1; n <= (int) Math.floor(s / 120.0) + 1; n++) {
            for (int k = 0; k < 3; k++) {
                int h = hash(n, 40 + k);
                double x = rx0 + 10 + h % (rw - 20);
                double y = s - (n + 1) * 120.0 + (h / 7) % 120;
                double len = 40 + h % 60;
                g.setColor(new Color(255, 244, 220, 40));
                g.draw(new Line2D.Double(x + Math.sin(now / 500.0 + h) * 3, y, x, y + len));
            }
        }
        // sóng nước
        g.setStroke(new BasicStroke(2f));
        for (double y = ((s * 0.8) % 46) - 46; y < H; y += 46) {
            Path2D.Double wave = new Path2D.Double();
            wave.moveTo(rx0, y);
            for (int x = rx0; x <= rx1; x += 16) wave.lineTo(x, y + Math.sin(now / 420.0 + x * 0.045 + y * 0.1) * 4);
            g.setColor(new Color(255, 255, 255, 34));
            g.draw(wave);
        }
        // gợn nước do mưa rơi
        g.setStroke(new BasicStroke(1.2f));
        for (int i = 0; i < 70; i++) {
            double cyc = (now / 800.0 + i * 0.371) % 1.0;
            int gen = (int) Math.floor(now / 800.0 + i * 0.371);
            int h = hash(gen, i);
            double x = rx0 + 8 + h % (rw - 16), y = (h / 13) % H;
            double rr = cyc * 13;
            g.setColor(new Color(235, 240, 245, (int) (150 * (1 - cyc))));
            g.draw(new Ellipse2D.Double(x - rr, y - rr * 0.6, 2 * rr, 1.2 * rr));
        }
        // rác, lá cây nổi trên nước
        for (int n = (int) Math.floor((s - H) / 160.0) - 1; n <= (int) Math.floor(s / 160.0) + 1; n++) {
            int h = hash(n, 55);
            if (h % 3 != 0) continue;
            double x = rx0 + 20 + (h / 3) % (rw - 40);
            double y = s - (n + 1) * 160.0 + (h / 11) % 160 + Math.sin(now / 600.0 + h) * 3;
            AffineTransform t0 = g.getTransform();
            g.translate(x, y);
            g.rotate((h % 628) / 100.0);
            if (h % 2 == 0) {
                g.setColor(new Color(86, 128, 70));
                g.fill(new Ellipse2D.Double(-8, -4, 16, 8));
                g.setColor(new Color(60, 96, 50));
                g.draw(new Line2D.Double(-8, 0, 8, 0));
            } else {
                g.setColor(new Color(225, 235, 245, 210));
                g.fill(new RoundRectangle2D.Double(-3, -9, 6, 18, 3, 3));
                g.setColor(new Color(81, 146, 185));
                g.fill(new Rectangle2D.Double(-2, -11, 4, 3));
            }
            g.setTransform(t0);
        }
        g.setClip(oldClip);
        // bọt nước sát vỉa hè
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int side = 0; side < 2; side++) {
            Path2D.Double foam = new Path2D.Double();
            double bx = side == 0 ? rx0 + 2 : rx1 - 2;
            foam.moveTo(bx, 0);
            for (int y = 0; y <= H; y += 14) foam.lineTo(bx + Math.sin(y * 0.06 + now / 400.0 + side * 2) * 2.5, y);
            g.setColor(new Color(244, 240, 228, 150));
            g.draw(foam);
        }
        g.setStroke(oldStroke);
    }
    void drawBoatWake(Graphics2D g, double sternY, long now) {
        Stroke oldStroke = g.getStroke();
        g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int k = 0; k < 3; k++) {
            double ph = ((now / 600.0) + k / 3.0) % 1.0;
            double yy = sternY + ph * 46;
            double spread = 10 + ph * 26;
            g.setColor(new Color(245, 248, 250, (int) (150 * (1 - ph))));
            g.draw(new Line2D.Double(-6, yy, -spread, yy + 12));
            g.draw(new Line2D.Double(6, yy, spread, yy + 12));
        }
        g.setStroke(oldStroke);
    }
    /** Người chơi ngày mưa lớn: Thắng chèo thuyền trên đường ngập. */
    void drawPlayerBoat(Graphics2D g, int px, int py) {
        long now = System.currentTimeMillis();
        Composite oldComposite = g.getComposite();
        AffineTransform oldTransform = g.getTransform();
        Stroke oldStroke = g.getStroke();
        if (invulnerable > now && (now / 90) % 2 == 0) {
            g.setComposite(AlphaComposite.SrcOver.derive(0.35f));
        }
        Color orange = new Color(255, 143, 66), orangeD = new Color(214, 104, 36);
        Color jacket = new Color(45, 61, 87), skin = new Color(255, 224, 196);
        g.translate(px, py);
        g.scale(0.6, 0.6);
        g.rotate(lean * 0.1);
        drawBoatWake(g, 52, now);
        g.setColor(new Color(0, 0, 0, 50));
        g.fill(new Ellipse2D.Double(-28, -44, 56, 104));
        Path2D.Double hull = new Path2D.Double();
        hull.moveTo(0, -56);
        hull.quadTo(26, -30, 23, 10);
        hull.quadTo(21, 44, 13, 54);
        hull.lineTo(-13, 54);
        hull.quadTo(-21, 44, -23, 10);
        hull.quadTo(-26, -30, 0, -56);
        hull.closePath();
        g.setColor(orange);
        g.fill(hull);
        g.setColor(orangeD);
        g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(hull);
        g.scale(0.74, 0.8);
        g.setColor(new Color(150, 92, 52));
        g.fill(hull);
        g.scale(1 / 0.74, 1 / 0.8);
        g.setColor(new Color(176, 112, 66));
        for (int k = -2; k <= 3; k++) g.fill(new Rectangle2D.Double(-15, k * 13 - 3, 30, 2));
        g.setColor(PINK);
        poly(g, 0, -56, -7, -44, 7, -44);
        // người chèo: ba lô, thân, mũ bảo hiểm
        g.setColor(MINT);
        g.fill(new RoundRectangle2D.Double(-9, 6, 18, 20, 9, 9));
        g.setColor(new Color(63, 145, 121));
        g.fill(new RoundRectangle2D.Double(-6, 15, 12, 7, 5, 5));
        g.setColor(jacket);
        g.fill(new Ellipse2D.Double(-14, -10, 28, 22));
        // mái chèo
        double swing = Math.sin(now / 240.0) * 0.38;
        AffineTransform t0 = g.getTransform();
        g.translate(0, -8);
        g.rotate(swing);
        g.setColor(new Color(176, 132, 90));
        g.setStroke(new BasicStroke(3.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(-40, 0, 40, 0));
        g.setColor(PINK);
        g.fill(new Ellipse2D.Double(-50, -6, 15, 26));
        g.fill(new Ellipse2D.Double(35, -6, 15, 26));
        g.setColor(skin);
        g.fill(new Ellipse2D.Double(-17, -4, 8, 8));
        g.fill(new Ellipse2D.Double(9, -4, 8, 8));
        g.setTransform(t0);
        g.setColor(new Color(235, 242, 248, 150));
        g.setStroke(new BasicStroke(1.6f));
        double dip = swing > 0 ? 1 : -1;
        g.draw(new Ellipse2D.Double(dip * 42 - 10, 10, 20, 9));
        g.setColor(new Color(52, 40, 44));
        g.fill(new Ellipse2D.Double(-8, -17, 16, 9));
        g.setColor(new Color(250, 250, 252));
        g.fill(new Ellipse2D.Double(-11, -27, 22, 23));
        g.setColor(new Color(200, 208, 222));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(new Ellipse2D.Double(-11, -27, 22, 23));
        g.setColor(PINK);
        g.fill(new RoundRectangle2D.Double(-2, -26, 4, 20, 3, 3));
        g.setTransform(oldTransform);
        g.setComposite(oldComposite);
        g.setStroke(oldStroke);
    }
    /** Người chèo thuyền khác (chỉ có ở ngày mưa lớn), đội nón lá. */
    void drawNpcBoat(Graphics2D g, Thing o) {
        long now = System.currentTimeMillis();
        Color[] hulls = {
            new Color(176, 124, 90), new Color(81, 146, 185), new Color(94, 170, 128), new Color(219, 93, 96)
        };
        Color hullC = hulls[o.color % 4];
        Color[] jackets = {
            new Color(219, 93, 96), new Color(81, 146, 185), new Color(240, 200, 90), new Color(94, 170, 128),
            new Color(150, 110, 180), new Color(58, 62, 76), new Color(240, 242, 246), new Color(255, 143, 66)
        };
        Color jacket = jackets[(o.color + 2) % 8];
        Stroke oldStroke = g.getStroke();
        double sc = o.h / 112.0;
        g.scale(sc, sc);
        drawBoatWake(g, 52, now);
        g.setColor(new Color(0, 0, 0, 50));
        g.fill(new Ellipse2D.Double(-28, -48, 56, 108));
        Path2D.Double hull = new Path2D.Double();
        hull.moveTo(0, -56);
        hull.quadTo(26, -30, 23, 10);
        hull.quadTo(21, 44, 13, 54);
        hull.lineTo(-13, 54);
        hull.quadTo(-21, 44, -23, 10);
        hull.quadTo(-26, -30, 0, -56);
        hull.closePath();
        g.setColor(hullC);
        g.fill(hull);
        g.setColor(shade(hullC, 0.72));
        g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(hull);
        g.scale(0.74, 0.8);
        g.setColor(shade(hullC, 0.66));
        g.fill(hull);
        g.scale(1 / 0.74, 1 / 0.8);
        g.setColor(jacket);
        g.fill(new Ellipse2D.Double(-14, -6, 28, 22));
        double swing = Math.sin(now / 300.0 + o.color) * 0.4;
        AffineTransform t0 = g.getTransform();
        g.translate(0, 0);
        g.rotate(swing);
        g.setColor(new Color(150, 110, 76));
        g.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(-40, 0, 40, 0));
        g.setColor(new Color(236, 230, 214));
        g.fill(new Ellipse2D.Double(-50, -6, 15, 26));
        g.fill(new Ellipse2D.Double(35, -6, 15, 26));
        g.setTransform(t0);
        g.setColor(new Color(232, 208, 150));
        g.fill(new Ellipse2D.Double(-16, -22, 32, 32));
        g.setColor(new Color(196, 166, 110));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(new Ellipse2D.Double(-16, -22, 32, 32));
        g.draw(new Ellipse2D.Double(-10.5, -16.5, 21, 21));
        g.draw(new Ellipse2D.Double(-5, -11, 10, 10));
        g.setColor(new Color(150, 60, 70));
        g.fill(new Ellipse2D.Double(-2.5, -8.5, 5, 5));
        g.setStroke(oldStroke);
    }
    // =====================================================================
    //  CUỘC GỌI: màn hình điện thoại đang gọi (kiểu iPhone) ở đoạn tắc đường
    // =====================================================================
    void findCallRange() {
        callStart = -1;
        callEnd = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (callStart < 0 && lines.get(i).who.contains("(điện thoại)")) callStart = i;
            else if (callStart >= 0 && lines.get(i).text.startsWith("Một lúc sau, tiếng cánh quạt")) {
                callEnd = i;
                break;
            }
        }
        if (callStart >= 0 && callEnd < 0) callEnd = lines.size();
    }
    boolean isCallLine(int idx) {
        if (idx < 0 || idx >= lines.size()) return false;
        if (lines.get(idx).who.contains("(loa ngoài)")) return true;
        return callStart >= 0 && idx >= callStart && idx < callEnd;
    }
    void callIcon(Graphics2D g, int type, double cx, double cy, Color c) {
        Stroke old = g.getStroke();
        g.setColor(c);
        g.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (type) {
            case 0:
                g.fill(new RoundRectangle2D.Double(cx - 5, cy - 14, 10, 17, 10, 10));
                g.draw(new Arc2D.Double(cx - 9, cy - 8, 18, 18, 200, 140, Arc2D.OPEN));
                g.draw(new Line2D.Double(cx, cy + 10, cx, cy + 15));
                g.draw(new Line2D.Double(cx - 12, cy + 14, cx + 12, cy - 14));
                break;
            case 1:
                for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++) g.fill(new Ellipse2D.Double(cx - 12 + i * 10, cy - 12 + j * 10, 5, 5));
                break;
            case 2:
                poly(g, cx - 13, cy - 4, cx - 6, cy - 4, cx + 2, cy - 11, cx + 2, cy + 11, cx - 6, cy + 4, cx - 13, cy + 4);
                g.draw(new Arc2D.Double(cx - 4, cy - 8, 14, 16, -55, 110, Arc2D.OPEN));
                g.draw(new Arc2D.Double(cx - 8, cy - 14, 24, 28, -55, 110, Arc2D.OPEN));
                break;
            case 3:
                g.draw(new Line2D.Double(cx, cy - 12, cx, cy + 12));
                g.draw(new Line2D.Double(cx - 12, cy, cx + 12, cy));
                break;
            case 4:
                g.fill(new RoundRectangle2D.Double(cx - 14, cy - 8, 18, 16, 5, 5));
                poly(g, cx + 6, cy - 3, cx + 15, cy - 9, cx + 15, cy + 9, cx + 6, cy + 3);
                break;
            default:
                g.fill(new Ellipse2D.Double(cx - 6, cy - 14, 12, 12));
                g.fill(new Arc2D.Double(cx - 13, cy + 1, 26, 22, 0, 180, Arc2D.PIE));
                break;
        }
        g.setStroke(old);
    }
    void drawCallPhone(Graphics2D g) {
        long now = System.currentTimeMillis();
        if (callStartedAt == 0) callStartedAt = now;
        Line cur = lines.get(dialogIndex);
        boolean speaker = cur.who.contains("(loa ngoài)");
        boolean contactSpeaking = cur.who.startsWith("Nam");
        boolean ringing = dialogIndex <= callStart;
        int pw = 330, ph = 566;
        double sc = 0.66;
        AffineTransform old = g.getTransform();
        Shape oldClip = g.getClip();
        Stroke oldStroke = g.getStroke();
        g.translate((W - pw * sc) / 2.0, 14);
        g.scale(sc, sc);
        g.setColor(new Color(0, 0, 0, 70));
        g.fill(new RoundRectangle2D.Double(6, 10, pw, ph, 48, 48));
        g.setColor(new Color(24, 27, 38));
        g.fill(new RoundRectangle2D.Double(0, 0, pw, ph, 48, 48));
        g.setColor(new Color(60, 66, 84));
        g.fillRoundRect(pw - 2, 110, 4, 56, 3, 3);
        g.fillRoundRect(-2, 96, 4, 30, 3, 3);
        g.fillRoundRect(-2, 136, 4, 30, 3, 3);
        Shape screen = new RoundRectangle2D.Double(10, 10, pw - 20, ph - 20, 38, 38);
        g.setPaint(new GradientPaint(0, 10, new Color(88, 106, 152), 0, ph - 10, new Color(30, 34, 56)));
        g.fill(screen);
        g.clip(screen);
        g.setColor(new Color(160, 130, 215, 70));
        g.fill(new Ellipse2D.Double(-50, 70, 270, 270));
        g.setColor(new Color(90, 175, 215, 60));
        g.fill(new Ellipse2D.Double(130, 250, 290, 290));
        // thanh trạng thái
        g.setColor(Color.WHITE);
        g.setFont(new Font("Dialog", Font.BOLD, 14));
        g.drawString(chatTime(), 36, 37);
        for (int i = 0; i < 4; i++) g.fillRoundRect(pw - 94 + i * 5, 33 - i * 3, 3, 4 + i * 3, 1, 1);
        g.setStroke(new BasicStroke(1.3f));
        g.drawRoundRect(pw - 60, 26, 22, 11, 4, 4);
        g.fillRoundRect(pw - 57, 29, 14, 5, 2, 2);
        // tên người gọi + trạng thái cuộc gọi
        g.setFont(new Font("Dialog", Font.BOLD, 38));
        g.drawString("Nam", pw / 2 - g.getFontMetrics().stringWidth("Nam") / 2, 118);
        g.setFont(new Font("Dialog", Font.PLAIN, 18));
        g.setColor(new Color(255, 255, 255, 185));
        long sec = Math.max(0, (now - callStartedAt) / 1000);
        String status = ringing ? "đang gọi…" : String.format("%02d:%02d", sec / 60, sec % 60);
        g.drawString(status, pw / 2 - g.getFontMetrics().stringWidth(status) / 2, 146);
        // ảnh đại diện (sóng âm khi Nam đang nói)
        if (contactSpeaking) {
            g.setStroke(new BasicStroke(3f));
            for (int k = 0; k < 2; k++) {
                double ph2 = ((now / 800.0) + k * 0.5) % 1.0;
                double rr = 64 + ph2 * 26;
                g.setColor(new Color(255, 255, 255, (int) (110 * (1 - ph2))));
                g.draw(new Ellipse2D.Double(pw / 2.0 - rr, 238 - rr, 2 * rr, 2 * rr));
            }
        }
        drawAvatar(g, "Nam", pw / 2, 238, 58);
        // lưới nút điều khiển
        String[] labels = {"Tắt tiếng", "Bàn phím", "Loa", "Thêm", "FaceTime", "Danh bạ"};
        g.setStroke(new BasicStroke(1f));
        for (int i = 0; i < 6; i++) {
            double cx = 70 + (i % 3) * 95, cy = 336 + (i / 3) * 84;
            boolean active = i == 2 && speaker;
            g.setColor(active ? Color.WHITE : new Color(255, 255, 255, 52));
            g.fill(new Ellipse2D.Double(cx - 30, cy - 30, 60, 60));
            callIcon(g, i, cx, cy, active ? INK : Color.WHITE);
            g.setFont(new Font("Dialog", Font.PLAIN, 14));
            g.setColor(new Color(255, 255, 255, 215));
            g.drawString(labels[i], (int) (cx - g.getFontMetrics().stringWidth(labels[i]) / 2.0), (int) (cy + 50));
        }
        // nút kết thúc cuộc gọi
        g.setColor(new Color(255, 59, 48));
        g.fill(new Ellipse2D.Double(pw / 2.0 - 31, 476, 62, 62));
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D.Double handset = new Path2D.Double();
        handset.moveTo(pw / 2.0 - 17, 512);
        handset.quadTo(pw / 2.0, 494, pw / 2.0 + 17, 512);
        g.draw(handset);
        g.fill(new RoundRectangle2D.Double(pw / 2.0 - 23, 508, 10, 10, 4, 4));
        g.fill(new RoundRectangle2D.Double(pw / 2.0 + 13, 508, 10, 10, 4, 4));
        g.setColor(new Color(10, 12, 18));
        g.fillRoundRect(pw / 2 - 42, 18, 84, 22, 22, 22);
        g.setTransform(old);
        g.setClip(oldClip);
        g.setStroke(oldStroke);
    }

    // =====================================================================
    //  MÀN HÌNH GIỚI THIỆU NHÂN VẬT
    // =====================================================================
    void drawIntro(Graphics2D g) {
        g.setColor(new Color(255, 232, 238));
        g.fillRect(0, 0, W, H);
        g.setColor(new Color(255, 255, 255, 110));
        g.fillOval(-80, 440, 360, 360);
        g.fillOval(720, -120, 380, 380);
        g.setColor(INK);
        g.setFont(new Font("Dialog", Font.BOLD, 30));
        g.drawString("GIỚI THIỆU NHÂN VẬT", 60, 56);
        g.setColor(PINK);
        g.fillRoundRect(60, 66, 150, 4, 4, 4);
        String[] names = {"Sanbaka", "Thắng", "Nam", "Chủ trọ"};
        String[] tags = {"Nữ chính", "Nam chính", "Bạn cùng phòng", ""};
        String[] desc = {
            "Nữ chính của trò chơi, một tân sinh viên ngành Toán tin tại HUST. Cô có tính cách dịu dàng, chu đáo, thông minh và đôi khi tinh nghịch.",
            "Nhân vật trung tâm của trò chơi, do người chơi điều khiển, là một sinh viên ngành Khoa học máy tính tại HUST. Cậu có tính cách vui vẻ, thân thiện, đôi khi hơi hậu đậu và có thói quen ngủ nướng.",
            "Một sinh viên cùng ngành, sống chung nhà trọ với Thắng. Nhân vật có tính cách hài hước, hợp cạ với Thắng. Dù thường thể hiện thái độ bông đùa, cậu vẫn là người bạn quan tâm đến Thắng và sẵn sàng giúp đỡ khi cần thiết.",
            "Là nhân vật phụ xuất hiện tại khu nhà trọ nơi Thắng sinh sống. Nhân vật có tính cách thẳng thắn, gần gũi, thường đưa ra những lời khuyên thiết thực cho Thắng trước khi cậu bắt đầu hành trình đến trường."
        };
        for (int i = 0; i < 4; i++) {
            int ry = 84 + i * 128;
            panel(g, 50, ry, 860, 120, new Color(255, 255, 255, 245));
            drawAvatar(g, names[i], 118, ry + 60, 42);
            g.setFont(new Font("Dialog", Font.BOLD, 21));
            g.setColor(PINK);
            g.drawString(names[i], 184, ry + 34);
            if (!tags[i].isEmpty()) {
                int nw = g.getFontMetrics().stringWidth(names[i]);
                g.setFont(new Font("Dialog", Font.BOLD, 12));
                int tw = g.getFontMetrics().stringWidth(tags[i]);
                g.setColor(MINT);
                g.fillRoundRect(184 + nw + 12, ry + 18, tw + 20, 22, 22, 22);
                g.setColor(Color.WHITE);
                g.drawString(tags[i], 184 + nw + 22, ry + 34);
            }
            int fs = 15;
            Font f = new Font("Dialog", Font.PLAIN, fs);
            java.util.List<String> rows = wrap(g.getFontMetrics(f), desc[i], 690);
            if (rows.size() > 3) {
                fs = 13;
                f = new Font("Dialog", Font.PLAIN, fs);
                rows = wrap(g.getFontMetrics(f), desc[i], 690);
            }
            g.setFont(f);
            g.setColor(INK);
            int step = rows.size() > 3 ? 19 : 22;
            for (int k = 0; k < rows.size(); k++) g.drawString(rows.get(k), 184, ry + 62 + k * step);
        }
        g.setColor(MINT);
        g.setFont(new Font("Dialog", Font.BOLD, 16));
        String hint = "ENTER / SPACE  ·  BẮT ĐẦU";
        g.drawString(hint, W / 2 - g.getFontMetrics().stringWidth(hint) / 2, 626);
    }
    void drawEnding(Graphics2D g, boolean happy) {
        long now = System.currentTimeMillis();
        g.setColor(happy ? new Color(255, 228, 234) : new Color(221, 230, 239));
        g.fillRect(0, 0, W, H);
        if (happy) {
            drawPopper(g, 36, H - 6, 0.5);
            drawPopper(g, W - 36, H - 6, -0.5);
        }
        panel(g, 90, 120, 780, 390, Color.WHITE);
        g.setColor(happy ? PINK : new Color(91, 110, 139));
        g.setFont(new Font("Dialog", Font.BOLD, 15));
        g.drawString(happy ? "HAPPY ENDING" : "BAD ENDING", 145, 168);
        g.setFont(new Font("Dialog", Font.BOLD, 38));
        g.drawString(happy ? "LỜI HẸN Ở C1" : "CỔ ĐÔNG CỦA TRƯỜNG", 145, 212);
        g.setColor(INK);
        g.setFont(new Font("Dialog", Font.PLAIN, 20));
        if (!happy) {
            drawWrapped(g, "Bạn đã tạch môn, chúc mừng bạn đã trở thành 1 cổ đông của trường, "
                    + "đóng góp cho sự phát triển của trường ta!", 145, 275, 660, 32);
            g.setColor(new Color(91, 110, 139));
            g.setFont(new Font("Dialog", Font.ITALIC, 17));
            g.drawString(beCause, 145, 380);
        } else {
            g.drawString("Một tuần thi vất vả đã khép lại.", 145, 275);
            g.drawString("Chúc mừng bạn đã được đi hẹn hò cùng Sanbaka.", 145, 315);
            drawHeart(g, 780, 260, 90, PINK);
        }
        g.setColor(MINT);
        g.setFont(new Font("Dialog", Font.BOLD, 17));
        g.drawString(happy ? "ENTER / SPACE · CHƠI LẠI" : "ENTER / SPACE · CHƠI LẠI NGÀY " + (day + 1), 145, 455);
        if (happy) drawConfetti(g, now);
    }
    @Override
    protected void paintComponent(Graphics gg) {
        super.paintComponent(gg);
        Graphics2D g=(Graphics2D)gg;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(CREAM);
        g.fillRect(0,0,W,H);
        if (screen == TITLE) drawTitle(g);
        else if (screen == ROAD) drawRoad(g);
        else if (screen == INTRO) drawIntro(g);
        else if (screen == FADE) drawFade(g);
        else if (screen == CARD) drawCard(g);
        else if (screen == ROOM) {
            drawRoom(g);
            drawCurrentDialog(g);
        }
        else if (screen == EXAM) drawExam(g);
        else if (screen == ENDING_HE) drawEnding(g, true);
        else if (screen == ENDING_BE) drawEnding(g, false);
        else if (screen == AIR) drawAir(g);
        else drawDialog(g);
        long fadeIn = System.currentTimeMillis() - fadeInStart;
        if (fadeIn >= 0 && fadeIn < 700) {
            g.setColor(new Color(0, 0, 0, (int) (255 * (1 - fadeIn / 700.0))));
            g.fillRect(0, 0, W, H);
        }
    }
    void panel(Graphics2D g,int x,int y,int w,int h,Color c) {
        g.setColor(new Color(27,36,53,35));
        g.fill(new RoundRectangle2D.Double(x+5,y+7,w,h,24,24));
        g.setColor(c);
        g.fill(new RoundRectangle2D.Double(x,y,w,h,24,24));
    }
    void drawTitle(Graphics2D g) {
        g.setColor(new Color(255,224,231));
        g.fillRect(0,0,W,H);
        g.setColor(new Color(255,245,233));
        g.fillOval(580,-130,480,480);
        g.setColor(new Color(255,255,255,90));
        for(int i=0;i<7;i++)g.fillOval(50+i*150,70+(i%2)*70,100,60);
        g.setColor(INK);
        g.setFont(new Font("Dialog",Font.BOLD,46));
        g.drawString("TUẦN THI CỦA SVBK",88,242);
        g.setFont(new Font("Dialog",Font.PLAIN,20));
        g.drawString("Một tuần thi, một lời hứa, và người bạn cùng phòng rất đáng ngờ.",90,291);
        drawSideBiker(g, 800, 212, 0.85);
        panel(g, 452, 44, 290, 62, new Color(255, 255, 255, 240));
        drawAvatar(g, "Sanbaka", 488, 75, 22);
        g.setColor(PINK);
        g.setFont(new Font("Dialog", Font.BOLD, 12));
        g.drawString("Sanbaka", 520, 67);
        g.setColor(INK);
        g.setFont(new Font("Dialog", Font.PLAIN, 14));
        g.drawString("Anh nhớ đến C1 đúng giờ nhé!", 520, 88);
        panel(g,90,360,780,155,Color.WHITE);
        g.setFont(new Font("Dialog",Font.BOLD,19));
        g.setColor(PINK);
        g.drawString("CÁCH CHƠI",122,405);
        g.setColor(INK);
        g.setFont(new Font("Dialog",Font.PLAIN,17));
        g.drawString("← → hoặc A / D: đổi làn    ↑ ↓ hoặc W / S: di chuyển dọc đường",122,440);
        g.drawString("Né xe cộ (có xe chạy lấn làn!) và đừng để HP về 0.",122,470);
        g.drawString("Ngày nắng nóng: năng lượng tự giảm, hãy nhặt tia sét để hồi lại.",122,500);
        g.setFont(new Font("Dialog",Font.BOLD,18));
        g.setColor(MINT);
        g.drawString("Mỗi chặng đường kéo dài 40 giây · Enter / Space để bắt đầu",122,548);
    }
    boolean roomAwake() {
        return dialogIndex < lines.size() && lines.get(dialogIndex).who.startsWith("Thắng");
    }
    void drawWindowView(Graphics2D g, int x, int y, int w, int h, int d, long now) {
        Color[] tops = {
            new Color(120, 190, 240), new Color(86, 106, 136), new Color(190, 205, 222),
            new Color(255, 170, 90), new Color(160, 196, 230)
        };
        Color[] bots = {
            new Color(200, 232, 250), new Color(130, 148, 172), new Color(226, 234, 242),
            new Color(255, 226, 160), new Color(222, 236, 248)
        };
        g.setPaint(new GradientPaint(x, y, tops[d], x, y + h, bots[d]));
        g.fillRect(x, y, w, h);
        g.setColor(new Color(150, 168, 196, d == 1 ? 200 : 150));
        for (int i = 0; i < 7; i++) g.fillRect(x + i * 30 - 8, y + h - (34 + (i * 37) % 46), 26, 90);
        if (d == 0 || d == 4) {
            drawCloud(g, x + 20 + (now / 90.0) % 40, y + 28, 0.7, 230);
            drawCloud(g, x + 110, y + 62, 0.5, 200);
        }
        if (d == 0) {
            g.setColor(new Color(255, 238, 160));
            g.fill(new Ellipse2D.Double(x + w - 62, y + 10, 40, 40));
        }
        if (d == 1) {
            g.setColor(new Color(92, 100, 120, 230));
            g.fill(new Ellipse2D.Double(x - 20, y - 24, 140, 56));
            g.fill(new Ellipse2D.Double(x + 80, y - 30, 150, 60));
            g.setColor(new Color(205, 228, 255, 190));
            for (int i = 0; i < 26; i++) {
                double rx = x + (i * 37 + now / 6.0) % (w + 20);
                double ry = y + (i * 53 + now / 3.0) % (h + 20);
                g.draw(new Line2D.Double(rx, ry, rx - 4, ry + 12));
            }
        }
        if (d == 2) {
            g.setColor(new Color(255, 255, 255, 170));
            for (int i = 0; i < 18; i++) {
                double sx = x + (i * 41 + Math.sin(now / 700.0 + i) * 8 + w) % w;
                double sy = y + (i * 29 + now / 25.0) % h;
                g.fill(new Ellipse2D.Double(sx, sy, 4, 4));
            }
            g.setColor(new Color(255, 255, 255, 120));
            poly(g, x, y, x + 46, y, x, y + 40);
            poly(g, x + w, y + h, x + w - 52, y + h, x + w, y + h - 46);
        }
        if (d == 3) {
            for (int i = 3; i >= 0; i--) {
                double r = 26 + i * 14;
                g.setColor(new Color(255, 240, 160, 40 + (3 - i) * 40));
                g.fill(new Ellipse2D.Double(x + w * 0.7 - r, y + 36 - r, 2 * r, 2 * r));
            }
        }
    }
    void drawCurtain(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(new Color(244, 170, 190));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 18, 18));
        g.setColor(new Color(224, 130, 158));
        for (int i = 0; i < 3; i++) g.fill(new RoundRectangle2D.Double(x + 8 + i * 14, y + 8, 5, h - 18, 4, 4));
    }
    void drawRoom(Graphics2D g) {
        long now = System.currentTimeMillis();
        int d = Math.max(0, Math.min(day, 4));
        int floorY = 348;
        Stroke oldStroke = g.getStroke();
        // tường + sàn gỗ + thảm
        g.setPaint(new GradientPaint(0, 0, new Color(255, 244, 226), 0, floorY, new Color(250, 224, 200)));
        g.fillRect(0, 0, W, floorY);
        g.setColor(new Color(255, 255, 255, 40));
        for (int x = 0; x < W; x += 48) g.fillRect(x, 0, 24, floorY);
        g.setColor(new Color(214, 170, 130));
        g.fillRect(0, floorY, W, H - floorY);
        g.setColor(new Color(192, 146, 108));
        for (int row = 0, y = floorY + 16; y < H; row++, y += 26) {
            g.fillRect(0, y, W, 2);
            for (int x = (row % 2) * 75; x < W; x += 150) g.fillRect(x, y - 24, 2, 24);
        }
        g.setColor(new Color(150, 104, 76));
        g.fillRect(0, floorY - 10, W, 12);
        g.setColor(new Color(255, 255, 255, 60));
        g.fillRect(0, floorY - 10, W, 2);
        g.setColor(new Color(244, 170, 190));
        g.fill(new Ellipse2D.Double(150, 356, 380, 48));
        g.setColor(new Color(255, 226, 232));
        g.fill(new Ellipse2D.Double(176, 362, 328, 36));

        // cửa sổ + rèm
        int wx = 650, wy = 70, ww = 190, wh = 140;
        g.setColor(new Color(160, 112, 82));
        g.fill(new RoundRectangle2D.Double(wx - 12, wy - 12, ww + 24, wh + 24, 14, 14));
        Shape oldClip = g.getClip();
        g.clip(new Rectangle2D.Double(wx, wy, ww, wh));
        drawWindowView(g, wx, wy, ww, wh, d, now);
        g.setClip(oldClip);
        g.setColor(new Color(160, 112, 82));
        g.fillRect(wx + ww / 2 - 4, wy, 8, wh);
        g.fillRect(wx, wy + wh / 2 - 4, ww, 8);
        g.setColor(new Color(130, 90, 66));
        g.fill(new RoundRectangle2D.Double(wx - 20, wy + wh + 8, ww + 40, 12, 8, 8));
        drawCurtain(g, wx - 44, wy - 20, 56, wh + 52);
        drawCurtain(g, wx + ww - 12, wy - 20, 56, wh + 52);
        g.setColor(new Color(110, 78, 60));
        g.fill(new RoundRectangle2D.Double(wx - 58, wy - 30, ww + 116, 8, 8, 8));

        // ảnh đôi, lịch tuần thi, đồng hồ
        int fx = 118, fy = 112;
        g.setColor(new Color(150, 104, 76));
        g.fill(new RoundRectangle2D.Double(fx, fy, 84, 96, 10, 10));
        g.setColor(new Color(255, 246, 236));
        g.fill(new RoundRectangle2D.Double(fx + 7, fy + 7, 70, 82, 6, 6));
        drawAvatar(g, "Thắng", fx + 27, fy + 36, 17);
        drawAvatar(g, "Sanbaka", fx + 57, fy + 36, 17);
        drawHeart(g, fx + 42, fy + 72, 16, PINK);

        int cx0 = 236, cy0 = 112;
        g.setColor(new Color(0, 0, 0, 25));
        g.fill(new RoundRectangle2D.Double(cx0 + 3, cy0 + 4, 132, 96, 12, 12));
        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Double(cx0, cy0, 132, 96, 12, 12));
        g.setColor(PINK);
        g.fill(new RoundRectangle2D.Double(cx0, cy0, 132, 26, 12, 12));
        g.fillRect(cx0, cy0 + 14, 132, 12);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Dialog", Font.BOLD, 13));
        g.drawString("TUẦN THI", cx0 + 66 - g.getFontMetrics().stringWidth("TUẦN THI") / 2, cy0 + 18);
        for (int i = 0; i < 5; i++) {
            double ccx = cx0 + 22 + i * 22, ccy = cy0 + 52;
            if (i < d) {
                g.setColor(new Color(226, 230, 238));
                g.fill(new Ellipse2D.Double(ccx - 9, ccy - 9, 18, 18));
                g.setColor(new Color(120, 190, 160));
                g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Path2D.Double tick = new Path2D.Double();
                tick.moveTo(ccx - 4, ccy);
                tick.lineTo(ccx - 1, ccy + 3.5);
                tick.lineTo(ccx + 5, ccy - 4);
                g.draw(tick);
                g.setStroke(oldStroke);
            } else if (i == d) {
                double pulse = 1 + 0.08 * Math.sin(now / 260.0);
                g.setColor(PINK);
                g.fill(new Ellipse2D.Double(ccx - 10 * pulse, ccy - 10 * pulse, 20 * pulse, 20 * pulse));
                g.setColor(Color.WHITE);
                g.setFont(new Font("Dialog", Font.BOLD, 12));
                g.drawString("" + (i + 1), (float) (ccx - 3.5), (float) (ccy + 4.5));
            } else {
                g.setColor(new Color(190, 196, 208));
                g.setStroke(new BasicStroke(1.5f));
                g.draw(new Ellipse2D.Double(ccx - 9, ccy - 9, 18, 18));
                g.setStroke(oldStroke);
                g.setFont(new Font("Dialog", Font.PLAIN, 12));
                g.drawString("" + (i + 1), (float) (ccx - 3.5), (float) (ccy + 4.5));
            }
        }
        g.setColor(new Color(120, 126, 140));
        g.setFont(new Font("Dialog", Font.PLAIN, 12));
        String calNote = d == 4 ? "Ngày cuối rồi, cố lên!" : "Ngày " + (d + 1) + " / 5";
        g.drawString(calNote, cx0 + 66 - g.getFontMetrics().stringWidth(calNote) / 2, cy0 + 84);

        double clockX = 470, clockY = 150;
        g.setColor(new Color(150, 104, 76));
        g.fill(new Ellipse2D.Double(clockX - 31, clockY - 31, 62, 62));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(clockX - 26, clockY - 26, 52, 52));
        g.setColor(new Color(150, 156, 170));
        for (int k = 0; k < 12; k++) {
            double ang = k * Math.PI / 6;
            g.fill(new Ellipse2D.Double(clockX + 21 * Math.sin(ang) - 1.5, clockY - 21 * Math.cos(ang) - 1.5, 3, 3));
        }
        double minute = 30 + d;
        double minAng = minute * Math.PI / 30, hourAng = (6 + minute / 60.0) * Math.PI / 6;
        g.setColor(INK);
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(clockX, clockY, clockX + 12 * Math.sin(hourAng), clockY - 12 * Math.cos(hourAng)));
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(clockX, clockY, clockX + 19 * Math.sin(minAng), clockY - 19 * Math.cos(minAng)));
        g.setStroke(oldStroke);

        // giường + Thắng
        int bx = 70, by = 232;
        g.setColor(new Color(150, 104, 76));
        g.fill(new RoundRectangle2D.Double(bx, by, 26, 114, 14, 14));
        g.fill(new RoundRectangle2D.Double(bx + 372, by + 54, 22, 60, 10, 10));
        g.setColor(new Color(176, 124, 90));
        g.fill(new RoundRectangle2D.Double(bx + 10, by + 76, 376, 38, 12, 12));
        g.setColor(new Color(247, 242, 232));
        g.fill(new RoundRectangle2D.Double(bx + 20, by + 52, 360, 40, 20, 20));
        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Double(bx + 30, by + 38, 80, 38, 26, 26));
        g.setColor(new Color(226, 222, 236));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(new RoundRectangle2D.Double(bx + 30, by + 38, 80, 38, 26, 26));
        g.setStroke(oldStroke);
        boolean awake = roomAwake();
        g.setColor(new Color(255, 224, 196));
        g.fill(new Ellipse2D.Double(bx + 50, by + 22, 38, 38));
        g.setColor(new Color(52, 40, 44));
        g.fill(new Arc2D.Double(bx + 48, by + 19, 42, 34, 0, 180, Arc2D.CHORD));
        g.setColor(new Color(255, 140, 150, 120));
        g.fill(new Ellipse2D.Double(bx + 52, by + 42, 8, 5));
        g.fill(new Ellipse2D.Double(bx + 78, by + 42, 8, 5));
        if (awake) {
            g.setColor(INK);
            g.fill(new Ellipse2D.Double(bx + 59, by + 36, 5, 6));
            g.fill(new Ellipse2D.Double(bx + 74, by + 36, 5, 6));
            g.setColor(new Color(160, 80, 80));
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Arc2D.Double(bx + 63, by + 42, 12, 8, 205, 130, Arc2D.OPEN));
        } else {
            g.setColor(INK);
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Arc2D.Double(bx + 57, by + 37, 9, 6, 200, 140, Arc2D.OPEN));
            g.draw(new Arc2D.Double(bx + 72, by + 37, 9, 6, 200, 140, Arc2D.OPEN));
            g.setColor(new Color(160, 80, 80));
            g.fill(new Ellipse2D.Double(bx + 66, by + 47, 6, 5));
        }
        g.setStroke(oldStroke);
        g.setColor(new Color(244, 160, 186));
        g.fill(new RoundRectangle2D.Double(bx + 96, by + 40, 288, 58, 30, 30));
        g.setColor(new Color(250, 196, 212));
        for (int k = 0; k < 4; k++) g.fill(new RoundRectangle2D.Double(bx + 134 + k * 60, by + 42, 20, 54, 8, 8));
        g.setColor(new Color(255, 246, 248));
        g.fill(new RoundRectangle2D.Double(bx + 94, by + 38, 22, 62, 16, 16));
        if (!awake) {
            g.setFont(new Font("Dialog", Font.BOLD, 16));
            for (int k = 0; k < 3; k++) {
                double t = ((now / 1000.0 + k * 0.7) % 2.1) / 2.1;
                int alpha = (int) (230 * (1 - t));
                g.setColor(new Color(237, 119, 155, Math.max(0, alpha)));
                g.setFont(new Font("Dialog", Font.BOLD, 14 + k * 5));
                g.drawString("Z", (float) (bx + 112 + k * 18 + t * 12), (float) (by + 14 - t * 26));
            }
        }

        // tủ đầu giường, đèn ngủ, điện thoại đang sáng
        g.setColor(new Color(176, 124, 90));
        g.fill(new RoundRectangle2D.Double(462, 300, 76, 48, 8, 8));
        g.setColor(new Color(150, 104, 76));
        g.fill(new RoundRectangle2D.Double(470, 318, 60, 24, 6, 6));
        g.setColor(new Color(255, 214, 140));
        g.fill(new Ellipse2D.Double(496, 296, 6, 6));
        g.setColor(new Color(255, 236, 170, 60));
        g.fill(new Ellipse2D.Double(480, 244, 56, 50));
        g.setColor(new Color(255, 214, 140));
        poly(g, 496, 258, 522, 258, 530, 284, 488, 284);
        g.setColor(new Color(150, 104, 76));
        g.fill(new Rectangle2D.Double(507, 284, 4, 14));
        double glow = 0.5 + 0.5 * Math.sin(now / 300.0);
        g.setColor(new Color(120, 200, 255, (int) (60 + 80 * glow)));
        g.fill(new Ellipse2D.Double(458, 262, 38, 44));
        g.setColor(new Color(34, 38, 52));
        g.fill(new RoundRectangle2D.Double(468, 272, 18, 30, 5, 5));
        g.setColor(new Color(150, 210, 255));
        g.fill(new RoundRectangle2D.Double(470, 275, 14, 24, 3, 3));
        g.setColor(PINK);
        g.fill(new Ellipse2D.Double(476, 267, 8, 8));

        // bàn học, laptop, cốc, sách, cây
        g.setColor(new Color(176, 124, 90));
        g.fill(new RoundRectangle2D.Double(596, 300, 310, 14, 8, 8));
        g.fillRect(606, 314, 12, 34);
        g.fillRect(884, 314, 12, 34);
        g.setColor(new Color(150, 104, 76));
        g.fill(new RoundRectangle2D.Double(770, 314, 108, 30, 6, 6));
        g.setColor(new Color(255, 214, 140));
        g.fill(new RoundRectangle2D.Double(816, 326, 16, 5, 3, 3));
        g.setColor(new Color(206, 210, 220));
        g.fill(new RoundRectangle2D.Double(636, 290, 100, 10, 4, 4));
        g.setColor(new Color(40, 46, 64));
        g.fill(new RoundRectangle2D.Double(646, 238, 80, 54, 6, 6));
        g.setPaint(new GradientPaint(650, 242, new Color(150, 190, 235), 650, 288, new Color(196, 220, 250)));
        g.fill(new RoundRectangle2D.Double(650, 242, 72, 46, 4, 4));
        g.setColor(new Color(255, 255, 255, 190));
        for (int k = 0; k < 4; k++) g.fill(new Rectangle2D.Double(656, 249 + k * 9, 30 + (k * 17) % 26, 4));
        g.setColor(new Color(244, 244, 248));
        g.fill(new RoundRectangle2D.Double(752, 274, 22, 26, 5, 5));
        g.setColor(PINK);
        g.setStroke(new BasicStroke(3f));
        g.draw(new Arc2D.Double(766, 278, 14, 14, 270, 180, Arc2D.OPEN));
        g.setStroke(oldStroke);
        drawHeart(g, 763, 289, 10, PINK);
        g.setColor(new Color(255, 255, 255, 150));
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int k = 0; k < 2; k++) {
            Path2D.Double steam = new Path2D.Double();
            double sxk = 758 + k * 9;
            steam.moveTo(sxk, 270);
            steam.curveTo(sxk + 5 * Math.sin(now / 400.0 + k), 262, sxk - 5 * Math.sin(now / 400.0 + k), 256, sxk, 248);
            g.draw(steam);
        }
        g.setStroke(oldStroke);
        g.setColor(PINK);
        g.fill(new RoundRectangle2D.Double(800, 289, 62, 11, 4, 4));
        g.setColor(MINT);
        g.fill(new RoundRectangle2D.Double(804, 279, 54, 10, 4, 4));
        g.setColor(new Color(255, 214, 120));
        g.fill(new RoundRectangle2D.Double(801, 270, 58, 9, 4, 4));
        g.setColor(new Color(214, 120, 90));
        poly(g, 872, 284, 900, 284, 896, 300, 876, 300);
        g.setColor(new Color(90, 160, 110));
        g.fill(new Ellipse2D.Double(868, 256, 18, 30));
        g.fill(new Ellipse2D.Double(880, 252, 18, 32));
        g.setColor(new Color(120, 190, 130));
        g.fill(new Ellipse2D.Double(874, 262, 12, 24));

        // dây đèn
        Path2D.Double wire = new Path2D.Double();
        wire.moveTo(0, 96);
        for (int x = 0; x < W; x += 160) wire.quadTo(x + 80, 128, x + 160, 96);
        g.setColor(new Color(80, 70, 66));
        g.setStroke(new BasicStroke(1.6f));
        g.draw(wire);
        g.setStroke(oldStroke);
        for (int x = 0; x < W; x += 160) {
            for (int k = 1; k <= 3; k++) {
                double t = k / 4.0;
                double lx = x + 160 * t, ly = 96 + 64 * t * (1 - t);
                int idx = (x / 160) * 3 + k;
                Color c = idx % 3 == 0 ? new Color(255, 214, 120) : idx % 3 == 1 ? new Color(255, 160, 190) : new Color(150, 226, 204);
                double pulse = 0.65 + 0.35 * Math.sin(now / 420.0 + idx);
                g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (70 * pulse)));
                g.fill(new Ellipse2D.Double(lx - 12, ly - 6, 24, 24));
                g.setColor(c);
                g.fill(new Ellipse2D.Double(lx - 4, ly, 8, 11));
            }
        }

        // ánh sáng theo thời tiết từng ngày
        Color[] tint = {
            new Color(255, 240, 200, 0), new Color(60, 80, 120, 38), new Color(150, 190, 240, 30),
            new Color(255, 170, 70, 40), new Color(255, 255, 255, 0)
        };
        g.setColor(tint[d]);
        g.fillRect(0, 0, W, H);

        // tiêu đề cảnh
        String title = dayTitles[d] + " · PHÒNG TRỌ";
        g.setFont(new Font("Dialog", Font.BOLD, 18));
        int tw = g.getFontMetrics().stringWidth(title);
        panel(g, 36, 18, tw + 44, 42, new Color(255, 255, 255, 235));
        g.setColor(INK);
        g.drawString(title, 58, 46);
    }
    void drawExam(Graphics2D g) {
        g.setColor(new Color(228,239,244));
        g.fillRect(0,0,W,H);
        g.setColor(new Color(248,248,243));
        g.fillRoundRect(210,90,540,470,28,28);
        g.setColor(INK);
        g.setFont(new Font("Dialog",Font.BOLD,30));
        g.drawString("ĐANG LÀM BÀI THI",345,153);
        g.setFont(new Font("Dialog",Font.PLAIN,20));
        g.drawString("…",475,300);
        g.setFont(new Font("Dialog",Font.PLAIN,16));
        g.drawString("Nhấn Enter khi đã nộp bài",375,510);
    }
    void drawDialog(Graphics2D g) {
        if (dialogBackground != null) {
            g.drawImage(dialogBackground, 0, 0, W, H, null);
        }
        if (dialogIndex >= lines.size()) {
            screen = ENDING_HE;
            drawEnding(g, true);
            return;
        }
        g.setColor(INK);
        g.setFont(new Font("Dialog",Font.BOLD,18));
        drawCurrentDialog(g);
    }
    void drawWrapped(Graphics2D g,String s,int x,int y,int width,int step) {
        FontMetrics fm=g.getFontMetrics();
        StringBuilder line=new StringBuilder();
        int yy=y;
        for(String word:s.split(" ")) {
            String candidate=line.length()==0?word:line+" "+word;
            if(fm.stringWidth(candidate)>width&&line.length()>0) {
                g.drawString(line.toString(),x,yy);
                yy+=step;
                line=new StringBuilder(word);
            }
            else line=new StringBuilder(candidate);
        }
        g.drawString(line.toString(),x,yy);
    }
    void drawRoad(Graphics2D g) {
        if (flightMode) drawFlightScene(g);
        else drawRoadScene(g);
        drawHud(g);
        long since = System.currentTimeMillis() - lastHitAt;
        if (since < 450) {
            g.setColor(new Color(255, 30, 60, (int) (120 * (1 - since / 450.0))));
            g.setStroke(new BasicStroke(36f));
            g.drawRect(-10, -10, W + 20, H + 20);
            g.setStroke(new BasicStroke(1f));
        }
    }
    // =====================================================================
    //  ĐƯỜNG BỘ: cảnh vật hai bên, xe cộ, trường học ở đích
    // =====================================================================
    static final double[] VEH_W = {50, 48, 50, 54, 58, 60, 26, 30, 40};
    static final double[] VEH_H = {86, 76, 86, 100, 132, 168, 60, 40, 84};
    static final Color[] CAR_COLORS = {
        new Color(219, 93, 96), new Color(81, 146, 185), new Color(240, 242, 246), new Color(176, 184, 196),
        new Color(240, 200, 90), new Color(94, 170, 128), new Color(150, 110, 180), new Color(58, 62, 76)
    };
    int hash(int a, int b) {
        int h = a * 73856093 ^ b * 19349663 ^ 0x5bd1e995;
        h ^= h >>> 13;
        h *= 0x5bd1e995;
        h ^= h >>> 15;
        return h & 0x7fffffff;
    }
    double laneX(int lane) {
        return ROAD_X + 45 + (ROAD_W - 90) * (2 * lane + 1) / 6.0;
    }
    double dividerX(int i) {
        return ROAD_X + 45 + (ROAD_W - 90) * i / 3.0;
    }
    /** Vị trí vạch đích trên màn hình (chỉ có ở 4 ngày đầu, trong vài giây cuối). */
    double finishY() {
        if (day == 4) return -9999;
        double r = Math.max(0, (ROAD_DURATION_MS - elapsed) / 1000.0);
        if (r >= SCHOOL_APPROACH) return -9999;
        double base = (3.6 + day * 0.32) * 60;
        return FINISH_Y - base * (0.2 * r + 0.4 * r * r / SCHOOL_APPROACH);
    }
    Color shade(Color c, double k) {
        return new Color(Math.max(0, Math.min(255, (int) (c.getRed() * k))),
                Math.max(0, Math.min(255, (int) (c.getGreen() * k))),
                Math.max(0, Math.min(255, (int) (c.getBlue() * k))), c.getAlpha());
    }

    void drawRoadScene(Graphics2D g) {
        long now = System.currentTimeMillis();
        double s = groundScroll;
        int rx0 = ROAD_X + 45, rx1 = ROAD_X + ROAD_W - 45;
        Color[] grass = {
            new Color(112, 169, 115), new Color(96, 146, 110), new Color(176, 198, 184),
            new Color(166, 172, 100), new Color(112, 169, 115)
        };
        g.setColor(grass[day]);
        g.fillRect(0, 0, W, H);
        for (int n = (int) Math.floor((s - H) / 120.0) - 1; n <= (int) Math.floor(s / 120.0) + 1; n++) {
            if ((n & 1) == 0) {
                int y = (int) (s - (n + 1) * 120.0);
                g.setColor(new Color(255, 255, 255, 14));
                g.fillRect(0, y, ROAD_X, 120);
                g.fillRect(ROAD_X + ROAD_W, y, W - ROAD_X - ROAD_W, 120);
            }
        }
        int n0 = (int) Math.floor((s - H) / 160.0) - 1, n1 = (int) Math.floor(s / 160.0) + 2;
        for (int n = n0; n <= n1; n++) {
            double topY = s - (n + 1) * 160.0;
            drawLot(g, 0, n, topY);
            drawLot(g, 1, n, topY);
        }
        // vỉa hè lát gạch
        g.setColor(day == 2 ? new Color(220, 226, 232) : new Color(206, 193, 168));
        g.fillRect(ROAD_X, 0, 45, H);
        g.fillRect(rx1, 0, 45, H);
        g.setColor(new Color(0, 0, 0, 22));
        for (double y = (s % 44) - 44; y < H; y += 44) {
            g.fillRect(ROAD_X, (int) y, 45, 2);
            g.fillRect(rx1, (int) y, 45, 2);
        }
        g.fillRect(ROAD_X + 22, 0, 1, H);
        g.fillRect(rx1 + 22, 0, 1, H);
        g.setColor(new Color(150, 140, 120));
        g.fillRect(rx0 - 4, 0, 4, H);
        g.fillRect(rx1, 0, 4, H);
        g.setColor(new Color(255, 255, 255, 70));
        g.fillRect(rx0 - 6, 0, 2, H);
        g.fillRect(rx1 + 4, 0, 2, H);
        for (int n = n0; n <= n1; n++) {
            double ly = s - (n + 1) * 160.0 + 100;
            drawLamp(g, ROAD_X + 9, ly, 1);
            drawLamp(g, ROAD_X + ROAD_W - 9, ly, -1);
        }
        // mặt đường
        Color[] asphalt = {
            new Color(84, 92, 104), new Color(74, 86, 104), new Color(104, 112, 124),
            new Color(98, 98, 102), new Color(84, 92, 104)
        };
        g.setColor(asphalt[day]);
        g.fillRect(rx0, 0, rx1 - rx0, H);
        g.setColor(new Color(0, 0, 0, 16));
        for (int l = 0; l < 3; l++) {
            g.fillRect((int) laneX(l) - 30, 0, 14, H);
            g.fillRect((int) laneX(l) + 16, 0, 14, H);
        }
        for (int n = n0; n <= n1; n++) {
            int h = hash(n, 7);
            double py = s - (n + 1) * 160.0 + (h % 120);
            double px = rx0 + 20 + (h / 7) % (rx1 - rx0 - 90);
            g.setColor(new Color(0, 0, 0, 26));
            g.fill(new RoundRectangle2D.Double(px, py, 36 + h % 30, 12 + h % 10, 6, 6));
            g.setColor(new Color(0, 0, 0, 40));
            g.setStroke(new BasicStroke(1.2f));
            g.draw(new Line2D.Double(px + 60, py + 30, px + 74, py + 52));
            g.draw(new Line2D.Double(px + 74, py + 52, px + 70, py + 70));
            g.setStroke(new BasicStroke(1f));
            if (hash(n, 3) % 9 == 0) {
                double cy = s - (n + 1) * 160.0 + 80;
                g.setColor(new Color(245, 245, 238, 205));
                for (int i = 0; i < 10; i++) g.fill(new Rectangle2D.Double(rx0 + 6 + i * 46, cy - 22, 26, 44));
            }
            if (day == 2 && h % 2 == 1) {
                double iw = 60 + h % 70, ih = 40 + h % 40;
                double ix = rx0 + 20 + (h / 3) % (rx1 - rx0 - 120), iy = py + 20;
                g.setColor(new Color(225, 240, 252, 150));
                g.fill(new Ellipse2D.Double(ix, iy, iw, ih));
                g.setColor(new Color(255, 255, 255, 190));
                double tw = 0.5 + 0.5 * Math.sin(now / 280.0 + h);
                g.fill(new Ellipse2D.Double(ix + iw * 0.3, iy + ih * 0.3, 4 + 3 * tw, 4 + 3 * tw));
            }
        }
        g.setColor(new Color(240, 240, 235, 210));
        g.fillRect(rx0 + 6, 0, 3, H);
        g.fillRect(rx1 - 9, 0, 3, H);
        for (double y = (s % 80) - 80; y < H; y += 80) {
            g.fillRect((int) dividerX(1) - 2, (int) y, 4, 40);
            g.fillRect((int) dividerX(2) - 2, (int) y, 4, 40);
        }
        if (day == 1) drawFlood(g, rx0, rx1, s, now);
        // cây ven đường (tán lá che nhẹ vỉa hè)
        for (int n = n0; n <= n1; n++) {
            int h = hash(n, 11);
            double ty = s - (n + 1) * 160.0 + 30 + h % 90;
            drawTree(g, ROAD_X - 4, ty, 28 + h % 8, h / 5);
            drawTree(g, ROAD_X + ROAD_W + 4, ty + 60, 28 + (h / 3) % 8, h / 9);
        }
        // trường học + vạch đích
        double fy = finishY();
        if (fy > 0) drawCampus(g, fy);
        // xe cộ, tia sét, người chơi
        for (Thing o : things) if (!o.snack) drawVehicle(g, o);
        for (Thing o : things) if (o.snack) drawBolt(g, o.x, o.y, 1 + 0.12 * Math.sin(now / 160.0));
        if (day == 1) drawPlayerBoat(g, (int) Math.round(playerX), (int) Math.round(playerY));
        else drawPlayerBike(g, (int) Math.round(playerX), (int) Math.round(playerY));
        // thời tiết
        if (day == 1) {
            g.setColor(new Color(10, 24, 50, 30));
            g.fillRect(0, 0, W, H);
            g.setColor(new Color(205, 225, 250, 150));
            g.setStroke(new BasicStroke(1.6f));
            for (int i = 0; i < 110; i++) {
                double rx = (i * 137 + now * 0.12) % (W + 60) - 30;
                double ry = (i * 89 + now * 0.9) % (H + 40) - 20;
                g.draw(new Line2D.Double(rx, ry, rx - 4, ry + 16));
            }
            g.setStroke(new BasicStroke(1f));
        } else if (day == 2) {
            g.setColor(new Color(190, 215, 245, 30));
            g.fillRect(0, 0, W, H);
            g.setColor(new Color(255, 255, 255, 220));
            for (int i = 0; i < 90; i++) {
                double fx = (i * 97 + Math.sin(now / 900.0 + i) * 18 + 2 * W) % W;
                double fyy = (i * 61 + now * 0.07 * (1 + i % 3)) % (H + 20) - 10;
                double fr = 2 + (i % 3);
                g.fill(new Ellipse2D.Double(fx, fyy, fr, fr));
            }
        } else if (day == HOT_DAY) {
            g.setColor(new Color(255, 214, 110, 26));
            g.fillRect(0, 0, W, H);
            g.setPaint(new LinearGradientPaint(0, 0, 0, 250, new float[]{0f, 0.55f, 1f},
                    new Color[]{new Color(255, 255, 255, 255), new Color(255, 255, 255, 232), new Color(255, 255, 255, 0)}));
            g.fillRect(0, 0, W, 250);
            g.setColor(new Color(255, 255, 255, 60));
            g.setStroke(new BasicStroke(2f));
            for (int i = 0; i < 5; i++) {
                Path2D.Double wave = new Path2D.Double();
                double wy = 200 + i * 14;
                wave.moveTo(rx0, wy);
                for (int x = rx0; x <= rx1; x += 20) wave.lineTo(x, wy + Math.sin(now / 260.0 + x * 0.05 + i) * 4);
                g.draw(wave);
            }
            g.setStroke(new BasicStroke(1f));
        }
    }
    void drawLamp(Graphics2D g, double x, double y, int dir) {
        g.setColor(new Color(255, 243, 190, 60));
        g.fill(new Ellipse2D.Double(x + dir * 24 - 16, y - 16, 32, 32));
        g.setColor(new Color(70, 76, 90));
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(x, y, x + dir * 22, y));
        g.setStroke(new BasicStroke(1f));
        g.fill(new Ellipse2D.Double(x - 4.5, y - 4.5, 9, 9));
        g.setColor(new Color(255, 243, 190));
        g.fill(new Ellipse2D.Double(x + dir * 24 - 6, y - 4, 12, 8));
    }
    void drawTree(Graphics2D g, double x, double y, double r, int variant) {
        Color[][] pal = {
            {new Color(52, 122, 82), new Color(72, 150, 98), new Color(118, 188, 124)},
            {new Color(98, 128, 64), new Color(124, 156, 80), new Color(166, 190, 108)},
            {new Color(226, 132, 168), new Color(244, 160, 190), new Color(255, 208, 224)}
        };
        Color[] c = pal[(variant % 7 == 0 ? 2 : variant % 2) % 3];
        if (day == 1) c = new Color[]{shade(c[0], 0.8), shade(c[1], 0.8), shade(c[2], 0.8)};
        g.setColor(new Color(0, 0, 0, 45));
        g.fill(new Ellipse2D.Double(x - r + 5, y - r + 8, 2 * r, 2 * r));
        g.setColor(c[0]);
        g.fill(new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r));
        g.setColor(c[1]);
        g.fill(new Ellipse2D.Double(x - r * 0.9, y - r * 0.92, r * 1.64, r * 1.64));
        g.setColor(c[2]);
        g.fill(new Ellipse2D.Double(x - r * 0.55, y - r * 0.62, r * 0.7, r * 0.6));
        if (day == 2) {
            g.setColor(new Color(244, 248, 252, 190));
            g.fill(new Ellipse2D.Double(x - r * 0.7, y - r * 0.7, r * 1.4, r * 1.2));
        }
    }
    void drawHouse(Graphics2D g, double x, double y, double w, double h, Color roof) {
        g.setColor(new Color(0, 0, 0, 40));
        g.fill(new RoundRectangle2D.Double(x - 7, y + 8, w, h, 8, 8));
        g.setColor(shade(roof, 1.12));
        g.fill(new RoundRectangle2D.Double(x, y, w / 2, h, 6, 6));
        g.setColor(shade(roof, 0.82));
        g.fill(new RoundRectangle2D.Double(x + w / 2, y, w / 2, h, 6, 6));
        g.setColor(new Color(0, 0, 0, 38));
        for (double ty = y + 8; ty < y + h - 4; ty += 9) g.fill(new Rectangle2D.Double(x + 3, ty, w - 6, 1));
        g.setColor(new Color(255, 255, 255, 150));
        g.fill(new Rectangle2D.Double(x + w / 2 - 2, y, 4, h));
        g.setColor(new Color(110, 86, 76));
        g.fill(new Rectangle2D.Double(x + w * 0.7, y + h * 0.2, 12, 14));
        if (day == 2) {
            g.setColor(new Color(244, 248, 252, 200));
            g.fill(new RoundRectangle2D.Double(x, y, w, h * 0.35, 6, 6));
        }
    }
    void drawLot(Graphics2D g, int side, int n, double y0) {
        AffineTransform old = g.getTransform();
        if (side == 1) {
            g.translate(W, 0);
            g.scale(-1, 1);
        }
        int h = hash(n, side + 1);
        int kind = h % 7;
        int h2 = h / 7;
        Color[] roofs = {
            new Color(196, 92, 84), new Color(120, 150, 190), new Color(92, 148, 120),
            new Color(214, 170, 92), new Color(150, 110, 150), new Color(112, 120, 140)
        };
        Color roof = roofs[h2 % 6];
        g.setColor(new Color(0, 0, 0, 14));
        g.fill(new RoundRectangle2D.Double(188, y0 + 4, 8, 152, 4, 4));
        switch (kind) {
            case 0:
            case 6:
                drawHouse(g, 48 + (h2 % 3) * 8, y0 + 30 + (h2 % 4) * 6, 116, 88, roof);
                g.setColor(new Color(255, 255, 255, 170));
                for (int i = 0; i < 8; i++) g.fill(new Rectangle2D.Double(182, y0 + 14 + i * 17, 3, 9));
                g.setColor(new Color(232, 196, 120, 190));
                g.fill(new Rectangle2D.Double(150, y0 + 118, 38, 6));
                break;
            case 1: {
                double sx = 40, sy = y0 + 36, sw = 140, sh = 76;
                g.setColor(new Color(0, 0, 0, 40));
                g.fill(new RoundRectangle2D.Double(sx - 7, sy + 8, sw, sh, 6, 6));
                g.setColor(new Color(176, 182, 194));
                g.fill(new RoundRectangle2D.Double(sx, sy, sw, sh, 6, 6));
                g.setColor(new Color(140, 146, 158));
                g.draw(new RoundRectangle2D.Double(sx + 3, sy + 3, sw - 6, sh - 6, 4, 4));
                g.setColor(new Color(214, 218, 226));
                g.fill(new Rectangle2D.Double(sx + 14, sy + 12, 26, 20));
                g.fill(new Rectangle2D.Double(sx + 50, sy + 12, 26, 20));
                g.setColor(new Color(110, 120, 140));
                g.fill(new Ellipse2D.Double(sx + 20, sy + 16, 14, 12));
                g.fill(new Ellipse2D.Double(sx + 56, sy + 16, 14, 12));
                Color[] aw = {PINK, new Color(255, 255, 255), MINT, new Color(255, 214, 120)};
                for (int i = 0; i < 6; i++) {
                    g.setColor(i % 2 == 0 ? aw[h2 % 4 == 1 ? 2 : 0] : aw[1]);
                    g.fill(new Rectangle2D.Double(sx + sw, sy + 4 + i * 12, 14, 12));
                }
                break;
            }
            case 2:
                drawTree(g, 150, y0 + 40, 30, h2);
                drawTree(g, 80, y0 + 96, 34, h2 + 1);
                drawTree(g, 160, y0 + 124, 26, h2 + 2);
                g.setColor(new Color(66, 140, 90));
                g.fill(new Ellipse2D.Double(30, y0 + 22, 30, 22));
                g.fill(new Ellipse2D.Double(36, y0 + 134, 28, 20));
                break;
            case 3: {
                g.setColor(new Color(150, 205, 140));
                g.fill(new RoundRectangle2D.Double(14, y0 + 8, 176, 144, 30, 30));
                g.setColor(new Color(214, 204, 180));
                g.fill(new RoundRectangle2D.Double(96, y0 + 8, 10, 144, 4, 4));
                g.setColor(new Color(110, 176, 214));
                g.fill(new Ellipse2D.Double(28, y0 + 40, 60, 40));
                g.setColor(new Color(190, 226, 244));
                g.fill(new Ellipse2D.Double(36, y0 + 46, 28, 12));
                g.setColor(new Color(150, 104, 76));
                g.fill(new RoundRectangle2D.Double(118, y0 + 70, 40, 12, 4, 4));
                Color[] fl = {PINK, new Color(255, 214, 92), Color.WHITE, new Color(170, 120, 220)};
                for (int i = 0; i < 16; i++) {
                    g.setColor(fl[(h2 + i) % 4]);
                    g.fill(new Ellipse2D.Double(24 + (i * 37 + h2) % 150, y0 + 100 + (i * 23 + h2) % 46, 6, 6));
                }
                drawTree(g, 150, y0 + 40, 22, h2);
                break;
            }
            case 4: {
                double ax = 44, ay = y0 + 18, aw2 = 134, ah = 124;
                g.setColor(new Color(0, 0, 0, 44));
                g.fill(new RoundRectangle2D.Double(ax - 8, ay + 9, aw2, ah, 6, 6));
                g.setColor(new Color(196, 200, 210));
                g.fill(new RoundRectangle2D.Double(ax, ay, aw2, ah, 6, 6));
                g.setColor(new Color(150, 156, 170));
                g.setStroke(new BasicStroke(3f));
                g.draw(new RoundRectangle2D.Double(ax + 4, ay + 4, aw2 - 8, ah - 8, 4, 4));
                g.setStroke(new BasicStroke(1f));
                g.setColor(new Color(130, 170, 200));
                g.fill(new Ellipse2D.Double(ax + 14, ay + 16, 32, 32));
                g.setColor(new Color(110, 140, 170));
                g.draw(new Ellipse2D.Double(ax + 14, ay + 16, 32, 32));
                g.setColor(new Color(120, 126, 140));
                g.fill(new Rectangle2D.Double(ax + 70, ay + 14, 40, 30));
                g.setColor(new Color(50, 84, 140));
                for (int i = 0; i < 3; i++) for (int j = 0; j < 2; j++) g.fill(new Rectangle2D.Double(ax + 14 + i * 34, ay + 66 + j * 26, 30, 22));
                break;
            }
            default: {
                Color[] um = {PINK, new Color(255, 214, 92), MINT, new Color(110, 176, 214)};
                for (int i = 0; i < 4; i++) {
                    double ux = 60 + (i % 2) * 74, uy = y0 + 36 + (i / 2) * 78;
                    g.setColor(new Color(0, 0, 0, 40));
                    g.fill(new Ellipse2D.Double(ux - 16, uy - 8, 36, 36));
                    for (int k = 0; k < 6; k++) {
                        g.setColor(k % 2 == 0 ? um[(h2 + i) % 4] : Color.WHITE);
                        g.fill(new Arc2D.Double(ux - 18, uy - 18, 36, 36, k * 60, 60, Arc2D.PIE));
                    }
                    g.setColor(new Color(90, 70, 60));
                    g.fill(new Ellipse2D.Double(ux - 2, uy - 2, 4, 4));
                    g.setColor(new Color(150, 104, 76));
                    g.fill(new Ellipse2D.Double(ux + 22, uy - 6, 12, 12));
                    g.fill(new Ellipse2D.Double(ux - 34, uy - 6, 12, 12));
                }
                break;
            }
        }
        g.setTransform(old);
    }
    void drawSchoolFacade(Graphics2D g, double cx, double baseY, double sc) {
        AffineTransform old = g.getTransform();
        long now = System.currentTimeMillis();
        g.translate(cx, baseY);
        g.scale(sc, sc);
        Color wall = new Color(250, 240, 222), wallD = new Color(232, 218, 196);
        Color roofC = new Color(200, 96, 84), roofD = new Color(166, 70, 66);
        Color glass = new Color(186, 222, 240), frame = new Color(150, 120, 96);
        g.setColor(new Color(0, 0, 0, 40));
        g.fill(new Rectangle2D.Double(-336, -4, 672, 12));
        for (int side = -1; side <= 1; side += 2) {
            double x0 = side < 0 ? -330 : 105;
            g.setColor(wall);
            g.fill(new Rectangle2D.Double(x0, -150, 225, 150));
            g.setColor(wallD);
            g.fill(new Rectangle2D.Double(x0, -12, 225, 12));
            g.setColor(roofC);
            g.fill(new Rectangle2D.Double(x0 - 8, -172, 241, 26));
            g.setColor(roofD);
            g.fill(new Rectangle2D.Double(x0 - 8, -150, 241, 6));
            for (int r = 0; r < 2; r++) {
                for (int c = 0; c < 5; c++) {
                    double wx = x0 + 14 + c * 42, wy = -128 + r * 58;
                    g.setColor(frame);
                    g.fill(new Rectangle2D.Double(wx - 2, wy - 2, 32, 44));
                    g.setColor(glass);
                    g.fill(new Rectangle2D.Double(wx, wy, 28, 40));
                    g.setColor(new Color(255, 255, 255, 120));
                    g.fill(new Rectangle2D.Double(wx + 3, wy + 3, 8, 14));
                    g.setColor(frame);
                    g.fill(new Rectangle2D.Double(wx + 13, wy, 2, 40));
                }
            }
        }
        g.setColor(wall);
        g.fill(new Rectangle2D.Double(-105, -232, 210, 232));
        g.setColor(roofD);
        poly(g, -128, -232, 0, -294, 128, -232);
        g.setColor(roofC);
        poly(g, -116, -234, 0, -284, 116, -234);
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(-19, -270, 38, 38));
        g.setColor(frame);
        g.setStroke(new BasicStroke(3f));
        g.draw(new Ellipse2D.Double(-19, -270, 38, 38));
        g.setColor(INK);
        g.draw(new Line2D.Double(0, -251, 0, -262));
        g.draw(new Line2D.Double(0, -251, 8, -247));
        g.setStroke(new BasicStroke(1f));
        g.setColor(new Color(36, 52, 92));
        g.fill(new RoundRectangle2D.Double(-140, -222, 280, 40, 12, 12));
        g.setColor(new Color(255, 214, 120));
        g.setStroke(new BasicStroke(2f));
        g.draw(new RoundRectangle2D.Double(-136, -218, 272, 32, 8, 8));
        g.setStroke(new BasicStroke(1f));
        g.setColor(Color.WHITE);
        g.setFont(new Font("Dialog", Font.BOLD, 22));
        String sign = "TRƯỜNG ĐẠI HỌC";
        g.drawString(sign, -g.getFontMetrics().stringWidth(sign) / 2, -193);
        for (int c = 0; c < 4; c++) {
            double wx = -84 + c * 44;
            g.setColor(frame);
            g.fill(new Rectangle2D.Double(wx - 2, -170, 32, 44));
            g.setColor(glass);
            g.fill(new Rectangle2D.Double(wx, -168, 28, 40));
            g.setColor(frame);
            g.fill(new Rectangle2D.Double(wx + 13, -168, 2, 40));
        }
        g.setColor(Color.WHITE);
        g.fill(new Rectangle2D.Double(-56, -112, 12, 112));
        g.fill(new Rectangle2D.Double(44, -112, 12, 112));
        g.setColor(wallD);
        g.fill(new Rectangle2D.Double(-66, -120, 132, 10));
        g.setColor(new Color(110, 78, 60));
        g.fill(new RoundRectangle2D.Double(-36, -88, 72, 88, 36, 36));
        g.setColor(MINT);
        g.fill(new RoundRectangle2D.Double(-32, -84, 31, 84, 28, 28));
        g.fill(new RoundRectangle2D.Double(1, -84, 31, 84, 28, 28));
        g.setColor(glass);
        g.fill(new RoundRectangle2D.Double(-26, -76, 19, 40, 14, 14));
        g.fill(new RoundRectangle2D.Double(7, -76, 19, 40, 14, 14));
        g.setColor(new Color(224, 218, 206));
        g.fill(new Rectangle2D.Double(-84, -10, 168, 10));
        g.setColor(new Color(210, 204, 190));
        g.fill(new Rectangle2D.Double(-100, -4, 200, 6));
        g.setColor(new Color(150, 150, 160));
        g.fill(new Rectangle2D.Double(266, -262, 4, 92));
        double wv = Math.sin(now / 240.0) * 5;
        g.setColor(new Color(218, 37, 29));
        Path2D.Double flag = new Path2D.Double();
        flag.moveTo(270, -262);
        flag.curveTo(286, -268 + wv, 300, -256 - wv, 316, -262);
        flag.lineTo(316, -230);
        flag.curveTo(300, -224 - wv, 286, -236 + wv, 270, -230);
        flag.closePath();
        g.fill(flag);
        g.setColor(new Color(255, 236, 0));
        double[] sp = new double[20];
        for (int i = 0; i < 10; i++) {
            double rr = i % 2 == 0 ? 8 : 3.2, aa = -Math.PI / 2 + i * Math.PI / 5;
            sp[i * 2] = 293 + rr * Math.cos(aa);
            sp[i * 2 + 1] = -246 + rr * Math.sin(aa);
        }
        poly(g, sp);
        for (int i = 0; i < 12; i++) {
            if (i >= 5 && i <= 6) continue;
            double bx = -318 + i * 53;
            g.setColor(new Color(66, 140, 90));
            g.fill(new Ellipse2D.Double(bx, -16, 40, 24));
            g.setColor(new Color(92, 168, 112));
            g.fill(new Ellipse2D.Double(bx + 6, -14, 22, 12));
        }
        g.setTransform(old);
    }
    void drawCampus(Graphics2D g, double fy) {
        int rx0 = ROAD_X + 45, rx1 = ROAD_X + ROAD_W - 45;
        g.setColor(new Color(132, 184, 120));
        g.fill(new Rectangle2D.Double(0, 0, W, fy));
        for (int i = 0; i < 7; i++) {
            double ty = fy - 50 - i * 76;
            if (ty < -40) break;
            drawTree(g, 40 + (i % 2) * 86, ty, 30, i);
            drawTree(g, W - 40 - (i % 2) * 86, ty, 30, i + 3);
        }
        g.setColor(new Color(226, 216, 194));
        g.fill(new Rectangle2D.Double(ROAD_X, 0, ROAD_W, fy));
        g.setColor(new Color(206, 196, 174));
        for (double y = fy - 24; y > 0; y -= 36) g.fill(new Rectangle2D.Double(ROAD_X, y, ROAD_W, 2));
        drawSchoolFacade(g, W / 2.0, fy - 38, 0.9);
        for (int side = 0; side < 2; side++) {
            double px = side == 0 ? ROAD_X + 2 : ROAD_X + ROAD_W - 26;
            g.setColor(new Color(236, 230, 218));
            g.fill(new RoundRectangle2D.Double(px, fy - 46, 24, 46, 4, 4));
            g.setColor(new Color(200, 96, 84));
            g.fill(new Ellipse2D.Double(px + 2, fy - 58, 20, 20));
        }
        int cell = 14, cols = (rx1 - rx0) / cell + 1;
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < 2; j++) {
                g.setColor((i + j) % 2 == 0 ? Color.WHITE : INK);
                g.fill(new Rectangle2D.Double(rx0 + i * cell, fy + j * cell, cell, cell));
            }
        }
    }
    void drawNpcBike(Graphics2D g, Thing o) {
        double hh = o.h / 2;
        Color[] jackets = {new Color(219, 93, 96), new Color(81, 146, 185), new Color(240, 200, 90), new Color(94, 170, 128), new Color(150, 110, 180), new Color(58, 62, 76), new Color(240, 242, 246), new Color(255, 143, 66)};
        Color jacket = jackets[o.color % 8], helmet = jackets[(o.color + 3) % 8];
        g.setColor(new Color(32, 34, 42));
        g.fill(new RoundRectangle2D.Double(-4, hh - 24, 8, 24, 6, 6));
        g.fill(new RoundRectangle2D.Double(-3, -hh, 6, 16, 5, 5));
        g.setColor(shade(jacket, 0.8));
        g.fill(new RoundRectangle2D.Double(-6, -4, 12, 30, 8, 8));
        g.setColor(jacket);
        g.fill(new Ellipse2D.Double(-10, -14, 20, 24));
        g.setColor(new Color(40, 44, 56));
        g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(-13, -hh + 14, 13, -hh + 14));
        g.setStroke(new BasicStroke(1f));
        g.setColor(helmet);
        g.fill(new Ellipse2D.Double(-7.5, -24, 15, 15));
        g.setColor(new Color(255, 255, 255, 110));
        g.fill(new Ellipse2D.Double(-4, -22, 5, 4));
        g.setColor(o.jam || o.fwd == 0 ? new Color(255, 60, 70) : new Color(190, 40, 52));
        g.fill(new Rectangle2D.Double(-3, hh - 4, 6, 3));
    }
    void drawVehicle(Graphics2D g, Thing o) {
        AffineTransform old = g.getTransform();
        Stroke oldStroke = g.getStroke();
        g.translate(o.x, o.y);
        g.rotate(o.angle);
        double w = o.w, h = o.h, hw = w / 2, hh = h / 2;
        if (o.kind == 8) {
            drawNpcBoat(g, o);
            g.setTransform(old);
            return;
        }
        boolean brake = o.jam || o.fwd == 0;
        g.setColor(new Color(0, 0, 0, 55));
        g.fill(new RoundRectangle2D.Double(-hw + 4, -hh + 7, w, h, 20, 20));
        if (o.kind == 6) {
            drawNpcBike(g, o);
            g.setTransform(old);
            return;
        }
        Color glass = new Color(52, 68, 92), glassHi = new Color(255, 255, 255, 70);
        Color body = o.kind == 2 ? new Color(52, 160, 110) : o.kind == 3 ? new Color(238, 240, 244) : CAR_COLORS[o.color % 8];
        if (o.kind == 5) body = o.color % 2 == 0 ? new Color(219, 93, 96) : new Color(81, 146, 185);
        if (o.kind == 4) body = CAR_COLORS[o.color % 8];
        double[] wy = o.kind == 4 ? new double[]{-hh + 20, hh - 40, hh - 18}
                : o.kind == 5 ? new double[]{-hh + 26, hh - 30} : new double[]{-hh + 17, hh - 17};
        g.setColor(new Color(24, 26, 32));
        for (double yy : wy) {
            g.fill(new RoundRectangle2D.Double(-hw - 2, yy - 8, 6, 16, 3, 3));
            g.fill(new RoundRectangle2D.Double(hw - 4, yy - 8, 6, 16, 3, 3));
        }
        double arc = o.kind >= 4 ? 12 : 22;
        RoundRectangle2D.Double shape = new RoundRectangle2D.Double(-hw, -hh, w, h, arc, arc);
        g.setColor(body);
        g.fill(shape);
        g.setColor(shade(body, 0.7));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(shape);
        Color lite = shade(body, 1.14);
        if (o.kind <= 2) {
            double y1 = -hh + h * 0.27, y2 = -hh + h * 0.40;
            double y3 = -hh + h * (o.kind == 1 ? 0.60 : 0.64), y4 = -hh + h * (o.kind == 1 ? 0.82 : 0.76);
            g.setColor(lite);
            g.fill(new RoundRectangle2D.Double(-hw + 4, -hh + 3, w - 8, h * 0.25, 14, 14));
            g.setColor(shade(body, 0.8));
            g.fill(new Rectangle2D.Double(-1, -hh + 6, 2, h * 0.2));
            g.setColor(glass);
            poly(g, -hw + 8, y2, hw - 8, y2, hw - 11, y1, -hw + 11, y1);
            g.setColor(lite);
            g.fill(new RoundRectangle2D.Double(-hw + 7, y2, w - 14, y3 - y2, 6, 6));
            g.setColor(glassHi);
            poly(g, -hw + 10, y2 + 3, -hw + 20, y2 + 3, -hw + 12, y3 - 3);
            g.setColor(glass);
            poly(g, -hw + 8, y3, hw - 8, y3, hw - 11, y4, -hw + 11, y4);
            if (o.kind == 2) {
                g.setColor(Color.WHITE);
                g.fill(new RoundRectangle2D.Double(-10, (y2 + y3) / 2 - 4, 20, 8, 3, 3));
                g.setColor(new Color(52, 160, 110));
                g.fill(new Rectangle2D.Double(-6, (y2 + y3) / 2 - 1, 12, 2));
            }
            g.setColor(shade(body, 0.8));
            g.fill(new Rectangle2D.Double(-hw - 3, y1 + 4, 4, 7));
            g.fill(new Rectangle2D.Double(hw - 1, y1 + 4, 4, 7));
        } else if (o.kind == 3) {
            g.setColor(glass);
            poly(g, -hw + 7, -hh + 22, hw - 7, -hh + 22, hw - 10, -hh + 7, -hw + 10, -hh + 7);
            g.setColor(shade(body, 0.92));
            g.fill(new RoundRectangle2D.Double(-hw + 5, -hh + 26, w - 10, h - 34, 6, 6));
            g.setColor(new Color(0, 0, 0, 36));
            for (int i = 0; i < 4; i++) g.fill(new Rectangle2D.Double(-hw + 7, -hh + 36 + i * 14, w - 14, 2));
            g.fill(new Rectangle2D.Double(-1, hh - 34, 2, 30));
            g.setColor(CAR_COLORS[o.color % 8]);
            g.fill(new Rectangle2D.Double(-hw + 5, -hh + 26, 5, h - 34));
            g.fill(new Rectangle2D.Double(hw - 10, -hh + 26, 5, h - 34));
        } else if (o.kind == 4) {
            g.setColor(shade(body, 1.1));
            g.fill(new RoundRectangle2D.Double(-hw + 3, -hh + 3, w - 6, 34, 10, 10));
            g.setColor(glass);
            poly(g, -hw + 8, -hh + 26, hw - 8, -hh + 26, hw - 10, -hh + 12, -hw + 10, -hh + 12);
            g.setColor(new Color(214, 218, 226));
            g.fill(new RoundRectangle2D.Double(-hw + 3, -hh + 40, w - 6, h - 42, 6, 6));
            g.setColor(new Color(0, 0, 0, 40));
            for (int i = 0; i < 7; i++) g.fill(new Rectangle2D.Double(-hw + 4, -hh + 48 + i * 11, w - 8, 2));
            g.setColor(new Color(150, 156, 170));
            g.draw(new RoundRectangle2D.Double(-hw + 3, -hh + 40, w - 6, h - 42, 6, 6));
        } else {
            g.setColor(glass);
            poly(g, -hw + 6, -hh + 30, hw - 6, -hh + 30, hw - 9, -hh + 8, -hw + 9, -hh + 8);
            g.setColor(shade(body, 1.12));
            g.fill(new RoundRectangle2D.Double(-hw + 5, -hh + 34, w - 10, h - 62, 6, 6));
            g.setColor(new Color(255, 255, 255, 190));
            for (int i = 0; i < 3; i++) g.fill(new RoundRectangle2D.Double(-12, -hh + 48 + i * 40, 24, 18, 5, 5));
            g.fill(new Rectangle2D.Double(-hw + 3, -hh + 34, 3, h - 62));
            g.fill(new Rectangle2D.Double(hw - 6, -hh + 34, 3, h - 62));
            g.setColor(glass);
            poly(g, -hw + 8, hh - 26, hw - 8, hh - 26, hw - 11, hh - 10, -hw + 11, hh - 10);
        }
        Color lamp = brake ? new Color(255, 60, 70) : new Color(176, 34, 48);
        if (brake) {
            g.setColor(new Color(255, 60, 70, 70));
            g.fill(new Ellipse2D.Double(-hw + 1, hh - 12, 18, 12));
            g.fill(new Ellipse2D.Double(hw - 19, hh - 12, 18, 12));
        }
        g.setColor(lamp);
        g.fill(new RoundRectangle2D.Double(-hw + 3, hh - 7, 12, 5, 3, 3));
        g.fill(new RoundRectangle2D.Double(hw - 15, hh - 7, 12, 5, 3, 3));
        g.setColor(new Color(245, 245, 240));
        g.fill(new RoundRectangle2D.Double(-7, hh - 8, 14, 5, 2, 2));
        if (day == 1 && o.kind <= 5) {
            long nowW = System.currentTimeMillis();
            g.setColor(new Color(124, 110, 86, 120));
            g.fill(new RoundRectangle2D.Double(-hw - 2, -hh * 0.05, w + 4, h * 0.55, 16, 16));
            g.setStroke(new BasicStroke(1.8f));
            for (int k = 0; k < 2; k++) {
                double ph = ((nowW / 900.0) + k * 0.5) % 1.0;
                double ex = 4 + ph * 10;
                g.setColor(new Color(240, 244, 248, (int) (130 * (1 - ph))));
                g.draw(new RoundRectangle2D.Double(-hw - ex, -hh - ex, w + 2 * ex, h + 2 * ex, 26 + ex, 26 + ex));
            }
        }
        g.setTransform(old);
        g.setStroke(oldStroke);
    }
    void drawBolt(Graphics2D g, double x, double y, double sc) {
        AffineTransform old = g.getTransform();
        g.translate(x, y);
        g.scale(sc, sc);
        g.setColor(new Color(255, 230, 90, 80));
        g.fill(new Ellipse2D.Double(-26, -26, 52, 52));
        g.setColor(new Color(255, 255, 255, 120));
        g.fill(new Ellipse2D.Double(-18, -18, 36, 36));
        Path2D.Double bolt = new Path2D.Double();
        bolt.moveTo(3, -18);
        bolt.lineTo(-10, 2);
        bolt.lineTo(-2, 2);
        bolt.lineTo(-6, 18);
        bolt.lineTo(10, -4);
        bolt.lineTo(2, -4);
        bolt.lineTo(8, -18);
        bolt.closePath();
        g.setColor(new Color(255, 214, 40));
        g.fill(bolt);
        g.setColor(new Color(224, 140, 20));
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(bolt);
        g.setStroke(new BasicStroke(1f));
        g.setTransform(old);
    }
    void drawHud(Graphics2D g) {
        long now = System.currentTimeMillis();
        boolean hot = day == HOT_DAY && !flightMode;
        panel(g, 20, 18, 270, hot ? 144 : 118, new Color(255, 255, 255, 238));
        g.setColor(INK);
        int fs = 17;
        Font f;
        do {
            f = new Font("Dialog", Font.BOLD, fs--);
        } while (g.getFontMetrics(f).stringWidth(dayTitles[day]) > 236 && fs > 10);
        g.setFont(f);
        g.drawString(dayTitles[day], 38, 46);
        g.setFont(new Font("Dialog", Font.PLAIN, 15));
        int secondsLeft = Math.max(0, (ROAD_DURATION_MS / 1000) - (int) (elapsed / 1000));
        g.drawString(String.format("THỜI GIAN  %02d:%02d", secondsLeft / 60, secondsLeft % 60), 38, 70);
        // thanh HP
        int shake = now - lastHitAt < 350 ? (int) (Math.sin(now / 22.0) * 3) : 0;
        drawHeart(g, 47 + shake, 96, 22, new Color(232, 70, 100));
        int bx = 66 + shake, by = 88, bw = 206, bh = 16;
        g.setColor(new Color(222, 224, 228));
        g.fillRoundRect(bx, by, bw, bh, 10, 10);
        int fill = (int) (bw * Math.max(0, hp) / 100.0);
        if (fill > 0) {
            g.setColor(hp > 50 ? MINT : hp > 25 ? new Color(240, 176, 64) : new Color(232, 70, 90));
            g.fillRoundRect(bx, by, Math.max(fill, 10), bh, 10, 10);
        }
        g.setFont(new Font("Dialog", Font.BOLD, 11));
        String ht = "HP  " + Math.max(0, hp) + " / 100";
        int hx = bx + bw / 2 - g.getFontMetrics().stringWidth(ht) / 2;
        g.setColor(INK);
        g.drawString(ht, hx, by + 12);
        // thanh năng lượng (chỉ ngày nắng nóng)
        if (hot) {
            drawBolt(g, 47, 123, 0.62);
            g.setColor(new Color(222, 224, 228));
            g.fillRoundRect(66, 114, 206, 16, 10, 10);
            int ef = (int) (206 * Math.max(0, energy) / 100.0);
            boolean low = energy < 25;
            if (ef > 0) {
                g.setColor(low && (now / 250) % 2 == 0 ? new Color(232, 70, 90) : new Color(255, 196, 40));
                g.fillRoundRect(66, 114, Math.max(ef, 10), 16, 10, 10);
            }
            g.setFont(new Font("Dialog", Font.BOLD, 11));
            String et = "NĂNG LƯỢNG  " + (int) Math.ceil(Math.max(0, energy));
            int ex = 66 + 103 - g.getFontMetrics().stringWidth(et) / 2;
            g.setColor(INK);
            g.drawString(et, ex, 126);
        }
        panel(g, 700, 18, 238, 58, new Color(255, 255, 255, 225));
        g.setColor(INK);
        g.setFont(new Font("Dialog", Font.BOLD, 14));
        g.drawString(flightMode ? "ĐƯỜNG BAY" : "ĐƯỜNG ĐI", 720, 42);
        g.setColor(new Color(98, 102, 114));
        g.fillRoundRect(720, 49, 196, 8, 8, 8);
        g.setColor(PINK);
        g.fillRoundRect(720, 49, Math.min(196, (int) (196.0 * elapsed / ROAD_DURATION_MS)), 8, 8, 8);
        if (day == 4 && !flightMode && elapsed / 1000.0 >= ROAD_DURATION_MS / 3000.0 - JAM_WINDOW + 1.2) {
            g.setColor(new Color(245, 250, 255, 190));
            g.fillRoundRect(ROAD_X + 140, 150, ROAD_W - 280, 38, 16, 16);
            g.setColor(INK);
            g.setFont(new Font("Dialog", Font.BOLD, 16));
            String jam = "TẮC ĐƯỜNG…";
            g.drawString(jam, ROAD_X + ROAD_W / 2 - g.getFontMetrics().stringWidth(jam) / 2, 175);
        }
        g.setFont(new Font("Dialog", Font.PLAIN, 13));
        String hint = flightMode ? "Né đàn chim · Đừng để HP về 0"
                : day == 1 ? "Chèo thuyền né xe cộ và thuyền khác · Đừng để HP về 0"
                : hot ? "Nhặt tia sét để giữ năng lượng · Đừng để HP hay năng lượng về 0"
                : "Né xe cộ (có xe chạy lấn làn!) · Đừng để HP về 0";
        int hw = g.getFontMetrics().stringWidth(hint);
        g.setColor(new Color(255, 255, 255, 190));
        g.fillRoundRect(W / 2 - hw / 2 - 14, 600, hw + 28, 26, 14, 14);
        g.setColor(INK);
        g.drawString(hint, W / 2 - hw / 2, 618);
    }
    int trackX(int track) {
        int sidewalkWidth = 45;
        int roadStart = ROAD_X + sidewalkWidth;
        int roadWidth = ROAD_W - 2 * sidewalkWidth;
        return switch (track) {
            case 0 -> ROAD_X + sidewalkWidth / 2;
            case 1 -> roadStart + roadWidth / 6;
            case 2 -> roadStart + roadWidth / 2;
            case 3 -> roadStart + roadWidth * 5 / 6;
            default -> ROAD_X + ROAD_W - sidewalkWidth / 2;
        };
    }
    // =====================================================================
    //  NHÂN VẬT: avatar, người lái xe máy
    // =====================================================================
    void poly(Graphics2D g, double... p) {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(p[0], p[1]);
        for (int i = 2; i + 1 < p.length; i += 2) path.lineTo(p[i], p[i + 1]);
        path.closePath();
        g.fill(path);
    }
    void drawHeart(Graphics2D g, double cx, double cy, double s, Color c) {
        Path2D.Double h = new Path2D.Double();
        h.moveTo(cx, cy + s * 0.42);
        h.curveTo(cx - s * 0.75, cy - s * 0.05, cx - s * 0.4, cy - s * 0.6, cx, cy - s * 0.2);
        h.curveTo(cx + s * 0.4, cy - s * 0.6, cx + s * 0.75, cy - s * 0.05, cx, cy + s * 0.42);
        g.setColor(c);
        g.fill(h);
    }
    /** Avatar tròn cho từng nhân vật (dùng trong khung thoại, chat, ảnh treo tường, trực thăng). */
    void drawAvatar(Graphics2D g, String who, int cx, int cy, int r) {
        int kind = who.startsWith("Sanbaka") ? 0
                : who.startsWith("Thắng") ? 1
                : who.startsWith("Nam") ? 2
                : who.startsWith("Chủ trọ") ? 3 : 4;
        Color[] bgs = {
            new Color(255, 214, 226), new Color(200, 226, 246), new Color(255, 226, 190),
            new Color(214, 236, 216), new Color(226, 230, 238)
        };
        Shape oldClip = g.getClip();
        AffineTransform oldT = g.getTransform();
        Stroke oldStroke = g.getStroke();
        Ellipse2D circle = new Ellipse2D.Double(cx - r, cy - r, 2.0 * r, 2.0 * r);
        g.setColor(bgs[kind]);
        g.fill(circle);
        g.clip(circle);
        g.translate(cx, cy + r * 0.16);
        double s = r / 32.0;
        g.scale(s, s);
        if (kind == 4) {
            g.setColor(new Color(150, 158, 176));
            for (int i = 0; i < 3; i++) g.fill(new Ellipse2D.Double(-19 + i * 14, -5, 9, 9));
        } else {
            Color skin = new Color(255, 224, 196);
            Color hair = kind == 0 ? new Color(58, 40, 48) : kind == 1 ? new Color(48, 38, 44)
                    : kind == 2 ? new Color(122, 78, 52) : new Color(208, 208, 216);
            Color cloth = kind == 0 ? PINK : kind == 1 ? new Color(45, 61, 87)
                    : kind == 2 ? new Color(240, 150, 70) : new Color(110, 160, 122);
            g.setColor(hair);
            if (kind == 0) g.fill(new Ellipse2D.Double(-27, -30, 54, 68));
            if (kind == 3) g.fill(new Ellipse2D.Double(-9, -44, 18, 18));
            g.setColor(new Color(240, 200, 170));
            g.fill(new Rectangle2D.Double(-6, 14, 12, 14));
            g.setColor(cloth);
            g.fill(new RoundRectangle2D.Double(-30, 24, 60, 44, 30, 30));
            if (kind == 1) {
                g.setColor(MINT);
                g.fill(new Rectangle2D.Double(-19, 26, 6, 30));
                g.fill(new Rectangle2D.Double(13, 26, 6, 30));
            }
            if (kind == 0) {
                g.setColor(Color.WHITE);
                poly(g, -9, 24, 0, 36, 9, 24);
            }
            if (kind == 2) {
                g.setColor(new Color(255, 255, 255, 220));
                g.fill(new Rectangle2D.Double(-7, 28, 2.5, 12));
                g.fill(new Rectangle2D.Double(4.5, 28, 2.5, 12));
            }
            g.setColor(skin);
            g.fill(new Ellipse2D.Double(-20, -22, 40, 44));
            g.setColor(hair);
            if (kind == 0) {
                g.fill(new Arc2D.Double(-23, -29, 46, 40, 0, 180, Arc2D.CHORD));
                g.fill(new RoundRectangle2D.Double(-25, -8, 8, 38, 8, 8));
                g.fill(new RoundRectangle2D.Double(17, -8, 8, 38, 8, 8));
            } else if (kind == 1) {
                g.fill(new Arc2D.Double(-22, -29, 44, 40, 0, 180, Arc2D.CHORD));
                poly(g, -16, -22, -11, -37, -4, -24);
                poly(g, -5, -25, 2, -39, 9, -25);
                poly(g, 6, -24, 15, -35, 18, -18);
            } else if (kind == 2) {
                g.fill(new Arc2D.Double(-23, -30, 46, 42, 0, 180, Arc2D.CHORD));
                g.fill(new Ellipse2D.Double(-19, -36, 17, 13));
                g.fill(new Ellipse2D.Double(2, -38, 19, 14));
            } else {
                g.fill(new Arc2D.Double(-21, -28, 42, 38, 0, 180, Arc2D.CHORD));
            }
            g.setColor(INK);
            g.fill(new Ellipse2D.Double(-12, -3, 7, 9));
            g.fill(new Ellipse2D.Double(5, -3, 7, 9));
            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Double(-10, -1.5, 3, 3));
            g.fill(new Ellipse2D.Double(7, -1.5, 3, 3));
            if (kind == 2) {
                g.setColor(new Color(70, 72, 86));
                g.setStroke(new BasicStroke(2f));
                g.draw(new Ellipse2D.Double(-16, -7, 15, 17));
                g.draw(new Ellipse2D.Double(1, -7, 15, 17));
                g.draw(new Line2D.Double(-1, 0, 1, 0));
            }
            g.setColor(new Color(255, 140, 150, 110));
            g.fill(new Ellipse2D.Double(-19, 7, 10, 6));
            g.fill(new Ellipse2D.Double(9, 7, 10, 6));
            g.setColor(new Color(160, 80, 80));
            g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Arc2D.Double(-6, 7, 12, 9, 205, 130, Arc2D.OPEN));
            if (kind == 3) {
                g.setColor(new Color(196, 150, 120));
                g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Line2D.Double(-17, -8, -11, -9));
                g.draw(new Line2D.Double(11, -9, 17, -8));
            }
            if (kind == 0) {
                g.setColor(PINK);
                g.fill(new Ellipse2D.Double(8, -27, 11, 8));
                g.fill(new Ellipse2D.Double(16, -27, 11, 8));
                g.setColor(Color.WHITE);
                g.fill(new Ellipse2D.Double(14, -25, 5, 5));
            }
        }
        g.setTransform(oldT);
        g.setClip(oldClip);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(Math.max(1.5f, r / 14f)));
        g.draw(circle);
        g.setStroke(oldStroke);
    }
    /** Người chơi trên đường: Thắng đi xe máy (nhìn từ phía sau). */
    void drawPlayerBike(Graphics2D g, int px, int py) {
        long now = System.currentTimeMillis();
        Composite oldComposite = g.getComposite();
        AffineTransform oldTransform = g.getTransform();
        Stroke oldStroke = g.getStroke();
        if (invulnerable > now && (now / 90) % 2 == 0) {
            g.setComposite(AlphaComposite.SrcOver.derive(0.35f));
        }
        Color tire = new Color(32, 34, 42);
        Color orange = new Color(255, 143, 66);
        Color orangeD = new Color(214, 104, 36);
        Color jacket = new Color(45, 61, 87);
        Color jeans = new Color(62, 84, 122);
        Color skin = new Color(255, 224, 196);
        Color bar = new Color(40, 44, 56);
        // khói xả
        for (int i = 0; i < 3; i++) {
            g.setColor(new Color(235, 235, 235, 70 - i * 20));
            int s = 8 + i * 5;
            int wob = (int) (Math.sin(now / 130.0 + i * 1.7) * 3);
            g.fillOval(px - s / 2 + wob, py + 48 + i * 9, s, s);
        }
        g.translate(px, py);
        g.scale(0.6, 0.6);
        g.translate(-px, -py);
        g.rotate(lean * 0.13, px, py + 20);
        // bóng đổ
        g.setColor(new Color(0, 0, 0, 48));
        g.fillOval(px - 20, py - 28, 40, 80);
        // bánh trước + chắn gió
        g.setColor(tire);
        g.fillRoundRect(px - 4, py - 48, 8, 18, 6, 6);
        g.setColor(orange);
        g.fillRoundRect(px - 9, py - 40, 18, 15, 10, 10);
        g.setColor(new Color(255, 255, 255, 90));
        g.fillRoundRect(px - 5, py - 38, 6, 5, 4, 4);
        // tay lái + gương
        g.setColor(bar);
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(px - 22, py - 31, px + 22, py - 31);
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(px - 20, py - 32, px - 24, py - 41);
        g.drawLine(px + 20, py - 32, px + 24, py - 41);
        g.setColor(new Color(186, 218, 238));
        g.fillOval(px - 30, py - 47, 11, 8);
        g.fillOval(px + 19, py - 47, 11, 8);
        g.setColor(bar);
        g.setStroke(new BasicStroke(1.4f));
        g.drawOval(px - 30, py - 47, 11, 8);
        g.drawOval(px + 19, py - 47, 11, 8);
        // thân xe, bánh sau (hoa lốp chạy), đuôi xe
        g.setColor(orange);
        g.fillRoundRect(px - 10, py - 26, 20, 36, 12, 12);
        g.setColor(tire);
        g.fillRoundRect(px - 6, py + 14, 12, 32, 8, 8);
        g.setColor(new Color(78, 82, 96));
        int off = (int) (((-now / 30) % 6 + 6) % 6);
        for (int ty = py + 17 + off; ty < py + 43; ty += 6) g.drawLine(px - 4, ty, px + 4, ty);
        g.setColor(orange);
        g.fillRoundRect(px - 11, py + 4, 22, 24, 12, 12);
        g.setColor(orangeD);
        g.fillRoundRect(px - 2, py + 7, 4, 17, 4, 4);
        g.setColor(new Color(255, 70, 80, 80));
        g.fillOval(px - 11, py + 21, 22, 12);
        g.setColor(new Color(255, 70, 80));
        g.fillRoundRect(px - 6, py + 25, 12, 4, 3, 3);
        // yên + chân
        g.setColor(new Color(58, 46, 48));
        g.fillRoundRect(px - 9, py - 6, 18, 20, 10, 10);
        g.setColor(jeans);
        g.fillRoundRect(px - 18, py - 8, 9, 22, 8, 8);
        g.fillRoundRect(px + 9, py - 8, 9, 22, 8, 8);
        g.setColor(new Color(245, 246, 250));
        g.fillRoundRect(px - 19, py - 16, 10, 9, 6, 6);
        g.fillRoundRect(px + 9, py - 16, 10, 9, 6, 6);
        // tay nắm ghi-đông
        g.setColor(jacket);
        g.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(px - 11, py - 12, px - 19, py - 30);
        g.drawLine(px + 11, py - 12, px + 19, py - 30);
        g.setColor(skin);
        g.fillOval(px - 24, py - 36, 9, 9);
        g.fillOval(px + 15, py - 36, 9, 9);
        // thân người + balo
        g.setColor(jacket);
        g.fillRoundRect(px - 13, py - 17, 26, 28, 14, 14);
        g.setColor(MINT);
        g.fillRoundRect(px - 10, py - 15, 20, 24, 10, 10);
        g.setColor(new Color(63, 145, 121));
        g.fillRoundRect(px - 7, py - 1, 14, 8, 6, 6);
        g.setColor(new Color(255, 255, 255, 150));
        g.setStroke(new BasicStroke(1.2f));
        g.drawLine(px, py - 13, px, py - 3);
        drawHeart(g, px + 7, py + 9, 7, PINK);
        // tóc gáy, tai, mũ bảo hiểm
        g.setColor(new Color(52, 40, 44));
        g.fillOval(px - 8, py - 17, 16, 9);
        g.setColor(skin);
        g.fillOval(px - 14, py - 27, 4, 7);
        g.fillOval(px + 10, py - 27, 4, 7);
        g.setColor(new Color(250, 250, 252));
        g.fillOval(px - 12, py - 37, 24, 25);
        g.setColor(new Color(200, 208, 222));
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(px - 12, py - 37, 24, 25);
        g.setColor(PINK);
        g.fillRoundRect(px - 2, py - 36, 4, 22, 3, 3);
        g.setColor(new Color(150, 200, 235, 150));
        g.fillOval(px - 9, py - 34, 6, 4);
        g.setTransform(oldTransform);
        g.setComposite(oldComposite);
        g.setStroke(oldStroke);
    }
    /** Hình minh họa nhìn nghiêng (màn hình tiêu đề): Thắng đi xe máy. */
    void drawSideBiker(Graphics2D g, double cx, double gy, double sc) {
        long now = System.currentTimeMillis();
        AffineTransform old = g.getTransform();
        Stroke oldStroke = g.getStroke();
        g.translate(cx, gy + Math.sin(now / 170.0) * 1.2);
        g.scale(sc, sc);
        Color orange = new Color(255, 143, 66), orangeD = new Color(214, 104, 36);
        Color tire = new Color(32, 34, 42), jacket = new Color(45, 61, 87);
        Color jeans = new Color(62, 84, 122), skin = new Color(255, 224, 196);
        Color dark = new Color(60, 64, 76);
        BasicStroke round = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g.setColor(new Color(0, 0, 0, 40));
        g.fill(new Ellipse2D.Double(-90, -5, 180, 12));
        // vệt gió
        g.setStroke(round);
        for (int i = 0; i < 3; i++) {
            double x = -96 - ((now / 8.0 + i * 37) % 70);
            double a = 1 - ((now / 8.0 + i * 37) % 70) / 70.0;
            g.setColor(new Color(255, 255, 255, (int) (190 * a)));
            g.draw(new Line2D.Double(x, -34 - i * 24, x - 26, -34 - i * 24));
        }
        // bánh xe
        for (double wx : new double[]{-52, 52}) {
            g.setColor(tire);
            g.fill(new Ellipse2D.Double(wx - 21, -42, 42, 42));
            g.setColor(new Color(196, 202, 212));
            g.fill(new Ellipse2D.Double(wx - 11, -32, 22, 22));
            g.setColor(new Color(120, 126, 138));
            g.setStroke(new BasicStroke(2f));
            double a = now / 70.0;
            for (int k = 0; k < 3; k++) {
                double aa = a + k * Math.PI / 3;
                g.draw(new Line2D.Double(wx - 10 * Math.cos(aa), -21 - 10 * Math.sin(aa),
                        wx + 10 * Math.cos(aa), -21 + 10 * Math.sin(aa)));
            }
            g.setColor(dark);
            g.fill(new Ellipse2D.Double(wx - 3, -24, 6, 6));
        }
        // thân xe
        g.setColor(orange);
        g.fill(new RoundRectangle2D.Double(-78, -66, 70, 38, 26, 26));
        g.setColor(orangeD);
        g.fill(new RoundRectangle2D.Double(-78, -40, 70, 12, 10, 10));
        g.fill(new RoundRectangle2D.Double(-14, -36, 56, 10, 8, 8));
        Path2D.Double shield = new Path2D.Double();
        shield.moveTo(30, -30);
        shield.lineTo(38, -88);
        shield.quadTo(46, -94, 54, -86);
        shield.lineTo(56, -30);
        shield.closePath();
        g.setColor(orange);
        g.fill(shield);
        g.setColor(dark);
        g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(48, -84, 53, -24));
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(42, -94, 34, -100));
        g.setColor(new Color(255, 246, 180, 70));
        poly(g, 62, -80, 124, -94, 124, -60);
        g.setColor(new Color(255, 246, 180));
        g.fill(new Ellipse2D.Double(52, -86, 12, 12));
        g.setColor(orange);
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Arc2D.Double(-77, -46, 50, 50, 15, 150, Arc2D.OPEN));
        g.draw(new Arc2D.Double(27, -46, 50, 50, 20, 140, Arc2D.OPEN));
        g.setColor(new Color(58, 46, 48));
        g.fill(new RoundRectangle2D.Double(-58, -76, 56, 13, 12, 12));
        g.setColor(new Color(255, 70, 80));
        g.fill(new RoundRectangle2D.Double(-82, -58, 7, 14, 4, 4));
        // balo
        g.setColor(MINT);
        g.fill(new RoundRectangle2D.Double(-52, -124, 22, 42, 12, 12));
        g.setColor(new Color(63, 145, 121));
        g.fill(new RoundRectangle2D.Double(-50, -98, 18, 11, 7, 7));
        // thân người, chân, tay
        g.setColor(jacket);
        g.setStroke(new BasicStroke(26f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(-26, -84, -18, -112));
        g.setColor(jeans);
        g.setStroke(new BasicStroke(12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D.Double leg = new Path2D.Double();
        leg.moveTo(-30, -78);
        leg.lineTo(8, -74);
        leg.lineTo(18, -40);
        g.draw(leg);
        g.setColor(new Color(245, 246, 250));
        g.fill(new Ellipse2D.Double(10, -42, 26, 11));
        g.setColor(jacket);
        g.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D.Double arm = new Path2D.Double();
        arm.moveTo(-16, -108);
        arm.lineTo(6, -92);
        arm.lineTo(34, -98);
        g.draw(arm);
        g.setColor(skin);
        g.fill(new Ellipse2D.Double(30, -104, 11, 11));
        // đầu + mũ bảo hiểm
        g.setColor(new Color(52, 40, 44));
        g.fill(new Ellipse2D.Double(-28, -148, 18, 22));
        g.setColor(skin);
        g.fill(new Ellipse2D.Double(-26, -152, 32, 32));
        g.setColor(INK);
        g.fill(new Ellipse2D.Double(-4, -141, 5, 7));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(-3, -140, 2, 2));
        g.setColor(new Color(255, 140, 150, 120));
        g.fill(new Ellipse2D.Double(0, -132, 8, 5));
        g.setColor(new Color(160, 80, 80));
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Arc2D.Double(-6, -135, 9, 6, 200, 140, Arc2D.OPEN));
        Arc2D.Double dome = new Arc2D.Double(-30, -160, 40, 38, 0, 180, Arc2D.CHORD);
        g.setColor(new Color(250, 250, 252));
        g.fill(dome);
        g.setColor(new Color(200, 208, 222));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(dome);
        g.setColor(PINK);
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g.draw(new Arc2D.Double(-26, -156, 32, 30, 65, 50, Arc2D.OPEN));
        g.setColor(new Color(200, 208, 222));
        g.fill(new RoundRectangle2D.Double(-3, -143, 14, 5, 4, 4));
        g.setTransform(old);
        g.setStroke(oldStroke);
    }
    // =====================================================================
    //  TIN NHẮN: khung chat kiểu Messenger trên màn hình điện thoại
    // =====================================================================
    boolean isMessage(Line l) {
        return l.who.endsWith("(tin nhắn)");
    }
    java.util.List<String> wrap(FontMetrics fm, String s, int width) {
        java.util.List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (String word : s.split(" ")) {
            String cand = cur.length() == 0 ? word : cur + " " + word;
            if (fm.stringWidth(cand) > width && cur.length() > 0) {
                out.add(cur.toString());
                cur = new StringBuilder(word);
            } else {
                cur = new StringBuilder(cand);
            }
        }
        out.add(cur.toString());
        return out;
    }
    String chatTime() {
        String section = "";
        for (int i = Math.min(dialogIndex, lines.size() - 1); i >= 0; i--) {
            if (lines.get(i).who.startsWith("@")) {
                section = lines.get(i).who;
                break;
            }
        }
        if (section.isEmpty()) return "22:41";
        if (section.contains("TẠI PHÒNG TRỌ")) return "06:3" + Math.max(0, Math.min(day, 4));
        if (section.contains("SAU KHI THI")) return "11:50";
        return "09:00";
    }
    void drawChatPhone(Graphics2D g) {
        long now = System.currentTimeMillis();
        if (dialogIndex != lastChatIndex) {
            lastChatIndex = dialogIndex;
            chatAnimStart = now;
        }
        double anim = Math.min(1.0, (now - chatAnimStart) / 220.0);
        Color messengerBlue = new Color(0, 132, 255);
        Color grayText = new Color(120, 126, 140);

        g.setColor(new Color(20, 24, 38, 150));
        g.fillRect(0, 0, W, H);

        int pw = 330, ph = 566, px = (W - pw) / 2, py = 12;
        g.setColor(new Color(0, 0, 0, 70));
        g.fill(new RoundRectangle2D.Double(px + 6, py + 10, pw, ph, 48, 48));
        g.setColor(new Color(24, 27, 38));
        g.fill(new RoundRectangle2D.Double(px, py, pw, ph, 48, 48));
        g.setColor(new Color(60, 66, 84));
        g.fillRoundRect(px + pw - 2, py + 110, 4, 56, 3, 3);
        g.fillRoundRect(px - 2, py + 96, 4, 30, 3, 3);
        g.fillRoundRect(px - 2, py + 136, 4, 30, 3, 3);

        int sx = px + 10, sy = py + 10, sw = pw - 20, sh = ph - 20;
        Shape screen = new RoundRectangle2D.Double(sx, sy, sw, sh, 38, 38);
        Shape screenClip = g.getClip();
        Stroke oldStroke = g.getStroke();
        g.setColor(Color.WHITE);
        g.fill(screen);
        g.clip(screen);

        // thanh trạng thái
        g.setFont(new Font("Dialog", Font.BOLD, 13));
        g.setColor(INK);
        g.drawString(chatTime(), sx + 26, sy + 27);
        for (int i = 0; i < 4; i++) g.fillRoundRect(sx + sw - 84 + i * 5, sy + 23 - i * 3, 3, 4 + i * 3, 1, 1);
        g.setStroke(new BasicStroke(1.3f));
        g.drawRoundRect(sx + sw - 50, sy + 16, 22, 11, 4, 4);
        g.fillRoundRect(sx + sw - 47, sy + 19, 14, 5, 2, 2);
        g.fillRect(sx + sw - 27, sy + 20, 2, 3);

        // tiêu đề cuộc trò chuyện
        int hy = sy + 38, hh = 56;
        g.setColor(Color.WHITE);
        g.fillRect(sx, hy, sw, hh);
        g.setColor(new Color(232, 234, 240));
        g.fillRect(sx, hy + hh, sw, 1);
        g.setColor(messengerBlue);
        g.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D.Double back = new Path2D.Double();
        back.moveTo(sx + 22, hy + 19);
        back.lineTo(sx + 13, hy + 28);
        back.lineTo(sx + 22, hy + 37);
        g.draw(back);
        drawAvatar(g, "Sanbaka", sx + 50, hy + 28, 18);
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(sx + 56, hy + 36, 13, 13));
        g.setColor(new Color(49, 200, 86));
        g.fill(new Ellipse2D.Double(sx + 58, hy + 38, 9, 9));
        g.setColor(INK);
        g.setFont(new Font("Dialog", Font.BOLD, 15));
        g.drawString("Sanbaka", sx + 76, hy + 25);
        g.setColor(grayText);
        g.setFont(new Font("Dialog", Font.PLAIN, 11));
        g.drawString("Đang hoạt động", sx + 76, hy + 41);
        g.setColor(messengerBlue);
        g.fill(new RoundRectangle2D.Double(sx + sw - 70, hy + 21, 17, 14, 4, 4));
        poly(g, sx + sw - 51, hy + 25, sx + sw - 43, hy + 20, sx + sw - 43, hy + 36, sx + sw - 51, hy + 31);
        g.setStroke(new BasicStroke(2f));
        g.draw(new Ellipse2D.Double(sx + sw - 32, hy + 19, 18, 18));
        g.fill(new Ellipse2D.Double(sx + sw - 24.2, hy + 23, 2.6, 2.6));
        g.fill(new Rectangle2D.Double(sx + sw - 24.2, hy + 27, 2.6, 6.5));

        // vùng tin nhắn
        int areaTop = hy + hh + 2, areaBottom = sy + sh - 56;
        g.setFont(new Font("Dialog", Font.PLAIN, 14));
        FontMetrics fm = g.getFontMetrics();
        int maxText = (int) (sw * 0.62);
        int start = dialogIndex;
        while (start > 0 && isMessage(lines.get(start - 1))) start--;
        java.util.List<java.util.List<String>> wrapped = new ArrayList<>();
        for (int i = start; i <= dialogIndex; i++) wrapped.add(wrap(fm, lines.get(i).text, maxText));
        int latestH = wrapped.get(wrapped.size() - 1).size() * 19 + 12;
        double scrollOff = (1 - anim) * (latestH + 9);
        Shape beforeChat = g.getClip();
        g.clipRect(sx, areaTop, sw, areaBottom - areaTop);
        int cursor = areaBottom - 10;
        for (int i = dialogIndex; i >= start; i--) {
            Line ln = lines.get(i);
            boolean mine = !ln.who.startsWith("Sanbaka");
            java.util.List<String> rows = wrapped.get(i - start);
            int tw = 0;
            for (String row : rows) tw = Math.max(tw, fm.stringWidth(row));
            int bw = tw + 24, bh = rows.size() * 19 + 12;
            int top = cursor - bh;
            double by = top + scrollOff;
            int bx = mine ? sx + sw - 14 - bw : sx + 46;
            Composite oldComposite = g.getComposite();
            if (i == dialogIndex) g.setComposite(AlphaComposite.SrcOver.derive((float) Math.max(0.05, anim)));
            Shape bubble = new RoundRectangle2D.Double(bx, by, bw, bh, 20, 20);
            if (mine) {
                g.setPaint(new GradientPaint((float) bx, (float) by, new Color(0, 140, 255),
                        (float) (bx + bw), (float) (by + bh), new Color(140, 70, 255)));
            } else {
                g.setColor(new Color(233, 234, 238));
            }
            g.fill(bubble);
            g.setColor(mine ? Color.WHITE : new Color(22, 26, 38));
            g.setFont(new Font("Dialog", Font.PLAIN, 14));
            for (int k = 0; k < rows.size(); k++) {
                g.drawString(rows.get(k), bx + 12, (int) (by + 6 + k * 19 + 14.5));
            }
            boolean nextMine = i < dialogIndex && !lines.get(i + 1).who.startsWith("Sanbaka");
            if (!mine && (i == dialogIndex || nextMine)) {
                drawAvatar(g, "Sanbaka", sx + 26, (int) (by + bh - 14), 14);
            }
            g.setComposite(oldComposite);
            if (i == start) {
                g.setFont(new Font("Dialog", Font.PLAIN, 11));
                g.setColor(grayText);
                String stamp = chatTime();
                g.drawString(stamp, sx + sw / 2 - g.getFontMetrics().stringWidth(stamp) / 2, (int) by - 12);
            }
            boolean sameAsPrev = i > start && (lines.get(i - 1).who.startsWith("Sanbaka") == !mine);
            cursor = top - (sameAsPrev ? 3 : 9);
        }
        g.setClip(beforeChat);

        // ô nhập tin nhắn
        int iy = sy + sh - 56;
        g.setColor(Color.WHITE);
        g.fillRect(sx, iy, sw, 56);
        g.setColor(new Color(232, 234, 240));
        g.fillRect(sx, iy, sw, 1);
        g.setColor(messengerBlue);
        g.fill(new Ellipse2D.Double(sx + 14, iy + 12, 28, 28));
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(sx + 28, iy + 19, sx + 28, iy + 33));
        g.draw(new Line2D.Double(sx + 21, iy + 26, sx + 35, iy + 26));
        g.setColor(new Color(240, 241, 245));
        g.fill(new RoundRectangle2D.Double(sx + 50, iy + 9, sw - 100, 34, 34, 34));
        g.setColor(new Color(150, 156, 170));
        g.setFont(new Font("Dialog", Font.PLAIN, 14));
        g.drawString("Aa", sx + 68, iy + 31);
        drawHeart(g, sx + sw - 29, iy + 27, 24, messengerBlue);
        g.setStroke(oldStroke);
        g.setClip(screenClip);

        // tai thỏ / đảo động
        g.setColor(new Color(10, 12, 18));
        g.fillRoundRect(px + pw / 2 - 42, sy + 8, 84, 22, 22, 22);

        // gợi ý phím
        g.setColor(MINT);
        g.fillRoundRect(W / 2 - 128, py + ph + 10, 256, 32, 32, 32);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Dialog", Font.BOLD, 14));
        String hint = "ENTER / SPACE  ·  TIẾP TỤC";
        g.drawString(hint, W / 2 - g.getFontMetrics().stringWidth(hint) / 2, py + ph + 32);
    }
    // =====================================================================
    //  TRỰC THĂNG: ngày 5, 2/3 đường sau — bay trên trời, né chim
    // =====================================================================
    void startFlight(long now) {
        flightMode = true;
        things.clear();
        birds.clear();
        heliX = 170;
        heliY = 300;
        heliTilt = 0;
        lastBird = now;
        invulnerable = 0;
        hp = FLIGHT_START_HP;
    }
    void updateFlight(long now) {
        double dt = Math.min((now - lastFrameAt) / 1000.0, 0.05);
        lastFrameAt = now;
        double flightLen = ROAD_DURATION_MS * 2.0 / 3.0;
        double progress = Math.max(0, Math.min(1, (elapsed - ROAD_DURATION_MS / 3.0) / flightLen));

        double dx = (right ? 1 : 0) - (left ? 1 : 0);
        double dy = (down ? 1 : 0) - (up ? 1 : 0);
        heliX = Math.max(90, Math.min(W - 120, heliX + dx * PLAYER_SPEED * 1.15 * dt));
        heliY = Math.max(80, Math.min(500, heliY + dy * VERTICAL_SPEED * dt));
        heliTilt += (dx * 0.14 - heliTilt) * Math.min(1.0, dt * 8);
        skyScroll += (240 + 120 * progress) * dt;

        boolean canSpawn = elapsed < ROAD_DURATION_MS - 3000;
        if (canSpawn && now - lastBird > 820 - 340 * progress) {
            lastBird = now;
            double speed = 300 + 150 * progress;
            double baseY = 130 + rng.nextInt(340);
            int count = rng.nextInt(5) == 0 ? 3 : 1;
            for (int i = 0; i < count; i++) {
                double off = i - (count - 1) / 2.0;
                birds.add(new Bird(W + 40 + Math.abs(off) * 42, baseY + off * 62, -speed, rng.nextDouble() * 6.28));
            }
        }

        Iterator<Bird> it = birds.iterator();
        while (it.hasNext()) {
            Bird b = it.next();
            b.x += b.vx * dt;
            b.phase += dt * 13;
            double by = b.y + Math.sin(b.phase * 0.35) * 6;
            if (Math.abs(b.x - heliX) < 52 && Math.abs(by - heliY) < 30) {
                if (invulnerable <= now) {
                    hp -= 22;
                    invulnerable = now + 950;
                    lastHitAt = now;
                }
                it.remove();
            } else if (b.x < -80) {
                it.remove();
            }
        }
    }
    void drawCloud(Graphics2D g, double x, double y, double s, int a) {
        g.setColor(new Color(255, 255, 255, a));
        g.fill(new Ellipse2D.Double(x, y, 110 * s, 34 * s));
        g.fill(new Ellipse2D.Double(x + 20 * s, y - 18 * s, 56 * s, 42 * s));
        g.fill(new Ellipse2D.Double(x + 54 * s, y - 10 * s, 48 * s, 36 * s));
    }
    /** Bầu trời + mặt trời + mây + thành phố tắc đường ở phía dưới. */
    void drawSkyScene(Graphics2D g, double scroll) {
        g.setPaint(new GradientPaint(0, 0, new Color(84, 156, 228), 0, 560, new Color(208, 234, 252)));
        g.fillRect(0, 0, W, H);
        for (int i = 3; i >= 0; i--) {
            double r = 40 + i * 22;
            g.setColor(new Color(255, 244, 196, 30 + (3 - i) * 40));
            g.fill(new Ellipse2D.Double(790 - r, 95 - r, 2 * r, 2 * r));
        }
        double[] cloudSpeed = {0.18, 0.4, 0.8};
        double[] cloudSize = {0.7, 1.0, 1.5};
        int[] cloudAlpha = {120, 170, 225};
        int span = W + 360;
        for (int l = 0; l < 3; l++) {
            for (int i = 0; i < 5; i++) {
                double x = (((i * 264 + l * 110 - scroll * cloudSpeed[l]) % span) + span) % span - 180;
                double y = 50 + ((i * 97 + l * 61) % 300);
                drawCloud(g, x, y, cloudSize[l], cloudAlpha[l]);
            }
        }
        int gy = 556;
        g.setColor(new Color(160, 196, 190));
        g.fill(new Ellipse2D.Double(-120, gy - 40, 520, 120));
        g.fill(new Ellipse2D.Double(330, gy - 56, 640, 140));
        for (int i = 0; i < 22; i++) {
            double bw = 34 + (i * 29) % 26;
            double bh = 28 + (i * 53) % 52;
            double bx = (((i * 52 - scroll * 0.3) % 1144) + 1144) % 1144 - 60;
            g.setColor(new Color(166, 186, 214));
            g.fill(new Rectangle2D.Double(bx, gy - bh, bw, bh + 4));
            g.setColor(new Color(214, 228, 244, 200));
            for (int wy = (int) (gy - bh + 8); wy < gy - 6; wy += 12) {
                for (int wx = 5; wx < bw - 6; wx += 11) g.fill(new Rectangle2D.Double(bx + wx, wy, 5, 6));
            }
        }
        g.setColor(new Color(112, 169, 115));
        g.fillRect(0, gy, W, H - gy);
        g.setColor(new Color(90, 100, 111));
        g.fillRect(0, gy + 10, W, 28);
        for (int i = 0; i < 24; i++) {
            double cx = (((i * 46 - scroll * 0.5) % 1104) + 1104) % 1104 - 60;
            g.setColor(i % 3 == 0 ? new Color(219, 93, 96) : i % 3 == 1 ? new Color(81, 146, 185) : new Color(240, 200, 90));
            g.fill(new RoundRectangle2D.Double(cx, gy + 16, 36, 16, 8, 8));
            g.setColor(new Color(225, 239, 242));
            g.fill(new Rectangle2D.Double(cx + 10, gy + 19, 14, 8));
        }
    }
    void drawFlightScene(Graphics2D g) {
        long now = System.currentTimeMillis();
        drawSkyScene(g, skyScroll);
        double rem = Math.max(0, (ROAD_DURATION_MS - elapsed) / 1000.0);
        if (rem < 8.0) drawSchoolFacade(g, W / 2.0 + rem / 8.0 * 700, 572, 0.62);
        Stroke oldStroke = g.getStroke();
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 9; i++) {
            double x = (((i * 173 - skyScroll * 2.4) % (W + 200)) + (W + 200)) % (W + 200) - 100;
            double y = 90 + (i * 61) % 400;
            g.setColor(new Color(255, 255, 255, 70));
            g.draw(new Line2D.Double(x, y, x + 70, y));
        }
        g.setStroke(oldStroke);
        for (Bird b : birds) drawBird(g, b.x, b.y + Math.sin(b.phase * 0.35) * 6, b.phase);
        Composite oldComposite = g.getComposite();
        if (invulnerable > now && (now / 90) % 2 == 0) {
            g.setComposite(AlphaComposite.SrcOver.derive(0.4f));
        }
        drawHelicopter(g, heliX, heliY, heliTilt, now / 1000.0, 0.9);
        g.setComposite(oldComposite);
    }
    void drawBird(Graphics2D g, double x, double y, double phase) {
        double flap = Math.sin(phase);
        Color body = new Color(44, 50, 70);
        Path2D.Double wing = new Path2D.Double();
        wing.moveTo(x + 2, y - 2);
        wing.quadTo(x + 14, y - 4 - flap * 20, x + 34, y - flap * 30);
        wing.quadTo(x + 18, y + 4 - flap * 8, x + 4, y + 5);
        wing.closePath();
        g.setColor(new Color(30, 36, 54));
        g.fill(wing);
        g.setColor(body);
        g.fill(new Ellipse2D.Double(x - 15, y - 8, 32, 17));
        poly(g, x + 14, y - 3, x + 30, y - 6, x + 28, y + 5);
        g.fill(new Ellipse2D.Double(x - 22, y - 11, 14, 14));
        g.setColor(new Color(214, 222, 236));
        g.fill(new Ellipse2D.Double(x - 11, y - 1, 22, 9));
        g.setColor(new Color(255, 176, 64));
        poly(g, x - 21, y - 7, x - 32, y - 3.5, x - 21, y - 1);
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(x - 19, y - 8, 5, 5));
        g.setColor(INK);
        g.fill(new Ellipse2D.Double(x - 18.5, y - 7, 2.6, 2.6));
        wing = new Path2D.Double();
        wing.moveTo(x + 4, y);
        wing.quadTo(x + 14, y - 2 - flap * 16, x + 28, y - flap * 22 + 4);
        wing.quadTo(x + 16, y + 6 - flap * 6, x + 2, y + 6);
        wing.closePath();
        g.setColor(new Color(60, 68, 94));
        g.fill(wing);
    }
    /** Trực thăng của bạn cùng phòng (hướng sang phải), có Thắng ngồi cùng. */
    void drawHelicopter(Graphics2D g, double cx, double cy, double tilt, double time, double sc) {
        AffineTransform old = g.getTransform();
        Stroke oldStroke = g.getStroke();
        g.translate(cx, cy + Math.sin(time * 4.2) * 2);
        g.rotate(tilt);
        g.scale(sc, sc);
        Color bodyD = new Color(63, 145, 121);
        Color glass = new Color(196, 230, 246);
        // đuôi + cánh quạt đuôi
        g.setColor(bodyD);
        poly(g, -40, -10, -128, -14, -128, -5, -40, 12);
        g.setColor(PINK);
        poly(g, -126, -12, -140, -40, -124, -40, -108, -10);
        double ta = time * 30;
        g.setColor(new Color(40, 50, 70, 200));
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(-130, -8 - 15 * Math.cos(ta), -130, -8 + 15 * Math.cos(ta)));
        // càng đáp
        g.setColor(INK);
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(-46, 44, 52, 44));
        g.draw(new Line2D.Double(52, 44, 62, 38));
        g.draw(new Line2D.Double(-22, 26, -26, 44));
        g.draw(new Line2D.Double(24, 26, 28, 44));
        // thân
        g.setColor(MINT);
        g.fill(new RoundRectangle2D.Double(-54, -32, 112, 62, 52, 52));
        g.setColor(new Color(255, 255, 255, 210));
        g.fill(new RoundRectangle2D.Double(-46, 8, 92, 9, 9, 9));
        g.setColor(glass);
        g.fill(new RoundRectangle2D.Double(6, -25, 48, 38, 30, 30));
        g.fill(new RoundRectangle2D.Double(-34, -21, 30, 24, 12, 12));
        drawAvatar(g, "Thắng", -19, -8, 9);
        drawAvatar(g, "Nam", 30, -6, 11);
        g.setColor(new Color(255, 255, 255, 110));
        g.fill(new Ellipse2D.Double(36, -22, 13, 7));
        // cánh quạt chính
        g.setColor(INK);
        g.fill(new Rectangle2D.Double(-5, -46, 10, 16));
        g.setColor(new Color(40, 50, 70, 40));
        g.fill(new Ellipse2D.Double(-100, -54, 200, 14));
        double a = time * 38;
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(40, 50, 70, 200));
        g.draw(new Line2D.Double(-96 * Math.cos(a), -47, 96 * Math.cos(a), -47));
        g.setColor(new Color(40, 50, 70, 120));
        g.draw(new Line2D.Double(-96 * Math.sin(a), -47, 96 * Math.sin(a), -47));
        g.setTransform(old);
        g.setStroke(oldStroke);
    }
    void drawCurrentDialog(Graphics2D g) {
        if (dialogIndex >= lines.size()) return;
        Line line = lines.get(dialogIndex);
        if (isMessage(line)) drawChatPhone(g);
        else {
            if (isCallLine(dialogIndex)) drawCallPhone(g);
            drawDialogueBox(g, line);
        }
    }
    void drawAir(Graphics2D g) {
        long now = System.currentTimeMillis();
        double t = Math.max(0, Math.min(1, (now - airSceneStarted) / 3500.0));
        drawSkyScene(g, now / 8.0);
        double ease = 1 - Math.pow(1 - Math.min(1.0, t * 1.5), 3);
        double hx = W / 2.0 + Math.sin(now / 400.0) * 6;
        double hy = -90 + 390 * ease;
        Stroke oldStroke = g.getStroke();
        g.setColor(new Color(90, 70, 60));
        g.setStroke(new BasicStroke(3f));
        double top = hy + 52;
        g.draw(new Line2D.Double(hx - 8, top, hx - 8, top + 130));
        g.draw(new Line2D.Double(hx + 8, top, hx + 8, top + 130));
        for (int i = 1; i <= 6; i++) g.draw(new Line2D.Double(hx - 8, top + i * 20, hx + 8, top + i * 20));
        g.setStroke(oldStroke);
        drawHelicopter(g, hx, hy, 0.05, now / 1000.0, 1.25);
        panel(g, 250, 36, 460, 88, new Color(255, 255, 255, 228));
        g.setColor(INK);
        g.setFont(new Font("Dialog", Font.BOLD, 28));
        String a = "LÊN TRỰC THĂNG!";
        g.drawString(a, W / 2 - g.getFontMetrics().stringWidth(a) / 2, 80);
        g.setFont(new Font("Dialog", Font.PLAIN, 17));
        String b = "Nhìn xuống, đường tắc đã ở phía sau…";
        g.drawString(b, W / 2 - g.getFontMetrics().stringWidth(b) / 2, 108);
    }
    void drawDialogueBox(Graphics2D g, Line line) {
        panel(g, 48, 398, 864, 198, new Color(255, 255, 255, 245));
        drawAvatar(g, line.who, 122, 497, 48);
        g.setColor(PINK);
        g.setFont(new Font("Dialog", Font.BOLD, 20));
        g.drawString(line.who, 196, 440);
        g.setColor(INK);
        g.setFont(new Font("Dialog", Font.PLAIN, 21));
        drawWrapped(g, line.text, 196, 480, 690, 31);
        g.setColor(MINT);
        g.setFont(new Font("Dialog", Font.BOLD, 15));
        String hint = "ENTER / SPACE  ·  TIẾP TỤC";
        g.drawString(hint, 888 - g.getFontMetrics().stringWidth(hint), 570);
    }
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) left = true;
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) right = true;
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) up = true;
        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) down = true;
        if (key != KeyEvent.VK_ENTER && key != KeyEvent.VK_SPACE) return;
        if (screen == TITLE) {
            screen = INTRO;
        }
        else if (screen == INTRO) {
            begin();
        }
        else if (screen == ROOM) {
            advanceRoom();
        }
        else if (screen == DIALOG) {
            advanceDialog();
        }
        else if (screen == CARD) {
            if (System.currentTimeMillis() - cardStart > 700) endCard(System.currentTimeMillis());
        }
        else if (screen == ENDING_BE) {
            dialogIndex = retryDialogIndex;   // chơi lại đúng ngày vừa thua
            beginDay();
        }
        else if (screen == ENDING_HE) {
            screen = TITLE;
            dialogIndex = 0;
            day = 0;
        }
    }
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) left = false;
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) right = false;
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) up = false;
        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) down = false;
    }
    public void keyTyped(KeyEvent e) {
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Tuần thi của SVBK");
            SanbakaGame game = new SanbakaGame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(game);
            frame.pack();
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            game.requestFocusInWindow();
        });
    }
}
