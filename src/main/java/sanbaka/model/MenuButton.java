package sanbaka.model;

/** Nút bấm tương tác trên giao diện Menu. */
public class MenuButton {
    public String id;
    public double x, y, w, h;
    public int menuIndex;

    public MenuButton() {}

    public MenuButton(String id, double x, double y, double w, double h, int menuIndex) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.menuIndex = menuIndex;
    }
}

