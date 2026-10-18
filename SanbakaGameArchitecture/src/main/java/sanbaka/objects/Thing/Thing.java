package sanbaka;

import java.awt.Graphics2D;

/** Trạng thái dùng chung của một vật thể xuất hiện trên đường. */
abstract class Thing {
    double x, y, w, h;
    double baseX, wobbleAmp, wobbleFreq, wobblePhase;
    double fwd;
    double y0;
    double angle;
    int kind, color;
    boolean snack;
    boolean jam;
    boolean hit;

    static Thing create(int kind) {
        return switch (kind) {
            case 6 -> new RoadMotorcycle();
            case 7 -> new EnergyBolt();
            case 8 -> new RoadBoat();
            default -> new RoadCar();
        };
    }

    abstract void draw(Graphics2D g, GameVisuals scene);
}
