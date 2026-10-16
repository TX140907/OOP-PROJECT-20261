package game.model.obstacle;

public enum ObstacleType {
    VEHICLE("vehicle"), BICYCLE("bicycle"), BARRIER("barrier"), SHARK("shark"),
    DRAIN("drain"), FLOATING_CRATE("floatingCrate"), ICE_PATCH("icePatch"), ICE_HOLE("iceHole"), TUMBLEWEED("tumbleweed"),
    SNOWBALL("snowball"), SNOWMAN("snowman"), BUS("bus"), CACTUS("cactus"),
    TRAFFIC_JAM_VEHICLE("trafficJamVehicle"), DOG("dog"), BIRD("bird"), ENERGY_PICKUP("energyPickup"), HEALTH_PICKUP("healthPickup"), ENEMY_PLANE("enemyPlane"), MISSILE("missile"),
    BOSS_PROJECTILE("bossProjectile"), SHIELD_PICKUP("shieldPickup");
    private final String key;
    ObstacleType(String key) { this.key = key; }
    public String key() { return key; }
    public static ObstacleType from(String key) {
        for (ObstacleType type : values()) if (type.key.equals(key)) return type;
        throw new IllegalArgumentException("Unknown obstacle: " + key);
    }
}
