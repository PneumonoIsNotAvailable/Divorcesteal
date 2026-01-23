package net.pneumono.divorcesteal.content;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.players.NameAndId;
import net.pneumono.divorcesteal.Divorcesteal;
import net.pneumono.divorcesteal.registry.DivorcestealNetworking;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ReviveBeaconInfoS2CPayload(int containerId, NameAndId target, List<NameAndId> revivableParticipants) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ReviveBeaconInfoS2CPayload> ID = new Type<>(Divorcesteal.id("revive_beacon_info"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ReviveBeaconInfoS2CPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ReviveBeaconInfoS2CPayload::containerId,
            DivorcestealNetworking.NAME_AND_ID_CODEC,
            ReviveBeaconInfoS2CPayload::target,
            DivorcestealNetworking.NAME_AND_ID_CODEC.apply(ByteBufCodecs.list()),
            ReviveBeaconInfoS2CPayload::revivableParticipants,
            ReviveBeaconInfoS2CPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
