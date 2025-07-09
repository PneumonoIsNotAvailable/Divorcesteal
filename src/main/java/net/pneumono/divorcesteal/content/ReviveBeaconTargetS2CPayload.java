package net.pneumono.divorcesteal.content;

import net.minecraft.component.type.ProfileComponent;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.pneumono.divorcesteal.Divorcesteal;

public record ReviveBeaconTargetS2CPayload(int syncId, ProfileComponent profileComponent) implements CustomPayload {
    public static final CustomPayload.Id<ReviveBeaconTargetS2CPayload> ID = new Id<>(Divorcesteal.id("revive_beacon_target"));
    public static final PacketCodec<RegistryByteBuf, ReviveBeaconTargetS2CPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT,
            ReviveBeaconTargetS2CPayload::syncId,
            ProfileComponent.PACKET_CODEC,
            ReviveBeaconTargetS2CPayload::profileComponent,
            ReviveBeaconTargetS2CPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
