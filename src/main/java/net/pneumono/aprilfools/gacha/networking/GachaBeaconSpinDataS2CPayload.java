package net.pneumono.aprilfools.gacha.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.pneumono.divorcesteal.Divorcesteal;
import net.pneumono.aprilfools.gacha.content.GachaResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record GachaBeaconSpinDataS2CPayload(int containerId, List<GachaResult> possibleResults, float randomSpinMultiplier) implements CustomPacketPayload {
    public static final Type<GachaBeaconSpinDataS2CPayload> ID = new Type<>(Divorcesteal.id("gacha_beacon_spin_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GachaBeaconSpinDataS2CPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            GachaBeaconSpinDataS2CPayload::containerId,
            GachaResult.STREAM_CODEC.apply(ByteBufCodecs.list()),
            GachaBeaconSpinDataS2CPayload::possibleResults,
            ByteBufCodecs.FLOAT,
            GachaBeaconSpinDataS2CPayload::randomSpinMultiplier,
            GachaBeaconSpinDataS2CPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
