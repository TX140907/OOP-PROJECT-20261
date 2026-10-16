package game.logic.collision;

import game.level.BaseLevel;
import game.config.ConfigValues;
import game.logic.effect.EffectManager.Type;

/** Composes an obstacle with its collision response, independent of its movement. */
public record ConfiguredCollision(CollisionKind kind) implements CollisionEffect {
    @Override public void apply(BaseLevel level) {
        ConfigValues c=level.config().values();
        switch (kind) {
            case LETHAL -> level.player().kill();
            case NORMAL_DAMAGE -> damage(level,c.integer("collision.normalDamage"));
            case PLANE_DAMAGE -> damage(level,c.integer("enemyPlane.damage"));
            case BOSS_DAMAGE -> damage(level,c.integer("boss.projectile.damage"));
            case SLOW -> {
                damage(level,c.integer("drain.damage"));
                level.effects().add(Type.DRAIN,c.number("drain.durationSeconds"));
            }
            case SHARK_SLOW -> {
                damage(level,(int)c.number("shark.damage",1));
                level.effects().add(Type.SHARK_SLOW,c.number("shark.slowDurationSeconds",1.2));
            }
            case SLIDE -> level.effects().add(Type.SLIDE,c.number("ice.durationSeconds"));
            case HEAL -> level.player().heal(c.integer("healthPickup.amount"));
            case SHIELD -> level.player().grantShield(c.number("boss.shield.durationSeconds"));
            case FREEZE -> {
                level.effects().add(Type.FREEZE,c.number("snowball.freezeDuration"));
                level.effects().add(Type.SNOWBALL_SLOW,c.number("snowball.freezeDuration")+c.number("snowball.slowDurationSeconds"));
                level.player().stop();
            }
            case ENERGY -> {
                level.energy().recover(c.number("energy.recovery"));
                if (level.energy().enabled()) {
                    level.effects().add(Type.ENERGY_BOOST,c.number("energy.pickupBoostDurationSeconds"));
                    level.player().heal((int)c.number("energy.pickupHealAmount",0));
                }
            }
        }
    }
    private static void damage(BaseLevel level,int amount) {
        if (level.player().hurt(amount)) {
            level.effects().add(Type.HIT,level.config().values().number("player.hitSlowDurationSeconds"));
            level.progress().hit(level.config().values().number("progress.hitPenaltyDurationSeconds"));
        }
    }
}
