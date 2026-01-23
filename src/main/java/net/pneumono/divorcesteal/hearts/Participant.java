package net.pneumono.divorcesteal.hearts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.ExtraCodecs;
import net.pneumono.divorcesteal.Divorcesteal;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class Participant {
    public static final Codec<Participant> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            UUIDUtil.CODEC.fieldOf("uuid").forGetter(Participant::getUuid),
            ExtraCodecs.PLAYER_NAME.fieldOf("name").forGetter(Participant::getName),
            Codec.INT.fieldOf("hearts").forGetter(Participant::getHearts),
            Codec.LONG.optionalFieldOf("banDate").forGetter(data -> data.banDate == null ? Optional.empty() : Optional.of(data.banDate.getTime()))
    ).apply(builder, Participant::deserialize));

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static Participant deserialize(UUID uuid, String name, int hearts, Optional<Long> banDate) {
        if (hearts < 0) {
            Divorcesteal.LOGGER.info("Participant {} has {} hearts, setting to 0...", name, hearts);
            hearts = 0;
        }
        Participant data = new Participant(uuid, name, hearts, banDate.map(Date::new).orElse(null));
        data.updateBannedState();
        return data;
    }

    private final UUID uuid;
    private String name;
    private int hearts;
    private @Nullable Date banDate;

    public Participant(UUID uuid, String name, int hearts, @Nullable Date banDate) {
        this.uuid = uuid;
        this.name = name;
        this.hearts = Math.max(hearts, 0);
        this.banDate = banDate;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public int getHearts() {
        return hearts;
    }

    public @Nullable Date getBanDate() {
        return banDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHearts(int hearts) {
        this.hearts = hearts;
        updateBannedState();
    }

    public boolean isBanned() {
        return this.hearts == 0;
    }

    public NameAndId getNameAndId() {
        return new NameAndId(this.uuid, this.name);
    }

    private void updateBannedState() {
        if (hearts > 0) {
            this.banDate = null;
        } else if (this.banDate == null) {
            this.banDate = new Date();
        }
    }
}
