package sanbaka.state;

import java.io.*;
import java.util.Properties;

/**
 * Trình nạp và quản lý toàn bộ thông số cân bằng của Màn đánh Boss trực thăng (Màn 5).
 * Nạp từ tệp boss_config.properties hoặc classpath, tự động fallback an toàn nếu tệp không tồn tại.
 */
public class BossConfig {
    public static int maxHp = 20;
    public static long warningFlashMs = 1800;
    public static long introChargeMs = 2500;
    public static double dashSpeed = 340.0;
    public static double minY = 110.0;
    public static double maxY = 490.0;

    public static double missileCooldownSec = 0.30;
    public static double missileSpeed = 680.0;
    public static int missileDamage = 1;

    public static long attack1DurationMs = 8500;
    public static long attack1PlaneIntervalMs = 1150;
    public static double attack1PlaneShootChance = 0.50;
    public static double enemyPlaneSpeed = 350.0;
    public static double enemyPlaneWidth = 86.0;
    public static double enemyPlaneHeight = 44.0;
    public static double enemyPlaneBulletSpeed = 420.0;
    public static long attack1AimShootIntervalMs = 1900;
    public static long attack1LaserWarningMs = 450;
    public static double bossAimBulletSpeed = 460.0;

    public static long transitionSprayDurationMs = 2200;
    public static double retreatSpeed = 750.0;

    public static long attack2WarningMs = 1200;
    public static long attack2DurationMs = 6800;
    public static long attack2BirdIntervalMs = 220;
    public static double attack2BirdSpeed = 470.0;

    public static long attack3IntroSprayMs = 1400;
    public static long attack3DurationMs = 8800;
    public static long attack3SquadronIntervalMs = 750;
    public static long attack3BombIntervalMs = 780;
    public static double enemyBombVx = -165.0;
    public static double enemyBombVy = 240.0;
    public static double enemyBombBlastRadius = 76.0;

    public static int enemyPlaneHp = 3;
    public static int enemyBirdHp = 1;
    public static int enemyBombHp = 1;

    public static int damageBossBullet = 13;
    public static int damagePlaneBullet = 11;
    public static int damagePlaneCollision = 19;
    public static int damageBombBlast = 17;
    public static int damageBirdCollision = 18;

    private static boolean loaded = false;

    public static synchronized void load() {
        Properties prop = new Properties();
        InputStream is = null;
        try {
            File localFile = new File("boss_config.properties");
            if (localFile.exists()) {
                is = new FileInputStream(localFile);
            } else {
                File subFile = new File("OOP-PROJECT-20261/boss_config.properties");
                if (subFile.exists()) {
                    is = new FileInputStream(subFile);
                } else {
                    is = BossConfig.class.getResourceAsStream("/boss_config.properties");
                }
            }
            if (is != null) {
                prop.load(new InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8));
                maxHp = getInt(prop, "boss.max_hp", maxHp);
                warningFlashMs = getLong(prop, "boss.warning_flash_ms", warningFlashMs);
                introChargeMs = getLong(prop, "boss.intro_charge_ms", introChargeMs);
                dashSpeed = getDouble(prop, "boss.dash_speed", dashSpeed);
                minY = getDouble(prop, "boss.min_y", minY);
                maxY = getDouble(prop, "boss.max_y", maxY);

                missileCooldownSec = getDouble(prop, "player.missile_cooldown_sec", missileCooldownSec);
                missileSpeed = getDouble(prop, "player.missile_speed", missileSpeed);
                missileDamage = getInt(prop, "player.missile_damage", missileDamage);

                attack1DurationMs = getLong(prop, "boss.attack1_duration_ms", attack1DurationMs);
                attack1PlaneIntervalMs = getLong(prop, "boss.attack1_plane_interval_ms", attack1PlaneIntervalMs);
                attack1PlaneShootChance = getDouble(prop, "boss.attack1_plane_shoot_chance", attack1PlaneShootChance);
                enemyPlaneSpeed = getDouble(prop, "enemy.plane_speed", enemyPlaneSpeed);
                enemyPlaneWidth = getDouble(prop, "enemy.plane_width", enemyPlaneWidth);
                enemyPlaneHeight = getDouble(prop, "enemy.plane_height", enemyPlaneHeight);
                enemyPlaneBulletSpeed = getDouble(prop, "enemy.plane_bullet_speed", enemyPlaneBulletSpeed);
                attack1AimShootIntervalMs = getLong(prop, "boss.attack1_aim_shoot_interval_ms", attack1AimShootIntervalMs);
                attack1LaserWarningMs = getLong(prop, "boss.attack1_laser_warning_ms", attack1LaserWarningMs);
                bossAimBulletSpeed = getDouble(prop, "boss.aim_bullet_speed", bossAimBulletSpeed);

                transitionSprayDurationMs = getLong(prop, "boss.transition_spray_duration_ms", transitionSprayDurationMs);
                retreatSpeed = getDouble(prop, "boss.retreat_speed", retreatSpeed);

                attack2WarningMs = getLong(prop, "boss.attack2_warning_ms", attack2WarningMs);
                attack2DurationMs = getLong(prop, "boss.attack2_duration_ms", attack2DurationMs);
                attack2BirdIntervalMs = getLong(prop, "boss.attack2_bird_interval_ms", attack2BirdIntervalMs);
                attack2BirdSpeed = getDouble(prop, "boss.attack2_bird_speed", attack2BirdSpeed);

                attack3IntroSprayMs = getLong(prop, "boss.attack3_intro_spray_ms", attack3IntroSprayMs);
                attack3DurationMs = getLong(prop, "boss.attack3_duration_ms", attack3DurationMs);
                attack3SquadronIntervalMs = getLong(prop, "boss.attack3_squadron_interval_ms", attack3SquadronIntervalMs);
                attack3BombIntervalMs = getLong(prop, "boss.attack3_bomb_interval_ms", attack3BombIntervalMs);
                enemyBombVx = getDouble(prop, "enemy.bomb_vx", enemyBombVx);
                enemyBombVy = getDouble(prop, "enemy.bomb_vy", enemyBombVy);
                enemyBombBlastRadius = getDouble(prop, "enemy.bomb_blast_radius", enemyBombBlastRadius);

                enemyPlaneHp = getInt(prop, "enemy.plane_hp", enemyPlaneHp);
                enemyBirdHp = getInt(prop, "enemy.bird_hp", enemyBirdHp);
                enemyBombHp = getInt(prop, "enemy.bomb_hp", enemyBombHp);

                damageBossBullet = getInt(prop, "damage.boss_bullet", damageBossBullet);
                damagePlaneBullet = getInt(prop, "damage.plane_bullet", damagePlaneBullet);
                damagePlaneCollision = getInt(prop, "damage.plane_collision", damagePlaneCollision);
                damageBombBlast = getInt(prop, "damage.bomb_blast", damageBombBlast);
                damageBirdCollision = getInt(prop, "damage.bird_collision", damageBirdCollision);
                loaded = true;
            }
        } catch (Exception e) {
            System.err.println("[BossConfig] Using defaults: " + e.getMessage());
        } finally {
            if (is != null) {
                try { is.close(); } catch (IOException ignored) {}
            }
        }
    }

    private static int getInt(Properties p, String key, int def) {
        String val = p.getProperty(key);
        if (val == null || val.trim().isEmpty()) return def;
        try { return Integer.parseInt(val.trim()); } catch (Exception e) { return def; }
    }

    private static long getLong(Properties p, String key, long def) {
        String val = p.getProperty(key);
        if (val == null || val.trim().isEmpty()) return def;
        try { return Long.parseLong(val.trim()); } catch (Exception e) { return def; }
    }

    private static double getDouble(Properties p, String key, double def) {
        String val = p.getProperty(key);
        if (val == null || val.trim().isEmpty()) return def;
        try { return Double.parseDouble(val.trim()); } catch (Exception e) { return def; }
    }
}

