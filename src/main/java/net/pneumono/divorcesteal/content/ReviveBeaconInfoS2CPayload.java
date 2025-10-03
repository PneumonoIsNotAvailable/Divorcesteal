package net.pneumono.divorcesteal.content;

import net.minecraft.component.type.ProfileComponent;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.pneumono.divorcesteal.Divorcesteal;

import java.util.List;

public record ReviveBeaconInfoS2CPayload(int syncId, ProfileComponent target, List<ProfileComponent> revivableParticipants) implements CustomPayload {
    public static final CustomPayload.Id<ReviveBeaconInfoS2CPayload> ID = new Id<>(Divorcesteal.id("revive_beacon_info"));
    public static final PacketCodec<RegistryByteBuf, ReviveBeaconInfoS2CPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT,
            ReviveBeaconInfoS2CPayload::syncId,
            ProfileComponent.PACKET_CODEC,
            ReviveBeaconInfoS2CPayload::target,
            ProfileComponent.PACKET_CODEC.collect(PacketCodecs.toList()),
            ReviveBeaconInfoS2CPayload::revivableParticipants,
            ReviveBeaconInfoS2CPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
