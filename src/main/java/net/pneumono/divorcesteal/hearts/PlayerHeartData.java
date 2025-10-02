package net.pneumono.divorcesteal.hearts;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Uuids;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class PlayerHeartData {
    public static final Codec<PlayerHeartData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Uuids.INT_STREAM_CODEC.fieldOf("uuid").forGetter(PlayerHeartData::uuid),
            Codecs.PLAYER_NAME.fieldOf("name").forGetter(PlayerHeartData::name),
            Codec.INT.fieldOf("hearts").forGetter(PlayerHeartData::hearts),
            Codec.LONG.optionalFieldOf("banDate").forGetter(data -> data.banDate == null ? Optional.empty() : Optional.of(data.banDate.getTime()))
    ).apply(builder, PlayerHeartData::deserialize));

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static PlayerHeartData deserialize(UUID uuid, String name, int hearts, Optional<Long> banDate) {
        return new PlayerHeartData(uuid, name, hearts, banDate.map(Date::new).orElse(null));
    }

    private final UUID uuid;
    private String name;
    private int hearts;
    private @Nullable Date banDate;

    public PlayerHeartData(UUID uuid, String name, int hearts, @Nullable Date banDate) {
        this.uuid = uuid;
        this.name = name;
        this.hearts = Math.max(hearts, 0);
        this.banDate = banDate;
    }

    public UUID uuid() {
        return uuid;
    }

    public String name() {
        return name;
    }

    public int hearts() {
        return hearts;
    }

    public @Nullable Date banDate() {
        return banDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHearts(int hearts) {
        this.hearts = hearts;
        if (hearts > 0) {
            this.banDate = null;
        } else if (this.banDate == null) {
            this.banDate = new Date();
        }
    }

    public boolean isBanned() {
        return this.hearts == 0;
    }

    public GameProfile gameProfile() {
        return new GameProfile(this.uuid, this.name);
    }
}
