package net.pneumono.divorcesteal.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.pneumono.divorcesteal.DivorcestealConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow public abstract ItemStack getItemInHand(InteractionHand interactionHand);

    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @SuppressWarnings("ConstantValue")
    @ModifyReturnValue(
            method = "canGlide",
            at = @At("RETURN")
    )
    private boolean canGlideWithConfig(boolean original) {
        if (((Entity)this) instanceof Player && DivorcestealConfig.DISABLE_ELYTRA.getValue()) return false;
        return original;
    }

    @SuppressWarnings("ConstantValue")
    @Inject(
            method = "checkTotemDeathProtection",
            at = @At("HEAD"),
            cancellable = true
    )
    private void canUseDeathProtectorWithConfig(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if (!(((Object)this) instanceof Player && DivorcestealConfig.DISABLE_TOTEMS.getValue())) {
            return;
        }

        ItemStack itemStack = null;

        for (InteractionHand interactionHand : InteractionHand.values()) {
            ItemStack itemStack2 = this.getItemInHand(interactionHand);
            if (itemStack2.get(DataComponents.DEATH_PROTECTION) != null) {
                itemStack = itemStack2.copy();
                itemStack2.shrink(1);
                break;
            }
        }

        if (itemStack != null) {
            if (((Object)this) instanceof ServerPlayer serverPlayer) {
                serverPlayer.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
                CriteriaTriggers.USED_TOTEM.trigger(serverPlayer, itemStack);
                this.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
            }

            this.level().broadcastEntityEvent(this, (byte)35);
        }

        cir.setReturnValue(false);
    }
}
