package sanbaka.render;

import sanbaka.model.*;
import sanbaka.state.GameState;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;

public class GameScreenRenderer extends GameMenuRenderer {
    private static final boolean IS_WINDOWS = System.getProperty("os.name", "").toLowerCase().contains("win");

    @Override
    void drawPauseBackground(Graphics2D g) {
        if (pausedScreen == DIALOG) drawDialog(g);
        else if (pausedScreen == ROOM) {
            drawRoom(g);
            drawCurrentDialog(g);
        } else drawRoad(g);
    }
    void drawFade(Graphics2D g) {
        if (fadeKind == FADE_ROOM) {
            int saved = dialogIndex;
            dialogIndex = fadeDialogIndex;
            drawDialog(g);
            dialogIndex = saved;
        } else {
            drawRoad(g);
        }
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

    void drawCallPhone(Graphics2D g) {
        long now = System.currentTimeMillis();
        if (callStartedAt == 0) callStartedAt = now;
        Line cur = lines.get(dialogIndex);
        boolean speaker = cur.who.contains("(loa ngoài)");
        boolean contactSpeaking = cur.who.startsWith("Komori");
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
        g.drawString("Komori", pw / 2 - g.getFontMetrics().stringWidth("Komori") / 2, 118);
        g.setFont(new Font("Dialog", Font.PLAIN, 18));
        g.setColor(new Color(255, 255, 255, 185));
        long sec = Math.max(0, (now - callStartedAt) / 1000);
        String status = ringing ? "đang gọi…" : String.format("%02d:%02d", sec / 60, sec % 60);
        g.drawString(status, pw / 2 - g.getFontMetrics().stringWidth(status) / 2, 146);
        // ảnh đại diện (sóng âm khi Komori đang nói)
        if (contactSpeaking) {
            g.setStroke(new BasicStroke(3f));
            for (int k = 0; k < 2; k++) {
                double ph2 = ((now / 800.0) + k * 0.5) % 1.0;
                double rr = 64 + ph2 * 26;
                g.setColor(new Color(255, 255, 255, (int) (110 * (1 - ph2))));
                g.draw(new Ellipse2D.Double(pw / 2.0 - rr, 238 - rr, 2 * rr, 2 * rr));
            }
        }
        drawAvatar(g, "Komori", pw / 2, 238, 58);
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

    void drawIntro(Graphics2D g) {
        drawSoftBackground(g);
        g.setColor(Color.WHITE);
        g.setFont(titleFont.deriveFont(Font.BOLD, 30f));
        g.drawString("GIỚI THIỆU NHÂN VẬT", 84, 50);
        g.setColor(PINK);
        g.fillRoundRect(84, 62, 150, 4, 4, 4);
        String[] names = {"Sanbaka", "Thắng", "Komori", "Chủ trọ"};
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
        String retryText = happy ? "ENTER / SPACE · CHƠI LẠI"
                : (day == 4 && flightMode) ? "ENTER / SPACE · CHƠI LẠI LƯỢT 2 (NGÀY 5)"
                : "ENTER / SPACE · CHƠI LẠI NGÀY " + (day + 1);
        g.drawString(retryText, 145, 455);
        if (happy) drawConfetti(g, now);
    }

    void drawMenuPageForeground(Graphics2D g, int page) {
        if (page == TITLE) drawTitle(g);
        else if (page == SCENES) drawScenes(g);
        else if (page == INTRO) {
            drawIntro(g);
            drawBackButton(g);
        }
        else if (page == HOWTO) drawHowto(g);
    }

    void drawMenuTransition(Graphics2D g) {
        double progress = Math.max(0, Math.min(1,
                (System.currentTimeMillis() - menuTransitionStart) / (double) MENU_TRANSITION_MS));
        double slide = progress * progress * (3 - 2 * progress);

        // Nền đứng yên và hòa dần tới đúng nền màn đích để không nháy ở khung cuối.
        Graphics2D targetBackground = (Graphics2D) g.create();
        if (menuTransitionFrom == TITLE) {
            drawTitleBackground(g);
            targetBackground.setComposite(AlphaComposite.SrcOver.derive((float) slide));
            drawSoftBackground(targetBackground);
        } else {
            drawSoftBackground(g);
            targetBackground.setComposite(AlphaComposite.SrcOver.derive((float) slide));
            drawTitleBackground(targetBackground);
        }
        targetBackground.dispose();

        menuForegroundOnly = true;
        Graphics2D outgoing = (Graphics2D) g.create();
        outgoing.translate(menuTransitionTo == TITLE ? slide * W : -slide * W, 0);
        drawMenuPageForeground(outgoing, menuTransitionFrom);
        outgoing.dispose();

        Graphics2D incoming = (Graphics2D) g.create();
        incoming.translate(menuTransitionTo == TITLE ? -(1 - slide) * W : (1 - slide) * W, 0);
        drawMenuPageForeground(incoming, menuTransitionTo);
        incoming.dispose();
        menuForegroundOnly = false;

        if (progress >= 1) {
            screen = menuTransitionTo;
            menuTransitionFrom = -1;
            menuTransitionTo = -1;
        }
    }

    protected void paintComponent(Graphics gg) {
        countFrame();
        long perfStart = PERF_LOG_ENABLED ? System.nanoTime() : 0L;
        super.paintComponent(gg);
        Graphics2D g=(Graphics2D)gg;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        btns.clear();
        g.setColor(CREAM);
        g.fillRect(0,0,W,H);
        if (menuTransitionTo >= 0) drawMenuTransition(g);
        else if (screen == TITLE) drawTitle(g);
        else if (screen == ROAD) {
            drawRoad(g);
            drawPauseButton(g);
        }
        else if (screen == INTRO) { drawIntro(g); drawBackButton(g); }
        else if (screen == SCENES) drawScenes(g);
        else if (screen == HOWTO) drawHowto(g);
        else if (screen == PAUSE) drawPause(g);
        else if (screen == FADE) drawFade(g);
        else if (screen == CARD) drawCard(g);
        else if (screen == ROOM) {
            drawRoom(g);
            drawCurrentDialog(g);
            drawPauseButton(g, W - 58);
        }
        else if (screen == DIALOG) {
            drawDialog(g);
            drawPauseButton(g, W - 58);
        }
        else if (screen == EXAM) drawExam(g);
        else if (screen == ENDING_HE) drawEnding(g, true);
        else if (screen == ENDING_BE) drawEnding(g, false);
        else if (screen == AIR) drawAir(g);
        else if (screen == MARIO) {
            if (marioGame != null) marioGame.render(g);
        }
        else drawDialog(g);
        long fadeIn = System.currentTimeMillis() - fadeInStart;
        if (fadeIn >= 0 && fadeIn < 700) {
            g.setColor(new Color(0, 0, 0, (int) (255 * (1 - fadeIn / 700.0))));
            g.fillRect(0, 0, W, H);
        }
        if (!IS_WINDOWS) {
            Toolkit.getDefaultToolkit().sync();
        }
        if (PERF_LOG_ENABLED) recordPerformance(true, System.nanoTime() - perfStart);
    }

    boolean roomAwake() {
        return dialogIndex < lines.size() && lines.get(dialogIndex).who.startsWith("Thắng");
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

        // giường trong phòng trọ
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
        int afterExamDay = afterExamDay(dialogIndex);
        java.awt.image.BufferedImage background = switch (afterExamDay) {
            case 2 -> afterExamRainBackground;
            case 3 -> afterExamSnowBackground;
            case 4 -> afterExamSunBackground;
            case 1, 5 -> afterExamBackground;
            default -> dialogBackground;
        };
        if (background == null) background = dialogBackground;
        if (background != null) {
            g.drawImage(background, 0, 0, W, H, null);
        }
        if (dialogIndex >= lines.size()) {
            screen = ENDING_HE;
            drawEnding(g, true);
            return;
        }
        Line currentLine = lines.get(dialogIndex);
        if (!isMessage(currentLine)) {
            String expressionName = sanbakaExpressionForDialog(dialogIndex);
            java.awt.image.BufferedImage expression = expressionName == null
                    ? null : sanbakaExpressionImages.get(expressionName);
            if (expression != null) {
                int height = (int) Math.round(H * 1.25);
                int width = (int) Math.round(expression.getWidth() * (height / (double) expression.getHeight()));
                int x = W - width;
                int y = -(int) Math.round(height * 0.08);
                g.drawImage(expression, x, y, width, height, null);
            }
        }
        g.setColor(INK);
        g.setFont(new Font("Dialog",Font.BOLD,18));
        drawCurrentDialog(g);
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
            java.util.List<String> rows = i == dialogIndex
                    ? wrap(fm, visibleDialogText(lines.get(i).text), maxText)
                    : wrapped.get(i - start);
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
        drawHelicopter(g, hx, hy, 0.05, now / 1000.0, 1.25, false);
        panel(g, 250, 36, 460, 88, new Color(255, 255, 255, 228));
        g.setColor(INK);
        g.setFont(new Font("Dialog", Font.BOLD, 28));
        String a = "LÊN TRỰC THĂNG NÀO BÉ YÊU!";
        g.drawString(a, W / 2 - g.getFontMetrics().stringWidth(a) / 2, 80);
        g.setFont(new Font("Dialog", Font.PLAIN, 17));
        String b = "Helikopter helikopter ~";
        g.drawString(b, W / 2 - g.getFontMetrics().stringWidth(b) / 2, 108);
    }

    void drawDialogueBox(Graphics2D g, Line line) {
        // Giữ mép dưới ở y=596, nâng mép trên để khung gọn hơn.
        panel(g, 48, 438, 864, 158, new Color(255, 255, 255, 190));
        drawAvatar(g, line.who, 122, 518, 40);

        g.setColor(PINK);
        g.setFont(new Font("Dialog", Font.BOLD, 20));
        g.drawString(line.who, 184, 474);

        g.setColor(INK);
        g.setFont(new Font("Dialog", Font.PLAIN, 20));
        drawWrapped(g, visibleDialogText(line.text), 184, 510, 690, 27);

        g.setColor(MINT);
        g.setFont(new Font("Dialog", Font.BOLD, 15));
        String hint = "ENTER / SPACE  ·  TIẾP TỤC";
        g.drawString(hint, 888 - g.getFontMetrics().stringWidth(hint), 574);
    }
}









