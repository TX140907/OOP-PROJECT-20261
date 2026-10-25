package sanbaka.render;

import sanbaka.model.*;
import sanbaka.state.GameState;

import java.awt.*;
import java.awt.geom.*;

/** Vẽ menu chính, chọn cảnh, tạm dừng và hướng dẫn. */
public class GameMenuRenderer extends GameRoadRenderer {
    void drawMenuIcon(Graphics2D g, String id, double cx, double cy, Color c) {
        Stroke old = g.getStroke();
        g.setColor(c);
        g.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (id) {
            case "play":
            case "resume":
                poly(g, cx - 6, cy - 10, cx - 6, cy + 10, cx + 10, cy);
                break;
            case "scenes":
                for (int i = 0; i < 3; i++) {
                    g.fill(new Ellipse2D.Double(cx - 11, cy - 9 + i * 8, 3.6, 3.6));
                    g.draw(new Line2D.Double(cx - 4, cy - 7.2 + i * 8, cx + 11, cy - 7.2 + i * 8));
                }
                break;
            case "chars":
                g.fill(new Ellipse2D.Double(cx - 5, cy - 11, 10, 10));
                g.fill(new Arc2D.Double(cx - 10, cy + 1, 20, 18, 0, 180, Arc2D.PIE));
                break;
            case "howto":
                g.setFont(new Font("Dialog", Font.BOLD, 22));
                g.drawString("?", (float) (cx - g.getFontMetrics().stringWidth("?") / 2.0), (float) (cy + 8));
                break;
            default:
                poly(g, cx - 12, cy - 1, cx, cy - 12, cx + 12, cy - 1);
                g.fill(new Rectangle2D.Double(cx - 8, cy - 1, 16, 12));
                break;
        }
        g.setStroke(old);
    }
    void menuButton(Graphics2D g, String id, String label, String sub, int idx, double x, double y, double w, double h) {
        addBtn(id, x, y, w, h, idx);
        boolean sel = isHover(x, y, w, h)
                || (screen == PAUSE && menuKeyboardActive && menuSel == idx);
        g.setColor(new Color(27, 36, 53, 70));
        g.fill(new RoundRectangle2D.Double(x + 3, y + 6, w, h, h, h));
        g.setColor(sel ? PINK : new Color(255, 255, 255, 242));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, h, h));
        g.setColor(sel ? new Color(255, 255, 255, 70) : new Color(255, 232, 238));
        g.fill(new Ellipse2D.Double(x + 9, y + 9, h - 18, h - 18));
        drawMenuIcon(g, id, x + h / 2, y + h / 2, sel ? Color.WHITE : PINK);
        g.setFont(new Font("Dialog", Font.BOLD, 21));
        g.setColor(sel ? Color.WHITE : INK);
        double ty = sub == null ? y + h / 2 + 8 : y + h * 0.45;
        g.drawString(label, (float) (x + h + 4), (float) ty);
        if (sub != null) {
            g.setFont(new Font("Dialog", Font.PLAIN, 13));
            g.setColor(sel ? new Color(255, 255, 255, 230) : new Color(110, 116, 130));
            g.drawString(sub, (float) (x + h + 4), (float) (ty + 20));
        }
    }
    void drawBackButton(Graphics2D g) {
        double x = 20, y = 16, s = 46;
        addBtn("back", x, y, s, s, -1);
        boolean hv = isHover(x, y, s, s);
        g.setColor(new Color(27, 36, 53, 60));
        g.fill(new Ellipse2D.Double(x + 2, y + 4, s, s));
        g.setColor(hv ? PINK : new Color(255, 255, 255, 245));
        g.fill(new Ellipse2D.Double(x, y, s, s));
        g.setColor(hv ? Color.WHITE : INK);
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(3.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D.Double chevron = new Path2D.Double();
        chevron.moveTo(x + 28, y + 13);
        chevron.lineTo(x + 18, y + 23);
        chevron.lineTo(x + 28, y + 33);
        g.draw(chevron);
        g.setStroke(old);
    }

    void drawPauseButton(Graphics2D g) {
        drawPauseButton(g, 648);
    }

    void drawPauseButton(Graphics2D g, double x) {
        double y = 22, s = 42;
        addBtn("pause", x, y, s, s, -1);
        boolean hv = isHover(x, y, s, s);
        g.setColor(new Color(27, 36, 53, 70));
        g.fill(new Ellipse2D.Double(x + 2, y + 4, s, s));
        g.setColor(hv ? PINK : new Color(255, 255, 255, 238));
        g.fill(new Ellipse2D.Double(x, y, s, s));
        g.setColor(hv ? Color.WHITE : INK);
        g.fillRoundRect((int) x + 14, (int) y + 12, 5, 18, 3, 3);
        g.fillRoundRect((int) x + 23, (int) y + 12, 5, 18, 3, 3);
    }

    void drawSoftBackground(Graphics2D g) {
        if (menuForegroundOnly) return;
        if (dialogBackground != null) {
            g.drawImage(dialogBackground, 0, 0, W, H, null);
            g.setColor(new Color(18, 22, 36, 150));
            g.fillRect(0, 0, W, H);
        } else {
            g.setPaint(new GradientPaint(0, 0, new Color(255, 232, 238), 0, H, new Color(255, 244, 233)));
            g.fillRect(0, 0, W, H);
            g.setColor(new Color(255, 255, 255, 110));
            g.fillOval(-80, 440, 360, 360);
            g.fillOval(720, -120, 380, 380);
        }
    }
    int unlockedCount() {
        int n = 0;
        for (int i = 0; i < scenes.size(); i++) if (sceneUnlocked(i)) n++;
        return n;
    }

    // ---------- màn hình chính ----------
    void drawTitleBackground(Graphics2D g) {
        boolean hasBg = dialogBackground != null;
        if (hasBg) {
            g.drawImage(dialogBackground, 0, 0, W, H, null);
            g.setPaint(new GradientPaint(0, 0, new Color(18, 22, 36, 175), 540, 0, new Color(18, 22, 36, 0)));
            g.fillRect(0, 0, 540, H);
        } else {
            g.setColor(new Color(255, 224, 231));
            g.fillRect(0, 0, W, H);
            g.setColor(new Color(255, 245, 233));
            g.fillOval(580, -130, 480, 480);
            g.setColor(new Color(255, 255, 255, 90));
            for (int i = 0; i < 7; i++) g.fillOval(50 + i * 150, 70 + (i % 2) * 70, 100, 60);
            drawSideBiker(g, 780, 330, 1.2);
        }
    }

    void drawTitle(Graphics2D g) {
        boolean hasBg = dialogBackground != null;
        if (!menuForegroundOnly) drawTitleBackground(g);
        g.setFont(new Font("Dialog", Font.BOLD, 46));
        if (hasBg) {
            g.setColor(new Color(0, 0, 0, 110));
            g.setFont(titleFont);
            g.drawString("TUẦN THI CỦA SVBK", 62, 122);
        }
        g.setColor(hasBg ? Color.WHITE : INK);
        g.drawString("TUẦN THI CỦA SVBK", 60, 120);
        g.setFont(new Font("Dialog", Font.PLAIN, 18));
        String[] sub = {"Một tuần thi, một lời hứa,", "    và một thằng bạn cùng phòng kỳ lạ."};
        for (int i = 0; i < 2; i++) {
            if (hasBg) {
                g.setColor(new Color(0, 0, 0, 110));
                g.drawString(sub[i], 62, 156 + i * 26);
            }
            g.setColor(hasBg ? new Color(255, 240, 244) : INK);
            g.drawString(sub[i], 60, 154 + i * 26);
        }
        menuSel = Math.max(0, Math.min(3, menuSel));
        String[] ids = {"play", "scenes", "chars", "howto"};
        String[] labels = {"Vào chơi", "Menu", "Giới thiệu nhân vật", "Hướng dẫn chơi game"};
        String[] subs = {playSubtitle(), "Chọn cảnh đã mở khóa để chơi lại",
                "Sanbaka, Thắng, Komori và chủ trọ", "Phím điều khiển và mẹo chơi"};
        for (int i = 0; i < 4; i++) menuButton(g, ids[i], labels[i], subs[i], i, 60, 226 + i * 84, 410, 68);
        g.setFont(new Font("Dialog", Font.PLAIN, 13));
        g.setColor(hasBg ? new Color(255, 255, 255, 210) : new Color(110, 116, 130));
        g.drawString("Đã mở khóa " + unlockedCount() + " / " + scenes.size() + " cảnh", 62, 604);
    }

    // ---------- tạm dừng ----------
    void drawPauseBackground(Graphics2D g) {
        drawRoad(g);
    }

    void drawPause(Graphics2D g) {
        drawPauseBackground(g);
        g.setColor(new Color(20, 24, 38, 170));
        g.fillRect(0, 0, W, H);
        panel(g, 250, 96, 460, 448, new Color(255, 255, 255, 246));
        g.setColor(PINK);
        g.setFont(new Font("Dialog", Font.BOLD, 34));
        String t = "TẠM DỪNG";
        g.drawString(t, W / 2 - g.getFontMetrics().stringWidth(t) / 2, 158);
        g.setColor(new Color(110, 116, 130));
        g.setFont(new Font("Dialog", Font.PLAIN, 14));
        String s = "Nhấn Esc để tiếp tục";
        g.drawString(s, W / 2 - g.getFontMetrics().stringWidth(s) / 2, 182);
        menuSel = Math.max(0, Math.min(3, menuSel));
        String[] ids = {"resume", "scenes", "howto", "main"};
        String[] labels = {"Tiếp tục", "Menu", "Hướng dẫn chơi game", "Về màn hình chính"};
        String[] subs = {"Quay lại chặng đang chơi", "Chọn cảnh đã mở khóa", "Phím điều khiển và mẹo chơi", "Thoát chặng này"};
        for (int i = 0; i < 4; i++) menuButton(g, ids[i], labels[i], subs[i], i, 280, 204 + i * 80, 400, 66);
    }

    // ---------- danh sách cảnh ----------
    void sceneIcon(Graphics2D g, int type, double cx, double cy, Color c) {
        g.setColor(c);
        if (type == SC_ROOM) {
            poly(g, cx - 12, cy - 1, cx, cy - 12, cx + 12, cy - 1);
            g.fill(new Rectangle2D.Double(cx - 8, cy - 1, 16, 12));
        } else if (type == SC_JAM) {
            g.fill(new RoundRectangle2D.Double(cx - 8, cy - 12, 16, 24, 8, 8));
            g.setColor(new Color(255, 255, 255, 190));
            g.fill(new Rectangle2D.Double(cx - 5, cy - 6, 10, 5));
            g.fill(new Rectangle2D.Double(cx - 5, cy + 4, 10, 4));
        } else {
            poly(g, cx - 15, cy - 4, cx, cy - 12, cx + 15, cy - 4, cx, cy + 4);
            g.fill(new RoundRectangle2D.Double(cx - 8, cy, 16, 9, 4, 4));
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Line2D.Double(cx + 13, cy - 3, cx + 13, cy + 8));
            g.setStroke(new BasicStroke(1f));
        }
    }
    void lockIcon(Graphics2D g, double cx, double cy, Color c) {
        g.setColor(c);
        g.fill(new RoundRectangle2D.Double(cx - 9, cy - 3, 18, 14, 4, 4));
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(2.6f));
        g.draw(new Arc2D.Double(cx - 6, cy - 12, 12, 16, 0, 180, Arc2D.OPEN));
        g.setStroke(old);
    }
    void sceneCard(Graphics2D g, int si, double x, double y, double w, double h) {
        Scene s = scenes.get(si);
        boolean open = sceneUnlocked(si);
        if (open) addBtn("scene:" + si, x, y, w, h, -1);
        boolean hv = open && isHover(x, y, w, h);
        g.setColor(new Color(27, 36, 53, 45));
        g.fill(new RoundRectangle2D.Double(x + 3, y + 5, w, h, 22, 22));
        g.setColor(!open ? new Color(226, 228, 236) : hv ? PINK : new Color(255, 255, 255, 245));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 22, 22));
        g.setColor(!open ? new Color(206, 210, 222) : hv ? new Color(255, 255, 255, 70) : new Color(255, 232, 238));
        g.fill(new Ellipse2D.Double(x + 10, y + 10, h - 20, h - 20));
        if (open) sceneIcon(g, s.type, x + h / 2, y + h / 2, hv ? Color.WHITE : PINK);
        else lockIcon(g, x + h / 2, y + h / 2, new Color(130, 136, 152));
        g.setFont(new Font("Dialog", Font.BOLD, 19));
        g.setColor(!open ? new Color(140, 146, 162) : hv ? Color.WHITE : INK);
        g.drawString(open ? s.name : "???", (float) (x + h + 2), (float) (y + h * 0.46));
        g.setFont(new Font("Dialog", Font.PLAIN, 13));
        g.setColor(!open ? new Color(150, 156, 172) : hv ? new Color(255, 255, 255, 230) : new Color(110, 116, 130));
        g.drawString(open ? "Nhấn để chơi" : "Chưa mở khóa", (float) (x + h + 2), (float) (y + h * 0.46 + 21));
    }
    void drawScenes(Graphics2D g) {
        drawSoftBackground(g);
        drawBackButton(g);
        g.setColor(Color.WHITE);
        g.setFont(titleFont.deriveFont(Font.BOLD, 28f));
        g.drawString("MENU - CÁC CẢNH", 84, 50);
        g.setColor(new Color(238, 237, 246));
        g.setFont(new Font("Dialog", Font.PLAIN, 15));
        g.drawString("Đã mở khóa " + unlockedCount() + " / " + scenes.size()
                + " cảnh · Chọn một cảnh để bắt đầu từ đó", 84, 74);
        for (int d = 0; d < 5; d++) {
            int ry = 92 + d * 104;
            g.setFont(new Font("Dialog", Font.BOLD, 15));
            g.setColor(PINK);
            g.drawString(dayTitles[d], 60, ry + 16);
            int k = 0;
            for (int si = 0; si < scenes.size(); si++) {
                if (scenes.get(si).dayIdx == d) {
                    sceneCard(g, si, 60 + k * 290, ry + 26, 276, 66);
                    k++;
                }
            }
        }
    }

    // ---------- hướng dẫn chơi ----------
    void keyCap(Graphics2D g, String label, double x, double y, double w, double h) {
        g.setColor(new Color(27, 36, 53, 60));
        g.fill(new RoundRectangle2D.Double(x, y + 4, w, h, 10, 10));
        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 10, 10));
        g.setColor(new Color(214, 218, 228));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(new RoundRectangle2D.Double(x, y, w, h, 10, 10));
        g.setStroke(new BasicStroke(1f));
        g.setColor(INK);
        g.setFont(new Font("Dialog", Font.BOLD, 17));
        g.drawString(label, (float) (x + w / 2 - g.getFontMetrics().stringWidth(label) / 2.0), (float) (y + h / 2 + 6));
    }
    void drawHowto(Graphics2D g) {
        drawSoftBackground(g);
        drawBackButton(g);
        g.setColor(Color.WHITE);
        g.setFont(titleFont.deriveFont(Font.BOLD, 28f));
        g.drawString("HƯỚNG DẪN CHƠI GAME", 84, 50);
        g.setColor(PINK);
        g.fillRoundRect(84, 62, 150, 4, 4, 4);
        panel(g, 50, 84, 860, 296, new Color(255, 255, 255, 245));
        g.setColor(PINK);
        g.setFont(new Font("Dialog", Font.BOLD, 17));
        g.drawString("ĐIỀU KHIỂN", 80, 118);
        String[][] rows = {
            {"← →", "A D", "Đổi làn: sang trái / sang phải"},
            {"↑ ↓", "W S", "Đi lên / lùi xuống trên đường (lúc lái trực thăng: bay lên / xuống)"},
            {"Enter", "Space", "Tiếp tục hội thoại, chuyển cảnh"},
            {"Esc", "", "Tạm dừng khi đang chơi trên đường (hoặc bấm nút II)"},
            {"Chuột", "", "Bấm vào các nút trên màn hình"}
        };
        for (int i = 0; i < rows.length; i++) {
            double ry = 134 + i * 48;
            double cx = 80;
            String[] parts = rows[i][0].split(" ");
            for (String p : parts) {
                double kw = p.length() > 2 ? 78 : 46;
                keyCap(g, p, cx, ry, kw, 36);
                cx += kw + 8;
            }
            if (!rows[i][1].isEmpty()) {
                g.setColor(new Color(150, 156, 172));
                g.setFont(new Font("Dialog", Font.BOLD, 14));
                g.drawString("hoặc", (float) cx + 2, (float) (ry + 24));
                cx += 48;
                for (String p : rows[i][1].split(" ")) {
                    double kw = p.length() > 2 ? 78 : 46;
                    keyCap(g, p, cx, ry, kw, 36);
                    cx += kw + 8;
                }
            }
            g.setColor(INK);
            g.setFont(new Font("Dialog", Font.PLAIN, 15));
            g.drawString(rows[i][2], 400, (float) (ry + 24));
        }
        panel(g, 50, 398, 860, 180, new Color(255, 255, 255, 245));
        g.setColor(MINT);
        g.setFont(new Font("Dialog", Font.BOLD, 17));
        g.drawString("MẸO CHƠI", 80, 432);
        String[] tips = {
            "• Né xe cộ trên đường — có xe chạy lấn làn, đè lên vạch kẻ. Mỗi lần va chạm mất 22 HP; HP về 0 thì chơi lại ngày đó.",
            "• Ngày 2 đường ngập: bạn chèo thuyền, né ô tô và những người chèo thuyền khác.",
            "• Ngày 4 nắng nóng: năng lượng tự giảm theo thời gian, hãy nhặt tia sét để hồi lại; hết năng lượng cũng thua.",
            "• Ngày 5: giữa đường có trực thăng đến đón, bạn điều khiển trực thăng và né đàn chim trên trời.",
            "• Tiến trình được tự động lưu; cảnh đã mở khóa có thể chơi lại trong mục Menu."
        };
        g.setColor(INK);
        g.setFont(new Font("Dialog", Font.PLAIN, 14));
        int ty = 458;
        for (String tip : tips) {
            for (String row : wrap(g.getFontMetrics(), tip, 790)) {
                g.drawString(row, 80, ty);
                ty += 20;
            }
            ty += 4;
        }
        g.setColor(MINT);
        g.setFont(new Font("Dialog", Font.BOLD, 16));
        String hint = "ENTER / SPACE  ·  BẮT ĐẦU";
        g.drawString(hint, W / 2 - g.getFontMetrics().stringWidth(hint) / 2, 626);
    }

}




