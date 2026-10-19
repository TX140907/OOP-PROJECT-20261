package sanbaka;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;

abstract class GameStory extends GameMenuState {
    void startFade(int kind, long now) {
        fadeKind = kind;
        fadeStart = now;
        screen = FADE;
    }

    void enterRoomMarker() {
        Line marker = lines.get(dialogIndex++);
        String title = marker.who;
        int dayNumberPosition = title.indexOf("NGÀY ") + "NGÀY ".length();
        int dayNumber = Character.digit(title.charAt(dayNumberPosition), 10);
        day = Math.max(0, Math.min(dayTitles.length - 1, dayNumber - 1));
        unlockScene(SC_ROOM, day);
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
        int shownIndex = dialogIndex;
        dialogIndex++;
        long now = System.currentTimeMillis();
        if (dialogIndex < lines.size() && lines.get(dialogIndex).text.startsWith("Một lúc sau, tiếng cánh quạt trực thăng")) {
            cardTop = "";
            cardMain = lines.get(dialogIndex).text;
            cardNarration = true;
            cardNext = CARD_TO_AIR;
            cardStart = now;
            screen = CARD;
        }
        else if (dialogIndex < lines.size() && lines.get(dialogIndex).who.startsWith("@NGÀY ")
                && lines.get(dialogIndex).who.contains("TẠI PHÒNG TRỌ")) {
            String marker = lines.get(dialogIndex).who;
            int digitAt = marker.indexOf("NGÀY ") + "NGÀY ".length();
            cardTop = "NGÀY " + Character.digit(marker.charAt(digitAt), 10);
            cardMain = "Ở TRỌ";
            cardNarration = false;
            cardNext = CARD_TO_ROOM;
            fadeDialogIndex = shownIndex;
            startFade(FADE_ROOM, now);
        }
        else if (dialogIndex < lines.size() && lines.get(dialogIndex).who.startsWith("@Một lúc sau")) {
            cardTop = "";
            cardMain = lines.get(dialogIndex).who.substring(1);
            cardNarration = true;
            cardNext = CARD_TO_DIALOG;
            cardStart = now;
            screen = CARD;
        }
        else if (day == 4 && dialogIndex < lines.size() && lines.get(dialogIndex).text.startsWith("5 phút sau")) {
            cardTop = "";
            cardMain = lines.get(dialogIndex).text;
            cardNarration = true;
            cardNext = CARD_TO_DIALOG;
            cardStart = now;
            screen = CARD;
        }
        else if (roadPausedForTraffic && dialogIndex < lines.size() && lines.get(dialogIndex).who.startsWith("@NGÀY 5 · SAU KHI THI")) {
            dialogIndex++;
            resumeRoad();
        }
    }

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
}
