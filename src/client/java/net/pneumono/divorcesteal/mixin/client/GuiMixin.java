package net.pneumono.divorcesteal.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.pneumono.divorcesteal.gui.DeathbanScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @WrapOperation(
            method = "setScreen",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/network/chat/Component;ZLnet/minecraft/client/player/LocalPlayer;)Lnet/minecraft/client/gui/screens/DeathScreen;"
            )
    )
    private DeathScreen setDeathbanScreen(Component causeOfDeath, boolean hardcore, LocalPlayer player, Operation<DeathScreen> original) {
        return DeathbanScreen.showShow(player) ? new DeathbanScreen(causeOfDeath, hardcore, player) : original.call(causeOfDeath, hardcore, player);
    }
}
