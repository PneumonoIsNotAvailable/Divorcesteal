package net.pneumono.divorcesteal.content;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.pneumono.divorcesteal.Divorcesteal;

public record RevivePlayerC2SPayload(GameProfile player) implements CustomPayload {
    public static final CustomPayload.Id<RevivePlayerC2SPayload> PAYLOAD_ID = new Id<>(Divorcesteal.id("revive_player"));
    public static final PacketCodec<RegistryByteBuf, RevivePlayerC2SPayload> PACKET_CODEC = PacketCodec.tuple(
            ReviveBeaconItem.PLAYER_PACKET_CODEC,
            RevivePlayerC2SPayload::player,
            RevivePlayerC2SPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return PAYLOAD_ID;
    }
}
