package sanbaka.render;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Hệ thống chữ nổi (Floating Combat Text) hiển thị các thông báo IN HOA cạnh nhân vật.
 * Dữ liệu được nạp từ file duy nhất `floating_texts.json` phân tách thành hai mục:
 * "harmful" (bất lợi) và "beneficial" (có lợi), trong mỗi mục chia theo các tình huống con.
 */
public class FloatingTextManager {
    private static final Font TEXT_FONT = new Font("Dialog", Font.BOLD, 13);

    public static class FloatingText {
        public double x, y;
        public double vy = -38.0;
        public String text;
        public boolean beneficial;
        public long bornAt;
        public long lifetimeMs = 1100;

        public FloatingText(double x, double y, String text, boolean beneficial, long bornAt) {
            this.x = x;
            this.y = y;
            this.text = text != null ? text.toUpperCase() : "";
            this.beneficial = beneficial;
            this.bornAt = bornAt;
        }
    }

    private final Map<String, List<String>> harmfulTexts = new LinkedHashMap<>();
    private final Map<String, List<String>> beneficialTexts = new LinkedHashMap<>();
    private final List<FloatingText> activeTexts = new ArrayList<>();
    private final Map<String, Long> lastSpawnTimes = new HashMap<>();
    private final Random rng = new Random();

    public FloatingTextManager() {
        initDefaults();
        loadFromJson();
    }

    private void initDefaults() {
        harmfulTexts.clear();
        beneficialTexts.clear();

        harmfulTexts.put("collision", new ArrayList<>(List.of("VA CHẠM!", "ÚI TRỜI!", "CẨN THẬN!", "MẤT MÁU!")));
        harmfulTexts.put("curb_fire", new ArrayList<>(List.of("BỐC LỬA!", "CẠ VỈA HÈ!", "NÓNG QUÁ!", "CHÁY XE!")));
        harmfulTexts.put("ice_slip", new ArrayList<>(List.of("TRƯỢT BĂNG!", "MẤT LÁI!", "TRƠN TRƯỢT!", "XÒE RỒI!")));
        harmfulTexts.put("freeze", new ArrayList<>(List.of("ĐÓNG BĂNG!", "RÉT CĂM CĂM!", "BỊ LÀM CHẬM!", "TÊ CỐNG!")));
        harmfulTexts.put("pothole", new ArrayList<>(List.of("SỤT Ổ GÀ!", "XÓC NẢY NGƯỜI!", "THỤT HỐ!", "HỎNG LỐP!")));
        harmfulTexts.put("dog_bark", new ArrayList<>(List.of("CHÓ RƯỢT!", "GIẬT CẢ MÌNH!", "GÂU GÂU!", "CẨN THẬN CHÓ!")));
        harmfulTexts.put("low_energy", new ArrayList<>(List.of("SAY NẮNG!", "HOA CẢ MẮT!", "KIỆT SỨC!", "CẦN NĂNG LƯỢNG!")));
        harmfulTexts.put("shark", new ArrayList<>(List.of("CÁ MẬP CẮN!", "NGUY HIỂM!", "ĐỚP HỤT!", "TOANG RỒI!")));
        harmfulTexts.put("boss_hit", new ArrayList<>(List.of("TRÚNG ĐẠN!", "SÁT THƯƠNG CAO!", "CẢNH BÁO!")));
        harmfulTexts.put("shield_break", new ArrayList<>(List.of("KHIÊN ĐÃ VỠ!", "MẤT BẢO HỘ!", "HẾT KHIÊN!")));

        beneficialTexts.put("energy_bolt", new ArrayList<>(List.of("NẠP ĐẦY PIN!", "TĂNG LỰC!", "HỒI NĂNG LƯỢNG!", "+27 NĂNG LƯỢNG!")));
        beneficialTexts.put("ginger_tea", new ArrayList<>(List.of("TRÀ GỪNG ẤM!", "HÓA GIẢI ĐÓNG BĂNG!", "GIỮ ẤM CƠ THỂ!", "+15 MÁU!")));
        beneficialTexts.put("shield_pickup", new ArrayList<>(List.of("KHIÊN BẢO VỆ!", "BẢO HỘ 1 ĐÒN!", "KHIÊN NĂNG LƯỢNG!", "AN TOÀN RỒI!")));
        beneficialTexts.put("shield_block", new ArrayList<>(List.of("CHẶN THÀNH CÔNG!", "MIỄN SÁT THƯƠNG!", "KHIÊN ĐỠ ĐÒN!", "KHÔNG SAO HẾT!")));
        beneficialTexts.put("heal", new ArrayList<>(List.of("HỒI MÁU!", "KHỎE LẠI RỒI!", "TĂNG SINH LỰC!")));
        beneficialTexts.put("missile_hit", new ArrayList<>(List.of("TRÚNG MỤC TIÊU!", "BẮN TỐT LẮM!", "BÙM!", "HẠ GỤC!")));
        beneficialTexts.put("speed_up", new ArrayList<>(List.of("TĂNG TỐC!", "LƯỚT NHANH!")));
        beneficialTexts.put("mystery_box", new ArrayList<>(List.of("???", "MYSTERY BOX!", "HỘP BÍ ẨN!", "EASTER EGG!")));
    }

    public synchronized void reload() {
        loadFromJson();
    }

    public void loadFromJson() {
        String json = readJsonString();
        if (json == null || json.trim().isEmpty()) return;
        try {
            MiniJsonParser parser = new MiniJsonParser(json);
            Object rootObj = parser.parse();
            if (!(rootObj instanceof Map<?, ?> root)) return;

            Object harmfulObj = root.get("harmful");
            if (harmfulObj instanceof Map<?, ?> hMap) {
                for (Map.Entry<?, ?> entry : hMap.entrySet()) {
                    String subcat = String.valueOf(entry.getKey());
                    if (entry.getValue() instanceof List<?> list) {
                        List<String> textList = new ArrayList<>();
                        for (Object item : list) {
                            if (item != null) textList.add(item.toString().toUpperCase());
                        }
                        if (!textList.isEmpty()) {
                            harmfulTexts.put(subcat, textList);
                        }
                    }
                }
            }

            Object beneficialObj = root.get("beneficial");
            if (beneficialObj instanceof Map<?, ?> bMap) {
                for (Map.Entry<?, ?> entry : bMap.entrySet()) {
                    String subcat = String.valueOf(entry.getKey());
                    if (entry.getValue() instanceof List<?> list) {
                        List<String> textList = new ArrayList<>();
                        for (Object item : list) {
                            if (item != null) textList.add(item.toString().toUpperCase());
                        }
                        if (!textList.isEmpty()) {
                            beneficialTexts.put(subcat, textList);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            // Nếu có lỗi định dạng trong file JSON người dùng sửa, vẫn dùng bộ mặc định an toàn
        }
    }

    private String readJsonString() {
        // 1. Thử đọc từ classpath
        try (InputStream in = FloatingTextManager.class.getResourceAsStream("/floating_texts.json")) {
            if (in != null) return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception ignored) {}

        try (InputStream in = FloatingTextManager.class.getResourceAsStream("/assets/floating_texts.json")) {
            if (in != null) return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception ignored) {}

        // 2. Thử đọc từ file hệ thống (thư mục hiện tại hoặc các thư mục cha)
        Path cwd = Paths.get("").toAbsolutePath();
        for (Path dir = cwd; dir != null; dir = dir.getParent()) {
            Path file1 = dir.resolve("floating_texts.json");
            if (Files.isRegularFile(file1)) {
                try {
                    return Files.readString(file1, StandardCharsets.UTF_8);
                } catch (Exception ignored) {}
            }
            Path file2 = dir.resolve("assets/floating_texts.json");
            if (Files.isRegularFile(file2)) {
                try {
                    return Files.readString(file2, StandardCharsets.UTF_8);
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    public synchronized void spawn(double x, double y, String subcategory, boolean beneficial) {
        Map<String, List<String>> map = beneficial ? beneficialTexts : harmfulTexts;
        List<String> options = map.get(subcategory);
        if (options == null || options.isEmpty()) {
            options = beneficial ? beneficialTexts.get("heal") : harmfulTexts.get("collision");
        }
        if (options == null || options.isEmpty()) return;

        String phrase = options.get(rng.nextInt(options.size()));
        long now = System.currentTimeMillis();

        // Tránh xếp chồng khít nếu các chữ nảy ra liên tiếp cùng lúc
        for (FloatingText existing : activeTexts) {
            if (Math.abs(existing.y - y) < 18 && Math.abs(existing.x - x) < 32 && (now - existing.bornAt) < 250) {
                y -= 18;
            }
        }

        activeTexts.add(new FloatingText(x, y, phrase, beneficial, now));
        lastSpawnTimes.put(subcategory, now);
    }

    public synchronized void spawnDebounced(double x, double y, String subcategory, boolean beneficial, long cooldownMs) {
        long now = System.currentTimeMillis();
        Long lastTime = lastSpawnTimes.get(subcategory);
        if (lastTime != null && now - lastTime < cooldownMs) return;
        spawn(x, y, subcategory, beneficial);
    }

    public synchronized void clear() {
        activeTexts.clear();
        lastSpawnTimes.clear();
    }

    public synchronized int getActiveCount() {
        return activeTexts.size();
    }

    public synchronized Map<String, List<String>> getHarmfulMap() {
        return harmfulTexts;
    }

    public synchronized Map<String, List<String>> getBeneficialMap() {
        return beneficialTexts;
    }

    public synchronized void draw(Graphics2D g, long now) {
        if (activeTexts.isEmpty()) return;
        Iterator<FloatingText> it = activeTexts.iterator();
        Font oldFont = g.getFont();
        g.setFont(TEXT_FONT);
        FontMetrics fm = g.getFontMetrics();

        while (it.hasNext()) {
            FloatingText ft = it.next();
            long age = now - ft.bornAt;
            if (age >= ft.lifetimeMs || age < 0) {
                it.remove();
                continue;
            }
            double progress = age / (double) ft.lifetimeMs;
            double curY = ft.y + ft.vy * (age / 1000.0);
            int alpha = (int) Math.max(0, Math.min(255, 255 * (1.0 - Math.pow(progress, 1.6))));
            if (alpha <= 6) continue;

            int strW = fm.stringWidth(ft.text);
            int drawX = (int) Math.round(ft.x);
            int drawY = (int) Math.round(curY);

            // Giữ chữ không bị tràn ra ngoài biên màn hình
            if (drawX + strW > 940) drawX = 940 - strW;
            if (drawX < 20) drawX = 20;

            // Vẽ viền tối 4 hướng tương phản cao giúp chữ nổi bật trên mọi nền
            Color outlineColor = ft.beneficial
                    ? new Color(8, 36, 12, (int) (alpha * 0.90f))
                    : new Color(42, 8, 8, (int) (alpha * 0.90f));
            g.setColor(outlineColor);
            g.drawString(ft.text, drawX - 1, drawY);
            g.drawString(ft.text, drawX + 1, drawY);
            g.drawString(ft.text, drawX, drawY - 1);
            g.drawString(ft.text, drawX, drawY + 1);

            // Vẽ chữ chính (Xanh lá sáng rực cho có lợi, Đỏ cam rực rỡ cho bất lợi)
            Color fillColor = ft.beneficial
                    ? new Color(95, 245, 120, alpha)
                    : new Color(255, 75, 75, alpha);
            g.setColor(fillColor);
            g.drawString(ft.text, drawX, drawY);
        }
        g.setFont(oldFont);
    }

    /** Parser JSON thuần Java siêu gọn, không cần thư viện ngoài. */
    public static class MiniJsonParser {
        private final String src;
        private int pos = 0;

        public MiniJsonParser(String src) {
            this.src = src != null ? src : "";
        }

        public Object parse() {
            skipWhitespace();
            if (pos >= src.length()) return null;
            char c = src.charAt(pos);
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            return null;
        }

        private void skipWhitespace() {
            while (pos < src.length()) {
                char c = src.charAt(pos);
                if (Character.isWhitespace(c)) {
                    pos++;
                } else if (c == '/' && pos + 1 < src.length() && src.charAt(pos + 1) == '/') {
                    pos += 2;
                    while (pos < src.length() && src.charAt(pos) != '\n' && src.charAt(pos) != '\r') pos++;
                } else {
                    break;
                }
            }
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            pos++; // bỏ '{'
            while (true) {
                skipWhitespace();
                if (pos >= src.length() || src.charAt(pos) == '}') {
                    if (pos < src.length()) pos++;
                    break;
                }
                if (src.charAt(pos) == ',') {
                    pos++;
                    continue;
                }
                String key = parseString();
                skipWhitespace();
                if (pos < src.length() && src.charAt(pos) == ':') pos++;
                Object val = parse();
                if (key != null) map.put(key, val);
            }
            return map;
        }

        private List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            pos++; // bỏ '['
            while (true) {
                skipWhitespace();
                if (pos >= src.length() || src.charAt(pos) == ']') {
                    if (pos < src.length()) pos++;
                    break;
                }
                if (src.charAt(pos) == ',') {
                    pos++;
                    continue;
                }
                Object val = parse();
                if (val != null) list.add(val);
            }
            return list;
        }

        private String parseString() {
            skipWhitespace();
            if (pos >= src.length() || src.charAt(pos) != '"') return "";
            pos++; // bỏ '"'
            StringBuilder sb = new StringBuilder();
            while (pos < src.length()) {
                char c = src.charAt(pos++);
                if (c == '"') break;
                if (c == '\\' && pos < src.length()) {
                    char esc = src.charAt(pos++);
                    switch (esc) {
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case '"' -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        default -> sb.append(esc);
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }
    }
}

