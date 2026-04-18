package net.pneumono.aprilfools.gacha;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

public enum GachaRarity implements StringRepresentable {
    COMMON("common"),
    UNCOMMON("uncommon"),
    RARE("rare"),
    EPIC("epic"),
    LEGENDARY("legendary"),
    MYTHIC("mythic");

    public static final Codec<GachaRarity> CODEC = StringRepresentable.fromEnum(GachaRarity::values);
    public static final StreamCodec<ByteBuf, GachaRarity> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    private final String name;

    GachaRarity(String name) {
        this.name = name;
    }

    @Override
    public @NonNull String getSerializedName() {
        return this.name;
    }

    public String getTranslationKey() {
        return "divorcesteal.gacha.rarity." + name;
    }
}
