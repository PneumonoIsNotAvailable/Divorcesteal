package net.pneumono.divorcesteal.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.text.Text;
import net.pneumono.divorcesteal.gui.DeathbanScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin extends ClientCommonNetworkHandler {
    protected ClientPlayNetworkHandlerMixin(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
        super(client, connection, connectionState);
    }

    @WrapOperation(
            method = "onDeathMessage",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/text/Text;Z)Lnet/minecraft/client/gui/screen/DeathScreen;"
            )
    )
    private DeathScreen setDeathbanScreen(Text message, boolean isHardcore, Operation<DeathScreen> original) {
        PlayerEntity player = Objects.requireNonNull(client.player);
        return DeathbanScreen.showShow(player) ? new DeathbanScreen(message, isHardcore) : original.call(message, isHardcore);
    }
}
