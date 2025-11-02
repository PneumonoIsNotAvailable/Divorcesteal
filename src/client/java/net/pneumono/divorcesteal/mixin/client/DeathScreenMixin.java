package net.pneumono.divorcesteal.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.pneumono.divorcesteal.gui.DeathbanScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DeathScreen.class)
public abstract class DeathScreenMixin extends Screen {
    protected DeathScreenMixin(Component title) {
        super(title);
    }

    @WrapOperation(
            method = "render",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/gui/screens/DeathScreen;title:Lnet/minecraft/network/chat/Component;"
            )
    )
    private Component useDeathbanTitle(DeathScreen instance, Operation<Component> original) {
        if ((Object)this instanceof DeathbanScreen) {
            return Component.translatable("divorcesteal.gui.deathban.title");
        }

        return original.call(instance);
    }
}
