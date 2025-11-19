package net.pneumono.divorcesteal.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.pneumono.divorcesteal.hearts.Hearts;
import net.pneumono.divorcesteal.hearts.Participant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow public abstract ServerPlayer getPlayer();

    @Inject(
            method = "handleClientCommand",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/game/ServerboundClientCommandPacket;getAction()Lnet/minecraft/network/protocol/game/ServerboundClientCommandPacket$Action;",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void disconnectBannedPlayer(ServerboundClientCommandPacket packet, CallbackInfo ci) {
        ServerboundClientCommandPacket.Action mode = packet.getAction();
        if (mode != ServerboundClientCommandPacket.Action.PERFORM_RESPAWN) return;
        ServerPlayer player = getPlayer();
        Participant data = Hearts.getParticipantHeartData(player);
        if (data != null && data.isBanned()) {
            player.connection.disconnect(Component.translatable("divorcesteal.deathban"));
            ci.cancel();
        }
    }
}
