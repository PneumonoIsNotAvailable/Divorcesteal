package net.pneumono.divorcesteal.content;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class MaceRebalance {
    public static double calculateMaceBonusDamage(Entity victim, LivingEntity attacker, DamageSource source) {
        double fallHeightThreshold1 = 3.0;
        double fallHeightThreshold2 = 8.0;
        double fallDistance = attacker.fallDistance;
        double damage;
        if (fallDistance <= 3.0) {
            damage = 4.0 * fallDistance;
        } else if (fallDistance <= 8.0) {
            damage = 12.0 + 2.0 * (fallDistance - 3.0);
        } else {
            damage = 22.0 + fallDistance - 8.0;
        }

        return Math.min(damage, 28);
    }

    private static double vanilla(Entity victim, LivingEntity attacker, DamageSource source) {
        double fallHeightThreshold1 = 3.0;
        double fallHeightThreshold2 = 8.0;
        double fallDistance = attacker.fallDistance;
        double damage;
        if (fallDistance <= 3.0) {
            damage = 4.0 * fallDistance;
        } else if (fallDistance <= 8.0) {
            damage = 12.0 + 2.0 * (fallDistance - 3.0);
        } else {
            damage = 22.0 + fallDistance - 8.0;
        }

        return damage;
    }
}
