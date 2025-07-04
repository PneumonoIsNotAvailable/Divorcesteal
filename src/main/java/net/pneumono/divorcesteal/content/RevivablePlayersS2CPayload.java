package net.pneumono.divorcesteal.content;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.pneumono.divorcesteal.Divorcesteal;

import java.util.List;

public record RevivablePlayersS2CPayload(List<GameProfile> players) implements CustomPayload {
    public static final CustomPayload.Id<RevivablePlayersS2CPayload> PAYLOAD_ID = new Id<>(Divorcesteal.id("revivable_players"));
    public static final PacketCodec<RegistryByteBuf, RevivablePlayersS2CPayload> PACKET_CODEC = PacketCodec.tuple(
            ReviveBeaconItem.PLAYER_PACKET_CODEC.collect(PacketCodecs.toList()),
            RevivablePlayersS2CPayload::players,
            RevivablePlayersS2CPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return PAYLOAD_ID;
    }
}
