package net.pneumono.divorcesteal.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.pneumono.divorcesteal.content.DivorcestealRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyReturnValue(
            method = "canGlide",
            at = @At("RETURN")
    )
    private boolean canGlideWithGamerule(boolean original) {
        if (getWorld() instanceof ServerWorld world && world.getGameRules().getBoolean(DivorcestealRegistry.DISABLE_ELYTRA_GAMERULE)) return false;
        return original;
    }

    @WrapOperation(
            method = "tryUseDeathProtector",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/damage/DamageSource;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"
            )
    )
    private boolean canUseDeathProtectorWithGamerule(DamageSource instance, TagKey<DamageType> tag, Operation<Boolean> original) {
        if (getWorld() instanceof ServerWorld world && world.getGameRules().getBoolean(DivorcestealRegistry.DISABLE_TOTEMS_GAMERULE)) return true;
        return original.call(instance, tag);
    }
}
