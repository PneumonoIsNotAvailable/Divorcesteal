package net.pneumono.aprilfools.gacha.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.pneumono.divorcesteal.Divorcesteal;
import net.pneumono.aprilfools.gacha.content.GachaResult;
import org.jetbrains.annotations.NotNull;

public record GachaBeaconResultS2CPayload(int containerId, GachaResult result) implements CustomPacketPayload {
    public static final Type<GachaBeaconResultS2CPayload> ID = new Type<>(Divorcesteal.id("gacha_beacon_result"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GachaBeaconResultS2CPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            GachaBeaconResultS2CPayload::containerId,
            GachaResult.STREAM_CODEC,
            GachaBeaconResultS2CPayload::result,
            GachaBeaconResultS2CPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
