package net.pneumono.divorcesteal.hearts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Uuids;
import net.minecraft.util.dynamic.Codecs;

import java.util.UUID;

public record PlayerHeartData(UUID uuid, String name, int hearts) {
    public static final Codec<PlayerHeartData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Uuids.INT_STREAM_CODEC.fieldOf("uuid").forGetter(PlayerHeartData::uuid),
            Codecs.PLAYER_NAME.fieldOf("name").forGetter(PlayerHeartData::name),
            Codec.INT.fieldOf("hearts").forGetter(PlayerHeartData::hearts)
    ).apply(builder, PlayerHeartData::new));

    public PlayerHeartData(PlayerEntity player, int hearts) {
        this(player.getGameProfile().getId(), player.getGameProfile().getName(), hearts);
    }
}
