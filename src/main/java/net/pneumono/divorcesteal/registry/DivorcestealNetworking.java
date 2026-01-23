package net.pneumono.divorcesteal.registry;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.players.NameAndId;
import net.pneumono.divorcesteal.content.ReviveBeaconInfoS2CPayload;

public class DivorcestealNetworking {
    public static final StreamCodec<RegistryFriendlyByteBuf, NameAndId> NAME_AND_ID_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            NameAndId::id,
            ByteBufCodecs.STRING_UTF8,
            NameAndId::name,
            NameAndId::new
    );

    public static void registerDivorcestealNetworking() {
        PayloadTypeRegistry.playS2C().register(ReviveBeaconInfoS2CPayload.ID, ReviveBeaconInfoS2CPayload.CODEC);
    }
}
