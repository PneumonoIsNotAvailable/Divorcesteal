package net.pneumono.divorcesteal.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.pneumono.divorcesteal.DivorcestealConfig;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Slot.class)
public abstract class SlotMixin {
    @Shadow
    @Final
    public Container container;

    // For some reason if I add local parameters it breaks
    @SuppressWarnings({"LocalMayBeArgsOnly", "LocalMayUseName"})
    @ModifyReturnValue(
            method = "mayPlace",
            at = @At("RETURN")
    )
    private boolean preventPlacingHearts(boolean original, @Local ItemStack itemStack) {
        if (!DivorcestealConfig.ALLOW_HEART_ECHESTING.getValue()) {
            if (itemStack.is(DivorcestealRegistry.HEART_ITEM) && this.container instanceof PlayerEnderChestContainer) {
                return false;
            }
        }
        return original;
    }
}
