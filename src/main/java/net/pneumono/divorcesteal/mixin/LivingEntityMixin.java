package net.pneumono.divorcesteal.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.pneumono.divorcesteal.DivorcestealConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @ModifyReturnValue(
            method = "canGlide",
            at = @At("RETURN")
    )
    private boolean canGlideWithGamerule(boolean original) {
        if (DivorcestealConfig.DISABLE_ELYTRA.getValue()) return false;
        return original;
    }

    @WrapOperation(
            method = "checkTotemDeathProtection",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z"
            )
    )
    private boolean canUseDeathProtectorWithGamerule(DamageSource instance, TagKey<DamageType> tag, Operation<Boolean> original) {
        if (DivorcestealConfig.DISABLE_TOTEMS.getValue()) return true;
        return original.call(instance, tag);
    }
}
