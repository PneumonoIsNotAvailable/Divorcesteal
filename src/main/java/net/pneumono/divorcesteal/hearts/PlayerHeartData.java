package net.pneumono.divorcesteal.hearts;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Uuids;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public record PlayerHeartData(UUID uuid, String name, int hearts, @Nullable Date banDate) {
    public static final Codec<PlayerHeartData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Uuids.INT_STREAM_CODEC.fieldOf("uuid").forGetter(PlayerHeartData::uuid),
            Codecs.PLAYER_NAME.fieldOf("name").forGetter(PlayerHeartData::name),
            Codec.INT.fieldOf("hearts").forGetter(PlayerHeartData::hearts),
            Codec.LONG.optionalFieldOf("banDate").forGetter(data -> data.banDate == null ? Optional.empty() : Optional.of(data.banDate.getTime()))
    ).apply(builder, PlayerHeartData::deserialize));
    public static final PacketCodec<RegistryByteBuf, PlayerHeartData> PACKET_CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC,
            PlayerHeartData::uuid,
            PacketCodecs.string(16),
            PlayerHeartData::name,
            PacketCodecs.VAR_INT,
            PlayerHeartData::hearts,
            PacketCodecs.VAR_LONG.collect(PacketCodecs::optional),
            data -> data.banDate == null ? Optional.empty() : Optional.of(data.banDate.getTime()),
            PlayerHeartData::deserialize
    );

    public PlayerHeartData(UUID uuid, String name, int hearts, Date banDate) {
        this.uuid = uuid;
        this.name = name;
        this.hearts = Math.max(hearts, 0);
        this.banDate = banDate;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static PlayerHeartData deserialize(UUID uuid, String name, int hearts, Optional<Long> banDate) {
        return new PlayerHeartData(uuid, name, hearts, banDate.map(Date::new).orElse(null));
    }

    public PlayerHeartData(PlayerEntity player, int hearts) {
        this(player.getGameProfile().getId(), player.getGameProfile().getName(), hearts, null);
    }

    public GameProfile gameProfile() {
        return new GameProfile(this.uuid, this.name);
    }
}
