package net.pneumono.divorcesteal.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.pneumono.divorcesteal.content.MaceRebalance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MaceItem.class)
public abstract class MaceItemMixin {
    // Ugly but whatever this server isn't gonna use other mace rebalance stuff if mace rebalance is already part of the mod
    @Inject(
            method = "getAttackDamageBonus",
            at = @At("HEAD"),
            cancellable = true
    )
    private void modifyAttackDamageBonus(Entity victim, float ignoredDamage, DamageSource damageSource, CallbackInfoReturnable<Float> cir) {
        if (damageSource.getDirectEntity() instanceof LivingEntity attacker) {
            if (!MaceItem.canSmashAttack(attacker)) {
                return;
            }

            double damage = MaceRebalance.calculateMaceBonusDamage(victim, attacker, damageSource);

            double fallDistance = attacker.fallDistance;
            cir.setReturnValue(attacker.level() instanceof ServerLevel level
                    ? (float)(damage + EnchantmentHelper.modifyFallBasedDamage(level, attacker.getWeaponItem(), victim, damageSource, 0.0F) * fallDistance)
                    : (float)damage
            );
        }
    }
}
