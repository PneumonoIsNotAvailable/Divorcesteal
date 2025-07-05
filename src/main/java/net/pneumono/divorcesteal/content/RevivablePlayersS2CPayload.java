package net.pneumono.divorcesteal.content;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.pneumono.divorcesteal.Divorcesteal;
import net.pneumono.divorcesteal.hearts.PlayerHeartData;

import java.util.List;

public record RevivablePlayersS2CPayload(List<PlayerHeartData> players) implements CustomPayload {
    public static final CustomPayload.Id<RevivablePlayersS2CPayload> PAYLOAD_ID = new Id<>(Divorcesteal.id("revivable_players"));
    public static final PacketCodec<RegistryByteBuf, RevivablePlayersS2CPayload> PACKET_CODEC = PacketCodec.tuple(
            PlayerHeartData.PACKET_CODEC.collect(PacketCodecs.toList()),
            RevivablePlayersS2CPayload::players,
            RevivablePlayersS2CPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return PAYLOAD_ID;
    }
}
