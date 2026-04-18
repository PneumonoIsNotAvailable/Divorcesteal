package net.pneumono.aprilfools.gacha.content;

import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.StreamCodec;
import net.pneumono.aprilfools.gacha.GachaRarity;
import org.jspecify.annotations.NonNull;

public class GachaRarityParticleOption implements ParticleOptions {
    private final ParticleType<GachaRarityParticleOption> type;
    private final GachaRarity rarity;

    public static MapCodec<GachaRarityParticleOption> codec(ParticleType<GachaRarityParticleOption> particleType) {
        return GachaRarity.CODEC.xmap(
                rarity -> new GachaRarityParticleOption(particleType, rarity),
                GachaRarityParticleOption::getRarity
        ).fieldOf("rarity");
    }

    public static StreamCodec<? super ByteBuf, GachaRarityParticleOption> streamCodec(ParticleType<GachaRarityParticleOption> particleType) {
        return GachaRarity.STREAM_CODEC.map(
                rarity -> new GachaRarityParticleOption(particleType, rarity),
                GachaRarityParticleOption::getRarity
        );
    }

    public GachaRarityParticleOption(ParticleType<GachaRarityParticleOption> type, GachaRarity rarity) {
        this.type = type;
        this.rarity = rarity;
    }

    @Override
    public @NonNull ParticleType<?> getType() {
        return this.type;
    }

    public GachaRarity getRarity() {
        return rarity;
    }
}
