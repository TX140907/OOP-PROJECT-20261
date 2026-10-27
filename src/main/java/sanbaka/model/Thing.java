package sanbaka.model;

import java.awt.Graphics2D;
import sanbaka.render.GameVisuals;

/** Trạng thái dùng chung của một vật thể xuất hiện trên đường. */
public abstract class Thing {
    public double x, y, w, h;
    public double baseX, wobbleAmp, wobbleFreq, wobblePhase;
    public double fwd;
    public double y0;
    public double angle;
    public int kind, color;
    public boolean snack;
    public boolean jam;
    public boolean hit;

    public static Thing create(int kind) {
        return switch (kind) {
            case 6 -> new RoadMotorcycle();
            case 7 -> new EnergyBolt();
            case 8 -> new RoadBoat();
            case 9 -> new HeavyTruck();
            case 10 -> new ConstructionBarrier();
            case 11 -> new Pothole();
            case 12 -> new NinjaLead();
            case 13 -> new Shark();
            case 14 -> new IcePatch();
            case 15 -> new StrayDog();
            case 16 -> new GingerTea();
            case 17 -> new ShieldItem();
            case 18 -> new Student();
            case 19 -> new MysteryBox();
            default -> new RoadCar();
        };
    }

    public abstract void draw(Graphics2D g, GameVisuals scene);
}

