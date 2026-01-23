package net.pneumono.divorcesteal.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.pneumono.divorcesteal.gui.DeathbanScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin extends ClientCommonPacketListenerImpl {
    protected ClientPacketListenerMixin(Minecraft client, Connection connection, CommonListenerCookie connectionState) {
        super(client, connection, connectionState);
    }

    @WrapOperation(
            method = "handlePlayerCombatKill",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/network/chat/Component;ZLnet/minecraft/client/player/LocalPlayer;)Lnet/minecraft/client/gui/screens/DeathScreen;"
            )
    )
    private DeathScreen setDeathbanScreen(Component message, boolean isHardcore, LocalPlayer player, Operation<DeathScreen> original) {
        return DeathbanScreen.showShow(player) ? new DeathbanScreen(message, isHardcore, player) : original.call(message, isHardcore, player);
    }
}
