package net.pneumono.divorcesteal.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.pneumono.divorcesteal.hearts.PlayerHeartDataReference;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow public abstract ServerPlayerEntity getPlayer();

    @Inject(
            method = "onClientStatus",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/packet/c2s/play/ClientStatusC2SPacket;getMode()Lnet/minecraft/network/packet/c2s/play/ClientStatusC2SPacket$Mode;",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void disconnectBannedPlayer(ClientStatusC2SPacket packet, CallbackInfo ci) {
        ClientStatusC2SPacket.Mode mode = packet.getMode();
        if (mode != ClientStatusC2SPacket.Mode.PERFORM_RESPAWN) return;

        ServerPlayerEntity player = getPlayer();
        if (PlayerHeartDataReference.create(player).isBanned()) {
            player.networkHandler.disconnect(Text.translatable("divorcesteal.deathban"));
            ci.cancel();
        }
    }
}
