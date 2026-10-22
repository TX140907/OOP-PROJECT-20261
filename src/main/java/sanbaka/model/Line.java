package sanbaka.model;

/** Đại diện cho một câu thoại trong kịch bản. */
public class Line {
    public String who;
    public String text;

    public Line(String w, String t) {
        this.who = w;
        this.text = t;
    }
}

