package sanbaka;

import java.util.List;
import sanbaka.assets.ArtAssets;
import sanbaka.logic.StoryData;
import sanbaka.model.*;
import sanbaka.state.GameMenuState;

public class SanbakaSmokeTest {
    public static void main(String[] args) {
        System.out.println("Running SanbakaSmokeTest with modular packages...");

        testStoryData();
        testSceneBuilding();
        testArtAssetsFallback();
        testEntityInstantiation();

        System.out.println("[PASS] All SanbakaSmokeTest checks passed successfully!");
    }

    private static void testStoryData() {
        List<Line> lines = StoryData.build();
        if (lines == null || lines.isEmpty()) {
            throw new AssertionError("StoryData lines should not be empty!");
        }
        if (lines.size() < 50) {
            throw new AssertionError("Expected at least 50 story lines, got: " + lines.size());
        }

        boolean foundDay1 = false, foundDay5 = false;
        for (Line line : lines) {
            if (line.who.contains("NGÀY 1")) foundDay1 = true;
            if (line.who.contains("NGÀY 5")) foundDay5 = true;
        }

        if (!foundDay1 || !foundDay5) {
            throw new AssertionError("Story must contain both Day 1 and Day 5!");
        }
        System.out.println("  ✓ testStoryData passed (" + lines.size() + " lines)");
    }

    private static void testSceneBuilding() {
        GameMenuState menu = new GameMenuState() {
            @Override public void beginDay() {}
            @Override public void resumeRoad() {}
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {}
            @Override public void keyTyped(java.awt.event.KeyEvent e) {}
            @Override public void keyPressed(java.awt.event.KeyEvent e) {}
            @Override public void keyReleased(java.awt.event.KeyEvent e) {}
        };

        menu.lines.addAll(StoryData.build());
        menu.buildScenes();

        if (menu.scenes.size() != 11) {
            throw new AssertionError("Expected 11 scenes (5 room + 1 jam + 5 after), got: " + menu.scenes.size());
        }
        if (!menu.sceneUnlocked(0)) {
            throw new AssertionError("Scene 0 should be unlocked by default or under UNLOCK_ALL!");
        }
        System.out.println("  ✓ testSceneBuilding passed (" + menu.scenes.size() + " scenes)");
    }

    private static void testArtAssetsFallback() {
        java.awt.image.BufferedImage img = ArtAssets.load("NonExistent", "no_image.png");
        if (img != null) {
            throw new AssertionError("Expected null for non-existent image!");
        }
        System.out.println("  ✓ testArtAssetsFallback passed");
    }

    private static void testEntityInstantiation() {
        PlayerVehicle bike = new PlayerBike();
        if (bike.x <= 0 || bike.y <= 0) {
            throw new AssertionError("PlayerBike default position is invalid");
        }

        PlayerVehicle boat = new PlayerBoat();
        if (boat.x <= 0 || boat.y <= 0) {
            throw new AssertionError("PlayerBoat default position is invalid");
        }

        RoadCar car = new RoadCar();
        RoadMotorcycle moto = new RoadMotorcycle();
        RoadBoat roadBoat = new RoadBoat();
        EnergyBolt bolt = new EnergyBolt();
        SchoolBuilding school = new SchoolBuilding();
        
        Thing thingCar = Thing.create(0);
        Thing thingMoto = Thing.create(6);
        Thing thingBolt = Thing.create(7);
        Thing thingBoat = Thing.create(8);

        if (car == null || moto == null || roadBoat == null || bolt == null || school == null) {
            throw new AssertionError("Entity instantiation failed");
        }
        if (thingCar == null || thingMoto == null || thingBolt == null || thingBoat == null) {
            throw new AssertionError("Thing factory creation failed");
        }
        System.out.println("  ✓ testEntityInstantiation passed");
    }
}
